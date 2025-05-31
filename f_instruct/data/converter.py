#!/usr/bin/env python3
# pylint: disable=too-many-lines
# pylint: disable=no-member
# pylint: disable=import-outside-toplevel, global-statement
"""
PDF to LaTeX Converter

This script converts PDF files to LaTeX format by extracting text using OCR
and handling non-textual elements as figures.
Uses asyncio and ThreadPoolExecutor for parallel processing.

Usage:
    uv run f_instruct/data/converter.py --input path/to/file.pdf
    uv run f_instruct/data/converter.py --input path/to/directory
    uv run f_instruct/data/converter.py --help
"""

import os
import re
import sys
import asyncio
import tempfile
import concurrent.futures
from typing import Optional, Any

import click
from rich.console import Console
from rich.progress import (
    Progress,
    BarColumn,
    TextColumn,
    TimeRemainingColumn,
    TimeElapsedColumn,
)
from rich.theme import Theme

import easyocr

__version__ = "1.0.0"

# --- Globals that will be initialized later ---
READER: Optional[easyocr.Reader] = None
CV2: Optional[Any] = None
FITZ: Optional[Any] = None
NP: Optional[Any] = None
TORCH: Optional[Any] = None
EASYOCR: Optional[Any] = None
IS_LOADED = False


# --- Secure Path Utilities ---
def safe_join(base, *paths):
    """
    Safely join one or more path components to the base directory.
    Prevents path traversal attacks by resolving and checking the final path.
    """
    base = os.path.abspath(base)
    final_path = os.path.abspath(os.path.join(base, *paths))
    if not final_path.startswith(base):
        raise ValueError(
            "Unsafe path detected (possible path traversal): " + final_path
        )
    return final_path


# --- Dependency Loading Function ---
def _ensure_dependencies_loaded():
    """Loads heavy dependencies and initializes READER if not already done."""
    global READER, CV2, FITZ, NP, TORCH, EASYOCR, IS_LOADED

    # Skip if already loaded
    if IS_LOADED:
        return

    try:
        import cv2 as cv2_module
        import fitz as fitz_module
        import numpy as np_module
        import torch as torch_module
        import easyocr as easyocr_module

        CV2 = cv2_module
        FITZ = fitz_module
        NP = np_module
        TORCH = torch_module
        EASYOCR = easyocr_module

        console.print("Initializing EasyOCR Reader...", style="info")
        READER = EASYOCR.Reader(['en'], gpu=TORCH.cuda.is_available())
        console.print("EasyOCR Reader initialized.", style="success")
        IS_LOADED = True

    except ImportError as e:
        console.print(f"Error importing dependencies: {e}", style="danger")
        console.print("Please ensure all required libraries (OpenCV, PyMuPDF, NumPy, PyTorch, EasyOCR) are installed.", style="warning")
        sys.exit(1)


# --- Console/Progress ---
# Define a custom theme (dark with purple accents)
custom_theme = Theme({
    "info": "dim cyan",
    "warning": "magenta",
    "danger": "bold red",
    "success": "bold green",
    "status": "bold bright_magenta",  # Purple-like for status
    "progress.description": "white",
    "progress.percentage": "bright_magenta",
    "bar.complete": "bright_magenta",
    "bar.finished": "green",
    "bar.pulse": "bright_magenta",
    "progress.remaining": "dim cyan",
    "progress.elapsed": "dim white",
})
console = Console(theme=custom_theme)

# Configure Progress columns
progress_columns = [
    TextColumn("[progress.description]{task.description}", justify="right"),
    BarColumn(
        bar_width=None,
        complete_style="bar.complete",
        finished_style="bar.finished",
        pulse_style="bar.pulse"
    ),
    "[progress.percentage]{task.percentage:>3.1f}%",
    TimeRemainingColumn(),
    TimeElapsedColumn(),
]


# --- Constants ---
def get_temp_dir():
    """Get system temporary directory for intermediate files."""
    return tempfile.gettempdir()

DEFAULT_DATA_FOLDER = os.path.join(get_temp_dir(), "f_instruct_data")
MIN_TEXT_SIZE = 10
HORIZONTAL_POOLING = 25
MAX_WORKERS = min(32, (os.cpu_count() or 1) + 4)


