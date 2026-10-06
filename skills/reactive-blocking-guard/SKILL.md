---
name: reactive-blocking-guard
description: Prohibits blocking calls (such as Thread.sleep, RestTemplate, and .block() on Reactor Mono/Flux) in Spring WebFlux reactive streams and includes an automated Node.js validation script. Use when writing, modifying, or auditing reactive Java files.
---

# Reactive Blocking Guard

Ce skill prévient l'introduction d'appels de méthode bloquants au sein de la stack réactive (Spring WebFlux et Project Reactor) du projet. Bloquer un thread dans un environnement réactif dégrade dramatiquement les performances en saturant l'Event Loop.

---

## Règles d'Or Réactives & Interdictions

### 1. Interdiction absolue de bloquer un flux réactif
N'appelez JAMAIS la méthode `.block()`, `.blockFirst()` ou `.blockLast()` au milieu de vos flux de données réactifs ou de vos méthodes de service.
* **Interdit** :
  ```java
  Mono<User> userMono = service.getUser(id);
  User user = userMono.block(); // Bloque le thread de l'Event Loop !
  ```
* **Autorisé** (Chaînage réactif) :
  ```java
  return service.getUser(id)
      .flatMap(user -> repository.saveProfile(user));
  ```

### 2. Pas de `Thread.sleep()`
Ne mettez jamais le thread actuel en sommeil pour introduire un délai ou temporiser une opération. Utilisez les opérateurs non-bloquants de Reactor.
* **Interdit** : `Thread.sleep(1000);`
* **Autorisé** : `Mono.delay(Duration.ofSeconds(1));`

### 3. Pas d'appels HTTP synchrones (`RestTemplate`)
L'utilisation de `RestTemplate` bloque le thread en attente de la réponse réseau. Utilisez le `WebClient` réactif ou le nouveau `RestClient` non-bloquant.
* **Interdit** : `restTemplate.getForObject(url, String.class);`
* **Autorisé** (WebClient) :
  ```java
  webClient.get()
      .uri(url)
      .retrieve()
      .bodyToMono(String.class);
  ```

---

## Outil de Validation Automatique

Ce skill embarque un script de validation statique en Node.js pour analyser de manière empirique votre code source avant toute soumission.

### Exécution du Validateur :
Lorsqu'on vous demande d'écrire ou de modifier un fichier de service ou de contrôleur WebFlux, vous devez exécuter ce script sur le fichier modifié :

```bash
node <chemin-du-skill>/scripts/validate_reactive_rules.cjs <chemin-du-fichier-java>
```

#### Exemple de sortie en cas de violation :
```text
❌ Échec de la validation réactive pour : WeatherService.java
Trouvé 1 violation(s) de blocage :
  - Ligne 45: [Appel bloquant Reactor (.block())] -> "String data = weatherMono.block();"
    💡 Suggestion: L'appel à .block() sur un Mono ou un Flux est interdit dans un contexte réactif.
```

---

## Grille de Décision pour l'Agent

Lors de l'écriture ou de l'analyse de code réactif :
1. **Étape 1 : Audit Automatique**
   * Exécutez le script `validate_reactive_rules.cjs` sur le fichier ciblé.
2. **Étape 2 : Résolution**
   * Si le script renvoie des erreurs, réécrivez le code réactif pour chaîner les opérations (via `.map()`, `.flatMap()`, etc.) ou utilisez des alternatives non-bloquantes (`Mono.delay` au lieu de `Thread.sleep`).
3. **Étape 3 : Ré-audit**
   * Relancez le validateur pour vous assurer que le code est désormais exempt de tout appel bloquant (code retour 0).
