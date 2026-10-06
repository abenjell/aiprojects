package com.sazyconseil.mcpclient.orchestrator;

import com.sazyconseil.mcpclient.agents.BacklogRefinerAgent;
import com.sazyconseil.mcpclient.agents.DeveloperAgent;
import com.sazyconseil.mcpclient.agents.QaAgent;
import com.sazyconseil.mcpclient.agents.PerformanceAgent;
import com.sazyconseil.mcpclient.observability.AgentEventPublisher;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AgentArchitect {

    private final ChatClient.Builder chatClientBuilder;
    private final BacklogRefinerAgent backlogRefinerAgent;
    private final DeveloperAgent developerAgent;
    private final QaAgent qaAgent;
    private final PerformanceAgent performanceAgent;
    private final AgentEventPublisher eventPublisher;

    @Value("${agents.architect.model:meta-llama/llama-3.3-70b-instruct}")
    private String model = "meta-llama/llama-3.3-70b-instruct";

    @Value("${agents.architect.temperature:0.2}")
    private double temperature = 0.2;

    public AgentArchitect(
            ChatClient.Builder chatClientBuilder,
            BacklogRefinerAgent backlogRefinerAgent,
            DeveloperAgent developerAgent,
            QaAgent qaAgent,
            PerformanceAgent performanceAgent,
            AgentEventPublisher eventPublisher) {
        this.chatClientBuilder = chatClientBuilder;
        this.backlogRefinerAgent = backlogRefinerAgent;
        this.developerAgent = developerAgent;
        this.qaAgent = qaAgent;
        this.performanceAgent = performanceAgent;
        this.eventPublisher = eventPublisher;
    }

    private record SpecAndBacklog(String spec, String backlogReport) {}

    /**
     * Reçoit le besoin utilisateur, crée la spec architecturale, puis coordonne les agents en cascade.
     */
    public Mono<String> processRequirement(String userRequirement) {
        return backlogRefinerAgent.refineAndPopulate(userRequirement)
            .onErrorResume(ex -> {
                eventPublisher.publish("BacklogRefiner", "ERROR", "Échec du raffinement du backlog : " + ex.getMessage());
                return Mono.just("// Échec du raffinement du backlog en raison d'une erreur ou d'une incompatibilité de modèle.\n// Détails : " + ex.getMessage());
            })
            .flatMap(backlogReport -> Mono.fromCallable(() -> {
                eventPublisher.publish("Architect", "THOUGHT", "Réception du rapport de backlog. Conception de la spécification d'architecture.");
                
                String systemPrompt = """
                    Tu es l'Agent Architecte (Superviseur) d'une équipe de développement d'élite.
                    Ton rôle est d'analyser le rapport des User Stories créées dans le backlog et d'en concevoir une spécification technique et d'architecture propre en Java/Spring Boot.
                    
                    Rédige un cahier des charges d'architecture (contrats d'interfaces, dépendances et structure des classes à créer).
                    """;

                ChatClient chatClient = chatClientBuilder
                    .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .temperature(temperature))
                    .build();
                
                eventPublisher.publish("Architect", "ACTION", "Appel de l'LLM (" + model + ") pour concevoir l'architecture...");
                
                String spec = chatClient.prompt()
                    .system(systemPrompt)
                    .user(backlogReport)
                    .call()
                    .content();
                    
                eventPublisher.publish("Architect", "OUTPUT", "Spécification technique d'architecture rédigée.");
                return new SpecAndBacklog(spec, backlogReport);
            })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(pair -> developerAgent.develop(pair.spec())
                .onErrorResume(ex -> {
                    eventPublisher.publish("Developer", "ERROR", "Échec de l'écriture du code : " + ex.getMessage());
                    return Mono.just("// Échec de la génération du code par le Développeur en raison d'une erreur ou d'un timeout.\n// Détails : " + ex.getMessage());
                })
                .flatMap(code -> qaAgent.generateTests(code)
                    .onErrorResume(ex -> {
                        eventPublisher.publish("QA", "ERROR", "Échec de la génération des tests : " + ex.getMessage());
                        return Mono.just("// Échec de la génération des tests par l'agent QA.\n// Détails : " + ex.getMessage());
                    })
                    .flatMap(tests -> performanceAgent.audit(code)
                        .onErrorResume(ex -> {
                            eventPublisher.publish("Performance", "ERROR", "Échec de l'audit de performance : " + ex.getMessage());
                            return Mono.just("# Échec de l'audit de performance\nDétails : " + ex.getMessage());
                        })
                        .map(audit -> {
                            eventPublisher.publish("System", "SYSTEM", "Compilation des livrables finaux par l'Architecte (certains agents ont pu échouer).");
                            return formatFinalReport(pair.backlogReport(), pair.spec(), code, tests, audit);
                        })
                    )
                )
            )
        );
    }

    private String formatFinalReport(String backlogReport, String spec, String code, String tests, String audit) {
        return """
            # 🚀 Rapport de Production de la Software Factory Multi-Agents
            
            ---
            
            ## 📋 0. Rapport du Backlog Raffiné (Backlog Refiner)
            %s
            
            ---
            
            ## 📐 1. Spécification d'Architecture (Architecte)
            %s
            
            ---
            
            ## 💻 2. Code de Production Généré (Developer)
            ```java
            %s
            ```
            
            ---
            
            ## 🧪 3. Suite de Tests Unitaires (QA Agent - AssertJ)
            ```java
            %s
            ```
            
            ---
            
            ## ⚡ 4. Audit de Performance & Sécurité (Performance Agent)
            %s
            """.formatted(backlogReport, spec, code, tests, audit);
    }
}
