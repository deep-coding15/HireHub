# Frontend — 100 % donnees reelles (plus de mock)

## Regle

Toutes les pages metier affichent des donnees issues de :

- **PostgreSQL** (utilisateurs admin via `hirehub_auth`)
- **Microservices** via **API Gateway** `http://localhost:8089`

Aucune ligne statique type « Jean Dupont », KPI fictifs ou graphiques en dur.

## Source par page

| Page | Source |
|------|--------|
| `/offres`, `/offres/{id}` | `offre-service` |
| `/recruteur/offres`, creation | `offre-service` + UUID recruteur connecte |
| `/recruteur/dashboard` | `offre-service` |
| `/recruteur/pipeline/{offreId}` | `candidature-service` |
| `/recruteur/statistiques` | `offre-service` + `candidature-service` |
| `/recruteur/entretiens` | `entretien-service` |
| `/candidat/dashboard` | `candidature-service` + `entretien-service` |
| `/candidat/mes-candidatures` | `candidature-service` |
| `/candidat/entretiens` | `entretien-service` |
| `/admin/*` | table `users` PostgreSQL |

## Services a demarrer

1. Docker (postgres, rabbitmq, mailpit)
2. Eureka, Config, Auth, **Offre (8092)**, **Candidature**, **Event**, **Entretien**, Email (8093), Verification, **Gateway**, **Frontend**

## Apres modification du code frontend

**Redemarrer `FrontendServiceApplication`** dans IntelliJ pour prendre en compte les changements.

## Verification rapide

```powershell
docker exec hirehub-postgres psql -U hirehub -d hirehub_offre -c "SELECT id,titre,recruteur_id FROM offres;"
docker exec hirehub-postgres psql -U hirehub -d hirehub_candidature -c "SELECT id,offre_id,status FROM candidatures;"
docker exec hirehub-postgres psql -U hirehub -d hirehub_entretien -c "SELECT id,candidature_id,date_heure FROM entretiens;"
```

Les memes donnees doivent apparaitre dans l'UI (connecte avec le bon compte recruteur/candidat).
