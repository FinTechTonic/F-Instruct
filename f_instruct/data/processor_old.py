import os
import re
import signal
import sys
import time
import json
import random
import logging
import argparse
import pandas as pd
import sqlite3
import itertools
from pathlib import Path
from typing import List, Dict, Any, Optional, Tuple, Union
from datetime import datetime
from rich.console import Console
from rich.panel import Panel
from rich.progress import Progress, SpinnerColumn, TextColumn, BarColumn, TaskProgressColumn, TimeElapsedColumn
from rich.table import Table
from collections import Counter, defaultdict

# Import langchain-ollama components
try:
    from langchain_ollama import ChatOllama
    from langchain_core.output_parsers import StrOutputParser
    from langchain_core.prompts import ChatPromptTemplate, PromptTemplate
    from langchain_core.messages import HumanMessage, SystemMessage
except ImportError:
    print("langchain-ollama not installed. Install with: pip install langchain-ollama")
    sys.exit(1)

# Import our prompts dictionary
from ..templates import PROMPTS

# Create console
console = Console()

# ASCII banner for CLI display
TONIC_BANNER = """
         _______  _______  __    _  ___   _______  _______  ___          
        |       ||       ||  |  | ||   | |       ||   _   ||   |         
        |_     _||   _   ||   |_| ||   | |       ||  |_|  ||   |         
          |   |  |  | |  ||       ||   | |       ||       ||   |         
          |   |  |  |_|  ||  _    ||   | |      _||       ||   |         
 _____    |   |  |       || | |   ||   | |     |_ |   _   ||   |  _____  
|_____|   |___|  |_______||_|  |__||___| |_______||__| |__||___| |_____| 
"""

# Global settings
SHUTDOWN_REQUESTED = False

# Training Pair Types - Different categories and subcategories of training data
TRAINING_PAIR_TYPES = {
    "question_answering": {
        "description": "Standard question-answer format for knowledge extraction",
        "subcategories": {
            "factual": "Questions about specific facts stated in the text.",
            "analytical": "Questions requiring analysis or interpretation of information.",
            "application": "Questions about how to apply concepts in practice.", # Renamed from application-based for consistency
            "clarification": "Questions seeking clarity on potentially confusing points.",
            "conceptual": "Questions about broader concepts or principles.",
            "regulatory-reporting": "Questions about EMIR reporting requirements.",
            "transaction-classification": "Questions about categorizing derivatives.",
            "valuation-collateral": "Questions about pricing and margin requirements.",
            "lifecycle-management": "Questions about handling events during derivative's life.",
            "settlement-services": "Questions about contract settlement procedures.",
            "calculation-agency": "Questions about determinations by calculation agent.",
            "data-quality": "Questions about ensuring accurate reporting."
        }
    },
    "instruction_following": {
        "description": "Tasks that require following specific instructions",
        "subcategories": {
            "data_extraction": "Extract specific financial data from provided text",
            "summarization": "Summarize financial documents or regulations",
            "transformation": "Transform financial data from one format to another",
            "code_generation": "Generate code for financial calculations or data processing",
            "outlining": "Create structured outlines of financial documents"
        }
    },
    "financial_product_creation": {
        "description": "Generation of structured financial product definitions",
        "subcategories": {
            "derivative_definition": "Define structured derivatives with specific parameters",
            "option_creation": "Create option contracts with specific terms",
            "swap_definition": "Define swap contracts with specific parameters",
            "bond_structuring": "Structure bonds with specific characteristics",
            "product_term_sheet": "Generate term sheets for financial products"
        }
    },
    "api_interaction": {
        "description": "Tasks related to banking and financial API interactions",
        "subcategories": {
            "query_formulation": "Formulate API queries for banking operations",
            "response_interpretation": "Interpret responses from banking APIs",
            "parameter_selection": "Select appropriate parameters for financial API calls",
            "error_handling": "Handle and interpret errors from financial APIs",
            "data_validation": "Validate data for financial API requests"
        }
    },
    "regulatory_compliance": {
        "description": "Tasks focused on financial regulatory compliance",
        "subcategories": {
            "requirement_identification": "Identify applicable regulatory requirements",
            "compliance_assessment": "Assess compliance with financial regulations",
            "reporting_guidance": "Provide guidance on regulatory reporting",
            "risk_assessment": "Assess regulatory risks in financial activities",
            "documentation": "Generate compliance documentation"
        }
    }
}


def signal_handler(sig, frame):
    """Handle keyboard interrupts and exit gracefully"""
    global SHUTDOWN_REQUESTED
    SHUTDOWN_REQUESTED = True
    console.print("\n[bold red]Interrupt received. Finishing current task and exiting...[/bold red]")

# Register signal handler
signal.signal(signal.SIGINT, signal_handler)

def fancy_print(message, style=None, panel=False, border_style=None):
    """Print formatted messages with rich"""
    if panel:
        console.print(Panel.fit(message, border_style=border_style or "blue"))
    else:
        console.print(message, style=style)

class TrainingPair:
    def __init__(self,
                 pair_type: str,
                 subcategory: str,
                 source_text_preview: str,
                 model_used: str,
                 generation_timestamp: str,
                 pair_specific_data: Dict[str, Any],
                 document_id: Optional[str] = None,
                 chunk_id: Optional[Any] = None,
                 document_title: Optional[str] = None,
                 source_document_name: Optional[str] = None):

        self.pair_type = pair_type
        self.subcategory = subcategory
        self.source_text_preview = source_text_preview
        self.model_used = model_used
        self.generation_timestamp = generation_timestamp
        self.pair_specific_data = pair_specific_data
        self.document_id = document_id
        self.chunk_id = chunk_id
        self.document_title = document_title
        self.source_document_name = source_document_name

    def to_dict(self) -> Dict[str, Any]:
        output_dict = {
            "type": self.pair_type,
            "subcategory": self.subcategory,
            "source_text_preview": self.source_text_preview,
            "model": self.model_used,
            "timestamp": self.generation_timestamp,
            "document_id": self.document_id,
            "chunk_id": self.chunk_id,
            "title": self.document_title,
            "source_name": self.source_document_name,
        }
        output_dict.update(self.pair_specific_data)
        return {k: v for k, v in output_dict.items() if v is not None}

