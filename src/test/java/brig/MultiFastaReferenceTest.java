package brig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import javax.swing.JTextArea;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for multi-FASTA reference support (GitHub issue #4).
 *
 * A multi-FASTA reference has multiple contigs. BRIG reformats them into a
 * single sequence with spacers, rewriting headers as {@code >name:start:stop}.
 * These tests verify the coordinate math through the pipeline:
 * formatMultiFASTA → FastaLength → GEN_LENGTH → reverseBlast → existingResult.
 */
class MultiFastaReferenceTest {

    private Document savedProfile;
    private int savedGenLength;
    private JTextArea savedOutputArea;

    @BeforeEach
    void saveGlobalState() {
        savedProfile = BRIG.PROFILE;
        savedGenLength = BRIG.GEN_LENGTH;
        savedOutputArea = BRIG.outputArea;
        // Redirect Print() to a dummy text area so tests don't pollute stdout
        BRIG.outputArea = new JTextArea();
    }

    @AfterEach
    void restoreGlobalState() {
        BRIG.PROFILE = savedProfile;
        BRIG.GEN_LENGTH = savedGenLength;
        BRIG.outputArea = savedOutputArea;
    }

    /** Minimal PROFILE Document with spacer attribute set. */
    private static Document profileWithSpacer(int spacer) {
        Element root = new Element("BRIG");
        root.setAttribute("spacer", Integer.toString(spacer));
        return new Document(root);
    }

    /**
     * Build one BLAST -outfmt 6 result line (12 tab-separated fields).
     *   qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore
     */
    private static String blastLine(String qid, String sid, double pident,
                                     int len, int sstart, int send,
                                     double evalue, double bitscore) {
        return String.join("\t",
                qid, sid,
                String.valueOf(pident), String.valueOf(len),
                "0", "0",                       // mismatch, gapopen
                "1", String.valueOf(len),       // qstart, qend
                String.valueOf(sstart), String.valueOf(send),
                String.valueOf(evalue), String.valueOf(bitscore));
    }

    // ---------------------------------------------------------------
    // formatMultiFASTA — coordinate math
    // ---------------------------------------------------------------

    @Test
    void formatMultiFASTA_spacerOffsetsCoordinatesCorrectly(@TempDir Path tmp)
            throws Exception {
        // 3 contigs: 8 bp, 7 bp, 4 bp with 100 bp spacer
        Path input = tmp.resolve("multi.fasta");
        Files.writeString(input,
                ">contig1 first\nATGCATGC\n"
              + ">contig2 second\nNNNATGC\n"
              + ">contig3 third\nATGC\n");
        Path output = tmp.resolve("formatted.fasta");

        BRIG.formatMultiFASTA(input.toString(), output.toString(), 100, false);

        List<String> lines = Files.readAllLines(output);
        // contig1: start=0, stop=8
        assertEquals(">contig1:0:8", lines.get(0));
        // contig2: start = 8 + 100 = 108, stop = 108 + 7 = 115
        assertEquals(">contig2:108:115", lines.get(2));
        // contig3: start = 115 + 100 = 215, stop = 215 + 4 = 219
        assertEquals(">contig3:215:219", lines.get(4));
    }

    @Test
    void formatMultiFASTA_zeroSpacer_coordinatesAreContiguous(@TempDir Path tmp)
            throws Exception {
        Path input = tmp.resolve("multi.fasta");
        Files.writeString(input,
                ">c1\nATGC\n"
              + ">c2\nATGCATGC\n");
        Path output = tmp.resolve("formatted.fasta");

        BRIG.formatMultiFASTA(input.toString(), output.toString(), 0, false);

        List<String> lines = Files.readAllLines(output);
        assertEquals(">c1:0:4", lines.get(0));
        assertEquals(">c2:4:12", lines.get(2));
    }

    @Test
    void formatMultiFASTA_preservesSequenceData(@TempDir Path tmp)
            throws Exception {
        Path input = tmp.resolve("multi.fasta");
        Files.writeString(input,
                ">c1\nATGCATGC\n"
              + ">c2\nNNNATGC\n");
        Path output = tmp.resolve("formatted.fasta");

        BRIG.formatMultiFASTA(input.toString(), output.toString(), 50, false);

        List<String> lines = Files.readAllLines(output);
        // Sequence lines are preserved verbatim
        assertEquals("ATGCATGC", lines.get(1));
        assertEquals("NNNATGC", lines.get(3));
    }

    // ---------------------------------------------------------------
    // FastaLength multi=true — round-trip with formatMultiFASTA
    // ---------------------------------------------------------------

    @Test
    void fastaLength_multiMode_returnsLastStopFromFormattedFile(@TempDir Path tmp)
            throws Exception {
        Path input = tmp.resolve("multi.fasta");
        Files.writeString(input,
                ">c1\nATGCATGC\n"     // 8 bp
              + ">c2\nNNNATGC\n"      // 7 bp
              + ">c3\nATGC\n");        // 4 bp
        Path formatted = tmp.resolve("formatted.fasta");

        BRIG.formatMultiFASTA(input.toString(), formatted.toString(), 100, false);

        int length = BRIG.FastaLength(formatted.toString(), true);
        // last header: >c3:215:219 → returns 219
        assertEquals(219, length);
    }

    // ---------------------------------------------------------------
    // GEN_LENGTH pipeline calculation
    // ---------------------------------------------------------------

    @Test
    void genLengthCalculation_matchesPipelineFormula(@TempDir Path tmp)
            throws Exception {
        // Reproduces the calculation from BRIGcli.runPipeline:
        //   GEN_LENGTH = FastaLength(formatted, true) + spacer + 10
        Path input = tmp.resolve("multi.fasta");
        Files.writeString(input,
                ">c1\nATGCATGC\n"
              + ">c2\nNNNATGC\n"
              + ">c3\nATGC\n");
        Path formatted = tmp.resolve("formatted.fasta");
        int spacer = 100;

        BRIG.formatMultiFASTA(input.toString(), formatted.toString(), spacer, false);
        int fastaLen = BRIG.FastaLength(formatted.toString(), true);
        int genLength = fastaLen + spacer + 10;

        assertEquals(219, fastaLen);
        assertEquals(329, genLength);
    }

    // ---------------------------------------------------------------
    // reverseBlast — multi-FASTA coordinate translation
    // ---------------------------------------------------------------

    @Test
    void reverseBlast_keepsNonOverlappingHitsOnDifferentContigs(@TempDir Path tmp)
            throws Exception {
        BRIG.PROFILE = profileWithSpacer(100);
        BRIG.GEN_LENGTH = 329;

        // Two hits on different contigs — they don't overlap after translation
        Path blastFile = tmp.resolve("blast.tab");
        Files.writeString(blastFile,
                blastLine("q1", "c1:0:8",     95.0, 8, 1, 8, 1e-10, 20.0) + "\n"
              + blastLine("q1", "c2:108:115",  90.0, 7, 1, 7, 1e-5,  16.0) + "\n");

        String resultFile = BRIG.reverseBlast(blastFile.toString(), false);
        List<String> results = Files.readAllLines(Path.of(resultFile));

        assertEquals(2, results.size(),
                "Both hits should survive — they map to non-overlapping regions");
    }

    @Test
    void reverseBlast_filtersOverlappingHitsOnSameContig(@TempDir Path tmp)
            throws Exception {
        BRIG.PROFILE = profileWithSpacer(100);
        BRIG.GEN_LENGTH = 329;

        // Two hits on the SAME contig covering the same region.
        // After e-value sort, the better hit (1e-10) claims positions first;
        // the worse hit (1e-3) fully overlaps → filtered out.
        Path blastFile = tmp.resolve("blast.tab");
        Files.writeString(blastFile,
                blastLine("q1", "c1:0:8", 95.0, 6, 1, 6, 1e-10, 20.0) + "\n"
              + blastLine("q2", "c1:0:8", 80.0, 6, 1, 6, 1e-3,  10.0) + "\n");

        String resultFile = BRIG.reverseBlast(blastFile.toString(), false);
        List<String> results = Files.readAllLines(Path.of(resultFile));

        assertEquals(1, results.size(),
                "Overlapping hit with worse e-value should be filtered");
        assertTrue(results.get(0).contains("95.0"),
                "The better hit (higher identity) should be kept");
    }

    @Test
    void reverseBlast_translatesCoordinatesByContigOffset(@TempDir Path tmp)
            throws Exception {
        BRIG.PROFILE = profileWithSpacer(100);
        // GEN_LENGTH must be large enough for translated coordinates.
        // A hit on c2:108:115 with sstart=1,send=7 translates to 109..115.
        BRIG.GEN_LENGTH = 329;

        // Two hits on different contigs — if coordinates were NOT translated,
        // they'd overlap (both at 1..7). After translation they don't.
        Path blastFile = tmp.resolve("blast.tab");
        Files.writeString(blastFile,
                blastLine("q1", "c1:0:8",    95.0, 7, 1, 7, 1e-10, 20.0) + "\n"
              + blastLine("q1", "c2:108:115", 90.0, 7, 1, 7, 1e-5,  16.0) + "\n");

        String resultFile = BRIG.reverseBlast(blastFile.toString(), false);
        List<String> results = Files.readAllLines(Path.of(resultFile));

        // Without translation: both map to 1..7, second would be filtered.
        // With translation: first maps to 1..7, second to 109..115 → both kept.
        assertEquals(2, results.size(),
                "Coordinate translation should prevent false overlap filtering");
    }

    @Test
    void reverseBlast_zeroGenLength_dropsAllResults(@TempDir Path tmp)
            throws Exception {
        // This demonstrates the root cause of issue #4 ("no rings"):
        // When GEN_LENGTH is 0 (e.g., spacer not configured), existingResult
        // receives an empty array and coordinates clamp to -1, causing
        // ArrayIndexOutOfBoundsException which is silently caught — every
        // hit is dropped and no rings are drawn.
        BRIG.PROFILE = profileWithSpacer(100);
        BRIG.GEN_LENGTH = 0;

        Path blastFile = tmp.resolve("blast.tab");
        Files.writeString(blastFile,
                blastLine("q1", "c1:0:8", 95.0, 8, 1, 8, 1e-10, 20.0) + "\n");

        String resultFile = BRIG.reverseBlast(blastFile.toString(), false);
        List<String> results = Files.readAllLines(Path.of(resultFile));

        assertTrue(results.isEmpty(),
                "With GEN_LENGTH=0 all results are silently dropped (root cause of 'no rings')");
    }

    // ---------------------------------------------------------------
    // existingResult — boundary conditions for multi-FASTA
    // ---------------------------------------------------------------

    @Test
    void existingResult_firstHitInRegionIsDrawn() {
        BRIG.GEN_LENGTH = 329;
        int[] existing = new int[BRIG.GEN_LENGTH];

        // Translated coordinate from contig2: positions 109..115
        @SuppressWarnings("rawtypes")
        List result = BRIG.existingResult("109", "115", existing);

        assertTrue((Boolean) result.get(0),
                "First hit in an untouched region should be drawn");
    }

    @Test
    void existingResult_duplicateRegionIsNotDrawn() {
        BRIG.GEN_LENGTH = 329;
        int[] existing = new int[BRIG.GEN_LENGTH];

        // First hit claims the region
        @SuppressWarnings("rawtypes")
        List first = BRIG.existingResult("109", "115", existing);
        int[] updated = (int[]) first.get(1);

        // Same region again — every position already visited
        @SuppressWarnings("rawtypes")
        List second = BRIG.existingResult("109", "115", updated);

        assertFalse((Boolean) second.get(0),
                "Fully overlapping duplicate should not be drawn");
    }

    @Test
    void existingResult_partialOverlapIsDrawn() {
        BRIG.GEN_LENGTH = 329;
        int[] existing = new int[BRIG.GEN_LENGTH];

        // First hit at 100..110
        @SuppressWarnings("rawtypes")
        List first = BRIG.existingResult("100", "110", existing);
        int[] updated = (int[]) first.get(1);

        // Second hit at 105..120 — partially overlaps but extends to new territory
        @SuppressWarnings("rawtypes")
        List second = BRIG.existingResult("105", "120", updated);

        assertTrue((Boolean) second.get(0),
                "Partially overlapping hit covering new positions should be drawn");
    }

    @Test
    void existingResult_clampsCoordinatesExceedingGenLength() {
        BRIG.GEN_LENGTH = 100;
        int[] existing = new int[BRIG.GEN_LENGTH];

        // Coordinates that exceed GEN_LENGTH get clamped to GEN_LENGTH-1
        @SuppressWarnings("rawtypes")
        List result = BRIG.existingResult("90", "200", existing);

        assertTrue((Boolean) result.get(0));
        // Should not throw ArrayIndexOutOfBoundsException
    }

    // ---------------------------------------------------------------
    // Protein multi-FASTA — coordinates tripled
    // ---------------------------------------------------------------

    @Test
    void formatMultiFASTA_proteinSequence_triplesCoordinates(@TempDir Path tmp)
            throws Exception {
        // Protein residues are counted as 3 bp each
        Path input = tmp.resolve("protein_multi.fasta");
        Files.writeString(input,
                ">gene1\nMVLS\n"       // 4 residues = 12 bp
              + ">gene2\nPADK\n");     // 4 residues = 12 bp
        Path output = tmp.resolve("formatted.fasta");

        BRIG.formatMultiFASTA(input.toString(), output.toString(), 50, false);

        List<String> lines = Files.readAllLines(output);
        // gene1: start=0, stop = 4*3 = 12
        assertEquals(">gene1:0:12", lines.get(0));
        // gene2: start = 12 + 50 = 62, stop = 62 + 12 = 74
        assertEquals(">gene2:62:74", lines.get(2));
    }

    @Test
    void reverseBlast_proteinMultiFasta_triplesHitCoordinates(@TempDir Path tmp)
            throws Exception {
        BRIG.PROFILE = profileWithSpacer(50);
        BRIG.GEN_LENGTH = 200;

        // Two hits: one on gene1, one on gene2. In protein mode (isPro=true),
        // sstart/send are multiplied by 3 before adding the contig offset.
        // gene1 hit: offset=0, sstart=1, send=4 → translated 0+(1*3)=3, 0+(4*3)=12
        // gene2 hit: offset=62, sstart=1, send=4 → translated 62+(1*3)=65, 62+(4*3)=74
        // These don't overlap → both should be kept.
        Path blastFile = tmp.resolve("blast.tab");
        Files.writeString(blastFile,
                blastLine("q1", "gene1:0:12",  95.0, 4, 1, 4, 1e-10, 20.0) + "\n"
              + blastLine("q1", "gene2:62:74", 90.0, 4, 1, 4, 1e-5,  16.0) + "\n");

        String resultFile = BRIG.reverseBlast(blastFile.toString(), true);
        List<String> results = Files.readAllLines(Path.of(resultFile));

        assertEquals(2, results.size(),
                "Protein hits on different contigs should both be kept");
    }
}
