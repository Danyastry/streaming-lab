#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/core/streams/src/main/assets/vod"
DURATION=20
FPS=24
GOP=$((FPS * 2))

rm -rf "$OUT"
mkdir -p "$OUT"

ffmpeg -hide_banner -loglevel error -y \
  -f lavfi -i "testsrc2=size=1280x720:rate=${FPS}:duration=${DURATION}" \
  -f lavfi -i "sine=frequency=440:sample_rate=48000:duration=${DURATION}" \
  -filter_complex "\
[0:v]split=4[s0][s1][s2][s3];\
[s0]scale=384:216,setsar=1,drawbox=x=0:y=0:w=36:h=36:color=red:t=fill[v0];\
[s1]scale=640:360,setsar=1,drawbox=x=0:y=0:w=60:h=60:color=orange:t=fill[v1];\
[s2]scale=960:540,setsar=1,drawbox=x=0:y=0:w=90:h=90:color=yellow:t=fill[v2];\
[s3]scale=1280:720,setsar=1,drawbox=x=0:y=0:w=120:h=120:color=green:t=fill[v3]" \
  -map "[v0]" -map "[v1]" -map "[v2]" -map "[v3]" -map 1:a \
  -c:v libx264 -preset medium -profile:v main -pix_fmt yuv420p -threads 1 \
  -g "$GOP" -keyint_min "$GOP" -sc_threshold 0 \
  -b:v:0 200k -maxrate:v:0 220k -bufsize:v:0 400k \
  -b:v:1 500k -maxrate:v:1 550k -bufsize:v:1 1000k \
  -b:v:2 1000k -maxrate:v:2 1100k -bufsize:v:2 2000k \
  -b:v:3 2000k -maxrate:v:3 2200k -bufsize:v:3 4000k \
  -c:a aac -b:a 64k -ac 2 \
  -fflags +bitexact -flags:v +bitexact -flags:a +bitexact \
  -f dash -seg_duration 2 -use_template 1 -use_timeline 0 -hls_playlist 1 \
  -adaptation_sets "id=0,streams=v id=1,streams=a" \
  -init_seg_name 'init-$RepresentationID$.m4s' \
  -media_seg_name 'chunk-$RepresentationID$-$Number%05d$.m4s' \
  "$OUT/manifest.mpd"

for playlist in "$OUT"/media_*.m3u8; do
  sed -i '' -e '/^#EXT-X-PROGRAM-DATE-TIME/d' -e 's/^#EXT-X-MEDIA-SEQUENCE:1$/#EXT-X-PLAYLIST-TYPE:VOD\
#EXT-X-MEDIA-SEQUENCE:1/' "$playlist"
  grep -q '^#EXT-X-ENDLIST' "$playlist" || echo '#EXT-X-ENDLIST' >> "$playlist"
done

echo "Wrote $(ls "$OUT" | wc -l | tr -d ' ') files, $(du -sh "$OUT" | cut -f1) to $OUT"
