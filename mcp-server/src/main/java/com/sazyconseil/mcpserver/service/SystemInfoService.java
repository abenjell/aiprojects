package com.sazyconseil.mcpserver.service;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpArg;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemInfoService {

    @McpResource(
            uri = "logs://system",
            name = "System Logs",
            description = "Provides access to the active system logs of the server"
    )
    public String getSystemLogs() {
        return "2026-06-11 12:39:10 [INFO] System initialized successfully.\n" +
               "2026-06-11 12:40:02 [INFO] Connection established to Open-Meteo API.\n" +
               "2026-06-11 12:42:15 [WARN] High memory usage detected: 82% of total RAM.\n" +
               "2026-06-11 12:45:00 [INFO] WeatherService_getWeather called for coordinates (48.8566, 2.3522).\n" +
               "2026-06-11 12:50:33 [INFO] WeatherService_getAlerts successfully fetched alerts for state 'CA'.\n";
    }

    @McpPrompt(
            name = "analyze_system_health",
            description = "Fills guidelines and constraints for analyzing system metrics and logs to evaluate system health"
    )
    public GetPromptResult analyzeSystemHealth(
            @McpArg(name = "threshold", description = "The warning threshold for resource metrics in percentage", required = false) String threshold
    ) {
        String warningThreshold = threshold != null ? threshold : "80%";
        String systemInstruction = "You are an expert DevOps engineer and systems reliability analyst.";
        String userPrompt = String.format(
                "%s\n\nPlease analyze the logs from `logs://system`. Check for any warnings, errors, or anomalies.\n" +
                "Evaluate if any metric exceeds the warning threshold of %s.\n" +
                "Provide a structured health assessment report including: Status (HEALTHY/WARNING/CRITICAL), " +
                "Issues identified, and recommended actions.",
                systemInstruction,
                warningThreshold
        );

        return new GetPromptResult(
                "System Health Analysis Template",
                List.of(
                        new PromptMessage(Role.USER, new TextContent(userPrompt))
                )
        );
    }
}
