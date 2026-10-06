# Guide & Mémo : Ollama sur Mini PC Ubuntu

Ce document récapitule l'ensemble de la configuration, des caractéristiques matérielles, des commandes d'administration et des bonnes pratiques pour l'exploitation de LLM locaux via **Ollama** sur le Mini PC dédié.

---

## 1. Caractéristiques Matérielles du Mini PC

* **Adresse IP** : `192.168.8.131` (Port Ollama : `11434`, Port SSH : `22`)
* **Utilisateur** : `adil`
* **Processeur (CPU)** : Intel Core i5-6200U (2 cœurs / 4 threads, 2.30 GHz, Skylake, supporte les instructions AVX2).
* **Carte graphique (GPU)** : Intel HD Graphics 520 intégrée (l'inférence s'exécute **100% sur le CPU**).
* **Stockage** : SSD ~117 Go (~76 Go disponibles pour les modèles).
* **Mémoire RAM actuelle** : 
  * Total : 8 Go (1 barrette SK Hynix `HMT41GS6BFR8A-PB` installée).
  * **1 slot SODIMM libre disponible**.
  * Capacité maximale de la carte mère : **16 Go** (2 x 8 Go).

### 🛒 Upgrade RAM Recommandée (~10 - 15 €)
Pour faire tourner des modèles 7B/8B confortablement et débloquer le **Dual Channel** (+15% à 20% de vitesse d'inférence CPU) :
* **Type** : `SODIMM DDR3L 8 Go 1600 MHz 1.35 V PC3L-12800S` (204 broches).
* **Points d'attention** : Impérativement **DDR3L** ou **PC3L** (**1.35 V** basse tension, ne pas prendre de la DDR3 1.5 V classique).
* **Marques recommandées** : Crucial (ex: *CT102464BF160B*), Samsung, SK Hynix (*HMT41GS6BFR8A-PB*), Kingston.

---

## 2. Modèles Recommandés & Performances Attendues (CPU-Only)

| Catégorie | Modèle | Taille disque / RAM | Vitesse estimée | Cas d'usage idéal |
| :--- | :--- | :--- | :--- | :--- |
| **Code / Dév** | `qwen2.5-coder:3b` | ~1.9 Go | 8 - 12 tok/s | Génération de code, scripts, refactoring |
| **Raisonnement** | `deepseek-r1:1.5b` | ~1.1 Go | 12 - 18 tok/s | Pensée pas-à-pas, logique, mathématiques |
| **Ultra-léger** | `llama3.2:1b` | ~1.3 Go | 15 - 25 tok/s | Réponses instantanées, résumés rapides |
| **Polyvalent (Sweet Spot)** | `qwen2.5:3b` | ~2.0 Go | 7 - 12 tok/s | Chat général, excellent en français |
| **Avancé (Lourd)** | `mistral:7b` ou `llama3.1:8b` | ~4.5 Go | 1.5 - 3 tok/s | Tâches de fond complexes (préférable avec 16 Go de RAM) |

---

## 3. Accès SSH & Authentification par Clé (CLI & PuTTY)

### Connexion en ligne de commande
```bash
# Déployer la clé publique existante (id_ed25519) sur le Mini PC
ssh-copy-id -i ~/.ssh/id_ed25519.pub adil@192.168.8.131

# Connexion directe sans mot de passe
ssh adil@192.168.8.131
```

### Configuration PuTTY (Windows)
PuTTY nécessite le format propriétaire `.ppk` :
1. **Convertir la clé sous Linux/WSL** :
   ```bash
   sudo apt install -y putty-tools
   puttygen ~/.ssh/id_ed25519 -O private -o /mnt/c/Users/<VotreUserWindows>/id_ed25519.ppk
   ```
2. **Dans PuTTY** :
   * **Host Name** : `192.168.8.131` (Port 22)
   * **Connection > Data** : `Auto-login username` = `adil`
   * **Connection > SSH > Auth > Credentials** : Sélectionner le fichier `.ppk` dans *Private key file for authentication*.
   * **Session** : Sauvegarder sous `Ollama-MiniPC`.

---

## 4. Configuration Réseau d'Ollama (Accès Distant)

Par défaut, Ollama écoute sur `127.0.0.1:11434`. Pour l'ouvrir au réseau local (`192.168.8.131:11434`) :

1. Créer le fichier d'override systemd sur la machine Ubuntu :
   ```bash
   sudo mkdir -p /etc/systemd/system/ollama.service.d
   echo -e '[Service]\nEnvironment="OLLAMA_HOST=0.0.0.0"' | sudo tee /etc/systemd/system/ollama.service.d/override.conf
   ```
2. Recharger et redémarrer le service :
   ```bash
   sudo systemctl daemon-reload && sudo systemctl restart ollama
   ```
3. Ouvrir le port dans le pare-feu :
   ```bash
   sudo ufw allow 11434/tcp
   ```

---

## 5. Différence `/api/generate` vs `/api/chat`

* **`/api/generate` (Complétion brute / One-shot)** :
  * Entrée : `"prompt": "texte brut"`.
  * Sortie : `"response": "..."`.
  * Idéal pour les scripts simples, autocomplétion, classification ou résumés sans mémoire de conversation.
* **`/api/chat` (Conversationnel multi-tours & Agents)** :
  * Entrée : `"messages": [{"role": "system|user|assistant|tool", "content": "..."}]`.
  * Sortie : `"message": {"role": "assistant", "content": "..."}`.
  * Supporte le **Tool Calling / Function Calling** (indispensable pour Spring AI, agents ReAct et MCP).

---

## 6. Aide-Mémoire Commandes CLI & API REST

| Action | Commande CLI Ollama | Requête API REST (`curl`) |
| :--- | :--- | :--- |
| **Lister les modèles installés** | `ollama list` | `curl -s http://192.168.8.131:11434/api/tags \| jq` |
| **Voir les modèles chargés en RAM** | `ollama ps` | `curl -s http://192.168.8.131:11434/api/ps \| jq` |
| **Décharger un modèle de la RAM** | `ollama stop <modele>` | `curl -s http://192.168.8.131:11434/api/generate -d '{"model":"<modele>","keep_alive":0}'` |
| **Télécharger un modèle** | `ollama pull <modele>` | `curl -s http://192.168.8.131:11434/api/pull -d '{"model":"<modele>"}'` |
| **Supprimer un modèle du disque** | `ollama rm <modele>` | `curl -s -X DELETE http://192.168.8.131:11434/api/delete -d '{"model":"<modele>"}'` |
| **Exécuter un prompt (generate)** | `ollama run <modele> "..."` | `curl -s http://192.168.8.131:11434/api/generate -d '{"model":"<modele>","prompt":"Bonjour","stream":false}' \| jq .response` |
| **Exécuter un message (chat)** | — | `curl -s http://192.168.8.131:11434/api/chat -d '{"model":"<modele>","messages":[{"role":"user","content":"Hello"}],"stream":false}' \| jq .message.content` |

---

## 7. Script Gestionnaire de Modèles (`scripts/start-llm.sh`)

Un script a été mis en place dans le dossier `scripts/start-llm.sh` de ce projet. Il s'assure de **purger automatiquement la RAM** de tout modèle précédent avant d'en charger un nouveau afin d'éviter le swap mémoire.

### Exemples d'utilisation :
```bash
./scripts/start-llm.sh              # Menu interactif avec état RAM en temps réel
./scripts/start-llm.sh code         # Lance qwen2.5-coder:3b
./scripts/start-llm.sh raisonnement # Lance deepseek-r1:1.5b
./scripts/start-llm.sh leger        # Lance llama3.2:1b
./scripts/start-llm.sh general      # Lance qwen2.5:3b
./scripts/start-llm.sh stop         # Vide immédiatement la RAM
./scripts/start-llm.sh list         # Affiche les modèles installés
```

---

## 8. Optimisation Système : Passage en mode Headless

Si le Mini PC possède une interface graphique Ubuntu Desktop, vous pouvez la désactiver pour récupérer **~1.2 Go de RAM supplémentaire** :

```bash
# Désactiver le bureau graphique au démarrage
sudo systemctl set-default multi-user.target
sudo reboot

# (Pour réactiver l'interface graphique ultérieurement si nécessaire) :
# sudo systemctl set-default graphical.target
```
