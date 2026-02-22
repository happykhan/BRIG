package brig;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AnnoXML GenBank/EMBL feature extraction.
 */
class AnnoXMLTest {

    @Test
    void createFeatureXML_gbk_extractsCDSFeatures() throws Exception {
        String gbkPath = getResourcePath("test-features.gbk");

        Document features = AnnoXML.CreateFeatureXML(gbkPath, false);

        assertNotNull(features);
        Element root = features.getRootElement();
        assertEquals("AnnoXML", root.getName());

        @SuppressWarnings("unchecked")
        List<Element> cdsList = root.getChildren("CDS");
        assertTrue(cdsList.size() >= 3,
                "Should extract at least 3 CDS features, got: " + cdsList.size());

        // Check first CDS has start/stop attributes
        Element firstCDS = cdsList.get(0);
        assertNotNull(firstCDS.getAttributeValue("start"));
        assertNotNull(firstCDS.getAttributeValue("stop"));
        assertEquals("10", firstCDS.getAttributeValue("start"));
        assertEquals("100", firstCDS.getAttributeValue("stop"));
    }

    @Test
    void createFeatureXML_gbk_detectsComplement() throws Exception {
        String gbkPath = getResourcePath("test-features.gbk");

        Document features = AnnoXML.CreateFeatureXML(gbkPath, false);

        @SuppressWarnings("unchecked")
        List<Element> cdsList = features.getRootElement().getChildren("CDS");

        // Second CDS (geneB) is complement
        Element complementCDS = cdsList.get(1);
        assertEquals("true", complementCDS.getAttributeValue("complement"));
        assertEquals("150", complementCDS.getAttributeValue("start"));
        assertEquals("300", complementCDS.getAttributeValue("stop"));
    }

    @Test
    void createFeatureXML_gbk_extractsNoteQualifiers() throws Exception {
        String gbkPath = getResourcePath("test-features.gbk");

        Document features = AnnoXML.CreateFeatureXML(gbkPath, false);

        @SuppressWarnings("unchecked")
        List<Element> cdsList = features.getRootElement().getChildren("CDS");

        // Find geneB which has /gene="geneB"
        Element geneB = cdsList.get(1);
        Element geneChild = geneB.getChild("gene");
        assertNotNull(geneChild, "CDS should have gene child element");
        assertEquals("geneB", geneChild.getAttributeValue("value"));
    }

    @Test
    void createFeatureXML_embl_extractsFeatures() throws Exception {
        String emblPath = getResourcePath("test-features.embl");

        Document features = AnnoXML.CreateFeatureXML(emblPath, true);

        assertNotNull(features);
        Element root = features.getRootElement();

        @SuppressWarnings("unchecked")
        List<Element> cdsList = root.getChildren("CDS");
        assertTrue(cdsList.size() >= 2,
                "Should extract at least 2 CDS features from EMBL, got: " + cdsList.size());

        // Check first EMBL CDS
        Element firstCDS = cdsList.get(0);
        assertEquals("20", firstCDS.getAttributeValue("start"));
        assertEquals("200", firstCDS.getAttributeValue("stop"));
    }

    @Test
    void createFeatureXML_gbk_extractsSourceFeature() throws Exception {
        String gbkPath = getResourcePath("test-features.gbk");

        Document features = AnnoXML.CreateFeatureXML(gbkPath, false);

        @SuppressWarnings("unchecked")
        List<Element> sourceList = features.getRootElement().getChildren("source");
        assertTrue(sourceList.size() >= 1, "Should have at least one source feature");
    }

    @Test
    void dumpXMLfile_writesToFile(@TempDir Path tmpDir) throws Exception {
        Document doc = new Document(new Element("test"));
        doc.getRootElement().setAttribute("value", "hello");

        String outPath = tmpDir.resolve("output.xml").toString();
        int result = AnnoXML.dumpXMLfile(doc, outPath);

        assertEquals(0, result);
        assertTrue(Files.exists(Path.of(outPath)));
        String content = Files.readString(Path.of(outPath));
        assertTrue(content.contains("hello"));
    }

    private String getResourcePath(String name) {
        var url = getClass().getClassLoader().getResource(name);
        assertNotNull(url, "Test resource not found: " + name);
        return url.getPath();
    }
}
