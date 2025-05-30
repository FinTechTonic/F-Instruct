package com.regnosys.drr

import com.regnosys.drr.TestPackCreatorModel.ReportSampleConfigItem
import com.regnosys.drr.TestPackCreatorModel.TestPackNameAndInputType
import com.regnosys.drr.transform.*
import com.regnosys.rosetta.common.util.ClassPathUtils
import java.nio.file.Path
import java.util.*

object TestPackCreatorConfig {

    private val drrEvents: TestPackNameAndInputType =
        TestPackNameAndInputType("Events", TestPackCreatorModel.transactionReportInstruction)
    private val drrProductCommodity: TestPackNameAndInputType =
        TestPackNameAndInputType("Commodity", TestPackCreatorModel.transactionReportInstruction)
    private val drrProductCredit: TestPackNameAndInputType =
        TestPackNameAndInputType("Credit", TestPackCreatorModel.transactionReportInstruction)
    private val drrProductCustomScenarios: TestPackNameAndInputType =
        TestPackNameAndInputType("Custom Scenarios", TestPackCreatorModel.transactionReportInstruction)
    private val drrProductEtd: TestPackNameAndInputType =
        TestPackNameAndInputType("ETD", TestPackCreatorModel.transactionReportInstruction)
    private val drrProductEquity: TestPackNameAndInputType =
        TestPackNameAndInputType("Equity", TestPackCreatorModel.transactionReportInstruction)
    private val drrProductFX: TestPackNameAndInputType =
        TestPackNameAndInputType("FX", TestPackCreatorModel.transactionReportInstruction)
    private val drrProductRates: TestPackNameAndInputType =
        TestPackNameAndInputType("Rates", TestPackCreatorModel.transactionReportInstruction)
    private val cftcEventScenarios: TestPackNameAndInputType =
        TestPackNameAndInputType("CFTC Event Scenarios", TestPackCreatorModel.transactionReportInstruction)
    private val drrDelegatedReporting: TestPackNameAndInputType =
        TestPackNameAndInputType("Delegated Reporting", TestPackCreatorModel.transactionReportInstruction)

    private val drrCollateral: TestPackNameAndInputType =
        TestPackNameAndInputType("Collateral", TestPackCreatorModel.reportableCollateral)

