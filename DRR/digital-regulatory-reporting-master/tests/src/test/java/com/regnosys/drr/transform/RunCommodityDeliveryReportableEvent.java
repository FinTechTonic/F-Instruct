package com.regnosys.drr.transform;

import cdm.base.staticdata.identifier.CommodityLocationIdentifierTypeEnum;
import cdm.base.staticdata.identifier.LocationIdentifier;
import cdm.product.asset.*;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.regnosys.rosetta.common.postprocess.WorkflowPostProcessor;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import drr.regulation.common.ReportableEvent;

import static com.regnosys.drr.transform.TransformUtils.getAssignedIdentifier;
import static com.regnosys.drr.transform.TransformUtils.getValueWithScheme;
import static drr.regulation.common.ReportableEvent.ReportableEventBuilder;

public class RunCommodityDeliveryReportableEvent {

    @Inject
    WorkflowPostProcessor postProcessor;

    public RunCommodityDeliveryReportableEvent() {
        Injector injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
        injector.injectMembers(this);
    }

    public ReportableEvent addDelivery(ReportableEvent reportableEvent) {
        ReportableEventBuilder reportableEventBuilder = reportableEvent.toBuilder();
        CommodityPayout.CommodityPayoutBuilder commodityPayout = getCommodityPayout(reportableEventBuilder);
        commodityPayout
                .setDelivery(AssetDeliveryInformation.builder()
                        .addLocation(LocationIdentifier.builder()
                                .addAssignedIdentifier(getAssignedIdentifier("dummy-location-scheme-1", "0000000000000000"))
                                .setIssuer(getValueWithScheme("DummyIssuer1", "dummy-issuer-scheme"))
                                .setLocationIdentifierType(CommodityLocationIdentifierTypeEnum.DELIVERY_POINT))
                        .addLocation(LocationIdentifier.builder()
                                .addAssignedIdentifier(getAssignedIdentifier("dummy-location-scheme-1", "1111111111111111"))
                                .setIssuer(getValueWithScheme("DummyIssuer1", "dummy-issuer-scheme"))
                                .setLocationIdentifierType(CommodityLocationIdentifierTypeEnum.INTERCONNECTION_POINT))
                        .addLocation(LocationIdentifier.builder()
                                .addAssignedIdentifier(getAssignedIdentifier("dummy-location-scheme-2", "11111AAAAA"))
                                .setIssuer(getValueWithScheme("DummyIssuer2", "dummy-issuer-scheme"))
                                .setLocationIdentifierType(CommodityLocationIdentifierTypeEnum.BUYER_HUB))
                        .addLocation(LocationIdentifier.builder()
                                .addAssignedIdentifier(getAssignedIdentifier("dummy-location-scheme-2", "22222BBBBB"))
                                .setIssuer(getValueWithScheme("DummyIssuer2", "dummy-issuer-scheme"))
                                .setLocationIdentifierType(CommodityLocationIdentifierTypeEnum.SELLER_HUB))
                        .setPeriods(AssetDeliveryPeriods.builder()
                                .addProfile(AssetDeliveryProfile.builder()
                                        .setLoadType(LoadTypeEnum.GAS_DAY))));
        return postProcess(reportableEventBuilder.build());
    }



    private static CommodityPayout.CommodityPayoutBuilder getCommodityPayout(ReportableEventBuilder reportableEventBuilder) {
        return reportableEventBuilder
                .getOriginatingWorkflowStep()
                .getProposedEvent()
                .getInstruction()
                .get(0)
                .getBefore()
                .getValue()
                .getTrade()
                .getTradableProduct()
                .getProduct()
                .getContractualProduct()
                .getEconomicTerms()
                .getPayout()
                .getCommodityPayout()
                .get(0);
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
