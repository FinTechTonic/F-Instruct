**Common Domain Model- An Overview Sep 2024**

![](_page_0_Picture_1.jpeg)

![](_page_1_Picture_1.jpeg)

## All parties store trade data in different formats & make lifecycle changes to these records inconsistently **Differences in booking models lead to real**

![](_page_1_Figure_3.jpeg)

What is the true "truth" at any point in time?

**world events in those models producing different outcomes:**

- **Reconciliation breaks**
- **Valuation differences**
- **Collateral disputes**
- **Reporting mismatches**
- **Operational inefficiency**
- **Settlement failures**
- **Barriers to automation**

![](_page_2_Picture_1.jpeg)

The Common Domain Model (CDM) is a standardised, machine-readable and machine-executable blueprint for how financial products are traded and managed across the transaction lifecycle.

## Dimensions of the CDM:

![](_page_2_Figure_4.jpeg)

The CDM is **NOT** an application in and of itself, but can be implemented within one **Composability** allows for re-use of components for efficiency

![](_page_3_Picture_1.jpeg)

While both CDM & FpML are standards, they can and will co-exist

- CDM is not a data format for messaging or storage, it is a logical model describing relationships between pieces of data
- CDM can be expressed in various forms including XML, JSON and other standard formats such as FpML, FIX & ISO20022 for exchange and storage of information
- FpML does not define standards for event and workflow processing, CDM prescribes the validation logic to express these more specifically

## Benefits- Consistency of representation

![](_page_4_Picture_1.jpeg)

![](_page_4_Figure_2.jpeg)

![](_page_5_Picture_1.jpeg)

### **Efficiency**

**Enhance interoperability, reduce reconciliations and promote straightthrough processing**

### **Transparency**

**Promote transparency and alignment between regulators and market participants**

### **Accelerated Innovation**

**Create an environment for innovation in financial markets**

- q A **mutualised free open-source standardised digital blueprint** on how to represent financial transactions, performance and business events.
- q **Extensible** to compose financial instruments by assembling reusable components. Already covers robustly derivative and securities financial transactions.
- q **Scalable** as event-driven model that encapsulates primitive components that will de facto make the fabric of complex business and operational processes.
- q **Operational and functional** to codify the contract mechanics and business logic of legal agreements.
- q **Unambiguous** in digitising functionally complex business and regulatory logic into code.
- q **Directly approachable** as published in both **human readable and machine executable languages.**
- q **Implementable across several strategic uses cases** in capital markets for better automation and greater consistency e.g. Trade management systems, clearing, digital documentation, collateral managements, regulatory reporting.

![](_page_6_Picture_1.jpeg)

Further Repo & Bond enhancements

|                                                                                  | 2017<br>ISDA Publishes<br>CDM Design Paper<br>RFQ for Technology<br>Partner |                                                      | Mar<br>2019<br>CDM 2.0 released<br>Interest Rate and Credit<br>Derivatives<br>Initial Margin CSA<br>Opened access to market |                                                                      | Jun 2021<br>First non-derivatives<br>by ISLA: | product class contributed<br>Securities Lending MVP                      | Nov/Dec 2022<br>First production use:<br>DRR for CFTC reporting<br>CDM 3.0<br>Further product, event and<br>workflow coverage | Jul 2023<br>CDM 4.0 | ICMA contributes Repos                                                               |
|----------------------------------------------------------------------------------|-----------------------------------------------------------------------------|------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|-----------------------------------------------|--------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|---------------------|--------------------------------------------------------------------------------------|
| 2017<br>2016                                                                     | 2018                                                                        | 2019                                                 | 2020                                                                                                                        | 2021                                                                 |                                               | 2022                                                                     | 2023                                                                                                                          |                     |                                                                                      |
| Sep 2016<br>ISDA publishes white<br>paper on Future of<br>Derivatives Processing |                                                                             | Jun 2018<br>Version 1.0 released<br>Conceptual pilot |                                                                                                                             | 2020<br>Addition of all IM<br>& VM CSAs<br>FX and Equity derivatives |                                               | Nov 2021<br>MoU-<br>ISDA, ICMA, ISLA<br>Enshrine Collaboration<br>on CDM | Feb 2023<br>CDM migrated to FINOS<br>Open-source community                                                                    |                     | Q4 2023<br>CDM 5.0<br>ETD & Commodity<br>Derivatives<br>ISLA GMSLA Clause<br>Library |

Increasing momentum through industry contributions

# Trade Association Collaboration

![](_page_7_Picture_1.jpeg)

![](_page_7_Figure_2.jpeg)

## **Associations are collaborating towards the same future goal, to benefit the whole industry**

- An open-source model mutualises cost of development between TAs and contributing firms while retaining best practice governance
- MoU in 2021 enshrined collaboration publicly
- Working groups were opened to each others' members
- TAs appointed FINOS to provide a repository with a view to fostering the growth of an open-source community for the CDM, with migration completed early 2023

# Product Coverage

![](_page_8_Picture_1.jpeg)

The scope of contractual products in the current model are summarized below:

- **Interest rate derivatives**:
	- Interest Rate Swaps (incl. cross-currency swaps, non-deliverable swaps, basis swaps, swaps with non-regular periods, ...)
	- Swaptions
	- Caps/floors
	- FRAs
	- OTC Options on Bonds
- **Credit derivatives**:
	- Credit Default Swaps (incl. baskets, tranche, swaps with mortgage and loans underliers, ...)
	- Options on Credit Default Swaps
- **Equity derivatives**:
	- Equity Swaps (TRS, PRS, single name/index/basket, VarSwap, VolSwap, Dispersion, Correlation, Dividend Swap)
	- Options & Forwards
- **Foreign Exchange derivatives:**
	- FX Swap, Forward, NDF, Options
- **Commodity derivatives**:
	- Swaps, options, swaptions
- **Exchange Traded derivatives**

The use of common elements allow for representation of multiple types of products and events in the trade workflow with minimal incremental work. Thus, this coverage list does not represent an exhaustive list of all possible combinations of elements or events

