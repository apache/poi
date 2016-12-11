#!/bin/sh

export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk-amd64

# use this JVM for creating javadocs:
#export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64

export PATH=$JAVA_HOME/bin:$PATH

export FORREST_HOME=/opt/apache/forrest/apache-forrest-0.5.1-bin

export ANT_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

#ls $JAVA_HOME
java -version

# compile-ooxml-lite test-all testcoveragereport
#/opt/apache/apache-ant/apache-ant-1.7.1/bin/
ant \
	-Dcoverage.enabled=true \
	-Dorg.apache.poi.util.POILogger=org.apache.poi.util.NullLogger \
	-Dhalt.on.test.failure=false \
	$*
/opt/apache/apache-ant/apache-ant-1.7.1/bin/ant -Dcoverage.enabled=true $*
