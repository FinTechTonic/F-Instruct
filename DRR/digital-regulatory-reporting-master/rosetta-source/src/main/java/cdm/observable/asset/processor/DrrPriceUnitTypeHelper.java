package cdm.observable.asset.processor;

import cdm.base.math.UnitType;
import cdm.observable.asset.PriceSchedule;
import cdm.observable.asset.PriceTypeEnum;
import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.Path;
import com.rosetta.model.lib.path.RosettaPath;

import java.util.List;
import java.util.Optional;

import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.getNonNullMapping;

public class DrrPriceUnitTypeHelper extends PriceUnitTypeHelper {

    private final List<Mapping> mappings;

    private final RosettaPath unitCurrencyModelPath;
    private final RosettaPath perUnitOfCapacityModelPath;

    public DrrPriceUnitTypeHelper(RosettaPath modelPath, MappingContext context) {
        super(modelPath, context);
        this.mappings = context.getMappings();
        this.unitCurrencyModelPath = modelPath.getParent().newSubPath("unit").newSubPath("currency").newSubPath("value");
        this.perUnitOfCapacityModelPath = modelPath.getParent().newSubPath("perUnitOf").newSubPath("capacityUnit");
    }

    public boolean mapUnitType(Path synonymPath, PriceSchedule.PriceScheduleBuilder priceScheduleBuilder) {
        if (!Optional.ofNullable(priceScheduleBuilder.getPriceType()).isPresent()) {
            return false;
        }
        return updatePriceFromQuote(priceScheduleBuilder, synonymPath, "LastAvailableSpotPrice")
                || super.mapUnitType(synonymPath, priceScheduleBuilder);

    }

    protected boolean updatePriceFromQuote(PriceSchedule.PriceScheduleBuilder builder, Path valueSynonymPath, String measureType) {
        if (valueSynonymPath.endsWith("quote", "value")) {
            PriceTypeEnum priceType = builder.getPriceType();
            if (priceType == PriceTypeEnum.ASSET_PRICE) {
                // Get the quote path with the correct index
                Path quoteSynonymPath = getNonNullMappingEndsWith(mappings, "quote", "measureType")
                        .filter(m -> measureType.equals(m.getXmlValue()))
                        .map(Mapping::getXmlPath) // measureType path (with correct index)
                        .map(Path::getParent) // quote path
                        .orElse(null);
                if (quoteSynonymPath == null) {
                    return false;
                }
                Path currencySynonymPath = quoteSynonymPath.addElement("currency");
                Optional<Mapping> unitMapping = getNonNullMapping(mappings, currencySynonymPath);
                Optional<UnitType.UnitTypeBuilder> unit = unitMapping.map(this::toCurrencyUnitType);
                Path quoteUnitsPath = quoteSynonymPath.addElement("quoteUnits");
                Optional<Mapping> perUnitOfMapping = getNonNullMapping(mappings, quoteUnitsPath);
                Optional<UnitType.UnitTypeBuilder> perUnitOf = perUnitOfMapping .map(this::toCapacityUnitEnumType);

                return unit.flatMap(u ->
                                perUnitOf.map(p -> {
                                    // Update builder
                                    updateBuilder(builder, u, p);
                                    // Update mappings
                                    updateEmptyMappings(unitMapping.get().getXmlPath(), mappings, unitCurrencyModelPath);
                                    updateEmptyMappings(perUnitOfMapping.get().getXmlPath(), mappings, perUnitOfCapacityModelPath);
                                    return true;
                                })
                        )
                        .orElse(false);
            }
        }
        return false;
    }

    private static Optional<Mapping> getNonNullMappingEndsWith(List<Mapping> mappings, String... endsWith) {
        return mappings.stream()
                .filter(m -> m.getXmlPath().endsWith(endsWith))
                .filter(m -> m.getXmlValue() != null)
                .findFirst();
    }
}
