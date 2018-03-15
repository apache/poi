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

import javax.xml.namespace.QName;

import xmlcursor.common.*;
import tools.util.JarUtil;

import java.net.URL;


/**
 *
 *
 */
public class GetTextTest extends BasicCursorTestCase {
    public GetTextTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(GetTextTest.class);
    }

    public void testGetTextFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.toEndDoc();
        m_xc.toPrevToken();
        assertEquals(TokenType.END, m_xc.currentTokenType());
        //assertEquals(null, m_xc.getTextValue());

        try {
            m_xc.getTextValue();
            fail("Expecting Illegal State Exception");
        } catch (IllegalStateException ie) {
        }

    }

    public void testGetTextFromPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        assertEquals("type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"", m_xc.getTextValue());
    }

    public void testGetTextFromCOMMENT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_COMMENT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.COMMENT);
        assertEquals(" comment text ", m_xc.getTextValue());
    }

    public void testGetTextFromNAMESPACE() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_NS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        //assertEquals(null, m_xc.getTextValue());

       //modifying test: behavior OK as of Sept 04
        //filed bug on API
           String text= m_xc.getTextValue();
        assertEquals("http://www.foo.org", text);


    }

    public void testGetTextFromENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ENDDOC);
        //assertEquals(null, m_xc.getTextValue());
        try {
            m_xc.getTextValue();
            fail("Expecting Illegal State Exception");
        } catch (IllegalStateException ie) {
        }

    }


    public void testGetTextFromTEXT() throws Exception {
        //  m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);

        m_xo = XmlObject.Factory.parse("<foo>text</foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(TokenType.TEXT, m_xc.currentTokenType());
        assertEquals("text", m_xc.getChars());
	assertEquals("text", m_xc.getTextValue());

	m_xc.toNextChar(2);
	assertEquals(TokenType.TEXT, m_xc.currentTokenType());
        assertEquals("xt", m_xc.getTextValue());
    }

    public void testGetTextFromSTART_NotNested() throws Exception {
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//FleetID");


        m_xc.toNextSelection();
        assertEquals("FLEETNAME", m_xc.getTextValue());
    }

    public void testGetTextFromSTART_Nested() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//EventStatus/EquipmentStructure");
        m_xc.toNextSelection();
        assertEquals("\n\t\t\tGATX\n\t\t\t123456\n\t\t\tL\n\t\t", m_xc.getTextValue());
    }

    public void testGetTextFromSTART_TextAferEND() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT_EXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        assertEquals("text", m_xc.getTextValue());
    }

    public void testGetTextFromSTART_TextAferEND_WS() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_WS_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        assertEquals(" text ", m_xc.getTextValue());
    }

    public void testGetTextFromATTR_Nested() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        String preface="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        m_xc.selectPath(preface+" .//po:billTo");
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("US", m_xc.getTextValue());
    }

    public void testGetTextFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT_EXT);
        m_xc = m_xo.newCursor();
        assertEquals("textextended", m_xc.getTextValue());
    }

    public void testGetTextEmptyElementSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        assertEquals("", m_xc.getTextValue());
    }

    public void testGetTextWhitespaceOnlyFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_WS_ONLY);
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertEquals("   ", m_xc.getTextValue());
    }

}

