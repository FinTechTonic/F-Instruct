Modelling Guide
===============

Digital Regulatory Reporting (DRR) involves a community of industry participants to translate reporting rules (as they exist in regulatory texts or other specification documents) into a data model (expressed as formal logic). This process is referred to as *digitising* the rules and the industry members who actively participate in it are known as *digitisers*.

This section presents how the DRR model is being developed to allow digitisers to contribute to it as independently as possible. To this end it describes:

- its design principles
- its scope and organisation
- a set of best practices for writing rules, using examples from the current model

As the DRR model is formally expressed in the Rosetta DSL, readers are advised to familiarise themselves with it and particularly with the `reporting components <https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-modelling-component/#reporting-component>`_ of the language.

Design Principles
-----------------

The DRR model is built as an extension of the `Common Domain Model <https://cdm.finos.org>`_ (CDM), which it uses to represent transaction events as inputs into the reporting process. Broadly speaking, this means that the DRR model adheres to the same `design principles <https://cdm.finos.org/docs/contribution/#design-principles>`_ as the CDM. In the particular context of regulatory reporting, those translate into four design principles:

- Functional
- Composable and reusable
- Auditable
- Test-driven

Functional
^^^^^^^^^^

The DRR model is fully functional. It defines a complete set of logical instructions required by a reporting implementation to process inputs and produce a report. There is no hidden or behind-the-scene logic that an implementor would need, in addition to the one expressed in the model, to build that reporting system. If that implementation uses the model's auto-generated executable code, no further code is required to express the reporting logic - although some coding if of course necessary to integrate what the DRR model provides.

This feature is key to ensuring the consistency and comparability of reported data across the market, i.e. the same inputs would produce the same reporting output regardless of the particularities of each firm's implementation.

In the example below, a reportable field called "Event type" is being extracted based on some classification of the ``BusinessEvent`` input, which is a CDM data type. The DRR model expresses the rule logic using a combination of conditional *if then else* statements applied to the underlying CDM classification logic, such that no other logic would be required by an implementor to report that field.

.. code-block:: Haskell

 extract
   if Qualify_Novation(BusinessEvent) = True or Qualify_PartialNovation(BusinessEvent) = True then "NOVA"
   else if Qualify_Termination(BusinessEvent) = True then "ETRM"
   else if Qualify_ClearedTrade(BusinessEvent) = True then "CLRG"
   else if Qualify_Allocation(BusinessEvent) = True or Qualify_Reallocation(BusinessEvent) = True then "ALOC"
   else if Qualify_Compression(BusinessEvent) = True then "COMP"
   else if Qualify_Exercise(BusinessEvent) = True then "EXER"
   else if Qualify_StockSplit(BusinessEvent) = True then "CORP"
   else if
     Qualify_ContractFormation(BusinessEvent) = True
     or Qualify_PartialTermination(BusinessEvent) = True
     or Qualify_Increase (BusinessEvent) = True
     or Qualify_Renegotiation (BusinessEvent) = True
     or Qualify_IndexTransition(BusinessEvent) = True
     or Qualify_FullReturn(BusinessEvent) = True
     or BusinessEvent -> instruction -> primitiveInstruction -> quantityChange exists
       then "TRAD"
   else if
     Qualify_CashTransfer(BusinessEvent) = True
     or Qualify_CashAndSecurityTransfer(BusinessEvent) = True
     or Qualify_MultipleTransfers(BusinessEvent) = True
     or Qualify_SecurityTransfer(BusinessEvent) = True
       then "PTNG"
   else "ToDo"
   as "27 Event type"

Composable and Reusable
^^^^^^^^^^^^^^^^^^^^^^^

In line with the CDM design principles of normalisation and composability, the DRR model is based on defining re-usable components (rules and functions) which can then be composed into higher-order components.

The ``TradeForEvent`` and ``ProductForTrade`` rules illustrate this approach. As the starting point of the reporting logic is a transaction event, the logic to extract many of the reportable fields requires to access the details of the trade and, further, some of its underlying economic attributes. That logic is implemented once in the ``TradeForEvent`` (respectively ``ProductForTrade``) rule and invoked in each rule that needs to access the trade (respectively the product) details.

