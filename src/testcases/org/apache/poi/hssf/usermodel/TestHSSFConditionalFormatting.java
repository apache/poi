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

package org.apache.poi.hssf.usermodel;


import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.junit.Test;

/**
 * HSSF-specific Conditional Formatting tests
 */
public final class TestHSSFConditionalFormatting extends BaseTestConditionalFormatting {
    public TestHSSFConditionalFormatting(){
        super(HSSFITestDataProvider.instance);
    }
    @Override
    protected void assertColour(String hexExpected, Color actual) {
        assertNotNull("Colour must be given", actual);
        if (actual instanceof HSSFColor) {
            HSSFColor colour = (HSSFColor)actual;
            assertEquals(hexExpected, colour.getHexString());
        } else {
            HSSFExtendedColor colour = (HSSFExtendedColor)actual;
            if (hexExpected.length() == 8) {
                assertEquals(hexExpected, colour.getARGBHex());
            } else {
                assertEquals(hexExpected, colour.getARGBHex().substring(2));
            }
        }
    }

    @Test
    public void testRead() throws IOException {
        testRead("WithConditionalFormatting.xls");
    }
    
    @Test
    public void testReadOffice2007() throws IOException {
        testReadOffice2007("NewStyleConditionalFormattings.xls");
    }

    @Test
    public void test53691() throws IOException {
        SheetConditionalFormatting cf;
        final Workbook wb = HSSFITestDataProvider.instance.openSampleWorkbook("53691.xls");
        /*
        FileInputStream s = new FileInputStream("C:\\temp\\53691bbadfixed.xls");
        try {
            wb = new HSSFWorkbook(s);
        } finally {
            s.close();
        }

        wb.removeSheetAt(1);*/
        
        // initially it is good
        writeTemp53691(wb, "agood");
        
        // clone sheet corrupts it
        Sheet sheet = wb.cloneSheet(0);
        writeTemp53691(wb, "bbad");

        // removing the sheet makes it good again
        wb.removeSheetAt(wb.getSheetIndex(sheet));
        writeTemp53691(wb, "cgood");
        
        // cloning again and removing the conditional formatting makes it good again
        sheet = wb.cloneSheet(0);
        removeConditionalFormatting(sheet);        
        writeTemp53691(wb, "dgood");
        
        // cloning the conditional formatting manually makes it bad again
        cf = sheet.getSheetConditionalFormatting();
        SheetConditionalFormatting scf = wb.getSheetAt(0).getSheetConditionalFormatting();
        for (int j = 0; j < scf.getNumConditionalFormattings(); j++) {
            cf.addConditionalFormatting(scf.getConditionalFormattingAt(j));
        }        
        writeTemp53691(wb, "ebad");

        // remove all conditional formatting for comparing BIFF output
        removeConditionalFormatting(sheet);        
        removeConditionalFormatting(wb.getSheetAt(0));        
        writeTemp53691(wb, "fgood");
        
        wb.close();
    }
    
    private void removeConditionalFormatting(Sheet sheet) {
        SheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
        for (int j = 0; j < cf.getNumConditionalFormattings(); j++) {
            cf.removeConditionalFormatting(j);
        }
    }

    private void writeTemp53691(Workbook wb, @SuppressWarnings("UnusedParameters") String suffix) throws IOException {
        // assert that we can write/read it in memory
        Workbook wbBack = HSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        wbBack.close();
    }
}
