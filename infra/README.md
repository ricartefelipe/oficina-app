# Infraestrutura (`/infra`)

## Terraform — Lambda de autenticacao (CPF/JWT) + API Gateway HTTP

Stack self-contained para a **Fase 3** (AWS):

- Diretorio: [`terraform/aws-auth-lambda/`](terraform/aws-auth-lambda/)
- Leia o [`README`](terraform/aws-auth-lambda/README.md) para variaveis, `build-zip.sh` e workflow manual no GitHub.

Outros modulos Terraform da disciplina (VPC completa, RDS isolado, EKS) podem viver nos repositorios **oficina-infra-database** e **oficina-infra-kubernetes-**, conforme o planeamento em [`docs/fase3/repositorios-planejados.md`](../docs/fase3/repositorios-planejados.md).

## Kubernetes (manifestos da aplicacao)

Manifestos da aplicacao Spring Boot: pasta **[`/k8s`](../k8s)** na raiz do repositorio.
