# Demarre toute la stack HireHub (Docker + microservices) dans le bon ordre.
# Usage : .\scripts\start-all.ps1
#         .\scripts\start-all.ps1 -OpenBrowser

param([switch]$OpenBrowser)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

$Services = @(
    @{ Module = "eureka-server";          Port = 8761; Seconds = 20 }
    @{ Module = "config-server";          Port = 8888; Seconds = 15 }
    @{ Module = "auth-service";           Port = 8081; Seconds = 25 }
    @{ Module = "offre-service";          Port = 8092; Seconds = 20 }
    @{ Module = "candidature-service";    Port = 8083; Seconds = 25 }
    @{ Module = "event-service";          Port = 8084; Seconds = 30 }
    @{ Module = "entretien-service";       Port = 8085; Seconds = 30 }
    @{ Module = "email-service";          Port = 8093; Seconds = 25 }
    @{ Module = "verification-service";    Port = 8090; Seconds = 20 }
    @{ Module = "api-gateway";            Port = 8089; Seconds = 20 }
    @{ Module = "frontend-service";        Port = 8086; Seconds = 25 }
)

function Test-PortListening([int]$Port) {
    return [bool](netstat -ano 2>$null | Select-String ":$Port\s" | Select-String "LISTENING")
}

function Wait-Port([int]$Port, [int]$TimeoutSec) {
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortListening $Port) { return $true }
        Start-Sleep -Seconds 2
    }
    return $false
}

function Wait-Postgres {
    Write-Host "Attente PostgreSQL..." -ForegroundColor Cyan
    for ($i = 1; $i -le 30; $i++) {
        docker exec hirehub-postgres pg_isready -U hirehub 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            docker exec hirehub-postgres psql -U hirehub -d postgres -tAc "SELECT 1" 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) { Write-Host "PostgreSQL pret." -ForegroundColor Green; return }
        }
        Start-Sleep -Seconds 2
    }
    throw "PostgreSQL non disponible sur le conteneur hirehub-postgres"
}

function Start-Microservice([string]$Module, [int]$Port, [int]$WaitSec) {
    if (Test-PortListening $Port) {
        Write-Host "  [deja actif] $Module (port $Port)" -ForegroundColor DarkGray
        return $true
    }
    $dir = Join-Path $Root $Module
    if (-not (Test-Path $dir)) {
        Write-Host "  [ignore] module introuvable : $Module" -ForegroundColor Yellow
        return $false
    }
    Write-Host "  Demarrage $Module (port $Port)..." -ForegroundColor Cyan
    $logDir = Join-Path $Root "logs"
    New-Item -ItemType Directory -Force -Path $logDir | Out-Null
    $logFile = Join-Path $logDir "$Module.log"
    $mvnw = Join-Path $Root "mvnw.cmd"
    $cmd = "cd /d `"$Root`" && `"$mvnw`" -pl $Module -am spring-boot:run -DskipTests > `"$logFile`" 2>&1"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $cmd -WindowStyle Hidden | Out-Null
    if (Wait-Port $Port $WaitSec) {
        Write-Host "  [OK] $Module" -ForegroundColor Green
        return $true
    }
    Write-Host "  [ECHEC] $Module — voir $logFile" -ForegroundColor Red
    return $false
}

Set-Location $Root
Write-Host "`n=== HireHub — demarrage complet ===" -ForegroundColor Cyan
Write-Host "Racine : $Root`n"

Write-Host "1. Docker..." -ForegroundColor Cyan
docker compose up -d 2>&1 | Out-Null
Start-Sleep -Seconds 3

Write-Host "2. Bases de donnees..." -ForegroundColor Cyan
& (Join-Path $PSScriptRoot "setup-databases.ps1")

Wait-Postgres

Write-Host "3. Compilation (une fois)..." -ForegroundColor Cyan
& (Join-Path $Root "mvnw.cmd") -q compile -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Compilation Maven echouee" }
Write-Host "Compilation OK.`n" -ForegroundColor Green

Write-Host "4. Microservices..." -ForegroundColor Cyan
$failed = @()
foreach ($svc in $Services) {
    if (-not (Start-Microservice $svc.Module $svc.Port $svc.Seconds)) {
        $failed += $svc.Module
    }
}

Write-Host "`n=== Resume ===" -ForegroundColor Cyan
& (Join-Path $PSScriptRoot "check-status.ps1")

if ($failed.Count -gt 0) {
    Write-Host "`nServices en echec : $($failed -join ', ')" -ForegroundColor Red
    Write-Host "Consultez les logs dans le dossier logs\" -ForegroundColor Yellow
    exit 1
}

Write-Host "`nTous les services sont demarres." -ForegroundColor Green
if ($OpenBrowser) {
    Start-Process "http://localhost:8086"
    Start-Process "http://localhost:8761"
}
exit 0
