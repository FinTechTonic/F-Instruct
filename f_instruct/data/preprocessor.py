import os
import sys
import time
import signal
import argparse

# Import marker for PDF conversion
try:
    from marker.converters.pdf import PdfConverter
    from marker.models import create_model_dict
    from marker.output import text_from_rendered
    from marker.config.parser import ConfigParser
except ImportError:
    print("marker-pdf not installed. Install with: pip install marker-pdf")
    sys.exit(1)

# Import rich for fancy console output
from rich.console import Console
from rich.panel import Panel

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
    console.print("\n[bold red]Interrupt received. Exiting...[/bold red]")
    sys.exit(1)

# Register signal handler
signal.signal(signal.SIGINT, signal_handler)

def fancy_print(message, style=None, panel=False, border_style=None):
    """Print formatted messages with rich"""
    if panel:
        console.print(Panel.fit(message, border_style=border_style or "blue"))
    else:
        console.print(message, style=style)

def get_marker_config(model=None):
    """Create configuration for marker"""
    config = {
        "output_format": "markdown",
        "format_lines": True,
        "disable_image_extraction": True,
        "NUM_WORKERS": 10
    }
    
    if model:
        config.update({
            "use_llm": True,
            "llm_service": "marker.services.ollama.OllamaService",
            "ollama_model": model,
            "ollama_base_url": "http://localhost:11434"
        })
        
    return config

def convert_file(input_file, output_file, model=None):
    """Convert a single PDF file to markdown"""
    fancy_print(f"[bold blue]Converting[/bold blue]: {input_file}")
    fancy_print(f"[yellow]LLM enhancement: {'Enabled with ' + model if model else 'Disabled'}[/yellow]")
    
    try:
        # Setup marker configuration and converter
        config = get_marker_config(model)
        config_parser = ConfigParser(config)
        
        converter = PdfConverter(
            artifact_dict=create_model_dict(),
            config=config_parser.generate_config_dict(),
            processor_list=config_parser.get_processors(),
            renderer=config_parser.get_renderer(),
            llm_service=config_parser.get_llm_service() if model else None
        )
        
        # Convert and time the process
        start_time = time.time()
        rendered = converter(input_file)
        conversion_time = time.time() - start_time
        
        # Extract and save markdown
        markdown_content, metadata, _ = text_from_rendered(rendered)
        markdown_text = extract_markdown_text(markdown_content)
        
        with open(output_file, "w", encoding="utf-8") as f:
            f.write(markdown_text)
        
        # Display success message with stats
        file_size_kb = os.path.getsize(output_file) / 1024
        fancy_print(f"[bold green]Success[/bold green]: Converted {os.path.basename(input_file)} ({file_size_kb:.2f} KB, {conversion_time:.2f}s)")
        
        return True
    except Exception as e:
        fancy_print(f"[bold red]Error[/bold red]: {str(e)}")
        return False

def extract_markdown_text(markdown_content):
    """Extract markdown text from various content formats"""
    if isinstance(markdown_content, str):
        return markdown_content
    elif isinstance(markdown_content, dict) and "markdown" in markdown_content:
        return markdown_content["markdown"]
    else:
        return str(markdown_content)
    
def ingest_data(input_path, output_dir, model=None):
    """Process PDF files and convert to markdown"""
    # Create output directory
    os.makedirs(output_dir, exist_ok=True)
    
    # Process single file
    if os.path.isfile(input_path):
        if not input_path.lower().endswith('.pdf'):
            fancy_print(f"[bold red]Error[/bold red]: {input_path} is not a PDF file")
            return False
            
        output_file = os.path.join(output_dir, f"{os.path.splitext(os.path.basename(input_path))[0]}.md")
        return convert_file(input_path, output_file, model)
    
    # Process directory of files
    elif os.path.isdir(input_path):
        pdf_files = [os.path.join(input_path, f) for f in os.listdir(input_path) 
                    if f.lower().endswith('.pdf')]
        
        if not pdf_files:
            fancy_print(f"[bold yellow]Warning[/bold yellow]: No PDF files found in {input_path}")
            return False
        
        fancy_print(f"[bold blue]Found {len(pdf_files)} PDF files to process[/bold blue]", panel=True)
        
        # Process files sequentially
        results = []
        for i, pdf_file in enumerate(pdf_files):
            fancy_print(f"[cyan]Processing file {i+1}/{len(pdf_files)}[/cyan]: {os.path.basename(pdf_file)}")
            output_file = os.path.join(output_dir, f"{os.path.splitext(os.path.basename(pdf_file))[0]}.md")
            result = convert_file(pdf_file, output_file, model)
            results.append(result)
        
        # Report results
        success_count = sum(1 for r in results if r)
        total_count = len(results)
        
        status_message = (
            f"[bold green]✓ Successfully processed all {total_count} files[/bold green]" 
            if success_count == total_count else
            f"[bold yellow]⚠ Processed {success_count}/{total_count} files successfully[/bold yellow]"
        )
        fancy_print(status_message, panel=True, border_style="green" if success_count == total_count else "yellow")
        
        return success_count > 0
    
    else:
        fancy_print(f"[bold red]Error[/bold red]: {input_path} does not exist")
        return False

def main():
    """Command line entry point"""
    parser = argparse.ArgumentParser(description="Convert PDF to Markdown")
    parser.add_argument("-i", "--input", required=True, help="Input PDF file or directory")
    parser.add_argument("-o", "--output", required=True, help="Output directory")
    parser.add_argument("-m", "--model", help="Ollama model to use for enhanced extraction (default: phi4)")
    args = parser.parse_args()
    
    # Display banner and info
    print(TONIC_BANNER)
    fancy_print(
        "[bold cyan]F-Instruct Document Converter[/bold cyan]\n"
        "Converting PDF to structured Markdown text",
        panel=True, 
        border_style="cyan"
    )
    
    # Use phi4 as default model when -m is specified without value
    model = args.model if args.model else (None if args.model is None else "phi4")
    
    # Process files and track time
    start_time = time.time()
    try:
        success = ingest_data(args.input, args.output, model)
        elapsed_time = time.time() - start_time
        fancy_print(f"[cyan]Total processing time: {elapsed_time:.2f} seconds[/cyan]")
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        fancy_print("[bold red]Process interrupted by user[/bold red]")
        sys.exit(1)

if __name__ == "__main__":
    main()
