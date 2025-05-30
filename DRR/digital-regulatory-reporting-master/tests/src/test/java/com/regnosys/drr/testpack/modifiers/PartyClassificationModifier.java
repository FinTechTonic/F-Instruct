package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import drr.regulation.common.FinancialSectorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.*;

@SuppressWarnings("unused") // instantiated reflectively
public class PartyClassificationModifier extends BaseModifier {

    public static final String INDUSTRY_CLASSIFICATION_SCHEME_VALUE = "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector";
    public static final String INDUSTRY_CLASSIFICATION_SCHEME_NAME = "industryClassificationScheme";
    private static final Logger LOGGER = LoggerFactory.getLogger(PartyClassificationModifier.class);

    public PartyClassificationModifier(ModifierContext context) {
        super(context);
    }

    private static void modifyClassification(Path xmlFile, XmlDom xml, List<Node> partyNodes, String id) throws XPathExpressionException {
        Node partyNode = getPartyNode(partyNodes, id);
        List<Node> classificationNodes = xml.getList(partyNode, "classification");

        if (classificationNodes.stream().noneMatch(cn -> attributeEquals(cn, INDUSTRY_CLASSIFICATION_SCHEME_NAME, INDUSTRY_CLASSIFICATION_SCHEME_VALUE))) {
            FinancialSectorEnum financialSector = randomlySelectClassification(xmlFile, id);
            LOGGER.info("Adding missing classification {} for party {}", financialSector, id);
            Node newClassificationNode = xml.createNode("classification", Map.of(INDUSTRY_CLASSIFICATION_SCHEME_NAME, INDUSTRY_CLASSIFICATION_SCHEME_VALUE), financialSector.toString());
            String siblingNameToAddAfter = getClassificationSiblingNameToAddAfter(xml, partyNode, classificationNodes);
            xml.addAfter(partyNode, siblingNameToAddAfter, newClassificationNode);
        }
    }

    private static String getClassificationSiblingNameToAddAfter(XmlDom xml, Node partyNode, List<Node> classificationNodes) throws XPathExpressionException {
        if (!classificationNodes.isEmpty()) {
            return "classification";
        } else if (xml.get(partyNode, "partyName") != null) {
            return "partyName";
        } else {
            return "partyId";
        }
    }

    private static FinancialSectorEnum randomlySelectClassification(Path xmlFile, String id) {
        String fileNameAndId = xmlFile.getFileName().toString() + id;
        int hashCode = Math.abs(fileNameAndId.hashCode());
        int modulus = hashCode % 7;
        switch (modulus) {
            case 0:
                return FinancialSectorEnum.AIFD;
            case 1:
                return FinancialSectorEnum.CDTI;
            case 2:
                return FinancialSectorEnum.CSDS;
            case 3:
                return FinancialSectorEnum.INUN;
            case 4:
                return FinancialSectorEnum.INVF;
            case 5:
                return FinancialSectorEnum.ORPI;
            case 6:
                return FinancialSectorEnum.UCIT;
        }
        throw new IllegalStateException("Failed to randomly select FinancialSectorEnum");
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        List<Node> partyNodes = xml.getList(PARTY_XPATH);
        if (partyNodes.isEmpty()) {
            LOGGER.warn("Sample {} has no parties", xmlFile.getFileName());
            return false;
        }
        List<String> partyReferenceHrefs = getCounterpartyHrefs(xml);
        if (partyReferenceHrefs.size() != 2) {
            LOGGER.warn("Sample {} does not have 2 counterparties, but has {}", xmlFile.getFileName(), partyReferenceHrefs.size());
            return false;
        }
        return true;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        List<String> partyReferenceHrefs = getCounterpartyHrefs(xml);
        List<Node> partyNodes = xml.getList(PARTY_XPATH);
        modifyClassification(xmlFile, xml, partyNodes, partyReferenceHrefs.get(0));
        modifyClassification(xmlFile, xml, partyNodes, partyReferenceHrefs.get(1));
    }
}
