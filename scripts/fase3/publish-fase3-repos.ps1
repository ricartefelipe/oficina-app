<#
.SYNOPSIS
    Cria os quatro repositórios remotos no GitHub e faz o primeiro push (requer GitHub CLI).
.DESCRIPTION
    Para cada pasta gerada por bootstrap-repos.ps1, executa `gh repo create` com --push
    quando o remoto `origin` ainda não existe. Se o repositório já existir no GitHub,
    adiciona `origin` e faz push da branch `main`.
.PARAMETER Owner
    Utilizador ou organização GitHub (ex.: ricartefelipe). Por defeito: conta `gh auth login`.
.PARAMETER DestinationRoot
    Mesma pasta usada no bootstrap (por defeito: <pai do monorepo>\oficina-fase3-repos).
.PARAMETER Public
    Repositórios públicos (por defeito são privados).
#>
param(
    [string] $Owner = "",
    [string] $DestinationRoot = "",
    [switch] $Public
)

$ErrorActionPreference = "Stop"

$fase3Dir = $PSScriptRoot
$scriptsDir = Split-Path $fase3Dir -Parent
$repoRoot = Split-Path $scriptsDir -Parent

if (-not $DestinationRoot) {
    $wks = Split-Path $repoRoot -Parent
    $DestinationRoot = Join-Path $wks "oficina-fase3-repos"
}

$repos = @(
    "oficina-auth-lambda",
    "oficina-infra-database",
    "oficina-infra-kubernetes",
    "oficina-app"
)

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Error "Instale o GitHub CLI (gh) e execute 'gh auth login'. Alternativa: crie os quatro repos no site e em cada pasta: git remote add origin git@github.com:OWNER/NOME.git && git push -u origin main"
}

if (-not $Owner) {
    $Owner = (gh api user -q .login)
    if (-not $Owner) {
        Write-Error "Não foi possível obter o utilizador GitHub. Passe -Owner."
    }
}

foreach ($name in $repos) {
    $path = Join-Path $DestinationRoot $name
    if (-not (Test-Path $path)) {
        Write-Warning "Pasta em falta (corra primeiro bootstrap-repos.ps1): $path"
        continue
    }
    Push-Location $path
    try {
        if (-not (Test-Path ".git")) {
            Write-Warning "Sem repositório Git em $path — ignorado."
            continue
        }

        $fullName = "$Owner/$name"
        $hasOrigin = git remote get-url origin 2>$null
        if (-not $hasOrigin) {
            Write-Host "`n=== $name : criar remoto e push ===" -ForegroundColor Cyan
            gh repo view $fullName 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) {
                git remote add origin "https://github.com/$fullName.git" 2>$null
                $b = git branch --show-current
                if (-not $b) { $b = "main" }
                git push -u origin $b
            } elseif ($Public) {
                gh repo create $fullName --public --source=. --remote=origin --push
            } else {
                gh repo create $fullName --private --source=. --remote=origin --push
            }
        } else {
            Write-Host "`n=== $name : origin já definido ($hasOrigin) ===" -ForegroundColor Yellow
            $currentBranch = git branch --show-current
            if (-not $currentBranch) { $currentBranch = "main" }
            git push -u origin $currentBranch
        }
    } finally {
        Pop-Location
    }
}

Write-Host "`nConcluído. Adicione soat-architecture como leitor em cada repo (Settings → Collaborators ou Organization teams)." -ForegroundColor Green
