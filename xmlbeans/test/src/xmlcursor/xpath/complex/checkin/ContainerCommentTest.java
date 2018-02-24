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
package xmlcursor.xpath.complex.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import junit.framework.TestCase;

/**
 * Date: Apr 12, 2005
 * Time: 1:08:39 PM
 * This class tests the execution of an XQuery where the
 * starting point of the query is a comment
 */
public class ContainerCommentTest extends TestCase
{
    public static void testFunctionPathWithCursor()  throws Exception
           {


               XmlObject employees = XmlObject.Factory.parse(sXml);
               String m_namespaceDeclaration =
                   "declare namespace xq='http://xmlbeans.apache.org/samples/xquery/employees';";

               boolean hasResults = false;
               String[] names;

               XmlCursor cursor = employees.newCursor();
               cursor.toNextToken();

               cursor.selectPath(m_namespaceDeclaration + "$this//xq:employee");
               if (cursor.getSelectionCount() > 0)
               {
                   hasResults = true;
                   cursor.toNextSelection();

                   names = new String[cursor.getSelectionCount()];

                   for (int i = 0; i < cursor.getSelectionCount(); i++)
                   {
                       XmlCursor nameCursor = cursor.newCursor();
                       nameCursor.selectPath(m_namespaceDeclaration +
                               "$this/xq:name/text()");
                       nameCursor.toNextSelection();
                       names[i] = nameCursor.getTextValue();
                       cursor.toNextSelection();
                       System.out.println(names[i]);
                   }
               }
           }

      static String prolog="<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<!-- My Comment -->";
    public static String sXml=prolog+
        "<employees xmlns=\"http://xmlbeans.apache.org/samples/xquery/employees\">\n" +
                "  <employee>\n" +
                "    <name>Fred Jones</name>\n" +
                "    <address location=\"home\">\n" +
                "      <street>900 Aurora Ave.</street>\n" +
                "      <city>Seattle</city>\n" +
                "      <state>WA</state>\n" +
                "      <zip>98115</zip>\n" +
                "    </address>\n" +
                "    <phone location=\"work\">(425)555-5665</phone>\n" +
                "    <phone location=\"home\">(206)555-5555</phone>\n" +
                "    <phone location=\"mobile\">(206)555-4321</phone>\n" +
                "  </employee>\n" +
                "  </employees>";


}
