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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Calendar;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CreatePivotTable2 {

    public static void main(String[] args) throws FileNotFoundException, IOException, InvalidFormatException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            //Create some data to build the pivot table on
            setCellData(sheet);

            AreaReference source = new AreaReference("A1:E7", SpreadsheetVersion.EXCEL2007);
            CellReference position = new CellReference("H1");
            // Create a pivot table on this sheet, with H1 as the top-left cell..
            // The pivot table's data source is on the same sheet in A1:E7
            XSSFPivotTable pivotTable = sheet.createPivotTable(source, position);
            //Configure the pivot table
            //Use first column as row label
            pivotTable.addRowLabel(0);
            //Sum up the second column with column title and data format
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, 1, "Values", "#,##0.00");
            //Use third column (month) as columns (side by side)
            pivotTable.addColLabel(3, "DD.MM.YYYY");

            //Add filter on forth column
            pivotTable.addReportFilter(4);

            try (FileOutputStream fileOut = new FileOutputStream("ooxml-pivottable2.xlsx")) {
                wb.write(fileOut);
            }
        }
    }

    public static void setCellData(XSSFSheet sheet){
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2017, 0, 1, 0, 0, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2017, 1, 1, 0, 0, 0);
        Row row1 = sheet.createRow(0);
        // Create a cell and put a value in it.
        // first row are column titles
        Cell cell11 = row1.createCell(0);
        cell11.setCellValue("Names");
        Cell cell12 = row1.createCell(1);
        cell12.setCellValue("Values");
        Cell cell13 = row1.createCell(2);
        cell13.setCellValue("%");
        Cell cell14 = row1.createCell(3);
        cell14.setCellValue("Month");
        Cell cell15 = row1.createCell(4);
        cell15.setCellValue("No");

        CellStyle csDbl = sheet.getWorkbook().createCellStyle();
        DataFormat dfDbl = sheet.getWorkbook().createDataFormat();
        csDbl.setDataFormat(dfDbl.getFormat("#,##0.00"));

        CellStyle csDt = sheet.getWorkbook().createCellStyle();
        DataFormat dfDt = sheet.getWorkbook().createDataFormat();
        csDt.setDataFormat(dfDt.getFormat("dd/MM/yyyy"));
        // data
        setDataRow(sheet, 1, "Jane", 1120.5, 100, cal1.getTime(), 1, csDbl, csDt);
        setDataRow(sheet, 2, "Jane", 1453.2, 95, cal2.getTime(), 2, csDbl, csDt);

        setDataRow(sheet, 3, "Tarzan", 1869.8, 88, cal1.getTime(), 1, csDbl, csDt);
        setDataRow(sheet, 4, "Tarzan", 1536.2, 92, cal2.getTime(), 2, csDbl, csDt);

        setDataRow(sheet, 5, "Terk", 1624.1, 75, cal1.getTime(), 1, csDbl, csDt);
        setDataRow(sheet, 6, "Terk", 1569.3, 82, cal2.getTime(), 2, csDbl, csDt);
        sheet.autoSizeColumn(3);
    }

    public static void setDataRow(XSSFSheet sheet, int rowNum, String name, double v1, int v2, Date dt, int no, CellStyle csDbl, CellStyle csDt){
        Row row = sheet.createRow(rowNum);
        // set the values for one row
        Cell c1 = row.createCell(0);
        c1.setCellValue(name);
        Cell c2 = row.createCell(1);
        c2.setCellValue(v1);
        c2.setCellStyle(csDbl);
        Cell c3 = row.createCell(2);
        c3.setCellValue(v2);
        Cell c4 = row.createCell(3);
        c4.setCellValue(dt);
        c4.setCellStyle(csDt);
        Cell c5 = row.createCell(4);
        c5.setCellValue(no);
    }

}
