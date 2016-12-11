#!/bin/sh

# find jar-file with highest version number
for i in `ls build/dist/poi-*.jar | grep -v -- "-sources" | grep -v -- "-excelant" | grep -v -- "-ooxml"`;do
	CP=$CP:$i
done
echo Using classpath $CP

java -cp $CP org.apache.poi.hslf.examples.PPT2PNG "$@"
