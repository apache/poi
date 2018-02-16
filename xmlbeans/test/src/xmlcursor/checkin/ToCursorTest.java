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

import org.apache.xmlbeans.XmlOptions;
import junit.framework.*;
import junit.framework.Assert.*;

import java.io.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;


/**
 *
 *
 */
public class ToCursorTest extends BasicCursorTestCase {
    public ToCursorTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ToCursorTest.class);
    }

    public void testToCursorMoves() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        xc0.toEndDoc();
        try {
            assertEquals(true, m_xc.toCursor(xc0));
            assertEquals(true, xc0.isAtSamePositionAs(m_xc));
        } finally {
            xc0.dispose();
        }
    }

    /**
     * FIXED: toCursor(null) does not return a boolean but throws an exception.
     */
    public void testToCursorNull() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        m_xc.toFirstChild();
        String s = m_xc.xmlText();
        boolean caught = false;
        try {
            m_xc.toCursor(null);
        } catch (java.lang.IllegalArgumentException e) {
            caught = true;
        }
        assertTrue("toCursor(null) did not throw IllegalArgumentException", caught);
        assertEquals(s, m_xc.xmlText());
    }

    public void testToCursorDifferentDocs() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        String s = m_xc.xmlText();
        toNextTokenOfType(xc0, TokenType.TEXT);
        try {
            assertEquals(false, m_xc.toCursor(xc0));
            assertEquals(s, m_xc.xmlText());
        } finally {
            xc0.dispose();
        }
    }

    public void testToCursorThis() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        m_xc.toFirstChild();
        String s = m_xc.xmlText();
        assertEquals(true, m_xc.toCursor(m_xc));
        assertEquals(s, m_xc.xmlText());
    }

}

