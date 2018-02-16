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

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;


/**
 *
 *
 */

public class SetNameTest extends BasicCursorTestCase{
    String sTestXml="<bk:book at0=\"value0\" xmlns:bk=\"urn:loc.gov:books\">text0<author at0=\"v0\" at1=\"value1\"/></bk:book>";


    public SetNameTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(SetNameTest.class);
    }


    public void testNormalCase(){
	m_xc.toFirstChild();
	QName newName=new QName("newBook");
	m_xc.setName(newName);
	assertEquals(m_xc.getName(),newName);

	 newName=new QName("uri:newUri","newBook");
	m_xc.setName(newName);
	assertEquals(m_xc.getName(),newName);


	 newName=new QName("uri:newUri","newBook","prefix");
	m_xc.setName(newName);
	assertEquals(m_xc.getName(),newName);

	//should work for attrs too...
	m_xc.toFirstAttribute();
	 newName=new QName("uri:newUri","newBook","prefix");
	m_xc.setName(newName);
	assertEquals(m_xc.getName(),newName);
    }

    public void testNoUri(){
	m_xc.toFirstChild();
	QName newName=new QName(null,"newBook");
	m_xc.setName(newName);
	assertEquals(m_xc.getName().getLocalPart(),"newBook");
    }

    public void testNull(){
	m_xc.toFirstChild();
	try{
	    m_xc.setName(null);
	    fail("QName null");
	}catch(Exception e){
	    System.err.println(e.getMessage());
	}

    }

    public void setUp()throws Exception{
	m_xc=XmlObject.Factory.parse(sTestXml).newCursor();
    }


    public static void main(String[]rgs){
	try{
	    SetNameTest myTest=new SetNameTest("");
	    myTest.setUp();
	    myTest.testNormalCase();
	    myTest.setUp();
	    myTest.testNoUri();
	     myTest.setUp();
	    myTest.testNull();

	}catch(Exception e){
	    System.err.println(e.getMessage());
	}
    }
}
