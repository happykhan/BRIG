package brig;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for pure/static utility methods in BRIG.java.
 */
class BRIGStaticMethodsTest {

    private Document savedProfile;

    @BeforeEach
    void saveGlobalState() {
        savedProfile = BRIG.PROFILE;
    }

    @AfterEach
    void restoreGlobalState() {
        BRIG.PROFILE = savedProfile;
    }

    // -------------------------------------------------------------------
    // formatCommand
    // -------------------------------------------------------------------

    @Test
    void formatCommand_simpleArgs() {
        List<String> cmd = Arrays.asList("blastn", "-query", "input.fna");
        assertEquals("blastn -query input.fna", BRIG.formatCommand(cmd));
    }

    @Test
    void formatCommand_quotesArgsWithSpaces() {
        List<String> cmd = Arrays.asList("blastn", "-query", "my file.fna");
        assertEquals("blastn -query \"my file.fna\"", BRIG.formatCommand(cmd));
    }

    @Test
    void formatCommand_emptyList() {
        assertEquals("", BRIG.formatCommand(List.of()));
    }

    @Test
    void formatCommand_singleArg() {
        assertEquals("blastn", BRIG.formatCommand(List.of("blastn")));
    }

    // -------------------------------------------------------------------
    // tokenizeOptions
    // -------------------------------------------------------------------

    @Test
    void tokenizeOptions_splitsOnWhitespace() {
        List<String> result = BRIG.tokenizeOptions("-evalue 1e-5 -dust no");
        assertEquals(List.of("-evalue", "1e-5", "-dust", "no"), result);
    }

    @Test
    void tokenizeOptions_handlesNull() {
        assertTrue(BRIG.tokenizeOptions(null).isEmpty());
    }

    @Test
    void tokenizeOptions_handlesEmpty() {
        assertTrue(BRIG.tokenizeOptions("").isEmpty());
    }

    @Test
    void tokenizeOptions_handlesBlank() {
        assertTrue(BRIG.tokenizeOptions("   ").isEmpty());
    }

    @Test
    void tokenizeOptions_handlesExtraWhitespace() {
        List<String> result = BRIG.tokenizeOptions("  -evalue   1e-5  ");
        assertEquals(List.of("-evalue", "1e-5"), result);
    }

    @Test
    void tokenizeOptions_singleToken() {
        assertEquals(List.of("-dust"), BRIG.tokenizeOptions("-dust"));
    }

    // -------------------------------------------------------------------
    // OutputColour
    // -------------------------------------------------------------------

    @Test
    void outputColour_fullIdentity() {
        // At 100% identity with lowerInt=50, should return close to the base color
        int result = BRIG.OutputColour(50, 102, 100);
        assertTrue(result <= 225, "Should not exceed 225");
    }

    @Test
    void outputColour_atLowerThreshold() {
        // At lowerInt identity, formula: 225 + (lowerInt - lowerInt) * ((base - 225) / (100 - lowerInt))
        // = 225 + 0 = 225
        assertEquals(225, BRIG.OutputColour(50, 102, 50));
    }

    @Test
    void outputColour_midIdentity() {
        int result = BRIG.OutputColour(50, 100, 75);
        assertTrue(result >= 100 && result <= 225,
                "Mid-identity should be between base and 225, got: " + result);
    }

    @Test
    void outputColour_clipsAt225() {
        // When the formula would exceed 225, should be clipped
        int result = BRIG.OutputColour(90, 250, 91);
        assertTrue(result <= 225, "Should clip at 225");
    }

    @Test
    void outputColour_zeroBase() {
        // base=0, at lowerInt: 225 + 0 = 225
        assertEquals(225, BRIG.OutputColour(70, 0, 70));
    }

    // -------------------------------------------------------------------
    // autoScale
    // -------------------------------------------------------------------

    @Test
    void autoScale_typicalGenome() {
        // 5 million bp / 3000 ≈ 1666
        assertEquals(1666, BRIG.autoScale(5_000_000));
    }

    @Test
    void autoScale_smallSequence() {
        // 50_000 / 3000 = 16, clamped to minimum of 50
        assertEquals(50, BRIG.autoScale(50_000));
    }

    @Test
    void autoScale_zeroLength() {
        assertEquals(50, BRIG.autoScale(0));
    }

    @Test
    void autoScale_smallPlasmid() {
        // 3000 / 3000 = 1, but minimum is 50
        assertEquals(50, BRIG.autoScale(3000));
    }

    @Test
    void autoScale_exactMultiple() {
        // 150000 / 3000 = 50 = minimum, so same result
        assertEquals(50, BRIG.autoScale(150_000));
    }

    // -------------------------------------------------------------------
    // existingResult
    // -------------------------------------------------------------------

    @Test
    void existingResult_newRegion_returnsTrue() {
        BRIG.GEN_LENGTH = 100;
        int[] existing = new int[100];

        List result = BRIG.existingResult("10", "20", existing);

        assertTrue((Boolean) result.get(0), "Should draw new region");
        int[] updated = (int[]) result.get(1);
        for (int i = 10; i <= 20; i++) {
            assertEquals(1, updated[i], "Position " + i + " should be marked");
        }
    }

