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
    global READER, CV2, FITZ, PLT, NP, TORCH, EASYOCR, IS_LOADED

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
        console.print("Please ensure all required libraries (OpenCV, PyMuPDF, Matplotlib, NumPy, PyTorch, EasyOCR) are installed.", style="warning")
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
    """Represents a content block within a Page (text or figure)."""

    def __init__(self, bbox, parent_page):
        """Initialize a block with its bounding box and parent page."""
        self.parent_page = parent_page
        self.bbox = bbox
        self.block = self._make_block(
            bbox, parent_page.page_img, parent_page.height
        )
        self.block_type, self.content_string = self._determine_content()

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
        """Determine if the block is text or figure using OCR."""
        if READER is None:
            console.print("Error: EasyOCR Reader not initialized.", style="danger")
            return (1, '--Block Type is Figure (OCR Not Loaded)--')
        try:
            if (
                self.block is None
                or self.block.size == 0
                or self.block.shape[0] < 5
                or self.block.shape[1] < 5
            ):
                return (1, '--Block Type is Figure (Too small)--')

            results = READER.readtext(self.block, paragraph=True)

            if results:
                # Fix: EasyOCR returns list of tuples (bbox, text, confidence)
                text_parts = []
                for result in results:
                    if isinstance(result, (list, tuple)) and len(result) >= 2:
                        # result[1] is the text portion
                        text_parts.append(str(result[1]))
                s = " ".join(text_parts)

                if s and not s.isspace() and len(s) > 3:
                    return (0, s)

            return (1, '--Block Type is Figure--')

        except Exception as e:  # pylint: disable=broad-except
            console.print(f"Error during OCR processing: {e}", style="danger")
            return (1, f'--Block Type is Figure (OCR Error: {str(e)})--')

    async def async_determine_content(self, block_img):
        """Asynchronous version of determine_content for parallel processing."""
        if READER is None:
            console.print("Error: EasyOCR Reader not initialized.", style="danger")
            self.block_type, self.content_string = 1, '--Block Type is Figure (OCR Not Loaded)--'
            return (1, '--Block Type is Figure (OCR Not Loaded)--')
        
        loop = asyncio.get_running_loop()
        try:
            if (
                block_img is None or block_img.size == 0 or
                block_img.shape[0] < 5 or block_img.shape[1] < 5
            ):
                self.block_type, self.content_string = 1, '--Block Type is Figure (Too small)--'
                return (1, '--Block Type is Figure (Too small)--')
            
            # Fix: Add proper function wrapper for thread executor
            def _run_ocr():
                if READER is None:
                    return []
                return READER.readtext(block_img, paragraph=True)
            
            results = await loop.run_in_executor(None, _run_ocr)
            
            if results:
                # Fix: EasyOCR returns list of tuples (bbox, text, confidence)
                text_parts = []
                for result in results:
                    if isinstance(result, (list, tuple)) and len(result) >= 2:
                        # result[1] is the text portion
                        text_parts.append(str(result[1]))
                s = " ".join(text_parts)
                if s and not s.isspace() and len(s) > 3:
                    self.block_type, self.content_string = 0, s
                    return (0, s)
            self.block_type, self.content_string = 1, '--Block Type is Figure--'
            return (1, '--Block Type is Figure--')
        except Exception as e:  # pylint: disable=broad-except
            console.print(
                f"Error during async OCR processing: {e}",
                style="danger"
            )
            self.block_type, self.content_string = 1, f'--Block Type is Figure (OCR Error: {str(e)})--'
            return (1, f'--Block Type is Figure (OCR Error: {str(e)})--')

    def generate_latex(self):
        """Generate LaTeX representation for this block."""
        if CV2 is None:
            console.print("Error: OpenCV not loaded.", style="danger")
            return []
        
        match self.block_type:
            case 0:
                if self.content_string and not self.content_string.isspace():
                    return [
                        LatexText(self.content_string),
                        Command('vspace', arguments=['10pt'])
                    ]
                return []
            case 1:
                figure_dir = self.parent_page.parent_pdf.figures_dir
                fig_filename = (
                    f"figure_{self.parent_page.parent_pdf.num_figs}.png"
                )
                fig_path = safe_join(figure_dir, fig_filename)

                try:
                    os.makedirs(figure_dir, exist_ok=True)
                    # Fix: Ensure block is not None before writing
                    if self.block is not None:
                        success = CV2.imwrite(fig_path, self.block)
                        if success:
                            self.parent_page.parent_pdf.num_figs += 1
                            relative_fig_path = f"figures/{fig_filename}"
                            fig_env_content = [
                                Command('centering'),
                                Command(
                                    'includegraphics',
                                    arguments=[relative_fig_path],
                                    options=[('width', r'0.8\textwidth')]
                                )
                            ]
                            return [
                                Environment(fig_env_content, 'figure', options=[('', 'H')])
                            ]
                except Exception as e:  # pylint: disable=broad-except
                    console.print(
                        f"Error saving figure {fig_path}: {e}",
                        style="danger"
                    )
                    return []
            case _:
                return []


