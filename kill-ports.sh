#!/bin/bash

PORTS=(5432 8090 4317 4318 8889 9090 3100 16686 14250 3000)

echo "Checking all project ports..."
echo "─────────────────────────────"

for PORT in "${PORTS[@]}"; do
  PID=$(lsof -ti:$PORT 2>/dev/null)
  if [ -n "$PID" ]; then
    echo "Port $PORT → occupied by PID $PID — killing..."
    kill -9 $PID 2>/dev/null
    echo "Port $PORT → freed ✓"
  else
    echo "Port $PORT → free ✓"
  fi
done

echo "─────────────────────────────"
echo "Done! Run: docker compose up -d"
