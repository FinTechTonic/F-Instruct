#!/usr/bin/env python3
import os
import re
import sys
import time
import uuid
import argparse
import signal
import datetime
import logging
import pandas as pd
import tiktoken
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

logger = logging.getLogger(__name__)
logging.basicConfig(format='[%(levelname)s] %(message)s', level=logging.INFO)
SHUTDOWN_REQUESTED = False

def signal_handler(sig, frame):
    global SHUTDOWN_REQUESTED
    SHUTDOWN_REQUESTED = True
    logger.info("Interrupt received. Finishing current task and exiting...")

signal.signal(signal.SIGINT, signal_handler)

def read_markdown_file(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        return f.read()

def extract_title_from_filename(file_name: str) -> str:
    return os.path.splitext(file_name)[0]

def split_text_by_token_limit(text, limit, count_tokens_fn):
    if count_tokens_fn(text) <= limit:
        return [text]
    mid = len(text) // 2
    window = 50
    left_bound = max(0, mid - window)
    right_bound = min(len(text) - 1, mid + window)
    candidates = [i for i in range(left_bound, right_bound + 1) if text[i] in {'.', ' '}]
    if candidates:
        best_index = min(candidates, key=lambda i: abs(i - mid))
        best_char = text[best_index]
    else:
        best_index = mid
        best_char = None
    if best_index <= 0 or best_index >= len(text) - 1:
        best_index = mid
        best_char = None
    if best_char == '.':
        left_part = text[:best_index + 1].strip()
        right_part = text[best_index + 1:].strip()
    else:
        left_part = text[:best_index].strip()
        right_part = text[best_index:].strip()
    left_chunks = split_text_by_token_limit(left_part, limit, count_tokens_fn)
    right_chunks = split_text_by_token_limit(right_part, limit, count_tokens_fn)
    return left_chunks + right_chunks

def adjust_chunks_for_document(chunks, min_chunk_size, max_chunk_size, count_tokens_fn):
    adjusted = []
    for chunk in chunks:
        t = chunk["text"]
        tokens = count_tokens_fn(t)
        if max_chunk_size > 0 and tokens > max_chunk_size:
            sub_texts = split_text_by_token_limit(t, max_chunk_size, count_tokens_fn)
            for sub in sub_texts:
                new_chunk = chunk.copy()
                new_chunk["text"] = sub
                new_chunk["token_count"] = count_tokens_fn(sub)
                adjusted.append(new_chunk)
            logger.info(f"Chunk with {tokens} tokens split into {len(sub_texts)} chunks")
        else:
            adjusted.append(chunk)
    merged = []
    i = 0
    while i < len(adjusted):
        current_text = adjusted[i]["text"]
        current_token_count = count_tokens(current_text)
        new_chunk = adjusted[i].copy()
        merged_start = adjusted[i]["position_start"]
        merged_end = adjusted[i]["position_end"]
        j = i
        while current_token_count < min_chunk_size and j < len(adjusted) - 1:
            j += 1
            current_text = current_text + " " + adjusted[j]["text"]
            current_token_count = count_tokens(current_text)
            merged_end = adjusted[j]["position_end"]
        new_chunk["text"] = current_text
        new_chunk["token_count"] = current_token_count
        new_chunk["position_start"] = merged_start
        new_chunk["position_end"] = merged_end
        merged.append(new_chunk)
        if j != i:
            logger.info(f"Merged chunks from index {i} to {j} into one chunk with {current_token_count} tokens")
        i = j + 1
    return merged

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
            "position_start": start_pos,
            "position_end": end_pos,
            "overlap": {"previous_chunk_id": prev_id, "next_chunk_id": next_id, "overlap_previous_chars": 0, "overlap_next_chars": 0},
            "sequence_info": {"is_first_chunk": i == 0, "is_last_chunk": i == len(paragraphs) - 1, "total_chunks": len(paragraphs), "relative_position": i / max(1, len(paragraphs) - 1) if len(paragraphs) > 1 else 0.5}
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
            "position_start": chunk_details["position_start"],
            "position_end": chunk_details["position_end"],
            "is_first_chunk": chunk_details["sequence_info"]["is_first_chunk"],
            "is_last_chunk": chunk_details["sequence_info"]["is_last_chunk"],
            "relative_position": chunk_details["sequence_info"]["relative_position"],
            "token_count": token_count
        })
    logger.info(f"Processed file {file_name} into {len(text_chunks)} chunks")
    return chunks_data, global_chunk_id

def remove_exact_duplicates(df):
    original_len = len(df)
    df_new = df.drop_duplicates(subset=["text"]).reset_index(drop=True)
    removed = original_len - len(df_new)
    if removed > 0:
        logger.info(f"Exact duplicate texts removed: {removed}")
    else:
        logger.info("No exact duplicate texts found")
    return df_new

def remove_non_alphanumeric_rows(df):
    pattern = re.compile(r'[A-Za-z0-9]')
    valid_mask = df["text"].apply(lambda x: bool(pattern.search(x)))
    removed = (~valid_mask).sum()
    if removed > 0:
        logger.info(f"Rows removed with no alphanumeric characters: {removed}")
    else:
        logger.info("All rows contain alphanumeric characters")
    return df[valid_mask].reset_index(drop=True)

def remove_long_sequences(df):
    df["text"] = df["text"].apply(lambda x: re.sub(r'([-_=])\1{3,}', '', x))
    logger.info("Removed long sequences from text fields")
    return df

