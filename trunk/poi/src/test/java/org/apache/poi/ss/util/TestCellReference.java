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

package org.apache.poi.ss.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/**
 * Tests that the common CellReference works as we need it to.
 * Note - some additional testing is also done in the HSSF class,
 *  {@link org.apache.poi.hssf.util.TestCellReference}
 */
final class TestCellReference {
    @Test
    void testConstructors() {
        CellReference cellReference;
        final String sheet = "Sheet1";
        final String cellRef = "A1";
        final int row = 0;
        final int col = 0;
        final boolean absRow = true;
        final boolean absCol = false;

        cellReference = new CellReference(row, col);
        assertEquals("A1", cellReference.formatAsString());
        assertEquals("A1", cellReference.formatAsString(true));
        assertEquals("A1", cellReference.formatAsString(false));

        cellReference = new CellReference(row, col, absRow, absCol);
        assertEquals("A$1", cellReference.formatAsString());
        assertEquals("A$1", cellReference.formatAsString(true));
        assertEquals("A$1", cellReference.formatAsString(false));

        cellReference = new CellReference(row, (short)col);
        assertEquals("A1", cellReference.formatAsString());
        assertEquals("A1", cellReference.formatAsString(true));
        assertEquals("A1", cellReference.formatAsString(false));

        cellReference = new CellReference(cellRef);
        assertEquals("A1", cellReference.formatAsString());
        assertEquals("A1", cellReference.formatAsString(true));
        assertEquals("A1", cellReference.formatAsString(false));

        cellReference = new CellReference(sheet, row, col, absRow, absCol);
        assertEquals("Sheet1!A$1", cellReference.formatAsString());
        assertEquals("Sheet1!A$1", cellReference.formatAsString(true));
        assertEquals("A$1", cellReference.formatAsString(false));

        cellReference = new CellReference(sheet+"!$A1");
        assertFalse(cellReference.isRowAbsolute());
        assertTrue(cellReference.isColAbsolute());

        cellReference = new CellReference(sheet+"!A$1");
        assertTrue(cellReference.isRowAbsolute());
        assertFalse(cellReference.isColAbsolute());
    }

    @Test
    void testCtorFromCell() {
        Cell cell = mock(Cell.class, RETURNS_DEEP_STUBS);
        when(cell.getSheet().getSheetName()).thenReturn("sheet");

        CellReference result = new CellReference(cell);
        assertEquals("sheet", result.getSheetName());
        assertEquals(cell.getRowIndex(), result.getRow());
        assertEquals(cell.getColumnIndex(), result.getCol());
        assertFalse(result.isRowAbsolute());
        assertFalse(result.isColAbsolute());
    }

    @Test
    void testFormatAsString() {
        CellReference cellReference;

        cellReference = new CellReference(null, 0, 0, false, false);
        assertEquals("A1", cellReference.formatAsString());
        assertEquals("A1", cellReference.formatAsString(true));
        assertEquals("A1", cellReference.formatAsString(false));

        //absolute references
        cellReference = new CellReference(null, 0, 0, true, false);
        assertEquals("A$1", cellReference.formatAsString());
        assertEquals("A$1", cellReference.formatAsString(true));
        assertEquals("A$1", cellReference.formatAsString(false));

        //sheet name with no spaces
        cellReference = new CellReference("Sheet1", 0, 0, true, false);
        assertEquals("Sheet1!A$1", cellReference.formatAsString());
        assertEquals("Sheet1!A$1", cellReference.formatAsString(true));
        assertEquals("A$1", cellReference.formatAsString(false));

        //sheet name with spaces
        cellReference = new CellReference("Sheet 1", 0, 0, true, false);
        assertEquals("'Sheet 1'!A$1", cellReference.formatAsString());
        assertEquals("'Sheet 1'!A$1", cellReference.formatAsString(true));
        assertEquals("A$1", cellReference.formatAsString(false));
    }

