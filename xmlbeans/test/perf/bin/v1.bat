@echo off
REM SUPPORTED PARAMS
REM TODO: Make command line arg parsing cleaner
set MEM=64
set FLAVOR=
set FILENAME=
set V1_LIB=%XMLBEANS_PERFROOT%\3rdparty\v1\xmlbeans-1.0.3\lib

REM SCAN FIRST PARAM
if not "%2"=="" goto %2
goto default
goto end
:memory
set MEM=%3
goto end
:flavor
set FLAVOR=%3
goto end
:filename
set FILENAME=%3
goto end
:default
goto end
:end

REM SCAN SECOND PARAM
if not "%4"=="" goto %4
goto default
goto end
:memory
set MEM=%5
goto end
:flavor
set FLAVOR=%5
goto end
:filename
set FILENAME=%5
goto end
:default
goto end
:end

REM SCAN THIRD PARAM
if not "%6"=="" goto %6
goto default
goto end
:memory
set MEM=%7
goto end
:flavor
set FLAVOR=%7
goto end
:filename
set FILENAME=%7
goto end
:default
goto end
:end

echo on
java -Xmx%MEM%m -classpath %XMLBEANS_PERFROOT%\build;%XMLBEANS_PERFROOT%\schema_build\v1-purchase-order.jar;%XMLBEANS_PERFROOT%\schema_build\v1-primitives.jar;%XMLBEANS_PERFROOT%\schema_build\v1-non-primitives.jar;%V1_LIB%\xbean.jar;%XMLBEANS_HOME%\external\lib\piccolo_apache_dist_20040629_v2.jar -DPERF_ROOT=%XMLBEANS_PERFROOT% org.apache.xmlbeans.test.performance.v1.%1 %FLAVOR% %FILENAME% 
