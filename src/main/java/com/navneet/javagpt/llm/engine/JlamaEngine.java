package com.navneet.javagpt.llm.engine;

import com.github.tjake.jlama.model.AbstractModel;
import com.github.tjake.jlama.model.ModelSupport;
import com.github.tjake.jlama.model.functions.Generator;
import com.github.tjake.jlama.safetensors.DType;
import com.github.tjake.jlama.safetensors.prompt.PromptContext;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class JlamaEngine implements LlmEngine {

    private AbstractModel model;
    private volatile boolean loaded = false;

    @Override
    public String getName() {
        return "jlama";
    }

    @Override
    public void loadModel(String modelPath) throws Exception {
        log.info("Loading model with Jlama engine from: {}", modelPath);
        File modelDir = new File(modelPath);

        // Jlama requires a HuggingFace model directory (with config.json, tokenizer, safetensors)
        // NOT a single GGUF file. If a .gguf file is provided, guide the user.
        if (modelPath.endsWith(".gguf")) {
            throw new IllegalArgumentException(
                    "Jlama does not support GGUF files. Provide a HuggingFace model directory path " +
                    "(e.g., ./models/mistral-7b-instruct-v0.2) or switch to engine: llamacpp for GGUF support.");
        }

        if (!modelDir.exists() || !modelDir.isDirectory()) {
            throw new IllegalArgumentException("Model directory not found: " + modelPath +
                    ". Jlama requires a HuggingFace model directory containing config.json and safetensors files.");
        }

        this.model = ModelSupport.loadModel(
                modelDir,
                DType.F32,
                DType.Q4
        );
        this.loaded = true;
        log.info("Jlama model loaded successfully");
    }

    @Override
    public String generate(String prompt, int maxTokens, float temperature, int topK, float topP) {
        if (!loaded) {
            throw new IllegalStateException("Model not loaded");
        }

        PromptContext promptContext = PromptContext.of(prompt);

        Generator.Response response = model.generate(
                UUID.randomUUID(),
                promptContext,
                temperature,
                maxTokens,
                (token, timing) -> {}
        );

        return response.responseText;
    }

    @Override
    public void generateStreaming(String prompt, int maxTokens, float temperature, Consumer<String> tokenCallback) {
        if (!loaded) {
            throw new IllegalStateException("Model not loaded");
        }

        PromptContext promptContext = PromptContext.of(prompt);

        model.generate(
                UUID.randomUUID(),
                promptContext,
                temperature,
                maxTokens,
                (String token, Float timing) -> tokenCallback.accept(token)
        );
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void close() {
        log.info("Closing Jlama engine");
        if (model != null) {
            model.close();
            model = null;
            loaded = false;
        }
    }
}
