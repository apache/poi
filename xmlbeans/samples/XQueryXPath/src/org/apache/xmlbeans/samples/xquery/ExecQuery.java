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

/**
 * This class demonstrates how to use the execQuery method to execute XQuery
 * expressions. Compare the code here with the code in the SelectPath class. 
 * That class uses the selectPath method to execute XPath expressions.
 * <p/>
 * You can call the execQuery method from either an XmlObject or XmlCursor 
 * instance. Calling from XmlObject returns an XmlObject array. Calling 
 * from XmlCursor returns a new XmlCursor instance positioned at the root
 * of a fragment containing copies of the XML queried against. Results of the
 * query (if any) are sibling children of the fragment's root.
 */
public class ExecQuery
{
    final static String m_namespaceDeclaration =
                "declare namespace xq='http://xmlbeans.apache.org/samples/xquery/employees';";
    
    /**
     * Uses XQuery to retrieve work <phone> elements from the incoming XML, then
     * changes the number in the results.
     * 
     * This method demonstrates the following characteristics of the execQuery method: 
     * 
     * - it supports XQuery.
     * - the XML it returns is a copy of the XML queried against; contrast this with
     * the selectPath method, which returns a portion of the original document.
     * Changes to returned XML do not impact the XML queried against. 
     * - execQuery called from an XmlCursor returns a cursor positioned at
     * the STARTDOC token of a new XML fragment. Contrast this with the 
     * XmlCursor.selectPath method, which stores results as "selections" in 
     * the cursor used to execute the query.
     * 
     * @param empDoc The incoming XML.
     * @return <code>true</code> if the XPath expression returned results;
     * otherwise, <code>false</code>.
     */
    public boolean updateWorkPhone(XmlObject empDoc)
    {
        boolean hasResults = false;
        
        // A cursor instance to query with.
        XmlCursor empCursor = empDoc.newCursor();

        // The expression: Get the <employee> elements with <state> elements whose
        // value is "WA".
        String queryExpression =
                "for $e in $this/xq:employees/xq:employee " +
                "let $s := $e/xq:address/xq:state " +
                "where $s = 'WA' " +
                "return $e//xq:phone[@location='work']";

        // Execute the query. Results, if any, will be available at 
        // the position of the resultCursor in a new XML document.
        XmlCursor resultCursor = 
            empCursor.execQuery(m_namespaceDeclaration + queryExpression);
        
        System.out.println("The query results, <phone> element copies made " +
        		"from the received document: \n");
        System.out.println(resultCursor.getObject().toString() + "\n");
        
        // If there are results, the results will be children of the fragment root
        // where the new cursor is positioned. This statement tests for children
        // and moves the cursor if to the first if it exists.
        if (resultCursor.toFirstChild())
        {
            hasResults = true;
	        // Use the cursor to loop through the results, printing each sibling
	        // <employee> element returned by the query.
	        int i = 0;
	        do 
	        {
	            // Change the phone numbers.
	            XmlCursor editCursor = resultCursor.newCursor();
	            editCursor.toLastAttribute();
	            editCursor.toNextToken();
	            editCursor.removeXml();
	            editCursor.insertChars("(206)555-1234");
	        } while (resultCursor.toNextSibling());

	        resultCursor.toStartDoc();
	        System.out.println("The query results after changes: \n");
	        System.out.println(resultCursor.getObject().toString() + "\n");

    			System.out.println("The received document -- note that it is unchanged. " +
            		"Changes were made to the copy created by the execQuery method. \n");
    			System.out.println(empDoc + "\n");
        }
        return hasResults;
    }

    /**
     * Uses XQuery to retrieve work <zip> elements from the incoming XML, adding the
     * elements as children to a <zip-list> element.
     * 
     * This method demonstrates the following characteristics of the execQuery method: 
     * 
     * - it supports XQuery.
     * - execQuery called from an XmlObject returns an array of XmlObject instances.
     * These are bound to copies of the received XML.
     * 
     * @param empDoc The incoming XML.
     * @return <code>true</code> if the XPath expression returned results;
     * otherwise, <code>false</code>.
     */
    public boolean collectZips(XmlObject empDoc)
    {
        // The query is designed to return results, so return
        // true if it does.
        boolean hasResults = false;

        // The expression: Get the <zip> elements and return them as children 
        // of a new <zip-list> element.
        String queryExpression =
                "let $e := $this/xq:employees " +
                "return " +
                "<zip-list> " +
                    "{for $z in $e/xq:employee/xq:address/xq:zip " +
                    "return $z} " +
                "</zip-list>";

        // Execute the query. Results will be copies of the XML queried against,
        // stored as members of an XmlObject array.
        XmlObject[] results = 
            empDoc.execQuery(m_namespaceDeclaration + queryExpression);

        // Print the results.
        if (results.length > 0)
        {
            hasResults = true;
            System.out.println("The query results: \n");
            System.out.println(results[0].toString() + "\n");
        }
        return hasResults;
    }
}
