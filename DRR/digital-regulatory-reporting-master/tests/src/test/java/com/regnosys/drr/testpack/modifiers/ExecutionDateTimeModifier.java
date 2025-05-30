package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.TRADE_HEADER_XPATH;

@SuppressWarnings("unused") // instantiated reflectively
public class ExecutionDateTimeModifier extends BaseModifier {

    public static final String PARTY_TRADE_INFORMATION_XPATH = TRADE_HEADER_XPATH + "/partyTradeInformation";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionDateTimeModifier.class);
    public static final String TRADE_DATE_XPATH = "//*/tradeDate";

    public ExecutionDateTimeModifier(ModifierContext context) {
        super(context);
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        return xml.get("//*/executionDateTime") == null && xml.get(TRADE_DATE_XPATH) != null;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        List<Node> partyTradeInformationNodes = xml.getList(PARTY_TRADE_INFORMATION_XPATH);
        if (!partyTradeInformationNodes.isEmpty()) {
            Node partyTradeInformationNode = partyTradeInformationNodes.get(0);
            LocalDate tradeDate = LocalDate.parse(xml.get(TRADE_DATE_XPATH).getTextContent());
            ZonedDateTime zonedDateTime = tradeDate.atStartOfDay().atZone(ZoneOffset.UTC);
            String formattedDateTime = zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
            Node executionDateTimeNode = xml.createNode("executionDateTime", formattedDateTime);
            setExecutionDateTime(xml, partyTradeInformationNode, executionDateTimeNode);
        }
    }

    private static void setExecutionDateTime(XmlDom xml, Node partyTradeInformationNode, Node reportingRegimeNode) {
        NodeList childNodes = partyTradeInformationNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (List.of("allocationStatus", "intentToClear", "clearingStatus", "collateralizationType", "reportingRegime").contains(item.getNodeName())) {
                partyTradeInformationNode.insertBefore(reportingRegimeNode, item);
                return;
            }
        }
    }
}