class Page:
    """Page object representing a PDF page containing Block objects."""

    def __init__(self, page_img, pdf):
        """Initialize a page with its image and parent PDF."""
        self.page_img = page_img
        self.parent_pdf = pdf
        self.height = page_img.shape[0]
        self.width = page_img.shape[1]
        self.blocks = []

    async def async_determine_content(self, block_img):
        """Determine content type for a block image."""
        if READER is None:
            console.print("Error: EasyOCR Reader not initialized.", style="danger")
            return (1, '--Block Type is Figure (OCR Not Loaded)--')
        
        loop = asyncio.get_running_loop()
        try:
            if (
                block_img is None or block_img.size == 0 or
                block_img.shape[0] < 5 or block_img.shape[1] < 5
            ):
                return (1, '--Block Type is Figure (Too small)--')
            
            # Fix: Add proper function wrapper for thread executor
            def _run_ocr():
                if READER is None:
                    return []
                return READER.readtext(block_img, paragraph=True)
            
            results = await loop.run_in_executor(None, _run_ocr)
            
            if results:
                # Fix: EasyOCR returns list of tuples (bbox, text, confidence)
                text_parts = []
                for result in results:
                    if isinstance(result, (list, tuple)) and len(result) >= 2:
                        # result[1] is the text portion
                        text_parts.append(str(result[1]))
                s = " ".join(text_parts)
                if s and not s.isspace() and len(s) > 3:
                    return (0, s)
            return (1, '--Block Type is Figure--')
        except Exception as e:  # pylint: disable=broad-except
            console.print(
                f"Error during async OCR processing: {e}",
                style="danger"
            )
            return (1, f'--Block Type is Figure (OCR Error: {str(e)})--')

    async def async_generate_blocks(self, page_img, parent_pdf_instance):
        """Asynchronously generate blocks from page image."""
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
                'height': page_height
            }
        )()

        for bbox in bboxes:
            async def process_single_block(bbox):
                block = Block.__new__(Block)
                block.parent_page = temp_page_context
                block.bbox = bbox

                with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
                    # Fix: Use proper method call with self parameter
                    block_img_result = await loop.run_in_executor(
                        executor,
                        lambda: block._make_block(bbox, page_img, page_height)
                    )

                block.block = block_img_result

                # Fix: Use the Page instance method instead of non-existent Block method
                block_type, content_string = await self.async_determine_content(
                    block.block
                )
                block.block_type = block_type
                block.content_string = content_string

                return block

            block_tasks.append(process_single_block(bbox))

        blocks = await asyncio.gather(*block_tasks)
        return blocks

    async def async_generate_latex(self):
        """Asynchronously generate LaTeX content for the page."""
        content = []
        for block in self.blocks:
            content.extend(block.generate_latex())
        return content + [
            Command('par'),
            Command('vspace', arguments=['10pt'])
        ]


