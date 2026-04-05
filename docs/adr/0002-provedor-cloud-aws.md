# ADR 0002 - Provedor cloud AWS para Fase 3

## Status

Proposto (revisar quando houver conta e orcamento)

## Contexto

E necessario API Gateway, funcao serverless, BD gerido, Kubernetes e integracao com observabilidade. A equipe precisa de um padrao unico para o video e para os professores.

## Decisao

Usar **AWS** como provedor principal para Fase 3:

- **API Gateway** HTTP para borda.
- **Lambda** para a funcao serverless de autenticacao.
- **RDS** PostgreSQL para BD gerido.
- **EKS** (ou **ECS Fargate** se o time simplificar K8s) para a aplicacao; o enunciario pede **cluster Kubernetes** - preferencia **EKS**.

Se o orcamento ou o tempo impedirem EKS, registrar **ADR de revisao** (ex.: k3s em EC2) com justificativa formal.

## Consequencias

- Custos de conta AWS; necessidade de IAM, VPC e secrets bem definidos.
- Alinhamento com documentacao oficial e exemplos de mercado.
