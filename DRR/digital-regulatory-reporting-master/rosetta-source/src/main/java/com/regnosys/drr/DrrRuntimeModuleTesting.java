package com.regnosys.drr;

import cdm.base.datetime.functions.Now;
import com.regnosys.rosetta.common.model.FunctionMemoisingModuleBuilder;
import com.rosetta.model.lib.ModuleConfig;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Runtime module that overrides the Now function to return a fixed date time which can be used in expectations.
 */
@ModuleConfig(model="DIGITAL-REGULATORY-REPORTING", type="Rosetta")
public class DrrRuntimeModuleTesting extends DrrRuntimeModuleExternalApi {

    @Override
    protected void configure() {
        super.configure();
        install(new FunctionMemoisingModuleBuilder()
                .setFromEnvironment()
                .build());
    }

    @Override
    protected Class<? extends Now> bindNow() {
        return FixedZonedDateTime.class;
    }

    /**
     * Configure a fixed datetime for the report expectations
     */
    private static class FixedZonedDateTime extends Now {
        @Override
        protected ZonedDateTime doEvaluate() {
            return ZonedDateTime.now(Clock.fixed(Instant.parse("2024-04-29T00:00:00.00Z"), ZoneOffset.UTC));
        }
    }
}
