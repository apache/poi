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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;


public class GetNameTest extends BasicCursorTestCase {
    public GetNameTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(GetNameTest.class);
    }

    public void testGetNameFromSTARTDOC() throws Exception {
       // String test="<?xml version=\"1.0\"?><purchaseOrder xmlns= \"http://www.bea.com/po\" orderDate=\"1999-10-20\"><shipTo country=\"US\"><name>Alice Smith</name><street>123 Maple Street</street><city>Mill Valley</city><state>CA</state><zip>90952</zip></shipTo><comment>Hurry, my lawn is going wild!</comment><!-- comment text --><items>2 <item partNum=\"872-AA\" partid=\"00A\"><productName>Lawnmower</productName> <quantity>10</quantity></item><item partNum=\"926-AA\" partid=\"00B\"><productName>Baby Monitor</productName><quantity>1</quantity></item></items></purchaseOrder>";
        String test="<?xml version=\"1.0\"?>\n" +
                "<po:purchaseOrder xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\" orderDate=\"1999-10-20\">\n" +
                "    <po:shipTo country=\"US\">\n" +
                "        <po:name>Alice Smith</po:name>\n" +
                "        <po:street>123 Maple Street</po:street>\n" +
                "        <po:city>Mill Valley</po:city>\n" +
                "        <po:state>CA</po:state>\n" +
                "        <po:zip>90952</po:zip>\n" +
                "    </po:shipTo>\n" +
                "    <po:billTo country=\"US\">\n" +
                "        <po:name>Robert Smith</po:name>\n" +
                "        <po:street>8 Oak Avenue</po:street>\n" +
                "        <po:city>Old Town</po:city>\n" +
                "        <po:state>PA</po:state>\n" +
                "        <po:zip>95819</po:zip>\n" +
                "    </po:billTo>\n" +
                "    <po:comment>Hurry, my lawn is going wild!</po:comment>\n" +
                "    <po:items>\n" +
                "        <po:item partNum=\"872-AA\">\n" +
                "            <po:productName>Lawnmower</po:productName>\n" +
                "            <po:quantity>1</po:quantity>\n" +
                "            <po:USPrice>148.95</po:USPrice>\n" +
                "            <po:comment>Confirm this is electric</po:comment>\n" +
                "        </po:item>\n" +
                "        <po:item partNum=\"926-AA\">\n" +
                "            <po:productName>Baby Monitor</po:productName>\n" +
                "            <po:quantity>1</po:quantity>\n" +
                "            <po:USPrice>39.98</po:USPrice>\n" +
                "            <po:shipDate>1999-05-21</po:shipDate>\n" +
                "        </po:item>\n" +
                "    </po:items>\n" +
                "</po:purchaseOrder>";
                m_xo = XmlObject.Factory.parse(test);
               /*   JarUtil.getResourceFromJar(Common.XMLCASES_JAR,
                        Common.TRANXML_FILE_XMLCURSOR_PO));
                        */
        m_xc = m_xo.newCursor();
        assertEquals(null, m_xc.getName());
    }

    public void testGetNameFromPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        assertEquals("xml-stylesheet", m_xc.getName().getLocalPart());
    }

    public void testGetNameFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";

        m_xc.selectPath(ns+" .//po:shipTo/po:city");
        m_xc.toNextSelection();
        assertEquals("city", m_xc.getName().getLocalPart());
    }

    public void testGetNameFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>text</bar></foo>");
        m_xc = m_xo.newCursor();
        m_xc.selectPath(".//bar");
        toNextTokenOfType(m_xc, TokenType.END);
        assertEquals(null, m_xc.getName());
    }

    public void testGetNameFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";

        m_xc.selectPath(ns+" .//po:shipTo");
        m_xc.toNextSelection();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("country", m_xc.getName().getLocalPart());
    }

    public void testGetNameFromCOMMENT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_COMMENT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.COMMENT);
        assertEquals(null, m_xc.getName());
    }

    public void testGetNameElementWithDefaultNamespace() throws Exception {
        m_xo =  XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + ".//ETA");
        m_xc.toNextSelection();
        assertEquals("ETA", m_xc.getName().getLocalPart());
        assertEquals(Common.CLM_NS, m_xc.getName().getNamespaceURI());
    }

    public void testGetNameAttrWithDefaultNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_NS_PREFIX);
        m_xc = m_xo.newCursor();
        String sDefaultElemNS = "declare default element namespace \"http://ecommerce.org/schema\"; ";
        m_xc.selectPath(sDefaultElemNS + ".//price");
        m_xc.toNextSelection();
        m_xc.toFirstAttribute();
        assertEquals("units", m_xc.getName().getLocalPart());
        // note: default namespace does not apply to attribute names, hence should be null
        assertEquals("", m_xc.getName().getNamespaceURI());
    }
}

