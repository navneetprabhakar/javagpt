package com.navneet.javagpt.llm.engine;

import java.util.function.Consumer;

public interface LlmEngine {

    String getName();

    void loadModel(String modelPath) throws Exception;

    String generate(String prompt, int maxTokens, float temperature, int topK, float topP);

    void generateStreaming(String prompt, int maxTokens, float temperature, Consumer<String> tokenCallback);

    boolean isLoaded();

    void close();
}
