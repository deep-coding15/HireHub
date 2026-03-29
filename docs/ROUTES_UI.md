# Routes UI HireHub (frontend-service)

Toutes les pages sont en **Thymeleaf** avec données de démonstration. Branchement Feign / Gateway à faire côté backend.

## Public

| URL | Page |
|-----|------|
| `/` | Accueil |
| `/offres` | Liste des offres + filtres |
| `/offres/{id}` | Détail offre |
| `/offres/{id}/postuler` | Formulaire candidature |
| `/login` | Connexion |
| `/register` | Inscription |
| `/mes-candidatures` | Redirige vers `/candidat/mes-candidatures` |

## Candidat

| URL | Page |
|-----|------|
| `/candidat/dashboard` | Tableau de bord |
| `/candidat/mes-candidatures` | Candidatures + timeline |
| `/candidat/entretiens` | Liste des entretiens |
| `/candidat/profil` | Profil |
| `/demande-recruteur` | Formulaire « Devenir recruteur » (envoi admin + emails) |

## Recruteur

| URL | Page |
|-----|------|
| `/recruteur/dashboard` | KPI + candidatures récentes |
| `/recruteur/offres` | Liste des offres |
| `/recruteur/offres/nouvelle` | Création d’offre |
| `/recruteur/offres/{id}/pipeline` | Pipeline / statuts |
| `/recruteur/entretiens` | Planification entretiens |
| `/recruteur/statistiques` | Graphiques (Chart.js) |

## Admin

| URL | Page |
|-----|------|
| `/admin/dashboard` | KPI globaux + graphique |
| `/admin/demandes-recruteur` | File d’attente : approuver / rejeter les demandes recruteur |
| `/admin/utilisateurs` | Gestion des comptes |
| `/admin/logs` | Journaux (mock) |

## Démo UI (menus conditionnels)

Ajoutez `?demo=` à l’URL : `visiteur` | `connecte` | `demande_attente` | `demande_rejetee` | `recruteur` | `admin`
