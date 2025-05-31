# F-Instruct

A structured financial agent system that produces compliant structured financial products and can integrate with open banking systems.

## API

### Data Converter

#### `ingest_data(input_path, output_dir, model=None)`

Converts PDF documents with optional LLM into structured Markdown format for downstream processing workflows.

**Parameters:**
- `input_path` (str) - Path to PDF file or directory containing PDF files
- `output_dir` (str) - Output directory for processed documents
- `model` (str, optional) - Ollama model to use for enhanced extraction (default model: 'phi4:latest')

**Returns:**
- `bool` - True if ingestion successful, False otherwise

**Example:**
```python
from f_instruct.data import ingest_data

# Process documents from ingress to processed
ingest_data("./data/ingress", "./data/processed")

# Process single document with Ollama LLM enhancement
ingest_data("./data/ingress/report.pdf", "./data/processed", model="phi4")
```

**CLI:**
```bash
# Basic usage
uv run f_instruct/data/converter.py -i ./data/ingress -o ./data/processed

# With LLM enhancement using the phi4 model
uv run f_instruct/data/converter.py -i ./data/ingress -o ./data/processed -m phi4
```

---

*Copyright 2025 Team Tonic*