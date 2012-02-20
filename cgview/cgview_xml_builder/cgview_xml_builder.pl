#!/usr/bin/perl
#
#cgview_xml_builder.pl 
#
#Version 1.0
#
#This script requires bioperl-1.4 or newer. 
#
#See README
#
#Written by Paul Stothard, University of Alberta
#
#stothard@ualberta.ca

use strict;
use warnings;
use Bio::SeqIO;
use Getopt::Long;
use Data::Dumper;

my $MIN_ORF_SIZE = 25;
my $MAX_ORF_SIZE = 1000;
my $MIN_STEP = 1;
my $MAX_STEP = 100;
my $MIN_WINDOW = 1000;
my $MAX_WINDOW = 10000;
my $MIN_LENGTH = 1000;
my $MAX_LENGTH = 10000000;

my %options = (sequence => undef,
	       output => undef,
	       reading_frames => 'F',
	       orfs => 'F',
	       combined_orfs => 'F',
	       orf_size => 100,
	       starts => 'atg|ttg|att|gtg|ctg',
	       stops => 'taa|tag|tga',
	       gc_content => 'T',
	       gc_skew => 'T',
	       at_content => 'F',
	       at_skew => 'F',
	       average => 'T',
	       scale => 'T',
	       step => undef,
	       window => undef,
	       size => "medium",
	       tick_density => 0.5,
	       linear => undef,
	       title => undef,
	       details => 'T',
	       legend => 'T',
	       parse_reading_frame => 'F',
	       show_queries => 'F',
               condensed => 'F',
               feature_labels => 'F',
               gene_labels => 'F',
               hit_labels => 'F',
               orf_labels => 'F',
	       global_label => 'T',
               use_opacity => 'F',
	       show_sequence_features => 'T',
	       genes => undef,
	       expression => undef,
	       blast => undef,
	       verbose => 'T',
	       log => undef);

#Feel free to add your own COG letters and colors
my %settings = (cogColors => {J => "rgb(128,0,0)", #maroon
			      K => "rgb(0,0,128)", #navy  
			      L => "rgb(128,0,128)", #purple  
			      D => "rgb(185,148,101)", #light brown          
			      O => "rgb(0,255,255)",  #aqua        
			      M => "rgb(0,128,128)",  #teal
			      N => "rgb(0,0,255)", #blue
			      P => "rgb(234,165,42)", #orange
			      T => "rgb(190,152,253)", #light purple
			      C => "rgb(128,128,0)", #olive       
			      G => "rgb(0,255,0)", #lime
			      E => "rgb(0,128,0)", #green
			      F => "rgb(255,0,255)", #fuchsia
			      H => "rgb(241,199,200)", #light pink
			      I => "rgb(255,0,0)", #red
			      Q => "rgb(255,255,0)", #yellow
			      R => "rgb(128,128,128)", #gray
			      S => "rgb(192,192,192)", #sliver
			      Unknown => "rgb(0,0,0)"}, #black

		blastColors => ["rgb(0,128,128)",  #teal
				"rgb(0,255,255)",  #aqua    
				"rgb(234,165,42)", #orange
				"rgb(190,152,253)", #light purple
				"rgb(255,0,0)", #red
				"rgb(0,0,255)", #blue
				"rgb(128,128,0)", #olive 
				"rgb(153,153,0)"], #yellow

		expressionColors => ["rgb(0,128,0)", #green
				     "rgb(241,199,200)", #light pink
				     "rgb(255,0,255)", #fuchsia
				     "rgb(0,153,153)", #light blue
				     "rgb(0,153,0)", #green
				     "rgb(153,153,0)"], #yellow	
	
		width => "3000",
		height => "3000",
		backboneRadius => "820",
		backboneColor => "rgb(102,102,102)", #dark gray
		backboneThickness => "20",
		featureThickness => "30",    #was 40
		featureThicknessPlot => "50", #was 45
		featureSlotSpacing => "2",
		rulerFontSize => "30",
		rulerFontColor => "rgb(0,0,0)",
		titleFontSize => "80",
		labelFontSize => "30",
		legendFontSize => "30",
		maxTitleLength => "90",
		maxLabelLength => "20",
		maxLegendLength => "30",
		plotLineThickness => "0.02",
		proteinColor => "rgb(0,0,153)", #dark blue
		tRNAColor => "rgb(153,0,0)",     #dark red
		rRNAColor => "rgb(153,0,153)", #dark purple
		otherColor => "rgb(51,51,51)", #dark gray
		featureOpacity => "1.0",
		featureOpacityOther => "0.5", #features of type 'other' are drawn with transparency so that underlying CDS can be seen
		gcColorPos => "rgb(0,0,0)", #black
		gcColorNeg => "rgb(0,0,0)", #black
		atColorPos => "rgb(51,51,51)", #dark gray
		atColorNeg => "rgb(51,51,51)", #dark gray
		gcSkewColorPos => "rgb(0,153,0)",  #dark green
		gcSkewColorNeg => "rgb(153,0,153)", #dark purple
		atSkewColorPos => "rgb(153,153,0)", #dark yellow 
		atSkewColorNeg => "rgb(0,0,153)", #dark blue
		orfColor => "rgb(204,0,0)", #dark red   
		startColor => "rgb(204,0,0)", #dark red
		stopColor => "rgb(153,0,153)", #dark purple
		backgroundColor => "white",
		foregroundColor => "black",
		sepColor => "rgb(0,51,0)", #dark green
		tickColor => "rgb(0,51,0)", #dark green
		labelLineLength => "200",
		labelPlacementQuality => "good", #good, better, best
		labelLineThickness => "4",
		rulerPadding => "40",
		tickThickness => "5",
		arrowheadLength => "6",
		minimumFeatureLength => "1.0",
		moveInnerLabelsToOuter => "false",
		tickLength => "20",
		useInnerLabels => "true",
		showBorder => "true",
		isLinear => "false",
		maxFeatureSize => 50000); #maxFeatureSize is used to prevent large features ('source' for example) from obscuring other features

my %global = (orfCount => 0,
    	      ncbiGiLink => "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Text\&amp;db=Protein\&amp;dopt=genpept\&amp;dispmax=20\&amp;uid=",
	      ncbiGeneLink => "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene\&amp;cmd=Retrieve\&amp;dopt=Graphics\&amp;list_uids=",
	      format => undef,
              length => undef,
              accession => undef,
              topology => "circular");

my %param = (options => \%options,
	     settings => \%settings,
	     global => \%global);
		

#read user options
GetOptions ('sequence=s' => \$options{sequence},
	    'output=s' => \$options{output},
	    'reading_frames=s' => \$options{reading_frames},
	    'orfs=s' => \$options{orfs},
	    'combined_orfs=s' => \$options{combined_orfs},
	    'orf_size=i' => \$options{orf_size},
	    'starts=s' => \$options{starts},
	    'stops=s' => \$options{stops},
	    'gc_content=s' => \$options{gc_content},
	    'gc_skew=s' => \$options{gc_skew},
	    'at_content=s' => \$options{at_content},
	    'at_skew=s' => \$options{at_skew},
	    'average=s' => \$options{average},
	    'scale=s' => \$options{scale},
	    'step=i' => \$options{step},
	    'window=i' => \$options{window},
	    'size=s' => \$options{size},
            'tick_density=f' => \$options{tick_density},
	    'linear=s' => \$options{linear},
	    'title=s' => \$options{title},
	    'details=s' => \$options{details},
	    'legend=s' => \$options{legend},
	    'parse_reading_frame=s' =>\$options{parse_reading_frame},
	    'show_queries=s' =>\$options{show_queries},
	    'condensed=s' => \$options{condensed},
            'feature_labels=s' => \$options{feature_labels},
            'gene_labels=s' => \$options{gene_labels},
            'hit_labels=s' => \$options{hit_labels},
            'orf_labels=s' => \$options{orf_labels},
	    'use_opacity=s' => \$options{use_opacity},
            'show_sequence_features=s' => \$options{show_sequence_features},
	    'global_label=s' => \$options{global_label},
	    'genes=s' => \$options{genes},
	    'expression=s' => \$options{expression},
	    'blast=s' => \$options{blast},
	    'verbose=s' => \$options{verbose},
	    'log=s' => \$options{log});


#check for required options
if (!(defined($param{options}->{sequence}))) {
    die ("Please specify a sequence using the '-sequence' option.\n");
}
if (!(defined($param{options}->{output}))) {
    die ("Please specify an output file using the '-output' option.\n");
}

#start log file
if (defined($param{options}->{log})) {
    _createLog($param{options}->{log});
}

#check some important values
#-orf_size
if ($options{orf_size} =~ m/(\d+)/) {
    $options{orf_size} = $1;
}
else {
    _message($param{options}, "-orf_size must be an integer value.");
    die ("-orf_size must be an integer value.");
}
if ($options{orf_size} < $MIN_ORF_SIZE) {
    _message($param{options}, "-orf_size must be greater than or equal to $MIN_ORF_SIZE.");
    die ("-orf_size must be greater than or equal to $MIN_ORF_SIZE.");
}
if ($options{orf_size} > $MAX_ORF_SIZE) {
    _message($param{options}, "-orf_size must be less than or equal to $MAX_ORF_SIZE.");
    die ("-orf_size must be less than or equal to $MAX_ORF_SIZE.");
}

#-step
if (defined($options{step})) {
    if ($options{step} =~ m/(\d+)/) {
	$options{step} = $1;
    }
    else {
	_message($param{options}, "-step must be an integer value.");
	die ("-step must be an integer value.");
    }
    if ($options{step} < $MIN_STEP) {
	_message($param{options}, "-step must be greater than or equal to $MIN_STEP.");
	die ("-step must be greater than or equal to $MIN_STEP.");
    }
    if ($options{step} > $MAX_STEP) {
	_message($param{options}, "-step must be less than or equal to $MAX_STEP.");
	die ("-step must be less than or equal to $MAX_STEP.");
    }
}

#-window
if (defined($options{window})) {
    if ($options{window} =~ m/(\d+)/) {
	$options{window} = $1;
    }
    else {
	_message($param{options}, "-window must be an integer value.");
	die ("-window must be an integer value.");
    }
    if ($options{window} < $MIN_WINDOW) {
	_message($param{options}, "-window must be greater than or equal to $MIN_WINDOW.");
	die ("-window must be greater than or equal to $MIN_WINDOW.");
    }
    if ($options{window} > $MAX_WINDOW) {
	_message($param{options}, "-window must be less than or equal to $MAX_WINDOW.");
	die ("-window must be less than or equal to $MAX_WINDOW.");
    }
}

#-tick_density
if ($options{tick_density} =~ m/([\d\.]+)/) {
    $options{tick_density} = $1;
}
else {
    _message($param{options}, "-tick_density must be a real value.");
    die ("-tick_density must be a real value.");
}
if ($options{tick_density} < 0) {
    _message($param{options}, "-tick_density must be greater than or equal to 0.");
    die ("-tick_density must be greater than or equal to 0.");
}
if ($options{tick_density} > 1) {
    _message($param{options}, "-tick_density must be less than or equal to 1.");
    die ("-tick_density must be less than or equal to 1.");
}

#-global_label


if ($options{global_label} =~ m/auto/i) {
    $options{global_label} = "auto";
}
elsif ($options{global_label} =~ m/t/i) {
    $options{global_label} = "true";
}
else {
    $options{global_label} = "false";
}


#-starts
if (defined($options{starts})) {
    my @starts = split(/\|/, $options{starts});
    foreach(@starts) {
        if (!($_ =~ m/[a-z]{3}/)) {
           _message($param{options}, "-starts must be given as codons separated by the '|', eg 'atg|ttg|att|gtg|ctg'. Be sure to include single quotes when passing as a command line argument.");
           die ("-starts must be given as codons separated by the '|', eg 'atg|ttg|att|gtg|ctg'. Be sure to include single quotes when passing as a command line argument.");
        }
    }
}

#-stops
if (defined($options{stops})) {
    my @stops = split(/\|/, $options{stops});
    foreach(@stops) {
        if (!($_ =~ m/[a-z]{3}/)) {
           _message($param{options}, "-stops must be given as codons separated by the '|', eg 'taa|tag|tga'. Be sure to include single quotes when passing as a command line argument.");
           die ("-stops must be given as codons separated by the '|', eg 'taa|tag|tga'. Be sure to include single quotes when passing as a command line argument.");
        }
    }
}

#convert file arguments to arrays of files
$param{options}->{blast} = _getFiles($param{options}->{blast});
$param{options}->{genes} = _getFiles($param{options}->{genes});
$param{options}->{expression} = _getFiles($param{options}->{expression});


#obtain BioPerl Bio::Seq sequence object
#_getSeqObject also sets value of $global{format} to 'genbank', 'embl', 'raw', or 'fasta'
my $seqObject = _getSeqObject(\%param);

#determine the length of the genome
$global{length} = $seqObject->length();
if (!(defined($global{length}))) {
    _message($param{options}, "The sequence length could not be determined from the -sequence file $options{sequence}.");
    die ("The sequence length could not be determined from the -sequence file $options{sequence}.");
}

