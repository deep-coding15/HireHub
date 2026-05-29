# Affiche l'etat de HireHub (Docker + ports + URLs)
$ports = @{
    "Eureka"       = 8761
    "Config"       = 8888
    "Auth"         = 8081
    "Offre"        = 8092
    "Candidature"  = 8083
    "Event"        = 8084
    "Entretien"    = 8085
    "Frontend"     = 8086
    "Email"        = 8093
    "Gateway"      = 8089
    "Verification" = 8090
}

Write-Host "`n=== Docker HireHub ===" -ForegroundColor Cyan
docker ps --filter "name=hirehub" --format "  {{.Names}} : {{.Status}}" 2>$null

Write-Host "`n=== Services (port en ecoute = OK) ===" -ForegroundColor Cyan
foreach ($name in $ports.Keys | Sort-Object { $ports[$_] }) {
    $port = $ports[$name]
    $listening = netstat -ano 2>$null | Select-String ":$port\s" | Select-String "LISTENING"
    if ($listening) { Write-Host "  OK   $name (port $port)" -ForegroundColor Green }
    else             { Write-Host "  --   $name (port $port) ARRETE" -ForegroundColor Yellow }
}

Write-Host "`n=== Liens utiles ===" -ForegroundColor Cyan
Write-Host "  Site web    : http://localhost:8086"
Write-Host "  Eureka      : http://localhost:8761"
Write-Host "  Mailpit     : http://localhost:8025"
Write-Host ""
