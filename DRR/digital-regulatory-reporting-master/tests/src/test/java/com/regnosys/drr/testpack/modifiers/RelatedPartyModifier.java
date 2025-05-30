package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.TRADE_HEADER_XPATH;
import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.textContentEquals;

@SuppressWarnings("unused") // instantiated reflectively
public class RelatedPartyModifier extends BaseModifier {

    public static final String PARTY_TRADE_INFORMATION_XPATH = TRADE_HEADER_XPATH + "/partyTradeInformation";
    public static final String RELATED_PARTY_XPATH = PARTY_TRADE_INFORMATION_XPATH + "/relatedParty";
    private static final Logger LOGGER = LoggerFactory.getLogger(RelatedPartyModifier.class);

    public RelatedPartyModifier(ModifierContext context) {
        super(context);
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        List<Node> partyRoleNodes = xml.getList(RELATED_PARTY_XPATH);
        if (partyRoleNodes.isEmpty()) {
            LOGGER.warn("Sample {} has no party roles", xmlFile.getFileName());
            return false;
        }
        return true;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        List<Node> partyTradeInformationNodes = xml.getList(PARTY_TRADE_INFORMATION_XPATH);
        for (Node partyTradeInformationNode : partyTradeInformationNodes) {
            List<Node> relatedPartyNodes = xml.getList(partyTradeInformationNode, "relatedParty");
            for (Node relatedPartyNode : relatedPartyNodes) {
                // Specified in reportingSide, remove from samples to avoid confusion
                if (textContentEquals(xml, relatedPartyNode, "role", List.of("ReportingParty", "Counterparty", "DataSubmitter"))) {
                    partyTradeInformationNode.removeChild(relatedPartyNode);
                }
            }
        }
    }
}
