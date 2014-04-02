
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
        

package org.apache.poi.hssf.usermodel.examples;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import java.io.IOException;
import java.io.FileOutputStream;

/**
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class SplitAndFreezePanes
{
    public static void main(String[] args)
        throws IOException
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");
        HSSFSheet sheet2 = wb.createSheet("second sheet");
        HSSFSheet sheet3 = wb.createSheet("third sheet");
        HSSFSheet sheet4 = wb.createSheet("fourth sheet");

        // Freeze just one row
        sheet1.createFreezePane( 0, 1, 0, 1 );
        // Freeze just one column
        sheet2.createFreezePane( 1, 0, 1, 0 );
        // Freeze the columns and rows (forget about scrolling position of the lower right quadrant).
        sheet3.createFreezePane( 2, 2 );
        // Create a split with the lower left side being the active quadrant
        sheet4.createSplitPane( 2000, 2000, 0, 0, HSSFSheet.PANE_LOWER_LEFT );

        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }
}
