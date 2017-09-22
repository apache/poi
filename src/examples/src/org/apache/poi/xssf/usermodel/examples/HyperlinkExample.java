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

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Demonstrates how to create hyperlinks.
 */
public class HyperlinkExample {

    public static void main(String[]args) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) { //or new HSSFWorkbook();
            CreationHelper createHelper = wb.getCreationHelper();

            //cell style for hyperlinks
            //by default hyperlinks are blue and underlined
            CellStyle hlink_style = wb.createCellStyle();
            Font hlink_font = wb.createFont();
            hlink_font.setUnderline(Font.U_SINGLE);
            hlink_font.setColor(IndexedColors.BLUE.getIndex());
            hlink_style.setFont(hlink_font);

            Cell cell;
            Sheet sheet = wb.createSheet("Hyperlinks");
            //URL
            cell = sheet.createRow(0).createCell(0);
            cell.setCellValue("URL Link");

            Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress("http://poi.apache.org/");
            cell.setHyperlink(link);
            cell.setCellStyle(hlink_style);

            //link to a file in the current directory
            cell = sheet.createRow(1).createCell(0);
            cell.setCellValue("File Link");
            link = createHelper.createHyperlink(HyperlinkType.FILE);
            link.setAddress("link1.xls");
            cell.setHyperlink(link);
            cell.setCellStyle(hlink_style);

            //e-mail link
            cell = sheet.createRow(2).createCell(0);
            cell.setCellValue("Email Link");
            link = createHelper.createHyperlink(HyperlinkType.EMAIL);
            //note, if subject contains white spaces, make sure they are url-encoded
            link.setAddress("mailto:poi@apache.org?subject=Hyperlinks");
            cell.setHyperlink(link);
            cell.setCellStyle(hlink_style);

            //link to a place in this workbook

            //create a target sheet and cell
            Sheet sheet2 = wb.createSheet("Target Sheet");
            sheet2.createRow(0).createCell(0).setCellValue("Target Cell");

            cell = sheet.createRow(3).createCell(0);
            cell.setCellValue("Worksheet Link");
            Hyperlink link2 = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            link2.setAddress("'Target Sheet'!A1");
            cell.setHyperlink(link2);
            cell.setCellStyle(hlink_style);

            try (FileOutputStream out = new FileOutputStream("hyperinks.xlsx")) {
                wb.write(out);
            }
        }
    }
}
