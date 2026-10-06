package com.sazyconseil.mcpclient.agents;

import com.sazyconseil.mcpclient.observability.AgentEventPublisher;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class DeveloperAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final AgentEventPublisher eventPublisher;

    @Value("${agents.developer.model:qwen/qwen-2.5-coder-32b-instruct}")
    private String model = "qwen/qwen-2.5-coder-32b-instruct";

    @Value("${agents.developer.temperature:0.1}")
    private double temperature = 0.1;

    public DeveloperAgent(ChatClient.Builder chatClientBuilder, AgentEventPublisher eventPublisher) {
        this.chatClientBuilder = chatClientBuilder;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Reçoit les spécifications de l'Architecte et génère le code de production.
     */
    public Mono<String> develop(String spec) {
        return Mono.fromCallable(() -> {
            eventPublisher.publish("Developer", "THOUGHT", "Analyse des spécifications de l'Architecte et préparation du plan de code.");
            
            String systemPrompt = """
                Tu es un Développeur Java d'élite spécialisé dans Spring Boot 4 et WebFlux. Ton unique objectif est de produire un code source hautement maintenable, conforme aux contrats d'interfaces fournis par l'Architecte.
                
                Règles d'ingénierie strictes :
                - Préfère la composition à l'héritage complexe.
                - Évite absolument l'anti-pattern des interfaces de constantes.
                - Utilise des références de méthodes (this::myMethod) plutôt que des lambdas simples.
                - Ne génère pas de placeholders ou de commentaires de code non implémenté (// TODO). Tout code doit être fonctionnel et complet.
                """;

            ChatClient chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(temperature))
                .build();
            
            eventPublisher.publish("Developer", "ACTION", "Appel de l'LLM (" + model + ") pour l'écriture du code source...");
            
            String code = chatClient.prompt()
                .system(systemPrompt)
                .user(spec)
                .call()
                .content();
                
            eventPublisher.publish("Developer", "OUTPUT", "Code de production généré.");
            return code;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
}
