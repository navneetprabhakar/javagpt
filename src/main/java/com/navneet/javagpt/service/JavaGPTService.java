package com.navneet.javagpt.service;

import ai.djl.Model;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import com.navneet.javagpt.config.ModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
@RequiredArgsConstructor
@Slf4j
public class JavaGPTService {

    private final Model model;
    private final HuggingFaceTokenizer tokenizer;
    private final ModelConfig config;

    private Predictor<String, String> predictor;

    @PostConstruct
    public void initialize() {
        log.info("Initializing JavaGPT Service with model: {}", config.getName());

        TextGenerationTranslator translator = new TextGenerationTranslator(
                tokenizer,
                config.getMaxLength(),
                config.getTemperature(),
                config.getTopK(),
                config.getTopP()
        );

        predictor = model.newPredictor(translator);
        log.info("JavaGPT Service initialized successfully");
    }

    @Cacheable(value = "textGeneration", key = "#prompt")
    public String generateText(String prompt) {
        try {
            log.debug("Generating text for prompt: {}", prompt);
            String result = predictor.predict(prompt);
            log.debug("Generated text length: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("Error generating text", e);
            throw new RuntimeException("Text generation failed", e);
        }
    }

    public String generateText(String prompt, int maxTokens, double temperature) {
        try {
            TextGenerationTranslator customTranslator = new TextGenerationTranslator(
                    tokenizer,
                    maxTokens,
                    temperature,
                    config.getTopK(),
                    config.getTopP()
            );

            try (Predictor<String, String> customPredictor = model.newPredictor(customTranslator)) {
                return customPredictor.predict(prompt);
            }
        } catch (Exception e) {
            log.error("Error generating text with custom parameters", e);
            throw new RuntimeException("Text generation failed", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up JavaGPT Service");
        if (predictor != null) {
            predictor.close();
        }
    }
}
