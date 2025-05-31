# F-Instruct

A structured financial agent system that produces compliant structured financial products and can integrate with open banking systems.

## API

### Converter

#### `ingest_data(input_path, output_dir)`

Converts PDF documents into structured LaTeX format for downstream processing workflows.

**Parameters:**
- `input_path` (str) - Path to PDF file or directory containing PDF files
- `output_dir` (str) - Output directory for processed documents

**Returns:**
- `bool` - True if ingestion successful, False otherwise

**Example:**
```python
from f_instruct.data import ingest_data

# Process documents from ingress to processed
ingest_data("./data/ingress", "./data/processed")

# Process single document
ingest_data("./data/ingress/report.pdf", "./data/processed")
```

**CLI:**
```bash
uv run f_instruct/data/converter.py -i ./data/ingress -o ./data/processed
```

---

*Copyright 2025 Team Tonic*