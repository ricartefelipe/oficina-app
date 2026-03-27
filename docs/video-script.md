# Roteiro do video (ate 15 minutos) - Tech Challenge Fases 1 e 2

Este roteiro cobre o MVP (Fase 1) e deve ser **complementado** com os blocos obrigatorios da **Fase 2** abaixo (mesmo video, ate 15 minutos no total).

Sugestao: grave em 1080p, com fonte grande e zoom (se necessario). Evite mostrar tokens completos em tela.

## Fase 2 - O que o enunciario exige no video (prioridade)

Reserve tempo para mostrar **nesta ordem** (pode encurtar secoes 5–8 da Fase 1 se precisar de minutos):

1. **Deploy da aplicacao** - `docker compose up` e/ou `kubectl apply` (manifestos em `/k8s`), rollout.
2. **CI/CD** - GitHub Actions: workflow `ci.yml` (build, testes, Terraform validate, imagem GHCR); mencionar workflows manuais `deploy-kubernetes.yml` / `terraform-aws.yml` se usar.
3. **Consumo das APIs** - Swagger ou `curl` (abertura OS, consulta status, listagem admin, resposta externa ao orcamento se aplicavel).
4. **Escalabilidade automatica** - HPA em `/k8s/hpa.yaml` (metricas CPU/memoria) e/ou simulacao de varias OS ou carga; explicar em 1 frase o comportamento esperado.

Referencia: [`docs/delivery/entrega-portal-fase2.md`](delivery/entrega-portal-fase2.md).

## 0) Abertura (0:00 - 0:40)
- Apresente rapidamente:
  - Nome do grupo: <NOME_DO_GRUPO>
  - Participantes + Discord: <NOME - @discord>
  - Repositorio: <LINK_PRIVADO>
  - Documentacao DDD (Miro ou equivalente): <LINK_MIRO>
- Contexto do problema (1 frase): oficina com processo desorganizado (planilhas/anotacoes), precisa de sistema integrado.

## 1) Visao geral do repositorio e stack (0:40 - 1:30)
Na raiz do projeto, mostre:
- `README.md` (objetivo, como rodar, endpoints)
- `docker-compose.yml` (PostgreSQL + Keycloak + app)
- `Dockerfile` (build da aplicacao)
- `src/main/java` (organizacao por bounded contexts)
- `docs/ddd/README.md` (indice: Domain Storytelling, dicionario, Event Storming, SVG)
- `docs/security/*` (notas de seguranca e, na Parte 7, relatorio de vulnerabilidades)

Mencione stack:
- Java 21
- Spring Boot 3
- PostgreSQL (justificativa resumida: ACID, consistencia, tooling e maturidade)
- Liquibase (YAML)
- JWT via Keycloak
- Swagger/OpenAPI
- Testes: JUnit 5 + Testcontainers + JaCoCo

## 2) Subir ambiente com docker-compose (1:30 - 2:30)
No terminal:
```bash
docker compose up --build
```
Mostre containers em execucao.

Em seguida, mostre URLs:
- Swagger UI: `http://localhost:8080/api/swagger-ui`
- Health: `http://localhost:8080/api/public/health`
- Keycloak: `http://localhost:8180`

## 3) Swagger/OpenAPI (2:30 - 3:30)
Abra o Swagger UI.
- Mostre que as rotas estao sob `/api`.
- Mostre separacao:
  - `/admin/*` (protegidas)
  - `/public/*` (consulta/aprovacao)

## 4) JWT (Keycloak) - obter token e provar protecao (3:30 - 5:00)
### 4.1 Provar que admin sem token falha
No terminal:
```bash
curl -i http://localhost:8080/api/admin/clientes
```
Mostre 401/403.

### 4.2 Obter token
Use o script:
```bash
export TOKEN="$(./scripts/get-admin-token.sh)"
```
Mostre que retorna um access_token (nao precisa exibir completo).

### 4.3 Chamar admin com token
```bash
curl -sS http://localhost:8080/api/admin/clientes \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Accept: application/json"
```

## 5) Demonstracao dos fluxos principais (5:00 - 11:30)

### 5.1 Criar OS completa (Admin)
Mostre que o fluxo cria cliente por CPF/CNPJ (se nao existir), cria veiculo por placa, inclui itens, calcula orcamento e gera trackingCode.

