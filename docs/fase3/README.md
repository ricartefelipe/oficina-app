# Tech Challenge - Fase 3 (SOAT)

Este diretorio concentra a **documentacao de arquitetura** e o **plano de execucao** para a Fase 3: operacao corporativa, multi-repositorio, API Gateway, autenticacao via CPF com funcao serverless, infraestrutura na nuvem e observabilidade.

## Fonte

Requisitos oficiais: documento **Tech Challenge - Fase 3** (disciplina SOAT).

## Indice

| Documento | Conteudo |
|-----------|----------|
| [visao-arquitetura-fase3.md](visao-arquitetura-fase3.md) | Diagrama de componentes (nuvem), fluxos e decisoes tecnicas resumidas |
| [diagrama-sequencia-auth-os.md](diagrama-sequencia-auth-os.md) | Sequencia: autenticacao com CPF e abertura de ordem de servico |
| [repositorios-planejados.md](repositorios-planejados.md) | Quatro repositorios, fronteiras e responsabilidades |
| [backlog-fase3.md](backlog-fase3.md) | Fases de entrega, dependencias e criterios de pronto |
| [executar-fase3.md](executar-fase3.md) | Script local para quatro pastas-gêmeas, checklist GitHub/AWS e entrega |
| [readmes-primeiro-commit/](readmes-primeiro-commit/) | README.md prontos para copiar para cada um dos 4 repositórios |
| [guia-enunciado-fase3.md](guia-enunciado-fase3.md) | Guia do enunciário Fase 3 (requisitos e implementação) |
| [aws-oidc-github.md](aws-oidc-github.md) | OIDC: secret `AWS_ROLE_ARN` e workflow Terraform |
| [observabilidade-prometheus.md](observabilidade-prometheus.md) | Métricas Prometheus, logs `k8s`, correlação, Grafana |
| [../development/architecture-standards.md](../development/architecture-standards.md) | Padrões de camadas, testes ArchUnit, qualidade |
| [../adr/README.md](../adr/README.md) | ADRs (decisoes arquiteturais permanentes) |
| [rfc/rfc-0001-autenticacao-cpf-jwt-serverless.md](rfc/rfc-0001-autenticacao-cpf-jwt-serverless.md) | RFC: fluxo de autenticacao e contratos |
| [../delivery/entrega-portal-fase3.md](../delivery/entrega-portal-fase3.md) | PDF portal — entrega ao professor (mesmo padrao da Fase 2) |
| [entrega-portal-fase3.md](entrega-portal-fase3.md) | Atalho para o documento completo em `docs/delivery/` |

## Relacao com o codigo atual

O repositorio da aplicacao principal e **`ricartefelipe/oficina-app`** (evolucao do MVP das fases anteriores). Os outros tres repositorios correspondem a **Lambda**, **Terraform BD** e **Terraform K8s**. A divisao exata e o momento da extracacao estao no [backlog](backlog-fase3.md).

## Implementacao iniciada (neste mono-repo)

| Item | Local |
|------|--------|
| JWT com **issuer** do fluxo CPF (HS256) + coexistencia Keycloak | `security.cpf-jwt` em `application.yml`; `MultiIssuerJwtDecoder` |
| API **GET /api/cliente/sessao** (protegida `ROLE_CLIENTE`) | `br.com.oficina.cpf.api.ClienteSessaoController` |
| Funcao **Python** + **Terraform AWS** (Lambda + HTTP API `POST /token`) | `auth-lambda/`, `infra/terraform/aws-auth-lambda/` |
| CI workflow Python | `.github/workflows/auth-lambda-ci.yml` |
| **Cliente.status** (`ATIVO`/`INATIVO`) + Lambda devolve `cliente_status` | Liquibase `0005`; `auth-lambda` |
| **Prometheus** + contador `oficina.os.criadas`; perfil **`k8s`** (logs JSON) | `micrometer-registry-prometheus`; `logback-spring.xml` |
| **ArchUnit** + normas em `architecture-standards` | `src/test/java/br/com/oficina/architecture/`; `docs/development/architecture-standards.md` |
