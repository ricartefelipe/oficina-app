# Scripts Fase 3

| Ficheiro | Descrição |
|----------|-----------|
| [`bootstrap-repos.ps1`](bootstrap-repos.ps1) | Gera quatro pastas-gêmeas do monorepo (repos separados) sob `oficina-fase3-repos` por defeito; branch inicial `main`; na app inclui CI + `deploy-k8s-branch.yml`. |
| [`publish-fase3-repos.ps1`](publish-fase3-repos.ps1) | Cria os quatro repositórios no GitHub com `gh repo create` e faz o primeiro push (requer GitHub CLI). |
| [`setup-github-oidc-aws.ps1`](setup-github-oidc-aws.ps1) | **Configuração rápida OIDC:** IAM provider + role com trust só no teu repo; imprime o ARN para o secret `AWS_ROLE_ARN` (requer AWS CLI). |
| [`templates/`](templates/) | Modelos de CI, Terraform e deploy (`deploy-k8s-branch.yml` para `hml`/`prd`). |

Documentação do fluxo completo: [`../../docs/fase3/executar-fase3.md`](../../docs/fase3/executar-fase3.md).
