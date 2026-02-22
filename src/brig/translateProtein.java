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
 * You should have received a copy of the GNU General Public License
 *  along with BRIG.  If not, see <http://www.gnu.org/licenses/>.
 */


package brig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 *
 * @author Nabil
 */
public class translateProtein {
    Hashtable proteins = new Hashtable();

    translateProtein() throws IOException {
        this(translateProtein.class.getResourceAsStream("/brig/resources/proteins.txt"));
    }

    translateProtein(InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("proteins.txt resource not found on classpath");
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] tabArray = line.split("\t");
                if (tabArray.length >= 2) {
                    proteins.put(tabArray[0], tabArray[1]);
                }
            }
        }
    }

    public String translateSeq(String input) {
        String out = "";
        int numCodons = input.length() / 3 ;
        input = input.substring(0,numCodons*3);
        for (int i = 0; i < input.length(); i += 3) {
            if(proteins.get(input.substring(i, i + 3).toUpperCase()) != null ){
                out += proteins.get(input.substring(i, i + 3).toUpperCase()).toString();
            }
        }
        return out;
    }
}
