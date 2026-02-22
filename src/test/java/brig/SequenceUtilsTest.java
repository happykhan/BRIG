package brig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jdom.Document;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Direct unit tests for SequenceUtils static methods.
 */
class SequenceUtilsTest {

    // -------------------------------------------------------------------
    // FetchFilename
    // -------------------------------------------------------------------

    @Test
    void fetchFilename_absolutePath() {
        String result = SequenceUtils.FetchFilename("/home/user/data/genome.fna");
        assertEquals("genome.fna", result);
    }

    @Test
    void fetchFilename_relativeFilename() {
        assertEquals("genome.fna", SequenceUtils.FetchFilename("genome.fna"));
    }

    @Test
    void fetchFilename_emptyString() {
        assertEquals("", SequenceUtils.FetchFilename(""));
    }

    @Test
    void fetchFilename_directoryTrailing() {
        String result = SequenceUtils.FetchFilename("/home/user" + File.separator + "file.txt");
        assertEquals("file.txt", result);
    }

    // -------------------------------------------------------------------
    // FastaLength
    // -------------------------------------------------------------------

    @Test
    void fastaLength_singleFasta(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("test.fna");
        Files.writeString(fasta, ">seq1\nATGCATGC\nNNNATGC\n");
        // 8 + 7 = 15
        assertEquals(15, SequenceUtils.FastaLength(fasta.toString(), false));
    }

    @Test
    void fastaLength_proteinFasta(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("test.faa");
        Files.writeString(fasta, ">prot1\nMVLSPAD\n");
        // 7 amino acids * 3 = 21
        assertEquals(21, SequenceUtils.FastaLength(fasta.toString(), false));
    }

    @Test
    void fastaLength_multiFastaMode(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("formatted.fna");
        Files.writeString(fasta,
                ">contig1:0:100\nATGC\n>contig2:150:300\nATGC\n");
        // multi=true reads last :stop value
        assertEquals(300, SequenceUtils.FastaLength(fasta.toString(), true));
    }

    // -------------------------------------------------------------------
    // isMultiFasta
    // -------------------------------------------------------------------

    @Test
    void isMultiFasta_single(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("single.fna");
        Files.writeString(fasta, ">seq1\nATGC\n");
        assertEquals(1, SequenceUtils.isMultiFasta(fasta.toString()));
    }

    @Test
    void isMultiFasta_multiple(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("multi.fna");
        Files.writeString(fasta, ">seq1\nATGC\n>seq2\nATGC\n>seq3\nATGC\n");
        assertEquals(3, SequenceUtils.isMultiFasta(fasta.toString()));
    }

    // -------------------------------------------------------------------
    // formatMultiFASTA
    // -------------------------------------------------------------------

    @Test
    void formatMultiFASTA_rewritesHeaders(@TempDir Path tmp) throws Exception {
        Path input = tmp.resolve("in.fna");
        Files.writeString(input, ">contig1 desc\nATGCATGC\n>contig2 desc\nNNNN\n");
        Path output = tmp.resolve("out.fna");

        String msg = SequenceUtils.formatMultiFASTA(
                input.toString(), output.toString(), 50, false);
        assertTrue(msg.contains("Multi-FASTA"));

        List<String> lines = Files.readAllLines(output);
        assertEquals(">contig1:0:8", lines.get(0));
        assertEquals(">contig2:58:62", lines.get(2));
    }

    @Test
    void formatMultiFASTA_withLabels(@TempDir Path tmp) throws Exception {
        Path input = tmp.resolve("in.fna");
        Files.writeString(input, ">c1 first contig\nATGC\n>c2 second\nATGC\n");
        Path output = tmp.resolve("out.fna");

        SequenceUtils.formatMultiFASTA(
                input.toString(), output.toString(), 10, true);

        List<String> lines = Files.readAllLines(output);
        assertTrue(lines.get(0).contains("first contig"),
                "Label mode should keep full header");
    }

    @Test
    void formatMultiFASTA_zeroSpacer(@TempDir Path tmp) throws Exception {
        Path input = tmp.resolve("in.fna");
        Files.writeString(input, ">c1\nATGC\n>c2\nATGC\n");
        Path output = tmp.resolve("out.fna");

        SequenceUtils.formatMultiFASTA(
                input.toString(), output.toString(), 0, false);

        List<String> lines = Files.readAllLines(output);
        assertEquals(">c1:0:4", lines.get(0));
        assertEquals(">c2:4:8", lines.get(2));
    }

