package com.regnosys.drr.transform;

import cdm.base.staticdata.party.Counterparty;
import cdm.base.staticdata.party.CounterpartyRoleEnum;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import cdm.event.common.Trade;
import cdm.event.common.TradeState;
import cdm.product.template.TradableProduct;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.rosetta.model.metafields.FieldWithMetaString;
import drr.regulation.common.*;
import drr.regulation.common.functions.Create_TransactionReportInstruction;
import drr.regulation.common.functions.Create_TransactionReportInstructionFromInstruction;
import drr.regulation.common.functions.TradeForEvent;
import drr.regulation.common.metafields.FieldWithMetaSupervisoryBodyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rosetta.util.CollectionUtils.emptyIfNull;

public class TransformTransactionHelper {

    private static final List<SupervisoryBodyEnum> TECHNICAL_RECORD_ID_REGIMES = List.of(SupervisoryBodyEnum.ASIC, SupervisoryBodyEnum.JFSA, SupervisoryBodyEnum.MAS);

    private final Logger LOGGER = LoggerFactory.getLogger(TransformTransactionHelper.class);

    @Inject
    private Create_TransactionReportInstructionFromInstruction createTransactionReportInstructionFromInstruction;

    @Inject
    private Create_TransactionReportInstruction createTransactionReportInstruction;

    @Inject
    private TradeForEvent tradeForEventFunc;

    public TransformTransactionHelper() {
        Injector injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
        injector.injectMembers(this);
    }

    public void setTechnicalRecordId(ReportableEvent.ReportableEventBuilder reportableEventBuilder) {
        FieldWithMetaString messageId = reportableEventBuilder.getOriginatingWorkflowStep().getOrCreateMessageInformation().getMessageId();
        emptyIfNull(reportableEventBuilder.getReportableInformation().getPartyInformation()).stream()
                .map(PartyInformation.PartyInformationBuilder::getRegimeInformation)
                .flatMap(Collection::stream)
                .filter(TransformTransactionHelper::regimeRequiresTechnicalRecordId)
                .forEach(reportingRegimeBuilder -> reportingRegimeBuilder.setTechnicalRecordId(messageId));
    }

    private static Boolean regimeRequiresTechnicalRecordId(ReportingRegime.ReportingRegimeBuilder reportingRegimeBuilder) {
        return Optional.ofNullable(reportingRegimeBuilder.getSupervisoryBody())
                .map(FieldWithMetaSupervisoryBodyEnum::getValue)
                .map(TECHNICAL_RECORD_ID_REGIMES::contains)
                .orElse(false);
    }

    public TransactionReportInstruction createTransactionReportInstructionSelf(ReportableEvent reportableEvent) {
        CounterpartyRoleEnum reportingParty = getDefaultReportingParty(reportableEvent);
        CounterpartyRoleEnum reportingCounterparty = getReportingCounterparty(reportingParty);
        return createTransactionReportInstructionFromInstruction.evaluate(reportableEvent, reportingParty, reportingCounterparty, null, null).build();
    }

    public TransactionReportInstruction createTransactionReportInstructionSelfOther(ReportableEvent reportableEvent) {
        CounterpartyRoleEnum reportingParty = getDefaultReportingParty(reportableEvent);
        CounterpartyRoleEnum reportingCounterparty = getReportingCounterparty(reportingParty);
        return createTransactionReportInstructionFromInstruction.evaluate(reportableEvent, reportingCounterparty, reportingParty, null, null).build();
    }

    public TransactionReportInstruction createTransactionReportInstruction(ReportableEvent reportableEvent, RegimeNameEnum regimeName, ReportingRoleEnum reportingRole) {
        CounterpartyRoleEnum reportingParty = getReportingParty(reportableEvent, regimeName, reportingRole);
        CounterpartyRoleEnum reportingCounterparty = getReportingCounterparty(reportingParty);
        return createTransactionReportInstructionFromInstruction.evaluate(reportableEvent, reportingParty, reportingCounterparty, null, null).build();
    }

    public TransactionReportInstruction createTransactionReportInstructionMandatoryDelegated(ReportableEvent.ReportableEventBuilder reportableEvent) {
        CounterpartyRoleEnum reportingParty = getDefaultReportingParty(reportableEvent);
        CounterpartyRoleEnum reportingCounterparty = getReportingCounterparty(reportingParty);
        return createTransactionReportInstructionFromInstruction.evaluate(reportableEvent, reportingCounterparty, reportingParty, reportingParty, reportingParty).build();
    }

    public TransactionReportInstruction createTransactionReportInstructionVoluntaryDelegated(ReportableEvent.ReportableEventBuilder reportableEvent) {
        CounterpartyRoleEnum reportingParty = getDefaultReportingParty(reportableEvent);
        CounterpartyRoleEnum reportingCounterparty = getReportingCounterparty(reportingParty);
        return createTransactionReportInstructionFromInstruction.evaluate(reportableEvent, reportingCounterparty, reportingParty, reportingCounterparty, reportingParty).build();
    }

