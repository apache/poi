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
package org.apache.poi.hwpf.converter;

import static org.apache.poi.hwpf.HWPFTestDataSamples.openSampleFile;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hwpf.HWPFDocument;
import org.junit.jupiter.api.Test;

public class TestWordToTextConverter {

    /**
     * [FAILING] Bug 47731 - Word Extractor considers text copied from some
     * website as an embedded object
     */
    @Test
    void testBug47731() throws Exception {
        try (HWPFDocument doc = openSampleFile( "Bug47731.doc" )) {
            String foundText = WordToTextConverter.getText(doc);

            assertTrue(foundText.contains("Soak the rice in water for three to four hours"));
        }
    }

    @Test
    void testBug52311() throws Exception {
        try (HWPFDocument doc = openSampleFile( "Bug52311.doc" )) {
            String result = WordToTextConverter.getText(doc);

            assertTrue(result.contains("2.1\tHeader 2.1"));
            assertTrue(result.contains("2.2\tHeader 2.2"));
            assertTrue(result.contains("2.3\tHeader 2.3"));
            assertTrue(result.contains("2.3.1\tHeader 2.3.1"));
            assertTrue(result.contains("2.99\tHeader 2.99"));
            assertTrue(result.contains("2.99.1\tHeader 2.99.1"));
            assertTrue(result.contains("2.100\tHeader 2.100"));
            assertTrue(result.contains("2.101\tHeader 2.101"));
        }
    }

    @Test
    void testBug53380_3() throws Exception {
        try (HWPFDocument doc = openSampleFile( "Bug53380_3.doc" )) {
            WordToTextConverter.getText(doc);
        }
    }
}
