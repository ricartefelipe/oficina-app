# Repositorios planejados (Fase 3)

O enunciario exige **quatro repositorios Git separados**, cada um com **CI/CD** e **deploy automatico** para a nuvem.

| # | Repositorio sugerido | Conteudo | Pipeline minima |
|---|----------------------|----------|-----------------|
| 1 | `oficina-auth-lambda` | Codigo da funcao serverless (validacao CPF, consulta cliente, emissao JWT), testes, IaC da funcao (ex.: SAM, Terraform `aws_lambda`) | build, testes, deploy para `dev`/`hml`/`prd` via branches |
| 2 | `oficina-infra-kubernetes` | Terraform (ou equivalente) para cluster gerenciado, redes, IAM, ingress, integracao com Gateway | `fmt`, `validate`, `plan` em PR; `apply` em ambiente controlado |
| 3 | `oficina-infra-database` | Terraform para RDS (ou Aurora) PostgreSQL, subnets, security groups, backups, parametros | idem |
| 4 | `oficina-app` (evolucao deste monolito) | Spring Boot, Dockerfile, Helm ou manifests K8s, integracao observabilidade | build, testes, imagem, deploy K8s |

O repositorio atual **oficina-springboot-mvp** pode ser **renomeado** ou **clonado** para `oficina-app` quando a equipa iniciar a separacao; ate la, a documentacao e o backlog vivem aqui.

## Convites

O utilizador **`soat-architecture`** deve ser adicionado a **todos** os quatro repositorios (leitura), conforme portal da Fase 3.

## README em cada um

Cada repositorio deve ter: proposito, stack, passos de execucao e deploy, diagrama **especifico** daquele repo, link para Swagger/API quando aplicavel.

Para gerar localmente as quatro pastas e o checklist (GitHub, AWS, convite **soat-architecture**), ver [executar-fase3.md](executar-fase3.md).
