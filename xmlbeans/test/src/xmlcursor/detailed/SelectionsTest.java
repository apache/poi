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

import junit.framework.*;
import junit.framework.Assert.*;

import java.io.*;

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;

import test.xbean.xmlcursor.cr196679.TestType;
import test.xbean.xmlcursor.cr196679.TestDocument;


/**
 *
 *
 */

public class SelectionsTest extends BasicCursorTestCase {

    static final String sXml="<foo><b>0</b><b>1</b><b>2</b><b attr=\"a3\">3</b><b>4</b><b>5</b><b>6</b></foo>";

    public SelectionsTest(String sName) {
	super(sName);
    }

     public static Test suite() {
        return new TestSuite(SelectionsTest.class);
    }

    //average case test
    public void testNormalCase()throws Exception{
	XmlCursor m_xc1=m_xo.newCursor();
	int nSelectionsCount=7;
	m_xc.selectPath("$this//a");
	assertEquals(false, m_xc.hasNextSelection());
	assertEquals(false, m_xc.toNextSelection());
	assertEquals(0, m_xc.getSelectionCount());

	 m_xc.selectPath("$this//b");
	 m_xc1.toFirstChild();
	 m_xc1.toFirstChild();
	 do{
	     m_xc1.addToSelection();
	 }while(m_xc1.toNextSibling());
	 assertEquals(nSelectionsCount, m_xc.getSelectionCount());
	 int i=0;
	 while(m_xc.hasNextSelection()){
	     m_xc.toNextSelection();
	     assertEquals("" + i, m_xc.getTextValue());
	     i++;
	 }
	 int j=0;
	 while(m_xc1.hasNextSelection()){
	      m_xc1.toSelection(j);
	      assertEquals("" + j, m_xc1.getTextValue());
	      j++;
	 }
	 assertEquals(nSelectionsCount,j);
	 assertEquals(nSelectionsCount,i);
    }

    public void testToSelectionIllegalIndex(){
	 m_xc.selectPath("$this//b");
	 int i=0;
	 boolean result=false;
     result=m_xc.toSelection(-1);
     assertEquals(result,false);

	 try{
	     result=m_xc.toSelection(m_xc.getSelectionCount()+1);
	     if (result)
		 fail(" Index > num selections");
	 }catch(IllegalStateException e){}

	 if (result && (i>0)) fail(" Index <0 ");

    }

    public void testClearSelections(){

	int nSelectionsCount=7;
	m_xc.selectPath("$this//b");
        m_xc.toSelection(0);
        m_xc.clearSelections();
        assertEquals("<b>0</b>",m_xc.xmlText());

    }

    public void testCR196679() throws Exception
  {
      TestDocument testDoc = null;
      String input="<ns:test xmlns:ns=\"http://xbean.test/xmlcursor/CR196679\">\n" +
              "  <ns:name>myTest</ns:name>" +
              "  <ns:value>5</ns:value>" +
              "  </ns:test>";
      testDoc = TestDocument.Factory.parse(input);
      TestType test = testDoc.getTest();

      String queryName =
        "declare namespace ns='http://xbean.test/xmlcursor/CR196679'" +
        "$this/ns:name";

      String queryValue =
        "declare namespace ns='http://xbean.test/xmlcursor/CR196679'" +
        "$this/ns:value";

      XmlCursor cursor = test.newCursor();
      cursor.push();
      cursor.selectPath(queryName);
      cursor.toNextSelection();

      assertEquals("myTest",cursor.getTextValue());

      cursor.pop();
      cursor.selectPath(queryValue);
      cursor.toNextSelection();

      assertEquals("5",cursor.getTextValue());//expected output is value=5

      cursor.dispose();

  }
    public void setUp()throws Exception{
	m_xo=XmlObject.Factory.parse(sXml);
	m_xc= m_xo.newCursor();
    }
}
