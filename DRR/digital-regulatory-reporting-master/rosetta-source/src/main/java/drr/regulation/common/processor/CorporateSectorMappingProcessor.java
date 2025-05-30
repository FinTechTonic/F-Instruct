package drr.regulation.common.processor;

import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.MappingProcessor;
import com.regnosys.rosetta.common.translation.Path;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import drr.regulation.common.*;
import drr.regulation.common.metafields.FieldWithMetaSupervisoryBodyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.getNonNullMappedValue;
import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.setValueAndOptionallyUpdateMappings;
import static drr.regulation.common.PartyInformation.PartyInformationBuilder;
import static drr.regulation.common.ReportingRegime.ReportingRegimeBuilder;

/**
 * FpML mapping processor.
 * Specs for the mapper:
 * - Access to partyTradeInformation list and extract the partyReference
 * - Relate the partyReference "href" with the party "id" bloc and extract the classification within it
 * - Map it int CDM in ReportableEvent->reportableInformation->partyInformation->regimeInformation->esmaPartyInformation->corporateSector->financialSector/nonFinancialSector->nonFinancialSectorIndicator
 * - Take into account that partyInformation in CDM is a list (1..*), financialSector is type FinancialSectorEnum and nonFinancialSector is a list 0..* of type NonFinancialSector and nonFinancialSectorIndicator is type NonFinancialSectorEnum
 * - For populating into the enums, use the new scheme Jan provided (fpml-schemeDefinitions) in order to decide how to populate the elements.
 */
@SuppressWarnings("unused")
public class CorporateSectorMappingProcessor extends MappingProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorporateSectorMappingProcessor.class);
    public static final String ESMA_CORPORATE_SECTOR_SCHEME = "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector";

    public CorporateSectorMappingProcessor(RosettaPath modelPath, List<Path> synonymPaths, MappingContext context) {
        super(modelPath, synonymPaths, context);
    }

    @Override
    public void map(Path synonymPath, List<? extends RosettaModelObjectBuilder> builders, RosettaModelObjectBuilder parent) {
        PartyInformationBuilder partyInformationBuilder = (PartyInformationBuilder) parent;
        List<ReportingRegimeBuilder> reportingRegimeBuilders = (List<ReportingRegimeBuilder>) builders;

        List<Path> classificationPaths = getClassificationPaths(partyInformationBuilder);

        setCorporateSector(reportingRegimeBuilders, classificationPaths, SupervisoryBodyEnum.ESMA);
        setCorporateSector(reportingRegimeBuilders, classificationPaths, SupervisoryBodyEnum.FCA);
    }

    private void setCorporateSector(List<ReportingRegimeBuilder> reportingRegimeBuilders, List<Path> classificationPaths, SupervisoryBodyEnum supervisoryBody) {
        reportingRegimeBuilders.stream()
                .filter(b -> isSupervisoryBody(b, supervisoryBody))
                .findFirst()
                .ifPresent(reportingRegimeBuilder -> {
                    for (Path classificationPath : classificationPaths) {
                        getNonNullMappedValue(classificationPath.addElement("industryClassificationScheme"), getMappings())
                                .filter(ESMA_CORPORATE_SECTOR_SCHEME::equals)
                                .ifPresent(industryClassificationScheme ->
                                        setValueAndOptionallyUpdateMappings(classificationPath,
                                                classificationValue -> {
                                                    CorporateSector.CorporateSectorBuilder corporateSectorBuilder = CorporateSector.builder();

                                                    // add financial or non-financial sector
                                                    getFinancialSectorEnum(classificationValue)
                                                            .ifPresent(financialSector ->
                                                                    corporateSectorBuilder.addFinancialSector(financialSector));
                                                    getNonFinancialSector(classificationValue)
                                                            .ifPresent(nonFinancialSector ->
                                                                    corporateSectorBuilder.addNonFinancialSector(nonFinancialSector));

                                                    // add corporate sector
                                                    if (corporateSectorBuilder.hasData()) {
                                                        updatePartyInformationForRegime(reportingRegimeBuilder, corporateSectorBuilder);
                                                        return true;
                                                    }
                                                    return false;
                                                },
                                                getMappings(),
                                                getModelPath()));
                    }
                });
    }

    private List<Path> getClassificationPaths(PartyInformationBuilder partyInformationBuilder) {
        return Optional.ofNullable(partyInformationBuilder.getPartyReference())
                .map(ReferenceWithMetaParty::getExternalReference)
                .map(partyReference -> getClassificationPathsForParty(partyReference))
                .orElse(Collections.emptyList());
    }

    private List<Path> getClassificationPathsForParty(String partyReference) {
        List<Path> partyClassificationPaths = new ArrayList<>();

        Optional<Path> partyPath = getPartyPath(partyReference, "party", "id");
        partyPath.ifPresent(path -> {
            String partyPathStr = path.toString();
            for (Mapping mapping : getMappings()) {
                Path xmlPath = mapping.getXmlPath();
                if (mapping.getXmlValue() != null
                        && xmlPath != null
                        && xmlPath.toString().startsWith(partyPathStr)
                        && xmlPath.endsWith("classification")) {
                    partyClassificationPaths.add(xmlPath);
                }
            }
        });

        return partyClassificationPaths;
    }

    private Optional<Path> getPartyPath(String xmlValue, String... xmlPathEndsWith) {
        return getMappings().stream()
                .filter(m -> m.getXmlPath().endsWith(xmlPathEndsWith))
                .filter(m -> {
                    Object xmlValueObj = m.getXmlValue();
                    String xmlValueStr = (xmlValueObj != null) ? xmlValueObj.toString() : null;
                    return Objects.equals(xmlValueStr, xmlValue);
                })
                .map(m -> m.getXmlPath()) // e.g. nonpublicExecutionReport.party(0).id
                .map(p -> p.getParent()) // e.g. nonpublicExecutionReport.party(0)
                .findFirst();
    }

    private Boolean isSupervisoryBody(ReportingRegimeBuilder reportingRegime, SupervisoryBodyEnum supervisoryBody) {
        return Optional.ofNullable(reportingRegime.getSupervisoryBody())
                .map(FieldWithMetaSupervisoryBodyEnum::getValue)
                .map(supervisoryBody::equals)
                .orElse(false);
    }

    private void updatePartyInformationForRegime(ReportingRegimeBuilder reportingRegimeBuilder, CorporateSector.CorporateSectorBuilder corporateSectorBuilder) {
        SupervisoryBodyEnum supervisoryBody = reportingRegimeBuilder.getSupervisoryBody().getValue();
        switch (supervisoryBody) {
            case ESMA:
                reportingRegimeBuilder.getOrCreateEsmaPartyInformation().setCorporateSector(corporateSectorBuilder);
                break;
            case FCA:
                reportingRegimeBuilder.getOrCreateFcaPartyInformation().setCorporateSector(corporateSectorBuilder);
                break;
            default:
                throw new RuntimeException("Cannot updated corporate sector, unexpected supervisoryBody " + supervisoryBody);
        }
    }

    private Optional<FinancialSectorEnum> getFinancialSectorEnum(String value) {
        try {
            return Optional.of(FinancialSectorEnum.valueOf(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<NonFinancialSector> getNonFinancialSector(String value) {
        try {
            return Optional.of(NonFinancialSectorEnum.valueOf(value))
                    .map(e ->
                            NonFinancialSector.builder()
                                    .setNonFinancialSectorIndicator(e)
                                    .setOrdinal(e.ordinal() + 1));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
