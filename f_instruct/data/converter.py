import os
import sys
import argparse

# Import marker for PDF conversion
try:
    from marker.converters.pdf import PdfConverter
    from marker.models import create_model_dict
    from marker.output import text_from_rendered
except ImportError:
    print("marker-pdf not installed. Install with: pip install marker-pdf")
    sys.exit(1)

def convert_file(input_file, output_file):
    """Convert a single PDF file to markdown with minimal fuss"""
    print(f"Converting: {input_file}")
    
    # Create converter
    converter = PdfConverter(artifact_dict=create_model_dict())
    
    # Convert file
    rendered = converter(input_file)
    
    # Extract markdown
    markdown_content, _, _ = text_from_rendered(rendered)
    
    # Get markdown text
    if isinstance(markdown_content, dict) and "markdown" in markdown_content:
        markdown_text = markdown_content["markdown"]
    elif isinstance(markdown_content, str):
        markdown_text = markdown_content
    else:
        markdown_text = str(markdown_content)
    
    # Save to file
    with open(output_file, "w", encoding="utf-8") as f:
        f.write(markdown_text)
    
    print(f"Saved to: {output_file}")
    
def ingest_data(input_path, output_dir, model=None):
    """Process PDF files and convert to markdown"""
    # Create output directory
    os.makedirs(output_dir, exist_ok=True)
    
    # Handle file vs directory
    if os.path.isfile(input_path):
        # Process single file
        if not input_path.lower().endswith('.pdf'):
            print(f"Error: {input_path} is not a PDF file")
            return False
            
        output_file = os.path.join(output_dir, f"{os.path.splitext(os.path.basename(input_path))[0]}.md")
        try:
            convert_file(input_path, output_file)
            return True
        except Exception as e:
            print(f"Error: {str(e)}")
            return False
    
    elif os.path.isdir(input_path):
        # Process all PDF files in directory
        pdf_files = [os.path.join(input_path, f) for f in os.listdir(input_path) 
                    if f.lower().endswith('.pdf')]
        
        if not pdf_files:
            print(f"No PDF files found in {input_path}")
            return False
        
        print(f"Found {len(pdf_files)} PDF files")
        
        # Process files one at a time
        success_count = 0
        for pdf_file in pdf_files:
            output_file = os.path.join(output_dir, f"{os.path.splitext(os.path.basename(pdf_file))[0]}.md")
            try:
                convert_file(pdf_file, output_file)
                success_count += 1
            except Exception as e:
                print(f"Error with {pdf_file}: {str(e)}")
        
        print(f"Processed {success_count}/{len(pdf_files)} files successfully")
        return success_count > 0
    
    else:
        print(f"Error: {input_path} does not exist")
        return False

# Simple CLI
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Convert PDF to Markdown")
    parser.add_argument("-i", "--input", required=True, help="Input PDF file or directory")
    parser.add_argument("-o", "--output", required=True, help="Output directory")
    args = parser.parse_args()
    
    print("PDF to Markdown Converter")
    print("-------------------------")
    
    success = ingest_data(args.input, args.output)
    sys.exit(0 if success else 1)
