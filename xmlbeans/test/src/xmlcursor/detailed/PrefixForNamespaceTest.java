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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 *
 */
public class PrefixForNamespaceTest extends BasicCursorTestCase {

    public PrefixForNamespaceTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(PrefixForNamespaceTest.class);
    }

    public void testprefixForNamespaceFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\">text</foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        m_xc.toStartDoc();
        assertEquals("pre1", m_xc.prefixForNamespace("uri1"));
        assertEquals("pre2", m_xc.prefixForNamespace("uri2"));
        assertEquals("pre3", m_xc.prefixForNamespace("uri3"));
    }

    public void testprefixForNamespaceFromSTARTDOCInvalid() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\">text</foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("ns1", "uri1");
        m_xc.insertNamespace("ns2", "uri2");
        m_xc.insertNamespace("ns3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        m_xc.toStartDoc();
        assertEquals("uri4", m_xc.prefixForNamespace("uri4"));
    }

    public void testprefixForNamespaceFromSTARTDOCNull() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\">text</foo>");
        m_xc = m_xo.newCursor();
        try {
            m_xc.prefixForNamespace(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testprefixForNamespaceFromSTARTDOCEmptyString() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\">text</foo>");
        m_xc = m_xo.newCursor();
        try {
            m_xc.prefixForNamespace("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testprefixForNamespaceFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(
                      JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertEquals("xsi",
                     m_xc.prefixForNamespace("http://www.w3.org/2000/10/XMLSchema-instance"));
    }

    public void testprefixForNamespaceFromSTARTdefaultNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse(
                      JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertEquals("",
                     m_xc.prefixForNamespace("http://www.tranxml.org/TranXML/Version4.0"));
    }

    public void testprefixForNamespaceFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\"><bar attr0=\"val0\">text</bar></foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        m_xc.toStartDoc();
        m_xc.selectPath("default element namespace=\"nsa\"" + "$this//bar");
        m_xc.toFirstAttribute();
        assertEquals("nsa", m_xc.prefixForNamespace("nsa"));
        assertEquals("pre1", m_xc.prefixForNamespace("uri1"));
    }

    public void testprefixForNamespaceFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\"><bar attr0=\"val0\">text</bar></foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        toNextTokenOfType(m_xc, TokenType.END);
        //the default prefix
         assertEquals("", m_xc.prefixForNamespace("nsa"));
        // assertEquals("pre1", m_xc.prefixForNamespace("uri1"));
    }


}