Rules are composable, so e.g. the product details can be extracted as shown below:

.. code-block:: Haskell

 TradeForEvent then ProductForTrade

While these two rules are often composed when reporting a trade's economic attributes, both are needed separately because some reportable trade details, such as the parties, are attached to the trade but not to the product.

.. code-block:: Haskell

 TradeForEvent then
   extract Trade -> contractDetails -> partyContractInformation -> relatedParty

Auditable
^^^^^^^^^

One of the benefits of using a model-driven approach for DRR is to provide reporting implementations with auditability all the way back to the regulatory texts. The model allows to associate rich meta-data to any logic component, including precise references to documents and to provisions contained within those documents. This means that a compliance implementation, when based on the executable code automatically generated from the DRR model, can be systematically validated for quality assurance against those provisions.

This meta-data is implemented in DRR using the `document reference <https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-modelling-component/#document-reference>`_ feature of the Rosetta DSL.

.. code-block:: Haskell

 reporting rule ActionType <"Action Type">
   [regulatoryReference CFTC Part45 appendix "1" dataElement "26" field "Action Type"
     provision "Type of action taken on the swap transaction or type of end-of-day reporting. Actions may include, but are not limited to, new, modify, correct, error, terminate, revive, transfer out, valuation, and collateral..."]

In some cases, the regulatory text provision may be insufficient or require clarification to be distilled into exhaustive, unambiguous logic. The model allows additional textual details to be provided as a ``rationale`` to further support the functional logic when existing texts are not sufficient. Typically, those textual details would be the result of working group conversations and may be recorded as minutes. This feature enhances auditability by embedding such content directly as model meta-data.

.. code-block:: Haskell

 [regulatoryReference CFTC Part45 appendix "1" dataElement "22" field "Submitter Identifier"
   rationale "Check whether trade is executed in a SEF first. If it is, SEF is obliged to be the Submitter. Then check for Reporting Party and Data Submitter."
   rationale_author "DRR Peer Review Group - 09/03/22"
   provision "Identifier of the entity submitting the data to the swap data repository (SDR). The Submitter identifier will be the same as the reporting counterparty or swap execution facility (SEF), unless they use a third-party service provider to submit the data to SDR in which case, report the identifier of the third-party service provider."]

Test-Driven
^^^^^^^^^^^

DRR uses a test-driven approach to develop the reporting model. This means that the model is being systematically tested using transaction data inputs, and its reported output validated against an expected result.

The transaction data inputs are synthetic data - i.e. not actual production data but data that are representative of real-life transaction scenarios and that can be used to test the validity of the reporting logic. They are typically provided by firms participating in DRR, after those firms apply suitable anonymisation and data scrambling to preserve privacy. A reporting rule is considered fully developed only once its logic has been verified against relevant test data.

Those data are organised around themes and grouped into *Test Packs* in the DRR model repository - for instance, by asset class. Each test pack contains both the transaction data inputs and their expected output. Those test packs are an integral part of the model and are readily available to allow firms to benchmark their own implementations. When a regulation contains transaction scenarios as part of its technical guidance, to illustrate how the regulator expects transactions to be reported using concrete examples, the DRR model looks to include them in a specific test pack.

This test-driven approach also supports the on-going governance of the DRR model. Any change request to the model needs to be tested against those expectations. By design, any mismatch would generate a build failure in the DRR project, which would need to be either resolved or explained (in case such change is justified).

Scope and Organisation
----------------------

Reporting Regime
^^^^^^^^^^^^^^^^

DRR currently covers the following reporting regimes. The focus is on trade and transaction reporting as those are being upgraded across the G20.

+-------------------+-------------------+-------------------+
| **Authority**     | **Regulation**    | **Version**       |
+===================+===================+===================+
| CFTC              | Part 43           | Rewrite           |
+-------------------+-------------------+-------------------+
| CFTC              | Part 45           | Rewrite           |
+-------------------+-------------------+-------------------+
| ESMA              | EMIR              | Refit             |
+-------------------+-------------------+-------------------+

