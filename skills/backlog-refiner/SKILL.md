---
name: backlog-refiner
description: Analyzes raw, complex user requirements and decomposes them into high-quality, refined Scrum User Stories, then creates them automatically in the backlog.
---

# Backlog Refiner Skill

Ce skill guide l'agent dans l'analyse de besoins utilisateurs bruts, leur décomposition en User Stories Scrum de haute qualité, et leur intégration automatique dans le Backlog via les outils MCP.

---

## Directives Méthodologiques de Raffinement (Refinement Guidelines)

### 1. Format Standard de la User Story (INVEST)
Chaque story doit être rédigée selon le format Scrum standard. La description du ticket doit inclure obligatoirement :
```text
**En tant que** [Rôle de l'utilisateur]
**Je veux** [Action que l'utilisateur souhaite effectuer]
**Afin de** [Valeur ajoutée ou bénéfice métier de la fonctionnalité]
```

### 2. Critères d'Acceptation (Acceptance Criteria) au Format Gherkin
Chaque story doit posséder au moins 2 critères d'acceptation formalisés au format Gherkin (`Given/When/Then` ou `Etant donné/Quand/Alors`) pour assurer une validation claire par l'ingénieur QA.
* **Exemple** :
  ```gherkin
  **Étant donné** que je suis un utilisateur authentifié sur la page de paiement
  **Quand** je saisis un numéro de carte bancaire valide et clique sur "Valider"
  **Alors** mon compte est débité du montant du panier et une notification de succès s'affiche.
  ```

### 3. Estimation des Story Points (Fibonacci)
Estimez la complexité de chaque tâche selon la suite de Fibonacci modifiée :
* **1** : Tâche triviale (changement de texte, couleur).
* **2** : Tâche simple (création d'un endpoint de lecture simple, validation de formulaire).
* **3** : Tâche moyenne (formulaire complexe, envoi d'emails simples).
* **5** : Tâche substantielle (authentification JWT, intégration d'une API externe simple).
* **8** : Tâche complexe (intégration de passerelle de paiement tierce, workflow multi-étapes).
* **13** : *Épique* (trop grand pour être une seule story, doit être découpé en plusieurs tickets !).

### 4. Attribution des Priorités
* **LOW** : Amélioration cosmétique ou optionnelle.
* **MEDIUM** : Fonctionnalité importante mais non bloquante pour la V1.
* **HIGH** : Fonctionnalité essentielle de la V1 (ex: panier d'achat).
* **CRITICAL** : Fonctionnalité bloquante sans laquelle l'application ne peut pas fonctionner (ex: authentification, paiement).

---

## Outils MCP Associés

Pour enregistrer les tickets, l'agent utilise les outils exposés par le serveur :
* **`addTask(title, description, priority, storyPoints, acceptanceCriteria)`** : Crée la User Story.
* **`getTasks()`** : Liste les tâches existantes pour éviter les doublons.

---

## Grille de Décision pour l'Agent

Lorsqu'un utilisateur émet un besoin (ex: *"Je veux un panier d'achat et pouvoir payer"*), appliquez la boucle de contrôle suivante :

1. **Étape 1 : Analyse & Décomposition**
   * Listez les fonctionnalités requises (ex: Ajouter au panier, Voir le panier, Payer).
2. **Étape 2 : Rédaction des Stories**
   * Pour chaque fonctionnalité, rédigez le titre, le format *En tant que...*, les critères d'acceptation Gherkin, déterminez la priorité et estimez les Story Points.
3. **Étape 3 : Création dans le Backlog**
   * Appelez l'outil `BacklogService_addTask` pour chaque story rédigée.
4. **Étape 4 : Synthèse**
   * Présentez à l'utilisateur un rapport récapitulatif des tâches ajoutées avec leurs identifiants (ex: TASK-1, TASK-2, etc.).
