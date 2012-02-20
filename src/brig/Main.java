/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package brig;

import java.io.*;
/**
 *
 * @author nabil
 */
public class Main {
    static PipedOutputStream OUT = new PipedOutputStream();
 
    public static void main(String[] args) {
        try {
            
            BRIGconsole console = new BRIGconsole(OUT);
            Print("BRIG console active...");
            
            Thread BrigMain = new Thread (new BRIG(OUT), "BRIG-Main");
            BrigMain.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    static void Print(String message) throws IOException {
        String threadName = Thread.currentThread().getName();
        String out = "[" + threadName + "] " + message + "\n";
        OUT.write(out.getBytes());
    }

    
}
