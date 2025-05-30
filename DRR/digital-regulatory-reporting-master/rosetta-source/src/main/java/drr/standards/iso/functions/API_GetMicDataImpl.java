package drr.standards.iso.functions;

import cdm.base.staticdata.party.PartyIdentifier;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.rosetta.model.lib.records.Date;
import drr.standards.iso.MicData;
import drr.standards.iso.MicTypeEnum;
import drr.standards.iso.MicMarketCategoryEnum;
import org.iso10383.Dataroot;
import org.iso10383.ISO10383MIC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static drr.standards.iso.MicData.MicDataBuilder;
import static drr.standards.iso.MicData.builder;
import static java.time.temporal.ChronoUnit.SECONDS;

public class API_GetMicDataImpl extends API_GetMicData {

    private static final Logger LOGGER = LoggerFactory.getLogger(API_GetMicDataImpl.class);
    private static final String DATA_SOURCE_URL = "https://www.iso20022.org/sites/default/files/ISO10383_MIC/ISO10383_MIC.xml";

    private final HttpClient httpClient;
    private final JAXBContext jaxbContext;

    @VisibleForTesting
    protected final Cache<String, MicData.MicDataBuilder> micDataCache =
            CacheBuilder.newBuilder()
                    .maximumSize(3000)
                    .build();

    public API_GetMicDataImpl() {
        this(HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(3))
                .build());
    }

    @VisibleForTesting
    public API_GetMicDataImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
        try {
            this.jaxbContext = JAXBContext.newInstance(Dataroot.class);
        } catch (JAXBException e) {
            LOGGER.error("JAXBContext Exception ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected MicDataBuilder doEvaluate(String mic) {
        if (null == mic) {
            return null;
        }
        // Check if cache is empty
        if (micDataCache.size() == 0) {
            Map<String, MicDataBuilder> data = load().stream()
                    .collect(Collectors.toMap(m -> m.getMic(), m -> m));
            micDataCache.putAll(data);
        }
        // Look up mic data from cache
        MicDataBuilder micData = Optional.ofNullable(mic)
                .map(micDataCache::getIfPresent)
                .orElse(null);
        LOGGER.info("Looked up mic {} and found data {}", mic, micData);
        return micData;
    }

    private List<MicDataBuilder> load() {
        LOGGER.info("Loading ISO10383_MIC data from www.iso20022.org");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(DATA_SOURCE_URL))
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();

            CompletableFuture<String> httpResponse =
                    httpClient
                            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenApply(HttpResponse::body);

            LOGGER.debug("Waiting for response");
            String body = httpResponse.join();
            LOGGER.debug("Got response");

            // unmarshall
            Dataroot dataroot = parseResponse(body);

            // transform
            List<MicDataBuilder> micData = dataroot.getISO10383MIC().stream()
                    .map(this::toMicData)
                    .collect(Collectors.toList());

            LOGGER.info("Loaded {} mic data item", micData.size());

            return micData;
        } catch (Exception e) {
            LOGGER.error("Exception occurred getting ISO10383_MIC data from www.iso20022.org", e);
            throw new RuntimeException(e);
        }
    }

    protected Dataroot parseResponse(String response) throws JAXBException {
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        InputStream inputStream = new ByteArrayInputStream(removeLineBreaks(response).getBytes(StandardCharsets.UTF_8));
        return (Dataroot) jaxbUnmarshaller.unmarshal(inputStream);
    }

    private String removeLineBreaks(String response) {
        return response
                .replace("\n", "")
                .replace("\t", "");
    }

    private MicDataBuilder toMicData(ISO10383MIC iso10383MIC) {
        MicDataBuilder micEnrichmentInformation = builder()
                .setMic(trim(iso10383MIC.getMIC()))
                .setOperatingMic(trim(iso10383MIC.getOPERATINGX0020MIC()))
                .setNameInstitutionDescription(trim(iso10383MIC.getMARKETX0020NAMEINSTITUTIONX0020DESCRIPTION()))
                .setLegalEntityName(trim(iso10383MIC.getLEGALX0020ENTITYX0020NAME()))
                .setAcronym(trim(iso10383MIC.getACRONYM()))
                .setCountryCode(trim(iso10383MIC.getISOX0020COUNTRYX0020CODEX0020X0028ISOX00203166X0029()))
                .setCity(trim(iso10383MIC.getCITY()))
                .setWebsite(trim(iso10383MIC.getWEBSITE()))
                .setStatus(trim(iso10383MIC.getSTATUS()))
                .setCreationDate(parseDate(iso10383MIC.getCREATIONX0020DATE()))
                .setLastUpdateDate(parseDate(iso10383MIC.getLASTX0020UPDATEX0020DATE()))
                .setLastValidationDate(parseDate(iso10383MIC.getLASTX0020VALIDATIONX0020DATE()))
                .setExpiryDate(parseDate(iso10383MIC.getEXPIRYX0020DATE()));

        Optional.ofNullable(trim(iso10383MIC.getOPRTX002FSGMT()))
                .map(this::toMicTypeEnum)
                .ifPresent(micEnrichmentInformation::setMicType);

        Optional.ofNullable(trim(iso10383MIC.getMARKETX0020CATEGORYX0020CODE()))
                .map(this::toMarketCategoryEnum)
                .ifPresent(micEnrichmentInformation::setMarketCategory);

        return micEnrichmentInformation;
    }

    private Date parseDate(String date) {
        date = trim(date);

        if ("".equals(date) || null == date) {
            return null;
        }
        return Date.of(LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE));
    }

    private static String trim(String s) {
        return Optional.ofNullable(s).map(String::trim).orElse(null);
    }


    private MicTypeEnum toMicTypeEnum(String micType) {
        try {
            return MicTypeEnum.valueOf(micType);
        } catch (Exception e) {
            LOGGER.warn("Unknown MIC type received {}", micType);
            return null;
        }
    }

    private MicMarketCategoryEnum toMarketCategoryEnum(String marketCategory) {
        try {
            return MicMarketCategoryEnum.valueOf(marketCategory);
        } catch (Exception e) {
            LOGGER.warn("Unknown MIC market category received {}", marketCategory);
            return null;
        }
    }
}
