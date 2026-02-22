package brig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SamCoverage SAM-to-graph conversion.
 */
class SamCoverageTest {

    @Test
    void samcontigCoverage_producesOutput(@TempDir Path tmpDir) throws Exception {
        // Create a minimal SAM file
        Path samFile = tmpDir.resolve("test.sam");
        StringBuilder sam = new StringBuilder();
        // Header: reference with length 5000
        sam.append("@SQ\tSN:ref1\tLN:5000\n");
        // Add some alignment lines
        // Fields: QNAME FLAG RNAME POS MAPQ CIGAR RNEXT PNEXT TLEN SEQ QUAL
        for (int i = 0; i < 100; i++) {
            int pos = (i * 40) + 1; // spread across reference
            sam.append("read").append(i).append("\t0\tref1\t").append(pos)
               .append("\t30\t50M\t*\t0\t0\t");
            sam.append("A".repeat(50)); // 50bp read
            sam.append("\t*\n");
        }
        Files.writeString(samFile, sam.toString());

        // Capture stdout
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            SamCoverage.SamcontigCoverage(samFile.toString(), "", "");
        } finally {
            System.setOut(oldOut);
        }

        String output = baos.toString();
        assertTrue(output.length() > 0, "Should produce output");

        // Should contain the reference length header
        assertTrue(output.contains("# 5000"), "Should contain reference length");

        // Should have tab-delimited coverage lines
        String[] lines = output.split("\n");
        assertTrue(lines.length > 1, "Should have multiple output lines");

        // Check format of coverage lines (start\tstop\tvalue)
        boolean foundCoverage = false;
        for (String line : lines) {
            if (!line.startsWith("#") && line.contains("\t")) {
                String[] parts = line.split("\t");
                assertEquals(3, parts.length,
                        "Coverage line should have 3 tab-separated fields: " + line);
                int start = Integer.parseInt(parts[0]);
                int stop = Integer.parseInt(parts[1]);
                double value = Double.parseDouble(parts[2]);
                assertTrue(start < stop, "Start should be < stop");
                assertTrue(value >= 0, "Coverage value should be >= 0");
                foundCoverage = true;
            }
        }
        assertTrue(foundCoverage, "Should have at least one coverage line");
    }

    @Test
    void samcontigCoverage_emptyFile_noOutput(@TempDir Path tmpDir) throws Exception {
        // SAM file with only header, no alignments
        Path samFile = tmpDir.resolve("empty.sam");
        Files.writeString(samFile, "@SQ\tSN:ref1\tLN:1000\n");

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            SamCoverage.SamcontigCoverage(samFile.toString(), "", "");
        } finally {
            System.setOut(oldOut);
        }

        String output = baos.toString();
        // Should at least have the header line
        assertTrue(output.contains("# 1000"));
    }

    @Test
    void samcontigCoverage_singleRead(@TempDir Path tmpDir) throws Exception {
        Path samFile = tmpDir.resolve("single.sam");
        StringBuilder sam = new StringBuilder();
        sam.append("@SQ\tSN:ref1\tLN:3000\n");
        // Single read at position 100, 50bp long
        sam.append("read1\t0\tref1\t100\t30\t50M\t*\t0\t0\t");
        sam.append("A".repeat(50));
        sam.append("\t*\n");
        Files.writeString(samFile, sam.toString());

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            SamCoverage.SamcontigCoverage(samFile.toString(), "", "");
        } finally {
            System.setOut(oldOut);
        }

        String output = baos.toString();
        assertTrue(output.contains("# 3000"));
        // With 3000bp ref and 1000bp windows, the read at pos 100
        // should contribute to the first window
    }
}
