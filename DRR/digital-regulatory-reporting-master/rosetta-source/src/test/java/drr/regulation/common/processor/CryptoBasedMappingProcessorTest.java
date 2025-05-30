package drr.regulation.common.processor;

import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.Path;

import com.rosetta.model.lib.path.RosettaPath;
import drr.regulation.common.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class CryptoBasedMappingProcessorTest {
    public static final Path SYNONYM_PATH = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation");
    public static final RosettaPath MODEL_PATH = RosettaPath.valueOf("ReportableEvent.reportableInformation.cryptoBased");



    @Test
    void shouldMapCryptoBased() {
        // Set up
        Path productTypePath = Path.parse("nonpublicExecutionReport.trade.swap.productType[0]");

        // Update metadata at category(0) level
        Map<String, String> metadata = new HashMap<>();
        metadata.put("productTypeScheme", "http://www.fpml.org/coding-scheme/esma-emir-refit-crypto-asset-indicator");


        // Find the category element
        Path.PathElement productTypeElement = null;
        int productTypeIndex = -1;
        for (int i = 0; i < productTypePath.getElements().size(); i++) {
            Path.PathElement element = productTypePath.getElements().get(i);
            if (element.getPathName().equals("productType")) {
                // Update metadata for category element
                Map<String, String> updatedMetadata = new HashMap<>(element.getMetas());
                updatedMetadata.put("productTypeScheme", "http://www.fpml.org/coding-scheme/esma-emir-refit-crypto-asset-indicator");
                productTypeElement = new Path.PathElement(element.getPathName(), element.getIndex(), updatedMetadata);
                productTypeIndex = i;
                break;
            }
        }

        // Replace the category element in the path
        if (productTypeElement != null && productTypeIndex >= 0) {
            List<Path.PathElement> updatedElements = new ArrayList<>(productTypePath.getElements());
            updatedElements.set(productTypeIndex, productTypeElement);
            productTypePath = new Path(updatedElements);
        }

        Path productTypeSchemePath = productTypePath.addElement("productTypeScheme");
        // Create mappings
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(productTypePath, "false"));
        mappings.add(getErrorMapping(productTypeSchemePath, "http://www.fpml.org/coding-scheme/esma-emir-refit-crypto-asset-indicator"));
        // Create context
        MappingContext context = new MappingContext(mappings, null, null, null);

        // Create the parent object
        ReportableInformation.ReportableInformationBuilder parent = ReportableInformation.builder();

        // Create the processor
        CryptoBasedMappingProcessor cryptoBasedMappingProcessor = new CryptoBasedMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);

        // Call mapBasic with the appropriate parent object
        cryptoBasedMappingProcessor.mapBasic(SYNONYM_PATH, Optional.empty(), parent);


        // Test
        // Assert the result
        assertEquals(false, parent.getCryptoBased());
    }

    @Test
    void shouldMapCryptoBased2() {
        // Set up
        Path productTypePath = Path.parse("nonpublicExecutionReport.trade.swap.productType[0]");

        // Create mappings
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(productTypePath, "true"));
        // This test does not contain a valid scheme
        // Create context
        MappingContext context = new MappingContext(mappings, null, null, null);

        // Create the parent object
        ReportableInformation.ReportableInformationBuilder parent = ReportableInformation.builder();
        // Create the processor
        CryptoBasedMappingProcessor cryptoBasedMappingProcessor = new CryptoBasedMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        cryptoBasedMappingProcessor.mapBasic(SYNONYM_PATH, Optional.empty(), parent);

        // Assert the result
        assertEquals(null, parent.getCryptoBased());
    }

    @Test
    void shouldMapCryptoBased3() {
        // Set up
        Path productTypePath = Path.parse("nonpublicExecutionReport.trade.swap.productType[0]");

        // Update metadata at category(0) level
        Map<String, String> metadata = new HashMap<>();
        metadata.put("productTypeScheme", "http://www.fpml.org/coding-scheme/esma-emir-refit-crypto-asset-indicator");


        // Find the category element
        Path.PathElement productTypeElement = null;
        int productTypeIndex = -1;
        for (int i = 0; i < productTypePath.getElements().size(); i++) {
            Path.PathElement element = productTypePath.getElements().get(i);
            if (element.getPathName().equals("productType")) {
                // Update metadata for category element
                Map<String, String> updatedMetadata = new HashMap<>(element.getMetas());
                updatedMetadata.put("productTypeScheme", "http://www.fpml.org/coding-scheme/esma-emir-refit-crypto-asset-indicator");
                productTypeElement = new Path.PathElement(element.getPathName(), element.getIndex(), updatedMetadata);
                productTypeIndex = i;
                break;
            }
        }

        // Replace the category element in the path
        if (productTypeElement != null && productTypeIndex >= 0) {
            List<Path.PathElement> updatedElements = new ArrayList<>(productTypePath.getElements());
            updatedElements.set(productTypeIndex, productTypeElement);
            productTypePath = new Path(updatedElements);
        }

        Path productTypeSchemePath = productTypePath.addElement("productTypeScheme");
        // Create mappings
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(productTypePath, "true"));
        mappings.add(getErrorMapping(productTypeSchemePath, "http://www.fpml.org/coding-scheme/esma-emir-refit-crypto-asset-indicator"));
        // Create context
        MappingContext context = new MappingContext(mappings, null, null, null);

        // Create the parent object
        ReportableInformation.ReportableInformationBuilder parent = ReportableInformation.builder();

        // Create the processor
        CryptoBasedMappingProcessor cryptoBasedMappingProcessor = new CryptoBasedMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);

        // Call mapBasic with the appropriate parent object
        cryptoBasedMappingProcessor.mapBasic(SYNONYM_PATH, Optional.empty(), parent);


        // Test
        // Assert the result
        assertEquals(true, parent.getCryptoBased());
    }

    @NotNull
    private static Mapping getErrorMapping(Path xmlPath, String xmlValue) {
        return new Mapping(xmlPath, xmlValue, null, null, "Not found", false, false, false);
    }

}