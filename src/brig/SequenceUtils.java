package brig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for sequence file handling: FASTA parsing, GenBank/EMBL
 * formatting, and related helpers extracted from BRIG.java.
 */
public class SequenceUtils {

    private static final Logger log = LoggerFactory.getLogger(SequenceUtils.class);

    private static final Pattern PROTEIN_PATTERN = Pattern.compile("[^ATGCNU\\-atgcnu]");

    public static String FetchFilename(String filename) {
        int dirend = filename.lastIndexOf(File.separatorChar);
        return filename.substring(dirend + 1);
    }

    public static int FastaLength(String file, boolean multi) throws FileNotFoundException, IOException {
        int caret = 0;
        boolean isPro = isProteinFASTA(file);
        try (var in = Files.newBufferedReader(Path.of(file))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.contains(">") && !multi) {
                    line.trim();
                    int len = line.toCharArray().length;
                    if (isPro) {
                        caret += len * 3;
                    } else {
                        caret += len;
                    }
                }
                if (multi && line.contains(">")) {
                    var commaarray = line.split(":");
                    caret = Integer.parseInt(commaarray[2]);
                }
            }
        }
        return caret;
    }

    public static int isMultiFasta(String file) throws FileNotFoundException, IOException {
        try (var in = Files.newBufferedReader(Path.of(file))) {
            String line;
            int i = 0;
            while ((line = in.readLine()) != null) {
                if (line.startsWith(">")) {
                    i++;
                }
            }
            return i;
        }
    }

    public static String formatMultiFASTA(String input, String output, int spacer, boolean label) throws FileNotFoundException, IOException {
        String error = "Multi-FASTA detected, Rewriting headers\n";
        var head = new Vector<String>();
        String tempHead = "";
        int start = 0;
        int lineNum = 0;
        int stop = 0;
        boolean multi = isProteinFASTA(input);
        try (var first = Files.newBufferedReader(Path.of(input))) {
            String line;
            while ((line = first.readLine()) != null) {
                if (line.startsWith(">") && lineNum == 0) {
                    tempHead = line;
                } else if (line.startsWith(">") && lineNum > 0) {
                    tempHead = tempHead.replaceAll(":", "");
                    if (!label) {
                        tempHead = tempHead.split(" ")[0];
                    }
                    head.add(tempHead + ":" + start + ":" + stop);
                    tempHead = line;
                    stop += spacer;
                    start = stop;
                } else {
                    var temp = line.replaceAll(" ", "");
                    if (multi) {
                        stop += temp.length() * 3;
                    } else {
                        stop += temp.length();
                    }
                }
                lineNum++;
            }
        }
        tempHead = tempHead.replaceAll(":", "");
        if (!label) {
            tempHead = tempHead.split(" ")[0];
        }
        head.add(tempHead + ":" + start + ":" + stop);
        lineNum = 0;
        try (var first = Files.newBufferedReader(Path.of(input));
             var out = Files.newBufferedWriter(Path.of(output))) {
            String line;
            while ((line = first.readLine()) != null) {
                if (line.startsWith(">")) {
                    out.write(head.get(lineNum).toString());
                    out.newLine();
                    lineNum++;
                } else {
                    out.write(line.replaceAll(" ", ""));
                    out.newLine();
                }
            }
        }
        return error;
    }

    public static boolean isProteinFASTA(String input) throws FileNotFoundException, IOException {
        try (var first = Files.newBufferedReader(Path.of(input))) {
            String line;
            int five = 0;
            while ((line = first.readLine()) != null) {
                if (!line.contains(">")) {
                    if (PROTEIN_PATTERN.matcher(line).find()) {
                        return true;
                    }
                    if (five == 5) {
                        return false;
                    }
                    five++;
                }
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static String FormatGenbank(String file, String header, String out, boolean embl, String pOption) throws FileNotFoundException, IOException {
        String error = "";
        try (var in = Files.newBufferedReader(Path.of(file));
             var out1 = Files.newBufferedWriter(Path.of(out + ".fna"))) {
            out1.write(">" + header);
            out1.newLine();
            error += "Formatting nucleotide file...\n";
            String line;
            int on = 0;
            while ((line = in.readLine()) != null) {
                if (on == 1) {
                    out1.write(line.replaceAll("[^a-zA-Z]+", ""));
                    out1.newLine();
                }
                if ((line.startsWith("ORIGIN") && !embl)
                        || (embl && line.startsWith("SQ"))) {
                    on = 1;
                }
            }
        }
        if ("T".equals(pOption)) {
            try (var out2 = Files.newBufferedWriter(Path.of(out + ".faa"))) {
                var features = AnnoXML.CreateFeatureXML(file, embl);
                List<Element> CDS = features.getRootElement().getChildren("CDS");
                error += "Formatting protein file...\n";
                var newList = new translateProtein();
                for (Element currentElement : CDS) {
                    String name = "unknown";
                    if (currentElement.getChild("locus_tag") != null) {
                        name = currentElement.getChild("locus_tag").getAttributeValue("value");
                    } else if (currentElement.getChild("gene") != null) {
                        name = currentElement.getChild("gene").getAttributeValue("value");
                    }
                    var stop = currentElement.getAttributeValue("stop");
                    var start = currentElement.getAttributeValue("start");
                    if (currentElement.getChild("translation") != null) {
                        out2.write(">" + name + ":" + start + ":" + stop);
                        out2.newLine();
                        out2.write(WordWrap(currentElement.getChild("translation").getAttributeValue("value")));
                    } else {
                        var nu = fastaSubsequence(out + ".fna", Integer.parseInt(start), Integer.parseInt(stop));
                        if (!"\\s+".equals(nu) && !nu.isEmpty()) {
                            out2.write(">" + name + ":" + start + ":" + stop);
                            out2.newLine();
                            var pu = newList.translateSeq(nu).replaceAll("\\*", "");
                            out2.write(WordWrap(pu));
                        }
                    }
                }
            }
        }
        return error;
    }

    public static boolean isGenbank(String file) {
        if (BRIG.PROFILE.getRootElement().getChild("brig_settings") != null) {
            var settings = BRIG.PROFILE.getRootElement().getChild("brig_settings");
            if (settings.getAttributeValue("genbankFiles") != null) {
                var em = settings.getAttributeValue("genbankFiles").split(",");
                for (var ext : em) {
                    if (file.endsWith(ext)) {
                        return true;
                    }
                }
            }
        } else {
            if (file.endsWith(".gbk")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmbl(String file) {
        if (BRIG.PROFILE.getRootElement().getChild("brig_settings") != null) {
            var settings = BRIG.PROFILE.getRootElement().getChild("brig_settings");
            if (settings.getAttributeValue("emblFiles") != null) {
                var em = settings.getAttributeValue("emblFiles").split(",");
                for (var ext : em) {
                    if (file.endsWith(ext)) {
                        return true;
                    }
                }
            }
        } else {
            if (file.endsWith(".embl")) {
                return true;
            }
        }
        return false;
    }

    public static String WordWrap(String terg) {
        int z = 100;
        int y = 0;
        var out = new StringBuilder();
        while (z < terg.length()) {
            out.append(terg, y, z).append("\n");
            y = z;
            z = z + 100;
            if (z > terg.length()) {
                out.append(terg, y, terg.length() - 1).append("\n");
            }
        }
        if (terg.length() <= 100) {
            out.append(terg.substring(y)).append("\n");
        }
        return out.toString();
    }

    public static String fastaSubsequence(String file, int start, int stop) {
        var spline = new StringBuilder();
        try (var in = Files.newBufferedReader(Path.of(file))) {
            String line;
            int caret = 1;
            while ((line = in.readLine()) != null) {
                if (!line.startsWith(">")) {
                    if (caret < stop && caret + line.length() > start) {
                        var lin = line.toCharArray();
                        for (int i = 0; i < lin.length; i++) {
                            if (i + caret >= start && i + caret <= stop) {
                                spline.append(lin[i]);
                            }
                        }
                    }
                    caret += line.length();
                }
            }
        } catch (Exception e) {
            log.error("Error reading FASTA subsequence from {} (start={}, stop={})", file, start, stop, e);
        }
        return spline.toString();
    }

    public static int checkComment(String file) throws FileNotFoundException, IOException {
        try (var in = Files.newBufferedReader(Path.of(file))) {
            String line;
            int d = 0;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("#")) {
                    d++;
                }
            }
            return d;
        }
    }
}
