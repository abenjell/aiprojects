package com.sazyconseil.mcpclient.observability;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AgentEventPublisherTest {

    private final AgentEventPublisher publisher = new AgentEventPublisher();

    @Test
    void shouldPublishAndStreamEvents() {
        // Given
        AgentEvent event = new AgentEvent(Instant.now(), "TestAgent", "THOUGHT", "Thinking...");

        // When/Then
        Flux<AgentEvent> stream = publisher.getEventStream();

        StepVerifier.create(stream)
            .then(() -> publisher.publish(event))
            .assertNext(received -> {
                assertThat(received.agentName()).isEqualTo("TestAgent");
                assertThat(received.type()).isEqualTo("THOUGHT");
                assertThat(received.content()).isEqualTo("Thinking...");
            })
            .thenCancel()
            .verify();
    }
}
