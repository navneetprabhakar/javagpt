package com.navneet.javagpt.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class TextGenerationTranslator implements Translator<String, String> {

    private final HuggingFaceTokenizer tokenizer;
    private final int maxLength;
    private final double temperature;
    private final int topK;
    private final double topP;

    public TextGenerationTranslator(
            HuggingFaceTokenizer tokenizer,
            int maxLength,
            double temperature,
            int topK,
            double topP) {
        this.tokenizer = tokenizer;
        this.maxLength = maxLength;
        this.temperature = temperature;
        this.topK = topK;
        this.topP = topP;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String input) {
        Encoding encoding = tokenizer.encode(input);
        long[] ids = encoding.getIds();

        NDManager manager = ctx.getNDManager();
        NDArray inputIds = manager.create(ids);

        return new NDList(inputIds);
    }

    @Override
    public String processOutput(TranslatorContext ctx, NDList list) {
        NDArray output = list.singletonOrThrow();
        long[] tokenIds = output.toLongArray();

        return tokenizer.decode(tokenIds);
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}