class TrainingPairGenerator:
    """Class to generate various training pairs from financial regulatory text"""
    
    def __init__(self, model_name: str = "phi4", temperature: float = 0.7, 
                 num_predict: int = 2048, ollama_host: str = "http://localhost:11434", 
                 api_schema_path: Optional[str] = None):
        """Initialize the Training Pair Generator"""
        self.model_name = model_name
        self.temperature = temperature
        self.num_predict = num_predict
        self.ollama_host = ollama_host
        self.api_schema_path = api_schema_path
        self.endpoint_usage_count = Counter()
        self.processed_endpoints = set()
        
        # Initialize the ChatOllama model
        try:
            self.llm = ChatOllama(
                model=model_name,
                temperature=temperature,
                base_url=ollama_host,
                num_predict=num_predict  # Using correct param name from API
            )
            console.print(f"[green]Successfully initialized {model_name} model[/green]")
        except Exception as e:
            console.print(f"[bold red]Error initializing Ollama model: {str(e)}[/bold red]")
            console.print("[yellow]Will attempt to continue, but generation may fail.[/yellow]")
    
    def get_next_api_endpoint(self, subcategory: str) -> Dict[str, Any]:
        """Get the next API endpoint to use for example generation with the least usage count"""
        try:
            # Get API directory path
            if self.api_schema_path and os.path.isdir(self.api_schema_path):
                api_dir = self.api_schema_path
            else:
                data_dir = os.path.dirname(os.path.dirname(__file__))
                api_dir = os.path.join(data_dir, "data", "api")
            
            # Load endpoints by tag
            endpoints_by_tag_file = os.path.join(api_dir, "endpoints_by_tag.json")
            if not os.path.exists(endpoints_by_tag_file):
                return {}
                
            with open(endpoints_by_tag_file, 'r', encoding='utf-8') as f:
                categorized_endpoints = json.load(f)
            
            # Get all endpoint paths
            all_endpoints = []
            for tag, endpoints in categorized_endpoints.items():
                all_endpoints.extend(endpoints)
            
            # Prioritize endpoints that haven't been processed yet
            unprocessed_endpoints = [
                ep for ep in all_endpoints 
                if f"{ep['method']}_{ep['path']}" not in self.processed_endpoints
            ]
            
            # Use TRAINING_PAIR_TYPES as source of truth for subcategories and their descriptions
            api_subcategories = TRAINING_PAIR_TYPES.get("api_interaction", {}).get("subcategories", {})
            
            # Build tag mapping dynamically based on subcategory descriptions
            tag_mapping = {}
            for subcat, description in api_subcategories.items():
                # Extract keywords from the description
                keywords = set(re.findall(r'\b\w+\b', description.lower()))
                
                # Common banking/financial API tags to match against
                common_tags = ["Account", "Bank", "Transaction", "Payment", "Transfer", 
                              "Customer", "Card", "API", "PSD2", "Validation", "Error"]
                
                # Find matching tags based on keywords
                matching_tags = []
                for tag in common_tags:
                    if tag.lower() in keywords:
                        matching_tags.append(tag)
                
                # Add default tags if no matches found
                if not matching_tags:
                    if "query" in subcat or "form" in subcat:
                        matching_tags = ["Account", "Bank", "API"]
                    elif "response" in subcat or "interpret" in subcat:
                        matching_tags = ["PSD2", "Account", "Transaction"]
                    elif "parameter" in subcat or "selection" in subcat:
                        matching_tags = ["Bank", "API", "Transaction", "Account"]
                    elif "error" in subcat:
                        matching_tags = ["Error", "Exception", "Bank", "Account"]
                    elif "valid" in subcat or "data" in subcat:
                        matching_tags = ["Validation", "Schema", "Bank", "Account"]
                    else:
                        matching_tags = ["Account", "Bank", "API"]
                
                tag_mapping[subcat] = matching_tags
            
            relevant_tags = tag_mapping.get(subcategory, ["Account", "Bank", "API"])
            candidate_endpoints = []
            
            # If we still have unprocessed endpoints, prioritize those first
            if unprocessed_endpoints:
                # Further filter by relevant tags if possible
                tagged_unprocessed = []
                for tag in relevant_tags:
                    if tag in categorized_endpoints:
                        for ep in categorized_endpoints[tag]:
                            ep_id = f"{ep['method']}_{ep['path']}"
                            if ep_id not in self.processed_endpoints:
                                tagged_unprocessed.append(ep)
                
                # If we found relevant tagged endpoints, use those, otherwise use all unprocessed
                candidate_endpoints = tagged_unprocessed if tagged_unprocessed else unprocessed_endpoints
            else:
                # All endpoints have been processed at least once, now use least used endpoints
                for tag in relevant_tags:
                    if tag in categorized_endpoints:
                        for ep in categorized_endpoints[tag]:
                            ep_id = f"{ep['method']}_{ep['path']}"
                            count = self.endpoint_usage_count.get(ep_id, 0)
                            # Only consider endpoints used fewer than 3 times
                            if count < 3:
                                candidate_endpoints.append(ep)
                
                # If no endpoints found with relevant tags, get the least used endpoints
                if not candidate_endpoints:
                    # Get all endpoints sorted by usage count
                    all_endpoints_with_count = [
                        (ep, self.endpoint_usage_count.get(f"{ep['method']}_{ep['path']}", 0))
                        for ep in all_endpoints
                    ]
                    all_endpoints_with_count.sort(key=lambda x: x[1])  # Sort by usage count
                    
                    # Take endpoints with the lowest usage count
                    min_count = all_endpoints_with_count[0][1] if all_endpoints_with_count else 0
                    candidate_endpoints = [
                        ep for ep, count in all_endpoints_with_count if count == min_count
                    ]
            
            # If we have candidates, select one
            if candidate_endpoints:
                selected = random.choice(candidate_endpoints)
                endpoint_id = f"{selected['method']}_{selected['path']}"
                
                # Update usage tracking
                self.endpoint_usage_count[endpoint_id] += 1
                self.processed_endpoints.add(endpoint_id)
                
                # Load full endpoint details
                path_key = selected['path'].replace('/', '_').replace('{', '').replace('}', '').strip('_')
                if not path_key:
                    path_key = "root"
                
                path_file = os.path.join(api_dir, "paths", f"{path_key}.json")
                if os.path.exists(path_file):
                    with open(path_file, 'r', encoding='utf-8') as f:
                        path_details = json.load(f)
                        if selected['method'].lower() in path_details:
                            details = path_details[selected['method'].lower()]
                            
                            # Also try to load related definitions
                            param_schemas = []
                            if 'parameters' in details:
                                for param in details.get('parameters', []):
                                    if 'schema' in param and '$ref' in param['schema']:
                                        schema_ref = param['schema']['$ref']
                                        if schema_ref.startswith('#/definitions/'):
                                            def_name = schema_ref.split('/')[-1]
                                            def_file = os.path.join(api_dir, "defs", f"{def_name}.json")
                                            if os.path.exists(def_file):
                                                with open(def_file, 'r', encoding='utf-8') as f:
                                                    param_schemas.append({
                                                        "name": def_name,
                                                        "schema": json.load(f)
                                                    })
                        
                        # Construct the endpoint info with related definitions
                        endpoint_info = {
                            "path": selected['path'],
                            "method": selected['method'].upper(),
                            "tags": details.get("tags", []),
                            "summary": details.get("summary", ""),
                            "description": details.get("description", ""),
                            "operation_id": details.get("operationId", ""),
                            "parameters": details.get("parameters", []),
                            "responses": details.get("responses", {}),
                            "definitions": param_schemas if param_schemas else []
                        }
                        return endpoint_info
        except Exception as e:
            console.print(f"[yellow]Error loading API endpoint: {str(e)}[/yellow]")
        
        # Fallback to empty dict if we couldn't find a suitable endpoint
        return {}
    
    def generate_compliance_pairs(self, chunk_text: str, subcategory: str, 
                               num_pairs: int = 2) -> List[TrainingPair]:
        """Generate regulatory compliance training pairs"""
        generated_pairs = []
        
        with Progress(
            SpinnerColumn(),
            TextColumn("[progress.description]{task.description}"),
            BarColumn(bar_width=40),
            TaskProgressColumn(),
            TimeElapsedColumn(),
        ) as progress:
            task = progress.add_task(f"[cyan]Generating compliance pairs ({subcategory})...", total=num_pairs)
            
            for i in range(num_pairs):
                if SHUTDOWN_REQUESTED:
                    break
                
                try:
                    # Generate scenario using ChatPromptTemplate
                    system_prompt = PROMPTS["regulatory_compliance"]["scenario_generation"].get("system", 
                                   "You are an expert in financial regulations and compliance.")
                    user_prompt = PROMPTS["regulatory_compliance"]["scenario_generation"]["user"]
                    
                    scenario_prompt = ChatPromptTemplate.from_messages([
                        ("system", system_prompt),
                        ("human", user_prompt)
                    ])
                    
                    scenario_chain = scenario_prompt | self.llm | StrOutputParser()
                    scenario = scenario_chain.invoke({
                        "text": chunk_text,
                        "subcategory": subcategory
                    })
                    
                    progress.update(task, description=f"[cyan]Generating compliance solution {i+1}/{num_pairs}[/cyan]")
                    
                    # Generate solution using ChatPromptTemplate
                    system_prompt = PROMPTS["regulatory_compliance"]["solution_generation"].get("system", 
                                   "You are an expert in financial regulations and compliance.")
                    user_prompt = PROMPTS["regulatory_compliance"]["solution_generation"]["user"]
                    
                    solution_prompt = ChatPromptTemplate.from_messages([
                        ("system", system_prompt),
                        ("human", user_prompt)
                    ])
                    
                    solution_chain = solution_prompt | self.llm | StrOutputParser()
                    compliance_solution = solution_chain.invoke({
                        "text": chunk_text,
                        "scenario": scenario,
                        "subcategory": subcategory
                    })
                    
                    if compliance_solution:
                        pair_data = {
                            "scenario": scenario,
                            "solution": compliance_solution
                        }
                        training_pair = TrainingPair(
                            pair_type="regulatory_compliance",
                            subcategory=subcategory,
                            pair_specific_data=pair_data,
                            source_text_preview=chunk_text[:300] + "..." if len(chunk_text) > 300 else chunk_text,
                            model_used=self.model_name,
                            generation_timestamp=datetime.now().isoformat()
                        )
                        generated_pairs.append(training_pair)
                        
                except Exception as e:
                    console.print(f"[red]Error generating compliance pair: {str(e)}[/red]")
                
                progress.advance(task)
        
        return generated_pairs

