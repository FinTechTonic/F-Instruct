package drr.enrichment.eic.functions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetAcceptedEicCodesImplTest {

    @Test
    void shouldPreloadAcceptedEicCodes() {
        GetAcceptedEicCodes acceptedEicCodesFunc = GetAcceptedEicCodesImpl.getInstanceWithPreloadedData();
        List<String> acceptedEicCodes = acceptedEicCodesFunc.evaluate();
        assertEquals(503, acceptedEicCodes.size());
        assertTrue(acceptedEicCodes.contains("10YFI-1--------U"));
        assertTrue(acceptedEicCodes.contains("59WFSRUGOLARTUNH"));
    }
}