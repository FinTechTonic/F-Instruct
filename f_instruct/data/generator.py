#!/usr/bin/env python3
import argparse
import json
import logging
import os
import time
from dataclasses import asdict, dataclass, field
from datetime import timedelta
from enum import Enum
from typing import Any, Dict, List, Optional
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from collections import deque

import pandas as pd
import tiktoken
from tenacity import RetryError, retry, retry_if_exception_type, stop_after_attempt, wait_exponential
from openai import OpenAI
from f_instruct.templates.categories import TRAINING_PAIR_TYPES
from f_instruct.templates.gen_prompts import CATEGORY_TEMPLATES, GENERATOR_PROMPTS, REFERENCE_FORMAT_TEMPLATE
from argparse import Namespace

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(), logging.FileHandler("generator.log")])

OPENROUTER_API_BASE_URL = "https://openrouter.ai/api/v1"
OPENROUTER_HTTP_REFERER = "https://github.com/FinTechTonic"
OPENROUTER_X_TITLE = "Team Tonic"

OPENROUTER_RETRY_ATTEMPTS = 5
OPENROUTER_RETRY_WAIT_MIN = 5
OPENROUTER_RETRY_WAIT_MAX = 60

class GenerationError(Exception): pass

@dataclass
class TrainingPair:
    id: int
    input_text: str
    output_text: str
    category: str
    subcategory: str
    page_id: int 
    source_doc_title: str
    metadata: Dict[str, Any] = field(default_factory=dict)

def count_tokens(text, encoding_name="cl100k_base"):
    encoding = tiktoken.get_encoding(encoding_name)
    return len(encoding.encode(text))

def format_references(reference_docs: List[pd.Series]) -> str:
    if not reference_docs:
        return ""
    
    formatted_refs = []
    for i, doc_series in enumerate(reference_docs, 1):
        title = doc_series.get('doc_title', f'Reference Document {i}')
        content = doc_series.get('text', '')
        
        if len(content) > 1500: 
            content = content[:1497] + "..."
        
        formatted_refs.append(REFERENCE_FORMAT_TEMPLATE.format(
            index=i,
            title=title,
            content=content
        ))
    
    return "".join(formatted_refs)

class AgentState(Enum):
    INIT = "INIT"
    LOADING_DATA = "LOADING_DATA"
    LOADING_EMBEDDINGS = "LOADING_EMBEDDINGS"
    PREPARING_GENERATION = "PREPARING_GENERATION"
    GENERATING_PAIRS = "GENERATING_PAIRS"
    FORMATTING_OUTPUT = "FORMATTING_OUTPUT"
    SAVING_RESULTS = "SAVING_RESULTS"
    GENERATING_REPORT = "GENERATING_REPORT"
    ERROR = "ERROR"
    DONE = "DONE"

