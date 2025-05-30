package com.regnosys.drr.validation.iso20022;

import com.regnosys.testing.TestingExpectationUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.regnosys.drr.validation.iso20022.ValidationSummaryWriter.*;

public class ValidationFileSummaryWriter {

    private static final String[] VALIDATION_PER_FILE_HEADER = new String[]{"#", "Projection", "Test Pack", "Sample", "DRR: Error", "DRR: CARDINALITY-Name", "DRR: CARDINALITY-Count", "DRR: TYPE_FORMAT-Name", "DRR: TYPE_FORMAT-Count", "DRR: CONDITION-Name", "DRR: CONDITION-Count", "DRR: CONDITION-All-Valid", "", "ISO: Error", "ISO: CARDINALITY-Name", "ISO: CARDINALITY-Count", "ISO: TYPE_FORMAT-Name", "ISO: TYPE_FORMAT-Count", "", "ISO: Schema Failures"};
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationFileSummaryWriter.class);
    private static final Path ANALYTICS_FILE_PATH = ANALYTICS_PATH.resolve("validation-file-summary.csv");

    public void writeCsv(List<ReportValidationData> reportValidationDataList) throws IOException {
        if (TEST_WRITE_BASE_PATH.isEmpty()) {
            LOGGER.info("TEST_WRITE_BASE_PATH not set");
            return;
        }
        writeFileSummary(reportValidationDataList);
    }

    private static void writeFileSummary(List<ReportValidationData> reportValidationDataList) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(VALIDATION_PER_FILE_HEADER)
                .setRecordSeparator(NEW_LINE)
                .setTrim(false)
                .build();
        StringBuilder csvContents = new StringBuilder();

        try (CSVPrinter csvBuilder = new CSVPrinter(csvContents, csvFormat)) {
            AtomicInteger count = new AtomicInteger();
            Collections.sort(reportValidationDataList);
            for (ReportValidationData reportValidationData : reportValidationDataList) {
                printFileSummaryRow(csvBuilder, count, reportValidationData);
            }
        }

        Path analyticsPath = TEST_WRITE_BASE_PATH.get().resolve(ANALYTICS_FILE_PATH);
        TestingExpectationUtil.writeFile(analyticsPath, csvContents.toString(), true);
    }

    private static void printFileSummaryRow(CSVPrinter csvBuilder, AtomicInteger count, ReportValidationData reportValidationData) throws IOException {
        csvBuilder.print(count.getAndIncrement());
        csvBuilder.print(reportValidationData.getProjectionName());
        csvBuilder.print(reportValidationData.getTestPack());
        csvBuilder.print(reportValidationData.getSampleName());

        TypeValidationData transactionReportData = reportValidationData.getTransactionReportData();
        print(csvBuilder, transactionReportData.getExceptionData());
        print(csvBuilder, transactionReportData.getCardinalityValidationData());
        print(csvBuilder, transactionReportData.getTypeFormatValidationData());

        ConditionValidationData transactionCondition = transactionReportData.getConditionValidationData();
        print(csvBuilder, transactionCondition);

        csvBuilder.print("");

        TypeValidationData isoReportData = reportValidationData.getIsoReportData();
        print(csvBuilder, isoReportData.getExceptionData());
        print(csvBuilder, isoReportData.getCardinalityValidationData());
        print(csvBuilder, isoReportData.getTypeFormatValidationData());

        csvBuilder.print("");

        csvBuilder.print(reportValidationData.getXsdSchemaValidationErrors());

        csvBuilder.printRecord();
    }

    private static void print(CSVPrinter csvBuilder, Exception exceptionData) throws IOException {
        csvBuilder.print(exceptionData);
    }

    private static void print(CSVPrinter csvBuilder, ConditionValidationData data) throws IOException {
        int failureCount = data.getFailureCount();
        csvBuilder.print(sortAndJoin(data.getFailedConditions()));
        csvBuilder.print(failureCount);
        int allValid = failureCount == 0 ? 1:0;
        csvBuilder.print(allValid);
    }

    private static void print(CSVPrinter csvBuilder, AttributeValidationData data) throws IOException {
        int failureCount = data.getFailureCount();
        csvBuilder.print(sortAndJoin(data.getFailedAttributeNames()));
        csvBuilder.print(failureCount);
    }

    private static String sortAndJoin(List<String> strings) {
        Collections.sort(strings);
        return String.join(DELIMITER, strings);
    }
}

