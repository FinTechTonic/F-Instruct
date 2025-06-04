#!/usr/bin/env python3
import os
import re
import sys
import time
import uuid
import argparse
import signal
import datetime
import pandas as pd
import tiktoken
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

SHUTDOWN_REQUESTED = False

def signal_handler(sig, frame):
    global SHUTDOWN_REQUESTED
    SHUTDOWN_REQUESTED = True
    print("\nInterrupt received. Finishing current task and exiting...")

signal.signal(signal.SIGINT, signal_handler)

def logger(message):
    print(message)

def read_markdown_file(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        return f.read()

def extract_title_from_filename(file_name: str) -> str:
    return os.path.splitext(file_name)[0]

def chunk_markdown_text(text):
    paragraphs = [p.strip() for p in re.split(r'\n{2,}', text) if p.strip()]
    if not paragraphs:
        return []
    chunks = []
    current_pos = 0
    for i, para_text in enumerate(paragraphs):
        start_pos = text.find(para_text, current_pos)
        if start_pos == -1:
            start_pos = current_pos
        end_pos = start_pos + len(para_text)
        current_pos = end_pos
        para_id = f"p{i}"
        prev_id = f"idx-{i-1}" if i > 0 else None
        next_id = f"idx-{i+1}" if i < len(paragraphs) - 1 else None
        chunks.append({
            "text": para_text,
            "paragraph_id": para_id,
            "position": {
                "start_char": start_pos,
                "end_char": end_pos,
                "start_paragraph": i,
                "end_paragraph": i
            },
            "overlap": {
                "previous_chunk_id": prev_id,
                "next_chunk_id": next_id,
                "overlap_previous_chars": 0,
                "overlap_next_chars": 0
            },
            "sequence_info": {
                "is_first_chunk": i == 0,
                "is_last_chunk": i == len(paragraphs) - 1,
                "total_chunks": len(paragraphs),
                "relative_position": i / max(1, len(paragraphs) - 1) if len(paragraphs) > 1 else 0.5
            }
        })
    return chunks

def generate_document_id():
    current_date = datetime.datetime.now()
    date_prefix = f"{str(current_date.year)[2:]}{current_date.month:02d}"
    random_suffix = str(uuid.uuid4())[:6]
    return f"{date_prefix}.{random_suffix}"

def generate_classifier_code(title):
    return "DOCS"

def count_tokens(text, model_name="gpt-3.5-turbo"):
    encoder = tiktoken.encoding_for_model(model_name)
    tokens = encoder.encode(text)
    return len(tokens)

def process_markdown_file(file_path, global_chunk_id):
    file_name = os.path.basename(file_path)
    markdown_text = read_markdown_file(file_path)
    title = extract_title_from_filename(file_name)
    document_id = generate_document_id()
    classifier_code = generate_classifier_code(title)
    text_chunks = chunk_markdown_text(markdown_text)
    chunks_data = []
    num_chunks_in_doc = len(text_chunks)
    for i, chunk_details in enumerate(text_chunks):
        current_chunk_id = global_chunk_id
        global_chunk_id += 1
        token_count = count_tokens(chunk_details["text"])
        chunks_data.append({
            "chunk_id": current_chunk_id,
            "document_id": document_id,
            "chunk_index": i,
            "text": chunk_details["text"],
            "title": title,
            "source_name": file_name,
            "classifier_code": classifier_code,
            "paragraph_id": chunk_details["paragraph_id"],
            "position_start": chunk_details["position"]["start_char"],
            "position_end": chunk_details["position"]["end_char"],
            "is_first_chunk": chunk_details["sequence_info"]["is_first_chunk"],
            "is_last_chunk": chunk_details["sequence_info"]["is_last_chunk"],
            "relative_position": chunk_details["sequence_info"]["relative_position"],
            "token_count": token_count
        })
    return chunks_data, global_chunk_id

def deduplicate_chunks(df, threshold=0.95):
    texts = df["text"].tolist()
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(texts)
    sim_matrix = cosine_similarity(tfidf_matrix)
    to_drop = set()
    n = df.shape[0]
    duplicate_count = 0
    for i in range(n):
        if i in to_drop:
            continue
        for j in range(i + 1, n):
            if j in to_drop:
                continue
            similarity = sim_matrix[i, j]
            if similarity >= threshold:
                logger(f"Duplicate found: Chunk {df.iloc[i]['chunk_id']} and Chunk {df.iloc[j]['chunk_id']} have similarity {similarity:.2f}")
                to_drop.add(j)
                duplicate_count += 1
    logger(f"Total duplicates removed: {duplicate_count}")
    new_df = df.drop(df.index[list(to_drop)]).reset_index(drop=True)
    new_df = reindex_chunks(new_df)
    return new_df

def reindex_chunks(df):
    df = df.sort_values(by=["document_id", "chunk_index"]).reset_index(drop=True)
    n = len(df)
    new_ids = list(range(1, n + 1))
    df["chunk_id"] = new_ids
    df["chunk_index"] = new_ids
    df["position_start"] = new_ids
    df["position_end"] = new_ids
    df["is_first_chunk"] = df.index == 0
    df["is_last_chunk"] = df.index == (n - 1)
    if n > 1:
        df["relative_position"] = df.index / (n - 1)
    else:
        df["relative_position"] = 0.5
    return df

def preprocess_data(input_dir, output_dir, model=None, dedup_threshold=None):
    start_time = time.time()
    logger("Starting preprocessing of Markdown files")
    logger(f"Input directory: {input_dir}")
    logger(f"Output directory: {output_dir}")
    if model:
        logger(f"Model specified: {model}")
    os.makedirs(output_dir, exist_ok=True)
    markdown_files = [os.path.join(input_dir, f) for f in os.listdir(input_dir)
                      if f.lower().endswith(('.md', '.markdown'))]
    if not markdown_files:
        logger(f"Error: No Markdown files found in {input_dir}")
        return {"data": pd.DataFrame()}
    logger(f"Found {len(markdown_files)} Markdown files to process")
    all_chunks = []
    global_chunk_id = 1
    for i, md_file in enumerate(markdown_files):
        if SHUTDOWN_REQUESTED:
            break
        file_name = os.path.basename(md_file)
        logger(f"Processing {i+1}/{len(markdown_files)}: {file_name}")
        try:
            chunks_data, global_chunk_id = process_markdown_file(md_file, global_chunk_id)
            all_chunks.extend(chunks_data)
        except Exception as e:
            logger(f"Error processing {file_name}: {str(e)}")
    data_df = pd.DataFrame(all_chunks)
    if not data_df.empty:
        if dedup_threshold is not None:
            data_df = deduplicate_chunks(data_df, threshold=dedup_threshold)
        else:
            data_df = reindex_chunks(data_df)
    if data_df.empty:
        logger("No data to save. Output Parquet/JSONL file will be empty or not created.")
    else:
        logger("Saving data to Parquet file...")
        data_file = os.path.join(output_dir, "data.parquet")
        data_df.to_parquet(data_file, index=False)
        logger("Saving data to JSONL file...")
        jsonl_file = os.path.splitext(data_file)[0] + ".jsonl"
        data_df.to_json(jsonl_file, orient="records", lines=True)
        logger(f"Data saved to {data_file} and {jsonl_file}")
    elapsed_time = time.time() - start_time
    total_chunks = len(data_df)
    summary_message = (
        f"✓ Successfully processed {len(markdown_files)} documents "
        f"into {total_chunks} chunks.\n"
        f"Total processing time: {elapsed_time:.2f} seconds."
    )
    logger(summary_message)
    return {"data": data_df}

def main():
    parser = argparse.ArgumentParser(description="Process Markdown to structured Parquet/JSONL datasets.")
    parser.add_argument("-i", "--input", required=True, help="Input directory containing Markdown files.")
    parser.add_argument("-o", "--output", required=True, help="Output directory for processed data.")
    parser.add_argument("-m", "--model", help="Ollama model to use for advanced features (e.g., 'phi4'). (Placeholder)")
    parser.add_argument("-d", "--dedup_threshold", type=float, default=None, help="Deduplication similarity threshold (e.g., 0.95)")
    args = parser.parse_args()
    logger("F-Instruct Document Preprocessor")
    logger("Converting Markdown to structured Parquet/JSONL datasets")
    try:
        preprocess_data(args.input, args.output, args.model, args.dedup_threshold)
        sys.exit(0)
    except KeyboardInterrupt:
        logger("Process interrupted by user")
        sys.exit(1)
    except Exception as e:
        logger(f"Error: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()