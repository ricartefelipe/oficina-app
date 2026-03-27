#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT_DIR="${ROOT_DIR}/build/security"

# You can override these images if your environment requires it.
DC_IMAGE="${DC_IMAGE:-owasp/dependency-check:latest}"
TRIVY_IMAGE="${TRIVY_IMAGE:-aquasec/trivy:0.56.2}"

# Severity levels used by Trivy (comma-separated)
TRIVY_SEVERITY="${TRIVY_SEVERITY:-CRITICAL,HIGH,MEDIUM,LOW,UNKNOWN}"

mkdir -p "${OUT_DIR}"
mkdir -p "${OUT_DIR}/dependency-check"

log() {
  printf '%s\n' "[$(date +'%Y-%m-%d %H:%M:%S')] $*"
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "ERROR: '$1' not found. Install it and retry." >&2
    exit 1
  fi
}

require_cmd docker

log "Security scans will be written to: ${OUT_DIR}"

############################################################
# 1) Dependency scan (OWASP Dependency-Check)
############################################################
log "[1/4] Running OWASP Dependency-Check (deps scan)..."
log "Using image: ${DC_IMAGE}"

# Optional: NVD API key to speed up updates / avoid rate-limits
# export NVD_API_KEY="..."

# Cache CVE database across runs with a named volume
DC_DATA_VOLUME="oficina_dependency_check_data"

set +e

docker run --rm \
  -v "${ROOT_DIR}:/src" \
  -v "${DC_DATA_VOLUME}:/usr/share/dependency-check/data" \
  -v "${OUT_DIR}/dependency-check:/report" \
  -e "NVD_API_KEY=${NVD_API_KEY:-}" \
  "${DC_IMAGE}" \
  --scan /src \
  --out /report \
  --project "oficina-service" \
  --format "ALL" \
  --exclude "/src/target" \
  --exclude "/src/.git" \
  --exclude "/src/build" \
  --enableExperimental

DC_EXIT=$?
set -e

if [ "${DC_EXIT}" -ne 0 ]; then
  log "WARNING: Dependency-Check exited with code ${DC_EXIT}."
  log "This commonly happens when the CVE database cannot be updated (network/rate-limit)."
  log "You can retry with an NVD API key: https://nvd.nist.gov/developers/request-an-api-key"
fi

log "Dependency-Check artifacts:" 
log "- ${OUT_DIR}/dependency-check (HTML/JSON/XML reports)"

############################################################
# 2) Build local container image to scan
############################################################
log "[2/4] Building application image (local) for image scan..."
log "docker build -t oficina-service:local ."

docker build -t oficina-service:local "${ROOT_DIR}"

############################################################
# 3) Filesystem scan (repo)
############################################################
log "[3/4] Running Trivy filesystem scan (repo scan)..."
log "Using image: ${TRIVY_IMAGE}"

# Trivy keeps its DB in the container; cache it in a named volume
TRIVY_CACHE_VOLUME="oficina_trivy_cache"

# Text report

docker run --rm \
  -v "${ROOT_DIR}:/workspace" \
  -v "${TRIVY_CACHE_VOLUME}:/root/.cache" \
  -w /workspace \
  "${TRIVY_IMAGE}" \
  fs \
  --scanners vuln,secret,config \
  --severity "${TRIVY_SEVERITY}" \
  --ignore-unfixed \
  --skip-dirs target \
  --skip-dirs .git \
  --skip-dirs build \
  --no-progress \
  --format table \
  --output "${OUT_DIR}/trivy-fs.txt" \
  .

# JSON report

docker run --rm \
  -v "${ROOT_DIR}:/workspace" \
  -v "${TRIVY_CACHE_VOLUME}:/root/.cache" \
  -w /workspace \
  "${TRIVY_IMAGE}" \
  fs \
  --scanners vuln,secret,config \
  --severity "${TRIVY_SEVERITY}" \
  --ignore-unfixed \
  --skip-dirs target \
  --skip-dirs .git \
  --skip-dirs build \
  --no-progress \
  --format json \
  --output "${OUT_DIR}/trivy-fs.json" \
  .

############################################################
# 4) Image scan (built image)
############################################################
log "[4/4] Running Trivy image scan (container image scan)..."

# Text report

docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "${TRIVY_CACHE_VOLUME}:/root/.cache" \
  "${TRIVY_IMAGE}" \
  image \
  --severity "${TRIVY_SEVERITY}" \
  --ignore-unfixed \
  --no-progress \
  --format table \
  --output "${OUT_DIR}/trivy-image.txt" \
  oficina-service:local

# JSON report

docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "${TRIVY_CACHE_VOLUME}:/root/.cache" \
  "${TRIVY_IMAGE}" \
  image \
  --severity "${TRIVY_SEVERITY}" \
  --ignore-unfixed \
  --no-progress \
  --format json \
  --output "${OUT_DIR}/trivy-image.json" \
  oficina-service:local

log "Done. Review outputs in: ${OUT_DIR}"
log "Next: update docs/security/vulnerability-report.md with the findings summary."
