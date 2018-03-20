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

package org.apache.xmlbeans.samples.enumeration;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.samples.enumeration.schemaenum.easypo.LineItem;
import org.apache.xmlbeans.samples.enumeration.schemaenum.easypo.PurchaseOrderDocument;
import org.apache.xmlbeans.samples.enumeration.schemaenum.pricesummary.ItemType;
import org.apache.xmlbeans.samples.enumeration.schemaenum.pricesummary.PriceSummaryDocument;
import org.apache.xmlbeans.samples.enumeration.schemaenum.pricesummary.PriceType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This sample illustrates how you can access XML values that are
 * defined in schema as enumerations. When a schema containing
 * enumerations is compiled, the generated Java types represent the
 * schema enumerations with Java enumerations. You can access these through
 * their constants and corresponding int values.
 * <p/>
 * The schemas used by this sample are defined in PriceSummary.xsd and
 * EasyPO.xsd.
 */
public class SchemaEnum
{
    /**
     * Receives an PO XML instance and uses its data to create an XML
     * document based another schema, and which summarizes the items
     * in the PO by price.
     *
     * @param args An array containing one argument: the path to an XML instance
     *             conforming to the schema in EasyPO.xsd.
     */
    public static void main(String[] args)
    {
        // Create an instance of this class to work with.
        SchemaEnum thisSample = new SchemaEnum();

        // Create an instance of a type based on the received XML's schema
        // and use it to print what the sample received.
        PurchaseOrderDocument poDoc = thisSample.parseXml(args[0]);
        System.out.println("Received XML: \n\n" + poDoc.toString());

        // Print the summarized items in XML based on a different schema.
        PriceSummaryDocument summaryDoc = thisSample.summarizeItems(poDoc);
        System.out.println("Summarized items: \n\n" + summaryDoc.toString());

        // Print a simple non-XML list of items by threshold.
        String sortedItems = thisSample.sortByThreshold(summaryDoc);
        System.out.println("Sorted items: \n" + sortedItems);

        // Validate the result.
        System.out.println("New XML is valid: " +
                thisSample.validateXml(summaryDoc));
    }

    /**
     * <p>This method uses values in the incoming XML to construct
     * a new XML document of a different schema. PriceSummary.xsd, the schema
     * for the new document, defines XML enumerations for a price
     * threshold attribute. Items whose price is between $10 and $20 receive
     * a threshold value of "Between10And20Dollars"; items above 20 get a threshold
     * value of "Above20Dollars".</p>
     * <p/>
     * <p>This method loops through the purchase order items, creating a summary
     * document that specifies their threshold value.</p>
     * <p/>
     * <p>You can verify this method's work by comparing the resulting XML with
     * the XML in PriceSummary.xml. You can also use this method's return value
     * to test the sortByThreshold method.</p>
     */
    public PriceSummaryDocument summarizeItems(PurchaseOrderDocument poDoc)
    {
        PurchaseOrderDocument.PurchaseOrder po = poDoc.getPurchaseOrder();

        // Create a new instance of the PriceSummary schema. This is the document
        // the code creates, extracting values from the purchase order.
        PriceSummaryDocument summaryDoc = PriceSummaryDocument.Factory.newInstance();
        PriceSummaryDocument.PriceSummary summary = summaryDoc.addNewPriceSummary();

        // Create <price> elements to hold <item> elements according to their
        // price threshold.
        PriceType priceZero = summary.addNewPrice();
        PriceType priceTen = summary.addNewPrice();
        PriceType priceTwenty = summary.addNewPrice();

        // Set the threshold attribute value for the two new elements.
        priceZero.setThreshold(PriceType.Threshold.BELOW_10_DOLLARS);
        priceTen.setThreshold(PriceType.Threshold.BETWEEN_10_AND_20_DOLLARS);
        priceTwenty.setThreshold(PriceType.Threshold.ABOVE_20_DOLLARS);

        // Loop through the purchase order <line-item> elements. If their
        // <price> child element is between 10.00 and 20.00, add the <line-item>
        // to the <price> element whose threshold is 10.00. For those over 20.00,
        // add them to the <price> element whose threshold is 20.00.

        // There don't happen to be any under 10.00, but handle this case anyway.
        LineItem[] items = po.getLineItemArray();
        for (int i = 0; i < items.length; i++)
        {
            LineItem item = items[i];

            if (item.getPrice() < 10.00)
            {

                ItemType newItem = priceZero.addNewItem();
                newItem.setTitle(item.getDescription());
                newItem.xsetQuantity(item.xgetQuantity());
                newItem.setAmount(item.getPrice());

            } else if (item.getPrice() >= 10.00 && item.getPrice() < 20.00)
            {

                ItemType newItem = priceTen.addNewItem();
                newItem.setTitle(item.getDescription());
                newItem.xsetQuantity(item.xgetQuantity());
                newItem.setAmount(item.getPrice());

            } else if (item.getPrice() >= 20.00)
            {

                ItemType newItem = priceTwenty.addNewItem();
                newItem.setTitle(item.getDescription());
                newItem.xsetQuantity(item.xgetQuantity());
                newItem.setAmount(item.getPrice());
            }
        }
        return summaryDoc;
    }

