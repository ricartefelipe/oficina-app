#!/usr/bin/env bash
# Empacota a função para AWS Lambda (linux x86_64 alinhado ao runner GitHub Actions / Lambda).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
STAGE="${ROOT}/dist/layer"
OUTZIP="${ROOT}/dist/function.zip"
rm -rf "${ROOT}/dist"
mkdir -p "${STAGE}"
python3 -m pip install -r "${ROOT}/requirements.txt" -t "${STAGE}" --upgrade --no-cache-dir
cp "${ROOT}/lambda_function.py" "${ROOT}/cpf_cnpj.py" "${STAGE}/"
( cd "${STAGE}" && zip -qr "${OUTZIP}" . )
echo "OK: ${OUTZIP}"
ls -la "${OUTZIP}"
