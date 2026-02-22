package brig;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BRIGcli {

    private static final Logger log = LoggerFactory.getLogger(BRIGcli.class);

    public static void main(String[] args) {
        run(args);
    }

    public static void run(String[] args) {
        if (args.length == 0 || (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0])))) {
            printUsage();
            return;
        }

        // Parse arguments
        String reference = null;
        String sequenceFolder = null;
        String output = null;
        String title = null;
        String format = "png";
        String configPath = null;
        boolean gcContent = false;
        boolean gcSkew = false;

        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "--output":
                    if (i + 1 < args.length) output = args[++i];
                    break;
                case "--title":
                    if (i + 1 < args.length) title = args[++i];
                    break;
                case "--format":
                    if (i + 1 < args.length) format = args[++i];
                    break;
                case "--config":
                    if (i + 1 < args.length) configPath = args[++i];
                    break;
                case "--gc-content":
                    gcContent = true;
                    break;
                case "--gc-skew":
                    gcSkew = true;
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    return;
                default:
                    if (args[i].startsWith("-")) {
                        System.err.println("Unknown option: " + args[i]);
                        printUsage();
                        System.exit(1);
                    }
                    if (reference == null) {
                        reference = args[i];
                    } else if (sequenceFolder == null) {
                        sequenceFolder = args[i];
                    }
                    break;
            }
            i++;
        }

        if (reference == null || sequenceFolder == null) {
            System.err.println("Error: reference and sequence_folder are required.");
            printUsage();
            System.exit(1);
        }

        File refFile = new File(reference);
        File seqDir = new File(sequenceFolder);

        if (!refFile.exists()) {
            System.err.println("Error: Reference file does not exist: " + reference);
            System.exit(1);
        }
        if (!seqDir.isDirectory()) {
            System.err.println("Error: Sequence folder does not exist or is not a directory: " + sequenceFolder);
            System.exit(1);
        }

        String outputFolder = seqDir.getAbsolutePath();
        if (output == null) {
            output = outputFolder + File.separator + "brig_output";
        } else {
            // Strip extension from output path — BRIG appends format extension
            if (output.contains(".")) {
                String ext = output.substring(output.lastIndexOf('.') + 1);
                if ("png".equals(ext) || "jpg".equals(ext) || "svg".equals(ext) || "svgz".equals(ext)) {
                    format = ext;
                    output = output.substring(0, output.lastIndexOf('.'));
                }
            }
        }

        if (title == null) {
            title = refFile.getName();
            if (title.contains(".")) {
                title = title.substring(0, title.lastIndexOf('.'));
            }
        }

        // Load default profile
        SAXBuilder builder = new SAXBuilder();
        try {
            BRIG.PROFILE = builder.build(BRIG.PROFILE_LOCATION);
        } catch (Exception e) {
            System.err.println("Error: Could not load " + BRIG.PROFILE_LOCATION + ": " + e.getMessage());
            System.exit(1);
        }

        // Ensure special elements exist
        ensureSpecialElements();

        // Apply JSON config overrides if provided
        if (configPath != null) {
            applyJsonConfig(configPath);
        }

        // Set profile attributes
        Element root = BRIG.PROFILE.getRootElement();
        root.setAttribute("queryFile", refFile.getAbsolutePath());
        root.setAttribute("outputFolder", outputFolder);
        root.setAttribute("outputFile", output);
        root.setAttribute("title", title);
        root.setAttribute("imageFormat", format);

        BRIG.Print(BRIG.header);
        BRIG.Print("CLI mode");
        BRIG.Print("Reference: " + refFile.getAbsolutePath());
        BRIG.Print("Sequences: " + seqDir.getAbsolutePath());

        // Check BLAST
        try {
            if (!BRIG.isBlastOk()) {
                System.err.println("Error: Could not find or download BLAST+. Please install BLAST+ and try again.");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("Error checking BLAST: " + e.getMessage());
            System.exit(1);
        }

        // Discover sequences
        Vector<String> files = One.SearchDir(seqDir.getAbsolutePath());
        if (files.isEmpty()) {
            System.err.println("Error: No valid sequence files found in " + seqDir.getAbsolutePath());
            System.exit(1);
        }

        BRIG.Print("Found " + files.size() + " sequence file(s)");

        // Remove any existing rings
        root.removeChildren("ring");
        BRIG.POSITION = 0;

        // Create rings for each discovered file
        for (int f = 0; f < files.size(); f++) {
            String filePath = files.get(f);
            createRing(root, filePath, f);
        }

        // Add GC Content ring
        if (gcContent) {
            createSpecialRing(root, "GC Content", BRIG.POSITION);
        }

        // Add GC Skew ring
        if (gcSkew) {
            createSpecialRing(root, "GC Skew", BRIG.POSITION);
        }

        // Add refDir for the sequence folder
        Element refDir = new Element("refDir");
        refDir.setAttribute("location", seqDir.getAbsolutePath());
        for (String filePath : files) {
            Element refFile2 = new Element("refFile");
            refFile2.setAttribute("location", filePath);
            refDir.addContent(refFile2);
        }
        root.addContent(refDir);

        // Run the pipeline (same logic as Three2.compute())
        runPipeline(root, format);

        // Save session XML
        String sessionPath = outputFolder + File.separator + "brig_session.xml";
        BRIG.saveXML(sessionPath, BRIG.PROFILE);
        BRIG.Print("Session saved to: " + sessionPath);
        BRIG.Print("Output image: " + output + "." + format);
    }

    private static void ensureSpecialElements() {
        @SuppressWarnings("unchecked")
        List<Element> refs = BRIG.PROFILE.getRootElement().getChildren("special");
        int noSkew = 0;
        int noContent = 0;
        for (Element current : refs) {
            if ("GC Skew".equals(current.getAttributeValue("value"))) noSkew++;
            if ("GC Content".equals(current.getAttributeValue("value"))) noContent++;
        }
        if (noContent == 0) {
            Element re = new Element("special");
            re.setAttribute("value", "GC Content");
            BRIG.PROFILE.getRootElement().addContent(re);
        }
        if (noSkew == 0) {
            Element re = new Element("special");
            re.setAttribute("value", "GC Skew");
            BRIG.PROFILE.getRootElement().addContent(re);
        }
    }

    private static void createRing(Element root, String filePath, int index) {
        Element ring = new Element("ring");
        Color col = BlastSettings.FetchColor(index + 1);
        String colorStr = col.getRed() + "," + col.getGreen() + "," + col.getBlue();
        ring.setAttribute("colour", colorStr);

        String name = new File(filePath).getName();
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        ring.setAttribute("name", name);
        ring.setAttribute("position", Integer.toString(BRIG.POSITION));
        ring.setAttribute("upperInt", "70");
        ring.setAttribute("lowerInt", "50");
        ring.setAttribute("legend", "yes");
        ring.setAttribute("size", "30");
        ring.setAttribute("labels", "no");
        ring.setAttribute("blastType", "blastn");

        Element seq = new Element("sequence");
        seq.setAttribute("location", filePath);
        ring.addContent(seq);

        root.addContent(ring);
        BRIG.POSITION++;
    }

    private static void createSpecialRing(Element root, String type, int position) {
        Element ring = new Element("ring");
        ring.setAttribute("colour", "0,0,0");
        ring.setAttribute("name", type);
        ring.setAttribute("position", Integer.toString(position));
        ring.setAttribute("upperInt", "0");
        ring.setAttribute("lowerInt", "0");
        ring.setAttribute("legend", "no");
        ring.setAttribute("size", "30");
        ring.setAttribute("labels", "no");
        ring.setAttribute("blastType", "blastn");

        Element seq = new Element("sequence");
        seq.setAttribute("location", type);
        ring.addContent(seq);

        root.addContent(ring);
        BRIG.POSITION++;
    }

    @SuppressWarnings("unchecked")
    private static void applyJsonConfig(String configPath) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String json = sb.toString().trim();
            Element root = BRIG.PROFILE.getRootElement();

            // Parse blastOptions
            String blastOptions = extractJsonString(json, "blastOptions");
            if (blastOptions != null) {
                root.setAttribute("blastOptions", blastOptions);
            }

            // Parse legendPosition
            String legendPosition = extractJsonString(json, "legendPosition");
            if (legendPosition != null) {
                root.setAttribute("legendPosition", legendPosition);
            }

            // Parse cgview settings
            String cgviewBlock = extractJsonObject(json, "cgview");
            if (cgviewBlock != null) {
                Element cgSettings = root.getChild("cgview_settings");
                if (cgSettings != null) {
                    applyJsonAttributes(cgviewBlock, cgSettings);
                }
            }

            // Parse brig settings
            String brigBlock = extractJsonObject(json, "brig");
            if (brigBlock != null) {
                Element brigSettings = root.getChild("brig_settings");
                if (brigSettings != null) {
                    applyJsonAttributes(brigBlock, brigSettings);
                }
            }

            BRIG.Print("Applied config from: " + configPath);
        } catch (IOException e) {
            System.err.println("Warning: Could not read config file: " + e.getMessage());
        }
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx == -1) return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart == -1) return null;
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd == -1) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private static String extractJsonObject(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        int braceStart = json.indexOf('{', idx + pattern.length());
        if (braceStart == -1) return null;
        int depth = 1;
        int pos = braceStart + 1;
        while (pos < json.length() && depth > 0) {
            if (json.charAt(pos) == '{') depth++;
            else if (json.charAt(pos) == '}') depth--;
            pos++;
        }
        if (depth != 0) return null;
        return json.substring(braceStart, pos);
    }

    private static void applyJsonAttributes(String jsonObj, Element element) {
        // Simple key-value parser for flat JSON objects
        String content = jsonObj.substring(1, jsonObj.length() - 1).trim();
        if (content.isEmpty()) return;

        int pos = 0;
        while (pos < content.length()) {
            // Find key
            int keyStart = content.indexOf('"', pos);
            if (keyStart == -1) break;
            int keyEnd = content.indexOf('"', keyStart + 1);
            if (keyEnd == -1) break;
            String attrKey = content.substring(keyStart + 1, keyEnd);

            // Find value
            int colonIdx = content.indexOf(':', keyEnd + 1);
            if (colonIdx == -1) break;
            int valStart = content.indexOf('"', colonIdx + 1);
            if (valStart == -1) break;
            int valEnd = content.indexOf('"', valStart + 1);
            if (valEnd == -1) break;
            String attrVal = content.substring(valStart + 1, valEnd);

            element.setAttribute(attrKey, attrVal);
            pos = valEnd + 1;
        }
    }

    private static void runPipeline(Element root, String format) {
        String output = root.getAttributeValue("outputFolder");
        if (!(new File(output)).isDirectory()) {
            BRIG.Print("Error: Output directory is not valid: " + output);
            System.exit(1);
        }

        new File(output + BRIG.SL + "scratch").mkdir();
        String fileName = BRIG.FetchFilename(root.getAttributeValue("queryFile"));
        boolean genbank = BRIG.isGenbank(root.getAttributeValue("queryFile"));
        boolean embl = BRIG.isEmbl(root.getAttributeValue("queryFile"));

        if (genbank || embl) {
            try {
                String ou = output + BRIG.SL + "scratch" + BRIG.SL + fileName;
                if (root.getAttribute("genbankProtein") != null) {
                    root.setAttribute("queryFastaFile", ou + ".faa");
                    root.setAttribute("spacer", Integer.toString(0));
                    String text = BRIG.FormatGenbank(root.getAttributeValue("queryFile"),
                        fileName, output + BRIG.SL + "scratch" + BRIG.SL + fileName, embl, "T");
                    BRIG.Print(text);
                } else {
                    root.setAttribute("queryFastaFile", ou + ".fna");
                    String text = BRIG.FormatGenbank(root.getAttributeValue("queryFile"),
                        fileName, output + BRIG.SL + "scratch" + BRIG.SL + fileName, embl, "F");
                    BRIG.Print(text);
                }
                BRIG.GEN_LENGTH = BRIG.FastaLength(ou + ".fna", false);
            } catch (Exception e) {
                BRIG.Print("Error processing GenBank/EMBL reference: " + e.getMessage());
                log.error("Failed to format genbank input", e);
                System.exit(1);
            }
        } else if (root.getAttributeValue("spacer") != null) {
            try {
                int space = Integer.parseInt(root.getAttributeValue("spacer"));
                String ou = output + BRIG.SL + "scratch" + BRIG.SL + "Spaced" + space + fileName;
                String text = BRIG.formatMultiFASTA(root.getAttributeValue("queryFile"), ou, space, false);
                root.setAttribute("queryFastaFile", ou);
                BRIG.Print(text);
                BRIG.GEN_LENGTH = BRIG.FastaLength(root.getAttributeValue("queryFastaFile"), true);
                BRIG.GEN_LENGTH += space + 10;
            } catch (Exception e) {
                BRIG.Print("Error processing multi-FASTA reference: " + e.getMessage());
                log.error("Failed to format multi-FASTA input", e);
                System.exit(1);
            }
        } else {
            root.setAttribute("queryFastaFile", root.getAttributeValue("queryFile"));
            try {
                BRIG.GEN_LENGTH = BRIG.FastaLength(root.getAttributeValue("queryFastaFile"), false);
                if (BRIG.isProteinFASTA(root.getAttributeValue("queryFastaFile"))) {
                    BRIG.GEN_LENGTH = BRIG.GEN_LENGTH * 3;
                    root.setAttribute("protein", "true");
                }
            } catch (Exception e) {
                BRIG.Print("Error reading FASTA length: " + e.getMessage());
                System.exit(1);
            }
        }

        BRIG.Print("Sequence length: " + BRIG.GEN_LENGTH);
        BRIG.Print("Initializing XML output...");
        BRIG.Print(BRIG.WriteXMLLegend());

        BRIG.Print("Running BLAST...");
        BRIG.Print(BRIG.RunBlast(1));

        BRIG.Print("Parsing BLAST results...");
        try {
            BRIG.Print(BRIG.ParseBlast());
        } catch (IOException e) {
            BRIG.Print("Error parsing BLAST: " + e.getMessage());
            System.exit(1);
        }

        BRIG.Print("Rendering image...");
        BRIG.Print(BRIG.RunCGview(format));
    }

    private static void printUsage() {
        System.out.println(BRIG.header);
        System.out.println();
        System.out.println("Usage: java -jar brig-cli.jar <reference> <sequence_folder> [options]");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  <reference>          Reference sequence (FASTA, GenBank, or EMBL)");
        System.out.println("  <sequence_folder>    Folder containing query sequences");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --output <path>      Output image file path (default: <folder>/brig_output.png)");
        System.out.println("  --title <text>       Image title (default: reference filename)");
        System.out.println("  --format <fmt>       Image format: png, jpg, svg, svgz (default: png)");
        System.out.println("  --gc-content         Add GC Content ring");
        System.out.println("  --gc-skew            Add GC Skew ring");
        System.out.println("  --config <path>      JSON config file for settings overrides");
        System.out.println("  --help, -h           Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar brig-cli.jar reference.fna sequences/");
        System.out.println("  java -jar brig-cli.jar ref.gbk seqs/ --gc-skew --title \"My Genome\" --format svg");
    }
}