    public TransactionReportInstruction createTransactionReportInstructionWithMultipleAfterTrades(ReportableEvent.ReportableEventBuilder reportableEvent, int reportableTradeIndex) {
        TradeState reportableTrade = reportableEvent.getOriginatingWorkflowStep().getBusinessEvent().getAfter().get(reportableTradeIndex);
        reportableEvent.setReportableTrade(reportableTrade);
        CounterpartyRoleEnum reportingParty = getDefaultReportingParty(reportableEvent);
        CounterpartyRoleEnum reportingCounterparty = getReportingCounterparty(reportingParty);
        return createTransactionReportInstructionFromInstruction.evaluate(reportableEvent, reportingParty, reportingCounterparty, null, null).build();
    }

    private CounterpartyRoleEnum getDefaultReportingParty(ReportableEvent reportableEvent) {
        return getCounterpartyWithReportingPartyRole(reportableEvent, RegimeNameEnum.EMIR, ReportingRoleEnum.REPORTING_PARTY).orElseThrow();
    }

    private CounterpartyRoleEnum getReportingParty(ReportableEvent reportableEvent, RegimeNameEnum regimeName, ReportingRoleEnum reportingRole) {
        return getCounterpartyWithReportingPartyRole(reportableEvent, regimeName, reportingRole).orElseThrow();
    }


    private CounterpartyRoleEnum getReportingCounterparty(CounterpartyRoleEnum reportingParty) {
        return reportingParty == CounterpartyRoleEnum.PARTY_1 ? CounterpartyRoleEnum.PARTY_2 : CounterpartyRoleEnum.PARTY_1;
    }

    private Optional<CounterpartyRoleEnum> getCounterpartyWithReportingPartyRole(ReportableEvent reportableEvent, RegimeNameEnum regimeName, ReportingRoleEnum reportingRole) {
        Optional<? extends Counterparty> reportingParty = getTradeCounterparties(reportableEvent).stream()
                .filter(counterparty -> isReportingParty(reportableEvent, counterparty.getPartyReference(), regimeName, reportingRole))
                .findFirst();
        reportingParty.ifPresent(cpty -> LOGGER.info("Using ReportingParty externalRef={} role={}", getExternalRef(cpty.getPartyReference()), cpty.getRole()));
        return reportingParty.map(Counterparty::getRole);
    }

    private boolean isReportingParty(ReportableEvent reportableEvent, ReferenceWithMetaParty party, RegimeNameEnum regimeName, ReportingRoleEnum reportingRole) {
        List<? extends PartyInformation> partyInfoList = getPartyInfoList(reportableEvent);
        return partyInfoList.stream()
                .anyMatch(partyInformation -> party.equals(partyInformation.getPartyReference())
                        && partyInformation.getRegimeInformation().stream()
                        .filter(regime ->
                                Optional.ofNullable(regime.getRegimeName()).map(name -> name.getValue() == regimeName).orElse(true))
                        .anyMatch(regime -> regime.getReportingRole() == reportingRole));
    }

    public TransactionReportInstruction createTransactionReportInstructionNoCounterparties(ReportableEvent reportableEvent) {
        List<? extends ReferenceWithMetaParty> parties = getPartyInfoList(reportableEvent).stream()
                .map(PartyInformation::getPartyReference)
                .collect(Collectors.toList());
        if (parties.size() < 2) {
            throw new IllegalStateException("ReportableEvent is missing PartyInformation for each party");
        }
        ReportingSide.ReportingSideBuilder reportingSide = ReportingSide.builder()
                .setReportingParty(parties.get(0))
                .setReportingCounterparty(parties.get(1));
        return createTransactionReportInstruction.evaluate(reportableEvent, reportingSide).build();
    }

    private List<? extends PartyInformation> getPartyInfoList(ReportableEvent reportableEvent) {
        return Optional.ofNullable(reportableEvent.getReportableInformation())
                .map(ReportableInformation::getPartyInformation)
                .orElse(Collections.emptyList());
    }

    private List<? extends Counterparty> getTradeCounterparties(ReportableEvent reportableEvent) {
        Trade trade = tradeForEventFunc.evaluate(reportableEvent);
        return Optional.ofNullable(trade)
                .map(Trade::getTradableProduct)
                .map(TradableProduct::getCounterparty)
                .orElse(Collections.emptyList());
    }

    private String getExternalRef(ReferenceWithMetaParty party) {
        return Optional.ofNullable(party).map(ReferenceWithMetaParty::getExternalReference).orElse(null);
    }
}
