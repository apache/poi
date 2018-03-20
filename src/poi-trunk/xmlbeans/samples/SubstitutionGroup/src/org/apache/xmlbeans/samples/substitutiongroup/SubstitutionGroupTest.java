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

import org.apache.xmlbeans.samples.substitutiongroup.easypo.PurchaseOrderDocument;

/**
 * A class to test the SubstitutionGroup sample.
 */
public class SubstitutionGroupTest
{
    /**
     * Tests the SubstitutionGroup sample.
     *
     * @param args An array in which the first item is a path to an XML file
     * based on the schema in EasyPo.xsd. The second item is a path to an XML
     * file that should be created by the sample.
     */
    public static void main(String[] args)
    {
        // Create an instance of the sample to test.
        SubstitutionGroup sample = new SubstitutionGroup();

        // Create a schema type instance from the XML indicated by the path.
        SubstitutionGroup subGrp = new SubstitutionGroup();
        PurchaseOrderDocument poDoc = subGrp.parseXml(args[0]);

        // Validate the XML.
        assert sample.validateXml(poDoc);

        // Create a new document that adds two elements.
        PurchaseOrderDocument newDoc = sample.createDocument(poDoc, args[1]);

        // Validate the XML.
        assert sample.validateXml(newDoc);
    }
}
