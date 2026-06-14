#!/usr/bin/env bash
# Pacote zipado da Fase 3 — estrutura em camadas para arquivo/backup.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WKS="$(cd "$ROOT/.." && pwd)"
STAGING="${STAGING:-/tmp/oficina-fase3-entrega}"
OUT_ZIP="${OUT_ZIP:-$HOME/Downloads/oficina-tech-challenge-fase3-entrega.zip}"
export PATH="${HOME}/.local/bin:${PATH}"

log() { printf '==> %s\n' "$*"; }

rm -rf "$STAGING"
mkdir -p "$STAGING"

log "Gerando PDF"
python3 "$ROOT/scripts/delivery/gen_entrega_pdf.py"

log "Montando estrutura em camadas"
mkdir -p "$STAGING/00-entrega"
mkdir -p "$STAGING/01-documentacao-arquitetura"
mkdir -p "$STAGING/02-camada-aplicacao"
mkdir -p "$STAGING/03-camada-autenticacao-serverless"
mkdir -p "$STAGING/04-camada-infraestrutura/kubernetes"
mkdir -p "$STAGING/04-camada-infraestrutura/banco-dados"

cp "$ROOT/docs/delivery/entrega-portal-fase3.pdf" "$STAGING/00-entrega/"
cp "$ROOT/docs/delivery/matriz-conformidade-fase3.md" "$STAGING/00-entrega/"
[ -f "$HOME/Downloads/13SOAT - Fase 3 - Tech Challenge.pdf" ] && \
  cp "$HOME/Downloads/13SOAT - Fase 3 - Tech Challenge.pdf" "$STAGING/00-entrega/enunciado-fase3.pdf" || true

cp -r "$ROOT/docs/fase3" "$STAGING/01-documentacao-arquitetura/"
cp -r "$ROOT/docs/adr" "$STAGING/01-documentacao-arquitetura/"
cp -r "$ROOT/docs/ddd" "$STAGING/01-documentacao-arquitetura/"
cp "$ROOT/docs/delivery/fase3-concluida.md" "$STAGING/01-documentacao-arquitetura/"

export_repo() {
  local repo="$1"
  local dest="$2"
  local path="$WKS/$repo"
  mkdir -p "$dest"
  git -C "$path" archive origin/main | tar -x -C "$dest"
}

export_repo oficina-app "$STAGING/02-camada-aplicacao/oficina-app"
export_repo oficina-auth-lambda "$STAGING/03-camada-autenticacao-serverless/oficina-auth-lambda"
export_repo oficina-infra-kubernetes- "$STAGING/04-camada-infraestrutura/kubernetes/oficina-infra-kubernetes-"
export_repo oficina-infra-database "$STAGING/04-camada-infraestrutura/banco-dados/oficina-infra-database"

cat > "$STAGING/LEIA-ME.md" <<'EOF'
# Oficina Turbo — Tech Challenge Fase 3 (pacote zipado)

**Grupo 106** — Felipe Ricarte Magalhães

## Estrutura (camadas)

| Pasta | Conteúdo |
|-------|----------|
| `00-entrega/` | PDF portal, matriz de conformidade, enunciado |
| `01-documentacao-arquitetura/` | Fase 3, ADR, RFC, DDD, checklist |
| `02-camada-aplicacao/` | Repositório `oficina-app` (Spring Boot + K8s) |
| `03-camada-autenticacao-serverless/` | Repositório `oficina-auth-lambda` |
| `04-camada-infraestrutura/` | Terraform Kind + Terraform BD |

## Repositórios Git (fonte de verdade)

1. https://github.com/ricartefelipe/oficina-auth-lambda
2. https://github.com/ricartefelipe/oficina-infra-database
3. https://github.com/ricartefelipe/oficina-infra-kubernetes-
4. https://github.com/ricartefelipe/oficina-app

Branches `main` e `develop` sincronizadas. Avaliador: **soat-architecture**.

## Demonstração local

```bash
cd 02-camada-aplicacao/oficina-app
./scripts/fase3/deploy-local-kind.sh
```

Gateway: http://localhost:8088
EOF

log "Verificando ausencia de rastros indesejados no pacote"
if grep -rIilE 'cursoragent|Co-authored-by: Cursor|Made-with: Cursor|ChatGPT|Copilot|Claude Code' "$STAGING" 2>/dev/null; then
  echo "ERRO: possivel rastro encontrado nos arquivos acima" >&2
  exit 1
fi

log "Criando zip: $OUT_ZIP"
rm -f "$OUT_ZIP"
( cd "$STAGING/.." && zip -qr "$OUT_ZIP" "$(basename "$STAGING")" )

log "Concluido: $OUT_ZIP ($(du -h "$OUT_ZIP" | cut -f1))"