    /**
     * <p>This method loops through a price summary XML document to
     * create a string that lists the items grouped by threshold.
     * Unlike the summarizeItems method, which creates a new XML
     * document that contains an attribute whose value is enumerated,
     * this method retrieves values from an enumeration.</p>
     * <p/>
     * <p>This method illustrates how you can use the int value corresponding
     * to enumerations to specify them in Java switch statements.</p>
     */
    public String sortByThreshold(PriceSummaryDocument summaryDoc)
    {
        // Extract the summary element from the incoming XML, then use it
        // to extract an array of the price elements.
        PriceSummaryDocument.PriceSummary summary = summaryDoc.getPriceSummary();
        PriceType[] priceElements = summary.getPriceArray();

        StringBuffer responseBuffer = new StringBuffer();

        // Create string buffers to hold the sorted results of the values
        // retrieved.
        StringBuffer zeroBuffer = new StringBuffer("\nItems under 10 dollars: \n");
        StringBuffer tenBuffer = new StringBuffer("\nItems between 10 and 20 dollars: \n");
        StringBuffer twentyBuffer = new StringBuffer("\nItems more than 20 dollars: \n");

        // Loop through the price elements, extracting the array of <item> child
        // elements in each.
        for (int i = 0; i < priceElements.length; i++)
        {
            ItemType[] itemElements = priceElements[i].getItemArray();

            // Loop through the <item> elements, discovering which threshold
            // the item belongs to, then using the element's <title> value
            // in in a sorted list.
            for (int j = 0; j < itemElements.length; j++)
            {
                ItemType item = itemElements[j];

                // For each <item> element, find out the int value of its <price>
                // parent element's threshold attribute value. Append the item's
                // title to the appropriate string buffer.
                switch (priceElements[i].getThreshold().intValue())
                {

                    case PriceType.Threshold.INT_BELOW_10_DOLLARS:
                        zeroBuffer.append(" " + item.getTitle() + "\n");
                        break;

                    case PriceType.Threshold.INT_BETWEEN_10_AND_20_DOLLARS:
                        tenBuffer.append(" " + item.getTitle() + "\n");
                        break;

                    case PriceType.Threshold.INT_ABOVE_20_DOLLARS:
                        twentyBuffer.append(" " + item.getTitle() + "\n");
                        break;

                    default:
                        System.out.println("Yo! Something unexpected happened!");
                        break;
                }
            }
        }
        responseBuffer.append(tenBuffer);
        responseBuffer.append(twentyBuffer);
        return responseBuffer.toString();
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
    public boolean validateXml(XmlObject xml)
    {
        boolean isXmlValid = false;

        // A collection instance to hold validation error messages.
        ArrayList validationMessages = new ArrayList();

        // Validate the XML, collecting messages.
        isXmlValid = xml.validate(new XmlOptions().setErrorListener(validationMessages));

        if (!isXmlValid)
        {
            System.out.println("Invalid XML: ");
            for (int i = 0; i < validationMessages.size(); i++)
            {
                XmlError error = (XmlError) validationMessages.get(i);
                System.out.println(error.getMessage());
                System.out.println(error.getObjectLocation());
            }
        }
        return isXmlValid;
    }

    /**
     * <p>Creates a File from the XML path provided in main arguments, then
     * parses the file's contents into a type generated from schema.</p>
     * <p/>
     * <p>Note that this work might have been done in main. Isolating it here
     * makes the code separately available from outside this class.</p>
     *
     * @param xmlFilePath A path to XML based on the schema in inventory.xsd.
     * @return An instance of a generated schema type that contains the parsed
     *         XML.
     */
    public PurchaseOrderDocument parseXml(String xmlFilePath)
    {
        File poFile = new File(xmlFilePath);
        PurchaseOrderDocument poDoc = null;
        try
        {
            poDoc = PurchaseOrderDocument.Factory.parse(poFile);
        } catch (XmlException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return poDoc;
    }
}
