package com.danyastry.streaming.core.player.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import com.danyastry.streaming.core.player.logic.PlaybackStatsReducer
import com.danyastry.streaming.core.player.model.PlaybackStats
import com.danyastry.streaming.core.player.model.PlayerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@OptIn(UnstableApi::class)
class ExoPlaybackEngine(context: Context) : PlaybackEngine {

    private val context = context.applicationContext

    private val _player = MutableStateFlow<ExoPlayer?>(null)
    override val player: StateFlow<Player?> = _player.asStateFlow()

    private val _stats = MutableStateFlow(PlaybackStats())
    override val stats: StateFlow<PlaybackStats> = _stats.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    private val progress = object : Runnable {
        override fun run() {
            _player.value?.let { emit(PlayerEvent.Progress(it.currentPosition, it.bufferedPosition)) }
            handler.postDelayed(this, PROGRESS_INTERVAL_MILLIS)
        }
    }

    private val listener = object : AnalyticsListener {
        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            emit(PlayerEvent.PhaseChanged(MediaEvents.phase(state)))
        }

        override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
            emit(PlayerEvent.PlayingChanged(isPlaying))
        }

        override fun onPositionDiscontinuity(
            eventTime: AnalyticsListener.EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            if (reason == Player.DISCONTINUITY_REASON_SEEK) emit(PlayerEvent.SeekStarted)
        }

        override fun onLoadStarted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            retryCount: Int,
        ) {
            val format = mediaLoadData.trackFormat ?: return
            if (mediaLoadData.dataType != C.DATA_TYPE_MEDIA || !MediaEvents.isVideo(mediaLoadData.trackType, format)) return
            emit(
                PlayerEvent.LoadingRenditionChanged(
                    rendition = MediaEvents.rendition(format),
                    reason = MediaEvents.reason(mediaLoadData.trackSelectionReason),
                    positionMillis = eventTime.currentPlaybackPositionMs,
                    mediaMillis = mediaLoadData.mediaStartTimeMs.coerceAtLeast(0),
                ),
            )
        }

        override fun onVideoInputFormatChanged(
            eventTime: AnalyticsListener.EventTime,
            format: Format,
            decoderReuseEvaluation: DecoderReuseEvaluation?,
        ) {
            emit(PlayerEvent.PlayingRenditionChanged(MediaEvents.rendition(format)))
        }

        override fun onLoadCompleted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
        ) {
            if (mediaLoadData.dataType == C.DATA_TYPE_MEDIA) emit(PlayerEvent.SegmentLoaded(loadEventInfo.bytesLoaded))
        }

        override fun onBandwidthEstimate(
            eventTime: AnalyticsListener.EventTime,
            totalLoadTimeMs: Int,
            totalBytesLoaded: Long,
            bitrateEstimate: Long,
        ) {
            emit(PlayerEvent.BandwidthEstimated(bitrateEstimate))
        }

        override fun onDroppedVideoFrames(eventTime: AnalyticsListener.EventTime, droppedFrames: Int, elapsedMs: Long) {
            emit(PlayerEvent.FramesDropped(droppedFrames))
        }

        override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
            emit(PlayerEvent.Failed("${error.errorCodeName}: ${error.cause?.message ?: error.message}"))
        }
    }

    init {
        handler.post(progress)
    }

    override fun play(url: String, mimeType: String) {
        releasePlayer()
        emit(PlayerEvent.Started)
        val player = ExoPlayer.Builder(context)
            .setBandwidthMeter(DefaultBandwidthMeter.Builder(context).build())
            .build()
        player.addAnalyticsListener(listener)
        player.setMediaItem(MediaItem.Builder().setUri(url).setMimeType(mimeType).build())
        player.prepare()
        player.play()
        _player.value = player
    }

    override fun pause() {
        _player.value?.pause()
    }

    override fun release() {
        handler.removeCallbacks(progress)
        releasePlayer()
    }

    private fun releasePlayer() {
        val player = _player.value ?: return
        _player.value = null
        player.removeAnalyticsListener(listener)
        player.release()
    }

    private fun emit(event: PlayerEvent) {
        val now = SystemClock.elapsedRealtime()
        _stats.update { PlaybackStatsReducer.reduce(it, event, now) }
    }

    private companion object {
        const val PROGRESS_INTERVAL_MILLIS = 250L
    }
}
