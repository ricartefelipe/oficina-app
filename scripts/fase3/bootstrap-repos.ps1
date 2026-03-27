<#
.SYNOPSIS
    Gera quatro pastas-gêmeas (repos separados) para a Fase 3 junto ao workspace.
.DESCRIPTION
    Copia auth-lambda, stacks Terraform (AWS sem kind; kind na raiz do repo K8s) e a app Spring
    (sem auth-lambda, sem infra). Inicializa repositório Git em cada pasta.
.PARAMETER DestinationRoot
    Pasta onde será criada a pasta de cada repositório. Por defeito: <pai do monorepo>\oficina-fase3-repos
.PARAMETER SkipAppCopy
    Não copia o código da app; cria apenas um README com instruções.
#>
param(
    [string] $DestinationRoot = "",
    [switch] $SkipAppCopy
)

$ErrorActionPreference = "Stop"

$fase3Dir = $PSScriptRoot
$scriptsDir = Split-Path $fase3Dir -Parent
$repoRoot = Split-Path $scriptsDir -Parent
$templatesDir = Join-Path $fase3Dir "templates"

if (-not $DestinationRoot) {
    $wks = Split-Path $repoRoot -Parent
    $DestinationRoot = Join-Path $wks "oficina-fase3-repos"
}

function Invoke-RobocopyMirror {
    param(
        [string] $Source,
        [string] $Destination,
        [string[]] $ExcludeDirs = @()
    )
    if (-not (Test-Path $Source)) {
        throw "Origem inexistente: $Source"
    }
    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    $args = @($Source, $Destination, "/E", "/NFL", "/NDL", "/NJH", "/NJS")
    foreach ($d in $ExcludeDirs) {
        $args += "/XD"
        $args += $d
    }
    $code = & robocopy @args
    if ($code -ge 8) {
        throw "robocopy falhou com código $code (origem: $Source)"
    }
}

function Copy-GitHubWorkflow {
    param(
        [string] $RepoPath,
        [string] $TemplateFileName,
        [string] $TargetFileName
    )
    $wf = Join-Path $RepoPath ".github\workflows"
    New-Item -ItemType Directory -Force -Path $wf | Out-Null
    $src = Join-Path $templatesDir $TemplateFileName
    $dst = Join-Path $wf $TargetFileName
    Copy-Item -Path $src -Destination $dst -Force
}

function Initialize-GitRepo {
    param([string] $Path)
    Push-Location $Path
    try {
        if (-not (Test-Path ".git")) {
            git init
            git branch -M main 2>$null
            git add -A
            git -c user.email="local@bootstrap" -c user.name="bootstrap" commit -m "chore: scaffold inicial (Fase 3)" 2>$null
            if ($LASTEXITCODE -ne 0) {
                Write-Warning "Commit inicial falhou ou sem ficheiros em $Path (normal se vazio)."
            }
        }
    } finally {
        Pop-Location
    }
}

Write-Host "Monorepo: $repoRoot"
Write-Host "Destino:  $DestinationRoot"
New-Item -ItemType Directory -Force -Path $DestinationRoot | Out-Null

# 1) oficina-auth-lambda
$lambda = Join-Path $DestinationRoot "oficina-auth-lambda"
Write-Host "`n[1/4] oficina-auth-lambda"
Invoke-RobocopyMirror -Source (Join-Path $repoRoot "auth-lambda") -Destination $lambda
Copy-GitHubWorkflow -RepoPath $lambda -TemplateFileName "auth-lambda-ci-standalone.yml" -TargetFileName "ci.yml"
Initialize-GitRepo -Path $lambda

# 2) oficina-infra-database (AWS VPC + RDS, sem kind)
$db = Join-Path $DestinationRoot "oficina-infra-database"
Write-Host "`n[2/4] oficina-infra-database"
Invoke-RobocopyMirror -Source (Join-Path $repoRoot "infra") -Destination $db -ExcludeDirs @("kind")
Copy-GitHubWorkflow -RepoPath $db -TemplateFileName "terraform-aws-ci-pr.yml" -TargetFileName "terraform-validate.yml"
Copy-GitHubWorkflow -RepoPath $db -TemplateFileName "terraform-aws-standalone.yml" -TargetFileName "terraform-aws.yml"
Initialize-GitRepo -Path $db

