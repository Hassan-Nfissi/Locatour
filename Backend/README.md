# Locatour - Application de Tourisme Intelligent

## Description
Locatour est une application de tourisme qui utilise l'IA et l'API Google Places pour aider les utilisateurs à découvrir et planifier leurs voyages. L'application offre des fonctionnalités de recherche de lieux d'intérêt, de restaurants et d'hébergements, avec un système de recommandations personnalisées.

## Fonctionnalités
- Recherche de lieux d'intérêt avec l'API Google Places
- Système de recommandations alimenté par l'IA (Ollama)
- Authentification sécurisée avec JWT
- Système d'abonnement avec PayPal
- Gestion des utilisateurs et des préférences

## Configuration Technique

### Prérequis
- Java 17 ou supérieur
- Maven 3.6.3 ou supérieur
- Base de données PostgreSQL (production) / H2 (développement)
- Ollama pour les fonctionnalités d'IA

### Variables d'Environnement Requises
```
GOOGLE_PLACES_API_KEY=votre_clé_api_google_places
DATABASE_URL=url_de_votre_base_de_données
DATABASE_USERNAME=nom_utilisateur_db
DATABASE_PASSWORD=mot_de_passe_db
JWT_SECRET=votre_clé_secrète_jwt
PAYPAL_CLIENT_ID=votre_client_id_paypal
PAYPAL_CLIENT_SECRET=votre_secret_paypal
OLLAMA_BASE_URL=url_de_votre_instance_ollama
```

### Installation et Démarrage
1. Cloner le repository
```bash
git clone https://github.com/votre-username/locatour.git
cd locatour
```

2. Installer les dépendances
```bash
mvn clean install
```

3. Démarrer l'application
```bash
# Mode développement
mvn spring-boot:run

# Mode production
mvn spring-boot:run -Dspring.profiles.active=prod
```

## Architecture
- Backend : Spring Boot 3.5.0
- Base de données : H2 (dev) / PostgreSQL (prod)
- Sécurité : Spring Security avec JWT
- API externe : Google Places API
- IA : Ollama (modèle llama3)

## Endpoints API

### Authentification
- POST /api/auth/register - Inscription
- POST /api/auth/login - Connexion

### Places
- GET /api/places/search - Recherche de lieux
- GET /api/places/{id} - Détails d'un lieu

### Abonnement
- POST /api/subscription/subscribe - Souscrire à l'abonnement
- GET /api/subscription/status - Statut de l'abonnement

## Sécurité
- Authentification JWT
- Validation des entrées
- Protection CSRF
- Rate limiting
- Gestion sécurisée des secrets

## Contribution
Les contributions sont les bienvenues ! Veuillez suivre ces étapes :
1. Fork du projet
2. Création d'une branche pour votre fonctionnalité
3. Commit de vos changements
4. Push vers la branche
5. Création d'une Pull Request

## Licence
Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails. 