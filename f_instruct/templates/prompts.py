"""
Prompts used for financial data generation and processing.
All prompts are stored in a central dictionary for easy access and management.
"""

PROMPTS = {
    # Question answering prompts
    "question_generation": {
        "system": """You are an expert in financial regulation and derivative markets.

Your task is to generate thought-provoking questions about the text provided.
Focus on creating questions that are directly answerable from the text and that
demonstrate understanding of financial concepts, regulatory requirements, or 
market practices.

When generating questions, consider various categories:
- Factual: Questions about specific facts stated in the text
- Analytical: Questions requiring analysis or interpretation of information
- Application: Questions about how to apply concepts in practice
- Clarification: Questions seeking clarity on potentially confusing points
- Conceptual: Questions about broader concepts or principles

Generate a single, focused question that is specific and can be answered 
completely based on the provided text.""",
        
        "user": """Please generate a {category} question based on the following text:

```
{text}
```

{category_desc}

The question should be directly answerable from the text, specific, and use appropriate 
financial terminology."""
    },
    
    "answer_generation": {
        "system": """You are an expert in financial regulation and derivative markets.
        
You provide accurate, comprehensive answers to questions based only on the information
provided in the text. If the answer cannot be fully determined from the text, explain
what can be answered and what additional information would be needed.""",
        
        "user": """Please answer the following question based only on the information 
in the provided text:

Text:
```
{text}
```

Question: {question}

Your answer should be comprehensive, accurate, and only use information from the provided text."""
    },
    
    "instruction_following": {
        "data_extraction": {
            "system": """You are an expert in financial data extraction.
            
Your task is to identify and extract key data points, entities, or numerical information
from financial regulatory texts.""",
            
            "user": """Given the following financial text, create a specific instruction that asks for extraction 
of key financial data points, entities, or numerical information from the text.

Text:
```
{text}
```

Create a precise instruction asking to extract specific data points from this text.
The instruction should be specific and clearly indicate what should be extracted."""
        },
        
        "summarization": {
            "system": """You are an expert in financial document summarization.
            
Your task is to create clear instructions for summarizing financial documents
with specific requirements.""",
            
            "user": """Given the following financial text, create an instruction that asks for a summary 
of the text with specific requirements like word count, focus areas, or target audience.

Text:
```
{text}
```

Create a specific summarization instruction."""
        },
        
        "transformation": {
            "system": """You are an expert in financial data transformation.
            
Your task is to create instructions for transforming financial information
between different formats.""",
            
            "user": """Given the following financial text, create an instruction that asks to transform 
financial information from one format to another (e.g., text to table, paragraph to bullet points, 
descriptive text to structured JSON).

Text:
```
{text}
```

Create a specific transformation instruction."""
        },
        
        "code_generation": {
            "system": """You are an expert in financial programming and code generation.
            
Your task is to create instructions for generating code that implements financial
concepts or calculations described in regulatory texts.""",
            
            "user": """Given the following financial text, create an instruction that asks for generation of code 
(Python, SQL, etc.) related to processing, analyzing, or implementing something described in the text.

Text:
```
{text}
```

Create a specific code generation instruction."""
        },
        
        "outlining": {
            "system": """You are an expert in financial document structure and organization.
            
Your task is to create instructions for developing structured outlines based on
financial regulatory content.""",
            
            "user": """Given the following financial text, create an instruction that asks for creating 
a structured outline or framework based on the content.

Text:
```
{text}
```

Create a specific outlining instruction."""
        },
        
        "response": {
            "system": """You are an expert in financial regulation and derivative markets.
            
Your task is to provide detailed, accurate responses to instructions based on
financial regulatory text.""",
            
            "user": """You are an expert in financial regulation and derivative markets.

Below is a text from a financial regulatory document:

```
{text}
```

Please follow this instruction based on the text above:

Instruction: {instruction}

Provide a detailed, accurate, and comprehensive response to the instruction."""
        }
    },
    
    # Financial product creation prompts
    "financial_product": {
        "instruction_generation": {
            "system": """You are an expert in financial product structuring and documentation.
            
Your task is to create clear instructions for defining or structuring financial products
based on concepts mentioned in financial regulatory texts.""",
            
            "user": """Based on the following financial text, create an instruction to define 
or structure a financial product of type '{subcategory}' that relates to concepts in the text:

```
{text}
```

Create a specific, detailed instruction for defining a {subcategory}."""
        },
        
        "product_definition": {
            "system": """You are an expert in financial products and derivatives.
            
Your task is to create detailed, structured definitions of financial products
following specific instructions and based on concepts mentioned in the provided text.""",
            
            "user": """Below is a text that contains financial concepts:

```
{text}
```

Based on this instruction:

{instruction}

Create a detailed, structured definition of a {subcategory}. Your response should be in JSON format 
with appropriate fields for this type of financial product."""
        }
    },
    
    "api_interaction": {
        "scenario_generation": {
            "system": """You are an expert in financial APIs and banking systems. 
Your task is to create realistic scenarios where a banking application would need to interact with a financial API.
Focus on the specific API endpoint and subcategory to create a targeted and useful scenario.""",
            
            "user": """Based on the following financial text and API endpoint details, create a detailed scenario related to {subcategory} in banking API interactions.

TEXT:
```
{text}
```

The scenario should involve a specific banking API and demonstrate a clear need for interaction,
focusing on the {subcategory} aspects. Include details like the type of data exchanged, the purpose
of the interaction, and any relevant conditions or requirements."""
        },
        
        "solution_generation": {
            "system": """You are an expert in financial APIs and banking systems.
            
Your task is to provide detailed technical solutions for API interaction scenarios,
including endpoints, parameters, and response handling.""",
            
            "user": """Consider this background information:

```
{text}
```

Here's a scenario involving API interaction:

{scenario}

Provide a detailed solution for this {subcategory} API scenario. Include any relevant API endpoints, 
parameters, request formatting, and response handling that would be needed."""
        }
    },
    
    # Regulatory compliance prompts
    "regulatory_compliance": {
        "scenario_generation": {
            "system": """You are an expert in financial regulation and compliance.
            
Your task is to create realistic scenarios where financial institutions or market participants
would need guidance on compliance issues based on regulatory texts.""",
            
            "user": """Given the following financial regulatory text, create a realistic scenario 
where a financial institution or market participant needs guidance on '{subcategory}' 
compliance issues:

```
{text}
```

Create a specific regulatory compliance scenario for {subcategory}."""
        },
        
        "guidance_generation": {
            "system": """You are an expert in financial regulation and compliance.
            
Your task is to provide detailed guidance on addressing compliance issues,
including specific steps, considerations, and references to relevant regulations.""",
            
            "user": """Consider this regulatory information:

```
{text}
```

Here's a compliance scenario:

{scenario}

Provide detailed guidance on how to address this {subcategory} compliance issue. 
Include specific steps, considerations, and references to relevant regulations where appropriate."""
        }
    },
    
    "finreg_code_gen": {
        "system": """You are an expert Python developer and financial regulations analyst.
Your task is to generate Python code that implements the requirements described in the regulation.""",
        
        "user": """You are an expert Python developer and financial regulations analyst. Your task is to read the following regulation text and XML code, and generate Python code that parses, validates, or manipulates the XML models as described in the regulation.

<regulation_text>
{regulation_text}
</regulation_text>

<xml_model>
{xml_code}
</xml_model>

Instructions:
- Write Python code that implements the requirements or operations described in the regulation.
- Use standard libraries (e.g., xml.etree.ElementTree) or any specified libraries.
- Add comments explaining each major step.
- If multiple operations are required, structure the code into functions.

Output your code inside <output_code> tags."""
    },
    
    "finreg_json_gen": {
        "system": """You are an expert in financial data modeling.
Your task is to produce JSON structures that capture data requirements from regulations.""",
        
        "user": """You are an expert in financial data modeling. Given the following regulation text and XML code, produce a JSON object that captures the required data structure, constraints, or operations as described.

<regulation_text>
{regulation_text}
</regulation_text>

<xml_model>
{xml_code}
</xml_model>

Instructions:
- The JSON should reflect the information model, constraints, or mappings required by the regulation.
- Use clear keys and values, and include nested structures if needed.
- If the regulation describes operations, encode them as JSON fields or objects.

Output your JSON inside <output_json> tags."""
    },
    
    "banking_api": {
        "endpoint_description": {
            "system": """You are an expert in financial APIs and banking integration.
            
Your task is to describe the purpose, parameters, and usage of banking API endpoints.""",
            
            "user": """Given the following API endpoint from a banking system:

Endpoint: {endpoint}

Create a detailed description of:
1. The purpose of this endpoint
2. Required parameters and their meaning
3. Optional parameters and their meaning
4. Common use cases for this endpoint
5. How responses should be interpreted

Your description should be clear, accurate, and useful for developers integrating with this banking API."""
        },
        
        "query_generation": {
            "system": """You are an expert in banking API integration.
            
Your task is to generate sample API queries for banking operations based on financial scenarios.""",
            
            "user": """Based on the following financial scenario:

```
{scenario}
```

Generate a sample API query for the endpoint: {endpoint}

Include:
1. All required parameters with realistic sample values
2. Any relevant optional parameters
3. Headers and authentication details (using placeholder values)
4. A brief explanation of what this API call accomplishes"""
        }
    },
    
    "relevance_checking": {
        "system": """You are an expert financial content evaluator.
Your task is to determine if a text chunk contains enough relevant information
to generate high-quality training examples for a specific category and subcategory.
Respond with ONLY 'yes' or 'no'.""",
        
        "user": """Evaluate if the following text contains enough relevant information 
to generate training examples for:

CATEGORY: {category} - {category_description}
SUBCATEGORY: {subcategory} - {subcategory_description}

TEXT TO EVALUATE:
```
{text}
```

Respond with 'yes' if the text is relevant and contains enough information, or 'no' if it does not."""
    }
}