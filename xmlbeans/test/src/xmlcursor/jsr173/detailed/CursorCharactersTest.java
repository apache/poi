/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package xmlcursor.jsr173.detailed;


import xmlcursor.jsr173.common.CharactersTest;

import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.XmlCursor;

/**
 *
 *
 */
public  class CursorCharactersTest
        extends CharactersTest {


    public  XMLStreamReader getStream(XmlCursor c)
    throws Exception{
        return c.newXMLStreamReader();
    }
}