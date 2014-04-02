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

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.*;

import java.io.FileOutputStream;

/**
 * Working with borders
 */
public class WorkingWithBorders {
    public static void main(String[] args) throws Exception {
        Workbook wb = new XSSFWorkbook();  //or new HSSFWorkbook();
        Sheet sheet = wb.createSheet("borders");

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow((short) 1);

        // Create a cell and put a value in it.
        Cell cell = row.createCell((short) 1);
        cell.setCellValue(4);

        // Style the cell with borders all around.
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.GREEN.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLUE.getIndex());
        style.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cell.setCellStyle(style);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("xssf-borders.xlsx");
        wb.write(fileOut);
        fileOut.close();
    }
}
