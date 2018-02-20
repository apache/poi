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

package xmlcursor.xpath.common;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor.TokenType;

import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.*;


/**
 * Verifies XPath nodetest functions
 */
public class XPathNodetestTest extends BasicCursorTestCase {



    public XPathNodetestTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(XPathNodetestTest.class);
    }


     static String fixPath(String path){
        return "."+path;
     }


    public void testAllNodes(){
	//e.g //A/B/*: tested by Zvon
    }

    public void testComment()throws Exception {
	String sXml=Common.XML_FOO_NS_PREFIX;
	m_xc= XmlObject.Factory.parse(sXml).newCursor();
	String sExpected=Common.XMLFRAG_BEGINTAG+"<!-- the 'price' element's namespace is http://ecommerce.org/schema -->"+Common.XMLFRAG_ENDTAG;//the comment string
	String sXPath="//comment()";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(m_xc.xmlText(),sExpected );
    }

    public void testNode()throws Exception{
	String sInput="<foo> <node>foo</node>txt</foo>";
	m_xc= XmlObject.Factory.parse(sInput).newCursor();
	String sXPath="//foo/node()";
	String[] sExpected=new String[]{Common.XMLFRAG_BEGINTAG+" "+Common.XMLFRAG_ENDTAG,"<node>foo</node>",Common.XMLFRAG_BEGINTAG+"txt"+Common.XMLFRAG_ENDTAG};
	m_xc.selectPath(fixPath(sXPath));
	int i=0;
	if (m_xc.getSelectionCount()!=sExpected.length)
	    fail("node() failed");
	while(m_xc.hasNextSelection()){
	    m_xc.toNextSelection();
	    assertEquals(m_xc.xmlText(),sExpected[i++]);
	}

    }

    public void testPI()throws Exception{
	String sInput=Common.XML_FOO_PROCINST;
	m_xc= XmlObject.Factory.parse(sInput).newCursor();
	String sXPath1="//processing-instruction()";
	String sXPath2="//processing-instruction(\"xml-stylesheet\")";
	String sXPath3="//processing-instruction(\"xsl\")";
	String sExpected1=Common.XMLFRAG_BEGINTAG+"<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?>"+Common.XMLFRAG_ENDTAG;
	String sExpected2="";
	m_xc.selectPath(fixPath(sXPath1));
	assertEquals(m_xc.getSelectionCount(),1);
	m_xc.toNextSelection();
	assertEquals(m_xc.xmlText(),sExpected1);


	m_xc.clearSelections();
	m_xc.selectPath(fixPath(sXPath2));
	assertEquals(m_xc.xmlText(),sExpected1);

	m_xc.clearSelections();
	//shouldn't select any nodes
	m_xc.selectPath(fixPath(sXPath3));
	assertEquals(m_xc.getSelectionCount(),0);

    }

    public void testText()throws Exception{
	String sInput="<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><br>foo<foo>text</foo></br>";
	m_xc= XmlObject.Factory.parse(sInput).newCursor();
	String sXPath="//text()";
	String sExpected1=Common.XMLFRAG_BEGINTAG+"foo"+Common.XMLFRAG_ENDTAG;
	String sExpected2=Common.XMLFRAG_BEGINTAG+"text"+Common.XMLFRAG_ENDTAG;
	m_xc.selectPath(sXPath);
	assertEquals(m_xc.getSelectionCount(),2);
	m_xc.toNextSelection();
	assertEquals(m_xc.xmlText(),sExpected1);
	m_xc.toNextSelection();
	assertEquals(m_xc.xmlText(),sExpected2);
    }

    public void testTextObject()throws Exception{
	String sInput="<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><br>foo<foo>text</foo></br>";
	m_xo= XmlObject.Factory.parse(sInput);
	String sXPath="//text()";
	String sExpected1=Common.XMLFRAG_BEGINTAG+"foo"+Common.XMLFRAG_ENDTAG;
	String sExpected2=Common.XMLFRAG_BEGINTAG+"text"+Common.XMLFRAG_ENDTAG;
	XmlObject[] res=m_xo.selectPath(sXPath);
	assertEquals(res.length,2);
	assertEquals(res[0].xmlText(),sExpected1);
	assertEquals(res[1].xmlText(),sExpected2);
    }
}


