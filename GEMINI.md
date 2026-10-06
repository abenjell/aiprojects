# Context & Instructions du Projet (GEMINI.md)

Ce document sert de contexte de référence pour l'agent Gemini CLI et les développeurs travaillant sur le projet **aiprojects**. Il définit l'architecture, les conventions de développement, les commandes d'exécution et les directives d'implémentation du projet.

---

## 1. Présentation du Projet & Architecture

**aiprojects** est un projet multi-module Maven conçu pour l'apprentissage, le développement et le test du **Model Context Protocol (MCP)** et de l'intégration d'**Agents autonomes**. Il est architecturé autour de la pile technologique **Java 21**, **Spring Boot 4.0.6** et **Spring AI 2.0.0-RC2**.

### Structure des Modules
Le projet est composé d'un projet parent (`ai-parent`) et de deux sous-modules :
1. **`mcp-server`** :
   * Serveur MCP autonome qui expose des outils, des ressources et des prompts.
   * **Dépendances clés** : `spring-ai-starter-mcp-server-webflux`, `spring-web`.
   * **Fonctionnalités exposées** :
     * **Outils (Tools)** :
       * `WeatherService_getWeather` (récupère les prévisions météo réelles via l'API Open-Meteo).
       * `WeatherService_getAlerts` (récupère les alertes météo actives aux États-Unis via l'API weather.gov).
     * **Ressources (Resources)** : `logs://system` (fournit un flux de logs système simulé).
     * **Prompts** : `analyze_system_health` (génère un prompt structuré de diagnostic système basé sur un seuil d'alerte configurable).
   * **Modes de transport** :
     * **SSE (Server-Sent Events)** : Activé par défaut en mode serveur web (port `8080`). Idéal pour le test via HTTP/curl.
     * **STDIO** : Utilisé pour la communication par flux standard (entrées/sorties standards) avec un client MCP local.
2. **`mcp-client`** :
   * Client MCP basé sur Spring AI configuré pour piloter des serveurs MCP locaux via stdio.
   * **Dépendances clés** : `spring-ai-starter-mcp-client`, `lombok`.
   * **Configuration** : Charge sa configuration depuis `classpath:mcp-servers.json`.

---

## 2. Guide d'Exécution & Commandes (Fils Conducteurs)

> **Règle absolue d'agent** : Ne JAMAIS exécuter de commandes Maven (`mvn`, `./mvnw`) directement. Présentez toujours la commande à l'utilisateur et demandez-lui de l'exécuter manuellement.

### Compilation et Tests
* **Tout compiler et installer** :
  ```bash
  ./mvnw clean install
  ```
* **Lancer les tests unitaires uniquement** :
  ```bash
  ./mvnw test
  ```

### Lancement du Serveur (Mode SSE - Port 8080)
* **Lancer le serveur** :
  ```bash
  ./mvnw spring-boot:run -pl mcp-server
  ```
  *Une fois démarré, le serveur est prêt à recevoir des connexions SSE sur `http://localhost:8080/sse`.*

### Lancement du Client (Mode STDIO)
Le client démarre le serveur MCP en tant que sous-processus stdio.
* **Important** : Le fichier `mcp-client/src/main/resources/mcp-servers.json` contient un chemin absolu vers le JAR du serveur (`D:/ideaProject/perso/aiprojects/mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar`). **Ce chemin doit être adapté localement** avant de lancer le client.
* **Lancer le client** :
  ```bash
  ./mvnw spring-boot:run -pl mcp-client
  ```

---

## 3. Conventions de Développement & Standards Techniques

Pour préserver la cohérence et la qualité industrielle du codebase, toutes les modifications de code doivent se conformer strictement aux standards suivants :

### Écriture des Tests Unitaires & Assertions
* **Assertions AssertJ** : Utilisez exclusivement les assertions AssertJ (`assertThat(...)`) pour tous les tests unitaires et d'intégration. Ne JAMAIS utiliser les assertions natives Java ou JUnit (`assertEquals`, etc.).
  ```java
  // Correct
  assertThat(result).isEqualTo(expected);
  assertThat(logs).contains("System initialized");
  ```

### Gestion des Constantes
* **Anti-pattern "Constant Interface" proscrit** : Ne définissez JAMAIS de constantes dans des interfaces. Utilisez soit :
  * Une classe finale (`final class`) avec un constructeur privé.
  * Une énumération (`enum`) si les constantes partagent une nature commune.

### Configuration du Projet
* **Fichiers YAML** : Toutes les configurations d'application doivent utiliser le format YAML (`application.yml`). Les fichiers `.properties` sont obsolètes dans ce projet.
* **Nommage des Packages** : Respecter l'arborescence des packages consolidée :
  * Client : `com.sazyconseil.mcpclient`
  * Serveur : `com.sazyconseil.mcpserver`

---

## 4. Documentation Connexe & Guides Pratiques

Le projet intègre des ressources documentaires clés pour guider le développement :
* **`LEARNING_PLAN_MCP.md`** : Plan de formation accéléré sur 10 jours couvrant l'architecture MCP, les Skills, les Agents (ReAct, Plan-and-Solve) et l'orchestration multi-agents.
* **`mcp-sse-guide.md`** : Guide pas-à-pas très complet pour tester manuellement le serveur MCP en mode SSE (Server-Sent Events) à l'aide de requêtes HTTP et de `curl`/`jq` pour le formatage.
* **`git-commands.md`** : Historique des commandes Git majeures utilisées pour consolider le dépôt et intégrer le sous-module en tant que dossier standard.
