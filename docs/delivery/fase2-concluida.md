# Fase 2 — Critérios do enunciário atendidos (Tech Challenge)

Este documento consolida **o que o repositório implementa** em relação ao PDF **14SOAT - Fase 2 - Tech Challenge**. Passos só na tua conta (portal, vídeo, `soat-architecture`) estão no [checklist de submissão](submission.md#checklist-entrega-fase-2).

## Evolução da aplicação

| Requisito | Evidência no repo |
|-----------|-------------------|
| Clean Code / **Clean Architecture** ou **Hexagonal** | Pacotes `domain`, `application` (portas), `adapters.in.web`, `adapters.out.*`; README [secção arquitetura](../../README.md#2-arquitetura-e-ddd-bounded-contexts) |
| Testes automatizados (fluxos críticos) | `mvn -Pci verify`; JaCoCo **≥80%** em `**/domain/**` ([`pom.xml`](../../pom.xml)); testes HTTP em `src/test/java/.../adapters/in/web` e segurança em `AdminJwtSecurityWebMvcTest` |
| APIs Fase 2 (abertura OS, status, aprovação externa, listagem priorizada) | Rotas documentadas no Swagger; regras em [README](../../README.md#3-status-da-os-e-regras-principais) |

## Infraestrutura

| Requisito | Evidência |
|-----------|-----------|
| **Dockerfile** + **docker-compose** | Raiz do repositório |
| **Kubernetes** (Deployment, Service, ConfigMap, Secret, **HPA**) | [`k8s/`](../../k8s/) e [`k8s/README.md`](../../k8s/README.md) |
| **Terraform** (cluster + BD, local ou cloud) | [`infra/`](../../infra/) (AWS VPC + RDS opcional), [`infra/kind/`](../../infra/kind) (Kind); [`infra/docs/terraform-vs-enunciado.md`](../../infra/docs/terraform-vs-enunciado.md) |
| **CI/CD** (build, testes, imagem, deploy, manifestos) | [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml): Maven, Terraform validate (AWS + Kind), build/push **GHCR** em push; [deploy-kubernetes.yml](../../.github/workflows/deploy-kubernetes.yml): `kubectl apply`, rollout, smoke; [terraform-aws.yml](../../.github/workflows/terraform-aws.yml): plan/apply opcional |

## Documentação e DDD

| Requisito | Evidência |
|-----------|-----------|
| README com solução, arquitetura, deploy, execução local | [`README.md`](../../README.md) |
| Diagramas / DDD | [`docs/ddd/README.md`](../ddd/README.md) |
| Collection / **Swagger** | Tabela **Links rápidos** no README |

## Vídeo e portal (manual)

- Roteiro: [`docs/video-script.md`](../video-script.md) (blocos Fase 2 no início).
- PDF: [`docs/delivery/entrega-portal-fase2.md`](entrega-portal-fase2.md) + regenerar PDF após alterações.

## O que não está no repositório (explícito)

- **EKS** na AWS: o Terraform provisiona rede + RDS; cluster gerido EKS seria evolução (ver `terraform-vs-enunciado.md`).
- **Branch protection** e **convite** `soat-architecture`: configurar no GitHub (ver [submission.md](submission.md)).

---

**Última verificação local:** executar `mvn -B -Pci verify` na raiz antes de considerar a Fase 2 “fechada” tecnicamente.
