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
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor.TokenType;

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.util.*;
import java.net.URL;


/**
 *
 *
 */
public class InsertNamespaceTest extends BasicCursorTestCase {
    public InsertNamespaceTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(InsertNamespaceTest.class);
    }

    public void testInsertNamespaceAfterSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertNamespace("prefix", "value");
        m_xc.toStartDoc();
        XmlOptions map = new XmlOptions();
        map.put(XmlOptions.SAVE_NAMESPACES_FIRST, "");
        assertEquals("<foo xmlns:prefix=\"value\" attr0=\"val0\" attr1=\"val1\">text</foo>", m_xc.xmlText(map));
    }

    public void testInsertNamespaceAfterATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        toNextTokenOfType(m_xc, TokenType.ATTR);
        m_xc.insertNamespace("prefix", "value");
        m_xc.toStartDoc();
        XmlOptions map = new XmlOptions();
        map.put(XmlOptions.SAVE_NAMESPACES_FIRST, "");
        assertEquals("<foo xmlns:prefix=\"value\" attr0=\"val0\" attr1=\"val1\">text</foo>", m_xc.xmlText(map));
    }

    public void testInsertNamespaceInsideTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        try {
            m_xc.insertNamespace("prefix", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertTrue(true);
    }

    public void testInsertNamespaceFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        try {
            m_xc.insertNamespace("prefix", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertTrue(true);
    }

    public void testInsertNamespaceAfterPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        m_xc.toNextToken();
        try {
            m_xc.insertNamespace("prefix", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertTrue(true);
    }

    public void testInsertNamespaceAfterNAMESPACE() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_NS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        m_xc.toNextToken();
        m_xc.insertNamespace("prefix", "value");
        m_xc.toStartDoc();
        assertEquals("<foo xmlns=\"http://www.foo.org\" xmlns:prefix=\"value\"/>", m_xc.xmlText());
    }

    public void testInsertDuplicateNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.insertNamespace("prefix", "http://www.foo.org");
        m_xc.insertNamespace("prefix", "http://www.foo.org");
        m_xc.toStartDoc();
        assertEquals("<foo xmlns:prefix=\"http://www.foo.org\">text</foo>", m_xc.xmlText());
    }

    public void testInsertNamespaceWithNullPrefix() throws Exception {
        // According to Eric V...  This test is not valid
        // Eric's comments:
        // is erroneous.  <foo> must be in no namespace.
        // By setting the default namespace to "http://www.foo.org"
        // and having it saved out as such, <foo> would be in that
        // namespace.  So, the saver does not save out that namespace
        // (note that mapping a prefix to no namespace "" is illegal).


        // Below is the original code.
        // m_xo = XmlObject.Factory.parse(Common.XML_FOO,
        // XmlOptions.AUTOTYPE_DOCUMENT_LAX);
        // m_xc = m_xo.newCursor();
        // toNextTokenOfType(m_xc, TokenType.END);
        // m_xc.insertNamespace(null, "http://www.foo.org");
        // m_xc.toStartDoc();
        // assertEquals("<foo xmlns=\"http://www.foo.org\"/>", m_xc.xmlText());
    }

    public void testInsertNamespaceWithNullValue() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);

       //EricV: this should be OK, but make sure the saver
        // doesn't serialize it since it's not legal XML

         m_xc.insertNamespace("prefix", null);
         m_xc.toStartDoc();
        assertEquals("<foo/>", m_xc.xmlText());
    }

    public void testInsertEmptyNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        try {
            m_xc.insertNamespace("", "");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
    }

}

