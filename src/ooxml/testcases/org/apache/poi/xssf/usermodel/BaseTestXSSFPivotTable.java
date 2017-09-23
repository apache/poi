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

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotTableDefinition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataConsolidateFunction;

public abstract class BaseTestXSSFPivotTable {
    private static final XSSFITestDataProvider _testDataProvider = XSSFITestDataProvider.instance;
    protected XSSFWorkbook wb;
    protected XSSFPivotTable pivotTable;
    protected XSSFPivotTable offsetPivotTable;
    protected Cell offsetOuterCell;
    
    /**
     * required to set up the test pivot tables and cell reference, either by name or reference.
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public abstract void setUp();
    
    @After
    public void tearDown() throws IOException {
        if (wb != null) {
            XSSFWorkbook wb2 = _testDataProvider.writeOutAndReadBack(wb);
            wb.close();
            wb2.close();
        }
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
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddRowLabelOutOfRangeThrowsException() {
        pivotTable.addRowLabel(5);
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
     * Verify that it's possible to set the format to the data column
     */
    @Test
    public void testColumnLabelSetDataFormat() {
        int columnIndex = 0;

        String format = "#,##0.0";
        
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex, null, format);

        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getDataFields().getDataFieldArray(0).getFld(), columnIndex);
        assertEquals(defintion.getDataFields().getDataFieldArray(0).getNumFmtId(), wb.createDataFormat().getFormat(format));
    }

    /**
     * Verify that it's not possible to create a column label outside of the referenced area.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddColumnLabelOutOfRangeThrowsException() {
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, 5);
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
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddDataColumnOutOfRangeThrowsException() {
        pivotTable.addDataColumn(5, true);
    }

     /**
     * Verify that it's possible to create a new filter
     */
    @Test
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
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddReportFilterOutOfRangeThrowsException() {
        pivotTable.addReportFilter(5);
    }
    
    /**
     * Verify that the Pivot Table operates only within the referenced area, even when the
     * first column of the referenced area is not index 0.
     */
    @Test
    public void testAddDataColumnWithOffsetData() {
        offsetPivotTable.addColumnLabel(DataConsolidateFunction.SUM, 1);
        assertEquals(CellType.NUMERIC, offsetOuterCell.getCellType());
        
        offsetPivotTable.addColumnLabel(DataConsolidateFunction.SUM, 0);
    }
    
    @Test
    public void testPivotTableSheetNamesAreCaseInsensitive() {
        wb.setSheetName(0,  "original");
        wb.setSheetName(1,  "offset");
        XSSFSheet original = wb.getSheet("OriginaL");
        XSSFSheet offset = wb.getSheet("OffseT");
        // assume sheets are accessible via case-insensitive name
        assertNotNull(original);
        assertNotNull(offset);
        
        AreaReference source = wb.getCreationHelper().createAreaReference("ORIGinal!A1:C2");
        // create a pivot table on the same sheet, case insensitive
        original.createPivotTable(source, new CellReference("W1"));
        // create a pivot table on a different sheet, case insensitive
        offset.createPivotTable(source, new CellReference("W1"));
    }


    /**
     * Verify that when creating a col label it's  created on the correct column
     * and the count is increased by one.
     */
    @Test
    public void testAddColLabelToPivotTable() {
        int columnIndex = 0;

        assertEquals(0, pivotTable.getColLabelColumns().size());
        
        pivotTable.addColLabel(columnIndex);
        CTPivotTableDefinition defintion = pivotTable.getCTPivotTableDefinition();

        assertEquals(defintion.getColFields().getFieldArray(0).getX(), columnIndex);
        assertEquals(defintion.getColFields().getCount(), 1);
        assertEquals(1, pivotTable.getColLabelColumns().size());
        
        columnIndex = 1;
        pivotTable.addColLabel(columnIndex);
        assertEquals(2, pivotTable.getColLabelColumns().size());
        
        assertEquals(0, (int)pivotTable.getColLabelColumns().get(0));
        assertEquals(1, (int)pivotTable.getColLabelColumns().get(1));
    }

    /**
     * Verify that it's not possible to create a col label outside of the referenced area.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddColLabelOutOfRangeThrowsException() {
        pivotTable.addColLabel(5);
    }
}