    @Test
    void existingResult_fullyOverlapping_returnsFalse() {
        BRIG.GEN_LENGTH = 100;
        int[] existing = new int[100];
        // Pre-fill positions 10-20
        for (int i = 10; i <= 20; i++) {
            existing[i] = 1;
        }

        List result = BRIG.existingResult("10", "20", existing);

        assertFalse((Boolean) result.get(0), "Should not draw fully overlapping region");
    }

    @Test
    void existingResult_partialOverlap_returnsTrue() {
        BRIG.GEN_LENGTH = 100;
        int[] existing = new int[100];
        // Pre-fill positions 10-15
        for (int i = 10; i <= 15; i++) {
            existing[i] = 1;
        }

        List result = BRIG.existingResult("10", "20", existing);

        assertTrue((Boolean) result.get(0), "Should draw partially overlapping region");
    }

    @Test
    void existingResult_clampsToGenLength() {
        BRIG.GEN_LENGTH = 50;
        int[] existing = new int[50];

        // start/stop beyond GEN_LENGTH should be clamped
        List result = BRIG.existingResult("40", "60", existing);

        assertNotNull(result);
        assertTrue((Boolean) result.get(0));
    }

    // -------------------------------------------------------------------
    // FormatArchive
    // -------------------------------------------------------------------

