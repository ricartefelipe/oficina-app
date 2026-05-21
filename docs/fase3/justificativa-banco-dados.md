# Justificativa do banco de dados — Fase 3

## Escolha: PostgreSQL

| Critério | PostgreSQL |
|----------|------------|
| Modelo relacional e integridade | FK, constraints, transações ACID |
| Domínio da Oficina | clientes, veículos, OS, itens, transições — relações 1:N e N:N naturais |
| Ecossistema Spring | JPA/Hibernate + Liquibase já adotados nas fases anteriores |
| Operacao | RDS (Terraform em `oficina-infra-database`) ou Postgres no cluster (`k8s/postgres.yaml`) |
| Auth serverless | consulta `clientes.cpf_cnpj` e `status` via JDBC/psycopg2 |

Alternativas descartadas para este projeto: MySQL (equivalente, sem ganho de domínio), SQL Server (custo/licenciamento em lab).

## Modelo relacional (resumo)

| Entidade | Relacionamentos principais |
|----------|---------------------------|
| `clientes` | 1:N `veiculos`; 1:N `ordens_servico`; campo `status` (ATIVO/INATIVO) para auth Fase 3 |
| `veiculos` | N:1 `clientes` |
| `ordens_servico` | N:1 `clientes`, N:1 `veiculos`; 1:N `itens_os`, `transicoes_os` |
| `servicos_catalogo`, `pecas_insumos` | referenciados por itens de OS |
| `itens_os` | N:1 OS, serviço ou peça |

Schema versionado em `src/main/resources/db/changelog/` (Liquibase). Evolução Fase 3: changeset `0005-cliente-status-fase3`, seed demo `0006-seed-cliente-demo-fase3`.

## Diagramas

- Agregado Ordem de Serviço (DDD): [`docs/ddd/diagrams/ordem-servico-agregado.svg`](../ddd/diagrams/ordem-servico-agregado.svg)
- Contextos e event storming: [`docs/ddd/diagrams/`](../ddd/diagrams/)
- Visão persistência na arquitetura: [`visao-arquitetura-fase3.md`](visao-arquitetura-fase3.md)
