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

package xmlcursor.xpath.complex.detailed;

import xmlcursor.common.Common;
import xmlcursor.xpath.common.XPathExpressionTest;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

/**
 * Verifies XPath with Expressions
 * http://www.w3schools.com/xpath/xpath_expressions.asp
 */
public class XPathExpressionTestImpl
    extends XPathExpressionTest
{
    public XPathExpressionTestImpl(String name)
    {
        super(name);
    }

    public String getQuery(String testName, int testCase)
        throws IllegalArgumentException
    {
        Object queries;

        if ((queries = testMap.get(testName)) == null)
            throw new IllegalArgumentException("No queries for test" +
                testName);
        else if (((String[]) queries).length <= testCase)
            throw new IllegalArgumentException("No query " + testCase +
                " for test" + testName);
        else
            return ((String[]) queries)[testCase];

    }

    private void verifySelection(XmlCursor c, String[] expected)
    {
        int count = c.getSelectionCount();
        assertEquals(expected.length, count);
        for (int i = 0; i < count; i++)
        {
            c.toNextSelection();
            assertEquals(expected[i], c.xmlText());
        }
    }

    public void testForExpression()
        throws Exception
    {
        String sXml = "<bib>\n" +
            "  <book>\n" +
            "    <title>TCP/IP Illustrated</title>\n" +
            "    <author>Stevens</author>\n" +
            "    <publisher>Addison-Wesley</publisher>\n" +
            "  </book>\n" +
            "  <book>\n" +
            "    <title>Advanced Programming in the Unix environment</title>\n" +
            "    <author>Stevens</author>\n" +
            "    <publisher>Addison-Wesley</publisher>\n" +
            "  </book>\n" +
            "  <book>\n" +
            "    <title>Data on the Web</title>\n" +
            "    <author>Abiteboul</author>\n" +
            "    <author>Buneman</author>\n" +
            "    <author>Suciu</author>\n" +
            "  </book>\n" +
            "</bib>";

        String query = "for $a in distinct-values(//author) " +
            "return ($a," +
            "        for $b in //book[author = $a]" +
            "        return $b/title)";
        String[] exp = new String[] {
            "<xml-fragment>Stevens</xml-fragment>",
            "<title>TCP/IP Illustrated</title>",
            "<title>Advanced Programming in the Unix environment</title>",
            "<xml-fragment>Abiteboul</xml-fragment>",
            "<title>Data on the Web</title>",
            "<xml-fragment>Buneman</xml-fragment>",
            "<title>Data on the Web</title>",
            "<xml-fragment>Suciu</xml-fragment>",
            "<title>Data on the Web</title>"
            };
        XmlCursor c = XmlObject.Factory.parse(sXml).newCursor();
        c.selectPath(query);
        verifySelection(c, exp);
    }

    public void testFor_1()
        throws Exception
    {
        XmlCursor c = XmlObject.Factory.parse("<a/>").newCursor();
        String query = "for $i in (10, 20),\n" +
            "    $j in (1, 2)\n" +
            "return ($i + $j)";
        c.selectPath(query);
        String[] expected = new String[] {
            Common.wrapInXmlFrag("11"),
            Common.wrapInXmlFrag("12"),
            Common.wrapInXmlFrag("21"),
            Common.wrapInXmlFrag("22")
        };
        verifySelection(c, expected);
    }

    public void testFor_2()
        throws Exception
    {
        XmlCursor c = XmlObject.Factory.parse("<a/>").newCursor();
        String query = "sum (for $i in (10, 20)" +
            "return $i)";
        c.selectPath(query);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("30"), c.xmlText());
    }

    public void testIf()
        throws Exception
    {
        XmlCursor c = XmlObject.Factory.parse("<root>" +
            "<book price='20'>Pooh</book>" +
            "<cd price='25'>Pooh</cd>" +
            "<book price='50'>Maid</book>" +
            "<cd price='25'>Maid</cd>" +
            "</root>").newCursor();
        String query = "if (//book[1]/@price) " +
            "  then //book[1] " +
            "  else 0";
        c.selectPath(query);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals("<book price=\"20\">Pooh</book>", c.xmlText());

        query = "for $b1 in //book, $b2 in //cd " +
            "return " +
            "if ( $b1/@price < $b2/@price )" +
            " then $b1" +
            " else $b2";
        c.selectPath(query);
        assertEquals(4, c.getSelectionCount());
        c.toNextSelection();
        assertEquals("<book price=\"20\">Pooh</book>", c.xmlText());
        c.toNextSelection();
        assertEquals("<book price=\"20\">Pooh</book>", c.xmlText());
        c.toNextSelection();
        assertEquals("<cd price=\"25\">Pooh</cd>", c.xmlText());
        c.toNextSelection();
        assertEquals("<cd price=\"25\">Maid</cd>", c.xmlText());
    }

    public void testQuantifiedExpression()
        throws Exception
    {
        XmlCursor c = XmlObject.Factory.parse("<root></root>").newCursor();
        String query =
            "some $x in (1, 2, 3), $y in (2, 3, 4) " +
            "satisfies $x + $y = 4";
        c.selectPath(query);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals("<xml-fragment>true</xml-fragment>", c.xmlText());
    }

}
