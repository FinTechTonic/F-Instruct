package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.*;

@SuppressWarnings("unused") // instantiated reflectively
public class PartyLeiModifier extends BaseModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyLeiModifier.class);

    public PartyLeiModifier(ModifierContext context) {
        super(context);
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
        modifyLei(xml, partyNodes, partyReferenceHrefs.get(0), PARTY1_LEI);
        modifyLei(xml, partyNodes, partyReferenceHrefs.get(1), PARTY2_LEI);
    }

    private static void modifyLei(XmlDom xml, List<Node> partyNodes, String id, String validLei) throws XPathExpressionException {
        Node partyNode = getPartyNode(partyNodes, id);
        Node leiPartyNode = xml.getList(partyNode, "partyId").stream()
                .filter(p -> attributeEquals(p, "partyIdScheme", LEI_SCHEME))
                .findFirst()
                .orElse(null);
        if (leiPartyNode != null) {
            Pattern pattern = Pattern.compile("[A-Z0-9]{18,18}[0-9]{2,2}");
            String existingLei = leiPartyNode.getTextContent();
            if (!pattern.matcher(existingLei).matches()) {
                LOGGER.info("Replacing invalid LEI {} with {}", existingLei, validLei);
                leiPartyNode.setTextContent(validLei);
                replaceAll(xml, existingLei, validLei);
            }
        } else {
            LOGGER.info("Adding missing partyId LEI {}", validLei);
            Node newPartyIdNode = xml.createNode("partyId", Map.of("partyIdScheme", LEI_SCHEME), validLei);
            xml.addFirst(partyNode, newPartyIdNode);
        }
    }

    private static void replaceAll(XmlDom xml, String find, String replace) throws XPathExpressionException {
        List<Node> nodes = xml.getList("//*[text()='" + find + "']");
        nodes.forEach(node -> {
            String nodeName = node.getNodeName();
            LOGGER.info("Found {} at node {}, replacing with {}", find, nodeName, replace);
            if (nodeName.equals("sentBy")) {
                Optional.ofNullable(node.getAttributes().getNamedItem("messageAddressScheme")).ifPresent(a -> a.setTextContent(LEI_SCHEME));
            }
            node.setTextContent(replace);
        });
    }
}
