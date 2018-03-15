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

java -Xmx"$MEM"m -classpath $XMLBEANS_PERFROOT/build:$XMLBEANS_PERFROOT/schema_build/jibx-purchase-order.jar:$XMLBEANS_PERFROOT/3rdparty/jibx/jibx//lib/jibx-run.jar:$XMLBEANS_PERFROOT/3rdparty/jibx/jibx/lib/xpp3.jar -DPERF_ROOT=$XMLBEANS_PERFROOT org.apache.xmlbeans.test.performance.jibx.$1 $FLAVOR $FILENAME 
