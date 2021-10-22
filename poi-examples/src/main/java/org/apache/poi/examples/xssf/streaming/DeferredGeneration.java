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

package org.apache.poi.examples.xssf.streaming;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.DeferredSXSSFSheet;
import org.apache.poi.xssf.streaming.DeferredSXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This sample demonstrates how to use DeferredSXSSFWorkbook to generate workbooks in a streaming way.
 * This approach reduces the use of temporary files and can be used to output to streams like
 * HTTP response streams.
 */
public class DeferredGeneration {

    public static void main(String[] args) throws IOException {
        try (DeferredSXSSFWorkbook wb = new DeferredSXSSFWorkbook()) {
            DeferredSXSSFSheet sheet1 = wb.createSheet("new sheet");

            // cell styles should be created outside the row generator function
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);

            sheet1.setRowGenerator((ssxSheet) -> {
                for (int i = 0; i < 10; i++) {
                    Row row = ssxSheet.createRow(i);
                    Cell cell = row.createCell(1);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("value " + i);
                }
            });

            try (FileOutputStream fileOut = new FileOutputStream("DeferredGeneration.xlsx")) {
                wb.write(fileOut);
                //writeAvoidingTempFiles was added as an experimental change in POI 5.1.0
                //wb.writeAvoidingTempFiles(fileOut);
            } finally {
                //the dispose call is necessary to ensure temp files are removed
                wb.dispose();
            }
            System.out.println("wrote DeferredGeneration.xlsx");
        }
    }
}
