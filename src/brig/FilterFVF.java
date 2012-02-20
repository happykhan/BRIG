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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author nabil
 */
public class FilterFVF {

    public static void main(String args[]) throws FileNotFoundException, IOException {
        String input = "/Users/nabil/BRIG_PROJECT/BRIGMANUAL/brig3/VFs.ffn";
        String outFile = "/Users/nabil/BRIG_PROJECT/BRIGMANUAL/brig3/VFs1.ffn";
        BufferedReader first = new BufferedReader(new FileReader(input));
        String line = "";
        FileWriter fstream4 = new FileWriter(outFile);
        BufferedWriter out = new BufferedWriter(fstream4);
        while ((line = first.readLine()) != null) {
            if(line.contains(">") ){
                out.write(">" + (line.split(" "))[1]);
                out.newLine();
            }else{
                out.write(line);
                out.newLine();
            }
        }

    }
}
