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

package xmlcursor.jsr173.common;


import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.namespace.QName;


import junit.framework.*;


//

/**
 * Methods tested
 * getText
 * getTextCharacters  x 2
 * getTextLength
 * getTextStart
 * hasText
 * Token Types should be DTD, ER, Chars, Comment, Space
 * currently DTD and ER are Not Impl
 */
public abstract class CharactersTest extends TestCase {

    public abstract XMLStreamReader getStream(XmlCursor c) throws Exception;

    public void testHasText() throws Exception {
        assertEquals(XMLStreamConstants.START_DOCUMENT,
                m_stream.getEventType());

        // assertEquals( XMLStreamConstants.ATTRIBUTE, m_stream.next()  );
        //  assertFalse( m_stream.hasText() );

        assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
        assertTrue(m_stream.hasText());

        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertFalse(m_stream.hasText());

        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
        assertTrue(m_stream.hasText());

        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertFalse(m_stream.hasText());
        assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
        assertFalse(m_stream.hasText());
        assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
        assertFalse(m_stream.hasText());

        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
//           assertTrue(  m_stream.isWhiteSpace());
        assertTrue(m_stream.hasText());
    }

    //also testing getTextStart and getTextLength
    public void testGetTextCharacters() throws Exception {
        try {
            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());
            m_stream.getTextLength();
            fail("Illegal State");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
        char[] result = m_stream.getTextCharacters();
        assertEquals(" some comment ", new String(result).substring(m_stream.getTextStart(),
                m_stream.getTextLength()));

        try {
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            m_stream.getTextLength();
            fail("Illegal State");
        } catch (IllegalStateException e) {
        }


        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
        result = m_stream.getTextCharacters();
        assertEquals("some text", new String(result).substring(m_stream.getTextStart(),
                m_stream.getTextLength()));

        m_stream.next();
        m_stream.next();//skip empty elt
        m_stream.next(); //end foo
        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
        result = m_stream.getTextCharacters();
        assertEquals("\t", new String(result).substring(m_stream.getTextStart(),
                m_stream.getTextLength()));
        try {
            m_stream.next();
            m_stream.getTextLength();
            fail("Illegal State");
        } catch (IllegalStateException e) {
        }
    }


    public void testGetTextCharactersBufferNegStart() throws Exception {
        m_stream.next();
        try {
            m_stream.getTextCharacters(-1, new char[10], 12, 12);
            fail(" java.lang.IndexOutOfBoundsException - if " +
                    "length < 0 or targetStart + length > length of target ");
        } catch (java.lang.IndexOutOfBoundsException e) {
        }
    }

    public void testGetTextCharactersBufferNull() throws Exception {
        m_stream.next();
        try {
            m_stream.getTextCharacters(0, null, 12, 12);
            fail(" java.lang.NullPointerException - is if target is null ");
        } catch (java.lang.NullPointerException e) {
        }

    }

    public void testGetTextCharactersLargeSrcOff() throws Exception {
        m_stream.next();
        try {
            m_stream.getTextCharacters(110, new char[10], 0, 9);
            fail(" java.lang.IndexOutOfBoundsException - if " +
                    "length < 0 or targetStart + length > length of target ");
        } catch (java.lang.IndexOutOfBoundsException e) {
        }
    }

    public void testGetTextCharactersLargeTrgOff() throws Exception {
        m_stream.next();
        try {
            m_stream.getTextCharacters(110, new char[10], 10, 9);
            fail(" java.lang.IndexOutOfBoundsException - if " +
                    "length < 0 or targetStart + length > length of target ");
        } catch (java.lang.IndexOutOfBoundsException e) {
        }
    }

    public void testGetTextCharactersLargeLen() throws Exception {
        m_stream.next();
        char[] buff = new char[9];
        try {
            int nCopied = m_stream.getTextCharacters(0, buff, 0, 30);
            assertEquals(nCopied, buff.length);
            fail(" java.lang.IndexOutOfBoundsException - if " +
                    "length < 0 or targetStart + length > length of target ");
        } catch (java.lang.IndexOutOfBoundsException e) {
        }

    }

    //off+len past end
    public void testGetTextCharactersLargeSum() throws Exception {
        m_stream.next();
        char[] buff = new char[9];
        try {
            int nCopied = m_stream.getTextCharacters(0, buff, 3, 10);
            fail(" java.lang.IndexOutOfBoundsException - if " +
                    "length < 0 or targetStart + length > length of target ");
        } catch (java.lang.IndexOutOfBoundsException e) {
        }

    }


    public void testGetText() throws Exception {
        try {
            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());
            m_stream.getText();
            fail("Illegal State");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
        String result = m_stream.getText();
        assertEquals(" some comment ", result);

        try {
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            m_stream.getText();
            fail("Illegal State");
        } catch (IllegalStateException e) {
        }


        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
        result = m_stream.getText();
        assertEquals("some text", result);

        m_stream.next();
        m_stream.next();//skip empty elt
        m_stream.next(); //end foo
        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
        result = m_stream.getText();
        assertEquals("\t", result);
        try {
            m_stream.next();
            m_stream.getText();
            fail("Illegal State");
        } catch (IllegalStateException e) {
        }
    }


    public void setUp() throws Exception {
        cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();

        //   cur.insertAttributeWithValue(new QName("foo.org", "at0", "pre"), "val0");
        cur.insertComment(" some comment ");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertAttribute("localName");
        cur.insertChars("some text");
        cur.insertElement("foo2");
        cur.toNextToken(); //close foo elt
        cur.insertChars("\t");


        cur.toStartDoc();
        m_stream = getStream(cur);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        if (m_stream != null)
            m_stream.close();
    }

    private XMLStreamReader m_stream;
    private XmlCursor cur;
}