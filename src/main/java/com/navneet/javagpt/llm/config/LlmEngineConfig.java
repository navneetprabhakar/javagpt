package com.navneet.javagpt.llm.config;

import com.navneet.javagpt.llm.engine.JlamaEngine;
import com.navneet.javagpt.llm.engine.LlamaCppEngine;
import com.navneet.javagpt.llm.engine.LlmEngine;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "javagpt.llm")
@Data
@Slf4j
public class LlmEngineConfig {

    private String engine = "jlama";
    private String modelPath = "./models/model.gguf";
    private int maxTokens = 256;
    private float temperature = 0.7f;
    private int topK = 40;
    private float topP = 0.9f;
    private boolean enabled = false;

    @Bean
    @ConditionalOnProperty(name = "javagpt.llm.engine", havingValue = "jlama", matchIfMissing = true)
    public LlmEngine jlamaEngine() {
        return new JlamaEngine();
    }

    @Bean
    @ConditionalOnProperty(name = "javagpt.llm.engine", havingValue = "llamacpp")
    public LlmEngine llamaCppEngine() {
        return new LlamaCppEngine();
    }
}
