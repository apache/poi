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
package org.apache.poi.xssf.usermodel.examples;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Demonstrates various settings avaiable in the Page Setup dialog
 */
public class WorkingWithPageSetup {

    public static void main(String[]args) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {  //or new HSSFWorkbook();

        /*
         * It's possible to set up repeating rows and columns in your printouts by using the setRepeatingRowsAndColumns() function in the Workbook object.
         *
         * This function Contains 5 parameters:
         * The first parameter is the index to the sheet (0 = first sheet).
         * The second and third parameters specify the range for the columns to repreat.
         * To stop the columns from repeating pass in -1 as the start and end column.
         * The fourth and fifth parameters specify the range for the rows to repeat.
         * To stop the columns from repeating pass in -1 as the start and end rows.
         */
            Sheet sheet1 = wb.createSheet("new sheet");
            Sheet sheet2 = wb.createSheet("second sheet");

            // Set the columns to repeat from column 0 to 2 on the first sheet
            Row row1 = sheet1.createRow(0);
            row1.createCell(0).setCellValue(1);
            row1.createCell(1).setCellValue(2);
            row1.createCell(2).setCellValue(3);
            Row row2 = sheet1.createRow(1);
            row2.createCell(1).setCellValue(4);
            row2.createCell(2).setCellValue(5);


            Row row3 = sheet2.createRow(1);
            row3.createCell(0).setCellValue(2.1);
            row3.createCell(4).setCellValue(2.2);
            row3.createCell(5).setCellValue(2.3);
            Row row4 = sheet2.createRow(2);
            row4.createCell(4).setCellValue(2.4);
            row4.createCell(5).setCellValue(2.5);

            // Set the columns to repeat from column 0 to 2 on the first sheet
            sheet1.setRepeatingColumns(CellRangeAddress.valueOf("A:C"));
            // Set the the repeating rows and columns on the second sheet.
            CellRangeAddress cra = CellRangeAddress.valueOf("E2:F3");
            sheet2.setRepeatingColumns(cra);
            sheet2.setRepeatingRows(cra);

            //set the print area for the first sheet
            wb.setPrintArea(0, 1, 2, 0, 3);


            try (FileOutputStream fileOut = new FileOutputStream("xssf-printsetup.xlsx")) {
                wb.write(fileOut);
            }
        }
    }
}
