# Terminar a Fase 3 (guia simples)

**Status:** entrega preparada — stack local Kind (custo zero), PDF e evidências CI.

---

## O que foi entregue

| # | Requisito | Estado |
|---|-----------|--------|
| A | 4 repos + CI/CD | OK — links na seção 6.2 de [`entrega-portal-fase3.md`](../delivery/entrega-portal-fase3.md) |
| B | API Gateway + auth CPF → JWT | OK — Traefik + `auth-lambda` HTTP no Kind |
| C | BD + Kubernetes (Terraform) | OK — Postgres no cluster + Kind (`oficina-infra-kubernetes-`) |
| D | Observabilidade | OK — Prometheus + Grafana + logs JSON |
| E | Documentação | OK — `docs/fase3/`, ADRs, RFC |
| F | PDF portal + soat-architecture | PDF em `docs/delivery/`; convite ao avaliador feito |

---

## Como demonstrar (1 comando)

```bash
./scripts/fase3/deploy-local-kind.sh
```

Gateway: http://localhost:8088  
Token: `POST /token` com `{"cpf":"52998224725"}`  
API: `GET /api/cliente/sessao` com Bearer JWT  

---

## PDF no portal

Enviar **`docs/delivery/entrega-portal-fase3.pdf`** no portal do aluno (também disponível no repositório em `docs/delivery/`).

---

## Referências

| Tema | Arquivo |
|------|---------|
| Entrega portal | [`../delivery/entrega-portal-fase3.md`](../delivery/entrega-portal-fase3.md) |
| Manifestos K8s | [`../../k8s/README.md`](../../k8s/README.md) |
| Observabilidade | [`observabilidade-prometheus.md`](observabilidade-prometheus.md) |
