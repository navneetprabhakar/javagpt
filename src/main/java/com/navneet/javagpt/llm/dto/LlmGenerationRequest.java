package com.navneet.javagpt.llm.dto;

import lombok.Data;

@Data
public class LlmGenerationRequest {
    private String prompt;
    private Integer maxTokens;
    private Float temperature;
    private Integer topK;
    private Float topP;
    private boolean stream = false;
}
