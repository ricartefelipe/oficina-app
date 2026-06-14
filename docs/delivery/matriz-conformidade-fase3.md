# Matriz de conformidade — 13SOAT Fase 3

Referência: enunciado **Tech Challenge — Fase 3**. Legenda: **Atende** | **Parcial** | **Substituto** (evidência alternativa documentada).

| Requisito (enunciado) | Status | Evidência |
|------------------------|--------|-----------|
| API Gateway (AWS/Kong/Traefik/etc.) | Atende | API Gateway HTTP (Lambda) em sa-east-1; Traefik apenas no Kind (lab) |
| Rotas sensíveis com auth CPF | Atende | JWT CPF + `SecurityConfig`; `POST /token` |
| Function serverless: validar CPF | Atende | Lambda `oficina-auth-cpf-fn` + repo `oficina-auth-lambda` |
| Consultar cliente e status na BD | Atende | Liquibase `0005`; query em `lambda_function.py` |
| Emitir JWT para APIs protegidas | Atende | HS256; `GET /api/cliente/sessao` com Bearer |
| 4 repositórios Git separados | Atende | auth-lambda, infra-k8s, infra-database, oficina-app |
| CI/CD em cada repositório | Atende | GitHub Actions (links no PDF portal) |
| Deploy automático para nuvem | Atende | Terraform RDS + Lambda + EKS; script `deploy-aws-eks-app.sh` |
| Branch main protegida + PR | Parcial | Política configurável; integração via `develop` |
| Deploy auto homologação/produção | Parcial | Workflow `deploy-k8s-branch.yml`; branches `hml`/`prd` sob demanda |
| BD gerenciado + Terraform | Atende | RDS PostgreSQL sa-east-1 — repo `oficina-infra-database` |
| Cluster K8s escalável + Terraform | Atende | EKS `oficina-dev` — `oficina-infra-kubernetes-/aws-eks/`; HPA em `k8s/hpa.yaml` |
| Observabilidade (APM livre) | Atende | Prometheus + Grafana no EKS (equivalente Datadog/New Relic) |
| Latência APIs, CPU/mem K8s, health | Atende | Actuator, métricas HTTP, probes |
| Logs JSON + correlação | Atende | Perfil `k8s`, `CorrelationIdFilter` |
| Dashboards OS / tempo por status | Atende | PromQL em `observabilidade-prometheus.md`; Grafana |
| Alertas falhas OS | Atende | Regra Prometheus `OficinaAppDown` — `k8s/aws/prometheus.yaml` |
| Serverless notificações | Parcial | Notificações por e-mail na app (não serverless) |
| Diagrama componentes | Atende | `docs/fase3/visao-arquitetura-fase3.md` |
| Diagrama sequência auth + OS | Atende | `docs/fase3/diagrama-sequencia-auth-os.md` |
| RFCs e ADRs | Atende | `docs/fase3/rfc/`, `docs/adr/` |
| Justificativa BD + ER | Atende | `docs/fase3/justificativa-banco-dados.md` + DDD |
| README em cada repo | Atende | README em cada repositório GitHub |
| Dockerfiles | Atende | App, auth-lambda |
| Vídeo ≤ 15 min | Parcial | Link a inserir no PDF portal (sec. 4) |
| PDF portal + 4 links + soat-architecture | Atende | `entrega-portal-fase3.pdf` |

**Verificação executável (AWS):** `./scripts/fase3/deploy-aws-eks-app.sh`  
**Verificação executável (local):** `./scripts/fase3/deploy-local-kind.sh`
