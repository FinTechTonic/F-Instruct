package com.regnosys.drr.testpack.modifiers;

import cdm.base.staticdata.asset.common.ProductTaxonomy;
import cdm.base.staticdata.asset.common.Taxonomy;
import cdm.base.staticdata.asset.common.TaxonomySourceEnum;
import cdm.base.staticdata.asset.common.TaxonomyValue;
import cdm.event.common.Instruction;
import cdm.event.common.Trade;
import cdm.event.common.TradeState;
import cdm.event.common.metafields.ReferenceWithMetaTradeState;
import cdm.event.workflow.EventInstruction;
import cdm.event.workflow.WorkflowStep;
import cdm.product.template.ContractualProduct;
import cdm.product.template.Product;
import cdm.product.template.TradableProduct;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.drr.testpack.util.XmlDom;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.rosetta.model.metafields.FieldWithMetaString;
import drr.regulation.common.ReportableEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestPackModifierHelper {

    public static final String TRADE_HEADER_XPATH = "//*/tradeHeader";
    public static final String TRADE_XPATH = "//*/trade";
    public static final String GENERIC_PRODUCT_XPATH = "//*/genericProduct";
    public static final String PARTY_XPATH = "/nonpublicExecutionReport/party";
    public static final String LEI_SCHEME = "http://www.fpml.org/coding-scheme/external/iso17442";
    public static final String PARTY1_LEI = "DUMMY0000000000LEI01";
    public static final String PARTY2_LEI = "DUMMY0000000000LEI02";
    public static final String PARTY3_LEI = "DUMMY0000000000LEI03";
    public static final String PARTY4_LEI = "DUMMY0000000000LEI04";
    public static final String PARTY5_LEI = "DUMMY0000000000LEI05";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestPackModifierHelper.class);

    static List<String> getCounterpartyHrefs(XmlDom xml) throws XPathExpressionException {
        List<Node> allPartyReferences = xml.getList("//payerPartyReference | //receiverPartyReference | //buyerPartyReference | //sellerPartyReference");
        List<String> partyReferenceHrefs = allPartyReferences.stream()
                .map(partyRefNode -> partyRefNode.getAttributes().getNamedItem("href").getTextContent())
                .distinct()
                .collect(Collectors.toList());
        return partyReferenceHrefs;
    }

    static Node getPartyNode(List<Node> partyNodes, String id) {
        return partyNodes.stream()
                .filter(p -> attributeEquals(p, "id", id))
                .findFirst()
                .orElseThrow();
    }

    static String getTextContent(XmlDom xml, Node node, String xpath) throws XPathExpressionException {
        return Optional.ofNullable(xml.get(node, xpath)).map(Node::getTextContent).orElse(null);
    }

    static boolean textContentEquals(XmlDom xml, Node node, String xpath, String textContent) throws XPathExpressionException {
        return textContentEquals(xml, node, xpath, Collections.singletonList(textContent));
    }

    static boolean textContentEquals(XmlDom xml, Node node, String xpath, List<String> textContent) throws XPathExpressionException {
        return textContent.contains(getTextContent(xml, node, xpath));
    }

    static boolean attributeEquals(Node node, String attributeName, String attributeContent) {
        return attributeContent.equals(getAttributeContent(node, attributeName));
    }

    static String getAttributeContent(Node node, String attributeName) {
        return Optional.ofNullable(node)
                .map(Node::getAttributes)
                .map(namedNodeMap -> namedNodeMap.getNamedItem(attributeName))
                .map(Node::getTextContent)
                .orElse(null);
    }

    public static String getQualifier(List<? extends ProductTaxonomy> productTaxonomyList) {
        return productTaxonomyList.stream()
                .filter(t -> t.getSource() == TaxonomySourceEnum.ISDA)
                .map(ProductTaxonomy::getProductQualifier)
                .filter(Objects::nonNull)
                .map(q -> q.replace("_", ":"))
                .findFirst()
                .orElse(null);
    }

    public static String getCfi(List<? extends ProductTaxonomy> productTaxonomyList) {
        return productTaxonomyList.stream()
                .filter(t -> t.getSource() == TaxonomySourceEnum.CFI)
                .map(Taxonomy::getValue)
                .filter(Objects::nonNull)
                .map(TaxonomyValue::getName)
                .map(FieldWithMetaString::getValue)
                .findFirst()
                .orElse(null);
    }

    public static ReportableEvent getReportableEvent(Path xmlFile) {
        try {
            Path outputFile = Paths.get(xmlFile.toString()
                    .replace("cdm-sample-files", "result-json-files")
                    .replace(".xml", ".json"));
            String json = Files.readString(Paths.get(outputFile.toUri()));
            ObjectMapper objectMapper = RosettaObjectMapper.getNewMinimalRosettaObjectMapper();
            return objectMapper.readValue(json, ReportableEvent.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<? extends ProductTaxonomy> getProductTaxonomyList(ReportableEvent reportableEvent) {
        return getBeforeTradeState(reportableEvent)
                .map(TradeState::getTrade)
                .map(Trade::getTradableProduct)
                .map(TradableProduct::getProduct)
                .map(Product::getContractualProduct)
                .map(ContractualProduct::getProductTaxonomy)
                .orElse(Collections.emptyList());
    }

    static Optional<TradeState> getBeforeTradeState(ReportableEvent reportableEvent) {
        return Optional.ofNullable(reportableEvent)
                .map(ReportableEvent::getOriginatingWorkflowStep)
                .map(WorkflowStep::getProposedEvent)
                .map(EventInstruction::getInstruction)
                .orElse(Collections.emptyList())
                .stream().findFirst()
                .map(Instruction::getBefore)
                .map(ReferenceWithMetaTradeState::getValue);
    }
}
