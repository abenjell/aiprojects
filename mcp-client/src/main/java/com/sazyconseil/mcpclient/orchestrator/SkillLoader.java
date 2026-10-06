package com.sazyconseil.mcpclient.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class SkillLoader {

    private static final Logger log = LoggerFactory.getLogger(SkillLoader.class);
    private final ResourceLoader resourceLoader;

    public SkillLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Charge le contenu d'un skill Markdown en fonction de son nom.
     * Le fichier doit se trouver dans le dossier racine : skills/[skillName]/SKILL.md
     */
    public String loadSkill(String skillName) {
        String path = "file:skills/" + skillName + "/SKILL.md";
        log.debug("Tentative de chargement du skill depuis {}", path);
        try {
            Resource resource = resourceLoader.getResource(path);
            if (!resource.exists()) {
                // Fallback si exécuté depuis le sous-dossier d'un module Maven (ex: mcp-client)
                String fallbackPath = "file:../skills/" + skillName + "/SKILL.md";
                log.debug("Skill non trouvé à la racine, tentative depuis {}", fallbackPath);
                resource = resourceLoader.getResource(fallbackPath);
                if (!resource.exists()) {
                    log.warn("Le skill '{}' n'existe pas aux emplacements '{}' ni '{}'", skillName, path, fallbackPath);
                    return "";
                }
            }
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier du skill: " + skillName, e);
            return "";
        }
    }
}