    private val reportSampleConfigs: Set<ReportSampleConfigItem> = setOf(
        ReportSampleConfigItem(
            drrEvents,
            "result-json-files/fpml-5-10/record-keeping/events/",
            excludeFileNames = setOf(
                "Partial-Novation.json",
                "Withdrawal-Error-ex01.json",
                "Withdrawal-Error-ex02.json",
                "Withdrawal-Termination.json"
            ),
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrEvents,
            "result-json-files/fpml-5-10/record-keeping/events/Partial-Novation.json",
            overrideTargetFileName = "partial-novation-new-trade.json",
            transformFunction = reportableEventToTransactionReportInstructionWithMultipleAfterTrades(0)
        ),
        ReportSampleConfigItem(
            drrEvents,
            "result-json-files/fpml-5-10/record-keeping/events/Partial-Novation.json",
            overrideTargetFileName = "partial-novation-old-trade.json",
            transformFunction = reportableEventToTransactionReportInstructionWithMultipleAfterTrades(1)
        ),
        ReportSampleConfigItem(
            drrEvents,
            "result-json-files/fpml-5-10/record-keeping/events/Withdrawal-Error-ex01.json", // Withdrawal event does not have counterparties specified
            transformFunction = reportableEventToTransactionReportInstructionNoCounterparties
        ),
        ReportSampleConfigItem(
            drrEvents,
            "result-json-files/fpml-5-10/record-keeping/events/Withdrawal-Error-ex02.json", // Withdrawal event does not have counterparties specified
            transformFunction = reportableEventToTransactionReportInstructionNoCounterparties
        ),
        ReportSampleConfigItem(
            drrEvents,
            "result-json-files/fpml-5-10/record-keeping/events/Withdrawal-Termination.json", // Withdrawal event does not have counterparties specified
            transformFunction = reportableEventToTransactionReportInstructionNoCounterparties
        ),
        ReportSampleConfigItem(
            drrProductCommodity,
            "result-json-files/fpml-5-10/record-keeping/products/commodity/",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrProductCredit,
            "result-json-files/fpml-5-10/record-keeping/products/credit/",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-10/record-keeping/products/custom-scenarios/",
            excludeFileNames = setOf("mockup-agent-trading-capacity.json", "mockup-principal-trading-capacity.json"),
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrEvents,
            "result-json-files/fpml-5-13/record-keeping/events/",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-13/record-keeping/products/custom-scenarios/",
            excludeFileNames = setOf("mockup-commodity-delivery.json", "mockup-strikePrice-ex01-strikePriceSchedule.json"),
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-10/record-keeping/products/custom-scenarios/mockup-agent-trading-capacity.json",
            transformFunction = reportableEventToTransactionReportInstructionMASAgent
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-10/record-keeping/products/custom-scenarios/mockup-principal-trading-capacity.json",
            transformFunction = reportableEventToTransactionReportInstructionMASPrincipal
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-13/record-keeping/products/custom-scenarios/mockup-commodity-delivery.json",
            transformFunction = transformCommodityDeliverySwap
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-13/record-keeping/products/custom-scenarios/mockup-strikePrice-ex01-strikePriceSchedule.json",
            transformFunction = reportableEventToTransactionReportInstructionNoCounterparties
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-13/record-keeping/products/custom-scenarios/mockup-post-priced.json",
            "mockup-external-api-enrich.json",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-13/record-keeping/products/custom-scenarios/mockup-post-priced.json",
            "mockup-pre-enrich.json",
            transformFunction = reportableEventToPreEnrichedTransactionReportInstruction
        ),
        ReportSampleConfigItem(
            drrProductCustomScenarios,
            "result-json-files/fpml-5-13/record-keeping/products/custom-scenarios/mockup-post-priced-apac.json",
            "mockup-pre-enrich-ssbs.json",
            transformFunction = reportableEventToPreEnrichedTransactionReportInstruction
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-etd-10-year-T-note-future.json",
            transformFunction = transformEtd10YearNoteFuture
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-etd-10-year-T-note-option.json",
            transformFunction = transformEtd10YearNoteOption
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-etd-cocoa-future.json",
            transformFunction = transformEtdCocoaFuture
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-etd-cocoa-option.json",
            transformFunction = transformEtdCocoaOption
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-etd-future-security-product.json",
            transformFunction = transformEtdFutureSecurityProduct
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-etd-option-security-product.json",
            transformFunction = transformEtdOptionSecurityProduct
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-estr-option-cme.json",
            transformFunction = transformEstrOptionCme
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-estr-features-cme.json",
            transformFunction = transformEstrFeaturesCme
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-gold-option-etd.json",
            transformFunction = transformGoldOptionEtd
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-gold-feature-etd.json",
            transformFunction = transformGoldFutureEtd
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-socgen-equity-listed-option.json",
            transformFunction = transformSocGenEquityListedOption
        ),
        ReportSampleConfigItem(
            drrProductEtd,
            "result-json-files/fpml-5-13/record-keeping/products/etd/mockup-etd.json",
            overrideTargetFileName = "mockup-socgen-equity-listed-future.json",
            transformFunction = transformSocGenEquityListedFuture
        ),
        ReportSampleConfigItem(
            drrProductEquity,
            "result-json-files/fpml-5-10/record-keeping/products/equity/",
            excludeFileNames = setOf("Equity-Portfolio-Swap-Price-Return-Basket-ex01.json"),
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrProductEquity,
            "result-json-files/fpml-5-10/record-keeping/products/equity/Equity-Portfolio-Swap-Price-Return-Basket-ex01.json",
            transformFunction = reportableEventToTransactionReportInstructionNoCounterparties
        ),
        ReportSampleConfigItem(
            drrProductFX,
            "result-json-files/fpml-5-10/record-keeping/products/fx/",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrProductRates,
            "result-json-files/fpml-5-10/record-keeping/products/rates/",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            cftcEventScenarios,
            "result-json-files/fpml-5-10/record-keeping/events/cftc-event-scenarios/",
            excludeFileNames = setOf("Example-02-Submission-2.json"),
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            cftcEventScenarios,
            "result-json-files/fpml-5-10/record-keeping/events/cftc-event-scenarios/Example-02-Submission-2.json",
            transformFunction = reportableEventToTransactionReportInstructionNoCounterparties // Cancel event does not have counterparties specified
        ),
        ReportSampleConfigItem(
            drrCollateral,
            "regulatory-reporting/input/collateral/Collateral-ex01.json",
            transformFunction = toCollateralReportInstruction
        ),
        ReportSampleConfigItem(
            drrDelegatedReporting,
            "result-json-files/fpml-5-10/record-keeping/products/delegated-reporting/dr-ex01.json",
            overrideTargetFileName = "dr-ex01-cftc-partyA-self.json",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrDelegatedReporting,
            "result-json-files/fpml-5-10/record-keeping/products/delegated-reporting/dr-ex01.json",
            overrideTargetFileName = "dr-ex01-emir-partyA-self.json",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrDelegatedReporting,
            "result-json-files/fpml-5-10/record-keeping/products/delegated-reporting/dr-ex01.json",
            overrideTargetFileName = "dr-ex01-emir-partyB-self.json",
            transformFunction = reportableEventToTransactionReportInstructionSelfOther
        ),
        ReportSampleConfigItem(
            drrDelegatedReporting,
            "result-json-files/fpml-5-10/record-keeping/products/delegated-reporting/dr-ex02.json",
            overrideTargetFileName = "dr-ex02-emir-partyA-self.json",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrDelegatedReporting,
            "result-json-files/fpml-5-10/record-keeping/products/delegated-reporting/dr-ex02.json",
            overrideTargetFileName = "dr-ex02-emir-partyA-mandatory-delegated.json",
            transformFunction = reportableEventToTransactionReportInstructionMandatoryDelegated
        ),
        ReportSampleConfigItem(
            drrDelegatedReporting,
            "result-json-files/fpml-5-10/record-keeping/products/delegated-reporting/dr-ex03.json",
            overrideTargetFileName = "dr-ex03-emir-partyA-self.json",
            transformFunction = reportableEventToTransactionReportInstructionSelf
        ),
        ReportSampleConfigItem(
            drrDelegatedReporting,
            "result-json-files/fpml-5-10/record-keeping/products/delegated-reporting/dr-ex03.json",
            overrideTargetFileName = "dr-ex03-emir-partyA-voluntary-delegated.json",
            transformFunction = reportableEventToTransactionReportInstructionVoluntaryDelegated
        )
    )

