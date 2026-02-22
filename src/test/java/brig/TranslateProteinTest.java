package brig;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for translateProtein codon translation.
 */
class TranslateProteinTest {

    @Test
    void loadFromClasspathResource() throws IOException {
        // The no-arg constructor loads from classpath resource
        translateProtein tp = new translateProtein();
        assertNotNull(tp);
        // Should have loaded 64 codons
        assertEquals(64, tp.proteins.size());
    }

    @Test
    void translateSeq_knownCodons() throws IOException {
        translateProtein tp = new translateProtein();
        // ATG = M (Met), GCT = A (Ala), TAA = * (STOP)
        assertEquals("MA*", tp.translateSeq("ATGGCTTAA"));
    }

    @Test
    void translateSeq_startCodon() throws IOException {
        translateProtein tp = new translateProtein();
        assertEquals("M", tp.translateSeq("ATG"));
    }

    @Test
    void translateSeq_truncatesPartialCodon() throws IOException {
        translateProtein tp = new translateProtein();
        // "ATGCC" has 1 full codon (ATG) + 2 leftover bases
        assertEquals("M", tp.translateSeq("ATGCC"));
    }

    @Test
    void translateSeq_emptyInput() throws IOException {
        translateProtein tp = new translateProtein();
        assertEquals("", tp.translateSeq(""));
    }

    @Test
    void translateSeq_lowercaseInput() throws IOException {
        translateProtein tp = new translateProtein();
        assertEquals("M", tp.translateSeq("atg"));
    }

    @Test
    void translateSeq_fullProtein() throws IOException {
        translateProtein tp = new translateProtein();
        // Met-Val-Leu-Ser = ATG GTT CTT TCT
        String result = tp.translateSeq("ATGGTCCTTTCT");
        assertEquals("MVLS", result);
    }

    @Test
    void constructor_fromInputStream() throws IOException {
        String data = "ATG\tM\tMet\nTAA\t*\tSTOP\n";
        var stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        translateProtein tp = new translateProtein(stream);
        assertEquals(2, tp.proteins.size());
        assertEquals("M", tp.translateSeq("ATG"));
    }

    @Test
    void constructor_nullStream_throws() {
        assertThrows(IOException.class, () -> new translateProtein(null));
    }
}