if ($global{length} < $MIN_LENGTH) {
    _message($param{options}, "The sequence must be longer than $MIN_LENGTH bases.");
    die ("The sequence must be longer than $MIN_LENGTH bases.");
}

if ($global{length} > $MAX_LENGTH) {
    _message($param{options}, "The sequence must be shorter than $MAX_LENGTH bases.");
    die ("The sequence must be shorter than $MAX_LENGTH bases.");
}

#set window
#these values may need to be adjusted
if (!(defined($options{window}))) {
    if ($global{length} < 1000) {
        $options{window} = 10;
    }
    elsif ($global{length} < 10000) {
        $options{window} = 50;
    }
    elsif ($global{length} < 100000) {
        $options{window} = 500;
    }
    elsif ($global{length} < 1000000) {
        $options{window} = 1000;
    }
    elsif ($global{length} < 10000000) {
        $options{window} = 10000;
    }
    else {
        $options{window} = 10000;
    }
}

#set step.
#these values may need to be adjusted
#the step may need to be smaller when a larger map is drawn
if (!(defined($options{step}))) {
    if ($global{length} < 1000) {
        $options{step} = 1;
    }
    elsif ($global{length} < 10000) {
        $options{step} = 1;
    }
    elsif ($global{length} < 100000) {
        $options{step} = 1;
    }
    elsif ($global{length} < 1000000) {
        $options{step} = 10;
    }
    elsif ($global{length} < 10000000) {
        $options{step} = 100;
    }
    else {
        $options{step} = 100;
    }

    #adjust based on map size
    if ($options{size} eq 'x-large') {
        if ($options{step} == 10) {
           $options{step} = 1;
        }
        elsif ($options{step} == 100) {
           $options{step} = 10;
        }
    }

    if ($options{size} eq 'large') {
        if ($options{step} == 10) {
           $options{step} = 5;
        }
        elsif ($options{step} == 100) {
           $options{step} = 50;
        }
    }

}

#determine some global settings from sequence file
if (($global{format} eq "embl") || ($global{format} eq "genbank")) {
    $global{"accession"} = $seqObject->accession_number;
    if (!(defined($options{title}))) {
	$options{title} = $seqObject->description();
    }    
}
if ($global{format} eq "fasta") {
    if (!(defined($options{title}))) {
	$options{title} = $seqObject->description();
    }
}

#try to determine topology from genbank or embl record
if (($global{format} eq "embl") || ($global{format} eq "genbank")) {
    if ($seqObject->is_circular) {
	$settings{isLinear} = "false";
	$global{topology} = "circular";
    }
    else {
	$settings{isLinear} = "true";
	$global{topology} = "linear";
    }
}

#user-supplied values take precedence
if (_isTrue($options{linear})) {
    $settings{isLinear} = "true";
    $settings{topology} = "linear";
}
elsif ((defined($options{linear})) && ($options{linear} =~ m/f/i)) {
    $settings{isLinear} = "false";
    $settings{topology} = "circular";
}

#adjust settings based on size of map
_adjustSettingsBasedOnSize($param{options}, $param{settings}, $param{global});

#start building XML file
_writeHeader(\%options, \%settings, \%global);

#write title legend
if (_isTrue($options{title})) {
    _writeTitleLegend(\%options, \%settings);
}

#write details legend
if (_isTrue($options{details})) {
    _writeDetailsLegend(\%param);
}

#write features legend
if (_isTrue($options{legend})) {
    _writeFeatureLegend(\%param, $seqObject);
}


#forward strand features drawn on outside of backbone circle. Those appearing first in the XML are drawn closest to the backbone.
_message(\%options, "Creating XML for feature sets on the outside of the backbone circle.");
_message(\%options, "Feature sets written first are drawn closest to the backbone circle.");

#write starts and stops for three reading frames
if (_isTrue($options{reading_frames})) {
    _message(\%options, "Writing starts and stops that are in reading frame +1.");    
    _writeStopsAndStarts(\%options, \%settings, $seqObject, 1, 1);
    _message(\%options, "Writing starts and stops that are in reading frame +2.");  
    _writeStopsAndStarts(\%options, \%settings, $seqObject, 1, 2);
    _message(\%options, "Writing starts and stops that are in reading frame +3.");  
    _writeStopsAndStarts(\%options, \%settings, $seqObject, 1, 3);
}

#write ORFs for three reading frames
if (_isTrue($options{orfs})) {
    _message(\%options, "Writing ORFs that are in reading frame +1.");   
    _writeOrfs(\%options, \%settings, \%global, $seqObject, 1, 1);
    _message(\%options, "Writing ORFs that are in reading frame +2."); 
    _writeOrfs(\%options, \%settings, \%global, $seqObject, 1, 2);
    _message(\%options, "Writing ORFs that are in reading frame +3."); 
    _writeOrfs(\%options, \%settings, \%global, $seqObject, 1, 3);
}

#write combined orfs
if (_isTrue($options{combined_orfs})) {
    _message(\%options, "Writing ORFs that are on the plus strand.");
    _writeOrfs(\%options, \%settings, \%global, $seqObject, 1, "all");
}

#write features in GenBank or EMBL file
if (_isTrue($options{show_sequence_features})) {
    if ($global{format} eq "genbank") {
	_message(\%options, "Writing features in the GenBank file that are on the plus strand.");
	_writeGenBankGenes(\%options, \%settings, \%global, $seqObject, 1, undef);
    }
    elsif ($options{format} eq "embl") {
	_message(\%options, "Writing features in the EMBL file that are on the plus strand.");
	_writeEmblGenes(\%options, \%settings, \%global, $seqObject, 1, undef);
    }
}

#write features info from -genes files if available
if (defined($options{genes})) {
    foreach(@{$options{genes}}) {
        _message(\%options, "Writing features in the genes file $_ that are on the plus strand.");
        _writeGenes(\%options, \%settings, \%global, $seqObject, 1, undef, undef, undef, $_, undef);
    }
}

#write info from expression file if available
if (defined($options{"expression"})) {
    my @colors = @{$settings{'expressionColors'}};
    foreach(@{$options{"expression"}}) {
        my $colorPos = shift(@colors);
        my $colorNeg = shift(@colors);
	push(@colors, $colorPos);
	push(@colors, $colorNeg);
        _message(\%options, "Writing expression values from the expression file $_ that are on the plus strand.");
        _writeGenes(\%options, \%settings, \%global, $seqObject, 1, undef, $colorPos, $colorNeg, $_, 1);
    }
}


#Reverse strand features drawn on inside of backbone circle. Those appearing first
#in the XML are drawn closest to the backbone.
_message(\%options, "Creating XML for feature sets on the inside of the backbone circle.");
_message(\%options, "Feature sets written first are drawn closest to the backbone circle.");

#write starts and stops for three reading frames
if (_isTrue($options{reading_frames})) {
    _message(\%options, "Writing starts and stops that are in reading frame -1.");    
    _writeStopsAndStarts(\%options, \%settings, $seqObject, -1, 1);
    _message(\%options, "Writing starts and stops that are in reading frame -2.");  
    _writeStopsAndStarts(\%options, \%settings, $seqObject, -1, 2);
    _message(\%options, "Writing starts and stops that are in reading frame -3.");  
    _writeStopsAndStarts(\%options, \%settings, $seqObject, -1, 3);
}

#write ORFs for three reading frames
if (_isTrue($options{orfs})) {
    _message(\%options, "Writing ORFs that are in reading frame -1.");   
    _writeOrfs(\%options, \%settings, \%global, $seqObject, -1, 1);
    _message(\%options, "Writing ORFs that are in reading frame -2."); 
    _writeOrfs(\%options, \%settings, \%global, $seqObject, -1, 2);
    _message(\%options, "Writing ORFs that are in reading frame -3."); 
    _writeOrfs(\%options, \%settings, \%global, $seqObject, -1, 3);
}

#write combined orfs
if (_isTrue($options{combined_orfs})) {
    _message(\%options, "Writing ORFs that are on the reverse strand.");
    _writeOrfs(\%options, \%settings, \%global, $seqObject, -1, "all");
}

#write features in GenBank or EMBL file
if (_isTrue($options{show_sequence_features})) {
    if ($global{format} eq "genbank") {
	_message(\%options, "Writing features in the GenBank file that are on the reverse strand.");
	_writeGenBankGenes(\%options, \%settings, \%global, $seqObject, -1, undef);
    }
    elsif ($options{format} eq "embl") {
	_message(\%options, "Writing features in the EMBL file that are on the reverse strand.");
	_writeEmblGenes(\%options, \%settings, \%global, $seqObject, -1, undef);
    }
}

#write features info from -genes files if available
if (defined($options{genes})) {
    foreach(@{$options{genes}}) {
        _message(\%options, "Writing features in the genes file $_ that are on the reverse strand.");
        _writeGenes(\%options, \%settings, \%global, $seqObject, -1, undef, undef, undef, $_, undef);
    }
}

#write info from expression file if available
if (defined($options{"expression"})) {
    my @colors = @{$settings{'expressionColors'}};
    foreach(@{$options{"expression"}}) {
        my $colorPos = shift(@colors);
        my $colorNeg = shift(@colors);
	push(@colors, $colorPos);
	push(@colors, $colorNeg);
        _message(\%options, "Writing expression values from the expression file $_ that are on the reverse strand.");
        _writeGenes(\%options, \%settings, \%global, $seqObject, -1, undef, $colorPos, $colorNeg, $_, 1);
    }
}

#write blast results
if (defined($options{"blast"})) {
    my @colors = @{$settings{'blastColors'}};
    foreach(@{$options{"blast"}}) {
        my $color = shift(@colors);
	push(@colors, $color);
        _message(\%options, "Writing ALL BLAST hits from the BLAST file $_.");
       _writeBlast(\%options, \%settings, \%global, $seqObject, -1, $_, $color, $settings{'featureThickness'}, undef);
    }
}

#draw base content graphs.
if (_isTrue($options{'at_content'})) {
    _message(\%options, "Writing AT content information.");
    _writeBaseContent(\%options, \%settings, $seqObject, -1, 'at_content');
}
if (_isTrue($options{'at_skew'})) {
    _message(\%options, "Writing AT skew information.");
    _writeBaseContent(\%options, \%settings, $seqObject, -1, 'at_skew');
}
if (_isTrue($options{'gc_content'})) {
    _message(\%options, "Writing GC content information.");
    _writeBaseContent(\%options, \%settings, $seqObject, -1, 'gc_content');
}
if (_isTrue($options{'gc_skew'})) {
    _message(\%options, "Writing GC skew information.");
    _writeBaseContent(\%options, \%settings, $seqObject, -1, 'gc_skew');
}


#write footer
_writeFooter(\%options);


#give information about running CGView and improving XML
_message(\%options, "CGView XML file complete.");
_message(\%options, "The recommended CGView command is:");
_message(\%options, "----------------------------------");
_message(\%options, "java -jar -Xmx1500m ../cgview.jar -i $options{output} -o map.png -f png");
_message(\%options, "Success!");

##############################


sub _createLog {
    my $file = shift;
    open(OUTFILE, ">" . $file) or die ("Cannot open file : $!");
    print(OUTFILE "#Results of cgview_sml_builder.pl run started on " . _getTime() . ".\n");
    close(OUTFILE) or die ("Cannot close file : $!");
}

sub _getTime {
    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
    $year += 1900;

    my @days = ('Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday');
    my @months = ('January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December');
    my $time = $days[$wday] . " " . $months[$mon] . " " . sprintf("%02d", $mday) . " " . sprintf("%02d", $hour) . ":" . sprintf("%02d", $min) . ":" . sprintf("%02d", $sec) . " " . sprintf("%04d", $year);  
    return $time;
}

sub _getFiles {
    my $string = shift;

    if (!(defined($string))) {
	return undef;
    }

    my @split = split(/(?<=[^\\])\s/, $string); 
    my @files = ();
    foreach(@split) {
	if ((!(defined($_))) || (!($_ =~ m/\S/))) {
	    next;
	}
	push (@files, $_);
    }
    if (scalar(@files) > 0) {
	return \@files;
    }
    return undef;
}