Comando exemplo (usando seeds do catalogo - IDs em `docs/assumptions.md`):
```bash
export TOKEN="$(./scripts/get-admin-token.sh)"

curl -sS -X POST "http://localhost:8080/api/admin/ordens-servico" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": {"nome": "Joao da Silva", "cpfCnpj": "39053344705"},
    "veiculo": {"placa": "ABC1D23", "marca": "VW", "modelo": "Gol", "ano": 2018},
    "servicos": [
      {"servicoId": "11111111-1111-1111-1111-111111111111", "quantidade": 1}
    ],
    "pecas": [
      {"pecaId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "quantidade": 4}
    ]
  }'
```
Mostre no retorno:
- `status=RECEBIDA`
- `trackingCode`
- `orcamentoTotal`

Guarde:
- `OS_ID` e `TRACKING_CODE`

### 5.2 Iniciar diagnostico (Admin)
```bash
curl -sS -X POST "http://localhost:8080/api/admin/ordens-servico/${OS_ID}/diagnostico/iniciar" \
  -H "Authorization: Bearer ${TOKEN}"
```
Mostre status `EM_DIAGNOSTICO`.

### 5.3 Enviar orcamento (Admin)
```bash
curl -sS -X POST "http://localhost:8080/api/admin/ordens-servico/${OS_ID}/orcamento/enviar" \
  -H "Authorization: Bearer ${TOKEN}"
```
Mostre status `AGUARDANDO_APROVACAO` e o campo `orcamentoEnviadoAt`.

Mostre rapidamente nos logs que o envio foi simulado (log estruturado), sem expor dados sensiveis.

### 5.4 Consulta publica (Cliente)
```bash
curl -sS "http://localhost:8080/api/public/ordens-servico/${TRACKING_CODE}"
```
Mostre:
- status atual
- itens
- historico de transicoes

### 5.5 Aprovar orcamento (Cliente) - inicia execucao e baixa estoque
```bash
curl -sS -X POST "http://localhost:8080/api/public/ordens-servico/${TRACKING_CODE}/aprovar" \
  -H "Content-Type: application/json" \
  -d '{"cpfCnpj":"39053344705"}'
```
Mostre:
- status agora `EM_EXECUCAO`
- `aprovadoAt`

Demonstre baixa de estoque:
- Antes: consultar a peca via admin `GET /admin/pecas/{id}`
- Depois da aprovacao: consultar novamente e mostrar `estoqueAtual` decrementado.

### 5.6 Finalizar execucao e registrar entrega (Admin)
```bash
curl -sS -X POST "http://localhost:8080/api/admin/ordens-servico/${OS_ID}/execucao/finalizar" \
  -H "Authorization: Bearer ${TOKEN}"

curl -sS -X POST "http://localhost:8080/api/admin/ordens-servico/${OS_ID}/entrega/registrar" \
  -H "Authorization: Bearer ${TOKEN}"
```
Mostre status `FINALIZADA` e depois `ENTREGUE`.

## 6) Metricas - tempo medio de execucao (11:30 - 12:30)
Chame o endpoint:
```bash
curl -sS "http://localhost:8080/api/admin/metricas/tempo-medio-execucao" \
  -H "Authorization: Bearer ${TOKEN}"
```
Explique rapidamente a definicao:
- media(FINALIZADA.at - EM_EXECUCAO.at)
- baseado no historico de transicoes.

## 7) Qualidade - testes e cobertura (12:30 - 14:00)
No terminal:
```bash
mvn -q verify
```
Mostre:
- testes unitarios (CPF/CNPJ, placa, orcamento, status)
- teste de integracao (fluxo completo)
- JaCoCo (minimo 80% nos pacotes de dominio)

Abra o relatorio:
- `target/site/jacoco/index.html`

## 8) DDD - documentacao (14:00 - 14:40)
Mostre:
- `docs/ddd/event-storming.md` (fluxos 1 e 2)
- `docs/ddd/diagramas.md` (context map, agregados, sequencias)
- `docs/ddd/ubiquitous-language.md` (glossario)

Se houver board no Miro:
- abrir o link e mostrar rapidamente as colunas de status e stickies.

## 9) Encerramento (14:40 - 15:00)
- Reforcar que:
  - Admin exige JWT/ADMIN
  - Cliente acompanha e aprova via API publica com trackingCode + CPF/CNPJ
  - Estoque baixa na entrada em execucao
  - Metricas e testes atendem MVP
- Mencionar que o relatorio de vulnerabilidades e o documento final de submissao estao em:
  - `docs/security/vulnerability-report.md`
  - `docs/delivery/submission.md`

