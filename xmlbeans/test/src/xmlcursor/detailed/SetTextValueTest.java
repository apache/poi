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

import junit.framework.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

/**
 *
 *
 */
public class SetTextValueTest extends BasicCursorTestCase {

    String sDoc = Common.XML_FOO_NS_PREFIX;

    public SetTextValueTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(SetTextValueTest.class);
    }

    /**
     * Depth first concatenation of all text leaves
     */
    public void testSTARTDOC() {
        String sExpected = Common.XMLFRAG_BEGINTAG + "&lt;newdoc/>" +
                Common.XMLFRAG_ENDTAG;
        char[] buffer = new String("<newdoc/>").toCharArray();
        m_xc.setTextValue(buffer, 0, buffer.length);
        //toPrevTokenOfType(m_xc,TokenType.STARTDOC);
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testSTART() {
        String sNewVal = "new test value ";
        String sExpected = "<foo xmlns:edi=\"http://ecommerce.org/schema\">" +
                sNewVal +
                "</foo>";
        toNextTokenOfType(m_xc, TokenType.START);
        char[] buffer = sNewVal.toCharArray();
        m_xc.setTextValue(buffer, 0, buffer.length);
        toPrevTokenOfType(m_xc, TokenType.STARTDOC);
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testAttr() {
        String sNewVal = "US\u0024 ";
        String sExpected = "<foo xmlns:edi=\"http://ecommerce.org/schema\"><!-- the 'price' element's namespace is http://ecommerce.org/schema -->  <edi:price units=\"" +
                sNewVal +
                "\">32.18</edi:price></foo>";
        toNextTokenOfType(m_xc, TokenType.ATTR);
        char[] buffer = sNewVal.toCharArray();
        m_xc.setTextValue(buffer, 0, buffer.length);
        toPrevTokenOfType(m_xc, TokenType.STARTDOC);
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testComment() {
        String sNewVal = "My new comment ";
        String sExpected = "<foo xmlns:edi=\"http://ecommerce.org/schema\"><!--" +
                sNewVal +
                "-->  <edi:price units=\"Euro\">32.18</edi:price></foo>";
        toNextTokenOfType(m_xc, TokenType.COMMENT);
        char[] buffer = sNewVal.toCharArray();
        m_xc.setTextValue(buffer, 0, buffer.length);
        toPrevTokenOfType(m_xc, TokenType.STARTDOC);
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testPI() throws Exception {
        String sTestXml = "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><foo at0=\"value0\">text</foo>";
        m_xc = XmlObject.Factory.parse(sTestXml).newCursor();
        String sNewVal = "type=\"text/html\" xmlns=\"http://newUri.org\" ";
        String sExpected = "<?xml-stylesheet " + sNewVal +
                "?><foo at0=\"value0\">text</foo>";
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        char[] buffer = sNewVal.toCharArray();
        m_xc.setTextValue(buffer, 0, buffer.length);
        toPrevTokenOfType(m_xc, TokenType.STARTDOC);
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testSetNull() {
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            m_xc.setTextValue(null, 0, 10);
            fail("Buffer was Null");
        }
        catch (IllegalArgumentException ie) {
        }
    }

    public void testNegativeOffset() {
        char[] buffer = new char[100];
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            m_xc.setTextValue(buffer, -1, 98);
            fail("Offset < 0");
        }
        catch (IndexOutOfBoundsException ie) {
        }
    }


    public void testNonZeroOffset() {
        char[] buffer = "Test".toCharArray();
        toNextTokenOfType(m_xc, TokenType.START);
        String sExpected = "st";
        m_xc.setTextValue(buffer, 2, buffer.length - 2);
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(sExpected, m_xc.getChars());
    }


    public void testLargeOffset() {
        String sNewVal = " 20";
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            m_xc.setTextValue(sNewVal.toCharArray(), 5, 3);
            fail("Offset Past end");
        }
        catch (IndexOutOfBoundsException ie) {
        }
    }

    //charCount<=0: should be a noop
    public void testNegativeCharCount() {
        char[] buffer = new char[100];
        toNextTokenOfType(m_xc, TokenType.START);
        String sExpected = m_xc.xmlText();
        try {
            m_xc.setTextValue(buffer, 10, -1);
            if (!m_xc.equals(sExpected)) fail("Negative Char Cnt");
        }
        catch (IndexOutOfBoundsException ie) {
        }
    }

    public void testZeroCharCount() {
        char[] buffer = new char[100];
        String sExpected = "<foo xmlns:edi=\"http://ecommerce.org/schema\"/>";
        assertEquals(XmlCursor.TokenType.STARTDOC,m_xc.currentTokenType());
        toNextTokenOfType(m_xc, TokenType.START);
        //since the operation is delete+replace
        //0,0 is equivalent to a delete
        m_xc.setTextValue(buffer, 0, 0);
        toPrevTokenOfType(m_xc, TokenType.STARTDOC);
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testLargeCharCount() {
        String sNewVal = " 20";
        int nCharCount = 10;
        assertEquals(true, sNewVal.length() < nCharCount);
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setTextValue(sNewVal.toCharArray(), 0, nCharCount);
//        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals(sNewVal, m_xc.getTextValue());
    }

    //offset+selection>buffer
    public void testSelectionPastEnd() {
        String sNewVal = " 20";
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setTextValue(sNewVal.toCharArray(), 2, 4);
//        toPrevTokenOfType(m_xc, TokenType.START);
        assertEquals("0", m_xc.getTextValue());
    }

    //spec doesn't say anything about text???
    public void testText() {
        String sNewVal = "5000 ";
        char[] buff = sNewVal.toCharArray();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.setTextValue(buff, 0, buff.length);
            fail("SetText in TEXT token");
        }
        catch (IllegalStateException e) {
        }

    }

    //$NOTE:did I forget a type
    public void testSetIllegalCursorPos() {

        char[] buffer = new char[100];
        int i = 0;
        toNextTokenOfType(m_xc, TokenType.END);
        try {
            m_xc.setTextValue(buffer, 0, 100);
            i++;
        }
        catch (IllegalStateException e) {
        }

        toNextTokenOfType(m_xc, TokenType.ENDDOC);
        try {
            m_xc.setTextValue(buffer, 0, 100);
            fail("SetText in ENDDOC token");
        }
        catch (IllegalStateException e) {
        }
        if (i > 0)
            fail("SetText in END token");
    }

    public void setUp() throws Exception {
        m_xc = XmlObject.Factory.parse(sDoc).newCursor();
    }
}