    fun getReportSampleConfigs(): Set<ReportSampleConfigItem> {
        return reportSampleConfigs
            .filter { reportSampleConfigItem -> !isSampleDirectory(reportSampleConfigItem) }
            .toSet() +
                getReportSampleConfigsForDirectories()

    }

    private fun getReportSampleConfigsForDirectories(): Set<ReportSampleConfigItem> {
        return reportSampleConfigs
            .filter { item -> isSampleDirectory(item) }
            .flatMap { item ->
                val directory = item.sampleFileLocation
                ClassPathUtils.findPathsFromClassPath(
                    listOf(directory),
                    ".*\\.json$",
                    Optional.empty(),
                    javaClass.classLoader
                )
                    .filter { p -> removeNestedFiles(directory, p) }
                    .filter { p -> !excludeFile(item.excludeFileNames, p) }
                    .map { p ->
                        ReportSampleConfigItem(
                            item.testPack,
                            relativize(p.toString()),
                            item.overrideTargetFileName,
                            item.excludeFileNames,
                            item.transformFunction
                        )
                    }
            }.toSet()

    }

    private fun excludeFile(excludeFileNames: Set<String>, p: Path): Boolean {
        return excludeFileNames.contains(p.fileName.toString())
    }

    private fun removeNestedFiles(directory: String, p: Path): Boolean {
        val regex = Regex(".*$directory[^/]+?\\.json$")
        return p.toString().matches(regex)
    }

    private fun relativize(path: String): String {
        return path.replace(Regex("^/"), "") //this replace will relativize paths running in java command line
            .replace(Regex("^.*/target/classes/"), "") //this replace will relativize paths running in IntelliJ
    }

    fun getAllReportTestPacks(): Set<TestPackNameAndInputType> {
        return getReportSampleConfigs()
            .map { reportConfigItem -> reportConfigItem.testPack }
            .toMutableSet()
    }

    fun lookupReportConfigItemsGivenTestPackName(testPackName: String): Set<ReportSampleConfigItem> {
        return getReportSampleConfigs()
            .filter { reportConfigItem -> reportConfigItem.testPack.name == testPackName }
            .toSet()
    }

    private fun isSampleDirectory(reportSampleConfigItem: ReportSampleConfigItem): Boolean {
        return reportSampleConfigItem.sampleFileLocation.endsWith("/")
    }
}