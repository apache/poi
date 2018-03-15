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
public class ToFirstChildElementTest extends BasicCursorTestCase {
    public ToFirstChildElementTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ToFirstChildElementTest.class);
    }

    public void testToFirstChildElemSTARTnested() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar></foo>").newCursor();
        assertEquals(true, m_xc.toFirstChild());
        assertEquals(true, m_xc.toFirstChild());
        assertEquals("text", m_xc.getTextValue());
    }

    public void testToFirstChildElemFromLastChild() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap</char></foo>").newCursor();
        assertEquals(true, m_xc.toFirstChild());
        assertEquals(true, m_xc.toFirstChild());
        assertEquals(false, m_xc.toFirstChild());
        assertEquals("text", m_xc.getTextValue());
    }

    public void testToFirstChildElemFromTEXTnested() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text<char>zap</char></bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals("early", m_xc.getChars());
        assertEquals(true, m_xc.toFirstChild());
        assertEquals("zap", m_xc.getTextValue());
    }

    public void testToFirstChildElemFromATTRnested() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\">early<bar>text<char>zap</char></bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val0", m_xc.getTextValue());
        assertEquals(true, m_xc.toFirstChild());
    }

    public void testToFirstChildElemFromSTARTnoChild() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early</foo>").newCursor();
        assertEquals(true, m_xc.toFirstChild());
        assertEquals(false, m_xc.toFirstChild());
    }

    public void testToFirstChildElemFromSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early</foo>").newCursor();
        assertEquals(true, m_xc.toFirstChild());
        assertEquals(TokenType.START, m_xc.currentTokenType());
    }
}

