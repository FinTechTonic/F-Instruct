package com.regnosys.drr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Injector;
import com.regnosys.drr.TestPackCreatorModel.ReportSampleConfigItem;
import com.regnosys.drr.report.ReportTestRuntimeModule;
import com.regnosys.rosetta.common.reports.RegReportPaths;
import com.regnosys.testing.reports.ObjectMapperGenerator;
import com.regnosys.testing.testpack.TestPackConfigCreator;
import com.regnosys.testing.testpack.TestPackDef;
import com.regnosys.testing.testpack.TestPackFilter;
import drr.regulation.asic.rewrite.trade.reports.ASICTradeReportFunction;
import drr.regulation.cftc.rewrite.reports.CFTCPart43ReportFunction;
import drr.regulation.cftc.rewrite.reports.CFTCPart45ReportFunction;
import drr.regulation.esma.emir.refit.trade.reports.ESMAEMIRTradeReportFunction;
import drr.regulation.mas.rewrite.trade.reports.MASTradeReportFunction;
import drr.regulation.techsprint.g20.mas.reports.MASSFAMAS_2013ReportFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.regnosys.testing.TestingExpectationUtil.TEST_WRITE_BASE_PATH;

/**
 * To overwrite test-pack config files, set CREATE_EXPECTATION_FILES to true.
 */
public class ConfigurableDrrTestPackCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableDrrTestPackCreator.class);

    /**
     * For the test pack display name (e.g. "CFTC Event Scenarios") only run the given reports
     */
    private static final ImmutableMultimap<String, Class<?>> TEST_PACK_INCLUDED_REPORTS =
            ImmutableMultimap.<String, Class<?>>builder()
                    .put("Delegated Reporting", ESMAEMIRTradeReportFunction.class)
                    .put("ETD", ESMAEMIRTradeReportFunction.class)
                    .put("Commodity", ESMAEMIRTradeReportFunction.class)
                    .put("Commodity", MASTradeReportFunction.class)
                    .put("Commodity", ASICTradeReportFunction.class)
                    .put("CFTC Event Scenarios", CFTCPart43ReportFunction.class)
                    .put("CFTC Event Scenarios", CFTCPart45ReportFunction.class)
                    .build();
    /**
     * For the report (e.g. MAS) only use the given data sets
     */
    private static final ImmutableMultimap<Class<?>, String> REPORT_INCLUDED_TEST_PACK =
            ImmutableMultimap.<Class<?>, String>builder()
                    .put(MASSFAMAS_2013ReportFunction.class, "Rates")
                    .build();
    private static final ImmutableList<String> ROSETTA_PATHS = ImmutableList.of("drr/rosetta");

    public static void main(String[] args) {
        try {
            Injector injector = new ReportTestRuntimeModule.InjectorProvider().getInjector();
            new ConfigurableDrrTestPackCreator(injector.getInstance(TestPackConfigCreator.class)).run();
            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Error executing {}.main()", ConfigurableDrrTestPackCreator.class.getName(), e);
            System.exit(1);
        }
    }

    private final TestPackConfigCreator testPackConfigCreator;
    private final ReportSampleItemHandler itemHandler;

    public ConfigurableDrrTestPackCreator(TestPackConfigCreator testPackConfigCreator) {
        this.testPackConfigCreator = testPackConfigCreator;
        this.itemHandler = new ReportSampleItemHandler(ObjectMapperGenerator.createWriterMapper(true));
    }

    private void run() {
        // Enrich ingest samples, and copy into reporting input folder
        copyAndEnrichSamples();

        // Generate pipeline and test-pack config files
        TestPackFilter filter = TestPackFilter.create()
                .withTestPackReportMap(TEST_PACK_INCLUDED_REPORTS)
                .withReportTestPackMap(REPORT_INCLUDED_TEST_PACK);
        testPackConfigCreator.createPipelineAndTestPackConfig(ROSETTA_PATHS, filter, getTestPacks());
    }

    private void copyAndEnrichSamples() {
        if (TEST_WRITE_BASE_PATH.isEmpty()) {
            LOGGER.error("TEST_WRITE_BASE_PATH not set");
            return;
        }
        Path writePath = TEST_WRITE_BASE_PATH.get();
        RegReportPaths paths = RegReportPaths.getDefault();

        LOGGER.info("Copy and process ingested test packs");
        itemHandler.copyFiles(writePath.resolve(paths.getInputRelativePath()), TestPackCreatorConfig.INSTANCE.getReportSampleConfigs());
    }

    private List<TestPackDef> getTestPacks() {
        TestPackCreatorConfig testPackConfig = TestPackCreatorConfig.INSTANCE;
        return testPackConfig.getAllReportTestPacks()
                .stream()
                .map(p -> new TestPackDef(p.getName(), p.getInputType(), getTestPackInputPaths(testPackConfig, p.getName())))
                .collect(Collectors.toList());
    }

    private List<String> getTestPackInputPaths(TestPackCreatorConfig testPackConfig, String testPackName) {
        return testPackConfig.lookupReportConfigItemsGivenTestPackName(testPackName)
                .stream()
                .map(ReportSampleConfigItem::targetLocation)
                .collect(Collectors.toList());
    }
}
