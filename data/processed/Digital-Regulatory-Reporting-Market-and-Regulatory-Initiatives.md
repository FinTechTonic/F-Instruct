# Digital Regulatory Reporting: Market and Regulatory Initiatives

The complexity and cost of global efforts to improve the data quality of derivatives regulatory reporting create a variety of challenges for market participants and policy-makers.

These concerns – and the opportunity to mitigate them as major changes to reporting rules are implemented over the next two years – have become a powerful impetus behind digital regulatory reporting (DRR) initiatives. Broadly defined, DRR refers to the publication of reporting rules by regulators and/or the implementation of derivatives reporting requirements by market participants via human-readable, machine-executable code.

DRR will allow regulators to publish reporting rules as executable code that can be automatically read and interpreted by the IT systems of reporting entities, improving the reporting process across asset classes. Regulated entities will be able to automatically execute new and amended regulatory reporting rules using an industry-led standardized interpretation of the requirements as free open-source code.

This paper explains what DRR is, reviews its potential benefits and describes the various initiatives that are under way. In particular, the paper highlights the work of ISDA and market participants to enable all firms to implement regulatory reporting rules consistently using the open-source Common Domain Model (CDM). The paper also describes several DRR initiatives launched by policy-makers that involved collaboration with market participants.

Digital Regulatory Reporting: Market and Regulatory Initiatives

## **CONTENTS**

| Executive Summary<br>                                     | 03 |
|-----------------------------------------------------------|----|
| Challenges in the Current Regulatory<br>Reporting Process | 04 |
| Derivatives Reporting Rules Changes<br>on the Horizon     | 05 |
| DRR and CDM                                               | 06 |
| Regulatory DRR-related Initiatives<br>                    | 08 |
| The FCA and BoE Pilots and Initiatives  08                |    |
| G-20 TechSprint 2020 09                                   |    |
| EC MRER Tender 09                                         |    |
| Project Ellipse 09                                        |    |
| Conclusion                                                | 11 |

**EXECUTIVE SUMMARY**

As the Financial Stability Board (FSB) has noted, "[r]egulatory reporting has become increasingly complex and expensive for regulated institutions. In addition, poor quality reporting and/or delayed data can create challenges for authorities…"1 .

These concerns, combined with the challenges of complying with new and amended reporting regulations in different jurisdictions over the next few years, have become powerful drivers of DRR initiatives by both the official and private sectors.

Market participants are working together on a mutualized, industry-wide DRR initiative for the amended US Commodity Futures Trading Commission (CFTC) and European Market Infrastructure Regulation (EMIR) trade reporting requirements, which will enable firms to interpret and implement regulatory reporting rules consistently via human-readable, machine-executable code using the CDM.

This DRR initiative will create a standardized data model with unambiguous data-driven logic. Instead of each firm individually creating a regulatory reporting solution based on its own interpretation of the rules, market participants are developing an industry-led standardized interpretation of the regulation as free open-source code.

With the CFTC's revised swap data reporting rules coming into force in December 2022, ISDA and over 30 contributing firms are currently prioritizing digitization of the CFTC requirements to help market participants comply with these amendments. ISDA is also working with market participants on digitization of the revised EU trade reporting requirements under EMIR, currently anticipated to go live in the fourth quarter of 2023.

Policy-makers have launched several DRR-related initiatives in recent years that have involved collaboration with market participants. These include pilots and projects by the Financial Conduct Authority (FCA) and Bank of England (BoE)2 , the Group-of-20 (G-20) TechSprint initiative3 , a European Commission (EC) tender to develop machine-readable and executable reporting (MRER) as a proof of concept4 , and the Bank for International Settlements (BIS) Innovation Hub's Project Ellipse5 .

<sup>4</sup>Digital regulatory reporting is also sometimes referred to as machine-readable and executable reporting (MRER). For consistency, DRR is used in this report 5 Ellipse Overview <https://ellipse.bisih.org/>

