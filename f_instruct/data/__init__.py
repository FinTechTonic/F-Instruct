"""
F-Instruct Data Processing Module

This module provides document ingestion capabilities for the F-Instruct financial agent system.
It converts PDF documents into structured LaTeX format for downstream processing workflows.

The primary function is document ingestion - converting raw PDF documents into a structured
format that can be processed by subsequent stages of the F-Instruct pipeline.

Usage:
    # Command line usage - auto-detects file vs directory
    uv run f_instruct/data/converter.py --input ./documents --output ./processed_data
    
    # Programmatic usage for workflow integration
    from f_instruct.data import ingest_data
    ingest_data("./input_documents", "./processed_data")
"""

from __future__ import annotations

__version__ = "1.0.0"

# Import core functions for workflow integration
from .converter import (
    convert,
    console,
)

__all__ = [
    'ingest_data',
]

# Module metadata
__author__ = "Team Tonic"
__copyright__ = "2025 Team Tonic"

def ingest_data(input_path, output_dir):
    """
    Main document ingestion function for F-Instruct workflow.
    
    Converts PDF documents (single file or directory) into structured LaTeX format
    for processing by subsequent workflow stages.
    
    Args:
        input_path (str): Path to PDF file or directory containing PDF files
        output_dir (str): Output directory for processed documents
    
    Returns:
        bool: True if ingestion successful, False otherwise
        
    Example:
        # Ingest a single document
        ingest_data("financial_report.pdf", "./processed_data")
        
        # Ingest a directory of documents
        ingest_data("./quarterly_reports", "./processed_data")
    """
    try:
        console.print(f"🔄 Starting document ingestion workflow", style="status")
        console.print(f"📁 Input: {input_path}", style="info")
        console.print(f"📁 Output: {output_dir}", style="info")
        
        convert(input_path, output_dir)
        
        console.print(f"✅ Document ingestion completed successfully", style="success")
        console.print(f"📄 Processed documents available in: {output_dir}", style="info")
        return True
        
    except Exception as e:
        console.print(f"❌ Document ingestion failed: {e}", style="danger")
        return False
