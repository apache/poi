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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTRowImpl;

/**
 * Shows how various alignment options work.
 *
 * Modified by Cristian Petrula, Romania on May 26, 2010
 * New method was added centerAcrossSelection to center a column content over 
 * one selection using {@link HorizontalAlignment#CENTER_SELECTION}
 * To create this method example was change for XSSF only and the previous
 * AligningCells.java example has been moved into the SS examples folder.
 */
public class AligningCells {

    public static void main(String[] args) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(2);
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

            //center text over B4, C4, D4
            row = sheet.createRow(3);
            centerAcrossSelection(wb, row, 1, 3, VerticalAlignment.CENTER);

            // Write the output to a file
            try (OutputStream fileOut = new FileOutputStream("xssf-align.xlsx")) {
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
    private static void createCell(XSSFWorkbook wb, XSSFRow row, int column,
                                   HorizontalAlignment halign, VerticalAlignment valign) {
        CreationHelper ch = wb.getCreationHelper();
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(ch.createRichTextString("Align It"));
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(halign);
        cellStyle.setVerticalAlignment(valign);
        cell.setCellStyle(cellStyle);
    }

    /**
     * Center a text over multiple columns using ALIGN_CENTER_SELECTION
     *
     * @param wb the workbook
     * @param row the row to create the cell in
     * @param start_column  the column number to create the cell in and where the selection starts
     * @param end_column    the column number where the selection ends
     * @param valign the horizontal alignment for the cell.
     */
    private static void centerAcrossSelection(XSSFWorkbook wb, XSSFRow row,
            int start_column, int end_column, VerticalAlignment valign) {
        CreationHelper ch = wb.getCreationHelper();

        // Create cell style with ALIGN_CENTER_SELECTION
        XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER_SELECTION);
        cellStyle.setVerticalAlignment(valign);

        // Create cells over the selected area
        for (int i = start_column; i <= end_column; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
        }

        // Set value to the first cell
        XSSFCell cell = row.getCell(start_column);
        cell.setCellValue(ch.createRichTextString("Align It"));

        // Make the selection
        CTRowImpl ctRow = (CTRowImpl) row.getCTRow();

        // Add object with format start_coll:end_coll. For example 1:3 will span from
        // cell 1 to cell 3, where the column index starts with 0
        //
        // You can add multiple spans for one row
        Object span = start_column + ":" + end_column;

        List<Object> spanList = new ArrayList<>();
        spanList.add(span);

        //add spns to the row
        ctRow.setSpans(spanList);
    }
}
