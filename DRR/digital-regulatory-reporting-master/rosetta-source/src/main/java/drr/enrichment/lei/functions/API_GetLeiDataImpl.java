package drr.enrichment.lei.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import drr.enrichment.lei.LeiCategoryEnum;
import drr.enrichment.lei.LeiData;
import drr.enrichment.lei.LeiRegistrationStatusEnum;
import drr.enrichment.lei.LeiStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static java.time.temporal.ChronoUnit.SECONDS;

public class API_GetLeiDataImpl extends API_GetLeiData {

    private static final Logger LOGGER = LoggerFactory.getLogger(API_GetLeiDataImpl.class);
    private static final String DATA_SOURCE_URL = "https://api.gleif.org/api/v1/lei-records/";

    private final HttpClient httpClient;

    @VisibleForTesting
    protected final Cache<String, Optional<LeiData.LeiDataBuilder>> leiDataCache =
            CacheBuilder.newBuilder()
                    .maximumSize(150)
                    .build();

    public API_GetLeiDataImpl() {
        this(HttpClient.newBuilder()
                        .executor(Executors.newFixedThreadPool(3))
                        .build());
    }

    public API_GetLeiDataImpl(Map<String, String> preloadLeiData) {
        this();
        preloadLeiData.forEach((lei, value) -> {
            Optional<LeiData.LeiDataBuilder> leiData =
                    Optional.ofNullable(value)
                            .flatMap(jsonResponse ->
                                    Optional.ofNullable(parseResponseJson(jsonResponse)));
            leiDataCache.put(lei, leiData);
        });
    }
    @VisibleForTesting
    public API_GetLeiDataImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    protected LeiData.LeiDataBuilder doEvaluate(String lei) {
        return Optional.ofNullable(lei)
                .flatMap(x -> {
                    try {
                        Optional<LeiData.LeiDataBuilder> leiData = leiDataCache.get(x, () -> getLeiDataFromGleif(x));
                        if (!leiData.isPresent()) {
                            LOGGER.debug("LEI data not found for {}", lei);
                        }
                        return leiData;
                    } catch (ExecutionException e) {
                        LOGGER.error("LEI record cache exception", e);
                        return Optional.empty();
                    }
                })
                .orElse(null);
    }

    private Optional<LeiData.LeiDataBuilder> getLeiDataFromGleif(String lei) {
        LOGGER.info("Looking up LEI {} in GLEIF", lei);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(DATA_SOURCE_URL + lei))
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();

            CompletableFuture<HttpResponse<String>> httpResponse =
                    httpClient
                            .sendAsync(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.debug("Waiting for response");
            HttpResponse<String> response = httpResponse.join();
            LOGGER.debug("Got response");
            int statusCode = response.statusCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                LOGGER.error("Got error code from GLEIF: lei {}, status code {}", lei, statusCode);
                return Optional.empty();
            }

            LeiData.LeiDataBuilder leiData = parseResponseJson(response.body());
            LOGGER.info("Fetched LEI data from GLEIF {}", leiData);
            return Optional.of(leiData);
        } catch (Exception e) {
            LOGGER.error("Exception occurred getting LEI record from GLEIF", e);
            return Optional.empty();
        }
    }

    protected static LeiData.LeiDataBuilder parseResponseJson(String responseJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(responseJson);

            return LeiData.builder()
                    .setLei(extractField(node, "/data/attributes/lei").orElse(null))
                    .setEntityName(extractField(node, "/data/attributes/entity/legalName/name").orElse(null))
                    .setEntityCategory(extractField(node, "/data/attributes/entity/category").map(API_GetLeiDataImpl::toLeiCategoryEnum).orElse(null))
                    .setEntityStatus(extractField(node, "/data/attributes/entity/status").map(API_GetLeiDataImpl::toLeiStatusEnum).orElse(LeiStatusEnum.NULL))
                    .setBranchEntityStatus(extractField(node, "/data/attributes/branches/entityStatus").map(API_GetLeiDataImpl::toLeiStatusEnum).orElse(LeiStatusEnum.NULL))
                    .setRegistrationStatus(extractField(node, "/data/attributes/registration/status").map(API_GetLeiDataImpl::toLeiRegistrationStatusEnum).orElse(null))
                    .setRegistrationDate(extractField(node, "/data/attributes/registration/initialRegistrationDate").map(API_GetLeiDataImpl::parseZonedDateTime).orElse(null))
                    .setPublished(isPublished(node));
        } catch (JsonProcessingException e) {
            LOGGER.error("Error occurred parsing JSON response: {}", responseJson, e);
            return null;
        }
    }

    private static Optional<String> extractField(JsonNode node, String path) {
        return Optional.ofNullable(node.at(path))
                .map(JsonNode::asText)
                .filter(str -> !str.isEmpty());
    }

    private static LeiCategoryEnum toLeiCategoryEnum(String entityCategory) {
        try {
            return LeiCategoryEnum.valueOf(entityCategory);
        } catch (Exception e) {
            LOGGER.warn("Unknown LEI entity category received from GLEIF {}", entityCategory);
            return null;
        }
    }

    private static LeiStatusEnum toLeiStatusEnum(String leiStatus) {
        try {
            return LeiStatusEnum.valueOf(leiStatus);
        } catch (Exception e) {
            LOGGER.warn("Unknown LEI status received from GLEIF {}", leiStatus);
            return null;
        }
    }

    private static LeiRegistrationStatusEnum toLeiRegistrationStatusEnum(String registrationStatus) {
        try {
            return LeiRegistrationStatusEnum.valueOf(registrationStatus);
        } catch (Exception e) {
            LOGGER.warn("Unknown LEI registration status received from GLEIF {}", registrationStatus);
            return null;
        }
    }

    private static ZonedDateTime parseZonedDateTime(String zonedDateTime) {
        return ZonedDateTime.parse(zonedDateTime);
    }

    private static boolean isPublished(JsonNode node) {
        return extractField(node, "/data/type").map("lei-records"::equals).orElse(false)
                && extractField(node, "/data/id").isPresent()
                && extractField(node, "/data/links/self").isPresent();
    }
}

