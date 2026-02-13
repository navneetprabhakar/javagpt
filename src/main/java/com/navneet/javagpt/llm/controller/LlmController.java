package com.navneet.javagpt.llm.controller;

import com.navneet.javagpt.llm.config.LlmEngineConfig;
import com.navneet.javagpt.llm.dto.LlmGenerationRequest;
import com.navneet.javagpt.llm.dto.LlmGenerationResponse;
import com.navneet.javagpt.llm.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/llm")
@ConditionalOnProperty(name = "javagpt.llm.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class LlmController {

    private final LlmService llmService;
    private final LlmEngineConfig config;

    @PostMapping("/generate")
    public ResponseEntity<LlmGenerationResponse> generate(@RequestBody LlmGenerationRequest request) {
        log.info("LLM generation request - engine: {}, prompt: {}", config.getEngine(), request.getPrompt());

        long startTime = System.currentTimeMillis();

        String generatedText = llmService.generate(
                request.getPrompt(),
                request.getMaxTokens(),
                request.getTemperature(),
                request.getTopK(),
                request.getTopP()
        );

        long processingTime = System.currentTimeMillis() - startTime;

        LlmGenerationResponse response = LlmGenerationResponse.builder()
                .prompt(request.getPrompt())
                .generatedText(generatedText)
                .processingTimeMs(processingTime)
                .modelName(config.getModelPath())
                .engine(llmService.getEngineName())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStream(@RequestBody LlmGenerationRequest request) {
        log.info("LLM streaming request - engine: {}, prompt: {}", config.getEngine(), request.getPrompt());

        return llmService.generateStream(
                request.getPrompt(),
                request.getMaxTokens(),
                request.getTemperature()
        );
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", llmService.isReady() ? "ready" : "not_ready",
                "engine", llmService.getEngineName(),
                "modelPath", config.getModelPath()
        ));
    }
}
