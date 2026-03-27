# READMEs para o primeiro commit (4 repositórios)

Copie o ficheiro correspondente para **`README.md`** na raiz de cada repositório Git antes do primeiro push.

| Repositório GitHub | Ficheiro aqui |
|--------------------|---------------|
| `oficina-auth-lambda` | [oficina-auth-lambda-README.md](oficina-auth-lambda-README.md) |
| `oficina-infra-database` | [oficina-infra-database-README.md](oficina-infra-database-README.md) |
| `oficina-infra-kubernetes` | [oficina-infra-kubernetes-README.md](oficina-infra-kubernetes-README.md) |
| `oficina-app` | [oficina-app-README.md](oficina-app-README.md) |

**PowerShell (exemplo):** na pasta do clone, após copiar o conteúdo do monorepo com `bootstrap-repos.ps1`, podes substituir só o README:

```powershell
Copy-Item "c:\wks\oficina-springboot-mvp\docs\fase3\readmes-primeiro-commit\oficina-auth-lambda-README.md" "c:\wks\oficina-fase3-repos\oficina-auth-lambda\README.md" -Force
```

Repita para os outros três nomes de ficheiro e pastas.
