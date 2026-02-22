package brig;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration and validation methods extracted from BRIG.java:
 * BLAST option validation, colour lookup, profile preparation, session validation.
 */
public class BlastSettings {

    private static final Logger log = LoggerFactory.getLogger(BlastSettings.class);

    public static int getBlastThreads() {
        int defaultThreads = Runtime.getRuntime().availableProcessors();
        if (BRIG.PROFILE == null) return defaultThreads;
        Element settings = BRIG.PROFILE.getRootElement().getChild("brig_settings");
        if (settings != null) {
            String val = settings.getAttributeValue("blastThreads");
            if (val != null) {
                try {
                    int t = Integer.parseInt(val);
                    if (t > 0) return t;
                } catch (NumberFormatException e) { /* use default */ }
            }
        }
        return defaultThreads;
    }

    public static String BlastOption(String opt) {
        var error = new StringBuilder("BLAST+ option error: ");
        var noBlast = new String[]{
            "-h", "-help", "-db", "-query", "subject", "-out", "-version", "-outfmt",
            "-p", "-d", "-i", "-e", "-m", "-o", "-F", "-G", "-E", "-X", "-I", "-q", "-r", "-v",
            "-b", "-f", "-g", "-Q", "-D", "-a", "-O", "-J", "-M", "-W", "-z", "-K", "-P", "-T",
            "-U", "-y", "-Z", "-R", "-L", "-w", "-B", "-C", "-s"
        };

        var tabArray = opt.split("\\s+");
        int er = 0;
        for (var blocked : noBlast) {
            for (var token : tabArray) {
                if (blocked.equals(token)) {
                    error.append(blocked).append(" ");
                    er++;
                }
            }
        }
        if (er == 1) {
            error.append("\nis not a valid parameter");
        } else {
            error.append("\nare not valid parameters");
        }
        return er > 0 ? error.toString() : null;
    }

    public static boolean isBlastOk() throws IOException {
        // 0. Try embedded BLAST (bundled inside jpackage installer)
        String embeddedBin = BRIG.APP_DIR + "blast" + BRIG.SL;
        if (tryBlastPlus(embeddedBin)) {
            setBlastLocation(embeddedBin.substring(0, embeddedBin.length() - 1));
            return true;
        }

        var bin = getConfiguredBlastBin();

        // 1. Try configured/PATH location
        if (tryBlastPlus(bin)) {
            // If found on bare PATH (empty bin), resolve the actual directory
            if (bin.isEmpty()) {
                String resolved = resolveBlastOnPath();
                if (resolved != null) {
                    setBlastLocation(resolved);
                }
            }
            return true;
        }

        // 2. Try previously downloaded local copy
        String localBin = BlastDownloader.findLocalBlast();
        if (localBin != null) {
            log.info("Found local BLAST+ at: {}", localBin);
            if (tryBlastPlus(localBin + BRIG.SL)) {
                setBlastLocation(localBin);
                return true;
            }
        }

        // 3. Auto-download BLAST+
        BRIG.Print("BLAST+ not found on system. Downloading from NCBI...");
        String downloaded = BlastDownloader.downloadBlast();
        if (downloaded != null) {
            if (tryBlastPlus(downloaded + BRIG.SL)) {
                setBlastLocation(downloaded);
                return true;
            }
        }

        return false;
    }

    /**
     * Resolves the directory containing blastn on PATH using "which" (Unix)
     * or "where" (Windows).
     */
    private static String resolveBlastOnPath() {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            List<String> cmd = os.contains("win")
                    ? Arrays.asList("where", "blastn")
                    : Arrays.asList("which", "blastn");
            Process p = BRIG.execCommand(cmd);
            try (var reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty()) {
                    Path blastnPath = Path.of(line.trim()).toRealPath();
                    return blastnPath.getParent().toString();
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve blastn on PATH", e);
        }
        return null;
    }

    private static String getConfiguredBlastBin() {
        var bin = "";
        if (BRIG.PROFILE.getRootElement().getChild("brig_settings") != null) {
            if (BRIG.PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation") != null) {
                bin = BRIG.PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation");
            }
        }
        if (!bin.endsWith(BRIG.SL) && !bin.isEmpty()) {
            bin += BRIG.SL;
        }
        return bin;
    }

    private static boolean tryBlastPlus(String bin) {
        try {
            var cmd = Arrays.asList(bin + "blastn", "-help");
            log.debug(BRIG.formatCommand(cmd));
            var q = BRIG.execCommand(cmd);
            var ergo = new StringBuilder();
            try (var buffrdr = new BufferedReader(new InputStreamReader(q.getInputStream()))) {
                String data;
                while ((data = buffrdr.readLine()) != null) {
                    ergo.append(data).append("\n");
                    if (ergo.toString().contains("USAGE")) {
                        log.debug(ergo.toString());
                        q.destroy();
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("BLAST+ not found at: {}", bin);
        }
        return false;
    }

    private static void setBlastLocation(String binDir) {
        var settings = BRIG.PROFILE.getRootElement().getChild("brig_settings");
        if (settings != null) {
            settings.setAttribute("blastLocation", binDir);
        }
    }

    public static Color FetchColor(int RingNumber) {
        var out = Color.red;
        int get = RingNumber % 10;
        var settings = BRIG.PROFILE.getRootElement().getChild("brig_settings");
        if (settings != null) {
            var col = settings.getAttributeValue("Ring" + get);
            if (col == null) {
                col = "225,0,0";
            }
            var tabArray = col.split(",");
            out = new Color(Integer.parseInt(tabArray[0]), Integer.parseInt(tabArray[1]), Integer.parseInt(tabArray[2]));
            FileUtils.dumpXML("errorlog.xml", BRIG.PROFILE);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static Document prepProfile(Document profil) {
        var doc = new Document(new Element("BRIG"));
        var root = profil.getRootElement();
        root.removeAttribute("archive");
        doc.getRootElement().addContent((Element) root.getChild("cgview_settings").clone());
        doc.getRootElement().addContent((Element) root.getChild("brig_settings").clone());
        if (root.getAttributeValue("blastOptions") != null) {
            doc.getRootElement().setAttribute("blastOptions", root.getAttributeValue("blastOptions"));
        }
        if (root.getAttributeValue("legendPosition") != null) {
            doc.getRootElement().setAttribute("legendPosition", root.getAttributeValue("legendPosition"));
        }
        List<Element> special = root.getChildren("special");
        for (Element element : special) {
            doc.getRootElement().addContent((Element) element.clone());
        }
        return doc;
    }

    @SuppressWarnings("unchecked")
    public static String ValidateSession() {
        var root = BRIG.PROFILE.getRootElement();
        var query = root.getAttributeValue("queryFile");
        if (!Files.exists(Path.of(query))) {
            return "query file " + query + " does not exist";
        }
        if (query.contains(" ")) {
            return query + " contains a space. Invalid filename.";
        }
        query = root.getAttributeValue("outputFolder");
        if (!Files.exists(Path.of(query))) {
            return "output file " + query + " does not exist";
        }
        if (query.contains(" ")) {
            return query + " contains a space. Invalid filename.";
        }
        if (root.getChild("refDir") != null) {
            List<Element> ref = ((Element) root.getChild("refDir")).getChildren();
            for (Element element : ref) {
                query = element.getAttributeValue("location");
                if (!Files.exists(Path.of(query))) {
                    return "query file " + query + " does not exist";
                }
                if (query.contains(" ")) {
                    return query + " contains a space. Invalid filename.";
                }
            }
        }
        return null;
    }
}
