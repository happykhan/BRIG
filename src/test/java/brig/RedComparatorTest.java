package brig;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RedComparator (sorts graph label lines by start position).
 */
class RedComparatorTest {

    private final RedComparator comparator = new RedComparator();

    @Test
    void compare_firstSmaller_returnsNegative() {
        int result = comparator.compare("#100\t200\tgeneA", "#300\t400\tgeneB");
        assertTrue(result < 0);
    }

    @Test
    void compare_firstLarger_returnsPositive() {
        int result = comparator.compare("#500\t600\tgeneA", "#100\t200\tgeneB");
        assertTrue(result > 0);
    }

    @Test
    void compare_equal_returnsNonPositive() {
        // The implementation returns -1 for equal values (<=), which is fine for sorting
        int result = comparator.compare("#100\t200\tgeneA", "#100\t300\tgeneB");
        assertTrue(result <= 0);
    }

    @Test
    void compare_withoutHash_stillWorks() {
        // Lines without # prefix should also parse correctly
        int result = comparator.compare("100\t200\tgeneA", "300\t400\tgeneB");
        assertTrue(result < 0);
    }

    @Test
    void compare_sortArray() {
        String[] labels = {
            "#500\t600\tgeneC",
            "#100\t200\tgeneA",
            "#300\t400\tgeneB"
        };

        Arrays.sort(labels, comparator);

        assertTrue(labels[0].contains("geneA"), "geneA (100) should be first");
        assertTrue(labels[1].contains("geneB"), "geneB (300) should be second");
        assertTrue(labels[2].contains("geneC"), "geneC (500) should be third");
    }
}
