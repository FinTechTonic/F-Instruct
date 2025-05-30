package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.TestPackModifier;
import com.regnosys.drr.testpack.util.XmlDom;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;

/**
 * This modifier will always be isApplicable to all samples. This will force the framework to format the files in a way that is consistent.
 */
public class BaseModifier implements TestPackModifier {

    private final ModifierContext context;

    public BaseModifier(ModifierContext context) {
        this.context = context;
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xmlDom) throws XPathExpressionException {
        return true;
    }

    @Override
    public String modify(Path xmlFile, String xmlContent) throws XPathExpressionException {
        return xmlContent;
    }

    @Override
    public void modify(Path xmlFile, XmlDom xmlDom) throws XPathExpressionException {
    }

    public ModifierContext getContext() {
        return context;
    }
}