    // -------------------------------------------------------------------
    // isProteinFASTA
    // -------------------------------------------------------------------

    @Test
    void isProteinFASTA_nucleotide(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("nuc.fna");
        Files.writeString(fasta, ">seq1\nATGCATGCNNNU\n");
        assertFalse(SequenceUtils.isProteinFASTA(fasta.toString()));
    }

    @Test
    void isProteinFASTA_protein(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("prot.faa");
        Files.writeString(fasta, ">prot1\nMVLSPADKTNVK\n");
        assertTrue(SequenceUtils.isProteinFASTA(fasta.toString()));
    }

    @Test
    void isProteinFASTA_ambiguousNucleotide(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("nuc.fna");
        Files.writeString(fasta, ">seq1\nATGCATGC\nATGCNNNN\nATGCATGC\nATGCATGC\nATGCATGC\nATGCATGC\n");
        assertFalse(SequenceUtils.isProteinFASTA(fasta.toString()));
    }

    // -------------------------------------------------------------------
    // WordWrap
    // -------------------------------------------------------------------

    @Test
    void wordWrap_shortString() {
        String result = SequenceUtils.WordWrap("ATGC");
        assertEquals("ATGC\n", result);
    }

    @Test
    void wordWrap_longString() {
        String seq = "A".repeat(250);
        String result = SequenceUtils.WordWrap(seq);
        String[] lines = result.split("\n");
        assertTrue(lines.length >= 3, "Should wrap into multiple lines");
        assertEquals(100, lines[0].length());
    }

    // -------------------------------------------------------------------
    // checkComment
    // -------------------------------------------------------------------

    @Test
    void checkComment_countsHashLines(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("test.graph");
        Files.writeString(file, "# header\n100\t200\t5.0\n# another\n300\t400\t3.0\n");
        assertEquals(2, SequenceUtils.checkComment(file.toString()));
    }

    @Test
    void checkComment_noComments(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("test.graph");
        Files.writeString(file, "100\t200\t5.0\n300\t400\t3.0\n");
        assertEquals(0, SequenceUtils.checkComment(file.toString()));
    }

    // -------------------------------------------------------------------
    // fastaSubsequence
    // -------------------------------------------------------------------

    @Test
    void fastaSubsequence_extractsRegion(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("ref.fna");
        // Create a FASTA with known sequence
        Files.writeString(fasta, ">ref\nATGCATGCAT\nGCNNNATGCA\nTGC\n");
        // Total: ATGCATGCATGCNNNATGCATGC (23 chars)

        String sub = SequenceUtils.fastaSubsequence(fasta.toString(), 5, 10);
        assertNotNull(sub);
        assertTrue(sub.length() > 0, "Should extract some bases");
    }

    // -------------------------------------------------------------------
    // isGenbank / isEmbl
    // -------------------------------------------------------------------

    @Test
    void isGenbank_withSettings() {
        // These methods depend on BRIG.PROFILE, so set up a minimal profile
        Document savedProfile = BRIG.PROFILE;
        try {
            Document doc = new Document(new org.jdom.Element("BRIG"));
            org.jdom.Element settings = new org.jdom.Element("brig_settings");
            settings.setAttribute("genbankFiles", "gbk,gb");
            doc.getRootElement().addContent(settings);
            BRIG.PROFILE = doc;

            assertTrue(SequenceUtils.isGenbank("file.gbk"));
            assertTrue(SequenceUtils.isGenbank("path/to/file.gb"));
            assertFalse(SequenceUtils.isGenbank("file.fna"));
        } finally {
            BRIG.PROFILE = savedProfile;
        }
    }

    @Test
    void isEmbl_withSettings() {
        Document savedProfile = BRIG.PROFILE;
        try {
            Document doc = new Document(new org.jdom.Element("BRIG"));
            org.jdom.Element settings = new org.jdom.Element("brig_settings");
            settings.setAttribute("emblFiles", "embl,dat");
            doc.getRootElement().addContent(settings);
            BRIG.PROFILE = doc;

            assertTrue(SequenceUtils.isEmbl("file.embl"));
            assertTrue(SequenceUtils.isEmbl("path/to/file.dat"));
            assertFalse(SequenceUtils.isEmbl("file.gbk"));
        } finally {
            BRIG.PROFILE = savedProfile;
        }
    }
}
