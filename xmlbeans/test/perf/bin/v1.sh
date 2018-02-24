V1_LIB=$XMLBEANS_PERFROOT/3rdparty/v1/xmlbeans-1.0.3/lib
export V1_LIB

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

java -Xmx"$MEM"m -classpath $XMLBEANS_PERFROOT/build:$XMLBEANS_PERFROOT/schema_build/v1-purchase-order.jar:$XMLBEANS_PERFROOT/schema_build/v1-primitives.jar:$XMLBEANS_PERFROOT/schema_build/v1-non-primitives.jar:$V1_LIB/xbean.jar:$XMLBEANS_HOME/external/lib/piccolo_apache_dist_20040629_v2.jar -DPERF_ROOT=$XMLBEANS_PERFROOT org.apache.xmlbeans.test.performance.v1.$1 $FLAVOR $FILENAME 
