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
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlOptions;

import junit.framework.*;
import junit.framework.Assert.*;

/**
 *
 *
 */
public abstract class AttributeTest extends TestCase {

    int indexMethods=6;
    public abstract XMLStreamReader getStream(XmlCursor c)throws Exception;
     public AttributeTest(String s) {
        super(s);
    }

    public void testAttrEvent() throws Exception {
        cur.toNextToken();
        m_stream =getStream(cur);
        assertEquals( XMLStreamConstants.ATTRIBUTE, m_stream.getEventType() );
        assertEquals(1, m_stream.getAttributeCount());
        assertEquals(m_stream.getAttributeValue(0),
        m_stream.getAttributeValue("foo.org", "at0"));

          assertFalse(m_stream.hasNext());

    }
   public void testAttrMethodsAtAttr() throws Exception{

        //2 attrs under the doc
//        assertEquals(2, m_stream.getAttributeCount());

         cur.toNextToken();
        m_stream= getStream(cur);;
        //move 2 first attr
       assertEquals( XMLStreamConstants.ATTRIBUTE, m_stream.getEventType() );
        assertEquals(1, m_stream.getAttributeCount());

        assertEquals(m_stream.getAttributeValue(0),
                m_stream.getAttributeValue("foo.org", "at0"));

        //Below methods tested at index 0 and last at index tests
        //getAttributeLocalName(int)
        //getAttributeName(int)
        //getAttributeNamespace(int)
        //getAttributePrefix(int)
        //getAttributeType(int)
        //getAttributeValue(int)


    }

    public void testAttrMethodsAtStartElt()  throws Exception{
        cur.toFirstChild();
        cur.toNextSibling();
        m_stream = getStream(cur);
        assertEquals(1, m_stream.getAttributeCount());
        assertTrue(m_stream.isStartElement());
        assertEquals(new QName("foo.org", "foo", ""), m_stream.getName());
        assertEquals(m_stream.getAttributeValue(0), "");
        assertEquals(m_stream.getAttributeValue(0),
                m_stream.getAttributeValue("", "localName"));
    }

    private void assertIllegalState1() {
        try {
            m_stream.getAttributeCount();
            fail("Illegal State");
        }
        catch (java.lang.IllegalStateException e) {
        }
    }

    private void assertIllegalState2() {
        try {
            m_stream.getAttributeValue(0);
            fail("Illegal State");
        }
        catch (java.lang.IllegalStateException e) {
        }
    }

    public void testAttrMethodsAtNamespace() throws Exception {
        cur.toNextToken();
        cur.toNextToken();
        assertEquals (XmlCursor.TokenType.NAMESPACE, cur.toNextToken());
        m_stream = getStream(cur);

        assertIllegalState1();
        assertIllegalState2();
//         assertEquals(1,m_stream.getNamespaceCount());
//         assertEquals("foons.bar.org",m_stream.getNamespaceURI(0));
//         assertEquals(m_stream.getNamespaceURI(0),m_stream.getAttributeValue("","localName"));
//
    }

//
//    java.lang.IllegalStateException - if this is not a START_ELEMENT or ATTRIBUTE
//

    public void testAttrMethodsAtEndElt()throws Exception  {
        cur.toFirstChild();
        cur.toNextSibling();
        cur.toNextToken();
        cur.toNextToken();
        assertEquals(XmlCursor.TokenType.END, cur.toNextToken()); //toEnd
        m_stream = getStream(cur);
        assertIllegalState1();
        assertIllegalState2();
    }

    public void testAttrMethodsAtEndDoc() throws Exception  {
        cur.toFirstChild();
        cur.toNextSibling();
        cur.toNextToken();
        cur.toNextToken();
        cur.toNextToken();
        cur.toNextToken();
        assertEquals(XmlCursor.TokenType.ENDDOC, cur.toNextToken());
        m_stream = getStream(cur);
        assertIllegalState1();
        assertIllegalState2();
    }

    public void testAttrMethodstAtText() throws Exception {
        cur.toFirstChild();
        cur.toNextSibling();
        cur.toNextToken();
        assertEquals(XmlCursor.TokenType.TEXT, cur.toNextToken()); //text
        m_stream = getStream(cur);
        assertIllegalState1();
        assertIllegalState2();
    }

