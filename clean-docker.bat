@echo off
setlocal enabledelayedexpansion

REM Vérifier si des conteneurs du projet sont en train de tourner
for /f %%i in ('docker compose ps --format "{{.State}}" 2^>nul ^| find /c "running"') do set running_count=%%i

if !running_count! gtr 0 (
    echo.
    echo ⚠️  Des conteneurs sont actifs. Lancement d'un nettoyage sécurisé...
    echo [*] Libération du cache de build ^(BuildKit^)...
    docker builder prune -f
    echo [*] Suppression des images de build intermédiaires...
    docker image prune -f
    echo ✅ Nettoyage sécurisé terminé ! Vos conteneurs et bases H2 n'ont pas été touchés.
) else (
    echo.
    echo 🛑 Aucun conteneur actif. Lancement du grand nettoyage à vide...
    echo [*] Suppression des conteneurs arrêtés et réseaux inutilisés...
    docker system prune -f
    echo [*] Suppression de TOUTES les images intermédiaires...
    docker image prune -f
    echo [*] Suppression de TOUS les volumes ^(Bases H2 incluses^)...
    for /f %%i in ('docker volume ls -q') do (
        docker volume rm %%i 2>nul
    )
    echo ✅ Coffre-fort Docker entièrement vidé et nettoyé !
)

pause