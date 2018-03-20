/*   Copyright 2005 The Apache Software Foundation
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
package xmlcursor.detailed;

import java.io.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlOptions;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import common.Common;

public class XmlLineNumberTest extends Common
{
    public static final String xml = 
        "<people><person born=\"1912\" died=\"1954\" id=\"p342\">\n" + 
        "    <name>\n" + 
        "\t\t<first_name>Alan</first_name>\n" + 
        "\t\t<last_name>Turing</last_name>\n" + 
        "</name>\n" + 
        "</person></people>";
 
    public static final String xmlFile = 
        XBEAN_CASE_ROOT + P + "xmlcursor" + P + "Employees.xml";

    public XmlLineNumberTest(String name)
    {
        super(name);
    }

    /** test obtaining XmlLineNumber bookmark with option
        XmlOptions.setLoadLineNumbers() */
    public void testGetBookmark1() throws Exception
    {
        File f = new File(xmlFile);
        XmlOptions opt = new XmlOptions();
        opt.setLoadLineNumbers();
        XmlObject xo = XmlObject.Factory.parse(f, opt);
        XmlCursor c = xo.newCursor();
        c.toFirstChild();
        assertEquals(XmlCursor.TokenType.START, c.currentTokenType());
        XmlLineNumber ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertTrue(ln != null);
        assertEquals(1, ln.getLine());
        c.toFirstChild();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(2, ln.getLine());
        c.toEndToken();
        assertEquals(XmlCursor.TokenType.END, c.currentTokenType());
        ln =(XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        // no bookmark at END
        assertEquals(null, ln);
    }

    /** test obtaining XmlLineNumber bookmark with option
        XmlOptions.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT)
    */
    public void testGetBookmark2() throws Exception
    {
        File f = new File(xmlFile);
        XmlOptions opt = new XmlOptions();
        opt.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
        XmlObject xo = XmlObject.Factory.parse(f, opt);
        XmlCursor c = xo.newCursor();
        c.toFirstChild();
        assertEquals(XmlCursor.TokenType.START, c.currentTokenType());
        XmlLineNumber ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertTrue(ln != null);
        assertEquals(1, ln.getLine());
        c.toFirstChild();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(2, ln.getLine());
        c.toEndToken();
        assertEquals(XmlCursor.TokenType.END, c.currentTokenType());
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        // there is a bookmark at END
        assertTrue(ln != null);
        assertEquals(19, ln.getLine());
    }

    /** test using XmlLineNumber to get line number, column, and offset
        - parsing xml from string */
    public void testLineNumber1() throws Exception
    {
        XmlOptions opt = new XmlOptions().setLoadLineNumbers();
        XmlObject xo = XmlObject.Factory.parse(xml, opt);
        XmlCursor c = xo.newCursor();
        c.toFirstContentToken();
        c.toFirstChild();
        XmlLineNumber ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(1, ln.getLine());
        //assertEquals(8, ln.getColumn()); // actual: 10
        assertTrue(8 <= ln.getColumn() && ln.getColumn() <= 10);
        // offset is not implemented
        assertEquals(-1, ln.getOffset());
        c.toFirstChild();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(2, ln.getLine());
        //assertEquals(4, ln.getColumn()); // actual: 6
        assertTrue(4 <= ln.getColumn() && ln.getColumn() <= 6);
        c.toFirstChild();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(3, ln.getLine());
        // tabs count as having single column width
        //assertEquals(2, ln.getColumn()); // actual: 4
        assertTrue(2 <= ln.getColumn() && ln.getColumn() <= 4);
    }

    /** test using XmlLineNumber to get line number, column, and offset
        - parsing xml from file */
    public void testLineNumber2() throws Exception
    {
        File f = new File(xmlFile);
        XmlOptions opt = new XmlOptions();
        opt.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
        XmlObject xo = XmlObject.Factory.parse(f, opt);
        XmlCursor c = xo.newCursor();
        c.toFirstContentToken();
        c.toFirstChild();
        XmlLineNumber ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(2, ln.getLine());
        assertTrue(2 <= ln.getColumn() && ln.getColumn() <= 4);
        assertEquals(-1, ln.getOffset());
        c.toFirstChild();
        c.push();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(3, ln.getLine());
        assertTrue(4 <= ln.getColumn() && ln.getColumn() <= 6);
        c.toEndToken();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(3, ln.getLine());
        assertTrue(23 <= ln.getColumn() && ln.getColumn() <= 25);
        c.pop();
        c.toNextSibling(); //address
        c.toEndToken();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(9, ln.getLine());
        assertTrue(4 <= ln.getColumn() && ln.getColumn() <= 6);
        assertEquals(-1, ln.getOffset());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(XmlLineNumberTest.class);
        return suite;
    }

    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }
}
