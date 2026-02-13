package com.navneet.javagpt.controller;

import com.navneet.javagpt.dto.GenerationRequest;
import com.navneet.javagpt.dto.GenerationResponse;
import com.navneet.javagpt.service.JavaGPTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/generate")
@RequiredArgsConstructor
@Slf4j
public class JavaGPTController {

    private final JavaGPTService javaGPTService;

    @PostMapping
    public ResponseEntity<GenerationResponse> generateText(@RequestBody GenerationRequest request) {
        log.info("Received generation request for prompt: {}", request.getPrompt());

        long startTime = System.currentTimeMillis();

        String generatedText;
        if (request.getMaxTokens() != null && request.getTemperature() != null) {
            generatedText = javaGPTService.generateText(
                    request.getPrompt(),
                    request.getMaxTokens(),
                    request.getTemperature()
            );
        } else {
            generatedText = javaGPTService.generateText(request.getPrompt());
        }

        long processingTime = System.currentTimeMillis() - startTime;

        GenerationResponse response = GenerationResponse.builder()
                .prompt(request.getPrompt())
                .generatedText(generatedText)
                .processingTimeMs(processingTime)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("JavaGPT Service is running");
    }
}