# --- Utility Class ---
class Utils:
    """Static utility methods for various operations."""

    @staticmethod
    def save_pil_images(items, path):
        """Save PIL Image items to folder specified by path."""
        safe_path = safe_join(os.getcwd(), path)
        if not os.path.isdir(safe_path):
            os.makedirs(safe_path, exist_ok=True)
        for idx, item in enumerate(items):
            save_path = safe_join(safe_path, f"{idx}.jpg")
            item.save(save_path)

    @staticmethod
    def pct_white(img):
        """Find percentage of white pixels in img."""
        if CV2 is None or NP is None:
            console.print("Error: OpenCV or NumPy not loaded.", style="danger")
            return 0
        
        white_count = 0
        imsize = 1

        if len(img.shape) == 3:
            b, g, r = CV2.split(img)
            wb, wg, wr = b == 255, g == 255, r == 255
            white_pixels = NP.bitwise_and(wb, NP.bitwise_and(wg, wr))
            white_count = NP.sum(white_pixels)
            imsize = img.size / 3
        elif len(img.shape) == 2:
            white_pixels = img == 255
            white_count = NP.sum(white_pixels)
            imsize = img.size
        return white_count / imsize if imsize > 0 else 0

    @staticmethod
    def remove_duplicate_bboxes(boxes):
        """Remove bounding boxes from a list that start at the same y-coord"""
        new = []
        seen_y = set()
        for box in boxes:
            if box.y not in seen_y:
                new.append(box)
                seen_y.add(box.y)
        return new

    @staticmethod
    def merge_bboxes(lst):
        """Merge overlapping bounding boxes based on vertical position."""
        if not lst:
            return []

        lst.sort(key=lambda box: box.y)

        merged = [lst[0]]
        for curr_box in lst[1:]:
            last_box = merged[-1]
            if curr_box.y < last_box.y + last_box.height:
                last_box.height = (
                    max(last_box.y_bottom, curr_box.y_bottom) - last_box.y
                )
                last_box.y_bottom = last_box.y + last_box.height
            else:
                merged.append(curr_box)

        return merged

    @staticmethod
    def expand_bbox(box, expand_factor):
        """Expand a bounding box by the given factor."""
        if NP is None:
            console.print("Error: NumPy not loaded.", style="danger")
            return box  # Return original box or handle error appropriately
        x, y, w, h = box.x, box.y, box.width, box.height
        expansion = int(min(h, w) * expand_factor)
        x = max(0, x - expansion)
        y = max(0, y - expansion)
        h, w = h + (2 * expansion), w + (2 * expansion)
        return BBox(x, y, w, h)

    @staticmethod
    def get_file_name(path):
        """Extract filename without extension using regex."""
        filename = os.path.basename(path)
        filename = re.sub(r'[^a-zA-Z0-9_\-\.]', '_', filename)
        # Updated regex to handle trailing dots correctly
        match = re.match(r'^(.+?)(\.[^.]+)?$', filename.rstrip('.'))
        if match:
            return match.group(1)
        return filename.rstrip('.')  # Fallback for names without extensions

    @staticmethod
    def escape_special_chars(s):
        """Return string s with LaTeX special characters escaped."""
        if not isinstance(s, str):
            s = str(s)

        # Process character by character instead of using string replace
        result = []
        for char in s:
            if char == '&':
                result.append(r'\&')
            elif char == '%':
                result.append(r'\%')
            elif char == '$':
                result.append(r'\$')
            elif char == '#':
                result.append(r'\#')
            elif char == '_':
                result.append(r'\_')
            elif char == '{':
                result.append(r'\{')
            elif char == '}':
                result.append(r'\}')
            elif char == '~':
                result.append(r'\textasciitilde{}')
            elif char == '^':
                result.append(r'\textasciicircum{}')
            elif char == '\\':
                result.append(r'\textbackslash{}')
            else:
                result.append(char)

        return ''.join(result)

    @staticmethod
    def make_strlist(lst):
        """Make all the items of a lst a string"""
        return [str(i) for i in lst]

    @staticmethod
    async def async_write_all(filename, lst):
        """Asynchronously write all strings in lst to filename"""
        try:
            async with asyncio.Lock():
                with open(filename, 'w', encoding='utf-8') as f:
                    for s in lst:
                        f.write(s)
                        f.write('\n')
            console.print(f"Wrote {len(lst)} strings to {filename}", style="info")
            return True
        except Exception as e:  # pylint: disable=broad-except
            console.print(f"Error writing to {filename}: {e}", style="danger")
            return False

    @staticmethod
    def write_all(filename, lst):
        """Write all strings in lst to filename"""
        with open(filename, 'w', encoding='utf-8') as f:
            for s in lst:
                f.write(s)
                f.write('\n')
        console.print(f"Wrote {len(lst)} strings to {filename}", style="info")

    @staticmethod
    def create_latex_project_structure(base_path, pdf_name):
        """Create a proper LaTeX project directory structure."""
        # If base_path is absolute, use it directly; else join with cwd
        if os.path.isabs(base_path):
            safe_base = base_path
        else:
            safe_base = safe_join(os.getcwd(), base_path)
        safe_pdf_name = re.sub(r'[^a-zA-Z0-9_\-]', '_', pdf_name)
        project_dir = safe_join(safe_base, safe_pdf_name)
        os.makedirs(project_dir, exist_ok=True)

        figures_dir = safe_join(project_dir, "figures")
        os.makedirs(figures_dir, exist_ok=True)

        build_dir = safe_join(project_dir, "build")
        os.makedirs(build_dir, exist_ok=True)

        return {
            "project_dir": project_dir,
            "figures_dir": figures_dir,
            "build_dir": build_dir
        }

    @staticmethod
    async def extract_images_from_pdf(pdf_path, output_dir):
        """Extract complete images from a PDF file asynchronously."""
        if FITZ is None:
            console.print("Error: PyMuPDF (fitz) not loaded.", style="danger")
            return []
        loop = asyncio.get_running_loop()
        safe_output_dir = safe_join(os.getcwd(), output_dir)

        def _extract_images():
            extracted_images = []
            try:
                # Fix: Add null check for FITZ
                if FITZ is None:
                    return []
                doc = FITZ.open(pdf_path)
                img_count = 0

                # Fix: Use range(len(doc)) instead of enumerate(doc)
                for page_num in range(len(doc)):
                    page = doc.load_page(page_num)
                    image_list = page.get_images(full=True)
                    for img_index, img_info in enumerate(image_list):
                        xref = img_info[0]
                        base_image = doc.extract_image(xref)
                        if base_image and base_image["image"]:
                            image_ext = base_image["ext"]
                            image_bytes = base_image["image"]
                            img_filename = (
                                f"embedded_image_{img_count}.{image_ext}"
                            )
                            img_path = safe_join(safe_output_dir, img_filename)
                            with open(img_path, "wb") as img_file:
                                img_file.write(image_bytes)
                            extracted_images.append(img_path)
                            img_count += 1
                doc.close()
                return extracted_images
            except Exception as e:  # pylint: disable=broad-except
                console.print(
                    f"Error extracting images from PDF: {e}",
                    style="danger"
                )
                return []

        with concurrent.futures.ThreadPoolExecutor() as executor:
            return await loop.run_in_executor(executor, _extract_images)


