#!/bin/sh

# find jar-file with highest version number
for i in `ls build/dist/poi-*.jar | grep -v -- "-sources" | grep -v -- "-excelant" | grep -v -- "-scratchpad" | grep -v -- "-javadoc"`;do
	CP=$i:$CP
done

for i in `ls ooxml-lib/*.jar | grep -v -- "xmlbeans-2.3.0.jar"`;do
	CP=$i:$CP
done

if [ "$CP" = "" ]
then
	echo Did not find jar files in directory build/dist, please run target "jar"
	exit 1
fi

echo Using jar $CP

java -cp $CP org.apache.poi.ss.examples.ToCSV "$@"
