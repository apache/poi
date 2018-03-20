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


package xmlcursor.detailed;

import org.apache.xmlbeans.XmlOptions;
import junit.framework.*;
import junit.framework.Assert.*;

import java.io.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor.TokenType;

import xmlcursor.common.*;

import java.net.URL;

/**
 *
 * 
 */
public class CopyCharsTest extends BasicCursorTestCase {
    public CopyCharsTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(CopyCharsTest.class);
    }

    public void testCopyCharsToNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.copyChars(4, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ise) {
        }
        assertEquals(true, true);
    }

    public void testCopyCharsNegative() throws Exception {
        m_xo = XmlObject.Factory
                .parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc1 = m_xc.newCursor();
	try {
            assertEquals(false, xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.copyChars(-1, xc0));
	    assertEquals(TokenType.TEXT, xc0.currentTokenType());
	    assertEquals("0123", xc0.getTextValue());

            xc0.toPrevToken();
	    assertEquals(TokenType.START,xc0.prevTokenType());
            assertEquals("WXYZ0123", xc0.getTextValue());
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
	    assertEquals(TokenType.START,xc1.prevTokenType());
	    assertEquals("WXYZ", xc1.getTextValue());
	} finally {
            xc0.dispose();
            xc1.dispose();
	}
    }

    public void testCopyCharsZero() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc1 = m_xc.newCursor();
        try {
            assertEquals(false, xc0.isAtSamePositionAs(xc1));
            assertEquals(0, xc1.copyChars(0, xc0));
            assertEquals("0123", xc0.getTextValue());
            xc0.toPrevToken();
            assertEquals("0123", xc0.getTextValue());
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
            assertEquals("WXYZ", xc1.getTextValue());
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }


    public void testCopyCharsThis() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc0 = m_xc.newCursor();
        XmlCursor xc1 = m_xc.newCursor();
        try {
            assertEquals(true, xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.copyChars(4, xc0));
            assertEquals("0123", xc0.getTextValue());
            xc0.toPrevToken();
            assertEquals("01230123", xc0.getTextValue());
            assertEquals("0123", xc1.getTextValue());
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    public void testCopyCharsGTmax() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc1 = m_xc.newCursor();
        try {
            assertEquals(false, xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.copyChars(1000, xc0));
            // verify xc0
            assertEquals("0123", xc0.getTextValue());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());
            // verify xc1
            assertEquals("WXYZ", xc1.getTextValue());
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    public void testCopyCharsToDifferentDocument() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlObject xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        XmlCursor xc1 = xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc1, TokenType.TEXT);
        assertEquals(5, m_xc.copyChars(5, xc1));
        assertEquals(5,xc1.toPrevChar(5));
        // verify xc1
        assertEquals("01234text", xc1.getTextValue());
        xc1.dispose();
        // verify m_xc
        assertEquals("01234", m_xc.getTextValue());
    }

    public void testCopyCharsToEmptyDocumentSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlObject xo = XmlObject.Factory.newInstance();
        XmlCursor xc1 = xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            assertEquals(5, m_xc.copyChars(5, xc1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ise) {
        }
        assertEquals(true, true);
    }
}