# --- Bounding Box Class ---
class BBox:
    """BBox object representing bounding rectangle (x, y, width, height)"""
    def __init__(self, x, y, width, height):
        self.x = int(x)
        self.y = int(y)
        self.width = int(width)
        self.height = int(height)
        self.y_bottom = self.y + self.height


# --- LaTeX Classes ---
class LatexText:  # Renamed to avoid conflict with rich.text.Text
    """Class representing regular LaTeX text."""
    def __init__(self, text):
        self.text = Utils.escape_special_chars(text)


class Command:
    """Class representing a LaTeX command."""
    # pylint: disable=too-few-public-methods
    def __init__(self, command_name, arguments=None, options=None):
        arguments = arguments or []
        options = options or []

        self.command_name = command_name
        self.arguments = arguments
        self.options = options
        self.text = self._make_text()

    def _make_text(self):
        """Creates command string, formatting command name, args, and options"""
        cmd_name = self.command_name
        args = self.arguments
        opts = self.options

        text = f"\\{cmd_name}"

        arg_text = ""
        for arg in args:
            arg_str = arg.text if hasattr(arg, 'text') else str(arg)
            arg_text += f"{{{arg_str}}}"

        if opts:
            opt_parts = []
            for opt_type, opt_val in opts:
                val_str = (
                    opt_val.text if hasattr(opt_val, 'text') else str(opt_val)
                )
                opt_str = f"{opt_type}={val_str}" if opt_type else val_str
                opt_parts.append(opt_str)

            opt_text = f"[{','.join(opt_parts)}]"
        else:
            opt_text = ""

        if cmd_name == 'begin':
            return text + arg_text + opt_text
        return text + opt_text + arg_text


class Environment:
    """Class representing a LaTeX environment."""
    # pylint: disable=too-few-public-methods
    def __init__(self, body, env_name, options=None):
        options = options or []

        self.env_name = env_name
        self.body = body
        self.options = options
        self.content = self._make_content()

    def _make_content(self):
        """Creates a list of LaTeX objects for the environment content."""
        env = self.env_name
        content = self.body
        opts = self.options

        start = Command('begin', arguments=[env], options=opts)
        end = Command('end', arguments=[env])
        return [start] + content + [end]


