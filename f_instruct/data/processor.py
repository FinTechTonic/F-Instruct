#!/usr/bin/env python3
import os
import sys
import time
import argparse
import json
import signal
import logging
import sqlite3
import asyncio
from datetime import datetime
from collections import defaultdict
import pandas as pd
import tiktoken
from ollama import AsyncClient
from tqdm import tqdm
import threading

logging.basicConfig(stream=sys.stdout, level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

def signal_handler(sig, frame):
    os._exit(0)
signal.signal(signal.SIGINT, signal_handler)

def ensure_directory(base_dir, subdir=None):
    path = os.path.join(base_dir, subdir) if subdir else base_dir
    os.makedirs(path, exist_ok=True)
    return path

def save_json(data, file_path):
    with open(file_path, "w") as f:
        json.dump(data, f, indent=2)
    return file_path

def count_tokens(text, encoding_name="cl100k_base"):
    encoding = tiktoken.get_encoding(encoding_name)
    return len(encoding.encode(text))

def _collect_context_chunks(doc_chunks, doc_indices, current_position, direction, max_steps, adjusted_page_size, estimated_tokens):
    collected_indices = []
    for i in range(1, max_steps + 1):
        new_position = current_position + (i * direction)
        if new_position < 0 or new_position >= len(doc_indices):
            break
        idx = doc_indices[new_position]
        text_tokens = doc_chunks.loc[idx]["token_count"]
        if estimated_tokens + text_tokens > adjusted_page_size:
            break
        estimated_tokens += text_tokens
        collected_indices.append(idx)
    if direction < 0:
        collected_indices.reverse()
    return collected_indices, estimated_tokens

class Page:
    def __init__(self, page_id, text, chunk_id, doc_title, ahead, behind, token_size=0, embedding=None):
        self.page_id = page_id
        self.text = text
        self.chunk_id = chunk_id
        self.doc_title = doc_title
        self.ahead = ahead
        self.behind = behind
        self.token_size = token_size
        self.embedding = embedding

def create_metadata_dict(chunk_row, all_chunk_ids, is_first_chunk, is_last_chunk, actual_text_tokens, total_tokens_val, num_context_chunks):
    return {"title": chunk_row.get("title", ""),
            "chunk_id": chunk_row.get("chunk_id", None),
            "ahead": max(0, len(all_chunk_ids)//2),
            "behind": max(0, len(all_chunk_ids) - (len(all_chunk_ids)//2) - 1)}

def get_chunk_context(df, chunk_id, page_size=2048):
    if chunk_id not in df.index:
        return None, {}
    chunk_row = df.loc[chunk_id]
    document_id = chunk_row["document_id"]
    doc_chunks = df[df["document_id"] == document_id].copy()
    doc_chunks = doc_chunks.reset_index(drop=True)
    doc_chunks.sort_values(by="chunk_id", inplace=True)
    doc_chunks.reset_index(drop=True, inplace=True)
    doc_ids = doc_chunks["chunk_id"].tolist()
    current_position = doc_ids.index(chunk_id)
    adjusted_page_size = max(0, page_size - 150)
    estimated_tokens = doc_chunks.iloc[current_position]["token_count"]
    max_chunks_before = current_position
    max_chunks_after = len(doc_ids) - 1 - current_position
    before_indices, estimated_tokens = _collect_context_chunks(doc_chunks, list(range(len(doc_ids))), current_position, -1, max_chunks_before, adjusted_page_size, estimated_tokens)
    after_indices, estimated_tokens = _collect_context_chunks(doc_chunks, list(range(len(doc_ids))), current_position, 1, max_chunks_after, adjusted_page_size, estimated_tokens)
    all_chunks = []
    for idx in before_indices:
        chunk_id_val = int(doc_chunks.iloc[idx]["chunk_id"])
        all_chunks.append({"chunk_id": chunk_id_val, "text": doc_chunks.iloc[idx]["text"]})
    main_chunk_id = int(chunk_row["chunk_id"])
    all_chunks.append({"chunk_id": main_chunk_id, "text": doc_chunks.iloc[current_position]["text"]})
    for idx in after_indices:
        chunk_id_val = int(doc_chunks.iloc[idx]["chunk_id"])
        all_chunks.append({"chunk_id": chunk_id_val, "text": doc_chunks.iloc[idx]["text"]})
    all_chunks.sort(key=lambda x: x["chunk_id"])
    existing_ids = {chunk["chunk_id"] for chunk in all_chunks}
    min_chunk = min(existing_ids)
    max_chunk = max(existing_ids)
    for i in range(min_chunk, max_chunk + 1):
        if i not in existing_ids:
            all_chunks.append({"chunk_id": i, "text": ""})
    all_chunks.sort(key=lambda x: x["chunk_id"])
    combined_text = " ".join(chunk["text"] for chunk in all_chunks)
    actual_text_tokens = count_tokens(combined_text)
    meta = create_metadata_dict(chunk_row, [chunk["chunk_id"] for chunk in all_chunks], False, False, actual_text_tokens, actual_text_tokens + 150, len(before_indices) + len(after_indices) + 1)
    meta["token_size"] = actual_text_tokens
    meta["ahead"] = main_chunk_id - min_chunk
    meta["behind"] = max_chunk - main_chunk_id
    return combined_text, meta

def find_next_chunk_id(all_chunk_ids_list, overlap=10):
    if not all_chunk_ids_list:
        return None
    all_chunk_ids_list.sort()
    return all_chunk_ids_list[0]

def create_page(df, next_chunk_id, page_size=2048):
    context_text, meta = get_chunk_context(df, next_chunk_id, page_size)
    if context_text:
        token_size = count_tokens(context_text)
        meta["token_size"] = token_size
        return Page(page_id=meta["chunk_id"], text=context_text, chunk_id=meta["chunk_id"], doc_title=meta["title"], ahead=meta["ahead"], behind=meta["behind"], token_size=token_size)
    return None

def load_data(input_file, page_size=2048, overlap=10):
    df = pd.read_parquet(input_file)
    df = df.set_index("chunk_id", drop=False)
    all_chunk_ids_list = df.index.unique().tolist()
    pages = []
    for _ in tqdm(range(len(all_chunk_ids_list)), desc="Creating page fragments", total=len(all_chunk_ids_list)):
        next_chunk_id = find_next_chunk_id(all_chunk_ids_list, overlap)
        if next_chunk_id is None:
            break
        page = create_page(df, next_chunk_id, page_size)
        if page:
            pages.append(page)
            all_chunk_ids_list.remove(next_chunk_id)
    pages.sort(key=lambda p: int(p.page_id))
    if pages:
        avg_chunks = sum(p.ahead + 1 + p.behind for p in pages) / len(pages)
        avg_tokens = sum(p.token_size for p in pages) / len(pages)
        logger.info("Page summary: %d pages generated. Average chunks per page: %.1f, Average tokens per page: %.1f", len(pages), avg_chunks, avg_tokens)
    return pages

async def compute_embeddings(pages, client, model, batch_size=10, checkpoint_interval=100, output_dir=None, page_offset=None, page_length=None):
    results = []
    start_time = time.time()
    total_pages = len(pages)
    total_tokens = sum(p.token_size for p in pages)
    processed_pages = 0
    processed_tokens = 0
    for p in pages:
        worker_id = "main"
        logger.info("<%s> : <%s> : Embedding page text (first 100 chars): %s", worker_id, int(p.page_id), p.text[:100])
        result = await client.embeddings(model=model, prompt=p.text)
        emb_raw = result.get("embedding")
        if emb_raw is None or emb_raw == "null":
            emb = []
        elif isinstance(emb_raw, str):
            try:
                emb = json.loads(emb_raw)
            except Exception:
                emb = emb_raw
        else:
            emb = emb_raw
        logger.info("<%s> : <%s> : Embedding result: %s", worker_id, int(p.page_id), str(emb)[:100])
        processed_pages += 1
        processed_tokens += p.token_size
        results.append((p, emb))
        logger.info("<%s> : <%s> : Processed page %d/%d", worker_id, int(p.page_id), processed_pages, total_pages)
        if processed_pages % checkpoint_interval == 0:
            elapsed = time.time() - start_time
            avg_tokens_per_sec = processed_tokens / elapsed if elapsed > 0 else 0
            eta = (total_tokens - processed_tokens) / avg_tokens_per_sec if avg_tokens_per_sec > 0 else float('inf')
            logger.info("Telemetry: %d/%d pages processed. ETA: %.1fs, Throughput: %.1f tokens/s", processed_pages, total_pages, eta, avg_tokens_per_sec)
            if output_dir:
                df_db = assemble_dataframe([pg for pg, _ in results])
                save_database(df_db, output_dir, is_checkpoint=True, page_offset=page_offset, page_length=page_length)
    for page, emb in results:
        page.embedding = emb
    elapsed_total = time.time() - start_time
    avg_tokens_per_sec = processed_tokens / elapsed_total if elapsed_total > 0 else 0
    logger.info("Final summary: Processed %d/%d pages. Total tokens: %d, Average throughput: %.1f tokens/s", processed_pages, total_pages, total_tokens, avg_tokens_per_sec)
    return [pg for pg, _ in results]

def assemble_dataframe(pages):
    rows = [{"page_id": p.page_id, "doc_title": p.doc_title, "chunk_id": p.chunk_id, "ahead": p.ahead, "behind": p.behind, "token_size": p.token_size, "text": p.text, "embedding": json.dumps(p.embedding)} for p in pages]
    return pd.DataFrame(rows)

def save_database(df_db, output_dir, is_checkpoint=False, page_offset=None, page_length=None):
    suffix = ""
    if page_offset is not None and page_length is not None:
        suffix = f"_page_offset_{page_offset}_page_length_{page_length}"
    if is_checkpoint:
        cp_dir = ensure_directory(output_dir, "checkpoints")
        filename = "pages_checkpoint_" + datetime.now().strftime("%Y%m%d_%H%M%S") + suffix + ".sqlite"
        db_path = os.path.join(cp_dir, filename)
    else:
        db_path = os.path.join(output_dir, "pages" + suffix + ".sqlite")
    conn = sqlite3.connect(db_path)
    df_db.to_sql("pages", conn, if_exists="replace", index=False)
    conn.commit()
    conn.close()
    logger.info("Database saved to %s", db_path)
    jsonl_path = os.path.splitext(db_path)[0] + ".jsonl"
    df_db.to_json(jsonl_path, orient="records", lines=True)
    logger.info("JSONL saved to %s", jsonl_path)
    parquet_path = os.path.splitext(db_path)[0] + ".parquet"
    df_db.to_parquet(parquet_path)
    logger.info("Parquet saved to %s", parquet_path)

def run_pipeline(args):
    pages = load_data(args.input, args.page_size, args.overlap)
    pages = pages[args.page_offset: args.page_offset + args.page_length]
    output_dir = ensure_directory(args.output, "database")
    client = AsyncClient()
    pages = asyncio.run(compute_embeddings(pages, client, args.model, args.batch_size, args.checkpoint_interval, output_dir, page_offset=args.page_offset, page_length=args.page_length))
    df_db = assemble_dataframe(pages)
    save_database(df_db, output_dir, is_checkpoint=False, page_offset=args.page_offset, page_length=args.page_length)

def parse_arguments():
    parser = argparse.ArgumentParser(description="Build a searchable vector database from financial documents.")
    parser.add_argument("-i", "--input", required=True, help="Path to data.parquet file")
    parser.add_argument("-o", "--output", required=True, help="Output directory for vector database and generated examples")
    parser.add_argument("-m", "--model", required=True, help="Ollama model to use for embeddings (e.g., 'phi4')")
    parser.add_argument("--max-pairs", type=int, default=1000, help="Maximum number of training pairs to generate (default: 1000)")
    parser.add_argument("--max-tokens", type=int, default=4096, help="Maximum tokens per document for embeddings (default: 4096)")
    parser.add_argument("--page-size", type=int, default=2048, help="Page size in tokens (default: 2048)")
    parser.add_argument("--api", required=True, help="Path to the OpenBank API directory with extracted files")
    parser.add_argument("--api-limit", type=int, default=1000, help="Maximum number of API examples to generate (default: 1000)")
    parser.add_argument("--checkpoint-interval", type=int, default=100, help="Telemetry checkpoint interval (default: 100)")
    parser.add_argument("--overlap", type=int, default=10, help="Number of chunks to overlap between documents (default: 10)")
    parser.add_argument("--batch-size", type=int, default=10, help="Batch size for asynchronous embeddings (default: 10)")
    parser.add_argument("--page-offset", type=int, default=0, help="Offset in pages array (default: 0)")
    parser.add_argument("--page-length", type=int, default=500, help="Number of pages to process (default: 500)")
    return parser.parse_args()

def main():
    args = parse_arguments()
    ensure_directory(args.output)
    if not os.path.exists(args.input):
        sys.exit(1)
    run_pipeline(args)

if __name__ == "__main__":
    sys.exit(main())