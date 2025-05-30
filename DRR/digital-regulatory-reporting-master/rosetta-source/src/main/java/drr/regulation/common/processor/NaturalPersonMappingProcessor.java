package drr.regulation.common.processor;

import cdm.base.staticdata.party.NaturalPersonRoleEnum;
import cdm.base.staticdata.party.metafields.FieldWithMetaNaturalPersonRoleEnum;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaNaturalPerson;
import com.regnosys.rosetta.common.translation.*;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static cdm.base.staticdata.party.NaturalPersonRole.NaturalPersonRoleBuilder;
import static cdm.base.staticdata.party.metafields.FieldWithMetaNaturalPersonRoleEnum.FieldWithMetaNaturalPersonRoleEnumBuilder;
import static cdm.base.staticdata.party.metafields.ReferenceWithMetaParty.ReferenceWithMetaPartyBuilder;
import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.subPath;
import static com.rosetta.util.CollectionUtils.emptyIfNull;
import static drr.regulation.common.PartyInformation.PartyInformationBuilder;

@SuppressWarnings("unused")
public class NaturalPersonMappingProcessor extends MappingProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NaturalPersonMappingProcessor.class);
    private final SynonymToEnumMap synonymToEnumMap;

    public NaturalPersonMappingProcessor(RosettaPath modelPath, List<Path> synonymPaths, MappingContext context) {
        super(modelPath, synonymPaths, context);
        this.synonymToEnumMap = context.getSynonymToEnumMap();
    }

    @Override
    public void map(Path synonymPath, List<? extends RosettaModelObjectBuilder> builders, RosettaModelObjectBuilder parent) {
        List<NaturalPersonRoleBuilder> naturalPersonRoleBuilders = (List<NaturalPersonRoleBuilder>) builders;
        PartyInformationBuilder partyInformationBuilder = (PartyInformationBuilder) parent;

        // Assuming you have a list of paths related to "relatedPerson"
        List<Path> relatedPersonHrefOrRolePaths = getRelatedPersonPaths();

        // Initialize sets to keep track of processed indices
        Set<Integer> processedIndices = new HashSet<>();
        Set<Integer> processedPartyTradeInformationIndices = new HashSet<>();
        Set<Integer> processedRelatedPersonIndices = new HashSet<>();

        // Initialize a variable to keep track of the previous size of processedPartyTradeInformationIndices
        int previousPartyTradeInformationSize = 0;

        ReferenceWithMetaPartyBuilder referenceWithMetaParty = partyInformationBuilder.getOrCreatePartyReference();
        for (int i = 0; i < relatedPersonHrefOrRolePaths.size(); i++) {
            Path relatedPersonHrefOrRolePath = relatedPersonHrefOrRolePaths.get(i);
            Path relatedPersonPath = subPath("relatedPerson", relatedPersonHrefOrRolePath).get();
            int currentIndex = relatedPersonPath.getLastElement().forceGetIndex();
            int partyTradeInformationIndex = relatedPersonPath.getParent().getLastElement().forceGetIndex();

            // Check if both indices have not been processed
            if (!processedPartyTradeInformationIndices.contains(partyTradeInformationIndex) ||
                    !processedRelatedPersonIndices.contains(currentIndex)) {

                // Assuming you have methods to extract values from XML paths
                Optional<String> personReferenceHrefOptional = extractPersonReferenceHref(relatedPersonHrefOrRolePath);
                Optional<NaturalPersonRoleEnum> naturalPersonRoleOptional =
                        extractRoleValue(relatedPersonHrefOrRolePaths.get(i + 1)).flatMap(this::getNaturalPersonRoleEnum);

                // Get the current NaturalPersonRoleBuilder
                NaturalPersonRoleBuilder naturalPersonRoleBuilder = naturalPersonRoleBuilders.get(currentIndex);

                personReferenceHrefOptional.ifPresent(personReferenceHref -> {
                    naturalPersonRoleOptional.ifPresent(naturalPersonRole -> {
                        try {
                            FieldWithMetaNaturalPersonRoleEnum fieldWithMeta =
                                    FieldWithMetaNaturalPersonRoleEnum.builder().setValue(naturalPersonRole);

                            List<? extends FieldWithMetaNaturalPersonRoleEnumBuilder> roles = emptyIfNull(naturalPersonRoleBuilder.getRole());
                            if (roles.isEmpty() ||
                                    roles.stream()
                                            .map(FieldWithMetaNaturalPersonRoleEnum::getValue)
                                            .filter(Objects::nonNull)
                                            .noneMatch(existingRole ->
                                                    existingRole.equals(naturalPersonRole))) {
                                // Create a list of FieldWithMetaNaturalPersonRoleEnum
                                List<FieldWithMetaNaturalPersonRoleEnum> fieldList = new ArrayList<>();
                                fieldList.add(fieldWithMeta);
                                naturalPersonRoleBuilder.addRole(fieldList);
                                naturalPersonRoleBuilder
                                        .setPersonReference(ReferenceWithMetaNaturalPerson.builder()
                                                .setExternalReference(personReferenceHref).build());
                            }
                        } catch (Exception e) {
                            // Log the exception for debugging purposes
                            LOGGER.error("Error setting value for NaturalPersonRoleEnum: " + e.getMessage(), e);
                        }
                    });
                });

                // Mark both indices as processed
                processedPartyTradeInformationIndices.add(partyTradeInformationIndex);
                processedRelatedPersonIndices.add(currentIndex);

                // Check if processedPartyTradeInformationIndices has increased in size
                if (processedPartyTradeInformationIndices.size() > previousPartyTradeInformationSize && processedPartyTradeInformationIndices.size() > 1) {
                    // If it has, reset processedRelatedPersonIndices
                    processedRelatedPersonIndices.clear();
                    // Update previousPartyTradeInformationSize
                    previousPartyTradeInformationSize = processedPartyTradeInformationIndices.size();
                    processedRelatedPersonIndices.add(currentIndex);
                }
            }
        }
    }

    private List<Path> getRelatedPersonPaths() {
        return getMappings().stream()
                .filter(mapping -> mapping.getXmlPath().toString().contains(".relatedPerson"))
                .map(Mapping::getXmlPath)
                .collect(Collectors.toList());
    }

    private Optional<NaturalPersonRoleEnum> getNaturalPersonRoleEnum(String role) {
        return synonymToEnumMap.getEnumValueOptional(NaturalPersonRoleEnum.class, role);
    }

    private Optional<String> extractPersonReferenceHref(Path path) {
        // Implement the logic to extract the personReferenceHref from the given path
        // You need to access the XML data correctly based on the path
        // and return the extracted value as an Optional<String>
        return getMappings()
                .stream()
                .filter(mapping -> {
                    // Check if the XML path ends with ".value"
                    String xmlPath = mapping.getXmlPath().toString();
                    return xmlPath.endsWith(".personReference.href") && xmlPath.equals(path.toString());
                })
                .map(Mapping::getXmlValue)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();
    }

    private Optional<String> extractRoleValue(Path path) {
        // Implement the logic to extract the roleValue from the given path
        // You need to access the XML data correctly based on the path
        // and return the extracted value as an Optional<String>
        return getMappings()
                .stream()
                .filter(mapping -> {
                    // Check if the XML path ends with ".value"
                    String xmlPath = mapping.getXmlPath().toString();
                    return xmlPath.endsWith(".role") && xmlPath.equals(path.toString());
                })
                .map(Mapping::getXmlValue)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();
    }
}
