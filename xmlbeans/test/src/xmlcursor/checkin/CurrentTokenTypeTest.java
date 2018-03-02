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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;



/**
 *
 *
 */
public class CurrentTokenTypeTest extends BasicCursorTestCase {

    String sInputDoc;

    public  CurrentTokenTypeTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(CurrentTokenTypeTest.class);
    }
    /**
        ATTR
	COMMENT
	END
	ENDDOC
	NAMESPACE
	NONE
	PROCINST
	START
	STARTDOC
	TEXT
    */

    public void testAttrType() throws XmlException{
	sInputDoc=Common.XML_FOO_2ATTR_TEXT;
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	assertEquals(m_xc.currentTokenType(),XmlCursor.TokenType.STARTDOC);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.START);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.ATTR);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.ATTR);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.TEXT);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.END);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.ENDDOC);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.NONE);
    }
    public void testCommentType()throws XmlException{
	sInputDoc=Common.XML_FOO_COMMENT;
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	assertEquals(m_xc.currentTokenType(),XmlCursor.TokenType.STARTDOC);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.COMMENT);
     }
    public void testEndType(){
	//tested by testAttrType
     }
    public void testEndDocType(){
	//tested by testAttrType
     }
    public void testNamespaceType()throws XmlException{
	sInputDoc=Common.XML_FOO_NS_PREFIX ;
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();

	assertEquals(m_xc.currentTokenType(),XmlCursor.TokenType.STARTDOC);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.START);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.NAMESPACE);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.COMMENT);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.TEXT);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.START);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.ATTR);

    }
    public void testNoneType()throws XmlException{
	sInputDoc="<a/>";
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	m_xc.toEndDoc();
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.NONE);
    }
    public void testProcinstType()throws XmlException{
	sInputDoc=Common.XML_FOO_PROCINST;
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	assertEquals(m_xc.currentTokenType(),XmlCursor.TokenType.STARTDOC);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.PROCINST);

     }
    public void testStartType(){
	//tested by testAttrType
    }
    public void testStartdocType(){
	//tested by testAttrType
    }
    public void testTextType()throws XmlException{
	sInputDoc="<text>blah<test>test and some more test</test>"+"\u042F\u0436\n\r</text>";
	m_xc= XmlObject.Factory.parse(sInputDoc).newCursor();
	assertEquals(m_xc.currentTokenType(),XmlCursor.TokenType.STARTDOC);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.START);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.TEXT);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.START);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.TEXT);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.END);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.TEXT);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.END);
	assertEquals(m_xc.toNextToken(),XmlCursor.TokenType.ENDDOC);

    }

    public static void main(String[] rgs){
	try{
	    (new CurrentTokenTypeTest("")).testAttrType();
	}catch (Exception e){
	    System.err.println(e.getMessage());
	}
    }
}
