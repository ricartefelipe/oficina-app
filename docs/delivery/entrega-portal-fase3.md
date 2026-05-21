# Tech Challenge — Fase 3 — Entrega no portal do aluno (PDF)

**Grupo:** Oficina Turbo (106)  
**Aluno:** Felipe Ricarte Magalhães  

Este Markdown é a **fonte do PDF** a submeter no portal da disciplina. Regenerar com:

```bash
pip install --user fpdf2
python3 scripts/delivery/gen_entrega_pdf.py
```

Arquivo gerado: **`docs/delivery/entrega-portal-fase3.pdf`**

---

## 1. Repositório principal da aplicação (Fase 3 — app no Kubernetes)

**URL:** https://github.com/ricartefelipe/oficina-app  

**Branches:** `main` (estável), `develop` (integração).  

**Acesso ao avaliador SOAT:** **`soat-architecture`** convidado com leitura nos quatro repositórios (confirmado).

**Documentação Fase 3:**  
https://github.com/ricartefelipe/oficina-app/tree/main/docs/fase3  

**Deploy local (Kind, custo zero):**  
`./scripts/fase3/deploy-local-kind.sh`

---

## 2. Quatro repositórios (Fase 3) + CI/CD

| # | Repositório | Conteúdo |
|---|-------------|----------|
| 1 | https://github.com/ricartefelipe/oficina-auth-lambda | Lambda Python — CPF/CNPJ, JWT |
| 2 | https://github.com/ricartefelipe/oficina-infra-database | Terraform — VPC/RDS (opcional), CI |
| 3 | https://github.com/ricartefelipe/oficina-infra-kubernetes- | Terraform — cluster **Kind** local, CI |
| 4 | https://github.com/ricartefelipe/oficina-app | Spring Boot, `k8s/`, Traefik, observabilidade |

---

## 3. Confirmação — usuário `soat-architecture`

O usuário **`soat-architecture`** tem **acesso de leitura** aos **quatro** repositórios acima.

---

## 4. Vídeo demonstrativo (≤ 15 minutos)

**Link:** *(não aplicável — evidências substitutas na seção 6)*  

**Substituto (autorizado no guia da Fase 3):** script `./scripts/fase3/deploy-local-kind.sh` + links CI + descrição do fluxo CPF → JWT → API protegida + métricas Prometheus/Grafana no cluster Kind.

---

## 5. Swagger / OpenAPI

- **Local (Kind):** http://localhost:8088/api/swagger-ui/index.html  
- **OpenAPI JSON:** http://localhost:8088/api/openapi  

---

## 6. Arquitetura e documentação técnica

### 6.1 Diagramas e texto

| Artefacto | Local |
|-----------|-------|
| Visão componentes / nuvem | [`docs/fase3/visao-arquitetura-fase3.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/visao-arquitetura-fase3.md) |
| Sequência auth + OS | [`docs/fase3/diagrama-sequencia-auth-os.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/diagrama-sequencia-auth-os.md) |
| RFC autenticação CPF/JWT | [`docs/fase3/rfc/rfc-0001-autenticacao-cpf-jwt-serverless.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/rfc/rfc-0001-autenticacao-cpf-jwt-serverless.md) |
| ADRs | [`docs/adr/README.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/adr/README.md) |
| Observabilidade | [`docs/fase3/observabilidade-prometheus.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/observabilidade-prometheus.md) |

### 6.2 Evidências CI/CD (últimas execuções com sucesso)

| Repositório | Link |
|-------------|------|
| oficina-app (CI) | https://github.com/ricartefelipe/oficina-app/actions/runs/25968127589 |
| oficina-app (Deploy K8s) | https://github.com/ricartefelipe/oficina-app/actions/runs/25968199744 |
| oficina-auth-lambda | https://github.com/ricartefelipe/oficina-auth-lambda/actions/runs/25646756207 |
| oficina-infra-database | https://github.com/ricartefelipe/oficina-infra-database/actions/runs/25647090367 |
| oficina-infra-kubernetes- | https://github.com/ricartefelipe/oficina-infra-kubernetes-/actions/runs/25647085909 |

### 6.3 Observabilidade

- **Gateway:** Traefik (`k8s/platform/traefik.yaml`) — rota `POST /token` → auth; `/api` → app.
- **Métricas:** Prometheus scrape em `/actuator/prometheus`; contadores `oficina_os_criadas_total`, timers por fase.
- **Logs:** perfil `k8s` — JSON com `correlationId`.
- **Dashboards:** Grafana (`k8s/platform/grafana.yaml`) — datasource Prometheus; queries PromQL em `observabilidade-prometheus.md`.

### 6.4 Decisão de infraestrutura (custo zero)

Em vez de AWS (RDS + Lambda + EKS), a demonstração usa **Kind + Terraform** (`oficina-infra-kubernetes-`), **Postgres no cluster**, **auth como container** (mesmo código da Lambda) e **Traefik** como API Gateway — conforme enunciado (“nuvem livre”, Kong/Traefik aceitos).

---

## 7. Mapeamento — requisitos Fase 3

| Requisito | Onde está coberto |
|-----------|-------------------|
| API Gateway + serverless CPF → JWT | Traefik + `auth-lambda/` (HTTP) + RFC-0001 |
| Quatro repositórios + CI/CD | Seção 2 + evidências 6.2 |
| BD gerenciado + K8s + Terraform | Postgres no cluster + Kind Terraform + `k8s/` |
| Observabilidade | Prometheus, Grafana, logs JSON — seção 6.3 |
| Diagramas, RFC, ADR | Seção 6.1 |
| `soat-architecture` | Seção 3 |

---

*Entrega Fase 3 — stack local Kind documentada e automatizada em `scripts/fase3/deploy-local-kind.sh`.*