<sup>1</sup>Financial Stability Board (FSB), The Use of Supervisory and Regulatory Technology by Authorities and Regulated Institutions [www.fsb.org/wp-content/](http://www.fsb.org/wp-content/uploads/P091020.pdf) [uploads/P091020.pdf](http://www.fsb.org/wp-content/uploads/P091020.pdf)

<sup>2</sup>Digital Regulatory Reporting [www.fca.org.uk/innovation/regtech/digital-regulatory-reporting](http://www.fca.org.uk/innovation/regtech/digital-regulatory-reporting)

<sup>3</sup>The Group-of-20 (G-20) TechSprint 2020 was driven by the Saudi G-20 presidency and the Bank for International Settlements (BIS) Innovation Hub [www.bis.org/hub/g20\\_techsprint.htm](http://www.bis.org/hub/g20_techsprint.htm)

# **CHALLENGES IN THE CURRENT REGULATORY REPORTING PROCESS**

Derivatives regulatory reporting requirements have been implemented in all major jurisdictions around the world. The explosion of data has brought more transparency to derivatives markets and enabled regulators to better monitor market activity and identify risks. However, it has also resulted in numerous challenges.

For market participants, these challenges include the operational costs and burdens of building and maintaining reporting regimes in different countries. Regulators have recognized the need for greater consistency and accuracy in reporting and are reviewing their rules with the aim of adopting globally harmonized data elements. But many have also imposed jurisdiction-specific reporting specifications and rules that require distinctive infrastructure builds.

Even within an individual jurisdiction, firms face operational challenges in regulatory reporting, such as interpreting reporting requirements and determining what data inputs to use for specific data fields.

To comply, each individual institution needs to allocate significant resources and budget. Interpreting the regulations incorrectly could have serious legal, operational and financial consequences. To reduce this risk, many smaller firms use external professional services to help interpret the requirements correctly. Larger firms have in-house compliance teams dedicated to these tasks.

In previous years, significant industry effort has been spent on working with policy-makers to understand the precise expectations for specific regulatory requirements. Where regulators have inconsistent jurisdictional trade reporting mandates for the same requirement, trade associations like ISDA have worked with market participants to develop and agree a standard approach.

Industry best practice documents would typically be drafted and published on the web. Institutions would then use their own resources to translate each industry best practice into clearly defined and operationally feasible instructions that could be built into their reporting logic and systems. This approach leaves the door open for interpretation differences between industry participants for an identical requirement.

When regulatory reporting changes are implemented, the cycle begins again. Infrastructure builds, validation and testing of data outputs to ensure compliance and related work is required by market participants.

Rule amendments result in industry best practice being reassessed, revised and republished. This leads to delays between data reporting amendments being published by policy-makers and implemented by firms.

Inefficient regulatory reporting processes also create challenges for regulators, as poor data quality and delayed data can affect their ability to effectively supervise and monitor financial markets.

# **DERIVATIVES REPORTING RULES CHANGES ON THE HORIZON**

Regulatory reporting requirements in major jurisdictions are set for significant changes in 2022 and 2023. This is prompting interest in DRR, as these initiatives will result in greater consistency across firms and systems, creating efficiency and further improving the quality of data for regulators.

Much of the forthcoming changes stem from the G-20 objective for authorities to effectively aggregate trade data for analysis. Subsequent work by the Committee on Payments and Market Infrastructures (CPMI), the International Organization of Securities Commissions (IOSCO) and the FSB has resulted in global recommendations for the standardization of over-the-counter (OTC) derivatives data reported to trade repositories (TRs)6 .

For example, CPMI-IOSCO technical guidance on the harmonization of critical OTC derivatives data elements (CDEs) other than unique transaction identifiers (UTIs) and unique product identifiers (UPIs)7 is intended to enhance consistency, which will help authorities better assess risks to financial stability8 .

The guidance does not prescribe which CDEs should be reported in each jurisdiction. Instead, it provides harmonized definitions, formats and allowable values for more than 100 recommended data elements related to dates, counterparties, clearing, trading, confirmation, settlement, valuation, collateral and margin, prices, notional amounts, payments, packages, links and custom baskets.

Multiple regulators have committed to rewrite their reporting rules to adopt the CDEs. However, they have varied the number and type of CDE elements they will introduce in their reporting regimes.

In the US, the CFTC's amendments overhaul parts 45, 46 and 49 of the swap data record-keeping and reporting requirements and part 43 of the real-time public reporting requirements by incorporating CDEs agreed by CPMI-IOSCO, as well as changing certain other CFTC-specific data fields9 .

In the EU, the European Securities and Markets Authority (ESMA) published a final report on technical standards under the EMIR REFIT regulation in December 2020 that covers data reporting to TRs, procedures to reconcile and validate data, access by the relevant authorities to data and registration of TRs10. ESMA also included CDEs in this report. The rules are expected to come into force in the second half of 2023.

Both the CFTC and ESMA will also require the inclusion of UPIs, as well as a new ISO 20022 messaging standard. Unless the compliance dates for these new standards are aligned with amendments to incorporate CDEs, both changes will likely result in further overhauls to reporting systems, with some of the logic developed to implement the current requirements having to be discarded and replaced.

<sup>9</sup>Commodity Futures Trading Commission Finalizes Rules to Improve Swap Data Reporting, Approves Other Measures at September 17 open meeting [www.cftc.gov/PressRoom/PressReleases/8247-20](http://www.cftc.gov/PressRoom/PressReleases/8247-20)

<sup>6</sup>The Committee on Payments and Market Infrastructures (CPMI) and International Organization of Securities Commissions (IOSCO), Technical Guidance: Harmonisation of the Unique Product Identifier [www.bis.org/cpmi/publ/d169.pdf](http://www.bis.org/cpmi/publ/d169.pdf); CPMI-IOSCO, Technical Guidance: Harmonisation of the Unique Transaction Identifier [www.bis.org/cpmi/publ/d158.pdf;](http://www.bis.org/cpmi/publ/d158.pdf) CPMI-IOSCO, Harmonisation of Critical OTC Derivatives Data Elements (Other Than UTI and UPI): Revised CDE Technical Guidance – Version 2 [www.leiroc.org/publications/gls/roc\\_20210922.pdf](http://www.leiroc.org/publications/gls/roc_20210922.pdf); FSB, Governance Arrangements for the Unique Transaction Identifier (UTI): Conclusions and Implementation Plan [www.fsb.org/2018/01/fsb-publishes-governance-arrangements-and](http://www.fsb.org/2018/01/fsb-publishes-governance-arrangements-and-implementation-plan-for-the-unique-transaction-identifier-uti/)[implementation-plan-for-the-unique-transaction-identifier-uti/;](http://www.fsb.org/2018/01/fsb-publishes-governance-arrangements-and-implementation-plan-for-the-unique-transaction-identifier-uti/) FSB, Governance Arrangements for the UPI: Conclusions, Implementation Plan and Next Steps to Establish the International Governance Body [www.fsb.org/wp-content/uploads/P091019.pdf](http://www.fsb.org/wp-content/uploads/P091019.pdf); CPMI-IOSCO, Governance Arrangements for Critical OTC Derivatives Data Elements (Other Than UTI and UPI) [www.fsb.org/wp-content/uploads/P091019.pdf](http://www.fsb.org/wp-content/uploads/P091019.pdf)

<sup>7</sup>A UPI is a 12-character reference code that would be assigned to each distinct over-the-counter derivatives product and be mapped to reference data elements with specific values that describe the product

<sup>8</sup>Harmonisation of Critical OTC Derivatives Data Elements (Other Than UTI and UPI) [www.iosco.org/library/pubdocs/pdf/IOSCOPD598.pdf](http://www.iosco.org/library/pubdocs/pdf/IOSCOPD598.pdf)

<sup>10</sup>European Securities and Markets Authority Final Report: Technical Standards on Reporting, Data Quality, Data Access and Registration of Trade Repositories under EMIR REFIT [www.esma.europa.eu/sites/default/files/library/esma74-362-824\\_fr\\_on\\_the\\_ts\\_on\\_reporting\\_data\\_quality\\_data\\_](http://www.esma.europa.eu/sites/default/files/library/esma74-362-824_fr_on_the_ts_on_reporting_data_quality_data_access_and_registration_of_trs_under_emir_refit_0.pdf) [access\\_and\\_registration\\_of\\_trs\\_under\\_emir\\_refit\\_0.pdf](http://www.esma.europa.eu/sites/default/files/library/esma74-362-824_fr_on_the_ts_on_reporting_data_quality_data_access_and_registration_of_trs_under_emir_refit_0.pdf)

**DRR AND CDM**

DRR is a mutualized industry-wide initiative championed by ISDA that will enable firms to interpret and implement regulatory reporting rules consistently via a common machine-readable code based on the CDM.

The initiative will create a free open-source standardized model with data-driven logic. Instead of each firm individually interpreting the rules and creating its own regulatory reporting solution, market participants will work together to develop a standardized interpretation of the regulation published as code that each firm can implement in its systems.

More than 40 institutions, including buy- and sell-side market participants, have contributed to ISDA's CDM DRR working groups. Members participate in weekly industry meetings and bilateral calls to model the regulatory reporting rules based on peer-reviewed interpretations of the requirements.

The current focus of the DRR working groups is the CFTC's revised swap data reporting rules, which come into force at the end of 2022.

Each of the 128 data elements from the CFTC regulation is being defined, digitized and modelled into the CDM, as appropriate, and then discussed and validated by the working group. Different members of the working group take the lead on different data elements, sharing the resulting work with the others, which makes the process significantly more efficient.

As of March 2022, more than 70% of the modelling for the CFTC's rules across five asset classes had been completed. In the first and second quarters of 2022, members reviewed, tested and validated digitization outputs with project tools. To validate the DRR output, ISDA will use portfolios of anonymized trade data from participants and will produce trade reports against which firms can validate their internal builds.

Phase two of the DRR program will focus on digitization of the reporting rules under EMIR, as well as requirements set by the Australian Securities and Investments Commission and the Monetary Authority of Singapore (MAS). While the rules will not be entirely identical, ISDA expects a large proportion of the globally harmonized data elements (such as CDEs) that are being coded for the CFTC requirements will be fully or partially reused elsewhere.

Adoption of DRR using the CDM by reporting parties and repositories, and acceptance by regulators, will promote alignment across the market. If changes are necessary due to regulatory clarifications or modifications, the reporting code can be altered centrally and then deployed consistently by all firms using DRR. This includes fundamental changes like ISO 20022, which many regulators, including the CFTC, are expected to adopt. Regulators will have access to this code, giving them full transparency into how the rules are being applied.

This has the potential to change the nature of regulatory engagement, with the prospect of an iterative approach to technical regulations. Regulators will be able to consult at a high level on changes and ask the industry to propose solutions in the CDM that show what the impact might be in a test environment with real test data.

ISDA will work with firms to develop a mutualized solution for implementing the global requirements for UPIs and the ISO 20022 messaging schema through DRR.

The CDM can integrate and work in a complementary way with any relevant data standards, including ISO 20022. Likewise, the CDM can be integrated into the internal systems of market participants, allowing multiple data and messaging formats to be consistently converted to the CDM format. Regulatory rules and industry best practice logic can be applied, and the necessary output format (ISO 20022) can be created consistently and seamlessly.

#### What is the CDM?

The Common Domain Model (CDM) is an open-source, human-readable and machine-executable data model for derivatives products, processes and calculations. It introduces a digital representation of trade agreements, regulatory reporting and trade events across the lifecycle of all financial products and includes synonyms (translators) that convert messages from other standards into the CDM format.

By creating a common digital representation of events and processes that occur throughout a trade lifecycle, the CDM allows market participants to achieve consistency in the processing of trades and the interpretation and implementation of complex requirements.

Examples include the affirmation/confirmation of trades, calculation and exchange of margin requirements and generation and categorization of risk and capital data – for instance, data represented in ISDA's Common Risk Interchange Format11.

The CDM can also be used to apply clearing rule books to portfolios to determine which trades need to be cleared, and analyze legal agreement data across portfolios to determine specific risks or features.

# **REGULATORY DRR-RELATED INITIATIVES**

Several DRR-related initiatives have been launched in recent years, including the FCA and BoE TechSprints, the FCA and BoE DRR initiative, the G-20 TechSprint 2020, the EC's tender to develop the MRER as a proof of concept, and the BIS Innovation Hub's Project Ellipse.

#### The FCA and BoE Pilots and Initiatives

The FCA has held several TechSprints, resulting in a proof of concept demonstrating that a small set of reporting instructions can be converted into machine-executable code. Machines then used the code to automatically carry out/execute the instructions, pulling the required information directly from a firm's systems12,13.

Following the TechSprints, the FCA and BoE launched their DRR initiative in 2018 to explore the use of technology to help firms meet their regulatory reporting requirements in a more effective and efficient way. The initiative aims to examine the feasibility of a model-driven and machine-readable regulatory environment that could transform how the financial services industry understands, interprets and reports regulatory information14.

The initiative will enable firms to digitally tag data, allowing it to be identified according to agreed standards. As a result, it will be possible to easily identify the same data across various institutions. Regulators could publish a digital version of reporting instructions that reference the common data standards agreed with firms, which could be distributed to those institutions15.

Two pilot phases have been completed so far, and phase three is set to follow. The pilot phases represent a collaboration between the BoE, FCA, Barclays, Credit Suisse, HSBC, Lloyds, Nationwide, Natwest and Santander.

Phase one of the DRR pilot took place in the second half of 2018. The focus was to improve the quality of regulatory data by exploring how to make reporting rules and instructions less reliant on human interpretation and implementation.

The pilot participants created a regulatory reporting prototype that involved converting regulation into code using the CDM, defining standardized firm data and implementing machine-executable regulation against that standardized data.

In the prototype, machine-executable versions of the regulation were created as smart contracts. These smart contracts were loaded into the virtual nodes that represented regulators and then distributed to the virtual nodes that represented regulated firms. The codified regulation was then executed against synthetic data supplied by the firms in a standardized format. However, technology and processes used to build the prototype needed further development to be deployed as a live system.

The focus of phase two was to assess the economic viability of DRR, explore technical solutions for the creation of machine-executable regulation and definition of data, and further develop and test processes for the definition of data and creation of machine-executable regulation explored in phase one16.

<sup>12</sup>Unlocking Regulatory Reporting TechSprint [www.fca.org.uk/events/techsprints/unlocking-regulatory-reporting-techsprint](http://www.fca.org.uk/events/techsprints/unlocking-regulatory-reporting-techsprint)

<sup>13</sup> Model Driven Machine Executable Regulatory Reporting TechSprint [www.fca.org.uk/events/techsprints/model-driven-machine-executable-regulatory](http://www.fca.org.uk/events/techsprints/model-driven-machine-executable-regulatory-reporting-techsprint)[reporting-techsprint](http://www.fca.org.uk/events/techsprints/model-driven-machine-executable-regulatory-reporting-techsprint)

<sup>14</sup>Digital Regulatory Reporting [www.fca.org.uk/innovation/regtech/digital-regulatory-reporting](http://www.fca.org.uk/innovation/regtech/digital-regulatory-reporting)

<sup>15</sup> Digital Regulatory Reporting Phase 2 Viability Assessment [www.fca.org.uk/publication/discussion/digital-regulatory-reporting-pilot-phase-2-viability](http://www.fca.org.uk/publication/discussion/digital-regulatory-reporting-pilot-phase-2-viability-assessment.pdf)[assessment.pdf](http://www.fca.org.uk/publication/discussion/digital-regulatory-reporting-pilot-phase-2-viability-assessment.pdf)

<sup>16</sup> Digital Regulatory Reporting Phase 2 Viability Assessment [www.fca.org.uk/publication/discussion/digital-regulatory-reporting-pilot-phase-2-viability](http://www.fca.org.uk/publication/discussion/digital-regulatory-reporting-pilot-phase-2-viability-assessment.pdf)[assessment.pdf](http://www.fca.org.uk/publication/discussion/digital-regulatory-reporting-pilot-phase-2-viability-assessment.pdf)

Phase three of the DRR pilot focuses on improving regulatory interpretation, implementation and execution, as well as delivering data. The FCA is in the process of establishing the requirements necessary to create machine-readable and machine-executable regulation. This involves alignment on definitions, interpretation and implementation of rules and data definitions and scaling technology-driven reporting solutions across the financial industry17.

#### G-20 TechSprint 2020

The G-20 TechSprint 2020 was a virtual hackathon organized by the BIS Innovation Hub and the Saudi G-20 presidency. The event was designed to highlight the potential for new technologies to resolve operational problems in regulatory compliance and supervision across three categories: regulatory reporting; monitoring and surveillance; and dynamic information sharing18.

In the regulatory reporting category, entrants were asked to develop tools to enable regulators to easily prepare and transmit machine-readable and machine-executable regulations to their regulated entities, allowing regulatory data to be easily mined and reported.

As part of the competition, ISDA and technology firm REGnosys developed a DRR pilot for derivatives reporting regulations using the CDM. The solution enabled reporting firms to access an executable code version of regulatory requirements set by the MAS and then run trades through a reporting engine to validate the business logic, which could be used consistently across the market19.

#### EC MRER Tender

The EC launched a tender in February 2020 to develop a proof-of-concept MRER for requirements specified in the revised EMIR legislation. The objective is to assess whether MRER can lead to a more effective and efficient development of reporting requirements and reduce the reporting burden for institutions. The project also aims to identify regulatory obstacles to MRER technology-enabled innovation in the financial sector20.

The proof of concept is intended to demonstrate the benefit of MRER during the design of new reporting requirements and their integration into the information systems of reporting entities and supervisors. Specifically, it will focus on transaction-reporting requirements of the EMIR REFIT regulation.

As part of the initiative, the reporting requirements currently written in natural language will be converted into executable code. This code could be automatically read and interpreted by the IT systems of reporting entities, allowing for the creation of reports in the format required by supervisors.

### Project Ellipse

Project Ellipse is a proof of concept launched by the BIS Innovation Hub to explore how regulatory authorities could better use data and technology to help them identify and assess emerging risks in real time. The goal is to enable regulators to digitally extract and analyze vast amounts of data from diverse resources21.

During the first phase of the project between January and September 2021, the BIS Innovation Hub, the MAS, the BoE and ISDA collaborated to explore the concept of cross-border digital regulatory reporting using a machine-executable data model.

<sup>20</sup>Call for tenders FISMA/2020/OP/0001- Machine Readable and Executable Reporting Requirements - Proof of Concept [https://etendering.ted.europa.](https://etendering.ted.europa.eu/cft/cft-display.html?cftId=6051) [eu/cft/cft-display.html?cftId=6051](https://etendering.ted.europa.eu/cft/cft-display.html?cftId=6051)

<sup>17</sup>FCA Digital Regulatory Reporting [www.fca.org.uk/innovation/regtech/digital-regulatory-reporting](http://www.fca.org.uk/innovation/regtech/digital-regulatory-reporting)

<sup>18</sup>G-20 TechSprint 2020 [www.bis.org/hub/g20\\_techsprint.htm](http://www.bis.org/hub/g20_techsprint.htm)

<sup>19</sup>ISDA and REGnosys Win G-20 TechSprint For Regulatory Reporting [www.isda.org/a/z3rTE/ISDA-Regnosys-Win-G20-TechSprint-Regulatory-Reporting.pdf](http://www.isda.org/a/z3rTE/ISDA-Regnosys-Win-G20-TechSprint-Regulatory-Reporting.pdf)

<sup>21</sup> Ellipse Overview <https://ellipse.bisih.org/>

Using the reporting requirements for retail mortgages in the UK and Singapore, the project explored whether authorities in different jurisdictions could establish common baseline reporting requirements.

A set of common requirements was developed for the collection of retail mortgage data, which was based on the main components of mortgages and represented the type of data required by both authorities for supervisory purposes. Key data attributes of retail mortgages were identified, and data definitions were sourced.

The project modelled the identified mortgage attributes using the CDM. Executable code automatically generated from the model definitions enabled retail mortgage reports to be created for Singapore and the UK referencing the same model22.

Phase two explores the use of advanced analytics (such as artificial intelligence) and machine learning to mine unstructured sources of data and highlight correlations between current events and supervisory metrics23.

These DRR-related initiatives have collectively demonstrated the potential to improve the regulatory reporting process, enabling it to be more efficient, accurate and timely, and less costly.

| DRR Process24                          |                                                                                                                                                                                                                                                            |                                                                                                                                        |
|----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
|                                        | Current Reporting Process                                                                                                                                                                                                                                  | Potential DRR Process                                                                                                                  |
| Description and Identification of Data | Firms use multiple terms or identifiers to<br>describe the same data. Firms often rely on<br>subject matter experts or key individuals to<br>understand the data they have                                                                                 | Data is digitally tagged and identified according<br>to agreed data standards                                                          |
| Reporting Instructions                 | Reporting instructions are primarily published in<br>natural language. Firms and reporting software<br>vendors convert those instructions where possible<br>into code. Many firms rely on third-party vendors<br>to conduct parts of the reporting process | Coded reporting instructions are published by<br>the regulator. The instructions reference the data<br>standards agreed with the firms |
| Report Generation                      | Reports are compiled by firms using their own<br>systems or systems purchased from software                                                                                                                                                                | Reporting systems can automatically execute<br>the digital regulation published by the regulator,                                      |

vendors and submitted to regulators

Table A: The Differences Between the Current Derivatives Reporting Process and the Potential

<sup>22</sup>Two Jurisdictions, One Common Data Model <https://ellipse.bisih.org/proof-of-concept/two-jurisdictions-one-common-data-model>

<sup>23</sup>Phases of Ellipse<https://ellipse.bisih.org/phases-of-project-ellipse>

<sup>24</sup>Digital Regulatory Reporting Pilot Phase 1 Report [www.fca.org.uk/publication/discussion/digital-regulatory-reporting-pilot-phase-1-report.pdf](http://www.fca.org.uk/publication/discussion/digital-regulatory-reporting-pilot-phase-1-report.pdf)

identify and collate the data required, and

provide reports to regulators

**CONCLUSION**

The current regulatory reporting process is complex, time consuming and expensive for regulated institutions. Inefficient regulatory reporting processes also create a challenge for regulators as data quality can affect their ability to effectively supervise and monitor financial markets.

Major changes in regulatory reporting are slated for 2022 and 2023. These changes present an opportunity for firms and regulators to adopt DRR to standardize, automate and improve derivatives data reporting.

The adoption of DRR can bring multiple benefits for both industry participants and regulators:

- DRR will enable regulated entities to automatically execute new and amended regulatory reporting rules using an industry-led standardized interpretation of the regulation as free open-source code.
- DRR will allow regulators to publish reporting rules as executable code that can be automatically read and interpreted by reporting entities' IT systems, improving the reporting process across asset classes.
- As a mutualized initiative, DRR creates industry-wide engagement and cooperation.
- DRR will create an industry-led standardized interpretation of the regulation published as code that each firm can implement in its systems.
- Firms will be able to save time, resources and budget as they will not need to individually interpret the reporting rules.
- DRR will create a greater consistency of how rules are interpreted and implemented across market participants.
- A standardized interpretation of regulations will promote greater alignment between regulators and market participants.
- Improved data quality will enhance regulatory supervision of the markets.
- DRR will help incorporate and enhance the effectiveness of global standards, including CDE fields, the ISO 20022 message format, UPIs, UTIs and LEIs.
- DRR can be consistently applied across jurisdictions and used for reporting of different asset classes.
- DRR could enable regulators to automatically query the underlying transaction data and generate regulatory metrics referencing the standardized data.
- DRR could revolutionize the nature of regulation.

ISDA has published other recent research papers:

- •*[ISDA-Clarus RFR Adoption Indicator: February 2022](http://www.isda.org/a/AY0gE/ISDA-Clarus-RFR-Adoption-Indicator-February-2022.pdf)* [www.isda.org/a/AY0gE/ISDA-Clarus-RFR-Adoption-Indicator-February-2022.pdf](http://www.isda.org/a/AY0gE/ISDA-Clarus-RFR-Adoption-Indicator-February-2022.pdf)
- •*[Interest Rate Derivatives Trading Activity Reported in EU, UK and US Markets:](http://www.isda.org/a/XQ0gE/Interest-Rate-Derivatives-Trading-Activity-Reported-in-EU-UK-and-US-Markets-December-2021.pdf)  [December 2021](http://www.isda.org/a/XQ0gE/Interest-Rate-Derivatives-Trading-Activity-Reported-in-EU-UK-and-US-Markets-December-2021.pdf)*

[www.isda.org/a/XQ0gE/Interest-Rate-Derivatives-Trading-Activity-Reported-in-EU-UK-and-US-](http://www.isda.org/a/XQ0gE/Interest-Rate-Derivatives-Trading-Activity-Reported-in-EU-UK-and-US-Markets-December-2021.pdf)[Markets-December-2021.pdf](http://www.isda.org/a/XQ0gE/Interest-Rate-Derivatives-Trading-Activity-Reported-in-EU-UK-and-US-Markets-December-2021.pdf)

•*[SwapsInfo Full Year 2021 and the Fourth Quarter of 2021 Review: Full Report](http://www.isda.org/a/tkkgE/SwapsInfo-Full-Year-2021-and-the-Fourth-Quarter-of-2021-Review-Full-Report.pdf)* [www.isda.org/a/tkkgE/SwapsInfo-Full-Year-2021-and-the-Fourth-Quarter-of-2021-Review-Full-](http://www.isda.org/a/tkkgE/SwapsInfo-Full-Year-2021-and-the-Fourth-Quarter-of-2021-Review-Full-Report.pdf)[Report.pdf](http://www.isda.org/a/tkkgE/SwapsInfo-Full-Year-2021-and-the-Fourth-Quarter-of-2021-Review-Full-Report.pdf)

## For questions on ISDA Research, please contact:

Olga Roman Head of Research International Swaps and Derivatives Association, Inc. (ISDA) Office: 212-901-6017 oroman@isda.org

# **ABOUT ISDA**

Since 1985, ISDA has worked to make the global derivatives markets safer and more efficient. Today, ISDA has over 980 member institutions from 78 countries. These members comprise a broad range of derivatives market participants, including corporations, investment managers, government and supranational entities, insurance companies, energy and commodities firms, and international and regional banks. In addition

to market participants, members also include key components of the derivatives market infrastructure, such as exchanges, intermediaries, clearing houses and repositories, as well as law firms, accounting firms and other service providers. Information about ISDA and its activities is available on the Association's website: [www.isda.org.](http://www.isda.org) Follow us on [Twitter,](https://twitter.com/isda) [LinkedIn,](https://www.linkedin.com/company/isda) [Facebook](https://www.facebook.com/ISDA.org) and [YouTube](https://www.youtube.com/channel/UCg5freZEYaKSWfdtH-0gsxg).