# --- PDF Segmentation Functions ---
def segment(img):
    """Input: cv2 image of page. Output: BBox objects for content blocks."""
    if CV2 is None:
        console.print("Error: OpenCV not loaded.", style="danger")
        return []
    img_width = img.shape[1]

    gray = CV2.cvtColor(img, CV2.COLOR_BGR2GRAY)
    img_bw = CV2.adaptiveThreshold(
        gray, 255, CV2.ADAPTIVE_THRESH_MEAN_C,
        CV2.THRESH_BINARY_INV, 11, 5
    )

    k1 = CV2.getStructuringElement(CV2.MORPH_RECT, (3, 3))
    m1 = CV2.morphologyEx(img_bw, CV2.MORPH_GRADIENT, k1)

    k2 = CV2.getStructuringElement(CV2.MORPH_RECT, (HORIZONTAL_POOLING, 5))
    m2 = CV2.morphologyEx(m1, CV2.MORPH_CLOSE, k2)

    k3 = CV2.getStructuringElement(CV2.MORPH_RECT, (5, 5))
    m3 = CV2.dilate(m2, k3, iterations=2)

    contours = CV2.findContours(m3, CV2.RETR_EXTERNAL, CV2.CHAIN_APPROX_SIMPLE)
    contours = contours[0] if len(contours) == 2 else contours[1]

    bboxes = []
    for c in contours:
        bx, by, bw, bh = CV2.boundingRect(c)

        if bh < MIN_TEXT_SIZE:
            continue

        if Utils.pct_white(img[by: by + bh, bx: bx + bw]) >= 1:
            continue

        bboxes.append(BBox(0, by, img_width, bh))

    return sorted(bboxes, key=lambda x: x.y)


def process_bboxes(bboxes):
    """Process list of BBox objects to remove redundancy."""
    bboxes = Utils.remove_duplicate_bboxes(bboxes)
    bboxes = Utils.merge_bboxes(bboxes)

    for i, curr_box in enumerate(bboxes[:-1]):
        next_box = bboxes[i + 1]
        if curr_box.y_bottom > next_box.y:
            new_y = (curr_box.y_bottom + next_box.y) / 2
            curr_box.y_bottom = int(new_y)
            next_box.y = int(new_y)

    return bboxes


def find_content_blocks(img):
    """Find all content blocks in page."""
    return process_bboxes(segment(img))


# --- PDF Processing Classes ---
class Block:
    """Represents a content block within a Page (text only - no figure extraction)."""

    def __init__(self, bbox, parent_page, block_index):
        """Initialize a block with its bounding box, parent page, and index."""
        self.parent_page = parent_page
        self.bbox = bbox
        self.block_index = block_index
        self.block = self._make_block(bbox, parent_page.page_img, parent_page.height)
        self.content_string = self._determine_content()

    def _make_block(self, bbox, page_img=None, height=None):
        """Extract the block image from the page."""
        if NP is None:
            console.print("Error: NumPy not loaded.", style="danger")
            return None  # Indicate error
        if height is None:
            console.print(
                "Warning: Height not provided to _make_block, "
                "potential error.",
                style="warning"
            )
            return NP.zeros((10, 10, 3), dtype=NP.uint8)

        # Fix: Add null check for page_img
        if page_img is None:
            console.print("Error: page_img is None in _make_block", style="danger")
            return NP.zeros((10, 10, 3), dtype=NP.uint8)

        if bbox.y < height and bbox.y_bottom <= height:
            return page_img[bbox.y:bbox.y_bottom, :]
        console.print(
            f"Warning: Invalid bbox coordinates - y:{bbox.y}, "
            f"y_bottom:{bbox.y_bottom}, height:{height}",
            style="warning"
        )
        return NP.zeros((10, 10, 3), dtype=NP.uint8)

    def _determine_content(self):
        """Extract text content using OCR - treat everything as potential text."""
        if READER is None:
            console.print("Error: EasyOCR Reader not initialized.", style="danger")
            return '--OCR Not Available--'
        
        try:
            if (
                self.block is None
                or self.block.size == 0
                or self.block.shape[0] < 5
                or self.block.shape[1] < 5
            ):
                return '--Block too small for OCR--'

            # More aggressive OCR settings for text tables and fancy formatting
            results = READER.readtext(
                self.block, 
                paragraph=True,
                width_ths=0.7,  # Lower threshold for text detection
                height_ths=0.7,
                detail=1        # Get bounding boxes too
            )

            if results:
                text_parts = []
                for result in results:
                    if isinstance(result, (list, tuple)) and len(result) >= 2:
                        text_parts.append(str(result[1]).strip())
                
                # Join with spaces and clean up
                text = " ".join(text_parts)
                if text and not text.isspace():
                    return text
            
            # If no text found, still return a placeholder rather than treating as figure
            return f'--No readable text detected in block {self.block_index}--'

        except Exception as e:
            console.print(f"Error during OCR processing: {e}", style="danger")
            return f'--OCR Error in block {self.block_index}: {str(e)}--'

    def generate_latex(self):
        """Generate LaTeX representation for this text block with metadata."""
        if not self.content_string or self.content_string.isspace():
            return []
        
        # Simplified block metadata - just page and block ID
        page_num = getattr(self.parent_page, 'page_number', 'Unknown')
        
        return [
            LatexText(f"% Page {page_num}, Block {self.block_index}"),
            LatexText(self.content_string),
            LatexText(''),  # Add blank line for readability
        ]


