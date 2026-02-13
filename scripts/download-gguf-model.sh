#!/bin/bash

# Download quantized GGUF models from HuggingFace
# Usage: ./download-gguf-model.sh [model-name]
#
# Available models:
#   tinyllama    - TinyLlama 1.1B Q4 (~670 MB) - great for testing
#   mistral-7b   - Mistral 7B Instruct Q4 (~4.1 GB)
#   llama2-7b    - Llama 2 7B Chat Q4 (~3.8 GB)
#   llama2-13b   - Llama 2 13B Chat Q4 (~7.3 GB)

MODEL_NAME="${1:-tinyllama}"
CACHE_DIR="./models"

mkdir -p "$CACHE_DIR"

case "$MODEL_NAME" in
    tinyllama)
        URL="https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        FILENAME="tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        echo "Downloading TinyLlama 1.1B Q4 (~670 MB)..."
        ;;
    mistral-7b)
        URL="https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.2-GGUF/resolve/main/mistral-7b-instruct-v0.2.Q4_K_M.gguf"
        FILENAME="mistral-7b-instruct-v0.2.Q4_K_M.gguf"
        echo "Downloading Mistral 7B Instruct Q4 (~4.1 GB)..."
        ;;
    llama2-7b)
        URL="https://huggingface.co/TheBloke/Llama-2-7B-Chat-GGUF/resolve/main/llama-2-7b-chat.Q4_K_M.gguf"
        FILENAME="llama-2-7b-chat.Q4_K_M.gguf"
        echo "Downloading Llama 2 7B Chat Q4 (~3.8 GB)..."
        ;;
    llama2-13b)
        URL="https://huggingface.co/TheBloke/Llama-2-13B-chat-GGUF/resolve/main/llama-2-13b-chat.Q4_K_M.gguf"
        FILENAME="llama-2-13b-chat.Q4_K_M.gguf"
        echo "Downloading Llama 2 13B Chat Q4 (~7.3 GB)..."
        ;;
    *)
        echo "Unknown model: $MODEL_NAME"
        echo "Available: tinyllama, mistral-7b, llama2-7b, llama2-13b"
        exit 1
        ;;
esac

OUTPUT="$CACHE_DIR/$FILENAME"

if [ -f "$OUTPUT" ]; then
    echo "Model already exists at: $OUTPUT"
    exit 0
fi

echo "Downloading to: $OUTPUT"
curl -L --progress-bar -o "$OUTPUT" "$URL"

if [ $? -eq 0 ]; then
    echo "Download complete: $OUTPUT"
    echo "File size: $(du -h "$OUTPUT" | cut -f1)"
else
    echo "Download failed!"
    rm -f "$OUTPUT"
    exit 1
fi
