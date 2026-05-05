# Terraform — API HTTP + Lambda auth CPF (AWS)

Provisiona **Lambda** Python (`auth-lambda/`) e **API Gateway HTTP API** `POST /token`.

## Pré-requisitos

1. [Terraform](https://www.terraform.io/) ≥ 1.5  
2. Credenciais AWS (`aws configure` ou variáveis de ambiente)  
3. ZIP da função:

```bash
cd ../../..   # raiz do repositório oficina-app
chmod +x auth-lambda/build-zip.sh
./auth-lambda/build-zip.sh
```

4. **PostgreSQL** acessível a partir da Lambda:
   - RDS **público** (laboratório) **ou**
   - RDS privado + `lambda_subnet_ids` / `lambda_security_group_ids` + SG liberando porta 5432 até o SG da Lambda.

## Uso

```bash
cd infra/terraform/aws-auth-lambda
cp terraform.tfvars.example terraform.tfvars
# Editar terraform.tfvars (senhas, host RDS, jwt_secret alinhado à app)

terraform init
terraform plan
terraform apply
```

Saída **`token_url`**: enviar `POST` com corpo `{"cpfCnpj":"..."}` (ver `auth-lambda/README.md`).

## GitHub Actions

Workflow manual **`deploy-auth-lambda-aws.yml`**: exige secret **`AWS_ROLE_ARN`** (OIDC) e secrets **`AUTH_LAMBDA_*`** descritos no workflow. **Consome recursos na AWS** ao rodar.

## Estado

Não commite `terraform.tfvars` nem `.tfstate`. Para equipe, use backend S3 + lock DynamoDB.
