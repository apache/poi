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

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;


/**
 *
 *
 */
public class InsertAttributeTest2 extends BasicCursorTestCase {


    static String sDoc=Common.XML_FOO_TEXT;

    public InsertAttributeTest2(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(InsertAttributeTest2.class);
    }



    public void testNormalCase(){
	for (int i=0;i<50;i++){
	    m_xc.insertAttribute("at"+i,"com.bea.foo");
	    //System.out.println(i);
	}
	toPrevTokenOfType(m_xc, TokenType.ATTR);

	int i=0;
	do{
	    i++;
        //System.out.println(m_xc.xmlText());
	}while(m_xc.toPrevAttribute());

	assertEquals(i,50);
    }

    public void testIllegalCursorPos(){
	//position curor at text
	toNextTokenOfType(m_xc,XmlCursor.TokenType.END);
	try{
	    m_xc.insertAttribute("at","com.bea.foo");
	    fail("Shoild not be able to insert at attr here ");
	}catch(Exception e){}
    }
    /**
       No xml tag can contain 2 attrib such that:
       1. have identical names, or
       2. have qualified names with the same local part and with prefixes which have been bound to namespace names that are identical.

       ** According to Eric he will perform the insert but
       * check upon serialization that only the first token with a given name is printed
       */
    public void testLocalNameCollision(){
	m_xc.insertAttributeWithValue("at","v1");
	m_xc.insertAttributeWithValue("at","v2");
	toPrevTokenOfType(m_xc,TokenType.START);
	m_xc.toFirstAttribute();
	assertEquals(m_xc.getName().getLocalPart(),"at");
	assertEquals(true, m_xc.toNextAttribute());
	assertEquals(m_xc.getName().getLocalPart(),"at");


    }

    /**
     * The idea was to try to force the following:
     * <test xmlns:n1="foo.org" xmlns:n2="foo.org"><bad n1:a="foo" n2:a="bar"/>
     * which would be illegal
     * This test case is not necessary: The implementation re-writes this as
     * xmlns:com="..." and com:at0,  com:at1
     * it seems impossible to force a binding of the same URI with two
     * different prefixes
     */
    /*
      public void testUriCollision(){
      m_xc.insertAttribute("at0","com.bea.foo");
      try{
      m_xc.insertAttribute("at1","com.bea.foo");
      toPrevTokenOfType(m_xc,TokenType.START);
      System.out.println(m_xc.xmlText());
      fail("Should not be able to insert at attr with colliding name ");
      }catch(Exception e){}
      }
    */
    public void testUriLocalNameOK(){
	m_xc.insertAttribute("at","");
	m_xc.insertAttribute("at","com.bea.foo");
	toPrevTokenOfType(m_xc,XmlCursor.TokenType.START);
	m_xc.toFirstAttribute();
	int i=1;
//	System.out.println(m_xc.xmlText());
	while(m_xc.toNextAttribute()) i++;
	assertEquals(i,2);
    }

    public void testUriNull(){
	m_xc.insertAttribute("at",null);
	toPrevTokenOfType(m_xc, TokenType.ATTR);
	assertEquals(m_xc.getName(),new QName("at"));
    }
    public void testLocalnameNull(){
	try{
	    m_xc.insertAttribute(null,"");
	    fail("Null Localname");
	}catch(Exception e){}
    }
    public void testUriEmpty(){
	m_xc.insertAttribute("myat","");
	toPrevTokenOfType(m_xc, TokenType.START);
	m_xc.toFirstAttribute();
	assertEquals(m_xc.getName(),new QName(null,"myat"));
    }
    public void testLocalnameEmpty(){
	try{
	    m_xc.insertAttribute("","");
	    fail("No Localname");
	}catch(Exception e){}
    }

    public void testInsertAttributeWithValue(){
	StringBuffer sb=new StringBuffer();
	String value0="test"+"\n\t\r";
	String value1="'QuotedText'";
	String value2="\"QuotedText2\"";

	int nStressBound=20000;//Integer.MAX_VALUE
	for (int i=0;i<nStressBound;i++)
	    sb.append('a');
	m_xc.insertAttributeWithValue("at0",value0);
	m_xc.insertAttributeWithValue("at1",value1);
	m_xc.insertAttributeWithValue("at2",value2);
	m_xc.insertAttributeWithValue("at3",sb.toString());

	toPrevTokenOfType(m_xc,TokenType.START);

	assertEquals(m_xc.getAttributeText(new QName("at3")).length(),nStressBound);
	assertEquals(m_xc.getAttributeText(new QName("at2")),value2);

	assertEquals(m_xc.getAttributeText(new QName("at1")),value1);
	assertEquals(m_xc.getAttributeText(new QName("at0")),value0);

    }

    public void testInsertAttributeWithValueNull(){
	m_xc.insertAttributeWithValue("at0",null);
	assertEquals(m_xc.getAttributeText(new QName("at0")),null);
    }


    public void setUp()throws Exception{
	m_xc= XmlObject.Factory.parse(sDoc).newCursor();
	toNextTokenOfType(m_xc,XmlCursor.TokenType.TEXT);//prepare for atts
    }


    public static void main(String[] rgs){
	try{
	    InsertAttributeTest2 t= (new InsertAttributeTest2(""));
	    t.setUp();
	    t.testInsertAttributeWithValue();

	}catch (Exception e){
	    System.err.println("Error "+e.getMessage());
	    e.printStackTrace();
	}
    }
}
