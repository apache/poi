#!/bin/sh

. setJava9.sh

export FORREST_HOME=/opt/apache/forrest/apache-forrest-0.5.1-bin

# requires Ant >= 1.9.5 due to https://bz.apache.org/bugzilla/show_bug.cgi?id=58271
export ANT_HOME=/opt/apache/apache-ant/apache-ant-1.9.7
export PATH=$ANT_HOME/bin:$PATH

export ANT_OPTS="-Xmx1024m"

ant -version

# disable file-leak-detector:	-Dfile.leak.detector=-Da=b \
# option to try to avoid sigsev:    -Djava9addarg=-XX:-PartialPeelLoop \

# disabled jacoco until we use 0.7.8, see https://github.com/jacoco/jacoco/pull/434

ant  \
    -Duser.language=en \
    -Duser.country=US \
    -Dmaxpermsize=-Dthis.is.a.dummy=true \
    -Djava9addmods=-addmods \
    -Djava9addmodsvalue=java.xml.bind \
	-Djava.locale.providers=JRE,CLDR \
	-Dcoverage.enabled=false \
	-Dorg.apache.poi.util.POILogger=org.apache.poi.util.NullLogger \
	-Dhalt.on.test.failure=false \
	$*

exit 0
