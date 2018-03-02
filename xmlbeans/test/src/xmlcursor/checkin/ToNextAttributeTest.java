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


package xmlcursor.checkin;

import org.apache.xmlbeans.XmlOptions;
import junit.framework.*;
import junit.framework.Assert.*;

import java.io.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;


/**
 *
 *
 */
public class ToNextAttributeTest extends BasicCursorTestCase {
    public ToNextAttributeTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ToNextAttributeTest.class);
    }

    public void testToNextAttrFromSTART() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals(false, m_xc.toNextAttribute());
    }

    public void testToNextAttrFromSingleATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals(false, m_xc.toNextAttribute());
        assertEquals("val0", m_xc.getTextValue());
    }

    public void testToNextAttrWithSiblings() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals(true, m_xc.toNextAttribute());
        assertEquals("val1", m_xc.getTextValue());
    }

    public void testToNextAttrFromLastSibling() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals(false, m_xc.toNextAttribute());
        assertEquals("val1", m_xc.getTextValue());
    }

    public void testToNextAttrWithXMLNS() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"uri\" attr1=\"val1\">text</foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val0", m_xc.getTextValue());
        assertEquals(true, m_xc.toNextAttribute());
        assertEquals("val1", m_xc.getTextValue());
        assertEquals(false, m_xc.toNextAttribute());
    }

    public void testToNextAttrFromTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(false, m_xc.toNextAttribute());
    }

}

