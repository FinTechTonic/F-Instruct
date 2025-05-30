package cdm.base.staticdata.party.processor;

import cdm.base.staticdata.party.NaturalPersonRole;
import cdm.base.staticdata.party.NaturalPersonRoleEnum;
import cdm.base.staticdata.party.Party;
import cdm.base.staticdata.party.metafields.FieldWithMetaNaturalPersonRoleEnum;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaNaturalPerson;
import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.MappingProcessor;
import com.regnosys.rosetta.common.translation.Path;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NaturalPersonPartyMappingProcessor extends MappingProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NaturalPersonPartyMappingProcessor.class);

    public NaturalPersonPartyMappingProcessor(RosettaPath modelPath, List<Path> synonymPaths, MappingContext context) {
        super(modelPath, synonymPaths, context);
    }

    @Override
    public void map(Path synonymPath, List<? extends RosettaModelObjectBuilder> builders, RosettaModelObjectBuilder parent) {
        List<Path> relatedPersonPaths = getMappings().stream()
                .filter(mapping -> mapping.getXmlPath().toString().contains(".relatedPerson"))
                .map(Mapping::getXmlPath)
                .collect(Collectors.toList());

        // Create a new NaturalPersonRoleBuilder
        List<NaturalPersonRole.NaturalPersonRoleBuilder> nPRbuilder =
                (List<NaturalPersonRole.NaturalPersonRoleBuilder>) builders;

        // Initialize sets to keep track of processed indices
        Set<Integer> processedIndices = new HashSet<>();
        Set<Integer> processedPartyTradeInformationIndices = new HashSet<>();
        Set<Integer> processedRelatedPersonIndices = new HashSet<>();

        // Initialize a variable to keep track of the previous size of processedPartyTradeInformationIndices
        int previousPartyTradeInformationSize = 0;

        Party.PartyBuilder partyBuilder = (Party.PartyBuilder) parent;

        for (int i = 0; i < relatedPersonPaths.size(); i++) {
            String pathStr = relatedPersonPaths.get(i).toString();
            int startIndex = pathStr.lastIndexOf("(");
            int endIndex = pathStr.lastIndexOf(")");

            if (startIndex != -1 && endIndex != -1) {
                int currentIndex = Integer.parseInt(pathStr.substring(startIndex + 1, endIndex));
                int partyTradeInformationIndex = extractPartyTradeInformationIndex(pathStr);

                // Check if both indices have not been processed
                if (!processedPartyTradeInformationIndices.contains(partyTradeInformationIndex) ||
                        !processedRelatedPersonIndices.contains(currentIndex)) {

                    // Assuming you have methods to extract values from XML paths
                    Optional<String> personReferenceHref = extractPersonReferenceHref(relatedPersonPaths.get(i));
                    Optional<String> roleValue = extractRoleValue(relatedPersonPaths.get(i + 1));

                    // Get the current NaturalPersonRoleBuilder
                    NaturalPersonRole.NaturalPersonRoleBuilder nPRCurrentbuilder;

                    if (nPRbuilder.size() > currentIndex) {
                        // If the builder already exists at the specified index, get it
                        nPRCurrentbuilder = nPRbuilder.get(currentIndex);
                    } else {
                        // If the builder doesn't exist, create a new one and add it to the list
                        nPRCurrentbuilder = NaturalPersonRole.builder();
                        nPRbuilder.add(nPRCurrentbuilder);
                    }
                    personReferenceHref.ifPresent(pRH -> {//rNPB.setExternalReference(pRH);

                        roleValue.ifPresent(rV -> {
                            if (rV != null) {
                                try {
                                    // Check if the roleValue is a valid constant in the NaturalPersonRoleEnum enum
                                    NaturalPersonRoleEnum roleEnum = null;
                                    for (NaturalPersonRoleEnum enumValue : NaturalPersonRoleEnum.values()) {
                                        if (enumValue.toString().trim().equalsIgnoreCase(rV.trim())) {
                                            roleEnum = enumValue;
                                            break; // Exit the loop once a match is found
                                        }
                                    }

                                    // Create a FieldWithMetaNaturalPersonRoleEnum object with the enum value
                                    FieldWithMetaNaturalPersonRoleEnum.FieldWithMetaNaturalPersonRoleEnumBuilder fieldBuilder = FieldWithMetaNaturalPersonRoleEnum.builder();
                                    FieldWithMetaNaturalPersonRoleEnum fieldWithMeta = fieldBuilder
                                            .setValue(roleEnum)
                                            .build();

                                    if (nPRCurrentbuilder.getRole().isEmpty() ||
                                            nPRCurrentbuilder.getRole().stream().noneMatch(existingRole ->
                                                    existingRole.getValue().equals(fieldWithMeta.getValue()))) {
                                        // Create a list of FieldWithMetaNaturalPersonRoleEnum
                                        List<FieldWithMetaNaturalPersonRoleEnum> fieldList = new ArrayList<>();
                                        fieldList.add(fieldWithMeta);
                                        nPRCurrentbuilder.addRole(fieldList);
                                        nPRCurrentbuilder.setPersonReference(ReferenceWithMetaNaturalPerson.builder().setExternalReference(pRH).build());
                                    }

                                } catch (Exception e) {
                                    // Log the exception for debugging purposes
                                    LOGGER.error("Error setting value for NaturalPersonRoleEnum: " + e.getMessage(), e);
                                }
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
    }

    private Integer extractPartyTradeInformationIndex(String pathStr) {
        int startIndex = pathStr.indexOf("partyTradeInformation(");
        int endIndex = pathStr.indexOf(")");
        try {
            if (startIndex != -1 && endIndex != -1) {
                String indexStr = pathStr.substring(startIndex + 21, endIndex);
                // Remove any non-numeric characters from the extracted string
                indexStr = indexStr.replaceAll("[^0-9]", "");
                if (!indexStr.isEmpty()) {
                    return Integer.parseInt(indexStr);
                }
            }
        } catch (NumberFormatException e) {
            // Handle the case where the extraction fails (e.g., not a valid integer)
            // You can log an error or take other appropriate action here.
            LOGGER.error("Error extracting partyTradeInformation index: " + e.getMessage(), e);
        }
        return 0; // Return 0 when there is no index
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
