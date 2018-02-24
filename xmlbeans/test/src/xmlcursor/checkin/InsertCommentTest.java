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
public class InsertCommentTest extends BasicCursorTestCase {
    public InsertCommentTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(InsertCommentTest.class);
    }

    public void testInsertCommentAtSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        m_xc.insertComment(" new comment ");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<foo><!-- new comment --><bar>text</bar></foo>", m_xc.xmlText());
    }

    public void testInsertCommentInMiddleOfTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.insertComment(" new comment ");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<bar>te<!-- new comment -->xt</bar>", m_xc.xmlText());
    }

    public void testInsertCommentAtEND() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.insertComment(" new comment ");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<bar>text<!-- new comment --></bar>", m_xc.xmlText());
    }

    public void testInsertCommentWithLTChar() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertComment("< new comment ");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<foo><!--< new comment -->text</foo>", m_xc.xmlText());
    }

    public void testInsertCommentWithDoubleDash() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertComment(" -- ");
        m_xc.toStartDoc();
        assertEquals("<foo><!-- -  -->text</foo>", m_xc.xmlText());
    }

    public void testInsertCommentWithDoubleDashNoWS() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertComment("--");
        m_xc.toStartDoc();
        assertEquals("<foo><!--- -->text</foo>", m_xc.xmlText());
    }

    public void testInsertCommentWithEndDash() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertComment(" -");
        m_xc.toStartDoc();
        assertEquals("<foo><!--  -->text</foo>", m_xc.xmlText());
    }


    public void testInsertCommentWithEmptyString() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertComment("");
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<foo><!---->text</foo>", m_xc.xmlText());
    }

    public void testInsertCommentWithNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertComment(null);
        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("<foo><!---->text</foo>", m_xc.xmlText());
    }

    public void testInsertCommentAtSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        try {
            m_xc.insertComment("should fail");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(true, true);
    }
}

