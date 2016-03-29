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

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotTableDefinition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataConsolidateFunction;

public class TestXSSFPivotTable extends TestCase {
    private XSSFPivotTable pivotTable;
    
    @Override
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
    }

    /**
     * Verify that when creating a row label it's  created on the correct row
     * and the count is increased by one.
     */
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
    public void testAddOneColumnLabelToPivotTableDoesNotCreateColField() {
        int columnIndex = 0;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex);
        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getColFields(), null);
    }

    /**
     * Verify that it's possible to create three column labels with different DataConsolidateFunction
     */
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
    public void testAddReportFilterOutOfRangeThrowsException() {
        int columnIndex = 5;
        try {
            pivotTable.addReportFilter(columnIndex);
        } catch(IndexOutOfBoundsException e) {
            return;
        }
        fail();
    }
}
