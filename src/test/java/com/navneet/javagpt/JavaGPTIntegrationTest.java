package com.navneet.javagpt;

import com.navneet.javagpt.dto.GenerationRequest;
import com.navneet.javagpt.dto.GenerationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JavaGPTIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testTextGeneration() {
        GenerationRequest request = new GenerationRequest();
        request.setPrompt("The future of AI is");
        request.setMaxTokens(50);
        request.setTemperature(0.7);

        ResponseEntity<GenerationResponse> response = restTemplate.postForEntity(
                "/api/v1/generate",
                request,
                GenerationResponse.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getGeneratedText()).isNotEmpty();
    }
}
