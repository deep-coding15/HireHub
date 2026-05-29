# Schéma PostgreSQL — HireHub (détail par microservice)

Connexion locale : `localhost:55432`, utilisateur/mot de passe `hirehub` / `hirehub`.

---

## auth-service — base `hirehub_auth`

### Table `users`

| Colonne | Type | Nullable | Description |
|---------|------|----------|-------------|
| `id` | UUID | NON | Clé primaire |
| `email` | VARCHAR(320) | NON | Unique, login |
| `full_name` | VARCHAR(200) | NON | Nom affiché |
| `password_hash` | VARCHAR(200) | NON | Mot de passe hashé |
| `role` | VARCHAR(32) | NON | `CANDIDAT`, `RECRUTEUR`, `ADMIN` |
| `recruiter_approved` | BOOLEAN | NON | Accès recruteur validé |
| `verification_status` | VARCHAR(40) | OUI | `PENDING_AUTO_CHECK`, `APPROVED`, `REVIEW_REQUIRED`, `REJECTED` |
| `company_name` | VARCHAR(300) | OUI | Entreprise |
| `company_siret` | VARCHAR(64) | OUI | SIRET / ICE |
| `company_presentation` | TEXT | OUI | Présentation |
| `blocked` | BOOLEAN | OUI | Compte bloqué |

---

## frontend-service — base `hirehub_auth` (même BDD, tables UI)

### Table `signup_email_challenges`

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | UUID | PK |
| `email` | VARCHAR | Email à valider |
| `code` | VARCHAR | Code OTP |
| `expires_at` | TIMESTAMP | Expiration |
| `consumed` | BOOLEAN | Déjà utilisé |

### Table `password_reset_tokens`

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | UUID | PK |
| `user_id` | UUID | FK logique vers `users` |
| `token` | VARCHAR | Jeton reset |
| `expires_at` | TIMESTAMP | Expiration |
| `used` | BOOLEAN | Déjà utilisé |

---

## offre-service — base `hirehub_offre` — port **8092**

### Table `offres`

| Colonne | Type | Nullable | Description |
|---------|------|----------|-------------|
| `id` | BIGINT | NON | PK auto |
| `titre` | VARCHAR(255) | NON | Intitulé |
| `description` | TEXT | NON | Description |
| `type_contrat` | VARCHAR | NON | `CDI`, `CDD`, `STAGE`, `ALTERNANCE`, `FREELANCE` |
| `ville` | VARCHAR(255) | NON | Ville |
| `salaire` | DOUBLE | OUI | Salaire |
| `date_creation` | TIMESTAMP | NON | Création auto |
| `date_expiration` | TIMESTAMP | OUI | Fin de validité (**futur** à la création) |
| `statut` | VARCHAR | NON | `BROUILLON`, `PUBLIEE`, `FERMEE` |
| `recruteur_id` | VARCHAR(64) | NON | **UUID** du recruteur connecté |
| `recruteur_email` | VARCHAR(255) | OUI | Email recruteur |

---

## candidature-service — base `hirehub_candidature` — port **8083**

### Table `candidatures`

| Colonne | Type | Nullable | Description |
|---------|------|----------|-------------|
| `id` | UUID | NON | PK |
| `candidat_id` | VARCHAR(255) | NON | ID candidat |
| `offre_id` | VARCHAR(255) | NON | ID offre |
| `cv_path` | VARCHAR(1000) | OUI | Chemin CV |
| `lettre_motivation_path` | VARCHAR(1000) | OUI | Lettre |
| `status` | VARCHAR(50) | NON | Statut pipeline |
| `date_soumission` | TIMESTAMP | NON | Date postulation |
| `date_modification` | TIMESTAMP | OUI | Dernière modif |
| `created_at` | TIMESTAMP | OUI | Audit |
| `updated_at` | TIMESTAMP | OUI | Audit |

### Table `historique_statut`

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | UUID | PK |
| `candidature_id` | UUID | FK `candidatures` |
| `ancien_status` | VARCHAR(50) | Ancien statut |
| `nouveau_status` | VARCHAR(50) | Nouveau statut |
| `commentaire` | VARCHAR(1000) | Commentaire |
| `utilisateur_id` | VARCHAR(255) | Auteur du changement |
| `date_changement` | TIMESTAMP | Horodatage |

---

## entretien-service — base `hirehub_entretien` — port **8085**

### Table `entretiens`

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | UUID | PK |
| `candidature_id` | VARCHAR(255) | Candidature liée |
| `candidat_id` | VARCHAR(255) | Candidat |
| `recruteur_id` | VARCHAR(255) | Recruteur |
| `date_heure` | TIMESTAMP | Date/heure entretien |
| `lieu` | VARCHAR(500) | Lieu physique |
| `lien_visio` | VARCHAR(1000) | Lien visio |
| `type` | VARCHAR(50) | Type entretien |
| `notes_internes` | VARCHAR(2000) | Notes |
| `status` | VARCHAR(50) | Statut |
| `date_creation` | TIMESTAMP | Création |
| `date_modification` | TIMESTAMP | Modification |
| `date_annulation` | TIMESTAMP | Annulation |

---

## event-service — base `hirehub_event` — port **8084**

### Table `event_audit_log`

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | BIGSERIAL | PK |
| `event_id` | VARCHAR(36) | UUID événement (unique) |
| `event_type` | VARCHAR(100) | Type |
| `message` | TEXT | Payload / message |
| `created_at` | TIMESTAMP | Date |
| `source_service` | VARCHAR(100) | Émetteur |
| `destination_service` | VARCHAR(100) | Destinataire |
| `status` | VARCHAR(20) | `SUCCESS`, `FAILED`, etc. |
| `error_message` | TEXT | Erreur éventuelle |

---

## email-service — base `hirehub_email` — port **8093**

### Table `email_events_processed`

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | BIGSERIAL | PK |
| `event_id` | VARCHAR(36) | UUID événement (unique) |
| `event_type` | VARCHAR(100) | Type |
| `recipient_email` | VARCHAR(255) | Destinataire |
| `status` | VARCHAR(20) | `SUCCESS`, `FAILED`, `PENDING`, `RETRY` |
| `processed_at` | TIMESTAMP | Traitement |
| `error_message` | TEXT | Erreur |
| `retry_count` | INTEGER | Nombre de retries |
| `created_at` | TIMESTAMP | Création |

---

## Services sans table PostgreSQL dédiée

| Service | Port | Rôle |
|---------|------|------|
| eureka-server | 8761 | Registry |
| config-server | 8888 | Config centralisée |
| api-gateway | 8089 | Routage `/offre-service`, etc. |
| verification-service | 8090 | Vérification recruteur |
| frontend-service | 8086 | UI Thymeleaf + tables auth locales |

---

## Corriger les offres orphelines (`recruteur_id = 10`)

Si des offres de test ont été créées avec l’ancien ID démo `10`, rattachez-les à votre compte recruteur :

```sql
UPDATE offres
SET recruteur_id = '20eef518-ac5a-4aaf-a8a7-997c3d9933fb',
    recruteur_email = 'elassal.douae@etu.uae.ac.ma'
WHERE recruteur_id = '10';
```

(Adapter `id` / `email` depuis `SELECT id, email FROM users WHERE role = 'RECRUTEUR';`.)
