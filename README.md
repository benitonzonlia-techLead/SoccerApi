# ⚽ Soccer API

API REST de gestion d’équipes et de joueurs, développée avec **Spring Boot 3.5.5** et **Java 21**.  
Fonctionnalités : Swagger/OpenAPI, sécurité basique, Actuator, base H2 en mémoire, tests unitaires et d’intégration.

---

## 📋 Prérequis

- **Java** 21 ou supérieur
- **Maven** 3.9+
- **Port** `8080` libre
- Navigateur web pour Swagger et la console H2

---

## 🚀 Procédure de test complète
```bash
# 1. Cloner le dépôt
git clone <URL_DU_DEPOT>

# 2. Installer les dépendances
mvn clean install

# 3. Lancer l’application
mvn spring-boot:run -Dspring-boot.run.profiles=dev

## 🔐 Authentification pour tester les endpoints protégés

Certaines routes nécessitent une **authentification Basic HTTP**.  

- **Nom d’utilisateur** : `user`  
- **Mot de passe** : généré automatiquement par Spring Boot au démarrage (visible dans les logs à la ligne :  
  `Using generated security password: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)   

# 4. Ouvrir Swagger
#   http://localhost:8080/swagger-ui.html

**Dans Swagger UI** :  
1. Clique sur **Authorize** (icône 🔒 en haut à droite).  
2. Entre les identifiants ci‑dessus.  
3. Valide — tes appels sécurisés seront autorisés. 

# 5. Vérifier Actuator
#   http://localhost:8080/actuator/health
#   http://localhost:8080/actuator/metrics

# 6. Lancer les tests
mvn test

# 7. Générer et consulter le rapport de couverture
mvn clean verify


# 8. Accéder à la console H2
#   http://localhost:8080/h2-console

## ⏱ Temps de réalisation

**Durée totale : environ 3h45**

**Répartition du temps :**
- 📦 Mise en place du projet vierge & configuration : ~10 min  
- 🏗 Implémentation des entités, repositories, services et contrôleurs : ~1h45  
- 🔐 Ajout de la sécurité, configuration Swagger, et logs : ~30 min  
- 🧪 Écriture des tests unitaires et d’intégration : ~1h10 min  
- 📝 Rédaction du README et ajustements finaux, plus les bonus : ~15 min
