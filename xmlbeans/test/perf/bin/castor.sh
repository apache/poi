#/bin/sh

export MEM=64
export FLAVOR=
export FILENAME=

#scan first param
case "$2" in
	memory) export MEM=$3 ;;
	flavor) export FLAVOR=$3 ;;
	filename) export FILENAME=$3 ;;
esac
#scan second param
case "$4" in
	memory) export MEM=$5 ;;
	flavor) export FLAVOR=$5 ;;
	filename) export FILENAME=$5 ;;
esac
#scan third param
case "$6" in
	memory) export MEM=$7 ;;
	flavor) export FLAVOR=$7 ;;
	filename) export FILENAME=$7 ;;
esac
java -Xmx"$MEM"m -Xbootclasspath/p:$XMLBEANS_PERFROOT/3rdparty/xerces/xerces-2_7_1/xml-apis.jar:$XMLBEANS_PERFROOT/3rdparty/xerces/xerces-2_7_1/xercesImpl.jar -classpath $XMLBEANS_PERFROOT/build:$XMLBEANS_PERFROOT/schema_build/castor-purchase-order.jar:$XMLBEANS_PERFROOT/schema_build/castor-primitives.jar:$XMLBEANS_PERFROOT/schema_build/castor-non-primitives.jar:$XMLBEANS_PERFROOT/3rdparty/castor/castor-0.9.5.4.jar -DPERF_ROOT=$XMLBEANS_PERFROOT org.apache.xmlbeans.test.performance.castor.$1 $FLAVOR $FILENAME 