def deduplicate_chunks(df, threshold=0.95):
    df = remove_exact_duplicates(df)
    df = remove_non_alphanumeric_rows(df)
    texts = df["text"].tolist()
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(texts)
    sim_matrix = cosine_similarity(tfidf_matrix)
    to_drop = set()
    logged_pairs = set()
    n = df.shape[0]
    duplicate_count = 0
    for i in range(n):
        if i in to_drop:
            continue
        for j in range(i + 1, n):
            if j in to_drop:
                continue
            similarity = sim_matrix[i, j]
            id_i = df.iloc[i]['chunk_id']
            id_j = df.iloc[j]['chunk_id']
            pair = tuple(sorted((id_i, id_j)))
            if similarity >= threshold and pair not in logged_pairs and id_i != id_j:
                logger.info(f"Duplicate found: Chunk {id_i} and Chunk {id_j} have similarity {similarity:.2f}")
                logged_pairs.add(pair)
                to_drop.add(j)
                duplicate_count += 1
    logger.info(f"Total duplicates removed: {duplicate_count}")
    new_df = df.drop(df.index[list(to_drop)]).reset_index(drop=True)
    new_df = reindex_chunks(new_df)
    new_df = remove_long_sequences(new_df)
    return new_df

def reindex_chunks(df):
    df = df.sort_values(by=["document_id", "position_start"]).reset_index(drop=True)
    n = len(df)
    new_ids = list(range(1, n + 1))
    df["chunk_id"] = new_ids
    df["chunk_index"] = new_ids
    df["is_first_chunk"] = (df.index == 0)
    df["is_last_chunk"] = (df.index == (n - 1))
    if n > 1:
        df["relative_position"] = df.index / (n - 1)
    else:
        df["relative_position"] = 0.5
    logger.info("Reindexed chunks")
    return df

def preprocess_data(input_dir, output_dir, dedup_threshold=None, min_chunk_size=0, max_chunk_size=0):
    start_time = time.time()
    logger.info("Starting preprocessing of Markdown files")
    logger.info(f"Input directory: {input_dir}")
    logger.info(f"Output directory: {output_dir}")
    os.makedirs(output_dir, exist_ok=True)
    markdown_files = [os.path.join(input_dir, f) for f in os.listdir(input_dir) if f.lower().endswith(('.md', '.markdown'))]
    if not markdown_files:
        logger.error(f"No Markdown files found in {input_dir}")
        return {"data": pd.DataFrame()}
    logger.info(f"Found {len(markdown_files)} Markdown files to process")
    all_chunks = []
    global_chunk_id = 1
    for i, md_file in enumerate(markdown_files):
        if SHUTDOWN_REQUESTED:
            break
        file_name = os.path.basename(md_file)
        logger.info(f"Processing {i+1}/{len(markdown_files)}: {file_name}")
        try:
            chunks_data, global_chunk_id = process_markdown_file(md_file, global_chunk_id)
            if min_chunk_size > 0 or max_chunk_size > 0:
                chunks_data = adjust_chunks_for_document(chunks_data, min_chunk_size, max_chunk_size, count_tokens)
            all_chunks.extend(chunks_data)
        except Exception as e:
            logger.error(f"Error processing {file_name}: {str(e)}")
    data_df = pd.DataFrame(all_chunks)
    if not data_df.empty:
        if dedup_threshold is not None:
            data_df = deduplicate_chunks(data_df, threshold=dedup_threshold)
        else:
            data_df = reindex_chunks(data_df)
    if data_df.empty:
        logger.info("No data to save. Output Parquet/JSONL file will be empty or not created.")
    else:
        logger.info("Saving data to Parquet file...")
        data_file = os.path.join(output_dir, "data.parquet")
        data_df.to_parquet(data_file, index=False)
        logger.info("Saving data to JSONL file...")
        jsonl_file = os.path.splitext(data_file)[0] + ".jsonl"
        data_df.to_json(jsonl_file, orient="records", lines=True)
        logger.info(f"Data saved to {data_file} and {jsonl_file}")
    elapsed_time = time.time() - start_time
    total_chunks = len(data_df)
    summary_message = f"✓ Successfully processed {len(markdown_files)} documents into {total_chunks} chunks.\nTotal processing time: {elapsed_time:.2f} seconds."
    logger.info(summary_message)
    return {"data": data_df}

def main():
    parser = argparse.ArgumentParser(description="Process Markdown to structured Parquet/JSONL datasets.")
    parser.add_argument("-i", "--input", required=True, help="Input directory containing Markdown files.")
    parser.add_argument("-o", "--output", required=True, help="Output directory for processed data.")
    parser.add_argument("-d", "--dedup_threshold", type=float, default=None, help="Deduplication similarity threshold (e.g., 0.90)")
    parser.add_argument("--min-chunk-size", type=int, default=128, help="Minimum tokens per chunk for merging. 0 to disable.")
    parser.add_argument("--max-chunk-size", type=int, default=256, help="Maximum tokens per chunk for splitting. 0 to disable.")
    args = parser.parse_args()
    logger.info("F-Instruct Document Preprocessor")
    logger.info("Converting Markdown to structured Parquet/JSONL datasets")
    try:
        preprocess_data(args.input, args.output, args.dedup_threshold, args.min_chunk_size, args.max_chunk_size)
        sys.exit(0)
    except KeyboardInterrupt:
        logger.info("Process interrupted by user")
        sys.exit(1)
    except Exception as e:
        logger.error(f"Error: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()