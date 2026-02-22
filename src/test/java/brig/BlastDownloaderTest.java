package brig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BlastDownloader utility methods (non-network).
 */
class BlastDownloaderTest {

    @Test
    void getArchiveSuffix_returnsNonNullOnSupportedPlatform() {
        String suffix = BlastDownloader.getArchiveSuffix();
        // On any CI/dev machine (macOS/Linux/Windows), should return a suffix
        assertNotNull(suffix, "Should return a suffix on a supported platform");
        assertTrue(suffix.endsWith(".tar.gz"), "Suffix should end with .tar.gz");
    }

    @Test
    void getArchiveSuffix_containsOsPart() {
        String suffix = BlastDownloader.getArchiveSuffix();
        assertNotNull(suffix);

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) {
            assertTrue(suffix.contains("macosx"), "macOS suffix should contain 'macosx'");
        } else if (os.contains("linux")) {
            assertTrue(suffix.contains("linux"), "Linux suffix should contain 'linux'");
        } else if (os.contains("win")) {
            assertTrue(suffix.contains("win64"), "Windows suffix should contain 'win64'");
        }
    }

    @Test
    void getArchiveSuffix_containsArchPart() {
        String suffix = BlastDownloader.getArchiveSuffix();
        assertNotNull(suffix);

        String arch = System.getProperty("os.arch", "").toLowerCase();
        if (arch.equals("aarch64") || arch.equals("arm64")) {
            assertTrue(suffix.contains("aarch64"), "ARM suffix should contain 'aarch64'");
        } else if (arch.equals("amd64") || arch.equals("x86_64")) {
            assertTrue(suffix.contains("x64"), "x86_64 suffix should contain 'x64'");
        }
    }

    @Test
    void getArchiveSuffix_formatIsArchDashOsDotTarGz() {
        String suffix = BlastDownloader.getArchiveSuffix();
        assertNotNull(suffix);
        // Format: <arch>-<os>.tar.gz
        assertTrue(suffix.matches("(aarch64|x64)-(macosx|linux|win64)\\.tar\\.gz"),
                "Suffix should match expected format, got: " + suffix);
    }
}
