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

ant  \
    -Duser.language=en \
    -Duser.country=US \
    -Dmaxpermsize=-Dthis.is.a.dummy=true \
    -Djava9addmods=-addmods \
    -Djava9addmodsvalue=java.xml.bind \
	-Djava.locale.providers=JRE,CLDR \
	-Dcoverage.enabled=true \
	-Dorg.apache.poi.util.POILogger=org.apache.poi.util.NullLogger \
	-Dhalt.on.test.failure=false \
	$*

exit 0

# running with: Java(TM) SE Runtime Environment (build 9-ea+112)
#
#

Link to bugs relevant to us: 
https://bugs.openjdk.java.net/browse/JDK-8152533?jql=labels%20%3D%20apache-poi-interest
https://bugs.openjdk.java.net/secure/IssueNavigator.jspa?reset=true&jqlQuery=labels+%3D+apache-poi-found
https://bugs.openjdk.java.net/browse/JDK-8157678?jql=labels%20in%20%28apache-poi-interest%2C%20apache-poi-found%29


#
# things found:

# Assertion when doing reflection: *** java.lang.instrument ASSERTION FAILED ***: "!errorOutstanding" with message transform method call failed at JPLISAgent.c line: 884, seems to continue to run, though

# java.lang.ArrayIndexOutOfBoundsException: 344 - [java]     at sun.font.ExtendedTextSourceLabel.createCharinfo(java.desktop@9-ea/ExtendedTextSourceLabel.java:814)
# Affected tests
# * org.apache.poi.xslf.usermodel.TestPPTX2PNG
# => see https://bugs.openjdk.java.net/browse/JDK-8151725, https://bugs.openjdk.java.net/browse/JDK-8145542, https://bugs.openjdk.java.net/browse/JDK-8041480
Main issue now: https://bugs.openjdk.java.net/browse/JDK-8041480

=> Review ID: JI-9038016


# Java crashed twice when running our tests, during the next run the same test did succeed, so it seems to be intermittent

    [junit] Reading document/53379.doc with class org.apache.poi.stress.HWPFFileHandler
    [junit] #
    [junit] # A fatal error has been detected by the Java Runtime Environment:
    [junit] #
    [junit] #  SIGSEGV (0xb) at pc=0x00007fa7e71ba451, pid=3467195, tid=3467211
    [junit] #
    [junit] # JRE version: Java(TM) SE Runtime Environment (9.0+119) (build 9-ea+119)
    [junit] # Java VM: Java HotSpot(TM) 64-Bit Server VM (9-ea+119, mixed mode, tiered, compressed oops, g1 gc, linux-amd64)
    [junit] # Problematic frame:
    [junit] # V  [libjvm.so+0x9a5451]  PhaseIdealLoop::idom_no_update(Node*) const [clone .isra.113]+0x11
    [junit] #
    [junit] # Core dump will be written. Default location: Core dumps may be processed with "/usr/share/apport/apport %p %s %c %P" (or dumping to /opt/apache/poi/git-svn/core.3467195)
    [junit] #
    [junit] # An error report file with more information is saved as:
    [junit] # /opt/apache/poi/git-svn/hs_err_pid3467195.log
    [junit] #
    [junit] # Compiler replay data is saved as:
    [junit] # /opt/apache/poi/git-svn/replay_pid3467195.log
    [junit] #
    [junit] # If you would like to submit a bug report, please visit:
    [junit] #   http://bugreport.java.com/bugreport/crash.jsp
    [junit] #
    [junit] Running org.apache.poi.TestAllFiles
    [junit] Tests run: 1, Failures: 0, Errors: 1, Time elapsed: 0 sec
    [junit] Test org.apache.poi.TestAllFiles FAILED (crashed)

Also happened at 
    [junit] Reading document/Bug47731.doc with class org.apache.poi.stress.HWPFFileHandler


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


# JaCoCo is failing, fixed with newer JaCoCo:
test-main:
[jacoco:coverage] Enhancing junit with coverage
    [junit] File leak detector installed
    [junit] Serving file leak stats on http://localhost:19998/ for stats
    [junit] FATAL ERROR in native method: processing of -javaagent failed
    [junit] Exception in thread "main" java.lang.reflect.InvocationTargetException
    [junit]     at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(java.base@9-ea/Native Method)
    [junit]     at jdk.internal.reflect.NativeMethodAccessorImpl.invoke(java.base@9-ea/NativeMethodAccessorImpl.java:62)
    [junit]     at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(java.base@9-ea/DelegatingMethodAccessorImpl.java:43)
    [junit]     at java.lang.reflect.Method.invoke(java.base@9-ea/Method.java:531)
    [junit]     at sun.instrument.InstrumentationImpl.loadClassAndStartAgent(java.instrument@9-ea/InstrumentationImpl.java:396)
    [junit]     at sun.instrument.InstrumentationImpl.loadClassAndCallPremain(java.instrument@9-ea/InstrumentationImpl.java:408)
    [junit] Caused by: java.lang.RuntimeException: Class java/util/UUID could not be instrumented.
    [junit]     at org.jacoco.agent.rt.internal_14f7ee5.core.runtime.ModifiedSystemClassRuntime.createFor(ModifiedSystemClassRuntime.java:139)
    [junit]     at org.jacoco.agent.rt.internal_14f7ee5.core.runtime.ModifiedSystemClassRuntime.createFor(ModifiedSystemClassRuntime.java:100)
    [junit]     at org.jacoco.agent.rt.internal_14f7ee5.PreMain.createRuntime(PreMain.java:55)
    [junit]     at org.jacoco.agent.rt.internal_14f7ee5.PreMain.premain(PreMain.java:47)
    [junit]     ... 6 more
    [junit] Caused by: java.lang.NoSuchFieldException: $jacocoAccess
    [junit]     at java.lang.Class.getField(java.base@9-ea/Class.java:1881)
    [junit]     at org.jacoco.agent.rt.internal_14f7ee5.core.runtime.ModifiedSystemClassRuntime.createFor(ModifiedSystemClassRuntime.java:137)
    [junit]     ... 9 more
