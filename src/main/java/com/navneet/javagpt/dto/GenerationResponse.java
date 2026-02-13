package com.navneet.javagpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResponse {
    private String prompt;
    private String generatedText;
    private long processingTimeMs;
    private int inputTokens;
    private int outputTokens;
}
