# Warning when using BLAST

BRIG relies on the Basic Local Alignment Search Tool (BLAST) for genome comparisons. BLAST has a number of behaviours that may seem counterintuitive and we encourage users to learn about local alignment and the BLAST algorithm to fully understand the images that BRIG produces. There are a few concepts to keep in mind when using BRIG:

## Low complexity filtering

!!! warning
    BLAST filters may cause gaps in alignments, which will show up as blank regions in BRIG images.

BLAST filters (BLAST legacy `-F` flag or BLAST+ `-dust`/`-seg` no flag) filter the query sequence for low-complexity sequences by default. This includes sequences that are highly repetitive or contain the same nucleotide for long lengths of the sequences. Low-complexity filtering is generally a good idea, but it may break long matches into several smaller matches.

This is often shown in BRIG images as truncations or gaps in alignments. It is particularly obvious in very small reference sequences where alignments are shown on a gene-by-gene level.

*To prevent this, either turn off filtering or use soft masking.*

## Expected values (e-values) and bit scores

!!! warning
    BLAST's bitscore filtering may cause different results in BRIG if users swap the query and reference sequences, particularly if these are very different sizes.

BLAST uses statistical thresholds to filter out "bad alignments"; alignment matches that appear random to BLAST. One of these thresholds is the e-value, which is the probability of the alignment occurring by chance, given the complexity of the match, sequence composition and the size of the database. It is more likely in a larger sequence that an alignment could occur by chance, so BLAST is more critical of these matches.

This can create different expected values if BLAST is used with the same reference sequence against databases of different sizes and may potentially filter out significant matches or include poor scoring ones.

Because of this, users might notice different results in BRIG images if they swap the order of the database and reference sequences around in the BLAST, especially if the two sequences are quite different in size. The differences are often due to a few very low-scoring hits.

Users should consider what an appropriate e-value threshold is for the comparisons that they run. Remember, that BLAST runs with an e-value of 10 by default, we recommend that users change this value. Users can set the final threshold (e-value) with the `-e` flag in BLAST legacy or `-evalue` flag in BLAST+.
