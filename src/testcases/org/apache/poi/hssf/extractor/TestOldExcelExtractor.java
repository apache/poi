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

package org.apache.poi.hssf.extractor;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Unit tests for the Excel 4 (and older) text extractor
 */
public final class TestOldExcelExtractor extends TestCase {
    private static OldExcelExtractor createExtractor(String sampleFileName) {
        InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);

        try {
            return new OldExcelExtractor(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testSimple() {
        OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls");

        // Check we can call getText without error
        String text = extractor.getText();

        // Check we find a few words we expect in there
        assertTrue(text, text.contains("Size"));
        assertTrue(text, text.contains("Returns"));
        
        // Check we find a few numbers we expect in there
        assertTrue(text, text.contains("11"));
        assertTrue(text, text.contains("784"));
    }

    public void testStrings() {
        OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls");
        String text = extractor.getText();

        // Simple strings
        assertTrue(text, text.contains("Table 10 -- Examination Coverage:"));
        assertTrue(text, text.contains("Recommended and Average Recommended Additional Tax After"));
        assertTrue(text, text.contains("Individual income tax returns, total"));
        
        // More complicated strings
        assertTrue(text, text.contains("$100,000 or more"));
        assertTrue(text, text.contains("S corporation returns, Form 1120S [10,15]"));
        // TODO Get these quotes working correctly
//        assertTrue(text, text.contains("individual income tax return “short forms.”"));
        
        // Formula based strings
        // TODO Find some then test
    }

    public void testFormattedNumbers() {
        OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls");
        String text = extractor.getText();

        // Simple numbers
        assertTrue(text, text.contains("151"));
        assertTrue(text, text.contains("784"));
        
        // Numbers which come from formulas
        // TODO
//        assertTrue(text, text.contains("0.40"));
//        assertTrue(text, text.contains("624"));
        
        // Formatted numbers
        // TODO
    }
}
