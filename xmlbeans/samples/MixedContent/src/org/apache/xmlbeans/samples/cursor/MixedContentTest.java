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

package org.apache.xmlbeans.samples.cursor;

import org.apache.xmlbeans.samples.cursor.mixedcontent.InventoryDocument;

/**
 * A class with which to test the MixedContent sample.
 */
public class MixedContentTest
{
    /**
     * Tests the MixedContent sample.
     *
     * @param args An array in which the first item is a path to an XML file
     * based on the schema in inventory.xsd.
     */
    public static void main(String[] args)
    {
        // Create an instance of this sample to work with.
        MixedContent sample = new MixedContent();

        // Create an schema type instance from the XML indicated by the path.
        InventoryDocument inventoryDoc = sample.parseXml(args[0]);

        // Validate the XML.
        boolean exampleIsValid = sample.validateXml(inventoryDoc);
        assert exampleIsValid;

        // Edit the XML, adding <link> elements to associate related items.
        InventoryDocument linkedResultDoc = sample.linkItems(inventoryDoc);

        // Validate the XML.
        boolean newXmlIsValid = sample.validateXml(linkedResultDoc);
        assert newXmlIsValid;
    }
}
