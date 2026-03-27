# oficina-infra-database

Infraestrutura como código (**Terraform**) para **rede** e **base de dados gerenciada** (Tech Challenge Fase 3 — SOAT).

## Propósito

- Provisionar **VPC**, subnets, **Internet Gateway** e roteamento.
- Opcionalmente **RDS PostgreSQL** (laboratório/produção conforme variáveis).
- Outputs para JDBC/secrets (consumo pela app e pela Lambda).

## Stack

- Terraform >= 1.5
- Provider **AWS** (região configurável, ex.: `sa-east-1`)

## Pré-requisitos

- [Terraform](https://www.terraform.io/) instalado
- Credenciais AWS (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` ou perfil IAM)

## Execução local

```bash
cd .
cp terraform.tfvars.example terraform.tfvars
# Editar terraform.tfvars
terraform init
terraform plan
terraform apply
```

**Não commite** `terraform.tfstate` nem segredos. Para equipa, use backend remoto (S3 + lock).

## CI

- `terraform fmt -check`, `init -backend=false`, `validate` em PR.
- `plan`/`apply` em workflow manual com secrets (não em cada push automático sem revisão).

## Diagrama (repositório)

```text
[Internet] <-> [IGW] <-> [Subnets públicas]
                              |
                         [RDS PostgreSQL]
                      (security group + VPC)
```

## Documentação

- `README.md` detalhado e `docs/terraform-vs-enunciado.md` (se existirem na cópia a partir do monorepo).

## Convite

Adicionar **`soat-architecture`** com leitura (portal SOAT).
