package com.regnosys.granite.ingestor.drr;

import com.regnosys.drr.DrrRuntimeModuleExternalApi;
import com.regnosys.ingest.test.framework.ingestor.ExpectationUtil;
import com.regnosys.ingest.test.framework.ingestor.IngestionTest;
import com.regnosys.ingest.test.framework.ingestor.IngestionTestUtil;
import com.regnosys.ingest.test.framework.ingestor.service.IngestionFactory;
import com.regnosys.ingest.test.framework.ingestor.service.IngestionService;
import drr.regulation.common.ReportableEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

class Fpml510RecordKeepingIngestionServiceTest extends IngestionTest<ReportableEvent> {

    private static final String SAMPLE_FILES_DIR = "cdm-sample-files/fpml-5-10/record-keeping/";
    public static final String INSTANCE = "target/FpML_5_10";
    private static IngestionService ingestionService;

    @BeforeAll
    static void setup() {
        writeActualExpectations = ExpectationUtil.WRITE_EXPECTATIONS;
        ClassLoader classLoader = Fpml510RecordKeepingIngestionServiceTest.class.getClassLoader();
        Collection<URL> ingestURLs = List.of(classLoader.getResource("ingestions/ingestions.json"), classLoader.getResource("ingestions/drr-ingestions.json"));
        DrrRuntimeModuleExternalApi runtimeModule = new DrrRuntimeModuleExternalApi();
        initialiseIngestionFactory(INSTANCE, ingestURLs, runtimeModule, IngestionTestUtil.getPostProcessors(runtimeModule));
        ingestionService = IngestionFactory.getInstance(INSTANCE).getService("FpML_5_RecordKeeping_To_ReportableEvent");
    }

    @Override
    protected Class<ReportableEvent> getClazz() {
        return ReportableEvent.class;
    }

    @Override
    protected IngestionService ingestionService() {
        return ingestionService;
    }

    @SuppressWarnings("unused")//used by the junit parameterized test
    private static Stream<Arguments> fpMLFiles() {
        return readExpectationsFromPath(SAMPLE_FILES_DIR);
    }
}