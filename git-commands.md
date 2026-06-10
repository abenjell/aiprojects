# Guide des commandes Git utilisées

Ce document répertorie les commandes Git clés utilisées au cours de cette session pour restructurer le projet multi-module, nettoyer la configuration et consolider l'historique de commits.

---

## 1. Analyse et diagnostic du dépôt
Commandes pour comprendre la structure actuelle du dépôt et des fichiers suivis :
```bash
# Vérifier l'état de l'index et des fichiers modifiés ou non suivis
git status

# Afficher tous les fichiers non suivis, y compris dans les sous-dossiers
git status -u

# Lister tous les fichiers actuellement suivis par Git
git ls-files

# Voir l'URL du dépôt distant (remote) configuré
git remote -v

# Consulter l'historique des derniers commits sous forme condensée
git log -n 5 --oneline
```

---

## 2. Consolidation d'un dépôt imbriqué (Submodule -> standard folder)
Pour intégrer un sous-module Git existant (`mcp-server`) en tant que dossier standard dans le dépôt racine de `aiprojects` :
```bash
# 1. Supprimer le dossier .git interne du sous-module
rm -rf mcp-server/.git

# 2. Ajouter l'intégralité du dossier mcp-server à l'index de staging de la racine
git add mcp-server
```

---

## 3. Gestion et nettoyage des fichiers
Commandes pour nettoyer les fichiers `.gitignore` doublons et renommer/convertir les configurations :

### Suppression des `.gitignore` redondants :
```bash
# Supprimer les .gitignore des sous-modules et enregistrer la suppression dans l'index
git rm -f mcp-server/.gitignore mcp-client/.gitignore
```

### Conversion de `.properties` vers `.yml` et modifications de packages :
```bash
# Supprimer les anciens fichiers properties de l'index
git rm mcp-server/src/main/resources/application.properties mcp-client/src/main/resources/application.properties

# Ajouter les nouveaux fichiers de configuration YAML et les classes Java mises à jour
git add mcp-server/src/main/resources/application.yml \
        mcp-client/src/main/resources/application.yml \
        mcp-client/src/main/java/com/sazyconseil/mcpclient/McpClientApplication.java \
        mcp-client/src/test/java/com/sazyconseil/mcpclient/AiprojectsApplicationTests.java
```

---

## 4. Commits et Push initiaux
```bash
# Créer un commit pour la restructuration et le gitignore unique
git commit -m "Consolidate project structure with single root .gitignore"

# Configurer la branche locale pour suivre la branche distante et pousser
git push -u origin main
```

---

## 5. Fusion de commits (Squash)
Pour fusionner (squasher) les 2 derniers commits locaux en un seul commit propre avant de pousser :
```bash
# 1. Effectuer un reset soft vers le parent d'il y a 2 commits (HEAD~2).
# Les modifications restent indexées (staged) prêtes à être commitées.
git reset --soft HEAD~2

# 2. Valider les changements fusionnés dans un unique commit propre
git commit -m "Convert configuration files to YAML and update mcp-client packages"

# 3. Pousser le commit consolidé vers le serveur distant
git push
```
