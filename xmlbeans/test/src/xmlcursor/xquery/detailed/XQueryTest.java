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
package xmlcursor.xquery.detailed;

import junit.framework.TestCase;
import tools.util.JarUtil;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlLong;

import java.io.IOException;
import java.io.InputStream;

import test.xbean.xmlcursor.xQueryInput.EmpT;
import xmlcursor.common.Common;

/**
 *
 */
public class XQueryTest extends TestCase {


    public void testSimple() throws XmlException,
            IOException {
        String xq = "for $e in //employee where $e/name='Bob' return  $e ";
        String xq1 = "for $e in //employee return  $e ";
        InputStream input = JarUtil.getResourceFromJarasStream("xbean/xmlcursor/XQueryInput.xml");

        XmlObject o = XmlObject.Factory.parse(input);
        XmlCursor c = o.newCursor();
        XmlCursor c1 = c.execQuery(xq);
        c1.toFirstContentToken();
        assertEquals("<employee>\n" +
                "\t\t<name>Bob</name>\n" +
                "\t\t<ssn>1000</ssn>\n" +
                "\t</employee>", c1.xmlText());

        XmlObject[] res = o.execQuery(xq);
        EmpT employeeType = (EmpT) res[0];
        assertEquals("Bob", employeeType.getName());

    }

    public void testObjConstruction()
            throws XmlException,
            IOException {
        String query = JarUtil.getResourceFromJar("xbean/xmlcursor/xquery/Constructor.xq");
        InputStream input = JarUtil.getResourceFromJarasStream("xbean/xmlcursor/XQueryInput.xml");
        XmlObject o = XmlObject.Factory.parse(input);
        //via Object
        XmlObject[] reslt = o.execQuery(query);
        assertEquals(3, reslt.length);
        assertEquals("<person><name>Bob</name></person>",
                reslt[0].xmlText());
        assertEquals("<person><name>Beth</name></person>",
                reslt[1].xmlText());
        assertEquals("<person><name>NotBob</name></person>",
                reslt[2].xmlText());
        XmlCursor c = o.newCursor();

        //via Cursor
        /*XmlCursor c1=c.execQuery(query);
         c1.toFirstContentToken();
        assertEquals("<person><name>Bob</name></person>",
                      c1.xmlText() );
          c1.toNextSibling();
        assertEquals("<person><name>Beth</name></person>",
                      c1.xmlText() );
        c1.dispose();
        c.dispose();
        */
        int i=0;
        while ( i++ < 2){
        //via Cursor--new
        XmlCursor c1 = c.execQuery(query);
        //c.dispose();
        assertEquals(XmlCursor.TokenType.STARTDOC, c1.currentTokenType());
        assertEquals(XmlCursor.TokenType.START, c1.toNextToken() );
              assertEquals("<person><name>Bob</name></person>",
                c1.xmlText());
       // assertTrue(c1.toNextSelection());
        assertTrue( c1.toNextSibling() );
        assertEquals("<person><name>Beth</name></person>",
                c1.xmlText());
        //assertTrue(c1.toNextSelection());
          assertTrue( c1.toNextSibling() );
              assertEquals("<person><name>NotBob</name></person>",
                c1.xmlText());
        c1.dispose();
        }
        c.dispose();
    }


    public void testJoin()
            throws XmlException,
            IOException {
        String query = JarUtil.getResourceFromJar("xbean/xmlcursor/xquery/Join.xq");
        InputStream input = JarUtil.getResourceFromJarasStream("xbean/xmlcursor/XQueryInput.xml");
        XmlObject o = XmlObject.Factory.parse(input);
        XmlCursor c = o.newCursor();
        XmlCursor c1 = c.execQuery(query);

      //  assertEquals(3, c1.getSelectionCount());
        c1.toFirstContentToken();
        assertEquals("<result>" +
                "<ssn>1000</ssn>,\n" +
                "\t\t<name>Bob</name>,\n" +
                "\t\t<name>NotBob</name>" +
                "</result>",
                c1.xmlText());
/*      assertEquals("<xml-fragment>" +
              "<result>" +
              "<ssn>1000</ssn>,\n" +
              "\t\t<name>Bob</name>,\n" +
              "\t\t<name>NotBob</name>" +
              "</result>" +
              "<result><ssn>1001</ssn>,\n" +
              "\t\t<name>Beth</name>,\n" +
              "\t\t</result>" +
              "<result><ssn>1000</ssn>,\n" +
              "\t\t<name>NotBob</name>,\n" +
              "\t\t<name>Bob</name>" +
              "</result>" +
              "</xml-fragment>",
                    c1.xmlText() ); */
        c1.dispose();
        c.dispose();

        XmlObject[] res = o.execQuery(query);
        assertEquals(3, res.length);
    }

    public void testTextSequenceRoot()
            throws XmlException,
            IOException {
        //String query = "$this//text()";
        String query = ".//text()";
        InputStream input = JarUtil.getResourceFromJarasStream("xbean/xmlcursor/XQueryInput.xml");
        XmlObject o = XmlObject.Factory.parse(input);
        XmlObject[] res = o.execQuery(query);
        assertEquals(19, res.length);

        XmlCursor c = o.newCursor();
        XmlCursor c1 = c.execQuery(query);
           assertEquals(XmlCursor.TokenType.TEXT,c1.toNextToken());
        //  assertEquals()
        //assertEquals(19, c1.getSelectionCount());
        c.dispose();//make sure this doesn't screw things up
        while (c1.toNextSibling())
            assertEquals(XmlCursor.TokenType.TEXT, c1.currentTokenType());
        c1.toStartDoc();
        assertEquals("<xml-fragment>Bob</xml-fragment>",
                c1.xmlText());
        c1.dispose();
        c.dispose();
    }


