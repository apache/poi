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

package xmlobject.xmlloader.detailed;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import xmlcursor.common.BasicCursorTestCase;

import javax.xml.namespace.QName;
import java.util.Vector;

public class ParseTest extends BasicCursorTestCase {
    private XmlOptions m_map = new XmlOptions();

    public ParseTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ParseTest.class);
    }

    public void testLoadStripWhitespace() throws Exception {
        m_map.put(XmlOptions.LOAD_STRIP_WHITESPACE, "");
        m_xo = XmlObject.Factory.parse("<foo>01234   <bar>text</bar>   chars \r\n</foo>  ",
                                       m_map);
        m_xc = m_xo.newCursor();
        assertEquals("<foo>01234<bar>text</bar>chars</foo>", m_xc.xmlText());
    }

    public void testLoadDiscardDocumentElement() throws Exception {
        QName name = new QName("");

        m_map.put(XmlOptions.LOAD_REPLACE_DOCUMENT_ELEMENT, name);
        System.out.println("here");

        try {
            m_xo = XmlObject.Factory.parse("<foo>01234   <bar>text</bar>   chars </foo>  ",
                                           m_map);
        } catch (IllegalArgumentException e) {
        }

    }

    public void testPrefixNotDefined() throws Exception {
        String sXml = "<Person xmlns=\"person\"><pre1:Name>steve</pre1:Name></Person>";
        try {
            XmlObject.Factory.parse(sXml);
            fail("Expected XmlException");
        } catch (org.apache.xmlbeans.XmlException e) {
        }
    }

    public void testErrorListener() throws Exception {
        Vector vErrors = new Vector();
        m_map.setErrorListener(vErrors);
        try {
            m_xo = XmlObject.Factory.parse("<foo>text<foo>", m_map);  // improper end tag
            fail("Expected parsing exception!");
        } catch (org.apache.xmlbeans.XmlException xe) {

        }
    }


}

