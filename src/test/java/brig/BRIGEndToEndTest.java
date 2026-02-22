package brig;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test that runs the full BRIG pipeline:
 * profile setup → BLAST → parse → CGView render.
 *
 * Uses embedded test resources from src/test/resources/examples/ecoli/.
 * Requires BLAST+ installed and on PATH.
 * Run with: mvn test -Dtest.excludedGroups= -Dtest=brig.BRIGEndToEndTest
 */
@Tag("e2e")
class BRIGEndToEndTest {

    private static final String REFERENCE = "BRIGExample-50k.fna";

    private static final String[] RING_FILES = {
            "E_coli_CFT073-50k.fna",
            "E_coli_HS-50k.fna",
            "E_coli_K12MG1655-50k.fna",
            "E_coli_O157H7Sakai-50k.gbk",
            "E_coli_UTI89-50k.fna",
            "Ecoli_O126-50k.fna",
            "EHEC_vir.fna",
    };

    private static final String[] COLORS = {
            "102,0,102", "0,102,102", "0,102,0", "0,0,153",
            "102,102,0", "0,153,0", "204,51,0"
    };

    @Test
    void fullPipeline_producesImage(@TempDir Path tmpDir) throws Exception {
        Path examplesPath = copyResourceDir(tmpDir);

        Document profile = loadProfile(examplesPath);
        Element root = profile.getRootElement();

        String refFile = examplesPath.resolve(REFERENCE).toString();
        String outputDir = tmpDir.toString();
        String outputFile = tmpDir.resolve(REFERENCE).toString();

        configureProfile(root, refFile, outputDir, outputFile);

        // Add all rings
        for (int i = 0; i < RING_FILES.length; i++) {
            String ringPath = examplesPath.resolve(RING_FILES[i]).toString();
            assertTrue(Files.exists(Path.of(ringPath)),
                    "Ring file not found: " + ringPath);
            addRing(root, i, RING_FILES[i], ringPath, COLORS[i % COLORS.length]);
        }

        runPipeline(profile, outputDir, refFile, outputFile, "jpg");
    }

    @Test
    void fullPipeline_handlesSpacesInPaths(@TempDir Path tmpDir) throws Exception {
        Path examplesPath = copyResourceDir(tmpDir);

        // Create directories with spaces in the name
        Path spacedDir = tmpDir.resolve("my data").resolve("genome files");
        Files.createDirectories(spacedDir);

        Path spacedOutput = tmpDir.resolve("output results");
        Files.createDirectories(spacedOutput);

        // Copy reference and one small ring file into the spaced directory
        Path refSrc = examplesPath.resolve(REFERENCE);
        Path refDest = spacedDir.resolve(REFERENCE);
        Files.copy(refSrc, refDest, StandardCopyOption.REPLACE_EXISTING);

        String ringFileName = "EHEC_vir.fna";
        Path ringSrc = examplesPath.resolve(ringFileName);
        Path ringDest = spacedDir.resolve(ringFileName);
        Files.copy(ringSrc, ringDest, StandardCopyOption.REPLACE_EXISTING);

        Document profile = loadProfile(examplesPath);
        Element root = profile.getRootElement();

        String refFile = refDest.toString();
        String outputDir = spacedOutput.toString();
        String outputFile = spacedOutput.resolve(REFERENCE).toString();

        configureProfile(root, refFile, outputDir, outputFile);
        addRing(root, 0, ringFileName, ringDest.toString(), COLORS[0]);

        runPipeline(profile, outputDir, refFile, outputFile, "jpg");
    }

