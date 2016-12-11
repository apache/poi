#!/bin/sh

. setJava9.sh

export FORREST_HOME=/opt/apache/forrest/apache-forrest-0.5.1-bin

# requires Ant >= 1.9.5 due to https://bz.apache.org/bugzilla/show_bug.cgi?id=58271
export ANT_HOME=/opt/apache/apache-ant/apache-ant-1.9.7
export PATH=$ANT_HOME/bin:$PATH

export ANT_OPTS="-Xmx1024m"

ant -version

# disable file-leak-detector:	-Dfile.leak.detector=-Da=b \

ant  \
    -Duser.language=en \
    -Duser.country=US \
    -Dmaxpermsize=-Dthis.is.a.dummy=true \
	-Djava.locale.providers=JRE,CLDR \
	-Dcoverage.enabled=true \
	-Dorg.apache.poi.util.POILogger=org.apache.poi.util.NullLogger \
	-Dhalt.on.test.failure=false \
	$*

exit 0

# running with: Java(TM) SE Runtime Environment (build 9-ea+112)
#
# things found:

# Assertion when doing reflection: *** java.lang.instrument ASSERTION FAILED ***: "!errorOutstanding" with message transform method call failed at JPLISAgent.c line: 884, seems to continue to run, though

# java.lang.ArrayIndexOutOfBoundsException: 344 - [java]     at sun.font.ExtendedTextSourceLabel.createCharinfo(java.desktop@9-ea/ExtendedTextSourceLabel.java:814)
# Affected tests
# * org.apache.poi.xslf.usermodel.TestPPTX2PNG
# => see https://bugs.openjdk.java.net/browse/JDK-8151725, https://bugs.openjdk.java.net/browse/JDK-8145542, https://bugs.openjdk.java.net/browse/JDK-8041480

=> Review ID: JI-9038016


# Java crashed once when running our tests, during the next run the same test did succeed, so it seems to be intermittent

    [junit] Tests run: 2, Failures: 0, Errors: 0, Time elapsed: 0.232 sec
    [junit] Running org.apache.poi.hwpf.converter.TestWordToConverterSuite
    [junit] #
    [junit] # A fatal error has been detected by the Java Runtime Environment:
    [junit] #
    [junit] #  SIGSEGV (0xb) at pc=0x00007f313e7c7441, pid=2589690, tid=2589706
    [junit] #
    [junit] # JRE version: Java(TM) SE Runtime Environment (9.0+112) (build 9-ea+112)
    [junit] # Java VM: Java HotSpot(TM) 64-Bit Server VM (9-ea+112, mixed mode, tiered, compressed oops, g1 gc, linux-amd64)
    [junit] # Problematic frame:
    [junit] # V  [libjvm.so+0x99b441]  PhaseIdealLoop::idom_no_update(Node*) const [clone .isra.105]+0x11
    [junit] #
    [junit] # Core dump will be written. Default location: Core dumps may be processed with "/usr/share/apport/apport %p %s %c %P" (or dumping to /opt/apache/poi/git-svn/core.2589690)
    [junit] #
    [junit] # An error report file with more information is saved as:
    [junit] # /opt/apache/poi/git-svn/hs_err_pid2589690.log
    [junit] #
    [junit] # Compiler replay data is saved as:
    [junit] # /opt/apache/poi/git-svn/replay_pid2589690.log
    [junit] #
    [junit] # If you would like to submit a bug report, please visit:
    [junit] #   http://bugreport.java.com/bugreport/crash.jsp
    [junit] #
    [junit] Running org.apache.poi.hwpf.usermodel.TestTableRow
    [junit] Tests run: 1, Failures: 0, Errors: 1, Time elapsed: 0 sec
    [junit] Tests FAILED (crashed)



# Solved issues:

# Some Generics related compile errors that can easily be fixed

# Option is gone: -XX:MaxPermSize=256m

# JUnit failure: expected:<28[-Apr-2010] 12:40:56> but was:<28[ Apr 2010,] 12:40:56> at org.apache.poi.hmef.TestAttachments.testAttachmentDetails(TestAttachments.java:92)
#      DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA);
#      fmt.setTimeZone(LocaleUtil.TIMEZONE_UTC);
# => expected, see https://bugs.openjdk.java.net/browse/JDK-8152154, define -Djava.locale.providers=JRE,CLDR

# ant javascript fails: <scriptdef name="propertyreset" language="javascript">, probably need to manually include the Javascript Engine
# => see https://bugs.openjdk.java.net/browse/JDK-8152533, probably fixed in b113
# => Ant 1.9.5 fixes this: https://bz.apache.org/bugzilla/show_bug.cgi?id=58271
