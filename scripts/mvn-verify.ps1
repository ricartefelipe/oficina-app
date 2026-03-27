# Build alinhado ao CI: mvn -B -Pci clean verify
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
mvn -B "-Pci" clean verify @args
