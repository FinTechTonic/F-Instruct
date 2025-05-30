package com.regnosys.drr.validation.iso20022;

import com.regnosys.testing.TestingExpectationUtil;
import com.regnosys.testing.projection.ProjectionPaths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class ValidationSummaryWriter {

    static final String DELIMITER = " | ";
    static final char NEW_LINE = '\n';
    static Optional<Path> TEST_WRITE_BASE_PATH = Optional.ofNullable(System.getenv("TEST_WRITE_BASE_PATH"))
            .map(Paths::get);
    static final Path ANALYTICS_PATH = ProjectionPaths.PROJECTION_PATH.resolve("iso-20022").resolve("analytics");

    private static final String ROW_HEADER_TOTAL = "Total";
    private static final String[] SUMMARY_HEADER = new String[]{"Projection", "DRR: Cardinality Failures", "DRR: Cardinality Total", "DRR: Cardinality Success", "DRR: Type Format Failures", "DRR: Type Format Total", "DRR: Type Format Success", "DRR: Condition Failures", "DRR: Condition Total", "DRR: Condition Success", "DRR: CONDITION-All-Valid", "", "ISO: Cardinality Failures", "ISO: Cardinality Total", "ISO: Cardinality Success", "ISO: Type Format Failures", "ISO: Type Format Total", "ISO: Type Format Success", "", "Failures", ROW_HEADER_TOTAL, "Success", "", "ISO: XML Schema Failures", "ISO: XML Schema Total", "ISO: XML Schema Success", "", "DRR: CONDITION All Valid", "DRR: CONDITION All Valid Total", "DRR: CONDITION All Valid Success"};
    private static final String[] SHORT_SUMMARY_HEADER = new String[]{"Projection", "Failures", ROW_HEADER_TOTAL, "Success", "", "DRR: CONDITION All Valid", "DRR: CONDITION All Valid Total", "DRR: CONDITION All Valid Success", "", "ISO: XML Schema Failures", "ISO: XML Schema Total", "ISO: XML Schema Success"};
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationSummaryWriter.class);
    private static final Path SUMMARY_FILE_PATH = ANALYTICS_PATH.resolve("validation-summary.csv");
    private static final Path SHORT_SUMMARY_FILE_PATH = ANALYTICS_PATH.resolve("validation-short-summary.csv");

    public void writeCsv(List<ReportValidationData> reportValidationDataList) throws IOException {
        if (TEST_WRITE_BASE_PATH.isEmpty()) {
            LOGGER.info("TEST_WRITE_BASE_PATH not set");
            return;
        }

        Collections.sort(reportValidationDataList);

        Map<String, CsvRow> projectionFuncSummary = new HashMap<>();
        for (ReportValidationData reportValidationData : reportValidationDataList) {
            String projectionName = reportValidationData.getProjectionName();

            if (reportValidationData.getTestPack().equals("Cleared Rates")) {
                continue;
            }

            projectionFuncSummary.compute(projectionName, (key, totals) -> {
                if (totals == null) {
                    totals = new CsvRow(projectionName);
                }
                totals.accumulate(getCsvRow(reportValidationData));
                return totals;
            });
        }

        List<CsvRow> csvRows = new ArrayList<>(projectionFuncSummary.values());
        Collections.sort(csvRows);
        writeSummary(csvRows);
        writeShortSummary(csvRows);
    }

    private void writeSummary(List<CsvRow> rows) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SUMMARY_HEADER)
                .setRecordSeparator(NEW_LINE)
                .setTrim(false)
                .build();

        StringBuilder csvContents = new StringBuilder();

        try (CSVPrinter csvBuilder = new CSVPrinter(csvContents, csvFormat)) {
            CsvRow totals = new CsvRow(ROW_HEADER_TOTAL);

            for (CsvRow row : rows) {
                // print data row
                printSummaryRow(csvBuilder, row.getProjectionName(), row);
                // accumulate totals for bottom row
                totals.accumulate(row);
            }
            // print totals row
            printSummaryRow(csvBuilder, ROW_HEADER_TOTAL, totals);
        }

        Path analyticsPath = TEST_WRITE_BASE_PATH.get().resolve(SUMMARY_FILE_PATH);
        TestingExpectationUtil.writeFile(analyticsPath, csvContents.toString(), true);
    }

    private void printSummaryRow(CSVPrinter csvBuilder, String projectionName, CsvRow row) throws IOException {
        csvBuilder.print(projectionName);

        print(csvBuilder, row.failures.drrCardinality, row.totals.drrCardinality);
        print(csvBuilder, row.failures.drrTypeFormat, row.totals.drrTypeFormat);
        print(csvBuilder, row.failures.drrCondition, row.totals.drrCondition);
        printAllValid(csvBuilder, row.failures.drrCondition);

        csvBuilder.print("");

        print(csvBuilder, row.failures.isoCardinality, row.totals.isoCardinality);
        print(csvBuilder, row.failures.isoTypeFormat, row.totals.isoTypeFormat);

        csvBuilder.print("");

        print(csvBuilder, row.failures.total, row.totals.total);

        csvBuilder.print("");

        print(csvBuilder, row.failures.validXml, row.totals.validXml);

        csvBuilder.print("");

        printAllValidRateDetails(csvBuilder, row.failures.drrConditionAllValidCount, row.totals.drrConditionAllValidCount);

        csvBuilder.printRecord();
    }

    private void writeShortSummary(List<CsvRow> rows) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SHORT_SUMMARY_HEADER)
                .setRecordSeparator(NEW_LINE)
                .setTrim(false)
                .build();

        StringBuilder csvContents = new StringBuilder();

        try (CSVPrinter csvBuilder = new CSVPrinter(csvContents, csvFormat)) {
            CsvRow totals = new CsvRow(ROW_HEADER_TOTAL);

            for (CsvRow row : rows) {
                // print data row
                printShortSummaryRow(csvBuilder, row.getProjectionName(), row);
                // accumulate totals for bottom row
                totals.accumulate(row);
            }
            // print totals row
            printShortSummaryRow(csvBuilder, ROW_HEADER_TOTAL, totals);
        }

        Path analyticsPath = TEST_WRITE_BASE_PATH.get().resolve(SHORT_SUMMARY_FILE_PATH);
        TestingExpectationUtil.writeFile(analyticsPath, csvContents.toString(), true);
    }

    private void printShortSummaryRow(CSVPrinter csvBuilder, String projectionName, CsvRow row) throws IOException {
        csvBuilder.print(projectionName);

        print(csvBuilder, row.failures.total, row.totals.total);
        csvBuilder.print("");

        printAllValidRateDetails(csvBuilder, row.failures.drrConditionAllValidCount, row.totals.drrConditionAllValidCount);

        csvBuilder.print("");

        print(csvBuilder, row.failures.validXml, row.totals.validXml);

        csvBuilder.printRecord();
    }

    private CsvRow getCsvRow(ReportValidationData reportValidationData) {
        TypeValidationData transactionReportData = reportValidationData.getTransactionReportData();

        AttributeValidationData drrCardinalityValidationData = transactionReportData.getCardinalityValidationData();
        int drrCardinalityFailureCount = drrCardinalityValidationData.getFailureCount();
        int drrCardinalityTotalCount = drrCardinalityValidationData.getTotalCount();

        AttributeValidationData drrTypeFormatValidationData = transactionReportData.getTypeFormatValidationData();
        int drrTypeFormatFailureCount = drrTypeFormatValidationData.getFailureCount();
        int drrTypeFormatTotalCount = drrTypeFormatValidationData.getTotalCount();

        ConditionValidationData drrTransactionCondition = transactionReportData.getConditionValidationData();
        int drrConditionFailureCount = drrTransactionCondition.getFailureCount();
        int drrConditionTotalCount = drrTransactionCondition.getTotalCount();
        int drrConditionAllValidCount =  drrConditionFailureCount == 0 ? 1:0;

        TypeValidationData isoReportData = reportValidationData.getIsoReportData();

        AttributeValidationData isoCardinalityValidationData = isoReportData.getCardinalityValidationData();
        int isoCardinalityFailureCount = isoCardinalityValidationData.getFailureCount();
        int isoCardinalityTotalCount = isoCardinalityValidationData.getTotalCount();

        AttributeValidationData isoTypeFormatValidationData = isoReportData.getTypeFormatValidationData();
        int isoTypeFormatFailureCount = isoTypeFormatValidationData.getFailureCount();
        int isoTypeFormatTotalCount = isoTypeFormatValidationData.getTotalCount();

        String xsdSchemaValidationErrors = reportValidationData.getXsdSchemaValidationErrors();
        int isoSchemaValidationFailureCount = xsdSchemaValidationErrors == null ? 0 : 1;

        CsvData failures = new CsvData(drrCardinalityFailureCount,
                drrTypeFormatFailureCount,
                drrConditionFailureCount,
                isoCardinalityFailureCount,
                isoTypeFormatFailureCount,
                isoSchemaValidationFailureCount,
                drrConditionAllValidCount);

        CsvData totals = new CsvData(drrCardinalityTotalCount,
                drrTypeFormatTotalCount,
                drrConditionTotalCount,
                isoCardinalityTotalCount,
                isoTypeFormatTotalCount,
                1,
                1);

        return new CsvRow("", failures, totals);
    }

    private void printAllValid(CSVPrinter csvBuilder, int failures) throws IOException {
        int allValid = failures == 0 ? 1:0;
        csvBuilder.print(allValid);
    }

    private void print(CSVPrinter csvBuilder, int failures, int total) throws IOException {
        csvBuilder.print(failures);
        csvBuilder.print(total);
        csvBuilder.print(getSuccessPercentage(failures, total));
    }

    private void printAllValidRateDetails(CSVPrinter csvBuilder, int allValid, int total) throws IOException {
        csvBuilder.print(allValid);
        csvBuilder.print(total);
        if (total == 0){
            total = 100;
        }
        int failures = total - allValid;
        csvBuilder.print(getSuccessPercentage(failures, total));
    }

    private String getSuccessPercentage(int failureCount, int totalCount) {
        double successPercentage = totalCount == 0 ?
                100.0 :
                ((totalCount - failureCount) / (double) totalCount) * 100.0;
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(successPercentage);
    }

    private static class CsvRow implements Comparable<CsvRow> {

        private final String projectionName;
        private final CsvData failures;
        private final CsvData totals;
        ;

        CsvRow(String projectionName) {
            this(projectionName, new CsvData(), new CsvData());
        }

        CsvRow(String projectionName, CsvData failures, CsvData totals) {
            this.projectionName = projectionName;
            this.failures = failures;
            this.totals = totals;
        }

        public String getProjectionName() {
            return projectionName;
        }

        void accumulate(CsvRow row) {
            this.failures.drrCardinality += row.failures.drrCardinality;
            this.failures.drrTypeFormat += row.failures.drrTypeFormat;
            this.failures.drrCondition += row.failures.drrCondition;
            this.failures.isoCardinality += row.failures.isoCardinality;
            this.failures.isoTypeFormat += row.failures.isoTypeFormat;
            this.failures.total += row.failures.total;
            this.failures.validXml += row.failures.validXml;
            this.failures.drrConditionAllValidCount += row.failures.drrConditionAllValidCount;

            this.totals.drrCardinality += row.totals.drrCardinality;
            this.totals.drrTypeFormat += row.totals.drrTypeFormat;
            this.totals.drrCondition += row.totals.drrCondition;
            this.totals.isoCardinality += row.totals.isoCardinality;
            this.totals.isoTypeFormat += row.totals.isoTypeFormat;
            this.totals.total += row.totals.total;
            this.totals.validXml += row.totals.validXml;
            this.totals.drrConditionAllValidCount += row.totals.drrConditionAllValidCount;
        }

        @Override
        public int compareTo(CsvRow o) {
            return Comparator.comparing(CsvRow::getProjectionName).compare(this, o);
        }
    }

    private static class CsvData {
        private int drrCardinality;
        private int drrTypeFormat;
        private int drrCondition;
        private int isoCardinality;
        private int isoTypeFormat;
        private int total;
        private int validXml;
        private int drrConditionAllValidCount;

        CsvData() {
            this.drrCardinality = 0;
            this.drrTypeFormat = 0;
            this.drrCondition = 0;
            this.isoCardinality = 0;
            this.isoTypeFormat = 0;
            this.total = 0;
            this.validXml = 0;
            this.drrConditionAllValidCount = 0;
        }

        CsvData(int drrCardinality, int drrTypeFormat, int drrCondition, int isoCardinality, int isoTypeFormat, int validXml, int drrConditionAllValidCount) {
            this.drrCardinality = drrCardinality;
            this.drrTypeFormat = drrTypeFormat;
            this.drrCondition = drrCondition;
            this.isoCardinality = isoCardinality;
            this.isoTypeFormat = isoTypeFormat;
            this.total = drrCardinality + drrTypeFormat + drrCondition + isoCardinality + isoTypeFormat;
            this.validXml = validXml;
            this.drrConditionAllValidCount = drrConditionAllValidCount;
        }
    }
}
