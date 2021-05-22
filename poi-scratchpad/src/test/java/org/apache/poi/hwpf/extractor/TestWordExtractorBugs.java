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

package org.apache.poi.hwpf.extractor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for bugs with the WordExtractor
 */
public final class TestWordExtractorBugs {
    private static final POIDataSamples SAMPLES = POIDataSamples.getDocumentInstance();

    @ParameterizedTest
    @ValueSource(strings = {
        "ProblemExtracting.doc",
        // bug 50688
        "parentinvguid.doc",
        // Bug60374
        "cn.orthodox.www_divenbog_APRIL_30-APRIL.DOC"
    })
    void testFile(String file) throws IOException {
        try (InputStream is = SAMPLES.openResourceAsStream(file);
             POITextExtractor ex = ExtractorFactory.createExtractor(is)) {
            // Check it gives text without error
            assertNotNull(ex.getText());
            if (ex instanceof WordExtractor) {
                WordExtractor extractor = (WordExtractor)ex;
                assertNotNull(extractor.getParagraphText());
                assertNotNull(extractor.getTextFromPieces());
            } else {
                Word6Extractor extractor = (Word6Extractor)ex;
                assertNotNull(extractor.getParagraphText());
            }
        }
    }
}