    private Path copyResourceDir(Path tmpDir) throws Exception {
        Path target = tmpDir.resolve("ecoli");
        Files.createDirectories(target);

        String indexPath = "examples/ecoli/files-index.txt";
        try (var is = getClass().getClassLoader().getResourceAsStream(indexPath);
             var reader = new BufferedReader(new InputStreamReader(is))) {
            String filename;
            while ((filename = reader.readLine()) != null) {
                filename = filename.trim();
                if (filename.isEmpty()) continue;
                String resourcePath = "examples/ecoli/" + filename;
                try (var resStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    assertNotNull(resStream, "Resource not found: " + resourcePath);
                    Files.copy(resStream, target.resolve(filename),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return target;
    }

    private Document loadProfile(Path examplesPath) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document profile = builder.build(examplesPath.resolve("ExampleProfile.xml").toString());

        boolean hasContent = false, hasSkew = false;
        for (Object o : profile.getRootElement().getChildren("special")) {
            Element e = (Element) o;
            if ("GC Content".equals(e.getAttributeValue("value"))) hasContent = true;
            if ("GC Skew".equals(e.getAttributeValue("value"))) hasSkew = true;
        }
        if (!hasContent) {
            Element e = new Element("special");
            e.setAttribute("value", "GC Content");
            profile.getRootElement().addContent(e);
        }
        if (!hasSkew) {
            Element e = new Element("special");
            e.setAttribute("value", "GC Skew");
            profile.getRootElement().addContent(e);
        }
        return profile;
    }

    private void configureProfile(Element root, String refFile, String outputDir, String outputFile) {
        root.setAttribute("queryFile", refFile);
        root.setAttribute("outputFolder", outputDir);
        root.setAttribute("outputFile", outputFile);
        root.setAttribute("blastOptions", "");
        root.setAttribute("imageFormat", "jpg");
        root.setAttribute("legendPosition", "upper-right");
    }

    private void addRing(Element root, int position, String fileName, String filePath, String color) {
        Element ring = new Element("ring");
        ring.setAttribute("position", Integer.toString(position));
        String name = fileName.contains(".")
                ? fileName.split("\\.")[0] : fileName;
        ring.setAttribute("name", name);
        ring.setAttribute("colour", color);
        ring.setAttribute("upperInt", "90");
        ring.setAttribute("lowerInt", "70");
        ring.setAttribute("legend", "yes");
        ring.setAttribute("size", "30");
        ring.setAttribute("labels", "no");
        ring.setAttribute("blastType", "blastn");

        Element seq = new Element("sequence");
        seq.setAttribute("location", filePath);
        ring.addContent(seq);
        root.addContent(ring);
    }

    private void runPipeline(Document profile, String outputDir, String refFile,
                             String outputFile, String imageFormat) throws Exception {
        BRIG.PROFILE = profile;
        Element root = profile.getRootElement();

        // Detect BLAST+
        assertTrue(BRIG.isBlastOk(), "BLAST+ not found on PATH");

        // Create scratch directory
        File scratchDir = new File(outputDir + File.separator + "scratch");
        assertTrue(scratchDir.mkdir(), "Failed to create scratch dir");

        // Set queryFastaFile and calculate genome length
        root.setAttribute("queryFastaFile", refFile);
        BRIG.GEN_LENGTH = BRIG.FastaLength(refFile, false);
        assertTrue(BRIG.GEN_LENGTH > 0,
                "Genome length should be > 0, got: " + BRIG.GEN_LENGTH);

        // Generate CGView XML legend
        BRIG.WriteXMLLegend();
        assertNotNull(root.getAttributeValue("cgXML"),
                "cgXML attribute should be set after WriteXMLLegend");
        String cgXML = root.getAttributeValue("cgXML");
        assertTrue(Files.exists(Path.of(cgXML)),
                "CGView XML file should exist: " + cgXML);

        // Run BLAST (clean=1: force fresh run)
        BRIG.RunBlast(1);

        // Verify .tab files were created in scratch
        File[] tabFiles = scratchDir.listFiles((dir, name) -> name.endsWith(".tab"));
        assertNotNull(tabFiles);
        assertTrue(tabFiles.length > 0,
                "Expected BLAST .tab output files in scratch dir");

        // Parse BLAST results
        BRIG.ParseBlast();

        // Render CGView image
        BRIG.RunCGview(imageFormat);

        // Verify output image was created
        File outputImage = new File(outputFile + "." + imageFormat);
        assertTrue(outputImage.exists(),
                "Output image should exist: " + outputImage);
        assertTrue(outputImage.length() > 0,
                "Output image should not be empty");

        System.out.println("SUCCESS: Generated " + outputImage.getAbsolutePath()
                + " (" + (outputImage.length() / 1024) + " KB)");
    }
}
