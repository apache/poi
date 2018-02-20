@REM
@REM  Copyright 2004 The Apache Software Foundation
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM

@rem Common script to set the XMLBEANS_LIB variable
@rem to the directory containing xbean.jar
@echo off

if "%XMLBEANS_HOME%" EQU "" (set XMLBEANS_HOME=%~dp0..)

set XMLBEANS_LIB=

if exist "%XMLBEANS_HOME%\build\lib\xbean.jar" set XMLBEANS_LIB=%XMLBEANS_HOME%\build\lib
if exist "%XMLBEANS_HOME%\lib\xbean.jar" set XMLBEANS_LIB=%XMLBEANS_HOME%\lib

if "%XMLBEANS_LIB%" EQU "" echo "ERROR: Could not find xbean.jar, try setting XMLBEANS_LIB to the directory containing xbean.jar"
