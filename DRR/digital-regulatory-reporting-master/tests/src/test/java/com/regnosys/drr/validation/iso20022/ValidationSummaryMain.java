package com.regnosys.drr.validation.iso20022;

import com.google.common.io.Resources;
import com.google.inject.Injector;
import com.regnosys.drr.report.ReportTestRuntimeModule;
import com.regnosys.rosetta.common.validation.RosettaTypeValidator;
import drr.projection.iso20022.asic.rewrite.margin.functions.Project_ASICMarginReportToIso20022;
import drr.projection.iso20022.asic.rewrite.trade.functions.Project_ASICTradeReportToIso20022;
import drr.projection.iso20022.esma.emir.refit.margin.functions.Project_EsmaEmirMarginReportToIso20022;
import drr.projection.iso20022.esma.emir.refit.trade.functions.Project_EsmaEmirTradeReportToIso20022;
import drr.projection.iso20022.fca.ukemir.refit.margin.functions.Project_FcaUkEmirMarginReportToIso20022;
import drr.projection.iso20022.fca.ukemir.refit.trade.functions.Project_FcaUkEmirTradeReportToIso20022;
import drr.projection.iso20022.jfsa.rewrite.margin.functions.Project_JFSARewriteMarginReportToIso20022;
import drr.projection.iso20022.jfsa.rewrite.trade.functions.Project_JFSARewriteTradeReportToIso20022;
import drr.projection.iso20022.mas.rewrite.margin.functions.Project_MASMarginReportToIso20022;
import drr.projection.iso20022.mas.rewrite.trade.functions.Project_MASTradeReportToIso20022;
import drr.regulation.asic.rewrite.margin.ASICMarginReport;
import drr.regulation.asic.rewrite.trade.ASICTransactionReport;
import drr.regulation.esma.emir.refit.margin.ESMAEMIRMarginReport;
import drr.regulation.esma.emir.refit.trade.ESMAEMIRTransactionReport;
import drr.regulation.fca.ukemir.refit.margin.FCAUKEMIRMarginReport;
import drr.regulation.fca.ukemir.refit.trade.FCAUKEMIRTransactionReport;
import drr.regulation.jfsa.rewrite.margin.JFSAMarginReport;
import drr.regulation.jfsa.rewrite.trade.JFSATransactionReport;
import drr.regulation.mas.rewrite.margin.MASMarginReport;
import drr.regulation.mas.rewrite.trade.MASTransactionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.regnosys.rosetta.common.transform.TestPackUtils.getProjectionTestPackName;

