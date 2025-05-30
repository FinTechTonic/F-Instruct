package com.regnosys.drr.testpack.runner;

import cdm.base.staticdata.asset.common.ProductTaxonomy;
import com.regnosys.drr.testpack.TestPackModifier;
import com.regnosys.drr.testpack.modifiers.ModifierContext;
import com.regnosys.drr.testpack.modifiers.TestPackModifierHelper;
import com.regnosys.drr.testpack.util.XmlDom;
import drr.regulation.common.ReportableEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestPackModifierRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPackModifierRunner.class);
    private final Path xmlRootDir;
    private final TestPackModifierFactory testPackModifierFactory;

    public TestPackModifierRunner(Path xmlRootDir, TestPackModifierFactory testPackModifierFactory) {
        this.xmlRootDir = xmlRootDir;
        this.testPackModifierFactory = testPackModifierFactory;
    }

    private static void writeFile(Path xmlFile, boolean dryRun, String before, String after) throws IOException {
        if (!before.equals(after) && !dryRun) {
            Files.writeString(xmlFile, after);
        }
    }

    public void run(boolean dryRun) throws IOException {
        List<Path> xmlFiles = findXMLFiles();
        ModifierContext context = getModifierContext(xmlFiles);
        List<TestPackModifier> testPackModifierModifiers = testPackModifierFactory.getTestPackModifierModifiers(context);

        for (Path xmlFile : xmlFiles) {
            LOGGER.info("Checking to see if {} needs modifying", xmlRootDir.relativize(xmlFile));
            for (TestPackModifier testPackModifierModifier : testPackModifierModifiers) {
                String xmlContent = Files.readString(xmlFile);
                modifySampleIfApplicable(xmlFile, testPackModifierModifier, xmlContent, dryRun);
            }
        }
    }

    private ModifierContext getModifierContext(List<Path> xmlFiles) {
        Map<Path, String> xmlFileToQualifierMap = new HashMap<>();
        Map<String, String> qualifierToCfiMap = new HashMap<>();
        for (Path xmlFile : xmlFiles) {
            LOGGER.info("Building modifier context data {}", xmlRootDir.relativize(xmlFile));
            ReportableEvent reportableEvent = TestPackModifierHelper.getReportableEvent(xmlFile);
            List<? extends ProductTaxonomy> productTaxonomyList = TestPackModifierHelper.getProductTaxonomyList(reportableEvent);
            String qualifier = TestPackModifierHelper.getQualifier(productTaxonomyList);
            if (qualifier != null) {
                xmlFileToQualifierMap.put(xmlFile, qualifier);

                String cfi = TestPackModifierHelper.getCfi(productTaxonomyList);
                if (cfi != null) {
                    qualifierToCfiMap.put(qualifier, cfi);
                }
            }
        }
        return new ModifierContext(xmlFileToQualifierMap, qualifierToCfiMap);
    }

    private void modifySampleIfApplicable(Path xmlFile, TestPackModifier testPackModifierModifier, String xmlContent, boolean dryRun) {
        try {
            boolean applicable = testPackModifierModifier.isApplicable(xmlFile, xmlContent, createXMLDocument(xmlFile));
            if (applicable) {
                String modifiedString = testPackModifierModifier.modify(xmlFile, xmlContent);

                writeFile(xmlFile, dryRun, xmlContent, modifiedString);

                XmlDom xmlDom = createXMLDocument(xmlFile);
                testPackModifierModifier.modify(xmlFile, xmlDom);
                String modifiedDocumentString = xmlDom.toString();

                writeFile(xmlFile, dryRun, modifiedString, modifiedDocumentString);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to modify sample ", xmlFile, e);
        }
    }

    private XmlDom createXMLDocument(Path xmlFile) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xmlFile.toFile());
            doc.getDocumentElement().normalize();
            return new XmlDom(doc);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Path> findXMLFiles() throws IOException {
        LOGGER.info("Loading all XML Files from {} ", xmlRootDir);
        try (Stream<Path> walk = Files.walk(xmlRootDir)) {
            return walk.filter(x -> x.getFileName().toString().endsWith(".xml"))
                    .collect(Collectors.toList());
        }
    }
}
