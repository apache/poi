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
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;


/**
 *
 *
 */

public class IsAtSamePositionAsTest extends BasicCursorTestCase{

    static String sDoc=Common.XML_FOO_DIGITS;

    public IsAtSamePositionAsTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(IsAtSamePositionAsTest.class);
    }
    public void testNormalCase(){
	XmlCursor m_xc1=m_xo.newCursor();; //parse
	m_xc.toFirstChild();
	m_xc1.toFirstChild();
	assertEquals(true,m_xc.isAtSamePositionAs(m_xc1));
    }

    public void testSamePosDiffDoc()throws Exception{
	XmlCursor m_xc1=XmlObject.Factory.parse(sDoc).newCursor();
	m_xc.toFirstChild();
	m_xc1.toFirstChild();
	try{
	    assertEquals(false,m_xc.isAtSamePositionAs(m_xc1));
	    fail("Cursors are in different docs");
	}catch (IllegalArgumentException e){}
    }
    public void testDiffPosSameDoc()throws Exception{
	XmlCursor m_xc1=m_xo.newCursor();
	m_xc.toFirstChild();
	m_xc1.toFirstChild();
	m_xc1.toFirstAttribute();
	assertEquals(false,m_xc.isAtSamePositionAs(m_xc1));
    }

    public void testNull(){
	 try {
	    assertEquals(false,m_xc.isAtSamePositionAs(null));
	    fail("Other cursor is Null");
	 }catch(Exception e){
	 }
    }

    public void testSelf(){
	m_xc.toFirstChild();
	assertEquals(true,m_xc.isAtSamePositionAs(m_xc));
    }

    public void setUp()throws Exception{
	m_xo=XmlObject.Factory.parse(sDoc);
	m_xc=m_xo.newCursor();
    }

  public static void main(String[] rgs){
      try{
	  IsAtSamePositionAsTest t=new IsAtSamePositionAsTest("");
	   t.setUp();
	   t.testNormalCase();
	  }catch (Exception e){
	    System.err.println("Error "+e.getMessage());
	    e.printStackTrace();
	}
  }

}
