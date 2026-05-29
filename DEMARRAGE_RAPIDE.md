# HireHub — démarrage rapide (version finale)

## Architecture (à retenir)

| Service Maven | Rôle métier | Port |
|---------------|-------------|------|
| `eureka-server` | Annuaire | 8761 |
| `config-server` | Configuration | 8888 |
| `auth-service` | Comptes + JWT | 8081 |
| `offre-service` | Offres d'emploi | **8092** |
| `candidature-service` | Candidatures + pipeline | 8083 |
| `event-service` | Audit des actions | 8084 |
| `entretien-service` | Entretiens | 8085 |
| **`email-service`** | **Emails (ex notification-service)** | **8093** |
| `verification-service` | OCR recruteur | 8090 |
| `api-gateway` | Passerelle API | 8089 |
| `frontend-service` | Site Thymeleaf | 8086 |

> Le module s'appelle **`email-service`** dans le code, mais le rôle métier est **notification** (envoi d'e-mails via RabbitMQ).  
> Ne pas chercher `NotificationServiceApplication` : lancer **`EmailServiceApplication`**.

---

## 1. Prérequis (une fois)

1. **Docker Desktop** démarré.
2. **JDK 17** configuré dans IntelliJ.
3. À la racine du projet :

```powershell
cd C:\Users\daurinia\Desktop\HireHub-PROJET-JEE
docker compose up -d
.\scripts\setup-databases.ps1
```

4. IntelliJ : **Maven → Reload All Maven Projects**, puis **Build → Rebuild Project**.

---

## 2. Lancer les microservices (IntelliJ)

**Services → Stop All**, puis dans cet ordre (voyant vert entre chaque) :

| # | Classe principale | Port |
|---|-------------------|------|
| 1 | `EurekaServerApplication` | 8761 |
| 2 | `ConfigServerApplication` | 8888 |
| 3 | `AuthServiceApplication` | 8081 |
| 4 | `OffreServiceApplication` | 8092 |
| 5 | `CandidatureServiceApplication` | 8083 |
| 6 | `EventServiceApplication` | 8084 |
| 7 | `EntretienServiceApplication` | 8085 |
| 8 | **`EmailServiceApplication`** | **8093** |
| 9 | `VerificationServiceApplication` | 8090 |
| 10 | `ApiGatewayApplication` | 8089 |
| 11 | **`FrontendServiceApplication`** | **8086** |

Port bloqué :

```powershell
.\scripts\free-hirehub-ports.ps1
```

---

## 3. URLs de test

| URL | Usage |
|-----|--------|
| http://localhost:8086 | Site HireHub |
| http://localhost:8761 | Eureka |
| http://localhost:8025 | Mailpit (e-mails) |
| http://localhost:15672 | RabbitMQ (hirehub / hirehub) |

---

## 4. Scénario de validation (Douae / frontend)

1. **Recruteur** : connexion → Mes offres → créer une offre → **Publier**.
2. **Candidat** : connexion → Parcourir les offres → **Postuler** (CV PDF + lettre).
3. **Candidat** : Mes candidatures → la candidature apparaît.
4. **Recruteur** : Pipeline de l'offre → voir la candidature.
5. **Mailpit** : e-mail de confirmation si `email-service` + RabbitMQ tournent.

---

## 5. Démarrage automatique

```powershell
.\scripts\start-all.ps1 -OpenBrowser
.\scripts\check-status.ps1
```

---

## 6. Erreurs fréquentes

| Problème | Solution |
|----------|----------|
| `Cannot resolve Jwts` dans IntelliJ | Maven → Reload All Maven Projects |
| Port 8082 / 8080 bloqué | Offre = **8092**, Email = **8093** |
| Mes candidatures vide / erreur | Gateway 8089 + Candidature 8083 + reconnectez-vous |
| Postuler échoue | Offre **publiée**, compte **CANDIDAT**, CV PDF &lt; 5 Mo |
| Pas d'e-mail | Docker RabbitMQ + `EmailServiceApplication` (8093) |
| `database does not exist` | `.\scripts\setup-databases.ps1` |

---

## 7. Build complet

```powershell
.\mvnw.cmd -DskipTests package
```
