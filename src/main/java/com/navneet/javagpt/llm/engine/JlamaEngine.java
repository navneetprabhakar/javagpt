package com.navneet.javagpt.llm.engine;

import com.github.tjake.jlama.model.AbstractModel;
import com.github.tjake.jlama.model.ModelSupport;
import com.github.tjake.jlama.safetensors.DType;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Optional;
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
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new IllegalArgumentException("Model file not found: " + modelPath);
        }

        this.model = ModelSupport.loadModel(
                modelFile,
                DType.F32,
                DType.I8,
                Optional.empty(),
                Optional.empty()
        );
        this.loaded = true;
        log.info("Jlama model loaded successfully");
    }

    @Override
    public String generate(String prompt, int maxTokens, float temperature, int topK, float topP) {
        if (!loaded) {
            throw new IllegalStateException("Model not loaded");
        }

        UUID session = UUID.randomUUID();
        StringBuilder result = new StringBuilder();

        model.generate(session, prompt, prompt, temperature, maxTokens, false,
                (token, timing) -> result.append(token));

        return result.toString();
    }

    @Override
    public void generateStreaming(String prompt, int maxTokens, float temperature, Consumer<String> tokenCallback) {
        if (!loaded) {
            throw new IllegalStateException("Model not loaded");
        }

        UUID session = UUID.randomUUID();

        model.generate(session, prompt, prompt, temperature, maxTokens, false,
                (token, timing) -> tokenCallback.accept(token));
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
