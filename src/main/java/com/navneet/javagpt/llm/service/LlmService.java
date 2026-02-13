package com.navneet.javagpt.llm.service;

import com.navneet.javagpt.llm.config.LlmEngineConfig;
import com.navneet.javagpt.llm.engine.LlmEngine;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@ConditionalOnProperty(name = "javagpt.llm.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class LlmService {

    private final LlmEngine engine;
    private final LlmEngineConfig config;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing LLM engine: {} with model: {}", config.getEngine(), config.getModelPath());
            engine.loadModel(config.getModelPath());
            log.info("LLM engine initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize LLM engine", e);
            throw new RuntimeException("LLM engine initialization failed", e);
        }
    }

    public String generate(String prompt, Integer maxTokens, Float temperature, Integer topK, Float topP) {
        int tokens = maxTokens != null ? maxTokens : config.getMaxTokens();
        float temp = temperature != null ? temperature : config.getTemperature();
        int k = topK != null ? topK : config.getTopK();
        float p = topP != null ? topP : config.getTopP();

        return engine.generate(prompt, tokens, temp, k, p);
    }

    public Flux<String> generateStream(String prompt, Integer maxTokens, Float temperature) {
        int tokens = maxTokens != null ? maxTokens : config.getMaxTokens();
        float temp = temperature != null ? temperature : config.getTemperature();

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        Thread.startVirtualThread(() -> {
            try {
                engine.generateStreaming(prompt, tokens, temp, token -> {
                    sink.tryEmitNext(token);
                });
                sink.tryEmitComplete();
            } catch (Exception e) {
                log.error("Streaming generation failed", e);
                sink.tryEmitError(e);
            }
        });

        return sink.asFlux();
    }

    public String getEngineName() {
        return engine.getName();
    }

    public boolean isReady() {
        return engine.isLoaded();
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down LLM engine");
        engine.close();
    }
}
