#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "==> Oficina: docker compose up --build"
docker compose up -d --build

echo "==> Aguardando app"
for i in $(seq 1 60); do
  if curl -fsS "http://localhost:8080/actuator/health" >/dev/null 2>&1 \
    || curl -fsS "http://localhost:8080/q/health" >/dev/null 2>&1 \
    || curl -fsS "http://localhost:8080/" >/dev/null 2>&1; then
    echo "OK — stack local no ar (Keycloak/MailHog conforme compose)"
    docker compose ps
    exit 0
  fi
  sleep 5
done

echo "Timeout. Logs:" >&2
docker compose logs --tail=60
exit 1
