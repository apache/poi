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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Fills and Colors
 */
public class FillsAndColors {
    public static void main(String[] args) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) { //or new HSSFWorkbook();
            Sheet sheet = wb.createSheet("new sheet");

            // Create a row and put some cells in it. Rows are 0 based.
            Row row = sheet.createRow(1);

            // Aqua background
            CellStyle style = wb.createCellStyle();
            style.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
            style.setFillPattern(FillPatternType.BIG_SPOTS);
            Cell cell = row.createCell(1);
            cell.setCellValue(new XSSFRichTextString("X"));
            cell.setCellStyle(style);

            // Orange "foreground", foreground being the fill foreground not the font color.
            style = wb.createCellStyle();
            style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell = row.createCell(2);
            cell.setCellValue(new XSSFRichTextString("X"));
            cell.setCellStyle(style);

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream("fill_colors.xlsx")) {
                wb.write(fileOut);
            }
        }
    }
}
