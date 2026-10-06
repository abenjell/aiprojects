package com.sazyconseil.mcpclient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.ai.mcp.client.enabled=false",
    "spring.ai.openai.api-key=dummy"
})
class McpClientApplicationTests {

    @Test
    void contextLoads() {
    }

}
