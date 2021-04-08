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

package org.apache.poi.examples.xssf.usermodel;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Demonstrates how to work with rich text
 */
public final class WorkingWithRichText {

    private WorkingWithRichText() {}

    public static void main(String[] args) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(2);

            XSSFCell cell = row.createCell(1);
            XSSFRichTextString rt = new XSSFRichTextString("The quick brown fox");

            XSSFFont font1 = wb.createFont();
            font1.setBold(true);
            font1.setColor(new XSSFColor(new java.awt.Color(255, 0, 0), wb.getStylesSource().getIndexedColors()));
            rt.applyFont(0, 10, font1);

            XSSFFont font2 = wb.createFont();
            font2.setItalic(true);
            font2.setUnderline(Font.U_DOUBLE);
            font2.setColor(new XSSFColor(new java.awt.Color(0, 255, 0), wb.getStylesSource().getIndexedColors()));
            rt.applyFont(10, 19, font2);

            XSSFFont font3 = wb.createFont();
            font3.setColor(new XSSFColor(new java.awt.Color(0, 0, 255), wb.getStylesSource().getIndexedColors()));
            rt.append(" Jumped over the lazy dog", font3);

            cell.setCellValue(rt);

            // Write the output to a file
            try (OutputStream fileOut = new FileOutputStream("xssf-richtext.xlsx")) {
                wb.write(fileOut);
            }
        }
    }
}
