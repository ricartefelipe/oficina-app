# Diagnóstico de lacunas e backlog Fase 2

## Fontes

- Enunciado oficial da Fase 2 (requisitos de APIs, infraestrutura, entregáveis e critérios de avaliação)
- Feedback acadêmico da Fase 1 (reforço de DDD visual, testes e clareza de artefatos)
- Estado atual do repositório `oficina-springboot-mvp` (Java 21, Spring Boot, PostgreSQL, Liquibase, Keycloak, Docker)

## Situação atual resumida

- **Domínio e fluxo**: bounded contexts (`cadastros`, `catalogo`, `ordemservico`, `shared`); OS com máquina de estados, histórico, orçamento automático, tracking code, baixa de estoque na execução, métricas
- **Segurança**: JWT com Keycloak para admin; rotas públicas para consulta e aprovação com validações
- **Persistência**: JPA + Liquibase; repositórios por contexto
- **Testes**: testes de domínio e integração presentes; cobertura ainda abaixo do desejado para Fase 2 em endpoints e erros
- **Documentação DDD**: [índice `docs/ddd/README.md`](../ddd/README.md) com **Domain Storytelling**, **Dicionário de linguagem ubíqua** (tabela), Event Storming (incl. fluxos alternativos) e diagramas **SVG** em `docs/ddd/diagrams/` (incl. lousa com elementos C/A/E/P/R) — alinhado ao feedback da Fase 1
- **Infra**: Dockerfile e docker-compose; `/k8s`; Terraform **AWS** (rede + RDS opcional) e **Kind local** em `infra/kind`; CI Maven + Terraform validate (AWS + Kind) + GHCR; **workflows manuais** Deploy Kubernetes e Terraform AWS; **EKS** na AWS não está no Terraform (cluster gerido seria evolução)
- **README**: alinhado aos entregáveis da Fase 2 (objetivos, arquitetura, fluxo de deploy, links Swagger/OpenAPI; URL do vídeo na tabela **Links rápidos** quando publicado)

## Lacunas em relação à Fase 2

| Área | Estado |
|------|--------|
| Código, K8s, Terraform, CI/CD no repo | **Atendido** — ver [`docs/delivery/fase2-concluida.md`](../delivery/fase2-concluida.md) |
| Arquitetura | Hexagonal em evolução incremental (bounded contexts); sem lacuna bloqueante para o enunciário |
| Testes | Cobertura JaCoCo em `domain` ≥80%; testes HTTP ampliados (métricas, health, admin OS, público, JWT) |
| DDD | Artefatos em [`docs/ddd/README.md`](../ddd/README.md) |
| CI/CD | `deploy-kubernetes.yml`: apply + rollout + smoke; `terraform-aws.yml` manual; **EKS** não incluído (documentado) |
| Entrega académica | **Manual:** PDF no portal, vídeo (≤15 min), **soat-architecture** — [`submission.md`](../delivery/submission.md#checklist-entrega-fase-2) |

## Backlog fatiado (ordem sugerida)

1. **hexagonal-structure**: reorganização incremental para domain, application, adapters e ports preservando testes - feito
2. **os-abertura-contract-tests**: revisão de abertura de OS, identificador único e testes de contrato - feito
3. **os-status-consulta**: consulta de status alinhada à máquina de estados e testes - feito
4. **orcamento-notificacao-externa**: endpoint idempotente para aprovação ou recusa externa, histórico e validação de transição - feito (`POST /admin/ordens-servico/{id}/orcamento/resposta-externa`, status `CANCELADA` na recusa)
5. **os-listagem-priorizada**: ordenação EM_EXECUCAO > AGUARDANDO_APROVACAO > EM_DIAGNOSTICO > RECEBIDA; mais antigas primeiro; excluir FINALIZADA e ENTREGUE - feito (exclui também `CANCELADA`; parâmetro `incluirEncerradas`)
6. **notificacao-email-mailhog**: porta de notificação, adapter SMTP, eventos em transições relevantes, compose atualizado - feito (`NotificacaoOrdemServicoPort`, SMTP/MailHog, `NOTIFICATION_ENABLED`, `docker-compose` com MailHog)
7. **testes-ampliados**: happy path, erros, estoque, endpoints principais, idempotência - **feito** (incl. `AdminMetricasControllerHttpTest`, `HealthControllerHttpTest`, validações admin OS; integração Docker/Testcontainers opcional local)
8. **kubernetes**: namespace, deployment, service, ConfigMap, Secret, probes, recursos, HPA, documentação de apply e rollback - feito (`k8s/`, `k8s/README.md`)
9. **terraform**: módulos ou stacks reproduzíveis, documentação de apply e destroy - feito (`infra/`: rede AWS + **RDS opcional** `enable_rds`, `infra/README.md`, `infra/docs/terraform-vs-enunciado.md`)
10. **cicd**: **feito** — `ci.yml` (Maven + Terraform + GHCR em push); **Deploy Kubernetes** (`kubectl apply` + rollout + smoke); **Terraform AWS** manual; EKS fora do repo
11. **ddd-visual-artifacts**: SVG em `docs/ddd/diagrams/` + Domain Storytelling + dicionário tabela + lousa Event Storming — feito; Mermaid em `diagramas.md`; **regenerar PDFs** do portal após alterações
12. **documentacao-readme-fase2**: README + [`fase2-concluida.md`](../delivery/fase2-concluida.md); **link do vídeo**, PDF no portal e convite **soat-architecture** - [`submission.md`](../delivery/submission.md#checklist-entrega-fase-2)

## Riscos e mitigação

- **Refatoração ampla**: fatiar por contexto ou por camada e manter testes verdes a cada passo
- **Pipeline e cluster**: quando não houver cluster remoto, documentar kind ou equivalente e limites do que a pipeline valida de ponta a ponta
