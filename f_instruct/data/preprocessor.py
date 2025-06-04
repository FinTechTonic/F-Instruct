import os
import re
import sys
import time
import uuid
import argparse
import signal
import datetime
import pandas as pd

# Import rich for fancy console output
from rich.console import Console
from rich.panel import Panel
from rich.progress import Progress, SpinnerColumn, TextColumn, BarColumn, TimeElapsedColumn

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

def read_markdown_file(file_path):
    """Read markdown file content"""
    with open(file_path, "r", encoding="utf-8") as f:
        return f.read()

def extract_title_from_filename(file_name: str) -> str:
    """Extract a placeholder title from filename"""
    # Simple placeholder logic: strip extension from filename.
    return os.path.splitext(file_name)[0]

def chunk_markdown_text(text):
    """Basic implementation for chunking markdown text by newlines.
    
    This simple implementation:
    - Splits text by one or more newlines (collapsing consecutive newlines).
    - Creates a chunk for each resulting paragraph.
    - Maintains navigation metadata between chunks.
    """
    
    # Split by one or more newlines and filter out empty chunks.
    paragraphs = [p.strip() for p in re.split(r'\n{2,}', text) if p.strip()]
    
    if not paragraphs:
        # If no paragraphs are found (e.g., empty or whitespace-only document), return an empty list.
        return []
    
    # Create chunks with appropriate metadata.
    chunks = []
    current_pos = 0  # Tracks current character position in the original text.
    
    for i, para_text in enumerate(paragraphs):
        # Find actual position in original text (accounting for newlines).
        start_pos = text.find(para_text, current_pos)
        if start_pos == -1:  # Fallback if exact matching fails (e.g., due to normalization).
            start_pos = current_pos
        end_pos = start_pos + len(para_text)
        current_pos = end_pos  # Update for the next iteration.
        
        # Generate paragraph ID.
        para_id = f"p{i}"
        
        # Determine previous and next chunks using temporary index-based IDs.
        # These will be replaced with proper document-scoped chunk IDs later.
        prev_id = f"idx-{i-1}" if i > 0 else None
        next_id = f"idx-{i+1}" if i < len(paragraphs) - 1 else None
        
        chunks.append({
            "text": para_text,
            "paragraph_id": para_id,
            "position": {
                "start_char": start_pos,
                "end_char": end_pos,
                "start_paragraph": i,  # Index of this paragraph in the document.
                "end_paragraph": i     # For simple paragraph chunks, start and end are the same.
            },
            "overlap": {
                "previous_chunk_id": prev_id,  # Temporary ID, will be updated.
                "next_chunk_id": next_id,      # Temporary ID, will be updated.
                "overlap_previous_chars": 0,   # Placeholder for future overlap calculation.
                "overlap_next_chars": 0        # Placeholder for future overlap calculation.
            },
            "sequence_info": {
                "is_first_chunk": i == 0,
                "is_last_chunk": i == len(paragraphs) - 1,
                "total_chunks": len(paragraphs),
                "relative_position": i / max(1, len(paragraphs) - 1) if len(paragraphs) > 1 else 0.5  # 0.0 to 1.0
            }
        })
    
    return chunks

def generate_document_id():
    """Generate a unique document ID similar to arXiv identifiers."""
    
    # Get current year and month in YY.MM format.
    current_date = datetime.datetime.now()
    date_prefix = f"{str(current_date.year)[2:]}{current_date.month:02d}"
    
    # Add a random component for uniqueness.
    random_suffix = str(uuid.uuid4())[:6]
    
    # Format: YYMM.randomstring (e.g., 2401.abcdef)
    return f"{date_prefix}.{random_suffix}"

def generate_classifier_code(title):
    """Generate a 4-letter classifier code from title.
    
    TODO: Implement LLM-based classifier code generation.
    """
    # Simplified to return a default code.
    return "DOCS"

def process_markdown_file(file_path, global_chunk_id):
    """Process a single markdown file and return structured data for its chunks."""
    file_name = os.path.basename(file_path)
    markdown_text = read_markdown_file(file_path)
    
    # Extract title from filename as a placeholder.
    title = extract_title_from_filename(file_name)
    
    # Generate document-level IDs and classifier code.
    document_id = generate_document_id()
    classifier_code = generate_classifier_code(title)
    
    # Chunk the markdown text.
    text_chunks = chunk_markdown_text(markdown_text)
    
    # Prepare data for each chunk.
    chunks_data = []
    num_chunks_in_doc = len(text_chunks)
    
    # Process all chunks in this document
    for i, chunk_details in enumerate(text_chunks):
        # Assign a global chunk ID and increment counter
        current_chunk_id = global_chunk_id
        global_chunk_id += 1
        
        # Determine previous and next chunk IDs (using -1 for null values)
        previous_chunk_id = current_chunk_id - 1 if i > 0 else -1
        next_chunk_id = current_chunk_id + 1 if i < (num_chunks_in_doc - 1) else -1
        
        chunks_data.append({
            "chunk_id": current_chunk_id,  # Simple integer ID, starting from 1
            "document_id": document_id,    # String document identifier
            "chunk_index": i,              # Local index within document
            "text": chunk_details["text"],
            "title": title,
            "source_name": file_name,
            "classifier_code": classifier_code,
            "paragraph_id": chunk_details["paragraph_id"],
            "position_start": chunk_details["position"]["start_char"],
            "position_end": chunk_details["position"]["end_char"],
            "previous_chunk_id": previous_chunk_id,  # Integer or -1
            "next_chunk_id": next_chunk_id,          # Integer or -1
            "is_first_chunk": chunk_details["sequence_info"]["is_first_chunk"],
            "is_last_chunk": chunk_details["sequence_info"]["is_last_chunk"],
            "relative_position": chunk_details["sequence_info"]["relative_position"]
        })
    
    # Return data for all chunks in the processed file and the updated global counter
    return chunks_data, global_chunk_id

