# oficina-infra-kubernetes

Infraestrutura como código (**Terraform**) para **Kubernetes** (Tech Challenge Fase 3 — SOAT).

## Propósito

- Provisionar cluster **Kubernetes** (ex.: **Kind** para laboratório local; na nuvem: **EKS** ou equivalente conforme evolução da equipa).
- Preparar base para **Ingress**, métricas e integração com API Gateway / load balancer.

## Stack

- Terraform >= 1.5
- Provider **Kind** (Docker) para ambiente local documentado no enunciário

## Pré-requisitos

- [Terraform](https://www.terraform.io/)
- [Docker](https://www.docker.com/) (para Kind)
- [Kind](https://kind.sigs.k8s.io/) (instalado pelo provider ou host)

## Execução local

```bash
terraform init
terraform plan
terraform apply
```

Ajustar `variables.tf` / `terraform.tfvars` conforme o repositório.

## CI

- `terraform fmt -check`, `init -backend=false`, `validate` em PR.
- `apply` apenas em pipeline controlada (hml/prd) com credenciais.

## Diagrama (repositório)

```text
[Terraform] -> [Cluster K8s]
                  |
                  v
            [Ingress / Services]
                  ^
                  |
            [oficina-app - outro repo]
```

## Relação com a app

- Manifestos da aplicação (Deployment, Service, HPA) podem viver no repositório **`oficina-app`** ou ser referenciados aqui — alinhar com a equipa.

## Convite

Adicionar **`soat-architecture`** com leitura (portal SOAT).
