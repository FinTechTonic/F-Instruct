TRAINING_PAIR_TYPES = {
    "question_answering": {
        "description": "Standard question-answer format for knowledge extraction.",
        "subcategories": {
            "factual": "Questions about specific facts in text.",
            "analytical": "Questions requiring analysis or interpretation.",
            "application": "Questions on applying concepts practically.",
            "clarification": "Questions seeking clarity on confusing points.",
            "conceptual": "Questions about broader concepts or principles.",
            "regulatory-reporting": "Questions on EMIR reporting requirements.",
            "transaction-classification": "Questions on categorizing derivatives.",
            "valuation-collateral": "Questions on pricing and margin requirements.",
            "lifecycle-management": "Questions on handling derivative lifecycle events.",
            "settlement-services": "Questions on contract settlement procedures.",
            "calculation-agency": "Questions on calculation agent determinations.",
            "data-quality": "Questions on ensuring accurate reporting."
        }
    },
    "instruction_following": {
        "description": "Tasks requiring adherence to specific instructions.",
        "subcategories": {
            "data_extraction": "Extract specific financial data from text.",
            "summarization": "Summarize financial documents or regulations.",
            "transformation": "Transform financial data between formats.",
            "code_generation": "Generate code for financial tasks.",
            "outlining": "Create structured outlines of financial documents."
        }
    },
    "financial_product_creation": {
        "description": "Generating structured financial product definitions.",
        "subcategories": {
            "derivative_definition": "Define structured derivatives with parameters.",
            "option_creation": "Create option contracts with specific terms.",
            "swap_definition": "Define swap contracts with specific parameters.",
            "bond_structuring": "Structure bonds with specific characteristics.",
            "product_term_sheet": "Generate term sheets for financial products."
        }
    },
    "regulatory_compliance": {
        "description": "Tasks focused on financial regulatory compliance.",
        "subcategories": {
            "requirement_identification": "Identify applicable regulatory requirements.",
            "compliance_assessment": "Assess compliance with financial regulations.",
            "reporting_guidance": "Provide guidance on regulatory reporting.",
            "risk_assessment": "Assess regulatory risks in finance.",
            "documentation": "Generate compliance documentation."
        }
    }
}

CLASSIFIER_PROMPT_TEMPLATE = """
TASK: DOCUMENT PAGE CLASSIFICATION

INPUT:
- Page ID: {page_id}
- Chunk ID: {chunk_id}
- Document Title: {doc_title}
- Category: '{category_name}'
- Subcategory: '{subcategory_name}'
- Subcategory Description: "{subcategory_description}"

DOCUMENT CONTENT:
```
{text_content}
```

INSTRUCTIONS:
1. Analyze how well this financial document page matches the specified category and subcategory.
2. Determine a similarity score (0.000-1.000) where:
   - 0.000-0.299: Not relevant
   - 0.300-0.599: Somewhat relevant
   - 0.600-0.799: Relevant
   - 0.800-1.000: Highly relevant
3. Write a concise description (ONE sentence only) summarizing the page content.
4. Provide brief notes (1-2 sentences) explaining your classification reasoning.
5. Extract 1-5 relevant keywords that support this classification.

RESPONSE FORMAT:
Respond with ONLY a valid JSON object with EXACTLY these fields:
{{
  "category": "{category_name}",
  "subcategory": "{subcategory_name}",
  "similarity_score": 0.XXX,
  "description": "One-sentence summary of page content.",
  "notes": "Brief explanation of classification reasoning.",
  "keywords": ["keyword1", "keyword2", "keyword3", "keyword4", "keyword5"]
}}

IMPORTANT: 
- Return ONLY the JSON object - no other text.
- Ensure similarity_score is a float between 0.000 and 1.000.
- The description MUST be a single sentence.
- Keywords MUST be actually present in the document text.
- EXACT category and subcategory names must be used.

ONLY JSON RESPONSE:
"""

CLASSIFIER_CORRECTION_PROMPT_TEMPLATE = """
Your previous JSON response had errors.

VALIDATION ERROR:
{validation_error}

PREVIOUS FAULTY JSON RESPONSE:
```json
{previous_json_response}
```

Please carefully review the error and the expected JSON format from the original request.
Return ONLY the corrected, valid JSON object. Do not include any other explanatory text.
The corrected JSON should strictly adhere to all field requirements and types specified in the initial task.

CORRECTED JSON RESPONSE:
"""