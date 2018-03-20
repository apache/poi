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

import org.apache.xmlbeans.samples.enumeration.schemaenum.easypo.PurchaseOrderDocument;
import org.apache.xmlbeans.samples.enumeration.schemaenum.pricesummary.PriceSummaryDocument;

/**
 * A class to test the SchemaEnum sample.
 */
public class SchemaEnumTest
{
    /**
     * Tests the SchemaEnum sample.
     *
     * @param args An array in which the first item is a path to an XML file
     * based on the schema in inventory.xsd.
     */
    public static void main(String[] args)
    {
        SchemaEnum sample = new SchemaEnum();
        PurchaseOrderDocument poDoc = sample.parseXml(args[0]);

        boolean exampleIsValid = sample.validateXml(poDoc);
        assert exampleIsValid;

        // Create a new document that summarizes the PO doc.
        PriceSummaryDocument summaryDoc = sample.summarizeItems(poDoc);

        boolean summaryIsValid = sample.validateXml(summaryDoc);
        assert summaryIsValid;

        // Create a summary of the items based on price.
        String sortedItems = sample.sortByThreshold(summaryDoc);

        boolean stringExists = (sortedItems != null);
        assert stringExists;
    }
}
