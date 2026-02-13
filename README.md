# JavaGPT

A lightweight GPT-style language model implementation using **Java 21**, **Spring Boot 3.2**, and **Deep Java Library (DJL)** for local execution.

JavaGPT wraps pre-trained GPT-2 models behind a REST API, enabling text generation without relying on external cloud APIs. Designed for local development and experimentation on Apple Silicon Macs.

## Features

- GPT-2 text generation via REST API
- Configurable generation parameters (temperature, top-k, top-p, max tokens)
- Caffeine-based response caching
- Spring Boot Actuator for health checks and metrics
- Optimized for Apple Silicon (M-series) with PyTorch ARM64 native binaries

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| ML Engine | Deep Java Library (DJL) 0.27.0 |
| Runtime | PyTorch 2.1.1 (CPU, ARM64) |
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
│   └── download-model.sh              # Model download helper
├── src/
│   ├── main/
│   │   ├── java/com/navneet/javagpt/
│   │   │   ├── JavaGPTApplication.java          # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── ModelConfig.java             # Model configuration & bean setup
│   │   │   │   └── CacheConfig.java             # Caffeine cache configuration
│   │   │   ├── service/
│   │   │   │   ├── JavaGPTService.java          # Core text generation service
│   │   │   │   └── TextGenerationTranslator.java # DJL translator (tokenize/decode)
│   │   │   ├── controller/
│   │   │   │   └── JavaGPTController.java       # REST API endpoints
│   │   │   └── dto/
│   │   │       ├── GenerationRequest.java       # Request DTO
│   │   │       └── GenerationResponse.java      # Response DTO
│   │   └── resources/
│   │       └── application.yml                  # Application configuration
│   └── test/
│       └── java/com/navneet/javagpt/
│           └── JavaGPTIntegrationTest.java      # Integration tests
└── models/                                       # Downloaded model weights (gitignored)
```

## Architecture

```
Client Request
      │
      ▼
┌─────────────────┐
│  JavaGPTController │  ← REST API layer
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  JavaGPTService    │  ← Business logic + caching
└────────┬────────┘
         │
         ▼
┌──────────────────────────┐
│  TextGenerationTranslator  │  ← Tokenization & decoding
└────────┬─────────────────┘
         │
         ▼
┌─────────────────┐
│  DJL / PyTorch     │  ← Model inference engine
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

| Model | Parameters | RAM Required |
|-------|-----------|-------------|
| GPT-2 Small | 117M | 2-3 GB |
| GPT-2 Medium | 345M | 4-6 GB |
| GPT-2 Large | 774M | 8-12 GB |
| GPT-2 XL | 1.5B | 16-20 GB |

## System Requirements

- Java 21
- Maven 3.8+
- 10-15 GB free disk space
- 8+ GB RAM (24 GB recommended for GPT-2 Medium/Large)

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/generate` | Generate text from a prompt |
| GET | `/api/v1/generate/health` | Service health check |
| GET | `/actuator/health` | Spring Actuator health |
| GET | `/actuator/metrics` | Application metrics |
