/* Copyright Nabil Alikhan. 2010.
 * This file is part of BLAST Ring Image Generator (BRIG).
 * BRIG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BRIG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *  along with BRIG.  If not, see <http://www.gnu.org/licenses/>.
 */
package brig;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import org.jdom.*;
import org.jdom.Attribute;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ualberta.stothard.cgview.Cgview;
import ca.ualberta.stothard.cgview.CgviewFactory;
import ca.ualberta.stothard.cgview.CgviewIO;

/**
 *
 * @author Nabil
 */
public class BRIG extends Thread{

    public static int POSITION = 0;
    public static Document PROFILE;
    public static String PROFILE_LOCATION;
    public static String SL = File.separator;
    public static int GEN_LENGTH = 0;
    public static final String header;
    public static final String APP_DIR;
    public static final String BRIG_HOME;
    public static final Image APP_ICON;
    static {
        String v = BRIG.class.getPackage().getImplementationVersion();
        if (v == null) v = "dev";
        header = "BLAST Ring Image Generator v" + v;
        APP_DIR = getAppDir();
        BRIG_HOME = System.getProperty("user.home") + File.separator + ".brig";
        PROFILE_LOCATION = initProfileLocation();
        APP_ICON = loadAppIcon();
    }

    private static Image loadAppIcon() {
        try {
            InputStream in = BRIG.class.getResourceAsStream("/brig/resources/brig-icon.png");
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (Exception e) {
            // icon is optional
        }
        return null;
    }

    static String getAppDir() {
        try {
            File jarFile = new File(BRIG.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // If running from a JAR, get its parent directory
            // If running from a classes directory, use that directory
            File dir = jarFile.isFile() ? jarFile.getParentFile() : jarFile;
            return dir.getAbsolutePath() + File.separator;
        } catch (URISyntaxException | SecurityException e) {
            // Fallback to current working directory
            return "";
        }
    }

    static String initProfileLocation() {
        // 1. Check ~/.brig/default-BRIG.xml — use it if it exists
        File brigHome = new File(BRIG_HOME);
        File homeProfile = new File(brigHome, "default-BRIG.xml");
        if (homeProfile.isFile()) {
            return homeProfile.getAbsolutePath();
        }
        // 2. Try to extract from classpath resource into ~/.brig/
        try (InputStream in = BRIG.class.getResourceAsStream("/brig/resources/default-BRIG.xml")) {
            if (in != null) {
                brigHome.mkdirs();
                try (FileOutputStream out = new FileOutputStream(homeProfile)) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                    }
                }
                return homeProfile.getAbsolutePath();
            }
        } catch (IOException e) {
            // Fall through to legacy path
        }
        // 3. Fallback: legacy location next to JAR
        return APP_DIR + "default-BRIG.xml";
    }

    public One parent;
    public static volatile JTextArea outputArea = null;
    private static final Logger log = LoggerFactory.getLogger(BRIG.class);

    static void Print(String message) {
        log.info(message);
        JTextArea area = outputArea;
        if (area != null) {
            SwingUtilities.invokeLater(() -> {
                area.append(message + "\n");
                area.setCaretPosition(area.getDocument().getLength());
            });
        } else {
            System.out.println(message);
        }
    }
    
    static Process execCommand(List<String> command) throws IOException {
        return new ProcessBuilder(command).start();
    }

    static String formatCommand(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (String arg : command) {
            if (sb.length() > 0) sb.append(' ');
            if (arg.contains(" ")) {
                sb.append('"').append(arg).append('"');
            } else {
                sb.append(arg);
            }
        }
        return sb.toString();
    }

