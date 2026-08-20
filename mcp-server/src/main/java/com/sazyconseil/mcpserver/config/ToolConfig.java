package com.sazyconseil.mcpserver.config;

import com.sazyconseil.mcpserver.service.BacklogService;
import com.sazyconseil.mcpserver.service.WeatherService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ToolConfig {

    @Bean
    public List<ToolCallback> mcpTools(WeatherService weatherService, BacklogService backlogService) {
        List<ToolCallback> allTools = new ArrayList<>();
        allTools.addAll(List.of(ToolCallbacks.from(weatherService)));
        allTools.addAll(List.of(ToolCallbacks.from(backlogService)));
        return allTools;
    }
}
