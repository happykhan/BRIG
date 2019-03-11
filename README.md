BRIG is a cross-platform (Windows/Mac/Unix) application that can display circular comparisons between a large number of 
genomes, with a focus on handling genome assembly data.

Major Features
==============
* Images show similarity between a central reference sequence and other sequences as concentric rings.
* BRIG will perform all BLAST comparisons and file parsing automatically via a simple GUI.
* Contig boundaries and read coverage can be displayed for draft genomes; customized graphs and annotations can be displayed.
* Using a user-defined set of genes as input, BRIG can display gene presence, absence, truncation or sequence variation in a set of complete genomes, draft genomes or even raw, unassembled sequence data.
* BRIG also accepts SAM-formatted read-mapping files enabling genomic regions present in unassembled sequence data from multiple samples to be compared simultaneously.

Available @ http://sourceforge.net/projects/brig/

Installation and usage
======================
There's no real 'Installation' process for BRIG itself  but users will require NCBI BLAST+ or BLAST legacy, and Java 1.6
 or greater to be installed.

To run BRIG users need to:

* Download the latest version (BRIG-x.xx-dist.zip) from http://sourceforge.net/projects/brig/
* Unzip BRIG-x.xx-dist.zip to a desired location
* Run BRIG.jar, by double clicking.

Users who wish to run BRIG from the command-line need to:
* Navigate to the unpacked BRIG folder in a command-line interface (terminal, console, command prompt).
* Run 'java -Xmx1500M -jar BRIG.jar'. Where -Xmx specifies the amount of memory allocated to BRIG.


Feedback/Issues
===============
Please report any issues to the [issues page](https://github.com/happykhan/BRIG/issues)

License
=======
Copyright Nabil-Fareed Alikhan 2010-2019.
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public 
License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later 
version.

This program is distributed in the hope that it will be useful, but without any warranty; without even the implied 
warranty of merchantability or fitness for a particular purpose. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, 
see <http://www.gnu.org/licenses/>.

Citation
========
Please cite the BRIG paper if BRIG is used to generate figures for publications:

NF Alikhan, NK Petty, NL Ben Zakour, SA Beatson (2011) BLAST Ring Image Generator (BRIG): simple prokaryote genome 
comparisons, BMC Genomics, 12:402. doi: [https://doi.org/10.1186/1471-2164-12-402](https://doi.org/10.1186/1471-2164-12-402)
