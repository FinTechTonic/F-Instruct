![](_page_0_Picture_1.jpeg)

#### **Important information about this document**

The information in this document is for information purposes only. No representation or warranty is made as to its accuracy, completeness or suitability for any purpose. None of ISDA, its directors, employees, agents or advisors accept any responsibility for any loss or damage arising directly or indirectly from reliance on such information. Readers are strongly recommended to refer to the 2021 ISDA Interest Rate Derivatives Definitions (including its ancillary documentation) or other relevant documents, in conjunction with such professional advisors as may be appropriate in the circumstances, for a definitive understanding of any of the topics that are discussed in this publication. This paper may be updated periodically at ISDA's discretion but at any point may not reflect changes that have been made to the definitional documents discussed. Capitalized terms used in this document and not otherwise defined will have the meanings given to them in the 2021 ISDA Interest Rate Derivatives Definitions as at the time of publication of this document. Section references are to sections of the 2021 ISDA Interest Rate Derivatives Definitions unless stated or otherwise required by the context.

| INTRODUCTION                                      | 03 |
|---------------------------------------------------|----|
| CONSOLIDATED STRUCTURE<br>                        | 04 |
| CHANGES TO KEY TERMS AND PROVISIONS               | 06 |
| CALCULATION AGENT PROVISIONS                      | 07 |
| DAYS, DATES AND PERIODS                           | 09 |
| CALCULATION OF FIXED AND FLOATING AMOUNTS         | 13 |
| FLOATING RATE OPTIONS                             | 15 |
| RFR COVENTIONS                                    | 19 |
| FALLBACKS<br>                                     | 20 |
| EXERCISE OF SWAPTIONS/OPTIONAL EARLY TERMINATIONS | 27 |
| CASH SETTLEMENT PROVISIONS<br>                    | 28 |
| CURRENCY PROVISIONS<br>                           | 37 |
| DIGITAL PUBLICATION<br>                           | 38 |
| PUBLICATION AND IMPLEMENTATION                    | 39 |

## INTRODUCTION

On June 11, 2021, ISDA published the 2021 ISDA Interest Rate Derivatives Definitions, the latest in a series of definitional booklets that have provided the framework for documenting over-the-counter interest rate derivatives transactions since 1985.

![](_page_2_Figure_3.jpeg)

Historically, the definitions have been periodically updated via the publication of supplements to keep pace with market developments – for example, when new interest rate benchmarks (or floating rates) used to determine floating amounts have been required. When more wide-ranging changes are needed, the definitions booklet itself has been republished in its entirety, consolidating the amendments made to the previous booklet and incorporating changes to reflect market conventions. The previous time the interest rate definitions were republished was in January 2007, when the 2006 ISDA Definitions were released.

Significant changes have occurred in derivatives markets since then, including the transition from paper to electronic confirmations, the rollout of regulatory reforms in the wake of the global financial crisis and the increased use of collateralization and central clearing.

As a result, over 75 supplements have been added to the 2006 Definitions, which has led to them becoming unwieldly and difficult to read. To fully understand all the terms (and therefore the risks) that apply to an interest rate swap traded under the 2006 Definitions, it is necessary to read though the original 2006 booklet together with all the supplements published since 2006 in case any terms or provisions of the original 2006 booklet relevant to that trade have been changed by one or more of the subsequent supplements.

Almost 15 years on from publication of the 2006 Definitions, a major update and consolidation of the ISDA Definitions was long overdue.

## CONSOLIDATED STRUCTURE

The 2006 Definitions (and previous generations of the ISDA Definitions) were published as a paper booklet. Subsequent amendments and additions were published as supplements in PDF form. As the number of supplements increased, it became increasingly difficult for users to determine which terms applied to their trades. To improve ease of use, the 2021 Definitions bring the prevailing provisions (including those being retained from the supplements) into a single consolidated structure.

The operative provisions and bulk of the definitions are set out in the main book. However, to improve visibility and standardization, certain default elections and definitions have been set out in separate matrices (as was the case under the 2006 Definitions).

![](_page_3_Figure_4.jpeg)

Although some of the matrices in the 2021 Definitions (such as the Mark-to-Market Matrix and the Compounding/Averaging Matrix) are substantively the same as those under the 2006 Definitions, others are new or have been expanded. In particular, Floating Rate Options (FROs) that were set out in the main book under the 2006 Definitions are included in a new Floating Rate Matrix under the 2021 Definitions. In addition, the definitions of currencies and their principal financial centers are now set out in the Currency/Business Day Matrix. As discussed in subsequent sections, the Settlement Matrix has been expanded and consolidates provisions that were contained in several matrices under the 2006 Definitions.

To avoid the need to continue making amendments via supplements, the 2021 Definitions have been published on a specially designed user interface called MyLibrary. This will enable the main book and any of its accompanying matrices to be restated in their entirety (so called 're-versioning') whenever an update or amendment is needed. That removes the need to publish supplements going forward.

Users will be able to navigate directly to the consolidated version of the 2021 Definitions that applied as of the date of their trade, making them much easier to use and reducing the scope for operational mistakes. This new interactive format also includes comparison tools that enable users to view different versions of the 2021 Definitions in blackline, hyperlinks between defined terms, links to ancillary documents and multimedia content, bookmarking and advanced search facilities. The 2021 Definitions are accessible on mobile devices as well as desktop computers and can be downloaded and printed from the platform. Access to MyLibrary is included in the subscription for ISDA Online Library and Online Library Plus users, but single-user annual licenses are also available. For further information on how to access MyLibrary, please contact [onlinelibrary@isda.org.](mailto:onlinelibrary@isda.org)

Re-versioning will occur separately for the main book and any of the matrices – in other words, if only the Floating Rate Matrix is amended, a new version of the Floating Rate Matrix will be published but the main book and other matrices will remain on their then-current version. Versions will be differentiated by version numbers and dates of publication.

ISDA expects new versions of the 2021 Definitions to be published prior to the October 4, 2021 implementation date (discussed further below) in order to maintain alignment as new FROs are added and other changes are made to the 2006 Definitions as part of the global benchmark reform process.

# CHANGES TO KEY TERMS AND PROVISIONS FROM THE 2006 DEFINITIONS

ISDA surveyed its members on potential improvements and changes to the 2006 Definitions and set up a number of working groups to develop solutions for the areas identified. Suggestions from members were received during the course of those working group discussions.

Importantly, much of what worked well under the 2006 Definitions has been retained in the 2021 Definitions. While there may be drafting updates, those provisions remain substantively unchanged.

Some provisions have been substantively updated, but only in order to align with or facilitate current market practices. Although there will be differences when compared with the equivalent provisions under the 2006 Definitions, there is expected to be little or no economic difference in practice. Notwithstanding this paper's focus on key changes, a number of these provisions are flagged in this document.

There are other areas where the 2021 Definitions have substantively changed and may result in different economic outcomes when compared to the 2006 Definitions. However, some updates to the 2006 Definitions made via supplements may have had the same effect. For example, a trade incorporating the 2006 Definitions today may have a substantively different economic outcome compared to a historic trade that only incorporated the first 45 supplements.

It is not possible to provide definitive guidance on whether certain differences between the 2006 and 2021 Definitions may result in different economic outcomes. This must be determined by each market participant given their own circumstances, risk tolerances, sensitivities, and the purposes for which the derivatives are being used.

## CALCULATION AGENT PROVISIONS

#### Calculation Agent (Section 1.2.1)

The Calculation Agent is the party specified as such in the relevant ISDA Master Agreement or the Confirmation for a transaction. The 2006 Definitions set out a long list of tasks for which the Calculation Agent is responsible, but this has been replaced in the 2021 Definitions by a generic statement that the Calculation Agent is responsible for all determinations that are not otherwise required to be made by a party to the relevant transaction or other specified person.

#### Calculation Agent Standard (Section 1.2.2)

*Section 1.2.2* of the 2021 Definitions changes the standard by which the Calculation Agent is required to act. It is still obliged to act "in good faith" but is now required to use "commercially reasonable procedures to produce a commercially reasonable result" (rather than "in a commercially reasonable manner" as per the 2006 Definitions). This is the same wording as used in the 2002 ISDA Master Agreement in relation to the calculation of a close-out amount.

Under Section 4.14 of the 2006 Definitions, the Calculation Agent is also required to select banks or dealers in relation to making a calculation or determination or select any exchange rate in good faith and, if practicable, after consultation with the other party/parties. *Section 1.2.2(ii)* of the 2021 Definitions tracks this wording and expands its application to also encompass the selection of any rate.

#### No Fiduciary Duty (Section 1.2.2(iii))

The 2021 Definitions includes an express statement that, unless otherwise agreed, the Calculation Agent does not act as a fiduciary for, or as an advisor to, either party. The 2006 Definitions do not contain an express statement of this nature.

#### Determinations by the Calculation Agent (Section 1.2.4) and the Calculation Statement (Section 1.2.3)

Under the 2006 Definitions, the Calculation Agent is responsible for delivering a notice to the parties on the Calculation Date showing in reasonable detail its calculation of various payment amounts. The 2021 Definitions instead require the Calculation Agent to notify the parties of any required determination as soon as reasonably practicable after making it.

In respect of a payment date, either party can then obtain further information by asking the Calculation Agent to notify them of the amount payable, the party from which it is due and the payment date. If requested to do so, the Calculation Agent must also provide a Calculation Statement (to the extent it would contain additional information). A Calculation Statement will show in reasonable detail any calculations made, including any Relevant Market Data1 used for a determination. As set out in *Section 1.2.4(c)*, the request for a Calculation Statement must be made in writing and within a reasonable time of being notified of the determination.

#### Limits on Disclosure (Section 1.2.4(d))

The Calculation Agent is not required to disclose (including in any Calculation Statement) information that it considers to be proprietary, constitutes material non-public information or the disclosure of which would breach a confidentiality obligation. It is required to notify the parties if it withholds any such information on these grounds.

#### Calculation Date (Section 1.2.5)

The 2021 Definitions draw on the wording of Section 4.15 of the 2006 Definitions in defining the deadline by which requested notice of the amount payable, the party from which it is due and the payment date must be provided. However, it distinguishes between different types of calculation (for example, those made in arrears and those made in advance of the start of the relevant period).

#### Notices to the Calculation Agent (Section 1.3)

The 2021 Definitions oblige the parties to provide the Calculation Agent with copies of notices they deliver to each other to the extent the Calculation Agent is not itself a party.

### Other Relevant Provisions

The Calculation Agent's duties are also set out in other provisions in the 2021 Definitions. In particular, *Section 8.6.7* (Calculation Agent Determinations under the Generic Fallback Provisions) and *Section 18.3* (Fallback Cash Settlement Amount – Dispute Resolution Process) contain new provisions relevant to the Calculation Agent, both in terms of the duties it is required to undertake and the standards it is required to apply. Those provisions are described separately in this document.

# DAYS, DATES AND PERIODS

The 2021 Definitions include important additions and improvements to how days, dates and periods are defined and determined in the ISDA Definitions – including Payment Dates, Period End Dates, Calculation Periods, and Business Day calendars and conventions. However, the basic framework of how days, dates and periods interact in the ISDA Definitions is unchanged – Calculation Periods, for example, are still defined by reference to Period End Dates and the concept of a Reset Date.

