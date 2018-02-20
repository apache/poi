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

import xmlcursor.common.BasicCursorTestCase;

import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;
import tools.util.JarUtil;

import java.io.IOException;

/**
 * Verifies XPath using functions
 * http://www.w3schools.com/xpath/xpath_functions.asp
 */

public class XPathFunctionAuxTest
    extends BasicCursorTestCase
{
    public XPathFunctionAuxTest(String sName)
    {
        super(sName);
    }

    public static Test suite()
    {
        return new TestSuite(XPathFunctionAuxTest.class);
    }

    static String fixPath(String path)
    {
        return path;
    }

    public void testFunctionCount_caseB()
        throws Exception
    {
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/cdcatalog.xml"));

        String ex1Simple = "count(//cd)";
        String ex1R1 = Common.XMLFRAG_BEGINTAG + "26" + Common.XMLFRAG_ENDTAG;
        XmlObject[] exXml1 = new XmlObject[]{XmlObject.Factory.parse(ex1R1)};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(ex1Simple);
        XPathCommon.display(x1);
        XPathCommon.compare(x1, exXml1);
        x1.dispose();
    }

    public void testFunctionConcat_caseB()
        throws Exception
    {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price>" +
            "<price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "concat(name(/bar[position()=1]/*[position()=last()])," +
            "//price[position()=1]/text())";
        String sExpected = Common.wrapInXmlFrag("price3.00");
        m_xc.selectPath(fixPath(sXPath));
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testFunctionStringLength_caseB()
        throws Exception
    {

        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "string-length(name(//bar/*[last()]))";
        String sExpected = Common.wrapInXmlFrag("price".length() + "");
        m_xc.selectPath(fixPath(sXPath));
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testFunctionSubString_caseB()
        throws Exception
    {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "substring(name(//bar[position()=1]/*[position()=1]),3,3)";
        String sExpected = Common.wrapInXmlFrag("ice");
        m_xc.selectPath(fixPath(sXPath));
        m_xc.toNextSelection();
        assertEquals(XmlCursor.TokenType.TEXT,
            m_xc.currentTokenType());
        assertEquals(sExpected, m_xc.xmlText());

    }

    public void testFunctionSubStringAfter_caseB()
        throws Exception
    {

        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "substring-after(name(//bar[position()=1]/*[position()=1]),'pr')";
        String sExpected = Common.wrapInXmlFrag("ice");
        m_xc.selectPath(fixPath(sXPath));
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());

    }

    public void testFunctionSubStringBefore_caseB()
        throws Exception
    {

        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "substring-before(" +
            "name(//bar[position()=1]/*[position()=1]),'ice')";
        String sExpected = Common.wrapInXmlFrag("pr");
        m_xc.selectPath(fixPath(sXPath));
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testFunctionTranslate_caseB()
        throws Exception
    {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "translate(//bar[position()=1]/price[position()=1]/text()," +
            "'200'," +
            "'654')";//0 is now 5 &&4?
        String sExpected = Common.wrapInXmlFrag("3.55");
        m_xc.selectPath(fixPath(sXPath));
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testFunctionNumber_caseB()
        throws Exception
    {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        String sXPath = "number(//price/text())+10";
        String sExpected = Common.wrapInXmlFrag("13.0");
        m_xc.selectPath(fixPath(sXPath));
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());

    }

    public void testFunctionRound_caseB()
        throws Exception
    {
        String sXml = "<foo><bar><price at=\"val0\">3.15</price><price at=\"val1\">2.87</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "round(//bar/price[position()=1]/text())";
        String sExpected = Common.wrapInXmlFrag("3.0");
        m_xc.selectPath(fixPath(sXPath));
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }

    public void testFunctionSum_caseB()
        throws Exception
    {

        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "sum(//bar/price)";
        String sExpected = Common.wrapInXmlFrag("5.0");
        m_xc.selectPath(sXPath);
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }
//

    public void testFunctionBoolean_caseB_delete()
        throws Exception
    {
        String sXml = "<foo><bar>" +
            "<price at=\"val0\">3.00</price>" +
            "<price at=\"val1\">2</price>" +
            "</bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = "boolean(//foo/text())";
        m_xc.selectPath(sXPath);
        System.out.println(m_xc.getSelectionCount());
        assertTrue(m_xc.toNextSelection());
        assertEquals(Common.wrapInXmlFrag("false"),
            m_xc.xmlText());
        assertTrue(!m_xc.toNextSelection());
        System.out.println("DOC  " + m_xc.xmlText());
        m_xc.clearSelections();
        m_xc.toStartDoc();
        m_xc.selectPath("boolean(//price/text())");
//     m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
   //     System.out.println("HERE " + m_xc.xmlText());
    }

    public void testFunctionBoolean_caseB()
        throws Exception
    {
        String sXml = "<foo><bar>" +
            "<price at=\"val0\">3.00</price>" +
            "<price at=\"val1\">2</price>" +
            "</bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        XmlCursor _startPos = m_xc.newCursor();

        String sXPath = "boolean(//foo/text())";//"boolean(//foo/text())";
        m_xc.push();
        m_xc.selectPath(sXPath);
        m_xc.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("false"),
            m_xc.xmlText());
        m_xc.clearSelections();

        //need to reset cursor since it's on a bool outside the doc
        m_xc.pop();
        //number
        m_xc.selectPath("boolean(//price/text())");
        m_xc.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("true"), m_xc.xmlText());
        m_xc.clearSelections();


        //number
        assertTrue(m_xc.toCursor(_startPos));
        //boolean of Nan is false
        m_xc.selectPath("boolean(number(name(//price[position()=last()])))");
        m_xc.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("false"), m_xc.xmlText());
        m_xc.clearSelections();

        //node-set
        m_xc.toCursor(_startPos);
        m_xc.selectPath("boolean(//price)");
        m_xc.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("true"), m_xc.xmlText());
        m_xc.clearSelections();

        m_xc.toCursor(_startPos);
        m_xc.selectPath("boolean(//barK)");
        m_xc.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("false"), m_xc.xmlText());
        m_xc.clearSelections();

    }

    public void testFunctionFalse_caseB()
        throws Exception
    {
        m_xc =
            XmlObject.Factory.parse(
                "<foo><price at=\"val0\">3.00</price></foo>")
            .newCursor();
        m_xc.selectPath("name(//*[boolean(text())=false()])");
        String sExpected = Common.wrapInXmlFrag("foo");
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());

    }
}
