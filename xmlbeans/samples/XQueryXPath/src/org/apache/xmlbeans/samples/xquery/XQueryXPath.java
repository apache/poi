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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import java.io.File;
import java.io.IOException;

/**
 * A sample to XMLBeans API features for executing XPath and XQuery
 * expressions. The sample illustrates these features:
 * 
 * - Using the XmlObject.selectPath and XmlCursor.selectPath methods
 * to execute XPath expressions. The selectPath method's results (if
 * any) are always chunks of the instance queried against. In other
 * words, changes to query results change the original instance.
 * However, you work with results differently depending on whether
 * selectPath was called from an XmlObject or XmlCursor instance. See
 * the SelectPath class for more information.
 * - Using the XmlObject.execQuery and XmlCursor.execQuery methods
 * to execute XQuery expressions. Results of these queries are copied
 * into new XML, meaning that changes to results do not change the 
 * original instance. Here again, you work with results differently
 * depending how which method you used to query. See the ExecQuery
 * class for more information.
 */
public class XQueryXPath
{
    /**
     * Receives an employees list XML instance, passing the instance to
     * methods that execute queries against it.
     * 
     * @param args An array in which the first item is a
     * path to the XML instance file.
     */
    public static void main(String[] args)
            throws org.apache.xmlbeans.XmlException, java.io.IOException
    {
        XQueryXPath sample = new XQueryXPath();
        sample.executeQueries(args);
    }
    
    /**
     * Returns <code>true</code> if all of the sample methods returned true
     * (i.e., their query expressions returned results).
     * 
     * @param args An array in which the first item is a
     * path to the XML instance file.
     * @return <code>true</code> if all of the sample methods returned true
     * (i.e., their query expressions returned results); otherwise, 
     * <code>false</code>.
     */
    public boolean executeQueries(String[] args)
    {
        XmlObject xml = this.parseXml(args[0]);
        
        // Execute the XQuery samples.
        ExecQuery xquerySample = new ExecQuery();
        
        System.out.println("Running ExecQuery.selectEmpsByStateCursor\n");
        boolean xqWorkPhoneSuccessful = xquerySample.updateWorkPhone(xml);
        
        System.out.println("Running ExecQuery.selectZipsNewDocCursor\n");
        boolean xqCollectZips = xquerySample.collectZips(xml);

        // Execute the XPath samples.
        SelectPath xpathSample = new SelectPath();
        
        System.out.println("Running SelectPath.updateWorkPhone \n");
        boolean xpWorkPhoneSuccessful = xpathSample.updateWorkPhone(xml);
        
        System.out.println("Running SelectPath.collectNames \n");
        boolean xpCollectNames = xpathSample.collectNames(xml);
        
        return (xqWorkPhoneSuccessful && xqCollectZips
                && xpWorkPhoneSuccessful && xpCollectNames) ? true : false;
    }

    /**
     * <p>Creates a File from the XML path provided in main arguments, then
     * parses the file's contents into a type generated from schema.</p>

     * @param xmlFilePath A path to XML based on the schema in inventory.xsd.
     * @return An instance of a generated schema type that contains the parsed
     *         XML.
     */
    public XmlObject parseXml(String xmlFilePath)
    {
        File xmlFile = new File(xmlFilePath);
        XmlObject xml = null;
        try
        {
            xml = XmlObject.Factory.parse(xmlFile);
        } catch (XmlException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return xml;
    }
}