class TrainingPairGenerator:
    
    def __init__(self, input_path: str, output_path: str, embeddings_path: str):
        self.input_path = input_path
        self.output_path = output_path
        self.embeddings_path = embeddings_path
        self.df = None
        self.embeddings_df = None
        self.training_pairs = []
        self.state = AgentState.INIT
        self.prev_state = None
        self.start_time = time.time()
        self.total_pages = 0
        self.total_pairs_generated = 0
        self.total_tokens_processed = 0
        self.args: Optional[Namespace] = None
        self.checkpoint_count = 0
        self.llm_client = None
        self.llm_extra_headers = None
        
        self.request_timestamps = deque()
        self.RATE_LIMIT_MAX_REQUESTS = 10
        self.RATE_LIMIT_INTERVAL_SECONDS = 10.0
        
        self.output_parquet_path: Optional[str] = None
        self.output_jsonl_path: Optional[str] = None
        
        if os.path.isdir(self.output_path) or \
           (not os.path.exists(self.output_path) and not os.path.splitext(self.output_path)[1]):
            effective_output_dir = self.output_path
        else:
            effective_output_dir = os.path.dirname(self.output_path)
        
        if not effective_output_dir:
            effective_output_dir = "."

        self.build_dir = os.path.join(effective_output_dir, "build")
        self.checkpoint_dir = os.path.join(effective_output_dir, "checkpoints")
        
        os.makedirs(self.build_dir, exist_ok=True)
        os.makedirs(self.checkpoint_dir, exist_ok=True)
        logging.info(f"Debug files (including conversation logs) will be stored in: {self.build_dir}")
        logging.info(f"Checkpoint files will be stored in: {self.checkpoint_dir}")
        
        self.state_handlers = {
            AgentState.INIT: self._init_state,
            AgentState.LOADING_DATA: self._loading_data_state,
            AgentState.LOADING_EMBEDDINGS: self._loading_embeddings_state,
            AgentState.PREPARING_GENERATION: self._preparing_generation_state,
            AgentState.GENERATING_PAIRS: self._generating_pairs_state,
            AgentState.FORMATTING_OUTPUT: self._formatting_output_state, 
            AgentState.SAVING_RESULTS: self._saving_results_state,       
            AgentState.GENERATING_REPORT: self._generating_report_state, 
        }
        
        self.templates = {}
        self.category_weights = {}
        self.current_page_index = 0
        self.processed_pages = set()
    
    def transition_state(self, new_state: AgentState, reason: Optional[str] = None):
        reason_msg = f" Reason: {reason}" if reason else ""
        logging.info(f"FSM State Transition: {self.state.value} -> {new_state.value}.{reason_msg}")
        self.prev_state, self.state = self.state, new_state
    
    def _log_telemetry(self):
        elapsed = time.time() - self.start_time
        tokens_per_sec = self.total_tokens_processed / elapsed if elapsed > 0 else 0
        pairs_per_sec = self.total_pairs_generated / elapsed if elapsed > 0 else 0
        elapsed_str = str(timedelta(seconds=int(elapsed)))
        
        if self.total_pages > 0:
            percent_complete = (len(self.processed_pages) / self.total_pages) * 100
        else:
            percent_complete = 0
        
        logging.info(f"Telemetry: {len(self.training_pairs)} training pairs generated. "
                   + f"Progress: {percent_complete:.1f}% ({len(self.processed_pages)}/{self.total_pages} pages). "
                   + f"Time: {elapsed_str}, "
                   + f"Tokens/sec: {tokens_per_sec:.1f}, Pairs/sec: {pairs_per_sec:.2f}")
    
    def _save_checkpoint(self):
        logging.info(f"Saving checkpoint with {len(self.training_pairs)} training pairs")
        timestamp = int(time.time())
        checkpoint_df = pd.DataFrame([asdict(pair) for pair in self.training_pairs])
        checkpoint_filename = f"training_pairs_checkpoint_{timestamp}"
        checkpoint_parquet_path = os.path.join(self.checkpoint_dir, f"{checkpoint_filename}.parquet")
        checkpoint_df.to_parquet(checkpoint_parquet_path, index=False)
        checkpoint_jsonl_path = os.path.join(self.checkpoint_dir, f"{checkpoint_filename}.jsonl")
        checkpoint_df.to_json(checkpoint_jsonl_path, orient='records', lines=True)
        logging.info(f"Checkpoint saved to {checkpoint_parquet_path} and {checkpoint_jsonl_path}")
    
    def _save_conversation(self, messages: List[Dict[str, str]], record_id: int, category: str, subcategory: str, is_success: bool) -> str:
        timestamp = int(time.time())
        status = "success" if is_success else "failed"
        filename = f"{record_id}_{status}_{timestamp}.json"
        conversation_filepath = os.path.join(self.build_dir, filename)
        
        conversation_data = {
            "id": f"{record_id}_{status}_{timestamp}",
            "messages": messages
        }
        
        try:
            with open(conversation_filepath, 'w', encoding='utf-8') as f:
                json.dump(conversation_data, f, indent=2)
            log_message = f"Saved {'successful' if is_success else 'failed'} conversation to {conversation_filepath}"
            if is_success:
                logging.debug(log_message) 
            else:
                logging.error(log_message)
        except Exception as e:
            logging.error(f"Failed to save conversation log {filename}: {e}")
            
        return conversation_filepath

    def _validate_response_format(self, response: str, category: str) -> bool:
        if category == "question_answering":
            return "QUESTION:" in response and "ANSWER:" in response
        elif category == "instruction_following":
            return "INSTRUCTION:" in response and "RESULT:" in response
        elif category == "financial_product_creation":
            return "TASK:" in response and "PRODUCT DEFINITION:" in response
        elif category == "regulatory_compliance":
            return "COMPLIANCE TASK:" in response and "COMPLIANCE OUTPUT:" in response
        return False

    def _save_dataframe_to_file(self, df: pd.DataFrame, filepath: str, format_type: str) -> None:
        if format_type == 'parquet': 
            df.to_parquet(filepath, index=False)
        elif format_type == 'jsonl': 
            df.to_json(filepath, orient='records', lines=True)
        else:
            logging.warning(f"Unsupported format: {format_type}, defaulting to parquet")
            df.to_parquet(filepath, index=False)
        logging.info(f"Saved data in {format_type.upper()} format to {filepath}")
    
    def run(self):
        logging.info(f"Starting TrainingPairGenerator")
        logging.info(f"Input path: {self.input_path}")
        logging.info(f"Embeddings path: {self.embeddings_path}")
        logging.info(f"Output path: {self.output_path}")
        self.start_time = time.time()
        self.state = AgentState.INIT
        
        while self.state != AgentState.DONE and self.state != AgentState.ERROR:
            try:
                handler = self.state_handlers.get(self.state)
                if handler:
                    handler()
                else:
                    logging.info(f"Unhandled state: {self.state.value}")
                    self.transition_state(AgentState.ERROR, f"Unhandled state: {self.state.value}")
            except Exception as e:
                logging.error(f"Error in state {self.state.value}: {str(e)}", exc_info=True)
                self.transition_state(AgentState.ERROR, f"Exception: {str(e)}")
        
        return self.state != AgentState.ERROR
    
    def _init_state(self):
        logging.info("Initializing training pair generation process")
        self.templates = self.get_templates()
        
        if self.args and self.args.openrouter_api_key:
            self.llm_extra_headers = {
                "HTTP-Referer": OPENROUTER_HTTP_REFERER,
                "X-Title": OPENROUTER_X_TITLE
            }
            
            self.llm_client = OpenAI(
                base_url=OPENROUTER_API_BASE_URL,
                api_key=self.args.openrouter_api_key,
            )
            if self.args and hasattr(self.args, 'openrouter_model'):
                logging.info(f"OpenRouter client initialized for model: {self.args.openrouter_model}")
            logging.info(f"OpenRouter headers: {self.llm_extra_headers}")
        else:
            logging.error("OpenRouter API key not provided. LLM-based answer generation will fail.")
            
        self.transition_state(AgentState.LOADING_DATA)
    
    def get_templates(self) -> Dict[str, Any]:
        templates = {}
        
        for category, category_info in TRAINING_PAIR_TYPES.items():
            category_template = CATEGORY_TEMPLATES.get(category, CATEGORY_TEMPLATES["question_answering"])
            
            for subcategory, subcategory_desc in category_info["subcategories"].items():
                template_key = f"{category}/{subcategory}"
                templates[template_key] = {
                    "input_template": category_template["input_template"],
                    "output_template": category_template["output_template"],
                    "instructions": category_template["instructions"],
                    "subcategory_description": subcategory_desc,
                    "tasks": category_template.get("tasks", []),
                    "questions": category_template.get("questions", [])
                }
            
            templates[category] = {
                "input_template": category_template["input_template"],
                "output_template": category_template["output_template"],
                "instructions": category_template["instructions"],
                "subcategory_description": category_info["description"],
                "tasks": category_template.get("tasks", []),
                "questions": category_template.get("questions", [])
            }
            
        return templates
    
    def _loading_data_state(self):
        logging.info(f"Loading dataset from {self.input_path}")
        try:
            self.df = pd.read_parquet(self.input_path)
            self.total_pages = len(self.df)
            logging.info(f"Successfully loaded {self.total_pages} pages from dataset")
            
            if self.df is not None and 'category' in self.df.columns:
                category_counts = self.df['category'].value_counts(normalize=True)
                self.category_weights = category_counts.to_dict()
                logging.info(f"Category distribution: {dict(category_counts.items())}")
            
            self.transition_state(AgentState.LOADING_EMBEDDINGS)
        except Exception as e:
            logging.error(f"Failed to load dataset: {str(e)}", exc_info=True)
            self.transition_state(AgentState.ERROR, f"Data loading failed: {str(e)}")
    
    def _loading_embeddings_state(self):
        logging.info(f"Loading embeddings from {self.embeddings_path}")
        try:
            self.embeddings_df = pd.read_parquet(self.embeddings_path)
            logging.info(f"Successfully loaded embeddings for {len(self.embeddings_df)} pages")
            
            self.embedding_cache = {}
            if 'page_id' in self.embeddings_df.columns and 'embedding' in self.embeddings_df.columns:
                for _, row in self.embeddings_df.iterrows():
                    page_id = row['page_id']
                    embedding_str = row['embedding']
                    try:
                        self.embedding_cache[page_id] = json.loads(embedding_str)
                    except Exception as e:
                        logging.warning(f"Failed to parse embedding for page_id {page_id}: {e}")
                logging.info(f"Cached embeddings for {len(self.embedding_cache)} pages in memory")
            
            self.transition_state(AgentState.PREPARING_GENERATION)
        except Exception as e:
            logging.error(f"Failed to load embeddings: {str(e)}", exc_info=True)
            self.transition_state(AgentState.ERROR, f"Embeddings loading failed: {str(e)}")
    
    def _find_similar_pages(self, page_id, top_n=3):
        if not hasattr(self, 'embedding_cache') or not self.embedding_cache:
            logging.warning("Embedding cache not available. Cannot find similar pages.")
            return []
        
        if page_id not in self.embedding_cache:
            logging.warning(f"Page ID {page_id} not found in embedding cache")
            return []
        
        try:
            current_embedding = self.embedding_cache[page_id]
            
            embedding_dim = len(current_embedding)
            logging.info(f"Embedding for page {page_id} has dimension {embedding_dim}")
            
            if embedding_dim == 0:
                logging.warning(f"Page {page_id} has empty embedding, skipping similarity search")
                return []
                
            current_embedding_array = np.array(current_embedding).reshape(1, embedding_dim)
            
            all_embeddings = []
            page_ids = []
            
            for pid, embedding in self.embedding_cache.items():
                if pid == page_id:
                    continue
                
                if len(embedding) != embedding_dim:
                    logging.warning(f"Embedding dimension mismatch: page {pid} has {len(embedding)} dims, expected {embedding_dim}")
                    continue
                
                all_embeddings.append(embedding)
                page_ids.append(pid)
            
            if not all_embeddings:
                logging.warning(f"No comparable embeddings found for page {page_id}")
                return []
            
            logging.info(f"Computing similarities between page {page_id} and {len(all_embeddings)} other pages")
            all_embeddings_array = np.array(all_embeddings)
            
            logging.info(f"Similarity computation shapes: {current_embedding_array.shape} x {all_embeddings_array.shape}")
            
            similarities = cosine_similarity(current_embedding_array, all_embeddings_array).flatten()
            
            if len(similarities) > 0:
                logging.info(f"Similarity score stats: min={similarities.min():.4f}, max={similarities.max():.4f}, mean={similarities.mean():.4f}")
            
            top_indices = similarities.argsort()[-top_n:][::-1]
            similar_page_ids = [page_ids[i] for i in top_indices]
            
            for i, idx in enumerate(top_indices):
                logging.info(f"Similar page {i+1}: page_id={page_ids[idx]}, similarity={similarities[idx]:.4f}")
            
            return similar_page_ids
        except Exception as e:
            logging.error(f"Error finding similar pages: {e}", exc_info=True)
            return []
    
    def _preparing_generation_state(self):
        logging.info("Preparing for training pair generation")
        
        if self.df is None:
            logging.error("DataFrame not loaded. Cannot prepare for generation.")
            self.transition_state(AgentState.ERROR, f"DataFrame not loaded.")
            return
            
        required_columns = ['page_id', 'text', 'doc_title', 'category', 'subcategory', 'keywords']
        if self.args and self.args.threshold > 0:
            required_columns.append('similarity_score')
            
        missing_columns = [col for col in required_columns if col not in self.df.columns]
        if missing_columns:
            logging.error(f"Missing required columns in input data: {missing_columns}")
            self.transition_state(AgentState.ERROR, f"Missing required columns: {missing_columns}")
            return
        
        self.current_page_index = 0
        self.training_pairs = []
        self.total_pairs_generated = 0
        self.total_tokens_processed = 0
        self.processed_pages = set()

        is_explicit_dir = os.path.isdir(self.output_path) or \
                          (not os.path.exists(self.output_path) and not os.path.splitext(self.output_path)[1])
        if is_explicit_dir:
            target_output_dir = self.output_path
            base_name = "training_pairs"
        else:
            target_output_dir = os.path.dirname(self.output_path)
            if not target_output_dir: target_output_dir = "."
            base_name = os.path.splitext(os.path.basename(self.output_path))[0]
        
        self.output_parquet_path = os.path.join(target_output_dir, base_name + ".parquet")
        self.output_jsonl_path = os.path.join(target_output_dir, base_name + ".jsonl")

        existing_data_loaded = False
        if os.path.exists(self.output_parquet_path):
            try:
                existing_df = pd.read_parquet(self.output_parquet_path)
                if not existing_df.empty and 'page_id' in existing_df.columns:
                    self.processed_pages.update(existing_df['page_id'].unique())
                    existing_data_loaded = True
                    logging.info(f"Loaded {len(self.processed_pages)} unique processed page IDs from existing {self.output_parquet_path}")
            except Exception as e:
                logging.warning(f"Could not load or process existing Parquet file {self.output_parquet_path}: {e}")
        
        if not existing_data_loaded and os.path.exists(self.output_jsonl_path):
            try:
                existing_df = pd.read_json(self.output_jsonl_path, lines=True)
                if not existing_df.empty and 'page_id' in existing_df.columns:
                    self.processed_pages.update(existing_df['page_id'].unique())
                    logging.info(f"Loaded {len(self.processed_pages)} unique processed page IDs from existing {self.output_jsonl_path}")
            except Exception as e:
                logging.warning(f"Could not load or process existing JSONL file {self.output_jsonl_path}: {e}")
        
        if self.processed_pages:
            logging.info(f"Will skip generating pairs for {len(self.processed_pages)} already processed page IDs.")

        if self.args and self.args.limit > 0:
            self.expected_total = min(self.args.limit, self.total_pages - len(self.processed_pages))
            logging.info(f"Limiting to {self.args.limit} new training pairs")
        else:
            self.expected_total = self.total_pages - len(self.processed_pages)
        
        if self.args and self.args.threshold > 0:
            threshold_filter = self.df['similarity_score'] >= self.args.threshold
            filtered_count = threshold_filter.sum()
            logging.info(f"Filtering pages with similarity_score >= {self.args.threshold}: {filtered_count}/{self.total_pages} pages qualify")
            self.df = self.df[threshold_filter].reset_index(drop=True)
            self.total_pages = len(self.df) 
        
        logging.info(f"Ready to generate training pairs from {self.total_pages} pages (after filtering, if any). Effective pages to process: {self.total_pages - len(self.processed_pages)}")
        self.transition_state(AgentState.GENERATING_PAIRS)
    
    def _generating_pairs_state(self):
        if self.df is None or self.current_page_index >= len(self.df):
            logging.info(f"All {self.current_page_index} pages from input dataframe considered. Moving to formatting output.")
            self.transition_state(AgentState.FORMATTING_OUTPUT)
            return
        
        if self.args and self.args.limit > 0 and len(self.training_pairs) >= self.args.limit:
            logging.info(f"Reached limit of {self.args.limit} training pairs for this run. Moving to formatting output.")
            self.transition_state(AgentState.FORMATTING_OUTPUT)
            return
        
        current_page_series = self.df.iloc[self.current_page_index]
        page_id_from_df = current_page_series.get('page_id', self.current_page_index) 
        
        if page_id_from_df in self.processed_pages:
            logging.info(f"Skipping already processed page ID {page_id_from_df} (Index {self.current_page_index})")
            self.current_page_index += 1
            self.transition_state(AgentState.GENERATING_PAIRS) 
            return
        
        doc_title = current_page_series.get('doc_title', 'Untitled')
        
        similar_page_ids = self._find_similar_pages(page_id_from_df)
        similar_pages_list = []
        if similar_page_ids and self.df is not None:
            for similar_id_val in similar_page_ids:
                similar_row = self.df[self.df['page_id'] == similar_id_val]
                if not similar_row.empty:
                    similar_pages_list.append(similar_row.iloc[0])
        
        logging.info(f"Processing page {self.current_page_index + 1}/{len(self.df)}: ID={page_id_from_df}, Title='{doc_title}'")
        if similar_pages_list:
            logging.info(f"Found {len(similar_pages_list)} similar pages to use as additional context")
        
        try:
            page_pairs = self._generate_pairs_from_page(current_page_series, similar_pages_list if similar_pages_list else [])
            if page_pairs:
                self.training_pairs.extend(page_pairs)
                self.total_pairs_generated += len(page_pairs)
                token_count = sum(count_tokens(p.input_text) + count_tokens(p.output_text) for p in page_pairs)
                self.total_tokens_processed += token_count
                logging.info(f"Generated {len(page_pairs)} training pairs from page {page_id_from_df}")
            else:
                logging.warning(f"No training pairs generated from page {page_id_from_df}")
            
            self.processed_pages.add(page_id_from_df) 
            
        except Exception as e:
            logging.error(f"Error generating pairs for page {page_id_from_df}: {str(e)}", exc_info=True)
        
        self.current_page_index += 1
        
        if len(self.training_pairs) % 50 == 0 and self.total_pairs_generated > 0:
            self._log_telemetry()
        
        if self.args and self.args.checkpoint_interval > 0 and len(self.training_pairs) >= (self.checkpoint_count + 1) * self.args.checkpoint_interval:
            self._save_checkpoint()
            self.checkpoint_count += 1
        
        self.transition_state(AgentState.GENERATING_PAIRS)
    
    def _generate_pairs_from_page(self, page: pd.Series, similar_pages: Optional[List[pd.Series]] = None) -> List[TrainingPair]:
        if similar_pages is None:
            similar_pages = []
        page_id_val = page.get('page_id', 0)
        doc_title = page.get('doc_title', 'Untitled')
        page_text_original = page.get('text', '')
        category = page.get('category', 'general')
        subcategory = page.get('subcategory', 'general')
        keywords = page.get('keywords', [])
        description = page.get('description', 'No description available')  
        notes = page.get('notes', 'No additional notes')  
        
        if not page_text_original.strip():
            logging.warning(f"Skipping empty page {page_id_val}")
            return []
        
        formatted_similar_pages_text = format_references(similar_pages)
        
        page_text_for_llm = page_text_original 
        references_for_llm = formatted_similar_pages_text
        
        if isinstance(keywords, list):
            keywords_str = ", ".join(keywords) if keywords else "financial terms"
        elif isinstance(keywords, str):
            try:
                keywords_list = json.loads(keywords)
                keywords_str = ", ".join(keywords_list) if keywords_list else "financial terms"
            except json.JSONDecodeError:
                keywords_str = keywords
        else:
            keywords_str = "financial terms"
    
        if category not in TRAINING_PAIR_TYPES:
            logging.warning(f"Invalid category: {category}. Using default.")
            category = next(iter(TRAINING_PAIR_TYPES.keys()))
        
        if subcategory not in TRAINING_PAIR_TYPES[category]["subcategories"]:
            logging.warning(f"Invalid subcategory: {subcategory} for category {category}. Using default.")
            subcategory = next(iter(TRAINING_PAIR_TYPES[category]["subcategories"].keys()))
        
        subcategory_description = TRAINING_PAIR_TYPES[category]["subcategories"][subcategory]
        
        generator_prompt = GENERATOR_PROMPTS.get(category, GENERATOR_PROMPTS["question_answering"])
        
        formatted_prompt = generator_prompt.format(
            text=page_text_for_llm,
            references=references_for_llm,
            description=description,  
            notes=notes,  
            subcategory_description=subcategory_description,
            keywords=keywords_str
        )
        
        pairs = []
        record_id = f"p{page_id_val}_cat{category}_sub{subcategory}"
        conversation_messages: List[Dict[str, str]] = [{"role": "user", "content": formatted_prompt}]

        try:
            llm_response = self._get_llm_answer(formatted_prompt)
            conversation_messages.append({"role": "assistant", "content": llm_response})
            
            if not self._validate_response_format(llm_response, category):
                logging.warning(f"Response for {category}/{subcategory} doesn't match expected format.")
        
            context_for_input_template = page_text_original
            first_part = None  
            second_part = None  

            if category == "question_answering":
                input_parts = llm_response.split("ANSWER:", 1)
                if len(input_parts) == 2 and "QUESTION:" in input_parts[0]:
                    first_part = input_parts[0].replace("QUESTION:", "").strip()
                    second_part = input_parts[1].strip()
                elif "QUESTION:" in input_parts[0]:
                    first_part = input_parts[0].replace("QUESTION:", "").strip()
        
            elif category == "instruction_following":
                input_parts = llm_response.split("RESULT:", 1)
                if len(input_parts) == 2 and "INSTRUCTION:" in input_parts[0]:
                    first_part = input_parts[0].replace("INSTRUCTION:", "").strip()
                    second_part = input_parts[1].strip()
                elif "INSTRUCTION:" in input_parts[0]:
                    first_part = input_parts[0].replace("INSTRUCTION:", "").strip()
        
            elif category == "financial_product_creation":
                input_parts = llm_response.split("PRODUCT DEFINITION:", 1)
                if len(input_parts) == 2 and "TASK:" in input_parts[0]:
                    first_part = input_parts[0].replace("TASK:", "").strip()
                    second_part = input_parts[1].strip()
                elif "TASK:" in input_parts[0]:
                    first_part = input_parts[0].replace("TASK:", "").strip()
        
            elif category == "regulatory_compliance":
                input_parts = llm_response.split("COMPLIANCE OUTPUT:", 1)
                if len(input_parts) == 2 and "COMPLIANCE TASK:" in input_parts[0]:
                    first_part = input_parts[0].replace("COMPLIANCE TASK:", "").strip()
                    second_part = input_parts[1].strip()
                elif "COMPLIANCE TASK:" in input_parts[0]:
                    first_part = input_parts[0].replace("COMPLIANCE TASK:", "").strip()
        
            if first_part and not second_part:
                logging.info(f"Got only the first part for {category}. Making a follow-up request for the second part.")
                
                if category == "question_answering":
                    followup_prompt = f"Based on the document provided earlier, please answer the following question:\n\nQUESTION: {first_part}\n\nPlease start your response with 'ANSWER:'"
                elif category == "instruction_following":
                    followup_prompt = f"Based on the document provided earlier, please follow this instruction:\n\nINSTRUCTION: {first_part}\n\nPlease start your response with 'RESULT:'"
                elif category == "financial_product_creation":
                    followup_prompt = f"Based on the document provided earlier, please complete this task:\n\nTASK: {first_part}\n\nPlease start your response with 'PRODUCT DEFINITION:'"
                elif category == "regulatory_compliance":
                    followup_prompt = f"Based on the document provided earlier, please complete this compliance task:\n\nCOMPLIANCE TASK: {first_part}\n\nPlease start your response with 'COMPLIANCE OUTPUT:'"
                else:
                    followup_prompt = f"Please complete your response to the previous prompt by providing the second part."
                    
                conversation_messages.append({"role": "user", "content": followup_prompt})
                
                followup_response = self._get_llm_answer(followup_prompt, messages=conversation_messages[:-1])
                conversation_messages.append({"role": "assistant", "content": followup_response})
                
                if category == "question_answering" and "ANSWER:" in followup_response:
                    second_part = followup_response.replace("ANSWER:", "", 1).strip()
                elif category == "instruction_following" and "RESULT:" in followup_response:
                    second_part = followup_response.replace("RESULT:", "", 1).strip()
                elif category == "financial_product_creation" and "PRODUCT DEFINITION:" in followup_response:
                    second_part = followup_response.replace("PRODUCT DEFINITION:", "", 1).strip()
                elif category == "regulatory_compliance" and "COMPLIANCE OUTPUT:" in followup_response:
                    second_part = followup_response.replace("COMPLIANCE OUTPUT:", "", 1).strip()
                else:
                    second_part = followup_response.strip()
            
            if first_part and second_part:
                # Create clean input/output text without context document
                if category == "question_answering":
                    input_text = f"QUESTION: {first_part}"
                    output_text = second_part
                
                elif category == "instruction_following":
                    input_text = f"INSTRUCTION: {first_part}"
                    output_text = second_part
                
                elif category == "financial_product_creation":
                    input_text = f"TASK: {first_part}"
                    output_text = second_part
                
                elif category == "regulatory_compliance":
                    input_text = f"COMPLIANCE TASK: {first_part}"
                    output_text = second_part
                
                else: 
                    input_text = f"QUESTION: Please summarize this document."
                    output_text = llm_response 
                
                pair = TrainingPair(
                    id=len(self.training_pairs) + len(pairs), 
                    input_text=input_text,
                    output_text=output_text,
                    category=category,
                    subcategory=subcategory,
                    page_id=page_id_val,
                    source_doc_title=doc_title,
                    metadata={
                        "token_count_input": count_tokens(input_text),
                        "token_count_output": count_tokens(output_text),
                        "keywords": keywords,
                        "subcategory_description": subcategory_description,
                        "similar_pages": [p.get('page_id') for p in similar_pages] if similar_pages else [],
                        "description": description,
                        "notes": notes
                    }
                )
                pairs.append(pair)
            else:
                logging.warning(f"Could not extract both parts for {category}/{subcategory} from page {page_id_val}. Skipping pair.")
            
            self._save_conversation(conversation_messages, page_id_val, category, subcategory, is_success=bool(pairs))

        except RetryError as e:
            logging.error(f"Failed to get LLM response for page {page_id_val} after multiple retries: {e}")
            conversation_messages.append({"role": "assistant", "content": f"Error: RetryError - {str(e)}"})
            self._save_conversation(conversation_messages, page_id_val, category, subcategory, is_success=False)
        except GenerationError as e:
            logging.error(f"GenerationError for page {page_id_val}: {e}")
            conversation_messages.append({"role": "assistant", "content": f"Error: GenerationError - {str(e)}"})
            self._save_conversation(conversation_messages, page_id_val, category, subcategory, is_success=False)
        except Exception as e:
            logging.error(f"Unexpected error generating pairs for page {page_id_val}: {e}", exc_info=True)
            conversation_messages.append({"role": "assistant", "content": f"Error: {type(e).__name__} - {str(e)}"})
            self._save_conversation(conversation_messages, page_id_val, category, subcategory, is_success=False)
    
        return pairs

    @retry(
        stop=stop_after_attempt(OPENROUTER_RETRY_ATTEMPTS),
        wait=wait_exponential(multiplier=1, min=OPENROUTER_RETRY_WAIT_MIN, max=OPENROUTER_RETRY_WAIT_MAX),
        retry=retry_if_exception_type(Exception), 
        reraise=True
    )
    def _get_llm_answer(self, prompt_text: str, messages: Optional[List[Dict[str, str]]] = None) -> str:
        if not self.llm_client:
            raise GenerationError("LLM client not initialized.")
        
        now = time.time()
        while self.request_timestamps and self.request_timestamps[0] <= now - self.RATE_LIMIT_INTERVAL_SECONDS:
            self.request_timestamps.popleft()
        
        if len(self.request_timestamps) >= self.RATE_LIMIT_MAX_REQUESTS:
            oldest_request_time = self.request_timestamps[0]
            time_to_wait = (oldest_request_time + self.RATE_LIMIT_INTERVAL_SECONDS) - now
            if time_to_wait > 0:
                logging.info(f"Rate limit reached ({self.RATE_LIMIT_MAX_REQUESTS} requests per {self.RATE_LIMIT_INTERVAL_SECONDS}s). Waiting for {time_to_wait:.2f} seconds.")
                time.sleep(time_to_wait)
                now = time.time() 
                while self.request_timestamps and self.request_timestamps[0] <= now - self.RATE_LIMIT_INTERVAL_SECONDS:
                    self.request_timestamps.popleft()

        self.request_timestamps.append(time.time())

        current_attempt = getattr(self._get_llm_answer.retry.statistics, 'attempt_number', 1)
        logging.info(f"LLM generation attempt {current_attempt}/{OPENROUTER_RETRY_ATTEMPTS}")

        if not self.args:
            raise GenerationError("Arguments not initialized for LLM call.")

        chat_messages = messages.copy() if messages else []
        if not chat_messages or chat_messages[-1]["role"] != "user":
            chat_messages.append({"role": "user", "content": prompt_text})

        completion_params = {
            "model": self.args.openrouter_model,
            "messages": chat_messages
        }
        if self.llm_extra_headers:
            completion_params["extra_headers"] = self.llm_extra_headers

        completion = self.llm_client.chat.completions.create(**completion_params)
        answer = completion.choices[0].message.content
        
        prompt_tokens = count_tokens(prompt_text)
        answer_tokens = count_tokens(answer)
        self.total_tokens_processed += (prompt_tokens + answer_tokens)
        
        logging.info(f"LLM generated response (tokens: prompt={prompt_tokens}, answer={answer_tokens})")
        return answer
    
    def _formatting_output_state(self): 
        if not self.training_pairs:
            logging.warning("No training pairs generated to format")
            self.transition_state(AgentState.SAVING_RESULTS)
            return
        
        logging.info(f"Formatting {len(self.training_pairs)} training pairs")
        
        try:
            self.output_data = []
            for pair in self.training_pairs:
                formatted_pair = {
                    "id": pair.id,
                    "input_text": pair.input_text,
                    "output_text": pair.output_text,
                    "category": pair.category,
                    "subcategory": pair.subcategory,
                    "page_id": pair.page_id, 
                    "source_doc_title": pair.source_doc_title,
                    "token_count_input": pair.metadata.get("token_count_input", 0),
                    "token_count_output": pair.metadata.get("token_count_output", 0),
                    "keywords": pair.metadata.get("keywords", []),
                    "subcategory_description": pair.metadata.get("subcategory_description", ""),
                    "similar_pages": pair.metadata.get("similar_pages", []),
                    "generation_timestamp": int(time.time()),
                    "model_used": self.args.openrouter_model if self.args and hasattr(self.args, "openrouter_model") else None
                }
                
                self.output_data.append(formatted_pair)
            
            logging.info(f"Successfully formatted {len(self.output_data)} training pairs")
            self.transition_state(AgentState.SAVING_RESULTS)
        except Exception as e:
            logging.error(f"Output formatting failed: {str(e)}", exc_info=True)
            self.transition_state(AgentState.ERROR, f"Output formatting failed: {str(e)}")
    
    def _saving_results_state(self): 
        logging.info(f"Saving {len(self.training_pairs)} training pairs to output")
        
        try:
            output_df = pd.DataFrame(self.output_data)
            
            if self.output_parquet_path:
                output_df.to_parquet(self.output_parquet_path, index=False)
                logging.info(f"Saved training pairs to {self.output_parquet_path}")
            else:
                logging.error("Output Parquet path not set. Cannot save Parquet file.")

            if self.output_jsonl_path:
                output_df.to_json(self.output_jsonl_path, orient='records', lines=True)
                logging.info(f"Saved training pairs to {self.output_jsonl_path}")
            else:
                logging.error("Output JSONL path not set. Cannot save JSONL file.")
            
            self.transition_state(AgentState.GENERATING_REPORT)
        except Exception as e:
            logging.error(f"Results saving failed: {e}", exc_info=True)
            self.transition_state(AgentState.ERROR, f"Results saving failed: {e}")
    
    def _generating_report_state(self): 
        logging.info("Generating final report")
        try:
            # Placeholder for any final report generation logic
            self.transition_state(AgentState.DONE)
        except Exception as e:
            logging.error(f"Error generating report: {e}", exc_info=True)
            self.transition_state(AgentState.ERROR, f"Report generation failed: {e}")

def parse_args():
    parser = argparse.ArgumentParser(description="Training Pair Generator")
    parser.add_argument("--input-path", "-i", type=str, required=True, help="Path to the input dataset (Parquet format)")
    parser.add_argument("--output-path", "-o", type=str, required=True, help="Path to save the output training pairs")
    parser.add_argument("--embeddings-path", "-e", type=str, required=True, help="Path to the embeddings dataset (Parquet format)")
    parser.add_argument("--openrouter-api-key", type=str, help="API key for OpenRouter")
    parser.add_argument("--openrouter-model", type=str, help="Model name for OpenRouter")
    parser.add_argument("--limit", "-l", type=int, help="Limit the number of training pairs")
    parser.add_argument("--threshold", "-t", type=float, help="Similarity threshold for filtering pages")
    parser.add_argument("--checkpoint-interval", "-c", type=int, help="Interval for saving checkpoints")
    return parser.parse_args()

def main():
    args = parse_args()
    
    generator = TrainingPairGenerator(args.input_path, args.output_path, args.embeddings_path)
    generator.args = args
    
    success = generator.run()
    
    return 0 if success else 1

if __name__ == "__main__":
    main()
