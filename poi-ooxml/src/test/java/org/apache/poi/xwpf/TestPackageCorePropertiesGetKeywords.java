/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xwpf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

/**
 * Tests if the {@link CoreProperties#getKeywords()} method. This test has been
 * submitted because even though the
 * {@link PackageProperties#getKeywordsProperty()} had been present before, the
 * {@link CoreProperties#getKeywords()} had been missing.
 * <p>
 * The author of this has added {@link CoreProperties#getKeywords()} and
 * {@link CoreProperties#setKeywords(String)} and this test is supposed to test
 * them.
 */
public final class TestPackageCorePropertiesGetKeywords {
    @Test
    void testGetSetKeywords() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestPoiXMLDocumentCorePropertiesGetKeywords.docx")) {
            String keywords = doc.getProperties().getCoreProperties().getKeywords();
            assertEquals("extractor, test, rdf", keywords);

            doc.getProperties().getCoreProperties().setKeywords("test, keywords");
            XWPFDocument docBack = XWPFTestDataSamples.writeOutAndReadBack(doc);
            keywords = docBack.getProperties().getCoreProperties().getKeywords();
            assertEquals("test, keywords", keywords);
        }
    }
}
