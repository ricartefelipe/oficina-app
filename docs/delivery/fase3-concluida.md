# Fase 3 — Critérios do enunciário atendidos (Tech Challenge)

Consolidação **no repositório oficina-app** em relação ao PDF **13SOAT — Fase 3**.

## Autenticacao e API

| Requisito | Evidencia |
|-----------|-----------|
| Funcao serverless: validar documento, consultar cliente e status, emitir JWT | [`auth-lambda/`](../../auth-lambda/) + [`k8s/auth-lambda.yaml`](../../k8s/auth-lambda.yaml) |
| API Gateway | **Traefik** — [`k8s/platform/traefik.yaml`](../../k8s/platform/traefik.yaml) + [`k8s/ingress.yaml`](../../k8s/ingress.yaml) |

## Repositorios e CI/CD

| Requisito | Evidencia |
|-----------|-----------|
| Quatro repositorios Git com CI/CD | Tabela em [entrega-portal-fase3.md](entrega-portal-fase3.md) |

## Infraestrutura

| Requisito | Evidencia |
|-----------|-----------|
| Terraform Kubernetes | Repositório **oficina-infra-kubernetes-** (Kind) |
| Terraform BD | Repositório **oficina-infra-database** (`enable_rds` opcional); laboratório usa Postgres em [`k8s/postgres.yaml`](../../k8s/postgres.yaml) |
| App no Kubernetes | [`k8s/`](../../k8s/) + [`scripts/fase3/deploy-local-kind.sh`](../../scripts/fase3/deploy-local-kind.sh) |

## Observabilidade

| Requisito | Evidencia |
|-----------|-----------|
| Metricas | [`k8s/platform/prometheus.yaml`](../../k8s/platform/prometheus.yaml), `/actuator/prometheus` |
| Logs JSON + correlacao | Perfil `k8s`, [`CorrelationIdFilter`](../../src/main/java/br/com/oficina/shared/infra/http/CorrelationIdFilter.java) |
| Dashboards | [`k8s/platform/grafana.yaml`](../../k8s/platform/grafana.yaml) |

## Entrega portal

| Requisito | Evidencia |
|-----------|-----------|
| PDF unico | [`entrega-portal-fase3.md`](entrega-portal-fase3.md) → `entrega-portal-fase3.pdf` |
| soat-architecture | Convidado nos quatro repos |

**Verificacao local:** `./scripts/fase3/deploy-local-kind.sh` (Docker + Kind + Terraform).
