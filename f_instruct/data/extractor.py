#!/usr/bin/env python3
"""
Module for extracting OpenBank API schema into smaller, more manageable files.
"""

import os
import sys
import json
import argparse
from pathlib import Path
from typing import Dict, List, Any, Optional, Union

from rich.console import Console
from rich.panel import Panel
from rich.progress import Progress, SpinnerColumn, TextColumn, BarColumn, TimeElapsedColumn

# Create console for rich output
console = Console()

class ApiExtractor:
    """Class for extracting and organizing OpenBank API schema into manageable files"""
    
    def __init__(self, source_file: str, output_dir: str, verbose: bool = False):
        """
        Initialize the API extractor
        
        Args:
            source_file: Path to the OpenBank API schema JSON file
            output_dir: Path to the directory where extracted files will be saved
            verbose: Whether to print verbose output
        """
        self.source_file = os.path.abspath(source_file)
        self.output_dir = os.path.abspath(output_dir)
        self.verbose = verbose
        
        # Create output directory if it doesn't exist
        os.makedirs(self.output_dir, exist_ok=True)
        
        # Create API directory within output directory
        self.api_dir = os.path.join(self.output_dir, 'api')
        os.makedirs(self.api_dir, exist_ok=True)
        
        # Create paths and definitions directories
        self.paths_dir = os.path.join(self.api_dir, 'paths')
        os.makedirs(self.paths_dir, exist_ok=True)
        
        self.defs_dir = os.path.join(self.api_dir, 'defs')
        os.makedirs(self.defs_dir, exist_ok=True)
        
        # Initialize statistics
        self.stats = {
            "total_endpoints": 0,
            "total_definitions": 0,
            "api_version": "unknown",
            "extracted_files": {
                "manifest": "openapi_manifest.json",
                "endpoints_directory": "paths/",
                "definitions_directory": "defs/",
                "endpoints_by_tag": "endpoints_by_tag.json"
            }
        }
    
    def log(self, message: str) -> None:
        """Log a message if verbose mode is enabled"""
        if self.verbose:
            console.print(message)
    
    def save_json_file(self, data: Any, filepath: str) -> None:
        """
        Save data as a JSON file
        
        Args:
            data: Data to save
            filepath: Path to save the file to
        """
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        self.log(f"[green]Saved: {filepath}[/green]")
    
    def load_api_schema(self) -> Dict[str, Any]:
        """
        Load the API schema from the source file
        
        Returns:
            The loaded API schema as a dictionary
        """
        try:
            with open(self.source_file, 'r', encoding='utf-8') as f:
                schema = json.load(f)
            return schema
        except Exception as e:
            console.print(f"[bold red]Error loading API schema: {str(e)}[/bold red]")
            sys.exit(1)
    
    def create_manifest(self, schema: Dict[str, Any]) -> Dict[str, Any]:
        """
        Create a manifest file with metadata and HATEOAS links
        
        Args:
            schema: The API schema
        
        Returns:
            The manifest as a dictionary
        """
        manifest = {
            "swagger": schema.get("swagger", "2.0"),
            "info": schema.get("info", {}),
            "host": schema.get("host", ""),
            "basePath": schema.get("basePath", "/"),
            "schemes": schema.get("schemes", ["https"]),
            "securityDefinitions": schema.get("securityDefinitions", {}),
            "security": schema.get("security", []),
            "_links": {
                "paths": {
                    "href": "./paths"
                },
                "definitions": {
                    "href": "./defs"
                }
            }
        }
        
        # Update API version in stats
        self.stats["api_version"] = manifest.get("info", {}).get("version", "unknown")
        
        return manifest
    
    def extract_paths(self, schema: Dict[str, Any]) -> Dict[str, Dict[str, Any]]:
        """
        Extract and save paths from the API schema
        
        Args:
            schema: The API schema
        
        Returns:
            Dictionary containing endpoints categorized by tag
        """
        if 'paths' not in schema:
            console.print("[yellow]No paths found in API schema[/yellow]")
            return {}
        
        paths = schema['paths']
        self.stats["total_endpoints"] = len(paths)
        
        # Save all paths in one file
        all_paths_file = os.path.join(self.api_dir, "all_endpoints.json")
        self.save_json_file(paths, all_paths_file)
        
        # Extract endpoint URLs to a separate file
        endpoint_urls = list(paths.keys())
        endpoint_urls_file = os.path.join(self.api_dir, "endpoint_urls.json")
        self.save_json_file(endpoint_urls, endpoint_urls_file)
        
        # Save each endpoint in a separate file
        with Progress(
            SpinnerColumn(),
            TextColumn("[progress.description]{task.description}"),
            BarColumn(),
            TimeElapsedColumn()
        ) as progress:
            task = progress.add_task("[cyan]Extracting API endpoints...", total=len(paths))
            
            for endpoint, methods in paths.items():
                # Create a valid filename from the endpoint
                safe_name = endpoint.replace('/', '_').replace('{', '').replace('}', '').strip('_')
                if not safe_name:
                    safe_name = "root"
                
                filepath = os.path.join(self.paths_dir, f"{safe_name}.json")
                self.save_json_file(methods, filepath)
                progress.advance(task)
        
        # Generate a categorized endpoints file by tags
        endpoints_by_tag = {}
        
        for endpoint, methods in paths.items():
            for method, details in methods.items():
                if 'tags' in details and details['tags']:
                    for tag in details['tags']:
                        if tag not in endpoints_by_tag:
                            endpoints_by_tag[tag] = []
                        
                        endpoints_by_tag[tag].append({
                            "path": endpoint,
                            "method": method.upper(),
                            "operation_id": details.get('operationId', ''),
                            "summary": details.get('summary', '')
                        })
        
        categorized_file = os.path.join(self.api_dir, "endpoints_by_tag.json")
        self.save_json_file(endpoints_by_tag, categorized_file)
        
        return endpoints_by_tag
    
    def extract_definitions(self, schema: Dict[str, Any]) -> None:
        """
        Extract and save definitions from the API schema
        
        Args:
            schema: The API schema
        """
        if 'definitions' not in schema:
            console.print("[yellow]No definitions found in API schema[/yellow]")
            return
        
        definitions = schema['definitions']
        self.stats["total_definitions"] = len(definitions)
        
        # Save all definitions in one file
        all_definitions_file = os.path.join(self.api_dir, "all_definitions.json")
        self.save_json_file(definitions, all_definitions_file)
        
        # Save each definition in a separate file
        with Progress(
            SpinnerColumn(),
            TextColumn("[progress.description]{task.description}"),
            BarColumn(),
            TimeElapsedColumn()
        ) as progress:
            task = progress.add_task("[cyan]Extracting API definitions...", total=len(definitions))
            
            for def_name, definition in definitions.items():
                filepath = os.path.join(self.defs_dir, f"{def_name}.json")
                self.save_json_file(definition, filepath)
                progress.advance(task)
    
    def extract(self) -> None:
        """Extract the API schema into smaller files"""
        console.print(
            Panel.fit(
                "[bold cyan]OpenBank API Extractor[/bold cyan]\n"
                f"Extracting API schema from {self.source_file}\n"
                f"Saving to {self.api_dir}",
                border_style="cyan"
            )
        )
        
        # Load the API schema
        schema = self.load_api_schema()
        
        # Create and save manifest
        manifest = self.create_manifest(schema)
        manifest_file = os.path.join(self.api_dir, "openapi_manifest.json")
        self.save_json_file(manifest, manifest_file)
        
        # Extract and save paths
        endpoints_by_tag = self.extract_paths(schema)
        
        # Extract and save definitions
        self.extract_definitions(schema)
        
        # Save extraction summary
        summary_file = os.path.join(self.api_dir, "extraction_summary.json")
        self.save_json_file(self.stats, summary_file)
        
        # Print summary
        console.print(
            Panel.fit(
                f"[bold green]Extraction complete![/bold green]\n"
                f"Extracted {self.stats['total_endpoints']} endpoints and {self.stats['total_definitions']} definitions\n"
                f"API version: {self.stats['api_version']}\n"
                f"Manifest: {manifest_file}\n"
                f"Summary: {summary_file}",
                border_style="green"
            )
        )

def extract_openbank_api(source_file: str, output_dir: str, verbose: bool = False) -> None:
    """
    Extract OpenBank API schema into smaller files
    
    Args:
        source_file: Path to the OpenBank API schema JSON file
        output_dir: Path to the directory where extracted files will be saved
        verbose: Whether to print verbose output
    """
    extractor = ApiExtractor(source_file, output_dir, verbose)
    extractor.extract()

def main() -> None:
    """Command line entry point"""
    parser = argparse.ArgumentParser(description="Extract OpenBank API schema into smaller files")
    parser.add_argument("-i", "--input", required=True, help="Input API schema JSON file")
    parser.add_argument("-o", "--output", required=True, help="Output directory")
    parser.add_argument("-v", "--verbose", action="store_true", help="Enable verbose output")
    args = parser.parse_args()
    
    try:
        extract_openbank_api(args.input, args.output, args.verbose)
    except KeyboardInterrupt:
        console.print("[bold red]Process interrupted by user[/bold red]")
        sys.exit(1)
    except Exception as e:
        console.print(f"[bold red]Error: {str(e)}[/bold red]")
        sys.exit(1)

if __name__ == "__main__":
    main()
