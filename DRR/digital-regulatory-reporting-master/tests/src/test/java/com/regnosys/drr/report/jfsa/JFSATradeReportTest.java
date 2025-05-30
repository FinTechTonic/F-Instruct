package com.regnosys.drr.report.jfsa;

import com.regnosys.drr.report.ReportTestRuntimeModule;
import com.regnosys.rosetta.common.transform.TestPackModel;
import com.regnosys.testing.transform.TransformTestExtension;
import drr.regulation.jfsa.rewrite.trade.reports.JFSATradeReportFunction;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.regnosys.rosetta.common.transform.TestPackUtils.REPORT_CONFIG_PATH;

public class JFSATradeReportTest {

    @RegisterExtension
    static TransformTestExtension<JFSATradeReportFunction> testExtension =
            new TransformTestExtension<>(
                    new ReportTestRuntimeModule(),
                    REPORT_CONFIG_PATH,
                    JFSATradeReportFunction.class);

    @ParameterizedTest(name = "{0}")
    @MethodSource("inputFiles")
    void runReport(String testName,
                   String testPackId,
                   TestPackModel.SampleModel sampleModel,
                   JFSATradeReportFunction func) {
        testExtension.runTransformAndAssert(testPackId, sampleModel, func::evaluate);
    }

    @SuppressWarnings("unused")//used by the junit parameterized test
    private static Stream<Arguments> inputFiles() {
        return testExtension.getArguments();
    }
}