class Page:
    """Page object representing a PDF page containing text blocks only."""

    def __init__(self, page_img, pdf, page_number):
        """Initialize a page with its image, parent PDF, and page number."""
        self.page_img = page_img
        self.parent_pdf = pdf
        self.page_number = page_number
        self.height = page_img.shape[0]
        self.width = page_img.shape[1]
        self.blocks = []

    async def async_generate_blocks(self, page_img, parent_pdf_instance):
        """Asynchronously generate text blocks from page image."""
        loop = asyncio.get_running_loop()

        with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
            bboxes = await loop.run_in_executor(
                executor,
                find_content_blocks,
                page_img
            )

        block_tasks = []
        page_height = page_img.shape[0]

        temp_page_context = type(
            'obj',
            (object,),
            {
                'parent_pdf': parent_pdf_instance,
                'page_img': page_img,
                'height': page_height,
                'page_number': self.page_number
            }
        )()

        for block_idx, bbox in enumerate(bboxes):
            async def process_single_block(bbox, idx):
                block = Block.__new__(Block)
                block.parent_page = temp_page_context
                block.bbox = bbox
                block.block_index = idx

                with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
                    block_img_result = await loop.run_in_executor(
                        executor,
                        lambda: block._make_block(bbox, page_img, page_height)
                    )

                block.block = block_img_result
                block.content_string = block._determine_content()
                return block

            block_tasks.append(process_single_block(bbox, block_idx))

        blocks = await asyncio.gather(*block_tasks)
        return blocks

    async def async_generate_latex(self):
        """Asynchronously generate LaTeX content for the page with page metadata."""
        content = [
            Command('newpage'),
            LatexText(f"% ===== PAGE {self.page_number} START ====="),
            Command('section*', arguments=[f'Page {self.page_number}']),
            LatexText(f"% blocks_count: {len(self.blocks)}"),
            LatexText(''),  # Blank line
        ]
        
        for block in self.blocks:
            content.extend(block.generate_latex())
        
        content.extend([
            LatexText(f"% ===== PAGE {self.page_number} END ====="),
            LatexText(''),  # Blank line
        ])
        
        return content


