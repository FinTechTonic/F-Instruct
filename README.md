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

### Data Preprocessor

#### `preprocess_data(input_dir, output_dir)`

Processes Markdown files into structured Parquet datasets, including chunked text, document metadata, and an entities dictionary.

1.  **Main Data**: This file contains individual text chunks extracted from the source documents, along with their associated metadata and extracted features.

    | Column Name       | Description                                                                 |
    |-------------------|-----------------------------------------------------------------------------|
    | `chunk_id`        | Unique identifier for the text chunk (primary key).                         |
    | `document_id`     | Identifier of the source document this chunk belongs to.                    |
    | `chunk_index`     | Sequential index of the chunk within its source document.                   |
    | `text`            | The actual text content of the chunk.                                       |
    | `title`           | Title of the source document.                                               |
    | `summary`         | Summary of the source document.                                             |
    | `entities`        | List of (text, label) tuples for named entities found in this chunk.        |
    | `acronyms`        | List of acronyms found in this chunk.                                       |
    | `keywords`        | List of general keywords extracted from this chunk.                         |
    | `source_name`     | Original filename of the source document.                                   |
    | `classifier_code` | Classifier code assigned to the source document.                            |

2.  **Document Manifest**: This file provides a summary and metadata for each source document processed, acting as a catalog.

    | Column Name         | Description                                                                 |
    |---------------------|-----------------------------------------------------------------------------|
    | `document_id`       | Unique identifier for the source document (primary key, e.g., arXiv-style ID). |
    | `original_filename` | Original filename of the source document.                                   |
    | `generated_title`   | The title generated or extracted for the document.                          |
    | `classifier_code`   | A category classification code (e.g., 4-letter library code) assigned to the document. |
    | `classifier_id`     | A composite ID combining `classifier_code` and `document_id` for unique library-style identification. |
    | `summary`           | A generated summary of the document content.                                |

3.  **Named Entities Dictionary**: This file serves as a global vocabulary, detailing unique named entities and acronyms found across all processed documents, along with their contextual information.

    | Column Name             | Description                                                                 |
    |-------------------------|-----------------------------------------------------------------------------|
    | `term_id`               | Unique identifier for the term in this dictionary (primary key).            |
    | `term_text`             | The actual text of the unique named entity or acronym.                      |
    | `term_type`             | Indicates if the term is an 'entity' or 'acronym'.                          |
    | `entity_label`          | The label of the named entity (e.g., 'ORG', 'PERSON'). Null or a generic marker like 'ACRONYM' if `term_type` is 'acronym'. |
    | `associated_keywords`   | A list of keywords aggregated from all chunks where this `term_text` appears. |
    | `source_document_ids`   | A list of unique `document_id`s indicating all source documents where this `term_text` was found. |
    
    > **Note:** General chunk-level keywords are present in the 'Main Data' file. The `associated_keywords` in this dictionary are derived from them, contextualized to each unique term.

**Parameters:**
- `input_dir` (str) - Path to directory containing Markdown files.
- `output_dir` (str) - Output directory for the processed Parquet data file(s).

**Returns:**
- `dict[str, pandas.DataFrame]` - A dictionary where keys are 'main_data', 'manifest', and 'named_entities_dictionary', and values are the corresponding Pandas DataFrames. (Note: The function also saves these DataFrames to Parquet file(s) in `output_dir`).

**Example:**
```python
from f_instruct.data import preprocess_data

# Process Markdown documents from processed to structured
preprocess_data("./data/processed", "./data/structured")
```

**CLI:**
```bash
# Basic usage
uv run f_instruct/data/preprocessor.py -i ./data/processed -o ./data/structured
```

---

*Copyright 2025 Team Tonic*