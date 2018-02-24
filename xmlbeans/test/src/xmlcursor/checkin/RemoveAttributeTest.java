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

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;


/**
 *
 *
 */
public class RemoveAttributeTest extends BasicCursorTestCase {
    public RemoveAttributeTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(RemoveAttributeTest.class);
    }

    public void testRemoveAttributeValidAttrFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("attr1");
        assertEquals(true, m_xc.removeAttribute(name));
        assertEquals(null, m_xc.getAttributeText(name));
    }

    public void testRemoveAttributeInvalidAttrFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("invalid");
        assertEquals(false, m_xc.removeAttribute(name));
    }

    public void testRemoveAttributeNullAttrFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("dummy");
        try {
            assertEquals(false, m_xc.removeAttribute(null));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertTrue(true);
    }

    public void testRemoveAttributeFromPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        QName name = new QName("type");
        assertEquals(false, m_xc.removeAttribute(name));
    }

    public void testRemoveAttributeXMLNS() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("xmlns");
        assertEquals(false, m_xc.removeAttribute(name));
    }

    public void testRemoveAttributeFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        QName name = new QName("attr1");
        assertEquals(false, m_xc.removeAttribute(name));
    }
}

