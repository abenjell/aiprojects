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
public class QaAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final SkillLoader skillLoader;
    private final AgentEventPublisher eventPublisher;

    @Value("${agents.qa.model:meta-llama/llama-3.1-8b-instruct:free}")
    private String model = "meta-llama/llama-3.1-8b-instruct:free";

    @Value("${agents.qa.temperature:0.2}")
    private double temperature = 0.2;

    public QaAgent(ChatClient.Builder chatClientBuilder, SkillLoader skillLoader, AgentEventPublisher eventPublisher) {
        this.chatClientBuilder = chatClientBuilder;
        this.skillLoader = skillLoader;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Génère une suite de tests unitaires conforme au skill AssertJ pour le code donné.
     */
    public Mono<String> generateTests(String code) {
        return Mono.fromCallable(() -> {
            eventPublisher.publish("QA", "THOUGHT", "Chargement des règles AssertJ pour l'écriture des tests unitaires.");
            
            String assertjSkill = skillLoader.loadSkill("java-assertj-guard");
            
            String systemPrompt = """
                Tu es un Ingénieur QA automatisé spécialisé dans la rédaction de tests unitaires Java de qualité industrielle.
                
                Voici les directives qualité que tu dois appliquer à la lettre (interdiction d'utiliser d'autres bibliothèques d'assertion) :
                %s
                
                Génère la suite de tests unitaires pour le code source fourni par l'utilisateur.
                """.formatted(assertjSkill);

            ChatClient chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(temperature))
                .build();
            
            eventPublisher.publish("QA", "ACTION", "Appel du LLM (" + model + ") pour la génération des tests avec injection de 'java-assertj-guard'...");
            
            String tests = chatClient.prompt()
                .system(systemPrompt)
                .user(code)
                .call()
                .content();
                
            eventPublisher.publish("QA", "OUTPUT", "Tests unitaires AssertJ rédigés.");
            return tests;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
}
