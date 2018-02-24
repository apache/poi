@echo off
REM   -------------------------------------------------------------------------
REM    Copyright 2004 The Apache Software Foundation
REM
REM    Licensed under the Apache License, Version 2.0 (the "License");
REM    you may not use this file except in compliance with the License.
REM    You may obtain a copy of the License at
REM
REM        http://www.apache.org/licenses/LICENSE-2.0
REM
REM    Unless required by applicable law or agreed to in writing, software
REM    distributed under the License is distributed on an "AS IS" BASIS,
REM    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM    See the License for the specific language governing permissions and
REM    limitations under the License.
REM   -------------------------------------------------------------------------

java -Xmx%MEM%m -classpath %XMLBEANS_PERFROOT%\build;%XMLBEANS_PERFROOT%\schema_build\parsers-jaxb2-purchase-order.jar;%XMLBEANS_PEFROOT%\schema_build\parsers-svn-doc.jar;%XMLBEANS_PERFROOT%\schema_build\parsers-svn-purchase-order.jar;%XMLBEANS_HOME%\build\lib\xbean.jar;%XMLBEANS_HOME%\external\lib\piccolo_apache_dist_20040629_v2.jar;%JAXB2_LIBDIR%\lib\jaxb-api.jar;%JAXB2_LIBDIR%\lib\jaxb-impl.jar;%JAXB2_LIBDIR%\lib\jsr173_1.0_api.jar;%JAXB2_LIBDIR%\lib\activation.jar;%XMLBEANS_PERFROOT%\3rdparty\xerces\xerces-2_7_1\xercesImpl.jar -DPERF_ROOT=%XMLBEANS_PERFROOT% org.apache.xmlbeans.test.performance.parsers.XMLParsers 



