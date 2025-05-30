package drr.regulation.common.processor;

import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.Path;
import com.regnosys.rosetta.common.util.PathUtils;
import com.rosetta.model.lib.path.RosettaPath;
import drr.regulation.common.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static drr.regulation.common.CorporateSector.*;
import static drr.regulation.common.ESMAPartyInformation.*;
import static org.junit.jupiter.api.Assertions.*;

class CorporateSectorMappingProcessorTest {

    public static final Path SYNONYM_PATH = Path.parse("nonpublicExecutionReport.trade.tradeHeader.partyTradeInformation.partyReference");
    public static final RosettaPath MODEL_PATH = RosettaPath.valueOf("ReportableEvent.reportableInformation.partyInformation(0).regimeInformation");
    public static final String PARTY_REFERENCE1 = "party1";
    public static final String PARTY_REFERENCE2 = "party2";
    @Test
    void shouldMapFinancialSector() {
        // set up
        PartyInformation.PartyInformationBuilder parent = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA), getReportingRegime(SupervisoryBodyEnum.CFTC));

        Path partyReferencePath = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath = Path.parse("nonpublicExecutionReport.party[0].classification");
        Path classificationSchemePath = Path.parse("nonpublicExecutionReport.party[0].classification.industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath, "party1"));
        mappings.add(getErrorMapping(classificationPath, "INVF"));
        mappings.add(getErrorMapping(classificationSchemePath, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        processor.map(SYNONYM_PATH, builders, parent);

        // assert
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder = builders.get(0);
        assertNotNull(esmaReportingRegimeBuilder);
        ESMAPartyInformationBuilder esmaPartyInformation = esmaReportingRegimeBuilder.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation);
        CorporateSectorBuilder corporateSector = esmaPartyInformation.getCorporateSector();
        assertEquals(FinancialSectorEnum.INVF, corporateSector.getFinancialSector().get(0));

        Mapping classificationMapping = mappings.get(1);
        assertEquals(classificationPath, classificationMapping.getXmlPath());
        assertEquals("INVF", classificationMapping.getXmlValue());
        assertEquals(PathUtils.toPath(MODEL_PATH), classificationMapping.getRosettaPath());
        assertNull(classificationMapping.getError()); // e.g. updated to success
    }
    @Test
    void shouldMapFinancialSector2() {
        // set up
        PartyInformation.PartyInformationBuilder parent = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath = Path.parse("nonpublicExecutionReport.party[0].classification");
        Path classificationSchemePath = Path.parse("nonpublicExecutionReport.party[0].classification.industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath, "party1"));
        mappings.add(getErrorMapping(classificationPath, "F"));
        mappings.add(getErrorMapping(classificationSchemePath, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        processor.map(SYNONYM_PATH, builders, parent);

        // assert
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder = builders.get(0);
        assertNotNull(esmaReportingRegimeBuilder);
        ESMAPartyInformationBuilder esmaPartyInformation = esmaReportingRegimeBuilder.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation);
        CorporateSectorBuilder corporateSector = esmaPartyInformation.getCorporateSector();
        NonFinancialSector.NonFinancialSectorBuilder nonFinancialSector = corporateSector.getNonFinancialSector().get(0);
        assertEquals(NonFinancialSectorEnum.F, nonFinancialSector.getNonFinancialSectorIndicator());
        assertEquals(6, nonFinancialSector.getOrdinal());


        Mapping classificationMapping = mappings.get(1);
        assertEquals(classificationPath, classificationMapping.getXmlPath());
        assertEquals("F", classificationMapping.getXmlValue());
        assertEquals(PathUtils.toPath(MODEL_PATH), classificationMapping.getRosettaPath());
        assertNull(classificationMapping.getError()); // e.g. updated to success
    }

    @Test
    void shouldMapFinancialSector3() {
        // set up
        PartyInformation.PartyInformationBuilder parent1 = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders1 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath1 = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath1 = Path.parse("nonpublicExecutionReport.party[0].classification");
        Path classificationSchemePath1 = Path.parse("nonpublicExecutionReport.party[0].classification.industryClassificationScheme");

        PartyInformation.PartyInformationBuilder parent2 = getParent(PARTY_REFERENCE2);
        List<ReportingRegime.ReportingRegimeBuilder> builders2 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath2 = Path.parse("nonpublicExecutionReport.party[1].id");
        Path classificationPath2 = Path.parse("nonpublicExecutionReport.party[1].classification");
        Path classificationSchemePath2 = Path.parse("nonpublicExecutionReport.party[1].classification.industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath1, "party1"));
        mappings.add(getErrorMapping(classificationPath1, "INVF"));
        mappings.add(getErrorMapping(classificationSchemePath1, "http://www.fpml.org/coding-scheme/dummy-scheme"));
        mappings.add(getErrorMapping(partyReferencePath2, "party2"));
        mappings.add(getErrorMapping(classificationPath2, "IncorrectValue"));
        mappings.add(getErrorMapping(classificationSchemePath2, "http://www.fpml.org/coding-scheme/dummy-scheme"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);

        // Perform the mapping for builders1 and parent1
        processor.map(SYNONYM_PATH, builders1, parent1);
        // assert
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder1 = builders1.get(0);
        assertNotNull(esmaReportingRegimeBuilder1);
        ESMAPartyInformationBuilder esmaPartyInformation1 = esmaReportingRegimeBuilder1.getEsmaPartyInformation();
        //assertNotNull(esmaPartyInformation1);
        //assertNull(esmaPartyInformation1.getCorporateSector());   //check if it has CorporateSector within EsmaPartyInformation
        assertNull(esmaPartyInformation1);

        // Perform the mapping for builders2 and parent2
        processor.map(SYNONYM_PATH, builders2, parent2);

        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder2 = builders2.get(0);
        assertNotNull(esmaReportingRegimeBuilder2);
        ESMAPartyInformationBuilder esmaPartyInformation2 = esmaReportingRegimeBuilder2.getEsmaPartyInformation();
        //assertNotNull(esmaPartyInformation2);
        //assertNull(esmaPartyInformation2.getCorporateSector());
        assertNull(esmaPartyInformation2); //check if it has CorporateSector within EsmaPartyInformation is null, e.g. updated to success
    }

    @Test
    void shouldMapFinancialSector4() {
        // set up
        PartyInformation.PartyInformationBuilder parent1 = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders1 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath1 = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath1 = Path.parse("nonpublicExecutionReport.party[0].classification");
        Path classificationSchemePath1 = Path.parse("nonpublicExecutionReport.party[0].classification.industryClassificationScheme");

        PartyInformation.PartyInformationBuilder parent2 = getParent(PARTY_REFERENCE2);
        List<ReportingRegime.ReportingRegimeBuilder> builders2 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath2 = Path.parse("nonpublicExecutionReport.party[1].id");
        Path classificationPath2 = Path.parse("nonpublicExecutionReport.party[1].classification");
        Path classificationSchemePath2 = Path.parse("nonpublicExecutionReport.party[1].classification.industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath1, "party1"));
        mappings.add(getErrorMapping(classificationPath1, "INVF"));
        mappings.add(getErrorMapping(classificationSchemePath1, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));
        mappings.add(getErrorMapping(partyReferencePath2, "party2"));
        mappings.add(getErrorMapping(classificationPath2, "CDTI"));
        mappings.add(getErrorMapping(classificationSchemePath2, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        processor.map(SYNONYM_PATH, builders1, parent1);

        // assert
        Mapping classificationMapping1 = mappings.get(1);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder1 = builders1.get(0);
        assertNotNull(esmaReportingRegimeBuilder1);
        ESMAPartyInformationBuilder esmaPartyInformation1 = esmaReportingRegimeBuilder1.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation1);
        CorporateSectorBuilder corporateSector1 = esmaPartyInformation1.getCorporateSector();
        assertEquals(classificationPath1, classificationMapping1.getXmlPath());
        assertEquals(FinancialSectorEnum.INVF, corporateSector1.getFinancialSector().get(0));
        assertEquals("INVF", classificationMapping1.getXmlValue());
        assertNull(classificationMapping1.getError()); // e.g. updated to success


        Mapping classificationMapping2 = mappings.get(4);
        processor.map(SYNONYM_PATH, builders2, parent2);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder2 = builders2.get(0);
        assertNotNull(esmaReportingRegimeBuilder2);
        ESMAPartyInformationBuilder esmaPartyInformation2 = esmaReportingRegimeBuilder2.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation2);
        CorporateSectorBuilder corporateSector2 = esmaPartyInformation2.getCorporateSector();
        assertEquals(FinancialSectorEnum.CDTI, corporateSector2.getFinancialSector().get(0));
        assertEquals("CDTI", classificationMapping2.getXmlValue());
        assertEquals(classificationPath2, classificationMapping2.getXmlPath());
        assertEquals(PathUtils.toPath(MODEL_PATH), classificationMapping2.getRosettaPath());
        assertNull(classificationMapping2.getError()); // e.g. updated to success
    }
    @Test
    void shouldMapFinancialSector5() {
        // set up
        PartyInformation.PartyInformationBuilder parent1 = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders1 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath1 = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath1 = Path.parse("nonpublicExecutionReport.party[0].classification");
        Path classificationSchemePath1 = Path.parse("nonpublicExecutionReport.party[0].classification.industryClassificationScheme");

        PartyInformation.PartyInformationBuilder parent2 = getParent(PARTY_REFERENCE2);
        List<ReportingRegime.ReportingRegimeBuilder> builders2 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath2 = Path.parse("nonpublicExecutionReport.party[1].id");
        Path classificationPath2 = Path.parse("nonpublicExecutionReport.party[1].classification");
        Path classificationSchemePath2 = Path.parse("nonpublicExecutionReport.party[1].classification.industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath1, "party1"));
        mappings.add(getErrorMapping(classificationPath1, "INVF"));
        mappings.add(getErrorMapping(classificationSchemePath1, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));
        mappings.add(getErrorMapping(partyReferencePath2, "party2"));
        mappings.add(getErrorMapping(classificationPath2, "F"));
        mappings.add(getErrorMapping(classificationSchemePath2, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        processor.map(SYNONYM_PATH, builders1, parent1);

        // assert
        Mapping classificationMapping1 = mappings.get(1);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder1 = builders1.get(0);
        assertNotNull(esmaReportingRegimeBuilder1);
        ESMAPartyInformationBuilder esmaPartyInformation1 = esmaReportingRegimeBuilder1.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation1);
        CorporateSectorBuilder corporateSector1 = esmaPartyInformation1.getCorporateSector();
        assertEquals(classificationPath1, classificationMapping1.getXmlPath());
        assertEquals(FinancialSectorEnum.INVF, corporateSector1.getFinancialSector().get(0));
        assertEquals("INVF", classificationMapping1.getXmlValue());
        assertNull(classificationMapping1.getError()); // e.g. updated to success


        Mapping classificationMapping2 = mappings.get(4);
        processor.map(SYNONYM_PATH, builders2, parent2);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder2 = builders2.get(0);
        assertNotNull(esmaReportingRegimeBuilder2);
        ESMAPartyInformationBuilder esmaPartyInformation2 = esmaReportingRegimeBuilder2.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation2);
        CorporateSectorBuilder corporateSector2 = esmaPartyInformation2.getCorporateSector();
        NonFinancialSector.NonFinancialSectorBuilder nonFinancialSector = corporateSector2.getNonFinancialSector().get(0);
        assertEquals(NonFinancialSectorEnum.F, nonFinancialSector.getNonFinancialSectorIndicator());
        assertEquals(6, nonFinancialSector.getOrdinal());
        assertEquals("F", classificationMapping2.getXmlValue());
        assertEquals(classificationPath2, classificationMapping2.getXmlPath());
        assertEquals(PathUtils.toPath(MODEL_PATH), classificationMapping2.getRosettaPath());
        assertNull(classificationMapping2.getError()); // e.g. updated to success
    }

    @Test
    void shouldMapFinancialSector6() {
        // set up
        PartyInformation.PartyInformationBuilder parent1 = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders1 =
                List.of(getReportingRegime(SupervisoryBodyEnum.CFTC));

        Path partyReferencePath1 = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath1 = Path.parse("nonpublicExecutionReport.party[0].classification");
        Path classificationSchemePath1 = Path.parse("nonpublicExecutionReport.party[0].classification.industryClassificationScheme");

        PartyInformation.PartyInformationBuilder parent2 = getParent(PARTY_REFERENCE2);
        List<ReportingRegime.ReportingRegimeBuilder> builders2 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath2 = Path.parse("nonpublicExecutionReport.party[1].id");
        Path classificationPath2 = Path.parse("nonpublicExecutionReport.party[1].classification");
        Path classificationSchemePath2 = Path.parse("nonpublicExecutionReport.party[1].classification.industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath1, "party1"));
        mappings.add(getErrorMapping(classificationPath1, "INVF"));
        mappings.add(getErrorMapping(classificationSchemePath1, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));
        mappings.add(getErrorMapping(partyReferencePath2, "party2"));
        mappings.add(getErrorMapping(classificationPath2, "F"));
        mappings.add(getErrorMapping(classificationSchemePath2, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        processor.map(SYNONYM_PATH, builders1, parent1);

        // assert
        Mapping classificationMapping1 = mappings.get(1);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder1 = builders1.get(0);
        assertNotNull(esmaReportingRegimeBuilder1);
        ESMAPartyInformationBuilder esmaPartyInformation1 = esmaReportingRegimeBuilder1.getEsmaPartyInformation();
        assertNull(esmaPartyInformation1);
        assertEquals(classificationPath1, classificationMapping1.getXmlPath());
        assertEquals("INVF", classificationMapping1.getXmlValue());


        Mapping classificationMapping2 = mappings.get(4);
        processor.map(SYNONYM_PATH, builders2, parent2);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder2 = builders2.get(0);
        assertNotNull(esmaReportingRegimeBuilder2);
        ESMAPartyInformationBuilder esmaPartyInformation2 = esmaReportingRegimeBuilder2.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation2);
        CorporateSectorBuilder corporateSector2 = esmaPartyInformation2.getCorporateSector();
        NonFinancialSector.NonFinancialSectorBuilder nonFinancialSector = corporateSector2.getNonFinancialSector().get(0);
        assertEquals(NonFinancialSectorEnum.F, nonFinancialSector.getNonFinancialSectorIndicator());
        assertEquals(6, nonFinancialSector.getOrdinal());
        assertEquals("F", classificationMapping2.getXmlValue());
        assertEquals(classificationPath2, classificationMapping2.getXmlPath());
        assertEquals(PathUtils.toPath(MODEL_PATH), classificationMapping2.getRosettaPath());
        assertNull(classificationMapping2.getError()); // e.g. updated to success
    }

    @Test
    void shouldMapFinancialSector7() {
        // set up
        PartyInformation.PartyInformationBuilder parent1 = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders1 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath1 = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath1 = Path.parse("nonpublicExecutionReport.party[0].classification");
        Path classificationSchemePath1 = Path.parse("nonpublicExecutionReport.party[0].classification.industryClassificationScheme");

        PartyInformation.PartyInformationBuilder parent2 = getParent(PARTY_REFERENCE2);
        List<ReportingRegime.ReportingRegimeBuilder> builders2 =
                List.of(getReportingRegime(SupervisoryBodyEnum.CFTC));

        Path partyReferencePath2 = Path.parse("nonpublicExecutionReport.party[1].id");
        Path classificationPath2 = Path.parse("nonpublicExecutionReport.party[1].classification");
        Path classificationSchemePath2 = Path.parse("nonpublicExecutionReport.party[1].classification.industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath1, "party1"));
        mappings.add(getErrorMapping(classificationPath1, "INVF"));
        mappings.add(getErrorMapping(classificationSchemePath1, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));
        mappings.add(getErrorMapping(partyReferencePath2, "party2"));
        mappings.add(getErrorMapping(classificationPath2, "F"));
        mappings.add(getErrorMapping(classificationSchemePath2, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        processor.map(SYNONYM_PATH, builders1, parent1);

        // assert
        Mapping classificationMapping1 = mappings.get(1);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder1 = builders1.get(0);
        assertNotNull(esmaReportingRegimeBuilder1);
        ESMAPartyInformationBuilder esmaPartyInformation1 = esmaReportingRegimeBuilder1.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation1);
        CorporateSectorBuilder corporateSector1 = esmaPartyInformation1.getCorporateSector();
        assertEquals(classificationPath1, classificationMapping1.getXmlPath());
        assertEquals(FinancialSectorEnum.INVF, corporateSector1.getFinancialSector().get(0));
        assertEquals("INVF", classificationMapping1.getXmlValue());
        assertNull(classificationMapping1.getError()); // e.g. updated to success


        Mapping classificationMapping2 = mappings.get(4);
        processor.map(SYNONYM_PATH, builders2, parent2);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder2 = builders2.get(0);
        assertNotNull(esmaReportingRegimeBuilder2);
        ESMAPartyInformationBuilder esmaPartyInformation2 = esmaReportingRegimeBuilder2.getEsmaPartyInformation();
        assertNull(esmaPartyInformation2);
        assertEquals("F", classificationMapping2.getXmlValue());
        assertEquals(classificationPath2, classificationMapping2.getXmlPath());
        //assertEquals(PathUtils.toPath(MODEL_PATH), classificationMapping2.getRosettaPath());
        //assertNull(classificationMapping2.getError()); // e.g. updated to success
    }

    @Test
    void shouldMapFinancialSector8() {
        // set up
        PartyInformation.PartyInformationBuilder parent1 = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders1 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA),getReportingRegime(SupervisoryBodyEnum.CFTC));

        Path partyReferencePath1 = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath1 = Path.parse("nonpublicExecutionReport.party[0].classification");
        Path classificationSchemePath1 = Path.parse("nonpublicExecutionReport.party[0].classification.industryClassificationScheme");

        PartyInformation.PartyInformationBuilder parent2 = getParent(PARTY_REFERENCE2);
        List<ReportingRegime.ReportingRegimeBuilder> builders2 =
                List.of(getReportingRegime(SupervisoryBodyEnum.CFTC),getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath2 = Path.parse("nonpublicExecutionReport.party[1].id");
        Path classificationPath2 = Path.parse("nonpublicExecutionReport.party[1].classification");
        Path classificationSchemePath2 = Path.parse("nonpublicExecutionReport.party[1].classification.industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath1, "party1"));
        mappings.add(getErrorMapping(classificationPath1, "INVF"));
        mappings.add(getErrorMapping(classificationSchemePath1, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));
        mappings.add(getErrorMapping(partyReferencePath2, "party2"));
        mappings.add(getErrorMapping(classificationPath2, "F"));
        mappings.add(getErrorMapping(classificationSchemePath2, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);
        processor.map(SYNONYM_PATH, builders1, parent1);

        // assert
        Mapping classificationMapping1 = mappings.get(1);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder1 = builders1.get(0);
        assertNotNull(esmaReportingRegimeBuilder1);
        ESMAPartyInformationBuilder esmaPartyInformation1 = esmaReportingRegimeBuilder1.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation1);
        CorporateSectorBuilder corporateSector1 = esmaPartyInformation1.getCorporateSector();
        assertEquals(classificationPath1, classificationMapping1.getXmlPath());
        assertEquals(FinancialSectorEnum.INVF, corporateSector1.getFinancialSector().get(0));
        assertEquals("INVF", classificationMapping1.getXmlValue());
        assertNull(classificationMapping1.getError()); // e.g. updated to success

        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder2 = builders1.get(1);
        assertNotNull(esmaReportingRegimeBuilder1);
        ESMAPartyInformationBuilder esmaPartyInformation2 = esmaReportingRegimeBuilder2.getEsmaPartyInformation();
        assertNull(esmaPartyInformation2);
        //CorporateSectorBuilder corporateSector2 = esmaPartyInformation2.getCorporateSector();
        assertEquals(classificationPath1, classificationMapping1.getXmlPath());
        //assertEquals(FinancialSectorEnum.INVF, corporateSector2.getFinancialSector());
        assertEquals("INVF", classificationMapping1.getXmlValue());


        Mapping classificationMapping2 = mappings.get(4);
        processor.map(SYNONYM_PATH, builders2, parent2);
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder3 = builders2.get(0);
        assertNotNull(esmaReportingRegimeBuilder3);
        ESMAPartyInformationBuilder esmaPartyInformation3 = esmaReportingRegimeBuilder3.getEsmaPartyInformation();
        assertNull(esmaPartyInformation3);
        assertEquals("F", classificationMapping2.getXmlValue());
        assertEquals(classificationPath2, classificationMapping2.getXmlPath());

        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder4 = builders2.get(1);
        assertNotNull(esmaReportingRegimeBuilder4);
        ESMAPartyInformationBuilder esmaPartyInformation4 = esmaReportingRegimeBuilder4.getEsmaPartyInformation();
        assertNotNull(esmaPartyInformation4);
        CorporateSectorBuilder corporateSector2 = esmaPartyInformation4.getCorporateSector();
        NonFinancialSector.NonFinancialSectorBuilder nonFinancialSector = corporateSector2.getNonFinancialSector().get(0);
        assertEquals(NonFinancialSectorEnum.F, nonFinancialSector.getNonFinancialSectorIndicator());
        assertEquals(6, nonFinancialSector.getOrdinal());
        assertEquals("F", classificationMapping2.getXmlValue());
        assertEquals(classificationPath2, classificationMapping2.getXmlPath());
        assertEquals(PathUtils.toPath(MODEL_PATH), classificationMapping2.getRosettaPath());
        assertNull(classificationMapping2.getError()); // e.g. updated to success
    }
    @Test
    void shouldMapFinancialSector9() {
        // set up
        PartyInformation.PartyInformationBuilder parent1 = getParent(PARTY_REFERENCE1);
        List<ReportingRegime.ReportingRegimeBuilder> builders1 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath1 = Path.parse("nonpublicExecutionReport.party[0].id");
        Path classificationPath1 = Path.parse("nonpublicExecutionReport.party[0].classification[0]");
        Path classificationSchemePath1 = Path.parse("nonpublicExecutionReport.party[0].classification[0].industryClassificationScheme");
        Path classificationPath2 = Path.parse("nonpublicExecutionReport.party[0].classification[1]");
        Path classificationSchemePath2 = Path.parse("nonpublicExecutionReport.party[0].classification[1].industryClassificationScheme");

        PartyInformation.PartyInformationBuilder parent2 = getParent(PARTY_REFERENCE2);
        List<ReportingRegime.ReportingRegimeBuilder> builders2 =
                List.of(getReportingRegime(SupervisoryBodyEnum.ESMA));

        Path partyReferencePath3 = Path.parse("nonpublicExecutionReport.party[1].id");
        Path classificationPath3 = Path.parse("nonpublicExecutionReport.party[1].classification[0]");
        Path classificationSchemePath3 = Path.parse("nonpublicExecutionReport.party[1].classification[0].industryClassificationScheme");
        Path classificationPath4 = Path.parse("nonpublicExecutionReport.party[1].classification[1]");
        Path classificationSchemePath4 = Path.parse("nonpublicExecutionReport.party[1].classification[1].industryClassificationScheme");

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(getErrorMapping(partyReferencePath1, "party1"));
        mappings.add(getErrorMapping(classificationPath1, "F"));
        mappings.add(getErrorMapping(classificationPath2, "G"));
        mappings.add(getErrorMapping(classificationSchemePath1, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector-1-0"));
        mappings.add(getErrorMapping(classificationSchemePath2, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector-1-0"));
        mappings.add(getErrorMapping(partyReferencePath3, "party2"));
        mappings.add(getErrorMapping(classificationPath3, "H"));
        mappings.add(getErrorMapping(classificationPath4, "I"));
        mappings.add(getErrorMapping(classificationSchemePath3, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));
        mappings.add(getErrorMapping(classificationSchemePath4, "http://www.fpml.org/coding-scheme/esma-emir-refit-regulatory-corporate-sector"));

        MappingContext context = new MappingContext(mappings, null, null, null);

        // test
        CorporateSectorMappingProcessor processor = new CorporateSectorMappingProcessor(MODEL_PATH, List.of(SYNONYM_PATH), context);

        // Perform the mapping for builders1 and parent1
        processor.map(SYNONYM_PATH, builders1, parent1);
        // assert
        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder1 = builders1.get(0);
        assertNotNull(esmaReportingRegimeBuilder1);
        ESMAPartyInformationBuilder esmaPartyInformation1 = esmaReportingRegimeBuilder1.getEsmaPartyInformation();
        //assertNotNull(esmaPartyInformation1);
        //assertNull(esmaPartyInformation1.getCorporateSector());   //check if it has CorporateSector within EsmaPartyInformation
        assertNull(esmaPartyInformation1);

        // Perform the mapping for builders2 and parent2
        processor.map(SYNONYM_PATH, builders2, parent2);

        ReportingRegime.ReportingRegimeBuilder esmaReportingRegimeBuilder2 = builders2.get(0);
        assertNotNull(esmaReportingRegimeBuilder2);
        ESMAPartyInformationBuilder esmaPartyInformation2 = esmaReportingRegimeBuilder2.getEsmaPartyInformation();
        //assertNotNull(esmaPartyInformation2);
        //assertNull(esmaPartyInformation2.getCorporateSector());
        assertNotNull(esmaPartyInformation2); //check if it has CorporateSector within EsmaPartyInformation is null, e.g. updated to success
    }

    private static ReportingRegime.ReportingRegimeBuilder getReportingRegime(SupervisoryBodyEnum supervisoryBody) {
        return ReportingRegime.builder().setSupervisoryBodyValue(supervisoryBody);
    }

    private static PartyInformation.PartyInformationBuilder getParent(String partyReference) {
        return PartyInformation.builder()
                .setPartyReference(ReferenceWithMetaParty.builder()
                        .setExternalReference(partyReference));
    }

    @NotNull
    private static Mapping getErrorMapping(Path xmlPath, String xmlValue) {
        return new Mapping(xmlPath, xmlValue, null, null, "Not found", false, false, false);
    }

}
