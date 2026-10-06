package com.sazyconseil.mcpclient.agents;

import com.sazyconseil.mcpclient.observability.AgentEventPublisher;
import com.sazyconseil.mcpclient.orchestrator.SkillLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class BacklogRefinerAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final SkillLoader skillLoader;
    private final AgentEventPublisher eventPublisher;
    private final SyncMcpToolCallbackProvider mcpTools;

    @Value("${agents.backlog-refiner.model:meta-llama/llama-3.1-8b-instruct:free}")
    private String model = "meta-llama/llama-3.1-8b-instruct:free";

    @Value("${agents.backlog-refiner.temperature:0.1}")
    private double temperature = 0.1;

    public BacklogRefinerAgent(
            ChatClient.Builder chatClientBuilder,
            SkillLoader skillLoader,
            AgentEventPublisher eventPublisher,
            @Autowired(required = false) SyncMcpToolCallbackProvider mcpTools) {
        this.chatClientBuilder = chatClientBuilder;
        this.skillLoader = skillLoader;
        this.eventPublisher = eventPublisher;
        this.mcpTools = mcpTools;
    }

    /**
     * Reçoit le besoin utilisateur brut, le décompose en User Stories Scrum de haute qualité,
     * et l'enregistre automatiquement dans le backlog JSON en utilisant les outils du serveur MCP.
     */
    public Mono<String> refineAndPopulate(String requirement) {
        return Mono.fromCallable(() -> {
            eventPublisher.publish("BacklogRefiner", "THOUGHT", "Analyse du besoin brut et chargement des règles de raffinement (backlog-refiner).");

            String refinerSkill = skillLoader.loadSkill("backlog-refiner");

            String systemPrompt = """
                Tu es un Product Owner (PO) d'élite spécialisé dans le raffinement de Backlog Scrum.
                Ton rôle est d'analyser l'expression de besoin de l'utilisateur, de la décomposer en User Stories Scrum de haute qualité, et d'appeler les outils du serveur MCP pour peupler automatiquement le Backlog.
                
                Voici tes consignes méthodologiques :
                %s
                
                Règles de comportement :
                - Pour CHAQUE User Story identifiée, tu DOIS obligatoirement appeler l'outil de création de tâche (ex: `addTask`) disponible dans tes outils MCP pour l'insérer dans le backlog.
                - N'oublie pas de spécifier la priorité, les story points, le format "En tant que... Je veux... Afin de...", et d'inclure des critères d'acceptation au format Gherkin clairs.
                - Une fois toutes les tâches créées, présente un rapport récapitulatif détaillé structuré des User Stories créées avec leurs identifiants retournés par l'outil de création.
                """.formatted(refinerSkill);

            ChatClient.Builder builder = chatClientBuilder;
            ChatClient chatClient;
            
            var options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature);
                
            if (mcpTools != null) {
                eventPublisher.publish("BacklogRefiner", "ACTION", "Exécution du raffinement (modèle : " + model + ") et appel dynamique des outils MCP...");
                chatClient = builder.defaultTools(mcpTools).defaultOptions(options).build();
            } else {
                eventPublisher.publish("BacklogRefiner", "ACTION", "Exécution du raffinement (modèle : " + model + ") en mode dégradé (sans outils)...");
                chatClient = builder.defaultOptions(options).build();
            }

            String summary = chatClient.prompt()
                .system(systemPrompt)
                .user(requirement)
                .call()
                .content();

            eventPublisher.publish("BacklogRefiner", "OUTPUT", "Backlog raffiné et peuplé. Rapport récapitulatif généré.");
            return summary;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
}
