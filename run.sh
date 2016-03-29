#!/bin/sh

# integration-tests fail: https://bitbucket.org/fakraemer/line-break-measurer-test/src/dc8e619063a4?at=master
#JAVA_HOME=/devtools/jdk1.6.0_45

#JAVA_HOME=/devtools/jdk1.7.0_76_x64
JAVA_HOME=/devtools/jdk1.8.0_66_x64
export JAVA_HOME

ANT_HOME=/devtools/apache-ant-1.9.4
export ANT_HOME

PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH
export PATH

FORREST_HOME=/devtools/apache-forrest-0.5.1-bin
export FORREST_HOME

ANT_OPTS="-Xmx2048m -XX:MaxPermSize=256m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=D:/workspaces"
export ANT_OPTS

rm -f build/*-testokfile.txt

# -Dadditionaljar=lib/xercesImpl-2.6.1.jar 
# -Dmain.docs.notRequired=true
nice -n 19 /devtools/apache-ant-1.9.6/bin/ant -Dcoverage.enabled=false  $*
