import argparse, json, logging, os, time, traceback
from dataclasses import asdict, dataclass, field
from datetime import timedelta
from typing import Any, Dict, List, Optional, Set
from enum import Enum

import pandas as pd
import tiktoken
from f_instruct.templates.categories import CLASSIFIER_CORRECTION_PROMPT_TEMPLATE, CLASSIFIER_PROMPT_TEMPLATE, TRAINING_PAIR_TYPES
from ollama import Client, AsyncClient
from tenacity import RetryError, retry, retry_if_exception_type, stop_after_attempt, wait_exponential

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(), logging.FileHandler("classifier.log")])

class LLMResponseValidationError(ValueError): pass
class JSONParsingError(ValueError): pass
class FieldValidationError(LLMResponseValidationError):
    def __init__(self, field_name, value, expected_type=None, message=None):
        self.field_name, self.value, self.expected_type = field_name, value, expected_type
        msg = message or f"Field '{field_name}' validation failed: {value}"
        if expected_type: msg += f" (expected type: {expected_type})"
        super().__init__(msg)

@dataclass
class ClassifiedPage:
    record_id: int
    page_id: int
    doc_title: str
    chunk_id: int
    ahead: int
    behind: int
    token_size: int
    text: str
    category: str
    subcategory: str
    similarity_score: float
    description: str = ""
    notes: str = ""
    keywords: List[str] = field(default_factory=list)
    note: str = ""

def count_tokens(text, encoding_name="cl100k_base"):
    encoding = tiktoken.get_encoding(encoding_name)
    return len(encoding.encode(text))

class AgentState(Enum):
    INIT = "INIT"
    LOADING_DATA = "LOADING_DATA"
    PREPARING_CLASSIFICATION = "PREPARING_CLASSIFICATION"
    PROCESSING_PAGE = "PROCESSING_PAGE"
    CLASSIFYING_CATEGORY = "CLASSIFYING_CATEGORY"
    CLASSIFYING_SUBCATEGORY = "CLASSIFYING_SUBCATEGORY"
    STORING_RESULTS = "STORING_RESULTS"
    GENERATING_REPORT = "GENERATING_REPORT"
    ERROR = "ERROR"
    DONE = "DONE"

