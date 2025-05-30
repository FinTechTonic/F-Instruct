package cdm.event.common.processor;

import cdm.base.staticdata.party.PartyRole;
import cdm.base.staticdata.party.PartyRoleEnum;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import cdm.event.common.Trade;
import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.MappingProcessor;
import com.regnosys.rosetta.common.translation.Path;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.subPath;
import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.updateMappingSuccess;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public class RelatedPartyRoleMappingProcessor extends MappingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelatedPartyRoleMappingProcessor.class);

    public RelatedPartyRoleMappingProcessor(RosettaPath modelPath, List<Path> synonymPaths, MappingContext context) {
        super(modelPath, synonymPaths, context);
    }

    @Override
    public void map(Path synonymPath, List<? extends RosettaModelObjectBuilder> builder, RosettaModelObjectBuilder parent) {
        Collection<PartyRoleMapping> partyRoleMappings = getMappings().stream()
                // find all partyTradeInformation.relatedParty mappings
                .filter(m -> synonymPath.getParent().nameStartMatches(m.getXmlPath()))
                // group by relatedParty path index
                .collect(Collectors.groupingBy(this::getKey, collectingAndThen(toList(), this::toPartyRoleMapping)))
                .values();

        partyRoleMappings.stream()
                .filter(Objects::nonNull)
                .filter(m -> PartyRoleEnum.CLEARING_ORGANIZATION.toDisplayString().equals(m.role)
                        || PartyRoleEnum.EXECUTION_FACILITY.toDisplayString().equals(m.role))
                .forEach(m -> {
                    Trade.TradeBuilder tradeBuilder = (Trade.TradeBuilder) parent;
                    // Add party role
                    tradeBuilder.addPartyRole(PartyRole.builder()
                            .setPartyReference(ReferenceWithMetaParty.builder()
                                    .setExternalReference(m.partyReference))
                            .setRole(PartyRoleEnum.fromDisplayName(m.role)));
                    // Update mappings
                    updateMappingSuccess(m.partyReferenceMapping, m.partyReferenceModelPath);
                    updateMappingSuccess(m.roleMapping, m.roleModelPath);
                });
    }

    /**
     * Group by path index, e.g. collect all mappings under relatedParty(index)
     * (nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation(0).relatedParty(10))
     */
    private String getKey(Mapping m) {
        int partyTradeInformationIndex = getPathIndex(m, "partyTradeInformation");
        int relatedPartyIndex = getPathIndex(m, "relatedParty");
        return partyTradeInformationIndex + "-" + relatedPartyIndex;
    }

    private int getPathIndex(Mapping m, String elementName) {
        return subPath(elementName, m.getXmlPath())
                .map(Path::getLastElement)
                .map(Path.PathElement::forceGetIndex)
                .orElse(-1);
    }

    private PartyRoleMapping toPartyRoleMapping(List<Mapping> mappings) {
        try {
            Mapping partyReferenceMapping = getMapping(mappings, Path.parse("partyReference.href"));
            RosettaPath partyReferenceModelPath = getModelPath().newSubPath("partyReference").newSubPath("externalReference");
            Mapping roleMapping = getMapping(mappings, Path.parse("role"));
            RosettaPath roleModelPath = getModelPath().newSubPath("role");
            if (partyReferenceMapping != null && roleMapping != null) {
                return new PartyRoleMapping(
                        String.valueOf(partyReferenceMapping.getXmlValue()),
                        partyReferenceMapping,
                        partyReferenceModelPath,
                        String.valueOf(roleMapping.getXmlValue()),
                        roleMapping,
                        roleModelPath);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to build party role mapping from mappings {}", mappings, e);
        }
        return null;
    }

    private Mapping getMapping(List<Mapping> mappings, Path endsWith) {
        return mappings.stream().filter(m -> m.getXmlPath().endsWith(endsWith)).findFirst().orElse(null);
    }

    private static class PartyRoleMapping {
        private final String partyReference;
        private final Mapping partyReferenceMapping;
        private final RosettaPath partyReferenceModelPath;
        private final String role;
        private final Mapping roleMapping;
        private final RosettaPath roleModelPath;

        public PartyRoleMapping(String partyReference, Mapping partyReferenceMapping, RosettaPath partyReferenceModelPath, String role, Mapping roleMapping, RosettaPath roleModelPath) {
            this.partyReference = partyReference;
            this.partyReferenceMapping = partyReferenceMapping;
            this.partyReferenceModelPath = partyReferenceModelPath;
            this.role = role;
            this.roleMapping = roleMapping;
            this.roleModelPath = roleModelPath;
        }
    }
}