sub _adjustSettingsBasedOnSize {
    my $options = shift;
    my $settings = shift;
    my $global = shift;
 
    if ($options->{size} eq "small") {
	$settings->{width} = "1000";
	$settings->{height} = "1000";
	$settings->{featureSlotSpacing} = "4",
	$settings->{backboneRadius} = "300";
	$settings->{backboneThickness} = "2";
	$settings->{featureThickness} = "8";
	$settings->{featureThicknessPlot} = "15";
	$settings->{rulerFontSize} = "8";
	$settings->{titleFontSize} = "30";
	$settings->{labelFontSize} = "10";
	$settings->{legendFontSize} = "8";
	$settings->{maxTitleLength} = "60";
	$settings->{maxLabelLength} = "20";
	$settings->{maxLegendLength} = "20";
	$settings->{plotLineThickness} = "0.02";
	$settings->{labelLineLength} = "60";
	$settings->{labelLineThickness} = "1";
	$settings->{rulerPadding} = "14";
	$settings->{tickThickness} = "1";
	$settings->{arrowheadLength} = "4";
	$settings->{minimumFeatureLength} = "0.4";
	$settings->{tickLength} = "5";
    }
    elsif ($options->{size} eq "medium") {
	$settings->{width} = "3000";
	$settings->{height} = "3000";
	$settings->{featureSlotSpacing} = "6",
	$settings->{backboneRadius} = "1000";
	$settings->{backboneThickness} = "8";
	$settings->{featureThickness} = "30";
	$settings->{featureThicknessPlot} = "80";
	$settings->{rulerFontSize} = "30";
	$settings->{titleFontSize} = "80";
	$settings->{labelFontSize} = "15";
	$settings->{legendFontSize} = "20";
	$settings->{maxTitleLength} = "90";
	$settings->{maxLabelLength} = "20";
	$settings->{maxLegendLength} = "30";
	$settings->{plotLineThickness} = "0.02";
	$settings->{labelLineLength} = "200";
	$settings->{labelLineThickness} = "4";
	$settings->{rulerPadding} = "40";
	$settings->{tickThickness} = "6";
	$settings->{arrowheadLength} = "6";
	$settings->{minimumFeatureLength} = "0.5";
	$settings->{tickLength} = "15";
	
	$options->{tick_density} = $options->{tick_density} / 3.0;
    }
    elsif ($options->{size} eq "large") {
	$settings->{width} = "9000";
	$settings->{height} = "9000";
	$settings->{featureSlotSpacing} = "6",
	$settings->{backboneRadius} = "3500";
	$settings->{backboneThickness} = "8";
	$settings->{featureThickness} = "60";
	$settings->{featureThicknessPlot} = "120";
	$settings->{rulerFontSize} = "60";
	$settings->{titleFontSize} = "100";
	$settings->{labelFontSize} = "40";
	$settings->{legendFontSize} = "50";
	$settings->{maxTitleLength} = "90";
	$settings->{maxLabelLength} = "20";
	$settings->{maxLegendLength} = "30";
	$settings->{plotLineThickness} = "0.02";
	$settings->{labelLineLength} = "200";
	$settings->{labelLineThickness} = "4";
	$settings->{rulerPadding} = "100";
	$settings->{tickThickness} = "10";
	$settings->{arrowheadLength} = "6";
	$settings->{minimumFeatureLength} = "0.5";
	$settings->{tickLength} = "25";
	$settings->{labelPlacementQuality} = "better";

	$options->{tick_density} = $options->{tick_density} / 9.0;
    }
    elsif ($options->{size} eq "x-large") {
	$settings->{width} = "12000";
	$settings->{height} = "12000";
	$settings->{featureSlotSpacing} = "2",
	$settings->{backboneRadius} = "4000";
	$settings->{featureThickness} = "100";
	$settings->{featureThicknessPlot} = "150";
	$settings->{rulerFontSize} = "60";
	$settings->{titleFontSize} = "80";
	$settings->{labelFontSize} = "15";
	$settings->{legendFontSize} = "60";
	$settings->{maxTitleLength} = "90";
	$settings->{maxLabelLength} = "20";
	$settings->{maxLegendLength} = "30";
	$settings->{plotLineThickness} = "0.02";
	$settings->{labelLineLength} = "200";
	$settings->{labelLineThickness} = "4";
	$settings->{rulerPadding} = "40";
	$settings->{tickThickness} = "5";
	$settings->{arrowheadLength} = "6";
	$settings->{minimumFeatureLength} = "0.5";
	$settings->{tickLength} = "20";
	$settings->{labelPlacementQuality} = "better";

	$options->{tick_density} = $options->{tick_density} / 12.0;
    }
    else {
	_message($options, "-size setting $options->{size} not recognized.");
	die("-size setting $options->{size} not recognized");
    }

    #count the number of featureSlots
    my $plotSlotsOuter = 0;
    my $plotSlotsInner = 0;
    my $otherSlotsOuter = 0;
    my $otherSlotsInner = 0;

    #non plot slots such as genes
    if (($global->{format} eq "embl") || ($global->{format} eq "genbank")) {
	if ($options->{show_sequence_features}) {
	    $otherSlotsOuter++;
	    $otherSlotsInner++;
	}
    }
    if (_isTrue($options->{reading_frames})) {
	$otherSlotsOuter = $otherSlotsOuter + 3;
	$otherSlotsInner = $otherSlotsInner + 3;
    }   
    if (_isTrue($options->{orfs})) {
	$otherSlotsOuter = $otherSlotsOuter + 3;
	$otherSlotsInner = $otherSlotsInner + 3;
    } 
    if (_isTrue($options->{combined_orfs})) {
	$otherSlotsOuter = $otherSlotsOuter + 3;
	$otherSlotsInner = $otherSlotsInner + 3;
    } 
    if (defined($options->{genes})) {
	$otherSlotsOuter = $otherSlotsOuter + scalar(@{$options->{genes}});
	$otherSlotsInner = $otherSlotsInner + scalar(@{$options->{genes}});
    }
    if (defined($options->{expression})) {
	$otherSlotsOuter = $otherSlotsOuter + scalar(@{$options->{expression}});
	$otherSlotsInner = $otherSlotsInner + scalar(@{$options->{expression}});
    }
    if (defined($options->{blast})) {
	if (_isTrue($options->{parse_reading_frame})) {
	    $otherSlotsInner = $otherSlotsInner + scalar(@{$options->{blast}}) * 6;
	}
	else {
	    $otherSlotsInner = $otherSlotsInner + scalar(@{$options->{blast}});
	}
    }

    #plot slots such as gc skew
    if (_isTrue($options->{at_content})) {
	$plotSlotsInner++;
    } 
    if (_isTrue($options->{at_skew})) {
	$plotSlotsInner++;
    }
    if (_isTrue($options->{gc_content})) {
	$plotSlotsInner++;
    } 
    if (_isTrue($options->{gc_skew})) {
	$plotSlotsInner++;
    }

    #adjust featureSlotThickness based on number of slots used
    #want plotSlots to be six times wider than other slots
    my $availableSpace = $settings->{backboneRadius} * 0.70 - $settings->{featureSlotSpacing} * ($plotSlotsInner + $plotSlotsOuter + $otherSlotsInner + $otherSlotsOuter - 1);
    my $slotUnits = 6.0 * ($plotSlotsInner + $plotSlotsOuter) + $otherSlotsInner + $otherSlotsOuter;
    
    if ($slotUnits == 0) {
	$slotUnits++;
    }

    my $slotWidths = $availableSpace / $slotUnits;

    $settings->{featureThickness} = sprintf("%.2f", $slotWidths);
    $settings->{featureThicknessPlot} = sprintf("%.2f", $slotWidths * 6.0);


    #check condensed setting.
    if (_isTrue($options->{condensed})) {
	my $newWidth;
	if ($options->{size} eq "small") {
	    $newWidth = 8;
	}
	if ($options->{size} eq "medium") {
	    $newWidth = 10;
	}
	if ($options->{size} eq "large") {
	    $newWidth = 10;
	}
	if ($options->{size} eq "x-large") {
	    $newWidth = 10;
	}

	if ($newWidth < $settings->{featureThickness}) {
	    $settings->{featureThickness} = $newWidth;
	    $settings->{backboneThickness} = "2";
	}
	if (($newWidth * 2) < $settings->{featureThicknessPlot}) {
	    $settings->{featureThicknessPlot} = $newWidth * 2;
	    $settings->{backboneThickness} = "2";
	}
    }
}

sub _message {
    my $options = shift;
    my $message = shift;
 
    if (_isTrue($options->{verbose})) {
	print "$message\n";
    }
    
    if (defined($options->{log})) {
	_writeLog($options->{log}, "$message\n");
    }
}

sub _isTrue {
    my $string = shift;
    if ((defined($string)) && ($string =~ m/t/i)) {
	return 1;
    }
    return 0;
}

sub _writeLog {
    my $file = shift;
    my $message = shift;
    open(OUTFILE, "+>>" . $file) or die ("Cannot open file : $!");
    print(OUTFILE $message);
    close(OUTFILE) or die ("Cannot close file : $!");
}

sub _getSeqObject {
    my $param = shift;
    my $file = $param->{options}->{sequence};

    open (INFILE, $file) or die( "Cannot open input file: $!" );   
    my $line = <INFILE>;
    close (INFILE) or die ("Cannot close input file: $!");

    #guess file format from first line
    if ($line =~ m/^LOCUS/) {
	$param->{global}->{format} = "genbank";
    }
    elsif ($line =~ m/^ID/) {
	$param->{global}->{format} = "embl";
    }
    elsif ($line =~ m/^>/) {
	$param->{global}->{format} = "fasta";
    }
    else {
	$param->{global}->{format} = "raw";
    }

    #get seqobj
    my $in = Bio::SeqIO->new(-format => $param->{global}->{format}, -file => $file);
    my $seq = $in->next_seq();
    
    return $seq;
    
}


sub _writeHeader {
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    
    my $header = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<cgview backboneRadius=\"$settings->{backboneRadius}\" backboneColor=\"$settings->{backboneColor}\" backboneThickness=\"$settings->{backboneThickness}\" featureSlotSpacing=\"$settings->{featureSlotSpacing}\" labelLineLength=\"$settings->{labelLineLength}\" labelPlacementQuality=\"$settings->{labelPlacementQuality}\" labelLineThickness=\"$settings->{labelLineThickness}\" rulerPadding=\"$settings->{rulerPadding}\" tickThickness=\"$settings->{tickThickness}\" arrowheadLength=\"$settings->{arrowheadLength}\" rulerFont=\"SansSerif, plain, $settings->{rulerFontSize}\" rulerFontColor=\"$settings->{rulerFontColor}\" labelFont=\"SansSerif, plain, $settings->{labelFontSize}\" isLinear=\"$settings->{isLinear}\" minimumFeatureLength=\"$settings->{minimumFeatureLength}\" sequenceLength=\"$global->{length}\" height=\"$settings->{height}\" width=\"$settings->{width}\" globalLabel=\"$options->{global_label}\" moveInnerLabelsToOuter=\"$settings->{moveInnerLabelsToOuter}\" featureThickness=\"$settings->{featureThickness} \" tickLength=\"$settings->{tickLength}\" useInnerLabels=\"$settings->{useInnerLabels}\" shortTickColor=\"$settings->{tickColor}\" longTickColor=\"$settings->{tickColor}\" zeroTickColor=\"$settings->{tickColor}\" showBorder=\"$settings->{showBorder}\" borderColor=\"$settings->{foregroundColor}\" backgroundColor=\"$settings->{backgroundColor}\" tickDensity=\"$options->{tick_density}\" >\n";

    open (OUTFILE, ">$options->{output}") or die( "Cannot open file : $!" );
    print (OUTFILE $header);
    close (OUTFILE) or die( "Cannot close file : $!");

}

