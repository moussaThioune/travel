# 🖥️ VOYAGEUR — Backend Spring Boot 3

API REST complète pour l'application de réservation de voyages avec paiements Mobile Money.

## 🚀 Démarrage rapide

```bash
cd backend
mvn spring-boot:run
```

L'API démarre sur **http://localhost:8080**

## 👤 Comptes de démo (créés automatiquement)

| Rôle | Email | Mot de passe |
|------|-------|-------------|
| **ADMIN** | admin@travelagency.com | admin123 |
| **CLIENT** | jean.dupont@email.com | client123 |

## 📡 Endpoints API

### 🔐 Authentification
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Créer un compte |
| POST | `/api/auth/login` | Se connecter → retourne JWT |

### ✈️ Voyages (public)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/voyages` | Liste tous les voyages |
| GET | `/api/voyages/available` | Voyages disponibles |
| GET | `/api/voyages/{id}` | Détail d'un voyage |
| GET | `/api/voyages/search?destination=Bali&prixMax=2000` | Recherche |
| POST | `/api/voyages` | Créer voyage (ADMIN) |
| PUT | `/api/voyages/{id}` | Modifier voyage (ADMIN) |
| DELETE | `/api/voyages/{id}` | Supprimer voyage (ADMIN) |

### 📋 Réservations (authentifié)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/reservations` | Créer une réservation |
| GET | `/api/reservations/my` | Mes réservations |
| GET | `/api/reservations` | Toutes (ADMIN) |
| PUT | `/api/reservations/{id}/cancel` | Annuler |

### 💳 Paiements
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/paiements` | Initier un paiement |
| GET | `/api/paiements/reservation/{id}` | Paiements d'une réservation |
| GET | `/api/paiements` | Tous (ADMIN) |
| GET | `/api/paiements/mobile-money/pending` | MM en attente (ADMIN) |
| GET | `/api/paiements/mobile-money` | Historique MM (ADMIN) |
| PUT | `/api/paiements/{id}/valider` | Valider MM (ADMIN) |
| PUT | `/api/paiements/{id}/rejeter` | Rejeter MM (ADMIN) |
| GET | `/api/paiements/stats` | Statistiques (ADMIN) |

### ⚙️ Admin (dashboard unifié)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/admin/dashboard` | Stats globales + alertes |
| GET | `/api/admin/reservations` | Toutes les réservations |
| PUT | `/api/admin/reservations/{id}/statut` | Changer statut |
| GET | `/api/admin/paiements/pending` | MM en attente |
| PUT | `/api/admin/paiements/{id}/valider` | Valider |
| PUT | `/api/admin/paiements/{id}/rejeter` | Rejeter avec raison |

## 📱 Paiements Mobile Money

### Modes supportés
- `ORANGE_MONEY` — Orange Money Sénégal/Mali/Côte d'Ivoire
- `WAVE` — Wave Sénégal/Côte d'Ivoire
- `FREE_MONEY` — Free Money Sénégal

### Flux de validation
1. **Client** envoie `POST /api/paiements` avec `modePaiement: "ORANGE_MONEY"` et `phoneNumber`
2. Le paiement est créé avec statut `EN_ATTENTE`
3. **Admin** voit le paiement dans le dashboard → `/api/admin/paiements/pending`
4. **Admin** valide → `PUT /api/admin/paiements/{id}/valider`
5. La réservation passe automatiquement à `PAYEE`
6. Le client reçoit un email de confirmation

### Exemple de requête de paiement
```json
POST /api/paiements
{
  "reservationId": 1,
  "montant": 185000,
  "modePaiement": "ORANGE_MONEY",
  "phoneNumber": "+221 77 123 45 67",
  "referenceTransaction": "OM-ABC12345"
}
```

## 🗄️ Base de données

**Développement** : H2 in-memory (démo, reset au redémarrage)
- Console H2 : http://localhost:8080/h2-console
- JDBC URL : `jdbc:h2:mem:voyageurdb`

**Production** : MySQL (décommentez dans `application.properties`)

## 📧 Emails

Configurez vos identifiants SMTP dans `application.properties` :
```properties
spring.mail.username=votre-email@gmail.com
spring.mail.password=mot-de-passe-application-gmail
```

> Pour Gmail: activez l'authentification 2FA et créez un "Mot de passe d'application"

## 🔧 Technologies

- Java 17 + Spring Boot 3.2
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA + Hibernate
- H2 (dev) / MySQL (prod)
- Lombok + MapStruct
- Spring Mail (SMTP)
- Maven
