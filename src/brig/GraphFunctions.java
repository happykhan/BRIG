/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nabil
 */
public class GraphFunctions {
       private static final Logger log = LoggerFactory.getLogger(GraphFunctions.class);
       public static String SL = File.separator;



public static String parseBlast2Graph(int[] value, int[] repeats, String input, String output, boolean multi,int div, int len) {
    String error = "";
        String line = "";
        try (BufferedWriter out1 = new BufferedWriter(new FileWriter(output));
             BufferedWriter out2 = new BufferedWriter(new FileWriter(output + "rep.graph"))) {
            BufferedReader first;
            try {
                first = new BufferedReader(new FileReader(input.trim()));
            } catch (FileNotFoundException ex) {
                error += "SYS_ERROR: Could not open file: " +
                        input.trim() + "\n" +
                        ex.getMessage() + "\n";
                return error;
            }
            try (first) {
                String prevLine = "";
                String bestLine = "";
                if ((line = first.readLine()) != null) {
                    prevLine = line;
                    bestLine = line;
                }
                int maxValue = 0;
                int maxRepeatvalue = 0;
                while ((line = first.readLine()) != null) {
                    String[] tabarray = line.split("\t");
                    String[] prevarray = prevLine.split("\t");
                    String[] bestarray = bestLine.split("\t");
                    if (tabarray[1].compareTo(prevarray[1]) == 0) {
                        if (Double.parseDouble(tabarray[11]) > Double.parseDouble(bestarray[11])) {
                            bestLine = line;
                        } else {
                            int start = Integer.parseInt(tabarray[6]) / div;
                            int stop = Integer.parseInt(tabarray[7]) / div;
                            String[] commaarray = tabarray[0].split(":");
                            if (multi) {
                                start = (Integer.parseInt(commaarray[1]) + Integer.parseInt(tabarray[6])) / div;
                                stop = (Integer.parseInt(commaarray[1]) + Integer.parseInt(tabarray[7])) / div;
                            }
                            int labelStart = Integer.parseInt(tabarray[6]);
                            if (labelStart == 0) {
                                labelStart = 1;
                            }
                            if (multi && (Integer.parseInt(commaarray[1]) + Integer.parseInt(tabarray[7])) < len) {
                                out2.write("#" + (Integer.parseInt(commaarray[1]) + Integer.parseInt(tabarray[6])) + "\t" + (Integer.parseInt(commaarray[1]) + Integer.parseInt(tabarray[7])) + "\t" + commaarray[0] + "\n");
                            } else if (Integer.parseInt(tabarray[7] ) < len ) {
                                out2.write("#" + tabarray[6] + "\t" + tabarray[7] + "\t" + tabarray[1] + "\n");
                            }
                            if (start == stop && repeats.length > start) {
                                repeats[start] = repeats[start] + 1;
                                if (repeats[start] > maxRepeatvalue) {
                                    maxRepeatvalue = repeats[start];
                                }
                            } else if (repeats.length > start) {
                                for (int j = start; j < stop; j++) {
                                    repeats[j] = repeats[j] + 1;
                                    if (repeats[start] > maxRepeatvalue) {
                                        maxRepeatvalue = repeats[start];
                                    }
                                }
                            }
                        }
                    } else {
                        bestarray = bestLine.split("\t");
                        /*Now publish the best line */
                        int start = Integer.parseInt(bestarray[6]) / div;
                        int stop = Integer.parseInt(bestarray[7]) / div;
                        String[] commaarray = bestarray[0].split(":");
                        if (multi) {
                            start = (Integer.parseInt(commaarray[1]) + Integer.parseInt(bestarray[6]))/div;
                            stop = (Integer.parseInt(commaarray[1]) + Integer.parseInt(bestarray[7])) / div;
                        }
                        /*output to array*/
                        if (multi) {
                            if((Integer.parseInt(commaarray[1]) + Integer.parseInt(bestarray[7])) < len){
                            out1.write("#" + (Integer.parseInt(commaarray[1]) + Integer.parseInt(bestarray[6])) + "\t" + (Integer.parseInt(commaarray[1]) + Integer.parseInt(bestarray[7])) + "\t" + commaarray[0] + "\n");
                            }
                        } else {
                            if ( Integer.parseInt(bestarray[7]) < len) {
                            out1.write("#" + bestarray[6] + "\t" + bestarray[7] + "\t" + commaarray[0] + "\n");
                            }
                        }
                        if (start == stop && repeats.length > start) {
                            value[start] = value[start] + 1;
                            if (value[start] > maxValue) {
                                maxValue = value[start];
                            }
                        } else if (repeats.length > start) {
                            for (int j = start; j < stop; j++) {
                                value[j] = value[j] + 1;
                                if (value[j] > maxValue) {
                                    maxValue = value[j];

                                }
                            }
                        }
                        bestLine = line;
                    }
                    prevLine = line;
                }
                out1.newLine();
                for (int x = 0; x < value.length; x++) {
                    if (value[x] > 0) {
                        double prop = (double) value[x];
                        if( ((x + 1) * div) < len ){
                        out1.write((x * div) + "\t" + ((x + 1) * div) + "\t" + prop);
                        out1.newLine();
                        }
                    }
                }
                log.debug("maxValue: {}", maxValue);
                log.debug("max: {}", maxRepeatvalue);
                out2.newLine();
                for (int x = 0; x < repeats.length; x++) {
                    if (repeats[x] > 0) {
                        log.debug("rep: {}", repeats[x]);
                        double prop = (double) repeats[x];
                        log.debug("prop: {}", prop);
                        if( ((x + 1) * div) < len ){
                        out2.write((x * div) + "\t" + ((x + 1) * div) + "\t" + prop);
                        out2.newLine();
                        }
                    }
                }
            } finally {
                first.close();
            }
        } catch (Exception e) {
            log.error("Error parsing BLAST results to graph", e);
        }
        return error;
    }