    static List<String> tokenizeOptions(String options) {
        List<String> result = new ArrayList<>();
        if (options != null && !options.trim().isEmpty()) {
            for (String token : options.trim().split("\\s+")) {
                if (!token.isEmpty()) result.add(token);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();
        File del = new File("errorlog.xml");
        del.delete();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        
        log.info( "free memory: " + format.format(freeMemory / 1024));
        log.info( "allocated memory: " + format.format(allocatedMemory / 1024));
        log.info( "max memory: " + format.format(maxMemory / 1024));
        log.info( "total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
        //LOAD UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        //Read & Check Profile
        SAXBuilder builder = new SAXBuilder();
        try {
            PROFILE = builder.build(PROFILE_LOCATION);
            //Open one page.
            List<Element> refs = PROFILE.getRootElement().getChildren("special");
            int noSkew = 0;
            int noContent = 0;
            for (Element current : refs) {
                if ("GC Skew".equals(current.getAttributeValue("value"))) {
                    noSkew++;
                }
                if ("GC Content".equals(current.getAttributeValue("value"))) {
                    noContent++;
                }
            }
            if (noContent == 0) {
                Element re = new Element("special");
                re.setAttribute("value", "GC Content");
                PROFILE.getRootElement().addContent(re);
            }
            if (noSkew == 0) {
                Element re = new Element("special");
                re.setAttribute("value", "GC Skew");
                PROFILE.getRootElement().addContent(re);
            }
            // Set macOS Dock icon
            if (APP_ICON != null) {
                try {
                    java.awt.Taskbar.getTaskbar().setIconImage(APP_ICON);
                } catch (UnsupportedOperationException | SecurityException ignored) {
                }
            }
            new One().setVisible(true);
        } catch (JDOMException e) {
            Print(PROFILE_LOCATION + " is corrupt. Please check");
            Print(e.getMessage());
        } catch (IOException e) {
            Print("Could not read " + PROFILE_LOCATION);
            Print(" because " + e.getMessage());
        }
    }

    public static String BlastOption(String opt) {
        return BlastSettings.BlastOption(opt);
    }

    public static String reverseBlast(String file, boolean isPro) throws IOException {
        String outFile = file + "flip";
        int num = 0;
        try (BufferedReader first = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = first.readLine()) != null) {
                if (!line.startsWith("#")) {
                    num++;
                }
            }
        }
        String[] results = new String[num];
        try (BufferedReader first = new BufferedReader(new FileReader(file))) {
            String line;
            int gum = 0;
            while ((line = first.readLine()) != null) {
                if (!line.startsWith("#")) {
                    results[gum] = line;
                    gum++;
                }
            }
        }
        BlastComparator comp = new BlastComparator();
        Arrays.sort(results, comp);
        int multiFasta = 0;
        if (PROFILE.getRootElement().getAttributeValue("spacer") != null) {
            multiFasta++;
        }
        int[] existing = new int[GEN_LENGTH];
        try (BufferedWriter out = new BufferedWriter(new FileWriter(outFile))) {
            for (int i = 0; i < results.length; i++) {
                String[] tabarray = results[i].split("\t");
                String start = tabarray[8];
                String stop = tabarray[9];
                if (Integer.parseInt(tabarray[8]) > Integer.parseInt(tabarray[9])) {
                    start = tabarray[9];
                    stop = tabarray[8];
                }
                if (multiFasta != 0) {
                    try {
                        String[] bestarray = results[i].split("\t");
                        String[] commaarray = bestarray[1].split(":");
                        if (BRIG.PROFILE.getRootElement().getAttribute("genbankProtein") != null || isPro) {
                            start = Integer.toString(Integer.parseInt(commaarray[1]) + (Integer.parseInt(start) * 3));
                            stop = Integer.toString(Integer.parseInt(commaarray[1]) + (Integer.parseInt(stop) * 3));
                        } else {
                            start = Integer.toString(Integer.parseInt(commaarray[1]) + Integer.parseInt(start));
                            stop = Integer.toString(Integer.parseInt(commaarray[1]) + Integer.parseInt(stop));
                        }
                    } catch (Exception e) {
                        Print("Could't read: " + outFile);
                        Print("Could't read: " + results[i]);
                        log.error("Error reading reverse BLAST result", e);
                    }
                }
                try {
                    List re = existingResult(start, stop, existing);
                    existing = (int[]) re.get(1);
                    if (((Boolean) re.get(0))) {
                        out.write(results[i]);
                        out.newLine();
                    }
                } catch (Exception e) {
                    Print(results[i]);
                    log.error("Error processing existing result", e);
                }
            }
        }
        return outFile;
    }

    @SuppressWarnings("unchecked")
    public static int bundleSession(String folder){
        File fold = new File(folder);
        Console con = new Console();
        con.setVisible(true);
        con.updateProgress("Bundling session...\n");
        if (fold.isDirectory()) {
            try{
            // create archive structure graph & gen
            File genFold = new File(fold.getAbsoluteFile() + BRIG.SL + "gen");
            File graFold = new File(fold.getAbsoluteFile() + BRIG.SL + "gra");
            genFold.mkdirs();
            graFold.mkdirs();
            // copy main profile
            Document bundle = (Document) BRIG.PROFILE.clone();
            // set archive tag
            Element root = bundle.getRootElement();
            root.setAttribute("archive", "true");
            // alter file locations
            root.removeAttribute("queryFastaFile");
            // move reference file to folder
            if(root.getAttributeValue("queryFile") != null ){
                    File queryFile = new File(root.getAttributeValue("queryFile"));
                    if (root.getAttributeValue("queryFile") != null) {
                        con.updateProgress("Copying " + queryFile.getName());
                        copyTo(queryFile, new File(fold.getAbsolutePath() + BRIG.SL + queryFile.getName()), false);
                        root.setAttribute("queryFile", queryFile.getName());
                    }
                }
            // move query files to folder & rename in refDirs
            Element gradir = new Element("refDir");
            gradir.setAttribute("location", "gra");
            Element gendir = new Element("refDir");
            gendir.setAttribute("location", "gen");
             List<Element> dirs = root.getChildren("refDir");
            List<String[]> alteredName = new LinkedList<>();
            for(int i=0; i < dirs.size(); i++ ){
                List<Element> seq = dirs.get(i).getChildren();
                for (int k = 0; k < seq.size(); k++) {
                    Element currSeq = seq.get(k);
                    if( !"GC Skew".equals(currSeq.getAttributeValue("location")) && !"GC Content".equals(currSeq.getAttributeValue("location"))){
                    File qer = new File(currSeq.getAttributeValue("location"));
                    if(qer.isFile()){
                    con.updateProgress("Copying " + qer.getName());
                    // Check if file exists.
                    String name = qer.getName();
                    boolean exists = false;
                    List<Element> existCheck = new LinkedList<>();
                    if (qer.getName().endsWith(".graph")) {
                        existCheck = gradir.getChildren("refFile");
                    }else{
                        existCheck = gendir.getChildren("refFile");
                    }
                    for(int f=0;f<existCheck.size();f++){
                        String existingFile =  new File(existCheck.get(f).getAttributeValue("location")).getName();
                        if  (existingFile.equals(qer.getName())){
                            exists = true;
                            }
                        }
                        if (exists) {
                            String temp = con.fileExists(qer.getName());
                            if (temp != null && !temp.isEmpty()) {
                                String[] changed = {qer.getAbsolutePath(), temp};
                                alteredName.add(changed);
                                name = temp;
                            }
                        }
                        if (qer.getName().endsWith(".graph")) {
                            currSeq.setAttribute("location", "gra" + BRIG.SL + name);
                            gendir.addContent((Element) currSeq.clone());
                            copyTo(qer, new File(graFold.getAbsolutePath() + BRIG.SL + name), false);
                        } else {
                            currSeq.setAttribute("location", "gen" + BRIG.SL + name);
                            gendir.addContent((Element) currSeq.clone());
                            copyTo(qer, new File(genFold.getAbsolutePath() + BRIG.SL + name), false);
                        }
                    }
                }
                }
            }
            root.removeChildren("refDir");
            root.addContent(gradir);
            root.addContent(gendir);
            // Rename query file locations in rings
            List<Element> rings = root.getChildren("ring");
            for(int i=0; i < rings.size(); i++ ){
                List<Element> seq = rings.get(i).getChildren("sequence");
                for(int j=0; j < seq.size(); j++ ){
                    Element currSeq = seq.get(j);
                    currSeq.removeAttribute("blastResults");
                    String name = new File(currSeq.getAttributeValue("location")).getName();
                    for(int a=0;a<alteredName.size() ;a++){
                        if( alteredName.get(a)[0].equals(currSeq.getAttributeValue("location"))) {
                            name = alteredName.get(a)[1];
                        }
                    }
                    currSeq.setAttribute("location",  "gen" + BRIG.SL + name);
                }
            }
             //Copy outputfile if exists
            if ( root.getAttributeValue("outputFile") != null ) {
                File out =new File( root.getAttributeValue("outputFile")+".jpg" );
                if(out.exists()){
                    con.updateProgress("Copying " + out.getName());
                    copyTo( out, new File(fold.getAbsolutePath() + BRIG.SL + out.getName()), true);
                }
            }
            root.removeAttribute("outputFile");
            root.setAttribute("outputFolder","");
            root.removeAttribute("cgXML");
            // save xml to folder
                String xmlName = "brig-archive.xml";
                if (new File(fold.getAbsolutePath() + BRIG.SL + xmlName).exists()) {
                    String newName = con.fileExists(xmlName);
                    if (newName != null && !newName.isEmpty()) {
                        xmlName = newName;
                    }
                }
            BRIG.saveXML(fold.getAbsolutePath() + BRIG.SL + xmlName , bundle);
            con.updateProgress("Writing XML to " + fold.getAbsolutePath() + BRIG.SL + xmlName);
            }catch(Exception e ){
                con.updateProgress("SYS_ERROR: "+  e.getMessage() +"\n");
                log.error("Error bundling session", e);
                return -1;
            }
           con.updateProgress("Done.\n");
        }else{
            con.updateProgress("You have not specified a valid directory.\n");
        }


        return 0;
    }
    public static void copyTo(File inputFile, File outputFile, boolean binary) throws FileNotFoundException, IOException {
        FileUtils.copyTo(inputFile, outputFile, binary);
    }

    @SuppressWarnings("unchecked")
    public static Document FormatArchive(Document temp, String file) {
        Element root = temp.getRootElement();
        File base = new File(file);
        String dir = base.getParent() + BRIG.SL;
        root.setAttribute("outputFolder", dir);
        // Reconnect with sequences.
        List<Element> rings = root.getChildren("ring");
        for (int i = 0; i < rings.size(); i++) {
            List<Element> seq = rings.get(i).getChildren("sequence");
            for (int j = 0; j < seq.size(); j++) {
                Element currSeq = seq.get(j);
                if( !"GC Skew".equals(currSeq.getAttributeValue("location")) && !"GC Content".equals(currSeq.getAttributeValue("location")) ){
                currSeq.setAttribute("location", dir + currSeq.getAttributeValue("location"));
                }
            }
        }
        List<Element> dirs = root.getChildren("refDir");
        for (int i = 0; i < dirs.size(); i++) {
            Element currentDir = dirs.get(i);
            List<Element> seq = currentDir.getChildren();
            currentDir.setAttribute("location", dir + currentDir.getAttributeValue("location"));
            for (int k = 0; k < seq.size(); k++) {
                Element currSeq = seq.get(k);
                currSeq.setAttribute("location", dir + currSeq.getAttributeValue("location"));
            }
        }
        root.setAttribute("queryFile", dir + root.getAttributeValue("queryFile"));
        return temp;
    }

    public static boolean isBlastOk() throws IOException {
        return BlastSettings.isBlastOk();
    }

    public static Color FetchColor(int RingNumber) {
        return BlastSettings.FetchColor(RingNumber);
    }

    public static void dumpXMLDebug() {
        FileUtils.dumpXMLDebug();
    }

    public static boolean isProteinFASTA(String input) throws FileNotFoundException, IOException {
        return SequenceUtils.isProteinFASTA(input);
    }

    public static String formatMultiFASTA(String input, String output, int spacer, boolean label) throws FileNotFoundException, IOException {
        return SequenceUtils.formatMultiFASTA(input, output, spacer, label);
    }

    public static Document prepProfile(Document profil) {
        return BlastSettings.prepProfile(profil);
    }

    public static String ValidateSession() {
        return BlastSettings.ValidateSession();
    }


    public static int saveXML(String output, Document profil) {
        return FileUtils.saveXML(output, profil);
    }

    public static int dumpXML(String output, Document profil) {
        return FileUtils.dumpXML(output, profil);
    }

    public static int isMultiFasta(String file) throws FileNotFoundException, IOException {
        return SequenceUtils.isMultiFasta(file);
    }

    public static String FetchFilename(String filename) {
        return SequenceUtils.FetchFilename(filename);
    }

    public static int FastaLength(String file, boolean multi) throws FileNotFoundException, IOException {
        return SequenceUtils.FastaLength(file, multi);
    }

    public static int checkComment(String file) throws FileNotFoundException, IOException {
        return SequenceUtils.checkComment(file);
    }

    public static String WordWrap(String terg) {
        return SequenceUtils.WordWrap(terg);
    }

    public static String FormatGenbank(String file, String header, String out, boolean embl, String pOption) throws FileNotFoundException, IOException {
        return SequenceUtils.FormatGenbank(file, header, out, embl, pOption);
    }

    public static String fastaSubsequence(String file, int start, int stop) {
        return SequenceUtils.fastaSubsequence(file, start, stop);
    }

    @SuppressWarnings("unchecked")
    public static String WriteXMLLegend() {
        String error = "";
        try {
            Element root = BRIG.PROFILE.getRootElement();
            String output = root.getAttributeValue("outputFolder");
            String query = root.getAttributeValue("queryFile");
            root.setAttribute("cgXML", output + SL + "scratch" + SL + FetchFilename(query) + ".xml");
            String cgXML = root.getAttributeValue("cgXML");
            String TITLE = root.getAttributeValue("title");
            Element cgSettings = root.getChild("cgview_settings");
            List<Attribute> settings = cgSettings.getAttributes();
            try (BufferedWriter out = new BufferedWriter(new FileWriter(cgXML))) {
            out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><cgview ");
            String[] doops = cgSettings.getAttributeValue("warningFont").toString().split(",");
            int LEGEND_SIZE = 15;
            try {
                LEGEND_SIZE = Integer.parseInt(doops[2]);
            } catch (NumberFormatException n) {
                error += "Bad legend font-size, setting to default\n";
                LEGEND_SIZE = 15;
            }
            for (Attribute setting : settings) {
                String badstring = setting.toString() + " ";
                badstring = badstring.replaceAll("^\\[Attribute: ", "");
                badstring = badstring.replaceAll("\\]", "");
                out.write(badstring);
            }
            List<Element> rings = PROFILE.getRootElement().getChildren("ring");
            out.write(" sequenceLength=\"" + GEN_LENGTH + "\" title=\"" + TITLE + "\">");
            out.newLine();
            String def = "upper-right";
            if (BRIG.PROFILE.getRootElement().getAttributeValue("legendPosition") != null) {
                def = BRIG.PROFILE.getRootElement().getAttributeValue("legendPosition");
            }
            if (!"null".equals(cgSettings.getAttributeValue("warningFont")) && !cgSettings.getAttributeValue("warningFont").isEmpty()) {
                out.write("<legend position=\"" + def + "\" font=\"" + cgSettings.getAttributeValue("warningFont") + "\" backgroundOpacity=\"0.8\">");
            } else {
                out.write("<legend position=\"" + def + "\" backgroundOpacity=\"0.8\">");
            }
            out.newLine();
            int i = 0;
            int stop = 1;
            while (stop != 0) {
                stop = 0;
                for (int a = 0; a < rings.size(); a++) {
                    Element currentRing = rings.get(a);
                    if (Integer.parseInt(currentRing.getAttributeValue("position")) == i) {
                        //FOR EACH SEQUENCE
                        List<Element> sequence = currentRing.getChildren("sequence");
                        if (!"null".equals(currentRing.getAttributeValue("name")) && !currentRing.getAttributeValue("name").isEmpty()) {
                            if (sequence.size() > 0) {
                                Element currentSeq = sequence.get(0);
                                //CHECK IF GRAPH
                                if (currentSeq.getAttributeValue("location").endsWith(".graph")) {
                                    String RGB = "rgb(" + currentRing.getAttributeValue("colour") + ")";
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name")
                                            + "\" drawSwatch=\"true\" swatchColor=\"" + RGB + "\" />");
                                    out.newLine();
                                } else if ("GC Skew".equals(currentSeq.getAttributeValue("location"))) {
                                    //CHECK IF GC SKEW
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "\" />");
                                    out.newLine();
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "(-)\" font=\"SanSerif,plain,"
                                            + (int) (LEGEND_SIZE * 0.75) + "\" drawSwatch=\"true\" swatchColor=\"rgb(152,0,152)\" />");
                                    out.newLine();
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "(+)\" font=\"SanSerif,plain,"
                                            + (int) (LEGEND_SIZE * 0.75) + "\" drawSwatch=\"true\" swatchColor=\"rgb(0,152,0)\" />");
                                    out.newLine();
                                } else if ("GC Content".equals(currentSeq.getAttributeValue("location"))) {
                                    //CHECK IF GC CONTENT
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "\" font=\"SanSerif,plain,"
                                            + (int) (LEGEND_SIZE) + "\" drawSwatch=\"true\" swatchColor=\"rgb(0,0,0)\" />");
                                    out.newLine();
                                    error += "Added " + currentRing.getAttributeValue("name") + " to Legend\n";
                                } else {
                                    String[] col = currentRing.getAttributeValue("colour").split(",");
                                    Color add = Color.RED;
                                    try {
                                        add = new Color(Integer.parseInt(col[0]), Integer.parseInt(col[1]), Integer.parseInt(col[2]));
                                    } catch (NumberFormatException n) {
                                        error += "Colour value, setting to red\n";
                                        add = Color.RED;
                                    }
                                    if ("yes".equals(currentRing.getAttributeValue("legend"))) {
                                        out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "\" />");
                                    } else {
                                        String RGB = "rgb(" + currentRing.getAttributeValue("colour") + ")";
                                        out.write("<legendItem drawSwatch=\"true\" text=\"" + currentRing.getAttributeValue("name") + "\" swatchColor=\"" + RGB + "\" />");
                                    }
                                    out.newLine();
                                    if ("yes".equals(currentRing.getAttributeValue("legend"))) {
                                        Vector<Integer> k = new Vector<Integer>();
                                        int uperInt = 0;
                                        int lowInt = 0;
                                        try {
                                            uperInt = Integer.parseInt(currentRing.getAttributeValue("upperInt"));
                                            lowInt = Integer.parseInt(currentRing.getAttributeValue("lowerInt"));
                                        } catch (NumberFormatException n) {
                                            error += "Bad threshold size, setting to default\n";
                                            uperInt = 90;
                                            lowInt = 70;
                                        }
                                        k.add(100);
                                        k.add(uperInt);
                                        k.add(lowInt);
                                        for (int z = 0; z < k.size(); z++) {
                                            int red = OutputColour(lowInt, add.getRed(), k.get(z));
                                            int green = OutputColour(lowInt, add.getGreen(), k.get(z));
                                            int blue = OutputColour(lowInt, add.getBlue(), k.get(z));
                                            String RGB = "rgb(" + red + "," + green + "," + blue + ")";
                                            if (!"null".equals(cgSettings.getAttributeValue("warningFont"))
                                                    && !cgSettings.getAttributeValue("warningFont").isEmpty()) {
                                                String[] to = cgSettings.getAttributeValue("warningFont").split(",");
                                                out.write("<legendItem text=\"" + k.get(z) + "% identity" + "\" font=\"" + to[0] + "," + to[1] + ","
                                                        + (int) (LEGEND_SIZE * 0.75) + "\" drawSwatch=\"true\" swatchColor=\"" + RGB + "\" />");
                                            } else {
                                                error += "Legend font not specified, ignoring...\n";
                                            }
                                            out.newLine();
                                        }
                                    }
                                }
                                //ELSE IF SEQUENCE  = 0
                            } else if (sequence.isEmpty()) {
                                String RGB = "rgb(" + currentRing.getAttributeValue("colour") + ")";
                                out.write("<legendItem text=\"" + currentRing.getAttributeValue("name")
                                        + "\" drawSwatch=\"true\" swatchColor=\"" + RGB + "\" />");
                                error += "Added " + currentRing.getAttributeValue("name") + " to Legend\n";
                            }
                        }
                        stop++;
                        i++;
                    }
                }
            }
            out.write("</legend>");
            out.newLine();
            out.flush();
            }
        } catch (Exception e) {
            error += e.getMessage();
        }
        return error;
    }

    public static boolean isGenbank(String file) {
        return SequenceUtils.isGenbank(file);
    }

    public static boolean isEmbl(String file) {
        return SequenceUtils.isEmbl(file);
    }

    @SuppressWarnings("unchecked")
    public static String RunBlast(int clean) {
        List<Element> ringList = PROFILE.getRootElement().getChildren("ring");
        String query = PROFILE.getRootElement().getAttributeValue("queryFile");
        String queryFastaFile = PROFILE.getRootElement().getAttributeValue("queryFastaFile");
        String output = PROFILE.getRootElement().getAttributeValue("outputFolder");
        String blastOptions = PROFILE.getRootElement().getAttributeValue("blastOptions");
        String blastLocation = "";
        if (PROFILE.getRootElement().getChild("brig_settings") != null) {
            if (PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation") != null) {
                blastLocation = PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation");
                if (!(PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation").endsWith(SL))) {
                    if (!PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation").isEmpty()) {
                        blastLocation += SL;
                    }
                }
            }
        }
        Print( "Reference sequence length: " + NumberFormat.getInstance().format(BRIG.GEN_LENGTH) + " bp");
        for (int i = 0; i < ringList.size(); i++) {
            Element currentRing = ringList.get(i);
            String ringName = currentRing.getAttributeValue("name");
            if (ringName == null || ringName.isEmpty()) ringName = "Ring " + i;
            List<Element> sequence = currentRing.getChildren("sequence");
            if (sequence.size() > 0) {
                Element firstElement = sequence.get(0);
                String loc = firstElement.getAttributeValue("location");
                if ("GC Skew".equals(loc) || "GC Content".equals(loc)) {
                    Print("[Ring " + i + "] " + ringName + " (computed)");
                } else if (loc.endsWith(".graph")) {
                    Print("[Ring " + i + "] " + ringName + " (graph: " + new File(loc).getName() + ")");
                }
                if (!"GC Skew".equals(loc)
                        && !"GC Content".equals(loc)
                        && !loc.endsWith(".graph")) {
                    String blastType = currentRing.getAttributeValue("blastType");
                    for (int j = 0; j < sequence.size(); j++) {
                        String ringFile = sequence.get(j).getAttributeValue("location").toString();
                        Print("[Ring " + i + "] " + ringName + " - " + blastType + ": " + new File(ringFile).getName());
                        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
                            ringFile = ringFile.replaceAll(SL + SL, SL);
                        }
                        String goodRingFile = ringFile;
                        String pOption = "F";
                        String opt = "nucl";
                        File dbOut = new File(queryFastaFile + ".nin");
                        if ("blastp".equals(blastType) || "blastx".equals(blastType)) {
                            dbOut = new File(queryFastaFile + ".pin");
                            pOption = "T";
                            opt = "prot";
                        }
                        if (BRIG.isEmbl(ringFile) || BRIG.isGenbank(ringFile)) {
                            boolean embl = false;
                            if (BRIG.isEmbl(ringFile)) {
                                embl = true;
                            }
                            //Create FASTA files for GENBANK-EMBL queries
                            String filename = FetchFilename(ringFile);
                            try {
                                String op = "F";
                                if ("blastp".equals(blastType) || "tblastn".equals(blastType)) {
                                    op = "T";
                                }
                                Print( BRIG.FormatGenbank(ringFile,
                                        filename, output + BRIG.SL + "scratch" + BRIG.SL + filename, embl, op));
                            } catch (Exception e) {
                                log.error("Error formatting GenBank file: {}", ringFile, e);
                            }
                            goodRingFile = output + BRIG.SL + "scratch" + BRIG.SL + filename + ".fna";
                            if ("blastp".equals(blastType) || "tblastn".equals(blastType)) {
                                goodRingFile = output + BRIG.SL + "scratch" + BRIG.SL + filename + ".faa";
                            }
                        }
                        List<String> dbCmd = new ArrayList<>();
                        dbCmd.addAll(Arrays.asList(blastLocation + "makeblastdb", "-dbtype", opt, "-in", queryFastaFile));
                        if (clean == 1 || !dbOut.exists()) {
                                try {
                                    Print("  Creating BLAST database...");
                                    log.debug(formatCommand(dbCmd));
                                    Process p = execCommand(dbCmd);
                                    String ergo = "";
                                    try (BufferedReader buffrdr = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                                        String data;
                                        while ((data = buffrdr.readLine()) != null) {
                                            ergo += data;
                                            Print("  " + data);
                                        }
                                    }
                                    if (ergo.length() < 3) {
                                    Print("  Database created.");
                                    }
                                } catch (Exception e) {
                                    Print("SYS_ERROR: Could not execute: " + formatCommand(dbCmd) );
                                    return "";
                                }

                        } else {
                            Print("  Database exists, reusing cached.");
                        }
                        String ou = output + SL + "scratch" + SL + FetchFilename(goodRingFile) + "Vs" + FetchFilename(queryFastaFile) + ".tab";
                        List<String> blastCmd = new ArrayList<>();
                        blastCmd.addAll(Arrays.asList(blastLocation + blastType, "-outfmt", "6", "-query", goodRingFile, "-db", queryFastaFile, "-out", ou));
                        blastCmd.addAll(tokenizeOptions(blastOptions));
                        if (!blastOptions.contains("-task")) {
                            if ("blastp".equals(blastType) || "blastn".equals(blastType)) {
                                blastCmd.addAll(Arrays.asList("-task", blastType));
                            }
                        }
                        sequence.get(j).setAttribute("blastResults", ou);
                        File blOut = new File(ou);
                        log.debug("Output: {}", ou);
                        try {
                            if (clean == 1 || !blOut.exists()) {
                                Print("  Running " + blastType + "...");
                                log.debug(formatCommand(blastCmd));
                                Process q = execCommand(blastCmd);
                                String ergo = "";
                                try (BufferedReader buffrdr = new BufferedReader(new InputStreamReader(q.getErrorStream()))) {
                                    String data;
                                    while ((data = buffrdr.readLine()) != null) {
                                        ergo += data;
                                        Print("  " + data);
                                    }
                                }
                                if (ergo.length() < 3) {
                                    Print("  Done.");
                                }
                                q.destroy();
                            } else {
                                Print("  Results cached, skipping.");
                            }
                        } catch (Exception e) {
                            Print("SYS_ERROR: Could not execute: " + formatCommand(blastCmd) + "\n"
                                    + e.getMessage() );
                            return "";
                        }
                    }
                }
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public static String ParseBlast() throws IOException {
        String error = "";
        String cgXML = PROFILE.getRootElement().getAttributeValue("cgXML");
        String queryFile = PROFILE.getRootElement().getAttributeValue("queryFile");
        String output = PROFILE.getRootElement().getAttributeValue("outputFolder");
        BRIG.dumpXMLDebug();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(cgXML, true))) {
            int GRAPH_MULTIPLIER = 3;
            if (PROFILE.getRootElement().getChild("brig_settings") != null) {
                if (PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("multiplier") != null) {
                    GRAPH_MULTIPLIER = Integer.parseInt(PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("multiplier"));
                }
            }
            List<Element> rings = BRIG.PROFILE.getRootElement().getChildren("ring");
            int i = 0;
            int stop = 1;
            String queryFastaFile = PROFILE.getRootElement().getAttributeValue("queryFastaFile");
            while (stop != 0) {
                stop = 0;
                for (int a = 0; a < rings.size(); a++) {
                    Element currentRing = rings.get(a);
                    if (Integer.parseInt(currentRing.getAttributeValue("position")) == i) {
                        List<Element> sequence = currentRing.getChildren("sequence");
                        List<Element> custom = currentRing.getChildren("feature");
                        if (sequence.size() > 0 || custom.size() > 0) {
                            String parseName = currentRing.getAttributeValue("name");
                            if (parseName == null || parseName.isEmpty()) parseName = "Ring " + currentRing.getAttributeValue("position");
                            String detail = "";
                            if (custom.size() > 0) detail += custom.size() + " features";
                            if (sequence.size() > 0) {
                                if (!detail.isEmpty()) detail += ", ";
                                detail += sequence.size() + " sequence(s)";
                            }
                            Print("[Ring " + currentRing.getAttributeValue("position") + "] Parsing " + parseName + " (" + detail + ")");
                            int size = 30;
                            if ( currentRing.getAttributeValue("size") != null ){
                                size = Integer.parseInt(currentRing.getAttributeValue("size"));
                            }
                            String location = "";
                            if (sequence.size() > 0) {
                                Element currentSeq = sequence.get(0);
                                if (currentSeq.getAttributeValue("location") != null) {
                                    location = currentSeq.getAttributeValue("location");
                                    if ("GC Content".equals(location) || "GC Skew".equals(location) || location.endsWith(".graph")) {
                                        out.write("<featureSlot strand=\"direct\" featureThickness=\"" + (size * GRAPH_MULTIPLIER) + "\">");
                                        out.newLine();
                                    } else {
                                        out.write("<featureSlot strand=\"direct\" featureThickness=\"" + size + "\">");
                                        out.newLine();
                                    }
                                }
                            } else if (custom.size() > 0 ) {
                                out.write("<featureSlot strand=\"direct\" featureThickness=\"" + size + "\">");
                                out.newLine();
                            }
                            if (custom.size() > 0) {
                                for (int h = 0; h < custom.size(); h++) {
                                    try {
                                        Element customElement = custom.get(h);
                                        List<Element> ranger = customElement.getChildren("featureRange");
                                        out.write("<feature color=\"" + customElement.getAttributeValue("colour") + "\" ");
                                        out.write(" decoration=\"" + customElement.getAttributeValue("decoration") + "\" ");
                                        if (customElement.getAttributeValue("label") != null && !customElement.getAttributeValue("label").isEmpty()) {
                                            out.write(" label= \"" + customElement.getAttributeValue("label") + "\" ");
                                        }
                                        out.write(">");
                                        out.newLine();
                                        for (int z = 0; z < ranger.size(); z++) {
                                            Element rangerElement = ranger.get(z);
                                            out.write("<featureRange start=\"" + rangerElement.getAttributeValue("start") + "\" stop=\"" + rangerElement.getAttributeValue("stop") + "\" />");
                                            out.newLine();
                                        }
                                        out.write("</feature>");
                                        out.newLine();
                                    } catch (Exception e) {
                                        log.error("Error processing custom feature", e);
                                    }
                                }
                            }
                            int other = 0;
                            for (int k = 0; k < sequence.size(); k++) {
                                Element currentSeq = sequence.get(k);
                                if ("GC Content".equals(currentSeq.getAttributeValue("location"))) {
                                    if(isEmbl(queryFile) || isGenbank(queryFile)){
                                        CGContent(out, output + BRIG.SL + "scratch" + BRIG.SL + BRIG.FetchFilename(queryFile) +".fna");
                                    }else{
                                        CGContent(out, queryFastaFile);
                                    }
                                    other++;
                                } else if ("GC Skew".equals(currentSeq.getAttributeValue("location"))) {
                                    if(isEmbl(queryFile) || isGenbank(queryFile)){
                                        CGskew(out, output + BRIG.SL + "scratch" + BRIG.SL + BRIG.FetchFilename(queryFile) +".fna");
                                    }else{
                                        CGskew(out, queryFastaFile);
                                    }
                                    other++;
                                } else if (currentSeq.getAttributeValue("location").endsWith(".graph")) {
                                    error += GraphIt(currentRing, out);
                                    other++;
                                }
                            }
                            if (other == 0) {
                                if (sequence.size() > 0) {
                                    Print("  Reading BLAST results...");
                                    BlastMaster(currentRing, out);
                                }
                            }
                            if (custom.size() > 0  || sequence.size() >0){
                                out.write("</featureSlot>");
                            }
                            out.flush();
                        }
                        stop++;
                        i++;
                    }
                }
            }
            out.write("</cgview>");
        } catch (IOException ex) {
            error += ("SYS_ERROR: Could not open output file: "
                    + cgXML + ".\n"
                    + ex.getMessage() + "\n");
            return error;
        }
        return error;
    }

    public static String RunCGview(String imgFormat ) {
        String cgXML = BRIG.PROFILE.getRootElement().getAttributeValue("cgXML");
        String outputFile = PROFILE.getRootElement().getAttributeValue("outputFile");
        String outPath = outputFile + "." + imgFormat;
        try {
            Print("Rendering " + imgFormat.toUpperCase() + " via CGView API...");
            CgviewFactory factory = new CgviewFactory();
            Cgview cgview = factory.createCgviewFromFile(cgXML);
            switch (imgFormat.toLowerCase()) {
                case "png":
                    CgviewIO.writeToPNGFile(cgview, outPath);
                    break;
                case "jpg":
                    CgviewIO.writeToJPGFile(cgview, outPath);
                    break;
                case "svg":
                    CgviewIO.writeToSVGFile(cgview, outPath, false);
                    break;
                case "svgz":
                    CgviewIO.writeToSVGFile(cgview, outPath, true);
                    break;
                default:
                    Print("SYS_ERROR: Unknown image format: " + imgFormat);
                    return "";
            }
        } catch (Exception ex) {
            Print("SYS_ERROR: CGView rendering failed: " + ex.getMessage());
            log.error("CGView rendering failed", ex);
            return "";
        }
        Print("Done.");
        return "";
    }

    public static int OutputColour(int lowerInt, int base, int identity) {
        int y = 0;
        y = 225 + (identity - lowerInt) * ((base - 225) / (100 - lowerInt));
        if (y > 225) {
            y = 225;
        }
        return y;
    }

    @SuppressWarnings("unchecked")
    private static String BlastMaster(Element currentRing, BufferedWriter out) throws IOException {
        String error = "";
        try {
            int multiFasta = 0;
            if (PROFILE.getRootElement().getAttributeValue("spacer") != null) {
                multiFasta++;
            }
            int minimus = 0;
            try {
                Element settings = PROFILE.getRootElement().getChild("brig_settings");
                if (settings != null) {
                    minimus = Integer.parseInt(settings.getAttributeValue("defaultMinimum"));
                }
            } catch (Exception e) {
                minimus = 0;
            }
            String outLine = "";
            String line = "";
            boolean isPro = BRIG.isProteinFASTA(PROFILE.getRootElement().getAttributeValue("queryFastaFile"));
            List<Element> sequence = currentRing.getChildren("sequence");
            // Create new blast results file. merging all ring results
            String tput = PROFILE.getRootElement().getAttributeValue("outputFolder");
            String mergedResults = tput + SL + "scratch" + SL + "Ring"+currentRing.getAttributeValue("position") + "merge";
            File merge = new File(mergedResults);
            merge.deleteOnExit();
            try (BufferedWriter mer = new BufferedWriter(new FileWriter(mergedResults, true))) {
                for (int z = 0; z < sequence.size(); z++) {
                    String blastResults = sequence.get(z).getAttributeValue("blastResults");
                    log.debug("Reading sequence: {} ({})", blastResults, z);
                    try (BufferedReader resul = new BufferedReader(new FileReader(blastResults.trim()))) {
                        while ((line = resul.readLine()) != null) {
                            mer.write(line);
                            mer.newLine();
                        }
                    } catch (FileNotFoundException ex) {
                        error += "SYS_ERROR: Could not open file: "
                                + mergedResults.trim() + "\n"
                                + ex.getMessage() + "\n";
                        return error;
                    }
                }
            }
            mergedResults = BRIG.reverseBlast(mergedResults, isPro);
            String[] col = currentRing.getAttributeValue("colour").split(",");
            int LOWER_INT = 50;
            if (!currentRing.getAttributeValue("lowerInt").isEmpty() && currentRing.getAttributeValue("lowerInt") != null) {
                LOWER_INT = Integer.parseInt(currentRing.getAttributeValue("lowerInt"));
            }
            Color current = new Color(Integer.parseInt(col[0]), Integer.parseInt(col[1]), Integer.parseInt(col[2]));
            /* Read outputfile, parse so that identity influences base-colour, start, stop*/
            try (BufferedReader first = new BufferedReader(new FileReader(mergedResults.trim()))) {
            while ((line = first.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String start = "";
                    String stop = "";
                    String[] tabarray = line.split("\t");
                    int identity = (int) (Double.parseDouble(tabarray[2]));
                    if (identity >= minimus) {
                        start = tabarray[8];
                        stop = tabarray[9];
                        if (Integer.parseInt(tabarray[8]) > Integer.parseInt(tabarray[9])) {
                            start = tabarray[9];
                            stop = tabarray[8];
                        }
                        if (multiFasta != 0) {
                            try {
                                String[] bestarray = line.split("\t");
                                String[] commaarray = bestarray[1].split(":");
                                if (BRIG.PROFILE.getRootElement().getAttribute("genbankProtein") != null || isPro) {
                                    start = Integer.toString(Integer.parseInt(commaarray[1]) + (Integer.parseInt(start) * 3));
                                    stop = Integer.toString(Integer.parseInt(commaarray[1]) + (Integer.parseInt(stop) * 3));
                                } else {
                                    start = Integer.toString(Integer.parseInt(commaarray[1]) + Integer.parseInt(start));
                                    stop = Integer.toString(Integer.parseInt(commaarray[1]) + Integer.parseInt(stop));
                                }
                            } catch (Exception e) {
                                Print("Could't read: " + line);
                                log.error("Error reading multi-FASTA blast result", e);
                            }
                        }
                        int red = OutputColour(LOWER_INT, current.getRed(), identity);
                        int green = OutputColour(LOWER_INT, current.getGreen(), identity);
                        int blue = OutputColour(LOWER_INT, current.getBlue(), identity);
                        String RGB = "rgb(" + red + "," + green + "," + blue + ")";
                        if ("yes".equals(currentRing.getAttributeValue("labels"))) {
                            outLine = "<feature color=\"" + RGB + "\" decoration=\"arc\" >"
                                    + "<featureRange start=\"" + start + "\" stop=\"" + stop + "\" label=\""
                                    + tabarray[2] + "\" /></feature>";
                        } else {
                            outLine = "<feature color=\"" + RGB + "\" decoration=\"arc\" >"
                                    + "<featureRange start=\"" + start + "\" stop=\"" + stop + "\" /></feature>";
                        }
                        out.write(outLine);
                        out.newLine();
                    }
                }
            }
            }
            merge.delete();
        } catch (Exception e) {
            log.error("Error in BlastMaster processing", e);
        }

        return error;
    }

    public static List existingResult(String instart, String instop, int[] existing) throws ArrayIndexOutOfBoundsException{
        List<Object> out = new LinkedList<>();
        int start= Integer.parseInt(instart);
        int stop = Integer.parseInt(instop);
        if(start >= GEN_LENGTH){
            start = GEN_LENGTH-1;
        }
        if(stop >= GEN_LENGTH){
            stop = GEN_LENGTH-1;
        }
        // Check for length of sequence across existing that all position are filled
        boolean draw = false;
        for(int i = start; i <= stop; i++){
            if(existing[i] == 0 ){
                draw = true;
            }
            existing[i]++;
        }

        out.add(draw);
        out.add(existing);
        return out;
    }

    private static String GraphIt(Element currentRing, BufferedWriter out) throws IOException {
        int FEATURE_THICKNESS = 40;
        if (BRIG.PROFILE.getRootElement().getAttributeValue("featureThickness") != null) {
            FEATURE_THICKNESS = Integer.parseInt(BRIG.PROFILE.getRootElement().getAttributeValue("featureThickness"));
        }
        String location = currentRing.getChild("sequence").getAttributeValue("location");
        String label = currentRing.getAttributeValue("labels");
        String error = "";
        Print("  Graphing: " + new File(location).getName());
        int GRAPH_DIVIDER = 3;
        int custom = -1;
        if (PROFILE.getRootElement().getChild("brig_settings") != null) {
            if (PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("divider") != null) {
                GRAPH_DIVIDER = Integer.parseInt(PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("divider"));
            }
        }
        if (currentRing.getAttributeValue("custom_graph") != null) {
            try {
                custom = Integer.parseInt(currentRing.getAttributeValue("custom_graph"));
            } catch (Exception e) {
                custom = -1;
            }
        }
        String colour = currentRing.getAttributeValue("colour");
        out.write("<feature color=\"rgb(" + colour + ")\" decoration=\"arc\">\n");
        // CHECK IF NOT -- OUTPUT
        log.debug("Parsing graph file {}", location);
        String col = currentRing.getAttributeValue("colour");
        /* your list of numbers: 1, 3, 4, 6, 9, 19
        mean: (1+3+4+6+9+19) / 6 = 42 / 6 = 7
        list of deviations: -6, -4, -3, -1, 2, 12
        squares of deviations: 36, 16, 9, 1, 4, 144
        sum of deviations: 36+16+9+1+4+144 = 210
        divided by one less than the number of items in the list: 210 / 5 = 42
        square root of this number: square root (42) = about 6.48*/
        Vector<Double> values = new Vector<Double>();
        Vector<Integer> start = new Vector<Integer>();
        Vector<Integer> stop = new Vector<Integer>();
        double mean = 0.0;
        double standardDeviation = 0.0;
        double max = 0.0;
        double min = 0.0;
        double maxDeviation = 0.0;
        String line;
        int lineNum = 1;
        try (BufferedReader first = new BufferedReader(new FileReader(location.trim()))) {
        while ((line = first.readLine()) != null) {
            try {
                if (!line.startsWith("#")) {
                    String[] tabarray = line.split("\\s+");
                    double temp = 0.0;
                    if (tabarray.length == 3) {
                        temp = Double.parseDouble(tabarray[2]);
                    } else if (tabarray.length == 1) {
                        try {
                            temp = Double.parseDouble(tabarray[0]);
                        } catch (NumberFormatException n) {
                            temp = 0.0;
                        }
                    }
                    if (custom == -1) {
                        // HANDLE DIFFERENT GRAPH MODES HERE
                        if (tabarray.length == 3) {
                            start.add(Integer.parseInt(tabarray[0]));
                            stop.add(Integer.parseInt(tabarray[1]));
                        } else if (tabarray.length == 1) {
                            start.add(lineNum);
                            stop.add(lineNum + 1);
                        }
                        values.add(temp);
                        mean += temp;
                        if (temp > max) {
                            max = temp;
                        }
                        if (temp < min) {
                            min = temp;
                        }
                    } else {
                        int starte = 0;
                        int stope = 0;
                        if (tabarray.length == 3) {
                            starte = Integer.parseInt(tabarray[0]);
                            stope = Integer.parseInt(tabarray[1]);
                        } else if (tabarray.length == 1) {
                            starte = lineNum;
                            stope = lineNum + 1;
                        }
                        if (starte == 0) {
                            starte = 1;
                        }
                        if ((temp / custom) >= 1) {
                            out.write("<featureRange color=\"blue\" start=\"" + starte + "\" stop=\"" + stope + "\" "
                                    + "proportionOfThickness=\"1\" />");
                        } else if ((temp / custom) <= 0) {
                            out.write("<featureRange color=\"red\" start=\"" + starte + "\" stop=\"" + stope + "\" "
                                    + "proportionOfThickness=\"0\" />");
                        } else {
                            out.write("<featureRange color=\"rgb(" + col + ")\" start=\"" + starte + "\" stop=\"" + stope + "\" "
                                    + "proportionOfThickness=\"" + (temp / custom) + "\" />");
                        }
                        out.newLine();

                    }
                    lineNum++;
                }
            } catch (Exception e) {
                log.error("Error processing graph line: {}", line, e);
            }
        }

        //IF CUSTOM VALUES HIDE THIS ALL AWAY
        if (custom == -1) {
            mean = mean / (double) values.size();
            if ((max - mean) > (mean - min)) {
                maxDeviation = max - mean;
            } else {
                maxDeviation = mean - min;
            }
            for (int j = 0; j < values.size(); j++) {
                standardDeviation += Math.pow(mean - values.get(j), 2);
            }
            standardDeviation = Math.sqrt(standardDeviation / (double) (values.size() - 1));
            double radiusShift = 0.0;
            double barHeight = 0.0;
            error += "Standard Deviation: " + standardDeviation + "\n";
            error += "Mean value: " + mean + "\n";
            error += "Max deviation: " + maxDeviation + "\n";
            for (int j = 0; j < values.size(); j++) {
                double skew = values.get(j);
                if (skew > mean) {
                    barHeight = skew - mean;
                    barHeight = barHeight * 0.5 / standardDeviation;
                    radiusShift = 0.5 + barHeight / 2.0;
                } else if (skew < mean) {
                    barHeight = mean - skew;
                    barHeight = barHeight * 0.5 / standardDeviation;
                    radiusShift = 0.5 - barHeight / 2.0;
                } else {
                    radiusShift = 0.5;
                }
                int starte = start.get(j);
                if (starte == 0) {
                    starte = 1;
                }
                int stope = stop.get(j);
                if (stope > (GEN_LENGTH - 1)) {
                    stope = GEN_LENGTH - 1;
                }
                if (barHeight > 1 && radiusShift < 0) {
                    //         Print( skew + "\t" +"\t" +barHeight +"\t" +radiusShift  +"\tSmall");
                    out.write("<featureRange color=\"red\" start=\"" + starte + "\" stop=\"" + stope + "\" "
                            + "proportionOfThickness=\"0.5\" radiusAdjustment=\"0.0\" />");
                } else if (barHeight > 1) {
                    //         Print(mean + "\t" + skew + "\t" +standardDeviation +"\t" +barHeight +"\t" +radiusShift +"\tBIG");
                    //        Print("<featureRange color=\"blue\" start=\"" + starte + "\" stop=\"" + stope + "\" "
                    //              + "proportionOfThickness=\"0.5\" radiusAdjustment=\"1.0\" />\t" + barHeight);
                    out.write("<featureRange color=\"blue\" start=\"" + starte + "\" stop=\"" + stope + "\" "
                            + "proportionOfThickness=\"0.5\" radiusAdjustment=\"1.0\" />");
                } else {
                    //      Print(mean + "\t" + skew + "\t" +standardDeviation +"\t" +barHeight+"\t" +radiusShift  );
                    out.write("<featureRange color=\"rgb(" + col + ")\" start=\"" + starte + "\" stop=\"" + stope + "\" "
                            + "proportionOfThickness=\"" + barHeight + "\" radiusAdjustment=\"" + radiusShift + "\" />");
                }
                out.newLine();
            }
        }
        //STOP HIDING
        out.write("</feature>\n");
        }
        if ("yes".equals(currentRing.getAttributeValue("legend"))) {
            String[] doop = new String[10];
            try {
                out.write("</featureSlot>\n");
                for (int j = 0; j < doop.length; j++) {
                    doop[j] = "-1";
                }
                int count = 0;
                try (BufferedReader first2 = new BufferedReader(new FileReader(location.trim()))) {
                    while ((line = first2.readLine()) != null) {
                        if (line.contains("#")) {
                            if (count >= doop.length) {
                                String[] extended = new String[doop.length + 10];
                                for (int j = 0; j < extended.length; j++) {
                                    extended[j] = "-1";
                                }
                                System.arraycopy(doop, 0, extended, 0, doop.length);
                                doop = extended;
                            }
                            doop[count] = line;
                            count++;
                        }
                    }
                }
                Comparator<String> comp = new RedComparator();
                Arrays.sort(doop, comp);
                out.write("<featureSlot strand=\"direct\" featureThickness=\"" + (FEATURE_THICKNESS / GRAPH_DIVIDER) + "\">\n");
                out.write("<feature color=\"black\" decoration=\"arc\">\n");
                for (int j = 0; j < doop.length; j++) {
                    String outLine = "";
                    if (!"-1".equals(doop[j])) {
                        String poop = doop[j].replaceAll("#", "");
                        String[] tabarray = poop.split("\\s+");
                        if (j % 2 == 0 ){
                            outLine = "<featureRange color=\"red\" start=\"" + tabarray[0] + "\" stop = \"" + tabarray[1] + "\" ";
                        }else{
                            outLine = "<featureRange color=\"blue\" start=\"" + tabarray[0] + "\" stop = \"" + tabarray[1] + "\" ";
                        }
                            if (label != null){
                                if("yes".equals(label)){
                                    outLine += " label= \"" + tabarray[2] + "\" ";
                                }
                            }
                        out.write(outLine + "/>\n");
                        out.newLine();
                    }
                }
                out.write("</feature>\n");
            } catch (Exception e) {
                log.error("Error processing graph labels", e);
            }
        }
        return error;
    }

    private static String CGContent(BufferedWriter out, String QUERY_MASTER_FILE) throws IOException {
        int div = autoScale(GEN_LENGTH);
        String error = "";
        int len = 0;
        int cPlusG = 0;
        int totalLen = 0;
        double average = 0.0;
        double maxDeviation = 0.0;
        double min = 10.0;
        double max = 0.0;
        int totalWindow = 0;
        String color = "";
        String line = "";
        out.write("<feature decoration=\"arc\" opacity = \"1.0\">");
        out.newLine();
        try (BufferedReader first = new BufferedReader(new FileReader(QUERY_MASTER_FILE))) {
            while ((line = first.readLine()) != null) {
                if (!line.contains(">")) {
                    for (int f = 0; f < line.length(); f++) {
                        if (len >= div) {
                            double skew = (double) cPlusG / (double) len;
                            skew = 0.5 + skew / 2.0;
                            if ((cPlusG) == 0) {
                                skew = 0.5;
                            }
                            average += skew;
                            if (skew > max) {
                                max = skew;
                            }
                            if (skew < min) {
                                min = skew;
                            }
                            cPlusG = 0;
                            totalLen += len;
                            len = 0;
                            totalWindow++;
                        }
                        int G = "G".compareToIgnoreCase(String.valueOf(line.charAt(f)));
                        int C = "C".compareToIgnoreCase(String.valueOf(line.charAt(f)));
                        if (C == 0 || G == 0) {
                            cPlusG++;
                        }
                        len++;
                    }
                }
            }
        }
        if (len > 0) {
            double skew = (double) cPlusG / (double) len;
            skew = 0.5 + skew / 2.0;
            if ((cPlusG) == 0) {
                skew = 0.5;
            }
            average += skew;
            if (skew > max) {
                max = skew;
            }
            if (skew < min) {
                min = skew;
            }
        }
        average = average / (double) totalWindow;
        if ((max - average) > (average - min)) {
            maxDeviation = max - average;
        } else {
            maxDeviation = average - min;
        }
        len = 0;
        cPlusG = 0;
        totalLen = 1;
        double radiusShift = 0.0;
        double barHeight = 0.0;
        error += ("Maximum value:  " + max + "\n");
        error += ("Minimum value:  " + min + "\n");
        error += ("Average value: " + average + "\n\n");
        try (BufferedReader first2 = new BufferedReader(new FileReader(QUERY_MASTER_FILE))) {
        int lineCount = 0;
        while ((line = first2.readLine()) != null) {
            if (line.contains(">")) {
                lineCount++;
            }
            if (line.contains(">") && lineCount > 1) {
                if (BRIG.PROFILE.getRootElement().getAttributeValue("spacer") != null) {
                    if (Integer.parseInt(BRIG.PROFILE.getRootElement().getAttributeValue("spacer")) > 0) {
                        double skew = (double) cPlusG / (double) len;
                        skew = 0.5 + skew / 2.0;
                        if ((cPlusG) == 0) {
                            skew = 0.5;
                        }
                        if (skew > average) {
                            color = "rgb(0,0,0)";
                            barHeight = skew - average;
                            barHeight = barHeight * 0.5 / maxDeviation;
                            radiusShift = 0.5 + barHeight / 2.0;
                        } else if (skew < average) {
                            color = "rgb(0,0,0)";
                            barHeight = average - skew;
                            barHeight = barHeight * 0.5 / maxDeviation;
                            radiusShift = 0.5 - barHeight / 2.0;
                        } else {
                            color = "rgb(0,0,0)";
                            radiusShift = 0.5;
                        }
                        if((totalLen + len) < GEN_LENGTH  ){
                        out.write("<featureRange color=\"" + color + "\" start=\"" + totalLen + "\" stop=\"" + (totalLen + len) + "\" "
                                + "proportionOfThickness=\"" + barHeight + "\" radiusAdjustment=\"" + radiusShift + "\" />");
                        }
                        out.newLine();
                        cPlusG = 0;
                        totalLen += len;
                        len = 0;
                        totalLen += Integer.parseInt(BRIG.PROFILE.getRootElement().getAttributeValue("spacer"));
                    }
                }
            } else if (!line.contains(">")) {
                for (int f = 0; f < line.length(); f++) {
                    if (len >= div) {
                        double skew = (double) cPlusG / (double) len;
                        skew = 0.5 + skew / 2.0;
                        if ((cPlusG) == 0) {
                            skew = 0.5;
                        }
                        if (skew > average) {
                            color = "rgb(0,0,0)";
                            barHeight = skew - average;
                            barHeight = barHeight * 0.5 / maxDeviation;
                            radiusShift = 0.5 + barHeight / 2.0;
                        } else if (skew < average) {
                            color = "rgb(0,0,0)";
                            barHeight = average - skew;
                            barHeight = barHeight * 0.5 / maxDeviation;
                            radiusShift = 0.5 - barHeight / 2;
                        } else {
                            color = "rgb(0,0,0)";
                            radiusShift = 0.5;
                        }
                        if((totalLen + len) < GEN_LENGTH  ){
                        out.write("<featureRange color=\"" + color + "\" start=\"" + totalLen + "\" stop=\"" + (totalLen + len) + "\" "
                                + "proportionOfThickness=\"" + barHeight + "\" radiusAdjustment=\"" + radiusShift + "\" />");
                        }
                        out.newLine();
                        cPlusG = 0;
                        totalLen += len;
                        len = 0;
                    }
                    int G = "G".compareToIgnoreCase(String.valueOf(line.charAt(f)));
                    int C = "C".compareToIgnoreCase(String.valueOf(line.charAt(f)));
                    if (C == 0 || G == 0) {
                        cPlusG++;
                    }
                    len++;
                }
            }

        }
        }
        if (len > 0) {
            double skew = (double) cPlusG / (double) len;
            skew = 0.5 + skew / 2.0;
            if ((cPlusG) == 0) {
                skew = 0.5;
            }
            if (skew > average) {
                color = "rgb(0,0,0)";
                barHeight = skew - average;
                barHeight = barHeight * 0.5 / maxDeviation;
                radiusShift = 0.5 + barHeight / 2.0;
            } else if (skew < average) {
                color = "rgb(0,0,0)";
                barHeight = average - skew;
                barHeight = barHeight * 0.5 / maxDeviation;
                radiusShift = 0.5 - barHeight / 2;
            } else {
                color = "rgb(0,0,0)";
                radiusShift = 0.5;
            }
            if((totalLen + len) < GEN_LENGTH  ){
            out.write("<featureRange color=\"" + color + "\" start=\"" + totalLen + "\" stop=\"" + (totalLen + len) + "\" "
                    + "proportionOfThickness=\"" + barHeight + "\" radiusAdjustment=\"" + radiusShift + "\" />");
          }
            out.newLine();
        }
        out.write("</feature>");
        out.newLine();
        return error;
    }

    private static String CGskew(BufferedWriter out, String QUERY_MASTER_FILE) throws IOException {
        String error = "";
        int div = autoScale(GEN_LENGTH);
        out.write("<feature decoration=\"arc\" opacity = \"1.0\">");
        out.newLine();
        int len = 0;
        int cPlusG = 0;
        int cMinusG = 0;
        int totalLen = 0;
        double average = 0.0;
        double maxDeviation = 0.0;
        double min = 10.0;
        double max = 0.0;
        int totalWindow = 0;
        String color = "";
        String line = "";
        try (BufferedReader first = new BufferedReader(new FileReader(QUERY_MASTER_FILE))) {
        while ((line = first.readLine()) != null) {
            if (!line.contains(">")) {
                for (int f = 0; f
                        < line.length(); f++) {
                    if (len >= div) {
                        double skew = (double) cMinusG / (double) cPlusG;
                        skew = 0.5 + skew / 2.0;
                        if ((cPlusG) == 0) {
                            skew = 0.5;
                        }
                        average += skew;
                        if (skew > max) {
                            max = skew;
                        }
                        if (skew < min) {
                            min = skew;
                        }
                        cPlusG = 0;
                        cMinusG = 0;
                        totalLen += len;
                        len = 0;
                        totalWindow++;
                    }
                    int G = "G".compareToIgnoreCase(String.valueOf(line.charAt(f)));
                    int C = "C".compareToIgnoreCase(String.valueOf(line.charAt(f)));
                    if (C == 0) {
                        cMinusG++;
                        cPlusG++;
                    } else if (G == 0) {
                        cMinusG--;
                        cPlusG++;
                    }
                    len++;
                }
            }
        }
        if (len > 0) {
            double skew = (double) cMinusG / (double) cPlusG;
            skew = 0.5 + skew / 2.0;
            if ((cPlusG) == 0) {
                skew = 0.5;
            }
            average += skew;
            if (skew > max) {
                max = skew;
            }
            if (skew < min) {
                min = skew;
            }
        }
        average = average / (double) totalWindow;
        if ((max - average) > (average - min)) {
            maxDeviation = max - average;
        } else {
            maxDeviation = average - min;
        }
        }
        len = 0;
        cPlusG = 0;
        cMinusG = 0;
        totalLen = 1;
        double radiusShift = 0.0;
        double barHeight = 0.0;
        error += ("Maximum value:  " + max + "\n");
        error += ("Minimum value:  " + min + "\n");
        error += ("Average value: " + average + "\n\n");
        try (BufferedReader first2 = new BufferedReader(new FileReader(QUERY_MASTER_FILE))) {
        int lineCount = 0;
        while ((line = first2.readLine()) != null) {
            if (line.contains(">")) {
                lineCount++;
            }
            if (line.contains(">") && lineCount > 1) {
                if (BRIG.PROFILE.getRootElement().getAttributeValue("spacer") != null) {
                    if (Integer.parseInt(BRIG.PROFILE.getRootElement().getAttributeValue("spacer")) > 0) {
                        double skew = (double) cMinusG / (double) cPlusG;
                        skew = 0.5 + skew / 2.0;
                        if ((cPlusG) == 0) {
                            skew = 0.5;
                        }
                        if (skew > average) {
                            color = "rgb(152,0,152)";
                            barHeight = skew - average;
                            barHeight = barHeight * 0.5 / maxDeviation;
                            radiusShift = 0.5 + barHeight / 2.0;
                        } else if (skew < average) {
                            color = "rgb(0,152,0)";
                            barHeight = average - skew;
                            barHeight = barHeight * 0.5 / maxDeviation;
                            radiusShift = 0.5 - barHeight / 2;
                        } else {
                            color = "rgb(152,0,152)";
                            radiusShift = 0.5;
                        }
                        cMinusG = 0;
                        cPlusG = 0;
                        totalLen += len;
                        len = 0;
                        totalLen += Integer.parseInt(BRIG.PROFILE.getRootElement().getAttributeValue("spacer"));
                    }
                }
            }
            if (!line.contains(">")) {
                for (int f = 0; f < line.length(); f++) {
                    if (len >= div) {
                        double skew = (double) cMinusG / (double) cPlusG;
                        skew = 0.5 + skew / 2.0;
                        if ((cPlusG) == 0) {
                            skew = 0.5;
                        }
                        if (skew > average) {
                            color = "rgb(152,0,152)";
                            barHeight = skew - average;
                            barHeight = barHeight * 0.5 / maxDeviation;
                            radiusShift = 0.5 + barHeight / 2.0;
                        } else if (skew < average) {
                            color = "rgb(0,152,0)";
                            barHeight = average - skew;
                            barHeight = barHeight * 0.5 / maxDeviation;
                            radiusShift = 0.5 - barHeight / 2;
                        } else {
                            color = "rgb(152,0,152)";
                            radiusShift = 0.5;
                        }
                        out.write("<featureRange color=\"" + color + "\" start=\"" + totalLen + "\" stop=\"" + (totalLen + len) + "\" "
                                + "proportionOfThickness=\"" + barHeight + "\" radiusAdjustment=\"" + radiusShift + "\" />");
                        out.newLine();
                        cPlusG = 0;
                        cMinusG = 0;
                        totalLen += len;
                        len = 0;
                    }
                    int G = "G".compareToIgnoreCase(String.valueOf(line.charAt(f)));
                    int C = "C".compareToIgnoreCase(String.valueOf(line.charAt(f)));
                    if (C == 0) {
                        cMinusG++;
                        cPlusG++;
                    } else if (G == 0) {
                        cMinusG--;
                        cPlusG++;
                    }
                    len++;
                }
            }
        }
        }
        if (len > 0) {
            double skew = (double) cMinusG / (double) cPlusG;
            skew = 0.5 + skew / 2.0;
            if ((cPlusG) == 0) {
                skew = 0.5;
            }
            out.write("<featureRange color=\"rgb(0,0,0)\" start=\"" + totalLen + "\" stop=\"" + (totalLen + len - 2) + "\" "
                    + "proportionOfThickness=\"" + skew + "\" radiusAdjustment=\"0.5\" />");
            out.newLine();
        }
        out.write("</feature>");
        out.newLine();
        return error;
    }

    public  static int autoScale(int length  ){
        return (length / 3000);
    }
}

class RedComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        o1 = o1.replaceAll("^#", "");
        o2 = o2.replaceAll("^#", "");
        String[] o1Array = o1.split("\t");
        String[] o2Array = o2.split("\t");
        if (Integer.parseInt(o1Array[0]) <= Integer.parseInt(o2Array[0])) {
            return -1;
        } else if (Integer.parseInt(o1Array[0]) > Integer.parseInt(o2Array[0])) {
            return 1;
        } else {
            return 0;
        }
    }
}

class BlastComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        String[] o1Array = o1.split("\t");
        String[] o2Array = o2.split("\t");
        if (Double.parseDouble(o1Array[10]) < Double.parseDouble(o2Array[10])) {
            return -1;
        } else if (Double.parseDouble(o1Array[10]) > Double.parseDouble(o2Array[10])) {
            return 1;
        } else if (Double.parseDouble(o1Array[10]) == Double.parseDouble(o2Array[10])) {
            return 0;
        } else {
            return 0;
        }
    }
}
