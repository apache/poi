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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

/**
 * Shows how various alignment options work.
 */
public class AligningCells {

    public static void main(String[] args)  throws IOException {
        Workbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();

        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow((short) 2);
        row.setHeightInPoints(30);
        for (int i = 0; i < 8; i++) {
            //column width is set in units of 1/256th of a character width
            sheet.setColumnWidth(i, 256*15);
        }

        createCell(wb, row, (short) 0, XSSFCellStyle.ALIGN_CENTER, XSSFCellStyle.VERTICAL_BOTTOM);
        createCell(wb, row, (short) 1, XSSFCellStyle.ALIGN_CENTER_SELECTION, XSSFCellStyle.VERTICAL_BOTTOM);
        createCell(wb, row, (short) 2, XSSFCellStyle.ALIGN_FILL, XSSFCellStyle.VERTICAL_CENTER);
        createCell(wb, row, (short) 3, XSSFCellStyle.ALIGN_GENERAL, XSSFCellStyle.VERTICAL_CENTER);
        createCell(wb, row, (short) 4, XSSFCellStyle.ALIGN_JUSTIFY, XSSFCellStyle.VERTICAL_JUSTIFY);
        createCell(wb, row, (short) 5, XSSFCellStyle.ALIGN_LEFT, XSSFCellStyle.VERTICAL_TOP);
        createCell(wb, row, (short) 6, XSSFCellStyle.ALIGN_RIGHT, XSSFCellStyle.VERTICAL_TOP);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("xssf-align.xlsx");
        wb.write(fileOut);
        fileOut.close();
    }

    /**
     * Creates a cell and aligns it a certain way.
     *
     * @param wb     the workbook
     * @param row    the row to create the cell in
     * @param column the column number to create the cell in
     * @param halign the horizontal alignment for the cell.
     */
    private static void createCell(Workbook wb, Row row, short column, short halign, short valign) {
        Cell cell = row.createCell(column);
        cell.setCellValue(new XSSFRichTextString("Align It"));
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(halign);
        cellStyle.setVerticalAlignment(valign);
        cell.setCellStyle(cellStyle);
    }
}
