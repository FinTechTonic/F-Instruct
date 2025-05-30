package com.regnosys.drr.transform;

import cdm.base.staticdata.party.Party;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.rosetta.model.metafields.MetaFields;
import drr.regulation.common.*;
import drr.regulation.common.functions.Create_CollateralReportInstruction;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TransformCollateralHelper {

    @Inject
    Create_CollateralReportInstruction createFunc;

    public TransformCollateralHelper() {
        Injector injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
        injector.injectMembers(this);
    }

    @NotNull
    public CollateralReportInstruction createCollateralReportInstruction(ReportableCollateral reportableCollateral) {
        List<? extends PartyInformation> partyInfoList = getPartyInfoList(reportableCollateral);
        Party reportingParty = partyInfoList.get(0).getPartyReference().getValue();
        Party reportingCounterparty = partyInfoList.get(1).getPartyReference().getValue();
        ReportingSide.ReportingSideBuilder reportingSide =
                ReportingSide.builder()
                        .setReportingParty(asReference(reportingParty))
                        .setReportingCounterparty(asReference(reportingCounterparty));
        return createFunc.evaluate(reportableCollateral, reportingSide);
    }

    private static ReferenceWithMetaParty asReference(Party party) {
        MetaFields meta = party.getMeta();
        return ReferenceWithMetaParty.builder()
                .setExternalReference(meta.getExternalKey())
                .setGlobalReference(meta.getGlobalKey());
    }

    private List<? extends PartyInformation> getPartyInfoList(ReportableCollateral reportableCollateral) {
        return Optional.ofNullable(reportableCollateral.getReportableInformation())
                .map(ReportableInformation::getPartyInformation)
                .orElse(Collections.emptyList());
    }
}
