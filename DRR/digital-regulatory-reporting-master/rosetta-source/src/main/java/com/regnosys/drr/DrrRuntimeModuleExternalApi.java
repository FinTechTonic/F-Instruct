package com.regnosys.drr;

import drr.enrichment.lei.functions.API_GetLeiData;
import drr.enrichment.lei.functions.API_GetLeiDataImpl;
import drr.standards.iso.functions.API_GetMicData;
import drr.standards.iso.functions.API_GetMicDataImpl;

/**
 * Binds any external API calls.
 */
public class DrrRuntimeModuleExternalApi extends DrrRuntimeModule {

    @Override
    protected void configure() {
        super.configure();
        bind(API_GetLeiData.class).toInstance(bindApiGetLeiDataInstance());
        bind(API_GetMicData.class).to(bindAPIGetMicData()).asEagerSingleton();
    }

    protected API_GetLeiData bindApiGetLeiDataInstance() {
        return new API_GetLeiDataImpl();
    }

    protected Class<? extends API_GetMicData> bindAPIGetMicData() {
        return API_GetMicDataImpl.class;
    }
}
