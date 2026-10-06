---
name: spring-boot-4-migration
description: Guides the migration of Java/Kotlin applications from Spring Boot 3.5 to Spring Boot 4.0, specifically preserving and configuring the WebFlux Reactive stack. Use when upgrading pom.xml, updating configuration properties, security beans, Jackson classes, or Mockito annotations.
---

# Spring Boot 4.0 Migration Guard & Guide (Stack Réactive / WebFlux)

Ce skill encadre et automatise la migration d'applications Java de **Spring Boot 3.5** vers **Spring Boot 4.0** (basé sur Spring Framework 7.0 et Jakarta EE 11). Il applique des règles strictes pour corriger les modifications majeures et préserve de manière absolue la **stack réactive (Spring WebFlux)**.

---

## 1. Prérequis & Changement de Plateforme (Stack Réactive)

* **Java Baseline** : Java 17 minimum (Java 21 ou 25 recommandé pour l'usage optimal des Virtual Threads et de l'ordonnancement réactif).
* **Jakarta EE 11** : Utilise désormais un conteneur Servlet 6.1 (pour les serveurs réactifs sous-jacents comme Netty, qui est mis à jour à sa dernière version compatible).
* **Préservation de la Stack Réactive** :
  * Conserver obligatoirement `spring-boot-starter-webflux` dans le `pom.xml`.
  * Pour les clients réactifs isolés, préférez le starter modulaire introduit en 4.0 : `spring-boot-starter-webclient`.

---

## 2. Guide de Migration de Code (Breaking Changes Majeurs)

### A. Remplacement de `@MockBean` et `@SpyBean` (Tests)
Ces deux annotations ont été complètement supprimées au profit des annotations natives de Spring Framework.
* **Interdit** :
  ```java
  import org.springframework.boot.test.mock.mockito.MockBean;
  import org.springframework.boot.test.mock.mockito.SpyBean;

  @MockBean
  private WeatherService weatherService;
  ```
* **Autorisé** :
  ```java
  import org.springframework.test.context.bean.override.mockito.MockitoBean;
  import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

  @MockitoBean
  private WeatherService weatherService;
  ```

### B. Migration vers Jackson 3 (Génération de JSON)
Jackson 3 est désormais la bibliothèque JSON préférée. Ses namespaces et packages ont totalement changé.
* **Imports de Packages** :
  * **Interdit** : `import com.fasterxml.jackson.databind.ObjectMapper;`
  * **Autorisé** : `import tools.jackson.databind.json.JsonMapper;`
* **Instanciation** :
  * **Interdit** : `new ObjectMapper()`
  * **Autorisé** : `JsonMapper.builder().build()`

### C. Spring Security 7 Réactif : Syntaxe Lambda DSL Obligatoire
Pour la stack réactive, la configuration s'appuie sur `ServerHttpSecurity` et produit un `SecurityWebFilterChain`. L'enchaînement de configurations via la méthode `.and()` a été complètement retiré.
* **Interdit** :
  ```java
  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
      return http
          .authorizeExchange()
              .pathMatchers("/sse").permitAll()
              .anyExchange().authenticated()
              .and()
          .httpBasic()
          .and()
          .build();
  }
  ```
* **Autorisé** :
  ```java
  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
      return http
          .authorizeExchange(exchanges -> exchanges
              .pathMatchers("/sse").permitAll()
              .anyExchange().authenticated()
          )
          .httpBasic(Customizer.withDefaults())
          .build();
  }
  ```

---

## 3. Grille de Décision pour l'Agent

Lorsqu'on vous demande d'exécuter ou d'analyser la migration vers Spring Boot 4 d'une application réactive :

1. **Étape 1 : Analyse et Mise à jour du `pom.xml` (Préservation Réactive)**
   * Montez la version parente de Spring Boot à `4.0.0` (ou ultérieure).
   * Modifiez la version de Java à `21` dans les propriétés.
   * **Conservez et sécurisez la présence de `spring-boot-starter-webflux`** (ne jamais la remplacer par `spring-boot-starter-web`).
   * Ajoutez temporairement le migrateur de propriétés au runtime :
     ```xml
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-properties-migrator</artifactId>
         <scope>runtime</scope>
     </dependency>
     ```

2. **Étape 2 : Réécriture des annotations de test réactifs**
   * Remplacez toutes les occurrences de `@MockBean` par `@MockitoBean` dans vos tests réactifs WebFlux.

3. **Étape 3 : Réécriture des Beans de Sécurité Réactive**
   * Identifiez les beans retournant un `SecurityWebFilterChain` et réécrivez-les pour éliminer toute utilisation de `.and()`, en adoptant la syntaxe Lambda DSL réactive.

4. **Étape 4 : Validation**
   * Demandez à l'utilisateur de lancer `./mvnw clean test` pour valider la compilation réactive et le passage de tous les tests.
