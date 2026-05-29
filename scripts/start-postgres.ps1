# Demarre PostgreSQL HireHub et attend qu'il accepte les connexions.
# Usage : .\scripts\start-postgres.ps1

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $Root

Write-Host "Demarrage Docker HireHub (postgres, rabbitmq, mailpit)..." -ForegroundColor Cyan
docker compose up -d postgres rabbitmq mailpit

Write-Host "Attente PostgreSQL sur localhost:55432 (max 90 s)..." -ForegroundColor Cyan
$ok = $false
for ($i = 1; $i -le 45; $i++) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("127.0.0.1", 55432)
        $tcp.Close()
        docker exec hirehub-postgres pg_isready -U hirehub 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            $ok = $true
            break
        }
    } catch { }
    Start-Sleep -Seconds 2
    Write-Host "  ... tentative $i/45"
}

if (-not $ok) {
    Write-Host ""
    Write-Host "ECHEC : PostgreSQL ne repond pas sur le port 55432." -ForegroundColor Red
    Write-Host "1. Ouvrez Docker Desktop et attendez qu'il soit vert (Running)." -ForegroundColor Yellow
    Write-Host "2. Relancez : docker compose up -d" -ForegroundColor Yellow
    Write-Host "3. Puis : .\scripts\setup-databases.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "PostgreSQL OK sur localhost:55432" -ForegroundColor Green
& (Join-Path $PSScriptRoot "setup-databases.ps1")
Write-Host ""
Write-Host "Vous pouvez maintenant lancer EventServiceApplication et EntretienServiceApplication dans IntelliJ." -ForegroundColor Green
