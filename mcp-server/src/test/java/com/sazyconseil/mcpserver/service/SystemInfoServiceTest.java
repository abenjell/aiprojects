package com.sazyconseil.mcpserver.service;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemInfoServiceTest {

    private final SystemInfoService systemInfoService = new SystemInfoService();

    @Test
    void shouldReturnSystemLogs() {
        String logs = systemInfoService.getSystemLogs();
        assertThat(logs).contains("System initialized successfully.");
        assertThat(logs).contains("High memory usage detected");
    }

    @Test
    void shouldCreateAnalyzeSystemHealthPromptWithDefaultThreshold() {
        GetPromptResult promptResult = systemInfoService.analyzeSystemHealth(null);
        
        assertThat(promptResult.description()).isEqualTo("System Health Analysis Template");
        assertThat(promptResult.messages()).hasSize(1);
        
        PromptMessage userMsg = promptResult.messages().get(0);
        assertThat(userMsg.role()).isEqualTo(Role.USER);
        assertThat(((TextContent) userMsg.content()).text()).contains("expert DevOps engineer");
        assertThat(((TextContent) userMsg.content()).text()).contains("warning threshold of 80%");
    }

    @Test
    void shouldCreateAnalyzeSystemHealthPromptWithCustomThreshold() {
        GetPromptResult promptResult = systemInfoService.analyzeSystemHealth("90%");
        
        assertThat(promptResult.messages()).hasSize(1);
        PromptMessage userMsg = promptResult.messages().get(0);
        assertThat(((TextContent) userMsg.content()).text()).contains("warning threshold of 90%");
    }
}
