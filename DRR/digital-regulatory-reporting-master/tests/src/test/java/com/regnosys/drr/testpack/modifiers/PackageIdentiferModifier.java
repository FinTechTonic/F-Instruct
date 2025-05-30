package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.TRADE_HEADER_XPATH;

@SuppressWarnings("unused") // instantiated reflectively
public class PackageIdentiferModifier extends BaseModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageIdentiferModifier.class);
    public static final String PACKAGE_IDENTIFIER_XPATH = TRADE_HEADER_XPATH + "/originatingPackage/packageIdentifier";
    public static final String PACKAGE_ID_SCHEMA_XPATH = PACKAGE_IDENTIFIER_XPATH + "/tradeId[@tradeIdScheme='http://sefco.com/package_id']";
    public static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9]{1,35}");

    public PackageIdentiferModifier(ModifierContext context) {
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
        List<Node> packageIDsWithUTISchema = xml.getList(PACKAGE_ID_SCHEMA_XPATH);

        if (utiTradeIdExists(packageIDsWithUTISchema)) {
            // check all trade IDs to see if they have the correct issuer/partyReference too
            List<Node> packageIdentifierNodes = xml.getList(PACKAGE_IDENTIFIER_XPATH);
            int i = 1;
            for (Node partyTradeIdentifierNode : packageIdentifierNodes) {
                Node tradeIdNode = xml.get(partyTradeIdentifierNode, "tradeId[@tradeIdScheme='http://sefco.com/package_id']");

                if (tradeIdNode != null) {
                    String tradeId = tradeIdNode.getTextContent();
                   if (!PATTERN.matcher(tradeId).matches()) {
                        // Update global UTI
                        // For Global UTI there should not be an issue (but optionally partyReference can be set)
                        String newUti = createtradeID35Char(xmlFileName, i);
                        tradeIdNode.setTextContent(newUti);
                    } else {
                        LOGGER.info("Not updating UTI");
                    }
                }
                i++;
            }
        }
    }

    private String createtradeID35Char(String xmlFileName, int index) {
        // e.g. DUMMYTRADEIDENTIFY01380084103`
        // First 20 chars should 18 chars + 2 numbers followed by up to 32 alpha numeric (max 52 chars)
        String onlyChars = xmlFileName
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();
        String truncatedTo18 = StringUtils.truncate(onlyChars, 18);

        StringBuilder alphaNumerics = new StringBuilder();
        char[] charArray = truncatedTo18.toCharArray();
        IntStream.range(0, charArray.length)
                .forEach(i -> alphaNumerics.insert(0, charArray[i] + "" + i));

        alphaNumerics.insert(0, truncatedTo18);
        String tradeID = alphaNumerics.toString();
        if(tradeID.length()>35){
            return tradeID.substring(0,35);
        }
        return tradeID;
    }

}