For every reporting regulation the model associates a set of reference documents and their author, each defined as a ``corpus`` and ``body``, respectively. For example:

.. code-block:: Haskell

 body Authority CFTC <"Commodity Futures Trading Commission (CFTC): The Federal regulatory agency established by the Commodity Futures Trading Act of 1974 to administer the Commodity Exchange Act.">``

 corpus Regulation "CFTC 17 CFR Parts 45" Part45 <"Part 45 of the CFTCs regulations specifies the Commissions swap data recordkeeping and reporting requirements, pursuant to section 2(a)(13)(G) of the Commodity Exchange Act (CEA), which states that all swaps, whether cleared or uncleared, must be reported to a Swap Data Repository (SDR)">``

Other examples of body and corpus include the work done by standard-setting organisations. For instance, the Critical Data Elements (CDE) harmonisation for the reporting of OTC derivatives by the Committee on Payments and Market Infrastructures (CPMI) and the International Organization of Securities Commissions (IOSCO) is referenced as follows:

.. code-block:: Haskell

 body Authority CPMI_IOSCO <"IOSCO and the Committee on Payments and Market Infrastructures (CPMI) work together to enhance coordination of standard and policy development and implementation, regarding clearing, settlement and reporting arrangements including financial market infrastructures (FMIs) worldwide...">
 
 corpus TechnicalGuidance "Harmonisation of Critical Data Elements (other than UTI and UPI)" CDE <"The G20 Leaders agreed in 2009 that all over-the-counter (OTC) derivative transactions should be reported to trade repositories (TRs) to further the goals of improving transparency, mitigating systemic risk and preventing market abuse...">

Namespace
^^^^^^^^^

The DRR model follows the CDM's organising principles into `namespaces <https://cdm.finos.org/docs/namespace>`_.

The DRR model itself belongs to a dedicated ``regulation`` namespace layer in the CDM so DRR namespaces are generally prefixed accordingly. Within DRR, model components are organised by reporting regulation using the following namespace convention.

``drr.regulation.<body>.<regulation>``

Reporting regulations can themselves be further sub-divided, for instance when rules go through an update:

``drr.regulation.esma.emir.refit``

By design since the model is composable, some components are not specific to any regulation and are built to be reusable across several of them. Those components are generally positioned in the ``regulation.common`` namespace.

Common components that are modelling the work carried-out by standard-settings bodies use the following namespace convention:

``cdm.standards.<body>.<standard>``

For example, the namespace containing the functional logic of the harmonised CDE reporting rules is:

``drr.standards.iosco.cde``

As CDE is being implemented in various trade reporting regimes across the G20, these rules can be re-used in each regulation-specific namespace that implements CDE by *importing* that namespace:

.. code-block:: Haskell

 namespace drr.regulation.cftc.rewrite
 
 import drr.standards.iosco.cde.*

Reportable Event
^^^^^^^^^^^^^^^^

Every trade and transaction reporting regime assumes that the input triggering a report is a transaction event. In the DRR model, this is represented by the ``ReportableEvent`` data type.

The CDM uses the ``BusinessEvent`` data type to specify a transaction event as a state transition. A business event is further encapsulated into a ``WorkflowStep`` data type containing workflow-specific concerns such as time-stamp, status or submitting party. While these additional details are not part of the state transition, at least some of them are usually required for reporting purposes.

In addition, a transaction event may need to be enriched with further information that is only relevant in a reporting context. The dedicated ``ReportableEvent`` data type in the DRR model allows to capture this additional, enriched information without overloading either the business event or workflow step concepts in the CDM.

This data type is meant to apply across reporting regimes so it is positioned in the ``drr.regulation.common`` namespace. It is not regulation-specific, so it supports a cross-over of the information needed for different reporting regimes.

