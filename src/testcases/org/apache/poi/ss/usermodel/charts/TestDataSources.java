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
package org.apache.poi.ss.usermodel.charts;

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetBuilder;

/**
 * Tests for {@link org.apache.poi.ss.usermodel.charts.DataSources}.
 *
 * @author Roman Kashitsyn
 */
public class TestDataSources extends TestCase {

    private static final Object[][] numericCells = {
            {0.0,      1.0,       2.0,     3.0,      4.0},
            {0.0, "=B1*2",  "=C1*2", "=D1*2", "=E1*2"}
    };

    private static final Object[][] stringCells = {
            {  1,    2,    3,   4,    5},
            {"A", "B", "C", "D", "E"}
    };

    private static final Object[][] mixedCells = {
            {1.0, "2.0", 3.0, "4.0", 5.0, "6.0"}
    };

    public void testNumericArrayDataSource() {
        Double[] doubles = new Double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        ChartDataSource<Double> doubleDataSource = DataSources.fromArray(doubles);
        assertTrue(doubleDataSource.isNumeric());
        assertFalse(doubleDataSource.isReference());
        assertDataSourceIsEqualToArray(doubleDataSource, doubles);
    }

    public void testStringArrayDataSource() {
        String[] strings = new String[]{"one", "two", "three", "four", "five"};
        ChartDataSource<String> stringDataSource = DataSources.fromArray(strings);
        assertFalse(stringDataSource.isNumeric());
        assertFalse(stringDataSource.isReference());
        assertDataSourceIsEqualToArray(stringDataSource, strings);
    }

    public void testNumericCellDataSource() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, numericCells).build();
        CellRangeAddress numCellRange = CellRangeAddress.valueOf("A2:E2");
        ChartDataSource<Number> numDataSource = DataSources.fromNumericCellRange(sheet, numCellRange);
        assertTrue(numDataSource.isReference());
        assertTrue(numDataSource.isNumeric());
        assertEquals(numericCells[0].length, numDataSource.getPointCount());
        for (int i = 0; i < numericCells[0].length; ++i) {
            assertEquals(((Number) numericCells[0][i]).doubleValue() * 2,
                    numDataSource.getPointAt(i).doubleValue(), 0.00001);
        }
    }

    public void testStringCellDataSource() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, stringCells).build();
        CellRangeAddress numCellRange = CellRangeAddress.valueOf("A2:E2");
        ChartDataSource<String> numDataSource = DataSources.fromStringCellRange(sheet, numCellRange);
        assertTrue(numDataSource.isReference());
        assertFalse(numDataSource.isNumeric());
        assertEquals(numericCells[0].length, numDataSource.getPointCount());
        for (int i = 0; i < stringCells[1].length; ++i) {
            assertEquals(stringCells[1][i], numDataSource.getPointAt(i));
        }
    }

    public void testMixedCellDataSource() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, mixedCells).build();
        CellRangeAddress mixedCellRange = CellRangeAddress.valueOf("A1:F1");
        ChartDataSource<String> strDataSource = DataSources.fromStringCellRange(sheet, mixedCellRange);
        ChartDataSource<Number> numDataSource = DataSources.fromNumericCellRange(sheet, mixedCellRange);
        for (int i = 0; i < mixedCells[0].length; ++i) {
            if (i % 2 == 0) {
                assertNull(strDataSource.getPointAt(i));
                assertEquals(((Number) mixedCells[0][i]).doubleValue(),
                        numDataSource.getPointAt(i).doubleValue(), 0.00001);
            } else {
                assertNull(numDataSource.getPointAt(i));
                assertEquals(mixedCells[0][i], strDataSource.getPointAt(i));
            }
        }
    }

    public void testIOBExceptionOnInvalidIndex() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, numericCells).build();
        CellRangeAddress rangeAddress = CellRangeAddress.valueOf("A2:E2");
        ChartDataSource<Number> numDataSource = DataSources.fromNumericCellRange(sheet, rangeAddress);
        IndexOutOfBoundsException exception = null;
        try {
            numDataSource.getPointAt(-1);
        } catch (IndexOutOfBoundsException e) {
            exception = e;
        }
        assertNotNull(exception);

        exception = null;
        try {
            numDataSource.getPointAt(numDataSource.getPointCount());
        } catch (IndexOutOfBoundsException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    private <T> void assertDataSourceIsEqualToArray(ChartDataSource<T> ds, T[] array) {
        assertEquals(ds.getPointCount(), array.length);
        for (int i = 0; i < array.length; ++i) {
            assertEquals(ds.getPointAt(i), array[i]);
        }
    }
}
