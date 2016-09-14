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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.junit.Before;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotTableDefinition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataConsolidateFunction;

public class TestXSSFPivotTable {
    private XSSFPivotTable pivotTable;
    private XSSFPivotTable offsetPivotTable;
    private Cell offsetOuterCell;
    
    @Before
    public void setUp(){
        Workbook wb = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) wb.createSheet();

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

        AreaReference source = new AreaReference("A1:C2");
        pivotTable = sheet.createPivotTable(source, new CellReference("H5"));
        
        XSSFSheet offsetSheet = (XSSFSheet) wb.createSheet();
        
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
        
        AreaReference offsetSource = new AreaReference(new CellReference("C2"), new CellReference("E4"));
        offsetPivotTable = offsetSheet.createPivotTable(offsetSource, new CellReference("C6"));
    }

    /**
     * Verify that when creating a row label it's  created on the correct row
     * and the count is increased by one.
     */
    @Test
    public void testAddRowLabelToPivotTable() {
        int columnIndex = 0;

        assertEquals(0, pivotTable.getRowLabelColumns().size());
        
        pivotTable.addRowLabel(columnIndex);
        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getRowFields().getFieldArray(0).getX(), columnIndex);
        assertEquals(defintion.getRowFields().getCount(), 1);
        assertEquals(1, pivotTable.getRowLabelColumns().size());
        
        columnIndex = 1;
        pivotTable.addRowLabel(columnIndex);
        assertEquals(2, pivotTable.getRowLabelColumns().size());
        
        assertEquals(0, (int)pivotTable.getRowLabelColumns().get(0));
        assertEquals(1, (int)pivotTable.getRowLabelColumns().get(1));
    }
    /**
     * Verify that it's not possible to create a row label outside of the referenced area.
     */
    @Test
    public void testAddRowLabelOutOfRangeThrowsException() {
        int columnIndex = 5;

        try {
            pivotTable.addRowLabel(columnIndex);
        } catch(IndexOutOfBoundsException e) {
            return;
        }
        fail();
    }

    /**
     * Verify that when creating one column label, no col fields are being created.
     */
    @Test
    public void testAddOneColumnLabelToPivotTableDoesNotCreateColField() {
        int columnIndex = 0;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex);
        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getColFields(), null);
    }

    /**
     * Verify that it's possible to create three column labels with different DataConsolidateFunction
     */
    @Test
    public void testAddThreeDifferentColumnLabelsToPivotTable() {
        int columnOne = 0;
        int columnTwo = 1;
        int columnThree = 2;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnOne);
        pivotTable.addColumnLabel(DataConsolidateFunction.MAX, columnTwo);
        pivotTable.addColumnLabel(DataConsolidateFunction.MIN, columnThree);
        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getDataFields().getDataFieldList().size(), 3);
    }
    
    
    /**
     * Verify that it's possible to create three column labels with the same DataConsolidateFunction
     */
    @Test
    public void testAddThreeSametColumnLabelsToPivotTable() {
        int columnOne = 0;
        int columnTwo = 1;
        int columnThree = 2;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnOne);
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnTwo);
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnThree);
        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getDataFields().getDataFieldList().size(), 3);
    }
    
    /**
     * Verify that when creating two column labels, a col field is being created and X is set to -2.
     */
    @Test
    public void testAddTwoColumnLabelsToPivotTable() {
        int columnOne = 0;
        int columnTwo = 1;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnOne);
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnTwo);
        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getColFields().getFieldArray(0).getX(), -2);
    }

    /**
     * Verify that a data field is created when creating a data column
     */
    @Test
    public void testColumnLabelCreatesDataField() {
        int columnIndex = 0;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex);

        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getDataFields().getDataFieldArray(0).getFld(), columnIndex);
        assertEquals(defintion.getDataFields().getDataFieldArray(0).getSubtotal(),
                STDataConsolidateFunction.Enum.forInt(DataConsolidateFunction.SUM.getValue()));
    }
    
    /**
     * Verify that it's possible to set a custom name when creating a data column
     */
    @Test
    public void testColumnLabelSetCustomName() {
        int columnIndex = 0;

        String customName = "Custom Name";
        
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex, customName);

        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getDataFields().getDataFieldArray(0).getFld(), columnIndex);
        assertEquals(defintion.getDataFields().getDataFieldArray(0).getName(), customName);
    }

    /**
     * Verify that it's not possible to create a column label outside of the referenced area.
     */
    @Test
    public void testAddColumnLabelOutOfRangeThrowsException() {
        int columnIndex = 5;

        try {
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex);
        } catch(IndexOutOfBoundsException e) {
            return;
        }
        fail();
    }

     /**
     * Verify when creating a data column set to a data field, the data field with the corresponding
     * column index will be set to true.
     */
    @Test
    public void testAddDataColumn() {
        int columnIndex = 0;
        boolean isDataField = true;

        pivotTable.addDataColumn(columnIndex, isDataField);
        CTPivotFields pivotFields = pivotTable.getCTPivotTableDefinition().getPivotFields();
        assertEquals(pivotFields.getPivotFieldArray(columnIndex).getDataField(), isDataField);
    }

    /**
     * Verify that it's not possible to create a data column outside of the referenced area.
     */
    @Test
    public void testAddDataColumnOutOfRangeThrowsException() {
        int columnIndex = 5;
        boolean isDataField = true;

        try {
            pivotTable.addDataColumn(columnIndex, isDataField);
        } catch(IndexOutOfBoundsException e) {
            return;
        }
        fail();
    }

     /**
     * Verify that it's possible to create a new filter
     */
    public void testAddReportFilter() {
        int columnIndex = 0;

        pivotTable.addReportFilter(columnIndex);
        CTPageFields fields = pivotTable.getCTPivotTableDefinition().getPageFields();
        CTPageField field = fields.getPageFieldArray(0);
        assertEquals(field.getFld(), columnIndex);
        assertEquals(field.getHier(), -1);
        assertEquals(fields.getCount(), 1);
    }

     /**
     * Verify that it's not possible to create a new filter outside of the referenced area.
     */
    @Test
    public void testAddReportFilterOutOfRangeThrowsException() {
        int columnIndex = 5;
        try {
            pivotTable.addReportFilter(columnIndex);
        } catch(IndexOutOfBoundsException e) {
            return;
        }
        fail();
    }
    
    /**
     * Verify that the Pivot Table operates only within the referenced area, even when the
     * first column of the referenced area is not index 0.
     */
    @Test
    public void testAddDataColumnWithOffsetData() {
        offsetPivotTable.addColumnLabel(DataConsolidateFunction.SUM, 1);
        assertEquals(CellType.NUMERIC, offsetOuterCell.getCellTypeEnum());
        
        offsetPivotTable.addColumnLabel(DataConsolidateFunction.SUM, 0);
    }
}