    public void testDocumentFunc()
            throws XmlException,
            IOException {
        //String query = "<result>{$this},{count(//employee)}</result>";
        String query = "<result>{.},{count(//employee)}</result>";
        InputStream input = JarUtil.getResourceFromJarasStream("xbean/xmlcursor/XQueryInput.xml");
        XmlCursor c = XmlObject.Factory.parse(input).newCursor();
        XmlCursor c1 = c.execQuery(query);
        assertEquals("",
                c1.xmlText());
        c1.dispose();
        c.dispose();
    }


    public void testTextAtOddPlaces() throws Exception {
        //String query = "<result>{$this},{count(//employee)}</result>";
        String query = "<result>{.},{count(//employee)}</result>";
        String input = "<foo><a><b>text</b>more text</a></foo>";
        XmlObject o = XmlObject.Factory.parse(input);
        XmlCursor c = o.newCursor();

        XmlObject[] res = o.execQuery("//a");
        assertEquals("<a><b>text</b>more text</a>", res[0].xmlText());

        XmlCursor cur = c.execQuery("//a");
       // assertEquals(1, cur.getSelectionCount());
        cur.toFirstContentToken();
        assertEquals("<a><b>text</b>more text</a>", cur.xmlText());


    }


    public void testMultiDocJoin()
            throws XmlException,
            IOException {
        String query = JarUtil.getResourceFromJar("xbean/xmlcursor/xquery/2DocJoin.xq");
        InputStream input = JarUtil.getResourceFromJarasStream("xbean/xmlcursor/XQueryInput.xml");
        XmlCursor c = XmlObject.Factory.parse(input).newCursor();
        XmlCursor c1 = c.execQuery(query);
        assertEquals("",
                c1.xmlText());
        c1.dispose();
        c.dispose();
    }

    public void testFunction() throws Exception {
        String query = " declare function local:summary($emps as element(employee)*) \n" +
                "   as element(dept)*\n" +
                "{\n" +
                "   for $d in fn:distinct-values($emps/deptno)\n" +
                "   let $e := $emps[deptno = $d]\n" +
                "   return\n" +
                "      <dept>\n" +
                "         <deptno>{$d}</deptno>\n" +
                "         <headcount> {fn:count($e)} </headcount>\n" +
                "         <payroll> {fn:sum($e/salary)} </payroll>\n" +
                "      </dept>\n" +
                "};\n" +
                "\n" +
                //"local:summary($this//employee[location = \"Denver\"])";
                "local:summary(.//employee[location = \"Denver\"])";

        String xml = " <list>" +
                "<employee>" +
                "<location>Denver</location>" +
                "<deptno>7</deptno>" +
                "<salary>20</salary>" +
                "</employee>" +
                "<employee>" +
                "<location>Seattle</location>" +
                "<deptno>6</deptno>" +
                "<salary>30</salary>" +
                "</employee>" +
                "<employee>" +
                "<location>Denver</location>" +
                "<deptno>5</deptno>" +
                "<salary>40</salary>" +
                "</employee>" +
                "<employee>" +
                "<location>Denver</location>" +
                "<deptno>7</deptno>" +
                "<salary>10</salary>" +
                "</employee>" +
                "</list>";

        XmlObject o = XmlObject.Factory.parse(xml);
        XmlObject[] res = o.execQuery(query);
        assertEquals(2, res.length);
        assertEquals("<dept><deptno>7</deptno><headcount>2</headcount><payroll>30</payroll></dept>",
                res[0].xmlText());
        assertEquals("<dept><deptno>5</deptno><headcount>1</headcount><payroll>40</payroll></dept>",
                res[1].xmlText());
        XmlCursor c = o.newCursor();
        XmlCursor c1 = c.execQuery(query);
        c1.toFirstContentToken();
        assertEquals(res[0].xmlText(),
                c1.xmlText());
        c1.dispose();
        c.dispose();
    }

    public void testType() throws Exception {
        String xml = "<a><b></b><b></b></a>";
        String query = "count(//b)";
        XmlObject o = XmlObject.Factory.parse(xml);
        XmlObject[] res = o.execQuery(query);
        XmlLong result = (XmlLong) res[0];
        assertEquals("2",result.getStringValue());
        assertEquals(2,result.getLongValue());

    }


    public void testQueryComment() throws Exception{
      String xml = "<a><b></b><b></b></a>";
        String query = "(:comment:) count(//b)";
        XmlObject o = XmlObject.Factory.parse(xml);
        XmlObject[] res = o.execQuery(query);
        XmlLong result = (XmlLong) res[0];
        assertEquals("2",result.getStringValue());
        assertEquals(2,result.getLongValue());
     }


    public void testStandaloneFunction() throws Exception{
        String query = "<results>\n" +
            "       {fn:not(xs:unsignedShort(\"65535\"))}\n" +
            "      </results>";
        XmlObject o = XmlObject.Factory.newInstance();
        XmlObject[] res = o.execQuery(query);
        XmlLong result = (XmlLong) res[0];
        assertEquals("2",result.getStringValue());
        assertEquals(2,result.getLongValue());

    }

}