class PDF:
    """PDF Object representing a PDF document - simplified for text-only processing."""
    
    def __init__(self, filepath, data_folder=DEFAULT_DATA_FOLDER, output_dir='.'):
        """Initialize a PDF object from a file path."""
        self.path = filepath
        self.name = Utils.get_file_name(filepath)
        self.data_folder = data_folder
        self.output_dir = output_dir  # Store but don't create subdirectories
        
        # Use temp directory for all intermediate files
        self.temp_asset_folder = safe_join(
            os.getcwd(), data_folder, f"{self.name}_temp"
        )
        os.makedirs(self.temp_asset_folder, exist_ok=True)
        
        self.pages = []

    @classmethod
    async def async_init(cls, filepath, data_folder=DEFAULT_DATA_FOLDER, output_dir='.'):
        """Asynchronous initializer for PDF class."""
        instance = cls.__new__(cls)
        instance.path = filepath
        instance.name = Utils.get_file_name(filepath)
        instance.data_folder = data_folder
        instance.output_dir = output_dir

        # Use temp directory only
        if os.path.isabs(data_folder):
            instance.temp_asset_folder = os.path.join(data_folder, f"{instance.name}_temp")
        else:
            instance.temp_asset_folder = safe_join(
                os.getcwd(), data_folder, f"{instance.name}_temp"
            )
        os.makedirs(instance.temp_asset_folder, exist_ok=True)

        console.print(f"Processing PDF: {instance.name}", style="status")
        instance.pages = await instance._async_pdf_to_pages()
        return instance

    async def _async_pdf_to_pages(self):
        """Asynchronously convert PDF file to a list of Page objects."""
        if FITZ is None or CV2 is None:
            console.print("Error: PyMuPDF or OpenCV not loaded.", style="danger")
            return []
        
        loop = asyncio.get_running_loop()
        path = self.path

        doc = await loop.run_in_executor(None, FITZ.open, path)
        page_imgs = []
        
        try:
            extract_tasks = []
            for page_num in range(len(doc)):
                extract_tasks.append(loop.run_in_executor(
                    None,
                    self._extract_page_image,
                    doc, page_num
                ))

            with Progress(*progress_columns, console=console, transient=True) as progress:
                task = progress.add_task("Extracting pages", total=len(doc))
                page_imgs = []
                for coro in asyncio.as_completed(extract_tasks):
                    img = await coro
                    page_imgs.append(img)
                    progress.update(task, advance=1)

            # Save page images to temp directory only
            save_dir = safe_join(self.temp_asset_folder, "pages")
            os.makedirs(save_dir, exist_ok=True)

            console.print("Processing pages for text extraction...", style="status")
            pages = []
            
            with Progress(*progress_columns, console=console, transient=True) as progress:
                task = progress.add_task("Creating pages", total=len(page_imgs))
                for page_num, page_img in enumerate(page_imgs, 1):
                    if page_img is None:
                        continue
                    
                    # Save to temp directory for debugging if needed
                    save_path = safe_join(save_dir, f"page_{page_num}.png")
                    await loop.run_in_executor(None, CV2.imwrite, save_path, page_img)
                    
                    page = Page.__new__(Page)
                    page.page_img = page_img
                    page.parent_pdf = self
                    page.page_number = page_num
                    page.height = page_img.shape[0]
                    page.width = page_img.shape[1]
                    page.blocks = []
                    pages.append(page)
                    progress.update(task, advance=1)

            # Process blocks for all pages
            block_tasks = [
                page.async_generate_blocks(page.page_img, self)
                for page in pages
            ]

            if block_tasks:
                with Progress(*progress_columns, console=console, transient=True) as progress:
                    task = progress.add_task("Extracting text blocks", total=len(block_tasks))
                    blocks_results = []
                    for coro in asyncio.as_completed(block_tasks):
                        result = await coro
                        blocks_results.append(result)
                        progress.update(task, advance=1)
            else:
                blocks_results = []

            # Assign blocks to pages
            for i, page in enumerate(pages):
                if i < len(blocks_results):
                    page.blocks = blocks_results[i]
                else:
                    page.blocks = []

            return pages

        finally:
            await loop.run_in_executor(None, doc.close)

    def _extract_page_image(self, doc, page_num):
        """Extract a single page as an image."""
        if FITZ is None or CV2 is None:
            console.print("Error: PyMuPDF or OpenCV not loaded.", style="danger")
            return None
            
        try:
            page = doc.load_page(page_num)
            # Get page as pixmap with good resolution
            mat = FITZ.Matrix(2.0, 2.0)  # 2x zoom for better quality
            pix = page.get_pixmap(matrix=mat)
            
            # Convert to OpenCV image
            img_data = pix.tobytes("png")
            import io
            from PIL import Image
            img = Image.open(io.BytesIO(img_data))
            
            # Convert PIL to OpenCV format
            import numpy as np
            opencv_img = np.array(img)
            opencv_img = CV2.cvtColor(opencv_img, CV2.COLOR_RGB2BGR)
            
            return opencv_img
            
        except Exception as e:
            console.print(f"Error extracting page {page_num}: {e}", style="danger")
            return None

    # Add project_dir property
    @property
    def project_dir(self):
        """Return the project directory path."""
        return self.output_dir

    async def async_generate_latex(self):
        """Generate LaTeX content with document metadata for pandas processing."""
        import datetime
        import os
        now = datetime.datetime.now()
        
        # Enhanced metadata for pandas DataFrame processing
        total_blocks = sum(len(page.blocks) for page in self.pages)
        
        content = [
            LatexText(f"% ===== DOCUMENT METADATA ====="),
            LatexText(f"% generator: F-Instruct PDF Converter v{__version__}"),
            LatexText(f"% source_file: {os.path.basename(self.path)}"),
            LatexText(f"% source_path: {self.path}"),
            LatexText(f"% generated_timestamp: {now.isoformat()}"),
            LatexText(f"% processing_mode: text_extraction_only"),
            LatexText(f"% total_pages: {len(self.pages)}"),
            LatexText(f"% total_blocks: {total_blocks}"),
            LatexText(f"% document_type: financial_regulatory_report"),
            LatexText(f"% content_language: en"),
            LatexText(f"% ocr_engine: easyocr"),
            LatexText(f"% ===== END METADATA ====="),
            LatexText(''),
            Command('title', arguments=[f'Document: {self.name}']),
            Command('author', arguments=['F-Instruct Converter']),
            Command('date', arguments=[now.strftime('%B %d, %Y')]),
            Command('maketitle'),
            LatexText(''),
            Command('tableofcontents'),
            Command('newpage'),
            LatexText(''),
        ]

        console.print("Generating LaTeX content...", style="status")
        
        tasks = [page.async_generate_latex() for page in self.pages]
        if tasks:
            with Progress(*progress_columns, console=console, transient=True) as progress:
                task = progress.add_task("Processing pages", total=len(tasks))
                results = []
                for coro in asyncio.as_completed(tasks):
                    page_content = await coro
                    results.append(page_content)
                    progress.update(task, advance=1)
        else:
            results = []

        for page_content in results:
            content.extend(page_content)

        return [Environment(content, 'document')]


