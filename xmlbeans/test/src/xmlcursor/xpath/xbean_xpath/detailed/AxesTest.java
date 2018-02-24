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
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;

/**
 *  Axes Tested:
 * child
 * descendant
 * attribute
 * descendant-or-self
 * self
 * namespace
 */

/**
 * 
 */
public class AxesTest extends TestCase {
    String sXmlChild = "<foo> <bar xmlns:pre=\"http://uri.com\" at0='val0'>" +
            "<pre:baz xmlns:baz='http://uri' baz:at0='val1'/>txt child</bar></foo>",

    sXmlDesc = "<foo> <foo xmlns:pre=\"http://uri.com\" at0='val0'>" +
            "<pre:baz xmlns:baz='http://uri' baz:at0='val1'/>txt child</foo></foo>"
            ;


    public void testChildAxisAbbrev() throws XmlException {

        String sQuery1 = "./foo/bar";
        String sExpected = "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
        XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
        c.selectPath(sQuery1);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());

        sQuery1 = "$this/foo/child::bar";
        c.clearSelections();
        c.toStartDoc();
        c.selectPath(sQuery1,options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());


    }

    public void testChildAxis() throws XmlException {
        String sQuery1 = "./foo/child::bar";
        String sExpected = "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
        XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
//        c.clearSelections();
//        c.toStartDoc();

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());

    }

    public void testChildAxisDot() throws XmlException {

        String sQuery1 = "$this/foo/./bar";
        String sExpected = "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
        XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
        c.selectPath(sQuery1, options );
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());


    }

    public void testChildAxisDNE() throws XmlException {

        String sQuery1 = "$this/foo/./baz";
        XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
        c.selectPath(sQuery1, options);
        assertEquals(0, c.getSelectionCount());

    }

//    public void testDescendantAxis() throws XmlException {
//
//        String sQuery1 = "./descendant::foo";
//        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
//                "<pre:baz  baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        assertEquals("foo", c.getName().getLocalPart());
//
//        c.selectPath(sQuery1,options );
//        assertEquals(1, c.getSelectionCount());
//        c.toNextSelection();
//        assertEquals(sExpected, c.xmlText());
//
//
//    }

    public void testDescendantAxisAbbrev() throws XmlException {

        String sQuery1 = ".//foo";
        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";
        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());

        c.selectPath(sQuery1, options );
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());


    }

//    public void testDescAxisDot() throws XmlException {
//
//        String sQuery1 = "$this/descendant::foo/.";
//        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
//                "<pre:baz  baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        c.selectPath(sQuery1,options);
//        assertEquals(1, c.getSelectionCount());
//        c.toNextSelection();
//        assertEquals(sExpected, c.xmlText());
//
//
//    }

//    public void testDescAxisDNE() throws XmlException {
//
//        String sQuery1 = "$this/descendant::baz";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        c.selectPath(sQuery1, options);
//        assertEquals(0, c.getSelectionCount());
//
//    }


    public void testChildAttribute() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        String sQuery1 = "$this/foo/bar/attribute::at0";
        XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
        c.selectPath(sQuery1, options );
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    public void testChildAttributeAbbrev() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        ;
        String sQuery1 = "$this/foo/bar/@at0";
        XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
        c.selectPath(sQuery1, options );
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    public void testDescAttribute() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        String sQuery1 = "$this//attribute::at0";
        XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.selectPath(sQuery1, options );
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }


//    public void testDescendantOrSelfAxis() throws XmlException {
//
//        String sQuery1 = "./descendant-or-self::foo";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        String[] sExpected = new String[]
//        {
//            c.xmlText()
//            , "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
//                "<pre:baz  baz:at0=\"val1\"" +
//                " xmlns:baz=\"http://uri\"/>txt child</foo>"
//        };
//
//
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        assertEquals("foo", c.getName().getLocalPart());
//
//        c.selectPath(sQuery1, options );
//        assertEquals(2, c.getSelectionCount());
//        c.toNextSelection();
//        assertEquals(sExpected[0], c.xmlText());
//        c.toNextSelection();
//        assertEquals(sExpected[1], c.xmlText());
//
//
//    }

