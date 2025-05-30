package com.regnosys.drr.testpack.util;

import com.google.common.collect.Iterables;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlDom {

    private final Document document;

    public XmlDom(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public List<Node> getList(String xpath) throws XPathExpressionException {
        return getList(document, xpath);
    }

    public Node get(String xpath) throws XPathExpressionException {
        return get(document, xpath);
    }

    public List<Node> getList(Node relativeNode, String xpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(relativeNode, XPathConstants.NODESET);
        return toList(nodeList);
    }

    public Node get(Node relativeNode, String xpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.compile(xpath).evaluate(relativeNode, XPathConstants.NODE);
        return node;
    }

    public Node createNode(String newElementName) {
        return document.createElement(newElementName);
    }

    public Node createNode(String newElementName, Map<String, String> attributes) {
        Node newChildNode = document.createElement(newElementName);
        addAttributes(newChildNode, attributes);
        return newChildNode;
    }

    public void addAttributes(Node node, Map<String, String> attributes) {
        for (String attrName : attributes.keySet()) {
            Attr attribute = document.createAttribute(attrName);
            attribute.setValue(attributes.get(attrName));
            node.getAttributes().setNamedItem(attribute);
        }
    }

    public Node createNode(String newElementName, Map<String, String> attributes, String value) {
        Node newChildNode = createNode(newElementName, attributes);
        newChildNode.setTextContent(value);
        return newChildNode;
    }

    public Node createNode(String newElementName, String value) {
        Node newChildNode = createNode(newElementName);
        newChildNode.setTextContent(value);
        return newChildNode;
    }

    public Node addFirst(Node parent, Node child) {
        parent.insertBefore(child, parent.getFirstChild());
        return child;
    }

    public Node addAfter(Node parent, String siblingNameToAddAfter, Node child) {
        List<Node> siblingNodeToAddAfterList = toList(parent.getChildNodes()).stream()
                .filter(n -> n.getNodeName().equals(siblingNameToAddAfter))
                .collect(Collectors.toList());
        Node lastSiblingNodeToAddAfter = Iterables.getLast(siblingNodeToAddAfterList);

        Node siblingNodeToAddBefore = lastSiblingNodeToAddAfter.getNextSibling();
        if (siblingNodeToAddBefore == null) {
            parent.appendChild(child);
        } else {
            parent.insertBefore(child, siblingNodeToAddBefore);
        }
        return child;
    }


    public Node getOrCreate(Node parent, XmlDom xml, String siblingToAddAfter, String child, String valueToSetIfNull) throws XPathExpressionException {
        Node node = xml.get(parent, child);
        if (node == null) {
            node = xml.addAfter(parent, siblingToAddAfter, xml.createNode(child));
        }
        String value = node.getTextContent();
        if (value == null || value.isEmpty()) {
            node.setTextContent(valueToSetIfNull);
        }
        return node;
    }


    public Node addLast(Node parent, Node child) {
        parent.appendChild(child);
        return child;
    }

    private static List<Node> toList(NodeList nodeList) {
        if (nodeList == null) {
            return List.of();
        }
        List<Node> properList = new ArrayList<>();
        for (int n = nodeList.getLength() - 1; n >= 0; n--) {
            Node child = nodeList.item(n);
            properList.add(child);
        }
        return properList;
    }

    public String domToString(Node document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer(new StreamSource(new StringReader(
                    "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                            "    <xsl:strip-space elements=\"*\"/>\n" +
                            "    <xsl:output method=\"xml\" encoding=\"UTF-8\"/>\n" +
                            "\n" +
                            "    <xsl:template match=\"@*|node()\">\n" +
                            "        <xsl:copy>\n" +
                            "            <xsl:apply-templates select=\"@*|node()\"/>\n" +
                            "        </xsl:copy>\n" +
                            "    </xsl:template>\n" +
                            "\n" +
                            "</xsl:stylesheet>\n")));

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");


            StringWriter stringWriter = new StringWriter();
            DOMSource dom = new DOMSource(document);
            transformer.transform(dom, new StreamResult(stringWriter));
            return stringWriter.toString().replace("--><", "-->\n<").replace("?><", "?>\n<");
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String toString() {
        return domToString(document);
    }
}
