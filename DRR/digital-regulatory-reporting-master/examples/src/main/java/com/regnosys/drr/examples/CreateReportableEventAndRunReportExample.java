package com.regnosys.drr.examples;

import cdm.base.math.NonNegativeQuantitySchedule;
import cdm.base.math.QuantityChangeDirectionEnum;
import cdm.base.math.UnitType;
import cdm.base.math.metafields.FieldWithMetaNonNegativeQuantitySchedule;
import cdm.base.staticdata.identifier.AssignedIdentifier;
import cdm.base.staticdata.identifier.Identifier;
import cdm.base.staticdata.identifier.TradeIdentifierTypeEnum;
import cdm.base.staticdata.party.*;
import cdm.event.common.*;
import cdm.event.workflow.EventInstruction;
import cdm.event.workflow.EventTimestamp;
import cdm.event.workflow.EventTimestampQualificationEnum;
import cdm.event.workflow.WorkflowStep;
import cdm.event.workflow.functions.Create_AcceptedWorkflowStepFromInstruction;
import cdm.product.common.settlement.PriceQuantity;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.postprocess.WorkflowPostProcessor;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.model.metafields.MetaFields;
import drr.regulation.common.*;
import drr.regulation.common.functions.Create_ReportableEvents;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This test demonstrates how to:
 * - create a WorkflowStep (representing a Novation) from a TradeState and Instructions
 * - then create a ReportableEvent from the WorkflowStep
 * - then create a CFTCPart45TransactionReport from the ReportableEvent
 */
public class CreateReportableEventAndRunReportExample {

    @Inject Create_AcceptedWorkflowStepFromInstruction createWorkflowStep;
    @Inject Create_ReportableEvents createReportableEvents;
    @Inject WorkflowPostProcessor postProcessor;

    public static void main(String[] args) throws IOException {
        // Initialise guice for dependency injection
        Injector injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
        // Get dependency injected instance
        CreateReportableEventAndRunReportExample example = injector.getInstance(CreateReportableEventAndRunReportExample.class);

        // Run example
        example.createReportableEventAndRunReport();
    }