sub _writeTitleLegend {
    my $options = shift;
    my $settings = shift;
    my $title = $options->{title};
    $title =~ s/[\.\,]//g;
    $title = _escapeText($title);
    if (length($title) > $settings->{maxTitleLength} - 3) {
	if ($options->{verbose}) {
	    _message ($options, "The sequence title was shortened because it is longer than $settings->{maxTitleLength} characters.");
	}
	$title = substr($title, 0, $settings->{maxTitleLength} - 3) . "...";
    }

    my $legend = "<legend position=\"lower-center\" backgroundOpacity=\"0.8\">\n<legendItem textAlignment=\"center\" font=\"SansSerif, plain, $settings->{titleFontSize}\" text=\"$title\" />\n</legend>\n";
    
    open (OUTFILE, "+>>" . $options->{output}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- title -->\n");
    print (OUTFILE $legend);
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");
}

sub _escapeText {
    my $text = shift;
    if (!defined($text)) {
	return undef;
    }
    $text =~ s/&/&amp;/g;
    $text =~ s/\'/&apos;/g;
    $text =~ s/\"/&quot;/g;
    $text =~ s/</&lt;/g;
    $text =~ s/>/&gt;/g;
    return $text;
}

sub _writeDetailsLegend {
    my $param = shift;

    my $accession = _escapeText($param->{global}->{accession});
    my $length = _escapeText($param->{global}->{length});
    my $topology = _escapeText($param->{global}->{topology});

    my $legend = "<legend position=\"upper-left\" font=\"SansSerif, plain, $param->{settings}->{titleFontSize}\" backgroundOpacity=\"0.8\">\n";
    if (defined($accession)) {
	$legend = $legend . "<legendItem text=\"Accession: $accession\" />\n";
    }

    while ($length =~ s/^(-?\d+)(\d\d\d)/$1,$2/) {
	1;
    }
    $length = $length . " bp";
    $legend = $legend . "<legendItem text=\"Length: $length\" />\n";

    if ($topology =~ m/linear/i) {
	$legend = $legend . "<legendItem text=\"" . "Topology: $topology\" />\n";
    }
    
    $legend = $legend . "</legend>\n";

    open (OUTFILE, "+>>" . $param->{options}->{output}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- details legend -->\n");
    print (OUTFILE $legend);
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");

}

sub _writeFeatureLegend {
    my $param = shift;
    my $options = $param->{options};
    my $settings = $param->{settings};
    my $global = $param->{global};
    my $seqObject = shift;


    #check -genes files to see if they contain COG information, or features such as 'other' 'tRNA' 'rRNA'
    my $hasCogs = 0;
    my $hasOther = 0;
    my $hastRNA = 0;
    my $hasrRNA = 0;
    my $hasCoding = 0;
    if (defined($options->{genes})) {
	foreach(@{$options->{genes}}) {
	    my @features = @{_parseGFF($_, $options, $settings, $global, $seqObject)};

	    foreach(@features) {
		my $feat = $_;
		my $type = lc($feat->{'feature'});

		if ($type eq "cds") {
		    $hasCoding = 1;
		}
		elsif ($type eq "rrna") {
		    $hasrRNA = 1;
		}
		elsif ($type eq "trna") {
		    $hastRNA = 1;
		}
		elsif ($type eq "other") {
		    $hasOther = 1;
		}
		elsif (defined($settings->{'cogColors'}->{uc($type)})) {
		    $hasCogs = 1;
		}
	    }
	}	
    }

    my $legend = "<legend position=\"upper-right\" textAlignment=\"left\" backgroundOpacity=\"0.8\" font=\"SansSerif, plain, " . $settings->{'legendFontSize'} . "\" >\n";


    #expression legends
    if (defined($options->{'expression'})) {
	my @colors = @{$settings->{'expressionColors'}};
	foreach(@{$options->{'expression'}}) {
	    my $title = _createLegendName($_);
	    my $colorPos = shift(@colors);
	    my $colorNeg = shift(@colors);
	    push(@colors, $colorPos);
	    push(@colors, $colorNeg);
	    $legend = $legend . "<legendItem text=\"$title+\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $colorPos . "\" />\n";
	    $legend = $legend . "<legendItem text=\"$title-\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $colorNeg . "\" />\n";
	}
    }

    #legend for COGs if genes file supplied and COGs were found
    if ($hasCogs) {
	if (defined($options->{'genes'})) {
	    my @cogs = keys(%{$settings->{'cogColors'}});
	    @cogs = sort(@cogs);
	    foreach(@cogs) {
		$legend = $legend . "<legendItem text=\"" . $_ . " COG" . "\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'cogColors'}->{$_} . "\" />\n";
	    }
	}
    }

    #protein encoding genes legend
    if (((($global->{'format'} eq "genbank") || ($global->{'format'} eq "embl"))) || ($hasCoding)) {
	$legend = $legend . "<legendItem text=\"CDS\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'proteinColor'} . "\" />\n";
    }

    if (!(defined($global->{'format'}))) {
	die ("_writeFeatureLegend requires that the sequence format be set by _getSeqObject.");
    }    
    
    #legend for tRNA, rRNA and other genes if input file is GenBank or EMBL, or if these features were found in the -genes files
    if ((((($global->{'format'} eq "genbank") || ($global->{'format'} eq "embl"))) && ($options->{show_sequence_features})) || ($hastRNA)) {
        $legend = $legend . "<legendItem text=\"tRNA\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'tRNAColor'} . "\" />\n";
    }
    if ((((($global->{'format'} eq "genbank") || ($global->{'format'} eq "embl"))) && ($options->{show_sequence_features})) || ($hasrRNA)) {
        $legend = $legend . "<legendItem text=\"rRNA\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'rRNAColor'} . "\" />\n";
    }
    if ((((($global->{'format'} eq "genbank") || ($global->{'format'} eq "embl"))) && ($options->{show_sequence_features})) || ($hasOther)) {
#        $legend = $legend . "<legendItem text=\"Other\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacityOther'} . "\" swatchColor=\"" . $settings->{'otherColor'} . "\" />\n";
    }      

    #orfs legend
    if ((_isTrue($options->{'orfs'})) || (_isTrue($options->{'combined_orfs'}))) {
	$legend = $legend . "<legendItem text=\"ORF\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'orfColor'} . "\" />\n";
    }

    #reading frames legend
    if (_isTrue($options->{'reading_frames'})) {
	$legend = $legend . "<legendItem text=\"Start\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'startColor'} . "\" />\n";
	$legend = $legend . "<legendItem text=\"Stop\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'stopColor'} . "\" />\n";
    }

    #blast legends
    if (defined($options->{'blast'})) {
	my @colors = @{$settings->{'blastColors'}};
	foreach(@{$options->{'blast'}}) {
	    my $title = "BLAST " . _createLegendName($_);
	    my $color = shift(@colors);
	    push(@colors, $color); 
	    $legend = $legend . "<legendItem text=\"$title\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $color . "\" />\n";
	}
    }

    #legend for various graphs
    #at
    if (_isTrue($options->{'at_content'})) {
	if ($settings->{'atColorPos'} eq $settings->{'atColorNeg'}) {
	    $legend = $legend . "<legendItem text=\"AT content\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'atColorPos'} . "\" />\n";
	}
	else {
	    $legend = $legend . "<legendItem text=\"AT content+\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'atColorPos'} . "\" />\n";
	    $legend = $legend . "<legendItem text=\"AT content-\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'atColorNeg'} . "\" />\n";
	}
    }
    #at_skew
    if (_isTrue($options->{'at_skew'})) {
	if ($settings->{'atSkewColorPos'} eq $settings->{'atSkewColorNeg'}) {
	    $legend = $legend . "<legendItem text=\"AT skew\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'atSkewColorPos'} . "\" />\n";
	}
	else {
	    $legend = $legend . "<legendItem text=\"AT skew+\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'atSkewColorPos'} . "\" />\n";
	    $legend = $legend . "<legendItem text=\"AT skew-\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'atSkewColorNeg'} . "\" />\n";
	}
    }
    #gc
    if (_isTrue($options->{'gc_content'})) {
	if ($settings->{'gcColorPos'} eq $settings->{'gcColorNeg'}) {
	    $legend = $legend . "<legendItem text=\"GC content\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'gcColorPos'} . "\" />\n";
	}
	else {
	    $legend = $legend . "<legendItem text=\"GC content+\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'gcColorPos'} . "\" />\n";
	    $legend = $legend . "<legendItem text=\"GC content-\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'gcColorNeg'} . "\" />\n";
	}
    }
    #gc_skew
    if (_isTrue($options->{'gc_skew'})) {
	if ($settings->{'gcSkewColorPos'} eq $settings->{'gcSkewColorNeg'}) {
	    $legend = $legend . "<legendItem text=\"GC skew\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'gcSkewColorPos'} . "\" />\n";
	}
	else {
	    $legend = $legend . "<legendItem text=\"GC skew+\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'gcSkewColorPos'} . "\" />\n";
	    $legend = $legend . "<legendItem text=\"GC skew-\" drawSwatch=\"true\" swatchOpacity=\"" . $settings->{'featureOpacity'} . "\" swatchColor=\"" . $settings->{'gcSkewColorNeg'} . "\" />\n";
	}
    }

    $legend = $legend . "</legend>\n";

    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- legend -->\n");
    print (OUTFILE $legend);
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");  

}

sub _createLegendName {
    my $file = shift;
    my $name;
    if ($file =~ m/\/([^\/]+)$/) {
	$name = $1;
	$name =~ s/\..*$//;
	$name =~ s/_/ /g;
    }
    if (defined($name)) {
	return $name;
    }
    return $file;
}

sub _writeStopsAndStarts {
    my $options = shift;
    my $settings = shift;
    my $seqObject = shift;
    my $strand = shift; #1 or -1
    my $rf = shift; #1,2, or 3

    my $opacity = $settings->{'featureOpacity'};
    my $startCodons = $options->{'starts'};
    my $stopCodons = $options->{'stops'};
   
    my @outputArray = ();
    if ($strand == 1) {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"direct\">\n");
    }
    else {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"reverse\">\n");
    }

    #add start and stop codons
    my $dna;
    if ($strand == 1) {
 
	if ($rf == 1) {
	    $dna = substr($seqObject->seq(), 0);
	}
	elsif ($rf == 2) {
	    $dna = substr($seqObject->seq(), 1);
	}
	else {
	    $dna = substr($seqObject->seq(), 2);
	}


	my $length = length($dna);
	my $codon;
	my $start;
	my $stop;
	my $feature;
	my $featureRange;
	#for start
	for (my $i = 0; $i < $length - 2; $i = $i + 3) {
	    $codon = substr($dna, $i, 3);
	    if ($codon =~ m/$startCodons/i) {	  
		$start = $i + $rf;
		$stop = $start + 2;
		$feature = "<feature color=\"" . $settings->{'startColor'} .  "\" decoration=\"arc\" opacity=\"$opacity\" >\n";
		$featureRange = "<featureRange start=\"$start\" stop=\"$stop\" proportionOfThickness=\"1.0\" />\n";
		push (@outputArray, $feature . $featureRange . "</feature>\n");
	    }
	}

	#for stop
	for (my $i = 0; $i < $length - 2; $i = $i + 3) {
	    $codon = substr($dna, $i, 3);
	    if ($codon =~ m/$stopCodons/i) {	  
		$start = $i + $rf;
		$stop = $start + 2;
		$feature = "<feature color=\"" . $settings->{'stopColor'} .  "\" decoration=\"arc\" opacity=\"$opacity\" >\n";
		$featureRange = "<featureRange start=\"$start\" stop=\"$stop\" proportionOfThickness=\"0.5\" />\n";
		push (@outputArray, $feature . $featureRange . "</feature>\n");
	    }
	}	
    }
    elsif ($strand == -1) {
	my $rev = $seqObject->revcom; 
	$dna = $rev->seq();
  
	if ($rf == 1) {
	    $dna = substr($dna, 0);
	}
	elsif ($rf == 2) {
	    $dna = substr($dna, 1);
	}
	else {
	    $dna = substr($dna, 2);
	}

	my $length = length($dna);
	my $codon;
	my $start;
	my $stop;
	my $feature;
	my $featureRange;

	#for start
	for (my $i = 0; $i < $length - 2; $i = $i + 3) {
	    $codon = substr($dna, $i, 3);
	    if ($codon =~ m/$startCodons/i) {	  
		$start = $length - $i - 1 - $rf;
		$stop = $start + 2;
		$feature = "<feature color=\"" . $settings->{'startColor'} .  "\" decoration=\"arc\" opacity=\"$opacity\" >\n";
		$featureRange = "<featureRange start=\"$start\" stop=\"$stop\" proportionOfThickness=\"1.0\" />\n";
		push (@outputArray, $feature . $featureRange . "</feature>\n");
	    }
	}

	#for stop
	for (my $i = 0; $i < $length - 2; $i = $i + 3) {
	    $codon = substr($dna, $i, 3);
	    if ($codon =~ m/$stopCodons/i) {	  
		$start = $length - $i - 1 - $rf;
		$stop = $start + 2;
		$feature = "<feature color=\"" . $settings->{'stopColor'} .  "\" decoration=\"arc\" opacity=\"$opacity\" >\n";
		$featureRange = "<featureRange start=\"$start\" stop=\"$stop\" proportionOfThickness=\"0.5\" />\n";
		push (@outputArray, $feature . $featureRange . "</feature>\n");
	    }
	}
    }

    push (@outputArray, "</featureSlot>\n");  
    my $strandTerm = undef;
    if ($strand == 1) {
	$strandTerm = "forward";
    }
    if ($strand == -1) {
	$strandTerm = "reverse";
    }

    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- stops and starts in rf $rf on $strandTerm strand -->\n");
    print (OUTFILE join("", @outputArray));
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");

}


sub _writeOrfs {
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $strand = shift; #1 or -1
    my $rf = shift; #1,2, 3, or all

    my $opacity = $settings->{'featureOpacity'};
    my $startCodons = $options->{'starts'};
    my $stopCodons = $options->{'stops'};
    my $orfLength = $options->{'orf_size'};
    my $color = $settings->{'orfColor'};
    my $decoration;
   
    my @outputArray = ();
    if ($strand == 1) {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"direct\">\n");
    }
    else {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"reverse\">\n");
    }

    my @orfs = ();

    #getOrfs
    if ($rf eq "all") {
	push (@orfs, @{_getOrfs($options, $settings, $global, $seqObject, $strand, 1)});
	push (@orfs, @{_getOrfs($options, $settings, $global, $seqObject, $strand, 2)});
	push (@orfs, @{_getOrfs($options, $settings, $global, $seqObject, $strand, 3)});
    }
    else {
	push (@orfs, @{_getOrfs($options, $settings, $global, $seqObject, $strand, $rf)});
    }
    
    #sort orfs
    @orfs = @{_sortOrfsByStart(\@orfs)};
    if ($strand == 1) {
	@orfs = reverse(@orfs);
    }
    push (@outputArray, @orfs);

    #end getOrfs

    push (@outputArray, "</featureSlot>\n");  
    my $strandTerm = undef;
    if ($strand == 1) {
	$strandTerm = "forward";
    }
    if ($strand == -1) {
	$strandTerm = "reverse";
    }

    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- ORFs in rf $rf on $strandTerm strand -->\n");
    print (OUTFILE join("", @outputArray));
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");

}

sub _sortOrfsByStart {
    my $orfs = shift;

    @$orfs = map { $_->[1] }
      sort { $a->[0] <=> $b->[0]}
      map { [ _getSortValueOrfs($_), $_] }
      @$orfs;
    
    return $orfs;
}

