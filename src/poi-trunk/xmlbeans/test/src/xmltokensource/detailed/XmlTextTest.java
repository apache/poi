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


package xmltokensource.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import tools.util.JarUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.xml.namespace.QName;



public class XmlTextTest extends BasicCursorTestCase {
    private XmlOptions m_map = new XmlOptions();

    public XmlTextTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(XmlTextTest.class);
    }

    public void testSAVENAMESPACESFIRST() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"http://www.foo.org\">01234</foo>");
        m_xc = m_xo.newCursor();
        m_map.put(XmlOptions.SAVE_NAMESPACES_FIRST, "");
        assertEquals("<foo xmlns=\"http://www.foo.org\" attr0=\"val0\">01234</foo>",
                     m_xc.xmlText(m_map));
    }

    public void testSAVENAMESPACESlast() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"http://www.foo.org\">01234</foo>");
        m_xc = m_xo.newCursor();
        assertEquals("<foo attr0=\"val0\" xmlns=\"http://www.foo.org\">01234</foo>",
                     m_xc.xmlText(m_map));
    }

    public void testSaveSyntheticDocumentElement() throws Exception {
        m_xo = XmlObject.Factory.parse("<bar>text</bar>");
        m_xc = m_xo.newCursor();
        m_map.put(XmlOptions.SAVE_SYNTHETIC_DOCUMENT_ELEMENT,
                  new QName("foo"));
        assertEquals("<foo><bar>text</bar></foo>", m_xc.xmlText(m_map));
    }

    public void testSavePrettyPrint() throws Exception {
        m_xo = XmlObject.Factory.parse("<a><b><c> text </c></b></a>");
        m_xc = m_xo.newCursor();
        m_map.put(XmlOptions.SAVE_PRETTY_PRINT, "");
        String lnSep = System.getProperty("line.separator");
        assertEquals("<a>" + lnSep + "  <b>" + lnSep + "    <c>text</c>" + lnSep + "  </b>" + lnSep + "</a>", m_xc.xmlText(m_map));
    }

    public void testSavePrettyPrintIndent3() throws Exception {
        m_xo = XmlObject.Factory.parse("<a><b><c> text </c></b></a>");
        m_xc = m_xo.newCursor();
        m_map.put(XmlOptions.SAVE_PRETTY_PRINT, "");
        m_map.put(XmlOptions.SAVE_PRETTY_PRINT_INDENT, new Integer(3));
        String lnSep = System.getProperty("line.separator");
        assertEquals("<a>" + lnSep + "   <b>" + lnSep + "      <c>text</c>" + lnSep + "   </b>" + lnSep + "</a>", m_xc.xmlText(m_map));
    }

    public void testSavePrettyPrintIndentNeg1() throws Exception {
        m_xc = XmlObject.Factory.parse("<a>  \n  <b>  \n    <c> text   </c>   \n  </b>  \n  </a>").newCursor();
        m_map.put(XmlOptions.SAVE_PRETTY_PRINT, "");
        m_map.put(XmlOptions.SAVE_PRETTY_PRINT_INDENT, new Integer(-1));
        assertEquals("<a><b><c>text</c></b></a>", m_xc.xmlText(m_map));
    }

    public void testDefaultNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//FleetID");
        m_xc.toNextSelection();
        m_map.put(XmlOptions.SAVE_NAMESPACES_FIRST, "");
        assertEquals("<FleetID xmlns=\"" + Common.CLM_NS + "\" " +
                     Common.CLM_XSI_NS +
                     ">FLEETNAME</FleetID>",
                     m_xc.xmlText(m_map));
    }

    public void testSTARTDOCvsFirstChild() throws Exception {
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        XmlCursor xc1 = m_xo.newCursor();
        xc1.toFirstChild();
        try {
            assertEquals(m_xc.xmlText(), xc1.xmlText());
        } finally {
            xc1.dispose();
        }
    }

    public void testXmlTextFromTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals("text", m_xc.getChars());
    }

    public void testXmlTextFromTEXTafterEND() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar> text </bar> ws \\r\\n </foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(" ws \\r\\n ", m_xc.getChars());
    }
}

