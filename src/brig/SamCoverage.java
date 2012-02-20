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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nabil
 */
public class SamCoverage {
    
    public static void main(String args[]) {
        try {
            if(args.length >= 1 ){
            SamcontigCoverage(args[0], "", "");
            }else{
                System.out.println("USAGE: SamCoverage converts a standard SAM file into a .graph file for use in BRIG.\n"
                        + "Coverage information is dumped to standard out, please pipe to .graph file for use in BRIG.");
                System.out.println("ERROR: Please specify location of a .sam file.");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


     public static void SamcontigCoverage(String samFile, String outDirectory, String file) throws IOException {
        int[] map = new int[1];
     //  FileWriter fstream5 = new FileWriter(outDirectory + Main.SL + Main.FetchFilename(file) + ".graph");
      //  BufferedWriter out = new BufferedWriter(fstream5);
        BufferedReader in = new BufferedReader(new FileReader(samFile));
        String line = "";
        Pattern SQpattern = Pattern.compile("^@SQ.+LN:(\\d+)");
        int length = 0;
        while ((line = in.readLine()) != null) {
            if (!line.startsWith("@")) {
                String[] lineArray = line.split("\t");
                int start = Integer.parseInt( lineArray[3] );
                int stop = start + lineArray[9].length();
                if (start < stop) {
                    for (int j = start; j < stop; j++) {
                       try {
                        map[j] = map[j] + 1;
                        }catch (Exception e ){
                            e.printStackTrace();
                        }
                    }
                } else if (start == stop) {
                    map[start] = map[start] + 1;
                }
            } else if (line.startsWith("@SQ")) {
                Matcher COmatcher = SQpattern.matcher(line);
                if (COmatcher.find()) {
                    length = Integer.parseInt(COmatcher.group(1));
                    map = new int[length];
                    System.out.println("# " + length);
                }
            }
        }
        in.close();
        int hund = 0;
        int hundInc = 0;
        int prevHund = 0;
        for (int i = 0; i < map.length; i++) {
            if (map[i] != -1) {
                if (hundInc >= 1000) {
                    //      System.out.println(pretemp + "\t" + prevHund + "\t" + hundInc + "\t" + hund);
           //         out.write((prevHund) + "\t" + (prevHund + hundInc) + "\t" + ((double) hund / (double) hundInc));
                    System.out.println( ((prevHund) + "\t" + (prevHund + hundInc) + "\t" + ((double) hund / (double) hundInc))   );
               //     out.newLine();
                    prevHund += hundInc;
                    hundInc = 0;
                    hund = 0;
                }
        //        System.out.println(map[i]);
                hund += map[i];
                hundInc++;
            }
        }
        if (hundInc > 0) {
      //      out.write((prevHund) + "\t" + (prevHund + hundInc) + "\t" + ((double) hund / (double) hundInc));
            System.out.println( (prevHund) + "\t" + (prevHund + hundInc) + "\t" + ((double) hund / (double) hundInc)  );
     //       out.newLine();
        }
    //    out.close();
    }

}
