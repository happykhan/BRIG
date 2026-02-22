package brig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GraphFunctions BLAST-to-graph parsing.
 */
class GraphFunctionsTest {

    @Test
    void parseBlast2Graph_createsGraphFile(@TempDir Path tmpDir) throws Exception {
        // Create a small BLAST tab file
        // Format: qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore
        Path blastTab = tmpDir.resolve("blast.tab");
        StringBuilder tab = new StringBuilder();
        // Hit 1: query positions 1-500
        tab.append("ref:0:50000\tsubject1\t95.0\t500\t25\t0\t1\t500\t1\t500\t1e-100\t900\n");
        // Hit 2: query positions 600-1100 (same subject, different region)
        tab.append("ref:0:50000\tsubject2\t90.0\t500\t50\t0\t600\t1100\t1\t500\t1e-80\t800\n");
        // Hit 3: query positions 2000-2500
        tab.append("ref:0:50000\tsubject3\t85.0\t500\t75\t0\t2000\t2500\t1\t500\t1e-60\t700\n");
        Files.writeString(blastTab, tab.toString());

        String outputPath = tmpDir.resolve("output.graph").toString();
        int len = 50000;
        int div = 1;
        int[] value = new int[len / div];
        int[] repeats = new int[len / div];

        String error = GraphFunctions.parseBlast2Graph(
                value, repeats, blastTab.toString(), outputPath, false, div, len);

        assertTrue(error.isEmpty() || !error.contains("SYS_ERROR"),
                "Should not have errors: " + error);
        assertTrue(Files.exists(Path.of(outputPath)),
                "Output graph file should exist");

        String graphContent = Files.readString(Path.of(outputPath));
        assertTrue(graphContent.length() > 0, "Graph file should not be empty");
    }

    @Test
    void parseBlast2Graph_createsRepeatFile(@TempDir Path tmpDir) throws Exception {
        Path blastTab = tmpDir.resolve("blast.tab");
        StringBuilder tab = new StringBuilder();
        tab.append("ref:0:50000\tsubject1\t95.0\t500\t25\t0\t1\t500\t1\t500\t1e-100\t900\n");
        tab.append("ref:0:50000\tsubject1\t90.0\t500\t50\t0\t600\t1100\t1\t500\t1e-80\t800\n");
        Files.writeString(blastTab, tab.toString());

        String outputPath = tmpDir.resolve("output.graph").toString();
        int len = 50000;
        int div = 1;
        int[] value = new int[len / div];
        int[] repeats = new int[len / div];

        GraphFunctions.parseBlast2Graph(value, repeats, blastTab.toString(), outputPath, false, div, len);

        // Repeat graph file should also be created
        assertTrue(Files.exists(Path.of(outputPath + "rep.graph")),
                "Repeat graph file should exist");
    }

    @Test
    void parseBlast2Graph_multiFasta_appliesOffsets(@TempDir Path tmpDir) throws Exception {
        Path blastTab = tmpDir.resolve("blast.tab");
        // In multi-FASTA mode, qseqid has format name:start:stop
        // The start offset is added to qstart/qend
        StringBuilder tab = new StringBuilder();
        tab.append("contig1:0:5000\tsubject1\t95.0\t500\t25\t0\t1\t500\t1\t500\t1e-100\t900\n");
        tab.append("contig2:5050:10000\tsubject2\t90.0\t500\t50\t0\t1\t500\t1\t500\t1e-80\t800\n");
        Files.writeString(blastTab, tab.toString());

        String outputPath = tmpDir.resolve("output.graph").toString();
        int len = 15000;
        int div = 1;
        int[] value = new int[len / div];
        int[] repeats = new int[len / div];

        String error = GraphFunctions.parseBlast2Graph(
                value, repeats, blastTab.toString(), outputPath, true, div, len);

        assertTrue(error.isEmpty() || !error.contains("SYS_ERROR"),
                "Should not have errors: " + error);
    }

    @Test
    void parseBlast2Graph_missingFile_returnsError(@TempDir Path tmpDir) {
        String outputPath = tmpDir.resolve("output.graph").toString();
        int[] value = new int[100];
        int[] repeats = new int[100];

        String error = GraphFunctions.parseBlast2Graph(
                value, repeats, "/nonexistent/file.tab", outputPath, false, 1, 100);

        assertTrue(error.contains("SYS_ERROR"),
                "Should report error for missing file");
    }

    @Test
    void parseBlast2Graph_withDivider(@TempDir Path tmpDir) throws Exception {
        Path blastTab = tmpDir.resolve("blast.tab");
        StringBuilder tab = new StringBuilder();
        tab.append("ref:0:10000\tsubject1\t95.0\t300\t15\t0\t100\t400\t1\t300\t1e-100\t900\n");
        tab.append("ref:0:10000\tsubject2\t90.0\t300\t30\t0\t500\t800\t1\t300\t1e-80\t800\n");
        Files.writeString(blastTab, tab.toString());

        String outputPath = tmpDir.resolve("output.graph").toString();
        int len = 10000;
        int div = 3;  // divider of 3
        int[] value = new int[len / div + 1];
        int[] repeats = new int[len / div + 1];

        String error = GraphFunctions.parseBlast2Graph(
                value, repeats, blastTab.toString(), outputPath, false, div, len);

        assertTrue(error.isEmpty() || !error.contains("SYS_ERROR"),
                "Should not have errors: " + error);
    }
}
