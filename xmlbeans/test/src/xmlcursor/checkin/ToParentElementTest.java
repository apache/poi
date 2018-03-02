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
public class ToParentElementTest extends BasicCursorTestCase {
    public ToParentElementTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ToParentElementTest.class);
    }

    public void testToParentElementFromSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar></foo>").newCursor();
        assertEquals(false, m_xc.toParent());
        assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
    }

    public void testToParentElementFromFirstChildOfSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar></foo>").newCursor();
        m_xc.toFirstChild();
        assertEquals(true, m_xc.toParent());
        assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
    }

    public void testToParentElementFromPrevTokenOfENDDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>text</foo>").newCursor();
        m_xc.toEndDoc();
        m_xc.toPrevToken();
        assertEquals(TokenType.END, m_xc.currentTokenType());

        assertEquals(true, m_xc.toParent());
        assertEquals(TokenType.START, m_xc.currentTokenType());
    }

    public void testToParentElementNested() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>").newCursor();
        m_xc.selectPath("$this//ear");
        m_xc.toNextSelection();
        assertEquals("yap", m_xc.getTextValue());
        assertEquals(true, m_xc.toParent());
        assertEquals("zapwapyap", m_xc.getTextValue());
    }

    public void testToParentElementFromATTR() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear attr0=\"val0\">yap</ear></char></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val0", m_xc.getTextValue());
        assertEquals(true, m_xc.toParent());
        assertEquals("yap", m_xc.getTextValue());
        assertEquals(true, m_xc.toParent());
        assertEquals("zapwapyap", m_xc.getTextValue());
    }

    public void testToParentElementFromTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>").newCursor();
        m_xc.selectPath("$this//ear");
        m_xc.toNextSelection();
        m_xc.toNextToken();
        assertEquals(TokenType.TEXT, m_xc.currentTokenType());
        assertEquals("yap", m_xc.getChars());
        assertEquals(true, m_xc.toParent());
        assertEquals("yap", m_xc.getTextValue());
        assertEquals(true, m_xc.toParent());
        assertEquals("zapwapyap", m_xc.getTextValue());
    }
}


