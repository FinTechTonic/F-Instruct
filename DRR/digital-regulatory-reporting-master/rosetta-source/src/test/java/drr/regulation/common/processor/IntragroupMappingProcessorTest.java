package drr.regulation.common.processor;

import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.Path;
import com.rosetta.model.lib.path.RosettaPath;
import drr.regulation.common.ReportableInformation;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


class IntragroupMappingProcessorTest {
    public static final Path SYNONYM_PATH = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation");
    public static final RosettaPath MODEL_PATH = RosettaPath.valueOf("ReportableEvent.reportableInformation.intragroup");



    @Test
    void shouldMapIntragroup() {
        // Set up
        Path categoryPath = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation.category[0]");

        // Update metadata at category(0) level
        Map<String, String> metadata = new HashMap<>();
        metadata.put("categoryScheme", "http://www.dtcc.com/intragroup");


        // Find the category element
        Path.PathElement categoryElement = null;
        int categoryIndex = -1;
        for (int i = 0; i < categoryPath.getElements().size(); i++) {
            Path.PathElement element = categoryPath.getElements().get(i);
            if (element.getPathName().equals("category")) {
                // Update metadata for category element
                Map<String, String> updatedMetadata = new HashMap<>(element.getMetas());
                updatedMetadata.put("categoryScheme", "http://www.dtcc.com/intragroup");
                categoryElement = new Path.PathElement(element.getPathName(), element.getIndex(), updatedMetadata);
                categoryIndex = i;
                break;
            }
        }

        // Replace the category element in the path
        if (categoryElement != null && categoryIndex >= 0) {
            List<Path.PathElement> updatedElements = new ArrayList<>(categoryPath.getElements());
            updatedElements.set(categoryIndex, categoryElement);
            categoryPath = new Path(updatedElements);
        }

        Path categorySchemePath = categoryPath.addElement("categoryScheme");
        // Create mappings
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(categoryPath, "false"));
        mappings.add(getErrorMapping(categorySchemePath, "http://www.dtcc.com/intragroup"));
        // Create context
        MappingContext context = new MappingContext(mappings, null, null, null);

        // Create the parent object
        ReportableInformation.ReportableInformationBuilder parent = ReportableInformation.builder();

        // Create the processor
        IntragroupMappingProcessor intragroupMappingProcessor = new IntragroupMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);

        // Call mapBasic with the appropriate parent object
        intragroupMappingProcessor.mapBasic(SYNONYM_PATH, Optional.empty(), parent);


        // Test
        // Assert the result
        assertEquals(false, parent.getIntragroup());
    }

    @Test
    void shouldMapIntragroup2() {
        // Set up
        Path tradePartyRelationshipTypePath = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation.reportingRegime[0].tradePartyRelationshipType");

        // Create mappings
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(tradePartyRelationshipTypePath, "Intragroup"));
        //mappings.add(new Mapping(tradePartyRelationshipTypePath, "Counterparty", null, null, "Mapping 2", false, false, false));

        // Create context
        MappingContext context = new MappingContext(mappings, null, null, null);

        // Create the parent object
        ReportableInformation.ReportableInformationBuilder parent = ReportableInformation.builder();
        // Create the processor
        IntragroupMappingProcessor intragroupMappingProcessor = new IntragroupMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        intragroupMappingProcessor.mapBasic(SYNONYM_PATH, Optional.empty(), parent);

        // Assert the result
        assertEquals(true, parent.getIntragroup());
    }

    @Test
    void shouldMapIntragroup3() {
        // Set up
        Path tradePartyRelationshipTypePath = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation.reportingRegime[0].tradePartyRelationshipType");
        // Create mappings
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(tradePartyRelationshipTypePath, "Inter-Dealer"));
        //mappings.add(new Mapping(tradePartyRelationshipTypePath, "Counterparty", null, null, "Mapping 2", false, false, false));

        // Create context
        MappingContext context = new MappingContext(mappings, null, null, null);

        // Create the parent object
        ReportableInformation.ReportableInformationBuilder parent = ReportableInformation.builder();
        // Create the processor
        IntragroupMappingProcessor intragroupMappingProcessor = new IntragroupMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        intragroupMappingProcessor.mapBasic(SYNONYM_PATH, Optional.empty(), parent);        Mapping categoryMapping = mappings.get(0);

        // Assert the result
        assertEquals(null, parent.getIntragroup());

    }

    @Test
    void shouldMapIntragroup4() {
        // Set up
        Path categoryPath = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation.category[0]");

        // Update metadata at category(0) level
        Map<String, String> metadata = new HashMap<>();
        metadata.put("categoryScheme", "http://www.dtcc.com/intragroup");


        // Find the category element
        Path.PathElement categoryElement = null;
        int categoryIndex = -1;
        for (int i = 0; i < categoryPath.getElements().size(); i++) {
            Path.PathElement element = categoryPath.getElements().get(i);
            if (element.getPathName().equals("category")) {
                // Update metadata for category element
                Map<String, String> updatedMetadata = new HashMap<>(element.getMetas());
                updatedMetadata.put("categoryScheme", "http://www.dtcc.com/intragroup");
                categoryElement = new Path.PathElement(element.getPathName(), element.getIndex(), updatedMetadata);
                categoryIndex = i;
                break;
            }
        }

        // Replace the category element in the path
        if (categoryElement != null && categoryIndex >= 0) {
            List<Path.PathElement> updatedElements = new ArrayList<>(categoryPath.getElements());
            updatedElements.set(categoryIndex, categoryElement);
            categoryPath = new Path(updatedElements);
        }

        Path categorySchemePath = categoryPath.addElement("categoryScheme");
        // Create mappings
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(categoryPath, "true"));
        mappings.add(getErrorMapping(categorySchemePath, "http://www.dtcc.com/intragroup"));
        // Create context
        MappingContext context = new MappingContext(mappings, null, null, null);

        // Create the parent object
        ReportableInformation.ReportableInformationBuilder parent = ReportableInformation.builder();

        // Create the processor
        IntragroupMappingProcessor intragroupMappingProcessor = new IntragroupMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);

        // Call mapBasic with the appropriate parent object
        intragroupMappingProcessor.mapBasic(SYNONYM_PATH, Optional.empty(), parent);


        // Test
        // Assert the result
        assertEquals(true, parent.getIntragroup());

    }
    private static Mapping getErrorMapping(Path xmlPath, String xmlValue) {
        return new Mapping(xmlPath, xmlValue, null, null, "Not found", false, false, false);
    }

}