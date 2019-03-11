BRIG is a cross-platform (Windows/Mac/Unix) application that can display circular comparisons between a large number of genomes, with a focus on handling genome assembly data.

MAJOR FEATURES
==============
* Images show similarity between a central reference sequence and other sequences as concentric rings.
* BRIG will perform all BLAST comparisons and file parsing automatically via a simple GUI.
* Contig boundaries and read coverage can be displayed for draft genomes; customized graphs and annotations can be displayed.
* Using a user-defined set of genes as input, BRIG can display gene presence, absence, truncation or sequence variation in a set of complete genomes, draft genomes or even raw, unassembled sequence data.
* BRIG also accepts SAM-formatted read-mapping files enabling genomic regions present in unassembled sequence data from multiple samples to be compared simultaneously.

Available @ http://sourceforge.net/projects/brig/

----Citing BRIG
Please cite the BRIG paper if BRIG is used to generate figures for publications:

NF Alikhan, NK Petty, NL Ben Zakour, SA Beatson (2011) BLAST Ring Image Generator (BRIG): simple prokaryote genome comparisons, BMC Genomics, 12:402. PMID: 21824423

----Installation----
There's no real 'Installation' process for BRIG itself  but users will require NCBI BLAST+ or BLAST legacy, and Java 1.6 or greater to be installed.

To run BRIG users need to:

* Download the latest version (BRIG-x.xx-dist.zip) from http://sourceforge.net/projects/brig/
* Unzip BRIG-x.xx-dist.zip to a desired location
* Run BRIG.jar, by double clicking.

Users who wish to run BRIG from the command-line need to:
* Navigate to the unpacked BRIG folder in a command-line interface (terminal, console, command prompt).
* Run 'java -Xmx1500M -jar BRIG.jar'. Where -Xmx specifies the amount of memory allocated to BRIG.

----Installing BLAST----
The latest version of BLAST+ can be downloaded from:

ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/

BLAST+ offers a number of improvements on the original BLAST implementation and comes as a bundled installer, which will walk users through the installation process. Please read the published paper on BLAST+: Camacho, C., G. Coulouris, et al. (2009). 
'BLAST+: architecture and applications.' BMC Bioinformatics 10(1): 421

The latest version of BLAST legacy can be downloaded from:

ftp://ftp.ncbi.nlm.nih.gov/blast/executables/release/LATEST/

BLAST legacy comes as a compressed package, which will unzip the BLAST binaries where ever the package is. We advise users to first create a BLAST directory (in either the home or applications directory), copy the downloaded BLAST package to that directory and unzip the package.

BRIG supports both BLAST+ & BLAST Legacy. If BRIG cannot find BLAST it will prompt users at runtime. Users can specify the location of their BLAST installation in the BRIG options menu which is: Main window > Preferences > BRIG options.

N.B: If BOTH BLAST+ and legacy versions are in the same location, BRIG will prefer BLAST+


----LICENCE----
Copyright Nabil Alikhan 2010-2011.
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
