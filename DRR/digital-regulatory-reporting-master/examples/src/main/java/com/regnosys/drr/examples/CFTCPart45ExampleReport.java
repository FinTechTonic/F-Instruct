package com.regnosys.drr.examples;

import cdm.base.staticdata.party.CounterpartyRoleEnum;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.regnosys.drr.examples.util.ResourcesUtils;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.rosetta.model.lib.reports.Tabulator;
import drr.regulation.cftc.rewrite.CFTCPart45TransactionReport;
import drr.regulation.cftc.rewrite.reports.CFTCPart45ReportFunction;
import drr.regulation.cftc.rewrite.reports.CFTCPart45ReportTabulator;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingSide;
import drr.regulation.common.TransactionReportInstruction;
import drr.regulation.common.functions.Create_TransactionReportInstruction;
import drr.regulation.common.functions.ExtractTradeCounterparty;

import java.io.IOException;
import java.util.List;

public class CFTCPart45ExampleReport {

    public static void main(String[] args) throws IOException {
        // 1. Deserialise a ReportableEvent JSON from the test pack
        ReportableEvent reportableEvent = ResourcesUtils.getObjectAndResolveReferences(ReportableEvent.class, "regulatory-reporting/input/events/New-Trade-01.json");

        // Run report
        CFTCPart45ExampleReport cftcPart45ExampleReport = new CFTCPart45ExampleReport();
        cftcPart45ExampleReport.runReport(reportableEvent);
    }

    private final Injector injector;

    CFTCPart45ExampleReport() {
        this.injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
    }

    void runReport(ReportableEvent reportableEvent) throws IOException {
        // TransactionReportInstruction from ReportableEvent and ReportingSide
        // For this example, arbitrarily PARTY_1 as the reporting party and PARTY_2 as the reporting counterparty
        final ReportingSide reportingSide = ReportingSide.builder()
                .setReportingParty(getCounterparty(reportableEvent, CounterpartyRoleEnum.PARTY_1))
                .setReportingCounterparty(getCounterparty(reportableEvent, CounterpartyRoleEnum.PARTY_2))
                .build();
        final Create_TransactionReportInstruction createInstructionFunc = injector.getInstance(Create_TransactionReportInstruction.class);
        final TransactionReportInstruction reportInstruction = createInstructionFunc.evaluate(reportableEvent, reportingSide);

        // Run the API to produce a CFTCPart45TransactionReport
        final CFTCPart45ReportFunction reportFunc = injector.getInstance(CFTCPart45ReportFunction.class);
        final CFTCPart45TransactionReport report = reportFunc.evaluate(reportInstruction);
        // Print
        System.out.println(RosettaObjectMapper.getNewRosettaObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report));

        // Get the API tabulator function
        final CFTCPart45ReportTabulator tabulator = injector.getInstance(CFTCPart45ReportTabulator.class);
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
}
