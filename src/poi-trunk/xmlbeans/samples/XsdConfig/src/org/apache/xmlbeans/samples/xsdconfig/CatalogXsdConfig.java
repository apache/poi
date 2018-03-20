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

package org.apache.xmlbeans.samples.xsdconfig;

/**
 *This class uses the package names and class names mentioned in XsdConfig. 
 *Note the difference between the imports in two files (CatalogXsdConfig.java and CatalogXsd.java)
 */

import com.catalog.XmlCatalogDocumentBean;
import com.catalog.XmlJournalDocumentBean;
import com.catalog.XmlArticleDocumentBean;
import com.catalog.XmlShortItemBean;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

public class CatalogXsdConfig
{

    public static void main(String[] args)
    {
        // Create an instance of this class to work with.
        CatalogXsdConfig catxsdconfig = new CatalogXsdConfig();

        // Create an instance of a type based on the received XML's schema
        XmlCatalogDocumentBean catdoc = catxsdconfig.parseXml(args[0]);

        //Prints the element values from the XML.
        catxsdconfig.printElements(catdoc);
    }

    /**
     * Creates a File from the XML path provided in main arguments, then
     * parses the file's contents into a type (CatalogDocument) generated from schema.
     *
     * @param xmlFilePath A path to XML based on the schema in EasyPo.xsd
     * @return An instance of a generated schema type (CatalogDocument) that contains the
     *         parsed XML.
     */
    public XmlCatalogDocumentBean parseXml(String xmlFilePath)
    {
        File xmlfile = new File(xmlFilePath);
        XmlCatalogDocumentBean catdoc = null;

        try
        {
            catdoc = XmlCatalogDocumentBean.Factory.parse(xmlfile);
        }
        catch (XmlException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return catdoc;
    }

    /*
     * This method prints all the element values in the given XML document based on Catalog.xsd
     */
    public void printElements(XmlCatalogDocumentBean catdoc)
    {
        // Get object reference of root element.
        XmlCatalogDocumentBean.Catalog catalogelement = catdoc.getCatalog();

        //Get all <journal> element from the root element.
        XmlJournalDocumentBean.Journal[] journalarray = catalogelement.getJournalArray();

        //Loop through <journal> element array.
        for (int i = 0; i < journalarray.length; i++)
        {

            //Retrieve all <article> elements within each <journal> element
            XmlArticleDocumentBean.Article[] articlearray = journalarray[i].getArticleArray();

            //Loop through <article> array retrieved above
            for (int j = 0; j < articlearray.length; j++)
            {
                System.out.println(articlearray[j].getTitle());

                String[] str = articlearray[j].getAuthorArray();

                for (int k = 0; k < str.length; k++)
                    System.out.println(str[k]);

                //Note the method for retrieving <forsample> element.
                System.out.println(
                    articlearray[j].getXmlShortItemBean().getGoodName());

            }
        }
        System.out.println("\n\n\n");
    }

    /**
     * <p>Validates the XML, printing error messages when the XML is invalid. Note
     * that this method will properly validate any instance of a compiled schema
     * type because all of these types extend XmlObject.</p>
     * <p/>
     * <p>Note that in actual practice, you'll probably want to use an assertion
     * when validating if you want to ensure that your code doesn't pass along
     * invalid XML. This sample prints the generated XML whether or not it's
     * valid so that you can see the result in both cases.</p>
     *
     * @param xml The XML to validate.
     * @return <code>true</code> if the XML is valid; otherwise, <code>false</code>
     */
    public static boolean validateXml(XmlObject xml)
    {
        boolean isXmlValid = false;

        // A collection instance to hold validation error messages.
        ArrayList validationMessages = new ArrayList();

        // Validate the XML, collecting messages.
        isXmlValid = xml.validate(
            new XmlOptions().setErrorListener(validationMessages));

        // If the XML isn't valid, print the messages.
        if (!isXmlValid)
        {
            System.out.println("\nInvalid XML: ");
            for (int i = 0; i < validationMessages.size(); i++)
            {
                XmlError error = (XmlError) validationMessages.get(i);
                System.out.println(error.getMessage());
                System.out.println(error.getObjectLocation());
            }
        }
        return isXmlValid;
    }

}
