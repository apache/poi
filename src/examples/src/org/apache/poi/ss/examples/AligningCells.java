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
package org.apache.poi.ss.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Shows how various alignment options work.
 */
public class AligningCells {

    public static void main(String[] args) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) { //or new HSSFWorkbook();

            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(2);
            row.setHeightInPoints(30);
            for (int i = 0; i < 8; i++) {
                //column width is set in units of 1/256th of a character width
                sheet.setColumnWidth(i, 256 * 15);
            }

            createCell(wb, row, 0, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
            createCell(wb, row, 1, HorizontalAlignment.CENTER_SELECTION, VerticalAlignment.BOTTOM);
            createCell(wb, row, 2, HorizontalAlignment.FILL, VerticalAlignment.CENTER);
            createCell(wb, row, 3, HorizontalAlignment.GENERAL, VerticalAlignment.CENTER);
            createCell(wb, row, 4, HorizontalAlignment.JUSTIFY, VerticalAlignment.JUSTIFY);
            createCell(wb, row, 5, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
            createCell(wb, row, 6, HorizontalAlignment.RIGHT, VerticalAlignment.TOP);

            // Write the output to a file
            try (OutputStream fileOut = new FileOutputStream("ss-example-align.xlsx")) {
                wb.write(fileOut);
            }
        }
    }

    /**
     * Creates a cell and aligns it a certain way.
     *
     * @param wb     the workbook
     * @param row    the row to create the cell in
     * @param column the column number to create the cell in
     * @param halign the horizontal alignment for the cell.
     */
    private static void createCell(Workbook wb, Row row, int column, HorizontalAlignment halign, VerticalAlignment valign) {
        CreationHelper ch = wb.getCreationHelper();
        Cell cell = row.createCell(column);
        cell.setCellValue(ch.createRichTextString("Align It"));
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(halign);
        cellStyle.setVerticalAlignment(valign);
        cell.setCellStyle(cellStyle);
    }
}