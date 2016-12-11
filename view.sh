#!/bin/bash

# find jar-file with highest version number
for i in `ls build/dist/maven/*/poi-*.jar | grep -v -- "-sources" | grep -v -- "-examples" | grep -v -- "-excelant" | grep -v -- "-ooxml" | grep -v -- "-javadoc"`;do
	CP=$i:$CP
done

if [ "$CP" = "" ]
then
	echo Did not find jar files in directory build/dist, please run target "jar"
	exit 1
fi

echo Using jar $CP

file=$1
echo Found extension ${file: -4}
if [ ${file: -4} == ".doc" ]
then
	java -cp $CP org.apache.poi.hwpf.dev.HWPFLister "$@" --pictures --escher
else
	java -cp $CP org.apache.poi.hssf.dev.BiffViewer --escher "$@"
fi