### Business Day Calendars

#### Business Day (Section 2.1.1) and Absence of a Banking Day Definition

Under the 2006 Definitions, there are separate definitions for Banking Days and Business Days, with Banking Days generally used to determine the fixing day under FROs and Business Days typically used to determine a Payment Date. However, in practice, there is no difference in the calendars used for each of them.

In order to simplify the interest rate definitions, and in alignment with the approach taken in other ISDA definitional booklets, the 2021 Definitions drop the use of Banking Day in favor of a single defined term (Business Day) to cover both concepts.

#### Currency Business Day (Section 2.1.2)

A separate defined term – Currency Business Day – has been added in the 2021 Definitions that references the default financial center of the currency. This term is used where a financial center or a bespoke day calendar is not otherwise specified in the 2021 Definitions or in the relevant Confirmation. In the 2006 Definitions, equivalent provisions were included within the definition of Business Day. The definitions for currencies and relevant financial centers are set out in a matrix (the Currency/Business Day Matrix) rather than in the main book, and currencies have been added and removed versus the 2006 Definitions (for example, Estonian kroon/Tallinn is not set out in the Currency/Business Day Matrix despite being included in Section 1.5 of the 2006 Definitions because it has been replaced with the euro).

#### Publication Calendar Day (Section 2.1.4)

In recent years, ISDA has published notifications to members about the non-publication of benchmarks on good Business or Banking Days under the Business or Banking Day calendar referenced in the relevant FRO. Some of these occurrences were due to the publication calendar of the relevant benchmark not aligning with any Business or Banking Day definition.

To address this, the concept of Publication Calendar Day has been included in the 2021 Definitions. This can be referenced in FROs when the publication calendar of the relevant benchmark does not align with the calendar of the relevant financial center or any bespoke Business Day definition included in the 2021 Definitions.

#### New York Fed Business Day (Section 2.1.6)

The definition of New York Fed Business Day in the 2006 Definitions is very broad and could result in a day being considered a good New York Fed Business Day even when those parts of the Fed that are important to the derivatives market are closed.

To address this, the scope of this definition has been narrowed under the 2021 Definitions. Only the Fedwire Securities Service and the Fedwire Funds Service are now required to be open in order for a day to be considered a good New York Fed Business Day.

#### NYSE Business Day (Section 2.1.7)

This definition is substantively unchanged from the 2006 Definitions. It tracks and merges Section 1.10 (NYSE Business Day) of the 2006 Definitions with wording pertinent to the definition of NYSE Business Day from Section 1.4 (Business Day) of the 2006 Definitions.

#### Abu Dhabi Days (Section 2.1.9)

Under the 2006 Definitions, the definition of Business Day with respect to Abu Dhabi does not make allowance for Saturdays being good days for making payments in UAE Dirham, but not being good fixing days under the EIBOR benchmark.

To address this, two bespoke calendar day definitions – Abu Dhabi Settlement Day and Abu Dhabi Business Day – have been included in the 2021 Definitions.

#### Hong Kong Business Days (Section 2.2)

The 2021 Definitions incorporate changes that were made to the 2006 Definitions following the resumption to work arrangements after a super typhoon hit Hong Kong. These provisions clarify what situations would be deemed a Hong Kong Business Day given a black rainstorm warning, a typhoon signal and/or an extreme conditions announcement. These provisions are expected to be supplemented in the 2006 Definitions shortly.

#### Business Day Convention

*Section 2.3* of the 2021 Definitions tracks the Business Day Convention definition in Section 4.12 of the 2006 Definitions but restructures it (for example, by splitting out the definitions of the conventions into separate sub-paragraphs). It also adds some new concepts described further below.

#### No Adjustment Business Day Convention (Section 2.3.5)

Period End Dates can be specified as being 'unadjusted' under the 2006 Definitions, but the concept is not formally captured as a Business Day Convention that can be applied to other dates.

Under the 2021 Definitions, a new No Adjustment Business Day Convention has been added that can be applied to any date.

#### Unscheduled Holidays (Section 2.3.6 and 2.3.7)

A new concept of Unscheduled Holidays has been introduced into the 2021 Definitions.

Under the 2006 Definitions, if a public holiday or market closure is announced without sufficient notice, the application of the Modified Following or Preceding Business Day Conventions can technically result in a payment obligation moving to a date in the past.

