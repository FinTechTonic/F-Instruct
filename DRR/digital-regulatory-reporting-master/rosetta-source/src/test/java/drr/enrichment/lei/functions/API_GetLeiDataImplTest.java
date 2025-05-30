package drr.enrichment.lei.functions;

import com.google.common.io.Resources;
import drr.enrichment.lei.LeiCategoryEnum;
import drr.enrichment.lei.LeiData;
import drr.enrichment.lei.LeiRegistrationStatusEnum;
import drr.enrichment.lei.LeiStatusEnum;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class API_GetLeiDataImplTest {

    private static final String LEI = "529900W18LQJJN6SJ336";

    @Test
    void parseResponseJson() throws IOException {
        String gleifResponseJson = readResource("gleif-api-response.json");

        API_GetLeiDataImpl func = new API_GetLeiDataImpl();
        LeiData leiData = func.parseResponseJson(gleifResponseJson);

        assertResponse(leiData);
    }

    @Test
    void testWithHttpResponse() throws IOException {
        String gleifResponseJson = readResource("gleif-api-response.json");
        // Set up mocks
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        doReturn(CompletableFuture.completedFuture(httpResponse)).when(httpClient).sendAsync(any(), any());
        doReturn(HttpURLConnection.HTTP_OK).when(httpResponse).statusCode();
        doReturn(gleifResponseJson).when(httpResponse).body();

        API_GetLeiDataImpl func = new API_GetLeiDataImpl(httpClient);

        // check cache is empty
        assertEquals(func.leiDataCache.size(), 0);

        // run func
        LeiData leiData = func.doEvaluate(LEI);

        // check cache is full
        assertEquals(func.leiDataCache.size(), 1);

        // assert data
        assertResponse(leiData);
    }

    @Test
    void testWithErrorHttpResponse() {
        // Set up mocks
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        doReturn(CompletableFuture.completedFuture(httpResponse)).when(httpClient).sendAsync(any(), any());
        doReturn(HttpURLConnection.HTTP_BAD_REQUEST).when(httpResponse).statusCode();

        API_GetLeiDataImpl func = new API_GetLeiDataImpl(httpClient);

        // check cache is empty
        assertEquals(func.leiDataCache.size(), 0);

        // run func
        LeiData leiData = func.doEvaluate(LEI);

        // check cache is full
        assertEquals(func.leiDataCache.size(), 1);

        // assert data
        assertNull(leiData);
    }

    private static void assertResponse(LeiData leiData) {
        assertEquals(LEI, leiData.getLei());
        assertEquals("Société Générale Effekten GmbH", leiData.getEntityName());
        assertEquals(LeiCategoryEnum.GENERAL, leiData.getEntityCategory());
        assertEquals(LeiStatusEnum.ACTIVE, leiData.getEntityStatus());
        assertEquals(LeiStatusEnum.NULL, leiData.getBranchEntityStatus());
        assertEquals(LeiRegistrationStatusEnum.ISSUED, leiData.getRegistrationStatus());
        assertEquals(ZonedDateTime.parse("2014-01-27T07:37:54Z"), leiData.getRegistrationDate());
        assertTrue(leiData.getPublished());
    }

    private String readResource(String path) throws IOException {
        URL url = Objects.requireNonNull(Resources.getResource(path));
        return Resources.toString(url, StandardCharsets.UTF_8);
    }
}