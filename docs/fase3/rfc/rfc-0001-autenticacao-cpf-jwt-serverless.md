# RFC 0001 - Autenticacao com CPF e JWT via funcao serverless

## Status

Rascunho

## Resumo

Implementar o fluxo exigido pela Fase 3: **API Gateway** expoe rota de **token**; **Lambda** valida **CPF**, consulta **cliente** na base de dados PostgreSQL, devolve **JWT** assinado para consumo das **APIs protegidas** atraves do Gateway.

## Escopo

- **Entrada**: CPF (somente digitos ou formatado) em corpo JSON ou header conforme definicao OpenAPI.
- **Saida**: JWT com `sub`, `exp`, claims minimas para identificar cliente e escopos de API.
- **Erros**: HTTP 400 (CPF invalido), 401/403/404 conforme existencia e estado do cliente.
- **Segredo**: chaves assinatura JWT em **AWS Secrets Manager** ou **Parameter Store**; rotacao documentada.

## Fora de escopo (nesta RFC)

- Substituicao completa do Keycloak para rotas **admin** internas; pode ser **fase posterior** ou **convivencia** (duas rotas de autenticacao documentadas).

## Dependencias

- BD gerido acessivel a partir da Lambda (VPC, security groups, IAM).
- Acordo de **issuer** e **audience** para a app Spring validar o JWT.

## Proximos passos

1. OpenAPI da rota de token.
2. Implementacao Lambda + testes.
3. Integracao Gateway - Lambda.
4. Ajuste Spring Security na app para validar JWT da Lambda.
