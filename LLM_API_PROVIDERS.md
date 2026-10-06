# 🔑 Guide des Fournisseurs d'API de LLM pour la Software Factory

Ce guide récapitule les différentes options d'API (gratuites et payantes) utilisables pour alimenter les agents de notre **Software Factory Multi-Agents**. 

Toutes ces options sont configurables directement dans le fichier `application.yml` ou via des variables d'environnement en raison de leur compatibilité avec le protocole OpenAI.

---

## ☁️ 1. Les Options Cloud 100% Gratuites

### A. Groq (Recommandé : Ultra-rapide & Gratuit)
Groq propose une infrastructure matérielle LPU extrêmement rapide et offre un accès gratuit généreux pour les développeurs.
*   **Création de clé API** : [https://console.groq.com](https://console.groq.com)
*   **Modèles recommandés** : `llama-3.1-8b-instant` (très rapide), `llama-3.3-70b-versatile` (très intelligent).
*   **Configuration `application.yml`** :
    ```yaml
    spring:
      ai:
        openai:
          api-key: "gsk_votre_cle_groq_ici"
          base-url: "https://api.groq.com/openai/v1"
          chat:
            options:
              model: "llama-3.1-8b-instant"
    ```

### B. OpenRouter (Le Catalogue de Modèles Multi-sources)
OpenRouter centralise des dizaines de modèles de différents fournisseurs, dont plusieurs sont totalement gratuits (marqués `:free`).
*   **Création de clé API** : [https://openrouter.ai](https://openrouter.ai)
*   **⚠️ Note importante pour les outils (Tools)** : Les modèles gratuits d'OpenRouter nécessitent parfois d'activer l'envoi de requêtes vers des serveurs tiers gratuits. Allez dans vos préférences de confidentialité : [https://openrouter.ai/settings/privacy](https://openrouter.ai/settings/privacy) et activez l'option **"Enable free providers that may publish your prompts"**.
*   **Modèles recommandés** : `meta-llama/llama-3-8b-instruct:free` (gratuit), `openai/gpt-4o-mini` (payant à coût ultra-infime, excellent pour les outils).
*   **Configuration `application.yml`** :
    ```yaml
    spring:
      ai:
        openai:
          api-key: "sk-or-v1-votre_cle_openrouter"
          base-url: "https://openrouter.ai/api/v1"
          chat:
            options:
              model: "meta-llama/llama-3-8b-instruct:free"
    ```

### C. Mistral AI (Version Dev d'Évaluation)
Mistral propose un plan "La Plateforme" d'évaluation gratuit pour tester leurs modèles lors du développement.
*   **Création de clé API** : [https://console.mistral.ai](https://console.mistral.ai)
*   **Modèle recommandé** : `open-mistral-7b`
*   **Configuration `application.yml`** :
    ```yaml
    spring:
      ai:
        openai:
          api-key: "votre_cle_mistral"
          base-url: "https://api.mistral.ai/v1"
          chat:
            options:
              model: "open-mistral-7b"
    ```

---

## 💻 2. L'Option Locale, Privée et Illimitée (Ollama)

Si vous avez une machine dotée d'au moins **16 Go de RAM** ou d'une carte graphique Nvidia dédiée, vous pouvez exécuter des modèles de manière **100% gratuite et hors-ligne**.

*   **Téléchargement** : [https://ollama.com](https://ollama.com)
*   **Lancement d'un modèle en local** (dans votre terminal d'ordinateur) :
    ```bash
    # Pour un modèle généraliste rapide
    ollama run llama3.2:3b
    
    # Pour le meilleur modèle de code en local (recommandé pour coder !)
    ollama run qwen2.5-coder:7b
    ```
*   **Configuration `application.yml`** :
    ```yaml
    spring:
      ai:
        openai:
          api-key: "ollama" # Requis syntaxiquement par le starter mais ignoré par Ollama
          base-url: "http://localhost:11434/v1"
          chat:
            options:
              model: "qwen2.5-coder:7b"
    ```

---

## 💳 3. L'Option Cloud Officielle (Payante à très faible coût)

### OpenAI (La référence pour le code et l'analyse de skills)
Utiliser l'API officielle OpenAI avec un petit modèle rapide et pas cher comme `gpt-4o-mini` offre la meilleure précision pour l'exécution d'outils et de skills.
*   **Création de clé API** : [https://platform.openai.com](https://platform.openai.com)
*   **Prix** : Nécessite de créditer son compte d'un minimum de **5 $**. Le coût unitaire de `gpt-4o-mini` est si bas que ce crédit dure généralement plusieurs semaines de développement.
*   **Modèle recommandé** : `gpt-4o-mini`
*   **Configuration `application.yml`** :
    ```yaml
    spring:
      ai:
        openai:
          api-key: "sk-proj-votre_cle_openai"
          chat:
            options:
              model: "gpt-4o-mini"
    ```

---

## 🛠️ Rappel : Démarrage rapide sans commiter la clé API (Variables d'environnement)

Pour éviter d'inscrire vos clés secrètes en clair dans vos fichiers commités sur Git, configurez les variables d'environnement dans votre terminal avant de démarrer :

```bash
# Exemple pour Groq
export SPRING_AI_OPENAI_API_KEY="gsk_votre_cle"
export SPRING_AI_OPENAI_BASE_URL="https://api.groq.com/openai/v1"
export SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL="llama-3.1-8b-instant"

./mvnw spring-boot:run -pl mcp-client
```
