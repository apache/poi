#!/bin/sh

# find jar-file with highest version number
for i in `ls build/dist/poi-*.jar | grep -v -- "-sources" | grep -v -- "-examples" | grep -v -- "-excelant" | grep -v -- "-ooxml" | grep -v -- "-scratchpad"`;do
	CP=$i
done
echo Using jar $CP

java -cp $CP org.apache.poi.hssf.dev.BiffViewer --escher "$@"
