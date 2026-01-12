#!/bin/zsh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../../../mcp-server-wecom" && pwd)"

cd "$PROJECT_DIR"

mvn spring-boot:run