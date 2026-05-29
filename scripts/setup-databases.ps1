# Cree les bases PostgreSQL manquantes (si le conteneur a ete initialise avant 01-databases.sql complet).
# Usage : depuis la racine du projet
#   .\scripts\setup-databases.ps1

$ErrorActionPreference = "Stop"
$container = "hirehub-postgres"

$databases = @(
    "hirehub_auth",
    "hirehub_offre",
    "hirehub_candidature",
    "hirehub_entretien",
    "hirehub_email",
    "hirehub_event"
)

if (-not (docker ps --format "{{.Names}}" | Select-String -Pattern "^$container$")) {
    Write-Host "Conteneur '$container' absent. Lancez d'abord : docker compose up -d" -ForegroundColor Yellow
    exit 1
}

foreach ($db in $databases) {
    $exists = docker exec $container psql -U hirehub -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$db'"
    if ($exists -match "1") {
        Write-Host "OK  $db (deja presente)"
    } else {
        docker exec $container psql -U hirehub -d postgres -c "CREATE DATABASE $db OWNER hirehub;"
        Write-Host "CREE $db"
    }
}

Write-Host ""
Write-Host "Bases disponibles :" -ForegroundColor Green
docker exec $container psql -U hirehub -d postgres -c "\l" | Select-String "hirehub"
