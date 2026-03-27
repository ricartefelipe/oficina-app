# ADR 0001 - Fronteiras multi-repositorio na Fase 3

## Status

Aceite

## Contexto

O enunciario da Fase 3 exige **quatro repositorios** com CI/CD e deploy automatico, separando Lambda, infraestrutura K8s, infraestrutura de BD e aplicacao.

## Decisao

Adotar quatro repositorios com responsabilidades:

1. **auth-lambda**: autenticacao baseada em CPF e emissao de JWT.
2. **infra-database**: Terraform para PostgreSQL gerido.
3. **infra-kubernetes**: Terraform para cluster e recursos de plataforma.
4. **app**: monolito Spring Boot em container no cluster.

A documentacao viva da Fase 3 permanece no repositorio da aplicacao ate a equipa bifurcar repositorios.

## Consequencias

- Coordenacao de versoes e contratos (JWT, URLs, secrets) via RFC e pipelines ordenados.
- Maior overhead operacional; ganho em autonomia de deploy e clareza de fronteiras.
