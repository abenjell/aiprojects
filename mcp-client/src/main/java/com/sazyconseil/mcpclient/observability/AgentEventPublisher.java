package com.sazyconseil.mcpclient.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Service
public class AgentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AgentEventPublisher.class);

    // Sink pour diffuser les événements à plusieurs abonnés en conservant la contre-pression.
    // autoCancel = false évite d'éteindre le sink lorsque le dernier client se déconnecte (ex: rafraîchissement SSE)
    private final Sinks.Many<AgentEvent> sink = Sinks.many()
            .multicast()
            .onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    /**
     * Publie un nouvel événement dans le flux.
     */
    public void publish(AgentEvent event) {
        log.info("[{}] [{}] {}", event.agentName(), event.type(), event.content());
        sink.tryEmitNext(event);
    }

    /**
     * Publie un nouvel événement à la volée.
     */
    public void publish(String agentName, String type, String content) {
        publish(AgentEvent.info(agentName, type, content));
    }

    /**
     * Récupère le flux d'événements pour l'abonnement SSE.
     */
    public Flux<AgentEvent> getEventStream() {
        return sink.asFlux();
    }
}
