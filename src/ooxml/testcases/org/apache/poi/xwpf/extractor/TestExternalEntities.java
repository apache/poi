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

package org.apache.poi.xwpf.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

class TestExternalEntities {

    /**
     * Get text out of the simple file
     */
    @Test
    void testFile() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("ExternalEntityInText.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            String text = extractor.getText();

            assertTrue(text.length() > 0);

            // Check contents, they should not contain the text from POI web site after colon!
            assertEquals("Here should not be the POI web site: \"\"", text.trim());
        }
    }

}
