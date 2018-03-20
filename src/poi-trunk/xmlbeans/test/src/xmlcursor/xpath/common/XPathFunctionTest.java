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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import tools.util.JarUtil;

/**
 * Verifies XPath using functions
 * http://www.w3schools.com/xpath/xpath_functions.asp
 */
public abstract class XPathFunctionTest extends BaseXPathTest {
    public XPathFunctionTest(String sName) {
        super(sName);
    }

    //    Node Set Functions
    //    http://www.w3.org/TR/xpath#section-Node-Set-Functions

    /**
     * count()
     *      Returns the number of nodes in a node-set
     *      number=count(node-set)
     *
     */
    public void testFunctionCount() throws Exception {
        String ex0Simple =getQuery("testFunctionCount",0) ;
        String ex0Simple1 =getQuery("testFunctionCount",1) ;
        System.out.println("Test 0: " + ex0Simple);
        XmlCursor x0 = XmlObject.Factory.parse(
                "<foo><cd>1</cd><cd>2</cd></foo>")
                .newCursor();
        XmlCursor x01 = x0.newCursor();
        /* XmlCursor countCheck = x0.newCursor();
           countCheck.selectPath("count(.//cd)");
           countCheck.toNextSelection();
           System.out.println(" Global count "+countCheck.xmlText());
        */
        String sExpectedResult = "<cd>2</cd>" ;

        x01.selectPath(ex0Simple1);
        assertEquals(1, x01.getSelectionCount());
        x01.toNextSelection();
        assertEquals(sExpectedResult, x01.xmlText());
        x01.dispose();

        sExpectedResult = "<xml-fragment>2</xml-fragment>";
        x0.selectPath(ex0Simple);
        XPathCommon.display(x0);
        assertEquals(1, x0.getSelectionCount());
        x0.toNextSelection();
        //XPathCommon.compare(x0, new XmlObject[]{XmlObject.Factory.parse("<a>foo</a>")});
        assertEquals(sExpectedResult, x0.xmlText());
        x0.dispose();

    }

    /**
     * id()
     *      Selects elements by their unique ID
     *      node-set=id(value)
     */
    public void testFunctionId() throws Exception {
        XmlObject xDoc = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/cdcatalog.xml"));
        //System.out.println(xDoc.xmlText());
        String ex1Simple =getQuery("testFunctionId",0); //"id(\"bobdylan\")"

        String ex1R1 = "<cd id=\"bobdylan\">" +
                "<title>Empire Burlesque</title>" +
                "<artist>Bob Dylan</artist><country>USA</country>" +
                "<company>Columbia</company><price>10.90</price>" +
                "<year>1985</year></cd>";
        XmlObject[] exXml1 = new XmlObject[]{XmlObject.Factory.parse(ex1R1)};

        String ex2Simple = getQuery("testFunctionId",1); //"id(\"foobar\")"
        String ex2R1 = "<cd/>";
        XmlObject[] exXml2 = new XmlObject[]{XmlObject.Factory.parse(ex2R1)};

        String ex3Simple = getQuery("testFunctionId",2); //"id(\"*\")/child::cd[position()=3]"
        String ex3R1 = "<cd id=\"id3\"><title>Greatest Hits</title><artist>Dolly Parton</artist><country>USA</country><company>RCA</company><price>9.90</price><year>1982</year></cd>";
        XmlObject[] exXml3 = new XmlObject[]{XmlObject.Factory.parse(ex3R1)};


        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.toChild("catalog");
        System.out.println(x1.currentTokenType());
        System.out.println(x1.getName());
        x1.selectPath(ex1Simple);
        //XPathCommon.display(x1);
        //assertEquals(1,x1.getSelectionCount());
        XPathCommon.compare(x1, exXml1);
        //assertEquals(ex1R1, x1.xmlText());
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(ex2Simple);
        XPathCommon.display(x2);
        XPathCommon.compare(x2, exXml2);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(ex3Simple);
        XPathCommon.display(x3);
        XPathCommon.compare(x3, exXml3);
        x3.dispose();
    }

