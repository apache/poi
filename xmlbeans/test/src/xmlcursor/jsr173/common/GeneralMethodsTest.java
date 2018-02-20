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
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlDocumentProperties;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamConstants;

import junit.framework.*;


//

/**
 * Methods tested
 * close()
 * getCharacterEncodingScheme()
 * getEncoding()
 * getLocation
 * getProperty
 * getVersion
 * isStandalone
 * require
 * standaloneSet
 *
 *
  *
 */
public abstract class GeneralMethodsTest extends TestCase {

     public abstract XMLStreamReader getStream(XmlCursor c)
             throws Exception;
    public void testClose() throws Exception {
        m_stream.close();


        m_stream.next();


    }

    public void testAll() throws Exception {
          m_stream.next();
         m_stream.next();
        assertEquals("utf-8", m_stream.getCharacterEncodingScheme());
       //  Eric says this refers to actual encoding, not xml def
        //  assertEquals("utf-8", m_stream.getEncoding());

         assertEquals(null, m_stream1.getCharacterEncodingScheme());
        assertEquals(null, m_stream1.getEncoding());
        //TODO: why is this still -1???
        Location l = m_stream.getLocation();
        assertEquals(-1, l.getCharacterOffset());
        assertEquals(-1, l.getColumnNumber());
        assertEquals(-1, l.getLineNumber());
        assertEquals(null, l.getPublicId());
        assertEquals(null, l.getSystemId());


       // m_stream.getProperty("");
        m_stream.getVersion();
//        m_stream.isStandalone();
        assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.getEventType() );
        //only elements can have localnames
         try{
               m_stream.require(10, "", "at1");
            fail("IllegalStateException needed here");
        }catch (IllegalStateException e){}


          m_stream.next();
         assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next() ) ;
          m_stream.require(1, "foo.org", "foo");
        try{
                     m_stream.require(10, "", "");
                   fail("XMLStreamException needed here");
               }catch (XMLStreamException e){}


//        m_stream.standaloneSet();
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
        XmlDocumentProperties opt=cur.documentProperties();

        m_stream1 = getStream( cur );

         opt.setEncoding("utf-8");
        m_stream=  getStream( cur );

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        if (m_stream != null)
            m_stream.close();
    }

    private XMLStreamReader m_stream;
     private XMLStreamReader m_stream1;
    private XmlCursor cur;
    private XmlOptions opt;


}