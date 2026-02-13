# JavaGPT

A lightweight GPT-style language model implementation using **Java 21**, **Spring Boot 3.2**, and **Deep Java Library (DJL)** for local execution.

JavaGPT wraps pre-trained GPT-2 models behind a REST API, enabling text generation without relying on external cloud APIs. Designed for local development and experimentation on Apple Silicon Macs.

## Features

- GPT-2 text generation via REST API (DJL + PyTorch)
- **Quantized large model support** (7B+ params) via Jlama and java-llama.cpp engines
- GGUF model format support for 4-bit/8-bit quantized models
- SSE streaming for token-by-token generation
- Switchable inference engines (DJL, Jlama, llama.cpp)
- Configurable generation parameters (temperature, top-k, top-p, max tokens)
- Caffeine-based response caching
- Spring Boot Actuator for health checks and metrics
- Optimized for Apple Silicon (M-series)

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| GPT-2 Engine | Deep Java Library (DJL) 0.27.0 + PyTorch 2.1.1 |
| LLM Engine (Pure Java) | Jlama 0.8.4 |
| LLM Engine (Native) | java-llama.cpp 4.1.0 |
| Streaming | Spring WebFlux (SSE) |
| Tokenizer | HuggingFace Tokenizers |
| Caching | Caffeine |
| Build | Maven |

## Project Structure

```
javagpt/
├── pom.xml
├── README.md
├── HOW-TO-USE.md
├── .gitignore
├── scripts/
│   ├── download-model.sh              # GPT-2 model download (HuggingFace)
│   └── download-gguf-model.sh         # GGUF quantized model download
├── src/
│   ├── main/
│   │   ├── java/com/navneet/javagpt/
│   │   │   ├── JavaGPTApplication.java          # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── ModelConfig.java             # GPT-2 model configuration
│   │   │   │   └── CacheConfig.java             # Caffeine cache configuration
│   │   │   ├── service/
│   │   │   │   ├── JavaGPTService.java          # GPT-2 text generation service
│   │   │   │   └── TextGenerationTranslator.java # DJL translator (tokenize/decode)
│   │   │   ├── controller/
│   │   │   │   └── JavaGPTController.java       # GPT-2 REST API endpoints
│   │   │   ├── dto/
│   │   │   │   ├── GenerationRequest.java       # GPT-2 request DTO
│   │   │   │   └── GenerationResponse.java      # GPT-2 response DTO
│   │   │   └── llm/                             # ** Quantized LLM support **
│   │   │       ├── config/
│   │   │       │   └── LlmEngineConfig.java     # Engine selection & config
│   │   │       ├── engine/
│   │   │       │   ├── LlmEngine.java           # Common engine interface
│   │   │       │   ├── JlamaEngine.java          # Jlama (pure Java) engine
│   │   │       │   └── LlamaCppEngine.java       # llama.cpp (JNI) engine
│   │   │       ├── service/
│   │   │       │   └── LlmService.java          # LLM orchestration service
│   │   │       ├── controller/
│   │   │       │   └── LlmController.java       # LLM REST + SSE endpoints
│   │   │       └── dto/
│   │   │           ├── LlmGenerationRequest.java
│   │   │           └── LlmGenerationResponse.java
│   │   └── resources/
│   │       └── application.yml                  # Application configuration
│   └── test/
│       └── java/com/navneet/javagpt/
│           └── JavaGPTIntegrationTest.java      # Integration tests
└── models/                                       # Downloaded model weights (gitignored)
```

## Architecture

The project has two inference paths:

```
                    Client Request
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
    /api/v1/generate          /api/v1/llm/*
              │                       │
    ┌─────────┴─────────┐   ┌────────┴────────┐
    │ JavaGPTController  │   │  LlmController   │
    └─────────┬─────────┘   └────────┬────────┘
              │                       │
    ┌─────────┴─────────┐   ┌────────┴────────┐
    │  JavaGPTService    │   │   LlmService     │
    │  (+ Caffeine cache)│   │  (+ SSE stream)  │
    └─────────┬─────────┘   └────────┬────────┘
              │                       │
    ┌─────────┴─────────┐   ┌────────┴────────┐
    │ DJL / PyTorch      │   │   LlmEngine      │
    │ (GPT-2 models)     │   │  ┌────┴─────┐   │
    └────────────────────┘   │  │          │   │
                             │ Jlama  llama.cpp│
                             │(pure Java)(JNI) │
                             └─────────────────┘
```

## Quick Start

```bash
# 1. Clone
git clone https://github.com/navneetprabhakar/javagpt.git
cd javagpt

# 2. Download model
chmod +x scripts/download-model.sh
./scripts/download-model.sh gpt2-medium

# 3. Build
mvn clean install -DskipTests

# 4. Run
mvn spring-boot:run

# 5. Test
curl -X POST http://localhost:8080/api/v1/generate \
  -H "Content-Type: application/json" \
  -d '{"prompt": "The future of AI is", "maxTokens": 100, "temperature": 0.7}'
```

See [HOW-TO-USE.md](HOW-TO-USE.md) for detailed usage instructions, configuration options, and troubleshooting.

## Supported Models

### GPT-2 (DJL Engine)

| Model | Parameters | RAM Required |
|-------|-----------|-------------|
| GPT-2 Small | 117M | 2-3 GB |
| GPT-2 Medium | 345M | 4-6 GB |
| GPT-2 Large | 774M | 8-12 GB |
| GPT-2 XL | 1.5B | 16-20 GB |

### Quantized GGUF Models (Jlama / llama.cpp)

| Model | Parameters | Quantization | Disk Size | RAM Required |
|-------|-----------|-------------|-----------|-------------|
| TinyLlama 1.1B | 1.1B | Q4_K_M | ~670 MB | ~2 GB |
| Llama 2 7B Chat | 7B | Q4_K_M | ~3.8 GB | ~6 GB |
| Mistral 7B Instruct | 7B | Q4_K_M | ~4.1 GB | ~6 GB |
| Llama 2 13B Chat | 13B | Q4_K_M | ~7.3 GB | ~10 GB |

## System Requirements

- Java 21
- Maven 3.8+
- 10-15 GB free disk space
- 8+ GB RAM (24 GB recommended for GPT-2 Medium/Large)

## API Endpoints

### GPT-2 (DJL)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/generate` | Generate text from a prompt |
| GET | `/api/v1/generate/health` | Service health check |

### Quantized LLM (Jlama / llama.cpp)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/llm/generate` | Generate text (JSON response) |
| POST | `/api/v1/llm/generate/stream` | Generate text (SSE streaming) |
| GET | `/api/v1/llm/health` | Engine status and model info |

### Common

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Spring Actuator health |
| GET | `/actuator/metrics` | Application metrics |
