/*   Copyright 2006 The Apache Software Foundation
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
 *   limitations under the License.
 */
package xmlcursor.xquery.detailed;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;

import common.Common;

/** This class tests the use of XmlOptions in binding XQuery variables */
public class XQueryVariableBindingTest extends Common
{
    public static final String XQUERY_CASE_DIR =
        XBEAN_CASE_ROOT + P + "xmlcursor" + P + "xquery";
    public static File dir = new File(XQUERY_CASE_DIR);

    public XQueryVariableBindingTest(String name)
    {
        super(name);
    }

    private XmlCursor _testDocCursor1() throws Exception
    {
        String xml =
            "<elem1>" +
            "<elem11 id=\"123\">text11</elem11>" +
            "<elem21 id=\"456\">text11</elem21>" +
            "<elem12 idRef=\"123\"/>" +
            "<elem13 idRef=\"456\"/>" +
            "<elem14 idRef=\"123\"/>" +
            "<elem15 idRef=\"456\"/>" +
            "<elem16 idRef=\"123\"/>" +
            "<elem17 idRef=\"789\"/>" +
            "</elem1>";
        XmlObject doc = XmlObject.Factory.parse(xml);
        XmlCursor xc = doc.newCursor();
        return xc;
    }

    private void _verifySelection(XmlCursor xc)
    {
        assertEquals(3, xc.getSelectionCount());
        assertTrue(xc.toNextSelection());
        assertEquals("<elem12 idRef=\"123\"/>", xc.xmlText());
        assertTrue(xc.toNextSelection());
        assertEquals("<elem14 idRef=\"123\"/>", xc.xmlText());
        assertTrue(xc.toNextSelection());
        assertEquals("<elem16 idRef=\"123\"/>", xc.xmlText());
    }

    /** test the automatic binding of $this to the current node: selectPath() */
    public void testThisVariable1() throws Exception
    {
        XmlCursor xc = _testDocCursor1();
        xc.toFirstChild(); //<elem1>
        xc.toFirstChild(); //<elem11>
        xc.selectPath("//*[@idRef=$this/@id]");
        _verifySelection(xc);
        xc.clearSelections();
        xc.dispose();
    }

    // this fails: see JIRA issue XMLBEANS-276
    /** test the binding of a variable to the current node: selectPath() */
    public void testCurrentNodeVariable1() throws Exception
    {
        XmlCursor xc = _testDocCursor1();
        xc.toFirstChild();
        xc.toFirstChild();
        XmlOptions opts = new XmlOptions();
        opts.setXqueryCurrentNodeVar("cur");
        //String varDecl = "declare variable $cur external; ";
        //xc.selectPath(varDecl + "//*[@idRef=$cur/@id]", opts);
        xc.selectPath("//*[@idRef=$cur/@id]", opts);
        _verifySelection(xc);
        xc.clearSelections();
        xc.dispose();
    }

    private XmlCursor _testDocCursor2() throws Exception
    {
        File f = new File(dir, "employees.xml");
        XmlObject doc = XmlObject.Factory.parse(f);
        XmlCursor xc = doc.newCursor();
        return xc;
    }

    public void _verifyQueryResult(XmlCursor qc)
    {
        System.out.println(qc.xmlText());
        assertTrue(qc.toFirstChild());
        assertEquals("<phone location=\"work\">(425)555-5665</phone>", 
                     qc.xmlText());
        assertTrue(qc.toNextSibling());
        assertEquals("<phone location=\"work\">(425)555-6897</phone>", 
                     qc.xmlText());
        assertFalse(qc.toNextSibling());
    }

    /** test the automatic binding of $this to the current node: execQuery() */
    public void testThisVariable2() throws Exception
    {
        XmlCursor xc = _testDocCursor2();
        xc.toNextToken();
        String q =
            "for $e in $this/employees/employee " +
            "let $s := $e/address/state " +
            "where $s = 'WA' " +
            "return $e//phone[@location='work']";
        XmlCursor qc = xc.execQuery(q);
        _verifyQueryResult(qc);
        xc.dispose();
        qc.dispose();
    }

    /** test the binding of a variable to the current node: execQuery() */
    public void testCurrentNodeVariable2() throws Exception
    {
        XmlCursor xc = _testDocCursor2();
        xc.toNextToken();
        String q =
            "for $e in $cur/employees/employee " +
            "let $s := $e/address/state " +
            "where $s = 'WA' " +
            "return $e//phone[@location='work']";
        XmlOptions opts = new XmlOptions();
        opts.setXqueryCurrentNodeVar("cur");
        //String varDecl = "declare variable $cur external; ";
        //XmlCursor qc = xc.execQuery(varDecl + q, opts);
        XmlCursor qc = xc.execQuery(q, opts);
        _verifyQueryResult(qc);
        xc.dispose();
        qc.dispose();
    }

    private XmlObject[] _execute(XmlObject xo, Map m, String q)
    {
        XmlOptions opts = new XmlOptions();
        opts.setXqueryVariables(m);
        XmlObject[] results = xo.execQuery(q, opts);
        return results;
    }

    /** test the binding of a variable to an XmlTokenSource using a map */
    public void testOneVariable() throws Exception
    {
        File f = new File(dir, "bookstore.xml");
        XmlObject doc = XmlObject.Factory.parse(f);
        String q =
            "declare variable $rt external; " +
            "for $x in $rt/book " +
            "where $x/price > 30 " +
            "return $x/title";
        Map m = new HashMap();
        m.put("rt", doc.selectChildren("", "bookstore")[0]);
        XmlObject[] results = _execute(doc, m, q);
        assertNotNull(results);
        assertEquals(2, results.length);
        assertEquals("<title lang=\"en\">XQuery Kick Start</title>",
                     results[0].xmlText());
        assertEquals("<title lang=\"en\">Learning XML</title>",
                     results[1].xmlText());
    }
    
    /** test the binding of multiple variables using a map;
        at the same time, test the binding of a variable to a String
     */
    public void testMultipleVariables() throws Exception
    {
        File f = new File(dir, "bookstore.xml");
        XmlObject doc = XmlObject.Factory.parse(f);
        String q =
            "declare variable $rt external; " +
            "declare variable $c external; " +
            "for $x in $rt/book " +
            "where $x[@category=$c] " +
            "return $x/title";
        Map m = new HashMap();
        m.put("rt", doc.selectChildren("", "bookstore")[0]);
        m.put("c", "CHILDREN");
        XmlObject[] results = _execute(doc, m, q);
        assertNotNull(results);
        assertEquals(1, results.length);
        assertEquals("<title lang=\"en\">Harry Potter</title>",
                     results[0].xmlText());
    }

}
