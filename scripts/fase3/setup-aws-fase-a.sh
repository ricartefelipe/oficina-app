#!/usr/bin/env bash
# Fase A — preparacao AWS local (nao grava credenciais; so verifica ambiente).
set -euo pipefail

export PATH="${HOME}/.local/bin:${PATH}"

echo "==> AWS CLI"
if ! command -v aws >/dev/null 2>&1; then
  echo "AWS CLI nao encontrado. Instale em ~/.local/bin:"
  echo "  curl -fsSL https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip -o /tmp/awscliv2.zip"
  echo "  unzip -qo /tmp/awscliv2.zip -d /tmp && /tmp/aws/install -i \$HOME/.local/aws-cli -b \$HOME/.local/bin"
  exit 1
fi
aws --version

echo "==> Credenciais"
if aws sts get-caller-identity; then
  echo "OK: autenticado"
else
  echo "Configure antes de continuar:"
  echo "  aws configure"
  echo "  Regiao sugerida: sa-east-1"
  exit 1
fi

echo "==> Terraform"
command -v terraform >/dev/null && terraform version | head -1 || echo "AVISO: terraform nao instalado (apt install terraform ou tfenv)"

echo "==> tfvars database repo"
DB_REPO="$(cd "$(dirname "$0")/../../../oficina-infra-database" 2>/dev/null && pwd || true)"
if [ -n "$DB_REPO" ] && [ -d "$DB_REPO" ]; then
  if [ ! -f "$DB_REPO/terraform.tfvars" ]; then
    echo "Crie: cp $DB_REPO/terraform.tfvars.example $DB_REPO/terraform.tfvars"
  else
    echo "OK: $DB_REPO/terraform.tfvars existe (nao commitar)"
  fi
fi

echo "==> Fase A pronta para Fase B (terraform plan/apply RDS)"
