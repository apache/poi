#!/bin/sh

echo
echo "POI Build System            "
echo "----------------------------"

cp -f ./lib/optional/xalan*.jar ./tools/lib
cp -f ./lib/optional/xerces*.jar ./tools/lib
cp -f ./lib/optional/xml-api*.jar ./tools/lib
cp -f ./lib/optional/jtidy*.jar ./tools/lib

chmod u+x ./tools/bin/antRun
chmod u+x ./tools/bin/ant

unset ANT_HOME

CP=$CLASSPATH
export CP
unset CLASSPATH

$PWD/tools/bin/ant -logger org.apache.tools.ant.NoBannerLogger -emacs $@ 

CLASSPATH=$CP
export CLASSPATH
