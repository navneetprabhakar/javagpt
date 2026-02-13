#!/bin/bash

MODEL_NAME="${1:-gpt2-medium}"
CACHE_DIR="./models"

echo "Downloading model: $MODEL_NAME"
echo "Cache directory: $CACHE_DIR"

mkdir -p "$CACHE_DIR"

# Use Python script to download via HuggingFace
python3 << EOF
from transformers import AutoTokenizer, AutoModelForCausalLM
import os

model_name = "$MODEL_NAME"
cache_dir = "$CACHE_DIR"

print(f"Downloading tokenizer for {model_name}...")
tokenizer = AutoTokenizer.from_pretrained(model_name, cache_dir=cache_dir)

print(f"Downloading model {model_name}...")
model = AutoModelForCausalLM.from_pretrained(model_name, cache_dir=cache_dir)

print("Download complete!")
print(f"Model saved to: {cache_dir}")
EOF

echo "Model download completed successfully"
