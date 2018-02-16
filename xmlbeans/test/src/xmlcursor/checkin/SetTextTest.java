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
public class SetTextTest extends BasicCursorTestCase {
    public SetTextTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(SetTextTest.class);
    }

    public void testSetTextFromCOMMENT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_COMMENT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.COMMENT);
        m_xc.setTextValue("fred");
        assertEquals("fred", m_xc.getTextValue());
    }

    public void testSetTextFromPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        m_xc.setTextValue("new procinst text");
        assertEquals("new procinst text", m_xc.getTextValue());
    }

    public void testSetTextFromPROCINSTInputNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        m_xc.setTextValue(null);
        assertEquals("", m_xc.getTextValue());
    }

    public void testSetTextFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_COMMENT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        try {
            m_xc.setTextValue("fred");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    public void testSetTextFromENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_NS);
        m_xc = m_xo.newCursor();
        m_xc.toEndDoc();
        try {
            m_xc.setTextValue("fred");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    public void testSetTextFromTEXTbegin() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals("01234", m_xc.getChars());
        try {
            m_xc.setTextValue("new text");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(true, true);
    }

    public void testSetTextFromTEXTmiddle() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("234", m_xc.getChars());
        try {
            m_xc.setTextValue("new text");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(true, true);
    }

    public void testSetTextFromSTARTnotNested() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals("01234", m_xc.getTextValue());
        m_xc.setTextValue("new text");
        assertEquals("new text", m_xc.getTextValue());
    }

    public void testSetTextFromSTARTnotNestedInputNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals("01234", m_xc.getTextValue());
        m_xc.setTextValue(null);
        assertEquals("", m_xc.getTextValue());
    }

    public void testSetTextFromSTARTnested() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals("text0nested0text1nested1", m_xc.getTextValue());
        m_xc.setTextValue("new text");
        assertEquals("<foo attr0=\"val0\">new text</foo>", m_xc.xmlText());
    }

    public void testSetTextFromSTARTnestedInputNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals("text0nested0text1nested1", m_xc.getTextValue());
        m_xc.setTextValue(null);
        assertEquals("<foo attr0=\"val0\"/>", m_xc.xmlText());
    }

    public void testSetTextFromATTRnested() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val0", m_xc.getTextValue());
        m_xc.setTextValue("new text");
        assertEquals("new text", m_xc.getTextValue());
    }

    public void testSetTextFromSTARTDOCnested() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        m_xc = m_xo.newCursor();
        assertEquals("text0nested0text1nested1", m_xc.getTextValue());
        m_xc.setTextValue("new text");
        assertEquals(Common.wrapInXmlFrag("new text"), m_xc.xmlText());
    }


}

