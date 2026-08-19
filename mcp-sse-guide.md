# Guide de Test du Serveur MCP en mode SSE (Server-Sent Events)

Ce guide détaille les étapes et les requêtes nécessaires pour tester manuellement le serveur MCP Spring Boot en utilisant un client HTTP comme **Postman** ou **curl**.

Le protocole MCP en mode SSE est asynchrone et étatique (stateful) : les requêtes HTTP `POST` envoient des commandes au serveur, et le serveur publie les réponses dans le flux continu `GET /sse`.

---

## Étape 1 : Ouvrir la connexion SSE (Flux de réception)

Cette requête doit rester ouverte et active pendant toute la durée du test. C'est ici que s'afficheront toutes les réponses du serveur (événements SSE).

* **Méthode** : `GET`
* **URL** : `http://localhost:8080/sse`
* **Headers** :
  * `Accept`: `text/event-stream`
  * `Cache-Control`: `no-cache`
  * `Connection`: `keep-alive`

### Réponse attendue (dans le flux SSE) :
Dès la connexion, le serveur publie un événement d'initialisation contenant votre `sessionId` unique :
```text
event: endpoint
data: {"sessionId":"4410da09-3d47-4af9-887d-e05bb55d9641","endpoint":"/mcp/message"}
```
> **Notez le `sessionId`** (par ex : `4410da09-3d47-4af9-887d-e05bb55d9641`) pour les étapes suivantes.

### Astuce : Formater le flux en temps réel avec `curl` et `jq`

Si vous testez depuis un terminal, le flux brut peut être difficile à lire et les outils comme `cut` ou `grep` classiques peuvent introduire des retards d'affichage à cause du tamponnage (buffering).

Voici la commande recommandée pour filtrer, extraire la `sessionId` et formater automatiquement le JSON en temps réel :

```bash
curl -N -s http://172.30.160.1:8080/sse | jq -r --unbuffered -R '
  select(startswith("data:")) 
  | sub("^data:\\s*"; "")
  | if test("sessionId=") then
      (capture("sessionId=(?<id>[^&\\s]+)") | "\n=== SESSION STARTED: \(.id) ===\n")
    elseTu p
      (fromjson? // .)
    end
'
```

Cette commande :
* Désactive le tamponnage de `curl` et de `jq` pour un affichage instantané.
* Filtre et nettoie les préfixes `data:`.
* Extrait proprement la `sessionId` lors de la connexion initiale et l'affiche clairement.
* Formate et colore magnifiquement tout le JSON reçu par la suite.

---

## Étape 2 : Initialiser la session (Handshake - Partie 1)

Cette requête envoie les informations du client au serveur.

* **Méthode** : `POST`
* **URL** : `http://localhost:8080/mcp/message?sessionId=VOTRE_SESSION_ID`
* **Headers** :
  * `Content-Type`: `application/json`
* **Body (JSON)** :
  ```json
  {
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {
        "name": "postman-client",
        "version": "1.0.0"
      }
    }
  }
  ```

### Réponse attendue (dans le flux SSE de l'Étape 1) :
Vous verrez apparaître l'événement de réponse d'initialisation avec l'identifiant `"id": 1` :
```json
event: message
data: {
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "completions": {},
      "logging": {},
      "prompts": { "listChanged": true },
      "resources": { "subscribe": false, "listChanged": true },
      "tools": { "listChanged": true }
    },
    "serverInfo": { "name": "my-weather-server", "version": "0.0.1" }
  }
}
```

---

## Étape 3 : Confirmer l'initialisation (Handshake - Partie 2)

**Obligatoire pour finaliser la poignée de main.** Sans cette étape, le serveur refusera de répondre à toute autre requête opérationnelle (comme lister les outils).

* **Méthode** : `POST`
* **URL** : `http://localhost:8080/mcp/message?sessionId=VOTRE_SESSION_ID`
* **Headers** :
  * `Content-Type`: `application/json`
* **Body (JSON)** :
  ```json
  {
    "jsonrpc": "2.0",
    "method": "notifications/initialized",
    "params": {}
  }
  ```
> *Note : Comme c'est une notification ("notification"), elle n'a pas de champ `"id"` et le serveur ne renverra aucune réponse dans le flux SSE. C'est normal.*

---

## Étape 4 : Lister les outils disponibles (`tools/list`)

Maintenant que la session est officiellement active, vous pouvez lister les outils exposés par le serveur.

* **Méthode** : `POST`
* **URL** : `http://localhost:8080/mcp/message?sessionId=VOTRE_SESSION_ID`
* **Headers** :
  * `Content-Type`: `application/json`
* **Body (JSON)** :
  ```json
  {
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }
  ```

### Réponse attendue (dans le flux SSE de l'Étape 1) :
L'événement contenant la liste de vos outils de météo va apparaître :
```json
event: message
data: {
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "tools": [
      {
        "name": "WeatherService_getWeather",
        "description": "Get weather forecast for a specific latitude/longitude",
        "inputSchema": {
          "type": "object",
          "properties": {
            "latitude": { "type": "number" },
            "longitude": { "type": "number" }
          },
          "required": ["latitude", "longitude"]
        }
      },
      {
        "name": "WeatherService_getAlerts",
        "description": "Get weather alerts for a US state",
        "inputSchema": {
          "type": "object",
          "properties": {
            "state": { "type": "string", "description": "Two-letter US state code (e.g. CA, NY)" }
          },
          "required": ["state"]
        }
      }
    ]
  }
}
```