public class ValidationSummaryMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationSummaryMain.class);

    // ASIC Trade
    private static final String ASIC_TRADE_PATH = getProjectionTestPackName("asic-trade");
    private static final String ASIC_TRADE_XML_CONFIG_PATH = "xml-config/auth030-asic-rosetta-xml-config.json";
    private static final String ASIC_TRADE_XSD_SCHEMA_PATH = "schemas/iso20022/auth.030.001.03_ASICUG_DATTAR_1.0.0_20240119_0916_iso15.xsd";

    // ASIC Margin
    private static final String ASIC_MARGIN_PATH = getProjectionTestPackName("asic-margin");
    private static final String ASIC_MARGIN_XML_CONFIG_PATH = "xml-config/auth108-asic-rosetta-xml-config.json";
    private static final String ASIC_MARGIN_XSD_SCHEMA_PATH = "schemas/iso20022/auth.108.001.01_ASICUG_DATMDA_1.0.0_20240119_0927_iso15.xsd";

    // EMIR Trade
    private static final String EMIR_TRADE_PATH = getProjectionTestPackName("esma-emir-trade");
    private static final String ESMA_TRADE_XML_CONFIG_PATH = "xml-config/auth030-esma-rosetta-xml-config.json";
    private static final String ESMA_TRADE_XSD_SCHEMA_PATH = "schemas/iso20022/auth.030.001.03_ESMAUG_DATTAR_1.1.0.xsd";

    // EMIR Margin
    private static final String EMIR_MARGIN_PATH = getProjectionTestPackName("esma-emir-margin");
    private static final String ESMA_MARGIN_XML_CONFIG_PATH = "xml-config/auth108-esma-rosetta-xml-config.json";
    private static final String ESMA_MARGIN_XSD_SCHEMA_PATH = "schemas/iso20022/auth.108.001.01_ESMAUG_DATMDA_1.1.0.xsd";

    // FCA UK EMIR Trade
    private static final String FCA_UKEMIR_TRADE_PATH = getProjectionTestPackName("fca-uk-emir-trade");
    private static final String FCA_UKEMIR_TRADE_XML_CONFIG_PATH = "xml-config/auth030-fca-rosetta-xml-config.json";
    private static final String FCA_UKEMIR_TRADE_XSD_SCHEMA_PATH = "schemas/iso20022/auth.030.001.03_FCAUG_DATTAR_1.0.0_20240304_1435_iso15.xsd";

    // FCA UK EMIR Margin
    private static final String FCA_UKEMIR_MARGIN_PATH = getProjectionTestPackName("fca-uk-emir-margin");
    private static final String FCA_UKEMIR_MARGIN_XML_CONFIG_PATH = "xml-config/auth108-fca-rosetta-xml-config.json";
    private static final String FCA_UKEMIR_MARGIN_XSD_SCHEMA_PATH = "schemas/iso20022/auth.108.001.01_FCAUG_DATMDA_1.0.0_20240305_1247_iso15.xsd";

    // JFSA Trade
    private static final String JFSA_TRADE_PATH = getProjectionTestPackName("jfsa-rewrite-trade");
    private static final String JFSA_TRADE_XML_CONFIG_PATH = "xml-config/auth030-jfsa-rosetta-xml-config.json";
    private static final String JFSA_TRADE_XSD_SCHEMA_PATH = "schemas/iso20022/auth.030.001.03_JFSAUG_DATTAR_1.0.0_20231121_1248_iso15.xsd";

    // JFSA Margin
    private static final String JFSA_MARGIN_PATH = getProjectionTestPackName("jfsa-rewrite-margin");
    private static final String JFSA_MARGIN_XML_CONFIG_PATH = "xml-config/auth108-jfsa-rosetta-xml-config.json";
    private static final String JFSA_MARGIN_XSD_SCHEMA_PATH = "schemas/iso20022/auth.108.001.01_JFSAUG_DATMDA_1.0.0_20231121_1253_iso15.xsd";

    // MAS Trade
    private static final String MAS_TRADE_PATH = getProjectionTestPackName("mas-trade");
    private static final String MAS_TRADE_XML_CONFIG_PATH = "xml-config/auth030-jfsa-rosetta-xml-config.json";
    private static final String MAS_TRADE_XSD_SCHEMA_PATH = "schemas/iso20022/auth.030.001.03_JFSAUG_DATTAR_1.0.0_20231121_1248_iso15.xsd";

    // MAS Margin
    private static final String MAS_MARGIN_PATH = getProjectionTestPackName("mas-margin");
    private static final String MAS_MARGIN_XML_CONFIG_PATH = "xml-config/auth108-jfsa-rosetta-xml-config.json";
    private static final String MAS_MARGIN_XSD_SCHEMA_PATH = "schemas/iso20022/auth.030.001.03-_MASUG_DATTAR_1.0.0_20240129_0805_iso15.xsd";


    public static void main(String[] args) {
        try {
            Injector injector = new ReportTestRuntimeModule.InjectorProvider().getInjector();
            ValidationSummaryMain writer = injector.getInstance(ValidationSummaryMain.class);
            writer.generateValidationSummaryAndWriteCsv();
            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Error executing {}.main()", ValidationSummaryMain.class.getName(), e);
            System.exit(1);
        }
    }

    @Inject
    private Project_ASICTradeReportToIso20022 asicTradeFunc;
    @Inject
    private Project_ASICMarginReportToIso20022 asicMarginFunc;
    @Inject
    private Project_EsmaEmirTradeReportToIso20022 emirTradeFunc;
    @Inject
    private Project_EsmaEmirMarginReportToIso20022 emirMarginFunc;
    @Inject
    private Project_FcaUkEmirTradeReportToIso20022 fcaUkEmirTradeFunc;
    @Inject
    private Project_FcaUkEmirMarginReportToIso20022 fcaUkEmirMarginFunc;
    @Inject
    private Project_JFSARewriteTradeReportToIso20022 jfsaTradeFunc;
    @Inject
    private Project_JFSARewriteMarginReportToIso20022 jfsaMarginFunc;
    @Inject
    private Project_MASTradeReportToIso20022 masTradeFunc;
    @Inject
    private Project_MASMarginReportToIso20022 masMarginFunc;

    @Inject
    private RosettaTypeValidator validator;

    void generateValidationSummaryAndWriteCsv() throws IOException {
        // Build validation summary
        List<ReportValidationData> reportValidationDataList = new ArrayList<>();

        // ASIC Trade
        ValidationSummaryCreator asicTradeValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(ASIC_TRADE_XML_CONFIG_PATH),
                        Resources.getResource(ASIC_TRADE_XSD_SCHEMA_PATH));

        String asicTradeProjectionName = Project_ASICTradeReportToIso20022.class.getName();
        reportValidationDataList.addAll(asicTradeValidation.getValidationData(ASIC_TRADE_PATH, asicTradeProjectionName, ASICTransactionReport.class, asicTradeFunc::evaluate));

        // ASIC Margin
        ValidationSummaryCreator asicMarginValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(ASIC_MARGIN_XML_CONFIG_PATH),
                        Resources.getResource(ASIC_MARGIN_XSD_SCHEMA_PATH));
        String asicMarginProjectionName = Project_ASICMarginReportToIso20022.class.getName();
        reportValidationDataList.addAll(asicMarginValidation.getValidationData(ASIC_MARGIN_PATH, asicMarginProjectionName, ASICMarginReport.class, asicMarginFunc::evaluate));

        // EMIR Trade
        ValidationSummaryCreator esmaTradeValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(ESMA_TRADE_XML_CONFIG_PATH),
                        Resources.getResource(ESMA_TRADE_XSD_SCHEMA_PATH));
        String emirTradeProjectionName = Project_EsmaEmirTradeReportToIso20022.class.getName();
        reportValidationDataList.addAll(esmaTradeValidation.getValidationData(EMIR_TRADE_PATH, emirTradeProjectionName, ESMAEMIRTransactionReport.class, emirTradeFunc::evaluate));

        // EMIR Margin
        ValidationSummaryCreator esmaMarginValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(ESMA_MARGIN_XML_CONFIG_PATH),
                        Resources.getResource(ESMA_MARGIN_XSD_SCHEMA_PATH));
        String emirMarginProjectionName = Project_EsmaEmirMarginReportToIso20022.class.getName();
        reportValidationDataList.addAll(esmaMarginValidation.getValidationData(EMIR_MARGIN_PATH, emirMarginProjectionName, ESMAEMIRMarginReport.class, emirMarginFunc::evaluate));

        // FCA UK EMIR Trade
        ValidationSummaryCreator fcaUkEmirTradeValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(FCA_UKEMIR_TRADE_XML_CONFIG_PATH),
                        Resources.getResource(FCA_UKEMIR_TRADE_XSD_SCHEMA_PATH));
        String fcaUkEmirTradeProjectionName = Project_FcaUkEmirTradeReportToIso20022.class.getName();
        reportValidationDataList.addAll(fcaUkEmirTradeValidation.getValidationData(FCA_UKEMIR_TRADE_PATH, fcaUkEmirTradeProjectionName, FCAUKEMIRTransactionReport.class, fcaUkEmirTradeFunc::evaluate));

        // FCA UK EMIR Margin
        ValidationSummaryCreator fcaUkEmirMarginValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(FCA_UKEMIR_MARGIN_XML_CONFIG_PATH),
                        Resources.getResource(FCA_UKEMIR_MARGIN_XSD_SCHEMA_PATH));
        String fcaUkEmirMarginProjectionName = Project_FcaUkEmirMarginReportToIso20022.class.getName();
        reportValidationDataList.addAll(fcaUkEmirMarginValidation.getValidationData(FCA_UKEMIR_MARGIN_PATH, fcaUkEmirMarginProjectionName, FCAUKEMIRMarginReport.class, fcaUkEmirMarginFunc::evaluate));

        // JFSA Trade
        ValidationSummaryCreator jfsaTradeValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(JFSA_TRADE_XML_CONFIG_PATH),
                        Resources.getResource(JFSA_TRADE_XSD_SCHEMA_PATH));
        String jfsaTradeProjectionName = Project_JFSARewriteTradeReportToIso20022.class.getName();
        reportValidationDataList.addAll(jfsaTradeValidation.getValidationData(JFSA_TRADE_PATH, jfsaTradeProjectionName, JFSATransactionReport.class, jfsaTradeFunc::evaluate));

        // JFSA Margin
        ValidationSummaryCreator jfsaMarginValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(JFSA_MARGIN_XML_CONFIG_PATH),
                        Resources.getResource(JFSA_MARGIN_XSD_SCHEMA_PATH));
        String jfsaMarginProjectionName = Project_JFSARewriteMarginReportToIso20022.class.getName();
        reportValidationDataList.addAll(jfsaMarginValidation.getValidationData(JFSA_MARGIN_PATH, jfsaMarginProjectionName, JFSAMarginReport.class, jfsaMarginFunc::evaluate));

        // MAS Trade
        ValidationSummaryCreator masTradeValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(MAS_TRADE_XML_CONFIG_PATH),
                        Resources.getResource(MAS_TRADE_XSD_SCHEMA_PATH));
        String masTradeProjectionName = Project_MASTradeReportToIso20022.class.getName();
        reportValidationDataList.addAll(masTradeValidation.getValidationData(MAS_TRADE_PATH, masTradeProjectionName, MASTransactionReport.class, masTradeFunc::evaluate));

        // MAS Margin
        ValidationSummaryCreator masMarginValidation =
                new ValidationSummaryCreator(validator,
                        Resources.getResource(MAS_MARGIN_XML_CONFIG_PATH),
                        Resources.getResource(MAS_MARGIN_XSD_SCHEMA_PATH));
        String masMarginProjectionName = Project_MASMarginReportToIso20022.class.getName();
        reportValidationDataList.addAll(masMarginValidation.getValidationData(MAS_MARGIN_PATH, masMarginProjectionName, MASMarginReport.class, masMarginFunc::evaluate));

        // Write results
        new ValidationSummaryWriter().writeCsv(reportValidationDataList);
        new ValidationFileSummaryWriter().writeCsv(reportValidationDataList);
    }
}
