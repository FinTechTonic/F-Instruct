
<!--====================================================================================================-->

<!-- This is a template for DRR release notes, not intended to be a full account but rather a practical -->
<!-- guide to release notes. Fore the full official style guide please check:-->
<!-- https://cdm.docs.rosetta-technology.io/source/contribution.html#content-of-release-notes -->

<!-- You can also check more examples of release notes in the "Release Notes" tab in Rosetta, -->
<!-- which is located on the "?" button in the upper right corner. -->

<!-- Please note that examples for the sections have been extracted from different release notes so -->
<!-- there is not necessarily any correspondence between them. -->

<!--====================================================================================================-->

# _DRR Documentation - Release Notes Template_

<!-- Release notes should begin with a high-level headline of the part of the model being changed, -->
<!-- followed by “–” and a short headline description -->

<!-- Example: -->

# _DRR EMIR Refit - Clearing Threshold_

<!------------------------------------------------------------------------------------------------->

_Background_

<!--Here we explain the context of the change-->

Text. **Bold text** should generally be avoided in release notes.

<!-- Example: -->

Under EMIR Refit, an explicit specification indicating whether the legal threshold for mandatory clearing for a specific entity has been exceeded or not needs to be reported for some cases. This information is captured in EMIR fields 1.7 - Clearing Threshold of Counterparty 1 (for the reporting party) and EMIR 1.13 - Clearing Threshold of Counterparty 2 (for the counterparty).

<!------------------------------------------------------------------------------------------------->

_What is being released?_

<!--Here we compile, generally as bullet points, the broad changes in the reporting rules introduced in the release.-->

This is a list:

1. Reporting Rule A
    - Change 1 with `monospace expressions for functions`
    - Change 2

2. Reporting Rule B
    - Change 3
    - Change 4

<!-- Example: -->

1. EMIR 2.1 - `UTI`:
    -    Replaced the filter based on the deprecated FpML's coding scheme `unique-transaction-identifier` by the CDM `identifierType`.
    -    Removed the filter to the last UTI version.

2. EMIR 1.2 - `Report Submitting Entity ID`:
    - Changed the `SupervisoryBodyEnum` value to `ESMA`.
    
    
<!------------------------------------------------------------------------------------------------->

_Validation rules_

<!-- If validation rules have been added, removed or altered, we list the changes here as bullet points. -->
<!-- If there are multiple validation rules related to a field, they can be listed below it -->

<!-- Example: -->

Added the validation rules specified for the fields:

-   EMIR 2.10 - Contract type: `EMIR-VR-2010-01`
-   EMIR 2.143 - Seniority: `EMIR-VR-2143-01`
-   EMIR 2.151 - Action type: `EMIR-VR-2151-01`
-   EMIR 2.152 - Event type:
    -   `EMIR-VR-2152-01` 
    -   `EMIR-VR-2152-02` 
    -   `EMIR-VR-2152-05`

<!------------------------------------------------------------------------------------------------->

_Functions_

<!-- If (non qualification) functions have been added, removed or altered, we list the changes here as bullet points. -->

<!-- Example: -->

New common functions in the `regulation-common-func.rosetta` file:

-   `IsInterestRateAssetClass`
-   `IsCreditAssetClass`
-   `IsFXAssetClass`
-   `IsEquityAssetClass`
-   `IsCommodityAssetClass`
    
<!------------------------------------------------------------------------------------------------->

_Data types_

<!-- If data types have been added, removed or altered, we list the changes here as bullet points. -->
<!-- If there are a lot of changes, they can be divided by folder, but this should be avoided in regular sized contributions -->

<!-- Example: -->

- Added new `ObservationInstruction` type.
- Added new `ObservationEvent` type.
- Added new `CreditEvent` type.
- Added new `CorporateAction` type.
- `observationHistory` attribute of type `ObservableEvent` added to `TradeState`.
- `observation` attribute of type `ObservationInstruction` added to `PrimitiveInstruction`.
- Type of `excludedReferenceEntity` in `IndexReference information` changed to `ReferenceInformation`.

<!------------------------------------------------------------------------------------------------->

_Enumerations_

<!-- If enumerations have been added, removed or altered, we list the changes here as bullet points. -->

<!-- Example: -->

- Added new `CreditEventTypeEnum` enumeration.
- Updated `EventIntentEnum` to support credit events.
- Updated `CorporateActionTypeEnum` with more values and documentation.
- Updated `FeeTypeEnum` enumeration to support credit events and corporate actions.

<!------------------------------------------------------------------------------------------------->

_Translate_

<!-- If synonyms have been added, removed or altered, we list the changes here as bullet points. -->
<!-- Unlike the other categories, since mappings generally involve many synonyms being changed, we should not list them all but instead we can just write "Mapping support added for X" or equivalent expression-->

<!-- Example: -->

Added mapping coverage for Clearing Exceptions and Exemptions.

<!------------------------------------------------------------------------------------------------->

_Review directions_

<!-- Here we specify how an user can check the changes introduced in the release, generally as steps to follow -->
<!-- If samples need to be used to check reporting rules, then, the report, test pack and name of the samples should always be included -->

<!-- Example 1: -->

In Rosetta, select the Textual View and inspect each of the changes identified above.
In Rosetta, open the reports tab, select the report `ESMA / EMIR Refit` and the dataset `Custom Scenarios` and review the expectations for the field 1.7 - `Clearing Threshold of Counterparty 1` and 1.13 - `Clearing Threshold of Counterparty 2` in the samples mockup-clearing-threshold-party-1 and mockup-clearing-threshold-party-2.

<!-- If samples need to be used to check mappings, the path and name of the samples should always be included -->

<!-- Example 2: -->

In Rosetta, select the Textual View and inspect each of the changes identified above

In the CDM DRR Portal, select Ingestion and review the following samples:

fpml-5-10/record-keeping/products/custom-scenarios

- mockup-clearing-threshold-party-1
- mockup-clearing-threshold-party-2


<!--====================================================================================================-->

