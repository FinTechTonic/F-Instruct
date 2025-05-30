package com.regnosys.drr.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LeiDataCacheHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeiDataCacheHelper.class);

    private static final String TEST_PACK_GLEIF_DATA = "regulatory-reporting/lookup/test-pack-gleif-data.json";

    public static Map<String, String> getPreloadCacheData() {
        // load from resources
        JsonNode leiToGleifJsonResponse = getLeiToGleifJsonResponse();
        // build as map
        return buildCacheAsMap(leiToGleifJsonResponse);
    }

    private static JsonNode getLeiToGleifJsonResponse() {
        try {
            URL url = Resources.getResource(TEST_PACK_GLEIF_DATA);
            String json = Resources.toString(url, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(json);
        } catch (IOException e) {
            LOGGER.error("Error occurred building LEI data cache preload", e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static Map<String, String> buildCacheAsMap(JsonNode leiToGleifJsonResponse) {
        Map<String, String> preloadCacheData = new HashMap<>();

        Iterator<String> leiIterator = leiToGleifJsonResponse.fieldNames();
        while (leiIterator.hasNext()) {
            String lei = leiIterator.next();
            JsonNode value = leiToGleifJsonResponse.path(lei);
            String jsonResponse = value.isNull() ? null : value.asText();
            LOGGER.debug("Preload LEI data cache [lei={}, jsonResponse={}]", lei, jsonResponse);
            preloadCacheData.put(lei, jsonResponse);
        }
        return preloadCacheData;
    }
}
