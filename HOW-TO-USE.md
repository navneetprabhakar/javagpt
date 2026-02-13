# How to Use JavaGPT

## Prerequisites

- **Java 21** installed
- **Maven 3.8+** installed
- **10-15 GB** free disk space
- **MacBook Air M4 with 24GB RAM** (recommended) or equivalent

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/navneetprabhakar/javagpt.git
cd javagpt
```

### 2. Download a GGUF Model

```bash
chmod +x scripts/download-gguf-model.sh

# TinyLlama 1.1B - great for testing (~670 MB)
./scripts/download-gguf-model.sh tinyllama

# Mistral 7B Instruct (~4.1 GB)
./scripts/download-gguf-model.sh mistral-7b

# Llama 2 7B Chat (~3.8 GB)
./scripts/download-gguf-model.sh llama2-7b

# Llama 2 13B Chat (~7.3 GB)
./scripts/download-gguf-model.sh llama2-13b
```

### 3. Enable the LLM Engine

Edit `src/main/resources/application.yml`:

```yaml
javagpt:
  llm:
    enabled: true
    engine: jlama              # Options: jlama, llamacpp
    model-path: "./models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
    max-tokens: 256
    temperature: 0.7
    top-k: 40
    top-p: 0.9
```

### 4. Build the Project

```bash
mvn clean install -DskipTests
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

Or with custom JVM settings for larger models:

```bash
java --enable-preview --add-modules jdk.incubator.vector \
     -Xms2g -Xmx16g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar target/javagpt-1.0.0.jar
```

The application starts on **port 8080** by default.

## API Usage

### Generate Text (JSON Response)

**POST** `/api/v1/llm/generate`

```bash
curl -X POST http://localhost:8080/api/v1/llm/generate \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Explain quantum computing in simple terms",
    "maxTokens": 200,
    "temperature": 0.7
  }'
```

**Request Body:**

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `prompt` | String | Yes | - | Input text to generate from |
| `maxTokens` | Integer | No | 256 | Maximum tokens to generate |
| `temperature` | Float | No | 0.7 | Sampling temperature (0.0-1.0) |
| `topK` | Integer | No | 40 | Top-K sampling parameter |
| `topP` | Float | No | 0.9 | Top-P (nucleus) sampling parameter |

**Response:**

```json
{
  "prompt": "Explain quantum computing in simple terms",
  "generatedText": "Quantum computing uses quantum bits or qubits...",
  "processingTimeMs": 1250,
  "modelName": "./models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
  "engine": "jlama"
}
```

### Stream Text (Server-Sent Events)

**POST** `/api/v1/llm/generate/stream`

```bash
curl -N -X POST http://localhost:8080/api/v1/llm/generate/stream \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Write a short poem about Java",
    "maxTokens": 100,
    "temperature": 0.8
  }'
```

Tokens are streamed in real-time as SSE events — ideal for chat-style UIs.

### Health Check

```bash
curl http://localhost:8080/api/v1/llm/health
```

**Response:**

```json
{
  "status": "ready",
  "engine": "jlama",
  "modelPath": "./models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
}
```

### Actuator Endpoints

```bash
# Application health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics
```

## Choosing an Engine

JavaGPT supports two inference engines for quantized models. Switch between them via `javagpt.llm.engine` in `application.yml`:

| Feature | Jlama | llama.cpp |
|---------|-------|-----------|
| Implementation | Pure Java | JNI (C++ backend) |
| Quantization | Q4, Q8 | Q2-Q8 |
| Dependencies | None (Java only) | Native libraries |
| Memory management | JVM GC | Manual (AutoCloseable) |
| Setup | Simple | Medium |
| Best for | Simple integration | Maximum performance |

```yaml
# Use Jlama (default)
javagpt:
  llm:
    engine: jlama

# Or switch to llama.cpp
javagpt:
  llm:
    engine: llamacpp
```

## Understanding Generation Parameters

- **temperature**: Controls randomness. Lower values (0.1-0.3) produce more focused/deterministic output. Higher values (0.7-1.0) produce more creative/diverse output.
- **topK**: Limits sampling to the top K most probable tokens at each step.
- **topP**: Nucleus sampling — considers tokens whose cumulative probability exceeds this threshold.
- **maxTokens**: Maximum number of tokens to generate in the response.

## Configuration Reference

```yaml
javagpt:
  llm:
    enabled: true              # Enable/disable LLM engine
    engine: jlama              # jlama or llamacpp
    model-path: "./models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
    max-tokens: 256            # Default max generation length
    temperature: 0.7           # Default temperature
    top-k: 40                  # Default top-k
    top-p: 0.9                 # Default top-p

server:
  port: 8080                   # Server port
```

## Troubleshooting

### OutOfMemoryError
- Use a smaller model (e.g., `tinyllama` instead of `mistral-7b`)
- Increase JVM heap: `java -Xmx16g --enable-preview --add-modules jdk.incubator.vector -jar target/javagpt-1.0.0.jar`

### Slow First Request
- This is expected due to model loading. Subsequent requests will be faster.

### LLM Engine Not Starting
- Ensure `javagpt.llm.enabled` is set to `true` in `application.yml`
- Verify the GGUF model file exists at the configured `model-path`
- Check logs for engine initialization errors

### Native Library Not Found (llama.cpp)
- Ensure `de.kherud:llama` dependency is present in `pom.xml`
- Verify your platform is supported (macOS ARM64, Linux x86-64)

### Jlama Vector API Warnings
- These are expected with `--enable-preview` on Java 21
- The Vector API is incubating and will be stable in future Java releases

### Model Download Fails
- Check your internet connection
- Verify HuggingFace is accessible
- Try: `./scripts/download-gguf-model.sh tinyllama`
