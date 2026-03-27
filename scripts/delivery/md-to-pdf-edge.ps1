# Gera PDF a partir de Markdown (Pandoc HTML + Edge headless).
# Uso: .\scripts\delivery\md-to-pdf-edge.ps1 -InputMd "docs\delivery\entrega-portal-fase2.md"

param(
  [Parameter(Mandatory = $true)]
  [string] $InputMd,
  [int] $VirtualTimeBudgetMs = 12000
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$mdPath = Join-Path $root $InputMd
if (-not (Test-Path $mdPath)) { throw "Ficheiro nao encontrado: $mdPath" }

$dir = Split-Path $mdPath -Parent
$name = [System.IO.Path]::GetFileNameWithoutExtension($mdPath)
$htmlPath = Join-Path $dir "$name.html"
$pdfPath = Join-Path $dir "$name.pdf"

$edge = "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe"
if (-not (Test-Path $edge)) {
  $edge = "${env:ProgramFiles}\Microsoft\Edge\Application\msedge.exe"
}
if (-not (Test-Path $edge)) { throw "Microsoft Edge nao encontrado para impressao headless." }

$pandoc = Get-Command pandoc -ErrorAction SilentlyContinue
if (-not $pandoc) { throw "Instale o Pandoc (winget install JohnMacFarlane.Pandoc) e reabra o terminal." }

Push-Location $dir
try {
  & pandoc $mdPath `
    -f gfm `
    -t html5 `
    --standalone `
    --css "pdf-print.css" `
    -o $htmlPath `
    --metadata title="$name"

  $htmlFull = (Resolve-Path -LiteralPath $htmlPath).Path
  $htmlUri = ([System.Uri]$htmlFull).AbsoluteUri
  Remove-Item -LiteralPath $pdfPath -ErrorAction SilentlyContinue
  & $edge --headless --disable-gpu --run-all-compositor-stages-before-draw "--virtual-time-budget=$VirtualTimeBudgetMs" --print-to-pdf="$pdfPath" "$htmlUri"
  $deadline = (Get-Date).AddSeconds(20)
  while (-not (Test-Path $pdfPath) -and (Get-Date) -lt $deadline) { Start-Sleep -Milliseconds 250 }
  if (-not (Test-Path $pdfPath)) { throw "Falha ao gerar PDF: $pdfPath" }
  Remove-Item -LiteralPath $htmlPath -ErrorAction SilentlyContinue
  Write-Host "OK: $pdfPath"
}
finally {
  Pop-Location
}
