package com.navneet.javagpt.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmGenerationResponse {
    private String prompt;
    private String generatedText;
    private long processingTimeMs;
    private String modelName;
    private String engine;
}
