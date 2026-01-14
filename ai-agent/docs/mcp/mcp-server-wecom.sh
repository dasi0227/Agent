#!/bin/zsh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../../../mcp-server-wecom" && pwd)"

PIDS="$(lsof -tiTCP:9002 -sTCP:LISTEN 2>/dev/null || true)"
if [[ -n "$PIDS" ]]; then
  kill $PIDS 2>/dev/null || true
  sleep 0.2
  PIDS2="$(lsof -tiTCP:9002 -sTCP:LISTEN 2>/dev/null || true)"
  [[ -n "$PIDS2" ]] && kill -9 $PIDS2 2>/dev/null || true
fi

cd "$PROJECT_DIR"
mvn spring-boot:run