# Product & Event Coverage

![](_page_9_Picture_1.jpeg)

The scope of contractual products and events in the current model are summarized below:

- **Securities Lending**:
	- Single underlier, cash collateralised, open/term security loan
- **Repurchase Agreements**:
	- Open Term, Fixed Term, Fixed Rate, Floating Rate
- **Events**:
	- Allocation, Re-allocation
	- Cash, Security transfers, DVP settlement
	- Clearing events
	- Compression
	- Increase and decreases/returns
	- Novations- full, partial
	- Terminations- full, partial
	- Renegotiation
	- Reset
	- Execution
	- Stock Split
	- Index Transition
	- Determination of corporate action and credit events

The use of common elements allow for representation of multiple types of products and events in the trade workflow with minimal incremental work. Thus, this coverage list does not represent an exhaustive list of all possible combinations of elements or events

# Legal Document Coverage

**2019 Clearstream SA (Security-taker) (Lux Law)**

![](_page_10_Picture_1.jpeg)

| ISDA Documentation                                | CDM | ISDACreate | ISDACreate/<br>CDM<br>Compatible | ISDA Documentation                                                   | CDM            | ISDACreate | ISDACreate/<br>CDM<br>Compatible |
|---------------------------------------------------|-----|------------|----------------------------------|----------------------------------------------------------------------|----------------|------------|----------------------------------|
| INITIAL MARGIN                                    |     |            |                                  | VARIATION MARGIN                                                     |                |            |                                  |
| 2016 ISDA IM CSD (English Law)                    |     |            | x                                | 2016 ISDA CSA (VM) (Loan - Japanese Law)                             |                | x          | x                                |
| 2016 ISDA IM CSA (NY Law)                         |     |            |                                  | 2016 ISDA CSA (VM) (Security Interest - New York Law)                |                |            | x                                |
| 2016 ISDA IM CSA (Japanese Law)                   |     |            | x                                | 2016 ISDA CSA (VM) (Title Transfer - English Law)                    |                |            | x                                |
| 2018 ISDA IM CSA (NY Law)                         |     |            |                                  | 2016 ISDA CSA (VM) (Title Transfer - French law)                     |                | x          | x                                |
| 2018 ISDA IM CSD (Eng Law)                        |     |            |                                  | 2016 ISDA CSA (VM) (Title Transfer - Irish law)                      |                | x          | x                                |
| 2019 ISDA Bank Custodian CTA                      |     |            |                                  | 1994 ISDA Credit Support Annex VM (Security Interest - New York Law) | In Development |            | x                                |
| 2019 ISDA Bank Custodian SA (NY Law)              |     |            |                                  | 1995 ISDA Credit Support Annex VM (Title Transfer - English Law)     | In Development |            | x                                |
| 2019 ISDA Bank Custodian SA (Eng Law)             |     |            |                                  | 1995 ISDA Credit Support Annex (Security Interest - Japanese Law)    | x              | x          | x                                |
| 2019 ISDA Bank Custodian SA Luxembourg Law        |     |            | x                                | 1995 ISDA Credit Support Deed (Security Interest - English Law)      | In Development |            | x                                |
| 2020 ISDA Bank Custodian SA Belgium Law           |     |            | x                                | ISDA MASTER AGREEMENT                                                |                |            |                                  |
|                                                   |     |            |                                  | 1992 ISDA Master Agreement                                           | See below      |            | x                                |
| 2016 Euroclear SA (Bel Law)                       |     |            | x                                | 2002 ISDA Master Agreement                                           | See below      |            | CP details only                  |
| 2017 Euroclear CTA (NY Law)                       |     |            | x                                | Automatic Early Termination ("AET")                                  |                |            | x                                |
| 2017 Euroclear CTA (Eng Law)                      |     |            | x                                | Address for Notices                                                  |                |            | x                                |
| 2018 Euroclear CTA (Eng Law)                      |     |            | x                                | Dated as of Date                                                     |                |            | x                                |
| 2018 Euroclear CTA (NY Law)                       |     |            | x                                | Credit Support Provider                                              |                |            | x                                |
| 2018 Euroclear SA (Bel Law)                       |     |            | x                                | Credit Support Document                                              |                |            | x                                |
| 2019 Euroclear CTA                                |     |            |                                  | Governing Law                                                        |                |            | x                                |
| 2019 Euroclear SA (Bel Law)                       |     |            | x                                | Specified Entity                                                     |                |            | x                                |
|                                                   |     |            |                                  | Termination Currency                                                 |                |            | x                                |
| 2016 Clearstream CTA (Eng Law)                    |     |            | x                                |                                                                      |                |            |                                  |
| 2016 Clearstream CTA (NY Law)                     |     |            | x                                |                                                                      |                |            |                                  |
| 2017 Clearstream SA (Lux Law)                     |     |            | x                                |                                                                      |                |            |                                  |
| 2016 Clearstream SA (Lux Law)                     |     |            | x                                |                                                                      |                |            |                                  |
| 2019 Clearstream CTA                              |     |            |                                  | ISLA have also contributed their Clause Library and                  |                |            |                                  |
| 2019 Clearstream SA (Security-provider) (Lux Law) |     |            |                                  |                                                                      |                |            |                                  |

**Taxonomy for the GMSLA 2000/2010**

![](_page_11_Picture_1.jpeg)

