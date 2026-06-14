# Terminar a Fase 3 (guia simples)

**Status:** entrega AWS ativa — RDS + Lambda + EKS (`sa-east-1`), observabilidade no cluster, PDF no portal.

---

## O que foi entregue

| # | Requisito | Estado |
|---|-----------|--------|
| A | 4 repos + CI/CD | OK — links na seção 6.3 de [`entrega-portal-fase3.md`](../delivery/entrega-portal-fase3.md) |
| B | API Gateway + auth CPF → JWT | OK — API Gateway HTTP + Lambda `oficina-auth-cpf-fn` |
| C | BD + Kubernetes (Terraform) | OK — RDS (`oficina-infra-database`) + EKS (`aws-eks/`) |
| D | Observabilidade | OK — Prometheus + Grafana no EKS; logs JSON |
| E | Documentação | OK — `docs/fase3/`, ADRs, RFC |
| F | PDF portal + soat-architecture | PDF em `docs/delivery/`; convite ao avaliador feito |
| G | Vídeo ≤ 15 min | Pendente — negociacao com professor |

---

## Como demonstrar (AWS)

```bash
./scripts/fase3/deploy-aws-eks-app.sh   # redeploy se necessario
```

Token: `POST https://i3te2gzkmk.execute-api.sa-east-1.amazonaws.com/token` com `{"cpf":"52998224725"}`  
API: Load Balancer EKS — URLs em [`entrega-portal-fase3.md`](../delivery/entrega-portal-fase3.md)  
Observabilidade: `kubectl port-forward -n oficina svc/prometheus 9090:9090` e `svc/grafana 3000:3000`

---

## Laboratorio local (opcional)

```bash
./scripts/fase3/deploy-local-kind.sh
```

Gateway: http://localhost:8088

---

## PDF no portal

Enviar **`docs/delivery/entrega-portal-fase3.pdf`** no portal do aluno (tambem em `docs/delivery/` no GitHub).

---

## Referências

| Tema | Arquivo |
|------|---------|
| Entrega portal | [`../delivery/entrega-portal-fase3.md`](../delivery/entrega-portal-fase3.md) |
| Manifestos K8s AWS | [`../../k8s/aws/`](../../k8s/aws/) |
| Observabilidade | [`observabilidade-prometheus.md`](observabilidade-prometheus.md) |
