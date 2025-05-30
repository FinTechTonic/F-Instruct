package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.Map;

@SuppressWarnings("unused") // instantiated reflectively
public class ChangeTopLevelElementModifier extends BaseModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeTopLevelElementModifier.class);

    public ChangeTopLevelElementModifier(ModifierContext context) {
        super(context);
    }

    private static void addAttribute(XmlDom xmlDom, Node nonpublicExecutionReport, String name, String value) {
        xmlDom.addAttributes(nonpublicExecutionReport, Map.of(name, value));
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xmlDom) throws XPathExpressionException {
        return xmlDom.get("nonpublicExecutionReport") != null;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xmlDom) throws XPathExpressionException {
        Node nonpublicExecutionReport = xmlDom.get("nonpublicExecutionReport");
        addAttribute(xmlDom, nonpublicExecutionReport, "xmlns", "http://www.fpml.org/FpML-5/recordkeeping");
        addAttribute(xmlDom, nonpublicExecutionReport, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

        String pathStr = xmlFile.toString();
        if (pathStr.contains("/fpml-5-13/")) {
            addAttribute(xmlDom, nonpublicExecutionReport, "fpmlVersion", "5-13");
            addAttribute(xmlDom, nonpublicExecutionReport, "xsi:schemaLocation", "http://www.fpml.org/FpML-5/recordkeeping /schemas/fpml-5-13/recordkeeping/fpml-main-5-13.xsd");
        } else if (pathStr.contains("/fpml-5-10/")) {
            addAttribute(xmlDom, nonpublicExecutionReport, "fpmlVersion", "5-10");
            addAttribute(xmlDom, nonpublicExecutionReport, "xsi:schemaLocation", "http://www.fpml.org/FpML-5/recordkeeping /schemas/fpml-5-10/recordkeeping/fpml-main-5-10.xsd");
        } else {
            LOGGER.warn("Unexpected path", xmlFile);
        }
    }
}