class TexFile:
    """Class representing a LaTeX file - simplified output to specified directory."""
    
    def __init__(self, pdf_obj, use_default_preamble=True):
        self.pdf_obj = pdf_obj
        self.preamble = (
            self._make_default_preamble() if use_default_preamble else []
        )
        self.body = []

    @classmethod
    async def async_init(cls, pdf_obj, use_default_preamble=True):
        instance = cls.__new__(cls)
        instance.pdf_obj = pdf_obj
        instance.preamble = (
            cls._make_default_preamble() if use_default_preamble else []
        )
        instance.body = await pdf_obj.async_generate_latex()
        return instance

    async def async_generate_tex_file(self, filename=None):
        """Write LaTeX file directly to output directory (no subdirectory)."""
        if filename is None:
            # Output directly to specified output directory
            filename = safe_join(self.pdf_obj.output_dir, f"{self.pdf_obj.name}.tex")
        
        # Ensure output directory exists
        os.makedirs(os.path.dirname(filename), exist_ok=True)
        
        content = self._unpack_content(self.preamble + self.body)
        await Utils.async_write_all(filename, content)
        
        # Clean up temp directory
        await self._cleanup_temp_files()

    async def _cleanup_temp_files(self):
        """Clean up temporary files after processing."""
        import shutil
        loop = asyncio.get_running_loop()
        
        try:
            if os.path.exists(self.pdf_obj.temp_asset_folder):
                await loop.run_in_executor(
                    None, 
                    shutil.rmtree, 
                    self.pdf_obj.temp_asset_folder
                )
                console.print(f"Cleaned up temporary files", style="dim cyan")
        except Exception as e:
            console.print(f"Warning: Could not clean up temp files: {e}", style="warning")

    @staticmethod
    def _make_default_preamble():
        """Enhanced preamble optimized for pandas processing."""
        return [
            Command(
                'documentclass',
                arguments=['article'],
                options=[('', 'a4paper'), ('', '11pt')]
            ),
            Command('usepackage', arguments=['amsmath']),
            Command('usepackage', arguments=['amssymb']),
            Command('usepackage', arguments=['geometry'], options=[('margin', '1in')]),
            Command('usepackage', arguments=['float']),
            Command('usepackage', arguments=['longtable']),
            Command('usepackage', arguments=['array']),
            Command('usepackage', arguments=['booktabs']),
            Command('usepackage', arguments=['xcolor']),
            Command('setlength', arguments=[Command('parindent'), '0pt']),
            Command('setlength', arguments=[Command('parskip'), '0.8em']),  # More spacing
            LatexText('% ===== PANDAS PROCESSING OPTIMIZED ====='),
            LatexText('% This document structure is optimized for DataFrame conversion'),
            LatexText('% Each text block is clearly separated with metadata comments'),
            LatexText('% Metadata format: % Page X, Block Y'),
            LatexText('% Section format: Page X for easy chunking'),
            LatexText('% ===== END OPTIMIZATION NOTES ====='),
            LatexText(''),
        ]

    def _unpack_content(self, content_list):
        """Unpack nested LaTeX content into flat string list."""
        result = []
        
        for item in content_list:
            if isinstance(item, LatexText):
                result.append(item.text)
            elif isinstance(item, Command):
                result.append(item.text)
            elif isinstance(item, Environment):
                # Recursively unpack environment content
                result.extend(self._unpack_content(item.content))
            elif isinstance(item, list):
                # Recursively unpack nested lists
                result.extend(self._unpack_content(item))
            elif hasattr(item, 'text'):
                result.append(item.text)
            else:
                result.append(str(item))
        
        return result


def print_status(msg):
    """Print status messages with rich colored output."""
    console.print(msg, style="status")


def print_success(msg):
    """Print success messages with rich colored output."""
    console.print(msg, style="success")


def print_error(msg):
    """Print error messages with rich colored output."""
    console.print(msg, style="danger")


async def async_convert(source_path, output_dir='.', data=None):
    """Asynchronously convert a PDF file or directory of PDF files to LaTeX."""
    _ensure_dependencies_loaded()
    
    # Use system temp directory if data not specified
    if data is None:
        data = os.path.join(get_temp_dir(), "f_instruct_temp")
    
    console.print(f"Processing: {source_path}", style="info")
    console.print(f"Temporary files: {data}", style="dim cyan")

    os.makedirs(data, exist_ok=True)

    if os.path.isdir(source_path):
        pdf_files = [f for f in os.listdir(source_path) if f.lower().endswith('.pdf')]
        if not pdf_files:
            console.print(f"No PDF files found in directory: {source_path}", style="warning")
            return

        console.print(f"Found {len(pdf_files)} PDF file(s) to process", style="info")
        tasks = []
        for filename in pdf_files:
            file_path = os.path.join(source_path, filename)
            # Create a unique output subdir for each file based on its name
            file_output_dir_name = Utils.get_file_name(filename)
            file_output_dir = safe_join(output_dir, file_output_dir_name)
            # Pass the specific output dir and the main data dir
            tasks.append(async_convert(file_path, file_output_dir, data))

        await asyncio.gather(*tasks)

    elif os.path.isfile(source_path) and source_path.lower().endswith('.pdf'):
        try:
            # PDF.async_init now needs the data directory
            pdf = await PDF.async_init(source_path, data, output_dir)
            if pdf:
                tex_file = await TexFile.async_init(pdf)
                if tex_file:
                    await tex_file.async_generate_tex_file()
                    console.print(
                        f"Successfully generated LaTeX project at\n{pdf.project_dir}",
                        style="success"
                    )
                else:
                    console.print(f"Failed to initialize TexFile for {source_path}", style="danger")
            else:
                console.print(f"Failed to initialize PDF for {source_path}", style="danger")

        except Exception as e:  # pylint: disable=broad-except
            console.print(f"Error processing {source_path}: {e}", style="danger")
    
    elif os.path.isfile(source_path):
        console.print(f"Error: {source_path} is not a PDF file", style="danger")
    
    else:
        console.print(f"Error: {source_path} does not exist or is not accessible", style="danger")


