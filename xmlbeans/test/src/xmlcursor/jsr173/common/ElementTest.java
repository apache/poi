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

import javax.xml.stream.XMLStreamConstants;

import junit.framework.*;
import junit.framework.Assert.*;

/**
 *
  *
 */
public abstract class ElementTest extends TestCase {

     public abstract XMLStreamReader getStream(XmlCursor c)throws Exception;
    public void testGetElementText() throws Exception {
        cur.toFirstChild(); //first element?
        m_stream = getStream(cur);

        assertEquals("some text", m_stream.getElementText());
    }

    public void testGetElementTextEmptyElt() throws Exception {
        cur.toFirstChild(); //first element?
        cur.toNextSibling();
        m_stream = getStream( cur );

        assertEquals("", m_stream.getElementText());
    }

    public void testGetElementTextMixedContent() throws Exception {
        cur.toFirstChild(); //first element?
        cur.toNextSibling();
        cur.toNextSibling();
        m_stream =getStream( cur );
        assertEquals(new QName("foo.org", "foo", ""), m_stream.getName());
        try {
            assertEquals("\thooa", m_stream.getElementText());
            fail("Mixed content needs exception");
        }
        catch (javax.xml.stream.XMLStreamException e) {
        }

        //mixed content txt1, PI, COMMENT,txt2:
        //should coalesce txt1 & txt2
        cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();
        cur.beginElement("foo");
        cur.insertChars("  \n ");
        cur.insertComment("My comment");
        cur.insertProcInst("xml-stylesheet", "http://foobar");
        cur.insertChars("txt1\t");
        cur.toStartDoc();
         m_stream = getStream( cur );
         assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next() );
        assertEquals("  \n txt1\t", m_stream.getElementText());


    }

    public void testGetNameAtStartElt() throws Exception {
        cur.toFirstChild(); //first element
        m_stream = getStream( cur );
        assertEquals(new QName("foo.org", "foo", ""), m_stream.getName());

    }

    public void testGetNameAtEndElt() throws Exception {
        cur.toFirstChild();
        m_stream = getStream( cur );
        m_stream.next();
        assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
        assertEquals(new QName("foo.org", "foo", ""), m_stream.getName());
    }

    public void testHasName() throws Exception{

        m_stream = getStream( cur );
        m_stream.next();
         m_stream.next();
         assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertTrue( m_stream.hasName() );

    }

    //call at a bad place..here just attr but should exhaust all
    public void testGetNameIllegal() throws Exception{
        cur.toNextToken(); //attr
         m_stream = getStream( cur );
        try {
            m_stream.getName();
            fail("getName illegal pos");
        }
        catch (java.lang.IllegalStateException e) {
        }

        assertFalse( m_stream.hasName() );
    }

    public void setUp() throws Exception {
        cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();

        cur.insertAttributeWithValue(new QName("foo.org", "at0", "pre"),
                "val0");
        cur.insertNamespace("pre", "foons.bar.org");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertAttribute("localName");
        cur.insertChars("some text");
        cur.toNextToken();
        cur.toNextToken();//end elt
        cur.insertElement("foo1");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertChars("\t");
        cur.insertElement("foo2");
        cur.insertChars("hooa");

        cur.toStartDoc();

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