package org.apache.poi.hssf.usermodel;

import junit.framework.*;
import org.apache.poi.hssf.record.NameRecord;

public class TestHSSFWorkbook extends TestCase
{
    HSSFWorkbook hssfWorkbook;

    public void testSetRepeatingRowsAndColumns() throws Exception
    {
        // Test bug 29747
        HSSFWorkbook b = new HSSFWorkbook( );
        b.createSheet();
        b.createSheet();
        b.createSheet();
        b.setRepeatingRowsAndColumns( 2, 0,1,-1,-1 );
        NameRecord nameRecord = b.getWorkbook().getNameRecord( 0 );
        assertEquals( 3, nameRecord.getIndexToSheet() );
    }
}