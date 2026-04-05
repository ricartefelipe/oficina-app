# Terminar a Fase 3 (guia simples)

Fases **1** e **2** já cumpriram o MVP, a arquitetura hexagonal, K8s, Terraform e CI no monólito.  
A **Fase 3** pede **mais quatro coisas** em cima disso: **4 repositórios**, **cloud “de serviço”** (Gateway + Lambda + BD + cluster), **observabilidade** e **entrega** (docs + vídeo + PDF).

---

## 1) O que o PDF exige (lista curta)

| # | O que é | Estado típico no seu projeto |
|---|---------|------------------------------|
| A | **4 repos** (Lambda, Terraform K8s, Terraform BD, App) com CI/CD | Repositórios existem; CI em execução; Terraform BD com **plan** OK (chaves IAM). |
| B | **API Gateway** + **Lambda** (CPF → JWT) + rotas protegidas na app | Lambda em `auth-lambda/`; **Gateway na AWS** = falta **criar na conta** (ver snippet Terraform em [`terraform-snippet-api-gateway.tf`](terraform-snippet-api-gateway.tf)). |
| C | **BD gerenciado** + **Kubernetes** (Terraform) | Código em `infra/`; **RDS/cluster reais** = `terraform apply` + custos. |
| D | **Observabilidade** (métricas, logs JSON, dashboards, **alertas** OS) | Prometheus + logs no app; **dashboards/alertas na ferramenta** = Grafana/console + regras (ver [`observabilidade-prometheus.md`](observabilidade-prometheus.md)). |
| E | **Documentação** (diagramas, RFC, ADR, ER BD) | Grande parte em `docs/`; **revisar** e juntar no PDF. |
| F | **PDF único** no portal (4 links + vídeo + **soat-architecture**) | **PDF** com base em [`entrega-portal-fase3.md`](entrega-portal-fase3.md). **Vídeo** pode ser omitido; use **evidências substitutas** (links CI, prints) na seção 6 desse documento. |

---

## 2) Ordem prática (o que fazer agora)

1. **Confirmar** `soat-architecture` em **todos** os 4 repositórios GitHub.  
2. **Terraform BD**: se ainda só fez **plan**, decida **sozinho** se faz **`apply`** (RDS custa dinheiro).  
3. **API Gateway**: deploy da Lambda na AWS (SAM ou zip) → depois **API HTTP** apontando para a Lambda (arquivo de exemplo no repo).  
4. **App no K8s**: imagem no GHCR + manifestos `k8s/` + cluster (Kind local ou EKS na AWS).  
5. **Observabilidade**: métricas/logs documentados; opcional **capturas** para o PDF (ver `entrega-portal-fase3.md`).  
6. **PDF único** (sem vídeo, se preferir): preencha **[`entrega-portal-fase3.md`](entrega-portal-fase3.md)** — links dos 4 repos, runs do Actions, `soat-architecture`, evidências da seção 6.  
7. **Vídeo** (opcional): só se o professor exigir; caso contrário, as evidências na seção 6 substituem a demonstração gravada.

---

## 3) Modo individual (sem equipe)

- **Não é obrigatório** vídeo para “provar” infra: use **links de CI** + **descrição do Terraform** + **prints** no PDF (modelo em [`entrega-portal-fase3.md`](entrega-portal-fase3.md)).  
- **Decisão de custo** na AWS (`apply`) é **sua** (RDS, cluster).  
- O **upload do PDF** no portal é **manual**; o conteúdo do PDF prepara-se a partir do arquivo acima.

---

## 4) Onde está a documentação técnica

| Tema | Arquivo |
|------|---------|
| Plano de execução e scripts | [`executar-fase3.md`](executar-fase3.md) |
| Backlog e prioridades | [`backlog-fase3.md`](backlog-fase3.md) |
| Visão e diagramas | [`visao-arquitetura-fase3.md`](visao-arquitetura-fase3.md) |
| OIDC / secrets | [`aws-oidc-github.md`](aws-oidc-github.md) |
| Prometheus / Grafana | [`observabilidade-prometheus.md`](observabilidade-prometheus.md) |
| Snippet API Gateway (Terraform) | [`terraform-snippet-api-gateway.tf`](terraform-snippet-api-gateway.tf) |

---

**Resumo:** “Terminar a Fase 3” = **infra demonstrável** (código + CI + plan/apply conforme possível) + **documentação** + **PDF** (modelo `entrega-portal-fase3.md`). **Vídeo** opcional; evidências **substitutas** no próprio PDF.
