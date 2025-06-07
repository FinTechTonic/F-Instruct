REFERENCE_FORMAT_TEMPLATE = """
<reference_document_{index}>
# {title}
{content}
</reference_document_{index}>
"""

CATEGORY_TEMPLATES = {
    "question_answering": {
        "input_template": "PAGE:\n{context}\n\nDESCRIPTION: {description}\n\nNOTES: {notes}\n\nQUESTION: {question}",
        "output_template": "{answer}",
        "instructions": "Answer the question based on the provided page. Be accurate, concise, and informative.",
        "questions": [
            "What are the key components of {keyword} mentioned in the page?",
            "How does the document define {keyword}?",
            "What requirements does the document specify for {keyword}?",
            "What are the implications of {keyword} according to the document?",
            "Can you explain the process of {keyword} as described in the document?"
        ]
    },
    "instruction_following": {
        "input_template": "FINANCIAL DOCUMENT:\n{context}\n\nDESCRIPTION: {description}\n\nNOTES: {notes}\n\nINSTRUCTION: {instruction}",
        "output_template": "{result}",
        "instructions": "Follow the given instruction precisely using information from the financial document.",
        "tasks": [
            "Extract all mentions of {keyword} from the document and organize them in a structured format.",
            "Summarize the key points about {keyword} in 3-5 bullet points.",
            "Transform the information about {keyword} into a table format with appropriate headers.",
            "Write a Python function that implements the {keyword} calculation described in the document.",
            "Create a structured outline of the document's content related to {keyword}."
        ]
    },
    "financial_product_creation": {
        "input_template": "REFERENCE DOCUMENT:\n{context}\n\nDESCRIPTION: {description}\n\nNOTES: {notes}\n\nTASK: {task}",
        "output_template": "{product_definition}",
        "instructions": "Create a structured financial product definition based on the reference document.",
        "tasks": [
            "Define a structured derivative product based on {keyword} with appropriate parameters and terms.",
            "Create an options contract with terms and conditions related to {keyword}.",
            "Define a swap contract incorporating {keyword} with specific parameters for pricing and settlement.",
            "Structure a bond instrument that includes {keyword} features and characteristics.",
            "Generate a term sheet for a financial product related to {keyword} with all required specifications."
        ]
    },
    "regulatory_compliance": {
        "input_template": "REGULATORY DOCUMENT:\n{context}\n\nDESCRIPTION: {description}\n\nNOTES: {notes}\n\nCOMPLIANCE TASK: {task}",
        "output_template": "{compliance_output}",
        "instructions": "Complete the compliance task based on the regulatory document provided.",
        "tasks": [
            "Identify all regulatory requirements related to {keyword} in the document.",
            "Assess compliance with financial regulations concerning {keyword} based on the document.",
            "Provide detailed guidance on regulatory reporting for {keyword} as specified in the document.",
            "Conduct a risk assessment for {keyword} according to the regulatory framework described.",
            "Generate compliance documentation for {keyword} that meets the requirements outlined in the document."
        ]
    }
}

