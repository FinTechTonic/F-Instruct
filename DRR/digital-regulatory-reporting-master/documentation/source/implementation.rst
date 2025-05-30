Implementation
==============

Reporting firms can use the DRR output for their reporting implementation along 3 approaches that can be combined: *Build*, *Benchmark* and *Buy*.

- **Build**. A firm uses the open-source DRR model components and executable code artefacts to develop its own internal implementation.

  - They develop a run-time execution engine that sits on top of the reporting rules and deploy it on their infrastructure.
  - They integrate the DRR code artefacts into their software lifecycle management.
  - They develop their own translation from their internal data formats.
  - They use the Test Pack for quality assurance, running the input data through their implementation and comparing against the expected output.

- **Benchmark**. A firm uses the testing capabilities that are freely available under the Community Edition of the Rosetta Platform supporting the DRR and the CDM more broadly to validate their own reporting implementation.

  - These services are designed in the context of the program to support firms’ testing, proof-of-concept or benchmarking of their own implementations, but not production reporting systems – they can only accommodate limited volume and throughput.
  - These services cover all of the *Translate* / *Enrich* / *Transform* / *Project* steps and are available both via a web interface or API.
  - The *Translate* and *Project* services only cover the formats that have been publicly developed and distributed in the CDM and DRR, not firms’ custom formats.

- **Buy**. A firm buys a reporting solution from a third-party vendor. That third-party vendor itself may have followed the “Build” approach to develop their commercial product (rather than an internal reporting system) based on the DRR output.

The next 3 sections focus on the *Build* approach. They detail the artefacts included in the DRR output for each of the *Translate*, *Enrich*, *Transform* and *Project* steps and how reporting firms can use them to develop their implementation. In each case, details of how reporting firms can use the equivalent *Benchmark* option in Rosetta for testing purposes are also provided.

Translate
---------

Why
^^^

- To transform an internal messaging format into a CDM transaction event object. This CDM object is required as an input to the DRR reporting rules.

What
^^^^

- Model-to-model mappings are available as compact ``synonym`` in the CDM distribution *for public models only*.
- The list of supported public models can be found at: https://cdm.finos.org/docs/mapping.
- Custom adaptations of public models would require these synonyms to be extended.

Where
^^^^^

- Synonyms are contained in the CDM distribution in ``.rosetta`` format. All synonym files in the distribution are contained in the ``cdm.mapping.*`` namespaces and prefixed accordingly.

How
^^^

Build
"""""

- Synonyms provide a functional specification for how to perform model-to-model mapping. The semantics of synonyms is documented at: https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-modelling-component/#mapping-component.
- Synonyms are distributed in machine-readable format so that implementors can use them as input to code-generate their translation’s executable code (in the same way as the CDM’s executable code is generated).
- A guide for writing a code generator is at: https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-code-generators/.
- Depending on their internal model, implementors have three approaches to build their translation:

  - If they use a public model, they should write their own code generator to generate the translation’s executable code from the machine-readable synonym specification.
  - If they use a custom adaptation of a public model, they should extend that translation’s implementation by either:

    - extending the synonyms first (as documented at: https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-modelling-component/#synonym-source), then code-generating from them, or
    - directly extending the executable code that they generate from the public model synonyms.

  - If they use a fully bespoke model that is not adapted from a public one, they will not be able to use the public synonyms and will need to create their own custom translation implementation.

Benchmark
"""""""""

- An ingestion service that translates *from a publicly available model only* into CDM is available on the Rosetta Platform and documented here: https://docs.rosetta-technology.io/rosetta/rosetta-products/4-api-export/#ingestion-service.

Enrich
------

Why
^^^

- To enrich a CDM transaction event with additional information obtained from an internal or external reference data source. This step is necessary because transaction data originating from front-office systems usually do not include all the static data required for reporting.
- The enrichment framework in DRR is designed to be highly flexible to allow implementors to choose which data sources they want to use and how such data extraction should be implemented.

What
^^^^

- Enrichment functions are annotated as ``[enrichment]`` in the model. Their input and output must be of the same type and single cardinality. The purpose of such enrichment function is to populate the input object with additional information. 
- An enrichment function typically extract relevant attributes on the input object and calls external API(s) based on those attributes to retrieve additional information from external sources. This additional information is then used to populate further attributes on the input object. Pre- and post-conditions built into the function allow to validate how those attributes are populated.
- Any call to an external API is itself wrapped into a function annotated as ``[external_api]`` in the model.
- The model does not define the logic of the API call but its inputs and output are fully modelled. This means that they can be used as model components to write any required business logic - e.g. applying some validation rules to check that the attributes are valid or consistent with the transaction details.

Where
^^^^^

- Enrichment functions and external API call functions are present in the model's ``.rosetta`` files and annotated as ``[enrichment]`` and ``[external_api]``, respectively.
- These functions may be positioned anywhere in the model and do not need to be in a specific namespace.

