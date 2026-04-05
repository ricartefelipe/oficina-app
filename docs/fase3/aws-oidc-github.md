# AWS + GitHub com OIDC (passo a passo)

## Modo fácil (recomendado)

1. Instale o [AWS CLI](https://aws.amazon.com/cli/) e execute **`aws configure`** com uma chave IAM (só para este setup; depois o GitHub usa OIDC).
2. Na raiz do monorepo, no PowerShell:

```powershell
Set-Location c:\wks\oficina-springboot-mvp
.\scripts\fase3\setup-github-oidc-aws.ps1 -GitHubOwner "TEU_USER_OU_ORG" -GitHubRepo "NOME_DO_REPO"
```

Exemplo: `-GitHubOwner "ricartefelipe" -GitHubRepo "oficina-springboot-mvp"`.

3. O script imprime o **ARN** — cole no GitHub: **Settings → Secrets → Actions** → secret **`AWS_ROLE_ARN`**.

Script: [`scripts/fase3/setup-github-oidc-aws.ps1`](../../scripts/fase3/setup-github-oidc-aws.ps1) (cria o OIDC provider se faltar, cria/atualiza a role e anexa `PowerUserAccess` por padrão). Para confiar em mais do que um repositório na mesma role: `-GitHubReposExtra` (ex.: `-GitHubReposExtra "oficina-infra-kubernetes-"`).

### AWS CloudShell (sem AWS CLI no PC)

No console AWS, abra **CloudShell** (ícone terminal no topo). Você já tem credenciais IAM na sessão.

1. Copie o arquivo [`scripts/fase3/cloudshell-github-oidc.sh`](../../scripts/fase3/cloudshell-github-oidc.sh) para o CloudShell (ou cole o conteúdo num arquivo `cloudshell-github-oidc.sh`).
2. `chmod +x cloudshell-github-oidc.sh && ./cloudshell-github-oidc.sh`
3. Copie o **ARN** exibido e, no seu PC (com `gh auth login`):  
   `gh secret set AWS_ROLE_ARN -b "ARN_AQUI" -R ricartefelipe/oficina-infra-database`  
   Alternativa: uma linha com o ARN em `scripts/fase3/aws-role-arn.txt` no monorepo (arquivo ignorado pelo git).

---

## Modo manual (console AWS)

O workflow **Terraform AWS** neste repositório já usa `role-to-assume` e `id-token: write`. Falta apenas configurar **uma vez** na AWS e colar o ARN no GitHub.

Substitua nos exemplos abaixo:

| Marcador | O que é |
|----------|---------|
| `ACCOUNT_ID` | ID da conta AWS (12 dígitos). Console AWS → canto superior direito → **Account ID**, ou em **IAM → Dashboard**. |
| `OWNER` | Usuário ou organização GitHub (ex.: `ricartefelipe`). |
| `REPO` | Nome do repositório (ex.: `oficina-springboot-mvp`). |

---

## 1) Identity provider OIDC (uma vez por conta AWS)

Se ainda **não** existir:

1. Console AWS → **IAM** → **Identity providers** → **Add provider**.
2. **Provider type:** OpenID Connect.
3. **Provider URL:** `https://token.actions.githubusercontent.com`  
   (clique em **Get thumbprint** para validar).
4. **Audience:** `sts.amazonaws.com`  
5. **Add provider**.

Se já aparecer `token.actions.githubusercontent.com` na lista, **não** crie outro.

---

## 2) Criar a IAM Role com trust só no seu repositório

1. **IAM** → **Roles** → **Create role**.
2. **Trusted entity type:** **Custom trust policy**.
3. Cole o JSON abaixo (troque `ACCOUNT_ID`, `OWNER`, `REPO`).

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "GitHubActions",
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:OWNER/REPO:*"
        }
      }
    }
  ]
}
```

- `repo:OWNER/REPO:*` = qualquer workflow desse repositório (qualquer branch/tag).
- Para **só uma branch** (ex. `main`):

```json
"token.actions.githubusercontent.com:sub": "repo:OWNER/REPO:ref:refs/heads/main"
```

4. **Next** → **Permissions** → escolhe políticas para o Terraform (VPC, RDS, etc.).  
   Para **testes/lab**, você pode anexar **PowerUserAccess** ou até **AdministratorAccess** (evita bloqueios enquanto aprende; em produção use políticas mínimas).
5. **Next** → nome da role, ex.: `GitHubActionsTerraform` → **Create role**.
6. Abra a role → copie o **ARN** (ex.: `arn:aws:iam::123456789012:role/GitHubActionsTerraform`).

---

## 3) Secret no GitHub

1. Repositório GitHub → **Settings** → **Secrets and variables** → **Actions**.
2. **New repository secret**
   - **Name:** `AWS_ROLE_ARN`
   - **Secret:** cole o ARN completo da role (passo 2.6).

Não é necessário `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` para OIDC.

---

## 4) Rodar o workflow

**Actions** → **Terraform AWS** → **Run workflow** (é manual no monólito).

Se falhar no passo **Configure AWS (OIDC)**, quase sempre é:

| Problema | O que verificar |
|----------|------------------|
| `Not authorized to perform sts:AssumeRoleWithWebIdentity` | Trust policy: `OWNER`/`REPO` errados; ou Federated ARN com `ACCOUNT_ID` errado. |
| Provider OIDC em falta | Passo 1 (identity provider). |
| `aud` incorreto | Deve ser `sts.amazonaws.com` na condição **StringEquals** (como no JSON). |

---

## 5) O que já está no YAML (não precisa alterar)

- `permissions: id-token: write` e `contents: read`
- `aws-actions/configure-aws-credentials` com `role-to-assume: ${{ secrets.AWS_ROLE_ARN }}`

Arquivos: `.github/workflows/terraform-aws.yml` e, no repo de infra extraído, `scripts/fase3/templates/terraform-aws-standalone.yml` (cópia do bootstrap).

---

## Região

A região do Terraform no workflow vem do input (por padrão `sa-east-1`). A role IAM é global à conta; não confundir com região dos recursos.
