package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.GENERIC_PRODUCT_XPATH;
import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.TRADE_XPATH;

@SuppressWarnings("unused") // instantiated reflectively
public class MasterAgreementModifier extends BaseModifier {

    public static final String DOCUMENTATION_XPATH = TRADE_XPATH + "/documentation";
    public static final String MASTER_AGREEMENT_XPATH = DOCUMENTATION_XPATH + "/masterAgreement";
    public static final String MASTER_AGREEMENT_TYPE_XPATH = MASTER_AGREEMENT_XPATH + "/masterAgreementType";
    public static final String MASTER_AGREEMENT_DATE_XPATH = MASTER_AGREEMENT_XPATH + "/masterAgreementDate";
    public static final String MASTER_AGREEMENT_VERSION_XPATH = MASTER_AGREEMENT_XPATH + "/masterAgreementVersion";
    private static final Logger LOGGER = LoggerFactory.getLogger(MasterAgreementModifier.class);

    public MasterAgreementModifier(ModifierContext context) {
        super(context);
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        return xml.get(TRADE_XPATH) != null
                && (xml.get(MASTER_AGREEMENT_TYPE_XPATH) == null || xml.get(MASTER_AGREEMENT_VERSION_XPATH) == null);
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        if (xml.get(DOCUMENTATION_XPATH) == null) {
            Node tradeNode = xml.get(TRADE_XPATH);
            xml.addLast(tradeNode, xml.createNode("documentation"));
        }
        Node documentationNode = xml.get(DOCUMENTATION_XPATH);
        if (xml.get(MASTER_AGREEMENT_XPATH) == null) {
            xml.addFirst(documentationNode, xml.createNode("masterAgreement"));
        }
        Node masterAgreemenNode = xml.get(MASTER_AGREEMENT_XPATH);
        if (xml.get(MASTER_AGREEMENT_TYPE_XPATH) == null) {
            xml.addLast(masterAgreemenNode, xml.createNode("masterAgreementType", "ISDA"));
        }
        if (xml.get(MASTER_AGREEMENT_VERSION_XPATH) == null && xml.get(MASTER_AGREEMENT_DATE_XPATH) == null) {
            xml.addLast(masterAgreemenNode, xml.createNode("masterAgreementVersion", "2002"));
        }
    }
}
