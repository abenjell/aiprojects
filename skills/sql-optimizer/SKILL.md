---
name: sql-optimizer
description: Enforces robust guardrails, performance guidelines, and security practices for SQL query generation. Use when writing, refactoring, or optimizing SQL queries or repository layers.
---

# SQL Optimizer Guard

Ce skill encadre la génération, l'écriture et l'optimisation des requêtes SQL au sein du projet. Son objectif est de garantir de hauts standards de performance, d'éviter les régressions d'indexation, et d'assurer une sécurité totale contre les injections.

---

## Garde-Fous de Performance (Performance Guardrails)

### 1. Interdiction absolue du `SELECT *`
Toutes les requêtes de sélection doivent lister explicitement les colonnes requises. Le `SELECT *` est strictement interdit pour éviter le gaspillage de bande passante réseau, préserver l'usage du cache et assurer la compatibilité ascendante des schémas.
* **Interdit** : `SELECT * FROM users WHERE id = :id`
* **Autorisé** : `SELECT id, email, role, created_at FROM users WHERE id = :id`

### 2. Préservation des Index (Index SARGability)
Toute clause `WHERE`, `JOIN` ou `ORDER BY` exploitant un index doit être **SARGable** (Search Argument Able). N'appliquez jamais de fonction ou d'opération arithmétique directement sur une colonne indexée.
* **Interdit** : `WHERE UPPER(username) = 'ADIL'` *(désactive l'index sur `username`)*
* **Autorisé** : `WHERE username = :username` *(avec conversion en amont dans l'application si nécessaire)*
* **Interdit** : `WHERE date_creation + INTERVAL 7 DAY >= NOW()`
* **Autorisé** : `WHERE date_creation >= NOW() - INTERVAL 7 DAY`

### 3. Jointures Explicites
L'utilisation de jointures implicites (sélection de plusieurs tables séparées par des virgules dans la clause `FROM`) est proscrite. Utilisez toujours des syntaxes de jointures explicites avec la clause `ON`.
* **Interdit** : `FROM orders o, users u WHERE o.user_id = u.id`
* **Autorisé** : `FROM orders o INNER JOIN users u ON o.user_id = u.id`

---

## Garde-Fous de Sécurité (Security Guardrails)

### Prévention des Injections SQL
Toute valeur dynamique passée à une requête SQL doit être paramétrée à l'aide de variables de liaison (bind variables, placeholders) ou de paramètres nommés. La concaténation de chaînes de caractères pour injecter des valeurs utilisateur dans le SQL est **strictement interdite**.
* **Interdit (Danger d'injection)** : `"SELECT id FROM products WHERE category = '" + userInput + "'"`
* **Autorisé (Sécurisé)** : `SELECT id FROM products WHERE category = :category` (ou `?` en JDBC)

---

## Grille de Décision pour l'Agent

Lorsque vous écrivez ou réparez une requête SQL :
1. **Étape 1 : Analyse des Variables**
   * Remplacez toute concaténation dynamique par des paramètres nommés (`:param`) ou positionnels (`?`).
2. **Étape 2 : Analyse des Colonnes**
   * Remplacez tous les `SELECT *` par les colonnes précises nécessaires au cas d'usage.
3. **Étape 3 : Analyse des Clauses SARGable**
   * Inspectez les fonctions appliquées aux colonnes dans `WHERE`. Réécrivez-les pour déplacer les calculs du côté de la valeur/du paramètre plutôt que de la colonne indexée.
4. **Étape 4 : Syntaxe de Jointure**
   * Assurez-vous d'utiliser `INNER JOIN`, `LEFT OUTER JOIN`, etc. de façon explicite.
