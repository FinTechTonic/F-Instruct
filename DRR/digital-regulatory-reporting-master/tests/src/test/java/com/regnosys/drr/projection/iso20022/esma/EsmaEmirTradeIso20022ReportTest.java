package com.regnosys.drr.projection.iso20022.esma;

import com.google.common.io.Resources;
import com.regnosys.drr.report.ReportTestRuntimeModule;
import com.regnosys.rosetta.common.transform.TestPackModel;
import com.regnosys.testing.transform.TransformTestExtension;
import drr.projection.iso20022.esma.emir.refit.trade.functions.Project_EsmaEmirTradeReportToIso20022;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.regnosys.rosetta.common.transform.TestPackUtils.PROJECTION_CONFIG_PATH;

public class EsmaEmirTradeIso20022ReportTest {
    private static final String XSD_SCHEMA_PATH = "schemas/iso20022/auth.030.001.03_ESMAUG_DATTAR_1.1.0.xsd";

    @RegisterExtension
    static TransformTestExtension<Project_EsmaEmirTradeReportToIso20022> testExtension =
            new TransformTestExtension<>(
                    new ReportTestRuntimeModule(),
                    PROJECTION_CONFIG_PATH,
                    Project_EsmaEmirTradeReportToIso20022.class)
                    .withSchemaValidation(Resources.getResource(XSD_SCHEMA_PATH));

    @ParameterizedTest(name = "{0}")
    @MethodSource("inputFiles")
    void runReport(String testName,
                   String testPackId,
                   TestPackModel.SampleModel sampleModel,
                   Project_EsmaEmirTradeReportToIso20022 func) {
        testExtension.runTransformAndAssert(testPackId, sampleModel, func::evaluate);
    }

    private static Stream<Arguments> inputFiles() {
        return testExtension.getArguments();
    }
}
