# HireHub — PROJET JEE

Application **ATS** : offres d'emploi, candidatures (CV PDF), pipeline recruteur, entretiens, admin, e-mails et audit via RabbitMQ.

**Dépôt :** [anadouae/HireHub---PROJET-JEE](https://github.com/anadouae/HireHub---PROJET-JEE)

## Démarrage rapide

Voir **`DEMARRAGE_RAPIDE.md`** et **`VERSION_FINALE.md`**.

```powershell
docker compose up -d
.\scripts\setup-databases.ps1
.\mvnw.cmd -DskipTests package
# Puis lancer les services dans IntelliJ (ordre dans DEMARRAGE_RAPIDE.md)
```

Site : http://localhost:8086 — Mailpit : http://localhost:8025

## Architecture

- Monorepo Maven multi-modules + `hirehub-common`
- Spring Boot 3.2, Spring Cloud (Eureka, Gateway, OpenFeign)
- PostgreSQL (une base par service), RabbitMQ, Mailpit
- **email-service** = envoi d'e-mails (rôle « notification-service » métier)
- **event-service** = audit des actions critiques

## Structure

```
hirehub-parent/
├── hirehub-common/
├── eureka-server/          :8761
├── config-server/          :8888
├── api-gateway/            :8089
├── auth-service/           :8081
├── verification-service/   :8090
├── offre-service/          :8092
├── candidature-service/    :8083
├── event-service/          :8084
├── email-service/          :8093  (notifications e-mail)
├── entretien-service/      :8085
├── frontend-service/       :8086
├── docker-compose.yml
└── docs/
```

## Prérequis

- JDK **17**
- Docker (PostgreSQL 55432, RabbitMQ, Mailpit)
- Maven Wrapper : `.\mvnw.cmd` (Windows) ou `./mvnw` (Linux/macOS)

## Documentation

| Document | Description |
|----------|-------------|
| `VERSION_FINALE.md` | État final + schémas |
| `DEMARRAGE_RAPIDE.md` | Lancement des services |
| `docs/SCHEMA_BASES_DONNEES.md` | Schéma BDD |
| `docs/API-CONTRACTS.md` | Contrats REST |
| `docs/ARCHITECTURE.md` | Vue d'ensemble |

## Équipe

- **Douae El Assal** — Frontend, Auth, Verification, Admin
- **Imane El Bouzidi** — Offre-service
- **Lydivine Merveille** — Candidature, Email/notification, Pipeline
- **Wissal Khalid** — Entretien-service
