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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
     */
    @BeforeEach
    protected abstract void setUp();

    @AfterEach
    void tearDown() throws IOException {
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
    void testAddRowLabelToPivotTable() {
        assertEquals(0, pivotTable.getRowLabelColumns().size());

        pivotTable.addRowLabel(0);
        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertEquals(0, definition.getRowFields().getFieldArray(0).getX());
        assertEquals(1, definition.getRowFields().getCount());
        assertEquals(1, pivotTable.getRowLabelColumns().size());

        pivotTable.addRowLabel(1);
        assertEquals(2, pivotTable.getRowLabelColumns().size());

        assertEquals(0, (int)pivotTable.getRowLabelColumns().get(0));
        assertEquals(1, (int)pivotTable.getRowLabelColumns().get(1));
    }

    /**
     * Verify that it's not possible to create a row label outside of the referenced area.
     */
    @Test
    void testAddRowLabelOutOfRangeThrowsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> pivotTable.addRowLabel(5));
    }

    /**
     * Verify that when creating one column label, no col fields are being created.
     */
    @Test
    void testAddOneColumnLabelToPivotTableDoesNotCreateColField() {
        int columnIndex = 0;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex);
        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertNull(definition.getColFields());
    }

    /**
     * Verify that it's possible to create three column labels with different DataConsolidateFunction
     */
    @Test
    void testAddThreeDifferentColumnLabelsToPivotTable() {
        int columnOne = 0;
        int columnTwo = 1;
        int columnThree = 2;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnOne);
        pivotTable.addColumnLabel(DataConsolidateFunction.MAX, columnTwo);
        pivotTable.addColumnLabel(DataConsolidateFunction.MIN, columnThree);
        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertEquals(3, definition.getDataFields().getDataFieldList().size());
    }


    /**
     * Verify that it's possible to create three column labels with the same DataConsolidateFunction
     */
    @Test
    void testAddThreeSametColumnLabelsToPivotTable() {
        int columnOne = 0;
        int columnTwo = 1;
        int columnThree = 2;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnOne);
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnTwo);
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnThree);
        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertEquals(3, definition.getDataFields().getDataFieldList().size());
    }

    /**
     * Verify that when creating two column labels, a col field is being created and X is set to -2.
     */
    @Test
    void testAddTwoColumnLabelsToPivotTable() {
        int columnOne = 0;
        int columnTwo = 1;

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnOne);
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnTwo);
        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertEquals(definition.getColFields().getFieldArray(0).getX(), -2);
    }

    /**
     * Verify that a data field is created when creating a data column
     */
    @Test
    void testColumnLabelCreatesDataField() {
        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, 0);

        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertEquals(0, definition.getDataFields().getDataFieldArray(0).getFld());
        assertSame(STDataConsolidateFunction.Enum.forInt(DataConsolidateFunction.SUM.getValue()),
                definition.getDataFields().getDataFieldArray(0).getSubtotal());
    }

    /**
     * Verify that it's possible to set a custom name when creating a data column
     */
    @Test
    void testColumnLabelSetCustomName() {
        int columnIndex = 0;

        String customName = "Custom Name";

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex, customName);

        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertEquals(definition.getDataFields().getDataFieldArray(0).getFld(), columnIndex);
        assertEquals(definition.getDataFields().getDataFieldArray(0).getName(), customName);
    }

    /**
     * Verify that it's possible to set the format to the data column
     */
    @Test
    void testColumnLabelSetDataFormat() {
        int columnIndex = 0;

        String format = "#,##0.0";

        pivotTable.addColumnLabel(DataConsolidateFunction.SUM, columnIndex, null, format);

        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertEquals(definition.getDataFields().getDataFieldArray(0).getFld(), columnIndex);
        assertEquals(definition.getDataFields().getDataFieldArray(0).getNumFmtId(), wb.createDataFormat().getFormat(format));
    }

    /**
     * Verify that it's not possible to create a column label outside of the referenced area.
     */
    @Test
    void testAddColumnLabelOutOfRangeThrowsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> pivotTable.addColumnLabel(DataConsolidateFunction.SUM, 5));
    }

     /**
     * Verify when creating a data column set to a data field, the data field with the corresponding
     * column index will be set to true.
     */
    @Test
    void testAddDataColumn() {
        pivotTable.addDataColumn(0, true);
        CTPivotFields pivotFields = pivotTable.getCTPivotTableDefinition().getPivotFields();
        assertTrue(pivotFields.getPivotFieldArray(0).getDataField());
    }

    /**
     * Verify that it's not possible to create a data column outside of the referenced area.
     */
    @Test
    void testAddDataColumnOutOfRangeThrowsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> pivotTable.addDataColumn(5, true));
    }

     /**
     * Verify that it's possible to create a new filter
     */
    @Test
    void testAddReportFilter() {
        int columnIndex = 0;

        pivotTable.addReportFilter(columnIndex);
        CTPageFields fields = pivotTable.getCTPivotTableDefinition().getPageFields();
        CTPageField field = fields.getPageFieldArray(0);
        assertEquals(field.getFld(), columnIndex);
        assertEquals(-1, field.getHier());
        assertEquals(1, fields.getCount());
    }

     /**
     * Verify that it's not possible to create a new filter outside of the referenced area.
     */
    @Test
    void testAddReportFilterOutOfRangeThrowsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> pivotTable.addReportFilter(5));
    }

    /**
     * Verify that the Pivot Table operates only within the referenced area, even when the
     * first column of the referenced area is not index 0.
     */
    @Test
    void testAddDataColumnWithOffsetData() {
        offsetPivotTable.addColumnLabel(DataConsolidateFunction.SUM, 1);
        assertEquals(CellType.NUMERIC, offsetOuterCell.getCellType());

        offsetPivotTable.addColumnLabel(DataConsolidateFunction.SUM, 0);
    }

    @Test
    void testPivotTableSheetNamesAreCaseInsensitive() {
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
    void testAddColLabelToPivotTable() {
        assertEquals(0, pivotTable.getColLabelColumns().size());

        pivotTable.addColLabel(0);
        CTPivotTableDefinition definition = pivotTable.getCTPivotTableDefinition();

        assertEquals(0, definition.getColFields().getFieldArray(0).getX());
        assertEquals(1, definition.getColFields().getCount());
        assertEquals(1, pivotTable.getColLabelColumns().size());

        pivotTable.addColLabel(1);
        assertEquals(2, pivotTable.getColLabelColumns().size());

        assertEquals(0, (int)pivotTable.getColLabelColumns().get(0));
        assertEquals(1, (int)pivotTable.getColLabelColumns().get(1));
    }

    /**
     * Verify that it's not possible to create a col label outside of the referenced area.
     */
    @Test
    void testAddColLabelOutOfRangeThrowsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> pivotTable.addColLabel(5));
    }
}
