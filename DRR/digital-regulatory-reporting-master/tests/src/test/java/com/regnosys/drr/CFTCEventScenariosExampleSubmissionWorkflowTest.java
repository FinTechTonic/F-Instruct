package com.regnosys.drr;

import cdm.base.datetime.*;
import cdm.base.datetime.metafields.FieldWithMetaBusinessCenterEnum;
import cdm.base.math.NonNegativeQuantity;
import cdm.base.math.NonNegativeQuantitySchedule;
import cdm.base.math.QuantityChangeDirectionEnum;
import cdm.base.math.UnitType;
import cdm.base.math.metafields.FieldWithMetaNonNegativeQuantitySchedule;
import cdm.base.staticdata.identifier.AssignedIdentifier;
import cdm.base.staticdata.identifier.Identifier;
import cdm.base.staticdata.identifier.TradeIdentifierTypeEnum;
import cdm.base.staticdata.party.*;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import cdm.event.common.*;
import cdm.event.common.functions.Create_BusinessEvent;
import cdm.event.common.metafields.ReferenceWithMetaTradeState;
import cdm.event.workflow.EventTimestamp;
import cdm.event.workflow.EventTimestampQualificationEnum;
import cdm.event.workflow.WorkflowStep;
import cdm.event.workflow.functions.Create_WorkflowStep;
import cdm.observable.asset.FeeTypeEnum;
import cdm.product.asset.InterestRatePayout;
import cdm.product.asset.ReferenceInformation;
import cdm.product.common.schedule.CalculationPeriodDates;
import cdm.product.common.settlement.PriceQuantity;
import cdm.product.common.settlement.ScheduledTransferEnum;
import cdm.product.template.TradableProduct;
import cdm.product.template.TradeLot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.testpack.modifiers.TestPackModifierHelper;
import com.regnosys.drr.transform.RunCreateWorkflowStepFromInstruction;
import com.regnosys.ingest.test.framework.ingestor.ExpectationUtil;
import com.regnosys.rosetta.common.hashing.GlobalKeyProcessStep;
import com.regnosys.rosetta.common.hashing.NonNullHashCollector;
import com.regnosys.rosetta.common.hashing.ReKeyProcessStep;
import com.regnosys.rosetta.common.postprocess.WorkflowPostProcessor;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.process.PostProcessStep;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.metafields.FieldWithMetaDate;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.model.metafields.MetaFields;
import drr.regulation.common.PartyInformation;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportableInformation;
import drr.regulation.common.ReportingRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CFTCEventScenariosExampleSubmissionWorkflowTest {

    private static final String UTI_SCHEME = "http://www.fpml.org/coding-scheme/external/unique-transaction-identifier";
    private static final String CURRENCY_SCHEME = "http://www.fpml.org/coding-scheme/external/iso4217";
    private static final String CFTC_ISSUER_IDENTIFIER_SCHEME = "http://www.fpml.org/coding-scheme/external/cftc/issuer-identifier";

    @Inject
    RunCreateWorkflowStepFromInstruction createWorkflowStepFromInstruction;
    @Inject
    Create_WorkflowStep createWorkflowStep;
    @Inject
    Create_BusinessEvent createBusinessEvent;
    @Inject
    WorkflowPostProcessor postProcessRunner;

    private String dataFolder;
    private ObjectMapper objectMapper;
    private static final boolean writeExpectations = ExpectationUtil.WRITE_EXPECTATIONS;

    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
        injector.injectMembers(this);
        dataFolder = "/result-json-files/fpml-5-10/record-keeping/events/cftc-event-scenarios/";
        objectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();
    }

    @Test
    void assertWriteDisabled() {
        assertFalse(writeExpectations);
    }

    /**
     * Example 1 - New-Modify (amendment, update), Correction
     * <p>
     * Submission 2: A mutually agreed change to the notional amount is reported as Modify-Trade (MODI-TRDE)
     * with [Amendment indicator] = ‘True’. This is an amendment.
     */
    @Test
    void generateExample1Submission2AsExpected() throws IOException {
        assertJsonEquals(generateExample1Submission2(), "Example-01-Submission-2.json");
    }

    private ReportableEvent generateExample1Submission2() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-01-Submission-1.json");
        // The before TradeState of the next WorkflowStep is the after TradeState of the previous WorkflowStep
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        QuantityChangeInstruction quantityChangeInstruction = generateQuantityChangeInstruction(QuantityChangeDirectionEnum.DECREASE, BigDecimal.valueOf(1000));

        Instruction instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setQuantityChange(quantityChangeInstruction)).build();

        return generateReportableEvent(List.of(instruction),
                null,
                Date.of(2018, 4, 2),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(10, 22),
                "example-1-submission-2",
                previousReportableEvent.getReportableInformation());
    }

    /**
     * Example 1 - New-Modify (amendment, update), Correction
     * <p>
     * Submission 3: A missing information, ‘Other payment type’, is reported as Modify-Trade (MODI-TRDE)
     * combination with [Amendment indicator] = ‘False’. This is an update.
     */
    @Test
    void generateExample1Submission3AsExpected() throws IOException {

        // As this is a correction, the previous WorkflowStep is submission 2 (rather than submission 1)
        ReportableEvent ex01Sub2ReportableEvent = generateExample1Submission2();
        WorkflowStep ex01Sub2WorkflowStep = ex01Sub2ReportableEvent.getOriginatingWorkflowStep();
        TradeState beforeTradeState = getTradeState(ex01Sub2WorkflowStep);

        TransferInstruction transferInstruction = generateTransferInstruction(BigDecimal.valueOf(1500), FeeTypeEnum.TERMINATION, null, Date.of(2026, 4, 07));

        Instruction instruction = Instruction.builder()
                .setBeforeValue(beforeTradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setTransfer(transferInstruction)).build();

        ReportableEvent ex01Sub3ReportableEvent = generateReportableEvent(List.of(instruction),
                null,
                Date.of(2018, 4, 3),
                ex01Sub2WorkflowStep,
                ActionEnum.CORRECT,
                LocalTime.of(15, 1),
                "example-1-submission-3",
                ex01Sub2ReportableEvent.getReportableInformation());

        assertJsonEquals(ex01Sub3ReportableEvent, "Example-01-Submission-3.json");
    }

    /**
     * Example 1 - New-Modify (amendment, update), Correction
     * <p>
     * Submission 2: A mutually agreed change to the notional amount is reported as Modify-Trade (MODI-TRDE)
     * with [Amendment indicator] = ‘True’. This is an amendment.
     */
    @Test
    void generateExample1Submission03AsExpected() throws IOException {
        assertJsonEquals(generateExample1Submission03(), "Example-01-Submission-03.json");
    }

    private ReportableEvent generateExample1Submission03() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-01-Submission-2.json");
        // The before TradeState of the next WorkflowStep is the after TradeState of the previous WorkflowStep
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        TransferInstruction transferInstruction = generateTransferInstruction(BigDecimal.valueOf(16000), FeeTypeEnum.TERMINATION, BusinessDayConventionEnum.MODFOLLOWING, Date.of(2026, 2, 07));

        Instruction instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setTransfer(transferInstruction)).build();

        return generateReportableEvent(List.of(instruction),
                null,
                Date.of(2018, 4, 3),
                previousWorkflowStep,
                null,
                LocalTime.of(10, 22),
                "example-1-submission-03",
                previousReportableEvent.getReportableInformation());
    }

    /**
     * Example 1 - New-Modify (amendment, update), Correction
     * <p>
     * Submission 3: A missing information, ‘Other payment type’, is reported as Modify-Trade (MODI-TRDE)
     * combination with [Amendment indicator] = ‘False’. This is an update.
     */
    @Test
    void generateExample1Submission4AsExpected() throws IOException {

        ReportableEvent ex01Sub2ReportableEvent = generateExample1Submission2();
        WorkflowStep ex01Sub2WorkflowStep = ex01Sub2ReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(ex01Sub2WorkflowStep);

        TransferInstruction transferInstruction = generateTransferInstruction(BigDecimal.valueOf(16000), FeeTypeEnum.UPFRONT, BusinessDayConventionEnum.MODFOLLOWING, Date.of(2026, 2, 07));

        Instruction instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setTransfer(transferInstruction)).build();

        // As this is a correction, the previous WorkflowStep is submission 3 (rather than submission 2 which is used as the before trade)
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-01-Submission-3.json");

        ReportableEvent ex01Sub3ReportableEvent = generateReportableEvent(List.of(instruction),
                null,
                Date.of(2018, 4, 3),
                previousReportableEvent.getOriginatingWorkflowStep(),
                ActionEnum.CORRECT,
                LocalTime.of(15, 1),
                "example-1-submission-4",
                ex01Sub2ReportableEvent.getReportableInformation());

        assertJsonEquals(ex01Sub3ReportableEvent, "Example-01-Submission-4.json");
    }

    /**
     * Example 2 - Error and Revive
     * <p>
     * Submission 2: The previous transaction was submitted in error to CFTC and the removal is reported with [Action type] = ‘EROR’ without any event type.
     */
    @Test
    void generateExample2Submission2AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-02-Submission-1.json");

        // Build new workflow step using new business event
        WorkflowStep workflowStep = generateWorkflowStep(null,
                previousReportableEvent.getOriginatingWorkflowStep(),
                ActionEnum.CANCEL,
                Date.of(2018, 4, 2),
                LocalTime.of(10, 30),
                "example-2-submission-2");

        ReportableEvent reportableEvent = ReportableEvent.builder()
                .setOriginatingWorkflowStep(workflowStep)
                .setReportableInformation(previousReportableEvent.getReportableInformation())
                .build();

        assertJsonEquals(reportableEvent, "Example-02-Submission-2.json");
    }

    /**
     * Example 3 - Early termination
     * <p>
     * Submission 2: The previously reported transaction is terminated and is reported as Terminate-Early
     * termination (TERM-EART) combination. The [Event timestamp] captures the date and time of when the
     * transaction is terminated by the reporting counterparty which will be earlier than the [Expiration date]
     * of the transaction.
     */
    @Test
    void generateExample3Submission2AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-03-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        QuantityChangeInstruction quantityChangeInstruction = generateQuantityChangeInstruction(QuantityChangeDirectionEnum.REPLACE, BigDecimal.ZERO);

        Instruction instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setQuantityChange(quantityChangeInstruction)).build();

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction),
                null,
                Date.of(2019, 12, 12),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-3-submission-2",
                previousReportableEvent.getReportableInformation());

        assertJsonEquals(reportableEvent, "Example-03-Submission-2.json");
    }

    /**
     * Example 4 - Full novation
     * <p>
     * Submission 2: Reporting counterparty (DUMMY0000000000LEI01) novates the transaction to a new reporting
     * counterparty and the original transaction, LEI1RPT0001CCCC, is now terminated and reported as
     * Terminate-Novation (TERM-NOVT) combination.
     * <p>
     * Submission 3: A new transaction is reported as New-Novation (NEWT-NOVT) combination with a new
     * unique transaction identifier, LEI3RPT0003CCCC. The UTI of the original transaction (now terminated)
     * is reported in the [Prior UTI] of this transaction and a new reporting counterparty (LEI3RPT0003)
     * is reported with this submission.
     */
    @Test
    void generateExample4AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-04-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        SplitInstruction splitInstruction = SplitInstruction.builder()
                .addBreakdown(PrimitiveInstruction.builder()
                        .setPartyChange(PartyChangeInstruction.builder()
                                .setCounterparty(Counterparty.builder()
                                        .setPartyReferenceValue(Party.builder()
                                                .setMeta(MetaFields.builder().setExternalKey("party3"))
                                                .setNameValue("Bank Z")
                                                .addPartyId(PartyIdentifier.builder()
                                                        .setIdentifierType(PartyIdentifierTypeEnum.LEI)
                                                        .setIdentifier(FieldWithMetaString.builder().setValue(TestPackModifierHelper.PARTY3_LEI).setMeta(MetaFields.builder()
                                                                .setScheme(TestPackModifierHelper.LEI_SCHEME)))))
                                        .setRole(CounterpartyRoleEnum.PARTY_1))
                                .setTradeId(Lists.newArrayList(TradeIdentifier.builder()
                                        .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                                        .addAssignedIdentifier(AssignedIdentifier.builder()
                                                .setIdentifier(FieldWithMetaString.builder()
                                                        .setMeta(MetaFields.builder().setScheme(UTI_SCHEME))
                                                        .setValue("LEI3RPT0003CCC"))
                                        )
                                        .setIssuer(FieldWithMetaString.builder()
                                                .setMeta(MetaFields.builder().setScheme(CFTC_ISSUER_IDENTIFIER_SCHEME))
                                                .setValue(TestPackModifierHelper.PARTY3_LEI))))))
                .addBreakdown(PrimitiveInstruction.builder()
                        .setQuantityChange(QuantityChangeInstruction.builder()
                                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                                .addChange(PriceQuantity.builder()
                                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                .setValue(NonNegativeQuantitySchedule.builder()
                                                        .setValue(BigDecimal.valueOf(0.0))
                                                        .setUnit(UnitType.builder()
                                                                .setCurrency(FieldWithMetaString.builder()
                                                                        .setMeta(MetaFields.builder()
                                                                                .setScheme(CURRENCY_SCHEME))
                                                                        .setValue("USD"))))))));

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder().setSplit(splitInstruction));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.NOVATION,
                Date.of(2018, 4, 3),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-4",
                previousReportableEvent.getReportableInformation());

        assertJsonEquals(setReportableTrade(reportableEvent, 1), "Example-04-Submission-2.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-04-Submission-3.json");
    }

    /**
     * Example 5 - Partial novation
     * <p>
     * Submission 2: Reporting counterparty (DUMMY0000000000LEI01) partially novates the transaction to a new
     * reporting counterparty and the update to original transaction is reported as Modify-Novation
     * (MODI-NOVT) combination with [Amendment indicator] = ‘True’ and a reduced notional amount of
     * ‘8000’. The transaction continues to be active.
     * <p>
     * Submission 3: A new transaction is reported as New-Novation (NEWT-NOVT) combination with a new
     * unique transaction identifier, LEI3RPT0003DDDD. The original reporting counterparty (DUMMY0000000000LEI01)
     * novated and transferred the obligations for the allocated amount of ‘5000’ to a new reporting
     * counterparty (LEI3RPT0003) and the UTI of the original transaction is reported in the [Prior UTI]
     * of this transaction.
     */
    @Test
    void generateExample5AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-05-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        SplitInstruction splitInstruction = SplitInstruction.builder()
                .addBreakdown(PrimitiveInstruction.builder()
                        .setPartyChange(PartyChangeInstruction.builder()
                                .setCounterparty(Counterparty.builder()
                                        .setPartyReferenceValue(Party.builder()
                                                .setMeta(MetaFields.builder().setExternalKey("party3"))
                                                .setNameValue("Bank Z")
                                                .addPartyId(PartyIdentifier.builder()
                                                        .setIdentifierType(PartyIdentifierTypeEnum.LEI)
                                                        .setIdentifier(FieldWithMetaString.builder().setValue(TestPackModifierHelper.PARTY3_LEI)
                                                                .setMeta(MetaFields.builder()
                                                                        .setScheme(TestPackModifierHelper.LEI_SCHEME)))))
                                        .setRole(CounterpartyRoleEnum.PARTY_1))
                                .setTradeId(Lists.newArrayList(TradeIdentifier.builder()
                                        .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                                        .addAssignedIdentifier(AssignedIdentifier.builder()
                                                .setIdentifier(FieldWithMetaString.builder()
                                                        .setMeta(MetaFields.builder().setScheme(UTI_SCHEME))
                                                        .setValue("LEI3RPT0003DDDD")))
                                        .setIssuer(FieldWithMetaString.builder()
                                                .setMeta(MetaFields.builder().setScheme(CFTC_ISSUER_IDENTIFIER_SCHEME))
                                                .setValue(TestPackModifierHelper.PARTY3_LEI)))))
                        .setQuantityChange(QuantityChangeInstruction.builder()
                                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                                .addChange(PriceQuantity.builder()
                                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                .setValue(NonNegativeQuantitySchedule.builder()
                                                        .setValue(BigDecimal.valueOf(5000.0))
                                                        .setUnit(UnitType.builder()
                                                                .setCurrency(FieldWithMetaString.builder()
                                                                        .setMeta(MetaFields.builder()
                                                                                .setScheme(CURRENCY_SCHEME))
                                                                        .setValue("USD"))))))))
                .addBreakdown(PrimitiveInstruction.builder()
                        .setQuantityChange(QuantityChangeInstruction.builder()
                                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                                .addChange(PriceQuantity.builder()
                                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                .setValue(NonNegativeQuantitySchedule.builder()
                                                        .setValue(BigDecimal.valueOf(8000.0))
                                                        .setUnit(UnitType.builder()
                                                                .setCurrency(FieldWithMetaString.builder()
                                                                        .setMeta(MetaFields.builder()
                                                                                .setScheme(CURRENCY_SCHEME))
                                                                        .setValue("USD"))))))));

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder().setSplit(splitInstruction));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.NOVATION,
                Date.of(2018, 4, 4),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-5",
                previousReportableEvent.getReportableInformation());

        assertJsonEquals(setReportableTrade(reportableEvent, 1), "Example-05-Submission-2.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-05-Submission-3.json");
    }

    /**
     * Example 6 - Clearing novation
     * <p>
     * Submission 2: Upon clearing acceptance, the original swap transaction is extinguished and reported
     * as Terminate-Clearing (TERM-CLRG) combination.
     * <p>
     * Submission 3, 4: Simultaneously, two new clearing swaps are created that replaces the original swap.
     * These two transactions are reported as New-Clearing (NEWT-CLRG) combination by the DCO as the
     * reporting counterparty and the UTI of the original swap is reported in the [Prior UTI] of these
     * transactions. Execution timestamp of clearing swap is the time when the original swap is
     * accepted by the DCO.
     */
    @Test
    void generateExample6AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-06-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        SplitInstruction splitInstruction = SplitInstruction.builder()
                .addBreakdown(PrimitiveInstruction.builder()
                        .setPartyChange(PartyChangeInstruction.builder()
                                .setCounterparty(Counterparty.builder()
                                        .setPartyReferenceValue(Party.builder()
                                                .setMeta(MetaFields.builder().setExternalKey("clearing-svc"))
                                                .setNameValue("ClearItAll")
                                                .addPartyId(PartyIdentifier.builder()
                                                        .setIdentifier(FieldWithMetaString.builder()
                                                                .setMeta(MetaFields.builder()
                                                                        .setScheme(TestPackModifierHelper.LEI_SCHEME))
                                                                .setValue(TestPackModifierHelper.PARTY4_LEI))
                                                        .setIdentifierType(PartyIdentifierTypeEnum.LEI))
                                                .build())
                                        .setRole(CounterpartyRoleEnum.PARTY_2))
                                .setTradeId(Lists.newArrayList(TradeIdentifier.builder()
                                        .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                                        .addAssignedIdentifier(AssignedIdentifier.builder()
                                                .setIdentifier(FieldWithMetaString.builder()
                                                        .setMeta(MetaFields.builder().setScheme(UTI_SCHEME))
                                                        .setValue("LEI1DCO01BETA")))
                                        .setIssuer(FieldWithMetaString.builder()
                                                .setMeta(MetaFields.builder().setScheme(CFTC_ISSUER_IDENTIFIER_SCHEME))
                                                .setValue(TestPackModifierHelper.PARTY4_LEI))))))
                .addBreakdown(PrimitiveInstruction.builder()
                        .setPartyChange(PartyChangeInstruction.builder()
                                .setCounterparty(Counterparty.builder()
                                        .setPartyReferenceValue(Party.builder()
                                                .setMeta(MetaFields.builder().setExternalKey("clearing-svc"))
                                                .setNameValue("ClearItAll")
                                                .addPartyId(PartyIdentifier.builder()
                                                        .setIdentifier(FieldWithMetaString.builder()
                                                                .setMeta(MetaFields.builder()
                                                                        .setScheme(TestPackModifierHelper.LEI_SCHEME))
                                                                .setValue(TestPackModifierHelper.PARTY4_LEI))
                                                        .setIdentifierType(PartyIdentifierTypeEnum.LEI))
                                                .build())
                                        .setRole(CounterpartyRoleEnum.PARTY_1))
                                .setTradeId(Lists.newArrayList(TradeIdentifier.builder()
                                        .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                                        .addAssignedIdentifier(AssignedIdentifier.builder()
                                                .setIdentifier(FieldWithMetaString.builder()
                                                        .setMeta(MetaFields.builder().setScheme(UTI_SCHEME))
                                                        .setValue("LEI1DCO01GAMMA")))
                                        .setIssuer(FieldWithMetaString.builder()
                                                .setMeta(MetaFields.builder().setScheme(CFTC_ISSUER_IDENTIFIER_SCHEME))
                                                .setValue(TestPackModifierHelper.PARTY4_LEI))))))
                .addBreakdown(PrimitiveInstruction.builder()
                        .setQuantityChange(QuantityChangeInstruction.builder()
                                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                                .addChange(PriceQuantity.builder()
                                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                .setValue(NonNegativeQuantitySchedule.builder()
                                                        .setValue(BigDecimal.valueOf(0.0))
                                                        .setUnit(UnitType.builder()
                                                                .setCurrency(FieldWithMetaString.builder()
                                                                        .setMeta(MetaFields.builder()
                                                                                .setScheme(CURRENCY_SCHEME))
                                                                        .setValue("USD"))))))));

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder().setSplit(splitInstruction));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.CLEARING,
                Date.of(2018, 4, 1),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-6",
                previousReportableEvent.getReportableInformation());

        assertJsonEquals(setReportableTrade(reportableEvent, 2), "Example-06-Submission-2.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-06-Submission-3.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 1), "Example-06-Submission-4.json");
    }

    /**
     * Example 7 - Compression
     * <p>
     * Submission 4, 5 and 6: All three transactions are terminated as a result of compression and
     * are reported as Terminate-Compression (TERM-COMP) combination as separate submissions.
     * [Event identifier] is reported for all terminated transactions to link with the resulted
     * new transaction.
     * <p>
     * Submission 7: The new compressed transaction is reported as New-Compression (NEWT-COMP)
     * combination with a new unique transaction identifier and a new execution timestamp. The
     * same [Event identifier] as the terminated pre-compression transactions is reported in
     * this post-compression transaction to link the compression event and the notional amount
     * is reduced as a result of compression.
     */
    @Test
    void generateExample7AsExpected() throws IOException {
        List<Instruction> instructions = new ArrayList<>();

        QuantityChangeInstruction terminateInstructions = QuantityChangeInstruction.builder()
                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                .addChange(PriceQuantity.builder()
                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                .setValue(NonNegativeQuantitySchedule.builder()
                                        .setValue(BigDecimal.valueOf(0.0))
                                        .setUnit(UnitType.builder()
                                                .setCurrency(FieldWithMetaString.builder()
                                                        .setMeta(MetaFields.builder().setScheme(CURRENCY_SCHEME))
                                                        .setValue("USD"))))));


        ReportableEvent previousReportableEvent1 = getReportableEventFromFpML("Example-07-Submission-1.json");
        TradeState tradeState1 = getTradeState(previousReportableEvent1.getOriginatingWorkflowStep());

        instructions.add(Instruction.builder()
                .setBeforeValue(tradeState1)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setQuantityChange(terminateInstructions))
                .build());

        ReportableEvent previousReportableEvent2 = getReportableEventFromFpML("Example-07-Submission-2.json");
        TradeState tradeState2 = getTradeState(previousReportableEvent2.getOriginatingWorkflowStep());

        instructions.add(Instruction.builder()
                .setBeforeValue(tradeState2)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setQuantityChange(terminateInstructions))
                .build());

        ReportableEvent previousReportableEvent3 = getReportableEventFromFpML("Example-07-Submission-3.json");
        TradeState tradeState3 = getTradeState(previousReportableEvent3.getOriginatingWorkflowStep());

        instructions.add(Instruction.builder()
                .setBeforeValue(tradeState3)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setQuantityChange(terminateInstructions))
                .build());

        instructions.add(Instruction.builder()
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setExecution(generateExample7ExecutionInstruction()))
                .build());

        ReportableEvent reportableEvent = generateReportableEvent(instructions,
                EventIntentEnum.COMPRESSION,
                Date.of(2018, 4, 3),
                null,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-7",
                previousReportableEvent1.getReportableInformation());

        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-07-Submission-4.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 1), "Example-07-Submission-5.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 2), "Example-07-Submission-6.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 3), "Example-07-Submission-7.json");
    }

    private ExecutionInstruction generateExample7ExecutionInstruction() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML(
                "/result-json-files/fpml-5-10/record-keeping/products/rates/",
                "IR-IRS-Fixed-Float-ex01.json");
        TradeState.TradeStateBuilder tradeStateBuilder = getTradeState(previousReportableEvent.getOriginatingWorkflowStep()).toBuilder();
        Trade.TradeBuilder tradeBuilder = tradeStateBuilder.getTrade();
        TradableProduct.TradableProductBuilder tradableProductBuilder = tradeBuilder.getTradableProduct();

        TradeLot.TradeLotBuilder tradeLotBuilder = tradableProductBuilder.getTradeLot().get(0);
        tradeLotBuilder
                .getPriceQuantity().get(0)
                .getQuantity().get(0)
                .getValue().setValue(BigDecimal.valueOf(16000.00));
        tradeLotBuilder
                .getPriceQuantity().get(1)
                .getQuantity().get(0)
                .getValue().setValue(BigDecimal.valueOf(16000.00));

        List<? extends InterestRatePayout.InterestRatePayoutBuilder> interestRatePayoutBuilders = tradableProductBuilder
                .getProduct()
                .getContractualProduct()
                .getEconomicTerms()
                .getPayout()
                .getInterestRatePayout();

        Date effectiveDate = Date.of(2018, 4, 3);
        Date terminationDate = Date.of(2026, 2, 8);

        setDates(interestRatePayoutBuilders.get(0), effectiveDate, terminationDate);
        setDates(interestRatePayoutBuilders.get(1), effectiveDate, terminationDate);

        tradeBuilder
                .getParty().get(0)
                .getPartyId().get(0)
                .setIdentifierValue(TestPackModifierHelper.PARTY1_LEI);

        tradeBuilder
                .getParty().get(1)
                .getPartyId().get(0)
                .setIdentifierValue(TestPackModifierHelper.PARTY2_LEI);

        TradeIdentifier tradeIdentifier = TradeIdentifier.builder()
                .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                .addAssignedIdentifier(AssignedIdentifier.builder()
                        .setIdentifier(FieldWithMetaString.builder()
                                .setMeta(MetaFields.builder().setScheme(UTI_SCHEME))
                                .setValue("LEI1RPT0003EFG")))
                .setIssuer(FieldWithMetaString.builder()
                        .setMeta(MetaFields.builder().setScheme(TestPackModifierHelper.LEI_SCHEME))
                        .setValue(TestPackModifierHelper.PARTY1_LEI));
        tradeBuilder
                .setTradeIdentifier(Lists.newArrayList(tradeIdentifier.build()))
                .setTradeDateValue(effectiveDate);

        reKey(tradeStateBuilder);

        return createExecutionInstructionFromTradeState(tradeStateBuilder.build());
    }

    public static ExecutionInstruction createExecutionInstructionFromTradeState(TradeState tradeState) {
        return ExecutionInstruction.builder()
                .setProduct(tradeState.getTrade().getTradableProduct().getProduct())
                .setPriceQuantity(tradeState.getTrade().getTradableProduct().getTradeLot().stream().map(t -> t.getPriceQuantity()).flatMap(Collection::stream).collect(Collectors.toList()))
                .addCounterparty(tradeState.getTrade().getTradableProduct().getCounterparty())
                .addAncillaryParty(tradeState.getTrade().getTradableProduct().getAncillaryParty())
                .addParties(tradeState.getTrade().getParty())
                .addPartyRoles(tradeState.getTrade().getPartyRole())
                .setTradeDateValue(Optional.ofNullable(tradeState.getTrade().getTradeDate()).map(FieldWithMetaDate::getValue).orElse(null))
                .addTradeIdentifier(tradeState.getTrade().getTradeIdentifier())
                .build();
    }

    private void setDates(InterestRatePayout.InterestRatePayoutBuilder payout, Date effectiveDate, Date terminationDate) {
        CalculationPeriodDates.CalculationPeriodDatesBuilder floatingLegCalcDates = payout
                .getCalculationPeriodDates();
        floatingLegCalcDates
                .getEffectiveDate()
                .getAdjustableDate()
                .setUnadjustedDate(effectiveDate);
        floatingLegCalcDates
                .getTerminationDate()
                .getAdjustableDate()
                .setUnadjustedDate(terminationDate);
    }

    /**
     * Example 8 - Exercise (Cash settled)
     * <p>
     * Submission 2: When the option holder fully exercises the transaction and cash settles the
     * transaction, it results in termination of the transaction as Terminate-Exercise (TERM-EXER)
     * combination and no new transaction is created. Note that the date on the [Event timestamp]
     * is the same as the date in [Expiration date] for a European style option.
     */
    @Test
    void generateExample8AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-08-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        QuantityChangeInstruction.QuantityChangeInstructionBuilder quantityChangeInstructionBuilder =
                QuantityChangeInstruction.builder()
                        .addChange(PriceQuantity.builder()
                                .addQuantityValue(NonNegativeQuantitySchedule.builder()
                                        .setValue(BigDecimal.ZERO)
                                        .setUnit(UnitType.builder().setCurrencyValue("EUR"))))
                        .setDirection(QuantityChangeDirectionEnum.REPLACE);

        TransferInstruction.TransferInstructionBuilder transferInstructionBuilder = TransferInstruction.builder();

        Transfer.TransferBuilder transferBuilder = transferInstructionBuilder
                .getOrCreateTransferState(0)
                .getOrCreateTransfer();

        transferBuilder.getOrCreatePayerReceiver()
                .setPayerPartyReference(ReferenceWithMetaParty.builder().setExternalReference("party1").build())
                .setReceiverPartyReference(ReferenceWithMetaParty.builder().setExternalReference("party2").build());

        transferBuilder.getOrCreateQuantity()
                .setValue(BigDecimal.valueOf(2000))
                .setUnit(UnitType.builder()
                        .setCurrency(FieldWithMetaString.builder()
                                .setValue("EUR")
                                .setMeta(MetaFields.builder().setScheme(CURRENCY_SCHEME).build())
                                .build())
                        .build());

        transferBuilder.getOrCreateSettlementDate()
                .setAdjustedDateValue(Date.of(2019, 4, 3));

        transferBuilder.getOrCreateTransferExpression()
                .getOrCreateScheduledTransfer()
                .setTransferType(ScheduledTransferEnum.EXERCISE);

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setQuantityChange(quantityChangeInstructionBuilder)
                        .setTransfer(transferInstructionBuilder));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.OPTION_EXERCISE,
                Date.of(2019, 4, 1),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-8",
                previousReportableEvent.getReportableInformation());

        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-08-Submission-2.json");
    }

    /**
     * Example 9 - Exercise (Partially exercised, physically settled)
     * <p>
     * Submission 2: When the reporting counterparty of the transaction partially (5000) exercises
     * the option as specified in the contract of the option transaction, the existing transaction
     * is reported with the remaining notional amount (11000) as Modify-Exercise (MODI-EXER)
     * combination with [Amendment indicator] = ‘False’. Note that the option holder still holds
     * the rights to exercise the remaining notional amount.
     * <p>
     * Submission 3: Since the option holder exercised and entered into the transaction, a new unique
     * transaction identifier, LEI1RPT0001IIIIEx, is reported as New-Exercise (NEWT-EXER) combination
     * with the partially exercised notional amount (5000). The UTI of the previous transaction is
     * reported in the [Prior UTI] of the new transaction.
     */
    @Test
    void generateExample9AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-09-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        ExerciseInstruction.ExerciseInstructionBuilder exerciseInstructionBuilder = ExerciseInstruction.builder();

        QuantityChangeInstruction.QuantityChangeInstructionBuilder quantityChangeInstructionBuilder =
                QuantityChangeInstruction.builder()
                        .addChange(PriceQuantity.builder()
                                .addQuantityValue(NonNegativeQuantitySchedule.builder()
                                        .setValue(BigDecimal.valueOf(11000))
                                        .setUnit(UnitType.builder().setCurrencyValue("EUR"))))
                        .setDirection(QuantityChangeDirectionEnum.REPLACE);

        exerciseInstructionBuilder.getOrCreateExerciseQuantity()
                .setQuantityChange(quantityChangeInstructionBuilder);

        exerciseInstructionBuilder.addReplacementTradeIdentifier(
                TradeIdentifier.builder()
                        .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                        .addAssignedIdentifier(
                                AssignedIdentifier.builder()
                                        .setIdentifier(FieldWithMetaString.builder()
                                                .setMeta(MetaFields.builder().setScheme(UTI_SCHEME))
                                                .setValue("LEI1RPT0001IIIIEx")
                                        )
                        )
                        .setIssuer(FieldWithMetaString.builder()
                                .setMeta(MetaFields.builder().setScheme(CFTC_ISSUER_IDENTIFIER_SCHEME))
                                .setValue("DUMMY0000000000LEI01")
                        )
        );

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder().setExercise(exerciseInstructionBuilder));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.OPTION_EXERCISE,
                Date.of(2019, 4, 1),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-9",
                previousReportableEvent.getReportableInformation());

        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-09-Submission-2.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 1), "Example-09-Submission-3.json");
    }

    /**
     * Example 10 - Exercise (Cancellable option)
     * <p>
     * Submission 2: When the option holder of the transaction exercises its rights specified in the
     * contract to partially exercise the transaction for the amount of ‘4000’, the existing transaction
     * is reported as Modify- Exercise (MODI-EXER) combination with [Amendment indicator] = ‘False’
     * and the remaining notional amount of ‘12000’.
     */
    @Test
    void generateExample10AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-10-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        QuantityChangeInstruction.QuantityChangeInstructionBuilder quantityChangeInstructionBuilder =
                QuantityChangeInstruction.builder()
                        .addChange(PriceQuantity.builder()
                                .addQuantityValue(NonNegativeQuantitySchedule.builder()
                                        .setValue(BigDecimal.valueOf(12000))
                                        .setUnit(UnitType.builder().setCurrencyValue("EUR"))))
                        .setDirection(QuantityChangeDirectionEnum.REPLACE);

        TransferInstruction.TransferInstructionBuilder transferInstructionBuilder = TransferInstruction.builder();

        Transfer.TransferBuilder transferBuilder = transferInstructionBuilder
                .getOrCreateTransferState(0)
                .getOrCreateTransfer();

        transferBuilder.getOrCreatePayerReceiver()
                .setPayerPartyReference(ReferenceWithMetaParty.builder().setExternalReference("party1"))
                .setReceiverPartyReference(ReferenceWithMetaParty.builder().setExternalReference("party2"));

        transferBuilder.getOrCreateQuantity()
                .setValue(BigDecimal.valueOf(2000))
                .setUnit(UnitType.builder()
                        .setCurrency(FieldWithMetaString.builder()
                                .setValue("EUR")
                                .setMeta(MetaFields.builder().setScheme(CURRENCY_SCHEME))
                        )
                );

        transferBuilder.setSettlementDate(AdjustableOrAdjustedOrRelativeDate.builder()
                .setAdjustedDateValue(Date.of(2019, 4, 3))
        );

        transferBuilder.getOrCreateTransferExpression()
                .getOrCreateScheduledTransfer()
                .setTransferType(ScheduledTransferEnum.EXERCISE);

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder()
                        .setQuantityChange(quantityChangeInstructionBuilder)
                        .setTransfer(transferInstructionBuilder));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.OPTION_EXERCISE,
                Date.of(2019, 4, 1),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-10",
                previousReportableEvent.getReportableInformation());

        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-10-Submission-2.json");
    }

    /**
     * Example 11 - Allocation
     * <p>
     * Submission 2: Upon allocation by the allocation agent, the pre-allocation swap transaction is
     * terminated as Terminate-Allocation (TERM-ALOC) combination.
     * <p>
     * Submission 3, 4: Simultaneously, two new post allocation swap transactions are created that replaces
     * the pre-allocation swap. These two transactions are reported as New-Allocation (NEWT-ALOC) combination
     * by the reporting counterparty and the UTI of the pre-allocation swap is reported in the [Prior UTI]
     * of these transactions.
     */
    @Test
    void generateExample11AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-11-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        SplitInstruction splitInstruction = SplitInstruction.builder()
                // Allocated to Fund 2
                .addBreakdown(PrimitiveInstruction.builder()
                        .setPartyChange(PartyChangeInstruction.builder()
                                .setCounterparty(Counterparty.builder()
                                        .setPartyReferenceValue(Party.builder()
                                                .setMeta(MetaFields.builder().setExternalKey("party3"))
                                                .setNameValue("Fund 2")
                                                .addPartyId(PartyIdentifier.builder()
                                                        .setIdentifierType(PartyIdentifierTypeEnum.LEI)
                                                        .setIdentifier(FieldWithMetaString.builder().setValue(TestPackModifierHelper.PARTY5_LEI)
                                                                .setMeta(MetaFields.builder().setScheme(TestPackModifierHelper.LEI_SCHEME)))))
                                        .setRole(CounterpartyRoleEnum.PARTY_2))
                                .setTradeId(Lists.newArrayList(TradeIdentifier.builder()
                                        .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                                        .addAssignedIdentifier(AssignedIdentifier.builder()
                                                .setIdentifier(FieldWithMetaString.builder()
                                                        .setMeta(MetaFields.builder().setScheme(UTI_SCHEME))
                                                        .setValue("LEI1RPT001POST1")))
                                        .setIssuer(FieldWithMetaString.builder()
                                                .setMeta(MetaFields.builder().setScheme(CFTC_ISSUER_IDENTIFIER_SCHEME))
                                                .setValue(TestPackModifierHelper.PARTY5_LEI)))))
                        .setQuantityChange(QuantityChangeInstruction.builder()
                                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                                .addChange(PriceQuantity.builder()
                                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                .setValue(NonNegativeQuantitySchedule.builder()
                                                        .setValue(BigDecimal.valueOf(7000.0))
                                                        .setUnit(UnitType.builder()
                                                                .setCurrencyValue("EUR")))))))
                // Allocated to Fund 3
                .addBreakdown(PrimitiveInstruction.builder()
                        .setPartyChange(PartyChangeInstruction.builder()
                                .setCounterparty(Counterparty.builder()
                                        .setPartyReferenceValue(Party.builder()
                                                .setMeta(MetaFields.builder().setExternalKey("party4"))
                                                .setNameValue("Fund 3")
                                                .addPartyId(PartyIdentifier.builder()
                                                        .setIdentifierType(PartyIdentifierTypeEnum.LEI)
                                                        .setIdentifier(FieldWithMetaString.builder().setValue(TestPackModifierHelper.PARTY5_LEI)
                                                                .setMeta(MetaFields.builder()
                                                                        .setScheme(TestPackModifierHelper.LEI_SCHEME)))))
                                        .setRole(CounterpartyRoleEnum.PARTY_2))
                                .setTradeId(Lists.newArrayList(TradeIdentifier.builder()
                                        .setIdentifierType(TradeIdentifierTypeEnum.UNIQUE_TRANSACTION_IDENTIFIER)
                                        .addAssignedIdentifier(AssignedIdentifier.builder()
                                                .setIdentifier(FieldWithMetaString.builder()
                                                        .setMeta(MetaFields.builder().setScheme(UTI_SCHEME))
                                                        .setValue("LEI1RPT001POST2")))
                                        .setIssuer(FieldWithMetaString.builder()
                                                .setMeta(MetaFields.builder().setScheme(CFTC_ISSUER_IDENTIFIER_SCHEME))
                                                .setValue(TestPackModifierHelper.PARTY5_LEI)))))
                        .setQuantityChange(QuantityChangeInstruction.builder()
                                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                                .addChange(PriceQuantity.builder()
                                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                .setValue(NonNegativeQuantitySchedule.builder()
                                                        .setValue(BigDecimal.valueOf(3000.0))
                                                        .setUnit(UnitType.builder()
                                                                .setCurrencyValue("EUR")))))))
                // Close original trade
                .addBreakdown(PrimitiveInstruction.builder()
                        .setQuantityChange(QuantityChangeInstruction.builder()
                                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                                .addChange(PriceQuantity.builder()
                                        .addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                .setValue(NonNegativeQuantitySchedule.builder()
                                                        .setValue(BigDecimal.valueOf(0.0))
                                                        .setUnit(UnitType.builder()
                                                                .setCurrencyValue("EUR")))))));

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder().setSplit(splitInstruction));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.ALLOCATION,
                Date.of(2018, 4, 1),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-11",
                previousReportableEvent.getReportableInformation());

        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-11-Submission-2.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 1), "Example-11-Submission-3.json");
        assertJsonEquals(setReportableTrade(reportableEvent, 2), "Example-11-Submission-4.json");
    }

    @Test
    void generateExample12Submission3AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-12-Submission-1.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        ObservationEvent observationEvent = ObservationEvent.builder()
                .setCreditEvent(CreditEvent.builder()
                        .setCreditEventType(CreditEventTypeEnum.BANKRUPTCY)
                        .setEventDeterminationDate(Date.of(2022, 3, 18))
                        .setAuctionDate(Date.of(2022, 4, 22))
                        .setReferenceInformation(ReferenceInformation.builder()
                                .setReferenceEntity(
                                        LegalEntity.builder()
                                                .setEntityId(Collections.singletonList(FieldWithMetaString.builder()
                                                        .setValue("US004421UD38")))
                                                .setName(FieldWithMetaString.builder()
                                                        .setValue("ACE SECURITIES CORP.  SERIES 2005-HE7")))
                                .setNoReferenceObligation(true))

                );
        ObservationInstruction observationInstruction = ObservationInstruction.builder()
                .setObservationEvent(observationEvent);

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder().setObservation(observationInstruction));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.CREDIT_EVENT,
                Date.of(2018, 4, 1),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-12",
                previousReportableEvent.getReportableInformation());
        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-12-Submission-3.json");
    }

    @Test
    void generateExample12Submission4AsExpected() throws IOException {
        ReportableEvent previousReportableEvent = getReportableEventFromFpML("Example-12-Submission-2.json");
        WorkflowStep previousWorkflowStep = previousReportableEvent.getOriginatingWorkflowStep();
        TradeState tradeState = getTradeState(previousWorkflowStep);

        ObservationEvent observationEvent = ObservationEvent.builder()
                .setCreditEvent(CreditEvent.builder()
                        .setCreditEventType(CreditEventTypeEnum.FAILURE_TO_PAY)
                        .setEventDeterminationDate(Date.of(2022, 3, 18))
                        .setAuctionDate(Date.of(2022, 4, 22))
                        .setReferenceInformation(ReferenceInformation.builder()
                                .setReferenceEntity(
                                        LegalEntity.builder()
                                                .setEntityId(Collections.singletonList(FieldWithMetaString.builder()
                                                        .setValue("US02376R1023")))
                                                .setName(FieldWithMetaString.builder()
                                                        .setValue("AMERICAN AIRLINES GROUP INC.")))
                                .setNoReferenceObligation(true))

                );
        ObservationInstruction observationInstruction = ObservationInstruction.builder()
                .setObservationEvent(observationEvent);

        Instruction.InstructionBuilder instruction = Instruction.builder()
                .setBeforeValue(tradeState)
                .setPrimitiveInstruction(PrimitiveInstruction.builder().setObservation(observationInstruction));

        reKey(instruction);

        ReportableEvent reportableEvent = generateReportableEvent(List.of(instruction.build()),
                EventIntentEnum.CREDIT_EVENT,
                Date.of(2018, 4, 1),
                previousWorkflowStep,
                ActionEnum.NEW,
                LocalTime.of(9, 0),
                "example-12",
                previousReportableEvent.getReportableInformation());
        assertJsonEquals(setReportableTrade(reportableEvent, 0), "Example-12-Submission-4.json");
    }

    private ReportableEvent getReportableEventFromFpML(String path, String fpmlResourceName) throws IOException {
        String fpmlResourceJson = readResource(path + fpmlResourceName);
        ReportableEvent reportableEvent = objectMapper.readValue(fpmlResourceJson, ReportableEvent.class);
        // FpML events are mapped to WorkflowStep instructions (e.g. businessEvent is absent, and proposedEvent exists)
        WorkflowStep workflowStepInstruction = reportableEvent.getOriginatingWorkflowStep();
        // Convert instruction into event
        WorkflowStep workflowStepEvent =
                createWorkflowStepFromInstruction.createWorkflowStepFromInstruction(workflowStepInstruction);
        // Regenerate keys
        ReportableEvent.ReportableEventBuilder reportableEventBuilder =
                reportableEvent.toBuilder().setOriginatingWorkflowStep(workflowStepEvent);
        return reKey(reportableEventBuilder);
    }

    private ReportableEvent getReportableEventFromFpML(String fpmlResourceName) throws IOException {
        return getReportableEventFromFpML(dataFolder, fpmlResourceName);
    }

    private QuantityChangeInstruction generateQuantityChangeInstruction(QuantityChangeDirectionEnum direction, BigDecimal amount) {
        return QuantityChangeInstruction.builder()
                .setDirection(direction)
                .addChange(PriceQuantity.builder()
                        .addQuantityValue(NonNegativeQuantitySchedule.builder()
                                .setValue(amount)
                                .setUnit(UnitType.builder()
                                        .setCurrency(FieldWithMetaString.builder()
                                                .setValue("USD")
                                                .setMeta(MetaFields.builder()
                                                        .setScheme(CURRENCY_SCHEME))))
                        ))

                .build();
    }

    private TransferInstruction generateTransferInstruction(BigDecimal amount, FeeTypeEnum feeTypeEnum, BusinessDayConventionEnum businessDayConvention, Date unadjustedDate) {
        return TransferInstruction.builder()
                .setTransferState(Collections.singletonList(TransferState.builder()
                        .setTransfer(Transfer.builder()
                                .setPayerReceiver(PartyReferencePayerReceiver.builder()
                                        .setPayerPartyReference(ReferenceWithMetaParty.builder()
                                                .setExternalReference("party1")
                                                .setGlobalReference("a3826565"))
                                        .setReceiverPartyReference(ReferenceWithMetaParty.builder()
                                                .setExternalReference("party2")
                                                .setGlobalReference("ee963b6")))
                                .setQuantity(NonNegativeQuantity.builder()
                                        .setValue(amount)
                                        .setUnit(UnitType.builder()
                                                .setCurrency(FieldWithMetaString.builder()
                                                        .setValue("USD")
                                                        .setMeta(MetaFields.builder()
                                                                .setScheme(CURRENCY_SCHEME))))
                                )
                                .setSettlementDate(AdjustableOrAdjustedOrRelativeDate.builder()
                                        .setDateAdjustments(BusinessDayAdjustments.builder()
                                                .setBusinessCenters(BusinessCenters.builder()
                                                        .setBusinessCenter(Collections.singletonList(FieldWithMetaBusinessCenterEnum.builder()
                                                                .setValue(BusinessCenterEnum.SGSI))))
                                                .setBusinessDayConvention(businessDayConvention))
                                        .setUnadjustedDate(unadjustedDate))
                                .setTransferExpression(TransferExpression.builder()
                                        .setPriceTransfer(feeTypeEnum))

                        )))
                .build();

    }

    private ReportableEvent generateReportableEvent(List<Instruction> instruction,
                                                    EventIntentEnum intent,
                                                    Date eventDate,
                                                    WorkflowStep previousWorkflowStep,
                                                    ActionEnum action,
                                                    LocalTime eventTime,
                                                    String identifier,
                                                    ReportableInformation reportableInformation) {
        // Create new business event using instruction
        BusinessEvent businessEvent = createBusinessEvent.evaluate(
                instruction,
                intent,
                eventDate,
                null);

        // Build new workflow step using new business event
        WorkflowStep workflowStep = generateWorkflowStep(businessEvent,
                previousWorkflowStep,
                action,
                eventDate,
                eventTime,
                identifier);

        // Build new reportable event using new business event
        ReportableEvent.ReportableEventBuilder reportableEventBuilder = ReportableEvent.builder()
                .setOriginatingWorkflowStep(workflowStep)
                .setReportableInformation(reportableInformation);

        return (ReportableEvent) postProcessRunner.postProcess(ReportableEvent.class, reportableEventBuilder).build();
    }

    private WorkflowStep generateWorkflowStep(BusinessEvent businessEvent,
                                              WorkflowStep previousWorkflowStep,
                                              ActionEnum action,
                                              Date date,
                                              LocalTime time,
                                              String identifierValue) {

        List<EventTimestamp> timestamp = List.of(EventTimestamp.builder()
                .setQualification(EventTimestampQualificationEnum.EVENT_CREATION_DATE_TIME)
                .setDateTime(ZonedDateTime.of(date.toLocalDate(), time, ZoneId.of("GMT"))));

        List<Identifier> identifier = List.of(Identifier.builder()
                .addAssignedIdentifier(AssignedIdentifier.builder().setIdentifierValue(identifierValue)));

        return createWorkflowStep.evaluate(null,
                timestamp,
                identifier,
                getParties(action == ActionEnum.CANCEL ? previousWorkflowStep.getBusinessEvent() : businessEvent),
                List.of(),
                previousWorkflowStep,
                action,
                businessEvent);
    }

    private ReportableEvent setReportableTrade(ReportableEvent reportableEvent, int reportableTradeIndex) {
        TradeState reportableTrade = reportableEvent.getOriginatingWorkflowStep().getBusinessEvent().getAfter().get(reportableTradeIndex);
        return reportableEvent.toBuilder()
                .setReportableTrade(reportableTrade)
                .setReportableInformation(blah(reportableEvent.getReportableInformation(), reportableTrade));
    }

    private ReportableInformation blah(ReportableInformation reportableInformation, TradeState reportableTrade) {
        ReportableInformation.ReportableInformationBuilder reportableInformationBuilder = reportableInformation.build().toBuilder();

        List<ReferenceWithMetaParty> counterpartyPartyReferences = reportableTrade.getTrade()
                .getTradableProduct()
                .getCounterparty().stream()
                .map(Counterparty::getPartyReference)
                .collect(Collectors.toList());

        List<ReferenceWithMetaParty> partyInformationPartyReferences =
                reportableInformationBuilder.getPartyInformation().stream()
                .map(PartyInformation::getPartyReference)
                .collect(Collectors.toList());

        reportableTrade.getTrade()
                .getTradableProduct()
                .getCounterparty().stream()
                .map(Counterparty::getPartyReference)
                .filter(p -> !partyInformationPartyReferences.contains(p))
                .findFirst()
                .ifPresent(stepInPartyReference -> {
                    // replace party that has stepped out
                    reportableInformationBuilder.getPartyInformation().stream()
                            .filter(p -> !counterpartyPartyReferences.contains(p.getPartyReference()))
                            .findFirst()
                            .ifPresent(partyInformationBuilder -> {
                                partyInformationBuilder.setPartyReference(stepInPartyReference);
                                partyInformationBuilder.getRegimeInformation()
                                        .forEach(reportingRegimeBuilder -> {
                                            reportingRegimeBuilder.setReportingRole(ReportingRoleEnum.COUNTERPARTY);
                                        });
                            });
                });

        return reportableInformationBuilder.build();
    }

    private List<Party> getParties(BusinessEvent businessEvent) {
        if (businessEvent == null) {
            return Collections.emptyList();
        }

        Set<? extends Party> afterTradesParties = businessEvent.getAfter().stream()
                .map(TradeState::getTrade)
                .map(Trade::getParty)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<? extends Party> beforeTradesParties = businessEvent.getInstruction().stream()
                .map(Instruction::getBefore)
                .filter(Objects::nonNull)
                .map(ReferenceWithMetaTradeState::getValue)
                .map(TradeState::getTrade)
                .map(Trade::getParty)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<Party> distinctParties = new HashSet<>();
        distinctParties.addAll(afterTradesParties);
        distinctParties.addAll(beforeTradesParties);

        return distinctParties.stream()
                .sorted(Comparator.comparing(p ->
                        Optional.ofNullable(p.getName())
                                .map(FieldWithMetaString::getValue)
                                .orElse("")))
                .collect(Collectors.toList());
    }


    private TradeState getTradeState(WorkflowStep previousWorkflowStep) {
        TradeState.TradeStateBuilder tradeState = previousWorkflowStep.getBusinessEvent().getAfter().get(0).toBuilder();
        // Temporary fix until FilterClosedTradeStates and FilterOpenTradeStates no longer check for positionState
        tradeState.setState(null);
        return tradeState.build();
    }

    private void assertJsonEquals(ReportableEvent actual, String expectedFileName) throws IOException {
        String actualContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual);

        if (writeExpectations) {
            writeExpectation(expectedFileName, actualContent);
        }

        assertEquals(readResource(dataFolder + expectedFileName),
                actualContent,
                "The generated event does not match it's expectation in file " + expectedFileName);
    }

    private void writeExpectation(String expectedFileName, String actualContent) {
        // Add environment variable TEST_WRITE_BASE_PATH to override the base write path, e.g.
        // TEST_WRITE_BASE_PATH=/Users/foo/working-area/digital-regulatory-reporting/rosetta-source/src/main/resources
        Path writePath = ExpectationUtil.TEST_WRITE_BASE_PATH.orElseThrow();
        Path filePath = writePath.resolve("result-json-files/fpml-5-10/record-keeping/events/cftc-event-scenarios").resolve(expectedFileName);
        try {
            Files.write(filePath, actualContent.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readResource(String inputJson) throws IOException {
        //noinspection UnstableApiUsage
        return Resources
                .toString(Objects
                        .requireNonNull(getClass().getResource(inputJson)), StandardCharsets.UTF_8);
    }

    public static <T extends RosettaModelObjectBuilder> T reKey(T builder) {
        GlobalKeyProcessStep globalKeyProcessStep = new GlobalKeyProcessStep(NonNullHashCollector::new);
        List<PostProcessStep> postProcessors = Arrays.asList(globalKeyProcessStep, new ReKeyProcessStep(globalKeyProcessStep));
        postProcessors.forEach(p -> p.runProcessStep(builder.getType(), builder));
        return builder;
    }
}
