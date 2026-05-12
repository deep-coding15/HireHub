# API-CONTRACTS — HireHub (brouillon d’équipe)

À compléter **en priorité** en réunion (P0). Toute personne peut coder en parallèle une fois les lignes ci-dessous figées.

## Authentification

- En-tête : `Authorization: Bearer <access_token>`
- Algorithme : **HS256** (clé symétrique partagée entre `auth-service` et les services qui valident le JWT, ex. `candidature-service`).
- Variable d’environnement : **`HIREHUB_JWT_SECRET`** (chaîne **≥ 32 octets** ; même valeur sur tous les services qui signent ou valident le token).
- Durée de vie (défaut) : **`HIREHUB_JWT_EXPIRATION_SECONDS`** (secondes, défaut `3600` côté auth).
- Claims JWT (access token) :
  - `sub` : UUID utilisateur (string)
  - `email` : email
  - `role` : `CANDIDAT` | `RECRUTEUR` | `ADMIN` (enum `UserRole`)
  - `recruteurApprouve` : boolean (`true` uniquement si `role=RECRUTEUR` et compte approuvé ; sinon `false`)

Constantes Java partagées : `com.hirehub.common.constants.JwtClaimNames`.

### `POST /api/v1/auth/login`

- Corps JSON : `{ "email": "...", "password": "..." }`
- Réponse (dans le payload métier `ApiResponse.data`) : `accessToken`, `tokenType` (`Bearer`), `expiresIn` (secondes).
- Erreurs : `401` identifiants invalides ; `403` compte bloqué.

## auth-service (exemples — à valider)

| Méthode | Chemin | Description |
|---------|--------|-------------|
| POST | `/api/v1/auth/register/candidat` | Corps : nom, email, mot de passe |
| POST | `/api/v1/auth/register/recruteur` | Corps : + raison sociale, SIRET optionnel, téléphone, etc. |
| POST | `/api/v1/auth/login` | JSON : `email`, `password` → `accessToken` (Bearer) |
| GET | `/api/v1/admin/recruteurs/en-attente` | Liste comptes `RECRUTEUR` avec `recruteurApprouve=false` |
| POST | `/api/v1/admin/recruteurs/{id}/approuver` | → email + `recruteurApprouve=true` |
| POST | `/api/v1/admin/recruteurs/{id}/rejeter` | Corps : motif optionnel → email refus |

## notification-service (événements)

- `RECRUTEUR_APPROUVE` — destinataire : email du recruteur  
- `RECRUTEUR_REJETE` — idem  

(Payload exact : à définir dans `hirehub-common`.)

---

*Les chemins peuvent être préfixés par le Gateway (`/auth/...`) selon votre configuration de routes.*
