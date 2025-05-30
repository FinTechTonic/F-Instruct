package drr.regulation.common.functions;

import cdm.base.math.metafields.FieldWithMetaNonNegativeQuantitySchedule;
import cdm.product.common.settlement.PriceQuantity;
import com.rosetta.model.lib.meta.Key;
import com.rosetta.model.metafields.MetaFields;

import java.util.*;
import java.util.stream.Collectors;

public class GetQuantityKeysImpl extends GetQuantityKeys {

    @Override
    protected List<String> doEvaluate(PriceQuantity priceQuantity) {
        return Optional.ofNullable(priceQuantity)
                .map(PriceQuantity::getQuantity)
                .orElse(Collections.emptyList())
                .stream()
                .map(FieldWithMetaNonNegativeQuantitySchedule::getMeta)
                .filter(Objects::nonNull)
                .map(MetaFields::getKey)
                .flatMap(Collection::stream)
                .map(Key::getKeyValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
