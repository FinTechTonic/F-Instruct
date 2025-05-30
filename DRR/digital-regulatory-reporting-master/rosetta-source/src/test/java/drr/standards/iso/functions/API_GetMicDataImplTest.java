package drr.standards.iso.functions;

import com.google.common.io.Resources;
import com.rosetta.model.lib.records.Date;
import drr.standards.iso.MicData;
import drr.standards.iso.MicMarketCategoryEnum;
import drr.standards.iso.MicTypeEnum;
import org.iso10383.Dataroot;
import org.iso10383.ISO10383MIC;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class API_GetMicDataImplTest {

    private static final String DATA_PATH = "ISO10383_MIC.xml";

    @Test
    void testParseResponse() throws IOException, JAXBException {
        String isoResponse = loadDataFromXmlResource();

        API_GetMicDataImpl func = new API_GetMicDataImpl();

        Dataroot dataroot = func.parseResponse(isoResponse);

        ISO10383MIC iso10383MIC = dataroot.getISO10383MIC().stream()
                .filter(mic -> mic.getMIC().equals("DRSP"))
                .findFirst()
                .orElse(null);

        // assert data
        assertEquals("DRSP", iso10383MIC.getMIC());
        assertEquals("ACTIVE", iso10383MIC.getSTATUS());
        assertEquals("", iso10383MIC.getEXPIRYX0020DATE());

    }
    @Test
    void testWithHttpResponse() throws IOException {
        // Set up mocks
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        doReturn(CompletableFuture.completedFuture(httpResponse)).when(httpClient).sendAsync(any(), any());
        doReturn(loadDataFromXmlResource()).when(httpResponse).body();

        API_GetMicDataImpl func = new API_GetMicDataImpl(httpClient);

        // check cache is empty
        assertEquals(func.micDataCache.size(), 0);

        // run func
        MicData micData = func.doEvaluate("DRSP");

        // check cache is full
        assertEquals(func.micDataCache.size(), 2580);

        // assert data
        assertChecks(micData);
    }

    private String loadDataFromXmlResource() throws IOException {
        URL url = Objects.requireNonNull(Resources.getResource(DATA_PATH));
        return Resources.toString(url, StandardCharsets.UTF_8);
    }

    private void assertChecks(MicData micData) {
        assertEquals("DRSP", micData.getMic());
        assertEquals("DRSP", micData.getOperatingMic());
        assertEquals(MicTypeEnum.OPRT, micData.getMicType());
        assertEquals("EURONEXT UK - REPORTING SERVICES", micData.getNameInstitutionDescription());
        assertEquals("EURONEXT LONDON LIMITED",micData.getLegalEntityName());
        assertEquals(MicMarketCategoryEnum.APPA, micData.getMarketCategory());
        assertEquals("", micData.getAcronym());
        assertEquals("GB", micData.getCountryCode());
        assertEquals("LONDON", micData.getCity());
        assertEquals("WWW.EURONEXT.COM", micData.getWebsite());
        assertEquals("ACTIVE", micData.getStatus());
        assertEquals(Date.of(2021,9,27), micData.getCreationDate());
        assertEquals(Date.of(2021,9,27), micData.getLastUpdateDate());
        assertEquals(Date.of(2021,9,27), micData.getLastValidationDate());
        assertNull(micData.getExpiryDate());
    }
}