    @Test
    void testGetCellRefParts() {
        CellReference cellReference;
        String[] parts;

        String cellRef = "A1";
        cellReference = new CellReference(cellRef);
        assertEquals(0, cellReference.getCol());
        parts = cellReference.getCellRefParts();
        assertNotNull(parts);
        assertNull(parts[0]);
        assertEquals("1", parts[1]);
        assertEquals("A", parts[2]);

        cellRef = "AA1";
        cellReference = new CellReference(cellRef);
        assertEquals(26, cellReference.getCol());
        parts = cellReference.getCellRefParts();
        assertNotNull(parts);
        assertNull(parts[0]);
        assertEquals("1", parts[1]);
        assertEquals("AA", parts[2]);

        cellRef = "AA100";
        cellReference = new CellReference(cellRef);
        assertEquals(26, cellReference.getCol());
        parts = cellReference.getCellRefParts();
        assertNotNull(parts);
        assertNull(parts[0]);
        assertEquals("100", parts[1]);
        assertEquals("AA", parts[2]);

        cellRef = "AAA300";
        cellReference = new CellReference(cellRef);
        assertEquals(702, cellReference.getCol());
        parts = cellReference.getCellRefParts();
        assertNotNull(parts);
        assertNull(parts[0]);
        assertEquals("300", parts[1]);
        assertEquals("AAA", parts[2]);

        cellRef = "ZZ100521";
        cellReference = new CellReference(cellRef);
        assertEquals(26*26+25, cellReference.getCol());
        parts = cellReference.getCellRefParts();
        assertNotNull(parts);
        assertNull(parts[0]);
        assertEquals("100521", parts[1]);
        assertEquals("ZZ", parts[2]);

        cellRef = "ZYX987";
        cellReference = new CellReference(cellRef);
        assertEquals(26*26*26 + 25*26 + 24 - 1, cellReference.getCol());
        parts = cellReference.getCellRefParts();
        assertNotNull(parts);
        assertNull(parts[0]);
        assertEquals("987", parts[1]);
        assertEquals("ZYX", parts[2]);

        cellRef = "AABC10065";
        cellReference = new CellReference(cellRef);
        parts = cellReference.getCellRefParts();
        assertNotNull(parts);
        assertNull(parts[0]);
        assertEquals("10065", parts[1]);
        assertEquals("AABC", parts[2]);
    }

    @Test
    void testGetColNumFromRef() {
        String cellRef = "A1";
        CellReference cellReference = new CellReference(cellRef);
        assertEquals(0, cellReference.getCol());

        cellRef = "AA1";
        cellReference = new CellReference(cellRef);
        assertEquals(26, cellReference.getCol());

        cellRef = "AB1";
        cellReference = new CellReference(cellRef);
        assertEquals(27, cellReference.getCol());

        cellRef = "BA1";
        cellReference = new CellReference(cellRef);
        assertEquals(26+26, cellReference.getCol());

        cellRef = "CA1";
        cellReference = new CellReference(cellRef);
        assertEquals(26+26+26, cellReference.getCol());

        cellRef = "ZA1";
        cellReference = new CellReference(cellRef);
        assertEquals(26*26, cellReference.getCol());

        cellRef = "ZZ1";
        cellReference = new CellReference(cellRef);
        assertEquals(26*26+25, cellReference.getCol());

        cellRef = "AAA1";
        cellReference = new CellReference(cellRef);
        assertEquals(26*26+26, cellReference.getCol());


        cellRef = "A1100";
        cellReference = new CellReference(cellRef);
        assertEquals(0, cellReference.getCol());

        cellRef = "BC15";
        cellReference = new CellReference(cellRef);
        assertEquals(54, cellReference.getCol());
    }

    @Test
    void testGetRowNumFromRef() {
        String cellRef = "A1";
        CellReference cellReference = new CellReference(cellRef);
        assertEquals(0, cellReference.getRow());

        cellRef = "A12";
        cellReference = new CellReference(cellRef);
        assertEquals(11, cellReference.getRow());

        cellRef = "AS121";
        cellReference = new CellReference(cellRef);
        assertEquals(120, cellReference.getRow());
    }

    @Test
    void testConvertNumToColString() {
        short col = 702;
        String collRef = new CellReference(0, col).formatAsString();
        assertEquals("AAA1", collRef);

        short col2 = 0;
        String collRef2 = new CellReference(0, col2).formatAsString();
        assertEquals("A1", collRef2);

        short col3 = 27;
        String collRef3 = new CellReference(0, col3).formatAsString();
        assertEquals("AB1", collRef3);

        short col4 = 2080;
        String collRef4 = new CellReference(0, col4).formatAsString();
        assertEquals("CBA1", collRef4);
    }

