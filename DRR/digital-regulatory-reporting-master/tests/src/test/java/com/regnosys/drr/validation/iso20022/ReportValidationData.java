package com.regnosys.drr.validation.iso20022;

import java.util.Comparator;

public class ReportValidationData implements Comparable<ReportValidationData> {

    private final String projectionName;
    private final String testPack;
    private final String sampleName;
    private final TypeValidationData transactionReportData;
    private final TypeValidationData isoReportData;
    private final String xsdSchemaValidationErrors;

    public ReportValidationData(String projectionName,
                                String testPack,
                                String sampleName,
                                TypeValidationData transactionReportData,
                                TypeValidationData isoReportData,
                                String xsdSchemaValidationErrors) {
        this.projectionName = projectionName;
        this.testPack = testPack;
        this.sampleName = sampleName;
        this.transactionReportData = transactionReportData;
        this.isoReportData = isoReportData;
        this.xsdSchemaValidationErrors = xsdSchemaValidationErrors;
    }

    public String getProjectionName() {
        return projectionName;
    }

    public String getTestPack() {
        return testPack;
    }

    public String getSampleName() {
        return sampleName;
    }

    public TypeValidationData getTransactionReportData() {
        return transactionReportData;
    }

    public TypeValidationData getIsoReportData() {
        return isoReportData;
    }

    public String getXsdSchemaValidationErrors() {
        return xsdSchemaValidationErrors;
    }

    @Override
    public int compareTo(ReportValidationData o) {
        return Comparator
                .comparing(ReportValidationData::getProjectionName)
                .thenComparing(ReportValidationData::getTestPack)
                .thenComparing(ReportValidationData::getSampleName)
                .compare(this, o);
    }
}
