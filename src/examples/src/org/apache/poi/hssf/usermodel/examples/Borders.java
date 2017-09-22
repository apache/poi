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

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderStyle;

/**
 * Demonstrates how to create borders around cells.
 */
public class Borders {
    public static void main(String[] args) throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet("new sheet");

            // Create a row and put some cells in it. Rows are 0 based.
            HSSFRow row = sheet.createRow(1);

            // Create a cell and put a value in it.
            HSSFCell cell = row.createCell(1);
            cell.setCellValue(4);

            // Style the cell with borders all around.
            HSSFCellStyle style = wb.createCellStyle();
            style.setBorderBottom(BorderStyle.THIN);
            style.setBottomBorderColor(HSSFColorPredefined.BLACK.getIndex());
            style.setBorderLeft(BorderStyle.THIN);
            style.setLeftBorderColor(HSSFColorPredefined.GREEN.getIndex());
            style.setBorderRight(BorderStyle.THIN);
            style.setRightBorderColor(HSSFColorPredefined.BLUE.getIndex());
            style.setBorderTop(BorderStyle.MEDIUM_DASHED);
            style.setTopBorderColor(HSSFColorPredefined.ORANGE.getIndex());
            cell.setCellStyle(style);

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream("workbook.xls")) {
                wb.write(fileOut);
            }
        }
    }
}
