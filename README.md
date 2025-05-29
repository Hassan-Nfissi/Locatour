# 🌍 Locatour

**Locatour** est une application mobile intelligente de planification de voyages. Elle aide les utilisateurs à découvrir des lieux, simuler un budget, et organiser leurs séjours grâce à l’**IA (Ollama)** et l’**API Google Places**.

---

## 📱 Aperçu de l’application

### Écrans principaux

- 📊 **Planificateur de Budget** : Calculez votre budget selon vos préférences (logement, nourriture, type de tourisme).
- 🧠 **Recommandations IA** : Suggestions de durée, hébergements, restaurants, activités, et conseils pratiques.
- 👤 **Profil Utilisateur** : Statut d’abonnement, statistiques de voyage, passage au premium.

| Budget | Recommandation | Profil |
|--------|----------------|--------|
| ![Budget](./screen/budget.jpg) | ![Recommandation](./screenshots/recommandation.jpg) | ![Profil](./screenshots/profil.jpg) |

> 📸 *(Placez vos images dans un dossier `screenshots/` du repo)*

---

## ⚙️ Fonctionnalités

- 🔎 Recherche de lieux avec Google Places API
- 🤖 Recommandations IA (via [Ollama](https://ollama.com/))
- 🔐 Authentification sécurisée avec JWT
- 💳 Abonnements via PayPal
- 📈 Statistiques personnalisées
- 🧳 Gestion des préférences utilisateur

---

## 🧰 Stack Technique

| Côté | Stack |
|------|-------|
| **Frontend** | React Native + TypeScript |
| **Backend** | Spring Boot 3.5.0 |
| **IA** | Ollama (modèle `llama3`) |
| **Base de données** | H2 (développement) / PostgreSQL (production) |
| **Paiements** | API PayPal |
| **Auth** | Spring Security + JWT |

---

## 🚀 Démarrage du projet

### 📦 Prérequis

- Java 17+
- Maven 3.6.3+
- Node.js + npm
- PostgreSQL
- [Ollama](https://ollama.com/)
- Android Studio (pour debug mobile)

### 🔐 Variables d’environnement à configurer

```env
GOOGLE_PLACES_API_KEY=clé_api_google_places
DATABASE_URL=jdbc:postgresql://localhost:5432/locatour
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=mot_de_passe
JWT_SECRET=clé_secrète_jwt
PAYPAL_CLIENT_ID=client_id_paypal
PAYPAL_CLIENT_SECRET=secret_paypal
OLLAMA_BASE_URL=http://localhost:11434
