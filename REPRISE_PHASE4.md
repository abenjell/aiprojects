# 📌 Reprise de la Phase 4 : Software Factory Multi-Agents

Ce document récapitule l'état d'avancement exact à la fin de notre session du **Samedi 5 Septembre 2026** pour nous permettre de reprendre immédiatement la prochaine fois.

---

## 🔍 État Actuel du Projet

### 1. Socle Multi-Agents Implémenté (Module `mcp-client`)
Nous avons conçu et implémenté l'ensemble de l'architecture de la **Software Factory** :
* **Observabilité SSE** : Les classes d'événements `AgentEvent`, `AgentEventPublisher` et `AgentStreamController` sont prêtes. L'endpoint `/agents/stream` diffuse les flux d'événements d'agents de manière non-bloquante.
* **Moteur de Skills** : `SkillLoader` lit de façon dynamique les fichiers `SKILL.md` (sous le dossier racine `skills/`) pour charger les compétences des agents.
* **Workers d'Élite** :
  * `DeveloperAgent` : Code de production Java/Spring Boot.
  * `QaAgent` : Tests unitaires (skill `java-assertj-guard` injecté dynamiquement).
  * `PerformanceAgent` : Audit réactif et SQL (skills `reactive-blocking-guard` et `sql-optimizer` injectés dynamiquement).
* **Superviseur** : `AgentArchitect` coordonne la cascade d'agents (`Architect -> Developer -> QA -> Performance`) et expose l'endpoint `POST /agents/process`.

### 2. Validation & Tests Unitaires
* Deux nouvelles classes de tests ont été créées avec **AssertJ** et **Reactor StepVerifier** :
  * `SkillLoaderTest.java` : Valide l'accès aux compétences sur le disque.
  * `AgentEventPublisherTest.java` : Valide la réactivité du bus d'événements.
* La classe `McpClientApplicationTests` a été ajustée pour simuler l'existence d'une clé API OpenAI (`spring.ai.openai.api-key=dummy`) et éviter les échecs lors des builds hors-ligne.

---

## 🚀 Étapes Suivantes (Pour la Reprise)

1. **Exécution des tests et compilation globale** :
   Lancer la commande de compilation et s'assurer que tous les tests unitaires (anciens et nouveaux) sont au vert :
   ```bash
   ./mvnw clean test
   ```
2. **Démarrage et Test Réel (End-to-End)** :
   * S'assurer que le serveur MCP tourne sur le port `8080` (STDIO/SSE).
   * Lancer le client sur le port `8081` avec une clé d'API OpenAI valide.
   * Ouvrir un terminal avec `curl -N http://localhost:8081/agents/stream` pour observer les agents.
   * Soumettre un besoin utilisateur à l'aide de l'endpoint `POST /agents/process`.
3. **Poursuite du plan Phase 4** :
   * Raccorder les outils MCP réels du serveur (comme le `BacklogService` pour peupler automatiquement le backlog JSON) à l'orchestration multi-agents.
   * Mettre en place un tableau d'événements interactif pour l'observabilité (UI légère).
