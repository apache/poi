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


import java.net.URL;

import org.apache.xmlbeans.XmlOptions;
import xmlcursor.common.*;

import java.util.HashMap;


/**
 *
 *
 */
public class InsertProcInstTest extends BasicCursorTestCase {
    public InsertProcInstTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(InsertProcInstTest.class);
    }

    public void testInsertProcInstWithNullTarget() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.insertProcInst(null, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertProcInstWithEmptyStringTarget() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.insertProcInst("", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertProcInstWithLTcharInTarget() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        try {
            m_xc.insertProcInst("<target", " value ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertTrue(true);
    }

    public void testInsertProcInstWithNullText() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.insertProcInst("target", null);
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<bar>te<?target?>xt</bar>", m_xc.xmlText());
    }

    public void testInsertProcInstWithEmptyStringText() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.insertProcInst("target", "");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<bar>te<?target?>xt</bar>", m_xc.xmlText());
    }

    public void testInsertProcInstWithLTcharInText() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        m_xc.insertProcInst("target", "< value ");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<foo><?target < value ?><bar>text</bar></foo>", m_xc.xmlText());
    }

    public void testInsertProcInstInMiddleOfTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.insertProcInst("target", " value ");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<bar>te<?target  value ?>xt</bar>", m_xc.xmlText());
    }

    public void testInsertProcInstAfterSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        m_xc.insertProcInst("target", " value ");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<foo><?target  value ?><bar>text</bar></foo>", m_xc.xmlText());
    }

    public void testInsertProcInstAtEND() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_NS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.insertProcInst("target", " value ");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<foo xmlns=\"http://www.foo.org\"><?target  value ?></foo>", m_xc.xmlText());
    }

    public void testInsertProcInstBeforeATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        try {
            m_xc.insertProcInst("target", " value ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ise) {
        }
        assertEquals(true, true);
    }
}

