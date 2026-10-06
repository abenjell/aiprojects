# aiprojects - Multi-Agent & MCP Platform 🚀

**aiprojects** est un projet multi-module Maven moderne, conçu pour l'expérimentation, le développement et la validation du **Model Context Protocol (MCP)** et l'implémentation de **systèmes multi-agents autonomes** en utilisant **Java 21**, **Spring Boot 4.0.6**, et **Spring AI 2.0.0-RC2**.

---

## 📂 Architecture des Modules

Le projet est divisé en deux modules autonomes :

1. **`mcp-server`** : Un serveur MCP autonome exposant :
   * **Outils (Tools)** : `WeatherService_getWeather` (prévisions Open-Meteo) et `WeatherService_getAlerts` (alertes NWS).
   * **Ressources (Resources)** : Flux de logs système simulés via `logs://system`.
   * **Prompts** : Modèles de diagnostic système comme `analyze_system_health`.
   * **Transports** : Supporte à la fois **SSE** (Server-Sent Events) sur le port `8080` et le protocole **STDIO** (standard input/output).

2. **`mcp-client`** : Un client MCP orchestrant une chaîne d'agents spécialisés (style ReAct/Superviseur) fonctionnant de concert pour transformer une expression de besoin en un code complet, audité et testé.

---

## 🤖 Orchestration Multi-Agents & Spécialisation des LLMs

Pour garantir une efficacité maximale sans frais d'API, l'orchestrateur du `mcp-client` s'appuie sur une chaîne de **5 agents autonomes**, chacun configuré avec le modèle OpenRouter gratuit (`:free`) le plus adapté à ses tâches cognitives :

| Agent | Rôle | Modèle OpenRouter Gratuit | Spécialisation & Force Cognitive |
| :--- | :--- | :--- | :--- |
| **Product Owner / Backlog Refiner** | Analyse l'expression de besoin, la décompose en User Stories Scrum et peuple automatiquement le backlog via les outils MCP. | `openrouter/free` | **Appels d'outils (Tool Calling) & JSON (Routage Résilient)** : OpenRouter redirige dynamiquement les requêtes vers le meilleur modèle gratuit supportant le Tool Calling. |
| **Architect (Superviseur)** | Reçoit le rapport du backlog raffiné, conçoit l'architecture globale (contrats, packages, flux réactifs) et rédige la spécification technique. | `google/gemma-4-31b-it:free` | **Raisonnement conceptuel & Conception** : Très fort pour le design d'architecture système, l'abstraction et l'application des patrons de conception. |
| **Developer** | Traduit les spécifications d'architecture en code source complet de production Java, Spring Boot 4 et WebFlux. | `cohere/north-mini-code:free` | **Génération de Code & Context** : Modèle de pointe optimisé spécifiquement pour le développement, avec une très grande fenêtre de contexte (256K). |
| **QA Engineer** | Écrit l'intégralité des suites de tests unitaires et d'intégration basées uniquement sur les règles AssertJ. | `cohere/north-mini-code:free` | **Génération de Code de Test** : Expert en syntaxe AssertJ et isolation des tests. |
| **Performance Auditor** | Inspecte le code généré pour s'assurer de l'absence d'appels bloquants réactifs et de requêtes SQL sous-optimales. | `google/gemma-4-31b-it:free` | **Analyse Logique & Sécurité** : Excellente compréhension des contraintes non-fonctionnelles et repérage de bugs potentiels. |

---

## ⚙️ Comment Exécuter le Projet

### 1. Compilation globale
Pour compiler le projet et lancer les tests unitaires :
```bash
./mvnw clean install
```

### 2. Démarrage du Serveur MCP (Mode SSE - Port 8080)
```bash
./mvnw spring-boot:run -pl mcp-server
```

### 3. Démarrage de la Chaîne Multi-Agents (Mode STDIO)
Le client lance automatiquement le serveur MCP sous-jacent en STDIO :
```bash
./mvnw spring-boot:run -pl mcp-client
```
