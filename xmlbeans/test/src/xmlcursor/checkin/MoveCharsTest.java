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
public class MoveCharsTest extends BasicCursorTestCase {
    public MoveCharsTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(MoveCharsTest.class);
    }

    public void testMoveCharsOverlap() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc1, TokenType.TEXT);
        xc1.toNextChar(2);
        try {
            assertEquals("234", xc1.getChars());
            assertEquals(3, m_xc.moveChars(3, xc1));
            assertEquals("34", m_xc.getChars());
            assertEquals("34", xc1.getChars());
        } finally {
            xc1.dispose();
        }
    }

    public void testMoveCharsNoOverlap() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        XmlCursor xc2 = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        xc1.toCursor(m_xc);
        xc2.toCursor(m_xc);
        xc1.toNextChar(3);
        xc2.toNextChar(4);
        try {
            assertEquals("34", xc1.getChars());
            assertEquals("4", xc2.getChars());
            assertEquals(2, m_xc.moveChars(2, xc1));
            assertEquals("20134", m_xc.getChars());
            assertEquals("34", xc1.getChars());
            assertEquals("4", xc2.getChars());
        } finally {
            xc1.dispose();
            xc2.dispose();
        }
    }

    public void testMoveCharsToNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.moveChars(4, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ise) {
        }
        assertEquals(true, true);
    }

    public void testMoveCharsSibling() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc1 = m_xc.newCursor();
        try {
            assertEquals(false, xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(4, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());
            System.out.println("we are here");
            assertEquals(TokenType.END, xc1.currentTokenType());


            try {
                xc1.getTextValue();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
            }
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    public void testMoveCharsNegative() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc1 = m_xc.newCursor();
        try {
            assertEquals(false, xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(-1, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());
            assertEquals(TokenType.END, xc1.currentTokenType());
            try {
                xc1.getTextValue();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
            }

        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    public void testMoveCharsZero() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc1 = m_xc.newCursor();
        try {
            assertEquals(false, xc0.isAtSamePositionAs(xc1));
            assertEquals(0, xc1.moveChars(0, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("0123", xc0.getTextValue());
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
            assertEquals("WXYZ", xc1.getChars());


        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    public void testMoveCharsToSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        XmlCursor xc0 = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.moveChars(4, xc0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ise) {
        } finally {
            xc0.dispose();
        }
    }

    public void testMoveCharsToPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        XmlCursor xc0 = m_xo.newCursor();
        toNextTokenOfType(xc0, TokenType.PROCINST);
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.moveChars(1, xc0);
        xc0.toPrevToken();
        try {
            assertEquals("t", xc0.getChars());
            assertEquals("ext", m_xc.getChars());
        } finally {
            xc0.dispose();
        }
    }

    public void testMoveCharsGTmax() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlCursor xc1 = m_xc.newCursor();
        try {
            assertEquals(false, xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(1000, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());

            assertEquals(TokenType.END, xc1.currentTokenType());

            try {
                xc1.getTextValue();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
            }
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    public void testMoveCharsToNewDocument() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlObject xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        XmlCursor xc1 = xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc1, TokenType.TEXT);
        assertEquals(5, m_xc.moveChars(5, xc1));
        xc1.toParent();
        // verify xc1
        assertEquals("01234text", xc1.getTextValue());
        xc1.dispose();
        // verify m_xc
        assertEquals(TokenType.END, m_xc.currentTokenType());
    }
}

