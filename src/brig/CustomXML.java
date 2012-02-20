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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Nabil
 */
public class CustomXML extends javax.swing.JFrame {

    private DefaultListModel refModel;
    private DefaultListModel ringModel;
    /** Creates new form CGHeader */
    private int ELEMENT_INT = 0;
    private int RINGPOS = 0;
    private Element currentRing;
    Two gotthis = null ;
    public CustomXML() {
  refModel = new DefaultListModel();
        ringModel = new DefaultListModel();
        initComponents();
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    save();
                    ELEMENT_INT = jList1.locationToIndex(e.getPoint());
                    reload();
                }
            }
        };
        jList1.addMouseListener(mouseListener);
        List doop = BRIG.PROFILE.getRootElement().getChildren("ring");
        currentRing = (Element) doop.get(BRIG.POSITION);
        for (int z = 0; z < doop.size(); z++) {
            if (((Element) doop.get(z)).getAttributeValue("position").compareTo(Integer.toString(BRIG.POSITION)) == 0) {
                currentRing = (Element) doop.get(z);
            }
        }
        if (BRIG.PROFILE.getRootElement().getAttributeValue("spacer") != null) {
            inputData.addItem("Multi-FASTA");
        }
        MouseListener mouseListener2 = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    save();
                    labelField.setText("");
                    startField.setText("");
                    stopField.setText("");
                    RINGPOS = ringu.locationToIndex(e.getPoint());

                    reload();
                }
            }
        };
        ringu.addMouseListener(mouseListener2);
        try {
            reload();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    
    public CustomXML(Two got) {
        refModel = new DefaultListModel();
        ringModel = new DefaultListModel();
        gotthis = got;
        initComponents();
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    save();
                    ELEMENT_INT = jList1.locationToIndex(e.getPoint());
                    reload();
                }
            }
        };
        jList1.addMouseListener(mouseListener);
        List doop = BRIG.PROFILE.getRootElement().getChildren("ring");
        currentRing = (Element) doop.get(BRIG.POSITION);
        for (int z = 0; z < doop.size(); z++) {
            if (((Element) doop.get(z)).getAttributeValue("position").compareTo(Integer.toString(BRIG.POSITION)) == 0) {
                currentRing = (Element) doop.get(z);
            }
        }
        if (BRIG.PROFILE.getRootElement().getAttributeValue("spacer") != null) {
            colourField.addItem("alternating red-blue");
            inputData.addItem("Multi-FASTA");
        }
        MouseListener mouseListener2 = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    save();
                    labelField.setText("");
                    startField.setText("");
                    stopField.setText("");
                    RINGPOS = ringu.locationToIndex(e.getPoint());
                    
                    reload();
                }
            }
        };
        ringu.addMouseListener(mouseListener2);
        try {
            reload();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSpinner1 = new javax.swing.JSpinner();
        addButton = new javax.swing.JButton();
        inputData = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList(refModel);
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ringu = new javax.swing.JList();
        jLabel11 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        mess = new javax.swing.JLabel();
        startPanel = new javax.swing.JPanel();
        startField = new javax.swing.JTextField();
        stopField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        gapsonly = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        colourField = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        decField = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        labelField = new javax.swing.JTextField();
        ignore = new javax.swing.JCheckBox();
        filePanel = new javax.swing.JPanel();
        fileLocation = new javax.swing.JTextField();
        featureName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        textContains = new javax.swing.JTextField();
        logicBox = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add Custom Features");

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        inputData.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Single entry", "Tab-delimited", "Genbank", "Embl" }));
        inputData.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputDataItemStateChanged(evt);
            }
        });

        jLabel6.setText("Input data: ");

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jScrollPane1.setViewportView(jList1);

        jButton2.setText("Delete");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Clear all");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jToggleButton1.setText("Close");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jTextField1.setEditable(false);

        jLabel10.setText("Editing custom features for: ");

        ringu.setModel(ringModel);
        jScrollPane2.setViewportView(ringu);

        jLabel11.setText("List of existing rings:");

        jButton4.setText("Add new ring");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        mess.setText("This ring contains BLAST comparisons; create a new ring first to show custom annotations on a blank ring.");

        jLabel2.setText("Start:");

        jLabel3.setText("Stop:");

        gapsonly.setText("Load gaps only");

        javax.swing.GroupLayout startPanelLayout = new javax.swing.GroupLayout(startPanel);
        startPanel.setLayout(startPanelLayout);
        startPanelLayout.setHorizontalGroup(
            startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(startPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(stopField)
                    .addComponent(startField, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(gapsonly)
                .addContainerGap(109, Short.MAX_VALUE))
        );
        startPanelLayout.setVerticalGroup(
            startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(startPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(gapsonly))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        colourField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "red", "aqua", "black", "blue", "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive", "orange", "purple", "silver", "teal", "white", "yellow" }));

        jLabel1.setText("Colour:");

        jLabel5.setText("Draw feature as:");

        decField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "arc", "hidden", "counterclockwise-arrow", "clockwise-arrow" }));

        jLabel4.setText("Label text:");

        ignore.setText("Ignore label text from file");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelField, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(decField, javax.swing.GroupLayout.Alignment.LEADING, 0, 220, Short.MAX_VALUE)
                            .addComponent(ignore)
                            .addComponent(colourField, 0, 220, Short.MAX_VALUE))))
                .addGap(68, 68, 68))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(colourField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(decField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(ignore))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        fileLocation.setEnabled(false);

        featureName.setEnabled(false);

        jButton1.setText("Browse");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        textContains.setEnabled(false);

        logicBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AND", "NOT" }));
        logicBox.setEnabled(false);

        jLabel7.setText("Feature name:");

        jLabel8.setText("File location:");

        jLabel9.setText("Text contains:");

        javax.swing.GroupLayout filePanelLayout = new javax.swing.GroupLayout(filePanel);
        filePanel.setLayout(filePanelLayout);
        filePanelLayout.setHorizontalGroup(
            filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filePanelLayout.createSequentialGroup()
                        .addComponent(logicBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9))
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addGroup(filePanelLayout.createSequentialGroup()
                        .addComponent(fileLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1))
                    .addComponent(textContains, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addComponent(featureName, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))
                .addContainerGap())
        );
        filePanelLayout.setVerticalGroup(
            filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(featureName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logicBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textContains, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButton5.setText("Clear all features");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mess)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(filePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(startPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addComponent(inputData, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(addButton)
                                .addGap(18, 18, 18)
                                .addComponent(jToggleButton1))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(jLabel11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton4))
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 562, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(mess)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jButton3)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(inputData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(startPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton1)
                    .addComponent(addButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if (inputData.getSelectedItem().toString().compareTo("Single entry") == 0) {
            try {
                Integer.parseInt(startField.getText());
                Integer.parseInt(stopField.getText());
                Element feature = new Element("feature");
                if (!startField.getText().isEmpty() && !stopField.getText().isEmpty()) {
                    String colour = colourField.getSelectedItem().toString();
                    if(colour.compareTo("default") ==0 ){
                        colour = "red";
                    }
                    String dec = decField.getSelectedItem().toString();
                    dec = decField.getSelectedItem().toString();
                    if(dec.compareTo("default") ==0 ){
                        dec = "arc";
                    }
                    if (!labelField.getText().isEmpty()) {
                        feature.setAttribute("label", labelField.getText());
                    }
                    feature.setAttribute("colour", colour);
                    feature.setAttribute("decoration", dec);
                    Element child = new Element("featureRange");
                    child.setAttribute("start", startField.getText());
                    child.setAttribute("stop", stopField.getText());
                    feature.addContent(child);
                    currentRing.addContent(feature);
                    reload();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Stop/Start fields can not be empty", "ERROR!",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Stop/Start fields are not integers",
                        "ERROR!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (inputData.getSelectedItem().toString().compareTo("Tab-delimited") == 0) {
            if (fileLocation.getText().compareTo("") != 0) {
                parseTab(fileLocation.getText());
                reload();
            } else {
                JOptionPane.showMessageDialog(this,
                        "File location field is blank",
                        "ERROR!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (inputData.getSelectedItem().toString().compareTo("Genbank") == 0) {
            if (fileLocation.getText().compareTo("") != 0) {
                parseXML(AnnoXML.CreateFeatureXML(fileLocation.getText(), false));
                reload();
            } else {
                JOptionPane.showMessageDialog(this,
                        "File location field is blank",
                        "ERROR!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (inputData.getSelectedItem().toString().compareTo("Embl") == 0) {
            if (fileLocation.getText().compareTo("") != 0) {
                parseXML(AnnoXML.CreateFeatureXML(fileLocation.getText(), true));
                reload();
            } else {
                JOptionPane.showMessageDialog(this,
                        "File location field is blank",
                        "ERROR!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (inputData.getSelectedItem().toString().compareTo("Multi-FASTA") == 0) {
            multiFasta();
            reload();
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        save();
        this.dispose();
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void inputDataItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputDataItemStateChanged
        changeStates();

    }//GEN-LAST:event_inputDataItemStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        File text = new File (fileLocation.getText());
        JFileChooser fc = new JFileChooser();
        if (text.exists()) {
            if (text.getParentFile().isDirectory()) {
                fc = new JFileChooser(text.getParent());
            }
        }
        fc.showOpenDialog(this);
        if (fc.getSelectedFile() != null) {
            fileLocation.setText(fc.getSelectedFile().toString());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        save();
        List custom = currentRing.getChildren("feature");
        int[] del = jList1.getSelectedIndices();
        for (int i = 0; i < del.length; i++) {
            System.out.println(del[i]);
            System.out.println(custom.size());
            ((Element) custom.get(del[0])).detach();
        }

        ELEMENT_INT = 0;
        refModel.clear();
        reload();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        currentRing.removeChildren("feature");
        labelField.setText("");
        startField.setText("");
        stopField.setText("");
        colourField.setSelectedIndex(0);
        decField.setSelectedIndex(0);
        reload();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        save();
        Element newChild = new Element("ring");
        List doop = BRIG.PROFILE.getRootElement().getChildren("ring");
        int big = 0;
        for (int i = 0; i < doop.size(); i++) {
            Element ccur = (Element) doop.get(i);
            int nextInt = Integer.parseInt(ccur.getAttributeValue("position"));
            if (nextInt > big) {
                big = nextInt;
            }
        }
        big++;
        Color newcol = BRIG.FetchColor(big);
        String col = newcol.getRed() + "," + newcol.getGreen() + "," + newcol.getBlue();
        newChild.setAttribute("colour", col);
        newChild.setAttribute("name", "null");
        newChild.setAttribute("position", Integer.toString(big));
        BRIG.PROFILE.getRootElement().addContent(newChild);
        reload();
        gotthis.reload();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
    List<Element> doop = BRIG.PROFILE.getRootElement().getChildren("ring");
        for( Element curr: doop){
            curr.removeChildren("feature");
        }
        reload();
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new CustomXML().setVisible(true);
            }
        });
    }

    public void changeStates() {
        if (inputData.getSelectedItem().toString().compareTo("Single entry") == 0) {
            //Single entry, Tab-delimited, Genbank, Embl
         //   startField.setEnabled(true);
        //    stopField.setEnabled(true);
            fileLocation.setEnabled(false);
            jButton1.setEnabled(false);
            featureName.setEnabled(false);
            logicBox.setEnabled(false);
            textContains.setEnabled(false);
            jLabel8.setText("File location:");
            ignore.setVisible(false);
            fileLocation.setToolTipText("Not required");
            gapsonly.setVisible(false);
            colourField.removeItem("alternating red-blue");
        } else if (inputData.getSelectedItem().toString().compareTo("Tab-delimited") == 0) {
        //    startField.setEnabled(false);
        //    stopField.setEnabled(false);
            fileLocation.setEnabled(true);
            jButton1.setEnabled(true);
            featureName.setEnabled(false);
            logicBox.setEnabled(false);
            textContains.setEnabled(false);
            labelField.setEditable(true);
            ignore.setVisible(false);
            gapsonly.setVisible(false);
            jLabel8.setText("Tab-delimited file location:");
            colourField.removeItem("alternating red-blue");
            fileLocation.setToolTipText("Remember, Tab-delimited file columns should be: START, STOP, Label, colour, Decoration");
        } else if (inputData.getSelectedItem().toString().compareTo("Genbank") == 0) {
        //    startField.setEnabled(false);
       //     stopField.setEnabled(false);
            fileLocation.setEnabled(true);
            jButton1.setEnabled(true);
            featureName.setEnabled(true);
            logicBox.setEnabled(true);
            textContains.setEnabled(true);
            labelField.setEditable(true);
            jLabel8.setText("Genbank file location:");
            ignore.setVisible(true);
            gapsonly.setVisible(false);
            int as = 0;
            for (int k = 0; k < colourField.getItemCount(); k++) {
                if (colourField.getItemAt(k).toString().compareTo("alternating red-blue") == 0) {
                    as++;
                }
            }
            if (as == 0) {
                colourField.addItem("alternating red-blue");
            }
            fileLocation.setToolTipText("Must be a Genbank file");
        } else if (inputData.getSelectedItem().toString().compareTo("Embl") == 0) {
       //     startField.setEnabled(false);
       //     stopField.setEnabled(false);
            fileLocation.setEnabled(true);
            jButton1.setEnabled(true);
            featureName.setEnabled(true);
            logicBox.setEnabled(true);
            textContains.setEnabled(true);
            ignore.setVisible(true);
            gapsonly.setVisible(false);
            labelField.setEditable(true);
            jLabel8.setText("EMBL file location:");
            int as = 0;
            fileLocation.setToolTipText("Must be an EMBL file");
            for (int k = 0; k < colourField.getItemCount(); k++) {
                if (colourField.getItemAt(k).toString().compareTo("alternating red-blue") == 0) {
                    as++;
                }
            }
            if (as == 0) {
                colourField.addItem("alternating red-blue");
            }
        } else if (inputData.getSelectedItem().toString().compareTo("Multi-FASTA") == 0) {
         //   startField.setEnabled(true);
         //   stopField.setEnabled(true);
            fileLocation.setEnabled(false);
            jButton1.setEnabled(false);
            labelField.setEditable(true);
            featureName.setEnabled(false);
            logicBox.setEnabled(false);
            if (BRIG.PROFILE.getRootElement().getAttributeValue("spacer") != null) {
                int as = 0;
                for( int k=0; k< colourField.getItemCount();k++){
                    if (colourField.getItemAt(k).toString().compareTo("alternating red-blue") == 0 ){
                        as++;
                    }
                }
                if (as == 0 ){
                colourField.addItem("alternating red-blue");
                }
            }
            int space = Integer.parseInt(BRIG.PROFILE.getRootElement().getAttributeValue("spacer"));
            if (space > 0) {
                gapsonly.setVisible(true);
            }
            textContains.setEnabled(false);
            ignore.setVisible(false);
            jLabel8.setText("EMBL file location:");
            fileLocation.setToolTipText("Not required");
        }
    }
    public void parseXML(Document input) {
        List features;
        if (featureName.getText().compareTo("") != 0) {
            features = input.getRootElement().getChildren(featureName.getText());
        } else {
            features = input.getRootElement().getChildren();
        }
        int logic = 0;
        if (logicBox.getSelectedItem().toString().compareTo("NOT") == 0) {
            logic = 1;
        }
        for (int i = 0; i < features.size(); i++) {
            Element currentElement = (Element) features.get(i);
            if (textContains.getText().compareTo("") != 0) {
                int scan = scanFeature(currentElement, textContains.getText());
                if (scan == 1 && ((scan ^ logic) == 1)) {
                    anno2xml(currentElement,i );
                }
            } else {
                anno2xml(currentElement,i);
            }
        }

    }

    public void anno2xml(Element currentElement, int count) {
        Element feature = new Element("feature");
        if (!labelField.getText().isEmpty() && labelField.getText().compareTo("") != 0) {
            feature.setAttribute("label", labelField.getText());
        } else {
            if (currentElement.getChild("gene") != null) {
                feature.setAttribute("label", currentElement.getChild("gene").getAttributeValue("value"));
            }
        }
        if (ignore.isSelected()) {
            feature.removeAttribute("label");
        }
        if (colourField.getSelectedItem().toString().compareTo("default") == 0) {
            if (currentElement.getChild("colour") != null) {
                int col = Integer.parseInt(currentElement.getChild("colour").getAttributeValue("value"));
                col++;
                feature.setAttribute("colour", colourField.getItemAt(col).toString());
            } else {
                feature.setAttribute("colour", "red");
            }
        } else if (colourField.getSelectedItem().toString().compareTo("alternating red-blue") == 0) {
            if (count % 2 == 1) {
                feature.setAttribute("colour", "blue");
            } else {
                feature.setAttribute("colour", "red");
            }
        } else {
            feature.setAttribute("colour", colourField.getSelectedItem().toString());
        }

        //default, arc, hidden, counterclockwise-arrow, clockwise-arrow
        String comp = currentElement.getAttributeValue("complement");
        if (decField.getSelectedItem().toString().compareTo("default") == 0) {
            if (currentElement.getName().compareTo("CDS") ==0 ) {
                if (comp.compareTo("true") ==0 ) {
                     feature.setAttribute("decoration", "counterclockwise-arrow");
                } else {
                    feature.setAttribute("decoration", "clockwise-arrow");
                }
            }else{
                feature.setAttribute("decoration", "arc");
            }
        }else{
            feature.setAttribute("decoration", decField.getSelectedItem().toString());
        }
        Element child = new Element("featureRange");
        child.setAttribute("start", currentElement.getAttributeValue("start"));
        child.setAttribute("stop", currentElement.getAttributeValue("stop"));
        child.removeAttribute("label");
        feature.addContent(child);
        currentRing.addContent(feature);
    }

    public int scanFeature(Element feature, String text) {
        if (feature.getChild("product") != null) {
            Element product = feature.getChild("product");
            if (product.getAttributeValue("value").contains(text)) {
                return 1;
            }
        }
        if (feature.getChild("note") != null) {
            Element notes = feature.getChild("note");
            if (notes.getAttributeValue("value").contains(text)) {
                return 1;
            }
        }
        return 0;
    }

    public void parseTab(String input) {
        try {
            if (BRIG.PROFILE.getRootElement().getAttributeValue("spacer") != null) {
                
            }
            BufferedReader in = new BufferedReader(new FileReader(input));
            String line = "";
            int currentLine = 1;
            while ((line = in.readLine()) != null) {
                if (!line.startsWith("#")) {
                    try {
                        line = line.replaceAll("\"", "");
                        line = line.trim();
                        String[] lineArray = line.split("\t");
                        
                        if(lineArray.length >= 2){
                            lineArray[0] = lineArray[0].replaceAll("\\D+", "");
                            lineArray[1] = lineArray[1].replaceAll("\\D+", "");
                        int start = Integer.parseInt(lineArray[0]);
                        int stop = Integer.parseInt(lineArray[1]);
                        Element feature = new Element("feature");
                        if (start != 0 && stop != 0) {
                            if (lineArray.length >= 3) {
                                if (lineArray[2].compareTo("") != 0) {
                                    feature.setAttribute("label", lineArray[2]);
                                }
                            }
                            if (colourField.getSelectedItem().toString().compareTo("default") == 0) {
                                if (lineArray.length >= 4) {
                                    if (lineArray[3].compareTo("") != 0) {
                                        feature.setAttribute("colour", lineArray[3]);
                                    } else {
                                        feature.setAttribute("colour", "green");
                                    }
                                } else {
                                    feature.setAttribute("colour", "red");
                                }
                            } else {
                                feature.setAttribute("colour", colourField.getSelectedItem().toString());
                            }
                            if (decField.getSelectedItem().toString().compareTo("default") == 0){
                                if (lineArray.length >= 5) {
                                if (lineArray[4].compareTo("") != 0) {
                                        feature.setAttribute("decoration", lineArray[4]);
                                    } else {
                                        feature.setAttribute("decoration", "arc");
                                    }
                                }else{
                                    feature.setAttribute("decoration", "arc");
                                }
                            } else {
                                feature.setAttribute("decoration", decField.getSelectedItem().toString());
                            }
                            Element child = new Element("featureRange");
                            child.setAttribute("start", lineArray[0]);
                            child.setAttribute("stop", lineArray[1]);
                            feature.addContent(child);
                            currentRing.addContent(feature);
                            
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Stop/Start fields can not be zero on line " + currentLine, "ERROR!",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this,
                                "Stop/Start fields are not integers on line " + currentLine,
                                "ERROR!",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            reload();
            currentLine++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        List doop = BRIG.PROFILE.getRootElement().getChildren("ring");
        for (int z = 0; z < doop.size(); z++) {
            if (((Element) doop.get(z)).getAttributeValue("position").compareTo(Integer.toString(RINGPOS)) == 0) {
                currentRing = (Element) doop.get(z);
            }
        }
        if (currentRing.getContentSize() > 0) {
            List xml = currentRing.getChildren("feature");
            int totalfeatures = 0;
            for (int i = 0; i < xml.size(); i++) {
                Element feature = (Element) xml.get(i);
                List featureRange = ((Element) xml.get(i)).getChildren("featureRange");
                for (int j = 0; j < featureRange.size(); j++) {
                    Element current = (Element) featureRange.get(j);
                    if (ELEMENT_INT == totalfeatures) {
                        feature.setAttribute("colour", colourField.getSelectedItem().toString());
                        feature.setAttribute("decoration", decField.getSelectedItem().toString());
                        feature.setAttribute("label", labelField.getText());
                        current.setAttribute("label", startField.getText());
                        current.setAttribute("stop", stopField.getText());
                    }
                    totalfeatures++;
                }
            }
        }
    }

    public void reload() {
        changeStates();
        List doop = BRIG.PROFILE.getRootElement().getChildren("ring");
        for (int z = 0; z < doop.size(); z++) {
            if (((Element) doop.get(z)).getAttributeValue("position").compareTo(Integer.toString(RINGPOS)) == 0) {
                currentRing = (Element) doop.get(z);
            }
        }
        jTextField1.setText((Integer.parseInt(currentRing.getAttributeValue("position")) + 1)
                + ": " + currentRing.getAttributeValue("name"));
        refModel.clear();
        if (currentRing.getChildren("sequence").size() > 0) {
            mess.setVisible(true);
        } else {
            mess.setVisible(false);
        }
        ringModel.clear();
        List rings = BRIG.PROFILE.getRootElement().getChildren("ring");
        int pos = 0;
        int stop = 1;
        while (stop != 0) {
            stop = 0;
            for (int k = 0; k < rings.size(); k++) {
                Element entRing = (Element) rings.get(k);
                if (Integer.parseInt(entRing.getAttributeValue("position")) == pos) {
                    ringModel.addElement("Ring "
                            + Integer.toString(Integer.parseInt(entRing.getAttributeValue("position")) + 1)
                            + ": " + entRing.getAttributeValue("name"));
                    stop++;
                    pos++;
                }
            }
        }
        if (currentRing.getContentSize() > 0) {
            List xml = currentRing.getChildren("feature");
            int totalfeatures = 0;
            for (int i = 0; i < xml.size(); i++) {
                Element feature = (Element) xml.get(i);
                List featureRange = ((Element) xml.get(i)).getChildren("featureRange");
                for (int j = 0; j < featureRange.size(); j++) {
                    Element current = (Element) featureRange.get(j);                    
                    if (feature.getAttributeValue("label") != null) {
                        refModel.addElement(current.getAttributeValue("start") + "-" + current.getAttributeValue("stop") + ":" + feature.getAttributeValue("label"));
                    } else {
                        refModel.addElement(current.getAttributeValue("start") + "-" + current.getAttributeValue("stop"));
                    }
                    if (ELEMENT_INT == totalfeatures) {
                        colourField.setSelectedItem(feature.getAttributeValue("colour"));
                        decField.setSelectedItem(feature.getAttributeValue("decoration"));
                        labelField.setText(feature.getAttributeValue("label"));
                        startField.setText(current.getAttributeValue("start"));
                        stopField.setText(current.getAttributeValue("stop"));
                    }
                    totalfeatures++;
                }
                jList1.setSelectedIndex(ELEMENT_INT);
            }
        }
    }

    private void multiFasta() {
        try {
            int space = Integer.parseInt(BRIG.PROFILE.getRootElement().getAttributeValue("spacer"));
            Element root = BRIG.PROFILE.getRootElement();
            String output = root.getAttributeValue("outputFolder");
            String fileName = BRIG.FetchFilename(root.getAttributeValue("queryFile"));
            String ou = output + BRIG.SL + "scratch" + BRIG.SL + "Spaced" + fileName;
            new File(output + BRIG.SL + "scratch").mkdir();
            String text = BRIG.formatMultiFASTA(root.getAttributeValue("queryFile"), ou,
                    space,true);
            root.setAttribute("queryFastaFile", ou);
            BufferedReader first = new BufferedReader(new FileReader(ou));
            String line = "";
            int start = 0;
            int stop = 0;
            String colour = "";
            if (colourField.getSelectedItem().toString().compareTo("default") == 0) {
                colour = "red";
            } else {
                colour = colourField.getSelectedItem().toString();
            }
            String dec = "";
            if (decField.getSelectedItem().toString().compareTo("default") == 0) {
                dec = "arc";
            } else {
                dec =  decField.getSelectedItem().toString();
            }
            int lineNum = 0 ;
            BRIG.GEN_LENGTH = BRIG.FastaLength(root.getAttributeValue("queryFastaFile"), true);
            while ((line = first.readLine()) != null) {
                if (line.contains(">")) {
                    if (gapsonly.isSelected()) {
                        stop = Integer.parseInt(line.split(":")[1]);
                        if (start != 0) {
                            Element feature = new Element("feature");
                            if (start != 0 && stop != 0) {
                                feature.setAttribute("decoration", dec);
                                if (colourField.getSelectedItem().toString().compareTo("alternating red-blue") == 0) { 
                                    if(lineNum % 2 == 1){
                                        feature.setAttribute("colour", "blue");
                                    }else{
                                        feature.setAttribute("colour", "red");
                                    }
                                }else{
                                    feature.setAttribute("colour", colour);
                                }
                                Element child = new Element("featureRange");

                                if(start >= BRIG.GEN_LENGTH ){
                                    start = BRIG.GEN_LENGTH -1;
                                }
                                if(stop >= BRIG.GEN_LENGTH ){
                                    stop = BRIG.GEN_LENGTH -1;
                                }
                                child.setAttribute("start", Integer.toString(start));
                                child.setAttribute("stop", Integer.toString(stop));
                                feature.addContent(child);
                                currentRing.addContent(feature);
                            }

                        }
                        start = Integer.parseInt(line.split(":")[2]);
                    } else {
                        start = Integer.parseInt(line.split(":")[1]);
                        stop = Integer.parseInt(line.split(":")[2]);
                        String label = line.split(":")[0];
                        label = label.replaceAll(">", "");
                        System.out.println(start + " " + stop + " " + label);
                        Element feature = new Element("feature");
                        if (start == 0) {
                            start = 1;
                        }
                        if (start != 0 && stop != 0) {
                            feature.setAttribute("decoration", dec);
                            if (colourField.getSelectedItem().toString().compareTo("alternating red-blue") == 0) {
                                if (lineNum % 2 == 1) {
                                    feature.setAttribute("colour", "blue");
                                } else {
                                    feature.setAttribute("colour", "red");
                                }
                            } else {
                                feature.setAttribute("colour", colour);
                            }
                            if (labelField.getText().compareTo("") == 0) {
                                feature.setAttribute("label", label);
                            } else {
                                feature.setAttribute("label", labelField.getText());
                            }
                            if (ignore.isSelected()) {
                                feature.removeAttribute("label");
                            }
                            Element child = new Element("featureRange");
                            child.setAttribute("start", Integer.toString(start));
                            child.setAttribute("stop", Integer.toString(stop));
                            feature.addContent(child);
                            currentRing.addContent(feature);
                        }

                    }
                lineNum++;
                }
            }
            first.close();
            if (gapsonly.isSelected()) {
                Element feature = new Element("feature");
                feature.setAttribute("decoration", dec);
                if (colourField.getSelectedItem().toString().compareTo("alternating red-blue") == 0) {
                    if (lineNum % 2 == 1) {
                        feature.setAttribute("colour", "blue");
                    } else {
                        feature.setAttribute("colour", "red");
                    }
                } else {
                    feature.setAttribute("colour", colour);
                }
                Element child = new Element("featureRange");
                child.setAttribute("start", Integer.toString(BRIG.GEN_LENGTH));
                child.setAttribute("stop", Integer.toString(BRIG.GEN_LENGTH+space));
                feature.addContent(child);
                currentRing.addContent(feature);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JComboBox colourField;
    private javax.swing.JComboBox decField;
    private javax.swing.JTextField featureName;
    private javax.swing.JTextField fileLocation;
    private javax.swing.JPanel filePanel;
    private javax.swing.JCheckBox gapsonly;
    private javax.swing.JCheckBox ignore;
    private javax.swing.JComboBox inputData;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTextField labelField;
    private javax.swing.JComboBox logicBox;
    private javax.swing.JLabel mess;
    private javax.swing.JList ringu;
    private javax.swing.JTextField startField;
    private javax.swing.JPanel startPanel;
    private javax.swing.JTextField stopField;
    private javax.swing.JTextField textContains;
    // End of variables declaration//GEN-END:variables


}
