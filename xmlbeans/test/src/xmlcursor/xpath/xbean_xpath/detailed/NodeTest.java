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

package xmlcursor.xpath.xbean_xpath.detailed;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;

/**
 *  Nodes Tested:
 * *

 * NameTest
 *    * "*"
 *    * NCName:*
 *    * QName
 * NodeType
 *    * comment
 *    * node
 *    * pi
 *    * text
 * PI(Literal)
 */

/**
 * 
 *
 */

public class NodeTest extends TestCase {

    String sXmlChild = "<foo> <bar xmlns:pre=\"http://uri.com\" at0='val0'>" +
            "<pre:baz xmlns:baz='http://uri' baz:at0='val1'/>txt child</bar>" +
            "</foo>";
    String sXmlPI = "<foo><?xml-stylesheet target=\"http://someuri\"?></foo>";

    public void testNameTestStar() throws XmlException {
        String sQuery1 = "./*";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        String sExpected = c.xmlText();
        c.selectPath(sQuery1);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());

    }

    public void testNameTestNCName() throws XmlException {
        String sQuery1 = "$this//*";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        String sExpected = "<pre:baz baz:at0=\"val1\" " +
                "xmlns:baz=\"http://uri\" xmlns:pre=\"http://uri.com\"/>";
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.toNextToken();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("bar", c.getName().getLocalPart());
        c.selectPath(sQuery1);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());

    }

    public void testNameTestQName_1() throws XmlException {
        String sQuery1 = "declare namespace pre=\"http://uri.com\"; $this//pre:*";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        String sExpected =
                "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\" xmlns:pre=\"http://uri.com\"/>";
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("foo", c.getName().getLocalPart());
        c.selectPath(sQuery1);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());

    }

    //test a QName that DNE
    public void testNameTestQName_2() throws XmlException {
        String sQuery1 = "declare namespace pre=\"http://uri\"; $this//pre:baz";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.selectPath(sQuery1);
        assertEquals(0, c.getSelectionCount());


    }

    public void testNameTestQName_3() throws XmlException {
        String sQuery1 = "$this//bar";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        String sExpected = "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.selectPath(sQuery1);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());

    }

    public void testNodeTypeComment() {

    }


    public void testNodeTypeNodeAbbrev() throws XmlException {
        String sQuery1 = "$this/foo/*";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        String sExpected = "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
        c.selectPath(sQuery1);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }
    /**
     * Will not support natively
     *
     public void testNodeTypeNode() throws XmlException {
     String sQuery1 = "$this/foo/node()";
     XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
     String sExpected ="<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
     "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
     c.selectPath(sQuery1);
     assertEquals(1, c.getSelectionCount());
     c.toNextSelection();
     assertEquals(sExpected, c.xmlText());
     }
     public void testNodeTypePI() throws XmlException {

     XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
     String sExpected ="<foo><?xml-stylesheet target=\"http://someuri\"?></foo>";
     String sQuery="./foo/processing-instruction()";
     c.selectPath(sQuery);
     assertEquals(1, c.getSelectionCount());
     c.toNextSelection();
     assertEquals(sExpected, c.xmlText());

     }

     public void testNodeTypeText() throws XmlException {
     String sQuery1 = "$this//text()";
     XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
     String sExpected =" ";
     assertEquals( XmlCursor.TokenType.START, c.toNextToken() );
     c.selectPath(sQuery1);
     assertEquals(1, c.getSelectionCount());
     c.toNextSelection();
     assertEquals(sExpected, c.xmlText());
     }

     public void testPI() throws XmlException {

     XmlCursor c = XmlObject.Factory.parse( sXmlPI ).newCursor();
     String sExpected ="<?xml-stylesheet target=\"http://someuri\"?>";
     String sQuery="./foo/processing-instruction('xml-stylesheet')";
     c.selectPath(sQuery);
     assertEquals(1, c.getSelectionCount());
     c.toNextSelection();
     assertEquals(sExpected, c.xmlText());

     }

     public void testPIDNE() throws XmlException {

     XmlCursor c = XmlObject.Factory.parse( sXmlPI ).newCursor();
     String sQuery="./foo/processing-instruction('stylesheet')";
     c.selectPath(sQuery);
     assertEquals(0, c.getSelectionCount());

     }
     */
}
