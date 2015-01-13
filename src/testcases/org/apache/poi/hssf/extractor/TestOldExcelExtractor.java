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

import java.io.File;
import java.io.InputStream;

import org.apache.poi.POITestCase;
import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Unit tests for the Excel 5/95 and Excel 4 (and older) text 
 *  extractor
 */
public final class TestOldExcelExtractor extends POITestCase {
    private static OldExcelExtractor createExtractor(String sampleFileName) {
        InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);

        try {
            return new OldExcelExtractor(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void testSimpleExcel3() {
        OldExcelExtractor extractor = createExtractor("testEXCEL_3.xls");

        // Check we can call getText without error
        String text = extractor.getText();

        // Check we find a few words we expect in there
        assertContains(text, "Season beginning August");
        assertContains(text, "USDA");
        
        // Check we find a few numbers we expect in there
        assertContains(text, "347");
        assertContains(text, "228");
        
        // Check we find a few string-literal dates in there
        assertContains(text, "1981/82");
        
        // Check the type
        assertEquals(3, extractor.getBiffVersion());
        assertEquals(0x10, extractor.getFileType());
    }
    public void testSimpleExcel4() {
        OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls");

        // Check we can call getText without error
        String text = extractor.getText();

        // Check we find a few words we expect in there
        assertContains(text, "Size");
        assertContains(text, "Returns");
        
        // Check we find a few numbers we expect in there
        assertContains(text, "11");
        assertContains(text, "784");
        
        // Check the type
        assertEquals(4, extractor.getBiffVersion());
        assertEquals(0x10, extractor.getFileType());
    }
    public void testSimpleExcel5() {
        for (String ver : new String[] {"5", "95"}) {
            OldExcelExtractor extractor = createExtractor("testEXCEL_"+ver+".xls");
    
            // Check we can call getText without error
            String text = extractor.getText();
    
            // Check we find a few words we expect in there
            assertContains(text, "Sample Excel");
            assertContains(text, "Written and saved");
            
            // Check we find a few numbers we expect in there
            assertContains(text, "15");
            assertContains(text, "169");
            
            // Check we got the sheet names (new formats only)
            assertContains(text, "Sheet: Feuil3");
            
            // Check the type
            assertEquals(5, extractor.getBiffVersion());
            assertEquals(0x05, extractor.getFileType());
        }
    }

    public void testStrings() {
        OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls");
        String text = extractor.getText();

        // Simple strings
        assertContains(text, "Table 10 -- Examination Coverage:");
        assertContains(text, "Recommended and Average Recommended Additional Tax After");
        assertContains(text, "Individual income tax returns, total");
        
        // More complicated strings
        assertContains(text, "$100,000 or more");
        assertContains(text, "S corporation returns, Form 1120S [10,15]");
        assertContains(text, "individual income tax return \u201Cshort forms.\u201D");
        
        // Formula based strings
        // TODO Find some then test
    }

    public void testFormattedNumbersExcel4() {
        OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls");
        String text = extractor.getText();

        // Simple numbers
        assertContains(text, "151");
        assertContains(text, "784");
        
        // Numbers which come from formulas
        assertContains(text, "0.398"); // TODO Rounding
        assertContains(text, "624");
        
        // Formatted numbers
        // TODO
//      assertContains(text, "55,624");
//      assertContains(text, "11,743,477");
    }
    public void testFormattedNumbersExcel5() {
        for (String ver : new String[] {"5", "95"}) {
            OldExcelExtractor extractor = createExtractor("testEXCEL_"+ver+".xls");
            String text = extractor.getText();
            
            // Simple numbers
            assertContains(text, "1");
            
            // Numbers which come from formulas
            assertContains(text, "13");
            assertContains(text, "169");
            
            // Formatted numbers
            // TODO
//          assertContains(text, "100.00%");
//          assertContains(text, "155.00%");
//          assertContains(text, "1,125");
//          assertContains(text, "189,945");
//          assertContains(text, "1,234,500");
//          assertContains(text, "$169.00");
//          assertContains(text, "$1,253.82");
        }
    }
    
    public void testFromFile() throws Exception {
        for (String ver : new String[] {"4", "5", "95"}) {
            String filename = "testEXCEL_"+ver+".xls";
            File f = HSSFTestDataSamples.getSampleFile(filename);
            
            OldExcelExtractor extractor = new OldExcelExtractor(f);
            String text = extractor.getText();
            assertNotNull(text);
            assertTrue(text.length() > 100);
        }
    }
}
