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
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;


/**
 *
 *
 */
public class ToChildTest extends BasicCursorTestCase {

    String sDoc="<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>";

    int nChildCount=2; //num children if TEXT is a child
    public ToChildTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ToChildTest.class);
    }
    /**
     * Testing toChild(String)
     * Cases:
     *      non-existing name
     *      2 children with same name
     *      nested child with same name
     *      Child of TEXT
     */

    public void testToChildNonExisting()throws Exception{
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	assertEquals(false,m_xc.toChild("yana"));
    }
    public void testToChildInvalidName()throws Exception{
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
    try{
        m_xc.toChild("");
        fail(" Name is invalid");
    }catch (java.lang.IllegalArgumentException e){}

    }

    public void testToChildNull()throws Exception{
	String sNull=null;
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	try{
	    assertEquals(false,m_xc.toChild(sNull));
	    fail("toChild with Null localName");
	}catch (IllegalArgumentException e){}
    }

    public void testNameCollision()throws Exception{
	sDoc="<foo><bar>txt0</bar><bar>txt1</bar></foo>";
	String sExpectedValue="<bar>txt0</bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild("bar"));
	assertEquals(sExpectedValue,m_xc.xmlText());
    }

    public void testSameNameDescendant()throws Exception{
	sDoc="<foo><bar><bar>txt0<bar/></bar></bar><bar>txt1</bar></foo>";
	String sExpectedValue="<bar><bar>txt0<bar/></bar></bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild("bar"));
	assertEquals(sExpectedValue,m_xc.xmlText());
    }

    public void testTextChild()throws Exception{
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	toNextTokenOfType(m_xc,TokenType.TEXT);
	assertEquals(false,m_xc.toChild("bar"));
    }

    /**
     * toChild(String,String)
     * Cases:
     *      non-existing ns, existing name
     *      non-existing name, existing ns
     *      2 children with same name
     *      2 children with same name, diff ns
     *      2 children with diff name, same ns
     *      nested child with same name & ns
     */


    public void testNullNS()throws Exception{
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	String sExpectedResult="<bar>text</bar>";
	assertEquals(true,m_xc.toChild(null,"bar"));
	assertEquals(sExpectedResult,m_xc.xmlText());
    }

    public void testNullName()throws Exception{
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	try{
	    assertEquals(false,m_xc.toChild("uri:foo.org",null));
	    fail("toChild(uri,localname) with Null localName");
	}catch (IllegalArgumentException e){}
    }

    public void testNamespaceOKNameInvalid()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(false,m_xc.toChild("fo","test"));
    }

    public void testNamespaceInvalidNameOK()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	String sExpectedResult="<bar>text</bar>";
	assertEquals(false,m_xc.toChild("bar","bar"));
    }

    public void testNormalCase()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>";
	String sExpectedResult="<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild("uri:foo.org","bar"));
	assertEquals(sExpectedResult,m_xc.xmlText());
    }

    public void testUriNameCollision()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><fo:bar>txt1</fo:bar></foo>";
	String sExpectedValue="<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild("uri:foo.org","bar"));
	assertEquals(sExpectedValue,m_xc.xmlText());
    }



    //same URI diff names
    public void testFakeNameCollision()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>";
	String sExpectedValue="<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild("uri:foo.org","bar"));
	assertEquals(sExpectedValue,m_xc.xmlText());
    }

    //diff URI same names
     public void testFakeNameCollision3()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>";
	String sExpectedValue="<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild("uri:foo.org","bar"));
	assertEquals(sExpectedValue,m_xc.xmlText());
    }


    public void  testSameNameDescendant1()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><bar><fo:bar>txt0<bar/></fo:bar></bar><bar>txt1</bar></foo>";

	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(false,m_xc.toChild("uri:foo.org","bar"));
    }

     public void testSameNameDescendant2()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><bar><fo:bar>txt0<bar/></fo:bar></bar><bar>txt1</bar><fo:bar>txt1</fo:bar></foo>";
	String sExpectedValue="<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild("uri:foo.org","bar"));
	assertEquals(sExpectedValue,m_xc.xmlText());
    }


    /**
     * toChild(int)
     * Cases:
     *       i<0
     *       i>numChildren
     *       i=0, numChildren=0
     */

    public void testNegativeIndex()throws Exception{
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	assertEquals(false,m_xc.toChild(-1));
    }

    public void testIndexOKFirst()throws Exception{
	String sExpectedValue="<bar>text</bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild(0));//text is not children
	assertEquals(sExpectedValue,m_xc.xmlText());
    }

     public void testIndexOKLast()throws Exception{
	String sExpectedValue="<char>zap<dar>wap</dar><ear>yap</ear></char>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild(nChildCount-1));
	assertEquals(sExpectedValue,m_xc.xmlText());
	m_xc.toParent();
	m_xc.toLastChild();
	assertEquals(sExpectedValue,m_xc.xmlText());
    }

     public void testLargeIndex()throws Exception{
	 m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(false,m_xc.toChild(20));

    }
    public void  testInd0Count0()throws Exception{
	sDoc="<foo/>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(false,m_xc.toChild(0));
    }


    /**
     * toChild(QName,int)
     * Cases:
     *       QName dne,
     *       QName OK, i OK;i >numChildren;i<0
     *       Name collision, i=1;i>numChildren
     *       Siblings and a child with same qname, ask for 2nd sibling
     */

    public void testToChildQNameDNE0()throws Exception{
	QName searchVal=new QName("fake:uri","bar");
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(false,m_xc.toChild(searchVal,1));
    }

    public void testToChildQNameDNE1()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>";
	QName searchVal=new QName("uri:foo.org","bar","pre");
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(false,m_xc.toChild(searchVal,1));
    }

    public void testToChildQNameOKIndexOK()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>";
	QName searchVal=new QName("uri:foo.org","bar","fo");
	String sExpectedValue="<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild(searchVal,0));
	assertEquals(sExpectedValue,m_xc.xmlText());
	assertEquals(false,m_xc.toChild(searchVal,1));
	assertEquals(false,m_xc.toChild(searchVal,-1));
    }

    public void testQNameNameCollision()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>";
	nChildCount=2;
	QName searchVal=new QName("uri:foo.org","bar","fo");
	String sExpectedValue="<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	assertEquals(true,m_xc.toChild(searchVal,0));
	assertEquals(sExpectedValue,m_xc.xmlText());
	int nInvalidCount=2;
	if(nInvalidCount>=nChildCount)
	    assertEquals(false,m_xc.toChild(searchVal,nInvalidCount));
	else fail("Broken Test");
    }


    public void testFakeQNameCollision()throws Exception{
	sDoc="<foo xmlns:fo=\"uri:foo.org\" xmlns:fo2=\"uri:foo.org\"><fo2:bar>txt0</fo2:bar><fo:bar>txt1</fo:bar></foo>";
	String sExpectedValue="<fo2:bar xmlns:fo=\"uri:foo.org\" xmlns:fo2=\"uri:foo.org\">txt0</fo2:bar>";
	m_xc = XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	QName searchVal=new QName("uri:foo.org","bar","fo");
	assertEquals(true,m_xc.toChild(searchVal,0));
	assertEquals(sExpectedValue,m_xc.xmlText());
    }
}
