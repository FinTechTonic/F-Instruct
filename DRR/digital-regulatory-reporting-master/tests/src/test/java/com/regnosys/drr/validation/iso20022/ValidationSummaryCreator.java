package com.regnosys.drr.validation.iso20022;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.io.Resources;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapperCreator;
import com.regnosys.rosetta.common.transform.TestPackModel;
import com.regnosys.rosetta.common.transform.TestPackUtils;
import com.regnosys.rosetta.common.validation.RosettaTypeValidator;
import com.regnosys.rosetta.common.validation.ValidationReport;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.regnosys.rosetta.common.transform.TestPackUtils.PROJECTION_CONFIG_PATH;

public class ValidationSummaryCreator {

    private final Logger LOGGER = LoggerFactory.getLogger(ValidationSummaryCreator.class);

    private final RosettaTypeValidator validator;
    private final ObjectWriter xmlObjectWriter;
    private final Validator xsdValidator;

    public ValidationSummaryCreator(RosettaTypeValidator validator, URL xmlConfig, URL xsdSchema) {
        this.validator = validator;
        try {
            this.xmlObjectWriter = RosettaObjectMapperCreator.forXML(xmlConfig.openStream()).create().writerWithDefaultPrettyPrinter();
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // required to process xml elements with an maxOccurs greater than 5000 (rather than unbounded)
            schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            Schema schema = schemaFactory.newSchema(xsdSchema);
            this.xsdValidator = schema.newValidator();
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public <IN extends RosettaModelObject, OUT extends RosettaModelObject> List<ReportValidationData> getValidationData(String projectionTestPackFileName,
                                                                                                                        String projectionName,
                                                                                                                        Class<IN> inputType,
                                                                                                                        Function<IN, OUT> projectionFunc) {
        List<URL> expectationFiles = TestPackUtils.findPaths(PROJECTION_CONFIG_PATH, getClass().getClassLoader(), projectionTestPackFileName);
        ObjectMapper mapper = RosettaObjectMapper.getNewRosettaObjectMapper();

        return expectationFiles.stream()
                .map(expectationUrl -> {
                    // Read projection expectation file
                    TestPackModel testPackModel =
                            TestPackUtils.readFile(expectationUrl, mapper, TestPackModel.class);
                    String testPack = testPackModel.getName();

                    return testPackModel.getSamples().stream()
                            .map(sampleModel -> {
                                // For each test pack sample
                                String inputFile = sampleModel.getInputPath();
                                URL inputFileUrl = Resources.getResource(inputFile);
                                String sampleName = getFileName(inputFileUrl);
                                LOGGER.info("Validating projection {}, test pack {}, sample {}", projectionName, testPack, sampleName);

                                IN transactionReport = TestPackUtils.readFile(inputFileUrl, mapper, inputType);

                                // Transaction report Rosetta validation
                                ValidationReport transactionValidation =
                                        validator.runProcessStep(transactionReport.getClass(), transactionReport);

                                TypeValidationData reportValidation = getReportValidation(transactionValidation);

                                TypeValidationData isoReportValidation;
                                String xsdSchemaValidationErrors;

                                try {
                                    // Run projection
                                    OUT isoReport = projectionFunc.apply(transactionReport);
                                    // ISO report Rosetta validation
                                    ValidationReport isoDocumentValidation =
                                            validator.runProcessStep(isoReport.getClass(), isoReport);

                                    isoReportValidation = getReportValidation(isoDocumentValidation);

                                    // XSD validation
                                    xsdSchemaValidationErrors = getXsdSchemaValidationErrors(isoReport);
                                } catch (Exception e) {
                                    LOGGER.error("Exception occurred generating project validation data", e);
                                    isoReportValidation = new TypeValidationData(e);
                                    xsdSchemaValidationErrors = null;
                                }

                                return new ReportValidationData(projectionName,
                                        testPack,
                                        sampleName,
                                        reportValidation,
                                        isoReportValidation,
                                        xsdSchemaValidationErrors);
                            })
                            .collect(Collectors.toList());
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private String getFileName(URL inputFileUrl) {
        try {
            return Path.of(inputFileUrl.toURI()).getFileName().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private TypeValidationData getReportValidation(ValidationReport reportValidation) {
        AttributeValidationData cardinalityValidationData = getAttributeValidationData(reportValidation, ValidationResult.ValidationType.CARDINALITY);
        AttributeValidationData typeFormatValidationData = getAttributeValidationData(reportValidation, ValidationResult.ValidationType.TYPE_FORMAT);
        ConditionValidationData conditionValidationData = getConditionValidationData(reportValidation);
        return new TypeValidationData(cardinalityValidationData, typeFormatValidationData, conditionValidationData);
    }

    private AttributeValidationData getAttributeValidationData(ValidationReport reportValidation, ValidationResult.ValidationType validationType) {
        int totalCount =
                reportValidation.results()
                        .stream()
                        .filter(r -> r.getValidationType() == validationType)
                        .collect(Collectors.toList())
                        .size();
        List<ValidationResult<?>> failures = reportValidation.validationFailures();
        List<String> failedAttributeNames = failures.stream()
                .filter(r -> r.getValidationType() == validationType)
                .map(r -> r.getFailureReason().get())
                .map(this::getAttributeName)
                .sorted()
                .collect(Collectors.toList());
        return new AttributeValidationData(failedAttributeNames.size(), failedAttributeNames, totalCount);
    }

    private ConditionValidationData getConditionValidationData(ValidationReport reportValidation) {
        int totalConditionsCount =
                reportValidation.results()
                        .stream()
                        .filter(r -> r.getValidationType() == ValidationResult.ValidationType.DATA_RULE)
                        .collect(Collectors.toList())
                        .size();
        List<String> failedConditions =
                reportValidation.validationFailures()
                        .stream()
                        .filter(r -> r.getValidationType() == ValidationResult.ValidationType.DATA_RULE)
                        .map(r -> r.getName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        return new ConditionValidationData(failedConditions.size(), failedConditions, totalConditionsCount);
    }

    private String getAttributeName(String reason) {
        try {
            String substring = reason.substring(reason.indexOf("'") + 1);
            return substring.substring(0, substring.indexOf("'"));
        } catch (Exception e) {
            LOGGER.error("Failed to get attribute name from reason {}", reason, e);
            throw e;
        }

    }

    private String getXsdSchemaValidationErrors(RosettaModelObject isoReport) {
        String actualXml;
        try {
            actualXml = xmlObjectWriter.writeValueAsString(isoReport);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialise to xml", e);
            throw new RuntimeException(e);
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(actualXml.getBytes(StandardCharsets.UTF_8))) {
            xsdValidator.validate(new StreamSource(inputStream));
            return null;
        } catch (SAXException e) {
            // Schema validation errors
            return e.getMessage();
        } catch (IOException e) {
            LOGGER.error("Failed to validate against xsd", e);
            throw new RuntimeException(e);
        }
    }
}
