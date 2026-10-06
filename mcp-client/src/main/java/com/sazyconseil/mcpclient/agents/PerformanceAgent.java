package com.sazyconseil.mcpclient.agents;

import com.sazyconseil.mcpclient.observability.AgentEventPublisher;
import com.sazyconseil.mcpclient.orchestrator.SkillLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class PerformanceAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final SkillLoader skillLoader;
    private final AgentEventPublisher eventPublisher;

    @Value("${agents.performance.model:meta-llama/llama-3.3-70b-instruct}")
    private String model = "meta-llama/llama-3.3-70b-instruct";

    @Value("${agents.performance.temperature:0.1}")
    private double temperature = 0.1;

    public PerformanceAgent(ChatClient.Builder chatClientBuilder, SkillLoader skillLoader, AgentEventPublisher eventPublisher) {
        this.chatClientBuilder = chatClientBuilder;
        this.skillLoader = skillLoader;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Réalise un audit de performance sur le code généré, en se basant sur les skills de blocage réactif et SQL.
     */
    public Mono<String> audit(String code) {
        return Mono.fromCallable(() -> {
            eventPublisher.publish("Performance", "THOUGHT", "Chargement des consignes d'optimisation de performance et de requêtes SQL.");
            
            String reactiveSkill = skillLoader.loadSkill("reactive-blocking-guard");
            String sqlSkill = skillLoader.loadSkill("sql-optimizer");
            
            String systemPrompt = """
                Tu es un Auditeur de performance et de sécurité logicielle. Ton but est de passer au crible le code de production Java pour détecter d'éventuels appels bloquants ou des requêtes SQL sous-optimales ou dangereuses.
                
                Voici les règles d'audit réactif (reactive-blocking-guard) :
                %s
                
                Voici les règles d'optimisation SQL (sql-optimizer) :
                %s
                
                Analyse le code Java fourni en entrée. Produis un rapport d'audit détaillé structuré en deux parties :
                1. Les points de vigilance et violations de règles (le cas échéant).
                2. Les propositions de correctifs optimisés.
                """.formatted(reactiveSkill, sqlSkill);

            ChatClient chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(temperature))
                .build();
            
            eventPublisher.publish("Performance", "ACTION", "Appel du LLM (" + model + ") pour l'audit du code...");
            
            String report = chatClient.prompt()
                .system(systemPrompt)
                .user(code)
                .call()
                .content();
                
            eventPublisher.publish("Performance", "OUTPUT", "Rapport d'audit de performance généré.");
            return report;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
}
