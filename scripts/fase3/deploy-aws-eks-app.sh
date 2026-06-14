#!/usr/bin/env bash
# Deploy da API no EKS (RDS + JWT secret locais em ~/.oficina-fase3/).
set -euo pipefail

export PATH="${HOME}/.local/bin:${PATH}"
AWS_REGION="${AWS_REGION:-sa-east-1}"
CLUSTER_NAME="${CLUSTER_NAME:-oficina-dev}"
ECR_REPO="${ECR_REPO:-381234267424.dkr.ecr.sa-east-1.amazonaws.com/oficina-app}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
SECRETS_DIR="${SECRETS_DIR:-${HOME}/.oficina-fase3}"
APP_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
K8S_AWS="${APP_ROOT}/k8s/aws"
WORK="/tmp/oficina-eks-deploy-$$"
RDS_HOST="${RDS_HOST:-oficina-dev-pg.cdw04ewgopr2.sa-east-1.rds.amazonaws.com}"

cleanup() { rm -rf "$WORK"; }
trap cleanup EXIT

require_file() {
  local f="$1"
  if [ ! -f "$f" ]; then
    echo "Arquivo obrigatorio ausente: $f" >&2
    exit 1
  fi
}

require_file "${SECRETS_DIR}/rds-password.txt"
require_file "${SECRETS_DIR}/jwt-secret.txt"

echo "==> kubeconfig (${CLUSTER_NAME})"
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

echo "==> build imagem"
cd "$APP_ROOT"
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "${ECR_REPO%%/*}"
docker build -t "${ECR_REPO}:${IMAGE_TAG}" .
docker push "${ECR_REPO}:${IMAGE_TAG}"

echo "==> manifests"
mkdir -p "$WORK"
DB_PASS="$(tr -d '\r\n' < "${SECRETS_DIR}/rds-password.txt")"
JWT_SECRET="$(tr -d '\r\n' < "${SECRETS_DIR}/jwt-secret.txt")"

kubectl apply -f "${APP_ROOT}/k8s/namespace.yaml"
kubectl create secret generic oficina-app-secret \
  --namespace oficina \
  --from-literal=DB_URL="jdbc:postgresql://${RDS_HOST}:5432/oficina" \
  --from-literal=DB_USER="oficina" \
  --from-literal=DB_PASS="$DB_PASS" \
  --from-literal=JWT_CPF_SECRET="$JWT_SECRET" \
  --dry-run=client -o yaml | kubectl apply -f -

sed "s|PLACEHOLDER_ECR_IMAGE|${ECR_REPO}:${IMAGE_TAG}|g" \
  "${K8S_AWS}/deployment.yaml" > "${WORK}/deployment.yaml"

kubectl apply -f "${K8S_AWS}/configmap.yaml"
kubectl apply -f "${WORK}/deployment.yaml"
kubectl apply -f "${K8S_AWS}/service-lb.yaml"
kubectl apply -f "${K8S_AWS}/prometheus.yaml"
kubectl apply -f "${APP_ROOT}/k8s/platform/grafana.yaml"

echo "==> aguardando rollout"
kubectl rollout status deployment/oficina-app -n oficina --timeout=600s

echo "==> aguardando LoadBalancer"
LB=""
for _ in $(seq 1 60); do
  LB="$(kubectl get svc oficina-app -n oficina -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)"
  if [ -n "$LB" ]; then
    break
  fi
  sleep 10
done

if [ -n "$LB" ]; then
  kubectl patch configmap oficina-app-config -n oficina --type merge \
    -p "{\"data\":{\"PUBLIC_BASE_URL\":\"http://${LB}/api/public\"}}"
  kubectl rollout restart deployment/oficina-app -n oficina
  kubectl rollout status deployment/oficina-app -n oficina --timeout=600s
  echo "API: http://${LB}/api"
  echo "Health: http://${LB}/api/actuator/health"
  echo "Prometheus (port-forward): kubectl port-forward -n oficina svc/prometheus 9090:9090"
  echo "Grafana (port-forward): kubectl port-forward -n oficina svc/grafana 3000:3000"
else
  echo "AVISO: LoadBalancer ainda sem hostname. Verifique: kubectl get svc -n oficina"
fi

echo "Token CPF: https://i3te2gzkmk.execute-api.sa-east-1.amazonaws.com/token"