How
^^^

Build
"""""

- The DRR Java code containing the enrichment and API call functions can be added as a code dependency (e.g. using maven or gradle) or downloaded from the Rosetta application, as explained below.
- The enrichment and external API functions are distributed in the DRR Java code as *interfaces*, which implementors are meant to develop according to their own business requirements and implementation choices.

Benchmark
"""""""""

- The Rosetta Platform provides some built-in implementation of the enrichment and external API functions, allowing users to automatically enrich data when developing and testing their regulatory logic. Examples of reference data for which a built-in API call is provided include:

  - Legal Entity Identifier (from GLEIF)
  - Market Identifier Code (from ISO)

- Implementations of the external API calls are not distributed with DRR as these functions' logic is not defined in the model.

Transform
---------

Why
^^^

- To generate a reportable output object in CDM format based on a CDM transaction event input.

What
^^^^

- A report is defined by 3 components: what (report fields), whether (eligibility) and when (timing). The “what” is represented by a CDM data type whose attributes are the reportable fields.
- Each reportable field is associated with a ``reporting rule`` component representing the logic to extract or compute that field from a CDM transaction event object.
- The DRR distribution contains a library component that takes the name of the report and a CDM transaction event object as input and returns a report object. That library is available as a Java JAR and compiled with Java 11, which is required for use.

Where
^^^^^

- The report and rule definitions are available as ``.rosetta`` files in the DRR distribution. The files are contained in the ``drr.regulation.*`` namespaces and prefixed accordingly – e.g. ``drr.regulation.cftc.rewrite``.
- Alignment of code generated DRR rules (``reporting rule``) with Rosetta functions (``func``). This facilitates the implementors’ building their own execution engine, by providing a single way of using the CDM’s business logic as executable code.

How
^^^

