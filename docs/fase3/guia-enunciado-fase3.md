# Tech Challenge Fase 3 — guia do enunciado (implementação)

Documento de referência alinhado ao PDF **13SOAT - Fase 3**. Usar para orientar desenvolvimento, revisões e material de demonstração.

## Contexto

- Peso: **60%** da nota das disciplinas da fase; **obrigatório**; prazo no portal.
- Objetivo: operação **corporativa** — cloud, **IaC**, **segurança**, **observabilidade**.

## Negócio

Multiunidades e muitos clientes → **segurança**, **escalabilidade**, **alta disponibilidade**, **visibilidade** total.

## Requisitos técnicos obrigatórios

### API Gateway + autenticação CPF + Lambda

- **API Gateway** (AWS, Kong, Traefik, etc.).
- Rotas sensíveis com autenticação por **CPF** (via fluxo serverless + JWT).
- **Função serverless**: validar CPF; consultar cliente na BD (**existência e status**); emitir **JWT** para APIs protegidas.

### Quatro repositórios + CI/CD

1. Lambda (serverless)  
2. Terraform **Kubernetes**  
3. Terraform **BD gerenciado**  
4. Aplicação **no Kubernetes**

Regras neste projeto: ramos **`develop`** (integração) e **`main`** (linha estável, atualizada a partir de `develop`); **só merge via PR**; deploy automático opcional em **Kubernetes** conforme pipelines (ambientes `homologacao` / `producao` no GitHub).

### Infra (nuvem livre)

- Gateway, Lambda, **BD gerenciado**, cluster **K8s escalável**, **Terraform**.

### Observabilidade

- APM tipo **Datadog** / **New Relic** (ou equivalente).
- Latência APIs; CPU/mem **K8s**; health/uptime; **alertas** em falhas de processamento de **OS**; logs **JSON** com **correlação**; dashboards: volume diário de OS, tempo médio por **status** (Diagnóstico, Execução, Finalização), erros de integração.

### Documentação

- Diagrama de **componentes** (nuvem).
- Diagrama de **sequência** (auth + abertura OS).
- **RFCs** e **ADRs**.
- **Justificativa de BD** + **ER** e relacionamentos.

### Entregas

- 4 repos: código, CI/CD, README (propósito, stack, deploy, diagrama do repo, Swagger/Postman).
- **Vídeo** ≤15 min (CPF, pipeline, deploy, APIs protegidas, dashboard, logs/traces) — quando exigido pelo enunciado.
- **PDF único** no portal: links dos 4 repos, vídeo (se aplicável), docs, **`soat-architecture`** em todos.

## Estado neste monorepo (transição)

- App: **JWT Keycloak (admin)** + **JWT CPF (HS256)** opcional (`security.cpf-jwt`); endpoint cliente; **Métricas Prometheus** (`/actuator/prometheus`), contador `oficina.os.criadas`; perfil **`k8s`** com logs JSON.
- **Cliente.status** (`ATIVO`/`INATIVO`) + Lambda devolve `cliente_status` no corpo e no JWT.
- Scripts: `scripts/fase3/bootstrap-repos.ps1`; docs: `executar-fase3.md`, ADRs/RFC em `docs/`.

## Próximos passos na nuvem

- Provisionar Gateway → Lambda → RDS; cluster EKS ou equivalente; **ServiceMonitor** Prometheus; export **OTLP** para APM se necessário.
