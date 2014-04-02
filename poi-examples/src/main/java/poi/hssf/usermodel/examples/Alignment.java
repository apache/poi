
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

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Shows how various alignment options work.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class Alignment {
    public static void main(String[] args) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");
        HSSFRow row = sheet.createRow(2);
        createCell(wb, row, 0, HSSFCellStyle.ALIGN_CENTER);
        createCell(wb, row, 1, HSSFCellStyle.ALIGN_CENTER_SELECTION);
        createCell(wb, row, 2, HSSFCellStyle.ALIGN_FILL);
        createCell(wb, row, 3, HSSFCellStyle.ALIGN_GENERAL);
        createCell(wb, row, 4, HSSFCellStyle.ALIGN_JUSTIFY);
        createCell(wb, row, 5, HSSFCellStyle.ALIGN_LEFT);
        createCell(wb, row, 6, HSSFCellStyle.ALIGN_RIGHT);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }

    /**
     * Creates a cell and aligns it a certain way.
     *
     * @param wb        the workbook
     * @param row       the row to create the cell in
     * @param column    the column number to create the cell in
     * @param align     the alignment for the cell.
     */
    private static void createCell(HSSFWorkbook wb, HSSFRow row, int column, int align) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue("Align It");
        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment((short)align);
        cell.setCellStyle(cellStyle);
    }
}