    @SuppressWarnings("rawtypes")
    public static String RunGraphBlast(String queryFile, Vector<Vector> reference,
            String output, String blastOptions, String queryFastaFile) {
        Pattern inputpattern = Pattern.compile(".gbk$");
        Matcher inputmatcher = inputpattern.matcher(queryFile);
        boolean isGbk = inputmatcher.find();
        String blastLocation = "";
        if (BRIG.PROFILE.getRootElement().getChild("brig_settings") != null) {
            if (BRIG.PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation") != null) {
                blastLocation = BRIG.PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation");
                if (!(BRIG.PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation").endsWith(BRIG.SL))) {
                    if (!BRIG.PROFILE.getRootElement().getChild("brig_settings").getAttributeValue("blastLocation").isEmpty()) {
                        blastLocation += BRIG.SL;
                    }
                }
            }
        }
        String error = "";
        for (int i = 0; i < reference.size(); i++) {
                @SuppressWarnings("rawtypes")
                Vector currentRing = reference.get(i);
                for (int j = 3; j < currentRing.size(); j++) {
                    if(!"GC Skew".equals(currentRing.get(j).toString()) &&
                            !"GC Content".equals(currentRing.get(j).toString())) {
                    String db = currentRing.get(j).toString();
                    if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
                        db = db.replaceAll(SL + SL, SL);
                    }
                    if (!(db.endsWith(".graph"))) {
                        Pattern fnapattern = Pattern.compile(".fna$");
                        Matcher fnamatcher = fnapattern.matcher(db);

                        Pattern faapattern = Pattern.compile(".faa$");
                        Matcher faamatcher = faapattern.matcher(db);
                        if (faamatcher.find() || fnamatcher.find()) {
                            faamatcher = faapattern.matcher(db);
                            fnamatcher = fnapattern.matcher(db);
                            String pOption = "T";
                            String blastProg = "blastn";
                            if (fnamatcher.find()) {
                                pOption = "F";
                                if (isGbk) {
                                    blastProg = "tblastn";
                                } else {
                                    blastProg = "blastn";
                                }
                            } else if (faamatcher.find()) {
                                pOption = "T";
                                if (isGbk) {
                                    blastProg = "blastp";
                                } else {
                                    blastProg = "blastx";
                                }
                            } else if (!(faamatcher.find()) || !(fnamatcher.find())) {
                                error += "SYS_ERROR: " + db +
                                        " is not a valid file. BLAST fail\n";

                                return error;
                            }
                            List<String> dbCmd = new ArrayList<>();
                            String pro = "T".equals(pOption) ? "prot" : "nucl";
                            dbCmd.addAll(Arrays.asList(blastLocation + "makeblastdb", "-dbtype", pro, "-in", db));
                            try {
                                error += BRIG.formatCommand(dbCmd) + "\n";
                                String data = "";
                                Process p = BRIG.execCommand(dbCmd);
                                String ergo = "";
                                try (BufferedReader buffrdr = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                                    while ((data = buffrdr.readLine()) != null) {
                                        ergo += data + "\n";
                                    }
                                }
                                if (ergo.length() > 3) {
                                    error += ergo;
                                    return error;
                                }
                                error += "Success!\n";
                            } catch (Exception e) {
                                error += "SYS_ERROR: Could not execute: " + BRIG.formatCommand(dbCmd) + "\n";
                                return error;
                            }
                            String ou = "";
                            List<String> blastCmd = new ArrayList<>();
                            try {
                                String queryArg;
                                if (isGbk) {
                                    ou = output + SL + "scratch" + SL +  BRIG.FetchFilename(queryFastaFile) + "Vs" + BRIG.FetchFilename(db) + ".tab";
                                    queryArg = queryFastaFile;
                                } else {
                                    ou = output + SL + "scratch" + SL + BRIG.FetchFilename(queryFile) + "Vs" + BRIG.FetchFilename(db) + ".tab";
                                    queryArg = queryFile;
                                }
                                if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
                                    ou = ou.replaceAll(SL + SL, SL);
                                }
                                blastCmd.addAll(Arrays.asList(blastLocation + blastProg, "-outfmt", "6", "-query", queryArg, "-db", db, "-out", ou));
                                blastCmd.addAll(BRIG.tokenizeOptions(blastOptions));
                                int graphThreads = BlastSettings.getBlastThreads();
                                if (graphThreads > 1 && (blastOptions == null || !blastOptions.contains("-num_threads"))) {
                                    blastCmd.addAll(Arrays.asList("-num_threads", String.valueOf(graphThreads)));
                                }
                                error += BRIG.formatCommand(blastCmd) + "\n";
                                Process q = BRIG.execCommand(blastCmd);
                                String data;
                                String ergo = "";
                                try (BufferedReader buffrdr = new BufferedReader(new InputStreamReader(q.getErrorStream()))) {
                                    while ((data = buffrdr.readLine()) != null) {
                                        ergo += data + "\n";
                                    }
                                }
                                if (ergo.length() > 3) {
                                    error += ergo;
                                    return error;
                                }
                                error += "Success!\n";
                                q.destroy();
                                /* save output file to array */
                            } catch (Exception e) {
                                error += "SYS_ERROR: Could not execute: " + BRIG.formatCommand(blastCmd) + "\n" +
                                        e.getMessage() + "\n";
                                return error;
                            }
                        }
                    }
                }
            }
        }
        return error;
    }



}