    /**
     * last()
     *      Returns the position number of the last node in the processed node list
     *      number=last()
     */
    public void testFunctionLast() throws Exception {
        XmlObject xDoc = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/cdcatalog.xml"));

        String ex1Simple = getQuery("testFunctionLast",0);
        String ex1R1 = "<cd>" +
                "<title>Unchain my heart</title>" +
                "<artist>Joe Cocker</artist><country>USA</country>" +
                "<company>EMI</company><price>8.20</price>" +
                "<year>1987</year></cd>";
        XmlObject[] exXml1 = new XmlObject[]{XmlObject.Factory.parse(ex1R1)};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(ex1Simple);
        XPathCommon.display(x1);
        XPathCommon.compare(x1, exXml1);
        x1.dispose();
    }

    /**
     * local-name()
     *      Returns the local part of a node. A node usually consists of a prefix, a colon, followed by the local name
     *      string=local-name(node)
     */
    public void testFunctionLocalName() throws Exception {
        String sXPath = getQuery("testFunctionLocalName",0);
        String sXml = "<foo xmlns:pre=\"uri.org\">" +
                "<pre:bar><price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">2</price></pre:bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        String sExpected =
                "<pre:bar xmlns:pre=\"uri.org\"><price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">2</price></pre:bar>";

        m_xc.selectPath(sXPath);
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }

    //    /**
    //     * name()
    //     *      Returns the name of a node
    //     *      string=name(node)
    //     */
    //    public void testFunctionName() throws Exception {
    //
    //    }

    /**
     * namespace-uri()
     *      Returns the namespace URI of a specified node
     *      uri=namespace-uri(node)
     */
    public void testFunctionNamespaceURI() throws Exception {
        String sXPath = getQuery("testFunctionNamespaceURI",0);

        String sXml = "<foo xmlns:pre=\"uri.org\">" +
                "<pre:bar><price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">2</price></pre:bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        String sExpected = "<pre:bar xmlns:pre=\"uri.org\">" +
                "<price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">2</price></pre:bar>";
        m_xc.selectPath(sXPath);
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }

    //    /**
    //     * position()
    //     *      Returns the position in the node list of the node that is currently being processed
    //     *      number=position()
    //     */
    //    public void testFunctionPosition() throws Exception {
    //
    //    }

    //    String Functions
    //    http://www.w3.org/TR/xpath#section-String-Functions

    /**
     * concat()
     *      Returns the concatenation of all its arguments string=concat(val1, val2, ..)
     *      Example: concat('The',' ','XML') Result: 'The XML'
     */
    public void testFunctionConcat() throws Exception {
        String sXPath=getQuery("testFunctionConcat",0);
        String sXml = "<foo><bar><price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

         m_xc.selectPath(sXPath);
        assertEquals(2, m_xc.getSelectionCount());
    }

    /**
     * string()
     *      Converts the value argument to a string
     *      string(value)
     *      Example:  string(314)    Result: '314'
     */
    public void testFunctionString() throws Exception {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">1</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath =getQuery("testFunctionString",0);
        m_xc.selectPath(sXPath);
        assertEquals(1,m_xc.getSelectionCount());

        System.out.println(" Test 0 Pass==================");

        /*	m_xc.clearSelections();
                sXPath="xf:concat(xf:string(//foo/bar/price[1]),xf:string(//foo/bar/price[last()]))";
                String sExpected="03.00";
                m_xc.selectPath(sXPath);
                m_xc.toNextSelection();
                assertEquals(sExpected,m_xc.xmlText());
        */
    }

    /**
     * string-length()
     *      Returns the number of characters in a string
     *      number=string-length(string)
     *    Example: string-length('Beatles') Result: 7
     */
    public void testFunctionStringLength() throws Exception {
        String sXml = "<foo><bar>" +
                "<price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>" +
                "<bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath =  getQuery("testFunctionStringLength",0);
        m_xc.selectPath(sXPath);
        assertEquals(2, m_xc.getSelectionCount());

        System.out.println(" Test 0 Pass==================");
        m_xc.clearSelections();
    }

