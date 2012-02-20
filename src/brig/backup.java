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

/**
 *
 * @author nabil
 */
public class backup {
/*
    public static int FastaLength(String file) throws FileNotFoundException, IOException {
        BufferedReader in = null;
        String line = null;
        int caret = 0;
        in = new BufferedReader(new FileReader(file));
        while ((line = in.readLine()) != null) {
            if (!line.contains(">")) {
                line.trim();
                char lineArray[] = line.toCharArray();
                for (int i = 0; i < lineArray.length; i++) {
                    if (lineArray[i] == '-' || lineArray[i] == 'A' || lineArray[i] == 'C' || lineArray[i] == 'T' || lineArray[i] == 'G' || lineArray[i] == 'N'
                            || lineArray[i] == 'a' || lineArray[i] == 'c' || lineArray[i] == 't' || lineArray[i] == 'g' || lineArray[i] == 'n') {
                        caret++;
                    }
                }
            }
        }
        in.close();
        return caret;
    }

 */

    /*
    public static String formatMultiFASTA2(String input, String output, int spacer, boolean protein) throws FileNotFoundException, IOException {
        String error = "Multi-FASTA detected, removing headers\n";
        BufferedReader first = new BufferedReader(new FileReader(input));
        FileWriter fstream4 = new FileWriter(output);
        BufferedWriter out = new BufferedWriter(fstream4);
        String line = "";
        int head = 0;
        String spacerString = "";
        String charx = "N";
        if (protein){
            charx = "X";
        }
        for (int i = 0; i < spacer; i++) {
            spacerString += charx;
        }
        String temp = "";
        while ((line = first.readLine()) != null) {
            while (temp.length() > 100) {
                out.write(temp.substring(0, 100));
                out.newLine();
                temp = temp.substring(100);
            }
            if (line.startsWith(">")) {
                head++;
                if (head > 1) {
                    temp += spacerString;
                } else if (head == 1) {
                    out.write(line);
                    out.newLine();
                }
            } else {
                temp += line;
            }
        }
        out.write(WordWrap(temp));
        out.close();
        first.close();
        return error;
    }
*/
/*

    private static String BlastMaster(Element currentRing, BufferedWriter out) throws IOException {
        String error = "";
        try {
            String query = PROFILE.getRootElement().getAttributeValue("queryFastaFile");
            String output = PROFILE.getRootElement().getAttributeValue("outputFolder");
            int spacer = -1;
            if(PROFILE.getRootElement().getAttributeValue("spacer") != null ){
                spacer = Integer.parseInt(PROFILE.getRootElement().getAttributeValue("spacer"));
            }
            String blastType = currentRing.getAttributeValue("blastType");
            int minimus = 0;
            try {
                Element settings = PROFILE.getRootElement().getChild("brig_settings");
                if (settings != null) {
                    minimus = Integer.parseInt(settings.getAttributeValue("defaultMinimum"));
                }
            } catch (Exception e) {
                minimus = 0;
            }
            boolean isProtein = false;
            String outLine = "";
            String line = "";
            FileWriter fstream2 = null;
            FileWriter fstream3 = null;
            try {
                fstream2 = new FileWriter(output + SL + FetchFilename(query) + ".tab");
                fstream3 = new FileWriter(output + SL + FetchFilename(query) + ".gbk");
            } catch (IOException ex) {
                error += ("SYS_ERROR: Could not open output file: "
                        + output + SL + FetchFilename(query) + ".(.tab or .gbk)..\n"
                        + ex.getMessage() + "\n");
                return error;
            }
            BufferedWriter out2 = new BufferedWriter(fstream2);
            BufferedWriter out3 = new BufferedWriter(fstream3);
            if (blastType.compareTo("blastp") == 0 || blastType.compareTo("blastx") == 0) {
                isProtein = true;
            }
            List sequence = currentRing.getChildren("sequence");
            Main.dumpXMLDebug();
            for (int z = 0; z < sequence.size(); z++) {
                System.out.println("NOT GRAPH current ring" + z);
                String blastResults = ((Element) sequence.get(z)).getAttributeValue("blastResults");
                String[] col = currentRing.getAttributeValue("colour").split(",");
                int LOWER_INT = 50;
                if (currentRing.getAttributeValue("lowerInt").compareTo("") != 0 && currentRing.getAttributeValue("lowerInt") != null) {
                    LOWER_INT = Integer.parseInt(currentRing.getAttributeValue("lowerInt"));
                }
                Color current = new Color(Integer.parseInt(col[0]), Integer.parseInt(col[1]), Integer.parseInt(col[2]));
                // Read outputfile, parse so that identity influences base-colour, start, stop
                BufferedReader first = null;
                try {
                    first = new BufferedReader(new FileReader(blastResults.trim()));
                } catch (FileNotFoundException ex) {
                    error += "SYS_ERROR: Could not open file: "
                            + blastResults.trim() + "\n"
                            + ex.getMessage() + "\n";
                    return error;
                }
                String prevLine = "";
                String bestLine = "";
                if (isProtein) {
                    if ((line = first.readLine()) != null) {
                        prevLine = line;
                        bestLine = line;
                    }
                }
                while ((line = first.readLine()) != null) {
                    String start = "";
                    String stop = "";
                    int identity = 0;
                    String[] tabarray = line.split("\t");
                    if (isProtein) {
                        String[] prevarray = prevLine.split("\t");
                        String[] bestarray = bestLine.split("\t");
                        if (tabarray[0].compareTo(prevarray[0]) == 0) {
                            if (Double.parseDouble(tabarray[11]) > Double.parseDouble(bestarray[11])) {
                                bestLine = line;
                            }
                        } else {
                            //Now publish the best line
                            // normalize identity
                            String[] commaarray = bestarray[0].split(":");
                            Double totalLen = (Double.parseDouble(commaarray[2]) / 3) - (Double.parseDouble(commaarray[1]) / 3);
                            Double querLen = Double.parseDouble(bestarray[7]) - Double.parseDouble(bestarray[6]);
                            Double ideity = (Double.parseDouble(bestarray[2]) * querLen) / totalLen;
                            //output to file
                            identity = ideity.intValue();
                            start = commaarray[1];
                            stop = commaarray[2];
                            if (Integer.parseInt(commaarray[2]) > (GEN_LENGTH - 1)) {
                                stop = Integer.toString(GEN_LENGTH - 1);
                            }
                            if (Integer.parseInt(commaarray[1]) > (GEN_LENGTH - 1)) {
                                start = Integer.toString(GEN_LENGTH - 5);
                            }
                            identity = (int) (Double.parseDouble(tabarray[2]));
                            int red = OutputColour(LOWER_INT, current.getRed(), identity);
                            int green = OutputColour(LOWER_INT, current.getGreen(), identity);
                            int blue = OutputColour(LOWER_INT, current.getBlue(), identity);
                            String RGB = "rgb(" + red + "," + green + "," + blue + ")";
                            outLine = line + "\n";
                            out2.write(outLine);
                            out2.newLine();
                            outLine = "     misc_feature    " + start + ".." + stop;
                            out3.write(outLine);
                            out3.newLine();
                            outLine = "                         /colour= " + 1;
                            out3.write(outLine);
                            out3.newLine();
                            outLine = "                         /label=\"" + currentRing.getAttributeValue("name") + "\"";
                            out3.write(outLine);
                            out3.newLine();
                            outLine = "                         /note=\"" + currentRing.getAttributeValue("name") + "\"";
                            out3.write(outLine);
                            out3.newLine();
                            if (PROFILE.getRootElement().getAttributeValue("interactive").compareTo("yes") == 0) {
                                outLine = "<feature color=\"" + RGB + "\" decoration=\"arc\" mouseover= \"" + tabarray[1] + " (" + tabarray[8] + ".." + tabarray[9] + ")\" label=\""
                                        + tabarray[2] + "%/" + tabarray[3] + "[" + tabarray[10] + "]\">"
                                        + "<featureRange start=\"" + start + "\" stop=\"" + stop + "\" /></feature>";
                            } else {
                                outLine = "<feature color=\"" + RGB + "\" decoration=\"arc\" >"
                                        + "<featureRange start=\"" + start + "\" stop=\"" + stop + "\" /></feature>";
                            }
                            out.write(outLine);
                            out.newLine();
                            // Reset the bestLine and begin again
                            bestLine = line;
                        }
                        prevLine = line;
                    } else {

                        start = tabarray[6];
                        stop = tabarray[7];
                        if (Integer.parseInt(tabarray[7]) > (GEN_LENGTH - 1)) {
                            stop = Integer.toString(GEN_LENGTH - 1);
                        }
                        identity = (int) (Double.parseDouble(tabarray[2]));
                        if (identity >= minimus) {
                            int red = OutputColour(LOWER_INT, current.getRed(), identity);
                            int green = OutputColour(LOWER_INT, current.getGreen(), identity);
                            int blue = OutputColour(LOWER_INT, current.getBlue(), identity);
                            String RGB = "rgb(" + red + "," + green + "," + blue + ")";
                            outLine = line + "\n";
                            out2.write(outLine);
                            out2.newLine();
                            outLine = "     misc_feature    " + start + ".." + stop;
                            out3.write(outLine);
                            out3.newLine();
                            outLine = "                         /colour= " + 1;
                            out3.write(outLine);
                            out3.newLine();
                            outLine = "                         /label=\"" + currentRing.getAttributeValue("interactive") + "\"";
                            out3.write(outLine);
                            out3.newLine();
                            outLine = "                         /note=\"" + currentRing.getAttributeValue("interactive") + "\"";
                            out3.write(outLine);
                            out3.newLine();
                            if (PROFILE.getRootElement().getAttributeValue("interactive").compareTo("yes") == 0) {
                                outLine = "<feature color=\"" + RGB + "\" decoration=\"arc\" mouseover= \"" + tabarray[1] + " (" + tabarray[8] + ".." + tabarray[9] + ")\" label=\""
                                        + tabarray[2] + "%/" + tabarray[3] + "[" + tabarray[10] + "]\">"
                                        + "<featureRange start=\"" + start + "\" stop=\"" + stop + "\" /></feature>";
                            } else if (currentRing.getAttributeValue("labels").compareTo("yes") == 0) {
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
            }
            out2.close();
            out3.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return error;
    }

*/

}
