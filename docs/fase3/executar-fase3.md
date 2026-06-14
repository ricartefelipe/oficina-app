# Executar a Fase 3 (o que é automático vs. manual)

Este documento separa o que o **repositório pode preparar** (arquivos e script) do que **depende da sua conta** (AWS, GitHub, convites).

## Deploy AWS (entrega Fase 3)

Ordem recomendada:

1. **RDS** — repo `oficina-infra-database`: `terraform apply` com `enable_rds=true`
2. **Lambda auth** — `oficina-app/infra/terraform/aws-auth-lambda`
3. **EKS** — repo `oficina-infra-kubernetes-`, pasta `aws-eks/`
4. **App + observabilidade** — `./scripts/fase3/deploy-aws-eks-app.sh`

Evidencias e URLs: [`docs/delivery/entrega-portal-fase3.md`](../delivery/entrega-portal-fase3.md).

## Deploy local (laboratorio — custo zero)

```bash
./scripts/fase3/deploy-local-kind.sh
```

Ver [`k8s/README.md`](../../k8s/README.md) e [`docs/delivery/entrega-portal-fase3.md`](../delivery/entrega-portal-fase3.md).

## O que você já pode gerar localmente

Na raiz do monorepo, execute (PowerShell):

```powershell
Set-Location c:\wks\oficina-springboot-mvp
.\scripts\fase3\bootstrap-repos.ps1
```

Por padrão cria a pasta `c:\wks\oficina-fase3-repos\` (ao lado do monorepo, **não** dentro dele) com quatro pastas:

> **Importante:** se `DestinationRoot` estiver **dentro** do monorepo, o robocopy pode copiar a própria pasta de saída e gerar uma cópia gigante. Use o caminho padrão ou outro diretório **fora** de `oficina-springboot-mvp`.

| Pasta | Conteúdo |
|-------|----------|
| `oficina-auth-lambda` | Código copiado de `auth-lambda/`, CI na raiz |
| `oficina-infra-database` | Stack Terraform AWS (VPC + RDS opcional), sem `kind/` |
| `oficina-infra-kubernetes` | Stack Terraform Kind (laboratório); roadmap EKS no README |
| `oficina-app` | Cópia do código da aplicação (exclui `.git`, `target`, `.terraform`) + CI + **deploy K8s por branch** |

Opções úteis:

```powershell
# Só README na app (sem copiar o código inteiro)
.\scripts\fase3\bootstrap-repos.ps1 -SkipAppCopy

# Outro destino
.\scripts\fase3\bootstrap-repos.ps1 -DestinationRoot D:\repos\fase3
```

O bootstrap inicializa Git em cada pasta com branch **`main`** (primeiro commit local).

### Publicar os quatro repositórios no GitHub (CLI)

Com [GitHub CLI](https://cli.github.com/) instalado e sessão iniciada (`gh auth login`):

```powershell
.\scripts\fase3\publish-fase3-repos.ps1
```

- Por padrão cria repositórios **privados** e faz push da `main`. Use `-Public` se quiser repositórios públicos.
- `-Owner minha-org` se os repositórios forem na organização (requer permissões na org).
- Se o repositório remoto **já existir** no GitHub, o script associa `origin` e faz push em vez de falhar.

Alternativa manual: criar os quatro repositórios vazios no site e, em cada pasta, `git remote add origin git@github.com:OWNER/NOME.git` e `git push -u origin main`.

## Branches `hml` e `prd` (CI e deploy)

Política suportada pelos workflows copiados pelo bootstrap e pelo monólito:

| Branch | Papel típico | CI | Deploy automático (só **oficina-app**) |
|--------|----------------|-----|----------------------------------------|
| `develop` | integração | sim | não |
| `main` | linha estável | sim + imagem GHCR | não |
| `hml` | homologação | sim + imagem `ghcr.io/...:hml` | sim — ambiente **homologacao** |
| `prd` | produção | sim + imagem `ghcr.io/...:prd` | sim — ambiente **producao** |

1. Crie as branches a partir de `main`/`develop` (`git checkout -b hml`, `git push -u origin hml`, idem para `prd`).
2. No repositório **oficina-app** (ou no monólito), em **Settings → Environments**, crie **homologacao** e **producao**.
3. Em cada ambiente, defina o secret **`KUBE_CONFIG_B64`** (kubeconfig em Base64) do cluster correspondente. Opcional: aprovadores antes do deploy (regra de proteção do ambiente).
4. O workflow `deploy-k8s-branch.yml` roda **depois** do workflow `CI` concluir com sucesso no mesmo push (evita usar a imagem antes de existir no GHCR): aplica `k8s/*.yaml` e faz `kubectl set image` para `ghcr.io/<repo>:hml` ou `:prd`.

Repositórios **Lambda** e **Terraform** apenas rodam **validação CI** nessas branches (sem deploy automático neste modelo).

## O que só você (ou a equipe) pode fazer

### GitHub

1. Criar os quatro repositórios (vazios ou com README) na organização ou conta desejada — ou usar `publish-fase3-repos.ps1`.
2. Adicionar o usuário **`soat-architecture`** com permissão de leitura em **todos** os quatro.
3. **Branch protection** na branch `main`:
   - Exigir pull request antes do merge.
   - Exigir aprovação de revisão (se aplicável à equipe).
   - Opcional: exigir que os checks de CI passem.

Se usar [GitHub CLI](https://cli.github.com/) (`gh`), é possível aplicar regras semelhantes por API; o portal também permite configurar em **Settings → Branches → Branch protection rules**.

4. **Secrets** por repositório (exemplos):
   - Infra AWS com **OIDC** (recomendado): um único secret **`AWS_ROLE_ARN`** com o ARN do papel IAM (ver [`aws-oidc-github.md`](aws-oidc-github.md)).
   - Alternativa: `AWS_ACCESS_KEY_ID` + `AWS_SECRET_ACCESS_KEY` (sem `AWS_ROLE_ARN`).
   - Deploy K8s / app: `KUBE_CONFIG_B64` por **environment** (`homologacao` / `producao`) ou ao nível do repositório para testes.

### AWS

- Conta e permissões para VPC, RDS, Lambda, API Gateway, EKS (conforme o desenho da Fase 3).
- `terraform apply` e pipelines de deploy **consomem custo**; valide região e limites.

### Observabilidade

- Escolha de APM (ex.: integração com export OTLP já alinhada nas ADRs) e criação de dashboards/alertas na ferramenta escolhida — configuração típica em secrets e variáveis de ambiente no cluster.

### Entrega acadêmica

- PDF único, vídeo (≤15 min) e links dos quatro repositórios no portal — upload manual.

## Ordem sugerida

1. Rodar `bootstrap-repos.ps1` e rever os quatro diretórios.
2. Rodar `publish-fase3-repos.ps1` (ou criar repos e fazer push manualmente).
3. Configurar branch protection e `soat-architecture`.
4. Criar branches `hml`/`prd` e environments com `KUBE_CONFIG_B64` quando o cluster existir.
5. Adicionar secrets AWS e testar CI (sem `apply` destrutivo em produção até validar o plano).

Para o mapa de responsabilidades e stacks, ver também [`repositorios-planejados.md`](repositorios-planejados.md).
