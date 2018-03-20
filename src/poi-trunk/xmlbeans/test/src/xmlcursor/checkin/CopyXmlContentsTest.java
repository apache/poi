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


import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;



/**
 *
 *
 */
public class CopyXmlContentsTest extends BasicCursorTestCase {
    public CopyXmlContentsTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(CopyXmlContentsTest.class);
    }

     public void testCopyToNull() throws Exception {

	 m_xc = XmlObject.Factory.parse(Common.XML_FOO_DIGITS).newCursor();
	 toNextTokenOfType(m_xc, TokenType.TEXT);
	 try {
	     m_xc.copyXmlContents(null);
	     fail("Expected IllegalArgumentException.  Can't copy to foreign document");
	 } catch (IllegalArgumentException ise) {
	 }
     }
     public void testCopyDifferentStoresLoadedByParseInvalidDest() throws Exception {
        String sDoc1=Common.XML_FOO_DIGITS;
	String sDoc2=Common.XML_FOO_2ATTR_TEXT;
        m_xc = XmlObject.Factory.parse(sDoc1).newCursor();
        XmlCursor xc1 = XmlObject.Factory.parse(sDoc2).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        toNextTokenOfType(xc1, TokenType.START);
	try{
	    xc1.dispose();
	    m_xc.copyXmlContents(xc1);
	    fail("Expected IllegalStateException. Destination cursor was disposed ");
	} catch (IllegalStateException ise) {
        }
    }
    public void testCopyDifferentStoresLoadedByParse() throws Exception {
        String sDoc1=Common.XML_FOO_DIGITS;
	String sDoc2=Common.XML_FOO_2ATTR_TEXT;
        m_xc = XmlObject.Factory.parse(sDoc1).newCursor();
        XmlCursor xc1 = XmlObject.Factory.parse(sDoc2).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        toNextTokenOfType(xc1, TokenType.TEXT);
	m_xc.copyXmlContents(xc1);
        xc1.toParent();
        // verify xc1
        assertEquals("01234text", xc1.getTextValue());
        xc1.dispose();

	System.out.println("test "+m_xc.xmlText());
        // verify m_xc
	toNextTokenOfType(m_xc, TokenType.TEXT); //get to the text
        assertEquals("01234", m_xc.getChars());
    }

    /* the source is not a container*/
    public void testCopyDifferentStoresLoadedByParseInvalidSrc() throws Exception {
        String sDoc1=Common.XML_FOO_DIGITS;
	String sDoc2=Common.XML_FOO_2ATTR_TEXT;
        m_xc = XmlObject.Factory.parse(sDoc1).newCursor();
        XmlCursor xc1 = XmlObject.Factory.parse(sDoc2).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc1, TokenType.START);
        boolean result=m_xc.copyXmlContents(xc1);
	assertEquals(false, result);

    }

    /*public void testCopyOntoItself() throws Exception {
        String sDoc1=Common.XML_FOO_DIGITS;
	m_xc = XmlObject.Factory.parse(sDoc1).newCursor();
	toNextTokenOfType(m_xc, TokenType.TEXT);
	String sExpectedXml=m_xc.xmlText();
	boolean result=m_xc.copyXmlContents(m_xc);

	//cursor is left immediately before copied material
	assertEquals(sExpectedXml,m_xc.getTextValue());

    }
    */
    public void testCopySelf() throws Exception {
	String sDoc1=Common.XML_FOO_DIGITS;
	m_xo = XmlObject.Factory.parse(sDoc1);
	m_xc=m_xo.newCursor();

	toNextTokenOfType(m_xc, TokenType.START);
	String sExpectedXml="<xml-fragment>01234<foo attr0=\"val0\" xmlns=\"http://www.foo.org\">01234</foo></xml-fragment>";
	boolean result=m_xc.copyXmlContents(m_xc);

	//cursor is left immediately before copied material
	m_xc.toStartDoc();
	//assertEquals(sExpectedXml.length(),m_xc.xmlText().length());
	assertEquals(sExpectedXml,m_xc.xmlText());
    }


    /**
       Can't really copy the whole doc, so copy all the contents
       into a false root */
     public void testCopyWholeDoc() throws Exception {
        String sDoc1=Common.XML_FOO_BAR_WS_TEXT;
	String sDoc2="<root></root>";
	m_xc = XmlObject.Factory.parse(sDoc1).newCursor();
	XmlCursor xc1 = XmlObject.Factory.parse(sDoc2).newCursor();
	xc1.toFirstChild();
	String sExpectedXml=m_xc.xmlText();
	boolean result=m_xc.copyXmlContents(xc1);
	toPrevTokenOfType(xc1,TokenType.STARTDOC);
	toNextTokenOfType(xc1,TokenType.START);
	assertEquals(sExpectedXml,xc1.xmlText());

	//namespaces are not copied
	sDoc1=Common.XML_FOO_NS_PREFIX;
	sDoc2="<root></root>";
	m_xc = XmlObject.Factory.parse(sDoc1).newCursor();
	xc1 = XmlObject.Factory.parse(sDoc2).newCursor();
	sExpectedXml=m_xc.xmlText();
	xc1.toFirstChild();

	result=m_xc.copyXmlContents(xc1);
	toPrevTokenOfType(xc1,TokenType.STARTDOC);
	assertEquals(false,sExpectedXml.equals(xc1.xmlText()));

	//attributes are not copied
	sDoc1=Common.XML_FOO_2ATTR;
	sDoc2="<root></root>";
	m_xc = XmlObject.Factory.parse(sDoc1).newCursor();
	xc1 = XmlObject.Factory.parse(sDoc2).newCursor();
	sExpectedXml=m_xc.xmlText();
	xc1.toFirstChild();

	result=m_xc.copyXmlContents(xc1);
	toPrevTokenOfType(xc1,TokenType.STARTDOC);
	assertEquals(false,sExpectedXml.equals(xc1.xmlText()));

    }

    public static void main(String[] rgs){
	try{
	    // (new CopyXmlContentsTest("")).testCopyOntoItself();
	    (new CopyXmlContentsTest("")).testCopyWholeDoc();
	}catch(Exception e){System.err.println(e.getMessage());}
    }

}
