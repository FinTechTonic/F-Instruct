package com.regnosys.drr.transform;

import cdm.base.datetime.BusinessCenterEnum;
import cdm.base.datetime.BusinessCenterTime;
import cdm.base.math.CapacityUnitEnum;
import cdm.base.math.FinancialUnitEnum;
import cdm.base.math.UnitType;
import cdm.base.staticdata.asset.common.*;
import cdm.base.staticdata.asset.common.metafields.ReferenceWithMetaProductIdentifier;
import cdm.base.staticdata.party.Counterparty;
import cdm.base.staticdata.party.CounterpartyRoleEnum;
import cdm.observable.asset.*;
import cdm.product.common.settlement.PriceQuantity;
import cdm.product.common.settlement.SettlementTypeEnum;
import cdm.product.template.*;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.regnosys.rosetta.common.postprocess.WorkflowPostProcessor;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.records.Date;
import drr.regulation.common.ReportableEvent;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static com.regnosys.drr.transform.TransformUtils.*;
import static drr.regulation.common.ReportableEvent.ReportableEventBuilder;

public class TransformEtdHelper {

    @Inject
    WorkflowPostProcessor postProcessor;

    public TransformEtdHelper() {
        Injector injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
        injector.injectMembers(this);
    }

    ReportableEvent getEtd10YearNoteFuture(ReportableEvent reportableEvent) {
        ForwardPayout.ForwardPayoutBuilder forwardPayout = get10YearNoteForwardPayout();
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                130.5,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.VALUE_PER_PERCENT),
                                PriceTypeEnum.CASH_PRICE,
                                null
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                15.0,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.PHYSICAL,
                                Date.of(2019, 9, 19)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addFuture(reportableEvent, getCounterparties(), AssetClassEnum.INTEREST_RATE, "FFXXXX", List.of(), forwardPayout, priceQuantity);
    }

    ReportableEvent getEtd10YearNoteOption(ReportableEvent reportableEvent) {
        List<ReferenceWithMetaProductIdentifier> securityProductIdentifiers = get10YearNoteProductIdentifiers();
        OptionPayout.OptionPayoutBuilder optionPayout = OptionPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setExerciseTerms(
                        OptionExercise.builder()
                                .setOptionStyle(
                                        OptionStyle.builder()
                                                .setAmericanExercise(
                                                        AmericanExercise.builder()
                                                                .setCommencementDate(getAdjustedDate(Date.of(2019, 8, 12)))
                                                                .setExpirationDate(getAdjustedDate(Date.of(2019, 11, 12)))
                                                                .setExpirationTime(
                                                                        BusinessCenterTime.builder()
                                                                                .setHourMinuteTime(LocalTime.of(23, 59, 0))
                                                                                .setBusinessCenterValue(BusinessCenterEnum.NYSE)
                                                                )
                                                )
                                )
                                .setStrike(
                                        OptionStrike.builder()
                                                .setStrikePrice(
                                                        getPrice(
                                                                79.5,
                                                                UnitType.builder().setCurrencyValue("USD"),
                                                                UnitType.builder().setCurrencyValue("BBL"),
                                                                PriceTypeEnum.CASH_PRICE
                                                        )
                                                )
                                )
                )
                .setOptionType(OptionTypeEnum.CALL)
                .setUnderlier(
                        Product.builder()
                                .setSecurity(
                                        Security.builder()
                                                .setSecurityType(SecurityTypeEnum.LISTED_DERIVATIVE)
                                                .setEconomicTerms(
                                                        EconomicTerms.builder()
                                                                .setPayout(
                                                                        Payout.builder()
                                                                                .addForwardPayout(get10YearNoteForwardPayout())
                                                                )
                                                )
                                )
                );
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                3.11,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.INDEX_UNIT),
                                PriceTypeEnum.CASH_PRICE,
                                null
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                48.0,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.CASH,
                                Date.of(2019, 11, 13)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addOption(reportableEvent, getCounterparties(), AssetClassEnum.INTEREST_RATE, "OMXXXX", securityProductIdentifiers, optionPayout, priceQuantity);
    }

    ReportableEvent getEtdCocoaFuture(ReportableEvent reportableEvent) {
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                3728.0,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setCapacityUnit(CapacityUnitEnum.MT),
                                PriceTypeEnum.CASH_PRICE,
                                null
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                4.0,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                10.0,
                                UnitType.builder().setCapacityUnit(CapacityUnitEnum.MT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.CASH,
                                Date.of(2023, 12, 20)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addFuture(reportableEvent, getCounterparties(), AssetClassEnum.COMMODITY, "FCXXXX", getCocoaForwardProductIdentifiers(), getCocoaForwardPayout(), priceQuantity);
    }

    ReportableEvent getEtdCocoaOption(ReportableEvent reportableEvent) {
        OptionPayout.OptionPayoutBuilder optionPayout = OptionPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setExerciseTerms(
                        OptionExercise.builder()
                                .setOptionStyle(
                                        OptionStyle.builder()
                                                .setAmericanExercise(
                                                        AmericanExercise.builder()
                                                                .setCommencementDate(getAdjustedDate(Date.of(2023, 9, 15)))
                                                                .setExpirationDate(getAdjustedDate(Date.of(2023, 12, 25)))
                                                                .setExpirationTime(
                                                                        BusinessCenterTime.builder()
                                                                                .setHourMinuteTime(LocalTime.of(23, 59, 0))
                                                                                .setBusinessCenterValue(BusinessCenterEnum.NYSE)
                                                                )
                                                )
                                )
                                .setStrike(
                                        OptionStrike.builder()
                                                .setStrikePrice(
                                                        getPrice(
                                                                3500.0,
                                                                UnitType.builder().setCurrencyValue("USD"),
                                                                UnitType.builder().setCapacityUnit(CapacityUnitEnum.MT),
                                                                PriceTypeEnum.CASH_PRICE
                                                        )
                                                )
                                )
                )
                .setOptionType(OptionTypeEnum.PUT)
                .setUnderlier(
                        Product.builder()
                                .setSecurity(
                                        Security.builder()
                                                .setSecurityType(SecurityTypeEnum.LISTED_DERIVATIVE)
                                                .setProductIdentifier(getCocoaForwardProductIdentifiers())
                                                .setEconomicTerms(
                                                        EconomicTerms.builder()
                                                                .setPayout(
                                                                        Payout.builder()
                                                                                .addForwardPayout(getCocoaForwardPayout())
                                                                )
                                                )
                                )
                );
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                287.0,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.SHARE),
                                PriceTypeEnum.CASH_PRICE,
                                null
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                8.0,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.CASH,
                                Date.of(2023, 12, 26)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addOption(reportableEvent, getCounterparties(), AssetClassEnum.COMMODITY, "OMXXXX", getCocoaOptionProductIdentifiers(), optionPayout, priceQuantity);
    }

    // 05-10 Reviewed until tradeLot
    ReportableEvent getEtdSecurityFuture(ReportableEvent reportableEvent) {
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                82.62,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setCapacityUnit(CapacityUnitEnum.BBL),
                                PriceTypeEnum.CASH_PRICE,
                                CashPrice.builder().setCashPriceType(CashPriceTypeEnum.FEE)
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                45.0,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.CASH,
                                Date.of(2023, 11, 17)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addFuture(reportableEvent, getCounterparties(), AssetClassEnum.INTEREST_RATE, "FFXXXX", getForwardSecurityProductIdentifiers(), getSecurityForwardPayout(), priceQuantity);
    }

    // 05-10 Finished until tradeLot
    ReportableEvent getEtdSecurityOption(ReportableEvent reportableEvent) {
        OptionPayout.OptionPayoutBuilder optionPayout = OptionPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setExerciseTerms(
                        OptionExercise.builder()
                                .setOptionStyle(
                                        OptionStyle.builder()
                                                .setAmericanExercise(
                                                        AmericanExercise.builder()
                                                                .setCommencementDate(getAdjustedDate(Date.of(2023, 9, 15)))
                                                                .setExpirationDate(getAdjustedDate(Date.of(2023, 11, 15)))
                                                                .setExpirationTime(
                                                                        BusinessCenterTime.builder()
                                                                                .setHourMinuteTime(LocalTime.of(23, 59, 0))
                                                                                .setBusinessCenterValue(BusinessCenterEnum.NYSE)
                                                                )
                                                )
                                )
                                .setStrike(
                                        OptionStrike.builder()
                                                .setStrikePrice(
                                                        getPrice(
                                                                79.5,
                                                                UnitType.builder().setCurrencyValue("USD"),
                                                                UnitType.builder().setCapacityUnit(CapacityUnitEnum.BBL),
                                                                PriceTypeEnum.CASH_PRICE
                                                        )
                                                )
                                )
                )
                .setOptionType(OptionTypeEnum.CALL)
                .setUnderlier(
                        Product.builder()
                                .setSecurity(
                                        Security.builder()
                                                .setSecurityType(SecurityTypeEnum.LISTED_DERIVATIVE)
                                                .setProductIdentifier(getForwardSecurityProductIdentifiers())
                                                .setEconomicTerms(
                                                        EconomicTerms.builder()
                                                                .setPayout(
                                                                        Payout.builder()
                                                                                .addForwardPayout(getSecurityOptionProductForwardPayout())
                                                                )
                                                )
                                )
                );
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                3.11,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setCapacityUnit(CapacityUnitEnum.BBL),
                                PriceTypeEnum.CASH_PRICE,
                                CashPrice.builder().setCashPriceType(CashPriceTypeEnum.FEE)
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                38.0,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.CASH,
                                null
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addOption(reportableEvent, getCounterparties(), AssetClassEnum.INTEREST_RATE, "OMXXXX", getForwardEtdOptionProductIdentifiers(), optionPayout, priceQuantity);
    }

    //5-10 Partially Reviewed
    ReportableEvent getEtdEstrCmeFuture(ReportableEvent reportableEvent) {
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                96.315,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.VALUE_PER_PERCENT),
                                PriceTypeEnum.CASH_PRICE,
                                null
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                51.0,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.CASH,
                                Date.of(2023, 12, 19)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addFuture(reportableEvent, getCounterparties(), AssetClassEnum.MONEY_MARKET, "FFXXXX", null, getEstrForwardPayout(), priceQuantity);
    }

    ReportableEvent getEtdEstrCmeOption(ReportableEvent reportableEvent) {
        OptionPayout.OptionPayoutBuilder optionPayout = OptionPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setExerciseTerms(
                        OptionExercise.builder()
                                .setOptionStyle(
                                        OptionStyle.builder()
                                                .setAmericanExercise(
                                                        AmericanExercise.builder()
                                                                .setCommencementDate(getAdjustedDate(Date.of(2023, 7, 19)))
                                                                .setExpirationDate(getAdjustedDate(Date.of(2023, 9, 19)))
                                                                .setExpirationTime(
                                                                        BusinessCenterTime.builder()
                                                                                .setHourMinuteTime(LocalTime.of(23, 59, 0))
                                                                                .setBusinessCenterValue(BusinessCenterEnum.NYSE)
                                                                )
                                                )
                                )
                                .setStrike(
                                        OptionStrike.builder()
                                                .setStrikePrice(
                                                        getPrice(
                                                                43.5,
                                                                UnitType.builder().setCurrencyValue("USD"),
                                                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.SHARE),
                                                                PriceTypeEnum.CASH_PRICE
                                                        )
                                                )
                                )
                )
                .setOptionType(OptionTypeEnum.CALL)
                .setUnderlier(
                        Product.builder()
                                .setSecurity(
                                        Security.builder()
                                                .setSecurityType(SecurityTypeEnum.LISTED_DERIVATIVE)
                                                .setProductIdentifier(null)
                                                .setEconomicTerms(
                                                        EconomicTerms.builder()
                                                                .setPayout(
                                                                        Payout.builder()
                                                                                .addForwardPayout(getEstrOptionForwardProductIdentifiers())
                                                                )
                                                )
                                )
                );
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                11.95,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.INDEX_UNIT),
                                PriceTypeEnum.CASH_PRICE,
                                null
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                23.0,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.CASH,
                                Date.of(2023, 9, 20)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addOption(reportableEvent, getCounterparties(), AssetClassEnum.MONEY_MARKET, "OMXXXX", getEstrOptionCmeProductIdentifiers(), optionPayout, priceQuantity);
    }

    //5-10 Finished
    ReportableEvent getEtdGoldFuture(ReportableEvent reportableEvent) {
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                1904.3,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setCapacityUnit(CapacityUnitEnum.OZT),
                                PriceTypeEnum.CASH_PRICE,
                                null
                        )
                )
                .addQuantityValue(
                        List.of(
                                getQuantitySchedule(
                                        13.0,
                                        UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                                ),
                                getQuantitySchedule(
                                        100.0,
                                        UnitType.builder().setCapacityUnit(CapacityUnitEnum.OZT)
                                ))// review that
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                null,
                                SettlementTypeEnum.PHYSICAL,
                                Date.of(2023, 9, 20)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addFuture(reportableEvent, getCounterparties(), AssetClassEnum.COMMODITY, "FCXXXX", getGoldForwardProductIdentifiers(), getGoldFutureForwardPayout(), priceQuantity);
    }

    ReportableEvent getEtdGoldOption(ReportableEvent reportableEvent) {
        OptionPayout.OptionPayoutBuilder optionPayout = OptionPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setExerciseTerms(
                        OptionExercise.builder()
                                .setOptionStyle(
                                        OptionStyle.builder()
                                                .setAmericanExercise(
                                                        AmericanExercise.builder()
                                                                .setCommencementDate(getAdjustedDate(Date.of(2023, 7, 10)))
                                                                .setExpirationDate(getAdjustedDate(Date.of(2023, 9, 18)))
                                                                .setExpirationTime(
                                                                        BusinessCenterTime.builder()
                                                                                .setHourMinuteTime(LocalTime.of(23, 59, 0))
                                                                                .setBusinessCenterValue(BusinessCenterEnum.NYSE)
                                                                )
                                                )
                                )
                                .setStrike(
                                        OptionStrike.builder()
                                                .setStrikePrice(
                                                        getPrice(
                                                                1900,
                                                                UnitType.builder().setCurrencyValue("USD"),
                                                                null,
                                                                PriceTypeEnum.CASH_PRICE
                                                        )
                                                )
                                )
                )
                .setOptionType(OptionTypeEnum.CALL)
                .setUnderlier(
                        Product.builder()
                                .setSecurity(
                                        Security.builder()
                                                .setSecurityType(SecurityTypeEnum.LISTED_DERIVATIVE)
                                                .setProductIdentifier(getGoldForwardProductIdentifiers())
                                                .setEconomicTerms(
                                                        EconomicTerms.builder()
                                                                .setPayout(
                                                                        Payout.builder()
                                                                                .addForwardPayout(getGoldOptionForwardProductIdentifiers())
                                                                )
                                                )
                                )
                );
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        getPriceSchedule(
                                46,
                                UnitType.builder().setCurrencyValue("USD"),
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.SHARE),
                                PriceTypeEnum.CASH_PRICE,
                                null
                        )
                )
                .addQuantityValue(
                        getQuantitySchedule(
                                14,
                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "USD",
                                SettlementTypeEnum.CASH,
                                Date.of(2023, 9, 26)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addOption(reportableEvent, getCounterparties(), AssetClassEnum.COMMODITY, "OMXXXX", getGoldForwardProductIdentifiers2(), optionPayout, priceQuantity);
    }

    //TODO:to be revisited 06-10, finished, to be reviewed
    ReportableEvent getSocGenEquityListedFuture(ReportableEvent reportableEvent) {
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        List.of(
                                getPriceSchedule(
                                        33320,
                                        UnitType.builder().setCurrencyValue("JPY"),
                                        UnitType.builder().setFinancialUnit(FinancialUnitEnum.INDEX_UNIT),
                                        PriceTypeEnum.CASH_PRICE,
                                        null
                                ),
                                getPriceSchedule(
                                        16660000,
                                        null,
                                        UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT),
                                        PriceTypeEnum.CASH_PRICE,
                                        null
                                )

                        )
                )
                .addQuantityValue(
                        List.of(
                                getQuantitySchedule(
                                        3,
                                        UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                                ),
                                getQuantitySchedule(
                                        49980000,
                                        UnitType.builder().setCurrencyValue("JPY")
                                ))// review that
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "JPY",
                                SettlementTypeEnum.CASH,
                                Date.of(2023, 9, 11)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addFuture(reportableEvent, getCounterparties(), AssetClassEnum.EQUITY, "FFXXXX", getEquityListedForwardProductIdentifiers(), getEquityListFutureForwardPayout(), priceQuantity);
    }

    //TODO: to be revisited 06-10
    ReportableEvent getSocGenEquityListedOption(ReportableEvent reportableEvent) {
        OptionPayout.OptionPayoutBuilder optionPayout = OptionPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setExerciseTerms(
                        OptionExercise.builder()
                                .setOptionStyle(
                                        OptionStyle.builder()
                                                .setAmericanExercise(
                                                        AmericanExercise.builder()
                                                                .setCommencementDate(getAdjustedDate(Date.of(2023, 9, 15)))
                                                                .setExpirationDate(getAdjustedDate(Date.of(2023, 9, 23)))
                                                                .setExpirationTime(
                                                                        BusinessCenterTime.builder()
                                                                                .setHourMinuteTime(LocalTime.of(23, 59, 0))
                                                                                .setBusinessCenterValue(BusinessCenterEnum.NYSE)
                                                                )
                                                                .setExpirationTimeType(ExpirationTimeTypeEnum.CLOSE)
                                                )
                                )
                                .setStrike(
                                        OptionStrike.builder()
                                                .setStrikePrice(
                                                        getPrice(
                                                                11.92,
                                                                UnitType.builder().setCurrencyValue("EUR"),
                                                                UnitType.builder().setFinancialUnit(FinancialUnitEnum.SHARE),
                                                                PriceTypeEnum.CASH_PRICE
                                                        )
                                                )
                                )
                )
                .setOptionType(OptionTypeEnum.PUT)
                .setUnderlier(
                        Product.builder()
                                .setSecurity(
                                        Security.builder()
                                                .setSecurityType(SecurityTypeEnum.EQUITY)
                                                .setEquityType(EquityTypeEnum.ORDINARY)
                                                .setProductIdentifier(getEquityOptionProductIdentifiers())
                                )
                );
        PriceQuantity.PriceQuantityBuilder priceQuantity = PriceQuantity.builder()
                .addPriceValue(
                        List.of(
                                getPriceSchedule(
                                        36.36,
                                        UnitType.builder().setCurrencyValue("EUR"),
                                        UnitType.builder().setFinancialUnit(FinancialUnitEnum.INDEX_UNIT),
                                        PriceTypeEnum.CASH_PRICE,
                                        CashPrice.builder()
                                                .setCashPriceType(CashPriceTypeEnum.PREMIUM)
                                                .setPremiumExpression(
                                                        PremiumExpression.builder()
                                                                .setPricePerOption(
                                                                        Money.builder()
                                                                                .setValue(BigDecimal.valueOf(0.36))
                                                                                .setUnit(UnitType.builder().setCurrencyValue("EUR"))
                                                                )
                                                )
                                ),
                                getPriceSchedule(
                                        101,
                                        null,
                                        UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT),
                                        PriceTypeEnum.CASH_PRICE,
                                        null
                                )
                        )
                )
                .addQuantityValue(
                        List.of(
                                getQuantitySchedule(
                                        3,
                                        UnitType.builder().setFinancialUnit(FinancialUnitEnum.CONTRACT)
                                ),
                                getQuantitySchedule(
                                        109.08,
                                        UnitType.builder().setCurrencyValue("EUR")
                                )
                        )
                )
                .setSettlementTerms(
                        getSettlementTerms(
                                "EUR",
                                SettlementTypeEnum.PHYSICAL,
                                Date.of(2023, 9, 18)
                        )
                )
                .setBuyerSeller(getBuyerSeller(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2));

        return addOption(reportableEvent, getCounterparties(), AssetClassEnum.EQUITY, "OMXXXX", getEquityOptionForwardProductIdentifiers(), optionPayout, priceQuantity);
    }

    private ForwardPayout.ForwardPayoutBuilder get10YearNoteForwardPayout() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setLoan(getLoan("ZNU9",
                        "https://www.cmegroup.com/markets/interest-rates/us-treasury/10-year-us-treasury-note.contractSpecs.html",
                        ProductIdTypeEnum.OTHER));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("U19");
    }

    private List<ReferenceWithMetaProductIdentifier> get10YearNoteProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("CL1:COM", "https://www.bloomberg.com/energy", ProductIdTypeEnum.OTHER)).build(),
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("WTI Crude Futures", null, ProductIdTypeEnum.NAME)).build()
        );
    }

    private ForwardPayout.ForwardPayoutBuilder getSecurityOptionProductForwardPayout() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setCommodityValue(getCommodity(null,
                        null,
                        null,
                        "WTI Crude",
                        null));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("Z23");
    }

    private ForwardPayout.ForwardPayoutBuilder getCocoaForwardPayout() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setCommodityValue(getCommodity("CJ",
                        "https://www.cmegroup.com/markets/interest-rates/stirs/euro-short-term-rate.contractSpecs.html",
                        ProductIdTypeEnum.BBGTICKER,
                        "Cocoa Contract",
                        QuotationSideEnum.CLOSING));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("Z23");
    }

    private ForwardPayout.ForwardPayoutBuilder getSecurityForwardPayout() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setCommodityValue(getCommodity(null,
                        null,
                        null,
                        "WTI Crude",
                        null));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("Z23");
    }

    private ForwardPayout.ForwardPayoutBuilder getEstrForwardPayout() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setIndex(getIndex("ESR",
                        "https://www.cmegroup.com/markets/interest-rates/stirs/euro-short-term-rate.contractSpecs.html",
                        ProductIdTypeEnum.BBGTICKER));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("Z23");
    }

    private ForwardPayout.ForwardPayoutBuilder getGoldFutureForwardPayout() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setCommodityValue(getCommodity("GC",
                        "https://www.cmegroup.com/markets/interest-rates/stirs/euro-short-term-rate.contractSpecs.html",
                        ProductIdTypeEnum.BBGTICKER,
                        "Gold Contract",
                        QuotationSideEnum.CLOSING));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("U23");
    }

    private ForwardPayout.ForwardPayoutBuilder getEquityListFutureForwardPayout() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setIndex(getIndex(".N225",
                        null,
                        ProductIdTypeEnum.RIC));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("U23");
    }

    private ForwardPayout.ForwardPayoutBuilder getEstrOptionForwardProductIdentifiers() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setIndex(getIndex("ESR",
                        "https://www.cmegroup.com/markets/interest-rates/stirs/euro-short-term-rate.contractSpecs.html",
                        ProductIdTypeEnum.BBGTICKER));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("Z23");
    }

    private ForwardPayout.ForwardPayoutBuilder getGoldOptionForwardProductIdentifiers() {
        Product.ProductBuilder forwardUnderlier = Product.builder()
                .setCommodityValue(getCommodity("GC",
                        "https://www.cmegroup.com/markets/interest-rates/stirs/euro-short-term-rate.contractSpecs.html",
                        ProductIdTypeEnum.BBGTICKER,
                        "Gold Contract",
                        QuotationSideEnum.CLOSING));
        return ForwardPayout.builder()
                .setPayerReceiver(getPayerReceiver(CounterpartyRoleEnum.PARTY_1, CounterpartyRoleEnum.PARTY_2))
                .setUnderlier(forwardUnderlier)
                .setDeliveryTerm("U23");
    }

    private List<ReferenceWithMetaProductIdentifier> getCocoaForwardProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("CJZ3", "https://www.bloomberg.com/energy", ProductIdTypeEnum.NAME)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getForwardSecurityProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("CL1:COM", "https://www.bloomberg.com/energy", ProductIdTypeEnum.BBGTICKER)).build(),
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("CLZ23", null, ProductIdTypeEnum.NAME)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getGoldForwardProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("GCU3", "https://www.bloomberg.com/energy", ProductIdTypeEnum.NAME)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getGoldForwardProductIdentifiers2() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("GC1:COM", "https://www.bloomberg.com/quote/BCOMCC:IND#xj4y7vzkg", ProductIdTypeEnum.BBGTICKER)).build(),
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("GCU23", null, ProductIdTypeEnum.NAME)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getEquityOptionForwardProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("SSIU3", null, ProductIdTypeEnum.RIC)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getEquityOptionProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("INGA.AS", null, ProductIdTypeEnum.RIC)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getEquityForwardProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("INGA.AS", null, ProductIdTypeEnum.RIC)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getEquityListedForwardProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("SSIU3", null, ProductIdTypeEnum.RIC)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getForwardEtdOptionProductIdentifiers() { //review that
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("CL1:COM", "https://www.bloomberg.com/energy", ProductIdTypeEnum.BBGTICKER)).build(),
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("WTI Crude Futures", null, ProductIdTypeEnum.NAME)).build()
        );
    }

    private List<ReferenceWithMetaProductIdentifier> getCocoaOptionProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("BCOMCC:IND", "https://www.bloomberg.com/quote/BCOMCC:IND#xj4y7vzkg", ProductIdTypeEnum.BBGTICKER)).build(),
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("CCZ23", null, ProductIdTypeEnum.NAME)).build()
        );
    }


    private List<ReferenceWithMetaProductIdentifier> getEstrOptionCmeProductIdentifiers() {
        return List.of(
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("CL1:COM", "https://www.bloomberg.com/energy", ProductIdTypeEnum.BBGTICKER)).build(),
                ReferenceWithMetaProductIdentifier.builder()
                        .setValue(getProductIdentifier("WTI Crude Futures", null, ProductIdTypeEnum.NAME)).build()
        );
    }

    private List<Counterparty.CounterpartyBuilder> getCounterparties() {
        return List.of(
                getCounterparty("party1", CounterpartyRoleEnum.PARTY_1),
                getCounterparty("party2", CounterpartyRoleEnum.PARTY_2)
        );
    }

    private ReportableEvent addFuture(ReportableEvent reportableEvent,
                                      List<Counterparty.CounterpartyBuilder> counterparties,
                                      AssetClassEnum primaryAssetClass,
                                      String cfi,
                                      List<? extends ReferenceWithMetaProductIdentifier> securityProductIdentifiers,
                                      ForwardPayout.ForwardPayoutBuilder forwardPayout,
                                      PriceQuantity.PriceQuantityBuilder priceQuantity) {
        ReportableEventBuilder reportableEventBuilder = reportableEvent.toBuilder();
        TradableProduct.TradableProductBuilder tradableProductBuilder =
                getTrade(reportableEventBuilder).getOrCreateTradableProduct();
        tradableProductBuilder.setCounterparty(counterparties);
        tradableProductBuilder
                .getOrCreateProduct()
                .getOrCreateSecurity()
                .addProductTaxonomy(ProductTaxonomy.builder().setPrimaryAssetClassValue(primaryAssetClass))
                .addProductTaxonomy(ProductTaxonomy.builder()
                        .setSource(TaxonomySourceEnum.CFI)
                        .setValue(TaxonomyValue.builder()
                                .setNameValue(cfi)))
                .setSecurityType(SecurityTypeEnum.LISTED_DERIVATIVE)
                .setProductIdentifier(securityProductIdentifiers)
                .getOrCreateEconomicTerms()
                .getOrCreatePayout()
                .addForwardPayout(forwardPayout);
        tradableProductBuilder.addTradeLot(TradeLot.builder().addPriceQuantity(priceQuantity));
        return postProcess(reportableEventBuilder.build());
    }

    private ReportableEvent addOption(ReportableEvent reportableEvent,
                                      List<Counterparty.CounterpartyBuilder> counterparties,
                                      AssetClassEnum primaryAssetClass,
                                      String cfi,
                                      List<? extends ReferenceWithMetaProductIdentifier> securityProductIdentifiers,
                                      OptionPayout.OptionPayoutBuilder optionPayout,
                                      PriceQuantity.PriceQuantityBuilder priceQuantity) {
        ReportableEventBuilder reportableEventBuilder = reportableEvent.toBuilder();
        TradableProduct.TradableProductBuilder tradableProductBuilder =
                getTrade(reportableEventBuilder).getOrCreateTradableProduct();
        tradableProductBuilder.setCounterparty(counterparties);
        Security.SecurityBuilder securityBuilder = tradableProductBuilder
                .getOrCreateProduct()
                .getOrCreateSecurity()
                .addProductTaxonomy(ProductTaxonomy.builder().setPrimaryAssetClassValue(primaryAssetClass))
                .addProductTaxonomy(ProductTaxonomy.builder()
                        .setSource(TaxonomySourceEnum.CFI)
                        .setValue(TaxonomyValue.builder()
                                .setNameValue(cfi)))
                .setSecurityType(SecurityTypeEnum.LISTED_DERIVATIVE)
                .setProductIdentifier(securityProductIdentifiers);
        securityBuilder
                .getOrCreateEconomicTerms()
                .getOrCreatePayout()
                .addOptionPayout(optionPayout);
        tradableProductBuilder.addTradeLot(TradeLot.builder().addPriceQuantity(priceQuantity));
        return postProcess(reportableEventBuilder.build());
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
