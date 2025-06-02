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

#### `preprocess_data(input_dir, output_dir, model=None)`

Processes Markdown files into structured Parquet datasets, including chunked text and document metadata.

1.  **data.parquet**: This file contains individual text chunks extracted from the source documents, along with their associated metadata.

    | Column Name       | Description                                                                 |
    |-------------------|-----------------------------------------------------------------------------|
    | `chunk_id`        | Unique identifier for the text chunk (primary key).                         |
    | `document_id`     | Identifier of the source document this chunk belongs to.                    |
    | `chunk_index`     | Sequential index of the chunk within its source document.                   |
    | `text`            | The actual text content of the chunk.                                       |
    | `title`           | Title of the source document.                                               |
    | `source_name`     | Original filename of the source document.                                   |
    | `classifier_code` | Classifier code assigned to the source document.                            |
    | `paragraph_id`    | Identifier of the paragraph this chunk belongs to.                          |
    | `position_start`  | Start character position of this chunk in the original document.            |
    | `position_end`    | End character position of this chunk in the original document.              |
    | `previous_chunk_id` | ID of the previous chunk in sequence (for context navigation).            |
    | `next_chunk_id`   | ID of the next chunk in sequence (for context navigation).                  |
    | `is_first_chunk`  | Boolean flag indicating if this is the first chunk in a document.           |
    | `is_last_chunk`   | Boolean flag indicating if this is the last chunk in a document.            |
    | `relative_position` | Float value (0.0-1.0) indicating relative position within document.       |

    > **Note:** Chunks maintain their original document sequence, allowing for easy forward/backward navigation to retrieve additional context when needed.

**Parameters:**
- `input_dir` (str) - Path to directory containing Markdown files.
- `output_dir` (str) - Output directory for the processed Parquet data file(s).
- `model` (str, optional) - Ollama model to use for advanced features (e.g., 'phi4'). (Currently a placeholder for future implementation).

**Returns:**
- `dict[str, pandas.DataFrame]` - A dictionary containing the processed data DataFrame.

**Example:**
```python
from f_instruct.data import preprocess_data

# Process Markdown documents from processed to structured
preprocess_data("./data/processed", "./data/structured")

# With model specified (for future NLP features)
preprocess_data("./data/processed", "./data/structured", model="phi4")
```

**CLI:**
```bash
# Basic usage
uv run f_instruct/data/preprocessor.py -i ./data/processed -o ./data/structured

# With model specification (for future NLP features)
uv run f_instruct/data/preprocessor.py -i ./data/processed -o ./data/structured -m phi4
```

---

*Copyright 2025 Team Tonic*