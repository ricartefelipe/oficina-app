#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "==> Oficina: docker compose up --build"
docker compose up -d --build

echo "==> Aguardando app em :8080"
for i in $(seq 1 90); do
  code="$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health || true)"
  if [[ "$code" =~ ^(2|3|401|403) ]]; then
    echo "OK — oficina responde HTTP $code (Keycloak em :8180, MailHog :8025)"
    docker compose ps
    exit 0
  fi
  code2="$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/ || true)"
  if [[ "$code2" =~ ^[1-5][0-9][0-9]$ ]]; then
    echo "OK — oficina responde HTTP $code2"
    docker compose ps
    exit 0
  fi
  sleep 5
done

echo "Timeout. Logs:" >&2
docker compose logs --tail=80
exit 1
