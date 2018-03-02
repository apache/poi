
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

java -Xmx"$MEM"m -classpath $XMLBEANS_PERFROOT/build:$XMLBEANS_PERFROOT/schema_build/svn-purchase-order.jar:$XMLBEANS_PERFROOT/schema_build/svn-primitives.jar:$XMLBEANS_PERFROOT/schema_build/svn-non-primitives.jar:$XMLBEANS_HOME/build/lib/xbean.jar:$XMLBEANS_HOME/build/lib/jsr173_1.0_api.jar:$XMLBEANS_HOME/build/lib/saxon8.jar:$XMLBEANS_HOME/build/lib/saxon8-dom.jar:$XMLBEANS_HOME/build/lib/xbean_xpath.jar -DPERF_ROOT=$XMLBEANS_PERFROOT org.apache.xmlbeans.test.performance.svn.$1 $FLAVOR $FILENAME
