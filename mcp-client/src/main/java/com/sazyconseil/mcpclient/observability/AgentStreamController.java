package com.sazyconseil.mcpclient.observability;

import com.sazyconseil.mcpclient.orchestrator.AgentArchitect;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/agents")
public class AgentStreamController {

    private final AgentEventPublisher eventPublisher;
    private final AgentArchitect agentArchitect;

    public AgentStreamController(AgentEventPublisher eventPublisher, AgentArchitect agentArchitect) {
        this.eventPublisher = eventPublisher;
        this.agentArchitect = agentArchitect;
    }

    /**
     * Flux d'événements SSE pour l'observabilité en direct.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AgentEvent>> streamEvents() {
        Flux<ServerSentEvent<AgentEvent>> heartBeat = Flux.interval(Duration.ofSeconds(15))
            .map(seq -> ServerSentEvent.<AgentEvent>builder()
                .comment("keep-alive")
                .build());

        Flux<ServerSentEvent<AgentEvent>> events = eventPublisher.getEventStream()
            .map(event -> ServerSentEvent.builder(event)
                .event("agent-event")
                .build());

        return Flux.merge(heartBeat, events);
    }

    /**
     * Déclenche l'orchestration multi-agents pour traiter une expression de besoin.
     */
    @PostMapping(value = "/process", consumes = MediaType.TEXT_PLAIN_VALUE, produces = "text/markdown;charset=UTF-8")
    public Mono<String> processRequirement(@RequestBody String requirement) {
        return agentArchitect.processRequirement(requirement);
    }
}
