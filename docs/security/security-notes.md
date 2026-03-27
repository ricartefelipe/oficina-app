# Security Notes (MVP - Fase 1)

## Objetivo
Definir decisoes de seguranca e trade-offs do MVP, alinhado ao enunciado.

## Autenticacao e Autorizacao (JWT)
- Estrategia: **JWT** emitido por **Keycloak** (OpenID Connect).
- Papel (realm role) principal: `ADMIN`.
- Protecao:
  - **Publico (cliente):** `/api/public/**` (nao exige JWT)
  - **Administrativo:** `/api/admin/**` exige JWT valido e **ROLE_ADMIN** (mapeada a partir de `realm_access.roles`)
  - **Negar por padrao:** qualquer outra rota nao explicitamente permitida.

## Validacao do token
- O recurso-server busca chaves publicas via **JWK Set URI**.
- Para manter o ambiente local reproduzivel no docker-compose, validamos `issuer` contra uma lista de issuers permitidos (configuravel via `JWT_ALLOWED_ISSUERS`).
  - Motivacao: o `iss` pode variar conforme o host usado para obter o token (ex.: `http://localhost:8180/...` vs `http://keycloak:8080/...`).

## Tratamento padronizado de 401/403
- Respostas de autenticacao/autorizacao retornam **Problem Details** (`application/problem+json`) e incluem:
  - `correlationId`
  - `path`

## Observabilidade / rastreabilidade
- `X-Correlation-Id`:
  - se o cliente nao enviar, o sistema gera.
  - o header e retornado em todas as respostas, inclusive 401/403.
- Logs incluem `correlationId` via MDC.

## Dependencias e CVEs (baseline)
- **Fonte de verdade de versoes:** `spring-boot-starter-parent` (BOM) - atualizar **minor/patch** do Spring Boot para absorver correcoes em Logback, Tomcat, Spring Framework, drivers, etc.
- **Excecoes:** apenas versoes declaradas no `pom.xml` quando nao geridas pelo BOM (ex.: `springdoc`, `logstash-logback-encoder`, Testcontainers) ou override pontual documentado.
- **SCA (Mend, OWASP Dependency-Check, Trivy):** reexecutar apos upgrades; alertas "Insufficient Information" ou falsos positivos exigem triagem manual.
- **CI:** `mvn -Pci verify` alinha com exclusao de testes que exigem Docker local; o repositorio usa **Dependabot** (`.github/dependabot.yml`) para propor PRs de atualizacao.

## Trade-offs do MVP
- Keycloak adiciona peso ao docker-compose, mas entrega um fluxo realista de JWT/roles.
- Credenciais default e secrets sao apenas para DEV (nao producao).