sub _getSortValueOrfs {
    my $orf = shift;
    #start=\"$firstBase\"
    $orf =~ m/start=\"(\d+)\"/;
    return $1;
}

sub _getOrfs {
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $strand = shift; #1 or -1
    my $rf = shift; #1,2, or 3
    my $rfForLabel = $rf;

    my $opacity = $settings->{'featureOpacity'};
    my $startCodons = $options->{'starts'};
    my $stopCodons = $options->{'stops'};
    my $orfLength = $options->{'orf_size'};
    my $color = $settings->{'orfColor'};
    my $decoration;

    if ($strand == 1) {
	#$decoration = "clockwise-arrow";
	$decoration = "arc";
    }
    else {
	#$decoration = "counterclockwise-arrow";
	$decoration = "arc";
    }
   
    my @orfs = ();

    my $dna;
    if ($strand == 1) {
	$dna = $seqObject->seq();
    }
    else {
 	my $rev = $seqObject->revcom; 
	$dna = $rev->seq();     
    }
    my $length = length($dna);
    my $i = 0;
    my $codon;
    my $foundStart = 0;
    my $proteinLength = 0;
    my $foundStop = 0;
    my $startPos = $rf - 1;
    my $feature;
    my $featureRange;
    my $firstBase;
    my $lastBase;
    my $temp;

    my @dna = ();

    while ($i <= $length - 3)	{
	for ($i = $startPos; $i <= $length - 3; $i = $i + 3)	{
	    $codon = substr($dna,$i,3);
	    if (($startCodons ne "any") && ($foundStart == 0) && (!($codon =~ m/$startCodons/i)))	{
		last;
	    }
	    $foundStart = 1;
	    
	    if ($codon =~ m/$stopCodons/i) {
		$foundStop = 1;
	    }
		
	    $proteinLength++;
	    push (@dna, $codon);

	    if (($foundStop) && ($proteinLength < $orfLength))	{
		last;
	    }
	    if ((($foundStop) && ($proteinLength >= $orfLength)) || (($i >= $length - 5) && ($proteinLength >= $orfLength)))	{
		$firstBase = $startPos + 1;
		$lastBase = $i + 3;

		if ($strand == -1) {
		    $temp = $length - $lastBase + 1;
		    $lastBase = $length - $firstBase + 1;
		    $firstBase = $temp;		    
		}

		$global->{orfCount}++;

		$feature = "<feature color=\"$color\" decoration=\"$decoration\" opacity=\"$opacity\" ";

		if (_isTrue($options->{orf_labels})) {
		    $feature = $feature . "label=\"orf_$global->{orfCount}\" ";
		    $feature = $feature . "mouseover=\"" . _escapeText("orf_$global->{orfCount}; $firstBase to $lastBase; strand=$strand; rf=$rfForLabel") . "\" ";
		}
		$feature = $feature . ">\n";

		$featureRange = "<featureRange start=\"$firstBase\" stop=\"$lastBase\" />\n";
		push (@orfs, $feature . $featureRange . "</feature>\n");
       
		last;
	    }
	}
	$startPos = $i + 3;
	$i = $startPos;
	$foundStart = 0;
	$foundStop = 0;
	$proteinLength = 0;
	@dna = ();
    }

    return \@orfs;

}

sub _writeEmblGenes {
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $strand = shift; #1 or -1
    my $rf = shift; #1,2,3, or undefined for all reading frames
    _writeGenBankGenes($options, $settings, $seqObject, $strand, $rf);
}

sub _writeGenBankGenes {
 
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $strand = shift; #1 or -1
    my $rf = shift; #1,2,3, or undefined for all reading frames

    my $opacity;
    
    #make rf 
    if ((defined($rf)) && ($rf == 3)) {
	$rf = 0;
    }

    my @outputArray = ();

    my $decoration;
    if ($strand == 1) {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"direct\">\n");
	#$decoration = "clockwise-arrow";
	$decoration = "arc";
    }
    else {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"reverse\">\n");
	#$decoration = "counterclockwise-arrow";
	$decoration = "arc";
    }
   
    #need to get the features from from the GenBank record.
    my @features = $seqObject->get_SeqFeatures();
    @features = @{_sortFeaturesByStart(\@features)};

    if ($strand == 1) {
	@features = reverse(@features);
    }

    foreach(@features) {
	my $feat = $_;
	my $type = lc($feat->primary_tag);
	my $color;

	if ($type eq "cds") {
	    $color = $settings->{'proteinColor'};
	    $opacity = $settings->{'featureOpacity'};
	}
	elsif ($type eq "rrna") {
	    $color = $settings->{'rRNAColor'};
	    $opacity = $settings->{'featureOpacity'};
	}
	elsif ($type eq "trna") {
	    $color = $settings->{'tRNAColor'};
	    $opacity = $settings->{'featureOpacity'};
	}
	else {
#	    $color = $settings->{'otherColor'};
#	    $opacity = $settings->{'featureOpacityOther'};
	    next;
	}

	#skip certain feature types
	if ($type eq "source") {
	    next;
	}
	if ($type eq "gene") {
	    next;
	}
	if ($type eq "misc_feature") {
	    next;
	}
	
	my $st = $feat->strand;
	unless($st == $strand) {
	    next;
	}

	my $start = $feat->start;
	my $stop = $feat->end;

	#this handles feature that spans start/stop
	#UPDATE: all features with more than 1 entry in @loc are skipped below
	my $location = $feat->location;
	my $locString = $location->to_FTstring;
	my @loc = split(/,/, $locString);

	if ($loc[0] =~ m/(\d+)\.\.(\d+)/) {
	    $start = $1;
	}

	if ($loc[scalar(@loc) - 1] =~ m/(\d+)\.\.(\d+)/) {
	    $stop = $2;
	}
	
	if (defined($rf)) {
	    if ($strand == 1) {
		unless ($rf == $start % 3) {
		    next;
		}
	    }
	    elsif ($strand == -1) {
		unless ($rf ==  ($seqObject->length() - $stop + 1)% 3) {
		    next;
		}
	    }
	}

	my $label;
	if ($feat->has_tag('gene')) {
	    $label = join("",$feat->get_tag_values('gene'));
	}
	elsif ($feat->has_tag('locus_tag')) {
	    $label = join("",$feat->get_tag_values('locus_tag'));
	}
	elsif ($feat->has_tag('note')) {
	    $label = join("",$feat->get_tag_values('note'));	
	}
	else {
	    $label = $feat->primary_tag;
	}

	$label = _escapeText($label);
	if (length($label) > $settings->{'maxLabelLength'} - 3) {
	    $label = substr($label, 0, $settings->{'maxLabelLength'} - 3) . "...";
	}

        #/db_xref="GI:11497049"
	my $hyperlink;
	if ($feat->has_tag('db_xref')) {
	    my $dbXref = join("",$feat->get_tag_values('db_xref'));
	    if ($dbXref =~ m/GI\:(\d+)/) {
		$hyperlink = $global->{'ncbiGiLink'} . $1;
	    }
	    #/db_xref="GeneID:1132092"
	    elsif ($dbXref =~ m/GeneID\:(\d+)/) {
		$hyperlink = $global->{'ncbiGeneLink'} . $1;
	    }
	}

	#/product="conserved hypothetical protein"
	my $mouseover = $label . "; " . $start . " to " . $stop;
	if ($feat->has_tag('product')) {
	    my $product = join("",$feat->get_tag_values('product'));
	    $mouseover = $mouseover . "; " . $product;
	}
	if ($feat->has_tag('db_xref')) {
	    my $dbXref = join("",$feat->get_tag_values('db_xref'));
	    if ($dbXref =~ m/(GI\:\d+)/) {
		$mouseover = $mouseover .  "; " . $1;
	    }
	    #/db_xref="GeneID:1132092"
	    elsif ($dbXref =~ m/(GeneID\:\d+)/) {
		$mouseover = $mouseover .  "; " . $1;
	    }	
	}
	$mouseover = _escapeText($mouseover);	    

	#check length of feature
	my $featLength = $stop - $start + 1;
	if (scalar(@loc) > 1) {
	    _message($options, "The following feature of type '$type' has a complex location string and will be ignored: label: $label");
	    next;
	}
	if ($featLength == 0) {
	    _message($options, "The following feature of type '$type' has length equal to zero and will be ignored: label: $label; start: $start; end: $stop");
	    next;
	}
	if ($featLength < 0) {
	    _message($options, "The following feature of type '$type' has 'start' less than 'end' and will be ignored: label: $label; start: $start; end: $stop");
	    next;
	}
	if ($featLength > $settings->{maxFeatureSize}) {
	    _message($options, "The following feature of type '$type' has length greater than $settings->{maxFeatureSize} and will be ignored: label: $label; start: $start; end: $stop");
	    next;
	}

	#now check entries and make tags
	my $feature = "<feature color=\"$color\" decoration=\"$decoration\" opacity=\"$opacity\" ";

	if (_isTrue($options->{feature_labels})) {
	    if (_containsText($label)) {
		$feature = $feature . "label=\"$label\" ";
	    }
	    if (_containsText($hyperlink)) {
		$feature = $feature . "hyperlink=\"$hyperlink\" ";		
	    }
	    if (_containsText($mouseover)) {
		$feature = $feature . "mouseover=\"$mouseover\" ";		
	    }
	}

	$feature = $feature . ">\n";

	my $featureRange = "<featureRange start=\"$start\" stop=\"$stop\" />\n";

	#now add $feature and $featureRange to @outputArray
	push (@outputArray, $feature . $featureRange . "</feature>\n");	


    }


    push (@outputArray, "</featureSlot>\n"); 
    my $strandTerm = undef;
    if ($strand == 1) {
	$strandTerm = "forward";
    }
    if ($strand == -1) {
	$strandTerm = "reverse";
    }
    
    my $rfTerm = undef;
    if (!(defined($rf))) {
	$rfTerm = "1,2,3";
    }
    else {
	$rfTerm = $rf;
    }
    
    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- GenBank or EMBL genes on strand $strandTerm in rf $rfTerm -->\n");
    print (OUTFILE join("", @outputArray));
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");

}

sub _sortFeaturesByStart {
    my $features = shift;

    @$features = map { $_->[1] }
      sort { $a->[0] <=> $b->[0]}
      map { [ _getSortValueFeature($_), $_] }
      @$features;
    
    return $features;
}

sub _getSortValueFeature {
    my $feature = shift;
    return $feature->start;
}

sub _containsText {
    my $text = shift;

    if (!(defined($text))) {
	return 0;
    }
    if ($text =~ m/[A-Za-z0-9]/g) {
	return 1;
    }
    else {
	return 0;
    }
}

