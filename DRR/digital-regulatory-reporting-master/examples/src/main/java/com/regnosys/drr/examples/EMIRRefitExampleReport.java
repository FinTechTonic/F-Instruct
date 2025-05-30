package com.regnosys.drr.examples;

import cdm.base.staticdata.party.CounterpartyRoleEnum;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapperCreator;
import com.rosetta.model.lib.reports.Tabulator;
import drr.projection.iso20022.esma.emir.refit.trade.functions.Project_EsmaEmirTradeReportToIso20022;
import drr.regulation.common.*;
import drr.regulation.common.functions.Create_RegimeReportableEvent;
import drr.regulation.common.functions.Create_TransactionReportInstruction;
import drr.regulation.common.functions.Create_TransactionReportInstructionForRegime;
import drr.regulation.common.functions.ExtractTradeCounterparty;
import drr.regulation.esma.emir.refit.trade.ESMAEMIRTransactionReport;
import drr.regulation.esma.emir.refit.trade.reports.ESMAEMIRTradeReportFunction;
import drr.regulation.esma.emir.refit.trade.reports.ESMAEMIRTradeReportTabulator;
import iso20022.auth030.esma.Document;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class EMIRRefitExampleReport {

    // From resources folder in dependency:
    //   groupId: org.iso20022
    //   artifactId: rosetta-source
    private static final String XML_CONFIG_PATH = "xml-config/auth030-esma-rosetta-xml-config.json";
    private static Injector injector;

    public static void main(String[] args) throws IOException {
        // 1. Deserialise a ReportableEvent JSON from the test pack
        ReportableEvent exampleInputData = ResourcesUtils.getObjectAndResolveReferences(ReportableEvent.class, "regulatory-reporting/input/events/New-Trade-01.json");

        EMIRRefitExampleReport example = new EMIRRefitExampleReport();

        // Simple example where ReportableEvent is enriched with a ReportingSide and the DRR report is generated.
        example.runSimpleExample(exampleInputData);

        // Example where ReportableEvent is enriched a ReportingSide for all regimes in during an eligibility phase, before the DRR report is run.
        example.runExampleWithEligibilityPhase(exampleInputData);
    }

    EMIRRefitExampleReport() {
        this.injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
    }

    /**
     * Simple example where ReportableEvent is enriched with a ReportingSide and the DRR report is generated.
     */
    void runSimpleExample(ReportableEvent reportableEvent) throws IOException {
        TransactionReportInstruction transactionReportInstruction = createTransactionReportInstruction(reportableEvent);

        ESMAEMIRTransactionReport transactionReport = runReport(transactionReportInstruction);

        String iso20022Xml = runProjectionToIso20022(transactionReport);
        // Print XML
        System.out.println(iso20022Xml);
    }

    /**
     * Example where ReportableEvent is enriched a ReportingSide for all regimes in during an eligibility phase, before the DRR report is run.
     */
    void runExampleWithEligibilityPhase(ReportableEvent reportableEvent) throws IOException {
        RegimeReportableEvent regimeReportableEvent = createRegimeReportableEvent(reportableEvent);

        TransactionReportInstruction transactionReportInstruction =
                createTransactionReportInstructionForRegime(regimeReportableEvent, SupervisoryBodyEnum.ESMA, RegimeNameEnum.EMIR, false);

        ESMAEMIRTransactionReport transactionReport = runReport(transactionReportInstruction);

        String iso20022Xml = runProjectionToIso20022(transactionReport);
        // Print XML
        System.out.println(iso20022Xml);
    }

    /**
     * TransactionReportInstruction from ReportableEvent and ReportingSide
     */
    private TransactionReportInstruction createTransactionReportInstruction(ReportableEvent reportableEvent) {
        // For this example, arbitrarily PARTY_1 as the reporting party and PARTY_2 as the reporting counterparty
        ReferenceWithMetaParty party1 = getCounterparty(reportableEvent, CounterpartyRoleEnum.PARTY_1);
        ReferenceWithMetaParty party2 = getCounterparty(reportableEvent, CounterpartyRoleEnum.PARTY_2);
        final ReportingSide reportingSide = getReportingSide(party1, party2);
        final Create_TransactionReportInstruction createInstructionFunc = injector.getInstance(Create_TransactionReportInstruction.class);
        return createInstructionFunc.evaluate(reportableEvent, reportingSide);
    }


    /**
     * Eligibility phase where ReportableEvent is enriched with reporting side for each regime.
     */
    private RegimeReportableEvent createRegimeReportableEvent(ReportableEvent reportableEvent) {
        // For this example, arbitrarily PARTY_1 as the reporting party and PARTY_2 as the reporting counterparty
        ReferenceWithMetaParty party1 = getCounterparty(reportableEvent, CounterpartyRoleEnum.PARTY_1);
        ReferenceWithMetaParty party2 = getCounterparty(reportableEvent, CounterpartyRoleEnum.PARTY_2);

        // Implementor's eligibility engine adds reporting sides for EMIR, CFTC etc
        RegimeReportingSide emirReportingSide = RegimeReportingSide.builder()
                .setSupervisoryBody(SupervisoryBodyEnum.ESMA)
                .setRegimeName(RegimeNameEnum.EMIR)
                .setReportingSide(getReportingSide(party1, party2))
                .setDelegatedReportingSide(getDelelgatedReportingSide(party2, party1, party1, party2));
        RegimeReportingSide cftcReportingSide = RegimeReportingSide.builder()
                .setSupervisoryBody(SupervisoryBodyEnum.CFTC)
                .setRegimeName(RegimeNameEnum.DODD_FRANK_ACT)
                .setReportingSide(getReportingSide(party1, party2));
        // TODO create RegimeReportingSide for other regimes

        final Create_RegimeReportableEvent createRegimeReportableEventFunc = injector.getInstance(Create_RegimeReportableEvent.class);
        return createRegimeReportableEventFunc.evaluate(reportableEvent, List.of(emirReportingSide, cftcReportingSide));
    }

    private TransactionReportInstruction createTransactionReportInstructionForRegime(RegimeReportableEvent regimeReportableEvent,
                                                                                     SupervisoryBodyEnum supervisoryBody,
                                                                                     RegimeNameEnum regimeName,
                                                                                     boolean delegatedReporting) {
        final Create_TransactionReportInstructionForRegime createInstructionForRegimeFunc = injector.getInstance(Create_TransactionReportInstructionForRegime.class);
        return createInstructionForRegimeFunc.evaluate(regimeReportableEvent, regimeName, supervisoryBody, delegatedReporting);
    }

    private ESMAEMIRTransactionReport runReport(TransactionReportInstruction reportInstruction) throws JsonProcessingException {
        // Get the API reporting function
        final ESMAEMIRTradeReportFunction function = injector.getInstance(ESMAEMIRTradeReportFunction.class);
        // Run the API to produce a CFTCPart45TransactionReport
        final ESMAEMIRTransactionReport report = function.evaluate(reportInstruction);

        // Print object
        System.out.println(RosettaObjectMapper.getNewRosettaObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        // Print tabulated object
        //printTabulatedReport(report);

        return report;
    }

    private String runProjectionToIso20022(ESMAEMIRTransactionReport report) throws IOException {
        // Get the projection function
        Project_EsmaEmirTradeReportToIso20022 iso20022Function = injector.getInstance(Project_EsmaEmirTradeReportToIso20022.class);
        // Run the projection function to product an iso20022.auth030.esma.Document
        Document iso20022Document = iso20022Function.evaluate(report);

        // Serialise iso20022.auth030.esma.Document to XML
        URL iso20022Auth030XmlConfig = Resources.getResource(XML_CONFIG_PATH);

        System.out.println("ISO-20022 auth30 XML mapper config path " + iso20022Auth030XmlConfig);
        ObjectMapper objectMapper = RosettaObjectMapperCreator.forXML(iso20022Auth030XmlConfig.openStream()).create();
        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(iso20022Document);
    }

    private void printTabulatedReport(ESMAEMIRTransactionReport report) throws JsonProcessingException {
        // Get the API tabulator function
        final ESMAEMIRTradeReportTabulator tabulator = injector.getInstance(ESMAEMIRTradeReportTabulator.class);
        // Run the API to extract key value pairs from the report
        final List<Tabulator.FieldValue> tabulatedReport = tabulator.tabulate(report);
        // Print
        System.out.println(RosettaObjectMapper.getNewRosettaObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(tabulatedReport));
    }

    private ReferenceWithMetaParty getCounterparty(ReportableEvent reportableEvent, CounterpartyRoleEnum party) {
        ExtractTradeCounterparty func = injector.getInstance(ExtractTradeCounterparty.class);
        return func.evaluate(reportableEvent, party).getPartyReference();
    }

    private ReportingSide getReportingSide(ReferenceWithMetaParty reportingParty, ReferenceWithMetaParty reportingCounterparty) {
        return ReportingSide.builder()
                .setReportingParty(reportingParty)
                .setReportingCounterparty(reportingCounterparty)
                .build();
    }

    private ReportingSide getDelelgatedReportingSide(ReferenceWithMetaParty reportingParty,
                                                     ReferenceWithMetaParty reportingCounterparty,
                                                     ReferenceWithMetaParty reportSubmittingParty,
                                                     ReferenceWithMetaParty partyResponsibleForReporting) {
        return ReportingSide.builder()
                .setReportingParty(reportingParty)
                .setReportingCounterparty(reportingCounterparty)
                .setReportSubmittingParty(reportSubmittingParty)
                .setPartyResponsibleForReporting(partyResponsibleForReporting)
                .build();
    }
}
