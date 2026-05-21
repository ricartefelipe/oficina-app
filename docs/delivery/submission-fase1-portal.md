# Tech Challenge — Fase 1 — Documento de Entrega (MVP Back-end Oficina)

## Identificação do Grupo
- **Nome do grupo:** Oficina Turbo
- **Código/identificador interno:** Grupo 106

## Participantes (nome + Discord)
- **Felipe Ricarte Magalhães** — Discord: **@felipe.ricarte**

## Links do Projeto
- **Repositório (privado):** https://github.com/ricartefelipe/oficina-springboot-mvp
    - **Permissão:** usuário **soat-architecture** adicionado com acesso ao repositório.
- **Swagger (local):** http://localhost:8080/api/swagger-ui/index.html

## Resumo do Entregável (Fase 1)
MVP do back-end (monolítico em camadas) do Sistema Integrado de Atendimento e Execução de Serviços para oficina mecânica, incluindo:
- CRUDs administrativos: clientes, veículos, serviços, peças/insumos (estoque)
- Gestão de Ordens de Serviço: criação completa, orçamento automático, transições de status, listagem e detalhamento
- Consulta pública da OS via `trackingCode` e aprovação de orçamento com validação adicional (CPF/CNPJ)
- Segurança: JWT para endpoints administrativos via Keycloak (role ADMIN)
- Métrica: tempo médio de execução (EM_EXECUCAO → FINALIZADA)
- Dockerfile + docker-compose para execução local simples
- Testes unitários e integração + cobertura mínima configurada via JaCoCo
- Documentação DDD (Event Storming, diagramas e linguagem ubíqua) + roteiro do vídeo

## Relatório de Vulnerabilidades (scan)
- **Arquivo do relatório:** `docs/security/vulnerability-report.md`
- **Evidências geradas pelo scan:** `build/security/*`
- **Resumo (valores de exemplo):**
    - CRITICAL: 0
    - HIGH: 0
    - MEDIUM: 2
    - LOW: 6
- **Plano de mitigação:** descrito no relatório (upgrade de dependências, ajustes de configuração e revisão contínua no pipeline).

## Como executar localmente (para o avaliador)
> Pré-requisito: Docker + Docker Compose v2

1) Subir o ambiente:
```bash
docker compose up --build
```

2) Obter token ADMIN (Keycloak):
```bash
export TOKEN="$(./scripts/get-admin-token.sh)"
```

3) Validar endpoints (exemplo):
```bash
curl -i http://localhost:8080/api/admin/clientes \
  -H "Authorization: Bearer ${TOKEN}"
```

4) Rodar testes e cobertura:
```bash
mvn verify
```

5) Rodar scan de vulnerabilidades e gerar evidências:
```bash
./scripts/security/run-security-scans.sh
```
