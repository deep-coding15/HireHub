# Répartition des tâches — HireHub (binôme)

Ce document remplace le tableur Excel : même contenu, plus lisible pour la soutenance et le suivi.

**Équipe :** toi + **Mervy**  
**Projet :** plateforme ATS microservices (Spring Boot 3, PostgreSQL, Thymeleaf).

---

## 1. Vue d’ensemble par phase

| Phase | Objectif |
|-------|----------|
| **0** | Infrastructure commune (Docker, Eureka, Config, Gateway, BDD) |
| **1** | Sécurité : authentification, JWT, rôles, **demande « Devenir recruteur »** + validation admin + emails |
| **2** | Services métier (offres, candidatures, notifications, entretiens) |
| **3** | Frontend Thymeleaf + Feign (par espace) |
| **4** | Tests, documentation, démo |

---

## 2. Toi — périmètre principal

| # | Tâche | Détail | Livrables |
|---|--------|--------|-----------|
| T1 | **Docker & PostgreSQL** | `docker-compose`, init des 4 bases, volumes | `docker-compose.yml`, `docker/postgres/init/` |
| T2 | **Eureka + Config + API Gateway** | Découverte, config native, routage, **filtre JWT** | Modules `eureka-server`, `config-server`, `api-gateway` |
| T3 | **hirehub-common** | DTO partagés, erreurs API, enums (statuts candidature, **statut demande recruteur**) | Module `hirehub-common` |
| T4 | **auth-service** | Inscription / login, JWT, rôles `CANDIDAT`, `RECRUTEUR`, `ADMIN` ; **compte recruteur uniquement après approbation admin** | `auth-service` + BDD `hirehub_auth` |
| T5 | **Workflow « Devenir recruteur » (backend)** | Entité/table **demande recruteur** (formulaire), file d’attente admin, endpoints **approuver / rejeter**, événement vers **notification-service** | API REST + persistance |
| T6 | **Gateway** | Protection des routes `/recruteur/**` si rôle `RECRUTEUR` ; `/admin/**` si `ADMIN` | Filtres Spring Cloud Gateway |
| T7 | **Frontend espace Admin** | Pages `/admin/*` dont **file des demandes recruteur**, KPI, utilisateurs, logs | Templates `pages/admin/*` + appels Feign |
| T8 | **Intégration emails (côté spec)** | Valider les templates « demande approuvée / rejetée » avec Mervy (contenu, variables) | Specs + relecture |

**Note :** le **premier** compte utilisateur est toujours **candidat** ; le passage **recruteur** passe par **demande → validation admin → email** (voir `docs/PARCOURS_RECRUTEUR.md`).

---

## 3. Mervy — périmètre principal

| # | Tâche | Détail | Livrables |
|---|--------|--------|-----------|
| M1 | **offre-service** | CRUD offres, pagination, filtres ville/contrat | `offre-service` + BDD `hirehub_offre` |
| M2 | **candidature-service** | Candidatures, upload CV PDF, pipeline, historique statuts, Feign → notification | `candidature-service` + BDD `hirehub_candidature` |
| M3 | **notification-service** | Spring Mail (Mailtrap), templates : **email approbation / rejet demande recruteur**, changement statut, entretien | `notification-service` |
| M4 | **entretien-service** | Planification, rappels, notifications | `entretien-service` + BDD `hirehub_entretien` |
| M5 | **Frontend public + candidat + recruteur (UI)** | Pages liste/détail offres, postuler, espace candidat, **formulaire « Devenir recruteur »**, dashboard recruteur une fois **rôle actif**, Feign | `templates/pages/public`, `candidat`, `recruteur` |
| M6 | **Tests** | Unitaires / intégration sur les services métier ci-dessus | Tests JUnit / Mockito |

---

## 4. Tâches partagées (binôme)

| Tâche | Qui | Détail |
|--------|-----|--------|
| Schéma global & démo | Les deux | Schéma architecture, ordre de démarrage des services, scénario de démo bout en bout |
| Revue de code | Croisée | PR sur `main`, au moins une relecture avant merge |
| Documentation | Les deux | README, `docs/`, capture d’écran du parcours recruteur |

---

## 5. Ordre de priorité suggéré (dépendances)

1. Infra (Toi) → **auth-service** (Toi) avec rôles de base.  
2. **Demande recruteur** + endpoints admin (Toi) en parallèle de **notification-service** (Mervy) pour les emails.  
3. Services métier Mervy branchés sur Gateway + JWT.  
4. Frontend : formulaire et pages admin (coordination Toi / Mervy sur les DTO et URLs).

---

## 6. Fichiers de référence dans le dépôt

- `docs/PARCOURS_RECRUTEUR.md` — logique métier « Devenir recruteur »
- `docs/ARCHITECTURE.md` — architecture technique
- `docs/ROUTES_UI.md` — routes Thymeleaf
- `docs/Repartition_taches_HireHub.csv` — export tabulaire (optionnel)

---

*Dernière mise à jour : alignée sur le parcours recruteur validé par l’équipe.*
