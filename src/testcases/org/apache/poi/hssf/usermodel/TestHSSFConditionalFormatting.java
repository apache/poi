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


import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestConditionalFormatting;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author Dmitriy Kumshayev
 */
public final class TestHSSFConditionalFormatting extends BaseTestConditionalFormatting {
    public TestHSSFConditionalFormatting(){
        super(HSSFITestDataProvider.instance);
    }

    public void testRead(){
        testRead("WithConditionalFormatting.xls");
    }

    public void test53691() throws IOException {
        SheetConditionalFormatting cf;
        final Workbook wb;
        wb = HSSFITestDataProvider.instance.openSampleWorkbook("53691.xls");
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
    }
    
    private void removeConditionalFormatting(Sheet sheet) {
        SheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
        for (int j = 0; j < cf.getNumConditionalFormattings(); j++) {
            cf.removeConditionalFormatting(j);
        }
    }

    private void writeTemp53691(Workbook wb, String suffix) throws FileNotFoundException,
            IOException {
        // assert that we can write/read it in memory
        Workbook wbBack = HSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        
        /* Just necessary for local testing... */
        /*OutputStream out = new FileOutputStream("C:\\temp\\53691" + suffix + ".xls");
        try {
            wb.write(out);
        } finally {
            out.close();
        }*/
    }
}
