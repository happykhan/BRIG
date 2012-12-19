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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import org.jdom.*;
import org.jdom.output.XMLOutputter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedOutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.UIManager;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;

/**
 *
 * @author Nabil
 */
public class BRIG extends Thread{

    public static int POSITION = 0;
    public static Document PROFILE;
    public static String PROFILE_LOCATION = "default-BRIG.xml";
    public static String SL = File.separator;
    public static int GEN_LENGTH = 0;
    public static String header = "BLAST Ring Image Generator v0.95-dev.0005";
    public One parent;
    static PipedOutputStream OUT = new PipedOutputStream();
    public final static Logger LOGGER = Logger.getLogger(BRIGLogger.class.getName());
        
    public BRIG(PipedOutputStream inOUT) throws IOException {
        OUT = inOUT;

    }

    static void Print(String message)  {
        try{
        String out = message +"\n";
        OUT.write(out.getBytes());
        }catch (Exception e ){
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();
        File del = new File("errorlog.xml");
        del.delete();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        
        LOGGER.info( "free memory: " + format.format(freeMemory / 1024));
        LOGGER.info( "allocated memory: " + format.format(allocatedMemory / 1024));
        LOGGER.info( "max memory: " + format.format(maxMemory / 1024));
        LOGGER.info( "total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
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
            List refs = PROFILE.getRootElement().getChildren("special");
            int noSkew = 0;
            int noContent = 0;
            for (int i = 0; i < refs.size(); i++) {
                Element current = (Element) refs.get(i);
                if (current.getAttributeValue("value").compareTo("GC Skew") == 0) {
                    noSkew++;
                }
                if (current.getAttributeValue("value").compareTo("GC Content") == 0) {
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
            new One().setVisible(true);
        } catch (JDOMException e) {
            Print(PROFILE_LOCATION + " is corrupt. Please check");
            Print(e.getMessage());
        } catch (IOException e) {
            Print("Could not read " + PROFILE_LOCATION);
            Print(" because " + e.getMessage());
        }
    }

    public static String BlastOption(String opt ){
        boolean blastPlus = true ;
        String error = "You are running BLAST+: ";
        String[] noBlast = {
            "-h", "-help", "-db", "-query", "subject","-out","-version", "-outfmt",
            "-p","-d","-i","-e","-m","-o","-F","-G","-E","-X","-I","-q","-r","-v",
            "-b","-f","-g","-Q","-D","-a","-O","-J","-M","-W","-z","-K","-P","-T",
            "-U","-y","-Z","-R","-L","-w","-B","-C","-s"
            } ;


        if ( PROFILE.getRootElement().getAttribute("blastPlus") == null ) {
            blastPlus = false;
            error = "You are running BLAST Legacy: ";
            noBlast = new String[]{
                        "-h","-p","-d","-i","-o","-m", "-help", "-import_search_strategy",
                        "-export_search_strategy", "-task", "-db",
                        "-dbsize,num_letters", "-gilist", "-seqidlist",
                        "-negative_gilist", "-entrez_query",
                        "-db_soft_mask", "-subject",
                        "-subject_loc", "-query", "-out",
                        "-evalue", "-word_size", "-gapopen",
                        "-gapextend", "-perc_identity",
                        "-xdrop_ungap", "-xdrop_gap",
                        "-xdrop_gap_final", "-searchsp", "-penalty",
                        "-reward", "-no_greedy", "-min_raw_gapped_score",
                        "-template_type", "-template_length", "-dust",
                        "-filtering_db,filtering_database",
                        "-window_masker_taxid,window_masker_taxid",
                        "-window_masker_db,window_masker_db", "-soft_masking,soft_masking",
                        "-ungapped", "-culling_limit", "-best_hit_overhang",
                        "-best_hit_score_edge", "-window_size",
                        "-off_diagonal_range", "-use_index", "-index_name",
                        "-lcase_masking", "-query_loc", "-strand", "-parse_deflines",
                        "-outfmt,format", "-show_gis", "-num_descriptions",
                        "-num_alignments", "-html", "-max_target_seqs",
                        "-num_threads", "-remote", "-version"};

        }
        String[] tabArray = opt.split("\\s+");
        int er = 0;
        for (int i=0; i<noBlast.length;i++){
            for( int j=0; j<tabArray.length;j++){
                if(noBlast[i].compareTo(tabArray[j]) == 0 ){
                    error += noBlast[i] + " ";
                    er++;
                }
            }
        }
        if( er == 1){
            error+= "\nis not a valid parameter";
        }else{
            error+= "\nare not valid parameters";
        }
        if(er > 0){
            return error;
        }else{
            return null;
        }
    }

    public static String reverseBlast(String file, boolean isPro) throws IOException {
        String outFile = file + "flip";
        BufferedReader first = new BufferedReader(new FileReader(file));
        String line = "";
        int num = 0;
        while ((line = first.readLine()) != null) {
            if (!line.startsWith("#")) {
                num++;
            }
        }
        first.close();
        String[] results = new String[num];
        first = new BufferedReader(new FileReader(file));
        line = "";
        int gum = 0;
        while ((line = first.readLine()) != null) {
            if (!line.startsWith("#")) {
                results[gum] = line;
                gum++;
            }
        }
        first.close();
        BlastComparator comp = new BlastComparator();
        Arrays.sort(results, comp);
        int multiFasta = 0;
        if (PROFILE.getRootElement().getAttributeValue("spacer") != null) {
            multiFasta++;
        }
        int[] existing = new int[GEN_LENGTH];
        FileWriter fstream4 = new FileWriter(outFile);
        BufferedWriter out = new BufferedWriter(fstream4);
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
                    e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        out.close();
        return outFile;
    }

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
             List dirs = root.getChildren("refDir");
            List alteredName = new LinkedList<String[]>();
            for(int i=0; i < dirs.size(); i++ ){
                List seq = ((Element)dirs.get(i)).getChildren();
                for (int k = 0; k < seq.size(); k++) {
                    Element currSeq = ((Element) seq.get(k));
                    if( currSeq.getAttributeValue("location").compareTo("GC Skew") != 0 && currSeq.getAttributeValue("location").compareTo("GC Content") != 0){
                    File qer = new File(currSeq.getAttributeValue("location"));
                    if(qer.isFile()){
                    con.updateProgress("Copying " + qer.getName());
                    // Check if file exists.
                    String name = qer.getName();
                    boolean exists = false;
                    List existCheck = new LinkedList();
                    if (qer.getName().endsWith(".graph")) {
                        existCheck = gradir.getChildren("refFile");
                    }else{
                        existCheck = gendir.getChildren("refFile");
                    }
                    for(int f=0;f<existCheck.size();f++){
                        String existingFile =  new File(((Element)existCheck.get(f)).getAttributeValue("location")).getName();
                        if  (existingFile.compareTo(qer.getName()) == 0  ){
                            exists = true;
                            }
                        }
                        if (exists) {
                            String temp = con.fileExists(qer.getName());
                            if (temp != null && temp.compareTo("") != 0) {
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
            List rings = root.getChildren("ring");
            for(int i=0; i < rings.size(); i++ ){
                List seq = ((Element)rings.get(i)).getChildren("sequence");
                for(int j=0; j < seq.size(); j++ ){
                    Element currSeq = ((Element) seq.get(j));
                    currSeq.removeAttribute("blastResults");
                    String name = new File(currSeq.getAttributeValue("location")).getName();
                    for(int a=0;a<alteredName.size() ;a++){
                        if( ((String[])alteredName.get(a))[0].compareTo(currSeq.getAttributeValue("location")) == 0) {
                            name = ( (String[])alteredName.get(a))[1];
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
                    if (newName != null && newName.compareTo("") != 0) {
                        xmlName = newName;
                    }
                }
            BRIG.saveXML(fold.getAbsolutePath() + BRIG.SL + xmlName , bundle);
            con.updateProgress("Writing XML to " + fold.getAbsolutePath() + BRIG.SL + xmlName);
            }catch(Exception e ){
                con.updateProgress("SYS_ERROR: "+  e.getMessage() +"\n");
                e.printStackTrace();
                return -1;
            }
           con.updateProgress("Done.\n");
        }else{
            con.updateProgress("You have not specified a valid directory.\n");
        }


        return 0;
    }
    public static void copyTo(File inputFile, File outputFile, boolean binary) throws FileNotFoundException, IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
        if( binary){
            bis = new BufferedInputStream(new FileInputStream(inputFile), 4096);
            bos = new BufferedOutputStream(new FileOutputStream(outputFile), 4096);
        }
        int c;
        while ((c = bis.read()) != -1) {
            bos.write(c);
        }
        bis.close();
        bos.close();
    }

    public static Document FormatArchive(Document temp, String file) {
        Element root = temp.getRootElement();
        File base = new File(file);
        String dir = base.getParent() + BRIG.SL;
        root.setAttribute("outputFolder", dir);
        // Reconnect with sequences.
        List rings = root.getChildren("ring");
        for (int i = 0; i < rings.size(); i++) {
            List seq = ((Element) rings.get(i)).getChildren("sequence");
            for (int j = 0; j < seq.size(); j++) {
                Element currSeq = ((Element) seq.get(j));
                if( currSeq.getAttributeValue("location").compareTo("GC Skew") != 0 && currSeq.getAttributeValue("location").compareTo("GC Content") != 0 ){
                currSeq.setAttribute("location", dir + currSeq.getAttributeValue("location"));
                }
            }
        }
        List dirs = root.getChildren("refDir");
        for (int i = 0; i < dirs.size(); i++) {
            Element currentDir = (Element) dirs.get(i);
            List seq = currentDir.getChildren();
            currentDir.setAttribute("location", dir + currentDir.getAttributeValue("location"));
            for (int k = 0; k < seq.size(); k++) {
                Element currSeq = ((Element) seq.get(k));
                currSeq.setAttribute("location", dir + currSeq.getAttributeValue("location"));
            }
        }
        root.setAttribute("queryFile", dir + root.getAttributeValue("queryFile"));
        return temp;
    }

    public static boolean isBlastOk() throws IOException {
        String bin = "";
        if (BRIG.PROFILE.getRootElement().getChild("brig_settings") != null) {
            if (BRIG.PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation") != null) {
                bin = BRIG.PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation");
            }
        }
        if (!bin.endsWith(BRIG.SL) && bin.compareTo("") != 0) {
            bin += BRIG.SL;
        }
        String ergo = "";
        try {
            String exec = bin + "blastn -help";
            Print(exec);
            Process q = Runtime.getRuntime().exec(exec);
            InputStream istrm = q.getInputStream();
            InputStreamReader istrmrdr = new InputStreamReader(istrm);
            String data;
            BufferedReader buffrdr = new BufferedReader(istrmrdr);
            while ((data = buffrdr.readLine()) != null) {
                ergo += data + "\n";
                if (ergo.contains("USAGE")) {
                    Print(ergo);
                    buffrdr.close();
                    q.destroy();
                    BRIG.PROFILE.getRootElement().setAttribute("blastPlus", "yes");
                    return true;
                }
            }
        } catch (Exception e) {
            Print("Could not find BLAST+, looking for BLAST legacy");
        }
        String data;
        String exec = bin + "blastall";
        Print(exec);
        Process q = Runtime.getRuntime().exec(exec);
        InputStream istrm = q.getInputStream();
        InputStreamReader istrmrdr = new InputStreamReader(istrm);
        BufferedReader buffrdr = new BufferedReader(istrmrdr);
        while ((data = buffrdr.readLine()) != null) {
            ergo += data + "\n";
            if (ergo.contains("  -p  Program Name [String]")) {
                Print(ergo);
                buffrdr.close();
                q.destroy();
                BRIG.PROFILE.getRootElement().removeAttribute("blastPlus");
                return true;
            }
        }
        Print(ergo);
        buffrdr.close();
        q.destroy();
        return false;
    }

    public static Color FetchColor(int RingNumber) {
        Color out = Color.red;
        int get = RingNumber % 10;
        Element settings = BRIG.PROFILE.getRootElement().getChild("brig_settings");
        if (settings != null) {
            String col = settings.getAttributeValue("Ring" + get);
            if (col == null) {
                col = "225,0,0";
            }
            String[] tabArray = col.split(",");
            out = new Color(Integer.parseInt(tabArray[0]), Integer.parseInt(tabArray[1]), Integer.parseInt(tabArray[2]));
            BRIG.dumpXML("errorlog.xml", BRIG.PROFILE);
        }
        return out;
    }

    public static void dumpXMLDebug() {
        try {
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            serializer.output(PROFILE, System.out);
        } catch (IOException e) {
            System.err.println(e);
        }

    }

    public static boolean isProteinFASTA(String input) throws FileNotFoundException, IOException {
        BufferedReader first = new BufferedReader(new FileReader(input));
        String line = "";
        int five = 0;
        while ((line = first.readLine()) != null) {
            if (!line.contains(">")) {
                Pattern p = Pattern.compile("[^ATGCNU\\-atgcnu]");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    return true;
                }
                if (five == 5) {
                    first.close();
                    return false;
                }
                five++;
            }
        }
        first.close();
        return false;
    }

    public static String formatMultiFASTA(String input, String output, int spacer, boolean label) throws FileNotFoundException, IOException {
        String error = "Multi-FASTA detected, Rewriting headers\n";
        BufferedReader first = new BufferedReader(new FileReader(input));
        FileWriter fstream4 = new FileWriter(output);
        BufferedWriter out = new BufferedWriter(fstream4);
        String line = "";
        int start = 0;
        int lineNum = 0;
        int stop = 0;
        Vector head = new Vector();
        String tempHead = "";
        boolean multi = BRIG.isProteinFASTA(input);
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
                String temp = line.replaceAll(" ", "");
                if (multi) {
                    stop += temp.length() * 3;
                } else {
                    stop += temp.length();
                }
            }
            lineNum++;
        }
        tempHead = tempHead.replaceAll(":", "");
        if (!label) {
            tempHead = tempHead.split(" ")[0];
        }
        head.add(tempHead + ":" + start + ":" + stop);
        lineNum = 0;
        first.close();
        first = new BufferedReader(new FileReader(input));
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
        out.close();
        first.close();
        return error;
    }

    public static Document prepProfile(Document profil) {
        Document doc = new Document(new Element("BRIG"));
        Element root = profil.getRootElement();
        root.removeAttribute("archive");
        doc.getRootElement().addContent((Element) root.getChild("cgview_settings").clone());
        doc.getRootElement().addContent((Element) root.getChild("brig_settings").clone());
        if (root.getAttributeValue("blastOptions") != null) {
            doc.getRootElement().setAttribute("blastOptions", root.getAttributeValue("blastOptions"));
        }
        if (root.getAttributeValue("legendPosition") != null) {
            doc.getRootElement().setAttribute("legendPosition", root.getAttributeValue("legendPosition"));
        }
        List special = root.getChildren("special");
        for (int i = 0; i < special.size(); i++) {
            doc.getRootElement().addContent((Element) ((Element) special.get(i)).clone());
        }
        return doc;
    }

    public static String ValidateSession() {
        Element root = BRIG.PROFILE.getRootElement();
        String query = root.getAttributeValue("queryFile");
        File temp = new File(query);
        if (!temp.exists()) {
            return "query file " + query + " does not exist";
        }
        if (query.contains(" ")) {
            return query + " contains a space. Invalid filename.";
        }
        query = root.getAttributeValue("outputFolder");
        temp = new File(query);
        if (!temp.exists()) {
            return "output file " + query + " does not exist";
        }
        if (query.contains(" ")) {
            return query + " contains a space. Invalid filename.";
        }
        if (((Element) root.getChild("refDir")) != null) {
            List ref = ((Element) root.getChild("refDir")).getChildren();
            for (int i = 0; i < ref.size(); i++) {
                query = ((Element) ref.get(i)).getAttributeValue("location");
                temp = new File(query);
                if (!temp.exists()) {
                    return "query file " + query + " does not exist";
                }
                if (query.contains(" ")) {
                    return query + " contains a space. Invalid filename.";
                }
            }
        }
        return null;
    }


    public static int saveXML(String output, Document profil) {
        try {
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            FileWriter fstream4 = new FileWriter(output);
            BufferedWriter out1 = new BufferedWriter(fstream4);
            serializer.output(profil, out1);
            out1.close();
            return 0;
        } catch (IOException e) {
            System.err.println(e);
            return 1;
        }

    }

    public static int dumpXML(String output, Document profil) {
        try {
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            FileWriter fstream4 = new FileWriter(output, true);
            BufferedWriter out1 = new BufferedWriter(fstream4);
            out1.write(getDateTime());
            out1.newLine();
            serializer.output(profil, out1);
            out1.close();
            return 0;
        } catch (IOException e) {
            System.err.println(e);
            return 1;
        }

    }

    private static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static int isMultiFasta(String file) throws FileNotFoundException, IOException {
        BufferedReader in = in = new BufferedReader(new FileReader(file));
        String line = null;
        int i = 0;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(">")) {
                i++;
            }
        }
        return i;
    }

    public static String FetchFilename(String filename) {
        String out = "";
        int dirend = filename.lastIndexOf(File.separatorChar);
        out = filename.substring((dirend + 1), filename.length());
        return out;
    }

    public static int FastaLength(String file, boolean multi) throws FileNotFoundException, IOException {
        BufferedReader in = null;
        String line = null;
        int caret = 0;
        in = new BufferedReader(new FileReader(file));
        boolean isPro = BRIG.isProteinFASTA(file);
        while ((line = in.readLine()) != null) {
            if (!line.contains(">") && !multi) {
                line.trim();
                char lineArray[] = line.toCharArray();
                for (int i = 0; i < lineArray.length; i++) {
                    if (isPro) {
                        caret += 3;
                    } else {
                        caret++;
                    }

                }
            }
            if (multi) {
                if (line.contains(">")) {
                    String[] commaarray = line.split(":");
                    /*     start = Integer.parseInt(commaarray[1]);
                    caret += (start - lastStop);
                    lastStop = Integer.parseInt(commaarray[2]);*/
                    caret = Integer.parseInt(commaarray[2]);
                }
            }
        }
        in.close();
        return caret;
    }

    public static int checkComment(String file) throws FileNotFoundException, IOException {
        BufferedReader in = null;
        String line = null;
        int d = 0;
        in = new BufferedReader(new FileReader(file));
        while ((line = in.readLine()) != null) {
            if (line.startsWith("#")) {
                d++;
            }
        }

        return d;

    }

    public static String WordWrap(String terg) {
        int z = 100;
        int y = 0;
        String out = "";
        while (z < terg.length()) {
            out = out + terg.substring(y, z) + "\n";
            y = z;
            z = z + 100;
            if (z > terg.length()) {
                out = out + terg.substring(y, (terg.length() - 1)) + "\n";
            }
        }
        if (terg.length() <= 100) {
            out = out + terg.substring(y) + "\n";
        }
        return out;
    }

    public static String FormatGenbank(String file, String header, String out, boolean embl, String pOption) throws FileNotFoundException, IOException {
        String error = "";
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line = "";
        int on = 0;
        FileWriter fstream4 = new FileWriter(out + ".fna");
        BufferedWriter out1 = new BufferedWriter(fstream4);
        out1.write(">" + header);
        out1.newLine();
        error += "Formatting nucleotide file...\n";
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
        out1.close();
        fstream4.close();
        in.close();
        if (pOption.compareTo("T") == 0) {
            FileWriter fstream5 = new FileWriter(out + ".faa");
            BufferedWriter out2 = new BufferedWriter(fstream5);
            Document features = AnnoXML.CreateFeatureXML(file, embl);
            List CDS = features.getRootElement().getChildren("CDS");
            error += "Formatting protein file...\n";
            translateProtein newList = new translateProtein("proteins.txt");
            for (int i = 0; i < CDS.size(); i++) {
                Element currentElement = (Element) CDS.get(i);
                String name = "unknown";
                if (currentElement.getChild("locus_tag") != null) {
                    name = currentElement.getChild("locus_tag").getAttributeValue("value");
                } else if (currentElement.getChild("gene") != null) {
                    name = currentElement.getChild("gene").getAttributeValue("value");
                }
                String stop = currentElement.getAttributeValue("stop");
                String start = currentElement.getAttributeValue("start");
                if (currentElement.getChild("translation") != null) {
                    out2.write(">" + name + ":" + start + ":" + stop);
                    out2.newLine();
                    out2.write(WordWrap(currentElement.getChild("translation").getAttributeValue("value")));
                } else {
                    // Translate sequence from file
                    // fetch subsequence
                    String nu = fastaSubsequence(out + ".fna", Integer.parseInt(start), Integer.parseInt(stop));
                    // translate subsequnece
                    if (nu.compareTo("\\s+") != 0 && nu.compareTo("") != 0) {
                        out2.write(">" + name + ":" + start + ":" + stop);
                        out2.newLine();
                        String pu = newList.translateSeq(nu).replaceAll("\\*", "");
                        out2.write(WordWrap(pu));
                    }
                }
            }
            out2.close();
            fstream5.close();
        }
        return error;
    }

    public static String fastaSubsequence(String file, int start, int stop) {
        String spline = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = "";
            int caret = 1;
            while ((line = in.readLine()) != null) {
                if (!line.startsWith(">")) {
                    if (caret < stop && caret + line.length() > start) {
                        char[] lin = line.toCharArray();
                        for (int i = 0; i < lin.length; i++) {
                            if (i + caret >= start && i + caret <= stop) {
                                spline += lin[i];
                            }
                        }
                    }
                    caret += line.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return spline;
    }

    public static String WriteXMLLegend() {
        String error = "";
        try {
            Element root = BRIG.PROFILE.getRootElement();
            String output = root.getAttributeValue("outputFolder");
            String query = root.getAttributeValue("queryFile");
            root.setAttribute("cgXML", output + SL + "scratch" + SL + FetchFilename(query) + ".xml");
            String cgXML = root.getAttributeValue("cgXML");
            String TITLE = root.getAttributeValue("title");
            FileWriter fstream = new FileWriter(cgXML);
            Element cgSettings = root.getChild("cgview_settings");
            List settings = cgSettings.getAttributes();
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><cgview ");
            String[] doops = cgSettings.getAttributeValue("warningFont").toString().split(",");
            int LEGEND_SIZE = 15;
            try {
                LEGEND_SIZE = Integer.parseInt(doops[2]);
            } catch (NumberFormatException n) {
                error += "Bad legend font-size, setting to default\n";
                LEGEND_SIZE = 15;
            }
            for (int i = 0; i < settings.size(); i++) {
                String badstring = settings.get(i).toString() + " ";
                badstring = badstring.replaceAll("^\\[Attribute: ", "");
                badstring = badstring.replaceAll("\\]", "");
                out.write(badstring);
            }
            List rings = PROFILE.getRootElement().getChildren("ring");
            out.write(" sequenceLength=\"" + GEN_LENGTH + "\" title=\"" + TITLE + "\">");
            out.newLine();
            String def = "upper-right";
            if (BRIG.PROFILE.getRootElement().getAttributeValue("legendPosition") != null) {
                def = BRIG.PROFILE.getRootElement().getAttributeValue("legendPosition");
            }
            if (cgSettings.getAttributeValue("warningFont").compareTo("null") != 0 && cgSettings.getAttributeValue("warningFont").compareTo("") != 0) {
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
                    Element currentRing = (Element) rings.get(a);
                    if (Integer.parseInt(currentRing.getAttributeValue("position")) == i) {
                        //FOR EACH SEQUENCE
                        List sequence = currentRing.getChildren("sequence");
                        if (currentRing.getAttributeValue("name").compareTo("null") != 0 && currentRing.getAttributeValue("name").compareTo("") != 0) {
                            if (sequence.size() > 0) {
                                Element currentSeq = (Element) sequence.get(0);
                                //CHECK IF GRAPH
                                if (currentSeq.getAttributeValue("location").endsWith(".graph")) {
                                    String RGB = "rgb(" + currentRing.getAttributeValue("colour") + ")";
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name")
                                            + "\" drawSwatch=\"true\" swatchColor=\"" + RGB + "\" />");
                                    out.newLine();
                                } else if (currentSeq.getAttributeValue("location").compareTo("GC Skew") == 0) {
                                    //CHECK IF GC SKEW
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "\" />");
                                    out.newLine();
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "(-)\" font=\"SanSerif,plain,"
                                            + (int) (LEGEND_SIZE * 0.75) + "\" drawSwatch=\"true\" swatchColor=\"rgb(152,0,152)\" />");
                                    out.newLine();
                                    out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "(+)\" font=\"SanSerif,plain,"
                                            + (int) (LEGEND_SIZE * 0.75) + "\" drawSwatch=\"true\" swatchColor=\"rgb(0,152,0)\" />");
                                    out.newLine();
                                } else if (currentSeq.getAttributeValue("location").compareTo("GC Content") == 0) {
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
                                    if (currentRing.getAttributeValue("legend").compareTo("yes") == 0) {
                                        out.write("<legendItem text=\"" + currentRing.getAttributeValue("name") + "\" />");
                                    } else {
                                        String RGB = "rgb(" + currentRing.getAttributeValue("colour") + ")";
                                        out.write("<legendItem drawSwatch=\"true\" text=\"" + currentRing.getAttributeValue("name") + "\" swatchColor=\"" + RGB + "\" />");
                                    }
                                    out.newLine();
                                    if (currentRing.getAttributeValue("legend").compareTo("yes") == 0) {
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
                                            if (cgSettings.getAttributeValue("warningFont").compareTo("null") != 0
                                                    && cgSettings.getAttributeValue("warningFont").compareTo("") != 0) {
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
            out.close();
        } catch (Exception e) {
            error += e.getMessage();
        }
        return error;
    }

    /*
    if (Integer.parseInt(currentRing.getAttributeValue("position")) == i) {

     */
    public static boolean isGenbank(String file) {
        if (PROFILE.getRootElement().getChild("brig_settings") != null) {
            Element settings = PROFILE.getRootElement().getChild("brig_settings");
            if (settings.getAttributeValue("genbankFiles") != null) {
                String[] em = settings.getAttributeValue("genbankFiles").split(",");
                for (int f = 0; f < em.length; f++) {
                    if (file.endsWith(em[f])) {
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
        if (PROFILE.getRootElement().getChild("brig_settings") != null) {
            Element settings = PROFILE.getRootElement().getChild("brig_settings");
            if (settings.getAttributeValue("emblFiles") != null) {
                String[] em = settings.getAttributeValue("emblFiles").split(",");
                for (int f = 0; f < em.length; f++) {
                    if (file.endsWith(em[f])) {
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

    public static String RunBlast(int clean) {
        List ringList = PROFILE.getRootElement().getChildren("ring");
        String query = PROFILE.getRootElement().getAttributeValue("queryFile");
        String queryFastaFile = PROFILE.getRootElement().getAttributeValue("queryFastaFile");
        String output = PROFILE.getRootElement().getAttributeValue("outputFolder");
        String blastOptions = PROFILE.getRootElement().getAttributeValue("blastOptions");
        //String reverseDb = PROFILE.getRootElement().getAttributeValue("reverseDb");
        // Removing reverseDB fucntionality
        String blastLocation = "";
        if (PROFILE.getRootElement().getChild("brig_settings") != null) {
            if (PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation") != null) {
                blastLocation = PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation");
                if (!(PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation").endsWith(SL))) {
                    if (PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation").compareTo("") != 0) {
                        blastLocation += SL;
                    }
                }
            }
        }
        Print( "Sequence length: " + BRIG.GEN_LENGTH );
        for (int i = 0; i < ringList.size(); i++) {
            Element currentRing = (Element) ringList.get(i);
            Print("ring\t"+  i );
            List sequence = currentRing.getChildren("sequence");
            if (sequence.size() > 0) {
                Element firstElement = (Element) sequence.get(0);
                if (firstElement.getAttributeValue("location").compareTo("GC Skew") != 0
                        && firstElement.getAttributeValue("location").compareTo("GC Content") != 0
                        && !firstElement.getAttributeValue("location").endsWith(".graph")) {
                    String blastType = currentRing.getAttributeValue("blastType");
                    for (int j = 0; j < sequence.size(); j++) {
                        String ringFile = ((Element) sequence.get(j)).getAttributeValue("location").toString();
                        Print("file\t" + ringFile);
                        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
                            ringFile = ringFile.replaceAll(SL + SL, SL);
                        }
                        String goodRingFile = ringFile;
                        String pOption = "F";
                        String opt = "nucl";
                        File dbOut = new File(queryFastaFile + ".nin");
                        if (blastType.compareTo("blastp") == 0 || blastType.compareTo("blastx") == 0) {
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
                                if (blastType.compareTo("blastp") == 0 || blastType.compareTo("tblastn") == 0) {
                                    op = "T";
                                }
                                Print( BRIG.FormatGenbank(ringFile,
                                        filename, output + BRIG.SL + "scratch" + BRIG.SL + filename, embl, op));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            goodRingFile = output + BRIG.SL + "scratch" + BRIG.SL + filename + ".fna";
                            if (blastType.compareTo("blastp") == 0 || blastType.compareTo("tblastn") == 0) {
                                goodRingFile = output + BRIG.SL + "scratch" + BRIG.SL + filename + ".faa";
                            }
                        }
                        String exec = "";
                        List<String> execList = new LinkedList<String>();
                        /*     if (reverseDb != null) {
                        if (reverseDb.compareTo("true") == 0) {
                        dbOut = new File(goodRingFile + ".nin");
                        if (blastType.compareTo("blastp") == 0 || blastType.compareTo("tblastn") == 0) {
                        pOption = "T";
                        opt = "prot";
                        dbOut = new File(goodRingFile + ".pin");
                        }
                        if (Main.PROFILE.getRootElement().getAttribute("blastPlus") != null) {
                        execList.add(blastLocation + "makeblastdb -dbtype " + opt + " -in " + goodRingFile);
                        } else {
                        execList.add(blastLocation + "formatdb -p " + pOption + " -i " + goodRingFile);
                        }
                        }
                        } else {*/
                        if (BRIG.PROFILE.getRootElement().getAttribute("blastPlus") != null) {
                            execList.add(blastLocation + "makeblastdb -dbtype " + opt + " -in " + queryFastaFile);
                        } else {
                            execList.add(blastLocation + "formatdb -p " + pOption + " -i " + queryFastaFile);
                        }
                        //       }
                        if (clean == 1 || !dbOut.exists()) {
                            for (int g = 0; g < execList.size(); g++) {
                                try {
                                    exec = execList.get(g);
                                    Print(exec);
                                    String data = "";
                                    Process p = Runtime.getRuntime().exec(exec);
                                    InputStream istrm = p.getErrorStream();
                                    InputStreamReader istrmrdr = new InputStreamReader(istrm);
                                    BufferedReader buffrdr = new BufferedReader(istrmrdr);
                                    String ergo = "";
                                    while ((data = buffrdr.readLine()) != null) {
                                        ergo += data;
                                        Print(data);
                                    }
                                    if (ergo.length() < 3) {
                                    Print("Success!");
                                    }
                                } catch (Exception e) {
                                    Print("SYS_ERROR: Could not execute: " + exec );
                                    return "";
                                }
                            }

                        } else {
                            Print( dbOut.getName() + " exists, skipping ..");
                        }
                        String ou = output + SL + "scratch" + SL + FetchFilename(goodRingFile) + "Vs" + FetchFilename(queryFastaFile) + ".tab";
                        if (BRIG.PROFILE.getRootElement().getAttribute("blastPlus") != null) {
                            String task = "";
                            if ( (!blastOptions.contains("-task")) && blastType.compareTo("blastp") == 0) {
                                task = "   -task blastp ";
                            } else if ((!blastOptions.contains("-task")) && blastType.compareTo("blastn") == 0) {
                                task = "  -task blastn ";
                            }
                            /*     if (reverseDb != null) {
                            if (reverseDb.compareTo("true") == 0) {
                            exec = blastLocation + blastType + " -outfmt 6 -query " + queryFastaFile + " -db " + goodRingFile + " -out " + ou + " " + blastOptions + task;
                            }
                            } else {*/
                            exec = blastLocation + blastType + " -outfmt 6 -query " + goodRingFile + " -db " + queryFastaFile + " -out " + ou + " " + blastOptions + task;
                            //  }
                        } else {
                            /*  if (reverseDb != null) {
                            if (reverseDb.compareTo("true") == 0) {
                            exec = blastLocation + "blastall -m8 -i " + queryFastaFile + " -d " + goodRingFile + " -o " + ou + " -p " + blastType + " " + blastOptions;
                            }
                            } else {*/
                            exec = blastLocation + "blastall -m8 -i " + goodRingFile + " -d " + queryFastaFile + " -o " + ou + " -p " + blastType + " " + blastOptions;
                            //  }
                        }
                        ((Element) sequence.get(j)).setAttribute("blastResults", ou);
                        File blOut = new File(ou);
                        Print("DOO    " + ou);
                        try {
                            if (clean == 1 || !blOut.exists()) {
                                Print(exec);
                                Process q = Runtime.getRuntime().exec(exec);
                                String data;
                                InputStream istrm = q.getErrorStream();
                                InputStreamReader istrmrdr = new InputStreamReader(istrm);
                                BufferedReader buffrdr = new BufferedReader(istrmrdr);
                                String ergo = "";
                                while ((data = buffrdr.readLine()) != null) {
                                    ergo += data;
                                    Print(data);
                                }
                                if (ergo.length() < 3) {
                                    Print("Success!");
                                }
                                buffrdr.close();
                                q.destroy();
                            } else {
                                if (blOut.getName().length() > 20) {
                                   Print( blOut.getName().substring(0, 20) + "... exists, skipping ..");
                                } else {
                                    Print( blOut.getName() + " exists, skipping ..");
                                }
                            }
                        } catch (Exception e) {
                            Print("SYS_ERROR: Could not execute: " + exec + "\n"
                                    + e.getMessage() );
                            return "";
                        }
                    }
                }
            }
        }
        return "";
    }

    public static String ParseBlast() throws IOException {
        String error = "";
        String cgXML = PROFILE.getRootElement().getAttributeValue("cgXML");
        String queryFile = PROFILE.getRootElement().getAttributeValue("queryFile");
        String output = PROFILE.getRootElement().getAttributeValue("outputFolder");
        BRIG.dumpXMLDebug();
        try {
            FileWriter fstream = null;
            fstream = new FileWriter(cgXML, true);
            BufferedWriter out = new BufferedWriter(fstream);
            int GRAPH_MULTIPLIER = 3;
            if (PROFILE.getRootElement().getChild("brig_settings") != null) {
                if (PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("multiplier") != null) {
                    GRAPH_MULTIPLIER = Integer.parseInt(PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("multiplier"));
                }
            }
            List rings = BRIG.PROFILE.getRootElement().getChildren("ring");
            int i = 0;
            int stop = 1;
            String queryFastaFile = PROFILE.getRootElement().getAttributeValue("queryFastaFile");
            while (stop != 0) {
                stop = 0;
                for (int a = 0; a < rings.size(); a++) {
                    Element currentRing = (Element) rings.get(a);
                    if (Integer.parseInt(currentRing.getAttributeValue("position")) == i) {
                        List sequence = currentRing.getChildren("sequence");
                        List custom = currentRing.getChildren("feature");
                        if (sequence.size() > 0 || custom.size() > 0) {
                            Print("RING  " + currentRing.getAttributeValue("position"));
                            int size = 30;
                            if ( currentRing.getAttributeValue("size") != null ){
                                size = Integer.parseInt(currentRing.getAttributeValue("size"));
                            }
                            String location = "";
                            Print("Custom features: " + custom.size() );
                            Print("Sequences: " + sequence.size() );
                            if (sequence.size() > 0) {
                                Element currentSeq = (Element) sequence.get(0);
                                if (currentSeq.getAttributeValue("location") != null) {
                                    location = currentSeq.getAttributeValue("location");
                                    if (location.compareTo("GC Content") == 0 || location.compareTo("GC Skew") == 0 || location.endsWith(".graph")) {
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
                                        Element customElement = (Element) custom.get(h);
                                        List ranger = customElement.getChildren("featureRange");
                                        out.write("<feature color=\"" + customElement.getAttributeValue("colour") + "\" ");
                                        out.write(" decoration=\"" + customElement.getAttributeValue("decoration") + "\" ");
                                        if (customElement.getAttributeValue("label") != null && customElement.getAttributeValue("label").compareTo("") != 0) {
                                            out.write(" label= \"" + customElement.getAttributeValue("label") + "\" ");
                                        }
                                        out.write(">");
                                        out.newLine();
                                        for (int z = 0; z < ranger.size(); z++) {
                                            Element rangerElement = (Element) ranger.get(z);
                                            out.write("<featureRange start=\"" + rangerElement.getAttributeValue("start") + "\" stop=\"" + rangerElement.getAttributeValue("stop") + "\" />");
                                            out.newLine();
                                        }
                                        out.write("</feature>");
                                        out.newLine();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            int other = 0;
                            for (int k = 0; k < sequence.size(); k++) {
                                Element currentSeq = (Element) sequence.get(k);
                                if (currentSeq.getAttributeValue("location").compareTo("GC Content") == 0) {
                                    if(isEmbl(queryFile) || isGenbank(queryFile)){
                                        CGContent(out, output + BRIG.SL + "scratch" + BRIG.SL + BRIG.FetchFilename(queryFile) +".fna");
                                    }else{
                                        CGContent(out, queryFastaFile);
                                    }
                                    other++;
                                } else if (currentSeq.getAttributeValue("location").compareTo("GC Skew") == 0) {
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
                                    Print("Reading BLAST tab...");
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
            out.close();
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
     //   String inter = Main.PROFILE.getRootElement().getAttributeValue("interactive");
        String query = PROFILE.getRootElement().getAttributeValue("queryFile");
        String output = PROFILE.getRootElement().getAttributeValue("outputFolder");
        String outputFile = PROFILE.getRootElement().getAttributeValue("outputFile");
        String mem = "";
        if (PROFILE.getRootElement().getChild("brig_settings") != null) {
            if (PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("memory") != null) {
                mem = "-Xmx" + PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("memory") + "m";


            }
        }
        BRIG.PROFILE.getRootElement().getAttributeValue("cgXML");

        String exec = "java " + mem + "  -jar cgview" + File.separator
                + "cgview.jar -f "+imgFormat +  "  -i " + cgXML + " -o "
                + outputFile + "." + imgFormat;
     /*   if (inter.compareTo("yes") == 0) {
            new File(output + SL + FetchFilename(query) + "HTML").mkdir();
            exec = "java -jar cgview/cgview.jar "
                    + " -i " + cgXML
                    + " -s " + output + SL + FetchFilename(query) + "HTML"
                    + " -e F -L 300";


        }*/
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            Print(exec);
            p = r.exec(exec);
            InputStream istrm = p.getInputStream();
            InputStreamReader istrmrdr = new InputStreamReader(istrm);
            BufferedReader buffrdr = new BufferedReader(istrmrdr);
            String data;
            while ((data = buffrdr.readLine()) != null) {
                Print(data );
            }
            istrm = p.getErrorStream();
            istrmrdr = new InputStreamReader(istrm);
            buffrdr = new BufferedReader(istrmrdr);
            while ((data = buffrdr.readLine()) != null) {
                Print("SYS_ERROR:" + data );
            }
        } catch (IOException ex) {
            Print ("SYS_ERROR: Could not execute: " + exec + "\n"
                    + ex.getMessage() );
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

    private static String BlastMaster(Element currentRing, BufferedWriter out) throws IOException {
        String error = "";
        String reverseDb = PROFILE.getRootElement().getAttributeValue("reverseDb");
        reverseDb = null;
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
            List sequence = currentRing.getChildren("sequence");
            // Create new blast results file. merging all ring results
            String tput = PROFILE.getRootElement().getAttributeValue("outputFolder");
            String mergedResults = tput + SL + "scratch" + SL + "Ring"+currentRing.getAttributeValue("position") + "merge";
            File merge = new File(mergedResults);
            merge.deleteOnExit();
             FileWriter fstream = new FileWriter(mergedResults, true);
            BufferedWriter mer = new BufferedWriter(fstream);
            for (int z = 0; z < sequence.size(); z++) {
                String blastResults = ((Element) sequence.get(z)).getAttributeValue("blastResults");
                Print("Reading sequence: " + blastResults + "\t" + z);
                BufferedReader resul = null;
                try {
                    resul = new BufferedReader(new FileReader(blastResults.trim()));
                    while ((line = resul.readLine()) != null) {
                        mer.write(line);
                        mer.newLine();
                    }
                    resul.close();
                } catch (FileNotFoundException ex) {
                    error += "SYS_ERROR: Could not open file: "
                            + mergedResults.trim() + "\n"
                            + ex.getMessage() + "\n";
                    return error;
                }
            }
            mer.close();
            mergedResults = BRIG.reverseBlast(mergedResults, isPro);
            String[] col = currentRing.getAttributeValue("colour").split(",");
            int LOWER_INT = 50;
            if (currentRing.getAttributeValue("lowerInt").compareTo("") != 0 && currentRing.getAttributeValue("lowerInt") != null) {
                LOWER_INT = Integer.parseInt(currentRing.getAttributeValue("lowerInt"));
            }
            Color current = new Color(Integer.parseInt(col[0]), Integer.parseInt(col[1]), Integer.parseInt(col[2]));
            /* Read outputfile, parse so that identity influences base-colour, start, stop*/
            BufferedReader first = null;
            try {
                first = new BufferedReader(new FileReader(mergedResults.trim()));
            } catch (FileNotFoundException ex) {
                error += "SYS_ERROR: Could not open file: "
                        + mergedResults.trim() + "\n"
                        + ex.getMessage() + "\n";
                return error;
            }
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
                        /*     if (reverseDb != null) {
                        start = tabarray[6];
                        stop = tabarray[7];
                        if (Integer.parseInt(tabarray[6]) > Integer.parseInt(tabarray[7])) {
                        start = tabarray[7];
                        stop = tabarray[6];
                        }
                        }*/
                        if (multiFasta != 0) {
                            try {
                                String[] bestarray = line.split("\t");
                                String[] commaarray = bestarray[1].split(":");
                                if (reverseDb != null) {
                                    commaarray = bestarray[0].split(":");
                                }
                                if (BRIG.PROFILE.getRootElement().getAttribute("genbankProtein") != null || isPro) {
                                    start = Integer.toString(Integer.parseInt(commaarray[1]) + (Integer.parseInt(start) * 3));
                                    stop = Integer.toString(Integer.parseInt(commaarray[1]) + (Integer.parseInt(stop) * 3));
                                } else {
                                    start = Integer.toString(Integer.parseInt(commaarray[1]) + Integer.parseInt(start));
                                    stop = Integer.toString(Integer.parseInt(commaarray[1]) + Integer.parseInt(stop));
                                }
                            } catch (Exception e) {
                                Print("Could't read: " + line);
                                e.printStackTrace();
                            }
                        }
                        int red = OutputColour(LOWER_INT, current.getRed(), identity);
                        int green = OutputColour(LOWER_INT, current.getGreen(), identity);
                        int blue = OutputColour(LOWER_INT, current.getBlue(), identity);
                        String RGB = "rgb(" + red + "," + green + "," + blue + ")";
                        outLine = line + "\n";
                        /*    if (PROFILE.getRootElement().getAttributeValue("interactive").compareTo("yes") == 0) {
                        outLine = "<feature color=\"" + RGB + "\" decoration=\"arc\" mouseover= \"" + tabarray[1] + " (" + tabarray[8] + ".." + tabarray[9] + ")\" label=\""
                        + tabarray[2] + "%/" + tabarray[3] + "[" + tabarray[10] + "]\">"
                        + "<featureRange start=\"" + start + "\" stop=\"" + stop + "\" /></feature>";
                        } else */
                        if (currentRing.getAttributeValue("labels").compareTo("yes") == 0) {
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
            first.close();
            merge.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return error;
    }

    public static List existingResult(String instart, String instop, int[] existing) throws ArrayIndexOutOfBoundsException{
        List out = new LinkedList();
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
        Print("graphing: " + location);
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
        error += "Parsing graph file " + location + "\n";
        String col = currentRing.getAttributeValue("colour");
        BufferedReader first = null;
        try {
            first = new BufferedReader(new FileReader(location.trim()));
        } catch (FileNotFoundException ex) {
            error += "SYS_ERROR: Could not open file: "
                    + location.trim() + "\n"
                    + ex.getMessage() + "\n";
            return error;
        }
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
        String line = "";
        int lineNum = 1;
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
                System.err.println(line);
                e.printStackTrace();
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
        first.close();
        if (currentRing.getAttributeValue("legend").compareTo("yes") == 0) {
            String[] doop = new String[10];
            try {
                out.write("</featureSlot>\n");
                try {
                    first = new BufferedReader(new FileReader(location.trim()));
                } catch (FileNotFoundException ex) {
                }

                for (int j = 0; j < doop.length; j++) {
                    doop[j] = "-1";
                }
                int count = 0;
                while ((line = first.readLine()) != null) {
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
                Comparator comp = new RedComparator();
                Arrays.sort(doop, comp);
                out.write("<featureSlot strand=\"direct\" featureThickness=\"" + (FEATURE_THICKNESS / GRAPH_DIVIDER) + "\">\n");
                out.write("<feature color=\"black\" decoration=\"arc\">\n");
                for (int j = 0; j < doop.length; j++) {
                    String outLine = "";
                    if (doop[j].compareTo("-1") != 0) {
                        String poop = doop[j].replaceAll("#", "");
                        String[] tabarray = poop.split("\\s+");
                        /*if (INTERACTIVE || currentRing.get(2).toString().compareTo("1") == 0) {
                        outLine = "<featureRange color=\"blue\" start=\"" + tabarray[0] + "\" stop = \"" + tabarray[1] + "\" label=\"" +
                        tabarray[2] + "\" />\n";
                        } else {*/
                        if (j % 2 == 0 ){
                            outLine = "<featureRange color=\"red\" start=\"" + tabarray[0] + "\" stop = \"" + tabarray[1] + "\" ";
                        }else{
                            outLine = "<featureRange color=\"blue\" start=\"" + tabarray[0] + "\" stop = \"" + tabarray[1] + "\" ";
                        }
                        /*}*/
                            if (label != null){
                                if(label.compareTo("yes") ==0 ){
                                    outLine += " label= \"" + tabarray[2] + "\" ";
                                }
                            }
                        out.write(outLine + "/>\n");
                        out.newLine();
                    }
                }
                out.write("</feature>\n");
                first.close();
            } catch (Exception e) {
                e.printStackTrace();
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
        BufferedReader first = new BufferedReader(new FileReader(QUERY_MASTER_FILE));
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
        first.close();
        len = 0;
        cPlusG = 0;
        totalLen = 1;
        double radiusShift = 0.0;
        double barHeight = 0.0;
        error += ("Maximum value:  " + max + "\n");
        error += ("Minimum value:  " + min + "\n");
        error += ("Average value: " + average + "\n\n");
        first = new BufferedReader(new FileReader(QUERY_MASTER_FILE));
        int lineCount = 0;
        while ((line = first.readLine()) != null) {
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
        first.close();
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
        BufferedReader first = new BufferedReader(new FileReader(QUERY_MASTER_FILE));
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
        first.close();
        len = 0;
        cPlusG = 0;
        cMinusG = 0;
        totalLen = 1;
        double radiusShift = 0.0;
        double barHeight = 0.0;
        error += ("Maximum value:  " + max + "\n");
        error += ("Minimum value:  " + min + "\n");
        error += ("Average value: " + average + "\n\n");
        first = new BufferedReader(new FileReader(QUERY_MASTER_FILE));
        int lineCount = 0;
        while ((line = first.readLine()) != null) {
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
                        /*       out.write("<featureRange color=\"" + color + "\" start=\"" + totalLen + "\" stop=\"" + (totalLen + len) + "\" "
                        + "proportionOfThickness=\"" + barHeight + "\" radiusAdjustment=\"" + radiusShift + "\" />");
                        out.newLine();*/
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
        first.close();
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

    private static String[] sort(String[] input) {
        int n = input.length;
        for (int pass = 1; pass < n; pass++) {  // count how many times
            // This next loop becomes shorter and shorter
            for (int i = 0; i < n - pass; i++) {
                String[] jezz = input[i].split("\\s+");
                jezz[0] = jezz[0].replaceAll("#", "");
                String[] mezz = input[i + 1].split("\\s+");
                mezz[0] = mezz[0].replaceAll("#", "");
                int a = Integer.parseInt(jezz[0]);
                int b = Integer.parseInt(mezz[0]);
                if (a > b) {
                    // exchange elements
                    String temp = input[i];
                    input[i] = input[i + 1];
                    input[i + 1] = temp;
                }
            }
        }
        return input;
    }

    public  static int autoScale(int length  ){
        return (length / 3000);
    }
}

class RedComparator implements Comparator<String> {

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
