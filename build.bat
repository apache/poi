@echo off

set OLD_ANT_HOME=%ANT_HOME%
set ANT_HOME=tools\antipede

set OLD_CLASSPATH=%CLASSPATH%
for %%i in (.\lib\endorsed\*.jar) do call appendcp.bat %%i

call %ANT_HOME%\bin\ant -emacs %1 %2 %3 %4 %5 %6 %7 %8 %9

set ANT_HOME=%OLD_ANT_HOME%
set CLASSPATH=%OLD_CLASSPATH%