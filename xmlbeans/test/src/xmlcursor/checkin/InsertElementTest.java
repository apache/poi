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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 *
 */
public class InsertElementTest extends BasicCursorTestCase {
    public InsertElementTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(InsertElementTest.class);
    }

    public void testInsertElementNullName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            m_xc.insertElementWithText(null, "uri", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertElementEmptyStringName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            m_xc.insertElementWithText("", "uri", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
        assertEquals(true, true);
    }

    public void testInsertElementNullUri() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.insertElementWithText("name", null, "value");
        m_xc.toPrevSibling();
        assertEquals("<name>value</name>", m_xc.xmlText());
    }

    public void testInsertElementNullText() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.insertElementWithText("name", "uri", null);
        m_xc.toPrevSibling();
        assertEquals("<uri:name xmlns:uri=\"uri\"/>", m_xc.xmlText());
    }

    public void testInsertElementEmptyStringText() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.insertElementWithText("name", null, "");
        m_xc.toPrevSibling();
        assertEquals("<name/>", m_xc.xmlText());
    }

    public void testInsertElementInMiddleOfTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.insertElementWithText("name", null, "value");
        m_xc.toStartDoc();
        assertEquals("<foo>te<name>value</name>xt</foo>", m_xc.xmlText());
    }

    public void testInsertElementAtEND() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.insertElementWithText("name", null, "value");
        m_xc.toStartDoc();
        assertEquals("<foo>text<name>value</name></foo>", m_xc.xmlText());
    }

    public void testInsertElementAtSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        try {
            m_xc.insertElementWithText("name", null, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(true, true);
    }

    public void testInsertElementAtENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ENDDOC);
        m_xc.insertElementWithText("name", null, "value");
        m_xc.toStartDoc();
        assertEquals(Common.wrapInXmlFrag("<foo>text</foo><name>value</name>"), m_xc.xmlText());
    }

    public void testInsertElementInStoreWithNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                        ".//FleetID");
        m_xc.toNextSelection();
        m_xc.insertElementWithText("name", "uri", "value");
        m_xc.toPrevSibling();
        assertEquals("<uri:name xmlns=\"" +
                     Common.CLM_NS + "\" " +
                     Common.CLM_XSI_NS + " " +
                     "xmlns:uri=\"uri\">value</uri:name>", m_xc.xmlText());
    }
}

