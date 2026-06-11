# Plan d'Apprentissage Personnalisé : MCP, Skills & Agents

Ce document présente un plan d'apprentissage structuré et intensif sur 10 jours pour maîtriser le **Model Context Protocol (MCP)**, la création de **Skills** de qualité industrielle, et l'orchestration d'**Agents autonomes**.

---

## Phase 1 : Model Context Protocol (MCP) – Fondations & Serveurs (Jours 1 à 4)
*Objectif : Comprendre l'architecture protocolaire, configurer des clients et développer vos propres serveurs MCP.*

### Jour 1 : Concepts & Spécifications
* **Tâches d'apprentissage :**
  * Comprendre l'architecture à 3 niveaux : `LLM` $\rightarrow$ `Client MCP` $\rightarrow$ `Serveurs MCP`.
  * Étudier les trois primitives clés du protocole : **Resources** (données passives en lecture seule), **Prompts** (modèles de consignes réutilisables) et **Tools** (actions actives exécutables par le modèle).
* **Ressources recommandées :**
  * [Spécification officielle MCP (modelcontextprotocol.io)](https://modelcontextprotocol.io)
  * Dépôts GitHub officiels de la spécification MCP.
* **Exercice pratique :**
  * Cartographier sur papier ou dans un schéma Mermaid le flux de données complet lorsqu'un agent lit un fichier local via un outil MCP.

### Jour 2 : Développement d'un premier serveur MCP (Node.js/TypeScript ou Python)
* **Tâches d'apprentissage :**
  * Initialiser un projet vierge avec le SDK MCP officiel (`@modelcontextprotocol/sdk` pour Node.js ou `mcp` pour Python).
  * Comprendre le cycle de vie de la connexion (stdio ou SSE).
  * Exposer un premier outil simple (par exemple, un utilitaire mathématique ou une API météo de test).
* **Ressources recommandées :**
  * [GitHub Quickstart d'Anthropic/MCP](https://github.com/modelcontextprotocol/quickstart)
  * Documentation officielle du SDK sélectionné.
* **Exercice pratique :**
  * Écrire un serveur MCP local en TypeScript ou Python exposant un outil nommé `get_system_metrics` qui retourne l'usage actuel CPU et RAM de la machine hôte.

### Jour 3 : Ressources Dynamiques et Prompts
* **Tâches d'apprentissage :**
  * Implémenter l'exposition de *Resources* dynamiques (ex: logs système ou contenu d'un dossier temporaire).
  * Créer et exposer des templates de *Prompts* pré-configurés pour guider le modèle dans des tâches spécifiques.
* **Exercice pratique :**
  * Modifier le serveur du Jour 2 pour exposer une ressource accessible via l'URI `logs://system` et ajouter un prompt nommé `analyze_system_health` qui pré-remplit les consignes d'analyse des métriques et des logs.

### Jour 4 : Connexion Client & Intégration IDE
* **Tâches d'apprentissage :**
  * Configurer un client MCP (comme l'application de bureau Claude Desktop ou l'IDE Cursor).
  * Maîtriser le fichier de configuration client `mcp.config.json` ou `claude_desktop_config.json`.
  * Expérimenter la découverte dynamique des outils par le modèle.
* **Exercice pratique :**
  * Déclarer votre serveur de métriques (Jour 2 et 3) dans votre configuration client locale, puis utiliser l'IDE pour lui demander d'analyser l'état actuel de votre machine et d'en faire un rapport.

---

## Phase 2 : Maîtrise des Skills (Jours 5 à 7)
*Objectif : Concevoir des compétences spécialisées (Skills), structurer les bases de connaissances et les règles d'exécution.*

### Jour 5 : Anatomie d'un Skill & Contextes Systèmes
* **Tâches d'apprentissage :**
  * Étudier la structure d'un fichier de skill (`SKILL.md`).
  * Différencier un outil technique brut (MCP Tool) d'un comportement guidé (Skill) encadré par des règles métier complexes, des normes de qualité et des patterns de conception.
* **Exercice pratique :**
  * Parcourir et analyser un fichier de skill existant (ex : `skills/reactive-stack-guard/SKILL.md` ou `skills/gitlab-fix-vulnerabilities/SKILL.md` dans ce dépôt) pour identifier les sections clés (Context, Instructions, Rules).

### Jour 6 : Prompt Engineering avancé pour les Skills
* **Tâches d'apprentissage :**
  * Concevoir des stratégies de "Guardrails" (garde-fous) robustes pour empêcher les dérives comportementales de l'agent.
  * Apprendre à forcer le respect des types, la propreté du code et l'évitement des anti-patterns sans saturer le contexte du LLM.
* **Exercice pratique :**
  * Rédiger un fichier de skill nommé `sql-optimizer.skill` contenant des consignes précises de validation de requêtes SQL (interdiction des select *, indexation requise, prévention d'injections), prêt à être chargé dynamiquement par un agent.

### Jour 7 : Automatisation et Scripts d'Accompagnement
* **Tâches d'apprentissage :**
  * Lier un Skill à des scripts de validation ou d'analyse statique (ex : parseur AST, linters, scripts Node/Python).
  * Comprendre comment l'exécution de ces scripts permet de valider de manière empirique le travail produit par l'agent.
* **Exercice pratique :**
  * Écrire un petit script Node.js ou Python qui analyse un fichier de code source pour s'assurer qu'aucune API bloquante n'est utilisée (dans un contexte réactif) et lier ce script aux critères de succès définis dans votre Skill de validation.

---

## Phase 3 : Agents & Orchestration Multi-Agents (Jours 8 à 10)
*Objectif : Concevoir des agents réactifs, planificateurs et collaboratifs.*

### Jour 8 : Patterns d'Agents (ReAct & Plan-and-Solve)
* **Tâches d'apprentissage :**
  * Maîtriser le cycle fondamental **Planifier $\rightarrow$ Agir $\rightarrow$ Valider**.
  * Étudier comment un agent décompose une requête complexe en étapes claires et gère le backtracking en cas d'erreur ou d'échec de test.
* **Exercice pratique :**
  * Simuler sur papier puis formaliser dans un diagramme le comportement d'un agent de correction de bugs : comment il reproduit le problème par un test $\rightarrow$ applique le correctif $\rightarrow$ valide par le test $\rightarrow$ re-itère en cas d'échec.

### Jour 9 : Délégation et Collaboration d'Agents
* **Tâches d'apprentissage :**
  * Comprendre l'architecture de délégation de tâches (Supervisor / Workers).
  * Apprendre à concevoir des prompts de délégation pour que l'agent coordinateur formule des requêtes complètes, auto-suffisantes et sans ambiguïté pour les sous-agents spécialisés.
  * Étudier comment la délégation permet de compresser l'historique de contexte de l'agent principal.
* **Exercice pratique :**
  * Concevoir l'architecture d'un système multi-agents composé d'un *Analyste de Sécurité*, d'un *Développeur de Correctifs* et d'un *Rédacteur de Tests*. Définir leurs rôles, leurs outils et leurs canaux d'échange.

### Jour 10 : Projet Pratique de Synthèse (Exercice Final)
* **Exercice pratique complet :**
  1. **Développer un serveur MCP** (TypeScript ou Python) qui se connecte à un gestionnaire de tâches léger (ex: un stockage local JSON simulant un backlog Kanban ou une intégration Todoist/Trello). Le serveur doit exposer des outils pour lire, ajouter et modifier des tickets.
  2. **Créer un Skill** (`backlog-refiner.skill`) définissant précisément comment formater, catégoriser et prioriser ces tâches selon les critères méthodologiques Scrum (User Story, Acceptance Criteria, Priority).
  3. **Orchestrer l'Agent** pour qu'il prenne une expression de besoin utilisateur textuelle complexe, la décompose en User Stories claires via le Skill, puis utilise les outils du serveur MCP pour peupler automatiquement le backlog.

---

## 📚 Ressources incontournables
* **Protocoles & SDK :** [Introduction to MCP (Anthropic)](https://modelcontextprotocol.io/introduction) et les dépôts GitHub associés (`@modelcontextprotocol/sdk`).
* **Exemples Pratiques :** Les serveurs officiels et communautaires [mcp-servers sur GitHub](https://github.com/modelcontextprotocol/servers).
* **Patterns d'Agents :** Le papier de recherche *ReAct (Reason and Act)* et les guides d'architecture de LangChain/LangGraph sur la gestion d'état et d'orchestration.
