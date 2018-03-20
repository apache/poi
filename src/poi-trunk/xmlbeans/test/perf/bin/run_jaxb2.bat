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
call jaxb2.bat POReadAllJaxb2 filename 1 
call jaxb2.bat POReadAllJaxb2 filename 2 
call jaxb2.bat POReadAllJaxb2 filename 3 
call jaxb2.bat POReadAllJaxb2 filename 4 
call jaxb2.bat POReadAllJaxb2 filename 5 
call jaxb2.bat POReadAllJaxb2 filename 6 
call jaxb2.bat POReadAllJaxb2 filename 7 
call jaxb2.bat POReadOneJaxb2 filename 1
call jaxb2.bat POReadOneJaxb2 filename 2
call jaxb2.bat POReadOneJaxb2 filename 3
call jaxb2.bat POReadOneJaxb2 filename 4
call jaxb2.bat POReadOneJaxb2 filename 5
call jaxb2.bat POReadOneJaxb2 filename 6
call jaxb2.bat POReadOneJaxb2 filename 7
call jaxb2.bat POGetCustNameJaxb2 filename 1
call jaxb2.bat POGetCustNameJaxb2 filename 2
call jaxb2.bat POGetCustNameJaxb2 filename 3
call jaxb2.bat POGetCustNameJaxb2 filename 4
call jaxb2.bat POGetCustNameJaxb2 filename 5
call jaxb2.bat POGetCustNameJaxb2 filename 6
call jaxb2.bat POGetCustNameJaxb2 filename 7
call jaxb2.bat POGetSetGetCustNameJaxb2 filename 1
call jaxb2.bat POGetSetGetCustNameJaxb2 filename 2
call jaxb2.bat POGetSetGetCustNameJaxb2 filename 3
call jaxb2.bat POGetSetGetCustNameJaxb2 filename 4
call jaxb2.bat POGetSetGetCustNameJaxb2 filename 5
call jaxb2.bat POGetSetGetCustNameJaxb2 filename 6
call jaxb2.bat POGetSetGetCustNameJaxb2 filename 7
call jaxb2.bat POTopDownJaxb2
call jaxb2.bat POTopDownSaveJaxb2
call jaxb2.bat PrimTopDownJaxb2
call jaxb2.bat NPrimTopDownJaxb2
