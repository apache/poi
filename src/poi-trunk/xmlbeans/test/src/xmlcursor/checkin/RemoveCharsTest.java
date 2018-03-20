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

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;


/**
 *
 *
 */
public class RemoveCharsTest extends BasicCursorTestCase {
    public RemoveCharsTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(RemoveCharsTest.class);
    }

    public void testRemoveCharsLTLengthFromTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(3, m_xc.removeChars(3));
        assertEquals("34", m_xc.getChars());
    }

    public void testRemoveCharsGTLengthFromTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(5, m_xc.removeChars(10));
        assertEquals(TokenType.END, m_xc.currentTokenType());
    }

    public void testRemoveCharsNegativeFromTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(5, m_xc.removeChars(-1));
        assertEquals(TokenType.END, m_xc.currentTokenType());
    }

    public void testRemoveCharsZeroFromTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(0, m_xc.removeChars(0));
        assertEquals("01234", m_xc.getChars());
    }

    public void testRemoveCharsFromPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        assertEquals(0, m_xc.removeChars(3));
    }

    public void testRemoveCharsFromNAMESPACE() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_NS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        assertEquals(0, m_xc.removeChars(3));
    }


}