sub _writeGenes {
 
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $strand = shift; #1 or -1
    my $rf = shift; #1,2,3, or undefined for all reading frames
    my $expColorPos = shift;
    my $expColorNeg = shift;
    my $file = shift;
    my $useScore = shift;

    my $opacity;
    
 
    if ((defined($rf)) && ($rf == 3)) {
	$rf = 0;
    }

    my @outputArray = ();

    my $decoration;
    if ($strand == 1) {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"direct\">\n");
	#$decoration = "clockwise-arrow";
	$decoration = "arc";
    }
    else {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"reverse\">\n");
	#$decoration = "counterclockwise-arrow";
	$decoration = "arc";
    }
   
    #need to get the features from from the GenBank record.
    my @features = ();
    @features = @{_parseGFF($file, $options, $settings, $global, $seqObject)};

    @features = @{_sortGFFByStart(\@features)};

    if ($strand == 1) {
	@features = reverse(@features);
    }

   
    foreach(@features) {
	my $feat = $_;
	my $type = lc($feat->{'feature'});

	#the genes in the GFF file can be coloured by the 'feature' column, based on the 
	#type of gene (CDS, rRNA, or tRNA) or by COG (J, K, L etc).
	#colour based on feature type first
	my $color;
	if ((defined($expColorPos)) && (defined($expColorNeg))) {
	    $color = undef;
	}
	elsif ($type eq "cds") {
	    $color = $settings->{'proteinColor'};
	    $opacity = $settings->{'featureOpacity'};
	}
	elsif ($type eq "rrna") {
	    $color = $settings->{'rRNAColor'};
	    $opacity = $settings->{'featureOpacity'};
	}
	elsif ($type eq "trna") {
	    $color = $settings->{'tRNAColor'};
	    $opacity = $settings->{'featureOpacity'};
	}
	elsif ($type eq "other") {
	    $color = $settings->{'otherColor'};
	    $opacity = $settings->{'featureOpacityOther'};
	}
	elsif (defined($settings->{'cogColors'}->{uc($type)})) {
	    $color = $settings->{'cogColors'}->{uc($type)};
	    $opacity = $settings->{'featureOpacity'};
	}
	else {
	    $color = $settings->{'cogColors'}->{'Unknown'};
	    $opacity = $settings->{'featureOpacity'};
	}
	
	my $st = $feat->{'strand'};
	if ($st eq "+") {
	    $st = 1;
	}
	elsif ($st eq "-") {
	    $st = -1;	    
	}

	if ($st ne $strand) {
	    next;
	}

	my $start = $feat->{'start'};
	my $stop = $feat->{'end'};

	if (defined($rf)) {
	    if ($strand == 1) {
		unless ($rf == $start % 3) {
		    next;
		}
	    }
	    elsif ($strand == -1) {
		unless ($rf ==  ($seqObject->length() - $stop + 1)% 3) {
		    next;
		}
	    }
	}

	my $label;
	if (_containsText($feat->{'seqname'})) {
	    $label = $feat->{'seqname'};
	    $label = _escapeText($label);
	    if (length($label) > $settings->{'maxLabelLength'} - 3) {
		$label = substr($label, 0, $settings->{'maxLabelLength'} - 3) . "...";
	    }
	}

	my $mouseover;
	$mouseover = $label . "; " . $start . " to " . $stop;
	if (_containsText($feat->{'feature'})) {
	    $mouseover = $mouseover . "; " . $feat->{'feature'};
	    $mouseover = _escapeText($mouseover);
	}

	#now check entries and make tags
	my $feature;
	if (defined($color)) {
	    $feature = "<feature color=\"$color\" decoration=\"$decoration\" opacity=\"$opacity\" ";
	}
	else {
	    $feature = "<feature decoration=\"$decoration\" opacity=\"$opacity\" ";
	}

	if (_isTrue($options->{gene_labels})) {
	    if (_containsText($label)) {
		$feature = $feature . "label=\"$label\" ";
	    }
	    if (_containsText($mouseover)) {
		$feature = $feature . "mouseover=\"$mouseover\" ";		
	    }
	}

	$feature = $feature . ">\n";

	my $featureRange;
	if (defined($useScore)) {
	    #score should be between 0 and -1
	    my $score;
	    if (defined($feat->{'score'})) {
		if ($feat->{'score'} =~ m/(\-?[\d\.]+)/) {
		    $score = $1;
		}
	    }
	    if (defined($score)) {
		if ($score > 1) {
		    $score = 1;
		}
		if ($score < -1) {
		    $score = -1;
		}
	    }
	    else {
		$score = 0;
	    }

	    #Want to draw as bars with positive values extending upwards and negative values extending downwards.
	    my $barHeight;
	    my $radiusShift;

	    if ($score > 0) {
		$barHeight = $score;
		$barHeight = $barHeight * 0.5;
		$radiusShift = 0.5 + $barHeight / 2.0;
		$color = $expColorPos;
	    }
	    elsif ($score < 0) {
		$barHeight = 0 - $score;
		$barHeight = $barHeight * 0.5;
		$radiusShift = 0.5 - $barHeight / 2;
    		$color = $expColorNeg;
	    }
	    else {
		$radiusShift = 0.5;
		$barHeight = $settings->{'plotLineThickness'};
        	$color = $expColorPos;
	    }

	    $featureRange = "<featureRange start=\"$start\" stop=\"$stop\" radiusAdjustment=\"$radiusShift\" proportionOfThickness=\"$barHeight\" color=\"$color\" />\n";
	}
	else {
	    $featureRange = "<featureRange start=\"$start\" stop=\"$stop\" />\n";
	}

	#now add $feature and $featureRange to @outputArray
	push (@outputArray, $feature . $featureRange . "</feature>\n");		
    }


    push (@outputArray, "</featureSlot>\n"); 
    my $strandTerm = undef;
    if ($strand == 1) {
	$strandTerm = "forward";
    }
    if ($strand == -1) {
	$strandTerm = "reverse";
    }
    
    my $rfTerm = undef;
    if (!(defined($rf))) {
	$rfTerm = "1,2,3";
    }
    else {
	$rfTerm = $rf;
    }
    
    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- genes or expression on $strandTerm strand in rf $rfTerm -->\n");
    print (OUTFILE join("", @outputArray));
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");

}

sub _parseGFF {
    my $file = shift;
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $lineCount = 0;
    my @columnTitles = ();
    my $columnsRead = 0;
    open (INFILE, $file) or die ("Cannot open the GFF file $file");
    
    #check for column titles
    while (my $line = <INFILE>) {
	$line =~ s/\cM|\n//g;
	$lineCount++;
	if ($line =~ m/^\#/) {
	    next;
	}
	if ($line =~ m/\S/) {
	    $columnsRead = 1;
	    @columnTitles = @{_split($line)};
	    last;
	}
    }
    #print Dumper(@columnTitles);
    my @gffColumns = ("seqname", "source", "feature", "start", "end", "score", "strand", "frame");

    for (my $i = 0; $i < scalar(@gffColumns); $i++) {
	if (!(defined($columnTitles[$i]))) {
	    _message($options, "Column $i in GFF file $file was not defined - must be titled \"$gffColumns[$i]\".");
	    die ("Column $i in GFF file $file was not defined - must be titled \"$gffColumns[$i]\""); 	    
	}
	elsif ($gffColumns[$i] ne lc($columnTitles[$i])) {
	    _message($options, "Column $i in GFF file $file was titled $columnTitles[$i] - must be titled \"$gffColumns[$i]\".");
	   die ("Column $i in GFF file $file was titled $columnTitles[$i] - must be titled \"$gffColumns[$i]\""); 
	}
    }

    #To allow for 'start', 'end', 'strand', and 'frame' values to be read from the '-sequence' file
    #build a hash of genes.
    my $sequenceGenes = _getFeatures($global, $seqObject);

    #print (Dumper($sequenceGenes));

    my @entries = ();
    while (my $line = <INFILE>) {    
	$line =~ s/\cM|\n//g;
	$lineCount++;
	if ($line =~ m/\S/) {
	    my @values = @{_split($line)};
	    my %entry = ();
	    for (my $i = 0; $i < scalar(@gffColumns); $i++) {
		$entry{$gffColumns[$i]} = $values[$i];
	    }	    
	    
	    #try to add to entry if needed
	    _addToEntry(\%entry, $sequenceGenes);

	    if (!(defined($entry{'start'}))) {
		if (defined($entry{'seqname'})) {
		    _message($options, "Warning: unable to obtain 'start' value for seqname $entry{seqname} in $file line $lineCount.");	    
		}
		else {
		    _message($options, "Warning: unable to obtain 'start' value for $file line $lineCount.");
		}
		next;
	    }

	    if (!(defined($entry{'end'}))) {
		if (defined($entry{'seqname'})) {
		    _message($options, "Warning: unable to obtain 'end' value for seqname $entry{seqname} in $file line $lineCount.");	    
		}
		else {
		    _message($options, "Warning: unable to obtain 'end' value for $file line $lineCount.");
		}
		next;
	    }

	    if (!(defined($entry{'strand'}))) {
		if (defined($entry{'seqname'})) {
		    _message($options, "Warning: unable to obtain 'strand' value for seqname $entry{seqname} in $file line $lineCount.");	    
		}
		else {
		    _message($options, "Warning: unable to obtain 'strand' value for $file line $lineCount.");
		}
		next;
	    }

	    if (!($entry{'start'} =~ m/^\d+$/)) {
		_message($options, "Warning: value in 'start' column must be a positive integer in $file line $lineCount.");
		next;
		#die ("Value in 'start' column must be a positive integer in $file line $lineCount");
	    }
	    if (!($entry{'end'} =~ m/^\d+$/)) {
		_message($options, "Warning: value in 'end' column must be a positive integer in $file line $lineCount.");
		next;
		#die ("Value in 'end' column must be a positive integer in $file line $lineCount");
	    }
	    if (!($entry{'strand'} =~ m/^(\+|\-|\.)$/)) {
		_message($options, "Warning: value in 'strand' column must be '+', '-', or '.' in $file line $lineCount.");
		next;
		#die ("Value in 'strand' column must be '+', '-', or '.' in $file line $lineCount");
	    }

	    #check start and end
	    if ($entry{'start'} > $global->{length}) {
		_message($options, "Warning: value in 'start' column must be less that the sequence length in $file line $lineCount.");
		next;
		#die ("Value in 'start' column must be less that the sequence length in $file line $lineCount");
	    }
	    if ($entry{'end'} > $global->{length}) {
		_message($options, "Warning: value in 'end' column must be less that the sequence length in $file line $lineCount.");
		next;
		#die ("Value in 'end' column must be less that the sequence length in $file line $lineCount");
	    }
	    
	    push (@entries, \%entry);
	}
    }
    close (INFILE) or die( "Cannot close file : $!");
    return \@entries;
}

sub _addToEntry {
    my $entry = shift;
    my $genBankFeatures = shift;
    
    if ((defined($entry->{'seqname'})) && (defined($genBankFeatures->{$entry->{'seqname'}})) && (!(defined($genBankFeatures->{$entry->{'seqname'}}->{multiples})))) {
	#will try to determine start, end, strand
	if ((!(defined($entry->{'start'}))) || (!($entry->{'start'} =~ m/^\d+$/))) {
	    $entry->{'start'} = $genBankFeatures->{$entry->{'seqname'}}->{start};
	}
	if ((!(defined($entry->{'end'}))) || (!($entry->{'end'} =~ m/^\d+$/))) {
	    $entry->{'end'} = $genBankFeatures->{$entry->{'seqname'}}->{end};
	}
	if ((!(defined($entry->{'strand'}))) || (!($entry->{'strand'} =~ m/^(\+|\-|\.)$/))) {
	    $entry->{'strand'} = $genBankFeatures->{$entry->{'seqname'}}->{strand};
	}
    }
}

sub _getFeatures {
    my $global = shift;
    my $seqObject = shift;

    if (($global->{format} ne "genbank") && ($global->{format} ne "embl")) {
	return undef;
    }

    #need to get the features from from the GenBank record.
    my @features = $seqObject->get_SeqFeatures();
    @features = @{_sortFeaturesByStart(\@features)};

    my %featureHash = ();
    foreach(@features) {
	my $feat = $_;

	my $strand = $feat->strand;
	my $start = $feat->start;
	my $stop = $feat->end;

	#this handles feature that spans start/stop
	my $location = $feat->location;
	my $locString = $location->to_FTstring;
	my @loc = split(/,/, $locString);

	if ($loc[0] =~ m/(\d+)\.\.(\d+)/) {
	    $start = $1;
	}

	if ($loc[scalar(@loc) - 1] =~ m/(\d+)\.\.(\d+)/) {
	    $stop = $2;
	}
	
	my $rf;
	if ($strand == 1) {
	    $rf = $start % 3;
	}
	elsif ($strand == -1) {
	    $rf = ($seqObject->length() - $stop + 1) % 3;
	}
	
	if ($rf == 0) {
	    $rf = 3;
	}

	my $geneName;
	if ($feat->has_tag('locus_tag')) {
	    $geneName = join("",$feat->get_tag_values('locus_tag'));
	}

	if (!(defined($geneName))) {
	    next;
	}

	#change some values
	if ($strand == -1) {
	    $strand = "-";
	}
	else {
	    $strand = "+";
	}

	#add this info to the geneHash
	my %geneHash = (start => undef,
			end => undef,
		        strand => undef,
			rf => undef);

	$geneHash{start} = $start;
	$geneHash{end} = $stop;
	$geneHash{rf} = $rf;
	$geneHash{strand} = $strand;

	if (defined($featureHash{$geneName})) {
	    #if multiple genes have the same tag, mark this gene with the multiples key
	    $featureHash{$geneName}->{multiples} = 1;
	}
	else {
	    $featureHash{$geneName} = \%geneHash;
	}
    }

    return \%featureHash;
}

sub _split {
    my $line = shift;
    my @values = ();
    if ($line =~ m/\t/) {
	@values = split(/\t/, $line);
    }
    elsif ($line =~ m/\,/) {
	@values = split(/\,/, $line); 
    }
    else {
	@values = split(/\s/, $line); 
    }
    foreach(@values) {
	$_ = _cleanValue($_);
    }
    return \@values;
}

sub _sortGFFByStart {
    my $features = shift;

    @$features = map { $_->[1] }
      sort { $a->[0] <=> $b->[0]}
      map { [ _getSortValueGFF($_), $_] }
      @$features;
    
    return $features;
}

sub _getSortValueGFF {
    my $feature = shift;
    return $feature->{'start'};
}

sub _cleanValue {
    my $value = shift;
    if (!defined($value)) {
	return ".";
    }
    if ($value =~ m/^\s*$/) {
	return ".";
    }
    $value =~ s/^\s+//g;
    $value =~ s/\s+$//g;
    $value =~ s/\"|\'//g;
    return $value;
}



sub _writeFooter {
    my $options = shift;
    my $footer = "</cgview>\n";
    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE $footer);
    close (OUTFILE) or die( "Cannot close file : $!");
}


