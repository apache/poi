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

package org.apache.xmlbeans.samples.substitutiongroup;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;

import org.apache.xmlbeans.samples.substitutiongroup.easypo.PurchaseOrderDocument;
import org.apache.xmlbeans.samples.substitutiongroup.easypo.InvoiceHeaderDocument;
import org.apache.xmlbeans.samples.substitutiongroup.easypo.NameAddress;
import org.apache.xmlbeans.samples.substitutiongroup.easypo.BookType;
import org.apache.xmlbeans.samples.substitutiongroup.easypo.ClothingType;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This sample illustrates how you can access substitution group element names and
 * values defined in the XML document. This sample also demonstrates how to write 
 * substitution group elements.
 *
 * The schema used by this sample are defined in EasyPo.xsd
 */
public class SubstitutionGroup
{
    /**
     * Receives an XML Instance and prints the substitution group element names and values,
     * Also creates a new XML Instance.
     *
     * @param args An array containing
     *             (a)Path to the XML Instance conforming to the XML schema in EasyPo.xsd.
     *             (b)Path for creating a new XML Instance.
     */
    public static void main(String args[])
    {
        // Create an instance of this class to work with.
        SubstitutionGroup subGrp = new SubstitutionGroup();

        // Create an instance of a type based on the received XML's schema
        PurchaseOrderDocument poDoc = subGrp.parseXml(args[0]);

        // Validate it
        validateXml(poDoc);

        // Prints the comments from the XML, also the name of the substitute tokens.
        subGrp.printComments(poDoc);

        // Creates a new XML and saves the file
        subGrp.createDocument(poDoc,args[1]);
    }

    public PurchaseOrderDocument parseXml(String file)
    {
        File xmlfile = new File(file);
        PurchaseOrderDocument poDoc = null;

        try 
        {
            poDoc = PurchaseOrderDocument.Factory.parse(xmlfile);
        }
        catch(XmlException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return poDoc;
    }

    /**
     * This method prints the substitution group element names(local part) and values for each Invoice-header
     * element in the XML Instance. (The rest of elements are ignored for the sake of simplicity)
     */
    public void printComments(PurchaseOrderDocument poDoc)
    {
        // Get object reference of root element.
        PurchaseOrderDocument.PurchaseOrder purchaseOrderElement = poDoc.getPurchaseOrder();

        // Get all the invoice-header elements for purchase-order.
        InvoiceHeaderDocument.InvoiceHeader[] invHeaders = purchaseOrderElement.getInvoiceHeaderArray();

        System.out.println("\n\n=========Contents==========\n");
        // Iterate through each invoice-header elements printing only the element name and value for substitution group
        // comment as defined in Easypo.xsd.
        for (int i=0;i<invHeaders.length;i++){
            System.out.println("\nInvoiceHeader["+i+"]");
            XmlCursor cursor = invHeaders[i].xgetComment().newCursor();
            System.out.println("Element Name (Local Part): " + cursor.getName().getLocalPart());
            System.out.println("Element Value: " + invHeaders[i].getComment().trim());
            cursor.dispose();
        }
    }

    /**
     * This method creates an new invoice-header element and attaches to the existing XML Instance, and saves the
     * new Instance to a file(args[1]).
     */
    public PurchaseOrderDocument createDocument(PurchaseOrderDocument poDoc, String file)
    {
        // Get object reference of root element.
        PurchaseOrderDocument.PurchaseOrder purchaseOrderElement = poDoc.getPurchaseOrder();

        InvoiceHeaderDocument.InvoiceHeader invHeaders = purchaseOrderElement.addNewInvoiceHeader();

        // Assign values to the newly created invoice-header element.
        NameAddress shipto = invHeaders.addNewShipTo();
        shipto.setName("New Company");
        shipto.setAddress("NewTown, NewCity");

        NameAddress billto = invHeaders.addNewBillTo();
        billto.setName("New Company");
        billto.setAddress("NewTown, NewCity");
        
        // Create a new Book and add it to the invoice.
        BookType book = BookType.Factory.newInstance();
        book.setId(1000);
        book.setTitle("Where the Red Fern Grows");
        invHeaders.setProduct(book);
        XmlCursor cursor = invHeaders.getProduct().newCursor();
        cursor.setName(new QName("http://xmlbeans.apache.org/samples/substitutiongroup/easypo", "book"));
        cursor.dispose();

        // Creating a new comment - with substitution group member bill-comment element.
        invHeaders.setComment("This is a new bill-comment");
        cursor = invHeaders.xgetComment().newCursor();
        cursor.setName(new QName("http://xmlbeans.apache.org/samples/substitutiongroup/easypo", "bill-comment"));
        cursor.dispose();

        // Add another invoice-header.
        invHeaders = purchaseOrderElement.addNewInvoiceHeader();

        // Assign values to the newly created invoice-header element.
        shipto = invHeaders.addNewShipTo();
        shipto.setName("Other Company");
        shipto.setAddress("OtherTown, OtherCity");

        billto = invHeaders.addNewBillTo();
        billto.setName("Other Company");
        billto.setAddress("OtherTown, OtherCity");

        // Create a new Clothing and add it to the invoice.
        ClothingType clothing = ClothingType.Factory.newInstance();
        clothing.setId(2000);
        clothing.setColor(ClothingType.Color.BLUE);
        invHeaders.setProduct(clothing);
        cursor = invHeaders.getProduct().newCursor();
        cursor.setName(new QName("http://xmlbeans.apache.org/samples/substitutiongroup/easypo", "clothing"));
        cursor.dispose();

        // Creating a new comment - with substitution group member bill-comment element.
        invHeaders.setComment("This is a new bill-comment");
        cursor = invHeaders.xgetComment().newCursor();
        cursor.setName(new QName("http://xmlbeans.apache.org/samples/substitutiongroup/easypo", "ship-comment"));
        cursor.dispose();

        // Validate it.
        validateXml(poDoc);

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSavePrettyPrint();

        File f = new File(file);

        try
        {
            //Writing the XML Instance to a file.
            poDoc.save(f,xmlOptions);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("\n\n\nXML Instance Document saved at : " + f.getPath());

        return poDoc;
    }


    /**
     * <p>Validates the XML, printing error messages when the XML is invalid. Note
     * that this method will properly validate any instance of a compiled schema
     * type because all of these types extend XmlObject.</p>
     *
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
