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

import java.io.*;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
/**
 *
 * @author nabil
 */
public class AnnoXML {

    /**
     * @param args the command line arguments
     */

    
    public static Document CreateFeatureXML(String inputFile,  boolean embl) {
        Document features = new Document();
        Element root = new Element("AnnoXML");
        features.addContent(root);
        try {
            BufferedReader first = new BufferedReader(new FileReader(inputFile));
            String line = "";
            Element child = new Element("blanko");
            Element noteElement = new Element("blanko");
            String helper = "";
            int on = 0;
            Vector notes = new Vector();
            Pattern header = Pattern.compile("^\\s{5}(\\S+)\\s+\\D+(\\d+)..(\\d+)");
            Pattern feature = Pattern.compile("^FEATURES");
            Pattern origin = Pattern.compile("^ORIGIN");
            Pattern note = Pattern.compile("^\\s+/(\\S+)=(.+)");
            if (embl) {
                header = Pattern.compile("^\\S{2}\\s{3}(\\S+)\\s+\\D+(\\d+)..(\\d+)");
                feature = Pattern.compile("^FH   Key.+");
                origin = Pattern.compile("^SQ");
                note = Pattern.compile("^\\S{2}\\s+/(\\S+)=(.+)");
            } 
            int lineNum = 0;
            while ((line = first.readLine()) != null) {             
                Matcher featureMatch = feature.matcher(line);
                Matcher originMatch = origin.matcher(line);
                if (originMatch.find()) {
                    on = 0;
                }
                if (on == 1) {
                    Matcher headerMatch = header.matcher(line);
                    if (headerMatch.find()) {
                        if (child.getName().compareTo("blanko") != 0) {
                            noteElement.setAttribute("value", helper);
                            notes.add(noteElement);
                            noteElement = new Element("blanko");
                            for (int i = 0; i < notes.size(); i++) {
                                child.addContent((Element) notes.get(i));
                            }
                            features.getRootElement().addContent(child);
                            notes = new Vector();
                        }
                        child = new Element(headerMatch.group(1));
                        if (line.contains("complement")) {
                            child.setAttribute("complement", "true");
                        } else {
                            child.setAttribute("complement", "false");
                        }
                        child.setAttribute("start", headerMatch.group(2));
                        child.setAttribute("stop", headerMatch.group(3));
                    } else {
                        Matcher noteMatch = note.matcher(line);
                        if (noteMatch.find()) {
                            noteElement.setAttribute("value", helper);
                            if (noteElement.getName().compareTo("blanko") != 0) {
                                notes.add(noteElement);
                            }
                            noteElement = new Element(noteMatch.group(1));
                            helper = noteMatch.group(2).trim();
                            helper = helper.replaceAll("\"", "");
                            helper = helper.replaceAll("=", " ");
                        } else {
                            String space = " ";
                            if (noteElement.getName().compareTo("translation") == 0) {
                                space = "";
                            }
                            if (embl) {
                                helper = helper + space + line.substring(2).replaceAll("\"", "").trim();
                            } else {
                                helper = helper + space + line.replaceAll("\"", "").trim();
                            }
                        }
                    }
                }
                if (featureMatch.find()) {
                    on = 1;
                }
                Matcher headerMatch = header.matcher(line);
                if (lineNum == 0 && headerMatch.find()) {
                    on = 1;
                }
                lineNum++;
            }
            features.getRootElement().addContent(child);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return features;
    }

    public static void printXML(Document PROFILE){
        try {
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat()) ;
            serializer.output(PROFILE, System.out);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    public static int dumpXMLfile(Document PROFILE, String output){
        try {
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat()) ;
            FileWriter fstream4 = new FileWriter(output);
            BufferedWriter out1 = new BufferedWriter(fstream4);
            serializer.output(PROFILE, out1);
            out1.close();
            return 0;
            
        } catch (IOException e) {
            System.err.println(e);
            return 1;
        }

    }
    
    public static void main(String[] args) {
    //String gen = "/home/nabil/genomes/Escherichia_coli_O157H7/NC_002695.gbk";
    String gen = "/home/nabil/BA000007.embl";
    printXML(CreateFeatureXML(gen,true));
        /*  String inputGbk = "/home/nabil/B47/EcB47.gbk";
            String output = "/home/nabil/B47/hits.txt";
            String inputWords = "/home/nabil/B47/buzzWords.txt";
            String output1 = "/home/nabil/B47/hitsSummary.txt";
            FileWriter fstream = new FileWriter(output);
            BufferedWriter out = new BufferedWriter(fstream);
            FileWriter fstream2 = new FileWriter(output1);
            BufferedWriter out2 = new BufferedWriter(fstream2);
            BufferedReader first = new BufferedReader(new FileReader(inputGbk));
            String line = "";
            int on = 0;
            System.out.println("start");
            String feat = "";
            String prevFeat = "";
            ArrayList<ArrayList> summary = new ArrayList();
            ArrayList<String> entry = new ArrayList();
            BufferedReader second = new BufferedReader(new FileReader(inputWords));
            while ((line = first.readLine()) != null) {
                Pattern header = Pattern.compile("\\s{5}\\S+\\s+\\d+..\\d+");
                Matcher headerMatch = header.matcher(line);
                Pattern header2 = Pattern.compile("\\s{5}\\S+\\s+complement\\S+\\d+..\\d+");
                Matcher headerMatch2 = header2.matcher(line);
                Pattern feature = Pattern.compile("^FEATURES\\s+");
                Matcher featureMatch = feature.matcher(line);
                Pattern origin = Pattern.compile("^ORIGIN\\s+");
                Matcher originMatch = origin.matcher(line);
                if (featureMatch.find()) {
                    on = 1;
                }
                if (originMatch.find()) {
                    on = 0;
                }
                if (on == 1) {
                    if (headerMatch.find() || headerMatch2.find()) {
                        prevFeat = feat;
                        feat = line + "\n";
                        if (prevFeat.length() > 1) {
                            second = new BufferedReader(new FileReader(inputWords));
                            buzzLine = "";
                            int countHeader = 0;
                            int totalHead = -1;
                            int outHit = 0;
                            String currentBuzz = "";
                            while ((buzzLine = second.readLine()) != null) {
                                buzzLine = buzzLine.trim();
                                if (buzzLine.contains(">")) {
                                    countHeader = 0;
                                    totalHead++;
                                }
                                if (!buzzLine.contains("#") && !buzzLine.contains(">") && buzzLine.length() > 0) {
                                    String[] buzzArray = (buzzLine.trim()).split("\\s+");
                                    countHeader++;
                                    for (int i = 0; i < buzzArray.length; i++) {
                                        if (prevFeat.contains(buzzArray[i].trim())) {
                                            try {
                                                currentBuzz = ">" + summary.get(totalHead).get(0).toString() + "\t" + buzzArray[i] ;
                                                String yes = summary.get(totalHead).get(countHeader).toString() + "*";
                                                summary.get(totalHead).set(countHeader, yes);
                                                outHit = 1;
                                            } catch (Exception e2) {
                                                System.err.println("error parsing: " + prevFeat);
                                            }
                                        }
                                    }
                                }
                            }
                            if (outHit == 1) {
                                out.write(currentBuzz);
                                out.newLine();
                                out.write(prevFeat);
                                out.newLine();
                            }
                            second.close();
                        }
                    } else {
                        feat += (line.trim() + "\n");
                    }

            }
            first.close();
            out.close();
            for (int k = 0; k < summary.size(); k++) {
                ArrayList current = summary.get(k);
                for (int g = 0; g < current.size(); g++) {
                    out2.write( current.get(g).toString() + "\t");
                }
                out2.write("\n");
            }
            out2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }
}