|                            | Title/Topic                                  | Chair                                              | Q1                                                                                                                                                                                                                                                                                                     | Q2                                                                                                                                                  | Q3                                                                              | Q4                                                                            |  |  |  |  |
|----------------------------|----------------------------------------------|----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|-------------------------------------------------------------------------------|--|--|--|--|
|                            | Steering WG                                  | David Shone, ISDA                                  | - Complete governance updates                                                                                                                                                                                                                                                                          | - Annual Review of governance- end June<br>- Establish release schedule and process                                                                 |                                                                                 |                                                                               |  |  |  |  |
| s<br>p<br>u<br>o<br>r<br>G | Technical Architecture WG                    | Chris Rayner, ISLA                                 | - Release build process moves to Github<br>Actions                                                                                                                                                                                                                                                     | - Release build process moves to Github<br>Actions<br>-- Redesign Testing Process<br>- Vision Statement<br>- Prioritise initiatives                 | '- Serialisation Phase 1 begins<br>- Ref data list management phase<br>1/2- TBC | - Serialisation Phase 1 complete<br>- Ref data list management<br>phase 3 TBC |  |  |  |  |
|                            | Contribution Review WG                       | Rotating- trade associations                       | - Establish Release management<br>process/bed in resources: Release Manager<br>& Engineer<br>- Release approval & review                                                                                                                                                                               | - Release approval & review                                                                                                                         | - Release approval & review                                                     | - Release approval & review                                                   |  |  |  |  |
|                            | -->Cross-product Modelling                   | N/A                                                | - Product model changes:<br>-- Asset & Observable refactoring [supports<br>structured products and strategic fix for sec<br>finance qualifications]<br>-- Define product with contractdetails<br>-- Redesign Product Qualification to separate<br>economic qualification from product<br>qualification | - Product model changes:<br>-- Asset & Observable refactoring [supports<br>structured products and strategic fix for sec<br>finance qualifications] | '- Product model changes:<br>-- Harmonise date/timestamp                        |                                                                               |  |  |  |  |
| g<br>n                     | Collateral WG<br>Vernon Alden-Smith, ISDA    |                                                    | - Extend ECS model<br>Ongoing adoption support & WG prioritised items<br>- Repo collateral extension                                                                                                                                                                                                   |                                                                                                                                                     |                                                                                 |                                                                               |  |  |  |  |
| ki                         | Securities Lending WG                        | Chris Rayner, ISLA                                 | Ongoing adoption support and WG prioritised items                                                                                                                                                                                                                                                      |                                                                                                                                                     |                                                                                 |                                                                               |  |  |  |  |
| or<br>W                    | Derivatives Product and Business Event<br>WG | David Shone, ISDA                                  | - Migrate to FINOS governance umbrella<br>- Option payout refactoring (ETD/OTC)<br>- Product enhancements driven by DRR<br>- Member modelling proposals: Equity Swaps                                                                                                                                  | - Member modelling proposals                                                                                                                        | - Member modelling proposals                                                    | - Member modelling proposals                                                  |  |  |  |  |
|                            | Structured Products WG                       | Jean-Baptiste Ziade, Fragmos<br>Chain              | Ongoing structured product enhancements & WG prioritised items                                                                                                                                                                                                                                         |                                                                                                                                                     |                                                                                 |                                                                               |  |  |  |  |
|                            | ICMA Repo/Bonds WG                           | Gabriel Callsen, ICMA                              | Ongoing adoption support & WG prioritised items                                                                                                                                                                                                                                                        |                                                                                                                                                     |                                                                                 |                                                                               |  |  |  |  |
|                            | Securities Finance Reg Reporting?            | TBC                                                |                                                                                                                                                                                                                                                                                                        |                                                                                                                                                     |                                                                                 |                                                                               |  |  |  |  |
|                            | ISDA Legal Agreement WG                      | Vernon Alden-Smith, ISDA<br>Ciaran McGonagle, ISDA | - Analyse and develop framework for<br>remaining 20 clauses of legacy CSA<br>agreements<br>- Model 10 clauses of legacy CSAs analysed<br>in 2023                                                                                                                                                       | Analyse and develop framework for remaining<br>20 clauses of legacy CSA agreements                                                                  | Complete legacy CSAs in CDM and<br>create test data                             | Complete legacy CSAs in CDM<br>and create test data                           |  |  |  |  |
|                            | ISDA DRR                                     | Eleanor Hsu, ISDA<br>Tabish Ahmed, ISDA            | - DRR 4.0 Release: Complete coverage EMIR<br>and JFSA<br>- FCA dev complete target 30th March                                                                                                                                                                                                          | - EMIR /JFSA Compliance Dates [1st/29th<br>April]<br>- ASIC & MAS dev complete target                                                               | - FCA compliance date- 30th Sept                                                | - ASIC/MAS Compliance date<br>21st October                                    |  |  |  |  |

![](_page_12_Picture_1.jpeg)

|                                                                                              | Title/Topic      | Chair | Q1                                                                                               | Q2                                                                                                                                                    | Q3                                                                                               | Q4                         |
|----------------------------------------------------------------------------------------------|------------------|-------|--------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|----------------------------|
| k<br>or<br>w<br>e<br>m<br>a<br>Fr<br>ort<br>p<br>p<br>u<br>S<br>n<br>o<br>pti<br>o<br>d<br>A | Documentation    |       | - Onboard shared documentation resource<br>-Model Documentation: Securities Lending<br>Use Cases | - Model documentation: Pre-trade securities<br>lending user guide                                                                                     | - Model documentation: GMSLA<br>user guide                                                       |                            |
|                                                                                              | Website          |       | - All agreed governance updated on FINOS<br>website                                              | - Addition of recorded demos                                                                                                                          | - Consistency and accuracy<br>exercise- website/github                                           |                            |
|                                                                                              | Support material |       | - Consistently branded overviews                                                                 | - Collateral getting started guide<br>- Training course development<br>- Tiered overviews<br>- Business case templates<br>- Reference implementations | -Expand getting started guides to<br>other use cases<br>- Develop certification/award<br>system? |                            |
| Events                                                                                       |                  |       | - CDM Showcase London Feb 28<br>- Informa Trade & Transaction reporting Conf<br>5th March        | - London OSFF - June 26<br>- ISDA AGM Tokyo 16-18th April<br>- ISLA Annual Conf Geneva 18-20 June                                                     | - New York OSFF - Sept 30, Oct 1                                                                 | - ISLA Post-Trade- Oct TBC |

# ISDA Extensions to CDM

![](_page_13_Picture_1.jpeg)

