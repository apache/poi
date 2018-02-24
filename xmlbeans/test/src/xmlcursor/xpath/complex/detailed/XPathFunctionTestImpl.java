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

import xmlcursor.xpath.common.XPathFunctionTest;

import org.apache.xmlbeans.XmlObject;

/**
 * Queries here overwrite whatever is loaded in the query map if
 * the syntax is different
 */

public class XPathFunctionTestImpl
    extends XPathFunctionTest
{
    public XPathFunctionTestImpl(String name)
    {
        super(name);

        testMap.put("testFunctionCount", new String[]{
            "count(//cd)",
            "//cd[position()=2]"});

        testMap.put("testFunctionLocalName", new String[]{
            "//*[local-name(.)='bar']"});

        testMap.put("testFunctionConcat", new String[]{
            "//bar/*[name(.)=concat(\"pr\",\"ice\")]"});

        testMap.put("testFunctionString", new String[]{
            "/foo/*[name(.)=" +
            "concat(\"bar\",string(./foo/bar/price[last()]))]"});

        testMap.put("testFunctionStringLength", new String[]{
            "//bar/*[string-length(name(.))=5]"});

        testMap.put("testFunctionSubString", new String[]{
            "//bar/*[substring(name(.),3,3)=\"ice\"]"});

        testMap.put("testFunctionSubStringAfter", new String[]{
            "//bar/*[substring-after(" +
            "name(.),'pr'" +
            ")=\"ice\"]"});

        testMap.put("testFunctionSubStringBefore", new String[]{
            "//bar/*[substring-before(" +
            "name(.),'ice'" +
            ")=\"pr\"]"});

        testMap.put("testFunctionTranslate", new String[]{
            "//bar/*[translate(name(.)," +
            "'ice','pr')=\"prpr\"]"});

        testMap.put("testFunctionLang", new String[]{
            "//price[lang(\"en\")=true()]",
            "//foo[lang(\"en\")=true()]"});

        testMap.put("testFunctionTrue", new String[]{
            "//*[boolean(@at)=true()]"});
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

    public void testErrorMessages()
        throws Exception
    {
        //do nothing for Jaxen
    }

    //ensure Jaxen is not in the classpath
    public void testAntiJaxenTest()
    {
        try
        {
            m_xc.selectPath("//*");
            fail("XQRL shouldn't handle absolute paths");
        }
        catch (Throwable t)
        {
        }
    }

    public void testExternalVariable()
        throws Exception
    {

    }

    public void testExternalFunction()
        throws Exception
    {
        String query = "" +
            "declare function local:toc($book-or-section as element()) as element()*;" +
            " local:toc($book-or-section/section)";
        String input =
            "<book>\n" +
            "  <title>Data on the Web</title>\n" +
            "  <author>Serge Abiteboul</author>\n" +
            "  <author>Peter Buneman</author>\n" +
            "  <author>Dan Suciu</author>\n" +
            "  <section id=\"intro\" difficulty=\"easy\" >\n" +
            "    <title>Introduction</title>\n" +
            "    <p>Text ... </p>\n" +
            "    <section>\n" +
            "      <title>Audience</title>\n" +
            "      <p>Text ... </p>\n" +
            "    </section>\n" +
            "    <section>\n" +
            "      <title>Web Data and the Two Cultures</title>\n" +
            "      <p>Text ... </p>\n" +
            "      <figure height=\"400\" width=\"400\">\n" +
            "        <title>Traditional client/server architecture</title>\n" +
            "        <image source=\"csarch.gif\"/>\n" +
            "      </figure>\n" +
            "      <p>Text ... </p>\n" +
            "    </section>\n" +
            "  </section>\n" +
            "  <section id=\"syntax\" difficulty=\"medium\" >\n" +
            "    <title>A Syntax For Data</title>\n" +
            "    <p>Text ... </p>\n" +
            "    <figure height=\"200\" width=\"500\">\n" +
            "      <title>Graph representations of structures</title>\n" +
            "      <image source=\"graphs.gif\"/>\n" +
            "    </figure>\n" +
            "    <p>Text ... </p>\n" +
            "    <section>\n" +
            "      <title>Base Types</title>\n" +
            "      <p>Text ... </p>\n" +
            "    </section>\n" +
            "    <section>\n" +
            "      <title>Representing Relational Databases</title>\n" +
            "      <p>Text ... </p>\n" +
            "      <figure height=\"250\" width=\"400\">\n" +
            "        <title>Examples of Relations</title>\n" +
            "        <image source=\"relations.gif\"/>\n" +
            "      </figure>\n" +
            "    </section>\n" +
            "    <section>\n" +
            "      <title>Representing Object Databases</title>\n" +
            "      <p>Text ... </p>\n" +
            "    </section>       \n" +
            "  </section>\n" +
            "</book>";
        XmlObject o = XmlObject.Factory.parse(input);
        XmlObject[] res = o.selectPath(query);
        assertEquals(1, res.length);
        assertEquals("", res[0].xmlText());
    }

}
