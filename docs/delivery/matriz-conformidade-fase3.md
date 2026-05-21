# Matriz de conformidade — 13SOAT Fase 3

Referência: enunciado **Tech Challenge — Fase 3**. Legenda: **Atende** | **Parcial** | **Substituto** (evidência alternativa documentada).

| Requisito (enunciado) | Status | Evidência |
|------------------------|--------|-----------|
| API Gateway (AWS/Kong/Traefik/etc.) | Atende | Traefik — `k8s/platform/traefik.yaml`, `k8s/ingress.yaml` |
| Rotas sensíveis com auth CPF | Atende | JWT CPF + `SecurityConfig`; `POST /token` no gateway |
| Function serverless: validar CPF | Atende | `auth-lambda/` + repo `oficina-auth-lambda` |
| Consultar cliente e status na BD | Atende | Liquibase `0005`; query em `lambda_function.py` |
| Emitir JWT para APIs protegidas | Atende | HS256; `GET /api/cliente/sessao` com Bearer |
| 4 repositórios Git separados | Atende | auth-lambda, infra-k8s, infra-database, oficina-app |
| CI/CD em cada repositório | Atende | GitHub Actions (links no PDF portal) |
| Deploy automático para nuvem | Parcial | Kind local + workflows; RDS/EKS opcionais (Terraform AWS) |
| Branch main protegida + PR | Parcial | Política configurável; integração via `develop` |
| Deploy auto homologação/produção | Parcial | Workflow `deploy-k8s-branch.yml`; branches `hml`/`prd` sob demanda |
| BD gerenciado + Terraform | Atende | Repo `oficina-infra-database`; Postgres no cluster (lab) |
| Cluster K8s escalável + Terraform | Atende | Kind (`oficina-infra-kubernetes-`); HPA em `k8s/hpa.yaml` |
| Observabilidade (APM livre) | Atende | Prometheus + Grafana (equivalente Datadog/New Relic em lab) |
| Latência APIs, CPU/mem K8s, health | Atende | Actuator, métricas HTTP, probes |
| Logs JSON + correlação | Atende | Perfil `k8s`, `CorrelationIdFilter` |
| Dashboards OS / tempo por status | Atende | PromQL em `observabilidade-prometheus.md`; Grafana |
| Alertas falhas OS | Parcial | Queries documentadas; regras Grafana configuráveis |
| Serverless notificações | Parcial | Notificações por e-mail na app (não serverless) |
| Diagrama componentes | Atende | `docs/fase3/visao-arquitetura-fase3.md` |
| Diagrama sequência auth + OS | Atende | `docs/fase3/diagrama-sequencia-auth-os.md` |
| RFCs e ADRs | Atende | `docs/fase3/rfc/`, `docs/adr/` |
| Justificativa BD + ER | Atende | `docs/fase3/justificativa-banco-dados.md` + DDD |
| README em cada repo | Atende | README em cada repositório GitHub |
| Dockerfiles | Atende | App, auth-lambda |
| Vídeo ≤ 15 min | Substituto | Evidências CI + script deploy no PDF (sec. 4 e 6.2) |
| PDF portal + 4 links + soat-architecture | Atende | `entrega-portal-fase3.pdf` |

**Verificação executável:** `./scripts/fase3/deploy-local-kind.sh` (Docker + Kind).
