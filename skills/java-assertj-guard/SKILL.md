---
name: java-assertj-guard
description: Enforces AssertJ assertions (assertThat) in Java unit and integration tests and strictly prohibits JUnit, TestNG, or native Java assert statements. Use when generating, modifying, or refactoring Java test classes.
---

# Java AssertJ Guard

Ce skill encadre la génération et la modification de tests unitaires et d'intégration en Java dans le projet. Il impose l'usage exclusif de la bibliothèque d'assertions **AssertJ** pour garantir des tests expressifs, lisibles et standardisés.

## Règles d'Or & Interdictions

### 1. Interdiction des assertions JUnit / TestNG
L'utilisation directe de méthodes d'assertion issues de frameworks de test comme JUnit 5 (`org.junit.jupiter.api.Assertions`) ou TestNG est **strictement interdite**.
* **Interdit** : `assertEquals(a, b)`, `assertTrue(cond)`, `assertNotNull(obj)`
* **Autorisé** : `assertThat(a).isEqualTo(b)`, `assertThat(cond).isTrue()`, `assertThat(obj).isNotNull()`

### 2. Interdiction du mot-clé natif `assert`
L'utilisation du mot-clé Java natif `assert` est proscrite pour valider le comportement du code dans les tests.
* **Interdit** : `assert x == 5;`
* **Autorisé** : `assertThat(x).isEqualTo(5);`

### 3. Import Statique Unique
Toutes les assertions doivent être importées statiquement via la classe d'entrée unique d'AssertJ :
```java
import static org.assertj.core.api.Assertions.assertThat;
// Ou pour les exceptions si nécessaire :
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

---

## Exemples de Conversion (Avant / Après)

### Validation d'Équivalence et de Nullité
* **Mauvais (JUnit 5)** :
  ```java
  assertEquals("succès", status);
  assertNotNull(response);
  ```
* **Bon (AssertJ)** :
  ```java
  assertThat(status).isEqualTo("succès");
  assertThat(response).isNotNull();
  ```

### Validation de Collections et Listes
* **Mauvais (JUnit 5)** :
  ```java
  assertEquals(3, list.size());
  assertTrue(list.contains("A"));
  ```
* **Bon (AssertJ)** :
  ```java
  assertThat(list)
      .hasSize(3)
      .contains("A")
      .doesNotContain("Z");
  ```

### Validation des Exceptions
* **Mauvais (JUnit 5)** :
  ```java
  assertThrows(IllegalArgumentException.class, () -> service.process(null));
  ```
* **Bon (AssertJ)** :
  ```java
  assertThatThrownBy(() -> service.process(null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("invalid argument");
  ```

---

## Grille de Décision pour l'Agent

Lorsqu'on vous demande d'écrire, modifier ou réparer un fichier de test Java :
1. **Étape 1 : Analyse des imports**
   * Parcourez les imports existants du fichier de test.
   * Supprimez tout import commençant par `org.junit.jupiter.api.Assertions.*` ou `static org.junit.jupiter.api.Assertions.*`.
   * Ajoutez l'import statique `import static org.assertj.core.api.Assertions.assertThat;`.
2. **Étape 2 : Réécriture chirurgicale**
   * Transformez chaque assertion non conforme en expression AssertJ équivalente et idiomatique.
   * Privilégiez le chaînage d'assertions AssertJ (Fluent API) pour les validations complexes (collections, objets imbriqués).
3. **Étape 3 : Validation**
   * Vérifiez la compilation et le passage des tests en demandant à l'utilisateur d'exécuter la commande Maven appropriée (`./mvnw test` ou `./mvnw test -Dtest=NomDuTest`).
