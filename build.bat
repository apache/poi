@echo off

set OLD_ANT_HOME=%ANT_HOME%
set ANT_HOME=tools\ant

set OLD_CLASSPATH=%CLASSPATH%
set CLASSPATH=tools\centipede\lib\xml-apis.jar;tools\centipede\lib\xerces.jar;tools\centipede\lib\xalan.jar;tools\centipede\lib\junit.jar;tools\centipede\lib\jIzPress.jar;tools\centipede\lib\jtidy.jar

call %ANT_HOME%\bin\ant -listener org.apache.tools.ant.XmlLogger %1 %2 %3 %4 %5 %6 %7 %8 %9

set ANT_HOME=%OLD_ANT_HOME%
set CLASSPATH=%OLD_CLASSPATH%

