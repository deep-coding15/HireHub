# Parcours « Devenir recruteur » — règles métier HireHub

## Principe

1. Tout nouvel utilisateur s’inscrit et se connecte comme **utilisateur standard** (rôle **CANDIDAT** / compte « personnel »).
2. L’accès aux fonctionnalités **recruteur** (publier des offres, pipeline, etc.) **n’est pas** activé à l’inscription.
3. Depuis **Mon profil** (menu compte), l’utilisateur peut lancer une demande **« Devenir recruteur »** : il remplit un **formulaire complet** (entreprise, SIREN/SIRET si besoin, justification, pièces jointes éventuelles — à définir en équipe).
4. La demande est enregistrée avec le statut **EN_ATTENTE** et apparaît dans l’**espace administrateur** (file d’attente).
5. L’**administrateur** ouvre la demande, vérifie les informations, et choisit :
   - **Approuver** → le compte reçoit le rôle **RECRUTEUR** (ou équivalent) ; un **email d’approbation** est envoyé au demandeur.
   - **Rejeter** → le rôle reste inchangé ; un **email de refus** est envoyé (motif optionnel).
6. Après **approbation**, le menu utilisateur affiche l’accès **« Espace recruteur »** (et les entrées de navigation recruteur deviennent visibles selon les règles UI).

## Emails (notification-service)

| Événement | Destinataire | Contenu minimal |
|-----------|--------------|-----------------|
| Demande soumise | Admin (optionnel) | Nouvelle demande à traiter |
| Demande approuvée | Demandeur | Confirmation + lien vers l’espace recruteur |
| Demande rejetée | Demandeur | Refus + contact support (optionnel) |

## Backend (à implémenter)

- Table ou agrégat **demande_recruteur** : id utilisateur, champs formulaire, statut, dates, commentaire admin.
- Endpoints réservés **ADMIN** : lister, détail, `POST .../approuver`, `POST .../rejeter`.
- Après décision : appel **notification-service** + mise à jour des **rôles** dans **auth-service**.

## Frontend (état actuel)

- Pages et menu reflètent ce scénario (libellés + états démo).
- Paramètre de démonstration UI : `?demo=` sur l’URL (voir comportement dans l’application).
