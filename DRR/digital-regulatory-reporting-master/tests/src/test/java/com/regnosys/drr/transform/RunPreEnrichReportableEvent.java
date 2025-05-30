package com.regnosys.drr.transform;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import drr.enrichment.common.EnrichmentData;
import drr.enrichment.lei.functions.API_GetLeiData;
import drr.regulation.common.PartyInformation;
import drr.regulation.common.ReportableEvent;
import drr.regulation.common.ReportingRegime;
import drr.standards.iso.functions.API_GetMicData;

import java.util.List;

import static drr.enrichment.common.EnrichmentData.EnrichmentDataBuilder;
import static drr.regulation.common.ReportableEvent.ReportableEventBuilder;

public class RunPreEnrichReportableEvent {

    @Inject
    private API_GetMicData micDataApi;

    @Inject
    private API_GetLeiData leiDataApi;

    public RunPreEnrichReportableEvent() {
        Injector injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
        injector.injectMembers(this);
    }

    public ReportableEvent preEnrichReportableEvent(ReportableEvent reportableEvent) {
        EnrichmentDataBuilder enrichmentBuilder =
                EnrichmentData.builder()
                        .addLeiData(leiDataApi.evaluate("5493001RKR55V4X61F71"))
                        .addMicData(micDataApi.evaluate("XNAS"));
        ReportableEventBuilder reportableEventBuilder = reportableEvent.toBuilder();

        for (PartyInformation.PartyInformationBuilder partyInformation : reportableEventBuilder.getOrCreateReportableInformation().getPartyInformation()) {
            for (ReportingRegime.ReportingRegimeBuilder regimeInformation : partyInformation.getRegimeInformation()) {
                // Check if regimeName equals "ASIC"
                if (regimeInformation.getRegimeName() != null && "ASIC".equals(regimeInformation.getRegimeName().getValue().toString())) {
                    // Set nonReportedTradePortfolio to true for ASIC party information
                    regimeInformation.getOrCreateAsicPartyInformation().setSmallScaleBuySideIndicator(true);
                }
            }
        }

        reportableEventBuilder.getOrCreateReportableInformation().setEnrichment(enrichmentBuilder);
        return reportableEventBuilder.build();
    }

}