.. code-block:: Haskell

 type ReportableEvent: <"Specifies a workflowstep with enriched information required for reporting.">
   [rootType]
   originatingWorkflowStep WorkflowStep (1..1) <"The workflowstep that originated the reportable event.">
   reportableTrade TradeState (0..1) <"The reportable trade decomposed from the originating workflow step when required.">
   reportableInformation ReportableInformation (0..*) <"Additional information required for a reportable transaction, including the reporting regime. A list of reportable information is provided when an event is reportable to more than one regime.">

Report Definition
^^^^^^^^^^^^^^^^^

Each report is specified as three components:

- *What* to report, i.e. reportable fields
- *Whether* to report, i.e. eligibility criteria
- *When* to report, i.e. timing constraint

The reportable fields are defined indirectly by specifying the report's output as a data type whose attributes are the reportable fields. The eligibility criteria is specified by referencing a functional rule that returns a boolean. The timing constraint is only specified as a syntactic indication and does not generate any executable code, so it has no impact on the reporting process. Additionally, each report must refer to a body and corpus as the source of the reporting mandate.

.. code-block:: Haskell

 report CFTC Part43 in T+1
   when ReportableEvent
   using standard ISO_20022
   with type CFTCPart43TransactionReport

.. note:: Currently, no eligibility rule has been developed for the regulations in scope. The ``ReportableEvent`` rule is simply a placeholder for these eligibility rules to be developed in future.

In the report's data type definition, each attribute can be associated to a functional rule via a ``ruleReference`` annotation that defines how to extract or compute that attribute.

.. code-block:: Haskell

 type CFTCPart43TransactionReport:
   [rootType]
   cleared string (1..1)
     [ruleReference Cleared]
   counterparty1 string (1..1)
     [ruleReference Counterparty1]

One of the benefits of modelling the report output as a data type is to allow that report to be validated at source as it is constructed. This is achieved by adding `validation rules <https://cdm.finos.org/docs/process-model#validation-process>`_ as conditions directly into that data type. E.g.

.. code-block:: Haskell

 condition IsCentralCounterpartyReportingParty:
   if cleared="Y" and centralCounterparty exists then centralCounterparty = counterparty1
   else if (cleared="I" or cleared="N") then centralCounterparty is absent

Rule Type
^^^^^^^^^

A rule is the smallest functional unit that can be built to define a report. A rule takes an input, defined as a data type in the DRR model, and returns an output, which is also typed (data type or basic type).

There are two types of rules:

- reporting rule - specified with the ``reporting rule`` keyword. Each attribute of a report's data type is associated to a reporting rule.
- eligibility rule - specified with the ``eligibility rule`` keyword. An eligibility rule must return a boolean.

Both the eligibility rules and reporting rules used to define a report assume that the input is of type ``ReportableEvent`` and syntax validation is in place to ensure that all such rules use the same input type. Rules are `composable <#composable-and-reusable>`_ so those rules can call other rules whose input is located further down inside ``ReportableEvent``.

How to Write Rules
------------------

This section provides some best practice guide on how to write regulatory rules. Users are advised to consult the `rule definition <https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-modelling-component/#rule-definition>`_ section of the Rosetta DSL documentation that details the main components available for writing rules and their purpose.

A rule definition must contain the following components, in that order:

- a name and description
- a regulatory reference - Optional in the syntax, but recommended for auditability
- the functional logic
- the field name - Optional, for reporting rules

An example of a properly defined rule is given below.

.. code-block:: Haskell

 reporting rule FixingDateLeg1 <"Fixing date-Leg 1">
   [regulatoryReference CFTC Part45 appendix "1" dataElement "54" field "Fixing date - Leg 1"
     rationale "Only applies to fixing date of an exchange rate as per definition"
     rationale_author "DRR Peer Review Group - 23/11/21"
     provision "Describes the specific date when a non-deliverable forward as well as various types of FX OTC options such as cash-settled options that will 'fix' against a particular exchange rate, which will be used to compute the ultimate cash settlement"]
   TradeForEvent then
   (
     ProductForTrade then extract InterestRateLeg1( Product ) then extract InterestRatePayout -> settlementTerms
     ,
     ProductForTrade then extract Product -> contractualProduct -> economicTerms -> payout then
     (
       extract Payout -> optionPayout -> settlementTerms,
       extract Payout -> forwardPayout -> settlementTerms
     )
   ) then extract SettlementTerms -> cashSettlementTerms -> valuationDate -> fxFixingDate -> fxFixingDate -> adjustableDate -> unadjustedDate
   as "54 Fixing date-Leg 1"

