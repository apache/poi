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


package dom.checkin;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class DomTests extends TestCase {
    public DomTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(DomTests.class);
    }

    static String[] _args;
    static String _test;


    public void doTestDomImport(String xml)
            throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder parser = f.newDocumentBuilder();

        Document doc = parser.parse(new InputSource(new StringReader(xml)));

        XmlObject x = XmlObject.Factory.parse(doc);

        Assert.assertTrue(x.xmlText().equals(xml));
    }

    public void doTestDomExport(String xml)
            throws Exception {
        XmlObject x = XmlObject.Factory.parse(
                XmlObject.Factory.parse(xml).newDomNode());
        Assert.assertTrue(x.xmlText().equals(xml));
    }

    public void doTest(String xml)
            throws Exception {
        doTestDomImport(xml);
        doTestDomExport(xml);
    }

//    public void testDom2 ( )
//        throws Exception
//    {
//        DOMParser parser = new DOMParser();
//        
//        parser.setFeature( "http://xml.org/sax/features/namespaces", true );
//        
//        parser.parse( new InputSource( new StringReader( "<foo a='x\n\ny'></foo>" ) ) );
//
//        XmlObject x = XmlLoader.Factory.parse( parser.getDocument() );
//
//        System.out.println( x.xmlText() );
//    }
    
    public void testDom()
            throws Exception {
        doTest("<foo xmlns=\"x\"/>");
        doTest("<foo xmlns=\"x\" xmlns:e=\"v\"/>");
        doTest("<foo>a<?X?>b</foo>");
        doTest("<foo>a<!--X-->b</foo>");
        doTest("<!--X--><foo/>");
        doTest("<foo/>");
        doTest("<foo x=\"y\"/>");
        doTest("<foo><a/><b>moo</b></foo>");

        String xx =
                "<!--gg--><?a b?><foo>sdsd<a/>sdsd<b>moo</b>sd<!--asas-->sd</foo><!--hh-->";

        doTest(xx);

        String xml =
                "<xml-fragment>" +
                "foo" +
                "</xml-fragment>";

        doTestDomExport(xml);


        XmlObject x = XmlObject.Factory.parse(xx);

        XmlCursor c = x.newCursor();

        for (; ;) {
            Node n = c.newDomNode();
            XmlObject.Factory.parse(n);

            if (c.toNextToken().isNone())
                break;
        }
    }
}
