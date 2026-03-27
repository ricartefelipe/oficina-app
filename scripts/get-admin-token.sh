#!/usr/bin/env bash
set -euo pipefail

KC_URL="${KC_URL:-http://localhost:8180}"
KC_REALM="${KC_REALM:-oficina}"
KC_CLIENT_ID="${KC_CLIENT_ID:-oficina-api}"
KC_USERNAME="${KC_USERNAME:-admin}"
KC_PASSWORD="${KC_PASSWORD:-admin}"

TOKEN_ENDPOINT="${KC_URL}/realms/${KC_REALM}/protocol/openid-connect/token"

response="$(curl -sS -X POST "${TOKEN_ENDPOINT}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=${KC_CLIENT_ID}" \
  -d "username=${KC_USERNAME}" \
  -d "password=${KC_PASSWORD}")"

if command -v jq >/dev/null 2>&1; then
  echo "${response}" | jq -r '.access_token'
  exit 0
fi

if command -v python3 >/dev/null 2>&1; then
  echo "${response}" | python3 -c 'import sys,json; print(json.load(sys.stdin)["access_token"])'
  exit 0
fi

echo "Nao foi possivel extrair access_token automaticamente (jq/python3 ausentes)." >&2
echo "Resposta completa do Keycloak:" >&2
echo "${response}" >&2
exit 1
