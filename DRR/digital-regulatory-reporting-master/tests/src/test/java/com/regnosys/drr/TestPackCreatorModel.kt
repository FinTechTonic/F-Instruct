package com.regnosys.drr

import com.rosetta.model.lib.RosettaModelObject
import drr.regulation.common.CollateralReportInstruction
import drr.regulation.common.TransactionReportInstruction

object TestPackCreatorModel {
    class ReportSampleConfigItem(
        val testPack: TestPackNameAndInputType,
        val sampleFileLocation: String,
        val overrideTargetFileName: String = "",
        val excludeFileNames: Set<String> = emptySet(),
        val transformFunction: Function1<String, RosettaModelObject>? = null
    ) {
        fun targetLocation(): String {
            return removeFilePrefix(rewriteLocation())
        }

        private fun rewriteLocation(): String {
            return if (overrideTargetFileName.isBlank()) sampleFileLocation
            else sampleFileLocation.replace(Regex("(.*/).+?\\.json"), "$1$overrideTargetFileName")
        }

        private fun removeFilePrefix(file: String): String {
            return file.replace(Regex("(?:result-json-files/|cdm-sample-files/)?(.*\\.(json|xml)$)"), "$1")
        }
    }

    class TestPackNameAndInputType(
        val name: String,
        val inputType: String
    )

    val transactionReportInstruction: String = TransactionReportInstruction::class.java.canonicalName

    val reportableCollateral: String = CollateralReportInstruction::class.java.canonicalName
}