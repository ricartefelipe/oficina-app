#!/usr/bin/env bash
# Executar no AWS CloudShell (conta admin) OU apos anexar IAMFullAccess ao oficina-fase3-deploy.
set -euo pipefail
ROLE=oficina-auth-cpf-lambda-role
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
aws iam create-role --role-name "$ROLE" \
  --assume-role-policy-document "file://${SCRIPT_DIR}/lambda-trust-policy.json" 2>/dev/null || true
aws iam attach-role-policy --role-name "$ROLE" \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole
aws iam attach-role-policy --role-name "$ROLE" \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole
aws iam get-role --role-name "$ROLE" --query Role.Arn --output text
