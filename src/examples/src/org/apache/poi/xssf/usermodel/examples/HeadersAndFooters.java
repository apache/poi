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

import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class HeadersAndFooters {


    public static void main(String[]args) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("format sheet");
        sheet.createRow(0).createCell(0).setCellValue(123);

        //set page numbers in the footer
        Footer footer = sheet.getFooter();
        //&P == current page number
        //&N == page numbers
        footer.setRight("Page &P of &N");

        // Create various cells and rows for spreadsheet.

        FileOutputStream fileOut = new FileOutputStream("pageNumerOnFooter.xlsx");
        wb.write(fileOut);
        fileOut.close();

    }
}
