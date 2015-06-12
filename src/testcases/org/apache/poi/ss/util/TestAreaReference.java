package org.apache.poi.ss.util;

import org.apache.poi.ss.SpreadsheetVersion;

import junit.framework.TestCase;

/**
 * Test for {@link AreaReference} handling of max rows.
 * 
 * @author David North
 */
public class TestAreaReference extends TestCase {
    
    public void testWholeColumn() {
        AreaReference oldStyle = AreaReference.getWholeColumn(SpreadsheetVersion.EXCEL97, "A", "B");
        assertEquals(0, oldStyle.getFirstCell().getCol());
        assertEquals(0, oldStyle.getFirstCell().getRow());
        assertEquals(1, oldStyle.getLastCell().getCol());
        assertEquals(SpreadsheetVersion.EXCEL97.getLastRowIndex(), oldStyle.getLastCell().getRow());
        assertTrue(oldStyle.isWholeColumnReference());

        AreaReference oldStyleNonWholeColumn = new AreaReference("A1:B23", SpreadsheetVersion.EXCEL97);
        assertFalse(oldStyleNonWholeColumn.isWholeColumnReference());

        AreaReference newStyle = AreaReference.getWholeColumn(SpreadsheetVersion.EXCEL2007, "A", "B");
        assertEquals(0, newStyle.getFirstCell().getCol());
        assertEquals(0, newStyle.getFirstCell().getRow());
        assertEquals(1, newStyle.getLastCell().getCol());
        assertEquals(SpreadsheetVersion.EXCEL2007.getLastRowIndex(), newStyle.getLastCell().getRow());
        assertTrue(newStyle.isWholeColumnReference());

        AreaReference newStyleNonWholeColumn = new AreaReference("A1:B23", SpreadsheetVersion.EXCEL2007);
        assertFalse(newStyleNonWholeColumn.isWholeColumnReference());
    }
    
    public void testWholeRow() {
        AreaReference oldStyle = AreaReference.getWholeRow(SpreadsheetVersion.EXCEL97, "1", "2");
        assertEquals(0, oldStyle.getFirstCell().getCol());
        assertEquals(0, oldStyle.getFirstCell().getRow());
        assertEquals(SpreadsheetVersion.EXCEL97.getLastColumnIndex(), oldStyle.getLastCell().getCol());
        assertEquals(1, oldStyle.getLastCell().getRow());
        
        AreaReference newStyle = AreaReference.getWholeRow(SpreadsheetVersion.EXCEL2007, "1", "2");
        assertEquals(0, newStyle.getFirstCell().getCol());
        assertEquals(0, newStyle.getFirstCell().getRow());
        assertEquals(SpreadsheetVersion.EXCEL2007.getLastColumnIndex(), newStyle.getLastCell().getCol());
        assertEquals(1, newStyle.getLastCell().getRow());
    }

    @SuppressWarnings("deprecation") // deliberate test for behaviour if deprecated constructor used.
    public void testFallbackToExcel97IfVersionNotSupplied() {
        assertTrue(new AreaReference("A:B").isWholeColumnReference());
        assertTrue(AreaReference.isWholeColumnReference(null, new CellReference("A$1"), new CellReference("A$" + SpreadsheetVersion.EXCEL97.getMaxRows())));
    }
}
