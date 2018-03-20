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
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Working with Fonts
 */
public class WorkingWithFonts {
    public static void main(String[] args) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {  //or new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Fonts");

            Font font0 = wb.createFont();
            font0.setColor(IndexedColors.BROWN.getIndex());
            CellStyle style0 = wb.createCellStyle();
            style0.setFont(font0);

            Font font1 = wb.createFont();
            font1.setFontHeightInPoints((short) 14);
            font1.setFontName("Courier New");
            font1.setColor(IndexedColors.RED.getIndex());
            CellStyle style1 = wb.createCellStyle();
            style1.setFont(font1);

            Font font2 = wb.createFont();
            font2.setFontHeightInPoints((short) 16);
            font2.setFontName("Arial");
            font2.setColor(IndexedColors.GREEN.getIndex());
            CellStyle style2 = wb.createCellStyle();
            style2.setFont(font2);

            Font font3 = wb.createFont();
            font3.setFontHeightInPoints((short) 18);
            font3.setFontName("Times New Roman");
            font3.setColor(IndexedColors.LAVENDER.getIndex());
            CellStyle style3 = wb.createCellStyle();
            style3.setFont(font3);

            Font font4 = wb.createFont();
            font4.setFontHeightInPoints((short) 18);
            font4.setFontName("Wingdings");
            font4.setColor(IndexedColors.GOLD.getIndex());
            CellStyle style4 = wb.createCellStyle();
            style4.setFont(font4);

            Font font5 = wb.createFont();
            font5.setFontName("Symbol");
            CellStyle style5 = wb.createCellStyle();
            style5.setFont(font5);

            Cell cell0 = sheet.createRow(0).createCell(1);
            cell0.setCellValue("Default");
            cell0.setCellStyle(style0);

            Cell cell1 = sheet.createRow(1).createCell(1);
            cell1.setCellValue("Courier");
            cell1.setCellStyle(style1);

            Cell cell2 = sheet.createRow(2).createCell(1);
            cell2.setCellValue("Arial");
            cell2.setCellStyle(style2);

            Cell cell3 = sheet.createRow(3).createCell(1);
            cell3.setCellValue("Times New Roman");
            cell3.setCellStyle(style3);

            Cell cell4 = sheet.createRow(4).createCell(1);
            cell4.setCellValue("Wingdings");
            cell4.setCellStyle(style4);

            Cell cell5 = sheet.createRow(5).createCell(1);
            cell5.setCellValue("Symbol");
            cell5.setCellStyle(style5);

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream("xssf-fonts.xlsx")) {
                wb.write(fileOut);
            }
        }
    }
}
