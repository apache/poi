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
import org.apache.xmlbeans.XmlOptions;

import java.util.Map;
import java.util.HashMap;
import javax.xml.namespace.QName;

import java.util.Vector;

import xmlcursor.common.*;
import tools.util.JarUtil;

import java.net.URL;

import org.apache.xmlbeans.xml.stream.XMLInputStream;


/**
 *
 *
 */
public class NamespaceForPrefixTest extends BasicCursorTestCase {
    public NamespaceForPrefixTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(NamespaceForPrefixTest.class);
    }

    public void testNamespaceForPrefixFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\">text</foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        m_xc.toStartDoc();
        assertEquals("uri1", m_xc.namespaceForPrefix("pre1"));
        assertEquals("uri2", m_xc.namespaceForPrefix("pre2"));
        assertEquals("uri3", m_xc.namespaceForPrefix("pre3"));
    }

    public void testNamespaceForPrefixFromSTARTDOCInvalid() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\">text</foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        m_xc.toStartDoc();
        assertEquals(null, m_xc.namespaceForPrefix("pre4"));
    }

    public void testNamespaceForPrefixFromSTARTDOCNull() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\">text</foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        m_xc.toStartDoc();
        assertEquals("uridefault", m_xc.namespaceForPrefix(null));
    }

    public void testNamespaceForPrefixFromSTARTDOCEmptyString() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\">text</foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        m_xc.toStartDoc();
        assertEquals("uridefault", m_xc.namespaceForPrefix(""));
    }

    public void testNamespaceForPrefixFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertEquals("http://www.w3.org/2000/10/XMLSchema-instance",
                     m_xc.namespaceForPrefix("xsi"));
    }

    public void testNamespaceForPrefixFromSTARTdefaultNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertEquals("http://www.tranxml.org/TranXML/Version4.0",
                     m_xc.namespaceForPrefix(""));
    }

    public void testNamespaceForPrefixFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\"><bar attr0=\"val0\">text</bar></foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        m_xc.toStartDoc();
        m_xc.selectPath("declare default element namespace \"nsa\";" + "$this//bar");
        m_xc.toNextSelection();
        m_xc.toFirstAttribute();

        try {
            m_xc.namespaceForPrefix(null);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException iae) {
        }
        //assertEquals("nsa", m_xc.namespaceForPrefix(null));
        // assertEquals("uri1", m_xc.namespaceForPrefix("pre1"));
    }

    public void testNamespaceForPrefixFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"nsa\"><bar attr0=\"val0\">text</bar></foo>");
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        System.out.println("i am here " + m_xc.currentTokenType());
        m_xc.insertNamespace("pre1", "uri1");
        m_xc.insertNamespace("pre2", "uri2");
        m_xc.insertNamespace("pre3", "uri3");
        m_xc.insertNamespace(null, "uridefault");
        toNextTokenOfType(m_xc, TokenType.END);
        try {
            m_xc.namespaceForPrefix(null);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException iae) {
        }
        //  assertEquals("nsa", m_xc.namespaceForPrefix(null));
        // assertEquals("uri1", m_xc.namespaceForPrefix("pre1"));
    }
}

