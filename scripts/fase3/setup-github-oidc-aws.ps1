<#
.SYNOPSIS
    Cria na AWS o Identity Provider OIDC do GitHub e uma IAM Role que só confia no teu repositório.
.DESCRIPTION
    Requer AWS CLI instalado e credenciais locais (aws configure ou variáveis de ambiente).
    No fim imprime o ARN para colares no GitHub como secret AWS_ROLE_ARN.
.PARAMETER GitHubOwner
    Utilizador ou organização GitHub (ex.: ricartefelipe).
.PARAMETER GitHubRepo
    Nome do repositório (ex.: oficina-springboot-mvp).
.PARAMETER RoleName
    Nome da role IAM a criar (por defeito: GitHubActionsTerraform).
.PARAMETER PolicyArn
    Política gerida a anexar (por defeito: PowerUserAccess — suficiente para Terraform de lab).
.EXAMPLE
    .\setup-github-oidc-aws.ps1 -GitHubOwner "ricartefelipe" -GitHubRepo "oficina-springboot-mvp"
#>
param(
    [Parameter(Mandatory = $true)]
    [string] $GitHubOwner,
    [Parameter(Mandatory = $true)]
    [string] $GitHubRepo,
    [string] $RoleName = "GitHubActionsTerraform",
    [string] $PolicyArn = "arn:aws:iam::aws:policy/PowerUserAccess"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    Write-Error "Instala o AWS CLI v2 e configura credenciais: https://aws.amazon.com/cli/"
}

$account = aws sts get-caller-identity --query Account --output text 2>$null
if (-not $account -or $account -match "error|None") {
    Write-Error "Falha ao obter a conta AWS. Executa 'aws configure' ou define AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY."
}

Write-Host "Conta AWS: $account" -ForegroundColor Cyan

# --- OIDC provider (uma vez por conta) ---
$providersJson = aws iam list-open-id-connect-providers --output json 2>$null
$hasGithub = $false
if ($providersJson) {
    $hasGithub = $providersJson -match "token\.actions\.githubusercontent\.com"
}
if (-not $hasGithub) {
    Write-Host "A criar OIDC provider token.actions.githubusercontent.com ..." -ForegroundColor Cyan
    $thumb = "6938fd4d98bab03faadb97b34396831e3780aea1"
    aws iam create-open-id-connect-provider `
        --url "https://token.actions.githubusercontent.com" `
        --client-id-list "sts.amazonaws.com" `
        --thumbprint-list $thumb
    if ($LASTEXITCODE -ne 0) {
        Write-Error "create-open-id-connect-provider falhou."
    }
} else {
    Write-Host "OIDC provider do GitHub já existe nesta conta." -ForegroundColor Green
}

$fedArn = "arn:aws:iam::${account}:oidc-provider/token.actions.githubusercontent.com"
$subClaim = "repo:${GitHubOwner}/${GitHubRepo}:*"

$trust = [ordered]@{
    Version   = "2012-10-17"
    Statement = @(
        @{
            Sid       = "GitHubActions"
            Effect    = "Allow"
            Principal = @{ Federated = $fedArn }
            Action    = "sts:AssumeRoleWithWebIdentity"
            Condition = @{
                StringEquals = @{
                    "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
                }
                StringLike   = @{
                    "token.actions.githubusercontent.com:sub" = $subClaim
                }
            }
        }
    )
}

$trustJson = $trust | ConvertTo-Json -Depth 10 -Compress
$trustFile = Join-Path ([System.IO.Path]::GetTempPath()) "github-oidc-trust-$([Guid]::NewGuid().ToString('N')).json"
[System.IO.File]::WriteAllText($trustFile, $trustJson, [System.Text.UTF8Encoding]::new($false))
# AWS CLI no Windows aceita file:// com barras normais
$trustFileUri = "file://$($trustFile -replace '\\', '/')"

aws iam get-role --role-name $RoleName --output json 2>$null | Out-Null
$roleExists = ($LASTEXITCODE -eq 0)

if ($roleExists) {
    Write-Host "Role '$RoleName' já existe — a atualizar trust policy ..." -ForegroundColor Yellow
    aws iam update-assume-role-policy --role-name $RoleName --policy-document $trustFileUri
    if ($LASTEXITCODE -ne 0) { Write-Error "update-assume-role-policy falhou." }
} else {
    Write-Host "A criar role '$RoleName' ..." -ForegroundColor Cyan
    aws iam create-role --role-name $RoleName --assume-role-policy-document $trustFileUri
    if ($LASTEXITCODE -ne 0) { Write-Error "create-role falhou." }
}

$attached = aws iam list-attached-role-policies --role-name $RoleName --output json | ConvertFrom-Json
$already = $attached.AttachedPolicies | Where-Object { $_.PolicyArn -eq $PolicyArn }
if (-not $already) {
    Write-Host "A anexar política: $PolicyArn" -ForegroundColor Cyan
    aws iam attach-role-policy --role-name $RoleName --policy-arn $PolicyArn
    if ($LASTEXITCODE -ne 0) { Write-Error "attach-role-policy falhou." }
} else {
    Write-Host "Política já anexada." -ForegroundColor Green
}

Remove-Item -LiteralPath $trustFile -Force -ErrorAction SilentlyContinue

$roleArn = "arn:aws:iam::${account}:role/${RoleName}"
Write-Host ""
Write-Host "=== Próximo passo no GitHub ===" -ForegroundColor Green
Write-Host "Settings → Secrets and variables → Actions → New repository secret"
Write-Host "  Nome:  AWS_ROLE_ARN"
Write-Host "  Valor: $roleArn"
Write-Host ""