    @Test
    void formatArchive_setsOutputFolder() {
        Document doc = createArchiveDocument();
        String file = "/home/user/archive/brig-archive.xml";

        Document result = BRIG.FormatArchive(doc, file);

        assertEquals("/home/user/archive/", result.getRootElement().getAttributeValue("outputFolder"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void formatArchive_reconnectsSequenceLocations() {
        Document doc = createArchiveDocument();
        String file = "/home/user/archive/brig-archive.xml";

        Document result = BRIG.FormatArchive(doc, file);

        List<Element> rings = result.getRootElement().getChildren("ring");
        Element seq = ((Element) rings.get(0).getChildren("sequence").get(0));
        assertTrue(seq.getAttributeValue("location").startsWith("/home/user/archive/"),
                "Sequence location should be prefixed with archive dir");
    }

    @Test
    @SuppressWarnings("unchecked")
    void formatArchive_reconnectsRefDirLocations() {
        Document doc = createArchiveDocument();
        String file = "/home/user/archive/brig-archive.xml";

        Document result = BRIG.FormatArchive(doc, file);

        List<Element> dirs = result.getRootElement().getChildren("refDir");
        assertTrue(dirs.get(0).getAttributeValue("location").startsWith("/home/user/archive/"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void formatArchive_skipsGCSpecialLocations() {
        Document doc = createArchiveDocument();
        // Add a sequence with GC Skew location
        Element ring2 = new Element("ring");
        ring2.setAttribute("position", "1");
        Element gcSeq = new Element("sequence");
        gcSeq.setAttribute("location", "GC Skew");
        ring2.addContent(gcSeq);
        doc.getRootElement().addContent(ring2);

        String file = "/home/user/archive/brig-archive.xml";
        Document result = BRIG.FormatArchive(doc, file);

        List<Element> rings = result.getRootElement().getChildren("ring");
        Element gcRing = rings.get(1);
        Element gcSequence = (Element) gcRing.getChildren("sequence").get(0);
        assertEquals("GC Skew", gcSequence.getAttributeValue("location"),
                "GC Skew location should not be prefixed");
    }

    @Test
    void formatArchive_updatesQueryFile() {
        Document doc = createArchiveDocument();
        String file = "/home/user/archive/brig-archive.xml";

        Document result = BRIG.FormatArchive(doc, file);

        assertTrue(result.getRootElement().getAttributeValue("queryFile")
                .startsWith("/home/user/archive/"));
    }

    // -------------------------------------------------------------------
    // resolveGcWindow (issue #56 — configurable GC skew/content window)
    // -------------------------------------------------------------------

    @Test
    void resolveGcWindow_noProfile_usesAutoScale() {
        BRIG.GEN_LENGTH = 3_000_000;
        BRIG.PROFILE = null;
        // autoScale(3_000_000) = 3_000_000 / 3000 = 1000
        assertEquals(1000, BRIG.resolveGcWindow());
    }

    @Test
    void resolveGcWindow_withGcWindowSetting_returnsConfiguredValue() {
        BRIG.GEN_LENGTH = 3_000_000;
        Document doc = new Document(new Element("BRIG"));
        Element brigSettings = new Element("brig_settings");
        brigSettings.setAttribute("gcWindow", "500");
        doc.getRootElement().addContent(brigSettings);
        BRIG.PROFILE = doc;

        assertEquals(500, BRIG.resolveGcWindow());
    }

    @Test
    void resolveGcWindow_withInvalidGcWindowSetting_fallsBackToAutoScale() {
        BRIG.GEN_LENGTH = 6_000_000;
        Document doc = new Document(new Element("BRIG"));
        Element brigSettings = new Element("brig_settings");
        brigSettings.setAttribute("gcWindow", "not-a-number");
        doc.getRootElement().addContent(brigSettings);
        BRIG.PROFILE = doc;

        // autoScale(6_000_000) = 2000
        assertEquals(2000, BRIG.resolveGcWindow());
    }

    @Test
    void resolveGcWindow_withZeroGcWindow_fallsBackToAutoScale() {
        BRIG.GEN_LENGTH = 3_000_000;
        Document doc = new Document(new Element("BRIG"));
        Element brigSettings = new Element("brig_settings");
        brigSettings.setAttribute("gcWindow", "0");
        doc.getRootElement().addContent(brigSettings);
        BRIG.PROFILE = doc;

        assertEquals(1000, BRIG.resolveGcWindow());
    }

    // -------------------------------------------------------------------
    // Same-filename BLAST output paths (issue #53)
    // -------------------------------------------------------------------

    @Test
    void blastOutputPath_differentRingIndices_produceDistinctPaths() {
        String output = "/tmp/out";
        String queryFastaFile = "/ref/reference.fna";
        String fileA = "/folderA/genome.fna";
        String fileB = "/folderB/genome.fna";

        String ouA = BRIG.blastOutputPath(output, 0, 0, fileA, queryFastaFile);
        String ouB = BRIG.blastOutputPath(output, 1, 0, fileB, queryFastaFile);

        assertNotEquals(ouA, ouB,
                "Two same-named files in different rings must produce distinct BLAST output paths");
    }

    @Test
    void blastOutputPath_sameRingDifferentSeqIndices_produceDistinctPaths() {
        String output = "/tmp/out";
        String queryFastaFile = "/ref/reference.fna";
        String fileA = "/folderA/genome.fna";
        String fileB = "/folderB/genome.fna";

        String ouA = BRIG.blastOutputPath(output, 0, 0, fileA, queryFastaFile);
        String ouB = BRIG.blastOutputPath(output, 0, 1, fileB, queryFastaFile);

        assertNotEquals(ouA, ouB,
                "Two same-named files in different sequence slots must produce distinct BLAST output paths");
    }

    // -------------------------------------------------------------------
    // blastOutputPath helper (extracted from RunBlast)
    // -------------------------------------------------------------------

    @Test
    void blastOutputPath_containsRingAndSeqIndices() {
        String path = BRIG.blastOutputPath("/out", 3, 7, "/dir/genome.fna", "/ref/ref.fna");
        assertTrue(path.contains("r3s7_"), "Path must contain ring/seq prefix");
        assertTrue(path.endsWith(".tab"), "Path must end with .tab");
    }

    @Test
    void blastOutputPath_usesFilenamesNotFullPaths() {
        String path = BRIG.blastOutputPath("/out", 0, 0, "/deep/dir/genome.fna", "/other/ref.fna");
        assertFalse(path.contains("/deep/dir/"), "Path should not contain full directory of ring file");
        assertTrue(path.contains("genome.fna"));
        assertTrue(path.contains("ref.fna"));
    }

    // -------------------------------------------------------------------
    // autoScale — minimum window for small sequences
    // -------------------------------------------------------------------

    @Test
    void autoScale_plasmid3kb_returnsMinimum50() {
        // 3000 bp plasmid: 3000/3000 = 1, clamped to 50
        assertEquals(50, BRIG.autoScale(3000));
    }

    @Test
    void autoScale_largeGenome_scalesNormally() {
        // 5 Mbp: 5000000/3000 = 1666, well above minimum
        assertEquals(1666, BRIG.autoScale(5_000_000));
        assertTrue(BRIG.autoScale(5_000_000) > 50);
    }

    // -------------------------------------------------------------------
    // Default BLAST threads — min(2, available)
    // -------------------------------------------------------------------

    @Test
    void getBlastThreads_default_atMostTwo() {
        BRIG.PROFILE = null;
        int threads = BlastSettings.getBlastThreads();
        assertTrue(threads >= 1 && threads <= 2,
                "Default threads should be min(2, CPUs) but was " + threads);
    }

    @Test
    void getBlastThreads_profileOverride_returnsConfiguredValue() {
        Document doc = new Document(new Element("BRIG"));
        Element settings = new Element("brig_settings");
        settings.setAttribute("blastThreads", "8");
        doc.getRootElement().addContent(settings);
        BRIG.PROFILE = doc;

        assertEquals(8, BlastSettings.getBlastThreads());
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private Document createArchiveDocument() {
        Document doc = new Document(new Element("BRIG"));
        Element root = doc.getRootElement();
        root.setAttribute("queryFile", "ref.fna");
        root.setAttribute("outputFolder", "");

        Element ring = new Element("ring");
        ring.setAttribute("position", "0");
        Element seq = new Element("sequence");
        seq.setAttribute("location", "gen/sample.fna");
        ring.addContent(seq);
        root.addContent(ring);

        Element refDir = new Element("refDir");
        refDir.setAttribute("location", "gen");
        Element refFile = new Element("refFile");
        refFile.setAttribute("location", "gen/sample.fna");
        refDir.addContent(refFile);
        root.addContent(refDir);

        return doc;
    }
}
