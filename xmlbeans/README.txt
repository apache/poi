/*  Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

Welcome to XMLBeans!

Layout of the tree:
- README.txt    this file
- build.xml     ant build file for building XmlBeans code and utilities
- testbuild.xml ant build file for building and running tests
- STATUS        essential info about the project
- xbeanenv.cmd  sets the XMLBEANS_HOME environment variable on Windows
- xbeanenv.sh   sets the XMLBEANS_HOME environment variable on Unix
- bin           contains useful command-line scripts for Win and Unix
- docs          contains several documentation files
- external      contains external jars upon which XmlBeans depends
- samples       contains ... samples
- src           the source code for XmlBeans, organized by area
- test          contains test cases and infrastructure
- xkit          contains the README file for the binary distribution

To quickly get started run:
"ant" to build XmlBeans or
"ant usage" to see the most useful build targets or
"cd bin" and then "<tool_name> -help" for a description of what the
     tool does and what parameters it accepts
 
For further information check out:
http://xmlbeans.apache.org
http://wiki.apache.org/xmlbeans
./samples/<sample_name>/README.txt  for running samples
./test/docs/*                       for running tests

XmlBeans depends on the following external libraries:
- jsr173_1.0_api.jar
- jam-0.1.0-src.jar [http://annogen.codehaus.org/JAM] for the extension
  feature using plain .java files
- tools.jar (O) from the JDK, dependency of jam-0.1.0-src
- resolver.jar (O) [http://xml.apache.org/commons/components/resolver/]
  for apache-commons catalog files that are used in entity resolving
- saxon9.0.0.4j (O) [http://sourceforge.net/projects/saxon/]
  for XPath/XQuery in XmlBeans
(O) means that the library is optional

For licensing information, see ./src/license/LICENSE.txt
Updated: 11/25/09.
