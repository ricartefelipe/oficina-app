# Tech Challenge — Fase 3 — Documento para entrega no portal (PDF)

**Uso:** converta este arquivo para PDF (veja a seção [Como gerar o PDF](#como-gerar-o-pdf)) e envie pelo portal do aluno, conforme o enunciado da disciplina.

---

## 1. Identificação

| Campo | Valor |
|-------|--------|
| **Aluno** | *(preencher: nome completo no portal acadêmico)* |
| **Conta GitHub (autor dos repositórios)** | `ricartefelipe` |
| **Disciplina / turma** | *(preencher)* |
| **Modalidade** | Trabalho **individual** (sem equipe) |

---

## 2. Links dos quatro repositórios (Fase 3)

Todos **privados**, com acesso de leitura para o usuário **`soat-architecture`** (GitHub).

| # | Repositório | Descrição breve |
|---|-------------|-----------------|
| 1 | https://github.com/ricartefelipe/oficina-auth-lambda | Função serverless (Python) — validação CPF e JWT |
| 2 | https://github.com/ricartefelipe/oficina-infra-database | Terraform — VPC, RDS opcional, CI |
| 3 | https://github.com/ricartefelipe/oficina-infra-kubernetes- | Terraform — cluster Kind (lab) + CI |
| 4 | https://github.com/ricartefelipe/oficina-app | Aplicação Spring Boot — container, K8s, CI/CD |

**Monólito / documentação central (evolução Fase 1–2 e referência técnica):**  
https://github.com/ricartefelipe/oficina-springboot-mvp — branches **`main`** (estável) e **`develop`** (integração), nomes literais no Git.

---

## 3. Confirmação — usuário `soat-architecture`

- O usuário **`soat-architecture`** foi convidado (ou equipe equivalente) em **todos** os repositórios listados na tabela acima, com permissão de **leitura** (ou conforme instrução do professor).
- **Como verificar:** em cada repositório → **Settings** → **Collaborators** / **Manage access** → confirmar presença de `soat-architecture`.

---

## 4. Componente vídeo (não aplicável nesta entrega)

O enunciado da Fase 3 prevê vídeo de **até 15 minutos**. **Nesta entrega opta-se por não incluir vídeo**, conforme orientação do aluno.  
**Evidências substitutas** para demonstração de funcionamento e infraestrutura estão na **seção 6** (links de CI, Terraform e observabilidade).

*(Se o professor exigir vídeo obrigatório, grave um único arquivo e cole o link aqui.)*

---

## 5. Links da documentação técnica

| Documento | URL (GitHub) |
|-----------|----------------|
| Índice Fase 3 (diagramas, backlog, execução) | `https://github.com/ricartefelipe/oficina-springboot-mvp/tree/main/docs/fase3` |
| ADRs | `https://github.com/ricartefelipe/oficina-springboot-mvp/tree/main/docs/adr` |
| RFC autenticação CPF / JWT | `https://github.com/ricartefelipe/oficina-springboot-mvp/blob/main/docs/fase3/rfc/rfc-0001-autenticacao-cpf-jwt-serverless.md` |
| Padrões de arquitetura (ArchUnit, camadas) | `https://github.com/ricartefelipe/oficina-springboot-mvp/blob/main/docs/development/architecture-standards.md` |
| Guia “terminar Fase 3” | `https://github.com/ricartefelipe/oficina-springboot-mvp/blob/main/docs/fase3/TERMINAR-FASE3.md` |
| Snippet Terraform API Gateway + Lambda | `https://github.com/ricartefelipe/oficina-springboot-mvp/blob/main/docs/fase3/terraform-snippet-api-gateway.tf` |

---

## 6. Demonstração da infraestrutura **sem vídeo** (modo individual)

Substituição aceitável: **prova documental** com links e, opcionalmente, **capturas de tela** anexadas ao PDF.

### 6.1 CI/CD (GitHub Actions)

Links verificados em **2026-04-05** (execuções com `success`). Se o Actions tiver runs mais recentes, atualize na cópia enviada ao portal.

| Repositório | O que demonstra | Evidência (link para run com sucesso) |
|-------------|-----------------|----------------------------------------|
| oficina-infra-database | Terraform (validação em CI) | https://github.com/ricartefelipe/oficina-infra-database/actions/runs/23996191967 |
| oficina-app | CI (build, testes, Docker) | https://github.com/ricartefelipe/oficina-app/actions/runs/23996171271 |
| oficina-auth-lambda | CI (Python) | https://github.com/ricartefelipe/oficina-auth-lambda/actions/runs/23996185747 |
| oficina-infra-kubernetes- | Terraform (Kind) | https://github.com/ricartefelipe/oficina-infra-kubernetes-/actions/runs/23996196664 |
| oficina-springboot-mvp | CI monólito (ArchUnit, testes) — referência | https://github.com/ricartefelipe/oficina-springboot-mvp/actions/runs/23996164027 |

### 6.2 Infraestrutura como código

- **Banco de dados (RDS):** código Terraform em `oficina-infra-database`. O workflow **Terraform AWS** no GitHub Actions pode executar **`terraform plan`** (execução manual); em todo push para `main`/`develop` roda **validação Terraform** em CI (evidência na tabela 6.1). **`apply`** na AWS é opcional por custo — se não aplicou, permanece *validação em CI*.
- **Kubernetes:** Terraform **Kind** em `oficina-infra-kubernetes-` (run verde na 6.1); manifestos da app em `oficina-app` (`k8s/`).
- **API Gateway + Lambda:** código em `oficina-auth-lambda`; integração HTTP documentada — snippet em `docs/fase3/terraform-snippet-api-gateway.tf` no monólito. Deploy completo na conta AWS é **opcional** se o custo for limitante; o repositório e o CI cobrem a **definição** da infra e da função.

### 6.3 Observabilidade

- **Métricas Prometheus** e métricas de negócio documentadas em `docs/fase3/observabilidade-prometheus.md`.
- **Logs JSON** (perfil `k8s`) e **correlation id** — referência em `architecture-standards` e código.
- *(Opcional)* anexar **1–2 capturas** de tela: Grafana, Prometheus ou console AWS, se existirem.

### 6.4 Autenticação CPF → Lambda → JWT → API

- Fluxo descrito na **RFC** e no código (`auth-lambda`, `ClienteSessaoController`, configuração JWT no Spring Boot).
- **Prova textual:** descrever **um** fluxo de teste manual (Postman/curl) ou **teste automatizado** referenciado no repositório.

---

## 7. Mapeamento rápido — requisitos Fase 3 (enunciado)

| Requisito | Onde está coberto |
|-----------|-------------------|
| 4 repositórios + CI/CD | Seção 2 + 6.1 |
| API Gateway + Lambda | Código + snippet Terraform; deploy AWS conforme seção 6 |
| BD gerenciado + K8s (Terraform) | Repositórios `oficina-infra-database` e `oficina-infra-kubernetes-` |
| Observabilidade | Métricas, logs, docs; seção 6.3 |
| Documentação (diagramas, RFC, ADR) | Seção 5 |
| `soat-architecture` | Seção 3 |

---

## Como gerar o PDF

1. Gere o PDF a partir deste `.md` (não há PDF versionado obrigatório no repositório; o arquivo final é o que você sobe no portal).
2. **Alternativa Word:** exporte o conteúdo para `.docx` e salve como PDF.
3. **Regenerar o PDF a partir deste `.md` (Windows, Edge):**  
   `pandoc entrega-portal-fase3.md -o _tmp.html --standalone`  
   depois Edge em modo headless:  
   `"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" --headless --disable-gpu --print-to-pdf=entrega-portal-fase3.pdf file:///C:/caminho/completo/_tmp.html`  
   Ou use o script em `scripts/delivery/md-to-pdf-edge.ps1` na raiz do monólito, apontando para este arquivo.

---

*Documento para suportar a entrega da Fase 3 em modo individual, com evidências substitutas ao vídeo.*
