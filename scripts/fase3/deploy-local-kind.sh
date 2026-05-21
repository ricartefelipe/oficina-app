#!/usr/bin/env bash
# Deploy Fase 3 — stack local Kind + Traefik + Postgres + auth + app + observabilidade.
# Custo AWS: zero. Requisitos: Docker, kind, kubectl, terraform, mvn.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
K8S_REPO="${K8S_REPO:-$ROOT/../oficina-infra-kubernetes-}"
CLUSTER_NAME="${CLUSTER_NAME:-oficina-local}"
GATEWAY_PORT="${GATEWAY_PORT:-8088}"
export PATH="${HOME}/.local/bin:${PATH}"

log() { printf '==> %s\n' "$*"; }

require() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Comando obrigatorio ausente: $1" >&2
    exit 1
  }
}

require docker
require kind
require kubectl
require terraform
require mvn

if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon nao esta rodando. Inicie o Docker e execute novamente." >&2
  exit 1
fi

log "Provisionando cluster Kind (Terraform) em ${K8S_REPO}"
pushd "$K8S_REPO" >/dev/null
terraform init -input=false
terraform apply -auto-approve -input=false
terraform output -raw kubeconfig > /tmp/oficina-kind.kubeconfig
popd >/dev/null

export KUBECONFIG=/tmp/oficina-kind.kubeconfig

log "Build JAR e imagens locais"
mvn -q -DskipTests package -f "$ROOT/pom.xml"
mkdir -p "$ROOT/docker-dist"
cp "$ROOT"/target/oficina-service-*.jar "$ROOT/docker-dist/app.jar"
docker build -t oficina-app:local -f "$ROOT/Dockerfile.kind" "$ROOT"
docker build -t oficina-auth:local "$ROOT/auth-lambda"

log "Carregando imagens no Kind"
kind load docker-image oficina-app:local --name "$CLUSTER_NAME"
kind load docker-image oficina-auth:local --name "$CLUSTER_NAME"

log "Aplicando manifestos Kubernetes"
kubectl apply -f "$ROOT/k8s/namespace.yaml"
cp "$ROOT/k8s/secret.example.yaml" /tmp/oficina-secret.yaml
kubectl apply -f /tmp/oficina-secret.yaml
kubectl apply -f "$ROOT/k8s/configmap.yaml"
kubectl apply -f "$ROOT/k8s/platform/traefik.yaml"
kubectl apply -f "$ROOT/k8s/postgres.yaml"
kubectl apply -f "$ROOT/k8s/auth-lambda.yaml"
kubectl apply -f "$ROOT/k8s/deployment.yaml"
kubectl apply -f "$ROOT/k8s/service.yaml"
kubectl apply -f "$ROOT/k8s/platform/prometheus.yaml"
kubectl apply -f "$ROOT/k8s/platform/grafana.yaml"
kubectl apply -f "$ROOT/k8s/ingress.yaml"

log "Aguardando pods"
kubectl -n oficina wait --for=condition=ready pod -l app=postgres --timeout=180s
kubectl -n oficina wait --for=condition=ready pod -l app=oficina-auth --timeout=180s
kubectl -n kube-system wait --for=condition=ready pod -l app=traefik --timeout=120s
kubectl -n oficina wait --for=condition=ready pod -l app=oficina-app --timeout=360s

BASE="http://127.0.0.1:${GATEWAY_PORT}"

log "Smoke — autenticacao CPF"
TOKEN_JSON="$(curl -fsS -X POST "${BASE}/token" \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"52998224725"}')"
echo "$TOKEN_JSON" | grep -q access_token

TOKEN="$(python3 - <<'PY' "$TOKEN_JSON"
import json, sys
print(json.loads(sys.argv[1])["access_token"])
PY
)"

log "Smoke — API protegida"
curl -fsS -H "Authorization: Bearer ${TOKEN}" "${BASE}/api/cliente/sessao" | grep -q cliente

log "Smoke — Prometheus"
kubectl -n oficina port-forward svc/prometheus 19090:9090 >/tmp/pf-prom.log 2>&1 &
PF1=$!
sleep 2
curl -fsS "http://127.0.0.1:19090/-/ready" >/dev/null
kill "$PF1" 2>/dev/null || true

cat <<EOF

Fase 3 local pronta.

Gateway (Traefik): ${BASE}
Auth token:        POST ${BASE}/token  body {"cpf":"52998224725"}
API protegida:     GET  ${BASE}/api/cliente/sessao  (Bearer JWT)
Swagger:           ${BASE}/api/swagger-ui/index.html
Prometheus:        kubectl -n oficina port-forward svc/prometheus 9090:9090
Grafana:           kubectl -n oficina port-forward svc/grafana 3000:3000  (admin/admin)

EOF
