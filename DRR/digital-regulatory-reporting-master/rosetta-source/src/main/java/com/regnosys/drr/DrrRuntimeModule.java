package com.regnosys.drr;

import com.regnosys.model.functions.NoOpConditionValidator;
import com.rosetta.model.lib.functions.ConditionValidator;
import drr.enrichment.eic.functions.GetAcceptedEicCodes;
import drr.enrichment.eic.functions.GetAcceptedEicCodesImpl;
import drr.regulation.common.functions.*;
import drr.regulation.common.util.functions.*;
import org.finos.cdm.CdmRuntimeModule;

public class DrrRuntimeModule extends CdmRuntimeModule {

    @Override
    protected void configure() {
        super.configure();
        bind(ConditionValidator.class).to(bindConditionValidator());

        bind(GetCommodityKey.class).to(bindGetCommodityKey());
        bind(GetQuantityKeys.class).to(bindGetQuantityKeys());
        bind(GetQuantityReference.class).to(bindGetQuantityReference());

        bind(StringLength.class).to(bindStringLength());
        bind(SubString.class).to(bindSubString());
        bind(StringContains.class).to(bindStringContains());

        bind(GetAcceptedEicCodes.class).toInstance(bindGetAcceptedEicCodesInstance());
    }

    protected Class<? extends GetCommodityKey> bindGetCommodityKey() {
        return GetCommodityKeyImpl.class;
    }

    protected Class<? extends GetQuantityKeys> bindGetQuantityKeys() {
        return GetQuantityKeysImpl.class;
    }

    protected Class<? extends GetQuantityReference> bindGetQuantityReference() {
        return GetQuantityReferenceImpl.class;
    }

    protected Class<? extends ConditionValidator> bindConditionValidator() {
        return NoOpConditionValidator.class;
    }

    protected Class<? extends StringLength> bindStringLength() {
        return StringLengthImpl.class;
    }

    protected Class<? extends SubString> bindSubString() {
        return SubStringImpl.class;
    }

    protected Class<? extends StringContains> bindStringContains() {
        return StringContainsImpl.class;
    }

    protected GetAcceptedEicCodes bindGetAcceptedEicCodesInstance() {
        return GetAcceptedEicCodesImpl.getInstanceWithPreloadedData();
    }
}