def preprocess_data(input_dir, output_dir, model=None):
    """Process Markdown files into structured Parquet datasets.
    
    Args:
        input_dir (str): Directory containing markdown files.
        output_dir (str): Directory to save output parquet files.
        model (str, optional): NLP model name for advanced features. 
                               (Currently a placeholder for future implementation).
    """
    # Initialize.
    start_time = time.time()
    fancy_print(f"[bold blue]Starting preprocessing of Markdown files[/bold blue]")
    fancy_print(f"[yellow]Input directory: {input_dir}[/yellow]")
    fancy_print(f"[yellow]Output directory: {output_dir}[/yellow]")
    
    if model:
        fancy_print(f"[yellow]Model specified: {model}[/yellow]")
    
    # Create output directory if it doesn't exist.
    os.makedirs(output_dir, exist_ok=True)
    
    # Find all markdown files in the input directory.
    markdown_files = [os.path.join(input_dir, f) for f in os.listdir(input_dir) 
                     if f.lower().endswith(('.md', '.markdown'))]
    
    if not markdown_files:
        fancy_print(f"[bold red]Error[/bold red]: No Markdown files found in {input_dir}")
        return {"data": pd.DataFrame()}  # Return an empty DataFrame structure.
    
    fancy_print(f"[bold blue]Found {len(markdown_files)} Markdown files to process[/bold blue]", panel=True)
    
    # Process files
    all_chunks = []
    global_chunk_id = 1  # Start chunk ID counter at 1 (not 0)
    
    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        TextColumn("[progress.percentage]{task.percentage:>3.0f}%"),
        TimeElapsedColumn()
    ) as progress:
        process_task = progress.add_task("[cyan]Processing files...", total=len(markdown_files))
        
        for i, md_file in enumerate(markdown_files):
            if SHUTDOWN_REQUESTED:
                break
                
            file_name = os.path.basename(md_file)
            progress.update(process_task, description=f"[cyan]Processing {i+1}/{len(markdown_files)}: {file_name}[/cyan]")
            
            try:
                # Pass current global_chunk_id and receive back chunks and updated counter
                chunks_data, global_chunk_id = process_markdown_file(md_file, global_chunk_id)
                all_chunks.extend(chunks_data)
                progress.advance(process_task)
            except Exception as e:
                fancy_print(f"[bold red]Error processing {file_name}[/bold red]: {str(e)}")
    
    # Create DataFrame for chunks only.
    data_df = pd.DataFrame(all_chunks)
    
    if data_df.empty:
        fancy_print("[yellow]No data to save. Output Parquet file will be empty or not created.[/yellow]")
    else:
        fancy_print("[bold blue]Saving data to Parquet file...[/bold blue]")
        data_file = os.path.join(output_dir, "data.parquet")
        data_df.to_parquet(data_file, index=False)
    
    # Report results
    elapsed_time = time.time() - start_time
    total_chunks = len(data_df)
    
    summary_message = (
        f"[bold green]✓ Successfully processed {len(markdown_files)} documents into {total_chunks} chunks.[/bold green]\n"
        f"[cyan]Total processing time: {elapsed_time:.2f} seconds.[/cyan]"
    )
    fancy_print(summary_message, panel=True, border_style="green")
    
    # Return only the data DataFrame
    return {
        "data": data_df
    }

def main():
    """Command line entry point."""
    parser = argparse.ArgumentParser(description="Process Markdown to structured Parquet datasets.")
    parser.add_argument("-i", "--input", required=True, help="Input directory containing Markdown files.")
    parser.add_argument("-o", "--output", required=True, help="Output directory for processed data.")
    parser.add_argument("-m", "--model", help="Ollama model to use for advanced features (e.g., 'phi4'). "
                                             "(Currently a placeholder).")
    args = parser.parse_args()
    
    # Display banner and info
    print(TONIC_BANNER)
    fancy_print(
        "[bold cyan]F-Instruct Document Preprocessor[/bold cyan]\n"
        "Converting Markdown to structured Parquet datasets",
        panel=True, 
        border_style="cyan"
    )
    
    # Process files
    try:
        # Simplified model assignment.
        # `args.model` will be None if the argument is not provided.
        model = args.model
        preprocess_data(args.input, args.output, model)
        sys.exit(0)
    except KeyboardInterrupt:
        fancy_print("[bold red]Process interrupted by user[/bold red]")
        sys.exit(1)
    except Exception as e:
        fancy_print(f"[bold red]Error: {str(e)}[/bold red]")
        sys.exit(1)

if __name__ == "__main__":
    main()