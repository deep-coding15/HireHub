# Setup bases de donnees - HireHub

## Bases PostgreSQL a creer

- `hirehub_auth`
- `hirehub_offre`
- `hirehub_candidature`
- `hirehub_entretien`
- `hirehub_email` (email-service)
- `hirehub_event` (event-service)

Le script `docker/postgres/init/01-databases.sql` cree ces bases automatiquement au **premier** demarrage du conteneur `postgres`.

## Demarrage de l'infrastructure

Depuis la racine du projet:

```powershell
docker compose up -d
```

Verifier:

```powershell
docker ps
```

## Config des microservices

- `auth-service` -> `jdbc:postgresql://localhost:55432/hirehub_auth`
- `offre-service` -> `jdbc:postgresql://localhost:55432/hirehub_offre`
- `candidature-service` -> `jdbc:postgresql://localhost:55432/hirehub_candidature`
- `entretien-service` -> `jdbc:postgresql://localhost:55432/hirehub_entretien`
- `email-service` -> `jdbc:postgresql://localhost:55432/hirehub_email`
- `event-service` -> `jdbc:postgresql://localhost:55432/hirehub_event`

Tous utilisent:

- utilisateur: `hirehub`
- mot de passe: `hirehub`
- `ddl-auto: update` (dev uniquement)

## Important

Si tu as deja demarre postgres avant modification du script init:

1. Arreter les conteneurs:
   ```powershell
   docker compose down
   ```
2. Supprimer le volume Postgres (reinitialise les donnees):
   ```powershell
   docker volume rm hirehub-projet-jee_hirehub-pg-data
   ```
3. Redemarrer:
   ```powershell
   docker compose up -d
   ```

Alternative sans supprimer volume (recommande) :

```powershell
.\scripts\setup-databases.ps1
```

Ou dans pgAdmin (Query Tool sur `postgres`) : `scripts/create-missing-databases.sql`.

## Ports deja utilises (IntelliJ)

Si un service echoue avec `Port 808x was already in use` :

```powershell
.\scripts\free-hirehub-ports.ps1
```

Puis relancer uniquement le service concerne.

## Ordre de demarrage IntelliJ

1. `docker compose up -d`
2. `EurekaServerApplication` (:8761)
3. `ConfigServerApplication` (:8888) — optionnel si config native locale
4. Microservices (auth, offre, candidature, event, entretien, email, verification)
5. `ApiGatewayApplication` (:8089)
6. `FrontendServiceApplication` (:8086)

Utiliser `EmailServiceApplication` (pas l’ancien `NotificationServiceApplication`).
