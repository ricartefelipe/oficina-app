# Kubernetes — aplicação Oficina (Fase 3)

Stack completa para **Kind local** (custo zero): Postgres, auth CPF, app Spring, **Traefik** (gateway), Prometheus e Grafana.

## Deploy automatizado

```bash
./scripts/fase3/deploy-local-kind.sh
```

Pré-requisitos: Docker (rodando), Kind, kubectl, Terraform, Maven.  
Gateway: http://localhost:8088

## Manifestos

| Arquivo | Conteúdo |
|---------|----------|
| `namespace.yaml` | Namespace `oficina` |
| `secret.example.yaml` | Copiar para secret antes do apply (valores de laboratório) |
| `configmap.yaml` | App + JWT CPF |
| `postgres.yaml` | PostgreSQL 16 |
| `auth-lambda.yaml` | Auth HTTP (código da Lambda) |
| `deployment.yaml` / `service.yaml` | API Spring Boot |
| `ingress.yaml` | Traefik — `/token` e `/api` |
| `platform/traefik.yaml` | API Gateway (Traefik) |
| `platform/prometheus.yaml` | Métricas |
| `platform/grafana.yaml` | Dashboards |
| `hpa.yaml` | HPA (requer metrics-server) |

## Ordem manual (sem script)

```bash
kubectl apply -f k8s/namespace.yaml
cp k8s/secret.example.yaml /tmp/secret.yaml && kubectl apply -f /tmp/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/platform/traefik.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/auth-lambda.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/platform/prometheus.yaml
kubectl apply -f k8s/platform/grafana.yaml
kubectl apply -f k8s/ingress.yaml
```

Imagens locais: `oficina-app:local`, `oficina-auth:local` (build + `kind load docker-image`).

## Fluxo de teste

```bash
curl -s -X POST http://localhost:8088/token \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"52998224725"}'

curl -s http://localhost:8088/api/cliente/sessao \
  -H "Authorization: Bearer <access_token>"
```

Cliente demo: CPF `52998224725` (Liquibase `0006-seed-cliente-demo-fase3`).
