package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;

@SuppressWarnings("unused") // instantiated reflectively
public class IsCorrectionModifier extends BaseModifier {

    public static final String IS_CORRECTION_XPATH = "//*/isCorrection";

    public IsCorrectionModifier(ModifierContext context) {
        super(context);
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        return xml.get("//*/isCorrection") == null && xml.get("nonpublicExecutionReport") != null;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        Node topLevelElement = xml.get("nonpublicExecutionReport");
        Node isCorrection = xml.createNode("isCorrection", "false");
        xml.addAfter(topLevelElement, "header", isCorrection);
    }
}
