#!/bin/sh

export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk-amd64

export PATH=$JAVA_HOME/bin:$PATH

export ANT_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

#ls $JAVA_HOME
java -version

# compile-ooxml-lite test-all testcoveragereport
#/opt/apache/apache-ant/apache-ant-1.7.1/bin/
ant init compile-examples
if [ $? -ne 0 ];then
  exit 1
fi

if [ ! -d build/ooxml-lite-classes ];then
  ant compile-ooxml-lite
  if [ $? -ne 0 ];then
	exit 1
  fi
fi


for i in build/dist/maven/poi*/*.jar build/*classes ooxml-lib/xmlbeans-2.3.0.jar ooxml-lib/dom4j-1.6.1.jar;do
	JARS="$JARS:$i"
done

ROWS=25000
COLS=25

AGENT=-agentpath:/opt/dynaTrace/dynatrace-6.1.0/agent/lib64/libdtagent.so=name=ApachePOI,server=localhost

echo CP: $JARS
java -cp $JARS -Xmx1024m $AGENT org.apache.poi.ss.examples.SSPerformanceTest HSSF $ROWS $COLS 0 && \
java -cp $JARS -Xmx1024m $AGENT org.apache.poi.ss.examples.SSPerformanceTest XSSF $ROWS $COLS 0 && \
java -cp $JARS -Xmx1024m $AGENT org.apache.poi.ss.examples.SSPerformanceTest SXSSF $ROWS $COLS 0 && \
echo All done