class FinancialDataProcessor:
    """Process financial regulatory data to generate training examples"""
    
    def __init__(self, input_dir: str, output_dir: str, model_name: str = "phi4", 
                 max_pairs: int = 1000, context_window: int = 10, 
                 api_schema_path: Optional[str] = None, api_limit: Optional[int] = None):
        """Initialize the processor"""
        self.input_dir = input_dir
        self.output_dir = output_dir
        self.model_name = model_name
        self.max_pairs = max_pairs
        self.context_window = context_window
        self.api_schema_path = api_schema_path
        
        # Check if we need to adjust api_limit based on extraction_summary.json
        self.api_limit = 100 if api_limit is None else api_limit
        
        # If api_schema_path is provided, check extraction_summary to ensure api_limit is sufficient
        if api_schema_path and os.path.isdir(api_schema_path):
            summary_path = os.path.join(api_schema_path, "extraction_summary.json")
            if os.path.exists(summary_path):
                try:
                    with open(summary_path, 'r', encoding='utf-8') as f:
                        summary = json.load(f)
                        total_endpoints = summary.get("total_endpoints", 0)
                        # Make sure api_limit is at least as large as total endpoints
                        if total_endpoints > 0 and self.api_limit < total_endpoints:
                            console.print(f"[yellow]Increasing API limit from {self.api_limit} to {total_endpoints} to ensure all endpoints are covered[/yellow]")
                            self.api_limit = total_endpoints
                except Exception as e:
                    console.print(f"[yellow]Could not read extraction_summary.json: {e}[/yellow]")
        
        # Derived attributes
        self.rag_dir = os.path.join(output_dir, "rag_database")
        
        # Create output directories
        os.makedirs(output_dir, exist_ok=True)
        os.makedirs(self.rag_dir, exist_ok=True)
    
    def load_data(self) -> pd.DataFrame:
        """
        Load preprocessed financial data from parquet files in the input directory
        
        Returns:
            Combined DataFrame containing all loaded data
        """
        all_files = os.listdir(self.input_dir)
        parquet_files = [f for f in all_files if f.endswith('.parquet')]
        
        if not parquet_files:
            console.print(f"[bold red]Error: No parquet files found in {self.input_dir}[/bold red]")
            return pd.DataFrame()  # Return empty DataFrame
        
        # Load all parquet files and concatenate into a single DataFrame
        data_frames = []
        for file in parquet_files:
            file_path = os.path.join(self.input_dir, file)
            try:
                df = pd.read_parquet(file_path)
                data_frames.append(df)
                console.print(f"[green]Loaded {len(df)} rows from {file}[/green]")
            except Exception as e:
                console.print(f"[bold red]Error loading {file}: {str(e)}[/bold red]")
        
        if not data_frames:
            console.print(f"[bold red]Error: No data frames were created from parquet files[/bold red]")
            return pd.DataFrame()
        
        combined_data = pd.concat(data_frames, ignore_index=True)
        console.print(f"[green]Successfully loaded {len(combined_data)} rows from {len(parquet_files)} files[/green]")
        return combined_data
    
    def get_contextual_chunks(self, data_df: pd.DataFrame, chunk_id: int, 
                         lookahead: Optional[int] = None, lookbehind: Optional[int] = None, 
                         max_tokens_estimate: int = 12000) -> List[Dict[str, Any]]:
        """
        Get a chunk with contextual information (lookahead and lookbehind)
        
        Args:
            data_df: DataFrame containing all chunks
            chunk_id: ID of the central chunk to retrieve context for
            lookahead: Number of chunks to include after the current chunk
            lookbehind: Number of chunks to include before the current chunk
            max_tokens_estimate: Maximum estimated tokens to include (rough estimate)
            
        Returns:
            List of chunk dictionaries with metadata, in sequential order
        """
        # Use class default if not specified
        if lookahead is None:
            lookahead = self.context_window
        if lookbehind is None:
            lookbehind = self.context_window
            
        # Find the row with the specified chunk_id
        target_row = data_df[data_df['chunk_id'] == chunk_id]
        
        if target_row.empty:
            console.print(f"[bold red]Error: Chunk ID {chunk_id} not found[/bold red]")
            return []
        
        # Get document_id to ensure we only retrieve chunks from the same document
        document_id = target_row.iloc[0]['document_id']
        
        # Get all chunks from the same document
        doc_chunks = data_df[data_df['document_id'] == document_id].sort_values('chunk_index')
        
        # Find current chunk's index in the document
        current_idx = doc_chunks[doc_chunks['chunk_id'] == chunk_id].index[0]
        doc_idx = doc_chunks.index.tolist()
        current_position = doc_idx.index(current_idx)
        
        # Calculate range of chunks to include
        start_position = max(0, current_position - lookbehind)
        end_position = min(len(doc_idx) - 1, current_position + lookahead)
        
        # Get chunk IDs in the range
        context_indices = doc_idx[start_position:end_position + 1]
        context_chunks = doc_chunks.loc[context_indices]
        
        # Convert to list of dictionaries
        result_chunks = []
        total_text_length = 0
        
        # Rough estimate: 4 chars ≈ 1 token
        chars_per_token = 4
        max_chars = max_tokens_estimate * chars_per_token
        
        # Add the central chunk first (most important)
        central_row = context_chunks[context_chunks['chunk_id'] == chunk_id].iloc[0]
        central_chunk = {
            'chunk_id': central_row['chunk_id'],
            'document_id': central_row['document_id'],
            'text': central_row['text'],
            'is_central_chunk': True,
            'position': 0,  # Central position
            'title': central_row.get('title', ''),
            'source_name': central_row.get('source_name', ''),
            'relative_position': central_row.get('relative_position', 0.5)
        }
        result_chunks.append(central_chunk)
        total_text_length += len(central_row['text'])
        
        # Add chunks before and after in the correct sequence
        before_indices = context_indices[:context_indices.index(current_idx)]
        after_indices = context_indices[context_indices.index(current_idx) + 1:]
        
        # Add chunks before the central chunk
        for i, idx in enumerate(reversed(before_indices)):
            if total_text_length >= max_chars:
                break
                
            row = doc_chunks.loc[idx]
            chunk = {
                'chunk_id': row['chunk_id'],
                'document_id': row['document_id'],
                'text': row['text'],
                'is_central_chunk': False,
                'position': -(i + 1),  # Negative position for chunks before
                'title': row.get('title', ''),
                'source_name': row.get('source_name', ''),
                'relative_position': row.get('relative_position', 0.0)
            }
            result_chunks.insert(0, chunk)  # Insert at beginning to maintain sequence
            total_text_length += len(row['text'])
        
        # Add chunks after the central chunk
        for i, idx in enumerate(after_indices):
            if total_text_length >= max_chars:
                break
                
            row = doc_chunks.loc[idx]
            chunk = {
                'chunk_id': row['chunk_id'],
                'document_id': row['document_id'],
                'text': row['text'],
                'is_central_chunk': False,
                'position': i + 1,  # Positive position for chunks after
                'title': row.get('title', ''),
                'source_name': row.get('source_name', ''),
                'relative_position': row.get('relative_position', 1.0)
            }
            result_chunks.append(chunk)  # Append to end to maintain sequence
            total_text_length += len(row['text'])
        
        return result_chunks

    def calculate_pair_distribution(self) -> Dict[str, Dict[str, int]]:
        """
        Calculate how many pairs to generate for each category and subcategory
        based on the maximum pairs limit
        
        Returns:
            A nested dictionary with counts for each category and subcategory
        """
        # Count total number of categories and subcategories
        total_subcategories = 0
        for category in TRAINING_PAIR_TYPES:
            total_subcategories += len(TRAINING_PAIR_TYPES[category]["subcategories"])
        
        # Allocate pairs per subcategory (rounded up to ensure we hit the max)
        base_pairs_per_subcategory = max(1, self.max_pairs // total_subcategories)
        
        # Distribute pairs across categories and subcategories
        distribution = {}
        for category, details in TRAINING_PAIR_TYPES.items():
            subcategories = details["subcategories"]
            distribution[category] = {}
            
            # Special handling for API interaction category if api_limit is specified
            if category == "api_interaction" and self.api_limit is not None:
                # Count API subcategory
                api_subcategory_count = len(subcategories)
                
                # Distribute API limit evenly across API subcategories
                api_pairs_per_subcategory = max(1, self.api_limit // api_subcategory_count)
                
                for subcategory in subcategories:
                    distribution[category][subcategory] = api_pairs_per_subcategory
            else:
                # Standard distribution for other categories
                for subcategory in subcategories:
                    distribution[category][subcategory] = base_pairs_per_subcategory
        
        # Adjust API examples to ensure endpoint coverage
        if self.api_schema_path and os.path.isdir(self.api_schema_path):
            try:
                # Count total endpoints from endpoints_by_tag.json
                endpoints_by_tag_file = os.path.join(self.api_schema_path, "endpoints_by_tag.json")
                if os.path.exists(endpoints_by_tag_file):
                    with open(endpoints_by_tag_file, 'r', encoding='utf-8') as f:
                        categorized_endpoints = json.load(f)
                        
                    # Count unique endpoints
                    unique_endpoints = set()
                    for tag, endpoints in categorized_endpoints.items():
                        for ep in endpoints:
                            unique_endpoints.add(f"{ep['method']}_{ep['path']}")
                    
                    total_endpoints = len(unique_endpoints)
                    
                    if total_endpoints > 0 and "api_interaction" in distribution:
                        # Ensure we have at least one example per endpoint
                        api_subcategories = list(distribution["api_interaction"].keys())
                        subcategory_count = len(api_subcategories)
                        
                        # Distribute endpoints across subcategories
                        endpoints_per_subcategory = max(1, total_endpoints // subcategory_count)
                        
                        # Ensure the total API examples is at least as many as endpoints
                        min_api_examples = total_endpoints + subcategory_count  # Add some buffer
                        
                        # If our api_limit is less than needed for full coverage, increase it
                        if self.api_limit is not None and self.api_limit < min_api_examples:
                            console.print(f"[yellow]Increasing API limit from {self.api_limit} to {min_api_examples} to ensure endpoint coverage[/yellow]")
                            self.api_limit = min_api_examples
                        
                        # Recalculate distribution for API subcategories
                        if self.api_limit is not None and subcategory_count > 0:
                            api_pairs_per_subcategory = max(endpoints_per_subcategory, self.api_limit // subcategory_count)
                            
                            for subcategory in api_subcategories:
                                distribution["api_interaction"][subcategory] = api_pairs_per_subcategory
                                
                            console.print(f"[green]API distribution: {api_pairs_per_subcategory} examples per subcategory to cover {total_endpoints} endpoints[/green]")
            except Exception as e:
                console.print(f"[yellow]Error calculating API distribution: {str(e)}[/yellow]")
        
        return distribution

    def export_rag_database(self, rag_dir: str) -> None:
        """
        Export preprocessed data to a simple RAG database format using parquet files
        
        Args:
            rag_dir: Directory to save the RAG database files
        """
        console.print(f"[cyan]Exporting RAG database to {rag_dir}...[/cyan]")
        
        try:
            # Load data
            data_df = self.load_data()
            
            if data_df.empty:
                console.print("[bold red]Error: No data found to export to RAG database[/bold red]")
                return
            
            # Create RAG directory
            os.makedirs(rag_dir, exist_ok=True)
            
            # Add some metadata columns for better searching
            data_df['text_length'] = data_df['text'].apply(len)
            data_df['word_count'] = data_df['text'].apply(lambda x: len(x.split()))
            
            # Export as parquet
            rag_parquet_file = os.path.join(rag_dir, "financial_rag_database.parquet")
            data_df.to_parquet(rag_parquet_file, index=False)
            console.print(f"[green]Successfully exported {len(data_df)} documents to {rag_parquet_file}[/green]")
            
            # Generate a summary file
            summary_data = {
                "total_documents": len(data_df),
                "total_sources": data_df['source_name'].nunique(),
                "total_chunks": data_df['chunk_id'].nunique(),
                "total_tokens_estimate": data_df['word_count'].sum() * 1.3,  # rough estimate
                "generated_timestamp": datetime.now().isoformat(),
                "sources": data_df['source_name'].value_counts().to_dict()
            }
            
            # Save summary
            with open(os.path.join(rag_dir, "rag_database_summary.json"), 'w') as f:
                json.dump(summary_data, f, indent=2)
            
            console.print(f"[bold green]RAG database successfully exported to {rag_dir}[/bold green]")
            
        except Exception as e:
            console.print(f"[bold red]Error exporting RAG database: {str(e)}[/bold red]")

    def build_rag_database(self) -> bool:
        """
        Build a RAG database from structured data in parquet files.
        
        This loads the data from parquet files in the input directory
        and creates proper document embeddings for retrieval.
        
        Returns:
            True if the database was built successfully, False otherwise
        """
        try:
            console.print("[cyan]Building RAG database from structured data...[/cyan]")
            
            # Load the data from parquet files
            data_df = self.load_data()
            
            if data_df.empty:
                console.print("[bold red]Error: No data found to build RAG database[/bold red]")
                return False
            
            # Create RAG directory if it doesn't exist
            os.makedirs(self.rag_dir, exist_ok=True)
            
            # Export data to parquet for simple access
            rag_parquet_file = os.path.join(self.rag_dir, "financial_rag_database.parquet")
            data_df.to_parquet(rag_parquet_file, index=False)
            
            # Generate a summary with document statistics
            summary_data = {
                "total_documents": len(data_df),
                "total_sources": data_df['source_name'].nunique(),
                "total_chunks": data_df['chunk_id'].nunique(),
                "total_tokens_estimate": len(" ".join(data_df['text'].tolist()).split()) * 1.3,
                "generated_timestamp": datetime.now().isoformat(),
                "sources": data_df['source_name'].value_counts().to_dict()
            }
            
            # Save the summary file
            with open(os.path.join(self.rag_dir, "rag_database_summary.json"), 'w') as f:
                json.dump(summary_data, f, indent=2)
            
            console.print(f"[green]Successfully built RAG database with {len(data_df)} documents[/green]")
            console.print(f"[green]Data exported to {rag_parquet_file}[/green]")
            return True
            
        except Exception as e:
            console.print(f"[bold red]Error building RAG database: {str(e)}[/bold red]")
            return False

    def run(self) -> str:
        """
        Run the data processing pipeline
        
        Returns:
            Path to the output file
        """
        # Process data to generate training pairs
        training_pairs = self.process_data()
        
        if not training_pairs:
            console.print("[bold red]Error: No training pairs were generated[/bold red]")
            return ""
        
        # Create output file name with timestamp
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_file = os.path.join(self.output_dir, f"financial_training_pairs_{timestamp}.json")
        
        # Convert training pairs to dictionary and save to JSON
        training_pairs_dict = [pair.to_dict() for pair in training_pairs]
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(training_pairs_dict, f, ensure_ascii=False, indent=2)
        
        console.print(f"[bold green]Successfully saved {len(training_pairs)} training pairs to {output_file}[/bold green]")
        
        # Generate summary
        category_counts = Counter([pair.pair_type for pair in training_pairs])
        subcategory_counts = Counter([(pair.pair_type, pair.subcategory) for pair in training_pairs])
        
        summary_table = Table(title="Training Pairs Summary")
        summary_table.add_column("Category", style="cyan")
        summary_table.add_column("Subcategory", style="blue")
        summary_table.add_column("Count", style="green", justify="right")
        
        for (category, subcategory), count in subcategory_counts.items():
            summary_table.add_row(category, subcategory, str(count))
        
        console.print(summary_table)
        
        return output_file

    def process_data(self) -> List[TrainingPair]:
        """
        Process financial data to generate training pairs
        
        Returns:
            List of generated training pairs
        """
        # Load data
        data_df = self.load_data()
        
        if data_df.empty:
            console.print("[bold red]Error: No data found to process[/bold red]")
            return []
        
        # Calculate pair distribution
        pair_distribution = self.calculate_pair_distribution()
        
        # Track generated pairs
        all_pairs: List[TrainingPair] = []
        
        # Initialize generator
        generator = TrainingPairGenerator(
            model_name=self.model_name,
            api_schema_path=self.api_schema_path
        )
        
        # Generate pairs for each category and subcategory
        fancy_print("[bold blue]Generating training pairs according to distribution:[/bold blue]")
        
        # Display distribution table
        distribution_table = Table(title="Pair Distribution")
        distribution_table.add_column("Category", style="cyan")
        distribution_table.add_column("Subcategory", style="blue")
        distribution_table.add_column("Target Count", style="green")
        
        for category, subcategories in pair_distribution.items():
            for subcategory, count in subcategories.items():
                distribution_table.add_row(category, subcategory, str(count))
        
        console.print(distribution_table)
        
        # Process each category and subcategory
        with Progress(
            SpinnerColumn(),
            TextColumn("[progress.description]{task.description}"),
            BarColumn(),
            TaskProgressColumn(),
            TimeElapsedColumn()
        ) as progress:
            # Select a subset of chunks to work with
            chunk_ids = data_df['chunk_id'].tolist()
            num_chunks = len(chunk_ids)
            
            # If we have too many chunks, select a diverse subset
            if num_chunks > self.max_pairs * 2:
                # Take a diverse sample of chunk IDs
                step = max(1, num_chunks // (self.max_pairs * 2))
                sampled_chunk_ids = chunk_ids[::step]
                random.shuffle(sampled_chunk_ids)  # Randomize the selection
            else:
                sampled_chunk_ids = chunk_ids
                random.shuffle(sampled_chunk_ids)
            
            # Create a task for overall progress
            main_task = progress.add_task("[cyan]Generating training pairs...", total=self.max_pairs)
            
            # Generate pairs for each category and subcategory
            pairs_generated = 0
            for category, subcategories in pair_distribution.items():
                for subcategory, target_count in subcategories.items():
                    category_task = progress.add_task(
                        f"[blue]Category: {category}, Subcategory: {subcategory}[/blue]", 
                        total=target_count
                    )
                    
                    # Generate the requested number of pairs
                    pairs_for_subcategory = []
                    for chunk_id in sampled_chunk_ids:
                        if len(pairs_for_subcategory) >= target_count or SHUTDOWN_REQUESTED:
                            break
                        
                        # Get contextual text for this chunk
                        context_text = self.get_contextual_text(data_df, chunk_id)
                        if not context_text:
                            continue
                        
                        # Check if the chunk is relevant for this category/subcategory
                        if not self.check_relevance(context_text, category, subcategory):
                            continue
                        
                        # Generate pairs for this chunk using compliance generation as fallback
                        # This replaces the non-existent generate_training_pairs method
                        chunk_pairs = generator.generate_compliance_pairs(
                            context_text, 
                            subcategory,
                            num_pairs=1  # Generate just one pair per chunk for diversity
                        )
                        
                        # Add metadata to the pairs
                        chunk_row = data_df[data_df['chunk_id'] == chunk_id].iloc[0]
                        for pair in chunk_pairs:
                            pair.document_id = chunk_row.get('document_id')
                            pair.chunk_id = chunk_id
                            pair.document_title = chunk_row.get('title')
                            pair.source_document_name = chunk_row.get('source_name')
                        
                        pairs_for_subcategory.extend(chunk_pairs)
                        progress.update(category_task, advance=len(chunk_pairs))
                        progress.update(main_task, advance=len(chunk_pairs))
                        pairs_generated += len(chunk_pairs)
                    
                    all_pairs.extend(pairs_for_subcategory)
                    progress.update(category_task, completed=target_count)
            
            progress.update(main_task, completed=pairs_generated)
        
        console.print(f"[bold green]Generated {len(all_pairs)} training pairs in total[/bold green]")
        return all_pairs

    def get_contextual_text(self, data_df, chunk_id):
        """Get combined text from contextual chunks for a specific chunk ID"""
        context_chunks = self.get_contextual_chunks(data_df, chunk_id)
        if not context_chunks:
            return ""
            
        # Combine all texts, keeping track of the central chunk
        combined_text = []
        for chunk in context_chunks:
            if chunk.get('is_central_chunk', False):
                combined_text.append(f"--- MAIN CHUNK ---\n{chunk['text']}\n--- END MAIN CHUNK ---")
            else:
                combined_text.append(chunk['text'])
                
        return "\n\n".join(combined_text)
    
    def check_relevance(self, text, category, subcategory):
        """Check if text is relevant for a specific category/subcategory
        
        Simple placeholder implementation that always returns True.
        In a production system, this would use LLM to check relevance.
        """
        # For simplicity, assume all texts are relevant
        # This avoids the need for the qa_generator attribute
        return True

def check_api_dir(api_path: str) -> bool:
    """
    Check if the API directory is valid and contains necessary files
    
    Args:
        api_path: Path to the API directory
        
    Returns:
        True if the directory is valid, False otherwise
    """
    if not os.path.isdir(api_path):
        fancy_print(f"[bold red]Error: API path '{api_path}' is not a directory[/bold red]")
        return False
        
    required_files = ["extraction_summary.json", "openapi_manifest.json"]
    missing_files = [f for f in required_files if not os.path.exists(os.path.join(api_path, f))]
    
    if missing_files:
        fancy_print(f"[bold red]Error: Missing required API files in {api_path}: {', '.join(missing_files)}[/bold red]")
        fancy_print("[yellow]Please run extract_openbank_api.py first to extract the API schema[/yellow]")
        return False
    
    # Check for paths and defs directories
    paths_dir = os.path.join(api_path, "paths")
    defs_dir = os.path.join(api_path, "defs")
    
    if not os.path.isdir(paths_dir) or not os.path.isdir(defs_dir):
        fancy_print(f"[bold red]Error: Missing required API directories in {api_path}: paths/ and/or defs/[/bold red]")
        fancy_print("[yellow]Please run extract_openbank_api.py first to extract the API schema[/yellow]")
        return False
    
    # Load and verify extraction summary
    try:
        with open(os.path.join(api_path, "extraction_summary.json"), 'r') as f:
            summary = json.load(f)
            if "total_endpoints" not in summary or "total_definitions" not in summary:
                fancy_print("[bold red]Error: Invalid extraction_summary.json file[/bold red]")
                return False
            
            fancy_print(f"[green]API summary loaded: {summary['total_endpoints']} endpoints and {summary['total_definitions']} definitions[/green]")
    except Exception as e:
        fancy_print(f"[bold red]Error reading extraction_summary.json: {str(e)}[/bold red]")
        return False
    
    fancy_print(f"[green]Using OpenBank API from: {api_path}[/green]")
    return True

def process_financial_data(input_dir: str, output_dir: str, model: str, 
                          max_pairs: int = 1000, context_window: int = 10,
                          api_schema_path: Optional[str] = None, api_limit: Optional[int] = None) -> str:
    """
    Process financial regulatory data to generate diverse training examples
    
    Args:
        input_dir: Directory containing preprocessed parquet files
        output_dir: Directory to save output files
        model: Ollama model to use for generation
        max_pairs: Maximum number of training pairs to generate
        context_window: Number of chunks to include in context window (lookbehind/lookahead)
        api_schema_path: Path to the OpenBank API directory with extracted files
        api_limit: Maximum number of API examples to generate
        
    Returns:
        Path to the generated output file
    """
    # Ensure output directories exist
    os.makedirs(output_dir, exist_ok=True)
    
    # Create RAG database directory
    rag_dir = os.path.join(output_dir, "rag_database")
    os.makedirs(rag_dir, exist_ok=True)
    
    # Calculate default API limit based on extraction_summary.json if available
    default_api_limit = 100  # Fallback default
    if api_schema_path and os.path.isdir(api_schema_path):
        summary_path = os.path.join(api_schema_path, "extraction_summary.json")
        if os.path.exists(summary_path):
            try:
                with open(summary_path, 'r', encoding='utf-8') as f:
                    summary = json.load(f)
                    total_endpoints = summary.get("total_endpoints", 0)
                    total_definitions = summary.get("total_definitions", 0)
                    # Set default to cover all endpoints and definitions
                    default_api_limit = max(100, total_endpoints + total_definitions)
                    console.print(f"[cyan]API summary found: {total_endpoints} endpoints and {total_definitions} definitions[/cyan]")
            except Exception as e:
                console.print(f"[yellow]Could not read extraction_summary.json: {e}[/yellow]")
    
    # Initialize processor with calculated default if none provided
    processor = FinancialDataProcessor(
        input_dir=input_dir,
        output_dir=output_dir,
        model_name=model,
        max_pairs=max_pairs,
        context_window=context_window,
        api_schema_path=api_schema_path,
        api_limit=api_limit if api_limit is not None else default_api_limit
    )
    
    # First build the RAG database
    fancy_print("[bold blue]Step 1: Building RAG database from structured data[/bold blue]", panel=True)
    if not processor.build_rag_database():
        return ""
    
    # Then process the data to generate training pairs
    fancy_print("[bold blue]Step 2: Generating training pairs[/bold blue]", panel=True)
    output_file = processor.run()
    
    return output_file


def main():
    """Command line entry point"""
    # Calculate default API limit based on extraction_summary.json
    default_api_limit = 100  # Fallback default
    script_dir = os.path.dirname(os.path.abspath(__file__))
    possible_api_paths = [
        os.path.join(script_dir, '..', '..', 'data', 'api'),  # Relative to script
        os.path.join(os.getcwd(), 'data', 'api')  # Relative to current working directory
    ]
    
    for api_path in possible_api_paths:
        if os.path.isdir(api_path):
            summary_path = os.path.join(api_path, "extraction_summary.json")
            if os.path.exists(summary_path):
                try:
                    with open(summary_path, 'r', encoding='utf-8') as f:
                        summary = json.load(f)
                        total_endpoints = summary.get("total_endpoints", 0)
                        total_definitions = summary.get("total_definitions", 0)
                        # Set default to cover all endpoints and definitions
                        default_api_limit = max(100, total_endpoints + total_definitions)
                        break
                except:
                    pass  # Silently continue if we can't read the file
    
    parser = argparse.ArgumentParser(description="Generate diverse training examples from financial regulatory documents")
    parser.add_argument("-i", "--input", required=True, help="Input directory containing preprocessed parquet files")
    parser.add_argument("-o", "--output", required=True, help="Output directory for generated training examples")
    parser.add_argument("-m", "--model", required=True, help="Ollama model to use (e.g., 'phi4')")
    parser.add_argument("--max-pairs", type=int, default=1000, help="Maximum number of training pairs to generate (default: 1000)")
    parser.add_argument("--context-window", type=int, default=10, help="Number of chunks for context window (default: 10)")
    parser.add_argument("--api", required=True, help="Path to the OpenBank API directory with extracted files")
    parser.add_argument("--api-limit", type=int, default=default_api_limit, 
                        help=f"Maximum number of API examples to generate (default: {default_api_limit}, calculated from API schema)")
    
    args = parser.parse_args()
    
    # Display banner
    print(TONIC_BANNER)
    fancy_print(
        "[bold cyan]F-Instruct Financial Training Data Generator[/bold cyan]\n"
        "Generating diverse training examples from financial regulatory documents",
        panel=True, 
        border_style="cyan"
    )
    
    # Check if the API directory is valid before proceeding
    if not check_api_dir(args.api):
        sys.exit(1)
    
    try:
        output_file = process_financial_data(
            input_dir=args.input,
            output_dir=args.output,
            model=args.model,
            max_pairs=args.max_pairs,
            context_window=args.context_window,
            api_schema_path=args.api,
            api_limit=args.api_limit
        )
        
        if output_file:
            fancy_print(f"[bold green]Process completed successfully. Output saved to {output_file}[/bold green]")
            sys.exit(0)
        else:
            fancy_print("[bold red]Process failed to generate output file.[/bold red]")
            sys.exit(1)
    except KeyboardInterrupt:
        fancy_print("[bold red]Process interrupted by user[/bold red]")
        sys.exit(1)
    except Exception as e:
        fancy_print(f"[bold red]Error: {str(e)}[/bold red]")
        sys.exit(1)