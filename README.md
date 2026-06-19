# HireHub — PROJET JEE

Application **ATS** : offres d'emploi, candidatures (CV PDF), pipeline recruteur, entretiens, admin, e-mails et audit via RabbitMQ.

**Dépôt :** [anadouae/HireHub---PROJET-JEE](https://github.com/anadouae/HireHub---PROJET-JEE)

---

## Démarrage rapide

### Option A — Docker Compose (développement local)

```bash
docker compose -f docker-compose-full.yml up -d
```

Site : http://localhost:8086 — Mailpit : http://localhost:8025 — RabbitMQ : http://localhost:15672

### Option B — Kubernetes avec Minikube (CI/CD)

```bash
# Prérequis
minikube start --memory=4096 --cpus=2

# Déployer l'infrastructure
kubectl apply -f k8s/infra/postgres.yml
kubectl apply -f k8s/infra/rabbitmq.yml
kubectl apply -f k8s/infra/mailpit.yml

# Vérifier
kubectl get pods -n hirehub
```

Interfaces d'administration (remplacer l'IP par `minikube ip`) :
- RabbitMQ : `http://$(minikube ip):30672` (hirehub / hirehub)
- Mailpit   : `http://$(minikube ip):30025`

---

## Architecture

- Monorepo Maven multi-modules + `hirehub-common`
- Spring Boot 3.2, Spring Cloud (Eureka, Gateway, OpenFeign)
- PostgreSQL (une base par service), RabbitMQ, Mailpit
- **email-service** = envoi d'e-mails (rôle « notification-service » métier)
- **event-service** = audit des actions critiques
- Résilience : Circuit Breakers (Resilience4j), Dead Letter Queue, métriques Prometheus / Grafana

---

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
├── docker-compose-full.yml
├── k8s/
│   └── infra/
│       ├── postgres.yml    (Deployment + Service + PVC + Secret)
│       ├── rabbitmq.yml    (Deployment + Service + PVC + Secret)
│       └── mailpit.yml     (Deployment + Service)
└── docs/
```

---

## Prérequis

| Outil | Version | Usage |
|-------|---------|-------|
| JDK | 17 | Compilation des services |
| Docker | 24+ | Conteneurs locaux |
| Minikube | latest | Cluster Kubernetes local |
| kubectl | latest | Gestion du cluster |
| Maven Wrapper | inclus | `./mvnw` (Linux) / `.\mvnw.cmd` (Windows) |

---

## Infrastructure Kubernetes

Chaque fichier `k8s/infra/*.yml` contient tous les objets Kubernetes nécessaires au service :

| Fichier | Objets créés | NodePort (accès navigateur) |
|---------|-------------|----------------------------|
| `postgres.yml` | Namespace, Secret, PVC, Deployment, Service | — (interne uniquement) |
| `rabbitmq.yml` | Secret, PVC, Deployment, Service x2 | 30672 (UI) |
| `mailpit.yml` | Deployment, Service x2 | 30025 (UI) |

---

## Documentation

| Document | Description |
|----------|-------------|
| `VERSION_FINALE.md` | État final + schémas |
| `DEMARRAGE_RAPIDE.md` | Lancement des services |
| `docs/SCHEMA_BASES_DONNEES.md` | Schéma BDD |
| `docs/API-CONTRACTS.md` | Contrats REST |
| `docs/ARCHITECTURE.md` | Vue d'ensemble |

---

## Équipe

- **Douae El Assal** — Frontend, Auth, Verification, Admin
- **Imane El Bouzidi** — Offre-service
- **Lydivine Merveille** — Candidature, Email/notification, Pipeline
- **Wissal Khalid** — Entretien-service