//    public void testDescendantOrSelfAxisDot() throws XmlException {
//
//        String sQuery1 = "./descendant-or-self::foo";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        String[] sExpected = new String[]
//        {
//            c.xmlText()
//            , "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
//                "<pre:baz  baz:at0=\"val1\"" +
//                " xmlns:baz=\"http://uri\"/>txt child</foo>"
//        };
//
//
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        c.selectPath(sQuery1, options );
//
//        c.selectPath(sQuery1, options );
//        assertEquals(2, c.getSelectionCount());
//        c.toNextSelection();
//        assertEquals(sExpected[0], c.xmlText());
//        c.toNextSelection();
//        assertEquals(sExpected[1], c.xmlText());
//
//    }

//    public void testDescendantOrSelfAxisDNE() throws XmlException {
//
//        String sQuery1 = "$this/descendant-or-self::baz";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        c.selectPath(sQuery1, options );
//        assertEquals(0, c.getSelectionCount());
//
//    }


//    public void testSelfAxis() throws XmlException {
//
//        String sQuery1 = "$this/self::foo";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        String sExpected =
//                c.xmlText();
//
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        assertEquals("foo", c.getName().getLocalPart());
//
//        c.selectPath(sQuery1, options );
//        assertEquals(1, c.getSelectionCount());
//        c.toNextSelection();
//        assertEquals(sExpected, c.xmlText());
//
//    }

    public void testSelfAxisAbbrev() throws XmlException {

        String sQuery1 = ".";
        XmlCursor c = XmlObject.Factory.parse( sXmlChild ).newCursor();
        String sExpected =
                c.xmlText();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("foo", c.getName().getLocalPart());

        c.selectPath(sQuery1, options );
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());

    }

//    public void testSelfAxisDot() throws XmlException {
//
//        String sQuery1 = "./self::foo";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        String sExpected =
//                c.xmlText();
//
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        assertEquals("foo", c.getName().getLocalPart());
//
//        c.selectPath(sQuery1, options );
//        assertEquals(1, c.getSelectionCount());
//        c.toNextSelection();
//        assertEquals(sExpected, c.xmlText());
//    }
//
//    public void testSelfAxisDNE() throws XmlException {
//
//        String sQuery1 = "$this/self::baz";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        c.selectPath(sQuery1, options );
//        assertEquals(0, c.getSelectionCount());
//
//    }
//
//    public void testNamespaceAxis() throws XmlException {
//
//        String sQuery1 = "$this/namespace::http://uri.com";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        String sExpected =
//                c.xmlText();
//
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        assertEquals(XmlCursor.TokenType.TEXT, c.toNextToken());
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        assertEquals("foo", c.getName().getLocalPart());
//
//        c.selectPath(sQuery1, options );
//        assertEquals(1, c.getSelectionCount());
//        c.toNextSelection();
//        assertEquals(sExpected, c.xmlText());
//    }
//
//    public void testNamespaceAxisDot() throws XmlException {
//
//        String sQuery1 = "./*/namespace::http://uri.com";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        String sExpected =
//                c.xmlText();
//
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        assertEquals("foo", c.getName().getLocalPart());
//
//        c.selectPath(sQuery1, options );
//        assertEquals(1, c.getSelectionCount());
//        c.toNextSelection();
//        assertEquals(sExpected, c.xmlText());
//    }
//
//    public void testNamespaceAxisDNE() throws XmlException {
//
//        String sQuery1 = "$this/namespace::*";
//        XmlCursor c = XmlObject.Factory.parse( sXmlDesc ).newCursor();
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        assertEquals(XmlCursor.TokenType.TEXT, c.toNextToken());
//        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
//        //to namespace
//        assertEquals(XmlCursor.TokenType.NAMESPACE, c.toNextToken());
//        c.selectPath(sQuery1, options );
//        assertEquals(0, c.getSelectionCount());
//
//    }
//
    public void setUp() {
        options = new XmlOptions();
        options.put("use xbean for xpath");
    }

    private XmlOptions options;

}

