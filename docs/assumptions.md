# Assumptions (MVP - Fase 1)

Este projeto e o MVP back-end monolitico do sistema de oficina.

## Credenciais e ambiente local (somente DEV)
Para garantir execucao local simples via docker-compose, usamos credenciais fixas no compose:

- Postgres (app):
  - db: `oficina`
  - user: `oficina`
  - password: `oficina`
  - porta no host: `5433`

- Keycloak:
  - Console Admin (para acessar /admin do Keycloak):
    - user: `kcadmin`
    - password: `kcadmin`
  - Realm: `oficina`
  - Usuario do realm (para obter JWT e chamar endpoints admin futuramente):
    - user: `admin`
    - password: `admin`
    - role (realm): `ADMIN`

> Seguranca: essas credenciais sao apenas para execucao local e demonstracao do MVP. Nao sao adequadas para producao.

## Base path /api
Todos os endpoints REST do sistema estao sob `/api`.

## JWT / Seguranca
- **Admin protegido por JWT**: `/api/admin/**` exige token valido com role `ADMIN`.
- **Publico**: `/api/public/**` e permitido sem JWT, mas a aprovacao do orcamento exige o CPF/CNPJ do cliente.
- O Keycloak sobe via docker-compose com realm importado para garantir reprodutibilidade.

> Observacao: por padrao, o resource-server valida o `issuer` contra uma lista de issuers permitidos (`JWT_ALLOWED_ISSUERS`) para cobrir variacoes de `iss` ao obter token via `localhost` vs. rede docker.

## Observabilidade minima
- Implementado `X-Correlation-Id` (gerado ou propagado) e inserido no MDC para logs.

## Seed minima (catalogo)
Para demonstracao rapida (sem depender de endpoints de cadastro), o Liquibase insere seed minima:

### Servicos (tabela `servicos_catalogo`)
- Troca de oleo (id: `11111111-1111-1111-1111-111111111111`)
- Alinhamento (id: `22222222-2222-2222-2222-222222222222`)
- Balanceamento (id: `33333333-3333-3333-3333-333333333333`)

### Pecas/Insumos (tabela `pecas_insumos`)
- Oleo 5W30 (1L) (id: `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa`)
- Filtro de oleo (id: `bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb`)
- Pneu 175/70R13 (id: `cccccccc-cccc-cccc-cccc-cccccccccccc`)

## Fluxo de Ordem de Servico (OS)
- Ao criar uma OS via `POST /api/admin/ordens-servico`, o sistema:
  - Cria (ou vincula) o Cliente por CPF/CNPJ.
  - Cria (ou vincula) o Veiculo por placa.
  - Calcula o orcamento automaticamente.
  - Mantem o status inicial como `RECEBIDA`.
- O envio de orcamento ao cliente (mudando para `AGUARDANDO_APROVACAO`) e uma acao explicita via `POST /api/admin/ordens-servico/{id}/orcamento/enviar`.
  - Isso garante o fluxo completo de status do enunciado: `RECEBIDA -> EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO -> EM_EXECUCAO -> FINALIZADA -> ENTREGUE`.
- Ao aprovar o orcamento (public) e entrar em `EM_EXECUCAO`, o sistema decrementa o estoque das pecas/insumos vinculadas a OS.