Name and Description
^^^^^^^^^^^^^^^^^^^^

The name and description should broadly match. Since the description can be any string including spaces and other special characters but the name is constrained by the syntax, the name is usually a condensed version of the description.

For reporting rules, the description should also match the name of the reportable field.

.. note:: By contrast with the CDM's `documentation guidance <https://cdm.finos.org/docs/contribution#documentation-style-guide>`_, the description for regulatory rules appears tautological. That is because any additional meta-data context is meant to be captured by the regulatory reference component.

Regulatory Reference
^^^^^^^^^^^^^^^^^^^^

The regulatory reference component specifies a reference to an external document that should contain a text provision supporting the rule's functional expression. This meta-data component is key to ensuring the auditability of compliance implementations that are based on DRR.

A regulatory reference comprises the following components, all illustrated in the example above:

- a ``body`` and ``corpus`` that identify the document being referenced, both as pre-defined model components
- a set of ``segment`` corresponding to how such document might be indexed, that point to the specific section in the document containing the provision - e.g. ``appendix``, ``dataElement``, ``field`` in the example above
- a ``provision`` containing the actual text of the rule that is translated into functional logic
- (Optional) a ``rationale`` that consists of an off-document explanation supporting the functional logic, in case the source document requires clarification or disambiguation.

The regulatory reference should be as precise as possible and the necessary segments should be defined to point exactly to the document provision. In future, this mechanism should allow automatically to extract the provision from the published document, if that source document provides a feature to access specific sections (e.g. using a URL). Absent such feature, the text of the provision should be simply copied and pasted in the regulatory reference.

Segments also provide an indexing mechanism for rules in the DRR model that is aligned onto the original indexing in the reference document. It allows easily to answer questions such as: "How does DRR comply with provision X?" that may be coming from auditors or regulators, by simply querying the model meta-data and linking it to the functional logic and ultimately executable code.

The rationale component provides further auditability by recording interpretation decisions that may be required to express certain rules into unambiguous functional logic.

Functional Logic
^^^^^^^^^^^^^^^^

The rule's functional logic consists of an expression in the Rosetta DSL. This expression applies to an input and must returns an output. It can be composed of multiple expressions that are executed sequentially in accordance with the Rosetta DSL's operator precedence rules.

The rule's input and output types are implicit and as implied by the expression's input and output types. One typical rule starting point is an ``extract`` statement followed by a path expression that starts from a data type: in this case the rule's input type is that data type. Another example is an expression that starts by invoking another rule, in which case the input type corresponds to that other rule's input type.

In the ``FixingDateLeg1`` rule example above, the input type is ``ReportableEvent``, which is the input type of the ``TradeForEvent`` rule invoked at the start of the logical expression. That ``TradeForEvent`` rule itself invokes another rule called ``TradeStateForEvent`` where the ``ReportableEvent`` input type is directly specified.

The output type of the TradeStateForEvent rule is a ``TradeState``, as implied by that rule's functional expression, and that of ``TradeForEvent`` is a ``Trade``.

.. code-block:: Haskell

 reporting rule TradeForEvent
   TradeStateForEvent then
   extract TradeState -> trade
   as "Trade"

