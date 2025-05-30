package drr.regulation.common.processor;

import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.MappingProcessor;
import com.regnosys.rosetta.common.translation.Path;
import com.regnosys.rosetta.common.translation.Path.PathElement;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import drr.regulation.common.ReportableInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.common.translation.Mapping;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.getNonNullMappedValue;

/**
 * FpML mapping processor.
 * Specs for the mapper:
 * Map Intragroup boolean:
 * 1- if the path is nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation.category = true and the attribute of category <category categoryScheme="http://www.dtcc.com/intragroup">
 * or if in the path nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation.reportingRegime.tradePartyRelationshipType = Intragroup then Intragroup populated = true
 * 2- if the path is nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation.category = true and the attribute of category <category categoryScheme="http://www.dtcc.com/intragroup" then Intragroup= false
 * 3- else do not populate Intragroup, leave it as blank.
 */

@SuppressWarnings("unused")
public class IntragroupMappingProcessor extends MappingProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntragroupMappingProcessor.class);
    public static final String DTCC_INTRAGROUP_SCHEME = "http://www.dtcc.com/intragroup";

    public IntragroupMappingProcessor(RosettaPath modelPath, List<Path> synonymPaths, MappingContext context) {
        super(modelPath, synonymPaths, context);
    }

    @Override
    public <T> void mapBasic(Path xmlPath, Optional<T> instance, RosettaModelObjectBuilder parent) {
        Boolean intragroup = determineIntragroup(xmlPath);
        if (intragroup != null) {
            ReportableInformation.ReportableInformationBuilder reportableInformationBuilder = (ReportableInformation.ReportableInformationBuilder) parent;
            reportableInformationBuilder.setIntragroup(intragroup);
        }
    }

    private Boolean determineIntragroup(Path xmlPath) {
        Boolean intragroup = isIntragroupByCategory(xmlPath);
        if (intragroup != null) {
            return intragroup;
        }

        return isIntragroupByTradePartyRelationshipType(xmlPath);
    }

    private Boolean isIntragroupByCategory(Path xmlPath) {
        List<Path> categoryPaths = getMappings().stream()
                .filter(m -> m.getXmlPath().endsWith("category"))
                .map(Mapping::getXmlPath)
                .collect(Collectors.toList());

        for (Path categoryPath : categoryPaths) {
            List<PathElement> categoryElements = categoryPath.getElements();
            Optional<Boolean> isDtccIntragroupScheme = categoryElements.stream()
                    .filter(element -> element.getPathName().equals("category"))
                    .filter(element -> element.getMetas().containsKey("categoryScheme"))
                    .filter(element -> {
                        String categoryScheme = element.getMetas().get("categoryScheme");
                        return categoryScheme != null && categoryScheme.equals(DTCC_INTRAGROUP_SCHEME);
                    })
                    .map(element -> true)
                    .findFirst();

            Optional<Boolean> isTrueValue = categoryElements.stream()
                    .filter(element -> element.getPathName().equals("category"))
                    .map(element -> {
                        Optional<String> mappedValue = getNonNullMappedValue(categoryPath, getMappings());
                        return mappedValue.isPresent() && mappedValue.get().equals("true");
                    })
                    .findFirst();

            if (isDtccIntragroupScheme.isPresent() && isTrueValue.isPresent()) {
                return isDtccIntragroupScheme.get() && isTrueValue.get();
            } else if (isDtccIntragroupScheme.isPresent()) {
                return isDtccIntragroupScheme.get();
            }
        }

        return null; // No matching conditions found
    }

    private Boolean isIntragroupByTradePartyRelationshipType(Path xmlPath) {
        List<Path> tradePartyRelationshipTypePaths = getMappings().stream()
                .filter(m -> m.getXmlPath().endsWith("tradePartyRelationshipType"))
                .map(Mapping::getXmlPath)
                .collect(Collectors.toList());

        for (Path tradePartyRelationshipTypePath : tradePartyRelationshipTypePaths) {
            String tradePartyRelationshipType = getNonNullMappedValue(tradePartyRelationshipTypePath, getMappings()).orElse(null);
            if ("Intragroup".equals(tradePartyRelationshipType)) {
                return true;
            }
        }

        return null; // No matching conditions found
    }
}