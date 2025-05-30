package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.TRADE_HEADER_XPATH;

@SuppressWarnings("unused") // instantiated reflectively
public class EventTimestampModifier extends BaseModifier {

    private static final String MESSAGE_ID_XPATH = "//*/messageId";
    private static final int MAX_IDENTIFIER_LENGTH = 52;

    public static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9]{1,52}");


    public EventTimestampModifier(ModifierContext context) {
        super(context);
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        List<Node> tradeHeaderNodes = xml.getList(MESSAGE_ID_XPATH);
        if (tradeHeaderNodes.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        Node messageIdNode = xml.get(MESSAGE_ID_XPATH);
        String messageId = messageIdNode.getTextContent();
        String filteredMessageiD = messageId.replaceAll("[^a-zA-Z0-9]+","");
        if(filteredMessageiD.length()>MAX_IDENTIFIER_LENGTH){
            filteredMessageiD = filteredMessageiD.substring(0,MAX_IDENTIFIER_LENGTH);
        }
        messageIdNode.setTextContent(filteredMessageiD);
    }

    private String create52CharId(String xmlFileName, String messageId) {
        // e.g. DUMMYTRADEIDENTIFY01380084103`
        // First 20 chars should 18 chars + 2 numbers followed by up to 32 alpha numeric (max 52 chars)
        if(!PATTERN.matcher(messageId).matches()) {
            String onlyChars = xmlFileName
                    .replaceAll("[^a-zA-Z]", "")
                    .toUpperCase();
            String truncatedTo18 = StringUtils.truncate(onlyChars, 18);

            StringBuilder alphaNumerics = new StringBuilder();
            char[] charArray = truncatedTo18.toCharArray();
            IntStream.range(0, charArray.length)
                    .forEach(i -> alphaNumerics.insert(0, charArray[i] + "" + i));

            alphaNumerics.insert(0, truncatedTo18);
            messageId = alphaNumerics.toString();
        }
        if(messageId.length()>MAX_IDENTIFIER_LENGTH){
            return messageId.substring(0,MAX_IDENTIFIER_LENGTH);
        }
        return messageId;
    }

}