    public void testAttrMethodstAtPI() throws Exception {
        cur.toFirstChild();
        cur.toNextSibling();
        cur.toNextToken();
        cur.toNextToken();
        cur.toNextToken();
        assertEquals(XmlCursor.TokenType.PROCINST, cur.toNextToken());
        m_stream = getStream(cur);
        assertIllegalState1();
        assertIllegalState2();
    }

   /**
     * verify index correctness for all index methods
     * tested w/ cursor positioned at first attr
     * //getAttributeLocalName(int)
     * //getAttributeName(int)
     * //getAttributeNamespace(int)
     * //getAttributePrefix(int)
     * //getAttributeType(int)
     * //getAttributeValue(int)
     */

    public void testAttrMethodsNegIndex() throws Exception {

        int cnt = 0;
        try {
            m_stream.getAttributeLocalName(-1);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributeName(-1);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributeNamespace(-1);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributePrefix(-1);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributeType(-1);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributeValue(-1);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }

          assertEquals( "A negative error wasn't thrown", indexMethods , cnt);
    }

    public void testAttrMethodsLargeIndex()
            throws XMLStreamException {

        int cnt = 0;
         int pos=-1;
        try {
             m_stream.next();
        pos=m_stream.getAttributeCount();
            m_stream.getAttributeLocalName(pos);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributeName(pos);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributeNamespace(pos);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributePrefix(pos);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributeType(pos);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }
        try {
            m_stream.getAttributeValue(pos);
        }
        catch (java.lang.IndexOutOfBoundsException e) {
            cnt++;
        }

          assertEquals( "A negative error wasn't thrown", indexMethods, cnt);
    }

    public void testAttrMethods0Index() throws Exception{
         assertEquals( XMLStreamConstants.START_DOCUMENT, m_stream.getEventType() );

        assertEquals( XMLStreamConstants.ATTRIBUTE, m_stream.next() );
        assertEquals(1, m_stream.getAttributeCount());

        assertEquals("val0", m_stream.getAttributeValue(0));

        assertEquals( XMLStreamConstants.ATTRIBUTE, m_stream.next() );

        assertEquals("val1", m_stream.getAttributeValue(0));
        //why does this crash here????
        assertEquals( XMLStreamConstants.NAMESPACE,m_stream.next()); //ns
        m_stream.next(); //elt
       assertEquals("", m_stream.getAttributeValue(0));

    }

    //NOTHING to do; eric always emits one event per attr=>
    //getAttributeCount is always 1
    public void testAttrMethodsLastIndex() {

    }
   public void testIsAttributeSpecified() throws Exception {
         assertEquals( XMLStreamConstants.START_DOCUMENT,
                 m_stream.getEventType() );
       try{
           m_stream.isAttributeSpecified(0);
           fail("Bad state");
       }catch (IllegalStateException e){}

        assertEquals( XMLStreamConstants.ATTRIBUTE, m_stream.next() );
        assertEquals(false, m_stream.isAttributeSpecified(0));

        try{
           m_stream.isAttributeSpecified(-1);
           fail("Bad state");
       }catch (java.lang.IndexOutOfBoundsException e){}

       try{
           m_stream.isAttributeSpecified(2);
           fail("Bad state");
       }catch (java.lang.IndexOutOfBoundsException e){}

   }

    public void setUp() throws Exception {
        cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();

        cur.insertAttributeWithValue(new QName("foo.org", "at0", "pre"),
                "val0");
        cur.insertAttributeWithValue(new QName("", "at1", "pre"), "val1");
        cur.insertNamespace("pre", "foons.bar.org");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertAttribute("localName");
        cur.insertChars("some text");
        cur.toNextToken();
        cur.toNextToken();//end elt
        cur.insertProcInst("xml-stylesheet", "http://foobar");

        cur.toStartDoc();
        m_stream=getStream(cur);
                //cur.newXMLStreamReader();

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        if (m_stream != null)
            m_stream.close();
    }

     XMLStreamReader m_stream;
     XmlCursor cur;

}