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

package org.apache.xmlbeans.samples.xquery;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.samples.xquery.employees.PhoneType;

/**
 * This class demonstrates how to use the selectPath method to execute XPath
 * expressions. Compare the code here with the code in the ExecQuery class. 
 * That class uses the execQuery method to execute XQuery expressions.
 * <p/>
 * You can call the selectPath method from either an XmlObject or XmlCursor 
 * instance. Calling from XmlObject returns an XmlObject array. Calling 
 * from XmlCursor returns void, and you use methods of the cursor to 
 * navigate among returned "selections".
 */
public class SelectPath
{
    // Declare a namespace corresponding to the namespace declared in the XML
    // instance. The string here will be used as part of the XPath expression to
    // ensure that the query finds namespace-qualified elements in the XML.
    final static String m_namespaceDeclaration = 
        "declare namespace xq='http://xmlbeans.apache.org/samples/xquery/employees';";

    /**
     * Prints the XML bound to <em>empDoc</em>, uses XPath to 
     * retrieve elements containing work phone numbers, changes the numbers 
     * to another number, then prints the XML again to display the changes.
     * 
     * This method demonstrates the following characteristics of the selectPath method: 
     * 
     * - it supports expressions that include predicates 
     * - the XML it returns is the XML queried against -- not a copy, as with results
     * returned via execQuery methods and XQuery. Changes to this XML update 
     * the XML queried against. 
     * - selectPath called from an XMLBean type (instead of a cursor) returns an 
     * array of results (if any). These results can be cast to a matching type 
     * generated from schema.
     * 
     * @param empDoc The incoming XML.
     * @return <code>true</code> if the XPath expression returned results;
     * otherwise, <code>false</code>.
     */
    public boolean updateWorkPhone(XmlObject empDoc)
    {
        boolean hasResults = false;

        // Print the XML received.
        System.out.println("XML as received by updateWorkPhone method: \n\n"
                + empDoc.toString());
 
        // Create a variable with the query expression.
        String pathExpression = 
            "$this/xq:employees/xq:employee/xq:phone[@location='work']";

        // Execute the query.
        XmlObject[] results = empDoc.selectPath(m_namespaceDeclaration
                + pathExpression);
        if (results.length > 0)
        {
            hasResults = true;

            // <phone> elements returned from the expression will conform to the
            // schema, so bind them to the appropriate XMLBeans type generated
            // from the schema.
            PhoneType[] phones = (PhoneType[]) results;

            // Change all the work phone numbers to the same number.
            for (int i = 0; i < phones.length; i++)
            {
                phones[i].setStringValue("(206)555-1234");
            }
            // Print the XML with updates.
            System.out.println("\nXML as updated by updateWorkPhone method (each work \n" +
                    "phone number has been changed to the same number): \n\n"
                    + empDoc.toString() + "\n");
        }
        return hasResults;
    }

    /**
     * Uses the XPath text() function to get values from <name>
     * elements in received XML, then collects those values as the value of a
     * <names> element created here.
     * <p/>
     * Demonstrates the following characteristics of the selectPath method: 
     * <p/>
     * - It supports expressions that include XPath function calls.
     * - selectPath called from an XmlCursor instance (instead of an XMLBeans 
     * type) places results (if any) into the cursor's selection set.
     * 
     * @param empDoc The incoming XML.
     * @return <code>true</code> if the XPath expression returned results;
     * otherwise, <code>false</code>.
     */
    public boolean collectNames(XmlObject empDoc)
    {
        boolean hasResults = false;

        // Create a cursor with which to execute query expressions. The cursor
        // is inserted at the very beginning of the incoming XML, then moved to
        // the first element's START token.
        XmlCursor pathCursor = empDoc.newCursor();
        pathCursor.toFirstChild();

        // Execute the path expression, qualifying it with the namespace
        // declaration.
        pathCursor.selectPath(m_namespaceDeclaration
                + "$this//xq:employee/xq:name/text()");

        // If there are results, then go ahead and do stuff.
        if (pathCursor.getSelectionCount() > 0)
        {
            hasResults = true;

            // Create a new <names> element into which names from the XML
            // will be copied. Note that this element is in the default
            // namespace; it's not part of the schema.
            XmlObject namesElement = null;
            try
            {
                namesElement = XmlObject.Factory.parse("<names/>");
            } catch (XmlException e)
            {
                e.printStackTrace();
            }

            // Add a cursor the new element and put it between its START and END
            // tokens, where new values can be inserted.
            XmlCursor namesCursor = namesElement.newCursor();
            namesCursor.toFirstContentToken();
            namesCursor.toEndToken();

            // Loop through the selections, appending the incoming <name> element's 
            // value to the new <name> element's value. (Of course, this could have
            // been done with a StringBuffer, but that wouldn't show the cursor in
            // use.)
            while (pathCursor.toNextSelection())
            {
                namesCursor.insertChars(pathCursor.getTextValue());
                if (pathCursor.hasNextSelection())
                {
                    namesCursor.insertChars(", ");
                }
            }
            // Dispose of the cursors now that they're not needed.
            pathCursor.dispose();
            namesCursor.dispose();

            // Print the new element.
            System.out.println("\nNames collected by collectNames method: \n\n"
                    + namesElement + "\n");
        }
        return hasResults;
    }
}
