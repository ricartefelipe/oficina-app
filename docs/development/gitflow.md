# Fluxo de branches e integração

## Princípios

- `main` e `master` permanecem como referência de releases estáveis; alterações de trabalho não são feitas diretamente nelas.
- `develop` concentra integração contínua das entregas; trabalho diário converge para `develop` via pull request.
- Cada entrega coesa fica em branch própria com prefixo `feature/` e nome em kebab-case curto e descritivo.
- Integração ocorre somente por pull request revisado; não há merge local direto de feature em `develop` fora do fluxo acordado no repositório remoto.

## Nomenclatura

- Padrão: `feature/<escopo-curto-kebab-case>`
- Exemplos válidos: `feature/hexagonal-adapters`, `feature/os-listagem-priorizada`, `feature/notificacao-email`
- Evite nomes genéricos como `feature/update` ou `feature/fix`

## Ciclo sugerido

1. Atualizar referências locais: `git fetch origin`
2. Criar ou atualizar a branch a partir de `develop`: `git checkout develop` e `git pull origin develop`, depois `git checkout -b feature/<nome>`
3. Commits pequenos, mensagens no imperativo e alinhadas a convenção semântica (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `build:`, `ci:`)
4. Abrir pull request para `develop` com descrição objetiva do escopo, validação executada e impactos
5. Após merge em `develop`, remover a branch remota quando não for mais necessária

## Commits

- Uma mensagem deve descrever uma mudança coerente; prefira vários commits pequenos a um único commit difuso
- Não inclua metadados de ferramentas, editores ou processos automatizados nas mensagens

## Base para releases

- Quando houver necessidade de estabilizar uma linha para entrega formal, o coordenador do repositório pode promover `develop` para `main` via pull request ou merge controlado, conforme política do time

## Fase 3 (multi-repositorio)

- Cada um dos **quatro** repositorios deve ter `main` (ou `master`) **protegida**, **sem push direto**; integracao apenas via **Pull Request**.
- Branches de **homologacao** e **producao** com **deploy automatico** ficam definidas nas pipelines de cada repo (nomes de branch: `develop`, `hml`, `release/*` ou `main` conforme politica acordada).
- Features neste repositorio (app) seguem: `feature/<nome-kebab>` a partir de `develop` atualizado, PR para `develop`, depois release para `main` quando estavel.
- Decisoes de fronteira e ordem de deploy estao em [`docs/fase3/backlog-fase3.md`](../fase3/backlog-fase3.md).