# 3) oficina-infra-kubernetes (stack Kind na raiz)
$k8s = Join-Path $DestinationRoot "oficina-infra-kubernetes"
Write-Host "`n[3/4] oficina-infra-kubernetes"
Invoke-RobocopyMirror -Source (Join-Path $repoRoot "infra\kind") -Destination $k8s -ExcludeDirs @(".terraform")
Copy-GitHubWorkflow -RepoPath $k8s -TemplateFileName "terraform-kind-ci-standalone.yml" -TargetFileName "terraform-validate.yml"
$readmeK8s = Join-Path $k8s "README-FASE3.md"
@"
# Repositório Kubernetes (Fase 3)

Este repositório contém o Terraform do cluster **Kind** (laboratório local). Para EKS ou outro cluster gerido na nuvem, adicionar módulos neste repo e alinhar CI/CD com o ambiente.

Origem: stack ``infra/kind`` do monorepo ``oficina-springboot-mvp``.

Convidado de leitura: **soat-architecture** (conforme enunciado).
"@ | Set-Content -Path $readmeK8s -Encoding utf8
Initialize-GitRepo -Path $k8s

# 4) oficina-app
$app = Join-Path $DestinationRoot "oficina-app"
Write-Host "`n[4/4] oficina-app"
if ($SkipAppCopy) {
    New-Item -ItemType Directory -Force -Path $app | Out-Null
    $readmeApp = Join-Path $app "README.md"
    @"
# oficina-app

Scaffold gerado com ``-SkipAppCopy``. Copie o código do monorepo manualmente ou execute o bootstrap **sem** ``-SkipAppCopy``.

- Excluir pastas ``auth-lambda`` e ``infra`` se existirem (passaram para outros repositórios).
- Adicionar **soat-architecture** como leitor.
"@ | Set-Content -Path $readmeApp -Encoding utf8
} else {
    Invoke-RobocopyMirror -Source $repoRoot -Destination $app -ExcludeDirs @(
        ".git",
        "target",
        "auth-lambda",
        "infra",
        ".idea",
        "node_modules",
        ".terraform",
        "oficina-fase3-repos",
        "_fase3-bootstrap-test",
        "_tmp-fase3-bootstrap"
    )
    $wfApp = Join-Path $app ".github\workflows"
    foreach ($obsolete in @("terraform-aws.yml", "auth-lambda-ci.yml")) {
        $p = Join-Path $wfApp $obsolete
        if (Test-Path $p) {
            Remove-Item $p -Force
        }
    }
    Copy-GitHubWorkflow -RepoPath $app -TemplateFileName "ci-app-standalone.yml" -TargetFileName "ci.yml"
    Copy-GitHubWorkflow -RepoPath $app -TemplateFileName "deploy-k8s-branch.yml" -TargetFileName "deploy-k8s-branch.yml"
    $readmeAppExtra = Join-Path $app "README-FASE3.md"
    @"
# Notas Fase 3 (oficina-app)

- **Lambda** e **Terraform** estão em repositórios separados; este repo contém a aplicação Spring Boot e artefactos relacionados (ex.: ``k8s/``, Docker).
- CI publica imagem em GHCR; pushes em ``hml`` ou ``prd`` disparam ``deploy-k8s-branch.yml`` (requer secrets por ambiente — ver monorepo ``docs/fase3/executar-fase3.md``).
- Adicionar **soat-architecture** como leitor.
"@ | Set-Content -Path $readmeAppExtra -Encoding utf8
}
Initialize-GitRepo -Path $app

Write-Host ""
Write-Host "Concluido. Proximos passos: rever cada pasta, ajustar README, criar repos no GitHub e fazer push."
Write-Host "Documentacao: docs/fase3/executar-fase3.md"
