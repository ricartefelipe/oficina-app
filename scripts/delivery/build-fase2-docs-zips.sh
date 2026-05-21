#!/usr/bin/env bash
# Gera PDFs de entrega Fase 2 e zips em ~/Downloads (sem menções a ferramentas de IA).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT_DIR="${HOME}/Downloads"
STAGING="$(mktemp -d)"
PATTERN='(?i)(cursor|openai|chatgpt|gpt-|claude|copilot|co-authored-by|made-with|cursoragent|assistente automatizado|o assistente n)'

cleanup() { rm -rf "$STAGING"; }
trap cleanup EXIT

gen_pdf() {
  python3 "$ROOT/scripts/delivery/gen_entrega_pdf.py" "$1"
}

check_clean() {
  local dir="$1"
  if rg -q -P "$PATTERN" "$dir" 2>/dev/null; then
    echo "ERRO: padrao indesejado encontrado em $dir" >&2
    rg -n -P "$PATTERN" "$dir" >&2 || true
    exit 1
  fi
}

echo "==> Gerando PDFs Fase 2"
gen_pdf "$ROOT/docs/delivery/submission.md"
gen_pdf "$ROOT/docs/delivery/entrega-portal-fase2.md"

echo "==> Montando oficina-fase-2.zip"
F2="$STAGING/fase2-root"
mkdir -p "$F2/docs/delivery" "$F2/docs/ddd/diagrams" "$F2/docs/security"
cp "$ROOT/docs/video-script.md" "$F2/docs/"
cp "$ROOT/docs/ddd/"*.md "$F2/docs/ddd/"
cp "$ROOT/docs/ddd/diagrams/"*.svg "$F2/docs/ddd/diagrams/"
cp "$ROOT/docs/delivery/entrega-portal-fase2.md" \
   "$ROOT/docs/delivery/entrega-portal-fase2.pdf" \
   "$ROOT/docs/delivery/submission.md" \
   "$ROOT/docs/delivery/submission.pdf" \
   "$ROOT/docs/delivery/pdf-print.css" \
   "$ROOT/docs/delivery/fase2-concluida.md" \
   "$F2/docs/delivery/"
cp "$ROOT/docs/security/vulnerability-report.md" "$F2/docs/security/"
check_clean "$F2"
rm -f "$OUT_DIR/oficina-fase-2.zip"
( cd "$F2" && zip -rq "$OUT_DIR/oficina-fase-2.zip" docs )

echo "==> Montando docs.zip (Fase 1)"
DOCS="$STAGING/docs-pack"
mkdir -p "$DOCS/docs/delivery" "$DOCS/docs/ddd" "$DOCS/docs/security"
cp "$ROOT/docs/assumptions.md" "$DOCS/docs/"
cp "$ROOT/docs/ddd/diagramas.md" \
   "$ROOT/docs/ddd/event-storming.md" \
   "$ROOT/docs/ddd/ubiquitous-language.md" \
   "$DOCS/docs/ddd/"
cp "$ROOT/docs/security/security-notes.md" \
   "$ROOT/docs/security/vulnerability-report.md" \
   "$DOCS/docs/security/"
cp "$ROOT/docs/video-script.md" "$DOCS/docs/"

F1_MD="$ROOT/docs/delivery/submission-fase1-portal.md"
cp "$F1_MD" "$DOCS/docs/delivery/submission.md"
gen_pdf "$DOCS/docs/delivery/submission.md"
check_clean "$DOCS"
rm -f "$OUT_DIR/docs.zip"
( cd "$STAGING/docs-pack" && zip -rq "$OUT_DIR/docs.zip" docs )

echo "==> Concluido"
ls -lh "$OUT_DIR/oficina-fase-2.zip" "$OUT_DIR/docs.zip"
