# F-Instruct

A structured financial agent system that produces compliant structured financial products and can integrate with open banking systems.

## API

### Data Converter

#### `ingest_data(input_path, output_dir, model=None)`

Converts PDF documents with optional LLM into structured Markdown format for downstream processing workflows.

**Parameters:**
- `input_path` (str) - Path to PDF file or directory containing PDF files
- `output_dir` (str) - Output directory for processed documents
- `model` (str, optional) - Ollama model to use for enhanced extraction (e.g., 'phi4'). If this argument is not provided, LLM enhancement is disabled.

**Returns:**
- `bool` - True if ingestion successful, False otherwise

**Example:**
```python
from f_instruct.data import ingest_data

# Process documents from ingress to processed (LLM enhancement disabled)
ingest_data("./data/ingress", "./data/processed")

# Process single document with Ollama LLM enhancement using the 'phi4' model
ingest_data("./data/ingress/report.pdf", "./data/processed", model="phi4")
```

**CLI:**
```bash
# Basic usage (LLM enhancement disabled)
uv run f_instruct/data/converter.py -i ./data/ingress -o ./data/processed

# With LLM enhancement using the 'phi4' model
uv run f_instruct/data/converter.py -i ./data/ingress -o ./data/processed -m phi4
```

### Data Preprocessor

#### `preprocess_data(input_dir, output_dir, model)`

Processes Markdown files into structured Parquet datasets, using spaCy for advanced paragraph detection and an Ollama LLM for text enhancement and named entity formatting.

1.  **data.parquet**: This file contains individual text chunks extracted from the source documents, along with their associated metadata.

    | Column Name       | Type    | Description                                                                 |
    |-------------------|---------|-----------------------------------------------------------------------------|
    | `chunk_id`        | int     | Unique integer identifier for the text chunk (primary key).                 |
    | `document_id`     | str     | Identifier of the source document this chunk belongs to.                    |
    | `chunk_index`     | int     | Sequential index of the chunk within its source document.                   |
    | `text`            | str     | The enhanced text content of the chunk with marked-up entities.             |
    | `title`           | str     | Title of the source document.                                               |
    | `source_name`     | str     | Original filename of the source document.                                   |
    | `classifier_code` | str     | Classifier code assigned to the source document.                            |
    | `paragraph_id`    | str     | Identifier of the paragraph this chunk belongs to.                          |
    | `position_start`  | int     | Start character position of this chunk in the original document.            |
    | `position_end`    | int     | End character position of this chunk in the original document.              |
    | `previous_chunk_id` | int   | ID of the previous chunk in sequence (-1 if none).                        |
    | `next_chunk_id`   | int     | ID of the next chunk in sequence (-1 if none).                             |
    | `is_first_chunk`  | bool    | Boolean flag indicating if this is the first chunk in a document.           |
    | `is_last_chunk`   | bool    | Boolean flag indicating if this is the last chunk in a document.            |
    | `relative_position` | float | Float value (0.0-1.0) indicating relative position within document.       |

    > **Note:** The preprocessor now uses spaCy for improved paragraph detection and boundary analysis, and leverages an Ollama LLM to enhance readability and mark up named entities with bold formatting.

**Parameters:**
- `input_dir` (str) - Path to directory containing Markdown files.
- `output_dir` (str) - Output directory for the processed Parquet data file(s).
- `model` (str) - Ollama model to use for text enhancement and named entity recognition (e.g., 'phi4').

**Returns:**
- `dict[str, pandas.DataFrame]` - A dictionary containing the processed data DataFrame.

**Example:**
```python
from f_instruct.data import preprocess_data

# Process Markdown documents with enhanced NLP features
preprocess_data("./data/processed", "./data/structured", model="phi4")
```

**CLI:**
```bash
# Preprocessor requires specifying a model
uv run f_instruct/data/preprocessor.py -i ./data/processed -o ./data/structured -m phi4
```

### Financial Data Processor

#### `process_financial_data(input_dir, output_dir, model="phi4")`

Generates diverse training examples from financial regulatory data, creating question-answer pairs, instruction examples, financial product definitions, API interactions, and regulatory compliance scenarios.

The processor uses a Language Model to generate high-quality training data in multiple formats and saves it as JSON, JSONL, and Parquet files.

**Parameters:**
- `input_dir` (str) - Directory containing preprocessed parquet files
- `output_dir` (str) - Output directory for generated training examples
- `model` (str, optional) - Ollama model to use for generation (default: 'phi4')

**Returns:**
- `str` - Path to the generated output file

**Example:**
```python
from f_instruct.data import process_financial_data

# Generate training data from preprocessed financial documents
output_path = process_financial_data(
    "./data/structured", 
    "./data/training", 
    model="phi4"
)
```

**CLI:**
```bash
# Generate training data using default phi4 model
uv run f_instruct/data/processor.py -i ./data/structured -o ./data/training

# Use a different model for generation
uv run f_instruct/data/processor.py -i ./data/structured -o ./data/training -m llama3
```

---

*Copyright 2025 Team Tonic*