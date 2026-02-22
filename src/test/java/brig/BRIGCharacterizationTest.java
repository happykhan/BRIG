package brig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests that pin existing behaviour of BRIG utility methods.
 * These act as a regression safety net before any refactoring.
 */
class BRIGCharacterizationTest {

    // ---------------------------------------------------------------
    // FetchFilename
    // ---------------------------------------------------------------

    @Test
    void fetchFilename_extractsNameFromAbsolutePath() {
        assertEquals("genome.fasta",
                BRIG.FetchFilename("/home/user/data/genome.fasta"));
    }

    @Test
    void fetchFilename_returnsNameWhenNoDirectory() {
        assertEquals("genome.fasta",
                BRIG.FetchFilename("genome.fasta"));
    }

    @Test
    void fetchFilename_returnsEmptyForEmptyString() {
        assertEquals("", BRIG.FetchFilename(""));
    }

    // ---------------------------------------------------------------
    // FastaLength — single FASTA (multi=false)
    // ---------------------------------------------------------------

    @Test
    void fastaLength_singleFasta_countsBases(@TempDir Path tmp) throws Exception {
        // "ATGCATGCATGC" (12) + "NNNATGCATGC" (11) = 23
        Path fasta = tmp.resolve("single.fasta");
        Files.writeString(fasta,
                ">seq1 test sequence\nATGCATGCATGC\nNNNATGCATGC\n");

        int length = BRIG.FastaLength(fasta.toString(), false);
        assertEquals(23, length);
    }

    @Test
    void fastaLength_proteinFasta_countsTripled(@TempDir Path tmp) throws Exception {
        // "MVLSPADKTNVK" = 12 chars, each counted as 3 → 36
        Path fasta = tmp.resolve("protein.fasta");
        Files.writeString(fasta,
                ">prot1 test protein\nMVLSPADKTNVK\n");

        int length = BRIG.FastaLength(fasta.toString(), false);
        assertEquals(36, length);
    }

    @Test
    void fastaLength_multiFasta_readsLastHeaderStop(@TempDir Path tmp) throws Exception {
        // multi=true reads the 3rd colon-field from each header; returns the last one
        Path fasta = tmp.resolve("multi_formatted.fasta");
        Files.writeString(fasta,
                ">contig1:0:16\nATGCATGCATGCATGC\n"
              + ">contig2:116:123\nNNNATGC\n"
              + ">contig3:223:227\nATGC\n");

        int length = BRIG.FastaLength(fasta.toString(), true);
        assertEquals(227, length);
    }

    // ---------------------------------------------------------------
    // formatMultiFASTA
    // ---------------------------------------------------------------

    @Test
    void formatMultiFASTA_rewritesHeaders(@TempDir Path tmp) throws Exception {
        Path input = tmp.resolve("multi_in.fasta");
        Files.writeString(input,
                ">contig1 first contig\nATGCATGC\nATGCATGC\n"
              + ">contig2 second contig\nNNNATGC\n"
              + ">contig3 third contig\nATGC\n");
        Path output = tmp.resolve("multi_out.fasta");

        String msg = BRIG.formatMultiFASTA(
                input.toString(), output.toString(), 100, false);

        assertEquals("Multi-FASTA detected, Rewriting headers\n", msg);

        List<String> lines = Files.readAllLines(output);
        // Verify rewritten headers with start:stop coordinates
        assertEquals(">contig1:0:16",   lines.get(0));
        assertEquals("ATGCATGC",        lines.get(1));
        assertEquals("ATGCATGC",        lines.get(2));
        assertEquals(">contig2:116:123", lines.get(3));
        assertEquals("NNNATGC",         lines.get(4));
        assertEquals(">contig3:223:227", lines.get(5));
        assertEquals("ATGC",            lines.get(6));
    }

    @Test
    void formatMultiFASTA_withLabels_keepsFullHeader(@TempDir Path tmp) throws Exception {
        Path input = tmp.resolve("label_in.fasta");
        Files.writeString(input,
                ">contig1 first contig\nATGC\n"
              + ">contig2 second contig\nATGC\n");
        Path output = tmp.resolve("label_out.fasta");

        BRIG.formatMultiFASTA(input.toString(), output.toString(), 10, true);

        List<String> lines = Files.readAllLines(output);
        // label=true keeps the full header text (after colon removal)
        assertEquals(">contig1 first contig:0:4", lines.get(0));
        assertEquals(">contig2 second contig:14:18", lines.get(2));
    }

