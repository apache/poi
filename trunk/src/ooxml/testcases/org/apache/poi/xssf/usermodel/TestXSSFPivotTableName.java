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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.junit.Before;

/**
 * Test pivot tables created by named range
 */
public class TestXSSFPivotTableName extends BaseTestXSSFPivotTable {

    @Override
    @Before
    public void setUp(){
        wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();

        Row row1 = sheet.createRow(0);
        // Create a cell and put a value in it.
        Cell cell = row1.createCell(0);
        cell.setCellValue("Names");
        Cell cell2 = row1.createCell(1);
        cell2.setCellValue("#");
        Cell cell7 = row1.createCell(2);
        cell7.setCellValue("Data");
        Cell cell10 = row1.createCell(3);
        cell10.setCellValue("Value");

        Row row2 = sheet.createRow(1);
        Cell cell3 = row2.createCell(0);
        cell3.setCellValue("Jan");
        Cell cell4 = row2.createCell(1);
        cell4.setCellValue(10);
        Cell cell8 = row2.createCell(2);
        cell8.setCellValue("Apa");
        Cell cell11 = row1.createCell(3);
        cell11.setCellValue(11.11);

        Row row3 = sheet.createRow(2);
        Cell cell5 = row3.createCell(0);
        cell5.setCellValue("Ben");
        Cell cell6 = row3.createCell(1);
        cell6.setCellValue(9);
        Cell cell9 = row3.createCell(2);
        cell9.setCellValue("Bepa");
        Cell cell12 = row1.createCell(3);
        cell12.setCellValue(12.12);

        XSSFName namedRange = sheet.getWorkbook().createName();
        namedRange.setRefersToFormula(sheet.getSheetName() + "!" + "A1:C2");
        pivotTable = sheet.createPivotTable(namedRange, new CellReference("H5"));
        
        XSSFSheet offsetSheet = wb.createSheet();
        
        Row tableRow_1 = offsetSheet.createRow(1);
        offsetOuterCell = tableRow_1.createCell(1);
        offsetOuterCell.setCellValue(-1);
        Cell tableCell_1_1 = tableRow_1.createCell(2);
        tableCell_1_1.setCellValue("Row #");
        Cell tableCell_1_2 = tableRow_1.createCell(3);
        tableCell_1_2.setCellValue("Exponent");
        Cell tableCell_1_3 = tableRow_1.createCell(4);
        tableCell_1_3.setCellValue("10^Exponent");
        
        Row tableRow_2 = offsetSheet.createRow(2);
        Cell tableCell_2_1 = tableRow_2.createCell(2);
        tableCell_2_1.setCellValue(0);
        Cell tableCell_2_2 = tableRow_2.createCell(3);
        tableCell_2_2.setCellValue(0);
        Cell tableCell_2_3 = tableRow_2.createCell(4);
        tableCell_2_3.setCellValue(1);
        
        Row tableRow_3= offsetSheet.createRow(3);
        Cell tableCell_3_1 = tableRow_3.createCell(2);
        tableCell_3_1.setCellValue(1);
        Cell tableCell_3_2 = tableRow_3.createCell(3);
        tableCell_3_2.setCellValue(1);
        Cell tableCell_3_3 = tableRow_3.createCell(4);
        tableCell_3_3.setCellValue(10);
        
        Row tableRow_4 = offsetSheet.createRow(4);
        Cell tableCell_4_1 = tableRow_4.createCell(2);
        tableCell_4_1.setCellValue(2);
        Cell tableCell_4_2 = tableRow_4.createCell(3);
        tableCell_4_2.setCellValue(2);
        Cell tableCell_4_3 = tableRow_4.createCell(4);
        tableCell_4_3.setCellValue(100);
        
        namedRange = sheet.getWorkbook().createName();
        namedRange.setRefersToFormula("C2:E4");
        namedRange.setSheetIndex(sheet.getWorkbook().getSheetIndex(sheet));
        offsetPivotTable = offsetSheet.createPivotTable(namedRange, new CellReference("C6"));
    }
}
