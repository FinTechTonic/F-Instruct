package drr.regulation.common.functions;

import cdm.base.math.metafields.ReferenceWithMetaNonNegativeQuantitySchedule;
import cdm.product.common.settlement.ResolvablePriceQuantity;
import com.rosetta.model.lib.meta.Reference;

import java.util.Optional;

public class GetQuantityReferenceImpl extends GetQuantityReference {

    @Override
    protected String doEvaluate(ResolvablePriceQuantity resolvablePriceQuantity) {
        return Optional.ofNullable(resolvablePriceQuantity)
                .map(ResolvablePriceQuantity::getQuantitySchedule)
                .map(ReferenceWithMetaNonNegativeQuantitySchedule::getReference)
                .map(Reference::getReference)
                .orElse(null);
    }
}
