# How to Use JavaGPT

## Prerequisites

- **Java 21** installed
- **Maven 3.8+** installed
- **Python 3** with `transformers` package (for model download only)
- **10-15 GB** free disk space
- **MacBook Air M4 with 24GB RAM** (recommended) or equivalent

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/navneetprabhakar/javagpt.git
cd javagpt
```

### 2. Download the GPT-2 Model

Before running the application, download the model weights:

```bash
chmod +x scripts/download-model.sh
./scripts/download-model.sh gpt2-medium
```

**Available model sizes:**

| Model | Parameters | RAM Usage | Disk Space |
|-------|-----------|-----------|------------|
| `gpt2` | 117M | 2-3 GB | ~500 MB |
| `gpt2-medium` | 345M | 4-6 GB | ~1.5 GB |
| `gpt2-large` | 774M | 8-12 GB | ~3 GB |
| `gpt2-xl` | 1.5B | 16-20 GB | ~6 GB |

To download a different model size:

```bash
./scripts/download-model.sh gpt2-large
```

### 3. Build the Project

```bash
mvn clean install -DskipTests
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or with custom JVM settings for larger models:

```bash
java -Xms2g -Xmx10g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar target/javagpt-1.0.0.jar
```

The application starts on **port 8080** by default.

## API Usage

### Generate Text

**POST** `/api/v1/generate`

```bash
curl -X POST http://localhost:8080/api/v1/generate \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "The future of artificial intelligence is",
    "maxTokens": 100,
    "temperature": 0.7
  }'
```

**Request Body:**

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `prompt` | String | Yes | - | Input text to generate from |
| `maxTokens` | Integer | No | 512 | Maximum tokens to generate |
| `temperature` | Double | No | 0.7 | Sampling temperature (0.0-1.0) |
| `topK` | Integer | No | 50 | Top-K sampling parameter |
| `topP` | Double | No | 0.9 | Top-P (nucleus) sampling parameter |

**Response:**

```json
{
  "prompt": "The future of artificial intelligence is",
  "generatedText": "The future of artificial intelligence is bright and full of possibilities...",
  "processingTimeMs": 340,
  "inputTokens": 7,
  "outputTokens": 50
}
```

### Health Check

```bash
curl http://localhost:8080/api/v1/generate/health
```

### Actuator Endpoints

```bash
# Application health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics
```

## Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
javagpt:
  model:
    name: "gpt2-medium"       # Model variant
    cache-dir: "./models"      # Model storage directory
    max-length: 512            # Default max generation length
    temperature: 0.7           # Default temperature
    top-k: 50                  # Default top-k
    top-p: 0.9                 # Default top-p

  performance:
    batch-size: 1              # Inference batch size
    thread-pool-size: 4        # Worker threads
    enable-gpu: false          # GPU acceleration (CPU only for Mac ARM)

server:
  port: 8080                   # Server port
```

## Understanding Generation Parameters

- **temperature**: Controls randomness. Lower values (0.1-0.3) produce more focused/deterministic output. Higher values (0.7-1.0) produce more creative/diverse output.
- **topK**: Limits sampling to the top K most probable tokens at each step.
- **topP**: Nucleus sampling - considers tokens whose cumulative probability exceeds this threshold.
- **maxTokens**: Maximum number of tokens to generate in the response.

## Performance Expectations

| Metric | Value |
|--------|-------|
| Cold Start | 10-15 seconds |
| Prompt Processing | 50-100 ms |
| Token Generation | 20-40 tokens/second |
| Warm Inference | 200-500 ms per request |
| Sequential Throughput | 2-5 requests/second |

## Troubleshooting

### OutOfMemoryError
- Use a smaller model (`gpt2` instead of `gpt2-medium`)
- Increase JVM heap: `java -Xmx12g -jar target/javagpt-1.0.0.jar`

### Slow First Request
- This is expected due to model loading. Subsequent requests will be faster.
- Cached responses (identical prompts) return instantly.

### Native Library Not Found
- Ensure the correct PyTorch native dependency for your platform is in `pom.xml`
- For Mac ARM64: `pytorch-native-cpu` with classifier `osx-aarch64`

### Model Download Fails
- Check your internet connection
- Verify HuggingFace is accessible
- Try the manual download script: `./scripts/download-model.sh`