![](_page_13_Figure_2.jpeg)

![](_page_14_Picture_0.jpeg)

**Use Cases**

![](_page_14_Picture_2.jpeg)

# Collateral - Today's Challenge

![](_page_15_Picture_1.jpeg)

**Guidelines outlined under BCBS/IOSCO and Basel III were translated by each regulatory regime spearheading collateral management as a key function in capital markets for both bilateral and cleared OTC. Compliance has increased processing volumes significantly and will continue to do so, the need for automation in collateral management processing. The industry is faced with many challenges which has led to fragmented implementations and operational inefficiencies.**

**CalculationDependencies Margin Monitoring Margin Allocation Documentation Collateral Segregation Establishing CustodyAccounts Eligibility Schedules Risk Control Optimisation Regulatory Compliance Reconciliation DisputeManagement Efficient Settlement**

![](_page_15_Figure_4.jpeg)

## **INDUSTRY PARTICIPANTS**

![](_page_15_Picture_6.jpeg)

- § Loss of inter-operability between solutions
- § Pervasive reconciliation issues and other operational inefficiencies

# Collateral- Documentation Model Representation 2020/2021

![](_page_16_Picture_1.jpeg)

## Collateral- Benefits of CDM Standard Documentation

![](_page_17_Picture_1.jpeg)

![](_page_17_Figure_2.jpeg)

![](_page_18_Picture_1.jpeg)

- 
- 
- 
- 
- 

