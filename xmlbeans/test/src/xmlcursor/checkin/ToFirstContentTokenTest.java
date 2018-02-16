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
public class ToFirstContentTokenTest extends BasicCursorTestCase {
    public ToFirstContentTokenTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ToFirstContentTokenTest.class);
    }

    public void testToFirstContentTokenFromSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar></foo>").newCursor();
        m_xc.toFirstChild();
        m_xc.insertAttributeWithValue("attr0", "val0");
        m_xc.insertNamespace("nsx", "valx");
        m_xc.toStartDoc();
        assertEquals(TokenType.START, m_xc.toFirstContentToken());
        assertEquals("earlytext", m_xc.getTextValue());
    }

    public void testToFirstContentTokenFromATTR() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"nsuri\">early<bar>text</bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
        assertEquals(TokenType.ATTR, m_xc.currentTokenType());
        assertEquals("val0", m_xc.getTextValue());
    }

    public void testToFirstContentTokenFromNAMESPACE() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"nsuri\">early<bar>text</bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
        assertEquals(TokenType.NAMESPACE, m_xc.currentTokenType());

        assertEquals(m_xc.getTextValue(),"nsuri");
    }

    public void testToFirstContentTokenFromSTARTwithContent() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"nsuri\">early<bar>text</bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals(TokenType.TEXT, m_xc.toFirstContentToken());
        assertEquals("early", m_xc.getChars());
    }

    public void testToFirstContentTokenFromSTARTnoContent() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"nsuri\"></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals(TokenType.END, m_xc.toFirstContentToken());
        m_xc.toNextToken();
        assertEquals(TokenType.ENDDOC, m_xc.currentTokenType());
    }

    public void testToFirstContentTokenEmptyDocument() throws Exception {
        m_xc = XmlObject.Factory.newInstance().newCursor();
        assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
        assertEquals(TokenType.ENDDOC, m_xc.toFirstContentToken());
    }

    public void testToFirstContentTokenFromTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"nsuri\"><bar>text</bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
        assertEquals("text", m_xc.getChars());
    }

    public void testToFirstContentTokenFromTEXTmiddle() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"nsuri\"><bar>text</bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
        assertEquals("xt", m_xc.getChars());
    }

}