Build (test only)
"""""""""""""""""

- The DRR Java code containing the reporting rules can be either:

  - downloaded from the Rosetta application – see: https://docs.rosetta-technology.io/rosetta/rosetta-products/1-workspace/#download-workspace, or
  - added as a code dependency (e.g. using maven, as illustrated below, or gradle). In particular, this dependency gives access to the generated Java class representing a specific report object – e.g. ``CFTCPart45TransactionReport``.

.. code-block:: XML

 <dependency>
   <groupId>com.regnosys.drr</groupId>
     <artifactId>rosetta-source</artifactId>
     <version>LATEST</version>
 </dependency>


- To execute a report for a particular DRR version requires the following steps:

  - Note the name of the report including the namespace:

    - namespace - the namespace for the report, e.g., ``"drr.regulation.cftc.rewrite"``
    - body – the body of the report that this class will generate, e.g. ``"CFTC"``
    - corpus list – a list of corpus for the report that this class generates, e.g. ``"Part45"``

  - Create the Guice module used to initialise the report functions. The convention for the DRR is: ``DrrRuntimeModuleExternalApi.class``.
  - Create the report function for the report.

    - The class will have been automatically generated by the DSL.

    .. code-block:: Java

     CFTCPart45ReportFunction function = injector.getInstance(CFTCPart45ReportFunction.class)


  - Create the tabulator for the report.

    - The class will have been automatically generated by the DSL.

    .. code-block:: Java

     CFTCPart45ReportTabulator tabulator = injector.getInstance(CFTCPart45ReportTabulator.class);


  - Create an input CDM object representing the transaction (always a ``RosettaModelObject`` sub-type, e.g. ``ReportableEvent``) by either:

    - converting JSON to Java by using a Jackson Object Mapper (the ``RosettaObjectMapper`` utility helps set things up),
    - creating a ``ReportableEvent`` by Ingesting Record Keeping FpML or other external models into the CDM,
    - creating a ``ReportableEvent`` using Java code, or
    - extracting a ``ReportableEvent`` from a CDM native implementation.

  - Run the report based on that input CDM object (``inputData``), to return a structured object, according to the report’s specified data type.

    .. code-block:: Java

     CFTCPart45TransactionReport report = function.evaluate(reportableEvent);


  - Generate a table, i.e. a list of key-value pairs.

    .. code-block:: Java

     List<Tabulator.FieldValue> tabulatedReport = tabulator.tabulate(report);


Below is a full example using the “CFTC Part 45” reporting regime:

.. code-block:: Java

 package com.regnosys.drr.examples;

import cdm.base.staticdata.party.CounterpartyRoleEnum;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.rosetta.model.lib.reports.Tabulator;
import drr.regulation.cftc.rewrite.CFTCPart45TransactionReport;
import drr.regulation.cftc.rewrite.reports.CFTCPart45ReportFunction;
import drr.regulation.cftc.rewrite.reports.CFTCPart45ReportTabulator;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import drr.regulation.common.functions.Create_TransactionReportInstruction;
import drr.regulation.common.functions.ExtractTradeCounterparty;

import java.io.IOException;
import java.util.List;

public class CFTCPart45ExampleReport {

    public static void main(String[] args) throws IOException {
        // 1. Deserialise a ReportableEvent JSON from the test pack
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(ReportableEvent.class, "regulatory-reporting/input/events/New-Trade-01.json");

        // Run report
        CFTCPart45ExampleReport cftcPart45ExampleReport = new CFTCPart45ExampleReport();
        cftcPart45ExampleReport.runReport(reportableEvent);
    }

    private final Injector injector;

    CFTCPart45ExampleReport() {
        this.injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
    }

    void runReport(ReportableEvent reportableEvent) throws IOException {
        // TransactionReportInstruction from ReportableEvent and ReportingSide
        // For this example, arbitrarily PARTY_1 as the reporting party and PARTY_2 as the reporting counterparty
        final ReportingSide reportingSide = ReportingSide.builder()
                .setReportingParty(getCounterparty(reportableEvent, CounterpartyRoleEnum.PARTY_1))
                .setReportingCounterparty(getCounterparty(reportableEvent, CounterpartyRoleEnum.PARTY_2))
                .build();
        final Create_TransactionReportInstruction createInstructionFunc = injector.getInstance(Create_TransactionReportInstruction.class);
        final TransactionReportInstruction reportInstruction = createInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Run the API to produce a CFTCPart45TransactionReport
        final CFTCPart45ReportFunction reportFunc = injector.getInstance(CFTCPart45ReportFunction.class);
        final CFTCPart45TransactionReport report = reportFunc.evaluate(reportInstruction);
        // Print
        System.out.println(RosettaObjectMapper.getNewRosettaObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        // Get the API tabulator function
        final CFTCPart45ReportTabulator tabulator = injector.getInstance(CFTCPart45ReportTabulator.class);
        // Run the API to extract key value pairs from the report
        final List<Tabulator.FieldValue> tabulatedReport = tabulator.tabulate(report);
        // Print
        System.out.println(RosettaObjectMapper.getNewRosettaObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(tabulatedReport));
    }

    private ReferenceWithMetaParty getCounterparty(ReportableEvent reportableEvent, CounterpartyRoleEnum party) {
        ExtractTradeCounterparty func = injector.getInstance(ExtractTradeCounterparty.class);
        return func.evaluate(reportableEvent, party).getPartyReference();
    }
}

.. note:: This code is available for download as part of the DRR distribution. See the Downloads page here: https://drr.docs.rosetta-technology.io/source/download.html.

Project
-------

Why
^^^

- To convert a CDM reportable output object into a message that is ready to be sent to a Trade Repository.

What
^^^^

XML output for ISO 20022
""""""""""""""""""""""""

In the last step from above, we converted the CDM report object to JSON using the following code.

.. code-block:: Java

 RosettaObjectMapper.getNewRosettaObjectMapper()
     .writerWithDefaultPrettyPrinter()
     .writeValueAsString(report);

However, the ISO 20022 standard requires the output to be serialised as XML. As an example, the following code can be
used to serialise an ``iso20022.auth108.esma.Document`` object to XML.

.. code-block:: Java

 URL xmlConfig = Resources.getResource("xml-config/auth108-esma-rosetta-xml-config.json");
 RosettaObjectMapperCreator
     .forXML(xmlConfig.openStream())
     .create()
     .writerWithDefaultPrettyPrinter()
     .writeValueAsString(document);

The JSON file ``auth108-esma-rosetta-xml-config.json`` defines the necessary metadata to ensure that
the output conforms exactly to the ISO ``auth.108.001.01.xsd`` XML schema file. It is available as a resource in the
following Maven artifact.

.. code-block:: XML

 <dependency>
     <groupId>org.iso20022</groupId>
     <artifactId>rosetta-source</artifactId>
     <version>LATEST</version>
 </dependency>

.. note:: To serialise a ``iso20022.auth030.esma.Document`` object to XML, the configuration file ``xml-config/auth030-esma-rosetta-xml-config.json`` should be used instead.

Where
^^^^^

- The projection functions are available as ``.rosetta`` files in the DRR distribution. The files are contained in the ``drr.projection.*`` namespaces and prefixed accordingly – e.g. ``drr.projection.iso20022.esma.emir.refit.trade``.
- Alignment of code generated Projection Report (``Project_EsmaEmirTradeReportToIso20022``) with Rosetta functions (``func``). This facilitates the implementors’ building their own execution engine, by providing a single way of using the CDM’s business logic as executable code.

Benchmark
"""""""""

-  A reporting service that packages all the above steps is available on the Rosetta Platform and documented here: https://docs.rosetta-technology.io/rosetta/rosetta-products/4-api-export/#regulation-report-service

