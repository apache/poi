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

/**
 * Verifies XPath with Expressions
 * http://www.w3schools.com/xpath/xpath_expressions.asp
 */
public abstract class XPathExpressionTest extends BaseXPathTest {

    String sXml="<foo>" +
            "<bar><price at=\"val0\">3.00</price>" +
            "<price at=\"val1\">2</price></bar><bar1>3.00</bar1>" +
            "</foo>";

    public XPathExpressionTest(String sName) {
        super(sName);
    }

    //("/catalog/cd[price>10.80]/price
    //Numerical Expressions
    /**
     * + Addition 6 + 4 10
     */
    public void testAddition() throws Exception {
        String sXpath=getQuery("testAddition",0);
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals("<price at=\"val0\">3.00</price>",m_xc.xmlText());
    }
    /**
     * - Subtraction 6 - 4 2
     */
    public void testSubtraction() throws Exception {

        String sXpath=getQuery("testSubtraction",0);
        String sExpected="<price at=\"val1\">2</price>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }
    /**
     * * Multiplication 6 * 4 24
     */
    public void testMultiplication() throws Exception {
        String sXpath=getQuery("testMultiplication",0);
        String sExpected="<price at=\"val1\">2</price>";
        m_xc.selectPath(sXpath);
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }
    /**
     * div Division 8 div 4 2
     * NOTE: do a case where res is infinite (eg 10 div 3 or 22/7)
     */
    public void testDiv() throws Exception {
        String sXpath=getQuery("testDiv",0); //get the second(last) price child
        String sExpected="<price at=\"val0\">3.00</price>";
        m_xc.selectPath(sXpath);
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());

        m_xc.clearSelections();
        m_xc.toStartDoc();

        sXpath=getQuery("testDiv",1); //get the second(last) price child
        sExpected="<price at=\"val1\">2</price>";
        m_xc.selectPath(sXpath);
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());

        m_xc.clearSelections();
        m_xc.toStartDoc();

        String sXpathZero=getQuery("testDiv",2);
        int i = 0;
        try{
            m_xc.selectPath(sXpathZero);
            i = m_xc.getSelectionCount();
            fail("Division by 0");
        }catch (Exception e){}
        assertEquals(0,i);

        m_xc.clearSelections();
        m_xc.toStartDoc();

        String sXpathInf=getQuery("testDiv",3);
        m_xc.selectPath(sXpathInf);
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }
    /**
     * mod Modulus (division remainder) 5 mod 2 1
     */
    public void testMod() throws Exception {

        String sXpath=getQuery("testMod",0); //get the second(last) price child
        String sExpected="<price at=\"val1\">2</price>";

        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());

        m_xc.clearSelections();
        m_xc.toStartDoc();


        sXpath=getQuery("testMod",1); //get the second(last) price child

        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());

        String sXpathZero="10 mod 0";
        m_xc.clearSelections();
        m_xc.toStartDoc();
        int i = 0;
        try{
            m_xc.selectPath(sXpathZero);
            i = m_xc.getSelectionCount();
            fail("Mod by 0");
        }catch (Exception e){}
        assertEquals(0,i);
    }

    //Equality Expressions
    /**
     * = Like (equal) price=9.80 true (if price is 9.80)
     */
    public void testEqual() throws Exception {
        String sXml="<foo><bar>" +
                "<price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">2</price></bar><bar>" +
                "<price>5.00</price></bar></foo>";
        m_xc=XmlObject.Factory.parse(sXml).newCursor();
        String sXpath=getQuery("testEqual",0);
        String sExpected="<bar><price>5.00</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    //Existential semantics of equality in a node set
    //check this--not sure how to create this test
    public void testEqualityNodeset() throws Exception {
        String sXpath=getQuery("testEqualityNodeset",0);
        String sExpected="<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }
    /**
     * != Not like (not equal) price!=9.80 false
     */
    public void testNotEqual() throws Exception {
        assertEquals(0,m_xc.getSelectionCount());
        String sXpath=getQuery("testNotEqual",0); //has to be double-comparison
        String sExpected="<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        System.out.println(m_xc.xmlText());
        assertEquals(sExpected,m_xc.xmlText());
    }

    //Relational Expressions
    /**
     * < Less than price<9.80 false (if price is 9.80)
     */
    public void testLessThan() throws Exception {
        String sXpath=getQuery("testLessThan",0);
        m_xc.selectPath(sXpath);
        assertEquals(0,m_xc.getSelectionCount());
    }
    /**
     * <= Less or equal price<=9.80 true
     */
    public void testLessOrEqual() throws Exception {
        String sXpath=getQuery("testLessOrEqual",0);
        String sExpected="<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }
    /**
     * > Greater than price>9.80 false
     */
    public void testGreaterThan() throws Exception {
        String sXpath=getQuery("testGreaterThan",0);
        String sExpected="<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }
    /**
     * >= Greater or equal price>=9.80 true
     */
    public void testGreaterOrEqual() throws Exception {
        String sXpath=getQuery("testGreaterOrEqual",0);
        String sExpected="<bar>" +
                "<price at=\"val0\">3.00</price><price at=\"val1\">2</price>" +
                "</bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    //Boolean Expressions
    /**
     * or or price=9.80 or price=9.70 true (if price is 9.80)
     */
    public void testOr() throws Exception {
        String sXpath=getQuery("testOr",0);
        String sExpected="<price at=\"val1\">2</price>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }
    /**
     * and and  price<=9.80 and price=9.70 false
     */
    public void testAnd() throws Exception {
        String sXpath=getQuery("testAnd",0);
        m_xc.selectPath(sXpath);
        assertEquals(0,m_xc.getSelectionCount());
    }

    public void setUp()throws Exception{
        m_xc=XmlObject.Factory.parse(sXml).newCursor();
    }

}