    @Test
    void testBadRowNumber() {
        SpreadsheetVersion v97 = SpreadsheetVersion.EXCEL97;
        SpreadsheetVersion v2007 = SpreadsheetVersion.EXCEL2007;

        confirmCrInRange(true, "A", "1", v97);
        confirmCrInRange(true, "IV", "65536", v97);
        confirmCrInRange(false, "IV", "65537", v97);
        confirmCrInRange(false, "IW", "65536", v97);

        confirmCrInRange(true, "A", "1", v2007);
        confirmCrInRange(true, "XFD", "1048576", v2007);
        confirmCrInRange(false, "XFD", "1048577", v2007);
        confirmCrInRange(false, "XFE", "1048576", v2007);

        assertFalse(CellReference.cellReferenceIsWithinRange("B", "0", v97), "Identified bug 47312a");

        confirmCrInRange(false, "A", "0", v97);
        confirmCrInRange(false, "A", "0", v2007);
    }

    @Test
    void testInvalidReference() {
        for (String s : new String[]{"Sheet1!#REF!", "'MySheetName'!#REF!", "#REF!"}) {
            assertThrows(IllegalArgumentException.class, () -> new CellReference(s),
                "Shouldn't be able to create a #REF! "+s);
        }
    }

    private static void confirmCrInRange(boolean expResult, String colStr, String rowStr, SpreadsheetVersion sv) {
        assertEquals(expResult, CellReference.cellReferenceIsWithinRange(colStr, rowStr, sv),
            "expected (c='" + colStr + "', r='" + rowStr + "' to be "
            + (expResult ? "within" : "out of") + " bounds for version " + sv.name()
        );
    }

