package com.navneet.javagpt.config;

import ai.djl.Model;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "javagpt.model")
@Data
public class ModelConfig {

    private String name = "gpt2-medium";
    private String cacheDir = "./models";
    private int maxLength = 512;
    private double temperature = 0.7;
    private int topK = 50;
    private double topP = 0.9;

    @Bean
    public Model loadModel() throws IOException {
        Model model = Model.newInstance(name);

        Path modelPath = Paths.get(cacheDir, name);

        model.load(modelPath);

        return model;
    }

    @Bean
    public HuggingFaceTokenizer tokenizer() throws IOException {
        return HuggingFaceTokenizer.builder()
                .optTokenizerName(name)
                .optManager(Model.newInstance(name).getNDManager())
                .build();
    }
}