sub _writeBlast {
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $strand = shift;
    my $file = shift;
    my $color = shift;
    my $thickness = shift;
    my $rf = shift; #1,2,3, or undefined for all reading frames

    if (_isTrue($options->{parse_reading_frame})) {
	#The order needs to be 3, 2, 1 because BLAST results are always drawn on the inside
	#of the backbone, and the slots are drawn closet to the backbone first.
	_writeBlastResults($options, $settings, $global, $seqObject, 1, $file, $color, $thickness, 3);
	_writeBlastResults($options, $settings, $global, $seqObject, 1, $file, $color, $thickness, 2);
 	_writeBlastResults($options, $settings, $global, $seqObject, 1, $file, $color, $thickness, 1);
	_drawDivider($options, $settings, $global, $seqObject, $strand);
	_writeBlastResults($options, $settings, $global, $seqObject, -1, $file, $color, $thickness, 1);
	_writeBlastResults($options, $settings, $global, $seqObject, -1, $file, $color, $thickness, 2);
 	_writeBlastResults($options, $settings, $global, $seqObject, -1, $file, $color, $thickness, 3);
    }
    else {
	_writeBlastResults($options, $settings, $global, $seqObject, 1, $file, $color, $thickness, undef);
    }
}


sub _drawDivider {

    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $strand = shift; #1 or -1
    
    my $opacity = $settings->{'featureOpacity'};
    my @outputArray = ();

    my $decoration = "arc";
    if ($strand == 1) {
	push (@outputArray, "<featureSlot featureThickness=\"$settings->{backboneThickness}\" showShading=\"true\" strand=\"direct\">\n");
    }
    else {
	push (@outputArray, "<featureSlot featureThickness=\"$settings->{backboneThickness}\" showShading=\"true\" strand=\"reverse\">\n");
    }
    
    push (@outputArray, "<feature color=\"$settings->{backboneColor}\" decoration=\"$decoration\">\n");
    push (@outputArray, "<featureRange start=\"1\" stop=\"$global->{length}\">\n");
    push (@outputArray, "</featureRange>\n");
    push (@outputArray, "</feature>\n");

    push (@outputArray, "</featureSlot>\n");
    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- BLAST results divider -->\n");
    print (OUTFILE join("", @outputArray));
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");
}

sub _writeBlastResults {
    my $options = shift;
    my $settings = shift;
    my $global = shift;
    my $seqObject = shift;
    my $strand = shift;
    my $file = shift;
    my $color = shift;
    my $thickness = shift;
    my $rf = shift; #1,2,3, or undefined for all reading frames


    my $opacity = $settings->{'featureOpacity'};
    
    my @outputArray = ();

    #blast results are always drawn on the inside of the backbone. Strand is used when parse_reading_frame is specified
    my $decoration = "arc";
    if ($strand == 1) {
	push (@outputArray, "<featureSlot strand=\"reverse\" showShading=\"false\" featureThickness=\"" . $thickness . "\" >\n");
    }
    else {
	push (@outputArray, "<featureSlot strand=\"reverse\" showShading=\"false\" featureThickness=\"" . $thickness . "\" >\n");
    }

    #may want to mark query positions on the map, regardless of whether they 
    #produced hits.
    if (_isTrue($options->{'show_queries'})) {
	my @queries = ();
	@queries = @{_parseBlastQueries($file, $settings)};
	@queries = @{_sortBLASTByStart(\@queries)};

	if ($strand == 1) {
	    @queries = reverse(@queries);
	}   
	#draw queries as faint features
	push(@outputArray, "<feature>\n");
	foreach(@queries) {
	    #if a reading frame is specified, only want to draw
	    #queries from this reading frame and strand
	    if ((defined($rf)) && (defined($_->{q_rf}))) {
		unless (($rf == $_->{q_rf}) && ($strand == $_->{q_strand})) {
		    next;
		}
	    }
	    push(@outputArray, "<featureRange start=\"$_->{q_start}\" stop=\"$_->{q_end}\" opacity=\"0.1\" radiusAdjustment=\"0.0\" proportionOfThickness=\"1.0\" color=\"$settings->{orfColor}\" />\n");
	}
	push(@outputArray, "</feature>\n");
    }
 
    my @features = ();
    @features = @{_parseBLAST($file, $settings, $global)};
    @features = @{_sortBLASTByStart(\@features)};

    if ($strand == 1) {
	@features = reverse(@features);
    }

   
    foreach(@features) {
	my $feat = $_;

	my $start = $feat->{'q_start'};
	my $stop = $feat->{'q_end'};

	if ((defined($rf)) && (defined($feat->{'q_rf'}))) { 

	    my $queryFrame = $feat->{'q_rf'};
	    my $queryStrand = $feat->{'q_strand'};

	    unless (($rf == $queryFrame) && ($strand == $queryStrand)) {
		next;
	    }
	}

	my $label;
	if (_containsText($feat->{'match_id'})) {
	    $label = $feat->{'match_id'};
	    $label = _escapeText($label);
	    if (length($label) > $settings->{'maxLabelLength'} - 3) {
		$label = substr($label, 0, $settings->{'maxLabelLength'} - 3) . "...";
	    }
	}

        #gi|15678261
	my $hyperlink;
	if (_containsText($feat->{'match_id'})) {
	    if ($feat->{'match_id'} =~ m/gi\|(\d+)/) {
		$hyperlink = $global->{'ncbiGiLink'} . $1;
	    }
	    #GeneID:1132092"
	    elsif ($feat->{'match_id'} =~ m/geneid\|(\d+)/) {
		$hyperlink = $global->{'ncbiGeneLink'} . $1;
	    }
	}

	my $mouseover;
	$mouseover = $label . "; " . $start . " to " . $stop;
	if (_containsText($feat->{'match_description'})) {
	    $mouseover = $mouseover . " " . $feat->{'match_description'};
	}

	$mouseover = $mouseover . "; percent identity=" . $feat->{'%_identity'} . "; alignment length=" . $feat->{alignment_length} . "; evalue=" . $feat->{'evalue'};
	$mouseover = _escapeText($mouseover);

	#now check entries and make tags
	if (_isTrue($options->{use_opacity})) {
	    $opacity = ".20";
	}
	else {
	    $opacity = "1.0";
	}

	my $feature = "<feature color=\"$color\" decoration=\"$decoration\" opacity=\"$opacity\" ";

	if (_isTrue($options->{hit_labels})) {
	    if (_containsText($label)) {
		$feature = $feature . "label=\"$label\" ";
	    }
	    if (_containsText($hyperlink)) {
		$feature = $feature . "hyperlink=\"$hyperlink\" ";		
	    }
	    if (_containsText($mouseover)) {
		$feature = $feature . "mouseover=\"$mouseover\" ";		
	    }
	}

	$feature = $feature . ">\n";


	#use %_identity to determine thickness of drawn feature
	my $thickness = $feat->{'%_identity'} / 100.0;
	$thickness = sprintf("%.10f", ($thickness)); 

	my $featureRange = "<featureRange start=\"$start\" stop=\"$stop\" radiusAdjustment=\"0.0\" proportionOfThickness=\"$thickness\" />\n";
	#now add $feature and $featureRange to @outputArray
	push (@outputArray, $feature . $featureRange . "</feature>\n");		
    }
    
    push (@outputArray, "</featureSlot>\n"); 
    my $strandTerm = undef;
    if ($strand == 1) {
	$strandTerm = "forward";
    }
    if ($strand == -1) {
	$strandTerm = "reverse";
    }
    
    my $rfTerm = undef;
    if (!(defined($rf))) {
	$rfTerm = "1,2,3";
    }
    else {
	$rfTerm = $rf;
    }
    
    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- BLAST results on $strandTerm strand in rf $rfTerm -->\n");
    print (OUTFILE join("", @outputArray));
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");
}

sub _parseBLAST {
    my $file = shift;
    my $settings = shift;
    my $global = shift;

    #The file can contain comments starting with'#'
    #The file must have a line beginning with a 'query_id' and indicating the column names:
    #query_id	match_id	match_description	%_identity	alignment_length	mismatches	gap_openings	q_start	q_end	s_start	s_end	evalue	bit_score
    
    my @required = ('query_id', '%_identity', 'q_start', 'q_end', 'alignment_length', 'evalue');

    my $lineCount = 0;
    my @columnTitles = ();
    my $columnsRead = 0;
    
    #program will be used to store the value of #PROGRAM in the blast results header.
    #if it is blastp or tblastn then q_start and q_end are in residues and need to
    #be converted to bases.
    my $program = undef;

    open (INFILE, $file) or die ("Cannot open the BLAST results file $file");
    
    #check for column titles
    while (my $line = <INFILE>) {
	$line =~ s/\cM|\n//g;
	$lineCount++;
	if ($line =~ m/^\#PROGRAM\s*=\s*([^\s]+)/) {
	    $program = $1;
	}
	if ($line =~ m/^\#/) {
	    next;
	}
	if ($line =~ m/^query_id/) {
	    $columnsRead = 1;
	    @columnTitles = @{_split($line)};
	    last;
	}
    }

    if (!(defined($program))) {
	die ("Cannot parse the #PROGRAM field in the BLAST results file $file");
    }

    #print Dumper(@columnTitles);

    #now check for required columns  
    foreach(@required) {
	my $req = $_;
	my $match = 0;
	foreach(@columnTitles) {
	    my $columnTitle = $_;
	    if ($columnTitle eq $req) {
		$match = 1;
		last;
	    }
	}
	if (!($match)) {
	    die ("The BLAST results in $file do not contain a column labeled $req");
	}
    }

    #factor to convert amino acid scales to base scales
    my $scale;
    if (($program =~ m/^blastp$/) || ($program =~ m/^tblastn$/)) {
	$scale = 3;
    }
    else {
	$scale = 1;
    }

    #read the remaining entries
    my @entries = ();
    while (my $line = <INFILE>) {    
	$line =~ s/\cM|\n//g;
	$lineCount++;
	if ($line =~ m/^\#/) {
	    next;
	}
	if ($line =~ m/\S/) {
	    my @values = @{_split($line)};
	    #skip lines with missing values
	    if (scalar(@values) != scalar(@columnTitles)) {
		next;
	    }

	    my %entry = ();
	    for (my $i = 0; $i < scalar(@columnTitles); $i++) {
		$entry{$columnTitles[$i]} = $values[$i]; 
	    }
	    
	    #do some error checking of values
	    #check query_id, %_identity, q_start, and q_end
	    #skip if no identity value
	    if (!($entry{'%_identity'} =~ m/\d/)) {
		die ("No \%_identity value BLAST results $file line $lineCount");
	    }

	    if (!($entry{'q_start'} =~ m/\d/)) {
		die ("No q_start value BLAST results $file line $lineCount");
	    }
	    if (!($entry{'q_end'} =~ m/\d/)) {
		die ("No q_end value BLAST results $file line $lineCount");
	    }

	    #try to add reading frame and strand information using information
	    #in the query_id:
	    #orf3_start=3691;end=3858;strand=1;rf=1
	    #orf16_start=8095;end=8178;strand=1;rf=1
	    #
	    #The q_start and q_end values are region of the query that matched
	    #the hit. Depending on the search type, these can be in amino acids
	    #or bases. The values in the query_id are always in bases. The
	    #scale factor is used to convert the q_start and q_end values
	    #so that they can be used to adjust the values in the query_id.
	    #This allows hits to be mapped to the genomic sequence
	    if ($entry{'query_id'} =~ m/start=(\d+);end=\d+;strand=(\-*\d+);rf=(\d+)\s*$/) {
		$entry{'q_start'} = $entry{'q_start'} * $scale + $1 - 1 * $scale;
		$entry{'q_end'} = $entry{'q_end'} * $scale + $1 - 1 * $scale;
		$entry{'q_strand'} = $2;
		$entry{'q_rf'} = $3;		
	    }
	    
	    #try to adjust q_start and q_end using information in title when rf and strand not specified
	    #start=X;end=Y
	    if ($entry{'query_id'} =~ m/start=(\d+);end=\d+\s*$/) {
		$entry{'q_start'} = $entry{'q_start'} * $scale + $1 - 1 * $scale;
		$entry{'q_end'} = $entry{'q_end'} * $scale + $1 - 1 * $scale;		
	    }

	    #check q_start and q_end to make sure they are in range
	    #depending on the blast search, the q_start may be larger than the q_end,
	    #to indicate that the hit was obtained using the reverse strand of the query
	    #Will add column called '_query_strand' to record this
	    if ($entry{'q_start'} > $entry{'q_end'}) {
		my $temp = $entry{'q_start'};
		$entry{'q_start'} = $entry{'q_end'};
		$entry{'q_end'} = $temp;
		$entry{'_query_strand'} = -1;
	    }
	    else {
		$entry{'_query_strand'} = 1;
	    }

	    if ($entry{'q_start'} < 1) {
		die ("q_start value " . $entry{'q_start'} . " is less than 1 BLAST results $file line $lineCount");
	    }
	    if ($entry{'q_end'} < 1) {
		die ("q_end value " . $entry{'q_end'} . " is less than 1 BLAST results $file line $lineCount");
	    }
	    if ($entry{'q_start'} > $global->{'length'}) {
		die ("q_start value " . $entry{'q_start'} . " is greater than sequence length " . $global->{'length'} . " BLAST results $file line $lineCount");
	    }
	    if ($entry{'q_end'} > $global->{'length'}) {
		die ("q_end value " . $entry{'q_end'} . " is greater than sequence length " . $global->{'length'} . " BLAST results $file line $lineCount");
	    }
	    push (@entries, \%entry);
	}
    }
    close (INFILE) or die( "Cannot close file : $!");
    return \@entries;
}


