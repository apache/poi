/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   Unless required by applicable law or agreed to in writing, software
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.samples.cursor;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.samples.cursor.mixedcontent.DescriptionType;
import org.apache.xmlbeans.samples.cursor.mixedcontent.InventoryDocument;
import org.apache.xmlbeans.samples.cursor.mixedcontent.ItemType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>This sample illustrates how you can use an XML cursor
 * to manipulate the content of an element. Even though
 * working with strongly-typed XML (in which you are accessing
 * the XML through an API generated from schema) provides easy
 * access for getting and setting the entire value of an
 * element or attribute, it does not easily provide finer
 * grained access to an element's content. This sample
 * shows how you can use an XML cursor to "dive into" an
 * element's content, manipulating it on a character-by-
 * character level.</p>
 * <p/>
 * <p>The code in this sample is designed to look at the
 * description of each item in an inventory list, creating
 * a link wherever the description contains a reference
 * to another item in the inventory list. This alters the
 * &lt;description&gt; element so that it contains a mix of text and
 * link elements. Such an element is said to have "mixed
 * content."</p>
 * <p/>
 * This sample uses the schema defined in inventory.xsd.
 */
public class MixedContent
{
    /**
     * Receives an inventory XML instance and rewrites it so that items listed
     * in the inventory point to one another via &lt;link&gt; elements.
     *
     * @param args An array containing one argument: the path to an XML instance
     *             conforming to the schema in inventory.xsd.
     */
    public static void main(String[] args)
    {
        // Create an instance of this sample to work with.
        MixedContent thisSample = new MixedContent();

        // Create an schema type instance from the XML indicated by the path.
        InventoryDocument inventoryDoc = thisSample.parseXml(args[0]);

        // Print what was received.
        System.out.println("Received XML: \n\n" + inventoryDoc.toString());

        // Edit the XML, adding <link> elements to associate related items.
        InventoryDocument linkedResultDoc = thisSample.linkItems(inventoryDoc);

        // Print the updated XML.
        System.out.println("XML with linked items: \n\n" + linkedResultDoc.toString());

        // Validate the result.
        System.out.println("New XML is valid: " +
                thisSample.validateXml(linkedResultDoc));
    }

    /**
     * <p>Creates "links" between items in an inventory list by inserting
     * a &lt;link&gt; element for each linked item. An XmlCursor
     * instance passes through each &lt;description&gt; element, looking
     * for text matching the name of an item.</p>
     *
     * @param inventoryDoc An XML document conforming to the schema in
     *                     inventory.xsd.
     */
    public InventoryDocument linkItems(InventoryDocument inventoryDoc)
    {
        // Retrieve the &lt;inventory&gt; element and get an array of
        // the &lt;item&gt; elements it contains.
        InventoryDocument.Inventory inventory = inventoryDoc.getInventory();
        ItemType[] items = inventory.getItemArray();

        // Loop through the &lt;item&gt; elements, examining the
        // description for each to see if another inventory item
        // is mentioned.
        for (int i = 0; i < items.length; i++)
        {
            // Get details about the current item, including
            // its length. This will be used to measure text
            // while exploring the description.
            String itemName = items[i].getName();
            String itemId = new Integer(items[i].getId()).toString();
            int itemCharCount = itemName.length();

            // Loop through the item descriptions, looking at each
            // for the name of the current item.
            for (int j = 0; j < items.length; j++)
            {
                DescriptionType description = items[j].getDescription();

                // Insert an XmlCursor instance and set it at
                // the beginning of the <&lt;description&gt; element's text,
                // just after the start tag.
                XmlCursor cursor = description.newCursor();
                cursor.toLastAttribute();
                cursor.toNextToken();

                // Get a String containing the characters to the
                // immediate right of the cursor, up to the next
                // token (in this case, the next element after
                // the description element). Get the number of
                // characters to the right of the cursor; this will
                // be used to mark the distance the cursor should move
                // before trying another item's description. Also,
                // create a charCount variable to mark the cursor's
                // current position.
                String cursorChars = cursor.getChars();
                int descCharCount = cursorChars.length();
                int charCount = 0;

                // As long at the cursor hasn't reached the end of the
                // description text, check to see if the text to the
                // cursor's immediate right matches the item name sought.
                // If it does match, remove the text and create a link
                // element to replace it.
                while (charCount < descCharCount)
                {

                    // A char array to hold the characters currently being
                    // checked.
                    char[] chars = new char[itemCharCount];

                    // Pass the char array with the getChars method. This
                    // method will find the chars from the cursor's
                    // immediate right to the char at itemCharCount (the
                    // length of the item name currently sought). The
                    // method's second argument indicates where in the char
                    // array the found text should begin -- in this case, at the
                    // beginning.
                    int charsReturned = cursor.getChars(chars, 0, itemCharCount);

                    // If the characters in chars match the item name, then
                    // make a link from the text.
                    if (new String(chars).equals(itemName))
                    {
                        // Remove the found item name.
                        cursor.removeChars(itemCharCount);

                        // Begin a new link element whose namespace is the
                        // same as the rest of the inventory document. The
                        // beginElement method creates a new element with the
                        // name specified around the current cursor.
                        cursor.beginElement("link",
                                "http://xmlbeans.apache.org/samples/cursor/mixedcontent");

                        // Insert an id attribute and make its value the id of
                        // the item sought.
                        cursor.insertAttributeWithValue("id", itemId);

                        // Insert the item name as the element's value.
                        cursor.insertChars(itemName);
                    }

                    // Move on to the next character in the description.
                    cursor.toNextChar(1);

                    // Increment the counter tracking the cursor's position.
                    charCount++;
                }

                // Be sure to dispose of a cursor that's no longer needed.
                // This allows it to be garbage collected.
                cursor.dispose();
            }
        }

        // Return the edited document.
        return inventoryDoc;
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
    public boolean validateXml(XmlObject xml)
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
     *
     * <p>Note that this work might have been done in main. Isolating it here
     * makes the code separately available from outside this class.</p>
     *
     * @param xmlFilePath A path to XML based on the schema in inventory.xsd.
     * @return An instance of a generated schema type that contains the parsed
     * XML.
     */
    public InventoryDocument parseXml(String xmlFilePath)
    {
        // Get the XML instance into a file using the path provided.
        File inventoryFile = new File(xmlFilePath);

        // Create an instance of a type generated from schema to hold the XML.
        InventoryDocument inventoryDoc = null;
        try
        {
            // Parse the instance into the type generated from the schema.
            inventoryDoc = InventoryDocument.Factory.parse(inventoryFile);
        } catch (XmlException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return inventoryDoc;
    }
}
