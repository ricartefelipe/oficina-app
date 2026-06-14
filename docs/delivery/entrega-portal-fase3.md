# Tech Challenge — Fase 3 — Entrega no portal do aluno (PDF)

**Grupo:** Oficina Turbo (106)  
**Aluno:** Felipe Ricarte Magalhães  

---

## 1. Repositório principal da aplicação (Fase 3 — app no Kubernetes)

**URL:** https://github.com/ricartefelipe/oficina-app  

**Branches:** `main` (estável), `develop` (integração).  

**Acesso ao avaliador SOAT:** **`soat-architecture`** convidado com leitura nos quatro repositórios (confirmado).

**Documentação Fase 3:**  
https://github.com/ricartefelipe/oficina-app/tree/main/docs/fase3  

**Deploy AWS (produção de demonstração):**  
`./scripts/fase3/deploy-aws-eks-app.sh`  

**Deploy local (Kind, laboratório):**  
`./scripts/fase3/deploy-local-kind.sh`

---

## 2. Quatro repositórios (Fase 3) + CI/CD

| # | Repositório | Conteúdo |
|---|-------------|----------|
| 1 | https://github.com/ricartefelipe/oficina-auth-lambda | Lambda Python — CPF/CNPJ, JWT |
| 2 | https://github.com/ricartefelipe/oficina-infra-database | Terraform — VPC + **RDS PostgreSQL** (sa-east-1) |
| 3 | https://github.com/ricartefelipe/oficina-infra-kubernetes- | Terraform — **EKS** (`aws-eks/`) + Kind local |
| 4 | https://github.com/ricartefelipe/oficina-app | Spring Boot, `k8s/`, observabilidade, Terraform Lambda |

---

## 3. Confirmação — usuário `soat-architecture`

O usuário **`soat-architecture`** tem **acesso de leitura** aos **quatro** repositórios acima.

---

## 4. Vídeo demonstrativo (≤ 15 minutos)

**Link:** *(inserir URL YouTube/Vimeo antes do reenvio no portal)*  

Roteiro sugerido: token CPF na Lambda → JWT → API no Load Balancer EKS → console AWS (RDS/EKS/Lambda) → Prometheus/Grafana no cluster.

---

## 5. Swagger / OpenAPI

- **AWS (Load Balancer EKS):** http://aff29bf52653240a4888cf832dcbd907-510655981.sa-east-1.elb.amazonaws.com/api/swagger-ui/index.html  
- **OpenAPI JSON:** http://aff29bf52653240a4888cf832dcbd907-510655981.sa-east-1.elb.amazonaws.com/api/openapi  
- **Local (Kind):** http://localhost:8088/api/swagger-ui/index.html  

---

## 6. Arquitetura e documentação técnica

### 6.1 Infraestrutura AWS (sa-east-1)

| Componente | Evidência |
|------------|-----------|
| **RDS PostgreSQL** | `oficina-dev-pg.cdw04ewgopr2.sa-east-1.rds.amazonaws.com` — repo `oficina-infra-database` |
| **Lambda auth CPF** | `oficina-auth-cpf-fn` — Terraform `oficina-app/infra/terraform/aws-auth-lambda` |
| **API Gateway (HTTP)** | Token: `https://i3te2gzkmk.execute-api.sa-east-1.amazonaws.com/token` |
| **EKS** | Cluster `oficina-dev` — Terraform `oficina-infra-kubernetes-/aws-eks/` |
| **ECR** | `381234267424.dkr.ecr.sa-east-1.amazonaws.com/oficina-app` |
| **API (Load Balancer)** | http://aff29bf52653240a4888cf832dcbd907-510655981.sa-east-1.elb.amazonaws.com/api |
| **Health** | http://aff29bf52653240a4888cf832dcbd907-510655981.sa-east-1.elb.amazonaws.com/api/actuator/health |

**Fluxo validado:** `POST /token` com CPF `52998224725` → JWT → `GET /api/cliente/sessao` com Bearer → HTTP 200.

### 6.2 Diagramas e texto

| Artefacto | Local |
|-----------|-------|
| Visão componentes / nuvem | [`docs/fase3/visao-arquitetura-fase3.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/visao-arquitetura-fase3.md) |
| Sequência auth + OS | [`docs/fase3/diagrama-sequencia-auth-os.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/diagrama-sequencia-auth-os.md) |
| RFC autenticação CPF/JWT | [`docs/fase3/rfc/rfc-0001-autenticacao-cpf-jwt-serverless.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/rfc/rfc-0001-autenticacao-cpf-jwt-serverless.md) |
| ADRs | [`docs/adr/README.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/adr/README.md) |
| Observabilidade | [`docs/fase3/observabilidade-prometheus.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/observabilidade-prometheus.md) |
| Justificativa BD + modelo relacional | [`docs/fase3/justificativa-banco-dados.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/fase3/justificativa-banco-dados.md) |
| Matriz conformidade enunciado | [`docs/delivery/matriz-conformidade-fase3.md`](https://github.com/ricartefelipe/oficina-app/blob/main/docs/delivery/matriz-conformidade-fase3.md) |

### 6.3 Evidências CI/CD (GitHub Actions)

| Repositório | Link |
|-------------|------|
| oficina-app (CI Maven) | https://github.com/ricartefelipe/oficina-app/actions |
| oficina-auth-lambda | https://github.com/ricartefelipe/oficina-auth-lambda/actions |
| oficina-infra-database | https://github.com/ricartefelipe/oficina-infra-database/actions |
| oficina-infra-kubernetes- | https://github.com/ricartefelipe/oficina-infra-kubernetes-/actions |

### 6.4 Observabilidade (EKS)

- **Métricas:** Prometheus scrape em `/api/actuator/prometheus`; alerta `OficinaAppDown` (`k8s/aws/prometheus.yaml`).
- **Dashboards:** Grafana (`k8s/platform/grafana.yaml`) — datasource Prometheus.
- **Logs:** perfil `k8s` — JSON com `correlationId`.
- **Acesso local:** `kubectl port-forward -n oficina svc/prometheus 9090:9090` e `svc/grafana 3000:3000`.

### 6.5 Laboratório local (Kind)

Kind + Traefik + Postgres in-cluster permanecem em `./scripts/fase3/deploy-local-kind.sh` para desenvolvimento sem custo AWS.

---

## 7. Mapeamento — requisitos Fase 3

| Requisito | Onde está coberto |
|-----------|-------------------|
| API Gateway + serverless CPF → JWT | API Gateway HTTP + Lambda + RFC-0001 |
| Quatro repositórios + CI/CD | Seção 2 + evidências 6.3 |
| BD gerenciado + K8s + Terraform | RDS (`oficina-infra-database`) + EKS (`aws-eks/`) + `k8s/aws/` |
| Observabilidade | Prometheus, Grafana, logs JSON — seção 6.4 |
| Diagramas, RFC, ADR, modelo dados | Seção 6.2 + justificativa BD |
| `soat-architecture` | Seção 3 |
| Matriz vs. enunciado PDF | [`matriz-conformidade-fase3.md`](matriz-conformidade-fase3.md) |

---

## 8. Pacote zipado (backup / arquivo)

Estrutura em camadas gerada por:

```bash
./scripts/delivery/build-fase3-entrega-zip.sh
```

Saída: `~/Downloads/oficina-tech-challenge-fase3-entrega.zip` (PDF + 4 repos + documentação).

---

*Entrega Fase 3 — stack AWS (RDS + Lambda + EKS) documentada em `scripts/fase3/deploy-aws-eks-app.sh`; Kind local mantido para laboratório.*
