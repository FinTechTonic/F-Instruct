package drr.regulation.common.functions;

import cdm.product.asset.CommodityPayout;
import com.rosetta.model.metafields.MetaFields;

import java.util.Optional;

public class GetCommodityKeyImpl extends GetCommodityKey {

    @Override
    protected String doEvaluate(CommodityPayout commodityPayout) {
        return Optional.ofNullable(commodityPayout)
                .map(CommodityPayout::getMeta)
                .map(MetaFields::getExternalKey)
                .orElse(null);
    }
}