.. code-block:: Haskell

 reporting rule TradeStateForEvent
   extract
     if ReportableEvent -> reportableTrade exists then
       ReportableEvent -> reportableTrade
     else if ReportableEvent -> originatingWorkflowStep -> businessEvent -> instruction exists
       then ReportableEvent -> originatingWorkflowStep -> businessEvent -> after
     else if ReportableEvent -> originatingWorkflowStep -> businessEvent -> primitives -> contractFormation -> after -> trade only exists
       then ReportableEvent -> originatingWorkflowStep -> businessEvent -> primitives -> contractFormation -> after
     else if ReportableEvent -> originatingWorkflowStep -> businessEvent -> primitives -> quantityChange -> after -> trade  exists
       then ReportableEvent -> originatingWorkflowStep -> businessEvent -> primitives -> quantityChange -> after
     else if ReportableEvent -> originatingWorkflowStep -> businessEvent -> primitives -> execution -> after -> trade  exists
       then ReportableEvent -> originatingWorkflowStep -> businessEvent -> primitives -> execution -> after
     else ReportableEvent -> originatingWorkflowStep -> businessEvent -> primitives -> contractFormation -> after
   as "TradeState"

.. note:: The ``FixingDateLeg1`` rule is expected to use ``ReportableEvent`` as input type because it is a field rule that is referenced in the report type's attribute definition.

Critical Data Elements
""""""""""""""""""""""

The Critical Data Elements (CDE) are recommendations published by the BIS's CPMI-IOSCO working group and aimed at harmonising the reporting of OTC derivatives across the G20. It specifies the key required attributes of OTC derivative transactions and how they should be reported.

While all jurisdictions have not fully adopted CDE, most are integrating large portions of these recommendations into their own reporting regimes. For this reason, much of the reporting logic is implemented once as common CDE logic and used as such in individual regulations.

The example below illustrates this approach on the "Counterparty1 (reporting counterparty)" field. The extraction logic is implemented as a CDE rule in the ``drr.standards.iosco.cde`` namespace and refers to the CDE document corpus. Then each of the corresponding CTFC and EMIR rules simply invoke the CDE rule, with an appropriate regulatory reference pointing to the CFTC and EMIR corpuses, respectively.

.. code-block:: Haskell

 reporting rule CDECounterparty1 <"Counterparty 1 (reporting counterparty)">
   [regulatoryReference CPMI_IOSCO CDE section "2" field "6"
     provision "Identifier of the counterparty to an OTC derivative transaction who is fulfilling its reporting obligation via the report in question. In jurisdictions where both parties must report the transaction, the identifier of Counterparty 1 always identifies the reporting counterparty. In the case of an allocated derivative transaction executed by a fund manager on behalf of a fund, the fund and not the fund manager is reported as the counterparty."]
   TradeForEvent then
     extract Trade -> contractDetails -> partyContractInformation -> relatedParty then
     filter when RelatedParty -> role = PartyRoleEnum -> ReportingParty then
     filter when RelatedParty -> partyReference -> partyId -> scheme contains "http://www.fpml.org/coding-scheme/external/iso17442" then
     extract RelatedParty -> partyReference -> partyId

.. code-block:: Haskell

 reporting rule Counterparty1 <"Counterparty 1 (reporting counterparty)">
   [regulatoryReference CFTC Part45 appendix "1" dataElement "13" field "Counterparty 1 (reporting counterparty)"
     provision "Identifier of the counterparty to an OTC derivative transaction who is fulfilling its reporting obligation via the report in question. In jurisdictions where both parties must report the transaction, the identifier of Counterparty 1 always identifies the reporting counterparty. In the case of an allocated derivative transaction executed by a fund manager on behalf of a fund, the fund, and not the fund manager is reported as the counterparty."]
   CDECounterparty1
   as "13 Counterparty 1"

.. code-block:: Haskell

 reporting rule Counterparty1 <"Counterparty 1 (reporting counterparty)">
   [regulatoryReference ESMA EMIR Refit table "1" field "4"
     provision "Identifier of the counterparty to a derivative transaction who is fulfilling its reporting obligation via the report in question. In the case of an allocated derivative transaction executed by a fund manager on behalf of a fund, the fund and not the fund manager is reported as the counterparty."]
   CDECounterparty1
   as "1.4 Counterparty 1 (reporting counterparty)"

.. note:: Typically the text of the provision for the reportable field is aligned between the CDE definition and the regulations that implement it. However it corresponds to different segments in those regulations' respective corpuses.

Fields for Different Legs
"""""""""""""""""""""""""

Swaps are a type of OTC derivative product that typically feature two legs, where the receiving party on one is the paying party on the other and conversely. Where relevant, a number of data attributes need to be reported for each leg identified as "Leg 1" and "Leg 2" but the CDE specification is silent on how Leg 1 and Leg 2 should be determined.

In this case, the CDE logic is implemented at the leg level. Then in each regulation, the field reporting logic for each of Leg 1 and Leg 2 begins with determining the leg before applying the CDE logic. This approach is illustrated below for "Notional Amount Leg 1" in the CFTC rules (and the same logic with ``Leg2`` substituted for ``Leg1`` would apply to the corresponding "Leg 2" field).

.. code-block:: Haskell

 reporting rule NotionalAmountLeg1 <"Notional Amount Leg 1">
 [regulatoryReference CFTC Part45 appendix "1" dataElement "31" field "Notional Amount"
   provision "For each leg of the transaction, where applicable:
     - for OTC derivative transactions negotiated in monetary amounts, amount specified in the contract.
     - for OTC derivative transactions negotiated in non-monetary amounts, refer to appendix B for converting notional amounts for non-monetary amounts.
     In addition:
     - For OTC derivative transactions with a notional amount schedule, the initial notional amount, agreed by the counterparties at the inception of the transaction, is reported in this data element.
     - For OTC foreign exchange options, in addition to this data element, the amounts are reported using the data elements Call amount and Put amount.
     - For amendments or lifecycle events, the resulting outstanding notional amount is reported; (steps in notional amount schedules are not considered to be amendments or lifecycle events);
     - Where the notional amount is not known when a new transaction is reported, the notional amount is updated as it becomes available."]
   TradeForEvent then
   (
     CDENotional
     as "31 Notional amount-Leg 1"
     ,
     ProductForTrade then
       extract
         if IsSwaption( Product ) then
           InterestRateLeg1( UnderlierForProduct( Product )  )
         else
           InterestRateLeg1( Product )
         then
           CDEInterestRateNotional
         as "31 Notional amount-Leg 1"
       ,
       extract FXLeg1( Trade ) then
       extract Cashflow -> payoutQuantity -> resolvedQuantity then
       CDEFXNotional
       as "31 Notional amount-Leg 1"
     ,
     ProductForTrade then
       extract CommodityLeg1( Product ) then
       CDECommodityNotional
       as "31 Notional amount-Leg 1"
   )

Leg Determination Logic
"""""""""""""""""""""""