    @Test
    void testConvertColStringToIndex() {
        assertEquals(0, CellReference.convertColStringToIndex("A"));
        assertEquals(1, CellReference.convertColStringToIndex("B"));
        assertEquals(14, CellReference.convertColStringToIndex("O"));
        assertEquals(701, CellReference.convertColStringToIndex("ZZ"));
        assertEquals(18252, CellReference.convertColStringToIndex("ZZA"));

        assertEquals(0, CellReference.convertColStringToIndex("$A"));
        assertEquals(1, CellReference.convertColStringToIndex("$B"));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> CellReference.convertColStringToIndex("A$"));
        assertTrue(e.getMessage().contains("A$"));
    }

    @Test
    void testConvertNumColColString() {
        assertEquals("A", CellReference.convertNumToColString(0));
        assertEquals("AV", CellReference.convertNumToColString(47));
        assertEquals("AW", CellReference.convertNumToColString(48));
        assertEquals("BF", CellReference.convertNumToColString(57));

        assertEquals("", CellReference.convertNumToColString(-1));
        assertEquals("", CellReference.convertNumToColString(Integer.MIN_VALUE));
        assertEquals("", CellReference.convertNumToColString(Integer.MAX_VALUE));
        assertEquals("FXSHRXW", CellReference.convertNumToColString(Integer.MAX_VALUE-1));
    }

    /**
     * bug 59684: separateRefParts fails on entire-column references
     */
    @Test
    void entireColumnReferences() {
        CellReference ref = new CellReference("HOME!$169");
        assertEquals("HOME", ref.getSheetName());
        assertEquals(168, ref.getRow());
        assertEquals(-1, ref.getCol());
        assertTrue(ref.isRowAbsolute(), "row absolute");
        //assertFalse("column absolute/relative is undefined", ref.isColAbsolute());
    }

    @Test
    void getSheetName() {
        assertNull(new CellReference("A5").getSheetName());
        assertNull(new CellReference(null, 0, 0, false, false).getSheetName());
        // FIXME: CellReference is inconsistent
        assertEquals("", new CellReference("", 0, 0, false, false).getSheetName());
        assertEquals("Sheet1", new CellReference("Sheet1!A5").getSheetName());
        assertEquals("Sheet 1", new CellReference("'Sheet 1'!A5").getSheetName());
    }

    @Test
    void testToString() {
        CellReference ref = new CellReference("'Sheet 1'!A5");
        assertEquals("org.apache.poi.ss.util.CellReference ['Sheet 1'!A5]", ref.toString());
    }

    @Test
    void testEqualsAndHashCode() {
        CellReference ref1 = new CellReference("'Sheet 1'!A5");
        CellReference ref2 = new CellReference("Sheet 1", 4, 0, false, false);
        assertEquals(ref1, ref2);
        assertEquals(ref1.hashCode(), ref2.hashCode());

        //noinspection ObjectEqualsNull
        assertNotEquals(null, ref1);
        assertNotEquals(ref1, new CellReference("A5"));
    }

    @Test
    void isRowWithinRange() {
        SpreadsheetVersion ss = SpreadsheetVersion.EXCEL2007;
        assertFalse(CellReference.isRowWithinRange("0", ss), "1 before first row");
        assertTrue(CellReference.isRowWithinRange("1", ss), "first row");
        assertTrue(CellReference.isRowWithinRange("1048576", ss), "last row");
        assertFalse(CellReference.isRowWithinRange("1048577", ss), "1 beyond last row");

        // int versions of above, using 0-based indices
        assertFalse(CellReference.isRowWithinRange(-1, ss), "1 before first row");
        assertTrue(CellReference.isRowWithinRange(0, ss), "first row");
        assertTrue(CellReference.isRowWithinRange(1048575, ss), "last row");
        assertFalse(CellReference.isRowWithinRange(1048576, ss), "1 beyond last row");
    }

    @Test
    void isRowWithinRangeNonInteger_BigNumber() {
        String rowNum = "4000000000";
        assertFalse(CellReference.isRowWithinRange(rowNum, SpreadsheetVersion.EXCEL2007));
    }

    @Test
    void isRowWithinRangeNonInteger_Alpha() {
        String rowNum = "NotANumber";
        assertThrows(NumberFormatException.class, () -> CellReference.isRowWithinRange(rowNum, SpreadsheetVersion.EXCEL2007));
    }

    @Test
    void isColWithinRange() {
        SpreadsheetVersion ss = SpreadsheetVersion.EXCEL2007;
        assertTrue(CellReference.isColumnWithinRange("", ss), "(empty)");
        assertTrue(CellReference.isColumnWithinRange("A", ss), "first column (A)");
        assertTrue(CellReference.isColumnWithinRange("XFD", ss), "last column (XFD)");
        assertFalse(CellReference.isColumnWithinRange("XFE", ss), "1 beyond last column (XFE)");
    }

    @Test
    void unquotedSheetName() {
        assertThrows(IllegalArgumentException.class, () -> new CellReference("'Sheet 1!A5"));
    }

    @Test
    void mismatchedQuotesSheetName() {
        assertThrows(IllegalArgumentException.class, () -> new CellReference("Sheet 1!A5"));
    }

    @Test
    void escapedSheetName() {
        String escapedName = "'Don''t Touch'!A5";
        String unescapedName = "'Don't Touch'!A5";
        new CellReference(escapedName);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new CellReference(unescapedName));
        assertTrue(e.getMessage().startsWith("Bad sheet name quote escaping: "));
    }

    @Test
    void negativeRow() {
        assertThrows(IllegalArgumentException.class, () -> new CellReference("sheet", -2, 0, false, false));
    }

    @Test
    void negativeColumn() {
        assertThrows(IllegalArgumentException.class, () -> new CellReference("sheet", 0, -2, false, false));
    }

    @Test
    void classifyEmptyStringCellReference() {
        assertThrows(IllegalArgumentException.class, () -> CellReference.classifyCellReference("", SpreadsheetVersion.EXCEL2007));
    }

    @Test
    void classifyInvalidFirstCharCellReference() {
        assertThrows(IllegalArgumentException.class, () -> CellReference.classifyCellReference("!A5", SpreadsheetVersion.EXCEL2007));
    }

    @Test
    void test62828() {
        final Workbook wb = new HSSFWorkbook();
        final Sheet sheet = wb.createSheet("Ctor test");
        final String sheetName = sheet.getSheetName();
        final Row row = sheet.createRow(0);
        final Cell cell = row.createCell(0);
        final CellReference goodCellRef = new CellReference(sheetName, cell.getRowIndex(), cell.getColumnIndex(), true,
                true);
        final CellReference badCellRef = new CellReference(cell);

        assertEquals("'Ctor test'!$A$1", goodCellRef.formatAsString());
        assertEquals("'Ctor test'!$A$1", goodCellRef.formatAsString(true));
        assertEquals("$A$1", goodCellRef.formatAsString(false));

        assertEquals("'Ctor test'!A1", badCellRef.formatAsString());
        assertEquals("'Ctor test'!A1", badCellRef.formatAsString(true));
        assertEquals("A1", badCellRef.formatAsString(false));
    }
}