This situation occurred when the 2020 Chinese Lunar New Year extension was announced after the start of the holiday period. The guidance that ISDA published can be [found here.](https://www.isda.org/a/eFLTE/ISDA-guidance-2020-Lunar-New-Year-extension-%E2%80%93-Interest-Rate-Derivatives.pdf)

Under the 2021 Definitions, if a public holiday (including an extension to an already scheduled public holiday) or market closure occurs with less than two Business Days' notice, then it is considered to be an Unscheduled Holiday. Notwithstanding the Modified Following or Preceding Business Day Conventions that may be stated to apply, any Cash Settlement Valuation Date, Date for Payment, Period End Date or Termination Date that would otherwise have fallen on such a day will instead move to the next following Business Day.

These Unscheduled Holiday provisions can also be applied to Period End Dates and the Termination Date, but not by default – they will need to be turned on via an election in the Confirmation to apply to such dates.

#### Period End Dates and the Actual/Actual (ICMA) Business Day Convention (Section 4.6.1(iii) and Section 3.1.12)

The Actual/Actual (ICMA) Day Count Fraction requires careful attention when a Payment Date/Period End Date falls on a non-Business Day and is adjusted by a Business Day Convention.

Under ICMA Rule 251, an adjustment made to a coupon date because it has fallen on a non-business day does not lead to an irregular period being created – the extra days are added to both the numerator and the denominator of the equation and therefore do not adjust the accrual.

The 2021 Definitions clarify this by stating that the No Adjustment Business Day Convention applies to Period End Dates when this Day Count Fraction applies.

### Dates and Periods

#### Independent Adjustment for Payment Dates/Period End Dates (Section 3.1.11 and Section 3.1.12)

Under the 2006 Definitions, it has been common practice for parties to set out the Payment Dates in their Confirmations and rely on the default position in Section 4.10(a) that, if not otherwise specified, the Period End Dates will be each Payment Date (rather than separately specifying the Period End Dates). In this scenario, the Business Day Convention for Period End Dates does not apply and the Period End Dates adjust in line with the Payment Dates instead – ie, there is no independent application of a Business Day Convention and it is not possible to elect for Period End Dates to be unadjusted. This caused outcomes that may not have been expected for some users of the 2006 Definitions following the 2020 Chinese Lunar New Year extension.

To address this, parties have the ability to specify an independent Business Day Convention for Period End Dates under the 2021 Definitions, even if they are relying on the default that Period End Dates are the dates specified as the Payment Dates in the Confirmation.

#### IMM Dates (Section 3.1.14)

Three new IMM Date definitions have been added to the 2021 Definitions. These relate to specific futures contracts denominated in Australian dollar, Canadian dollar and New Zealand dollar. These are in addition to the original IMM Settlement Date definition that is carried over from the 2006 Definitions.

#### End of Month Convention (Section 3.1.15)

An end-of-month Payment Date/Period End Date convention has been used by market infrastructures for a number of years but is not a concept that is defined in the 2006 Definitions.

Under the 2021 Definitions, a definition had been added so that if End-of-month (EOM) is specified, it will mean the last calendar day of each month during the term of the transaction.

#### Adjustment Hierarchy for Payment Dates/Period End Dates (Section 3.3)

It is common for Payment Dates and Period End Dates to be specified in Confirmations by referring to other dates – for example, the final Payment Date is often specified as the Termination Date.

The 2021 Definitions include Adjustment Hierarchy provisions that clarify which Business Day Convention adjustment applies in these scenarios – for example, where the Payment Dates are stated to be subject to the Modified Following Business Day Convention, while the Termination Date is said to be unadjusted.

In the example above, this means the final Payment Date will adjust in accordance with the Business Day Convention specified to apply to Payment Dates while the Termination Date itself remains unadjusted.

## CALCULATIONS OF FIXED AND FLOATING AMOUNTS

The way in which Floating Amounts and Fixed Amounts are determined has not been fundamentally changed in the 2021 Definitions. There are, however, some important additions and improvements described below. To improve clarity and facilitate the translation into code, calculations have been set out as formulae where possible rather than using the narrative text applied in the 2006 Definitions.

#### Day Count Fractions (Section 4.6)

The following additional Day Count Fraction has been included in the 2021 Definitions:

• Calculation/252

As described earlier, a clarification has also been added to the Actual/Actual (ICMA) Day Count Fraction for when a Period End Date falls on a non-Business Day. ICMA has indicated it is updating its Rule 251 (which this Day Count Fraction references) to similarly clarify this scenario.

Other Day Count Fractions included in the 2006 Definitions have been carried over to the 2021 Definitions with no material changes, except for narrative text being replaced by formula where possible.

#### Discounting (Section 4.7)

Discounting provisions are largely unchanged from the 2006 Definitions. However, to avoid the Discount Rate term used under the Discounting provisions being conflated with the Discount Rate term used under the Cash Settlement provisions, the term used for discounting in relation to the Standard Discounting and FRA Discounting formulas has been renamed Discounting Rate.

#### Compounding Methods (Section 4.9)

The Compounding Amount elections included in the 2006 Definitions – Straight Compounding Amount, Flat Compounding Period Amount and Spread Exclusive Compounding Amount (introduced via Supplement 16) – have all been carried over to the 2021 Definitions with minimal changes and have been set out as formulae where possible.

Straight Compounding can also be applied to Fixed Amounts under the 2021 Definitions (*Section 5.2.3*).

These elections should not be conflated with the Overnight Rate Compounding Methods, such as OIS Compounding, which were introduced either to be embedded in Compounded FROs or used in conjunction with overnight FROs (see the Overnight Conventions section for further information).

#### Correction Provisions (Section 4.11)

Under the 2006 Definitions, certain published and displayed rates are subject to corrections within one hour of the time when the rate is first displayed on the relevant page. This has caused issues in the past when the correction window allowed by the relevant administrator is longer than one hour.

To address this, the default correction cut-off has been changed to the longer of one hour and the period specified for corrections by the relevant administrator under its methodology. Note that this default may be overridden for specific FROs via the Floating Rate Matrix or the Confirmation.

Under the 2021 Definitions, there is no obligation for parties to pay interest on the amount of any adjustment payment resulting from any such correction.

The scope of these provisions has also been expanded generally to cover any Floating Rate Option, Settlement Rate or Currency Exchange Rate (other than certain excluded Bloomberg-published fallback rates that are included in Supplement 70 (the IBOR Fallbacks Supplement)).

#### Negative Interest Rates (Section 5.5 and Section 6.8)

The fixed negative interest rate provisions of the 2006 Definitions have been substantively imported into the 2021 Definitions and continue to apply by default.

A new floating negative interest rate method has been added to the 2021 Definitions – Zero Interest Rate Method Excluding Spread. Under this mechanism, the actual floating rate is floored at zero prior to the addition or subtraction of any Spread that is applicable to the calculation of the Floating Amount.

The floating negative interest rate methods currently included under the 2006 Definitions – Floating Negative Interest Rate Method and Zero Interest Rate Method – have been carried over to the 2021 Definitions with no material changes. The default will continue to be the Floating Negative Interest Rate Method.

#### Interpolation (Section 6.10)

Linear Interpolation is generally used to calculate a floating rate when a rate is not published for the tenor that corresponds to the relevant Calculation Period or Compounding Period. If Linear Interpolation applies, the rate will be calculated using straight-line interpolation between a rate published for the next shorter tenor and a rate published for the next longer tenor.

Under the 2006 Definitions, this interpolation calculation is described in a paragraph of narrative text. It does not express precisely how to determine the number of days in the periods corresponding to the rates for the shorter and the longer tenors. To address this, the interpolation calculation is set out as a precise formula under the 2021 Definitions.

#### DRM Provisions (Section 6.11)

The material terms of the ISDA 2013 Discontinued Rates Maturities Protocol have been incorporated into the 2021 Definitions.

These provisions deal with situations where a particular tenor (Designated Maturity) that is specified in the Confirmation for the calculation of a Floating Amount is discontinued or, if applicable, is no longer representative but at least one shorter and at least one longer tenor remain available. In this case, the rate will be determined by interpolating between the next shorter and next longer tenors that are available.

The provisions have been significantly simplified compared to the ISDA 2013 Discontinued Rates Maturities Protocol, but no substantive changes have been made except for a clarification to what happens when no shorter or longer tenor is available. In this case, the fallbacks that operate upon a permanent cessation will apply (see the Fallbacks section for further information).

# FLOATING RATE OPTIONS

FROs that set out the interest rate benchmarks used to determine floating amounts have been reformatted under the 2021 Definitions. Under the 2006 Definitions, FROs were written as blocks of narrative text with no standard format or naming convention. As they were added over time, divergent approaches to drafting increased complexity and the potential for unexpected consequences.

Where FROs in the 2021 Definitions map (ie, reference the same underlying benchmark as) FROs in the 2006 Definitions, updates may have been made – for example, to reflect the current time at which the administrator publishes the relevant benchmark. That might result in apparent differences between the FROs in the 2006 and 2021 Definitions, but these variations generally reflect the fact that the description of the underlying benchmark in the 2006 Definitions FRO has become inaccurate over time. Other changes are more substantive – for example, the addition of more robust fallbacks for benchmarks that have not been updated to include fallbacks under the 2006 Definitions (see the Fallbacks section for further information).

## The Floating Rate Matrix

FROs have been reorganized into a matrix or grid format under the 2021 Definitions, with rows representing different interest rate benchmarks and columns representing the key attributes, such as Fixing Day, Fixing Time, Administrator, etc. These column headings and each capitalized term referred to in the Floating Rate Matrix, including specific methodologies or formulae, are defined in operative provisions within the main book.

Under the Floating Rate Matrix, each FRO is organized into a Category and a Style that establish which columns in the matrix are applicable and which operative provisions of the main book apply. There are two Categories of FROs: Screen Rate and Calculated Rate.

| Category        | Style                           | Method                                                                                                                                               |
|-----------------|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| Screen Rate     | Term Rate                       |                                                                                                                                                      |
|                 | Overnight Rate                  |                                                                                                                                                      |
|                 | Swap Rate                       |                                                                                                                                                      |
|                 | Published Average Rate          |                                                                                                                                                      |
|                 | Compounded Index                |                                                                                                                                                      |
|                 | Index                           |                                                                                                                                                      |
|                 | Other                           |                                                                                                                                                      |
| Calculated Rate | Compounded Floating Rate Option | OIS Compounding                                                                                                                                      |
|                 |                                 | NB: while other methods may over time be<br>specified in the Floating Rate Matrix, the<br>only method currently referenced is OIS<br>Compounding     |
|                 | Average Floating Rate Option    | Overnight Averaging                                                                                                                                  |
|                 |                                 | NB: while other methods may over time<br>be specified in the Floating Rate Matrix,<br>the only method currently referenced is<br>Overnight Averaging |
|                 | Specified Formula               | Formula specified in the Floating Rate Matrix<br>(eg, Money Market Yield)                                                                            |

|  | Table 1: Categorization schema for FROs under the 2021 Definitions |  |
|--|--------------------------------------------------------------------|--|
|--|--------------------------------------------------------------------|--|

Screen Rate FROs typically require no input from the Calculation Agent to determine the relevant rate – the rate is observed from a chosen source. There are currently seven sub-categories of Screen Rate (each a Style): Term Rate, Overnight Rate, Swap Rate, Published Average Rate (such as the AMERIBOR Average rates), Compounded Index (such as the SONIA Compounded Index), Index (for other Index types) and Other (which is a catch-all). Note that while Overnight Rates and Compounded Indices will be observed from a chosen source, they will often be used within a specified formula set out in the main book in order to determine, respectively, a Compounded Floating Rate Option/Average Floating Rate Option or a Compounded Index Floating Rate Option.

Calculated Rate FROs require the Calculation Agent to make a calculation to determine the relevant rate using a formula or methodology set out or referenced in the Floating Rate Matrix. There are currently three sub-categories of Calculated Rate (each a Calculated Rate Style): Compounded Floating Rate Option, Average Floating Rate Option and Specified Formula.

Under the Compounded Floating Rate Option and Average Floating Rate Option Calculated Rate Styles, the relevant Compounding Method or Averaging Method will also be specified in the matrix (for example, OIS Compounding or Overnight Averaging).

The Specified Formula Calculated Rate Style is intended for Calculated Rate FROs that use a bespoke formula or a methodology not otherwise covered by the Compounding Methods or Averaging Methods. Bond Equivalent Yield and Money Market Yield are two examples of a Specified Formula that is referred to in the Floating Rate Matrix and defined in the main book.

## Naming Convention

FRO names under the 2006 Definitions do not follow any strict or defined conventions or rules. This makes it difficult to predict what a new FRO will be named or to understand what type of rate it is without reading the FRO definition (for example, whether it requires a calculation to be made or includes a specific formula). Some FROs under the 2006 Definitions also include special characters (such as punctuation) that can cause processing difficulties (for example, in matching or saving to a database).

To address these issues, a common rule-based naming convention for FROs has been introduced under the 2021 Definitions. The new naming convention combines the currency, the administrator-assigned name and the function (where applicable) in a specific, consistent pattern. The naming convention also designates the character set and how delimiters are to be used. It has not been possible to create a set of immutable rules in this respect. Administrators do not conform to any conventions in naming their benchmarks, so it may be necessary (and has been in Version 1 of the Floating Rate Matrix) to flex the convention in order to ensure FROs are described logically and uniquely.

#### FRO Naming Convention:

### **CCY-Rate Name-[Function]**

where:

**CCY** is a three-character ISO (International Organization for Standardization) currency code (eg, USD).

**Rate Name** is an easily identifiable and sufficiently unique name for the rate. It is based on the name given to the rate by the sponsor or administrator. ISDA may enhance the name to make it unique and to meet other guidelines.

**Function** is included, for example, for Calculated Rate FROs and is the calculation function – eg, OIS Compounding.

Within the FRO name, individual words will be expressed in mixed case separated by single space characters, with acronyms in uppercase.

A document detailing the FRO naming convention rules has been published separately on the ISDA website2 .

As a consequence of the new naming convention, most of the FROs included under the 2006 Definitions will have different names under the 2021 Definitions (if included). In many cases, this will be due to updating outdated references (eg, the reference to British Bankers' Association will be removed from the LIBOR FROs), in addition to following the conventions described above.

### Golden Source

Unlike the 2006 Definitions, FROs under the 2021 Definitions are publication-source agnostic; multiple FROs will no longer be produced for a single rate in order to represent all or a subset of alternative publication sources.

Instead, a single FRO will be defined by reference to the administrator that produces the underlying benchmark. This will streamline the ISDA Definitions and help facilitate fungibility for clearing. Market participants can also then source the FRO from their preferred price source. In the event of any inconsistency between those sources, it will be the level produced by the Administrator that will be definitive (whether the Administrator publishes the rate or makes it available through any other source).

### Reference Bank Floating Rate Options

Unlike the 2006 Definitions, the 2021 Definitions do not include standalone Reference Bank FROs. If a fallback to a Reference Bank-determined rate is required for a particular FRO, the relevant row in the Floating Rate Matrix will refer to a set of generic Reference Bank fallback provisions in the main book (see the Fallbacks section for further information).

### Broker Rates

Some FROs currently included in the 2006 Definitions are not used to determine Floating Amounts under derivatives documentation (these are generally broker rates). These rates have typically been incorporated as FROs in order to generate a Financial products Markup Language (FpML) code so the relevant rate can be used in systems and infrastructures for other purposes (such as bond pricing).

These benchmarks are not being included as FROs in the 2021 Definitions. Instead, the generation of FpML codes for these types of rates is being achieved by means of their inclusion in a separate publication on the ISDA website called the Broker Rate Source. This will allow FpML codes to be generated quickly and efficiently and will streamline the 2021 Definitions. The naming of these rates will follow the naming logic of the FROs.

#### Updating the Floating Rate Matrix

It is common for new FROs to be added to the interest rate definitions (eg, when a new benchmark is created) and for existing FROs to be amended (eg, because a publication time or an administrator has changed).

To add or amend an FRO under the 2006 Definitions, a supplement to the main book would need to be published. A large number of supplements have been published as a result, making it difficult to find the current version of an FRO. To confirm a trade using an FRO that is published in the original 2006 Definitions booklet, market participants would need to review all subsequent supplements to determine if that FRO has been amended or replaced at a later date.

To address this, the Floating Rate Matrix will be re-versioned in its entirety whenever a new FRO is added, removed or amended. This will enable users to effortlessly view all current versions of FROs at the time of their trade.

As changes to FROs and the addition of new ones will likely be frequent, the Floating Rate Matrix will be re-versioned separately to the main book, making it easier to distinguish amendments to FROs and other operative provisions.

As described further in the Fallbacks section, certain FROs in the Floating Rate Matrix refer to bespoke fallback provisions set out in *Section 9* of the main book. That may mean the main book also needs to be re-versioned if there are amendments to those FROs, or if FROs that require bespoke fallback provisions are introduced or amended to require such provisions.

## OVERNIGHT RATE CONVENTIONS

A framework for documenting the different approaches for determining a Floating Amount using Overnight Rates (eg, risk-free rates or RFRs) – including compounding and averaging with a lookback, observation period shift and lockout – was recently introduced into the 2006 Definitions via Supplement 75 and has been carried over to the 2021 Definitions with minimal changes (made to ensure they fit within the 2021 Definitions structure).

This modular framework involves users selecting: (i) an appropriate Floating Rate Option for an overnight floating rate; (ii) one of the compounding or averaging approaches; and (iii) certain other elections in the Confirmation in order to complete the provisions (such as the number of days in the lookback, etc).

An ISDA memo describing these approaches is available on the ISDA website3 .

<sup>3</sup>Documenting RFR Derivatives Using Different Approaches to Compounding/Averaging Under the 2006 ISDA Definitions, [www.isda.org/a/nlzTE/](http://www.isda.org/a/nlzTE/Approaches-to-compounded-RFRs-under-the-2006-ISDA-Definitions.pdf) [Approaches-to-compounded-RFRs-under-the-2006-ISDA-Definitions.pdf](http://www.isda.org/a/nlzTE/Approaches-to-compounded-RFRs-under-the-2006-ISDA-Definitions.pdf)

## FALLBACKS

### IBOR and RFR Fallbacks

The 2021 Definitions substantively contain the triggers and fallbacks introduced by Supplement 70 to the 2006 Definitions (the IBOR Fallbacks Supplement). They also substantively contain the triggers and fallbacks set out in other recent supplements in respect of the RFRs that have been identified as alternatives to certain interbank offered rates (IBORs), including SOFR, SONIA, TONA and €STR.

These IBOR and RFR-related supplements have, for the most part, introduced both temporary nonpublication fallback provisions and permanent cessation fallback provisions. For LIBOR only, a nonrepresentativeness trigger was also included. Further information on these fallback provisions can be found on the ISDA website4 .

There are differences in the architectures of the 2006 Definitions and the 2021 Definitions and, in some instances, they use different terminology to describe the same concepts. This means the wording and format of the IBOR and RFR-related fallbacks that apply following a temporary non-publication or permanent cessation/non-representativeness trigger are not identical, even if they are expected to have substantively the same effect in practice.

As described in more detail below, the 2021 Definitions also include a new trigger that allows parties to move to an alternative rate if either one of them, or the Calculation Agent, is not or will not be permitted under any applicable law or regulation to use the underlying benchmark for the FRO. This represents a substantive difference to the position under the 2006 Definitions. Please refer to the Administrator/Benchmark Event description below for further information.

### Other Fallbacks Under the 2006 Definitions and New FROs

There are many other FROs in the 2006 Definitions that have not been the subject of specific global reform efforts and the triggers and fallbacks that currently apply (to the extent they have any) may be of varying utility. The 2021 Definitions also contain some FROs that were not included in the 2006 Definitions and so have required new fallbacks to be created.

### Broad Approach to Fallbacks Under the 2021 Definitions

Table 2 sets out the general approach that was taken to populating triggers and fallbacks under the 2021 Definitions. There are, however, exceptions to these broad principles, so the relevant provisions in the Floating Rate Matrix and main book should be reviewed for each FRO to obtain a definitive view of the triggers and fallbacks. Note that *Section 8* (Fallbacks) also applies to other benchmarks under the 2021 Definitions, including Settlement Rates and Currency Exchange Rates. The full list is set out in the definition of Applicable Benchmark at *Section 8.5.1*.

| Type of Benchmark                                                                                                                                                                                                                                                                                             | Temporary Non<br>Publication Trigger                                                                                                             | Temporary<br>Non-Publication<br>Fallback                                                                                                                                                                                                                                                                                | Permanent<br>Cessation<br>Trigger | Permanent<br>Cessation<br>Fallback | Administrator/<br>Benchmark<br>Event Trigger5 | Administrator/<br>Benchmark<br>Event Fallback      |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|------------------------------------|-----------------------------------------------|----------------------------------------------------|
| •Supplement 70<br>Benchmarks (such as<br>LIBOR)<br>•RFRs recently updated<br>or added to the 2006<br>Definitions that include<br>bespoke fallback<br>language.<br>•Other benchmarks with<br>bespoke temporary<br>non-publication and<br>permanent cessation<br>fallback provisions in<br>the 2006 Definitions |                                                                                                                                                  | Substantively ported into the 2021 Definitions by means of Section 9                                                                                                                                                                                                                                                    |                                   |                                    | Applicable                                    | Same as bespoke<br>Permanent<br>Cessation Fallback |
| FROs included in the<br>2006 Definitions with<br>fallbacks to previous<br>day's rate                                                                                                                                                                                                                          | Standard Temporary<br>Non-Publication Trigger<br>(without explicitly<br>carrying over any cut-off<br>times specified in the<br>2006 Definitions) | Temporary Non<br>Publication – Previous<br>Day's Rate                                                                                                                                                                                                                                                                   | Index Cessation<br>Event          | Generic Fallbacks                  | Applicable                                    | Generic Fallbacks                                  |
| FROs included in the<br>2006 Definitions with<br>Reference Bank fallbacks                                                                                                                                                                                                                                     | Standard Temporary<br>Non-Publication Trigger<br>(without explicitly<br>carrying over any cut-off<br>times specified in the<br>2006 Definitions) | Where bespoke,<br>the Reference Bank<br>fallback has been<br>substantively ported<br>into Section 9. Where<br>more straightforward,<br>the Floating Rate Matrix<br>specifies Temporary Non<br>Publication – Reference<br>Banks (using the same<br>number of Reference<br>Banks as specified in the<br>2006 Definitions) | Index Cessation<br>Event          | Generic Fallbacks                  | Applicable                                    | Generic Fallbacks                                  |
| FROs included in the<br>2006 Definitions with a<br>fallback to Calculation<br>Agent determination or<br>without any fallback                                                                                                                                                                                  | Standard Temporary<br>Non-Publication Trigger<br>(without explicitly<br>carrying over any cut-off<br>times specified in the<br>2006 Definitions) | Temporary Non<br>Publication Fallback:<br>Alternative Rate                                                                                                                                                                                                                                                              | Index Cessation<br>Event          | Generic Fallbacks                  | Applicable                                    | Generic Fallbacks                                  |

### Temporary Non-Publication Trigger

The IBOR Fallbacks Supplement introduced temporary non-publication triggers and fallbacks for the IBORs it covered. The 2021 Definitions extend this concept to other benchmarks (including other FROs set out in the Floating Rate Matrix).

Some FROs have bespoke Temporary Non-Publication Triggers specified in the Floating Rate Matrix or Section 9 of the main book. These are typically based on triggers already contained in equivalent (or related) FROs in the 2006 Definitions for the same underlying benchmark.

For other Applicable Benchmarks, a Standard Temporary Non-Publication Trigger has been created (see Section 8.1.2). This standardized trigger is essentially an amalgamation of the various temporary nonpublication triggers set out for each IBOR in the IBOR Fallbacks Supplement.

<sup>5</sup>NB: this trigger has been broadened (when compared to Administrator/Benchmark Event from the 2018 ISDA Benchmarks Supplement) to include any event or circumstance that results in the parties being unable to use the benchmark in question in the relevant transaction

FROs in the 2006 Definitions sometimes contain cut-off times that stipulate the latest time at which an FRO level can be published before a fallback will begin to apply. These were not generally carried over to the 2021 Definitions because the Standard Temporary Non-Publication Trigger, when read in conjunction with the definition of Fixing Time, provides a standardized set of provisions relating to the timing of any cut-off.

### Temporary Non-Publication Fallback

Some Applicable Benchmarks have bespoke Temporary Non-Publication Fallbacks set out in Section 9 of the main book. These are based on the bespoke fallbacks contained in the FROs in the 2006 Definitions for the same underlying benchmark. For others, standardized fallback provisions have been created (see Section 8.1.3), as summarized in Table 3.

| Standardized fallbacks for<br>temporary non-publication                             | Summary                                                                                                                                                                                                                                                                                                                                                                                                                                                  | Comments                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|-------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Temporary Non-Publication Fallback –<br>Alternative Rate (<br>)<br>Section 8.1.4    | •A rate formally recommended by the<br>Administrator<br>•A rate formally recommended by: (i) the<br>supervisor/competent authority responsible<br>for supervising the Applicable Benchmark<br>or Administrator; OR (ii) a committee<br>officially endorsed/convened by a<br>supervisor/competent authority specified<br>in (i) above<br>•If no rate results from the above two<br>limbs, Calculation Agent Alternative Rate<br>Determination (see below) | •Based upon the temporary non-publication<br>fallbacks in Supplement 70. A version<br>of this fallback appearing in several of<br>the Supplement 70 FROs refers to a<br>formal recommendation by a committee<br>endorsed/convened by the regulator<br>•Often used as the standardized temporary<br>non-publication fallback for Term Rates                                                                                                                      |
| Temporary Non-Publication Fallback –<br>Previous Day's Rate (<br>)<br>Section 8.1.5 | The last provided level is used                                                                                                                                                                                                                                                                                                                                                                                                                          | •A number of FROs (particularly overnight<br>rates) in the 2006 Definitions fall back to<br>the previous day's rate<br>•Often used as the standardized temporary<br>non-publication fallback for Overnight<br>Rates in the 2021 Definitions                                                                                                                                                                                                                     |
| Temporary Non-Publication Fallback –<br>Reference Banks (<br>)<br>Section 8.1.6     | This uses the arithmetic mean of quotations<br>sourced from Reference Banks                                                                                                                                                                                                                                                                                                                                                                              | •A number of FROs in the 2006 Definitions<br>contain a fallback to a Reference Bank<br>poll, although they use a variety of<br>averaging methodologies to determine<br>the resulting rate. Some include a further<br>fallback to rates provided by major banks<br>if there are insufficient quotations from<br>major dealers<br>•The 2021 Definitions standardize these<br>different approaches and specify which<br>should be used in the Floating Rate Matrix |
| Calculation Agent Alternative Rate<br>Determination (<br>)<br>Section 8.1.7         | A commercially reasonable alternative for<br>the Applicable Benchmark determined by<br>the Calculation Agent after consideration<br>of relevant information, including a rate<br>implemented by central counterparties and/<br>or futures exchanges, in each case with<br>sufficient trading volumes                                                                                                                                                     | •Use of this upon failure of Temporary<br>Non-Publication Fallback – Alternative Rate<br>maps similar provisions in Supplement 70                                                                                                                                                                                                                                                                                                                               |

Table 3: Standardized Temporary Non-Publication Fallback Provisions

### Permanent Cessation and Administrator/Benchmark Event

The 2021 Definitions contain two standard triggers that result in the permanent application of an alternative rate: Index Cessation Event and Administrator/Benchmark Event. The fallbacks that apply following these events are either bespoke (usually to ensure substantive alignment with the 2006 Definitions) or use the Generic Fallback Provisions, which are closely based on similar provisions from the 2018 ISDA Benchmarks Supplement.

### Index Cessation Event (Including for a Non-Representative Election)

*Section 8.2.3* of the 2021 Definitions closely tracks the definition of Index Cessation Event from the IBOR Fallbacks Supplement to provide a standardized permanent cessation trigger and extends it to any fallback rate. It encompasses the concept of a Fallback Index Cessation Event under the IBOR Fallbacks Supplement for streamlining purposes.

As with the IBOR Fallbacks Supplement, the definition of Index Cessation Event in the 2021 Definitions includes a Non-Representative limb, but this must be specified to apply to an Applicable Benchmark, either in the Floating Rate Matrix or in the Confirmation. To maintain alignment with the IBOR Fallbacks Supplement, this election has been applied for the LIBOR FROs in Version 1 of the Floating Rate Matrix. If parties want to apply this election to other rates, then this can be achieved in the Confirmation. Alternatively, if there is sufficient support for applying the Non-Representative trigger by default to a specific FRO to which it does not currently apply, the trigger could be specified to apply in an updated version of the Floating Rate Matrix.

The 2021 Definitions also mirror the provisions contained in the IBOR Fallbacks Supplement that relate to index cessation in the context of individual tenors (*Section 6.11* (Discontinued Rates Maturities), as well as *Section 9.35* (SGD-SOR) and *Section 9.38* (THB-THBFIX)).

The timing of when a transaction stops referencing a benchmark that is subject to an Index Cessation Event follows the approach taken under the IBOR Fallbacks Supplement. Specifically, the fallback rate is only used from the date the original benchmark permanently ceases publication or becomes nonrepresentative (if applicable) rather than, for example, from the date on which an announcement is made that the original benchmark will fall away on a date in the future (see *Section 8.2.4* (Index Cessation Effective Date)).

The Permanent Cessation Fallbacks for an FRO are specified in the Floating Rate Matrix or the Confirmation. *Section 9* (Bespoke Triggers and Fallbacks) of the main book sets out all the bespoke fallbacks that are referenced in that matrix. These are usually bespoke to align with the 2006 Definitions. The fallbacks for LIBOR and other benchmarks in scope of the IBOR Fallbacks Supplement are set out in *Section 9* of the main book alongside the bespoke fallbacks for the RFRs recently published under the 2006 Definitions. For FROs without bespoke fallbacks, the Generic Fallback Provisions apply.

### Administrator/Benchmark Event

This new trigger, which was not included in the 2006 Definitions, has been introduced to ensure robust fallbacks in circumstances where a party to a transaction delivers a notice (including Publicly Available Information) to the other party confirming that either or both of the parties (or the Calculation Agent) is not or will not be permitted under any applicable law or regulation to use the relevant benchmark to perform its/their obligations.

This is a transaction-specific and party-specific trigger. In other words, if the parties and Calculation Agent continue to be permitted to use the benchmark for the transaction type in question, then there will be no trigger and no fallback, even if they are prohibited from referencing the same benchmark in another transaction type or other parties are barred from using the benchmark in similar transactions.

This trigger has been made broader than the Administrator/Benchmark Event trigger first introduced in the 2018 ISDA Benchmarks Supplement. In the 2021 Definitions, it refers to "an event or circumstance", whereas the 2018 ISDA Benchmarks Supplement listed specific events or circumstances that would qualify in order to more closely track the provisions of the EU Benchmarks Regulation.

In respect of each Floating Rate Option set out in the Floating Rate Matrix, an election is included in the matrix specifying whether the Administrator/Benchmark Event trigger is applicable. In Version 1 of the Floating Rate Matrix, this trigger is specified to be applicable for each Floating Rate Option included in the Floating Rate Matrix. The application of this trigger, however, is subject to any alternative election made in the Confirmation. For any other Applicable Benchmarks (including a Settlement Rate, Currency Exchange Rate, Discounting Rate – see *Section 8.5.1* for the complete list), the Administrator/ Benchmark Event trigger applies by default unless the parties elect otherwise in the Confirmation.

If an event or circumstance that would qualify as, or give rise to, an Administrator/Benchmark Event also qualifies as an Index Cessation Event, then the Index Cessation Event provisions would prevail.

The way in which the fallbacks for Administrator/Benchmark Event are conceived in the Floating Rate Matrix is that, for benchmarks that have bespoke Permanent Cessation Fallbacks, the same fallbacks will apply in the event of an Administrator/Benchmark Event. For the IBORs covered by the IBOR Fallback Supplement, the Bloomberg-published fallback rate will use the indicative Bloomberg spread until that spread is 'fixed' upon the occurrence of an Index Cessation Event under the Bloomberg Rulebook (at which point, the fixed spread will be used). For benchmarks that do not have bespoke Permanent Cessation Fallbacks but instead apply the Generic Fallback Provisions as the Permanent Cessation Fallback, those Generic Fallback Provisions will also apply as the Administrator/Benchmark Fallback.

### Generic Fallback Provisions

These provisions have been introduced to provide a framework for ascertaining a fallback rate upon the occurrence of a Permanent Cessation Trigger or Administrator/Benchmark Event, but where no marketstandard fallbacks have otherwise been identified and included in the 2021 Definitions. They closely track the Alternative Continuation Fallback provisions from the 2018 ISDA Benchmarks Supplement.

Under the Generic Fallback Provisions, the parties must use commercially reasonable efforts, acting in good faith, to try to identify or agree a fallback to allow the relevant transaction to continue in accordance with its terms. This must be done during a defined period that starts on the date of the Permanent Cessation Trigger or Administrator/Benchmark Event (as the case may be) and ends on the Cut-off Date. This is designed to provide as much time to the parties as reasonably possible to identify a fallback before their inability to use a level of the original benchmark becomes material.

### Alternative Continuation Fallbacks Under the Generic Fallback Provisions

*Section 8.6.3* sets out a list of Alternative Continuation Fallbacks that the parties must seek to apply during the period described above. Rather than attempting these in sequence (which would likely be slow, resulting in time being spent trying to apply provisions that are unlikely to supply a fallback rate in the prevailing circumstances), the parties are required to attempt to apply them all in parallel. If more than one fallback is identified, the first Alternative Continuation Fallback listed in *Section 8.6.3* would prevail.

![](_page_24_Figure_1.jpeg)

Figure 1: Alternative Continuation Fallbacks to be Pursued in Parallel or No-Fault Termination Rights Arise

### Adjustments Under the Generic Fallback Provisions

The alternative index applied under an Alternative Continuation Fallback may have different characteristics from the index it replaces. Provision is therefore made for an Adjustment Payment (to be agreed between the parties) or an Adjustment Spread (to be agreed between the parties, determined by the Calculation Agent or, for an Alternative Post-nominated Index, using any spread formally designated by the Relevant Nominating Body). The Calculation Agent may make other adjustments to account for the effect of using the new index in the transaction.

## Disputes Under the Generic Fallback Provisions

*Section 8.6.7* gives the parties the right to dispute Calculation Agent determinations in relation to the Adjustment Spread, nomination of the Calculation Agent Nominated Replacement Index and any other adjustment made to the terms of a transaction by the Calculation Agent under the Generic Fallback Provisions. The dispute needs to be reasonable and supported by data. If a dispute cannot be resolved by agreement, the relevant Alternative Continuation Fallback is disregarded. If the parties have agreed a different process for disputing determinations made by the Calculation Agent, then that process will apply instead of the dispute resolution process set out in *Section 8.6.7*.

## No-Fault Termination Rights Under the Generic Fallback Provisions

If none of the Alternative Continuation Fallbacks can be successfully applied within the allotted period of time, each party acquires the right to terminate the relevant transaction using a no-fault termination methodology. This termination right expires 10 Local Business Days after any of the Alternative Continuation Fallbacks are disregarded following a dispute. Thereafter, the relevant Alternative Continuation Fallback will be applied using the determination made by the Calculation Agent before, or as part of, the dispute process.

### Temporary Fallbacks

If the level of an index is required after the original benchmark has ceased to exist or after its use has been prohibited following an Administrator/Benchmark Event, but before the relevant Alternative Continuation Fallback has been applied, *Section 8.6.6(ii)* provides a series of temporary fallbacks that can be used (including those specified for a Temporary Non-Publication Trigger).

### Non-compliant Fallbacks

If it is unlawful under any applicable law or regulation to determine the benchmark in accordance with a fallback, a fallback would contravene any applicable licensing requirements, or the Calculation Agent considers the Adjustment Spread to constitute a benchmark that would subject the Calculation Agent to additional regulatory obligations it is unwilling to undertake, then the fallback is deemed not to apply.

#### Major Change in Methodology for a Benchmark

The 2021 Definitions do not introduce new fallback arrangements following a material change to the methodology of a benchmark (unless specified for a particular FRO in the Floating Rate Matrix or *Section 9* (Bespoke Triggers and Fallbacks)). Instead, the new definitions track the approach taken in the IBOR Fallbacks Supplement and the 2018 ISDA Benchmarks Supplement in including an acknowledgement that the transaction will continue to reference the benchmark as changed (see *Section 1.4*).

# EXERCISE OF SWAPTIONS/OPTIONAL EARLY TERMINATIONS

The framework and provisions relating to the exercise of Swaptions and Optional Early Terminations in the 2006 Definitions are substantively unchanged in the 2021 Definitions.

However, there are some changes in certain areas.

### Definition of Option Transaction

Under the 2006 Definitions, a swap transaction that includes rights of early termination (Optional Early Termination Rights) is classified as an Option Transaction. Under the 2021 Definitions, only Swaptions and Swaption Straddles are classified as Option Transactions (as well as any other transaction identified as an Option Transaction in the Confirmation). Optional Early Terminations Rights are defined separately under the 2021 Definitions, although they rely on the same provisions for the exercise of Option Transactions, now called Exercise of Options and Optional Early Termination Right.

### Bermuda Options

Bermuda Option Exercise Date (as used in the 2006 Definitions) has been renamed Bermuda Option Potential Exercise Date under the 2021 Definitions to reflect the fact there may not be an actual exercise or deemed exercise on such dates.

### Automatic Exercise and Fallback Exercise

The definitions of In-the-Money and Out-of-the-Money (which are used to determine if the exercise of the relevant rights is triggered under the Automatic Exercise and Fallback Exercise provisions and if a Cash Settlement Amount is payable by a party) have been modified under the 2021 Definitions to accommodate transactions other than vanilla interest rate swaps. For more complex transactions, the Mid-Market Valuation (Calculation Agent Determination) Cash Settlement Method will be used to calculate a hypothetical cash settlement amount that is then used to determine whether a party is In-the-Money or Out-of-the-Money.

For simplification purposes, the provisions for determining the Settlement Rate for the purposes of Automatic Exercise and Fallback Exercise (ie, for determining whether a vanilla interest rate swap is Inthe-Money or Out-of-the-Money for such purpose) are being merged in the 2021 Definitions with the provisions for determining the Settlement Rate under Cash Settlement, as they are conceptually the same and the provisions are largely duplicative in the 2006 Definitions.

## CASH SETTLEMENT PROVISIONS

Under the 2021 Definitions (as was the case under the 2006 Definitions), a Cash Settlement Amount is due upon: (i) the exercise of a Swaption to which Cash Settlement applies; (ii) the exercise of an Optional Early Termination (OET) right under a transaction (unless Cash Settlement is excluded); or (iii) the Mandatory Early Termination (MET) of a transaction.

As per the 2006 Definitions, the parties to one of these transactions will first attempt to mutually agree the Cash Settlement Amount (Section 18.1.1). If they do, then payment is made and that is the end of the process. If the parties are unable to agree on the amount, then it is determined according to a methodology (a Cash Settlement Method) that the parties elect at the point of trading. Under the 2021 Definitions, the amount determined by the Calculation Agent under any of these methodologies is called the Fallback Cash Settlement Amount (Section 18.1.4).

The Cash Settlement Method used to determine the Fallback Cash Settlement Amount can be specified in the Confirmation. If not specified in the Confirmation, then it will be as set out in the ISDA Settlement Matrix for the relevant currency and relevant type (ie, swaption cash settlement or OET/ MET).

This structure is materially the same under the 2006 and 2021 Definitions.

### Limitations with the Cash Price Cash Settlement Method Under the 2006 Definitions

Most of the Cash Settlement Methods set out in the 2006 Definitions were developed before the financial crisis and the regulatory reforms that followed. They therefore do not reflect subsequent changes in market practice, such as the increased use of collateral and the shift to overnight index swap (OIS) discounting.

The default Cash Settlement Method for OET/MET under the 2006 Definitions – Cash Price – leaves a number of parameters unspecified – parameters that the Cash Settlement Reference Banks or Calculation Agent need to or could apply when making the determination. This may increase the possibility of receiving inconsistent quotations or outcomes for the same or similar transactions.

In the absence of sufficient quotations, the Calculation Agent is required to make the determination. The wide discretion and lack of prescribed parameters mean some market participants (and Calculation Agents) have preferred to avoid using that fallback where possible.

### Changes Under the 2021 Definitions

New Cash Settlement Methods have been included in the 2021 Definitions to replace the old Cash Price mechanism and have been designed to reduce the scope of discretion.

The new methodologies fall into two categories:

- Mid-market valuation approaches; and
- Replacement valuation approaches.

Under both approaches, there are two sub-categories, which represent the possible ways for the determination to be made:

- Reference Bank quotations; and
- Calculation Agent Determination.

Different Cash Settlement Methods are included in the 2021 Definitions for each sub-category under each approach.

Under the new methodologies, the parameters that Cash Settlement Reference Banks (or the Calculation Agent) must follow when making the determination are clearly set out. This removes much of the discretion and room for divergent outcomes that could result from the old Cash Price methodology. This will reduce uncertainty and the likelihood of disputes. It is also hoped that having a more prescribed and standardized approach will make it more likely that a Reference Bank will be able to provide a quotation when requested.

Table 4 sets out a high-level comparison of the Cash Settlement provisions between the two sets of definitions.

| 2006 Definitions – Cash Settlement Method | 2021 Definitions – Cash Settlement Method                       |
|-------------------------------------------|-----------------------------------------------------------------|
| Cash Price                                | Mid-market Valuation (Indicative Quotations)                    |
|                                           | Mid-market Valuation (Calculation Agent Determination)          |
|                                           | Replacement Value (Firm Quotations)                             |
|                                           | Replacement Value (Calculation Agent Determination)             |
| Cash Price – Alternate Method             | Mid-market Valuation (Indicative Quotations) – Alternate Method |
| Par Yield Curve - Adjusted                | Not included                                                    |
| Par Yield Curve - Unadjusted              | Par Yield Curve - Unadjusted                                    |
| Zero Coupon Yield - Adjusted              | Not included                                                    |
| Cross Currency Method                     | Not included                                                    |
| Collateralized Cash Price                 | Collateralized Cash Price                                       |

Table 4: Comparison of Cash Settlement Methods

Each of the new methodologies is described further below.

### Mid-market Valuation

The objective of this approach is to determine a theoretical mid-market price for the transaction (in the case of an OET or MET) or the underlying transaction (in the case of the exercise of a swaption).

Under this approach, there are three Cash Settlement Methods:

- Mid-market Valuation (Indicative Quotations);
- Mid-market Valuation (Calculation Agent Determination); and
- Mid-market Valuation (Indicative Quotations) Alternate Method.

#### Mid-market Valuation (Indicative Quotations) (Section 18.2.1)

Under this method, Cash Settlement Reference Banks are requested to provide an indicative quotation of the net present value of the amounts that would have been due by the parties under the remaining term of the transaction (for an OET or MET) or the underlying transaction (for a swaption).

If at least two quotations are provided, then the Cash Settlement Amount will be the arithmetic mean of the amounts specified in the quotations provided. If fewer than two Cash Settlement Reference Banks provide quotations, then the Cash Settlement Amount shall be determined as if the Cash Settlement Method is Mid-market Valuation (Calculation Agent Determination). More information about the process for selecting Cash Settlement Reference Banks and obtaining quotations is set out in a subsequent section.

A clear process and a set of assumptions that the Cash Settlement Reference Banks must follow when making a determination (called the Prescribed Methodology) are set out under this method (see box). This limits discretion and the ability of a Cash Settlement Reference Bank to make adjustments.

#### Summary of Prescribed Methodology (Section 18.2.1(iv))

- The price shall be the mid-point between the bid and offer.
- The transaction is governed by a standard-form 2002 ISDA Master Agreement (if the transaction is subject to an ISDA Master Agreement or is not subject to any other standard master agreement) or a standard-form of another industry standard master agreement (if the transaction is subject to that industry standard master agreement).
- The collateralization criteria are made via an election in the confirmation:
	- º If No CSA is specified in the Confirmation, then the assumption is that the transaction is not collateralized.
	- º If Existing CSA is specified in the Confirmation, then the assumption is that the specified main terms (the Relevant Terms) of the credit support annex (CSA) between the parties apply on the basis that the transaction is the only transaction.
	- º Otherwise, the assumption is that the transaction is subject to a bilateral variation margin (VM) CSA (a Reference VM CSA) where:
		- The transaction is the only transaction subject to the Reference VM CSA.
		- Eligible collateral is cash in the cash settlement currency (or, if there are two cash settlement currencies, then the Cash Collateral Currency specified in the Confirmation. Failing that, a default Single Cash Settlement Currency will apply).
		- Negative Interest applies.
		- The Minimum Transfer Amount is zero.
		- No Threshold applies.

Continues on next page

Continued from previous page

- The Interest Rate is the Cash Collateral Interest Rate specified in the Confirmation. If nothing is specified, then the Interest Rate is the Discount Rate set out in the ISDA Settlement Matrix for the relevant currency (or, if not set out in that matrix, the interest rate selected by the Calculation Agent for the relevant purposes).
- The methodology for how to derive the discount factors is described (if No CSA applies, then an Agreed Discount Rate can be specified in the Confirmation).
- A basis adjustment can be made if the discount rate used to determine the zero-coupon curve for discounting purposes is for a different currency than the currency of the relevant cash flows under the transaction.
- No other adjustments are permitted (such as regulatory capital, credit valuation or funding).
- Contingencies to which the payments are subject under the transaction shall be taken into account.

Recognizing that market participants use the Cash Settlement Methods more broadly than for trades under ISDA Master Agreements, the Prescribed Methodology contemplates non-ISDA master agreements and collateral agreements applying. ISDA has not undertaken any due diligence on whether use of this methodology is appropriate, effective or would require amendments to such documents to work as intended. Market participants are strongly advised to undertake their own analysis.

Requirements for the Cash Settlement Reference Banks to follow when providing a quotation, including the Prescribed Methodology and the collateralization election, will be set out in a standard Template Quotation Request Form published as an annex to the 2021 Definitions that must be used to request an indicative quotation from a Cash Settlement Reference Bank.

#### Mid-market Valuation (Calculation Agent Determination) (Section 18.2.3)

This method is the same as the Mid-market Valuation (Indicative Quotations) method, except instead of requesting quotations from Cash Settlement Reference Banks, the Calculation Agent makes the determination.

In order to reduce the scope for divergent outcomes or the need to exercise discretion, the Calculation Agent is required to follow the same Prescribed Methodology when making its determination as the Cash Settlement Reference Banks under Mid-market Valuation (Indicative Quotations).

#### Mid-market Valuation (Indicative Quotations) – Alternate Method (Section 18.2.2)

This method is the same as the Mid-market Valuation (Indicative Quotations) method, except each party selects three Cash Settlement Reference Banks and requests quotations from those they have selected.

If at least two quotations are received, then the Cash Settlement Amount is the arithmetic mean of the quotations received. If fewer than two quotations are received, then each party shall make a determination as if it is the Calculation Agent under the Mid-market Valuation (Calculation Agent Determination) method, and the Cash Settlement Amount shall be the arithmetic average of the two determinations.

This method reflects the spirit of the Cash Price – Alternate Method in the 2006 Definitions.

| Confirmation Field               | Possible Elections in the<br>Confirmations                                                       | Default Where No Election is<br>Specified in the Confirmation                                                                                                                                                                  |
|----------------------------------|--------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [MMV Applicable CSA:]            | [No CSA]/[Existing CSA]/[Reference VM CSA]                                                       | Reference VM CSA                                                                                                                                                                                                               |
| [Cash Collateral Currency:]      | [Specify currency – only needs to be<br>specified if there are two cash settlement<br>currencies | As specified in the Settlement Matrix for the<br>currency pair, or if those currencies are not<br>a currency pair under the Settlement Matrix,<br>the Termination Currency under the Relevant<br>Master Agreement              |
| [Cash Collateral Interest Rate:] | [Specify rate]                                                                                   | The Discount Rate (specified in the ISDA<br>Settlement Matrix for the relevant currency<br>or, if not specified, determined by the<br>Calculation Agent)                                                                       |
| [Agreed Discount Rate:]          | Specify rate – only needs to be specified if<br>No CSA is applicable                             | If No CSA is applicable and no election is<br>made in the Confirmation, the Discount Rate<br>specified in the ISDA Settlement Matrix for<br>the relevant currency or, if not specified,<br>determined by the Calculation Agent |

|  | Table 5: Confirmation Elections and Defaults Associated with the Mid-market Valuation Methods |  |  |  |  |  |  |  |  |  |
|--|-----------------------------------------------------------------------------------------------|--|--|--|--|--|--|--|--|--|
|--|-----------------------------------------------------------------------------------------------|--|--|--|--|--|--|--|--|--|

Under the ISDA Settlement Matrix, the default Cash Settlement Method for MET is Mid-market Valuation (Indicative Quotations), and the default Cash Settlement Method for OET is Mid-market Valuation (Indicative Quotations) with Existing CSA applicable. The default Cash Settlement Method for a cash-settled swaption varies depending on the currency and the Floating Rate for the floating leg of the underlying swap transaction but Collateralized Cash Price is used as the default for the majority of currencies.

#### Replacement Value

The objective of the second approach is to determine an amount that protects one of the parties from the financial impact of an early termination of the transaction. It is not envisaged that this approach will be used for cash-settled swaptions.

The party that is being protected (the Protected Party) can be elected in the Confirmation. However, defaults will apply if this is not specified, as follows:

- The default for an OET is for the non-exercising party to be the Protected Party; and
- The default for a MET is for both parties to be a Protected Party.

Like the Mid-market Valuation approach, there are sub-categories:

- Replacement Value (Firm Quotations); and
- Replacement Value (Calculation Agent Determination).

Both of these methods attempt to determine a value for the transaction on a replacement-cost basis – ie, the cost that the Protected Party would incur (or the amount it would gain) if it were to go out to the market to replace the transaction.

### Replacement Value (Firm Quotations) (Section 18.2.4)

Under this method, each Cash Settlement Reference Bank is requested to provide a firm quotation (ie, a tradeable quote) for the amount it would pay or charge the Protected Party to enter into a replacement transaction. Unlike the Prescribed Methodology used in the Mid-market Valuation methods, providing a tradeable quote will necessarily involve the Cash Settlement Reference Banks accounting for adjustments relating to, for example, credit risk or associated costs of funding.

The Cash Settlement Reference Banks are instructed to provide firm quotations on the basis that the replacement transaction is governed by certain documentation, called the Prescribed Documentation (see box).

### Summary of Prescribed Documentation

- The ISDA Master Agreement or other industry standard master agreement (including CSA) in place between the relevant Cash Settlement Reference Bank and the Protected Party.
- If there is no ISDA Master Agreement or other industry standard master agreement in place, then a standard-form 2002 ISDA Master Agreement with a standard form variation margin CSA is assumed to govern the transaction, where:
	- º The governing law is the same as that of the transaction;
	- º The replacement transaction is the only covered transaction;
	- º The only eligible collateral is cash in the cash settlement currency; and
	- º If there are two cash settlement currencies, then the Cash Collateral Currency specified in the confirmation (defaults apply if not specified)

To account for any material differences between the Prescribed Documentation and the actual documentation in place between the two parties to the transaction, the parties can specify Prescribed Documentation Adjustment to apply via the Confirmation. If specified to apply, the Calculation Agent will adjust each of the firm quotations received to account for these differences.

If at least two quotations are received, then the Cash Settlement Amount (where there is only one Protected Party) will be the best quotation received (ie, the highest amount that would be paid to the Protected Party or the lowest amount that would be paid by the Protected Party). This reflects the reality of what would happen if the Protected Party entered the market to put on a replacement transaction.

If there are two Protected Parties (which is the default for MET), each Reference Bank will provide a firm quotation with respect to each of the parties. A mid-point will then be calculated between those two quotations, and the Cash Settlement Amount will be the arithmetic mean of each mid-point.

If fewer than two firm quotations are received (or two mid-point quotations determined), then the Calculation Agent will determine the Cash Settlement Amount as if the Cash Settlement Method were Replacement Value (Calculation Agent Determination).

As per the Mid-market Valuation approach, requirements for the Cash Settlement Reference Banks to follow when providing a quotation, including details of the Prescribed Documentation and the election for the Cash Collateral Currency, will be set out in a standard Template Quotation Request Form. This is published as an annex to the 2021 Definitions and must be used to request a firm quotation from a Cash Settlement Reference Bank.

#### Replacement Value (Calculation Agent Determination) (Section 18.2.5)

This is equivalent to the Replacement Value (Firm Quotations) method, but instead of using the Cash Settlement Reference Bank quotation process described above, the Calculation Agent makes the determination using the Close-out Amount methodology specified in the standard-form 2002 ISDA

Master Agreement, irrespective of the actual master agreement that governs the transaction, on the assumption that the transaction is collateralized under an ISDA standard CSA for variation margin, unless the relevant transaction is uncollateralized.

| Confirmation Field                     | Possible Elections in the<br>Confirmations                                                                                                                                                                                                                                            | Default Where No Election is<br>Specified in the Confirmation                                                                                                                                                     |  |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--|
|                                        | Replacement Value (Firm Quotations)                                                                                                                                                                                                                                                   |                                                                                                                                                                                                                   |  |
| [Protected Party:]                     | [Specify]/[Both parties]                                                                                                                                                                                                                                                              | The default for OET is for the non-exercising<br>party to be the Protected Party. The default<br>for any other use, including MET, is for both<br>parties to be a Protected Party                                 |  |
| [Cash Collateral Currency:]            | [Specify currency – only needs to be specified<br>if there is no governing master agreement<br>and there are two cash settlement currencies.<br>If there is a governing master agreement,<br>irrespective of whether there is a governing<br>CSA, this does not need to be specified] | As specified in the Settlement Matrix for the<br>currency pair, or if those currencies are not<br>a currency pair under the Settlement Matrix,<br>the Termination Currency under the Relevant<br>Master Agreement |  |
| [Prescribed Documentation Adjustment:] | [Applicable]/[Not applicable]                                                                                                                                                                                                                                                         | Does not apply                                                                                                                                                                                                    |  |
|                                        | Replacement Value (Calculation Agent Determination)                                                                                                                                                                                                                                   |                                                                                                                                                                                                                   |  |
| [Protected Party:]                     | [Specify]/[Both parties]                                                                                                                                                                                                                                                              | The default for OET is for the non-exercising<br>party to be the Protected Party. The default<br>for any other use, including MET, is for both<br>parties to be a Protected Party                                 |  |
| [Cash Collateral Currency:]            | [Specify currency – only needs to be<br>specified if the relevant transaction is<br>collateralized and there are two cash<br>settlement currencies]                                                                                                                                   | As specified in the Settlement Matrix for the<br>currency pair, or if those currencies are not<br>a currency pair under the Settlement Matrix,<br>the Termination Currency under the Relevant<br>Master Agreement |  |

Table 6: Confirmation Elections and Defaults Associated with the Replacement Value Cash Settlement Methods

Neither Replacement Value (Firm Quotations) nor Replacement Value (Calculation Agent Determination) is used as the default Cash Settlement Method for any currency currently set out under the ISDA Settlement Matrix.

### Cash Settlement Reference Banks

Under the 2021 Definitions, several changes have been made to the way in which Cash Settlement Reference Banks are selected and how quotations are requested from them. These changes are designed to standardize the process, improve transparency and increase the chances of a successful quotation process.

As set out in *Section 18.1.6*:

- A standardized quotation request template (a Template Quotation Request Form, published as an annex to the main book of the 2021 Definitions) must be used to request the quotations if any of the Mid-market Valuation or Replacement Value Cash Settlement Methods apply. A copy must be shared with the other party upon request.
- Quotations should be submitted at or as soon as reasonably practicable after the Cash Settlement Valuation Time on the Cash Settlement Valuation Date.
- Quotations should be as of the Cash Settlement Valuation Time on the Cash Settlement Valuation Date (or, if that is not reasonably practicable with respect to Replacement Value (Firm Quotations), at a time agreed between the parties or determined by the Calculation Agent).

Except in relation to Mid-market Valuation (Indicative Quotations) – Alternate Method, the Cash Settlement Reference Banks are the institutions specified in the confirmation, or else five institutions mutually agreed by the parties (*Section 18.1.5*).

If not specified in the Confirmation and the parties fail to agree on five institutions, the Calculation Agent would select the Cash Settlement Reference Banks. However, the non-Calculation Agent has the right to select and/or seek quotations from up to two of the five Cash Settlement Reference Banks if the Calculation Agent is a party to the transaction6 .

In this circumstance, the Calculation Agent would select and seek quotations from the remainder (so the total number is five). Under the 2006 Definitions, in contrast, the Calculation Agent would select all of the Cash Settlement Reference Banks and request quotations from all of those institutions if the parties fail to mutually agree7 .

In order to ensure the selection and quotation procurement process remains robust, the party requesting quotations must act in good faith and comply with the other requirements of *Section 18.1.6*, including those set out above.

### Cash Settlement Methods Retained From the 2006 Definitions

Two Cash Settlement Methods included in the 2006 Definitions (either via the original booklet or a Supplement) have been carried over to the 2021 Definitions. These are:

#### Collateralized Cash Price

The Collateralized Cash Price8 method has been retained in the 2021 Definitions with minimal drafting changes that are clarificatory in purpose and are not intended to change the substance of this method from that which appears in the supplemented 2006 Definitions. This Cash Settlement Method was recently updated in the 2006 Definitions via Supplement 64 to allow parties to lock-in an Agreed Discount Rate in the Confirmation upfront.

Under the 2021 Definitions, the ISDA Collateral Cash Price Matrix – which sets out the default Settlement Rate and Discount Rate per currency – has been merged into the ISDA Settlement Matrix.

Collateralized Cash Price is the default Cash Settlement Method for many single currency Swaptions set out under the ISDA Settlement Matrix.

#### Par Yield Curve – Unadjusted

Market participants have generally been moving away from use of Par Yield Curve – Unadjusted in favor of the Collateralized Cash Price method. The fundamental difference between the two is that discounting is at the Settlement Rate for Par Yield Curve – Unadjusted versus OIS discounting for Collateralized Cash Price.

The market standard Cash Settlement Method for the settlement of euro swaptions switched from Par Yield Curve – Unadjusted to Collateralized Cash Price in November 2018. However, Par Yield Curve – Unadjusted is still currently the standard for sterling LIBOR Swaptions (although such swaptions are not expected to trade frequently, if at all, given the upcoming cessation date for sterling LIBOR at the end of 2021).

<sup>6</sup>This is subject to providing notice to the Calculation Agent in the form of the template Selection Notice (published as an annex to the main book of the 2021 Definitions) no later than 30 minutes following the Cash Settlement Valuation Time

<sup>7</sup>Except in relation to Cash Price – Alternate Method

<sup>8</sup> Extensive guidance notes on the Collateralized Cash Price method are available on the ISDA website, [http://assets.isda.org/media/8617642d/9cc38d66.pptx\)](http://assets.isda.org/media/8617642d/9cc38d66.pptx)

For now, therefore, Par Yield Curve – Unadjusted has been retained in the 2021 Definitions without substantive amendment primarily for the purpose of sterling LIBOR swaptions.

### Cash Settlement Methods Not Retained From the 2006 Definitions

Cash Price, Cash Price – Alternate Method, Cross Currency Method, Par Yield Curve – Adjusted and Zero Coupon Yield – Adjusted are Cash Settlement Methods in the 2006 Definitions that have not been carried over to the 2021 Definitions.

These methods have not been carried over as they are either:

- Superseded (the Cash Price and Cash Price Alternate Method methods have been replaced by the Mid-market Valuation and Replacement Value methods); or
- Redundant (the Cross Currency Method is no longer needed as the Mid-market Valuation and Replacement Value methods both cater for single and cross-currency swaps, and the Par Yield Curve – Adjusted and Zero Coupon Yield – Adjusted methods are no longer used as market standards).

#### ISDA Settlement Matrix

Under the 2006 Definitions, the ISDA Settlement Matrix specifies the default settings for Cash Settlement that apply if no elections have been made in the Confirmation.

Under the 2021 Definitions, this matrix (the 2021 ISDA Interest Rate Derivatives Definitions Settlement Matrix for Settlement, Early Termination and Swaptions) has been expanded and updated to reflect current market practices for swaptions, OET and MET.

Changes and additions to the ISDA Settlement Matrix under the 2021 Definitions include:

- The ISDA Collateral Cash Price Matrix from the 2006 Definitions has been merged into the ISDA Settlement Matrix;
- The ISDA Cross Currency Settlement Matrix has been merged into the ISDA Settlement Matrix such that the defaults for cross-currency swaps have been included;
- For swaptions, the default Settlement Method has been included in addition to the default Cash Settlement Method (ie, whether Physical Settlement, Cleared Physical Settlement or Cash Settlement is the default); and
- For certain currencies where swaptions are traded on an underlying swap and the floating leg is a risk-free rate, defaults for those swaptions have been included in addition to the existing floating rate swaptions.

# CURRENCY PROVISIONS

The definitions of currencies, related financial centers and other provisions specific to a currency have been set out in a matrix under the 2021 Definitions (the Currency/Business Day Matrix) rather than being included in a long list as per Section 1.6 and Section 1.7 of the 2006 Definitions. This will help facilitate electronic processing and make them easier to update. Additional currencies have been included in this matrix to align with Annex A of the 1998 FX and Currency Option Definitions.

ISDA working groups discussed whether the additional provisions and standard terms supplements published alongside the 2006 Definitions that deal with deliverable currency disruptions and provide terms for non-deliverable swaps should be standardized, expanded to other currencies and incorporated into the main book of the 2021 Definitions.

Following discussions with other FX trade associations, it was agreed that updating the FX and currency provisions in the ISDA Definitions should be a separate joint project with ISDA interest rate working groups and experts from FX markets, including relevant FX trade associations and ISDA FX working groups. This project will be initiated at some point after implementation of the 2021 Definitions.

These additional provisions and standard terms supplements will, however, be produced for the 2021 Definitions (with minimal changes) to allow them to be used under the 2021 Definitions framework.

The Mark-to-market provisions (*Section 11*) have not been materially altered in the initial version of the 2021 Definitions, except for including more robust fallbacks for the Currency Exchange Rate used to determine the MTM Amount.

## DIGITAL PUBLICATION

The 2021 Definitions have been published in a purely digital format via ISDA's new interactive webbased user interface, MyLibrary. This new platform includes comparison tools that allow different versions of the 2021 Definitions to be viewed in blackline9 , hyperlinking of defined terms to their definitions, links to other relevant documents and multimedia content, bookmarking and advanced search facilities.

This digital format removes the need for supplements each time amendments or additional provisions to the 2021 Definitions are required. As the 2021 Definitions have not been published as a physical book, a new digital version can be published for each amendment and addition. Matrices, such as the Floating Rate Matrix and ISDA Settlement Matrix, will be versioned separately to the main book, making it easier to distinguish between amendments – ie, those relating to benchmarks versus those relating to the operative provisions.

Users will be able to navigate directly to the version of the main book or relevant matrix that applied as of the date of their relevant trade, making the 2021 Definitions much easier to use than the 2006 Definitions. Under the 2006 Definitions, users were required to read the original published book and each PDF supplement published up to the date of their trade in order to understand all the terms that apply.

| My Library                                                                                                                   | $\times$ +                                                                                                                     |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | $\circ$       |  |  |  |  |
|------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|--|--|--|--|
| $\leftarrow$ $\rightarrow$ C                                                                                                 | $\hat{ }$                                                                                                                      | Bi mylibrary.isda.org/document/95892da0-caec-11eb-9d07-0242e05cf0ae/content/89a66c50-caec-11eb-9d07-0242e05cf0ae/definition/8c259d20-caec-11eb-9d07-0242e05cf0ae/definition/8c259d20-caec-11eb-9d07-0242e05cf0ae/element/p3ad945d4076e479db677b27f4773969f                                                                                                                                                                                                                                                                       | ÷.<br>■ 寿     |  |  |  |  |
|                                                                                                                              | Apps [b Suggested Sites   Imported From IE   Imported From IE (1)   Imported From IE (2)   Imported From IE (3)     My Library |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | <b>B</b> Read |  |  |  |  |
| $2^{\circledbullet}_{\text{zoom}}$<br>$ISDA \cong$<br>Library<br>2021 ISDA Interest Rate Derivatives Definitions<br>v search |                                                                                                                                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |               |  |  |  |  |
| ≡                                                                                                                            | $\circ$<br>Menu                                                                                                                | International Swaps and Derivatives Association, Inc.                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | ○ 四 日 间 13    |  |  |  |  |
| 间                                                                                                                            | Cover                                                                                                                          | <b>2021 ISDA INTEREST RATE DERIVATIVES DEFINITIONS</b>                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |               |  |  |  |  |
| 图                                                                                                                            | 2021 ISDA INTEREST RATE DERIVATIVES<br><b>DEFINITIONS</b>                                                                      | Any or all of the following provisions may be incorporated into a document (including in electronic form) by wording in the document indicating that, or the extent to which, the document (or one or more parts of it) is subject to the 2021 ISDA Interest R<br>International Swaps and Derivatives Association. Inc. All provisions so incorporated into a document will apply to that document unless otherwise provided in that document and all terms defined in the 2021 Definitions and used in any provision that is in |               |  |  |  |  |
|                                                                                                                              | SECTION 1 GENERAL DEFINITIONS AND<br><b>PROVISIONS</b>                                                                         | meanings set out in the 2021 Definitions unless otherwise provided in that document.                                                                                                                                                                                                                                                                                                                                                                                                                                             |               |  |  |  |  |
|                                                                                                                              | SECTION 2 BUSINESS DAYS AND<br><b>CURRENCIES</b>                                                                               | <b>SECTION 1</b>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |               |  |  |  |  |
|                                                                                                                              | SECTION 3 DATES AND PERIODS<br>$\sim$                                                                                          | <b>GENERAL DEFINITIONS AND PROVISIONS</b><br>1.1 Architecture.                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |               |  |  |  |  |
|                                                                                                                              | <b>SECTION 4 CERTAIN PAYMENTS.</b><br>$\checkmark$<br>CALCULATIONS AND CORRECTIONS                                             | 1.1.1 2021 Definitions.<br>$\theta \equiv 1$<br>2.1.3 Currency/Business Day Matrix.                                                                                                                                                                                                                                                                                                                                                                                                                                              |               |  |  |  |  |
|                                                                                                                              | SECTION 5 FIXED AMOUNTS                                                                                                        | "2021 Definitions" means the 20<br>[ISDA on its website (http://www.isda.org).<br>1.1.2 Matrix.                                                                                                                                                                                                                                                                                                                                                                                                                                  |               |  |  |  |  |
|                                                                                                                              | SECTION 6 FLOATING AMOUNTS<br>SECTION 7 OVERNIGHT RATE                                                                         | "Currency/Business Day Matrix" means the "2021 ISDA Interest Rate Derivatives Definitions<br>Currency/Business Day Matrix"<br>"Matrix" means each of the follow                                                                                                                                                                                                                                                                                                                                                                  |               |  |  |  |  |
|                                                                                                                              | COMPOUNDING AND AVERAGING AND<br>$\sim$<br><b>INDICES</b>                                                                      | (i) the Currency/Business Day<br>(ii) the Settlement Matrix:                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |               |  |  |  |  |
|                                                                                                                              | <b>SECTION 8 FALLBACKS</b>                                                                                                     | (iii) the Floating Rate Matrix;<br>(iv) the Mark-to-Market Matrix                                                                                                                                                                                                                                                                                                                                                                                                                                                                |               |  |  |  |  |
|                                                                                                                              | 8.1 Temporary Non-Publication.                                                                                                 | (v) the Compounding/Averagin                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |               |  |  |  |  |
|                                                                                                                              | 8.2 Provisions Relating to Permanent<br>Cessation.                                                                             | These are collectively referred to<br>1.1.3 Application of the 2021 Definition                                                                                                                                                                                                                                                                                                                                                                                                                                                   |               |  |  |  |  |
|                                                                                                                              | 8.3 Provisions Relating to<br>Administrator/Benchmark Event.                                                                   | A Go to definition Q. Find in document<br>to apply to only part of the Transaction, to that part of the Transaction, and will not be affected by any further updates in a later<br>Unless otherwise agreed, the latest variance.<br>dated version of the 2021 Definitions. In order to exclude any amendments made to the 2021 Definitions pursuant to a subsequent version (whether to the 2021 Definitions generally or to one or more Matrices), the parties must, in the Confirmation, specify                               |               |  |  |  |  |
|                                                                                                                              | 8.4 Hierarchy of Events.                                                                                                       | Definitions they wish to apply.<br>1.1.4 Application of Matrices.                                                                                                                                                                                                                                                                                                                                                                                                                                                                |               |  |  |  |  |
|                                                                                                                              | 8.5 Certain Definitions Relating to<br>Fallbacks.                                                                              | (i) If one or more of the Matrices apply, the relevant elections in the latest version of that Matrix on the Trade Date of the Transaction will be deemed to have been specified in the Confirmation.                                                                                                                                                                                                                                                                                                                            |               |  |  |  |  |
|                                                                                                                              | 8.6 Generic Fallback Provisions.                                                                                               | (ii) In the event of any inconsistency between the election in a Matrix and the election in the Confirmation, the election in the Confirmation shall prevail.<br>1.1.5 Application of the 2021 Definitions to Parties.                                                                                                                                                                                                                                                                                                           |               |  |  |  |  |
|                                                                                                                              | 8.7 Certain Definitions Relating to Generic                                                                                    |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |               |  |  |  |  |

The MyLibrary user interface will be accessible on mobile devices as well as desktop computers.

## PUBLICATION AND IMPLEMENTATION

Although the 2021 Definitions were published in June 2021, they will not be implemented as the market standard definitional book for interest rate derivatives until October 4, 2021.

Market participants and infrastructures require a period of time between the publication of the first version of the 2021 Definitions (and related matrices) and the implementation date in order to digest the changes and make the necessary updates to systems and processes.

The ISDA working groups considered it important for market participants and infrastructures to target a particular date for adopting the 2021 Definitions to help ensure an efficient implementation and to minimize disruption. As it is likely that not all market participants will be in a position to adopt the 2021 Definitions by that date, the 2006 Definitions may continue to be used for some time (as was the case with the 2000 Definitions after publication of the 2006 Definitions).

However, it is important to note that ISDA does not intend to maintain or update the 2006 Definitions after October 1, 2021. Changes and additions that are not already in the pipeline by that date will only be reflected in the 2021 Definitions. Given the pace of change in this market, it is anticipated the 2006 Definitions will become outdated reasonably quickly.

Most major clearing houses plan to transition cleared trades to the 2021 Definitions on the implementation date (eg, via changes to their rule books). Therefore, market participants are encouraged to transition to the 2021 Definitions where alignment between cleared and non-cleared trades is important.

ISDA has established a working group to map out the operational details of the implementation (the ISDA 2021 Definitions Implementation sub-group). This working group includes clearing houses, trading venues, middleware providers and other market participants. Please contact [jmartin@isda.org](mailto:jmartin@isda.org) to join the group.

### Further information

Further information on the 2021 Definitions is available on the ISDA website: [https://www.isda.](https://www.isda.org/2021/05/01/2021-isda-interest-rate-derivatives-definitions/) [org/2021/05/01/2021-isda-interest-rate-derivatives-definitions/](https://www.isda.org/2021/05/01/2021-isda-interest-rate-derivatives-definitions/).

## ABOUT ISDA

Since 1985, ISDA has worked to make the global derivatives markets safer and more efficient. Today, ISDA has over 950 member institutions from 76 countries. These members comprise a broad range of derivatives market participants, including corporations, investment managers, government and supranational entities, insurance companies, energy and commodities firms, and international and regional banks. In

addition to market participants, members also include key components of the derivatives market infrastructure, such as exchanges, intermediaries, clearing houses and repositories, as well as law firms, accounting firms and other service providers. Information about ISDA and its activities is available on the Association's website: [www.isda.org.](http://www.isda.org) Follow us on [Twitter,](https://twitter.com/isda) [LinkedIn](https://www.linkedin.com/company/isda), [Facebook](https://www.facebook.com/ISDA.org) and [YouTube.](https://www.youtube.com/channel/UCg5freZEYaKSWfdtH-0gsxg)