    /**
     * substring()
     *      Returns a part of the string in the string argument
     *      string=substring(string,start,length)
     *    Example: substring('Beatles',1,4) Result: 'Beat'
     */
    public void testFunctionSubString() throws Exception {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = getQuery("testFunctionSubString",0);
        m_xc.selectPath(sXPath);
        assertEquals(2, m_xc.getSelectionCount());

        m_xc.clearSelections();
        System.out.println(" Test 0 Pass==================");
    }

    /**
     * substring-after()
     *      Returns the part of the string in the string argument that occurs after the substring in the substr argument
     *      string=substring-after(string,substr)
     *      Example: substring-after('12/10','/') Result: '10'
     */
    public void testFunctionSubStringAfter() throws Exception {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = getQuery("testFunctionSubStringAfter",0);
                m_xc.selectPath(sXPath);
        assertEquals(2, m_xc.getSelectionCount());
    }

    /**
     * substring-before()
     *      Returns the part of the string in the string argument that occurs before the substring in the substr argument
     *      string=substring-before(string,substr)
     *      Example: substring-before('12/10','/') Result: '12'
     */
    public void testFunctionSubStringBefore() throws Exception {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = getQuery("testFunctionSubStringBefore",0);
                m_xc.selectPath(sXPath);
        assertEquals(2, m_xc.getSelectionCount());
    }

