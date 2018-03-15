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

import junit.framework.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;

import javax.xml.namespace.QName;

import xmlcursor.common.*;


import tools.util.JarUtil;
import tools.util.Util;


/**
 *
 *
 */
public class MoveTest extends BasicCursorTestCase {
    public MoveTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(MoveTest.class);
    }

    public void testMoveToNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.moveXml(null);
            fail(
                    "Expected IllegalArgumentException.  Can't move to foreign document");
        }
        catch (IllegalArgumentException e) {
        }
    }

    public void testMoveDifferentStoresLoadedByParse() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlObject xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        XmlCursor xc1 = xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc1, TokenType.TEXT);
        m_xc.moveXml(xc1);
        xc1.toParent();
        // verify xc1
        assertEquals("01234text", xc1.getTextValue());
        xc1.dispose();
        // verify m_xc
        assertEquals(TokenType.END, m_xc.currentTokenType());
    }

    public void testMoveDifferentStoresLoadedFromFile() throws Exception {
        // load the documents and obtain a cursor
        XmlObject xobj0 = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        XmlObject xobj1 = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));

        XmlCursor xc0 = xobj0.newCursor();
        XmlCursor xc1 = xobj1.newCursor();

        xc0.selectPath(Common.CLM_NS_XQUERY_DEFAULT + ".//Initial");
        xc0.toNextSelection();

        String sQuery=
                "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; "+
                ".//po:zip";
        xc1.selectPath( sQuery );
        assertTrue( 0 < xc1.getSelectionCount());
        xc1.toNextSelection();


        xc0.moveXml(xc1); // should move the <Initial>GATX</Initial> element plus the namespace


        xc1.toPrevSibling();
        // verify xc1
        String sExpected = "<ver:Initial " +
                "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\" " +
                "xmlns:ver=\"http://www.tranxml.org/TranXML/Version4.0\">" +
                "GATX</ver:Initial>";
        assertEquals(sExpected, xc1.xmlText());
        // verify xc0
        xc0.toNextToken();  // skip the whitespace token
        assertEquals("123456", xc0.getTextValue());

        xc0.dispose();
        xc1.dispose();

    }

    public void testMoveSameLocation() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc1, TokenType.TEXT);
        m_xc.moveXml(xc1);
        xc1.dispose();
        assertEquals("01234", m_xc.getChars());
    }

    public void testMoveNewLocation() throws Exception {
       m_xo=XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";

        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        m_xc.selectPath(ns+" .//po:shipTo/po:city");
        m_xc.toNextSelection();
        xc1.selectPath(ns +" .//po:billTo/po:city");
        xc1.toNextSelection();
        m_xc.moveXml(xc1);
        xc1.toPrevToken();
        xc1.toPrevToken();

        // verify xc1
        assertEquals("Mill Valley", xc1.getChars());

        // verify m_xc
        m_xc.toNextToken(); // skip the whitespace token

        assertEquals("CA", m_xc.getTextValue());
    }

    public void testMoveElementToMiddleOfTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";

        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        m_xc.selectPath(ns+" .//po:shipTo/po:city");
        m_xc.toNextSelection();
        xc1.selectPath(ns+" .//po:billTo/po:city");
        xc1.toNextSelection();
        xc1.toNextToken();
        xc1.toNextChar(4);  // should be at 'T' in "Old Town"
        m_xc.moveXml(xc1);     // should be "Old <city>Mill Valley</city>Town"
        // verify xc1
        xc1.toPrevToken();
        assertEquals(TokenType.END, xc1.currentTokenType());
        xc1.toPrevToken();
        assertEquals("Mill Valley", xc1.getChars());
        xc1.toPrevToken();
        assertEquals(TokenType.START, xc1.currentTokenType());
        assertEquals(new QName("city").getLocalPart(),
                xc1.getName().getLocalPart());
        xc1.toPrevToken();

        assertEquals("Old ", xc1.getChars());
        // verify m_xc
        m_xc.toNextToken(); // skip the whitespace token

        assertEquals("CA", m_xc.getTextValue());
    }

    /**
     * Method testMoveFromSTARTDOC
     * <p/>
     * Also used to verify radar bug 16160
     *
     * @throws Exception
     */
    public void testMoveFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO);
        m_xc = m_xo.newCursor();
        try {
            m_xc.moveXml(m_xc);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // verify 16160
            String sTrace = Util.getStackTrace(e);
            int i = sTrace.indexOf("splay.bitch");
            assertTrue(i < 0);
        }
        assertTrue(true);
    }


}