    // ---------------------------------------------------------------
    // FormatGenbank — nucleotide path (pOption="F")
    // ---------------------------------------------------------------

    @Test
    void formatGenbank_extractsNucleotideSequence(@TempDir Path tmp) throws Exception {
        Path gbk = tmp.resolve("test.gbk");
        Files.writeString(gbk,
                "LOCUS       TEST_SEQ                 23 bp\n"
              + "ORIGIN\n"
              + "        1 atgcatgcat gcnnnatgca tgc\n"
              + "//\n");

        String outBase = tmp.resolve("out").toString();
        String msg = BRIG.FormatGenbank(
                gbk.toString(), "test_header", outBase, false, "F");

        assertEquals("Formatting nucleotide file...\n", msg);

        List<String> lines = Files.readAllLines(Path.of(outBase + ".fna"));
        assertEquals(">test_header", lines.get(0));
        // Regex strips all non-alpha chars (digits, spaces)
        assertEquals("atgcatgcatgcnnnatgcatgc", lines.get(1));
        // "//" becomes empty string after stripping non-alpha
        assertEquals("", lines.get(2));
    }

    @Test
    void formatGenbank_embl_extractsAfterSQLine(@TempDir Path tmp) throws Exception {
        Path embl = tmp.resolve("test.embl");
        Files.writeString(embl,
                "ID   TEST_SEQ\n"
              + "SQ   Sequence 23 BP;\n"
              + "     atgcatgcat gcnnnatgca tgc                23\n"
              + "//\n");

        String outBase = tmp.resolve("out").toString();
        String msg = BRIG.FormatGenbank(
                embl.toString(), "embl_header", outBase, true, "F");

        assertEquals("Formatting nucleotide file...\n", msg);

        List<String> lines = Files.readAllLines(Path.of(outBase + ".fna"));
        assertEquals(">embl_header", lines.get(0));
        // Strips digits and spaces from lines after the SQ trigger
        assertEquals("atgcatgcatgcnnnatgcatgc", lines.get(1));
    }

    // ---------------------------------------------------------------
    // copyTo
    // ---------------------------------------------------------------

    @Test
    void copyTo_textMode_copiesFaithfully(@TempDir Path tmp) throws Exception {
        Path src = tmp.resolve("source.txt");
        Files.writeString(src, "Hello, BRIG!\nLine two.\n");
        File dest = tmp.resolve("dest.txt").toFile();

        BRIG.copyTo(src.toFile(), dest, false);

        assertEquals(
                Files.readString(src),
                Files.readString(dest.toPath()));
    }

    @Test
    void copyTo_binaryMode_copiesFaithfully(@TempDir Path tmp) throws Exception {
        Path src = tmp.resolve("source.bin");
        byte[] data = new byte[]{0, 1, 2, (byte) 0xFF, 127, (byte) 0x80};
        Files.write(src, data);
        File dest = tmp.resolve("dest.bin").toFile();

        BRIG.copyTo(src.toFile(), dest, true);

        assertArrayEquals(data, Files.readAllBytes(dest.toPath()));
    }

    // ---------------------------------------------------------------
    // isProteinFASTA
    // ---------------------------------------------------------------

    @Test
    void isProteinFASTA_nucleotide_returnsFalse(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("nuc.fasta");
        Files.writeString(fasta, ">seq1\nATGCATGCNNNU\n");

        assertFalse(BRIG.isProteinFASTA(fasta.toString()));
    }

    @Test
    void isProteinFASTA_protein_returnsTrue(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("prot.fasta");
        Files.writeString(fasta, ">prot1\nMVLSPADKTNVK\n");

        assertTrue(BRIG.isProteinFASTA(fasta.toString()));
    }

    // ---------------------------------------------------------------
    // isMultiFasta
    // ---------------------------------------------------------------

    @Test
    void isMultiFasta_countsHeaders(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("multi.fasta");
        Files.writeString(fasta,
                ">seq1\nATGC\n>seq2\nATGC\n>seq3\nATGC\n");

        assertEquals(3, BRIG.isMultiFasta(fasta.toString()));
    }

    @Test
    void isMultiFasta_singleSequence(@TempDir Path tmp) throws Exception {
        Path fasta = tmp.resolve("single.fasta");
        Files.writeString(fasta, ">seq1\nATGC\n");

        assertEquals(1, BRIG.isMultiFasta(fasta.toString()));
    }
}