    /**
     * This method demonstrates how to create a ReportableEvent from a WorkflowStep, and then create a CFTCPart45TransactionReport.
     */
    void createReportableEventAndRunReport() throws IOException {
        // 1. Trade to be novated.  Note that all references are resolved here.
        TradeState tradeState = ResourcesUtils.getObjectAndResolveReferences(TradeState.class, "result-json-files/fpml-5-10/products/rates/USD-Vanilla-swap.json");

        // 2. Create instructions to novate the TradeState.
        WorkflowStep workflowStepInstruction = getNovationInstruction(tradeState);

        // 3. Invoke function to create a WorkflowStep that contains a BusinessEvent that represents a Novation.
        // The Novation BusinessEvent contains two after trades, i.e. the new trade with the stepping-in party, and
        // the original trade (which has been terminated).  Both are reportable under CFTC.
        WorkflowStep workflowStep = postProcess(createWorkflowStep.evaluate(workflowStepInstruction));

        // 4. Invoke function to convert a WorkflowStep into a list of ReportableEvents.
        // In this Novation example, there are two ReportableEvents.
        List<? extends ReportableEvent> reportableEvents =
                createReportableEvents.evaluate(workflowStep);

        // 5. Before creating the transaction report, the ReportableInformation should be added to the ReportableEvent.
        // ReportableInformation contains information such as which party is the reporting party.
        // ReportableInformation should probably an input to the function in step 4.
        List<? extends ReportableEvent> reportableEventsWithReportableInformation =
                reportableEvents.stream()
                        .map(reportableEvent -> reportableEvent.toBuilder()
                                .setReportableInformation(getReportableInformation()).build())
                        .collect(Collectors.toList());

        // 6. For each ReportableEvent, create and print the CFTCPart45TransactionReport.
        reportableEventsWithReportableInformation.forEach(reportableEvent -> {
            try {
                CFTCPart45ExampleReport cftcPart45ExampleReport = new CFTCPart45ExampleReport();
                cftcPart45ExampleReport.runReport(reportableEvent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Creates function input for a Novation event, i.e., before TradeState and Instructions.
     *
     * @return WorkflowStep containing a proposed EventInstruction for a Novation
     * @throws IOException
     * @param beforeTradeState
     */
    private WorkflowStep getNovationInstruction(TradeState beforeTradeState) {
        Date eventDate = Date.of(2013, 2, 12);

        // SplitInstruction contains two split breakdowns
        SplitInstruction splitInstruction = SplitInstruction.builder()
                // Split breakdown for party change, new trade id etc
                .addBreakdown(PrimitiveInstruction.builder()
                        .setPartyChange(PartyChangeInstruction.builder()
                                .setCounterparty(Counterparty.builder()
                                        .setPartyReferenceValue(getParty())
                                        .setRole(CounterpartyRoleEnum.PARTY_2))
                                .setTradeId(Lists.newArrayList(TradeIdentifier.builder()
                                                .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                                        .addAssignedIdentifier(AssignedIdentifier.builder().setIdentifierValue("UTI_Bank_Z"))
                                        .setIssuerValue("LEI_Bank_Z")))))
                // Split breakdown to terminate the original trade
                .addBreakdown(PrimitiveInstruction.builder()
                        .setQuantityChange(QuantityChangeInstruction.builder()
                                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                                .addChange(PriceQuantity.builder()
                                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                .setValue(NonNegativeQuantitySchedule.builder()
                                                        .setValue(BigDecimal.valueOf(0.0))
                                                        .setUnit(UnitType.builder()
                                                                .setCurrency(FieldWithMetaString.builder()
                                                                        .setValue("USD")
                                                                        .setMeta(MetaFields.builder()
                                                                                .setScheme("http://www.fpml.org/coding-scheme/external/iso4217")))))))));

        // Create an Instruction that contains:
        // - before TradeState
        // - PrimitiveInstruction containing a SplitInstruction
        Instruction tradeStateInstruction = Instruction.builder()
                .setBeforeValue(beforeTradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setSplit(splitInstruction));

        // Create a workflow step instruction containing the EventInstruction, EventTimestamp and EventIdentifiers
        return WorkflowStep.builder()
                .setProposedEvent(EventInstruction.builder()
                        .addInstruction(tradeStateInstruction)
                        .setIntent(EventIntentEnum.NOVATION)
                        .setEventDate(eventDate))
                .addTimestamp(EventTimestamp.builder()
                        .setDateTime(ZonedDateTime.of(eventDate.toLocalDate(), LocalTime.of(9, 0), ZoneOffset.UTC.normalized()))
                        .setQualification(EventTimestampQualificationEnum.EVENT_CREATION_DATE_TIME))
                .addEventIdentifier(Identifier.builder()
                        .addAssignedIdentifier(AssignedIdentifier.builder().setIdentifierValue("Novation-Example")))
                .build(); // ensure you call build() on the function input
    }

    /**
     * ReportableEvent requires ReportableInformation to specify data such as which party is the reporting party.
     */
    private ReportableInformation getReportableInformation() {
        return ReportableInformation.builder()
                .setConfirmationMethod(ConfirmationMethodEnum.ELECTRONIC)
                .setExecutionVenueType(ExecutionVenueTypeEnum.SEF)
                .setLargeSizeTrade(false)
                .setPartyInformation(Collections.singletonList(PartyInformation.builder()
                        .setPartyReferenceValue(getParty())
                        .setRegimeInformation(Collections.singletonList(ReportingRegime.builder()
                                .setSupervisoryBodyValue(SupervisoryBodyEnum.CFTC)
                                .setReportingRole(ReportingRoleEnum.REPORTING_PARTY)
                                .setMandatorilyClearable(MandatorilyClearableEnum.PRODUCT_MANDATORY_BUT_NOT_CPTY))
                        )))
                .build();
    }

    /**
     * The novation transferee party, i.e. the party stepping in.
     */
    private Party getParty() {
        return Party.builder()
                .setMeta(MetaFields.builder().setExternalKey("party3"))
                .setNameValue("Bank Z")
                .addPartyId(PartyIdentifier.builder()
                        .setIdentifierType(PartyIdentifierTypeEnum.LEI)
                        .setIdentifierValue("LEI_Bank_Z"))
                .build();
    }

    /**
     * Post-processing the function output, generates keys on any new objects, and runs qualification etc.
     */
    private <T extends RosettaModelObject> T postProcess(T o) {
        RosettaModelObjectBuilder builder = o.toBuilder();
        postProcessor.postProcess(builder.getType(), builder);
        return (T) builder;
    }
}