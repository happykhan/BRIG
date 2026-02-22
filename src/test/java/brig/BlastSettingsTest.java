package brig;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BlastSettings: BLAST option validation, profile preparation, session validation.
 */
class BlastSettingsTest {

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
    // BlastOption
    // -------------------------------------------------------------------

    @Test
    void blastOption_validOptions_returnsNull() {
        setupBlastPlusProfile();
        // -evalue is not in the blocked list for BLAST+
        assertNull(BlastSettings.BlastOption("-evalue 1e-5"));
    }

    @Test
    void blastOption_blockedOption_returnsError() {
        setupBlastPlusProfile();
        String result = BlastSettings.BlastOption("-db mydb");
        assertNotNull(result, "Should reject -db option");
        assertTrue(result.contains("-db"));
    }

    @Test
    void blastOption_multipleBlockedOptions_returnsError() {
        setupBlastPlusProfile();
        String result = BlastSettings.BlastOption("-db mydb -query input.fna");
        assertNotNull(result);
        assertTrue(result.contains("are not valid parameters"));
    }

    @Test
    void blastOption_emptyString_returnsNull() {
        setupBlastPlusProfile();
        assertNull(BlastSettings.BlastOption(""));
    }

    @Test
    void blastOption_helpBlocked() {
        setupBlastPlusProfile();
        String result = BlastSettings.BlastOption("-help");
        assertNotNull(result, "Should reject -help option");
    }

    // -------------------------------------------------------------------
    // ValidateSession
    // -------------------------------------------------------------------

    @Test
    void validateSession_validPaths_returnsNull(@TempDir Path tmpDir) throws Exception {
        Path queryFile = tmpDir.resolve("test.fna");
        Files.writeString(queryFile, ">test\nATGC\n");

        Document profile = createMinimalProfile();
        profile.getRootElement().setAttribute("queryFile", queryFile.toString());
        profile.getRootElement().setAttribute("outputFolder", tmpDir.toString());
        BRIG.PROFILE = profile;

        assertNull(BlastSettings.ValidateSession());
    }

    @Test
    void validateSession_missingQueryFile_returnsError(@TempDir Path tmpDir) {
        Document profile = createMinimalProfile();
        profile.getRootElement().setAttribute("queryFile", tmpDir.resolve("nonexistent.fna").toString());
        profile.getRootElement().setAttribute("outputFolder", tmpDir.toString());
        BRIG.PROFILE = profile;

        String result = BlastSettings.ValidateSession();
        assertNotNull(result);
        assertTrue(result.contains("does not exist"));
    }

    @Test
    void validateSession_spaceInQueryFile_returnsError(@TempDir Path tmpDir) throws Exception {
        Path spacedDir = tmpDir.resolve("my dir");
        Files.createDirectories(spacedDir);
        Path queryFile = spacedDir.resolve("test.fna");
        Files.writeString(queryFile, ">test\nATGC\n");

        Document profile = createMinimalProfile();
        profile.getRootElement().setAttribute("queryFile", queryFile.toString());
        profile.getRootElement().setAttribute("outputFolder", tmpDir.toString());
        BRIG.PROFILE = profile;

        String result = BlastSettings.ValidateSession();
        assertNotNull(result);
        assertTrue(result.contains("space"));
    }

    // -------------------------------------------------------------------
    // prepProfile
    // -------------------------------------------------------------------

    @Test
    void prepProfile_preservesSettings() {
        Document profile = createMinimalProfile();
        Element root = profile.getRootElement();
        root.setAttribute("blastOptions", "-evalue 1e-5");
        root.setAttribute("legendPosition", "lower-left");

        Element special = new Element("special");
        special.setAttribute("value", "GC Content");
        root.addContent(special);

        Document result = BlastSettings.prepProfile(profile);

        assertNotNull(result.getRootElement().getChild("cgview_settings"));
        assertNotNull(result.getRootElement().getChild("brig_settings"));
        assertEquals("-evalue 1e-5", result.getRootElement().getAttributeValue("blastOptions"));
        assertEquals("lower-left", result.getRootElement().getAttributeValue("legendPosition"));

        @SuppressWarnings("unchecked")
        List<Element> specials = result.getRootElement().getChildren("special");
        assertEquals(1, specials.size());
    }

    @Test
    void prepProfile_removesArchiveAttribute() {
        Document profile = createMinimalProfile();
        profile.getRootElement().setAttribute("archive", "/some/path");

        BlastSettings.prepProfile(profile);

        assertNull(profile.getRootElement().getAttribute("archive"));
    }

    // -------------------------------------------------------------------
    // FetchColor
    // -------------------------------------------------------------------

    @Test
    void fetchColor_returnsConfiguredColor() {
        Document profile = createMinimalProfile();
        BRIG.PROFILE = profile;

        java.awt.Color color = BlastSettings.FetchColor(1);
        assertNotNull(color);
        // Ring1 in our profile is "102,0,102"
        assertEquals(102, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(102, color.getBlue());
    }

    @Test
    void fetchColor_wrapsAt10() {
        Document profile = createMinimalProfile();
        BRIG.PROFILE = profile;

        // Ring 11 should wrap to Ring1
        java.awt.Color color1 = BlastSettings.FetchColor(1);
        java.awt.Color color11 = BlastSettings.FetchColor(11);
        assertEquals(color1, color11);
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private void setupBlastPlusProfile() {
        Document profile = createMinimalProfile();
        BRIG.PROFILE = profile;
    }

    private Document createMinimalProfile() {
        Document doc = new Document(new Element("BRIG"));
        Element root = doc.getRootElement();

        Element cgSettings = new Element("cgview_settings");
        cgSettings.setAttribute("warningFont", "Default,plain,30");
        cgSettings.setAttribute("backgroundColor", "white");
        root.addContent(cgSettings);

        Element brigSettings = new Element("brig_settings");
        brigSettings.setAttribute("Ring1", "102,0,102");
        brigSettings.setAttribute("Ring2", "0,102,102");
        brigSettings.setAttribute("Ring3", "0,102,0");
        brigSettings.setAttribute("Ring4", "0,0,153");
        brigSettings.setAttribute("Ring5", "102,102,0");
        brigSettings.setAttribute("Ring6", "0,153,0");
        brigSettings.setAttribute("Ring7", "204,51,0");
        brigSettings.setAttribute("Ring8", "0,102,102");
        brigSettings.setAttribute("Ring9", "0,153,102");
        brigSettings.setAttribute("Ring10", "204,0,51");
        brigSettings.setAttribute("defaultUpper", "70");
        brigSettings.setAttribute("defaultLower", "50");
        brigSettings.setAttribute("defaultMinimum", "50");
        brigSettings.setAttribute("genbankFiles", "gbk");
        brigSettings.setAttribute("fastaFiles", "fna,faa,fas");
        brigSettings.setAttribute("emblFiles", "embl");
        brigSettings.setAttribute("blastLocation", "");
        brigSettings.setAttribute("divider", "3");
        brigSettings.setAttribute("multiplier", "3");
        brigSettings.setAttribute("memory", "1500");
        brigSettings.setAttribute("defaultSpacer", "0");
        root.addContent(brigSettings);

        return doc;
    }
}
