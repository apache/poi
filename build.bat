@echo off
rem ----------------------------------------------------------------------------
rem build.bat - Win32 Build Script for Apache Cocoon
rem
rem $Id$
rem ----------------------------------------------------------------------------

rem ----- Copy Xalan and Xerces for the build system    ------------------------
copy lib\optional\xerces*.jar  tools\lib
copy lib\optional\xalan*.jar   tools\lib
copy lib\optional\xml-api*.jar tools\lib
copy lib\optional\jtidy*.jar tools\lib

rem ----- Verify and Set Required Environment Variables ------------------------

REM Commented out so it always uses poi's ant -- gjs -- if not "%ANT_HOME%" == "" goto gotAntHome
set OLD_ANT_HOME=%ANT_HOME%
set ANT_HOME=tools
:gotAntHome

call %ANT_HOME%\bin\ant %1 %2 %3 %4 %5 %6 %7 %8 %9
set ANT_HOME=%OLD_ANT_HOME%

