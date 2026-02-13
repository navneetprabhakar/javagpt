package com.navneet.javagpt.llm.engine;

import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.LlamaOutput;
import de.kherud.llama.ModelParameters;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.function.Consumer;

@Slf4j
public class LlamaCppEngine implements LlmEngine {

    private LlamaModel model;
    private volatile boolean loaded = false;

    @Override
    public String getName() {
        return "llamacpp";
    }

    @Override
    public void loadModel(String modelPath) throws Exception {
        log.info("Loading model with llama.cpp engine from: {}", modelPath);
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new IllegalArgumentException("Model file not found: " + modelPath);
        }

        ModelParameters modelParams = new ModelParameters()
                .setModelFilePath(modelPath)
                .setNGpuLayers(0);

        this.model = new LlamaModel(modelParams);
        this.loaded = true;
        log.info("llama.cpp model loaded successfully");
    }

    @Override
    public String generate(String prompt, int maxTokens, float temperature, int topK, float topP) {
        if (!loaded) {
            throw new IllegalStateException("Model not loaded");
        }

        InferenceParameters params = new InferenceParameters(prompt)
                .setTemperature(temperature)
                .setTopK(topK)
                .setTopP(topP)
                .setNPredict(maxTokens);

        StringBuilder result = new StringBuilder();
        for (LlamaOutput output : model.generate(params)) {
            result.append(output);
        }

        return result.toString();
    }

    @Override
    public void generateStreaming(String prompt, int maxTokens, float temperature, Consumer<String> tokenCallback) {
        if (!loaded) {
            throw new IllegalStateException("Model not loaded");
        }

        InferenceParameters params = new InferenceParameters(prompt)
                .setTemperature(temperature)
                .setNPredict(maxTokens);

        for (LlamaOutput output : model.generate(params)) {
            tokenCallback.accept(output.toString());
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void close() {
        log.info("Closing llama.cpp engine");
        if (model != null) {
            model.close();
            model = null;
            loaded = false;
        }
    }
}
