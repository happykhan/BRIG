/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package brig;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author nabil
 */
public class BRIGconsole implements Runnable {
    private Thread reader;
    private JFrame frame;
    private final JTextArea textArea;
    private boolean quit;
    private final PipedInputStream pin ;
    
    
    public BRIGconsole(PipedOutputStream OUT  ) throws IOException {
        // create all components and add them
        frame = new JFrame("BRIG Console");
        frame.setBounds(800, 100, 300, 400);
        pin = new PipedInputStream(OUT);
        //PrintStream ps = new PrintStream(OUT);
        
        textArea = new JTextArea(5,20);
        final JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            BoundedRangeModel brm = scrollPane.getVerticalScrollBar().getModel();
            boolean wasAtBottom = true;
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!brm.getValueIsAdjusting()) {
                    if (wasAtBottom) {
                        brm.setValue(brm.getMaximum());
                    }
                } else {
                    wasAtBottom = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());
                }

            }
        });
        textArea.setVisible(true);
        textArea.setEditable(false);
        frame.add(scrollPane);
        frame.setVisible(true);
        
       
        reader = new Thread(this);
        reader.setDaemon(true);
        reader.start();
    }


    @Override
    public synchronized void run() {
        try {
            while (Thread.currentThread() == reader) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {
                }
                if (pin.available() != 0) {
                    String input = this.readLine(pin);
                    textArea.append(input);
                }
                if (quit) {
                    return;
                }
            }

        } catch (Exception e) {
            textArea.append("\nConsole reports an Internal error.");
            textArea.append("The error is: " + e);
        }
    }

    public synchronized String readLine(PipedInputStream in) throws IOException {
        String input = "";
        do {
            int available = in.available();
            if (available == 0) {
                break;
            }
            byte b[] = new byte[available];
            in.read(b);
            input = input + new String(b, 0, b.length);
        } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
        return input;
    }


}
