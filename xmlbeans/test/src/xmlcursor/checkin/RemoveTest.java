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
import org.apache.xmlbeans.XmlCursor.TokenType;

import xmlcursor.common.*;

import test.xbean.xmlcursor.purchaseOrder.USAddress;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import tools.util.JarUtil;


/**
 *
 *
 */
public class RemoveTest extends BasicCursorTestCase {
    public RemoveTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(RemoveTest.class);
    }

    public void testRemoveFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        try {
            m_xc.removeXml();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e) {
        }
    }

    public void testRemoveFromFirstChild() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.removeXml();
        assertEquals(TokenType.ENDDOC, m_xc.currentTokenType());
    }

    public void testRemoveAllText() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.removeXml();
        assertEquals(TokenType.END, m_xc.currentTokenType());
        m_xc.toStartDoc();
        assertEquals("<foo><bar/></foo>", m_xc.xmlText());
    }

    public void testRemovePartialText() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.removeXml();
        assertEquals(TokenType.END, m_xc.currentTokenType());
        m_xc.toStartDoc();
        assertEquals("<foo><bar>te</bar></foo>", m_xc.xmlText());
    }

    public void testRemoveFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        m_xc.removeXml();
        assertEquals(TokenType.ATTR, m_xc.currentTokenType());
        m_xc.toStartDoc();
        assertEquals("<foo attr1=\"val1\">text</foo>", m_xc.xmlText());
    }

    public void testRemoveAffectOnXmlObjectGetXXX() throws Exception {
        //  m_xo =XmlObject.Factory.parse(JarUtil.getResourceFromJar(
        //          Common.XMLCASES_JAR, Common.TRANXML_FILE_XMLCURSOR_PO));
        //XmlObject.Factory.parse(Common.XML_PURCHASEORDER);
        m_xo = XmlObject.Factory.parse(JarUtil.getResourceFromJar(
                "xbean/xmlcursor/po.xml"));
        m_xc = m_xo.newCursor();
        String sQuery=
                 "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";"+
                 "$this//po:shipTo";
        m_xc.selectPath( sQuery );
        m_xc.toNextSelection();
        XmlObject xo = m_xc.getObject();
        USAddress usa = (USAddress) xo;
        m_xc.removeXml();
        try {
            usa.getCity();
            fail("Expected XmlValueDisconnectedException");
        }
        catch (XmlValueDisconnectedException xvde) {
        }
        assertTrue(true);
    }

    public void testRemoveAffectOnXmlObjectNewCursor() throws Exception {

        // m_xo = XmlObject.Factory.parse(Common.XML_PURCHASEORDER);
        m_xo = XmlObject.Factory.parse(JarUtil.getResourceFromJar(
              "xbean/xmlcursor/po.xml"));
        m_xc = m_xo.newCursor();
         String sQuery=
                 "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";"+
                 "$this//po:shipTo";
        m_xc.selectPath( sQuery );
        m_xc.toNextSelection();
        XmlObject xo = m_xc.getObject();
        USAddress usa = (USAddress) xo;
        m_xc.removeXml();
        assertNotNull("USAddress object expected non-null, but is null", usa);
        try {
            m_xc = usa.newCursor();
            fail("Expected XmlValueDisconnectedException");
        }
        catch (XmlValueDisconnectedException npe) {
        }
        assertTrue(true);
    }
}