|     | Items of Eligible Collateral (IM) and Eligible<br><b>Currencies</b> |  | [In respect of<br>Party A's<br>posting<br>obligation |  | [In respect of<br>Party B's posting<br>obligation]                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |  | <b>[Valuation</b><br>Percentagel |  |
|-----|---------------------------------------------------------------------|--|------------------------------------------------------|--|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--|----------------------------------|--|
| (A) | $\mathbf{1}$                                                        |  | $\left[ -1 \right]$                                  |  | [1]                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |  | $[$ 1%                           |  |
| (B) | $\begin{bmatrix} \\ \\ \\ \\ \end{bmatrix}$                         |  | $\Box$                                               |  | $\begin{bmatrix} 1 \end{bmatrix}$                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |  | 196                              |  |
| (C) | 1                                                                   |  | $\left[ \begin{array}{c} 1 \end{array} \right]$      |  | $\begin{bmatrix} 1 \end{bmatrix}$                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |  | 196                              |  |
| (D) | $\mathbf{1}$                                                        |  | $\Box$                                               |  | $\left[ -1 \right]$                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |  | 196                              |  |
|     | [FX Haircut Percentage]                                             |  |                                                      |  | [In respect of Party A's posting obligation: [8]% [.<br>unless the Eligible Collateral (IM) is denominated in the<br>Termination Currency specified with respect to Party B<br>under the Agreement (including, without limitation,<br>pursuant to this Annex), in which case, 0%.]<br>[In respect of Party B's posting obligation: [8]% [.<br>unless the Eligible Collateral (IM) is denominated in the<br>Termination Currency specified with respect to Party A<br>under the Agreement (including, without limitation,<br>pursuant to this Annex), in which case, 0%.] |  |                                  |  |
|     |                                                                     |  |                                                      |  | With respect to Party A: [].<br>With respect to Party B: [].                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |  |                                  |  |
|     | [Termination Currency] <sup>10</sup>                                |  |                                                      |  | In relation to a calculation pursuant to Section<br>6(e)(ii)(2) in respect of an Early Termination Date                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |  |                                  |  |

|                  | <b>Remaining Maturity</b> |                                                                        |                                                                       |                            |  |  |  |  |
|------------------|---------------------------|------------------------------------------------------------------------|-----------------------------------------------------------------------|----------------------------|--|--|--|--|
| <b>[CAD Code</b> | One (1) year<br>or under  | More than<br>one $(1)$ year up<br>to and including<br>five $(5)$ years | More than<br>five $(5)$ years up<br>to and including<br>ten(10) years | More than<br>ten(10) years |  |  |  |  |
| A-CA-GOV         |                           |                                                                        |                                                                       |                            |  |  |  |  |
| A-TBILL          | 93%                       | N/A                                                                    | N/A                                                                   | <b>N/A</b>                 |  |  |  |  |
| A-BOND           | 97%                       | 97%                                                                    | 95%                                                                   | 93%                        |  |  |  |  |
| A-RRB            | 98%                       | 96%                                                                    | 94%                                                                   | 92%                        |  |  |  |  |
| A-US-GOV         |                           |                                                                        |                                                                       |                            |  |  |  |  |
| S-TBILL          | 98%                       | N/A                                                                    | N/A                                                                   | N/A                        |  |  |  |  |
| S-TNOTE          | 98%                       | 97%                                                                    | 95%                                                                   | 93%                        |  |  |  |  |
| S-TBOND          | 98%                       | 97%                                                                    | 95%                                                                   | 93%                        |  |  |  |  |
|                  |                           |                                                                        |                                                                       |                            |  |  |  |  |

![](_page_18_Figure_12.jpeg)

|                    |                                | <b>Eligibility criteria</b>        |                           |                                                            |                           |  |        |           |      |
|--------------------|--------------------------------|------------------------------------|---------------------------|------------------------------------------------------------|---------------------------|--|--------|-----------|------|
| Order              | Field                          |                                    | Oper                      | Value                                                      | Outcome                   |  |        |           |      |
| 1                  | Security Types                 | $\equiv$                           |                           | Bond, Equity                                               | Accepted                  |  |        |           |      |
| $\overline{2}$     |                                | Counterparty Own Issue<br>$\equiv$ |                           | Yes                                                        | Not eligible              |  |        |           |      |
| 3                  | Asset Types                    | $\equiv$                           |                           | Cash                                                       | Not eligible              |  |        |           |      |
| 4                  | <b>Bond Risk Profiles</b><br>٠ |                                    |                           | Sovereign, Agency, Structured, Corporate, Convertible bond | Accepted                  |  |        |           |      |
| 5                  | M asset class. EU<br>$\alpha$  |                                    |                           | C.D.E.F.G.H.L.J.K.L.N.Q-NFI,Q-FI                           | Eligible                  |  |        |           |      |
| IM asset class. US |                                | $\equiv$                           | 2.3.4.5-a.5-b.6.7.8-a.8-b |                                                            |                           |  |        |           |      |
|                    | Final outcome                  |                                    |                           | If none of the above criteria have been met                | Not eligible              |  |        |           |      |
|                    |                                | <b>Haircut criteria</b>            |                           |                                                            |                           |  |        |           |      |
| Group              | Order                          | Field                              | Oper                      | Value                                                      | Outcome                   |  |        |           |      |
| 1                  | 1                              | Security Currency                  | Not in                    | EUR                                                        | 8%                        |  |        |           |      |
|                    |                                | IM asset class. EU                 | $\alpha$                  | C.D.E.H.L.J.K                                              |                           |  |        |           |      |
| $\overline{z}$     | Applied Rating                 |                                    |                           | Time To Maturity Security                                  |                           |  | $^{4}$ | 12 Months | 0.5% |
|                    |                                |                                    | ٠                         | AAA LT, AA+ LT, AA LT, AA- LT                              |                           |  |        |           |      |
|                    |                                | IM asset class. EU                 | $\alpha$                  | C.D.E.H.L.J.K.                                             |                           |  |        |           |      |
| $\boldsymbol{2}$   | 2                              | Time To Maturity Security          | $\overline{\phantom{a}}$  | 12 Months                                                  | 2%                        |  |        |           |      |
|                    |                                | Time To Maturity Security          | $\leq$                    | 60 Months                                                  |                           |  |        |           |      |
|                    |                                | Applied Rating                     | $\scriptstyle\rm II$      | AAA LT, AA+ LT, AA LT, AA- LT                              |                           |  |        |           |      |
|                    |                                | IM asset class. EU                 | $\alpha$                  | C, D, E, H, I, J, K                                        |                           |  |        |           |      |
| $\overline{2}$     | з                              | Time To Maturity Security          | $\mathbf{r}$              | 60 Months                                                  | 4%                        |  |        |           |      |
|                    |                                | Applied Rating                     | $\mathbf{r}$              | AAA LT, AA+ LT, AA LT, AA- LT                              |                           |  |        |           |      |
|                    |                                | <b>Masset class FU</b>             | $\mathbb{R}$              | C.D.E.H.L.J.K                                              |                           |  |        |           |      |
| $\overline{z}$     | è                              | Applied Rating                     | $\overline{z}$            | A+ LT, A LT, A- LT, BBB+ LT, BBB LT, BBB- LT               | 1%                        |  |        |           |      |
|                    |                                | Time To Maturity Security          | $\epsilon$ z              | 12 Months                                                  |                           |  |        |           |      |
|                    |                                | <b>Concentration limits</b>        |                           |                                                            |                           |  |        |           |      |
| Limit Type Limit   |                                | Granularity                        | Field                     | Value<br>Open                                              | <b>Basis</b>              |  |        |           |      |
| Max                | 15.00 %                        | Per UPI<br>10.000.000.00 EUR       |                           | M asset class. EU<br>F. G. L. N. Q-NFI<br>٠                | Contract Collateral Basis |  |        |           |      |

## Collateral- Eligible Collateral Schedules

![](_page_19_Picture_1.jpeg)

![](_page_19_Figure_2.jpeg)

![](_page_20_Picture_1.jpeg)

## **ISDA CDM will offer the flexibility to identify collateral asset types, with particular focus on securities, as most common form found in collateralschedules. However,this can be extended to cover many otherassets.**

![](_page_20_Figure_3.jpeg)

including specific issuer name and use of common identifiers

Other issuer typesinclude:

- Sovereign Central Banks
- Corporate
- Supranational Debt
- SPV and Funds

![](_page_21_Picture_1.jpeg)

## **CDM offers standard data references points required for many industry forms of ECS. The structure enables consistent expression of data with the ability to apply various include/exclude rules and complex concentration limits. ISDA has demonstrated translation of several ECS provided by members into digital output**

- 
- 
- 
- 
- 
- 
- 

![](_page_21_Figure_11.jpeg)

|     | Items of Eligible Collateral (IM) and Eligible<br><b>Currencies</b> | [In respect of<br>Party A's<br>posting<br>obligation]         | [In respect of<br>Party B's posting<br>obligation]                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       | [Valuation<br>Percentage] |  |  |  |
|-----|---------------------------------------------------------------------|---------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|--|--|--|
| (A) | $\begin{bmatrix} \end{bmatrix}$                                     | [ ]                                                           | $\begin{bmatrix} \end{bmatrix}$                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | $[$ 1%                    |  |  |  |
| (B) | $\lceil$ $\rceil$                                                   | $\lceil$ $\rceil$                                             | $\Box$                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | $[$ $]$ %                 |  |  |  |
| (C) | $\begin{bmatrix} \end{bmatrix}$                                     | $\lceil$ $\rceil$                                             | $\begin{bmatrix} \end{bmatrix}$                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | $\lceil$ 1%               |  |  |  |
| (D) | $\begin{bmatrix} \end{bmatrix}$                                     | $\lceil$ 1                                                    | $\lceil$ $\rceil$                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | $[$ 1%                    |  |  |  |
|     | [FX Haircut Percentage]                                             |                                                               | [In respect of Party A's posting obligation: [8]% [.<br>unless the Eligible Collateral (IM) is denominated in the<br>Termination Currency specified with respect to Party B<br>under the Agreement (including, without limitation,<br>pursuant to this Annex), in which case, 0%.]<br>[In respect of Party B's posting obligation: [8]% [.<br>unless the Eligible Collateral (IM) is denominated in the<br>Termination Currency specified with respect to Party A<br>under the Agreement (including, without limitation,<br>pursuant to this Annex), in which case, 0%.] |                           |  |  |  |
|     |                                                                     | With respect to Party A: [ ].<br>With respect to Party B: []. |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |                           |  |  |  |
|     | [Termination Currency] <sup>10</sup>                                |                                                               | In relation to a calculation pursuant to Section<br>6(e)(ii)(2) in respect of an Early Termination Date<br>resulting from a Termination Event where there are<br>two Affected Parties: [ ].                                                                                                                                                                                                                                                                                                                                                                              |                           |  |  |  |

![](_page_22_Picture_1.jpeg)

An Eligible Collateral Schedule is represented in the CDM through the specification of criteria that can be used to "filter" whether a piece of collateral is eligible or not.

Asset Type – is used to specify criteria related to the nature of the asset, such as its type (cash, equity, debt, etc), country of origin or denominated currency

Issuer Type – is used to specify criteria related to the issuer of the asset, such the type of issuer (government, corporate, etc), specific issuer name, or agency rating

Treatment – is used to specify the valuation percentage, any concentration limits and whether the criteria specify inclusion or exclusion conditions

The combination of these terms allows a wide variety of eligible collateral types to be represented and can be applied across industry use cases for OTC, Securities Lending, Repo, Cleared and ETD. s

Solving problems for Global Banks, Custodians, Data Providers, Vendors and connecting solutions.

Standard data for Eligible Collateral information facilitates DLT, Smart Contract and technology to be built to add further efficiencies to processes

# Collateral- CDM Object Builder

![](_page_23_Picture_1.jpeg)

Vendors **REGnosys on behalf of ISDA have developed a user interface (UI) this allows you to create CDM Eligible Collateral schedule information using drop down functions. The user can create, import, share or inspect in CDM JSON and view in a tabular format. The Object Builder will be contributed to FINOS in 2023**

![](_page_23_Figure_3.jpeg)

**The UI can be used for predefined common eligibility profiles to import and edit and producing industry compatible consumable data output. The current UI gives the user the ability to also validate and construct many version of eligible collateral as CDM data and has the scope to be development further and built into services for use cases beyond collateral**

![](_page_24_Picture_0.jpeg)

## **Commitment to CDM Integration: Digital Documentation SLIDE 1**

![](_page_24_Picture_2.jpeg)

**2021/22 – TechnicalIntegration work with ISDA Create completed, and CDM standard format IM documentation available via CreateAPI**

**2019- 2023 – Continued support and input on CDM Collateral related representations Focus – VM & IM CSA, CSD and IM CTA, ISDA Master. Support CDM build for Legacy VM CSA** 

**Q3 2024 – Analysis phase** 

**2021/22 – Workshops to assess compatibility, first-stage mapping and analysis for IM CSA H2 2023 – Development in COBRA for CDM IM CSA ingestion to COLLINE Q1 2024 – CDM connector (IM CSA) released to production in COLLINE ready for client use Q2 2024 - Ongoing discussions with [clients to de](mailto:valdensmith@isda.org)ploy connector into production.**

**2023 - Representation mapping and analysis for supporting selected CSA types feed in CDM format completed. Focus shifted to ECS for H1 2024 H2 2024 – Continue analysis and mapping to prepare for ingestion status. Potential to connect to service providers able to support** 

**CDM**

![](_page_24_Picture_11.jpeg)

**2023 – Mapping for CDM IM CSA coverage completed to deliver integration into Murex (CDM vs MX.3) First version of CDM agreement import available (without eligibility schedule and legacy fields) 2024 – Extend upon IM CSA CDM availability with mapping for Legacy CSAs once delivered to CDM**

![](_page_24_Picture_13.jpeg)

**Focus – CDM for representing IM, VM, Legacy CSA and Master Agreement data 2022/23 – Investigation into ability to round trip CDM data between Lyncs and ISDA Create for IM CSA 2024 – Ensure compatibility of CDM with internal model for Legacy VM CSA inc complex clauses. Production status/ timeline dependent on clients interested in using CDM**

![](_page_25_Picture_0.jpeg)

## **Commitment to CDM Integration: Digital Documentation SLIDE 2**

![](_page_25_Picture_2.jpeg)

**Focus – using CDM standards to link the library of composable contracts into an asset class agnostic automated lifecycle platform.** 

**2023/24 - for Interest Rate Swaps and Verified Carbon Credits.**

**2025 - creating a golden record of highfidelity trade data to reduce operational pain points on reconciliation and streamline back-office processes.**

![](_page_25_Picture_6.jpeg)

**Focus – CDM mappings for an adapter for collateral agreement data. 2024 – Review and update current mappings for IM, VM and Legacy CSA agreements**

![](_page_26_Picture_0.jpeg)

## **Commitment to CDM Integration: Eligible Collateral Representation SLIDE 1**

**2019- 2023 – Continued support and input on CDM Collateral related representations Focus – Eligible Collateral and concentration attributes (in legacy/regulatory CSA/CTA docs and Triparty ECS. Ability to compare and reconcile eligible collateral and concentration representations from any CMS or Collateral service providers Q3 2024 – Analysis phase** 

**Focus – Delivery of an ingestion mechanism for all Eligibility terms in CDM format**

**H2 2023 / 2024 – Integration analysis completed in 2023, including workshops held with CDM. ECS mapping exercise in progress, to enable complex eligibility terms ingestion into CloudMargin in CDM format** 

**Focus – Build CDM translator for Eligible Collateral terms for 2- way client transfer via API 2021/2 – Phase1 ECS representation mapping analysis completed 2022/23 – Stage 2 technical mapping into application import/export functionality 2023/24 – Functionality release pending launch; dependent on connecting firms to support CDM**

![](_page_26_Picture_9.jpeg)

**2024 – Extension for Eligible Collateral in 2024, internal effort to re-build eligibility model leveraging CDM foundational structure** 

![](_page_26_Picture_11.jpeg)

**2022/2 3 – Continued support, input to workshops and contribution to CDM Eligible Collateral terms/conditions 2024 – Analysis for integration and model mapping of collateral eligibility terms, and pilot test with connecting client for POC.** 

![](_page_26_Picture_13.jpeg)

**2024 – As part of the full representation of legal agreement, the CDM Eligible Collateral schedule use case would be in scope for clients wishing to connect using CDM. Logical Construct will continue to support through CDM working groups**

![](_page_27_Picture_0.jpeg)

## **Commitment to CDM Integration: Eligible Collateral Representation SLIDE 2**

![](_page_27_Picture_2.jpeg)

**Focus –Export collateral representation data from Ark51 to the CDM, continue with WG to ensure data is captured correctly.**

**2025 – Expand AI capacity of our application to extract collateral information automatically for exporting to CDM**

**Focus – Established linkage between CDM collateral model and FIA Tech's Eligible Collateral schema, allowing interoperability for end users to evaluate eligible collateral assets on a range of global CCPs supported within FIA Tech's ETD focused data set. Continued interest in further building out mappings for a production feed for end user consumption but seeking support and feedback from subscribers to prioritize resource for the build out.**

**Focus – combining CDM standards, to build composable contracts, with tokenisation of real-world assets enabling near real time eligible collateral mobility within an automated lifecycle platform. 2023/24 - for Interest Rate Swaps and Verified Carbon Credits. 2025 - expand scope to cover derivatives in other asset classes including repo trades and [using CDM to](mailto:valdensmith@isda.org) deliver a golden record of high-fidelity trade data to reduce operational pain points on reconciliation**

# Collateral- CDM Margin Call / Positions / Balances and Exposure

![](_page_28_Picture_1.jpeg)

## **Data to support the Collateral Margin Call process and its related components for Collateral Balance , Collateral Positions and Exposure are now represented in the CDM.**

**A foundationalstructure to support the data required for the margin call process including:**

- o **Standard margin call action labels**
- o **Base details for margin call data types and attributesto support unique featuresfor issuance and response**
- o **Collateral positions and ability to list collateral assets for responding to margin demands and for information purposes**
- o **Collateral balance data requirements and aggregate values for margin call data and reporting**

![](_page_28_Figure_8.jpeg)

# Collateral- Margin Call connection to other CDM components

![](_page_29_Picture_1.jpeg)

![](_page_29_Figure_2.jpeg)

![](_page_30_Picture_1.jpeg)

# **2023/2024 - CDM Collateral Initiatives**

**Objectives:**

**Documentation Extensions: 1995 VM CSA ISDA Master Agreement AmendmentAgreements**

**Support Adoption of CDM Documentation and ECSinto Production Environments of External Platforms**

**Validate CDM DataStructure for Margin Call Issuance and Response Standards**

**Engage with Membersfor Support and Adoption**

**Collaboration with other Trade Associations to extend CDM**

**Repo and Securities Lending CollateralProcess**

# Digital Regulatory Reporting

**Trade Reporting Rule Implementation Today**

- 
- 
- 
- 
- 

![](_page_32_Picture_1.jpeg)

### **Trade Reporting Rule Implementation Using the DRR**

![](_page_32_Figure_3.jpeg)

![](_page_33_Picture_1.jpeg)

### **Trade Reporting Rule Implementation Using the DRR**

**Mutualize regulatory reporting compliance effort**

• Rule interpretations and compliance effort is spread across the industry

**Gives you an unambiguous rule interpretation**

• Reflects rules, guidance and industry best practices in an unambiguous way within the DRR model

### **DRR is open-access and increases transparency**

• The DRR will be accessible to regulators and market participants

### **Defines core regulatory reporting ruleset only once**

- Thereafter, only incremental efforts are required to extend the DRR model to other jurisdictions and future changes to reporting rules
- And such updates will be delivered through centralized DRR model changes

### **Significant resource and cost savings**

• Through the mutualized effort, firms leveraging DRR using the CDM will reap significant compliance, reporting and implementation project savings

![](_page_34_Picture_0.jpeg)

![](_page_34_Figure_1.jpeg)

# Integrating CDM and Legal Agreements

![](_page_35_Picture_1.jpeg)

![](_page_35_Figure_2.jpeg)

# Further Use Cases: Ecosystem

![](_page_36_Picture_1.jpeg)

![](_page_36_Figure_2.jpeg)

# Further Use Cases: Smart contract technology support

![](_page_37_Picture_1.jpeg)

![](_page_37_Figure_3.jpeg)

- -
	-
	-
	-
- 

![](_page_38_Picture_1.jpeg)

Integration with CRIF standard for FRTB, SIMM, and SA-CVA reporting

Set a standard for the efficient digitalisation of collateral related margin process

Transcribe legally prescribed functional clauses from ISDA Def into machine readable and human readable codified functions

Assert and mutualise the standardised encoding and capacity for implementation of legal clauses supporting the life cycle events of derivative transactions.

Support more consistent implementation of market infrastructures processes such as clearing in tally with upcoming new innovative technologies (DLT, Cloud, Smart Contract, etc)

Match and store consistent trade representations that feed in "real time" FO trading systems using DLT and detect inconsistencies if any.

Facilitate more efficient re-use of data e.g. data template for large volume of increases of an Equity portfolio swap

Express the CCP clearing handbook book that regulates the registration and clearing of a transaction into a machine readable and executable code that can be automatically generated.

Aid the standardized representation of SSIs

![](_page_39_Picture_0.jpeg)

**Get Involved**

![](_page_39_Picture_2.jpeg)

# How to get involved- Community Structure

![](_page_40_Figure_1.jpeg)

### List of DWGs & their Scope as at Mar 24:

- Collateral- *Collateral schedules & processes*
- Repo & Bonds- *Repo & bond products*
- Securities Lending- *Securities Lending*
- Derivatives Products and Business Events (DBPE)- *Derivatives products of a non-structured/exotic nature*
- Structured Products- *Structured & Exotic Derivatives*
- DRR Peer Review- *Digital Regulatory Reporting (Derivatives regimes)*
- ISDA Legal Agreements- *ISDA Legal Agreement modelling*

## How to get involved

### Info hub for FINOS including user documentation downloadable distributions: Homepage | Common Domain Model (finos.org)

|                                                                     |                                                      | D C Download Source Code   Comm: X +<br>$\circledcirc$ |                                                                                                  |                                                      |                           |  |  |
|---------------------------------------------------------------------|------------------------------------------------------|--------------------------------------------------------|--------------------------------------------------------------------------------------------------|------------------------------------------------------|---------------------------|--|--|
| Ò<br>$\Box$<br>$\circledcirc$ Homepage   Common Domain M $\times$ + |                                                      | $\leftarrow$ C<br>https://cdm.finos.org/docs/download  |                                                                                                  | $A^h$ $\Omega$                                       |                           |  |  |
| C<br>https://cdm.finos.org/docs/home<br>â<br>$\leftarrow$           |                                                      | Common Domain Model                                    |                                                                                                  |                                                      |                           |  |  |
| Common Domain Model                                                 |                                                      | <b>Main Menu</b><br>$\checkmark$                       | <b>CDM as Java Examples</b>                                                                      | Versions                                             |                           |  |  |
|                                                                     |                                                      | Homepage                                               |                                                                                                  | CDM as Java                                          |                           |  |  |
| <b>Main Menu</b><br>$\checkmark$                                    |                                                      | Overview of the FINOS CDM                              | Examples of how you can use the CDM can be found in GitHub: CDM GitHub Examples.                 | Setup<br>CDM as Java                                 |                           |  |  |
|                                                                     | Main Menu > Homepage<br>$\rightarrow$<br>A           | The Common Domain Model<br>Product Model               |                                                                                                  |                                                      | CDM as Pyth               |  |  |
| Homepage                                                            |                                                      | Event Model                                            | <b>CDM</b> as Python                                                                             | CDM as DAM                                           |                           |  |  |
| Overview of the FINOS CDM                                           | <b>Homepage</b>                                      | Legal Agreements                                       |                                                                                                  | CDM as CIT 8                                         | CDM as Scala              |  |  |
| The Common Domain Model                                             |                                                      | Process Model                                          | CDM as Python is published here. Then click on the Browse button for latest version available as |                                                      |                           |  |  |
|                                                                     | Welcome to CDM documentation!                        | Reference Data Model                                   | .tar.gz file.                                                                                    | CDM as Go                                            |                           |  |  |
| Product Model                                                       | • Overview of Finos CDM<br>• The Common Domain Model | Mapping (Synonym)                                      |                                                                                                  | CDM as Type<br>CDM as Kotli                          |                           |  |  |
| Event Model                                                         |                                                      | <b>CDM as DAML</b>                                     | CDM as Exce                                                                                      |                                                      |                           |  |  |
| Legal Agreements                                                    |                                                      | Eligible Collateral Representation                     |                                                                                                  | CDM Event S                                          |                           |  |  |
|                                                                     |                                                      | Repurchase Transaction<br>Representation in the CDM    | CDM as DAML is published here. Then click on the Browse button for latest version available as   |                                                      |                           |  |  |
| Process Model                                                       | o product-model                                      | CDM Java Distribution Guidelines                       | .tar.gz file containing compiled .dar files.                                                     |                                                      |                           |  |  |
| Reference Data Model                                                | o event-model                                        | Development Guidelines                                 |                                                                                                  |                                                      |                           |  |  |
| Mapping (Synonym)                                                   |                                                      | <b>Download Source Code</b>                            | <b>CDM</b> as Scala                                                                              |                                                      |                           |  |  |
|                                                                     | · legal-agreements                                   |                                                        |                                                                                                  | $2-0$                                                |                           |  |  |
| Namespace                                                           | o process-model                                      |                                                        | CDM as Scala is published here. Then click on the Browse button for latest version available as  | $\leftarrow$ $\,$ $\,$ $\,$ $\,$ $\,$ $\,$ $\,$ $\,$ |                           |  |  |
| Eligible Collateral Representation                                  | o reference-data-model                               |                                                        | . jar file.                                                                                      |                                                      | $\bigcirc$ Come           |  |  |
| Repurchase Transaction                                              |                                                      |                                                        |                                                                                                  |                                                      | <b>Main Mer</b><br>Homepa |  |  |
| Representation in the CDM                                           | • mapping                                            |                                                        | <b>CDM</b> as C# 8.0                                                                             |                                                      | Overview                  |  |  |
| <b>CDM Java Distribution Guidelines</b>                             | o namespace                                          |                                                        |                                                                                                  |                                                      | The Com<br>Product        |  |  |
| Development Guidelines                                              | • Eligible Collateral Representation                 |                                                        |                                                                                                  |                                                      | Event Mo                  |  |  |
|                                                                     |                                                      |                                                        |                                                                                                  |                                                      | Legal Ag<br>Process       |  |  |
| Download Source Code                                                | • CDM Java Distribution Guidelines                   |                                                        |                                                                                                  |                                                      | Referenc                  |  |  |
|                                                                     | • Development Guidelines                             |                                                        |                                                                                                  |                                                      | Mapping<br>Namespa        |  |  |
|                                                                     | • Get Involved                                       |                                                        |                                                                                                  |                                                      | Eligible                  |  |  |
|                                                                     |                                                      |                                                        |                                                                                                  |                                                      | Repurch<br>Represe        |  |  |
|                                                                     | • Download                                           |                                                        | σ                                                                                                |                                                      | CDM Jav                   |  |  |
|                                                                     |                                                      |                                                        | Ø                                                                                                |                                                      | Develops<br>Downloa       |  |  |
|                                                                     |                                                      |                                                        |                                                                                                  |                                                      |                           |  |  |
|                                                                     | $\angle$ Edit this page                              |                                                        | සි<br>$\overline{\mathbf{v}}$                                                                    |                                                      |                           |  |  |

![](_page_42_Picture_0.jpeg)

The Common Domain Model is brought to you by:

![](_page_42_Picture_2.jpeg)