GENERATOR_PROMPTS = {
    "question_answering": """
## Role
You are a financial domain expert specializing in creating high-quality question-answer pairs for training data. Your goal is to create precise, informative questions and comprehensive answers that demonstrate deep financial knowledge.

## Input Structure
<primary_page>
{text}
</primary_page>

<page_description>
{description}
</page_description>

<additional_notes>
{notes}
</additional_notes>

<additional_references>
{references}
</additional_references>

<task_description>
Create detailed and specific questions about {subcategory_description} that can be answered from the primary page, potentially supplemented by the reference materials. Focus on {keywords} as the main topic.
</task_description>

## Guidelines
1. Analyze the primary page thoroughly to identify key concepts, regulatory requirements, financial mechanisms, or procedural details.
2. Formulate questions that target critical information in the text and demonstrate understanding of financial concepts.
3. Craft comprehensive answers that are precise, factually accurate, and directly supported by the provided content.
4. When relevant, incorporate supporting information from reference materials to enhance answers, but ensure the primary page remains the main source.
5. Focus on {keywords} as central elements in your questions.
6. Avoid overly simplistic questions - aim for thoughtful inquiries that demonstrate financial expertise.

## Output Format
Your response should be in this exact format:
QUESTION: [Your specific question about the primary page content]
ANSWER: [Your comprehensive answer based on the page and references]
""",

    "instruction_following": """
## Role
You are a financial domain expert creating instruction-following examples for training data. Your role is to develop precise instructions and demonstrate their execution for financial content processing.

## Input Structure
<primary_document>
{text}
</primary_document>

<document_description>
{description}
</document_description>

<additional_notes>
{notes}
</additional_notes>

<reference_materials>
{references}
</reference_materials>

<task_description>
Create an instruction related to {subcategory_description} that can be completed using the document, followed by the detailed result of following that instruction. Focus on {keywords} as the main topic.
</task_description>

## Guidelines
1. Thoroughly analyze the primary document to identify suitable instruction opportunities.
2. Create a clear, specific instruction that requires understanding of financial concepts.
3. The instruction should be related to {subcategory_description} and focused on {keywords}.
4. Ensure your instruction is meaningful within the financial domain and practical to execute.
5. In the result, demonstrate precisely how the instruction should be followed, providing thorough, structured output.
6. Use the reference materials to enhance the quality of your result when appropriate.
7. Ensure both instruction and result showcase professional financial domain knowledge.

## Output Format
Your response should be in this exact format:
INSTRUCTION: [Your specific instruction related to the document content]
RESULT: [The detailed result of following the instruction based on the document and references]
""",

    "financial_product_creation": """
## Role
You are a financial product specialist creating structured product definition examples for training data. Your expertise lies in designing financial instruments that are technically sound and market-appropriate.

## Input Structure
<primary_document>
{text}
</primary_document>

<document_description>
{description}
</document_description>

<additional_notes>
{notes}
</additional_notes>

<supplementary_materials>
{references}
</supplementary_materials>

<task_description>
Create a task prompt for {subcategory_description} that can be completed using the document, followed by a detailed product definition. Focus on {keywords} as the main product feature.
</task_description>

## Guidelines
1. Analyze the primary document to identify financial concepts, market conditions, or regulatory requirements that could inform product design.
2. Create a realistic task that would be encountered in financial product development.
3. The task should be focused on {subcategory_description} and highlight {keywords} as a central feature.
4. Your product definition should be comprehensive, including:
   - Product structure and mechanics
   - Key terms and conditions
   - Pricing considerations
   - Risk characteristics
   - Regulatory considerations
5. Incorporate information from supplementary materials where relevant to enhance the product definition.
6. Ensure the product is technically viable and adheres to financial industry standards.
7. Be precise with financial terminology and product specifications.

## Output Format
Your response should be in this exact format:
TASK: [Your specific task for creating a financial product definition]
PRODUCT DEFINITION: [A detailed, structured definition of the financial product based on the document and references]
""",

    "regulatory_compliance": """
## Role
You are a regulatory compliance expert creating compliance task examples for training data. Your expertise is in interpreting financial regulations and providing comprehensive compliance guidance.

## Input Structure
<regulatory_document>
{text}
</regulatory_document>

<document_description>
{description}
</document_description>

<additional_notes>
{notes}
</additional_notes>

<related_materials>
{references}
</related_materials>

<task_description>
Create a compliance task related to {subcategory_description} that can be completed using the document, followed by the compliance output. Focus on {keywords} as the main regulatory focus.
</task_description>

## Guidelines
1. Carefully analyze the regulatory document to identify compliance requirements, obligations, or procedures.
2. Create a realistic compliance task that financial professionals would need to address.
3. The task should focus on {subcategory_description} with particular attention to {keywords}.
4. Your compliance output should be comprehensive, including:
   - Interpretation of applicable regulations
   - Specific compliance requirements
   - Implementation guidance
   - Documentation or reporting needs
   - Risk considerations
5. Use related materials to provide additional context or supporting information when appropriate.
6. Ensure technical accuracy and precision in regulatory terminology.
7. Structure the output in a way that would be useful for compliance professionals.

## Output Format
Your response should be in this exact format:
COMPLIANCE TASK: [Your specific compliance task related to the document content]
COMPLIANCE OUTPUT: [Detailed compliance analysis, guidance, or documentation based on the document and references]
"""
}

SYSTEM_MESSAGES = {
    "standard": "You are a financial domain expert helping to create high-quality training data for a language model. Respond in the exact format requested.",
    "creative": "You are a creative financial domain expert crafting diverse and insightful training examples. While maintaining accuracy, explore different angles and approaches. Respond in the exact format requested.",
    "technical": "You are a technical financial domain expert focusing on precise, detailed financial concepts and calculations. Your responses should be technically accurate and thorough. Respond in the exact format requested.",
    "regulatory": "You are a regulatory compliance expert with deep knowledge of financial regulations. Focus on accurate interpretation of rules, requirements, and compliance procedures. Respond in the exact format requested."
}

CONVERSATION_OPENERS = {
    "question_answering": """I'll help you generate high-quality question-answer pairs on financial topics. I'll carefully analyze both the primary content and any reference materials to create thoughtful questions and comprehensive answers.""",
    
    "instruction_following": """I'll create instruction-following examples that demonstrate financial expertise. I'll design clear instructions and show exactly how they should be executed using the provided materials.""",
    
    "financial_product_creation": """I'll develop financial product definitions that are technically sound and market-appropriate. I'll analyze the provided materials to inform product features, terms, and specifications.""",
    
    "regulatory_compliance": """I'll craft compliance tasks and solutions that reflect regulatory expertise. I'll interpret the provided materials to create realistic compliance scenarios and comprehensive guidance."""
}
