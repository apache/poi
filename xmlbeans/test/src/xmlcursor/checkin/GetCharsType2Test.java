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
public class GetCharsType2Test extends BasicCursorTestCase {
    public GetCharsType2Test(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(GetCharsType2Test.class);
    }

    public void testGetCharsType2LessThanBufLength() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        char[] buf = new char[5];
        assertEquals(3, m_xc.getChars(buf, 0, 3));
        String s = new String(buf);
        assertEquals("012\0\0", s);
    }

    public void testGetCharsType2GTBufLengthMinusOffset() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        char[] buf = new char[5];
        assertEquals(2, m_xc.getChars(buf, 3, 3));
        assertEquals('\0', buf[0]);
        assertEquals('\0', buf[1]);
        assertEquals('\0', buf[2]);
        assertEquals('0', buf[3]);
        assertEquals('1', buf[4]);
    }

    public void testGetCharsType2FromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        char[] buf = new char[5];
        assertEquals(0, m_xc.getChars(buf, 3, 4));
    }

    public void testGetCharsType2FromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        char[] buf = new char[5];
        assertEquals(0, m_xc.getChars(buf, 3, 4));
    }

    public void testGetCharsType2FromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        char[] buf = new char[5];
        assertEquals(0, m_xc.getChars(buf, 3, 4));
    }

    public void testGetCharsType2FromNAMESPACE() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        char[] buf = new char[5];
        assertEquals(0, m_xc.getChars(buf, 3, 4));
    }

    public void testGetCharsType2FromPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        char[] buf = new char[5];
        assertEquals(0, m_xc.getChars(buf, 3, 4));
    }

    public void testGetCharsType2FromEND() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        char[] buf = new char[5];
        assertEquals(0, m_xc.getChars(buf, 3, 4));
    }

    public void testGetCharsType2FromENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        m_xc.toEndDoc();
        char[] buf = new char[5];
        assertEquals(0, m_xc.getChars(buf, 3, 4));
    }

    public void testGetCharsType2FromCOMMENT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_COMMENT);
        m_xc = m_xo.newCursor();
        m_xc.toEndDoc();
        char[] buf = new char[5];
        assertEquals(0, m_xc.getChars(buf, 3, 4));
    }

}

