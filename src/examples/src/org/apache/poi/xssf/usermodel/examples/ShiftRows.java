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
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * How to shift rows up or down 
 */
public class ShiftRows {

    public static void main(String[]args) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {   //or new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Sheet1");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue(1);

            Row row2 = sheet.createRow(4);
            row2.createCell(1).setCellValue(2);

            Row row3 = sheet.createRow(5);
            row3.createCell(2).setCellValue(3);

            Row row4 = sheet.createRow(6);
            row4.createCell(3).setCellValue(4);

            Row row5 = sheet.createRow(9);
            row5.createCell(4).setCellValue(5);

            // Shift rows 6 - 11 on the spreadsheet to the top (rows 0 - 5)
            sheet.shiftRows(5, 10, -4);

            try (FileOutputStream fileOut = new FileOutputStream("shiftRows.xlsx")) {
                wb.write(fileOut);
            }
        }
    }
}
