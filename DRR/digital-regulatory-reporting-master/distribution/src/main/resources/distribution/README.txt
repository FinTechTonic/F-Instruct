Users of the distribution are encouraged to start with the Digital Regulatory Reporting and CDM documentation to understand the organisation of the model and its components.

The Digital Regulatory Reporting (DRR) distribution is organised into the folders below.  Please also see ISDA-DRR-License-Version-1.0.pdf for licence terms.

/documentation
Digital Regulatory Reporting (DRR) documentation - https://drr.docs.rosetta-technology.io/
CDM documentation - https://cdm.finos.org/
Rosetta documentation, including syntax - https://docs.rosetta-technology.io/#/

/model
This set of rosetta files describe the Digital Regulatory Reporting (DRR) model.  The files use the .rosetta extension as that carries special meaning in the Rosetta DSL.  To view the files simply use the text editor of your choice as these are in effect, plain text files.

/translate
The Rosetta DSL provides a mechanism for specifying how documents in other formats (e.g. FpML) should be translated to Rosetta model. Mappings are specified as synonym annotations in the model.
- /xml - Sample files in an external data format, such as FpML (xml).
- /json - Sample files translated to DRR model and serialised into JSON format.

/test-pack
The reporting rules specified in the DRR project are tested against this set of Reportable Event samples.

/lib
The generated Java artefacts of the CDM and DRR, including the generated Java source code.

/lib/dependencies
Contains all upstream dependencies necessary to compile and run the generated Java code.