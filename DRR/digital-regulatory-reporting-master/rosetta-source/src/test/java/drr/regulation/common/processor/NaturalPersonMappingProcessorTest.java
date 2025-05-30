package drr.regulation.common.processor;

import cdm.base.staticdata.party.NaturalPersonRole;
import cdm.base.staticdata.party.NaturalPersonRoleEnum;
import cdm.base.staticdata.party.metafields.FieldWithMetaNaturalPersonRoleEnum;
import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.Path;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import drr.regulation.common.PartyInformation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NaturalPersonMappingProcessorTest {

    public static final Path SYNONYM_PATH = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation[0].relatedPerson");
    public static final RosettaPath MODEL_PATH = RosettaPath.valueOf("ReportableEvent.reportableInformation.partyInformation.relatedPerson");

    @Test
    void shouldMapNaturalPerson() {
        // Set up
        Path personReferencePath = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation[0].relatedPerson[0].personReference.href");
        Path rolePath = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation[0].relatedPerson[0].role");
        Path personReferencePath2 = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation[0].relatedPerson[1].personReference.href");
        Path rolePath2 = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation[0].relatedPerson[1].role");

        // Create mappings (define your mappings here)
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(personReferencePath, "trader"));
        mappings.add(getErrorMapping(rolePath, "Trader"));
        mappings.add(getErrorMapping(personReferencePath2, "trader"));
        mappings.add(getErrorMapping(rolePath2, "InvestmentDecisionMaker"));

        // Create context
        MappingContext context = new MappingContext(mappings, null, getSynonymToEnumMap(), null);

        // Create the processor
        NaturalPersonMappingProcessor naturalPersonMappingProcessor = new NaturalPersonMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);


        NaturalPersonRole.NaturalPersonRoleBuilder nPRbuilder = NaturalPersonRole.builder();
        List<RosettaModelObjectBuilder> buildersList = new ArrayList<>();
        buildersList.add(nPRbuilder);
        buildersList.add(nPRbuilder);
        buildersList.add(nPRbuilder);

        PartyInformation.PartyInformationBuilder parent = PartyInformation.builder().addRelatedPerson(nPRbuilder);


        // Invoke the map method
        naturalPersonMappingProcessor.map(SYNONYM_PATH, buildersList, parent);

        // Assert the result based on your expected outcome
        // For example, if you expect that a certain field should be set, check it here.

        NaturalPersonRole naturalPersonRole = parent.getRelatedPerson().get(0);
        FieldWithMetaNaturalPersonRoleEnum fieldWithMeta = naturalPersonRole.getRole().get(0);
        FieldWithMetaNaturalPersonRoleEnum fieldWithMeta2 = naturalPersonRole.getRole().get(1);
        NaturalPersonRoleEnum naturalPersonRoleEnum = fieldWithMeta.getValue();
        NaturalPersonRoleEnum naturalPersonRoleEnum2 = fieldWithMeta2.getValue();
        assertEquals("Trader", naturalPersonRoleEnum.toString());
        assertEquals("InvestmentDecisionMaker", naturalPersonRoleEnum2.toString());
    }

    @NotNull
    private static Map<Class<?>, Map<String, Enum<?>>> getSynonymToEnumMap() {
        Map<String, Enum<?>> naturalPersonRoleEnumMap = new HashMap<>();
        naturalPersonRoleEnumMap.put("Trader", NaturalPersonRoleEnum.TRADER);
        naturalPersonRoleEnumMap.put("InvestmentDecisionMaker", NaturalPersonRoleEnum.INVESTMENT_DECISION_MAKER);

        Map<Class<?>, Map<String, Enum<?>>> synonymToEnumMap = new HashMap<>();
        synonymToEnumMap.put(NaturalPersonRoleEnum.class, naturalPersonRoleEnumMap);
        return synonymToEnumMap;
    }

    private static Mapping getErrorMapping(Path xmlPath, String xmlValue) {
        return new Mapping(xmlPath, xmlValue, null, null, "Not found", false, false, false);
    }
}
