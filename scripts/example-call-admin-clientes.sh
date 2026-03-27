#!/usr/bin/env bash
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080/api}"

token="$("$(dirname "$0")/get-admin-token.sh")"

curl -sS "${API_URL}/admin/clientes" \
  -H "Authorization: Bearer ${token}" \
  -H "Accept: application/json" | sed -n '1,200p'
