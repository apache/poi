#!/bin/sh
# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

chmod u+x ./tools/ant/bin/antRun
chmod u+x ./tools/ant/bin/ant

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$TERM" = "cygwin" ] ; then
  S=';'
else
  S=':'
fi

# ----- Set Up The Runtime Classpath ------------------------------------------

OLD_ANT_HOME=$ANT_HOME
unset ANT_HOME

CP=$CLASSPATH
export CP
unset CLASSPATH

CLASSPATH="`echo ./lib/endorsed/*.jar | tr ' ' $S``echo ./tools/centipede/lib/*.jar | tr ' ' $S`"
export CLASSPATH

$PWD/./tools/ant/bin/ant -listener org.apache.tools.ant.XmlLogger $@ 

unset CLASSPATH

CLASSPATH=$CP
export CLASSPATH
ANT_HOME=OLD_ANT_HOME
export ANT_HOME
