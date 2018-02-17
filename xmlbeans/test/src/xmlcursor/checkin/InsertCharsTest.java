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
public class InsertCharsTest extends BasicCursorTestCase {
    public InsertCharsTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(InsertCharsTest.class);
    }

    public void testInsertCharsAtSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        m_xc.insertChars(" new chars ");
        m_xc.toPrevToken();
        System.out.println(m_xc.currentTokenType());
        assertEquals(" new chars ", m_xc.getChars());
    }

    public void testInsertCharsAtSTARTnonEmptyPriorTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_WS_ONLY);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        m_xc.insertChars("new chars ");
        m_xc.toPrevToken();
        System.out.println(m_xc.currentTokenType());
        assertEquals(" new chars ", m_xc.getChars());
    }

    public void testInsertCharsAtENDnonEmptyPriorTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_WS_ONLY);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.insertChars("new chars ");
        m_xc.toPrevToken();
        assertEquals(" new chars ", m_xc.getChars());
    }

    public void testInsertCharsInMiddleOfTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.insertChars("new chars ");
        assertEquals("xt", m_xc.getChars());
        m_xc.toPrevToken();
        assertEquals("tenew chars xt", m_xc.getTextValue());
    }

    public void testInsertCharsNullInMiddleOfTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.insertChars(null);
        assertEquals("xt", m_xc.getChars());
        m_xc.toPrevToken();
        assertEquals("text", m_xc.getTextValue());
    }

    public void testInsertCharsEmptyInMiddleOfTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.insertChars("");
        assertEquals("xt", m_xc.getChars());
        m_xc.toPrevToken();
        assertEquals("text", m_xc.getTextValue());
    }

    public void testInsertCharsInNAMESPACE() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_NS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        try {
            m_xc.insertChars("fred");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(true, true);
    }

}

