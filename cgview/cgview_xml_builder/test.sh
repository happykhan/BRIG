#!/bin/sh -x
#cgview=/home/paul/misc/cgview/application/cgview/cgview.jar
cgview=../cgview.jar

if [ ! -d ./sample_output ]; then
    mkdir ./sample_output
fi

for size in small medium large x-large
do
    #simple
    perl cgview_xml_builder.pl -sequence sample_input/R_denitrificans.gbk -genes sample_input/R_denitrificans.cogs -output sample_output/${size}_simple.xml -gc_content T -gc_skew T -size $size -title 'Roseobacter denitrificans'

    java -jar -Xmx2000m $cgview -i sample_output/${size}_simple.xml -o sample_output/${size}_simple.png -f png

    #complex
    perl cgview_xml_builder.pl -sequence sample_input/R_denitrificans.gbk -genes sample_input/R_denitrificans.cogs -output sample_output/${size}_complex.xml -reading_frames T -orfs T -combined_orfs T -gc_content T -gc_skew T -at_content T -at_skew T -size $size -title 'Roseobacter denitrificans'

    java -jar -Xmx2000m $cgview -i sample_output/${size}_complex.xml -o sample_output/${size}_complex.png -f png 
done


#create an x-large with feature labels
size=x-large
perl cgview_xml_builder.pl -sequence sample_input/R_denitrificans.gbk -genes sample_input/R_denitrificans.cogs -output sample_output/${size}_complex_labels.xml -reading_frames T -orfs T -combined_orfs T -gc_content T -gc_skew T -at_content T -at_skew T -size $size -title 'Roseobacter denitrificans' -feature_label T

java -jar -Xmx2000m $cgview -i sample_output/${size}_complex_labels.xml -o sample_output/${size}_complex_labels.png -f png 
