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

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;

import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Demonstrates how to create hyperlinks.
 *
 * @author Yegor Kozlov (yegor at apach.org)
 */
public class Hyperlinks {

    public static void main(String[] args) throws IOException  {
        HSSFWorkbook wb = new HSSFWorkbook();

        //cell style for hyperlinks
        //by default hyperlinks are blue and underlined
        HSSFCellStyle hlink_style = wb.createCellStyle();
        HSSFFont hlink_font = wb.createFont();
        hlink_font.setUnderline(HSSFFont.U_SINGLE);
        hlink_font.setColor(HSSFColor.BLUE.index);
        hlink_style.setFont(hlink_font);

        HSSFCell cell;
        HSSFSheet sheet = wb.createSheet("Hyperlinks");

        //URL
        cell = sheet.createRow(0).createCell(0);
        cell.setCellValue("URL Link");
        HSSFHyperlink link = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
        link.setAddress("http://poi.apache.org/");
        cell.setHyperlink(link);
        cell.setCellStyle(hlink_style);

        //link to a file in the current directory
        cell = sheet.createRow(1).createCell(0);
        cell.setCellValue("File Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_FILE);
        link.setAddress("link1.xls");
        cell.setHyperlink(link);
        cell.setCellStyle(hlink_style);

        //e-mail link
        cell = sheet.createRow(2).createCell(0);
        cell.setCellValue("Email Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_EMAIL);
        //note, if subject contains white spaces, make sure they are url-encoded
        link.setAddress("mailto:poi@apache.org?subject=Hyperlinks");
        cell.setHyperlink(link);
        cell.setCellStyle(hlink_style);

        //link to a place in this workbook

        //create a target sheet and cell
        HSSFSheet sheet2 = wb.createSheet("Target Sheet");
        sheet2.createRow(0).createCell(0).setCellValue("Target Cell");

        cell = sheet.createRow(3).createCell(0);
        cell.setCellValue("Worksheet Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_DOCUMENT);
        link.setAddress("'Target Sheet'!A1");
        cell.setHyperlink(link);
        cell.setCellStyle(hlink_style);

        FileOutputStream out = new FileOutputStream("hssf-links.xls");
        wb.write(out);
        out.close();
    }
}
