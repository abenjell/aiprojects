#!/bin/bash
# ==============================================================================
# Aide-Mémoire : Commandes CLI & API REST Ollama (Serveur : 192.168.8.131:11434)
# ==============================================================================
#
# 1. Lister les modèles installés :
#    - CLI : ollama list
#    - API : curl -s http://192.168.8.131:11434/api/tags | jq
#
# 2. Voir les modèles actuellement chargés en RAM :
#    - CLI : ollama ps
#    - API : curl -s http://192.168.8.131:11434/api/ps | jq
#
# 3. Décharger / Arrêter un modèle actif en RAM (libérer la mémoire) :
#    - CLI : ollama stop <modele>
#    - API : curl -s http://192.168.8.131:11434/api/generate -d '{"model": "<modele>", "keep_alive": 0}'
#
# 4. Télécharger un modèle sans le lancer :
#    - CLI : ollama pull <modele>
#    - API : curl -s http://192.168.8.131:11434/api/pull -d '{"model": "<modele>"}'
#
# 5. Supprimer un modèle du disque :
#    - CLI : ollama rm <modele>
#    - API : curl -s -X DELETE http://192.168.8.131:11434/api/delete -d '{"model": "<modele>"}'
#
# 6. Exécuter une invite (Prompt / Inférence texte) :
#    - CLI : ollama run <modele> "Explique la différence entre Mono et Flux en Spring WebFlux"
#    - API : curl -s http://192.168.8.131:11434/api/generate -d '{
#              "model": "<modele>",
#              "prompt": "Bonjour, qui es-tu ?",
#              "stream": false
#            }' | jq .response
#
# 7. Endpoint Chat / Messages structurés (Compatible API standard) :
#    - API : curl -s http://192.168.8.131:11434/api/chat -d '{
#              "model": "<modele>",
#              "messages": [{"role": "user", "content": "Hello !"}],
#              "stream": false
#            }' | jq .message.content
#
# 8. Maintenance du service Ollama sur Ubuntu :
#    - Statut   : sudo systemctl status ollama
#    - Redémarrer : sudo systemctl restart ollama
#    - Logs     : sudo journalctl -u ollama -f --no-tail
# ==============================================================================

# Couleurs pour le terminal
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Fonction pour décharger tous les modèles de la RAM
liberer_memoire() {
    modeles_actifs=$(ollama ps | awk 'NR>1 {print $1}')
    if [ -n "$modeles_actifs" ]; then
        echo -e "${YELLOW}>>> Modèle(s) actif(s) détecté(s) en RAM. Libération de la mémoire...${NC}"
        for m in $modeles_actifs; do
            echo -e "    Arrêt forcé de : ${RED}$m${NC}..."
            ollama stop "$m"
        done
        echo -e "${GREEN}>>> RAM libérée avec succès.${NC}\n"
    else
        echo -e "${GREEN}>>> Aucun modèle en RAM.${NC}\n"
    fi
}

afficher_menu() {
    clear
    echo -e "${BLUE}====================================================${NC}"
    echo -e "${GREEN}       Sélecteur de LLM Local (Ollama - CPU)       ${NC}"
    echo -e "${BLUE}====================================================${NC}"
    
    modele_en_cours=$(ollama ps | awk 'NR==2 {print $1}')
    if [ -n "$modele_en_cours" ]; then
        echo -e "État RAM : ${YELLOW}Modèle actif -> $modele_en_cours${NC}"
    else
        echo -e "État RAM : ${GREEN}Aucun modèle en RAM (RAM libre)${NC}"
    fi
    echo -e "${BLUE}----------------------------------------------------${NC}"
    echo -e "1) ${GREEN}Code / Dév${NC}          : qwen2.5-coder:3b (~1.9 Go)"
    echo -e "2) ${GREEN}Raisonnement${NC}        : deepseek-r1:1.5b (~1.1 Go)"
    echo -e "3) ${GREEN}Ultra-Léger & Rapide${NC}: llama3.2:1b     (~1.3 Go)"
    echo -e "4) ${GREEN}Général / Polyvalent${NC}: qwen2.5:3b       (~2.0 Go)"
    echo -e "5) ${YELLOW}Avancé (Lourd)${NC}      : mistral:7b       (~4.1 Go, ~2 tok/s)"
    echo -e "----------------------------------------------------"
    echo -e "6) ${RED}Vider la RAM (Stop tous les modèles)${NC}"
    echo -e "7) Voir les modèles installés (${BLUE}ollama list${NC})"
    echo -e "8) Quitter"
    echo -e "${BLUE}====================================================${NC}"
}

lancer_modele() {
    local modele=$1
    liberer_memoire
    echo -e "${YELLOW}>>> Démarrage du modèle [${modele}]...${NC}"
    echo -e "${YELLOW}>>> (Astuce : tapez /bye pour quitter la session interactve)${NC}\n"
    ollama run "$modele"
}

# Mode CLI direct (passé en argument)
case "$1" in
    code)
        lancer_modele "qwen2.5-coder:3b"
        exit 0
        ;;
    raisonnement|reasoning)
        lancer_modele "deepseek-r1:1.5b"
        exit 0
        ;;
    leger|fast)
        lancer_modele "llama3.2:1b"
        exit 0
        ;;
    general|polyvalent)
        lancer_modele "qwen2.5:3b"
        exit 0
        ;;
    lourd|7b)
        lancer_modele "mistral:7b"
        exit 0
        ;;
    stop|free)
        liberer_memoire
        exit 0
        ;;
    list)
        ollama list
        exit 0
        ;;
esac

# Mode interactif avec menu
while true; do
    afficher_menu
    read -p "Choisissez une option [1-8] : " choix
    case $choix in
        1)
            lancer_modele "qwen2.5-coder:3b"
            break
            ;;
        2)
            lancer_modele "deepseek-r1:1.5b"
            break
            ;;
        3)
            lancer_modele "llama3.2:1b"
            break
            ;;
        4)
            lancer_modele "qwen2.5:3b"
            break
            ;;
        5)
            echo -e "${RED}Attention : Sur un Core i5 à 2 cœurs, un modèle 7B tourne à ~2 tokens/s.${NC}"
            read -p "Continuer quand même ? (o/N) : " rep
            if [[ "$rep" =~ ^[oO]$ ]]; then
                lancer_modele "mistral:7b"
                break
            fi
            ;;
        6)
            liberer_memoire
            read -p "Appuyez sur Entrée pour continuer..."
            ;;
        7)
            echo -e "\n${BLUE}Modèles installés localement :${NC}"
            ollama list
            read -p "Appuyez sur Entrée pour revenir au menu..."
            ;;
        8)
            echo "Au revoir !"
            exit 0
            ;;
        *)
            echo -e "${RED}Option invalide.${NC}"
            sleep 1
            ;;
    esac
done
