variable "aws_region" {
  type        = string
  description = "Região AWS (ex.: sa-east-1)"
  default     = "sa-east-1"
}

variable "project_name" {
  type        = string
  description = "Prefixo dos nomes dos recursos"
  default     = "oficina"
}

variable "lambda_zip_path" {
  type        = string
  description = "Caminho para function.zip (rodar auth-lambda/build-zip.sh antes)"
}

variable "pg_host" {
  type        = string
  description = "Host PostgreSQL (RDS ou IP público de laboratório)"
}

variable "pg_port" {
  type        = number
  description = "Porta PostgreSQL"
  default     = 5432
}

variable "pg_database" {
  type        = string
  description = "Nome do banco"
  default     = "oficina"
}

variable "pg_user" {
  type        = string
  description = "Usuário do banco"
}

variable "pg_password" {
  type        = string
  description = "Senha do banco"
  sensitive   = true
}

variable "jwt_issuer" {
  type        = string
  description = "Mesmo issuer configurado na app Spring (security.cpf-jwt.issuer)"
  default     = "https://oficina.local/auth/cpf"
}

variable "jwt_secret" {
  type        = string
  description = "Mesmo segredo HS256 da app (JWT_CPF_SECRET / security.cpf-jwt.secret)"
  sensitive   = true
}

variable "jwt_expiration_seconds" {
  type        = string
  description = "TTL do token na Lambda"
  default     = "3600"
}

variable "lambda_subnet_ids" {
  type        = list(string)
  description = "Subnets para Lambda em VPC (vazio = Lambda sem VPC; RDS precisa aceitar conexão desse modo ou usar IPs públicos/SG)"
  default     = []
}

variable "lambda_security_group_ids" {
  type        = list(string)
  description = "Security groups da Lambda em VPC"
  default     = []
}
