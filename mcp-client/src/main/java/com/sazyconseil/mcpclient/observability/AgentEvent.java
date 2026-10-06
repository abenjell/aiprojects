package com.sazyconseil.mcpclient.observability;

import java.time.Instant;

/**
 * Représente un événement de trace d'agent pour l'observabilité en direct.
 */
public record AgentEvent(
    Instant timestamp,
    String agentName,
    String type,
    String content
) {
    public static AgentEvent info(String agentName, String type, String content) {
        return new AgentEvent(Instant.now(), agentName, type, content);
    }
}
