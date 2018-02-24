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
public class InsertAttributeTest extends BasicCursorTestCase {
    public InsertAttributeTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(InsertAttributeTest.class);
    }

    public void testInsertAttributeAtSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertAttributeWithValue("name", "uri", "value");
        m_xc.toStartDoc();
        assertEquals("<foo uri:name=\"value\" xmlns:uri=\"uri\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeAtATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        m_xc.insertAttributeWithValue("name", null, "value");
        m_xc.toStartDoc();
        assertEquals("<foo name=\"value\" attr0=\"val0\" attr1=\"val1\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeAt2ndATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        toNextTokenOfType(m_xc, TokenType.ATTR);
        m_xc.insertAttributeWithValue("name", null, "value");
        m_xc.toStartDoc();
        assertEquals("<foo attr0=\"val0\" name=\"value\" attr1=\"val1\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeAtPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        m_xc.toNextToken();
        try {
            m_xc.insertAttributeWithValue("name", null, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeAtSTARTwithEmptyStringName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            m_xc.insertAttributeWithValue("", "uri", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeAtSTARTwithNullName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            m_xc.insertAttributeWithValue(null, "uri", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeWithNullQName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            QName name = new QName(null);
            m_xc.insertAttribute(name);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeAtSTARTwithEmptyStringUri() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertAttributeWithValue("name", "", "value");
        m_xc.toStartDoc();
        assertEquals("<foo name=\"value\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeAtSTARTwithNameXml() throws Exception {
        /*
m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
m_xc = m_xo.newCursor();
toNextTokenOfType(m_xc, TokenType.TEXT);
try
{
m_xc.insertAttributeWithValue("xml", null, "value");
fail("Expected IllegalArgumentException");
}
catch (IllegalArgumentException iae)
{
}
assertEquals(true,true);
        */

        try {
            m_xo = XmlObject.Factory.parse("<foo>text</foo>");
            m_xc = m_xo.newCursor();
            m_xc.insertAttributeWithValue("xml", null, "value");
            fail("Expected Exception");
        } catch (Exception e) {
        }

        assertTrue(true);
    }

    public void testInsertAttributeAtSTARTwithValueXml() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertAttributeWithValue("name", null, "xml");
        m_xc.toStartDoc();
        assertEquals("<foo name=\"xml\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeAtSTARTwithLTcharInName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.insertAttributeWithValue("<b", null, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeAtSTARTwithLTcharInValue() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertAttributeWithValue("name", null, "<value");
        m_xc.toStartDoc();
        assertEquals("<foo name=\"&lt;value\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeAtSTARTwithAmpCharInValue() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertAttributeWithValue("name", null, "&value");
        m_xc.toStartDoc();
        assertEquals("<foo name=\"&amp;value\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeAtSTARTwithAmpCharInName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.insertAttributeWithValue("&bar", null, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    // tests below use the XMLName form of the parameter signature

    public void testInsertAttributeType2AtATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        QName name = new QName("name");
        m_xc.insertAttributeWithValue(name, "value");
        m_xc.toStartDoc();
        assertEquals("<foo name=\"value\" attr0=\"val0\" attr1=\"val1\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeType2AfterSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        QName name = new QName("name");
        m_xc.insertAttributeWithValue(name, null);
        m_xc.toStartDoc();
        assertEquals("<foo attr0=\"val0\" attr1=\"val1\" name=\"\">text</foo>", m_xc.xmlText());
    }

    public void testInsertAttributeType2WithXMLinName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        QName name = new QName("<xml>");
        try {
            m_xc.insertAttributeWithValue(name, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeType2WithLeadingSpaceinName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        QName name = new QName(" any");
        try {
            m_xc.insertAttributeWithValue(name, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeType2ContainingSpaceinName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        QName name = new QName("any any");
        try {
            m_xc.insertAttributeWithValue(name, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeType2WithTrailingSpaceinName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        QName name = new QName("any ");
        try {
            m_xc.insertAttributeWithValue(name, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertAttributeType2WithXMLinNameCase() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        QName name = new QName("<xMlzorro>");
        try {
            m_xc.insertAttributeWithValue(name, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }
}

