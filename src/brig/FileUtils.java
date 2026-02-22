package brig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File I/O utility methods extracted from BRIG.java:
 * binary/text copy, XML serialization.
 */
public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static void copyTo(File inputFile, File outputFile, boolean binary) throws FileNotFoundException, IOException {
        Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static int saveXML(String output, Document profil) {
        try (var out1 = Files.newBufferedWriter(java.nio.file.Path.of(output))) {
            var serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            serializer.output(profil, out1);
            return 0;
        } catch (IOException e) {
            log.error("Failed to save XML to {}", output, e);
            return 1;
        }
    }

    public static int dumpXML(String output, Document profil) {
        try (var out1 = Files.newBufferedWriter(java.nio.file.Path.of(output),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND)) {
            var serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            out1.write(getDateTime());
            out1.newLine();
            serializer.output(profil, out1);
            return 0;
        } catch (IOException e) {
            log.error("Failed to dump XML to {}", output, e);
            return 1;
        }
    }

    public static void dumpXMLDebug() {
        try {
            var serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            serializer.output(BRIG.PROFILE, System.out);
        } catch (IOException e) {
            log.error("Failed to dump XML debug output", e);
        }
    }

    private static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