sub _sortBLASTByStart {
    my $features = shift;

    @$features = map { $_->[1] }
      sort { $a->[0] <=> $b->[0]}
      map { [ _getSortValueBLAST($_), $_] }
      @$features;
    
    return $features;
}

sub _getSortValueBLAST {
    my $feature = shift;
    return $feature->{'q_start'};
}

sub _parseBlastQueries {
    my $file = shift;
    my $lineCount = 0;

    open (INFILE, $file) or die ("Cannot open the BLAST results file $file");
    #won't bother checking columns.
    my %entries = ();
    while (my $line = <INFILE>) { 
	$line =~ s/\cM|\n//g;
	$lineCount++;
	#orf1_start=142;end=255;strand=1;rf=1
	#orf_900_start=142;end=255;strand=1;rf=1
	#some gene_start=142;end=255;strand=1;rf=1 
	if ($line =~ m/^([^\t]+)_start=(\d+);end=(\d+);strand=(\-*\d+);rf=(\d+)/) {
	    my %entry = ();
	    $entry{'q_start'} = $2;
	    $entry{'q_end'} = $3;
	    $entry{'q_strand'} = $4;
	    $entry{'q_rf'} = $5;
	    $entries{$1 . $2 . $3 . $4} = \%entry;		
	}
	#some gene_start=142;end=255
	elsif ($line =~ m/^([^\t]+)_start=(\d+);end=(\d+)/) {
	    my %entry = ();
	    $entry{'q_start'} = $2;
	    $entry{'q_end'} = $3;
	    $entry{'q_strand'} = "1";
	    $entry{'q_rf'} = undef;
	    $entries{$1 . $2 . $3} = \%entry;	
	}
    }
    close (INFILE) or die( "Cannot close file : $!");
    my @values = values(%entries);
    return \@values;
}

sub _writeBaseContent {
    my $options = shift;
    my $settings = shift;
    my $seqObject = shift;

    #1 or -1. Strand here only determines whether graph is drawn on outside or inside of backbone.
    my $strand = shift;

    #type should be gc_content, at_content, gc_skew, or at_skew.
    my $type = shift;

    my $plotTerm;
    if ($type eq 'gc_content') {
	$plotTerm = "GC content";
    }
    elsif ($type eq 'gc_skew') {
	$plotTerm = "GC skew";
    }
    elsif ($type eq 'at_content') {
	$plotTerm = "AT content";
    }
    elsif ($type eq 'at_skew') {
	$plotTerm = "AT skew";
    }
    
    my $globalBaseContentInfo = _getGlobalBaseContentInfo($options, $settings, $seqObject, $type);

    #The max, min, and average have been scaled for some plot types to facilatate CGView plotting.
    #When returning these to the user, adjust the values so that they are in the standard ranges.
    my $actualMax;
    my $actualMin;
    my $actualAverage;

    if (($type eq 'gc_content') || ($type eq 'at_content')) {
	$actualMax = $globalBaseContentInfo->{'max'};
	$actualMin = $globalBaseContentInfo->{'min'};
	$actualAverage = $globalBaseContentInfo->{'average'};
    }
    elsif (($type eq 'gc_skew') || ($type eq 'at_skew')) {
	$actualMax = _skewCorrect($globalBaseContentInfo->{'max'});
	$actualMin = _skewCorrect($globalBaseContentInfo->{'min'});
	$actualAverage = _skewCorrect($globalBaseContentInfo->{'average'});
    }

    $actualMax = sprintf("%.4f", $actualMax);
    $actualMin = sprintf("%.4f", $actualMin);
    $actualAverage = sprintf("%.4f", $actualAverage);

    _message($param{options}, "Plotting $plotTerm using a window size of $options->{window} and a step of $options->{step}.");
    _message($param{options}, "The maximum $plotTerm value is $actualMax.");
    _message($param{options}, "The minimum $plotTerm value is $actualMin.");
    _message($param{options}, "The average $plotTerm value is $actualAverage.");

    if (_isTrue($options->{scale})) {
	_message($param{options}, "$plotTerm will be scaled based on the maximum and minimum values.");
    }

    if (_isTrue($options->{average})) {
	_message($param{options}, "$plotTerm will be plotted as the deviation from the average value.");
    }

    my $opacity = $settings->{'featureOpacity'};
    my $decoration = 'arc';
    my $positiveColor;
    my $negativeColor;
    if ($type eq 'gc_content') {
	$positiveColor = $settings->{'gcColorPos'};
	$negativeColor = $settings->{'gcColorNeg'};
    }
    elsif ($type eq 'at_content') {
	$positiveColor = $settings->{'atColorPos'};
	$negativeColor = $settings->{'atColorNeg'};
    }
    elsif ($type eq 'gc_skew') {
	$positiveColor = $settings->{'gcSkewColorPos'};
	$negativeColor = $settings->{'gcSkewColorNeg'};
    }
    elsif ($type eq 'at_skew') {
	$positiveColor = $settings->{'atSkewColorPos'};
	$negativeColor = $settings->{'atSkewColorNeg'};
    }

    my $upstreamLength = sprintf("%.f", $options->{'window'} / 2);
    my $downstreamLength = $options->{'window'} - $upstreamLength;
    my $step = $options->{'step'};
    my $isLinear = undef;
    if ($settings->{'isLinear'} eq "true") {
	$isLinear = 1;
    }
    else {
	$isLinear = 0;
    }

    my $dna = $seqObject->seq();
    my $originalLength = length($dna);
    my $subseq;
    my $value;
    my $positionCorrection = 0;
    my $firstBase;
    my $lastBase;

    if (!($isLinear)) {
	my $prefix = substr($dna, length($dna) - $upstreamLength, $upstreamLength);
	my $suffix = substr($dna, 0, $downstreamLength);
	$dna = $prefix . $dna . $suffix;
	$positionCorrection = length($prefix);
    }
    
    my $length = length($dna);
    my $maxDeviationUp = $globalBaseContentInfo->{'max'} - $globalBaseContentInfo->{'average'};
    my $maxDeviationDown = $globalBaseContentInfo->{'average'} - $globalBaseContentInfo->{'min'};
    my $average = $globalBaseContentInfo->{'average'};
    my $maxDeviation;
    if ($maxDeviationUp > $maxDeviationDown) {
	$maxDeviation = $maxDeviationUp;
    }
    else {
	$maxDeviation = $maxDeviationDown;
    }

    my @outputArray = ();
    if ($strand == 1) {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"direct\" featureThickness=\"" . $settings->{'featureThicknessPlot'} . "\" >\n");
    }
    else {
	push (@outputArray, "<featureSlot showShading=\"false\" strand=\"reverse\" featureThickness=\"" . $settings->{'featureThicknessPlot'} . "\" >\n");
    }

    push (@outputArray, "<feature decoration=\"$decoration\" opacity=\"$opacity\" >\n");

    for (my $i = 1 + $upstreamLength; $i <= $length - $downstreamLength; $i = $i + $step) {

	$subseq = substr($dna, $i - $upstreamLength - 1, ($i + $downstreamLength) - ($i - $upstreamLength - 1));

	#These set the width and position of the "point" on the map. 
	#They are not the actual first base and last base in the sliding window.
	$firstBase = $i - $positionCorrection;
	$lastBase = $firstBase + $step;

	if ($lastBase > $originalLength) {
	    $lastBase = $originalLength;
	}

	$value = _calc($type, $subseq);
        #want bars above middle line for values > 0.5 and below middle line for values < 0.5

	my $barHeight;
	my $radiusShift;
	my $color;

	if ($value > $average) {
	    $color = $positiveColor;
	    $barHeight = $value - $average;
	    $barHeight = $barHeight * 0.5 / $maxDeviation;
	    $radiusShift = 0.5 + $barHeight / 2.0;
	}
	elsif ($value < $average) {
	    $color = $negativeColor;
	    $barHeight = $average - $value;
	    $barHeight = $barHeight * 0.5 / $maxDeviation;
	    $radiusShift = 0.5 - $barHeight / 2;
	}
	else {
	    $color = $positiveColor;
	    $radiusShift = 0.5;
	    $barHeight = $settings->{'plotLineThickness'};
	}

	push (@outputArray, "<featureRange color=\"" . $color . "\" start=\"$firstBase\" stop=\"$lastBase\" proportionOfThickness=\"" .  $barHeight  . "\" radiusAdjustment=\"$radiusShift\" />\n");
    } 

    push (@outputArray, "</feature>\n");

    push (@outputArray, "</featureSlot>\n");  
    my $strandTerm = undef;
    if ($strand == 1) {
	$strandTerm = "forward";
    }
    if ($strand == -1) {
	$strandTerm = "reverse";
    }

    open (OUTFILE, "+>>" . $options->{"output"}) or die( "Cannot open file : $!" );
    print (OUTFILE "\n\n\n\n");
    print (OUTFILE "<!-- $plotTerm for $strandTerm strand -->\n");
    print (OUTFILE join("", @outputArray));
    print (OUTFILE "\n");
    close (OUTFILE) or die( "Cannot close file : $!");

}

sub _getGlobalBaseContentInfo {
    my $options = shift;
    my $settings = shift;
    my $seqObject = shift;

    #type should be gc_content, at_content, gc_skew, or at_skew.
    my $type = shift;

    my %globalBaseContentInfo = (min => 1,
				 max => 0,
				 average => 0);

    my $upstreamLength = sprintf("%.f", $options->{'window'} / 2);
    my $downstreamLength = $options->{'window'} - $upstreamLength;
    my $step = $options->{'step'};
    my $isLinear = undef;
    if ($settings->{'isLinear'} eq "true") {
	$isLinear = 1;
    }
    else {
	$isLinear = 0;
    }

    my $dna = $seqObject->seq();
 
    if (_isTrue($options->{'average'})) {   
	$globalBaseContentInfo{'average'} = _calc($type, $dna);
    }
    else {
	$globalBaseContentInfo{'average'} = 0.5;
    }

    if (!(_isTrue($options->{'scale'}))) { 
	$globalBaseContentInfo{'min'} = 0;
	$globalBaseContentInfo{'max'} = 1;
	return \%globalBaseContentInfo;	
    }

    my $subseq;
    my $value;

    if (!($isLinear)) {
	my $prefix = substr($dna, length($dna) - $upstreamLength, $upstreamLength);
	my $suffix = substr($dna, 0, $downstreamLength);
	$dna = $prefix . $dna . $suffix;	
    }
    
    my $length = length($dna);

    for (my $i = 1 + $upstreamLength; $i <= $length - $downstreamLength; $i = $i + $step) {

	$subseq = substr($dna, $i - $upstreamLength - 1, ($i + $downstreamLength) - ($i - $upstreamLength - 1));

	$value = _calc($type, $subseq);

	if ($value > $globalBaseContentInfo{'max'}) {
	    $globalBaseContentInfo{'max'} = $value;
	}
		
	if ($value < $globalBaseContentInfo{'min'}) {
	    $globalBaseContentInfo{'min'} = $value;
	}
    } 

    return \%globalBaseContentInfo;

}   

sub _calc {
    my $type = shift;
    my $dna = shift;

    if ($type eq "gc_content") {
	return _calcGCContent($dna);
    }
    elsif ($type eq "at_content") {
	return _calcATContent($dna);
    }
    elsif ($type eq "gc_skew") {
	return _calcGCSkew($dna);
    }
    elsif ($type eq "at_skew") {
	return _calcATSkew($dna);
    }
    else {
	die ("unknown calc type");
    }
}

sub _calcGCContent {
    my $dna = lc(shift);
    my $total = length($dna);
    my $g = ($dna =~ tr/g/G/);
    my $c = ($dna =~ tr/c/C/);

    if ($total == 0) {
	return 0.5;
    }

    return sprintf("%.5f", (($g + $c) / $total));
}

sub _calcATContent {
    my $dna = lc(shift);
    my $total = length($dna);
    my $a = ($dna =~ tr/a/A/);
    my $t = ($dna =~ tr/t/T/);

    if ($total == 0) {
	return 0.5;
    }
    
    return sprintf("%.5f", (($a + $t) / $total));
}

sub _calcGCSkew {
    my $dna = lc(shift);
    my $c = ($dna =~ tr/c/C/);
    my $g = ($dna =~ tr/g/G/);
    
    if (($g + $c) == 0) {
	return 0.5;
    }    

    #gives value between -1 and 1
    my $value = ($g - $c) / ($g + $c);

    #scale to a value between 0 and 1
    $value = 0.5 + $value / 2.0; 

    return sprintf("%.5f", ($value));
}

sub _calcATSkew {
    my $dna = lc(shift);
    my $a = ($dna =~ tr/a/A/);
    my $t = ($dna =~ tr/t/T/);
    
    if (($a + $t) == 0) {
	return 0.5;
    }    

    #gives value between -1 and 1
    my $value = ($a - $t) / ($a + $t);

    #scale to a value between 0 and 1
    $value = 0.5 + $value / 2.0; 

    return sprintf("%.5f", ($value));
}

#for reporting values to user
sub _skewCorrect {
    my $value = shift;
    return (2*($value - 0.5));
}
