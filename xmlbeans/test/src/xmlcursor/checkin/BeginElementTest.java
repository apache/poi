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

import xmlcursor.common.Common;
import xmlcursor.common.BasicCursorTestCase;

import java.net.URL;


/**
 *
 *
 */
public class BeginElementTest extends BasicCursorTestCase {
    XmlCursor.TokenType tok;

    String sLocalName="localName";
    String sUri="fakeURI";
    String sDefaultPrefix=sUri.substring(0,3); //$BUGBUG:WHY???
    String sExpectedStart="<"+sDefaultPrefix+":localName xmlns:"+sDefaultPrefix+"=\"fakeURI\"/>";

    String sInputDoc=Common.XML_FOO_DIGITS;


    public BeginElementTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(BeginElementTest.class);
    }



    public void testBeginElementStr()throws Exception {
	//same for string API
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	toNextTokenOfType(m_xc, TokenType.START);
	m_xc.beginElement(sLocalName,sUri);
	toPrevTokenOfType(m_xc, TokenType.START);
	/*System.out.println(" SAW "+m_xc.xmlText());
	System.out.println(" Wanted "+sExpectedStart);
	System.out.println(" Java Wanted "+new QName(sUri,sLocalName));
	*/
	assertEquals(m_xc.xmlText(),sExpectedStart);
    }

     public void testBeginElementQName() throws Exception{
	//Qname call

	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	//insert new under the first element
	toNextTokenOfType(m_xc, TokenType.START);
	QName qName=new QName(sUri,sLocalName);
	m_xc.beginElement(qName);
	checkResult(qName);
    }
    public void testBeginElementQNamePrefix() throws Exception{
	//Qname with prefix
	String sPrefix="pre";
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	toNextTokenOfType(m_xc, TokenType.START);
	QName qName=new QName(sUri,sLocalName,sPrefix);
	System.out.println("Java prefix Qname: "+qName);
	m_xc.beginElement(qName);
	checkResult(qName);
    }

    //pre: cursor is not moved after beginElt call
    private void checkResult(QName qName){
	tok=m_xc.toPrevToken();

	assertEquals(m_xc.getName(),qName);

    }

    public void testBeginElementStartDoc(String sLocalName, String sUri)throws Exception {
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	m_xc.beginElement(sLocalName,sUri);
	m_xc.toPrevToken();
	m_xc.toPrevToken();
	assertEquals(true,m_xc.isStartdoc());

    }

    //
    public static void main(String[] rgs){
	try{
	    BeginElementTest test=(new BeginElementTest(""));
	    test.testBeginElementQNamePrefix();
		// test.testBeginElementQName();
	    System.out.println("getName(): "+test.m_xc.getName());
	    System.out.println("cmlText(): "+test.m_xc.xmlText());
	}catch (Exception e){

	}
    }
}
