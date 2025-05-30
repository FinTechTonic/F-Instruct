package com.regnosys.drr.transform;

import cdm.base.datetime.AdjustableDate;
import cdm.base.datetime.AdjustableOrRelativeDate;
import cdm.base.datetime.Frequency;
import cdm.base.math.Measure;
import cdm.base.math.NonNegativeQuantitySchedule;
import cdm.base.math.UnitType;
import cdm.base.staticdata.asset.common.*;
import cdm.base.staticdata.identifier.AssignedIdentifier;
import cdm.base.staticdata.party.BuyerSeller;
import cdm.base.staticdata.party.Counterparty;
import cdm.base.staticdata.party.CounterpartyRoleEnum;
import cdm.base.staticdata.party.PayerReceiver;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import cdm.event.common.Trade;
import cdm.observable.asset.*;
import cdm.product.common.settlement.SettlementDate;
import cdm.product.common.settlement.SettlementTerms;
import cdm.product.common.settlement.SettlementTypeEnum;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.model.metafields.MetaFields;
import drr.regulation.common.ReportableEvent;

import java.math.BigDecimal;

public class TransformUtils {

    static FieldWithMetaString.FieldWithMetaStringBuilder getValueWithScheme(String value, String scheme) {
        FieldWithMetaString.FieldWithMetaStringBuilder builder =
                FieldWithMetaString.builder().setValue(value);
        if (scheme != null) {
            builder.setMeta(MetaFields.builder()
                    .setScheme(scheme));
        }
        return builder;
    }

    static AssignedIdentifier.AssignedIdentifierBuilder getAssignedIdentifier(String scheme, String value) {
        return AssignedIdentifier.builder()
                .setIdentifier(getValueWithScheme(value, scheme));
    }

    static ProductIdentifier.ProductIdentifierBuilder getProductIdentifier(String value, String scheme, ProductIdTypeEnum source) {
        return ProductIdentifier.builder()
                .setIdentifier(getValueWithScheme(value, scheme))
                .setSource(source);
    }

    static BuyerSeller.BuyerSellerBuilder getBuyerSeller(CounterpartyRoleEnum buyer, CounterpartyRoleEnum seller) {
        return BuyerSeller.builder()
                .setBuyer(buyer)
                .setSeller(seller);
    }

    static SettlementTerms.SettlementTermsBuilder getSettlementTerms(String settlementCurrency, SettlementTypeEnum settlementType, Date settlementDate) {
        return SettlementTerms.builder()
                .setSettlementCurrencyValue(settlementCurrency)
                .setSettlementType(settlementType)
                .setSettlementDate(SettlementDate.builder().setValueDate(settlementDate));
    }

    static NonNegativeQuantitySchedule.NonNegativeQuantityScheduleBuilder getQuantitySchedule(double value, UnitType unit) {
        return NonNegativeQuantitySchedule.builder()
                .setValue(BigDecimal.valueOf(value))
                .setUnit(unit);
    }

    static PriceSchedule.PriceScheduleBuilder getPriceSchedule(double value, UnitType unit, UnitType perUnitOf, PriceTypeEnum priceType, CashPrice cashPrice) {
        return PriceSchedule.builder()
                .setValue(BigDecimal.valueOf(value))
                .setUnit(unit)
                .setPerUnitOf(perUnitOf)
                .setPriceType(priceType)
                .setCashPrice(cashPrice);
    }

    static Price.PriceBuilder getPrice(double value, UnitType unit, UnitType perUnitOf, PriceTypeEnum priceType) {
        return Price.builder()
                .setValue(BigDecimal.valueOf(value))
                .setUnit(unit)
                .setPerUnitOf(perUnitOf)
                .setPriceType(priceType);
    }

    static Loan.LoanBuilder getLoan(String value, String scheme, ProductIdTypeEnum source) {
        return Loan.builder()
                .addProductIdentifierValue(ProductIdentifier.builder()
                        .setIdentifier(FieldWithMetaString.builder()
                                .setMeta(MetaFields.builder()
                                        .setScheme(scheme))
                                .setValue(value))
                        .setSource(source));
    }

    static Commodity.CommodityBuilder getCommodity(String value,
                                                   String scheme,
                                                   ProductIdTypeEnum source,
                                                   String description,
                                                   QuotationSideEnum priceQuoteType) {
        return Commodity.builder()
                .setDescription(description)
                .setPriceQuoteType(priceQuoteType)
                .addProductIdentifierValue(ProductIdentifier.builder()
                        .setIdentifier(FieldWithMetaString.builder()
                                .setMeta(MetaFields.builder()
                                        .setScheme(scheme))
                                .setValue(value))
                        .setSource(source));
    }

    static Index.IndexBuilder getIndex(String value,
                                               String scheme,
                                               ProductIdTypeEnum source) {
        return Index.builder()
                .addProductIdentifierValue(ProductIdentifier.builder()
                        .setIdentifier(FieldWithMetaString.builder()
                                .setMeta(MetaFields.builder()
                                        .setScheme(scheme))
                                .setValue(value))
                        .setSource(source));
    }

    static PayerReceiver.PayerReceiverBuilder getPayerReceiver(CounterpartyRoleEnum payer, CounterpartyRoleEnum receiver) {
        return PayerReceiver.builder()
                .setPayer(payer)
                .setReceiver(receiver);
    }

    static Counterparty.CounterpartyBuilder getCounterparty(String externalReference, CounterpartyRoleEnum role) {
        return Counterparty.builder()
                .setPartyReference(ReferenceWithMetaParty.builder().setExternalReference(externalReference))
                .setRole(role);
    }

    static Trade.TradeBuilder getTrade(ReportableEvent.ReportableEventBuilder reportableEventBuilder) {
        return reportableEventBuilder
                .getOriginatingWorkflowStep()
                .getProposedEvent()
                .getInstruction()
                .get(0)
                .getBefore()
                .getValue()
                .getTrade();
    }

    static AdjustableOrRelativeDate.AdjustableOrRelativeDateBuilder getAdjustedDate(Date adjusteddate) {
        return AdjustableOrRelativeDate.builder()
                .setAdjustableDate(
                        AdjustableDate.builder()
                                .setAdjustedDateValue(adjusteddate));
    }
}
