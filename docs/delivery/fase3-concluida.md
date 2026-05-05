# Fase 3 — Critérios do enunciário atendidos (Tech Challenge)

Consolidacao **no repositorio oficina-app** em relacao ao PDF **13SOAT — Fase 3**. Convites ao portal, video e custos AWS ficam do lado da conta do aluno (ver [entrega-portal-fase3.md](entrega-portal-fase3.md)).

## Autenticacao e API

| Requisito | Evidencia |
|-----------|-----------|
| Funcao serverless: validar documento, consultar cliente e status, emitir JWT | [`auth-lambda/`](../../auth-lambda/) (Python), [`ClienteSessaoController`](../../src/main/java/br/com/oficina/cpf/api/ClienteSessaoController.java), `security.cpf-jwt` em [`application.yml`](../../src/main/resources/application.yml) |
| API Gateway (definicao IaC) | Terraform [`infra/terraform/aws-auth-lambda/`](../../infra/terraform/aws-auth-lambda/) — HTTP API `POST /token`; workflow [`.github/workflows/deploy-auth-lambda-aws.yml`](../../.github/workflows/deploy-auth-lambda-aws.yml) |

## Repositorios e CI/CD

| Requisito | Evidencia |
|-----------|-----------|
| Quatro repositorios Git com CI/CD | Tabela em [entrega-portal-fase3.md](entrega-portal-fase3.md); neste repo: [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml), [`auth-lambda-ci.yml`](../../.github/workflows/auth-lambda-ci.yml), [`deploy-auth-lambda-aws.yml`](../../.github/workflows/deploy-auth-lambda-aws.yml) |

## Infraestrutura (codigo no repo)

| Requisito | Evidencia |
|-----------|-----------|
| Terraform na nuvem (Lambda + Gateway HTTP) | [`infra/terraform/aws-auth-lambda/README.md`](../../infra/terraform/aws-auth-lambda/README.md) |
| Kubernetes (app) | [`k8s/`](../../k8s/) |
| BD gerenciado | Liquibase + Postgres — schema em [`db/changelog`](../../src/main/resources/db/changelog); RDS propriamente dito nos repositorios **oficina-infra-database** |

## Observabilidade

| Requisito | Evidencia |
|-----------|-----------|
| Metricas (latencia HTTP Spring, dominio OS, falhas notificacao) | [`observabilidade-prometheus.md`](../fase3/observabilidade-prometheus.md), endpoint `/actuator/prometheus` |
| Logs JSON + correlacao | Perfil `k8s` em [`logback-spring.xml`](../../src/main/resources/logback-spring.xml), [`CorrelationIdFilter`](../../src/main/java/br/com/oficina/shared/infra/http/CorrelationIdFilter.java) |

## Documentacao de arquitetura

| Requisito | Evidencia |
|-----------|-----------|
| Diagramas, RFC, ADR | [`docs/fase3/README.md`](../fase3/README.md), [`docs/adr/`](../adr/) |

---

**Verificacao local sugerida antes da entrega:** `mvn verify` na raiz; para Lambda, `pytest` em `auth-lambda/` (com venv).