The leg determination logic itself is the object of industry best practice, illustrated below for ``InterestRateLeg1``. In this case, the corpus being referenced is not the regulation but the best practice guide, when published.

.. code-block:: Haskell

 func InterestRateLeg1:
   [regulatoryReference ISDA EMIRReportingBestPractice
     table "ESMA reporting best practices matrix March 2020" provision "Best Practice For Leg 1 / Leg 2 Determination and population of Counterparty Side for EMIR RTS 2.0"]
   inputs: product Product (1..1)
   output: interestRateLeg1 InterestRatePayout (0..1)
   set interestRateLeg1:
     if IsInterestRateFixedFloatSingleCurrency( product ) then
       InterestRateLeg1FixedFloatSingleCurrency( product )
     else if IsInterestRateCrossCurrency( product ) then
       InterestRateLeg1CrossCurrency( product )
     else if IsInterestRateFixedFixed( product ) then
       InterestRateLeg1FixedFixed( product )
     else if IsInterestRateBasis( product ) then
       InterestRateLeg1Basis( product )
     else if IsCapFloor( product ) then
       InterestRateLeg1CapFloor( product )

.. note:: The leg determination logic has been captured into a function instead of a rule, using the corresponding ``func`` syntax that is slightly different from the ``rule`` syntax. The Rosetta DSL allows functions to be invoked inside rules and to be associated to regulatory references too.

Field name
^^^^^^^^^^

The field name is a string specified with the keyword ``as`` at the end of a rule logic. That string should match the field name as defined in the regulation, usually preceded by the field number - e.g. "54 Fixing date-Leg1" in the example above.

This component can be used by reporting implementations as a displaying device to annotate the reported values.