class AutoClassifierAgent:
    def __init__(self, model: str, input_path: str, output_path: str):
        self.model, self.input_path, self.output_path = model, input_path, output_path
        self.df: Optional[pd.DataFrame] = None
        self.classified_pages: List[ClassifiedPage] = []
        self.ollama_client = Client()
        self.args: Optional[argparse.Namespace] = None
        self.state: AgentState = AgentState.INIT
        self.prev_state: Optional[AgentState] = None
        self.current_page_index, self.current_category_index, self.current_subcategory_index = 0, 0, 0
        self.current_page: Optional[pd.Series] = None
        self.current_category: Optional[str] = None
        self.current_subcategory: Optional[str] = None
        self.current_subcategory_desc: Optional[str] = None
        self.start_time = time.time()
        self.total_pages = self.total_categories = self.total_subcategories = 0
        self.total_classification_attempts = self.successful_classifications = self.failed_classifications = 0
        self.total_tokens_processed = 0
        self.total_classifications_needed = 0
        self.unique_pages_processed: Set[int] = set()
        self.next_record_id = 0
        
        output_dir = os.path.dirname(self.output_path)
        if not output_dir: output_dir = "."
        self.build_dir = os.path.join(output_dir, "build")
        self.checkpoint_dir = os.path.join(output_dir, "checkpoints")
        os.makedirs(self.build_dir, exist_ok=True)
        os.makedirs(self.checkpoint_dir, exist_ok=True)
        logging.info(f"Debug files will be stored in: {self.build_dir}")
        logging.info(f"Checkpoint files will be stored in: {self.checkpoint_dir}")
        
        self.conversation_history = []
        self.checkpoint_count = 0

        self.state_handlers = {
            AgentState.INIT: self._init_state,
            AgentState.LOADING_DATA: self._loading_data_state,
            AgentState.PREPARING_CLASSIFICATION: self._preparing_classification_state,
            AgentState.PROCESSING_PAGE: self._processing_page_state,
            AgentState.CLASSIFYING_CATEGORY: self._classifying_category_state,
            AgentState.CLASSIFYING_SUBCATEGORY: self._classifying_subcategory_state,
            AgentState.STORING_RESULTS: self._storing_results_state,
            AgentState.GENERATING_REPORT: self._generating_report_state,
        }

    def _log_telemetry(self):
        elapsed = time.time() - self.start_time
        tokens_per_sec = self.total_tokens_processed / elapsed if elapsed > 0 else 0
        classifications_per_sec = self.total_classification_attempts / elapsed if elapsed > 0 else 0
        remaining_classifications = self.total_classifications_needed - self.total_classification_attempts
        eta_seconds = remaining_classifications / classifications_per_sec if classifications_per_sec > 0 else float('inf')
        eta_str = str(timedelta(seconds=int(eta_seconds))) if eta_seconds != float('inf') else "N/A"
        percent_complete = (self.total_classification_attempts / self.total_classifications_needed) * 100 if self.total_classifications_needed > 0 else 0
        elapsed_str = str(timedelta(seconds=int(elapsed)))
        logging.info(f"Telemetry: {len(self.classified_pages)} classified pages generated. "
                   + f"Progress: {percent_complete:.1f}% ({self.total_classification_attempts}/{self.total_classifications_needed}). "
                   + f"Success rate: {(self.successful_classifications / self.total_classification_attempts) * 100:.1f}%. "
                   + f"Time: {elapsed_str}, ETA: {eta_str}, "
                   + f"Tokens/sec: {tokens_per_sec:.1f}, Classifications/sec: {classifications_per_sec:.2f}")

    def _create_error_page(self, record_id: int, base_page_data: Dict[str, Any], error: Exception) -> ClassifiedPage:
        error_message, stack_trace = str(error), traceback.format_exc()
        logging.error(f"Failed to classify page {self.current_page_index + 1} as "
                    + f"{self.current_category}/{self.current_subcategory}. Error: {error_message}")
        logging.error(f"Stack trace:\n{stack_trace}")
        logging.error(f"Context:\n- Page ID: {base_page_data['page_id']}\n"
                      f"- Category: {self.current_category}\n- Subcategory: {self.current_subcategory}")
        assert self.current_category is not None, "FSM Error: current_category is None when creating error page"
        assert self.current_subcategory is not None, "FSM Error: current_subcategory is None when creating error page"
        return ClassifiedPage(
            record_id=record_id,
            **base_page_data,
            category=self.current_category, 
            subcategory=self.current_subcategory, 
            similarity_score=-1.0,
            description=f"ERROR: Classification failed for {self.current_category}/{self.current_subcategory}",
            notes=f"Error type: {type(error).__name__}\nMessage: {error_message}\n\nStack trace:\n{stack_trace}",
            keywords=[],
            note=f"Classification failed: {type(error).__name__}")

    def _save_conversation(self, conversation: Dict[str, Any], conversation_id: str, is_success: bool) -> str:
        timestamp = int(time.time())
        status = "success" if is_success else "failed"
        filename = f"{conversation_id}_{status}_{timestamp}.json"
        conversation_filepath = os.path.join(self.build_dir, filename)
        with open(conversation_filepath, 'w', encoding='utf-8') as f:
            json.dump(conversation, f, indent=2)
        log_message = f"Saved {'successful' if is_success else 'failed'} conversation to {conversation_filepath}"
        if is_success: logging.info(log_message)
        else: logging.error(log_message)
        return conversation_filepath

    def _save_dataframe_to_file(self, df: pd.DataFrame, filepath: str, format_type: str) -> None:
        if format_type == 'parquet': df.to_parquet(filepath, index=False)
        elif format_type == 'jsonl': df.to_json(filepath, orient='records', lines=True)
        else:
            logging.warning(f"Unsupported format: {format_type}, defaulting to parquet")
            df.to_parquet(filepath, index=False)
        logging.info(f"Saved data in {format_type.upper()} format to {filepath}")

    def transition_state(self, new_state: AgentState, reason: Optional[str] = None):
        reason_msg = f" Reason: {reason}" if reason else ""
        logging.info(f"FSM State Transition: {self.state.value} -> {new_state.value}.{reason_msg}")
        self.prev_state, self.state = self.state, new_state

    def run(self):
        logging.info(f"Starting AutoClassifierAgent with model: {self.model}")
        logging.info(f"Input path: {self.input_path}")
        logging.info(f"Output path: {self.output_path}")
        self.start_time = time.time()
        self.state = AgentState.INIT
        while self.state != AgentState.DONE and self.state != AgentState.ERROR:
            try:
                handler = self.state_handlers.get(self.state)
                if handler: handler()
                else:
                    logging.info(f"Unhandled state: {self.state.value}")
                    self.transition_state(AgentState.ERROR, f"Unhandled state: {self.state.value}")
            except Exception as e:
                logging.error(f"Error in state {self.state.value}: {str(e)}", exc_info=True)
                self.transition_state(AgentState.ERROR, f"Exception: {str(e)}")
        return self.state != AgentState.ERROR

    def _init_state(self):
        logging.info("Initializing classification process")
        self.transition_state(AgentState.LOADING_DATA)

    def _loading_data_state(self):
        logging.info(f"Loading dataset from {self.input_path}")
        try:
            self.df = pd.read_parquet(self.input_path)
            self.total_pages = len(self.df)
            logging.info(f"Successfully loaded {self.total_pages} pages from dataset")
            self.total_categories = len(TRAINING_PAIR_TYPES)
            self.total_subcategories = sum(len(cat_info["subcategories"]) for cat_info in TRAINING_PAIR_TYPES.values())
            self.total_classifications_needed = self.total_pages * self.total_subcategories
            if self.args and self.args.limit > 0:
                self.total_classifications_needed = min(self.total_classifications_needed, self.args.limit)
                logging.info(f"Limiting to {self.args.limit} total classifications")
            logging.info(f"Total classifications required: {self.total_classifications_needed} ({self.total_pages} pages × {self.total_subcategories} subcategories)")
            self.transition_state(AgentState.PREPARING_CLASSIFICATION)
        except Exception as e:
            logging.error(f"Failed to load dataset: {str(e)}", exc_info=True)
            self.transition_state(AgentState.ERROR, f"Data loading failed: {str(e)}")

    def _preparing_classification_state(self):
        logging.info("Preparing for classification")
        logging.info(f"Classification workload: {self.total_pages} pages × {self.total_categories} categories × "
                   + f"~{self.total_subcategories / self.total_categories:.1f} subcategories/category = {self.total_classifications_needed} total classification tasks")
        self.current_page_index = 0
        self.classified_pages = []
        self.total_classification_attempts = self.successful_classifications = self.failed_classifications = 0
        self.total_tokens_processed = 0
        self.unique_pages_processed = set()
        self.next_record_id = 0
        self.transition_state(AgentState.PROCESSING_PAGE)

    def _processing_page_state(self):
        if self.df is None or self.current_page_index >= len(self.df):
            logging.info(f"All {self.current_page_index} pages processed. Moving to results storage.")
            self.transition_state(AgentState.STORING_RESULTS)
            return
        self.current_page = self.df.iloc[self.current_page_index]
        page_id = self.current_page['page_id'] if 'page_id' in self.current_page else self.current_page_index
        self.unique_pages_processed.add(page_id)
        logging.info(f"Processing page {self.current_page_index + 1}/{len(self.df)}: ID={page_id}, Title='{self.current_page.get('doc_title', 'N/A')}'")
        self.current_category_index = 0
        self.transition_state(AgentState.CLASSIFYING_CATEGORY)

    def _classifying_category_state(self):
        category_keys = list(TRAINING_PAIR_TYPES.keys())
        if self.current_category_index >= len(category_keys):
            logging.info(f"Completed all categories for page {self.current_page_index + 1}")
            self.current_page_index += 1
            self.transition_state(AgentState.PROCESSING_PAGE)
            return
        self.current_category = category_keys[self.current_category_index]
        logging.info(f"Processing category {self.current_category_index + 1}/{len(category_keys)}: {self.current_category}")
        self.current_subcategory_index = 0
        self.transition_state(AgentState.CLASSIFYING_SUBCATEGORY)

    def _classifying_subcategory_state(self):
        if self.args and self.args.limit > 0 and len(self.classified_pages) >= self.args.limit:
            logging.info(f"Reached limit of {self.args.limit} total classifications. Moving to results storage.")
            self.transition_state(AgentState.STORING_RESULTS)
            return
        assert self.current_page is not None, "FSM Error: current_page is None in _classifying_subcategory_state"
        assert self.current_category is not None, "FSM Error: current_category is None in _classifying_subcategory_state"
        category_info = TRAINING_PAIR_TYPES.get(self.current_category)
        if not category_info:
            logging.warning(f"Invalid current_category: '{self.current_category}' not found in TRAINING_PAIR_TYPES.")
            self.current_category_index += 1 
            self.transition_state(AgentState.CLASSIFYING_CATEGORY, reason=f"Invalid category '{self.current_category}'")
            return
        subcategory_keys = list(category_info["subcategories"].keys())
        if self.current_subcategory_index >= len(subcategory_keys):
            logging.info(f"Completed all subcategories for category {self.current_category}")
            self.current_category_index += 1
            self.transition_state(AgentState.CLASSIFYING_CATEGORY)
            return
        self.current_subcategory = subcategory_keys[self.current_subcategory_index]
        self.current_subcategory_desc = category_info["subcategories"].get(self.current_subcategory)
        if self.current_subcategory_desc is None:
            logging.warning(f"Invalid subcategory: '{self.current_subcategory}' not found for category '{self.current_category}'.")
            self.current_subcategory_index += 1 
            self.transition_state(AgentState.CLASSIFYING_SUBCATEGORY, reason=f"Invalid subcategory '{self.current_subcategory}'")
            return
        logging.info(f"Processing subcategory {self.current_subcategory_index + 1}/{len(subcategory_keys)}: "
                   + f"{self.current_subcategory} - {self.current_subcategory_desc}")
        page_full_text = self.current_page.get("text", "")
        prompt = CLASSIFIER_PROMPT_TEMPLATE.format(
            page_id=self.current_page.get('page_id', self.current_page_index),
            chunk_id=self.current_page.get('chunk_id', 0),
            doc_title=self.current_page.get('doc_title', ''),
            text_content=page_full_text,
            category_name=self.current_category, 
            subcategory_name=self.current_subcategory, 
            subcategory_description=self.current_subcategory_desc)
        prompt_truncated = prompt[:500] + "..." if len(prompt) > 500 else prompt
        logging.info(f"Generated prompt (truncated): {prompt_truncated}")
        
        current_record_id = self.next_record_id
        self.next_record_id += 1
        
        self.total_classification_attempts += 1
        prompt_tokens = count_tokens(prompt)
        self.total_tokens_processed += prompt_tokens
        
        base_page_data = {
            "page_id": self.current_page.get("page_id", self.current_page_index),
            "doc_title": self.current_page.get("doc_title", ""),
            "chunk_id": self.current_page.get("chunk_id", 0),
            "ahead": self.current_page.get("ahead", 0),
            "behind": self.current_page.get("behind", 0),
            "token_size": self.current_page.get("token_size", 0),
            "text": page_full_text,
        }
        try:
            assert self.current_subcategory is not None, "FSM Error: current_subcategory is None before LLM call"
            logging.info(f"Invoking LLM for classification task {self.total_classification_attempts}")
            llm_result = self._get_llm_classification(prompt, self.current_category, self.current_subcategory)
            classified_page = ClassifiedPage(
                record_id=current_record_id,
                **base_page_data,
                category=llm_result["category"],
                subcategory=llm_result["subcategory"],
                similarity_score=llm_result["similarity_score"],
                description=llm_result["description"],
                notes=llm_result["notes"],
                keywords=llm_result["keywords"],
                note="Auto-classified successfully")
            self.classified_pages.append(classified_page)
            self.successful_classifications += 1
            logging.info(f"Successfully classified page {self.current_page_index + 1} as "
                       + f"{self.current_category}/{self.current_subcategory} with score {llm_result['similarity_score']:.3f}")
            response_tokens = count_tokens(json.dumps(llm_result))
            self.total_tokens_processed += response_tokens
        except (RetryError, JSONParsingError, LLMResponseValidationError, Exception) as e:
            self.failed_classifications += 1
            error_page = self._create_error_page(current_record_id, base_page_data, e)
            self.classified_pages.append(error_page)
        
        self.current_subcategory_index += 1
        
        if len(self.classified_pages) % 50 == 0 and self.total_classification_attempts > 0:
            self._log_telemetry()
        
        if self.args and self.args.checkpoint_interval > 0 and len(self.classified_pages) >= (self.checkpoint_count + 1) * self.args.checkpoint_interval:
            self._save_checkpoint()
            self.checkpoint_count += 1
        self.transition_state(AgentState.CLASSIFYING_SUBCATEGORY)

    def _save_checkpoint(self):
        logging.info(f"Saving checkpoint with {len(self.classified_pages)} classified pages")
        checkpoint_df = pd.DataFrame([asdict(page) for page in self.classified_pages])
        timestamp = int(time.time())
        checkpoint_filename = f"classified_checkpoint_{timestamp}"
        checkpoint_parquet_path = os.path.join(self.checkpoint_dir, f"{checkpoint_filename}.parquet")
        self._save_dataframe_to_file(checkpoint_df, checkpoint_parquet_path, 'parquet')
        checkpoint_jsonl_path = os.path.join(self.checkpoint_dir, f"{checkpoint_filename}.jsonl")
        self._save_dataframe_to_file(checkpoint_df, checkpoint_jsonl_path, 'jsonl')
        logging.info(f"Checkpoint saved to {checkpoint_parquet_path} and {checkpoint_jsonl_path}")

    def _storing_results_state(self):
        if not self.classified_pages:
            logging.info("No classified pages to store.")
            self.transition_state(AgentState.GENERATING_REPORT)
            return
        logging.info(f"Storing {len(self.classified_pages)} classification results to {self.output_path}")
        try:
            results_df = pd.DataFrame([asdict(page) for page in self.classified_pages])
            results_df['is_error'] = results_df['similarity_score'] < 0
            output_dir = os.path.dirname(self.output_path)
            output_basename = os.path.basename(self.output_path)
            if output_basename == "" or output_basename.startswith("."):
                output_basename = "classified" + (output_basename if output_basename.startswith(".") else "")
            output_base, output_ext = os.path.splitext(output_basename)
            if not output_ext: output_ext = ".parquet"
            format_type = output_ext[1:] if output_ext.startswith('.') else output_ext
            output_path = os.path.join(output_dir, output_base + output_ext)
            self._save_dataframe_to_file(results_df, output_path, format_type)
            jsonl_output_path = os.path.join(output_dir, output_base + ".jsonl")
            self._save_dataframe_to_file(results_df, jsonl_output_path, 'jsonl')
            error_records = results_df[results_df['is_error']]
            if not error_records.empty:
                error_output_path = os.path.join(output_dir, output_base + "_errors" + output_ext)
                self._save_dataframe_to_file(error_records, error_output_path, format_type)
                logging.info(f"Saved {len(error_records)} error records to {error_output_path}")
            self.transition_state(AgentState.GENERATING_REPORT)
        except Exception as e:
            logging.error(f"Failed to store results: {e}", exc_info=True)
            self.transition_state(AgentState.ERROR, f"Results storage failed: {e}")

    def _generating_report_state(self):
        logging.info("Classification complete")
        runtime_seconds = time.time() - self.start_time
        runtime_mins = runtime_seconds / 60
        tokens_per_sec = self.total_tokens_processed / runtime_seconds if runtime_seconds > 0 else 0
        classifications_per_sec = self.total_classification_attempts / runtime_seconds if runtime_seconds > 0 else 0
        runtime_str = str(timedelta(seconds=int(runtime_seconds)))
        logging.info("Classification Report\n====================")
        logging.info(f"Model: {self.model}")
        logging.info(f"Input: {self.input_path}")
        logging.info(f"Output: {self.output_path}")
        logging.info("Statistics:")
        logging.info(f"  Total pages processed: {len(self.unique_pages_processed)}")
        logging.info(f"  Total classification attempts: {self.total_classification_attempts}")
        logging.info(f"  Successful classifications: {self.successful_classifications}")
        logging.info(f"  Failed classifications: {self.failed_classifications}")
        success_rate = (self.successful_classifications / self.total_classification_attempts) * 100 if self.total_classification_attempts > 0 else 0
        logging.info(f"  Success rate: {success_rate:.1f}%")
        logging.info(f"  Total tokens processed: {self.total_tokens_processed}")
        logging.info(f"  Average tokens per second: {tokens_per_sec:.1f}")
        logging.info(f"  Average classifications per second: {classifications_per_sec:.2f}")
        logging.info(f"  Total runtime: {runtime_str} ({runtime_seconds:.1f}s, {runtime_mins:.1f}min)")
        if self.classified_pages:
            results_df = pd.DataFrame([asdict(page) for page in self.classified_pages])
            results_df['is_error'] = results_df['similarity_score'] < 0
            valid_results = results_df[~results_df['is_error']]
            if not valid_results.empty:
                score_by_category = valid_results.groupby('category')['similarity_score'].agg(
                    ['count', 'mean', 'min', 'max']).sort_values(by="count", ascending=False)
                logging.info(f"Score distribution by category:\n{score_by_category}")
                score_by_subcategory = valid_results.groupby(['category', 'subcategory'])['similarity_score'].agg(
                    ['count', 'mean', 'min', 'max']).sort_values(by="count", ascending=False)
                logging.info(f"Top classification subcategories by count:\n{score_by_subcategory.head(10)}")
            error_dist = results_df[results_df['is_error']].groupby(['category', 'subcategory']).size().reset_index(name='error_count')
            if not error_dist.empty:
                error_dist = error_dist.sort_values('error_count', ascending=False)
                logging.info(f"Error counts by category-subcategory:\n{error_dist.head(10)}")
        self.transition_state(AgentState.DONE)

    def _validate_json_structure(self, data: Dict[str, Any], category: str, subcategory: str) -> Dict[str, Any]:
        required_fields = {"category", "subcategory", "similarity_score", "description", "notes", "keywords"}
        missing_fields = required_fields - set(data.keys())
        if missing_fields: raise FieldValidationError("multiple", None, message=f"Missing required fields: {missing_fields}")
        if data["category"] != category: raise FieldValidationError("category", data["category"], message=f"Expected '{category}', got '{data['category']}'")
        if data["subcategory"] != subcategory: raise FieldValidationError("subcategory", data["subcategory"], message=f"Expected '{subcategory}', got '{data['subcategory']}'")
        try:
            score = float(data["similarity_score"])
            if not (0.0 <= score <= 1.0):
                raise FieldValidationError("similarity_score", score, float, message=f"similarity_score must be between 0.0 and 1.0, got {score}")
            data["similarity_score"] = score
        except (ValueError, TypeError):
            raise FieldValidationError("similarity_score", data["similarity_score"], float, message=f"similarity_score must be a float, got {data['similarity_score']}")
        if not isinstance(data["description"], str) or not data["description"].strip():
            raise FieldValidationError("description", data["description"], str, message="description must be a non-empty string")
        if not isinstance(data["notes"], str):
            raise FieldValidationError("notes", data["notes"], str, message="notes must be a string")
        if not isinstance(data["keywords"], list):
            raise FieldValidationError("keywords", data["keywords"], list, message="keywords must be a list")
        for i, keyword in enumerate(data["keywords"]):
            if not isinstance(keyword, str) or not keyword.strip():
                if data["keywords"]:
                    raise FieldValidationError(f"keywords[{i}]", keyword, str, message=f"keywords[{i}] must be a non-empty string if provided")
        return data

    @retry(
        stop=stop_after_attempt(3), 
        wait=wait_exponential(multiplier=1, min=2, max=10),
        retry=retry_if_exception_type((JSONParsingError, LLMResponseValidationError)),
        reraise=True
    )
    def _get_llm_classification(self, initial_prompt_text: str, category: str, subcategory: str) -> Dict[str, Any]:
        current_attempt = getattr(self._get_llm_classification.retry.statistics, 'attempt_number', 1)
        logging.info(f"LLM classification attempt {current_attempt}/3 for {category}/{subcategory}")
        messages = [{"role": "user", "content": initial_prompt_text}]
        conversation_id = f"p{self.current_page_index}_c{self.current_category_index}_s{self.current_subcategory_index}_a{current_attempt}"
        conversation = {"id": conversation_id, "messages": messages.copy()}
        for conversational_turn in range(10):
            logging.info(f"Conversational turn {conversational_turn + 1}/10 for {category}/{subcategory}")
            try:
                response = self.ollama_client.chat(model=self.model, messages=messages, format="json")
                assistant_response_content = response['message']['content']
                response_truncated = assistant_response_content[:500] + "..." if len(assistant_response_content) > 500 else assistant_response_content
                logging.info(f"LLM response (turn {conversational_turn + 1}): {response_truncated}")
                conversation["messages"].append({"role": "assistant", "content": assistant_response_content})
                response_tokens = count_tokens(assistant_response_content)
                self.total_tokens_processed += response_tokens
                try:
                    response_data = json.loads(assistant_response_content)
                except json.JSONDecodeError as e:
                    logging.warning(f"JSON parsing failed (turn {conversational_turn + 1}): {e}")
                    if conversational_turn < 9:
                        correction_request = CLASSIFIER_CORRECTION_PROMPT_TEMPLATE.format(
                            validation_error=f"JSON Parsing Error: {e}. Ensure your entire response is a single, valid JSON object.",
                            previous_json_response=assistant_response_content)
                        messages.append({"role": "assistant", "content": assistant_response_content})
                        messages.append({"role": "user", "content": correction_request})
                        conversation["messages"].append({"role": "user", "content": correction_request})
                        correction_tokens = count_tokens(correction_request)
                        self.total_tokens_processed += correction_tokens
                        time.sleep(1)
                        continue
                    else: raise JSONParsingError(f"Failed to parse JSON after {conversational_turn + 1} turns: {e}")
                try:
                    validated_data = self._validate_json_structure(response_data, category, subcategory)
                    logging.info(f"Response validated successfully after {conversational_turn + 1} turns")
                    self._save_conversation(conversation, conversation_id, True)
                    return validated_data
                except LLMResponseValidationError as e:
                    logging.warning(f"Validation failed (turn {conversational_turn + 1}): {e}")
                    if conversational_turn < 9:
                        correction_prompt = CLASSIFIER_CORRECTION_PROMPT_TEMPLATE.format(
                            validation_error=str(e),
                            previous_json_response=assistant_response_content)
                        messages.append({"role": "assistant", "content": assistant_response_content})
                        messages.append({"role": "user", "content": correction_prompt})
                        conversation["messages"].append({"role": "user", "content": correction_prompt})
                        correction_tokens = count_tokens(correction_prompt)
                        self.total_tokens_processed += correction_tokens
                        time.sleep(1)
                        continue
                    else: raise LLMResponseValidationError(f"Validation failed after {conversational_turn + 1} turns: {e}")
            except Exception as e:
                logging.error(f"API call error (turn {conversational_turn + 1}): {e}", exc_info=True)
                if conversational_turn < 9:
                    recovery_prompt = f"An error occurred: {type(e).__name__}. Please provide a valid JSON response according to the original instructions."
                    messages.append({"role": "user", "content": recovery_prompt})
                    conversation["messages"].append({"role": "user", "content": recovery_prompt})
                    recovery_tokens = count_tokens(recovery_prompt)
                    self.total_tokens_processed += recovery_tokens
                    time.sleep(1)
                    continue
                else: raise
        logging.error(f"Failed to get valid classification after 10 conversational turns")
        self._save_conversation(conversation, conversation_id, False)
        raise LLMResponseValidationError(f"Failed to get valid classification after 10 conversational turns")

def parse_args():
    parser = argparse.ArgumentParser(description="AutoClassifierAgent utilizing LLMs for robust document classification.")
    parser.add_argument("--input", "-i", type=str, required=True, help="Path to the Parquet file with page text")
    parser.add_argument("--output", "-o", type=str, required=True, help="Path to store the classified output")
    parser.add_argument("--model", "-m", type=str, default="llama3:latest", help="Ollama model to use for classification")
    parser.add_argument("--checkpoint-interval", type=int, default=500, help="Save checkpoint after processing this many records (default: 500)")
    parser.add_argument("--limit", "-l", type=int, default=0, help="Maximum number of classifications to perform (default: 0 means no limit)")
    return parser.parse_args()

def main():
    args = parse_args()
    agent = AutoClassifierAgent(model=args.model, input_path=args.input, output_path=args.output)
    agent.args = args
    success = agent.run()
    return 0 if success else 1

if __name__ == "__main__": main()