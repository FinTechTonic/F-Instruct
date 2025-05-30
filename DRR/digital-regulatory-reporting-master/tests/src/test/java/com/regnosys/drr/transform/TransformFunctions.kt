package com.regnosys.drr.transform

import cdm.event.workflow.*
import cdm.product.template.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.regnosys.drr.transform.TransformUtils.*
import com.regnosys.rosetta.common.hashing.ReferenceResolverProcessStep
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper
import com.rosetta.model.lib.RosettaModelObject
import drr.regulation.common.*
import org.isda.cdm.processor.CdmReferenceConfig
import java.util.*

private val rosettaObjectMapper: ObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper()
private val referenceResolver = ReferenceResolverProcessStep(CdmReferenceConfig.get())

private val transformTransactionHelper = TransformTransactionHelper()
private val transformEtdHelper = TransformEtdHelper()
private val transformCollateralHelper = TransformCollateralHelper()


private val runCreateWorkflowStepFromInstruction = RunCreateWorkflowStepFromInstruction()
private val preEnrichData = RunPreEnrichReportableEvent()
private val runCommodityDeliveryReportableEvent = RunCommodityDeliveryReportableEvent()


fun <T : RosettaModelObject> deserializeRosettaObject(type: Class<T>): (String) -> T {
    return fun(contents): T {
        val builder = rosettaObjectMapper.readValue(contents, type).toBuilder()
        referenceResolver.runProcessStep(type, builder)
        @Suppress("UNCHECKED_CAST")
        return builder.build() as T
    }
}

val reportableEventToTransactionReportInstructionSelf: (String) -> TransactionReportInstruction =
    fun(input): TransactionReportInstruction {
        val reportableEvent = deserialiseReportableEventAndEnrich(input)
        return transformTransactionHelper.createTransactionReportInstructionSelf(reportableEvent)
    }

val reportableEventToTransactionReportInstructionSelfOther: (String) -> TransactionReportInstruction =
    fun(input): TransactionReportInstruction {
        val reportableEvent = deserialiseReportableEventAndEnrich(input)
        return transformTransactionHelper.createTransactionReportInstructionSelfOther(reportableEvent)
    }

val reportableEventToTransactionReportInstructionMandatoryDelegated: (String) -> TransactionReportInstruction =
    fun(input): TransactionReportInstruction {
        val reportableEvent = deserialiseReportableEventAndEnrich(input)
        return transformTransactionHelper.createTransactionReportInstructionMandatoryDelegated(reportableEvent)
    }

val reportableEventToTransactionReportInstructionVoluntaryDelegated: (String) -> TransactionReportInstruction =
    fun(input): TransactionReportInstruction {
        val reportableEvent = deserialiseReportableEventAndEnrich(input)
        return transformTransactionHelper.createTransactionReportInstructionVoluntaryDelegated(reportableEvent)
    }

val reportableEventToTransactionReportInstructionNoCounterparties: (String) -> TransactionReportInstruction =
    fun(input): TransactionReportInstruction {
        val reportableEvent = deserialiseReportableEventAndEnrich(input)
        return transformTransactionHelper.createTransactionReportInstructionNoCounterparties(reportableEvent)
    }

val reportableEventToTransactionReportInstructionWithMultipleAfterTrades: (Int) -> (String) -> TransactionReportInstruction =
    { reportableTradeIndex ->
        fun(input): TransactionReportInstruction {
            val reportableEvent = deserialiseReportableEventAndEnrich(input)
            return transformTransactionHelper.createTransactionReportInstructionWithMultipleAfterTrades(
                reportableEvent,
                reportableTradeIndex
            )
        }
    }

val reportableEventToTransactionReportInstructionMASAgent: (String) -> TransactionReportInstruction =
    fun(input): TransactionReportInstruction {
        val reportableEvent = deserialiseReportableEventAndEnrich(input)
        return transformTransactionHelper.createTransactionReportInstruction(reportableEvent, RegimeNameEnum.MAS, ReportingRoleEnum.AGENT)
    }

val reportableEventToTransactionReportInstructionMASPrincipal: (String) -> TransactionReportInstruction =
    fun(input): TransactionReportInstruction {
        val reportableEvent = deserialiseReportableEventAndEnrich(input)
        return transformTransactionHelper.createTransactionReportInstruction(reportableEvent, RegimeNameEnum.MAS, ReportingRoleEnum.PRINCIPAL)
    }

val reportableEventToPreEnrichedTransactionReportInstruction: (String) -> TransactionReportInstruction =
    fun(input): TransactionReportInstruction {
        val reportableEvent = deserialiseReportableEventAndEnrich(input)
        val preEnrichedReportableEvent = preEnrichData.preEnrichReportableEvent(reportableEvent)
        return transformTransactionHelper.createTransactionReportInstructionSelf(preEnrichedReportableEvent)
    }

val transformCommodityDeliverySwap: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val reportableEventWithDelivery = accept(runCommodityDeliveryReportableEvent.addDelivery(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(reportableEventWithDelivery)
    }

val transformEtd10YearNoteFuture: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtd10YearNoteFuture(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformEtd10YearNoteOption: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtd10YearNoteOption(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformEtdCocoaFuture: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtdCocoaFuture(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformEtdCocoaOption: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtdCocoaOption(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }
val transformEtdFutureSecurityProduct: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtdSecurityFuture(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformEtdOptionSecurityProduct: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtdSecurityOption(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformEstrFeaturesCme: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtdEstrCmeFuture(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformEstrOptionCme: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtdEstrCmeOption(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformGoldFutureEtd: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtdGoldFuture(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformGoldOptionEtd: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getEtdGoldOption(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformSocGenEquityListedOption: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getSocGenEquityListedOption(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val transformSocGenEquityListedFuture: (String) -> ReportableEvent =
    fun(input): ReportableEvent {
        val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
        val acceptedReportableEvent = accept(transformEtdHelper.getSocGenEquityListedFuture(reportableEvent))
        return transformTransactionHelper.createTransactionReportInstructionSelf(acceptedReportableEvent)
    }

val toCollateralReportInstruction: (String) -> CollateralReportInstruction =
    fun(input): CollateralReportInstruction {
        val reportableCollateral = deserializeRosettaObject(ReportableCollateral::class.java)(input)
        return transformCollateralHelper.createCollateralReportInstruction(reportableCollateral)
    }

private fun deserialiseReportableEventAndEnrich(input: String): ReportableEvent.ReportableEventBuilder {
    val reportableEvent = deserializeRosettaObject(ReportableEvent::class.java)(input)
    val  acceptedReportableEvent = accept(reportableEvent)
    transformTransactionHelper.setTechnicalRecordId(acceptedReportableEvent)
    return acceptedReportableEvent
}

private fun accept(reportableEventWithDelivery: ReportableEvent): ReportableEvent.ReportableEventBuilder {
    val originatingWorkflowStep =
        runCreateWorkflowStepFromInstruction.createWorkflowStepFromInstruction(reportableEventWithDelivery.originatingWorkflowStep)
    return reportableEventWithDelivery.toBuilder()
        .setOriginatingWorkflowStep(originatingWorkflowStep)
}
