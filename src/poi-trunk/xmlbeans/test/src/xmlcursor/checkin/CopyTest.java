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


import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.xml.namespace.QName;



/**
 *
 *
 */
public class CopyTest extends BasicCursorTestCase {
    public CopyTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(CopyTest.class);
    }

    public void testCopyToNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try {
            m_xc.copyXml(null);
            fail(
                    "Expected IllegalArgumentException.  Can't copy to foreign document");
        }
        catch (IllegalArgumentException ise) {
        }
    }

    public void testCopyDifferentStoresLoadedByParse() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlObject xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        XmlCursor xc1 = xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc1, TokenType.TEXT);
        m_xc.copyXml(xc1);
        xc1.toParent();
        // verify xc1
        assertEquals("01234text", xc1.getTextValue());
        xc1.dispose();
        // verify m_xc
        assertEquals("01234", m_xc.getChars());
    }

    /**
     * Method testCopyDifferentStoresLoadedFromFile
     * <p/>
     * Tests copy from document w/ namespaces to doc w/o
     *
     * @throws Exception
     */
    public void testCopyDifferentStoresLoadedFromFile() throws Exception {
        // load the documents and obtain a cursor
        XmlObject xobj0 = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        XmlObject xobj1 = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));

        XmlCursor xc0 = xobj0.newCursor();
        XmlCursor xc1 = xobj1.newCursor();

        xc0.selectPath(Common.CLM_NS_XQUERY_DEFAULT + " .//Initial");
        xc0.toNextSelection();
         String sQuery=
                 "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; "+
                 ".//po:zip";
        xc1.selectPath( sQuery );
        xc1.toNextSelection();

        xc0.copyXml(xc1); // should copy the <Initial>GATX</Initial> element plus the default namespace
        xc1.toPrevSibling();
        // verify xc1
        String sExpected = "<ver:Initial " +
                "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\" " +
                "xmlns:ver=\"http://www.tranxml.org/TranXML/Version4.0\">" +
                "GATX</ver:Initial>";
        assertEquals(sExpected, xc1.xmlText());
        // verify xc0
        // should contain all the namespaces for the document
        assertEquals(
                "<Initial xmlns=\"" + Common.CLM_NS + "\" " +
                Common.CLM_XSI_NS +
                ">GATX</Initial>",
                xc0.xmlText());
        xc0.dispose();
        xc1.dispose();

    }

    /**
     * Method testCopyDifferentStoresLoadedFromFile2
     * <p/>
     * Tests copy from document w/o namespaces to document with namespaces
     *
     * @throws Exception
     */
    public void testCopyDifferentStoresLoadedFromFile2() throws Exception {
        // load the documents and obtain a cursor
        XmlObject xobj0 = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        XmlObject xobj1 = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));

        XmlCursor xc0 = xobj0.newCursor();
        XmlCursor xc1 = xobj1.newCursor();

        xc0.selectPath(Common.CLM_NS_XQUERY_DEFAULT + " .//Initial");
        xc0.toNextSelection();

         String sQuery=
                 "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; "+
                 ".//po:zip";
        xc1.selectPath( sQuery );
        xc1.selectPath( sQuery );
        xc1.toNextSelection();

        xc1.copyXml(xc0); // should copy the <zip>90952</zip> element
        // verify xc1
        assertEquals(
                "<po:zip xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\">90952</po:zip>",
                xc1.xmlText());
        // verify xc0
        // should contain all the namespaces for the document
        xc0.toPrevSibling();
        // assertEquals("<zip xmlns=\"" + Common.CLM_NS + "\" " + Common.CLM_XSI_NS + ">90952</zip>", xc0.xmlText());
        String sExpected = "<po:zip " +
                "xmlns=\"http://www.tranxml.org/TranXML/Version4.0\" " +
                "xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\" " +
                "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\">" +
                "90952</po:zip>";

        assertEquals(sExpected, xc0.xmlText());
        xc0.dispose();
        xc1.dispose();

    }

    public void testCopySameLocation() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc1, TokenType.TEXT);
        m_xc.copyXml(xc1);
        xc1.dispose();
        m_xc.toParent();
        assertEquals("0123401234", m_xc.getTextValue());
    }

    public void testCopyNewLocation() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));

        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";
        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        m_xc.selectPath(ns+" .//po:shipTo/po:city");
        m_xc.toNextSelection();
        xc1.selectPath(ns +" .//po:billTo/po:city");
        xc1.toNextSelection();
        m_xc.copyXml(xc1);
        xc1.toPrevToken();
        xc1.toPrevToken();
        // verify xc1
        assertEquals("Mill Valley", xc1.getChars());
        xc1.dispose();
        // verify m_xc
        assertEquals("Mill Valley", m_xc.getTextValue());

    }

    public void testCopyElementToMiddleOfTEXT() throws Exception {

        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";
        String exp_ns="xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        m_xc.selectPath(ns+" .//po:shipTo/po:city");
        m_xc.toNextSelection();
        xc1.selectPath(ns+" .//po:billTo/po:city");
        xc1.toNextSelection();
        xc1.toNextToken();
        xc1.toNextChar(4);  // should be at 'T' in "Old Town"
        m_xc.copyXml(xc1);     // should be "Old <city>Mill Valley</city>Town"
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
        assertEquals("<po:city "+exp_ns+">Mill Valley</po:city>", m_xc.xmlText());
    }



}

