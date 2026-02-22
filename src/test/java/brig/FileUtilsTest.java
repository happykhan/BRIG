package brig;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FileUtils XML I/O methods.
 */
class FileUtilsTest {

    @Test
    void saveXML_writesValidXML(@TempDir Path tmpDir) throws Exception {
        Document doc = new Document(new Element("root"));
        doc.getRootElement().setAttribute("name", "test");
        Element child = new Element("child");
        child.setAttribute("value", "hello");
        doc.getRootElement().addContent(child);

        String path = tmpDir.resolve("output.xml").toString();
        int result = FileUtils.saveXML(path, doc);

        assertEquals(0, result);
        assertTrue(Files.exists(Path.of(path)));

        // Verify it's parseable
        SAXBuilder builder = new SAXBuilder();
        Document parsed = builder.build(path);
        assertEquals("root", parsed.getRootElement().getName());
        assertEquals("test", parsed.getRootElement().getAttributeValue("name"));
        assertEquals("hello",
                parsed.getRootElement().getChild("child").getAttributeValue("value"));
    }

    @Test
    void saveXML_returnsOneOnFailure() {
        Document doc = new Document(new Element("root"));
        int result = FileUtils.saveXML("/nonexistent/path/file.xml", doc);
        assertEquals(1, result);
    }

    @Test
    void dumpXML_appendsWithTimestamp(@TempDir Path tmpDir) throws Exception {
        Document doc1 = new Document(new Element("first"));
        Document doc2 = new Document(new Element("second"));

        String path = tmpDir.resolve("dump.xml").toString();

        int r1 = FileUtils.dumpXML(path, doc1);
        assertEquals(0, r1);

        int r2 = FileUtils.dumpXML(path, doc2);
        assertEquals(0, r2);

        String content = Files.readString(Path.of(path));
        // Should contain both documents
        assertTrue(content.contains("<first"), "Should contain first document");
        assertTrue(content.contains("<second"), "Should contain second document");

        // Should contain timestamps (yyyy/MM/dd format)
        assertTrue(content.contains("/"),
                "Should contain timestamp separator");
    }

    @Test
    void copyTo_copiesFile(@TempDir Path tmpDir) throws Exception {
        Path src = tmpDir.resolve("source.txt");
        Files.writeString(src, "test content\nline two\n");
        File dest = tmpDir.resolve("dest.txt").toFile();

        FileUtils.copyTo(src.toFile(), dest, false);

        assertTrue(dest.exists());
        assertEquals(Files.readString(src), Files.readString(dest.toPath()));
    }

    @Test
    void copyTo_binaryMode(@TempDir Path tmpDir) throws Exception {
        Path src = tmpDir.resolve("source.bin");
        byte[] data = {0, 1, 2, 127, (byte) 0xFF, (byte) 0x80};
        Files.write(src, data);
        File dest = tmpDir.resolve("dest.bin").toFile();

        FileUtils.copyTo(src.toFile(), dest, true);

        assertArrayEquals(data, Files.readAllBytes(dest.toPath()));
    }
}
