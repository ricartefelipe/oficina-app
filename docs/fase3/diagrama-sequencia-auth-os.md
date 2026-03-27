# Diagrama de sequencia - autenticacao (CPF) e abertura de OS

## Autenticacao com CPF (JWT via serverless)

```mermaid
sequenceDiagram
  participant C as Cliente
  participant GW as API Gateway
  participant L as Funcao serverless
  participant DB as PostgreSQL gerido
  participant IdP as Emissor JWT

  C->>GW: POST /auth/token (CPF)
  GW->>L: encaminhar payload
  L->>L: validar formato CPF
  L->>DB: SELECT cliente por CPF
  alt cliente existe e ativo
    L->>IdP: gerar JWT (claims acordadas)
    IdP-->>L: JWT
    L-->>GW: 200 + JWT
    GW-->>C: 200 + JWT
  else cliente inexistente ou invalido
    L-->>GW: 401/403/404 conforme politica
    GW-->>C: erro padronizado
  end
```

## Abertura de ordem de servico (API protegida)

```mermaid
sequenceDiagram
  participant C as Cliente
  participant GW as API Gateway
  participant APP as App Spring Boot
  participant DB as PostgreSQL gerido

  C->>GW: POST /api/.../ordens-servico (Authorization Bearer JWT)
  GW->>GW: validar rota e opcionalmente token na borda
  GW->>APP: proxy
  APP->>APP: validar JWT (assinatura, issuer, audience)
  APP->>DB: persistir OS e itens
  DB-->>APP: OK
  APP-->>GW: 201 + recurso
  GW-->>C: resposta
```

Estes fluxos serao refinados quando o Gateway e o contrato JWT estiverem definidos nos repositorios de infraestrutura e na aplicacao.
