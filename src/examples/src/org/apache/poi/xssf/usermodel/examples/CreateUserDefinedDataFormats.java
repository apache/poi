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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * How to set user-defined date formats
 */
public class CreateUserDefinedDataFormats {


    public static void main(String[]args) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {  //or new HSSFWorkbook();
            Sheet sheet = wb.createSheet("format sheet");
            CellStyle style;
            DataFormat format = wb.createDataFormat();
            Row row;
            Cell cell;
            short rowNum = 0;
            short colNum = 0;

            row = sheet.createRow(rowNum);
            cell = row.createCell(colNum);
            cell.setCellValue(11111.25);
            style = wb.createCellStyle();
            style.setDataFormat(format.getFormat("0.0"));
            cell.setCellStyle(style);

            row = sheet.createRow(++rowNum);
            cell = row.createCell(colNum);
            cell.setCellValue(11111.25);
            style = wb.createCellStyle();
            style.setDataFormat(format.getFormat("#,##0.0000"));
            cell.setCellStyle(style);

            try (FileOutputStream fileOut = new FileOutputStream("ooxml_dataFormat.xlsx")) {
                wb.write(fileOut);
            }
        }
    }

}
