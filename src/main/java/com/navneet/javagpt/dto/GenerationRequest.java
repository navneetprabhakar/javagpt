package com.navneet.javagpt.dto;

import lombok.Data;

@Data
public class GenerationRequest {
    private String prompt;
    private Integer maxTokens;
    private Double temperature;
    private Integer topK;
    private Double topP;
}
