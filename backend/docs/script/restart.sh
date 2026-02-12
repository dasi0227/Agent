#!/usr/bin/env bash
set -euo pipefail
set -x

cd ~/Agent
git pull
sync && sleep 5

cd ~/Agent/frontend
npm ci
sync && sleep 5
npm run build
sync && sleep 5

cd ~/Agent/backend/docs/docker
docker compose -f docker-compose.yml up -d --force-recreate
sync && sleep 5

cd ~/Agent/backend/docs/mysql
docker exec -i mysql mysql -u root -pjason2004 ai-agent < data.sql
sync && sleep 5

cd ~/Agent/backend
mvn -DskipTests clean package
sync && sleep 5

JAR="$(ls -1 ai-agent-app/target/*.jar | grep -v '\.original$' | head -n 1)"
if [[ -z "${JAR:-}" ]]; then
  echo "No runnable jar found in ai-agent-app/target" >&2
  exit 1
fi

exec java -jar "$JAR" --server.address=127.0.0.1 --server.port=8066
