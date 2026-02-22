package brig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads and extracts BLAST+ from NCBI when it is not found on the system.
 * Files are stored in ~/.brig/blast+/ and reused across sessions.
 */
public class BlastDownloader {

    private static final Logger log = LoggerFactory.getLogger(BlastDownloader.class);
    private static final String BLAST_FTP =
            "https://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/";
    private static final Path BLAST_HOME =
            Path.of(System.getProperty("user.home"), ".brig", "blast+");

    /**
     * Returns the bin/ directory of a locally installed BLAST+, or null if none found.
     */
    public static String findLocalBlast() {
        if (!Files.isDirectory(BLAST_HOME)) return null;
        try (var dirs = Files.list(BLAST_HOME)) {
            return dirs
                    .filter(Files::isDirectory)
                    .map(d -> d.resolve("bin"))
                    .filter(Files::isDirectory)
                    .filter(bin -> Files.isExecutable(bin.resolve("blastn"))
                            || Files.exists(bin.resolve("blastn.exe")))
                    .map(Path::toString)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            log.warn("Error scanning local BLAST+ directory", e);
            return null;
        }
    }

    /**
     * Downloads and extracts BLAST+ for the current OS/architecture.
     * Returns the bin/ directory path on success, or null on failure.
     */
    public static String downloadBlast() {
        String suffix = getArchiveSuffix();
        if (suffix == null) {
            BRIG.Print("SYS_ERROR: Unsupported OS/architecture for BLAST+ download: "
                    + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
            return null;
        }

        try {
            // Fetch directory listing to find the exact filename (version changes)
            String filename = findBlastFilename(suffix);
            if (filename == null) {
                BRIG.Print("SYS_ERROR: Could not find BLAST+ package for " + suffix);
                return null;
            }

            Files.createDirectories(BLAST_HOME);
            Path tarGz = BLAST_HOME.resolve(filename);

            // Download
            String downloadUrl = BLAST_FTP + filename;
            BRIG.Print("Downloading BLAST+ from " + downloadUrl + " ...");
            downloadFile(downloadUrl, tarGz);
            BRIG.Print("Download complete: " + tarGz);

            // Extract
            BRIG.Print("Extracting BLAST+ ...");
            extract(tarGz);
            BRIG.Print("Extraction complete.");

            // Clean up the tar.gz
            Files.deleteIfExists(tarGz);

            // Find the extracted bin dir
            String binDir = findLocalBlast();
            if (binDir != null) {
                BRIG.Print("BLAST+ installed to: " + binDir);
            }
            return binDir;
        } catch (Exception e) {
            log.error("Failed to download BLAST+", e);
            BRIG.Print("SYS_ERROR: Failed to download BLAST+: " + e.getMessage());
            return null;
        }
    }

    static String getArchiveSuffix() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();

        String osPart;
        if (os.contains("mac")) {
            osPart = "macosx";
        } else if (os.contains("linux")) {
            osPart = "linux";
        } else if (os.contains("win")) {
            osPart = "win64";
        } else {
            return null;
        }

        String archPart;
        if (arch.equals("aarch64") || arch.equals("arm64")) {
            archPart = "aarch64";
        } else if (arch.equals("amd64") || arch.equals("x86_64")) {
            archPart = "x64";
        } else {
            return null;
        }

        return archPart + "-" + osPart + ".tar.gz";
    }

    static String findBlastFilename(String suffix) throws IOException {
        URL url = new URL(BLAST_FTP);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        try {
            int status = conn.getResponseCode();
            if (status != 200) {
                BRIG.Print("SYS_ERROR: HTTP " + status + " from NCBI FTP");
                return null;
            }
            String html;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                html = sb.toString();
            }
            // Look for filename like ncbi-blast-2.17.0+-x64-macosx.tar.gz
            Pattern p = Pattern.compile("(ncbi-blast-[\\d.]+\\+-" + Pattern.quote(suffix) + ")");
            Matcher m = p.matcher(html);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        } finally {
            conn.disconnect();
        }
    }

    static void downloadFile(String urlStr, Path dest) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        try {
            long total = conn.getContentLengthLong();
            try (InputStream in = conn.getInputStream();
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(dest))) {
                byte[] buf = new byte[65536];
                long downloaded = 0;
                int lastPercent = -1;
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                    downloaded += n;
                    if (total > 0) {
                        int percent = (int) (downloaded * 100 / total);
                        if (percent != lastPercent && percent % 10 == 0) {
                            BRIG.Print("  " + percent + "% (" + (downloaded / (1024 * 1024)) + " MB)");
                            lastPercent = percent;
                        }
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
    }

    static void extract(Path tarGz) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("tar", "xzf", tarGz.toString(),
                "-C", BLAST_HOME.toString());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("tar: {}", line);
            }
        }
        int exit = p.waitFor();
        if (exit != 0) {
            throw new IOException("tar extraction failed with exit code " + exit);
        }
    }
}
