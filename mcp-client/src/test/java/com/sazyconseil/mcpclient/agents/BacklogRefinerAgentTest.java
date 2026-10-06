package com.sazyconseil.mcpclient.agents;

import com.sazyconseil.mcpclient.observability.AgentEventPublisher;
import com.sazyconseil.mcpclient.orchestrator.SkillLoader;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BacklogRefinerAgentTest {

    @Test
    void shouldRefineAndPopulateSuccessfully() {
        // Given
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(builder.build()).thenReturn(chatClient);
        when(builder.defaultTools(any())).thenReturn(builder);
        when(builder.defaultOptions(any())).thenReturn(builder);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Mocked summary of backlog");

        SkillLoader skillLoader = mock(SkillLoader.class);
        when(skillLoader.loadSkill("backlog-refiner")).thenReturn("Mocked skill rules");

        AgentEventPublisher eventPublisher = mock(AgentEventPublisher.class);
        SyncMcpToolCallbackProvider mcpTools = mock(SyncMcpToolCallbackProvider.class);

        BacklogRefinerAgent agent = new BacklogRefinerAgent(builder, skillLoader, eventPublisher, mcpTools);

        // When
        var resultMono = agent.refineAndPopulate("Un besoin utilisateur");

        // Then
        StepVerifier.create(resultMono)
                .assertNext(summary -> {
                    assertThat(summary).isEqualTo("Mocked summary of backlog");
                })
                .verifyComplete();

        verify(eventPublisher, times(3)).publish(anyString(), anyString(), anyString());
        verify(builder).defaultTools(mcpTools);
    }

    @Test
    void shouldRefineInDegradedModeWhenMcpToolsNull() {
        // Given
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(builder.build()).thenReturn(chatClient);
        when(builder.defaultOptions(any())).thenReturn(builder);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Mocked summary of backlog in degraded mode");

        SkillLoader skillLoader = mock(SkillLoader.class);
        when(skillLoader.loadSkill("backlog-refiner")).thenReturn("Mocked skill rules");

        AgentEventPublisher eventPublisher = mock(AgentEventPublisher.class);

        BacklogRefinerAgent agent = new BacklogRefinerAgent(builder, skillLoader, eventPublisher, null);

        // When
        var resultMono = agent.refineAndPopulate("Un besoin utilisateur");

        // Then
        StepVerifier.create(resultMono)
                .assertNext(summary -> {
                    assertThat(summary).isEqualTo("Mocked summary of backlog in degraded mode");
                })
                .verifyComplete();

        verify(eventPublisher, times(3)).publish(anyString(), anyString(), anyString());
        verify(builder, never()).defaultTools(any());
    }
}
