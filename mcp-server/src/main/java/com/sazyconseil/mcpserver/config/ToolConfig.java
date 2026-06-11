package com.sazyconseil.mcpserver.config;

import com.sazyconseil.mcpserver.service.WeatherService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ToolConfig {


    @Bean
    public List<ToolCallback> weatherTools(WeatherService weatherService) {
        return List.of(ToolCallbacks.from(weatherService));
    }
}
