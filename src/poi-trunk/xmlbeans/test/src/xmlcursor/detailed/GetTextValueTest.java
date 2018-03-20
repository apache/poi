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
import org.apache.xmlbeans.XmlCursor.TokenType;
import xmlcursor.common.*;


/**
 *
 *
 */
public class GetTextValueTest extends BasicCursorTestCase {


    String sDoc = Common.XML_FOO_NS_PREFIX;

    public GetTextValueTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(GetTextValueTest.class);
    }

    // Depth first concatenation of all text leaves
     
    public void testNormalCase() {
        String sExpected = "  32.18";
        char[] buffer = new char[100];
        int nCopied = m_xc.getTextValue(buffer, 0, 100);
        assertEquals(sExpected.length(), nCopied);
        assertEquals(sExpected, new String(buffer).substring(0, nCopied));
    }

    public void testGetNull() {
        try {
            m_xc.getTextValue(null, 0, 10);
            fail("Buffer was Null");
        } catch (IllegalArgumentException ie) {
        }
    }

    public void testNegativeOffset() {
        char[] buffer = new char[100];
        try {
            m_xc.getTextValue(buffer, -1, 100);
            fail("Offset < 0");
        } catch (IllegalArgumentException ie) {
        }

    }

    public void testNonZeroOffset() {
        String sExpected = "T\0  32.18";
        char[] buffer = new char[10];
        buffer[0] = 'T';
        int nOffset = 2;
        int nCopied = m_xc.getTextValue(buffer, 2, 8);
        assertEquals(7, nCopied);
        assertEquals(sExpected,
                new String(buffer).substring(0, nCopied + nOffset));
        assertEquals("",
                new String(buffer).substring(nOffset + nCopied, buffer.length)
                .trim());

    }

    public void testLargeOffset() {
        char[] buffer = new char[100];
        try {
            m_xc.getTextValue(buffer, 101, 1);
            fail("Offset Past end");
        } catch (IllegalArgumentException ie) {
        }
    }

    //charCount<=0: should be a noop
    //BUT: Assumption is that <0=infinity, so all is copies
    public void testNegativeCharCount() {
        char[] buffer = new char[100];
        String sExpected = m_xc.getTextValue();
        int nCount = m_xc.getTextValue(buffer, 0, -1);
        assertEquals(sExpected.length(), nCount);
        assertEquals(sExpected, new String(buffer, 0, nCount));
    }

    public void testZeroCharCount() {
        char[] buffer = new char[10];
        int nCopied = m_xc.getTextValue(buffer, 0, 0);
        assertEquals(0, nCopied);
        assertEquals("", new String(buffer).trim());
    }

    public void testLargeCharCount() {
        String sExpected = "  32.18";
        char[] buffer = new char[200];
        int nCharCount = 300;
        assertEquals(true, sDoc.length() < nCharCount);
        assertEquals(false, buffer.length >= nCharCount);
        int nCopied = m_xc.getTextValue(buffer, 0, nCharCount);
        assertEquals(sExpected.length(), nCopied);
        assertEquals(sExpected, new String(buffer).substring(0, nCopied));
    }

    //offset+selection>buffer
    public void testSelectionPastEnd() {
        String sExpected = "  3";
        char[] buffer = new char[100];
        int nCopied = m_xc.getTextValue(buffer, 97, 4);
        assertEquals(sExpected.length(), nCopied);
        assertEquals(sExpected, new String(buffer,97, nCopied) );
        assertEquals("", new String(buffer,0, 97).trim());
    }


    //End,Enddoc,Namespace should
    //return 0 as per spec
    //NB: Design changed, should work now
    public void testGetNonTextElement() {
        char[] buffer = new char[100];
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        int nCopied = m_xc.getTextValue(buffer, 0, 100);
        String sExpected = "http://ecommerce.org/schema";
        assertEquals(sExpected,
                new String(buffer, 0, nCopied));
        assertEquals(sExpected.length(), nCopied);
        try {
            toNextTokenOfType(m_xc, TokenType.END);
            nCopied = m_xc.getTextValue(buffer, 0, 100);
            fail("Operation not allowed");
        } catch (java.lang.IllegalStateException e) {
        }

        try {
            toNextTokenOfType(m_xc, TokenType.ENDDOC);
            nCopied = m_xc.getTextValue(buffer, 0, 100);
            fail("Operation not allowed");
        } catch (java.lang.IllegalStateException e) {
        }


    }

    //test text of comment, PI or Attr
    public void testCommentPIAttr() throws Exception {
        String sExpected = "http://ecommerce.org/schema";
        int nSize = sExpected.length();
        char[] buffer = new char[nSize + 1];
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        int nCopied = m_xc.getTextValue(buffer, 0, nSize);
        assertEquals(sExpected,
                new String(buffer)
                .substring(0, nCopied));
        assertEquals(sExpected.length(), nCopied);

        String sTestXml = "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><foo at0=\"value0\">text</foo>";
        m_xc = XmlObject.Factory.parse(sTestXml).newCursor();
        nCopied = m_xc.getTextValue(buffer, 0, nSize);
        //assert attributes are skipped
        assertEquals("text", new String(buffer).substring(0, nCopied));
        assertEquals("text".length(), nCopied);

        buffer = new char[100];
        toNextTokenOfType(m_xc, TokenType.ATTR);
        nCopied = m_xc.getTextValue(buffer, 0, 100);
        assertEquals("value0", new String(buffer).substring(0, nCopied));
        assertEquals("value0".length(), nCopied);

        sExpected =
                "type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"";
        nSize = sExpected.length();
        toPrevTokenOfType(m_xc, TokenType.PROCINST);
        nCopied = m_xc.getTextValue(buffer, 0, nSize);
        assertEquals(sExpected, new String(buffer)
                .substring(0, nCopied));
        assertEquals(sExpected.length(), nCopied);

    }

    public void setUp() throws Exception {
        m_xc = XmlObject.Factory.parse(sDoc)
                .newCursor();
    }
}
