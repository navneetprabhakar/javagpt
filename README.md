# JavaGPT

A local LLM inference engine for Java — run **Llama 2, Mistral 7B, TinyLlama** and other quantized models (GGUF) locally through a REST API with **SSE streaming**. Built with **Java 21**, **Spring Boot 3.2**, **Jlama**, and **java-llama.cpp**.

Also includes a lightweight GPT-2 module powered by Deep Java Library (DJL) for smaller-scale experimentation.

## Features

- **Run 7B+ parameter models locally** with 4-bit/8-bit quantized GGUF files
- **Two inference engines** — Jlama (pure Java) and java-llama.cpp (JNI), switchable via config
- **SSE streaming** for real-time token-by-token generation
- Supports Llama 2, Mistral, TinyLlama, and any GGUF-compatible model
- REST API with configurable generation parameters (temperature, top-k, top-p, max tokens)
- GPT-2 text generation module (DJL + PyTorch) for lightweight use cases
- Caffeine-based response caching
- Spring Boot Actuator for health checks and metrics
- Optimized for Apple Silicon (M-series)

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| LLM Engine (Pure Java) | Jlama 0.8.4 |
| LLM Engine (Native) | java-llama.cpp 4.1.0 |
| Streaming | Spring WebFlux (SSE) |
| GPT-2 Engine | Deep Java Library (DJL) 0.27.0 + PyTorch 2.1.1 |
| Tokenizer | HuggingFace Tokenizers |
| Caching | Caffeine |
| Build | Maven |

## Quick Start

```bash
# 1. Clone
git clone https://github.com/navneetprabhakar/javagpt.git
cd javagpt

# 2. Download a quantized model
chmod +x scripts/download-gguf-model.sh
./scripts/download-gguf-model.sh tinyllama       # 1.1B params, ~670 MB

# 3. Enable LLM engine in application.yml
#    Set javagpt.llm.enabled: true

# 4. Build
mvn clean install -DskipTests

# 5. Run
mvn spring-boot:run

# 6. Generate text
curl -X POST http://localhost:8080/api/v1/llm/generate \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Explain quantum computing", "maxTokens": 100, "temperature": 0.7}'

# 7. Stream tokens in real-time
curl -N -X POST http://localhost:8080/api/v1/llm/generate/stream \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Write a poem about Java", "maxTokens": 100}'
```

See [HOW-TO-USE.md](HOW-TO-USE.md) for detailed usage instructions, configuration options, and troubleshooting.

## Supported Models

### Quantized GGUF Models (Jlama / llama.cpp)

| Model | Parameters | Quantization | Disk Size | RAM Required |
|-------|-----------|-------------|-----------|-------------|
| TinyLlama 1.1B | 1.1B | Q4_K_M | ~670 MB | ~2 GB |
| Llama 2 7B Chat | 7B | Q4_K_M | ~3.8 GB | ~6 GB |
| Mistral 7B Instruct | 7B | Q4_K_M | ~4.1 GB | ~6 GB |
| Llama 2 13B Chat | 13B | Q4_K_M | ~7.3 GB | ~10 GB |

### GPT-2 (DJL Engine)

| Model | Parameters | RAM Required |
|-------|-----------|-------------|
| GPT-2 Small | 117M | 2-3 GB |
| GPT-2 Medium | 345M | 4-6 GB |
| GPT-2 Large | 774M | 8-12 GB |
| GPT-2 XL | 1.5B | 16-20 GB |

## Architecture

The project has two inference paths:

```
                    Client Request
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
      /api/v1/llm/*           /api/v1/generate
              │                       │
    ┌─────────┴─────────┐   ┌────────┴────────┐
    │   LlmController    │   │ JavaGPTController │
    └─────────┬─────────┘   └────────┬────────┘
              │                       │
    ┌─────────┴─────────┐   ┌────────┴────────┐
    │    LlmService      │   │  JavaGPTService   │
    │  (+ SSE stream)    │   │  (+ Caffeine cache)│
    └─────────┬─────────┘   └────────┬────────┘
              │                       │
    ┌─────────┴─────────┐   ┌────────┴────────┐
    │    LlmEngine       │   │  DJL / PyTorch   │
    │  ┌────┴─────┐     │   │  (GPT-2 models)  │
    │  │          │     │   └─────────────────┘
    │ Jlama  llama.cpp  │
    │(pure Java) (JNI)  │
    └───────────────────┘
```

## Project Structure

```
javagpt/
├── pom.xml
├── README.md
├── HOW-TO-USE.md
├── .gitignore
├── scripts/
│   ├── download-gguf-model.sh         # GGUF quantized model download
│   └── download-model.sh              # GPT-2 model download (HuggingFace)
├── src/
│   ├── main/
│   │   ├── java/com/navneet/javagpt/
│   │   │   ├── JavaGPTApplication.java          # Spring Boot entry point
│   │   │   ├── llm/                             # ** Quantized LLM inference **
│   │   │   │   ├── config/
│   │   │   │   │   └── LlmEngineConfig.java     # Engine selection & config
│   │   │   │   ├── engine/
│   │   │   │   │   ├── LlmEngine.java           # Common engine interface
│   │   │   │   │   ├── JlamaEngine.java          # Jlama (pure Java) engine
│   │   │   │   │   └── LlamaCppEngine.java       # llama.cpp (JNI) engine
│   │   │   │   ├── service/
│   │   │   │   │   └── LlmService.java          # LLM orchestration + streaming
│   │   │   │   ├── controller/
│   │   │   │   │   └── LlmController.java       # REST + SSE endpoints
│   │   │   │   └── dto/
│   │   │   │       ├── LlmGenerationRequest.java
│   │   │   │       └── LlmGenerationResponse.java
│   │   │   ├── config/                          # GPT-2 configuration
│   │   │   │   ├── ModelConfig.java
│   │   │   │   └── CacheConfig.java
│   │   │   ├── service/                         # GPT-2 service
│   │   │   │   ├── JavaGPTService.java
│   │   │   │   └── TextGenerationTranslator.java
│   │   │   ├── controller/                      # GPT-2 controller
│   │   │   │   └── JavaGPTController.java
│   │   │   └── dto/                             # GPT-2 DTOs
│   │   │       ├── GenerationRequest.java
│   │   │       └── GenerationResponse.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/navneet/javagpt/
│           └── JavaGPTIntegrationTest.java
└── models/                                       # Downloaded model weights (gitignored)
```

## API Endpoints

### Quantized LLM (Jlama / llama.cpp)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/llm/generate` | Generate text (JSON response) |
| POST | `/api/v1/llm/generate/stream` | Generate text (SSE streaming) |
| GET | `/api/v1/llm/health` | Engine status and model info |

### GPT-2 (DJL)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/generate` | Generate text from a prompt |
| GET | `/api/v1/generate/health` | Service health check |

### Common

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Spring Actuator health |
| GET | `/actuator/metrics` | Application metrics |

## System Requirements

- Java 21
- Maven 3.8+
- 10-15 GB free disk space
- 8+ GB RAM (24 GB recommended for 7B models)
