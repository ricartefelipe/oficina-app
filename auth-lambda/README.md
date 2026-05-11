# Função serverless — autenticação Fase 3 (SOAT)

Valida **CPF ou CNPJ**, consulta o cliente em **PostgreSQL** (`clientes.cpf_cnpj`, `status`) e devolve **JWT HS256** no formato esperado pelo Spring Boot (`security.cpf-jwt`: mesmo `issuer` e `secret`).

## Contrato HTTP (API Gateway)

**POST** corpo JSON:

```json
{ "cpfCnpj": "529.982.247-25" }
```

Aceita também `cpf`, `cpf_cnpj` ou `documento`.

**200** — `access_token`, `token_type`, `expires_in`, `cliente_id`, `cliente_status`.

**400** — documento inválido.

**403** — cliente existe mas `status` ≠ `ATIVO`.

**404** — não há linha em `clientes`.

## Variáveis de ambiente

| Variável | Descrição |
|----------|-----------|
| `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` | Conexão RDS / Postgres |
| `JWT_SECRET` | Mesmo valor de `JWT_CPF_SECRET` na app |
| `JWT_ISSUER` | Mesmo valor de `JWT_CPF_ISSUER` (default `https://oficina.local/auth/cpf`) |
| `JWT_EXPIRATION_SECONDS` | TTL do token (300–86400, default 3600) |

## Empacotamento Lambda

Inclua na raiz do zip: `lambda_function.py`, `cpf_cnpj.py` e dependências (`pip install -r requirements.txt -t .`).

Handler: `lambda_function.lambda_handler`.

## Testes locais

```bash
cd auth-lambda
python -m venv .venv && source .venv/bin/activate
pip install -r requirements-dev.txt
pytest -q
```

## Deploy na AWS (Terraform)

Na raiz do repositório:

```bash
./auth-lambda/build-zip.sh
cd infra/terraform/aws-auth-lambda
cp terraform.tfvars.example terraform.tfvars   # editar host RDS e credenciais
terraform init && terraform apply
```

Saída **`token_url`**: `POST` com `{"cpfCnpj":"..."}`.

Ou use o workflow manual **Deploy auth Lambda AWS** (GitHub Actions): configure **`AWS_ROLE_ARN`** (OIDC) e segredos **`AUTH_LAMBDA_PG_*`**, **`AUTH_LAMBDA_JWT_SECRET`** — ver `.github/workflows/deploy-auth-lambda-aws.yml`.

A Lambda precisa **alcançar o Postgres** (RDS público em laboratório ou Lambda em VPC + subnets/SGs).

## Repositório separado (gitflow)

Este diretório pode ser copiado para o repositório **`oficina-auth-lambda`** com CI próprio; ver `scripts/fase3/bootstrap-repos.ps1` e `docs/fase3/executar-fase3.md`.
