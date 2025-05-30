package com.regnosys.drr.testpack.modifiers;

import com.regnosys.drr.testpack.util.XmlDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.TRADE_HEADER_XPATH;
import static com.regnosys.drr.testpack.modifiers.TestPackModifierHelper.attributeEquals;

@SuppressWarnings("unused") // instantiated reflectively
public class ProductTypeModifier extends BaseModifier {

    public static final String PRIMARY_ASSET_CLASS_XPATH = "//*/primaryAssetClass";
    public static final String SECONDARY_ASSET_CLASS_XPATH = "//*/secondaryAssetClass";
    public static final String PRODUCT_ID_XPATH = "//*/productId";
    public static final String PRODUCT_TYPE_XPATH = "//*/productType";
    public static final String CFI_SCHEME = "http://www.fpml.org/coding-scheme/external/product-classification/iso10962";
    public static final String PRODUCT_TYPE_SCHEME_NAME = "productTypeScheme";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductTypeModifier.class);

    public ProductTypeModifier(ModifierContext context) {
        super(context);
    }

    private static boolean cfiProductTypeExists(XmlDom xml) throws XPathExpressionException {
        return xml.getList(PRODUCT_TYPE_XPATH).stream().anyMatch(n -> attributeEquals(n, PRODUCT_TYPE_SCHEME_NAME, CFI_SCHEME));
    }

    private static boolean primaryAssetClassExists(XmlDom xml) throws XPathExpressionException {
        return xml.get(PRIMARY_ASSET_CLASS_XPATH) != null;
    }

    private static boolean secondaryAssetClassExists(XmlDom xml) throws XPathExpressionException {
        return xml.get(SECONDARY_ASSET_CLASS_XPATH) != null;
    }

    private static Node getProductNode(XmlDom xml) throws XPathExpressionException {
        Node tradeHeaderNode = xml.get(TRADE_HEADER_XPATH);
        return tradeHeaderNode.getNextSibling().getNextSibling();
    }

    private static String getPrimaryAssetClassFromQualifier(String qualifier) {
        String primary = qualifier.split(":")[0];
        if (primary.contains("Credit")) {
            return "Credit";
        }
        return primary;
    }

    private static String getCfiFromQualifier(String qualifier) {
        if (qualifier.equals("Commodity:Option")) {
            return "HTOXVX";
        } else if (qualifier.equals("Commodity:Swaption")) {
            return "HTSXVX";
        } else if (qualifier.equals("CreditDefaultSwap:Index")) {
            return "SCICXX";
        } else if (qualifier.equals("CreditDefaultSwap:Loan")) {
            return "SCMCXX";
        } else if (qualifier.equals("CreditDefaultSwap:SingleName")) {
            return "SCSCXX";
        } else if (qualifier.equals("CreditDefaultSwaption")) {
            return "HCSXVX";
        } else if (qualifier.equals("EquityOption:PriceReturnBasicPerformance:Basket")) {
            return "HEBPXX";
        } else if (qualifier.equals("EquityOption:PriceReturnBasicPerformance:Index")) {
            return "HEIPXX";
        } else if (qualifier.equals("EquityOption:PriceReturnBasicPerformance:SingleName")) {
            return "HESPXX";
        } else if (qualifier.equals("EquitySwap:ParameterReturnVariance:Index")) {
            return "SEIVXX";
        } else if (qualifier.equals("EquitySwap:ParameterReturnVariance:SingleName")) {
            return "SESVXX";
        } else if (qualifier.equals("EquitySwap:ParameterReturnVolatility:Index")) {
            return "SEILXX";
        } else if (qualifier.equals("EquitySwap:PriceReturnBasicPerformance:Basket")) {
            return "SEBPXX";
        } else if (qualifier.equals("EquitySwap:PriceReturnBasicPerformance:SingleName")) {
            return "SESPXX";
        } else if (qualifier.equals("ForeignExchange:Swap")) {
            return "SFMXXX";
        } else if (qualifier.equals("InterestRate:IRSwap:Basis:OIS")) {
            return "SRAXSC";
        } else if (qualifier.equals("InterestRate:IRSwap:FixedFloat:ZeroCoupon")) {
            return "SRZXSC";
        } else if (qualifier.equals("InterestRate:InflationSwap:FixedFloat:ZeroCoupon")) {
            return "SRZXSC";
        } else {
            return null;
        }
    }

    @Override
    public boolean isApplicable(Path xmlFile, String xmlContent, XmlDom xml) throws XPathExpressionException {
        return !primaryAssetClassExists(xml) || !cfiProductTypeExists(xml);
    }

    @Override
    public void modify(Path xmlFile, XmlDom xml) throws XPathExpressionException {
        String qualifier = getContext().getXmlFileToQualifierMap().get(xmlFile);
        if (qualifier != null) {
            Node productNode = getProductNode(xml);

            if (!primaryAssetClassExists(xml)) {
                String primaryAssetClass = getPrimaryAssetClassFromQualifier(qualifier);
                xml.addFirst(productNode, xml.createNode("primaryAssetClass", primaryAssetClass));
            }

            if (!cfiProductTypeExists(xml)) {
                String cfi = Optional
                        .ofNullable(getContext().getQualifierToCfiMap().get(qualifier))
                        .orElseGet(() -> getCfiFromQualifier(qualifier));
                if (cfi != null) {
                    Node productTypeCfi =
                            xml.createNode("productType",
                                    Map.of(PRODUCT_TYPE_SCHEME_NAME, CFI_SCHEME),
                                    cfi);
                    if (secondaryAssetClassExists(xml)) {
                        xml.addAfter(productNode, "secondaryAssetClass", productTypeCfi);
                    } else if (primaryAssetClassExists(xml)) {
                        xml.addAfter(productNode, "primaryAssetClass", productTypeCfi);
                    } else {
                        xml.addFirst(productNode, productTypeCfi);
                    }
                } else {
                    LOGGER.info("No CFI found for qualifier {}", qualifier);
                }
            }
        }
    }
}
