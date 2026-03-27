# Notas Fase 3 (oficina-app)

- **Lambda** e **Terraform** estÃ£o em repositÃ³rios separados; este repo contÃ©m a aplicaÃ§Ã£o Spring Boot e artefactos relacionados (ex.: `k8s/`, Docker).
- CI publica imagem em GHCR; pushes em `hml` ou `prd` disparam `deploy-k8s-branch.yml` (requer secrets por ambiente â€” ver monorepo `docs/fase3/executar-fase3.md`).
- Adicionar **soat-architecture** como leitor.
