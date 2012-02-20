/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package brig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author nabil
 */
public class BRIGrun {

    public static void main(String args[] ){
    try{
        File dir = new File("/home/nabil/iprave/seq");
    File[] der = dir.listFiles();
    for(File tem : der  ){
        if(tem.getName().endsWith("2M.fasta")){
            BufferedReader in = new BufferedReader(new FileReader(tem));
            File outer = new File(tem.getAbsolutePath().replaceAll("2M", "500") );
            BufferedWriter out = new BufferedWriter(new FileWriter(outer)  ) ;
            String line = "" ;
            int count = 0;
            while(count < 1000000 &&  (line = in.readLine()) != null ){
                out.write(line);
                out.newLine();
                count++;
            }
            out.close();
            in.close();

        }
    }
        
    
    }catch(Exception e ) {
         e.printStackTrace(); 
    }
    }
}