class PDF:
    """PDF Object representing a PDF document containing Page objects."""
    def __init__(self, filepath, data_folder=DEFAULT_DATA_FOLDER, output_dir='.'):
        """Initialize a PDF object from a file path."""
        self.path = filepath
        self.name = Utils.get_file_name(filepath)
        self.data_folder = data_folder

        # Use output_dir as the base for project structure
        project_paths = Utils.create_latex_project_structure(output_dir, self.name)
        self.project_dir = project_paths["project_dir"]
        self.figures_dir = project_paths["figures_dir"]
        self.build_dir = project_paths["build_dir"]

        self.asset_folder = self.figures_dir

        # Place temp assets relative to data_folder, ensuring safety
        self.temp_asset_folder = safe_join(
            os.getcwd(), data_folder, f"{self.name}_assets"
        )
        os.makedirs(self.temp_asset_folder, exist_ok=True)

        self.num_figs = 0
        self.pages = []
        self.embedded_images = []

    @classmethod
    async def async_init(cls, filepath, data_folder=DEFAULT_DATA_FOLDER, output_dir='.'):
        """Asynchronous initializer for PDF class."""
        instance = cls.__new__(cls)
        instance.path = filepath
        instance.name = Utils.get_file_name(filepath)
        instance.data_folder = data_folder

        project_paths = Utils.create_latex_project_structure(
            output_dir, instance.name
        )
        instance.project_dir = project_paths["project_dir"]
        instance.figures_dir = project_paths["figures_dir"]
        instance.build_dir = project_paths["build_dir"]

        instance.asset_folder = instance.figures_dir

        # Fix: If data_folder is absolute, use it directly for temp_asset_folder
        if os.path.isabs(data_folder):
            instance.temp_asset_folder = os.path.join(data_folder, f"{instance.name}_assets")
        else:
            instance.temp_asset_folder = safe_join(
                os.getcwd(), data_folder, f"{instance.name}_assets"
            )
        os.makedirs(instance.temp_asset_folder, exist_ok=True)

        instance.num_figs = 0

        console.print(
            f"Extracting embedded images from {instance.name}...",
            style="status"
        )
        instance.embedded_images = await Utils.extract_images_from_pdf(
            filepath, instance.figures_dir
        )
        console.print(
            f"Extracted {len(instance.embedded_images)} embedded images",
            style="info"
        )

        instance.pages = await instance._async_pdf_to_pages()
        return instance

    def _extract_page_image(self, doc, page_num):
        """Extracts a single page image from the PDF document."""
        if NP is None or CV2 is None:
            console.print("Error: NumPy or OpenCV not loaded.", style="danger")
            return None
        try:
            page = doc.load_page(page_num)
            pix = page.get_pixmap(dpi=300)
            img = NP.frombuffer(
                pix.samples, dtype=NP.uint8
            ).reshape(pix.height, pix.width, pix.n)
            if pix.n == 4:
                img = CV2.cvtColor(img, CV2.COLOR_RGBA2BGR)
            elif pix.n == 3:
                img = CV2.cvtColor(img, CV2.COLOR_RGB2BGR)
            elif pix.n == 1:
                img = CV2.cvtColor(img, CV2.COLOR_GRAY2BGR)
            return img
        except Exception as e:  # pylint: disable=broad-except
            console.print(
                f"Error extracting image for page {page_num}: {e}",
                style="danger"
            )
            return None

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
            # Fix: Use range(len(doc)) instead of enumerate(doc)
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

            save_dir = safe_join(self.build_dir, "pages")
            os.makedirs(save_dir, exist_ok=True)
            console.print("Saving page images asynchronously...", style="status")

            with Progress(*progress_columns, console=console, transient=True) as progress:
                task = progress.add_task("Saving pages", total=len(page_imgs))
                with concurrent.futures.ThreadPoolExecutor(
                    max_workers=MAX_WORKERS
                ) as executor:
                    save_futures = []
                    for i, img in enumerate(page_imgs):
                        if img is None:
                            continue
                        save_path = safe_join(save_dir, f"page_{i+1}.png")
                        save_futures.append(loop.run_in_executor(
                            executor,
                            CV2.imwrite,
                            save_path, img
                        ))
                    for coro in asyncio.as_completed(save_futures):
                        await coro
                        progress.update(task, advance=1)

            console.print("Segmenting pages asynchronously...", style="status")
            pages = []
            with Progress(*progress_columns, console=console, transient=True) as progress:
                task = progress.add_task("Segmenting", total=len(page_imgs))
                for page_img in page_imgs:
                    if page_img is None:
                        continue
                    page = Page.__new__(Page)
                    page.page_img = page_img
                    page.parent_pdf = self
                    page.height = page_img.shape[0]
                    page.width = page_img.shape[1]
                    page.blocks = []
                    pages.append(page)
                    progress.update(task, advance=1)

            block_tasks = [
                page.async_generate_blocks(page.page_img, self)
                for page in pages
            ]

            if block_tasks:
                with Progress(*progress_columns, console=console, transient=True) as progress:
                    task = progress.add_task(
                        "Block segmentation",
                        total=len(block_tasks)
                    )
                    blocks_results = []
                    for coro in asyncio.as_completed(block_tasks):
                        result = await coro
                        blocks_results.append(result)
                        progress.update(task, advance=1)
            else:
                blocks_results = []

            page_idx = 0
            for i, page in enumerate(pages):
                if page.page_img is not None:
                    if page_idx < len(blocks_results):
                        page.blocks = blocks_results[page_idx]
                        page_idx += 1
                    else:
                        console.print(
                            f"Warning: Mismatch in block results for page {i}",
                            style="warning"
                        )
                        page.blocks = []

            return pages

        finally:
            await loop.run_in_executor(None, doc.close)

    async def async_generate_latex(self):
        """Asynchronously generate LaTeX content for the PDF."""
        content = []
        console.print("Generating LaTeX asynchronously...", style="status")

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

        if self.embedded_images:
            content.append(Command('section', arguments=['Embedded Images']))
            content.append(
                LatexText('The following images were embedded in the PDF:')
            )
            content.append(Command('vspace', arguments=['10pt']))

            for idx, img_path in enumerate(self.embedded_images):
                img_filename = os.path.basename(img_path)
                fig_env_content = [
                    Command('centering'),
                    Command(
                        'includegraphics',
                        arguments=[f"figures/{img_filename}"],
                        options=[('width', r'0.8\textwidth')]
                    ),
                    Command('caption', arguments=[f"Embedded image {idx+1}"])
                ]
                content.append(
                    Environment(fig_env_content, 'figure', options=[('', 'H')])
                )

        return [Environment(content, 'document')]