    /**
     * translate()
     *      Performs a character by character replacement. It looks in the value argument for characters
     *      contained in string1, and replaces each character for the one in the same position in the string2
     *      string=translate(value,string1,string2)
     *      Examples: translate('12:30','30','45') Result: '12:45'
     *                translate('12:30','03','54') Result: '12:45'
     *                translate('12:30','0123','abcd') Result: 'bc:da'
     */
    public void testFunctionTranslate() throws Exception {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        //TODO: is this a bug in XQRL?
        String sXPath =getQuery("testFunctionTranslate",0);
        m_xc.selectPath(sXPath);
        assertEquals(2, m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals("<price at=\"val0\">3.00</price>", m_xc.xmlText());
        m_xc.toNextSelection();
        assertEquals("<price at=\"val1\">2</price>", m_xc.xmlText());
    }

    //    Number Functions
    //    http://www.w3.org/TR/xpath#section-Number-Functions

    //    /**
    //     * ceiling()
    //     *      Returns the smallest integer that is not less than the number argument
    //     *      number=ceiling(number)
    //     *      Example: ceiling(3.14) Result: 4
    //     */
    //    public void testFunctionCeiling() throws Exception {
    //
    //    }

    //    /**
    //     * floor()
    //     *      Returns the largest integer that is not greater than the number argument
    //     *      number=floor(number)
    //     *      Example: floor(3.14) Result: 3
    //     */
    //    public void testFunctionFloor() throws Exception {
    //
    //    }

    /**
     * number()
     *      Converts the value argument to a number
     *      number=number(value)
     *      Example: number('100') Result: 100
     */
    public void testFunctionNumber() throws Exception {
        String sXml = "<foo><bar><price at=\"val0\">3.00</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        //really wanted . here...seems like number function doesn't
        //recognize context?
        String sXPath =getQuery("testFunctionNumber",0);
        m_xc.selectPath(sXPath);
        assertEquals(1, m_xc.getSelectionCount());

        System.out.println(" Test 0 Pass==================");
        m_xc.clearSelections();
    }

    /**
     * round()
     *      Rounds the number argument to the nearest integer
     *      integer=round(number)
     *      Example: round(3.14) Result: 3
     */
    public void testFunctionRound() throws Exception {
        String sXml = "<foo><bar><price at=\"val0\">3.15</price>" +
                "<price at=\"val1\">2.87</price></bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = getQuery("testFunctionRound",0);
        m_xc.selectPath(sXPath); //"//bar//*[round(text())=3]"
        assertEquals(2, m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals("<price at=\"val0\">3.15</price>", m_xc.xmlText());
        m_xc.toNextSelection();
        assertEquals("<price at=\"val1\">2.87</price>", m_xc.xmlText());
        m_xc.clearSelections();
    }

    /**
     * sum()
     *      Returns the total value of a set of numeric values in a node-set
     *      number=sum(nodeset)
     *      Example: sum(/cd/price)
     */
    public void testFunctionSum() throws Exception {
        String sXml = "<foo><bar><price at=\"val0\">" +
                "3.00</price><price at=\"val1\">2</price>" +
                "</bar><bar1>3.00</bar1></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();

        String sXPath = getQuery("testFunctionSum",0);
        m_xc.selectPath(sXPath);
        assertEquals(1, m_xc.getSelectionCount());
        m_xc.toNextSelection();
        String exp = "<bar><price at=\"val0\">" +
                "3.00</price><price at=\"val1\">2</price>" +
                "</bar>";
        assertEquals(exp, m_xc.xmlText());
        m_xc.clearSelections();
    }

    //    Boolean Functions
    //    http://www.w3.org/TR/xpath#section-Boolean-Functions

    /**
     * boolean()
     *      Converts the value argument to Boolean and returns true or false
     *      bool=boolean(value)
     * a number is true if and only if it is neither positive or negative zero nor NaN
     * a node-set is true if and only if it is non-empty
     * a string is true if and only if its length is non-zero
     * an object of a type other than the four basic types is converted to a boolean in a way that is dependent on that type
     */
    public void testFunctionBoolean() throws Exception {
        m_xc =
                XmlObject.Factory.parse(
                        "<foo><price at=\"val0\">3.00</price></foo>")
                .newCursor();
        String sXPath= getQuery("testFunctionBoolean",0);
        m_xc.selectPath(sXPath);
        m_xc.toNextSelection();
        assertEquals(1, m_xc.getSelectionCount());
    }

    /**
     * false()
     *      Returns false false()
     *    Example: number(false()) Result: 0
     */
    public void testFunctionFalse() throws Exception {
        m_xc =
                XmlObject.Factory.parse(
                        "<foo><price at=\"val0\">3.00</price></foo>")
                .newCursor();
          String sXPath= getQuery("testFunctionFalse",0);
        m_xc.selectPath(sXPath);
        assertEquals(0, m_xc.getSelectionCount());
    }

    /**
     * lang()
     *      Returns true if the language argument matches the language of the the
     *      xsl:lang element, otherwise it returns false
     *      bool=lang(language)
     */
    public void testFunctionLang() throws Exception {
        m_xc =
                XmlObject.Factory.parse(
                        "<foo><div xml:lang=\"en\"><para/><price at=\"val0\">3.00</price></div></foo>")
                .newCursor();
          String sXPath= getQuery("testFunctionLang",0);
        m_xc.selectPath(sXPath);
        String sExpected = "<price at=\"val0\">3.00</price>";
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
        m_xc.clearSelections();

        sXPath= getQuery("testFunctionLang",1);
        m_xc.selectPath(sXPath);
        assertEquals(0, m_xc.getSelectionCount());
    }

    //    /**
    //     * not()
    //     *      Returns true if the condition argument is false,
    //     *      and false if the condition argument is true
    //     *      bool=not(condition)
    //     *    Example: not(false())
    //     */
    //    public void testFunctionNot() throws Exception {
    //
    //    }

    /**
     * true()
     *      Returns true
     *      true()
     *    Example: number(true()) Result: 1
     */
    public void testFunctionTrue() throws Exception {
        m_xc =
                XmlObject.Factory.parse(
                        "<foo><price at=\"val0\">3.00</price></foo>")
                .newCursor();

        String sXPath= getQuery("testFunctionTrue",0);
        m_xc.selectPath(sXPath);
        String sExpected = "<price at=\"val0\">3.00</price>";
        m_xc.toNextSelection();
        assertEquals(sExpected, m_xc.xmlText());
    }

}
