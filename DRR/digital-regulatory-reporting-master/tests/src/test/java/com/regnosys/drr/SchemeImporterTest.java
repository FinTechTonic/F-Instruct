package com.regnosys.drr;

import com.regnosys.ingest.test.framework.ingestor.ExpectationUtil;
import com.regnosys.testing.schemaimport.SchemeImportInjectorProvider;
import com.regnosys.testing.schemaimport.SchemeImporterTestHelper;
import com.regnosys.testing.schemaimport.fpml.FpMLSchemeEnumReader;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(InjectionExtension.class)
@InjectWith(SchemeImportInjectorProvider.class)
public class SchemeImporterTest {

    private static final boolean WRITE_TEST_OUTPUT = ExpectationUtil.WRITE_EXPECTATIONS;
    private static final String BODY = "ISDA";
    private static final String CODING_SCHEME = "FpML_Coding_Scheme";
    private static final String ROSETTA_PATH_ROOT = "drr/rosetta";

    @Inject
    private SchemeImporterTestHelper schemeImporterTestHelper;
    @Inject
    private FpMLSchemeEnumReader fpMLSchemeEnumReader;

    @Test
    void checkFpMLEnumsAreValid() throws IOException {
        schemeImporterTestHelper.checkEnumsAreValid(ROSETTA_PATH_ROOT, "^drr\\..*", BODY, CODING_SCHEME, fpMLSchemeEnumReader, WRITE_TEST_OUTPUT);
    }

    @Test
    void writeIsFalse() {
        assertThat(WRITE_TEST_OUTPUT, equalTo(false));
    }

}