class TexFile:
    """Class representing a LaTeX file with preamble and body content."""
    def __init__(self, pdf_obj, use_default_preamble=True):
        """Initialize a TexFile object from a PDF object."""
        self.pdf_obj = pdf_obj
        self.preamble = (
            self._make_default_preamble() if use_default_preamble else []
        )
        self.body = []

    @classmethod
    async def async_init(cls, pdf_obj, use_default_preamble=True):
        """Asynchronous initializer for TexFile."""
        instance = cls.__new__(cls)
        instance.pdf_obj = pdf_obj
        instance.preamble = (
            cls._make_default_preamble() if use_default_preamble else []
        )
        instance.body = await pdf_obj.async_generate_latex()
        return instance

    async def async_generate_tex_file(self, filename=None):
        """Asynchronously write the LaTeX file content to the specified file."""
        if filename is None:
            filename = safe_join(self.pdf_obj.project_dir, f"{self.pdf_obj.name}.tex")
        content = self._unpack_content(self.preamble + self.body)
        await Utils.async_write_all(filename, content)

    def generate_tex_file(self, filename):
        """Write the LaTeX file content to the specified file."""
        content = self._unpack_content(self.preamble + self.body)
        Utils.write_all(filename, content)

    @staticmethod
    def _unpack_content(lst):
        """Convert LaTeX objects to strings suitable for writing to a file."""
        content = []
        for obj in lst:
            if isinstance(obj, Environment):
                env_content = TexFile._unpack_content(obj.content)
                content.extend(env_content)
            elif hasattr(obj, 'text'):
                content.append(obj.text)
            else:
                content.append(str(obj))
        return [
            s.replace("\x0c", Command('vspace', arguments=['10pt']).text)
            for s in content if s
        ]

    def add_to_preamble(self, obj):
        """Add a LaTeX object to the preamble."""
        self.preamble.append(obj)

    @staticmethod
    def _make_default_preamble():
        """Create default preamble for LaTeX document."""
        return [
            Command(
                'documentclass',
                arguments=['article'],
                options=[('', 'a4paper'), ('', '12pt')]
            ),
            Command('usepackage', arguments=['amsmath']),
            Command('usepackage', arguments=['amssymb']),
            Command('usepackage', arguments=['graphicx']),
            Command(
                'usepackage',
                arguments=['geometry'],
                options=[('margin', '1in')]
            ),
            Command('usepackage', arguments=['float']),
            Command('usepackage', arguments=['caption']),
            Command('setlength', arguments=[Command('parindent'), '0pt']),
            Command('setlength', arguments=[Command('parskip'), '1em']),
        ]


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