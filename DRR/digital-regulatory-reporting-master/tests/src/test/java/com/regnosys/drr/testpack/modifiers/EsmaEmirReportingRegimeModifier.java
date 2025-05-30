package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.TRADE_HEADER_XPATH;

@SuppressWarnings("unused") // instantiated reflectively
public class EsmaEmirReportingRegimeModifier extends BaseModifier {

    public static final String REPORTING_REGIME_XPATH = TRADE_HEADER_XPATH + "/partyTradeInformation/reportingRegime";

    public EsmaEmirReportingRegimeModifier(ModifierContext context) {
        super(context);
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        List<Node> reportingRegimeNodes = xml.getList(REPORTING_REGIME_XPATH);
        return !reportingRegimeNodes.isEmpty();
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        List<Node> reportingRegimeNodes = xml.getList(REPORTING_REGIME_XPATH);
        for (Node reportingRegimeNode : reportingRegimeNodes) {
            modify(xmlFile, reportingRegimeNode, xml);
        }
    }

    private void modify(Path xmlFile, Node reportingRegimeNode, XmlDom xml) throws XPathExpressionException {
        Node supervisoryBody = xml.get(reportingRegimeNode, "supervisorRegistration/supervisoryBody");
        Node name = xml.get(reportingRegimeNode, "name");
        if ((name != null && name.getTextContent().equals("EMIR")) || (supervisoryBody != null && supervisoryBody.getTextContent().equals("ESMA"))) {
            // 8 required attributes in reportingRegimeNode
            // 1. name
            // 2. supervisorRegistration
            modifyNameAndSupervisoryBody(reportingRegimeNode, xml);

            // 3. reportingRole
            modifyReportingRole(reportingRegimeNode, xml);

            // 4. reportingPurpose
            modifyReportingPurpose(reportingRegimeNode, xml);

            // 5. mandatorilyClearable
            modifyMandatorilyClearable(reportingRegimeNode, xml);

            // 6. exceedsClearingThreshold
            // 7.  entityClassification
            modifyEntityClassification(xmlFile.getFileName().toString(), reportingRegimeNode, xml);

            // 8. tradePartyRelationshipType
            modifyTradePartyRelationshipType(reportingRegimeNode, xml);
        }
    }

    private void modifyTradePartyRelationshipType(Node reportingRegimeNode, XmlDom xml) throws XPathExpressionException {
        if (xml.get("partyEntityClassification") != null) {
            xml.getOrCreate(reportingRegimeNode, xml, "partyEntityClassification", "tradePartyRelationshipType", "Intragroup");
        }

        if (xml.get("entityClassification") != null) {
            xml.getOrCreate(reportingRegimeNode, xml, "entityClassification", "tradePartyRelationshipType", "Intragroup");
        }
    }

    private void modifyReportingPurpose(Node reportingRegimeNode, XmlDom xml) throws XPathExpressionException {
        xml.getOrCreate(reportingRegimeNode, xml, "reportingRole", "reportingPurpose", "PrimaryEconomicTerms");
    }

    private void modifyMandatorilyClearable(Node reportingRegimeNode, XmlDom xml) throws XPathExpressionException {
        xml.getOrCreate(reportingRegimeNode, xml, "reportingPurpose", "mandatorilyClearable", "false");
    }

    private void modifyReportingRole(Node reportingRegimeNode, XmlDom xml) throws XPathExpressionException {
        Node reportingRoleNode = xml.getOrCreate(reportingRegimeNode, xml, "supervisorRegistration", "reportingRole", "ReportingParty");
        String textContent = reportingRoleNode.getTextContent();
        if (!(textContent.equals("Counterparty") || textContent.equals("VoluntaryParty") || textContent.equals("FullyDelegated"))) {
            reportingRoleNode.setTextContent("ReportingParty");
        }
    }

    private void modifyEntityClassification(String xmlFileName, Node reportingRegimeNode, XmlDom xml) throws XPathExpressionException {
        // Only need entityClassification when partyEntityClassification is not set
        if (xml.get(reportingRegimeNode, "partyEntityClassification") != null) {
            return;
        }

        Node exceedsClearingThresholdNode = xml.getOrCreate(reportingRegimeNode, xml, "mandatorilyClearable", "exceedsClearingThreshold", "true");

        Node entityClassificationNode = xml.getOrCreate(reportingRegimeNode, xml, "exceedsClearingThreshold", "entityClassification", "Financial");
        xml.addAttributes(entityClassificationNode, Map.of("entityClassificationScheme", "http://www.fpml.org/coding-scheme/esma-entity-classification"));

        String entityClassification = entityClassificationNode.getTextContent();
        if ("Financial".equals(entityClassification)) {
            exceedsClearingThresholdNode.setTextContent("true");
        }

        if ("NonFinancial".equals(entityClassification)) {
            String randomTrueFalse = Math.floorMod(xmlFileName.hashCode(), 2) == 0 ? "true" : "false";
            exceedsClearingThresholdNode.setTextContent(randomTrueFalse);
        }
    }

    private void modifyNameAndSupervisoryBody(Node reportingRegimeNode, XmlDom xml) throws XPathExpressionException {
        Node nameNode = xml.get(reportingRegimeNode, "name");
        if (nameNode == null) {
            nameNode = xml.addFirst(reportingRegimeNode, xml.createNode("name"));
        }

        Node supervisoryBodyNode = xml.get(reportingRegimeNode, "supervisorRegistration/supervisoryBody");
        if (supervisoryBodyNode == null) {
            Node supervisorRegistrationNode = xml.addAfter(reportingRegimeNode, "name", xml.createNode("supervisorRegistration"));
            supervisoryBodyNode = xml.addFirst(supervisorRegistrationNode, xml.createNode("supervisoryBody"));
        }

        String name = nameNode.getTextContent();
        String supervisoryBody = supervisoryBodyNode.getTextContent();

        if ("ESMA".equals(supervisoryBody)) {
            nameNode.setTextContent("EMIR");
        }
        if ("EMIR".equals(name)) {
            supervisoryBodyNode.setTextContent("ESMA");
        }
    }
}
