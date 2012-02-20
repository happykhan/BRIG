/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author nabil
 */
public class Session {
String blastOptions;
String legendPosition;
File queryFile;
File outputFolder;
boolean blastPlus;
int spacer;
String imageFormat;
String title;
boolean archive;

/* BRIG blastOptions="-evalue 0.00000000005 -dust no -num_threads 8" legendPosition="upper-right" queryF
ile="SPLE4-LEE.fna" outputFolder="" blastPlus="yes" spacer="50" imageFormat="jpg" title="" archive="tr
ue"*/
BRIGSettings BRIGSet;
CGViewSettings CGSet;
ArrayList<Ring> Rings;
Ref RefFiles; 

    public Session(){
        blastOptions = "" ;
        legendPosition = "middle-right";
        blastPlus = true;
        imageFormat = "png";
        title = "" ;
        archive = false; 
        
    }
    
    public Session(File inXML) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document temp = builder.build(inXML);
        BRIGSet = new BRIGSettings(temp.getRootElement().getChild("brig_settings")) ;
        CGSet = new CGViewSettings(temp.getRootElement().getChild("cgview_settings")) ;
    }
    
    public void saveXML(){
        
    }

    public void saveProfile(){
        
    }
    
}

class BRIGSettings {
    int defaultUpper;
    int defaultLower;
    int defaultMin;
    ArrayList RingColour;
    ArrayList genbankFiles;
    ArrayList emblFiles;
    ArrayList fastaFiles;
    File blastLocation;
    int divider  ;
    int mulitpler ;
    int memory ;
    int defaultSpacer; 

    /*
    Ring1="172,14,225" Ring2="222,149,220" Ring3="161,221,231" Ring4="49,34,221" Ring5="1
    16,152,226" Ring6="224,206,38" Ring7="40,191,140" Ring8="158,223,139" Ring9="226,38,122" Ring10="211,4
    1,77" defaultUpper="90" defaultLower="70" defaultMinimum="70" genbankFiles="gbk" fastaFiles="fna,faa,f
    as,fasta,fa" emblFiles="embl" blastLocation="" divider="3" multiplier="3" memory="3000" defaultSpacer=
    "0"
     */
    public BRIGSettings(Element BRIGSet){

    }
}

class CGViewSettings {
    /* arrowheadLength="medium" backboneColor="black" backboneRadius="800" backboneThickne
    ss="medium" backgroundColor="white" borderColor="black" featureSlotSpacing="x-small" featureThickness=
    "30" giveFeaturePositions="false" globalLabel="true" height="7500" isLinear="false" labelFont="SansSer
    if,plain,50" labelLineLength="medium" labelLineThickness="medium" labelPlacementQuality="best" labelsT
    oKeep="1000" longTickColor="black" minimumFeatureLength="medium" moveInnerLabelsToOuter="true" origin=
    "12" rulerFont="SansSerif,plain,40" rulerFontColor="black" rulerPadding="40" rulerUnits="bases" shortT
    ickColor="black" shortTickThickness="medium" showBorder="false" showShading="false" showWarning="false
    " tickDensity="0.2333" tickThickness="medium" titleFont="SansSerif,plain,50" titleFontColor="black" us
    eColoredLabelBackgrounds="false" useInnerLabels="true" warningFont="Default,plain,50" warningFontColor
    ="black" width="8000" zeroTickColor="black" tickLength="medium" */
    public CGViewSettings(Element cgSet){
        
    }
        
}

class Ref {
    
}

class Ring {
    
}