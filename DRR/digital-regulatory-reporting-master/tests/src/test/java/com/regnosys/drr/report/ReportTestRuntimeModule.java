package com.regnosys.drr.report;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.regnosys.drr.DrrRuntimeModuleTesting;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.testing.RosettaTestingInjectorProvider;
import com.regnosys.testing.RosettaTestingModule;
import drr.enrichment.lei.functions.API_GetLeiData;
import drr.enrichment.lei.functions.API_GetLeiDataImpl;

public class ReportTestRuntimeModule extends DrrRuntimeModuleTesting {

    @Override
    protected void configure() {
        super.configure();
        install(new RosettaTestingModule());
    }

    @Override
    protected API_GetLeiData bindApiGetLeiDataInstance() {
        return new API_GetLeiDataImpl(LeiDataCacheHelper.getPreloadCacheData());
    }


    public static class InjectorProvider extends RosettaTestingInjectorProvider {
        @Override
        protected Injector internalCreateInjector() {
            return new RosettaStandaloneSetup() {
                @Override
                public Injector createInjector() {
                    return Guice.createInjector(Modules.override(createRuntimeModule()).with(new ReportTestRuntimeModule()));
                }
            }.createInjectorAndDoEMFRegistration();
        }
    }
}