def convert(source_path, output_dir='.', data=None):
    """Synchronously convert a PDF file or directory of PDF files to LaTeX."""
    # Runs the async_convert function using asyncio.run()
    asyncio.run(async_convert(source_path, output_dir, data))


# --- CLI Interface ---
@click.command(context_settings=dict(help_option_names=['-h', '--help']))
@click.option('--input', '-i', 'input_path', type=click.Path(exists=True), required=True, 
              help="Path to PDF file or directory containing PDF files to convert")
@click.option('--output', '-o', type=click.Path(), default='.', show_default=True, 
              help="Output directory for generated LaTeX projects")
@click.version_option(version=__version__, message='PDF2LaTeX Converter %(version)s')
def cli(input_path, output):
    """
    PDF2LaTeX Converter - Convert PDF files to LaTeX format.

    This tool automatically detects whether the input is a single PDF file or a directory
    containing PDF files and processes them accordingly. It extracts text using OCR and 
    treats non-textual elements as figures, processing files in parallel for improved performance.
    
    Examples:
        uv run f_instruct/data/converter.py -i document.pdf
        uv run f_instruct/data/converter.py -i ./pdf_folder -o ./latex_output
        uv run f_instruct/data/converter.py --input report.pdf --output ./results
    """
    if '--help' not in sys.argv and '-h' not in sys.argv:
        _ensure_dependencies_loaded()

    # Create output directory if it doesn't exist
    os.makedirs(output, exist_ok=True)
    
    # Use system temp directory for intermediate files
    temp_data_dir = os.path.join(get_temp_dir(), "f_instruct_temp")

    console.print(f"Input: {input_path}", style="info")
    console.print(f"Output directory: {os.path.abspath(output)}", style="info")
    console.print(f"PDF2LaTeX Converter v{__version__}", style="status")

    # Smart detection of input type
    if os.path.isfile(input_path):
        if input_path.lower().endswith('.pdf'):
            console.print("Detected: Single PDF file", style="dim cyan")
        else:
            console.print("Error: Input file is not a PDF", style="danger")
            sys.exit(1)
    elif os.path.isdir(input_path):
        pdf_count = len([f for f in os.listdir(input_path) if f.lower().endswith('.pdf')])
        console.print(f"Detected: Directory with {pdf_count} PDF file(s)", style="dim cyan")
        if pdf_count == 0:
            console.print("Error: No PDF files found in directory", style="danger")
            sys.exit(1)
    else:
        console.print("Error: Input path does not exist", style="danger")
        sys.exit(1)

    try:
        convert(input_path, output, temp_data_dir)
        console.print("Conversion completed successfully! 🎉", style="success")
        
    except KeyboardInterrupt:
        console.print("\nConversion interrupted by user.", style="warning")
        sys.exit(1)
    except Exception as e:
        console.print(f"Conversion failed: {e}", style="danger")
        sys.exit(1)


# --- Helper Functions for API usage ---
def convert_pdf(pdf_path, output_dir='.', data=None):
    """
    Convert a PDF file to LaTeX format.
    
    Args:
        pdf_path: Path to the PDF file
        output_dir: Directory where LaTeX project will be created
        data: Directory for intermediate files (uses system temp if None)
    """
    _ensure_dependencies_loaded()
    return convert(pdf_path, output_dir, data)


def convert_pdfs_in_directory(directory_path, output_dir='.', data=None):
    """
    Convert all PDF files in a directory to LaTeX format.
    
    Args:
        directory_path: Path to directory containing PDF files
        output_dir: Directory where LaTeX projects will be created
        data: Directory for intermediate files (uses system temp if None)
    """
    _ensure_dependencies_loaded()
    return convert(directory_path, output_dir, data)


def smart_convert(input_path, output_dir='.'):
    """
    Smart conversion function that auto-detects file vs directory.
    
    Args:
        input_path: Path to PDF file or directory containing PDF files
        output_dir: Directory where LaTeX projects will be created
    
    Returns:
        bool: True if conversion successful, False otherwise
    """
    try:
        _ensure_dependencies_loaded()
        convert(input_path, output_dir)
        return True
    except Exception as e:
        console.print(f"Smart conversion failed: {e}", style="danger")
        return False


if __name__ == "__main__":
    cli()  # pylint: disable=no-value-for-parameter