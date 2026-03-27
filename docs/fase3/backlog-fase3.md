# Backlog Fase 3 (ordenado por dependencia)

## Fase A - Fundacao (sem bloqueio de cloud)

1. Congelar decisoes em ADR e RFC (auth, cloud, APM, gateway).
2. Criar os quatro repositorios remotos vazios com README inicial, branch `develop` e `main`, protecoes e template de PR.
3. Repositorio **BD**: Terraform de VPC minima + subnet group + RDS PostgreSQL + outputs (host, porta, secret manager).
4. Repositorio **K8s**: Terraform de cluster + node groups + add-ons (ingress, metrics server se necessario).
5. Repositorio **Lambda**: função minima + deploy + testes de unidade + integracao com BD de leitura.
6. Gateway na borda conectando **POST token** a Lambda e **rotas** ao ingress do cluster.

## Fase B - Aplicacao

7. Ajustar app para validar JWT emitido pela Lambda (JWKS ou segredo partilhado documentado).
8. Endpoints sensiveis protegidos conforme RFC; rotas publicas mantidas onde o dominio exigir.
9. Pipelines: **hml** e **prd** com deploy automatico a partir das branches definidas na politica do time.

## Fase C - Observabilidade

10. Logs JSON estruturados com correlacao (ja existe filtro base; estender).
11. Metricas RED/USE ou equivalente; dashboards: volume OS/dia, tempo medio por status, erros de integracao.
12. Alertas para falhas no processamento de OS.

## Fase D - Documentacao e entrega

13. Diagramas finais (componentes, sequencia, ER atualizado).
14. RFCs e ADRs fechados.
15. Video **<= 15 min** e PDF unico no portal com links dos quatro repos + video + documentacao.

## Criterio de pronto (Fase 3)

- Quatro repositorios com CI/CD funcional e README completo.
- Fluxo CPF - Lambda - JWT - API protegida demonstravel.
- Dashboards e alertas minimos conforme enunciario.
- `soat-architecture` em todos os repos.

## Estado neste monorepo (referencia)

Trabalho que **ja** esta no codigo ou documentacao de `oficina-springboot-mvp` antes da separacao em quatro repositorios remotos:

| Itens | Coberto |
|-------|---------|
| 1 | ADRs e RFC em `docs/adr/`, `docs/fase3/rfc/`; decisoes em `visao-arquitetura-fase3.md` |
| 5 | `auth-lambda/` + CI `.github/workflows/auth-lambda-ci.yml` |
| 7 | JWT CPF (HS256) + `MultiIssuerJwtDecoder`; ver `security.cpf-jwt` |
| 8 | `SecurityConfig` + rotas publicas/admin/cliente; `ClienteSessaoController` |
| 10 | Perfil `k8s` log JSON; `CorrelationIdFilter`; extensao futura: Grafana/OTel |
| 11 | Prometheus + metrica `oficina.os.criadas`; dashboards cloud ainda por definir |
| — | ArchUnit + `docs/development/architecture-standards.md` |

**Pendente fora deste repo:** itens 2–4 (Terraform BD/K8s isolados), 6 (API Gateway), 12 (alertas), 13–15 (entrega final e video).

**Pipelines por branch (item 9):** workflows e script `publish-fase3-repos.ps1` no monorepo; após criar os quatro repos, criar branches `hml`/`prd` e configurar GitHub Environments + `KUBE_CONFIG_B64` (ver [`executar-fase3.md`](executar-fase3.md)).
