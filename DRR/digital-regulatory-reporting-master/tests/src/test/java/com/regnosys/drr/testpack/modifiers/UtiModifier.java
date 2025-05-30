package com.regnosys.drr.testpack.modifiers;

import com.google.common.collect.Iterables;
import com.regnosys.drr.testpack.util.XmlDom;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.TRADE_HEADER_XPATH;

@SuppressWarnings("unused") // instantiated reflectively
public class UtiModifier extends BaseModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtiModifier.class);
    public static final String PARTY_TRADE_IDENTIFIER_XPATH = TRADE_HEADER_XPATH + "/partyTradeIdentifier";
    public static final String GLOBAL_UTI_SCHEMA_XPATH = PARTY_TRADE_IDENTIFIER_XPATH + "/tradeId[@tradeIdScheme='http://www.fpml.org/coding-scheme/external/unique-transaction-identifier']";
    public static final Pattern GLOBAL_UTI_PATTERN = Pattern.compile("[A-Z0-9]{18}[0-9]{2}[A-Z0-9]{0,32}");
    public static final Pattern ISSUER_UTI_PATTERN = Pattern.compile("[A-Z0-9]{18}[0-9]{2}");

    public static final String INTERNAL_REF_SCHEMA_XPATH = PARTY_TRADE_IDENTIFIER_XPATH + "/tradeId[@tradeIdScheme='http://www.dtcc.com/internal-reference-id']";

    public static final Pattern INTERNAL_REF_PATTERN = Pattern.compile("[a-zA-Z0-9]{1,72}");

    public UtiModifier(ModifierContext context) {
        super(context);
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        // some samples like terminations do not have a trade (they have "originalTrade")
        List<Node> tradeHeaderNodes = xml.getList(TRADE_HEADER_XPATH);
        if (tradeHeaderNodes.isEmpty()) {
            return false;
        }
        return true;
    }

    private static boolean utiTradeIdExists(List<Node> tradeIDsWithUTISchema) {
        return !tradeIDsWithUTISchema.isEmpty();
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        String xmlFileName = xmlFile.getFileName().toString();
        Node tradeHeaderNode = xml.get(TRADE_HEADER_XPATH);
        modifyUtiTradeID(xmlFile, xml, xmlFileName, tradeHeaderNode);
        modifySecondaryTransactionIdentifier(xmlFile, xml, xmlFileName, tradeHeaderNode);

    }

    private void modifyUtiTradeID(Path xmlFile, XmlDom xml, String xmlFileName, Node tradeHeaderNode) throws XPathExpressionException {
        List<Node> tradeIDsWithUTISchema = xml.getList(GLOBAL_UTI_SCHEMA_XPATH);

        if (utiTradeIdExists(tradeIDsWithUTISchema)) {
            // check all trade IDs to see if they have the correct issuer/partyReference too
            List<Node> partyTradeIdentifierNodes = xml.getList(PARTY_TRADE_IDENTIFIER_XPATH);
            int i = 1;
            for (Node partyTradeIdentifierNode : partyTradeIdentifierNodes) {
                Node issuerNode = xml.get(partyTradeIdentifierNode, "issuer");
                Node partyReferenceNode = xml.get(partyTradeIdentifierNode, "partyReference");
                Node utiTradeIdNode = xml.get(partyTradeIdentifierNode, "tradeId[@tradeIdScheme='http://www.fpml.org/coding-scheme/external/unique-transaction-identifier']");

                if (utiTradeIdNode != null) {
                    String tradeId = utiTradeIdNode.getTextContent();
                    if (issuerNode != null && utiTradeIdNode != null && !ISSUER_UTI_PATTERN.matcher(tradeId).matches()) {
                        // Update issuer
                        List<Node> leiNodes = xml.getList("//*/partyId[@partyIdScheme='http://www.fpml.org/coding-scheme/external/iso17442']");
                        Node leiPartyNode = leiNodes.get(i%2);
                        String lei = leiPartyNode.getTextContent();
                        issuerNode.setTextContent(lei);
                        // Update issuer scheme
                        NamedNodeMap attributes = issuerNode.getAttributes();
                        Optional.ofNullable(attributes.getNamedItem("issuerIdScheme"))
                                .ifPresent(item -> item.setTextContent("http://www.fpml.org/coding-scheme/external/issuer-identifier"));
                        // Update issuer UTI
                        String newUti = createUti20Char(xmlFileName, i);
                        utiTradeIdNode.setTextContent(newUti);
                    } else if (issuerNode == null && !GLOBAL_UTI_PATTERN.matcher(tradeId).matches()) {
                        // Update global UTI
                        // For Global UTI there should not be an issue (but optionally partyReference can be set)
                        String newUti = createUti52Char(xmlFileName, i);
                        utiTradeIdNode.setTextContent(newUti);
                    } else {
                        LOGGER.info("Not updating UTI");
                    }
                }
                i++;
            }
        } else {
            // Add new global UTI
            Node newPartyTradeIdentifier = xml.createNode("partyTradeIdentifier");
            xml.addFirst(tradeHeaderNode, newPartyTradeIdentifier);

            String newUti = createUti52Char(xmlFile.getFileName().toString(), 0);

            Node tradeIdNode = xml.createNode(
                    "tradeId",
                    Map.of("tradeIdScheme", "http://www.fpml.org/coding-scheme/external/unique-transaction-identifier"),
                    newUti);
            xml.addFirst(newPartyTradeIdentifier, tradeIdNode);

            // find first payerPartyReference
            // if we got one, grab its href
            List<Node> allParties = xml.getList("//payerPartyReference | //buyerPartyReference | //partyReference");
            Node allParty = Iterables.getLast(allParties);
            String partyId = allParty.getAttributes().getNamedItem("href").getTextContent();
            // else
            // find first buyerPartyReference
            // if we got one, grab its href

            Node partyReferenceNode = xml.createNode("partyReference", Map.of("href", partyId));
            xml.addFirst(newPartyTradeIdentifier, partyReferenceNode);
        }
    }

    private void modifySecondaryTransactionIdentifier(Path xmlFile, XmlDom xml, String xmlFileName, Node tradeHeaderNode) throws XPathExpressionException {
        List<Node> tradeIDsWithUTISchema = xml.getList(INTERNAL_REF_SCHEMA_XPATH);

        if (utiTradeIdExists(tradeIDsWithUTISchema)) {
            // check all trade IDs to see if they have the correct issuer/partyReference too
            List<Node> partyTradeIdentifierNodes = xml.getList(PARTY_TRADE_IDENTIFIER_XPATH);
            int i = 1;
            for (Node partyTradeIdentifierNode : partyTradeIdentifierNodes) {
                Node utiTradeIdNode = xml.get(partyTradeIdentifierNode, "tradeId[@tradeIdScheme='http://www.dtcc.com/internal-reference-id']");

                if (utiTradeIdNode != null) {
                    String tradeId = utiTradeIdNode.getTextContent();
                    if (utiTradeIdNode != null && (!INTERNAL_REF_PATTERN.matcher(tradeId).matches() || tradeId.length()<20 || tradeId.length() > 72)) {
                        // Update issuer
                        String newUti = createUti20Char(xmlFileName, i);
                        utiTradeIdNode.setTextContent(newUti);
                    }
                }
                i++;
            }
        }
    }

    private String createUti52Char(String xmlFileName, int index) {
        // e.g. DUMMYTRADEIDENTIFY01380084103`
        // First 20 chars should 18 chars + 2 numbers followed by up to 32 alpha numeric (max 52 chars)
        String onlyChars = xmlFileName
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();
        String truncatedTo18 = StringUtils.truncate(onlyChars, 18);
        String paddedTo20With2Numbers = createUti20Char(xmlFileName, index);

        StringBuilder alphaNumerics = new StringBuilder();
        char[] charArray = truncatedTo18.toCharArray();
        IntStream.range(0, charArray.length)
                .forEach(i -> alphaNumerics.insert(0, charArray[i] + "" + i));

        alphaNumerics.insert(0, paddedTo20With2Numbers);
        String uti52Char = alphaNumerics.toString();
        if(uti52Char.length()>52){
            return uti52Char.substring(0,52);
        }
        return uti52Char;
    }

    private String createUti20Char(String xmlFileName, int index) {
        // e.g. DUMMYTRADEIDENTIFY01380084103`
        // First 20 chars should 18 chars + 2 numbers
        String onlyChars = xmlFileName
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();
        String truncatedTo18 = StringUtils.truncate(onlyChars, 18);
        String paddedTo18 = StringUtils.rightPad(truncatedTo18, 18, 'X');
        String paddedTo20With2Numbers = paddedTo18 + "0" + index;
        return paddedTo20With2Numbers.toString();
    }
}
