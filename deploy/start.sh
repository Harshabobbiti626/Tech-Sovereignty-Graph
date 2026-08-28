#!/bin/sh
set -eu

# HF injects PORT (7860 by default); locally override with -e PORT=8090 -p 8090:8090
sed -i "s/__PORT__/${PORT:-7860}/" /app/nginx.conf

# The JVM supervises itself: if it ever dies (OOM, crash), the loop restarts
# it in 3s. nginx is the container's foreground process, so a platform-level
# restart heals everything at once.
(
  while true; do
    java -Dserver.port=8080 -jar /app/app.jar || echo "[start] JVM exited ($?), restarting in 3s"
    sleep 3
  done
) &

exec nginx -c /app/nginx.conf -g 'daemon off;'
