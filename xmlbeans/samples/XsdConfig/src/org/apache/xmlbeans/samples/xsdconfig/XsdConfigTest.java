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

import com.catalog.XmlCatalogDocumentBean;

public class XsdConfigTest
{

    /**
     * Tests the CatalogXsdConfig.java class. This does consider XsdConfig file.
     */
    public static void main(String[] args)
    {
        // Create an instance of the sample to test.
        CatalogXsdConfig sample = new CatalogXsdConfig();

        XmlCatalogDocumentBean catdoc = sample.parseXml(args[0]);

        //Prints the element values from the XML.
        sample.printElements(catdoc);

        // Validate the XML.
        assert sample.validateXml(catdoc);


    }
}