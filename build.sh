#!/bin/sh

chmod u+x ./tools/ant/bin/antRun
chmod u+x ./tools/ant/bin/ant


OLD_ANT_HOME=$ANT_HOME
unset ANT_HOME

CP=$CLASSPATH
export CP
unset CLASSPATH

CLASSPATH="./tools/centipede/lib/xml-apis.jar:./tools/centipede/lib/xerces.jar:./tools/centipede/lib/xalan.jar:./tools/centipede/lib/junit.jar:./tools/centipede/lib/jIzPress.jar:./tools/centipede/lib/jtidy.jar"
export CLASSPATH

$PWD/./tools/ant/bin/ant -listener org.apache.tools.ant.XmlLogger $@ 

unset CLASSPATH

CLASSPATH=$CP
export CLASSPATH
ANT_HOME=OLD_ANT_HOME
export ANT_HOME
