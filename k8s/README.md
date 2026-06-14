# Kubernetes — aplicação Oficina (Fase 3)

## Deploy AWS (entrega principal)

Cluster **EKS** `oficina-dev` (`sa-east-1`), app conectada ao **RDS**, auth via **Lambda + API Gateway**.

```bash
./scripts/fase3/deploy-aws-eks-app.sh
```

Manifestos: **`k8s/aws/`** (deployment, LoadBalancer, Prometheus, Grafana).  
URLs publicas: [`docs/delivery/entrega-portal-fase3.md`](../docs/delivery/entrega-portal-fase3.md).

---

## Laboratorio local (Kind)

Stack Kind (custo zero): Postgres in-cluster, auth HTTP, Traefik, Prometheus, Grafana.

```bash
./scripts/fase3/deploy-local-kind.sh
```

Gateway: http://localhost:8088

### Manifestos Kind

| Arquivo | Conteúdo |
|---------|----------|
| `namespace.yaml` | Namespace `oficina` |
| `secret.example.yaml` | Copiar para secret antes do apply |
| `configmap.yaml` | App + JWT CPF |
| `postgres.yaml` | PostgreSQL 16 |
| `auth-lambda.yaml` | Auth HTTP (código da Lambda) |
| `deployment.yaml` / `service.yaml` | API Spring Boot |
| `ingress.yaml` | Traefik — `/token` e `/api` |
| `platform/traefik.yaml` | API Gateway (Traefik) |
| `platform/prometheus.yaml` | Métricas |
| `platform/grafana.yaml` | Dashboards |
| `hpa.yaml` | HPA (requer metrics-server) |

### Fluxo de teste (Kind)

```bash
curl -s -X POST http://localhost:8088/token \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"52998224725"}'

curl -s http://localhost:8088/api/cliente/sessao \
  -H "Authorization: Bearer <access_token>"
```

Cliente demo: CPF `52998224725` (Liquibase `0006-seed-cliente-demo-fase3`).
