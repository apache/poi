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

    public void testDuplicateNames()
            throws Exception
    {
        HSSFWorkbook b = new HSSFWorkbook( );
        b.createSheet("Sheet1");
        b.createSheet();
        b.createSheet("name1");
        try
        {
            b.createSheet("name1");
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }
        b.createSheet();
        try
        {
            b.setSheetName( 3,  "name1" );
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }

        try
        {
            b.setSheetName( 3,  "name1", HSSFWorkbook.ENCODING_UTF_16 );
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }

        b.setSheetName( 3,  "name2", HSSFWorkbook.ENCODING_UTF_16 );
        b.setSheetName( 3,  "name2", HSSFWorkbook.ENCODING_UTF_16 );
        b.setSheetName( 3,  "name2" );
        
        HSSFWorkbook c = new HSSFWorkbook( );
        c.createSheet("Sheet1");
        c.createSheet("Sheet2");
        c.createSheet("Sheet3");
        c.createSheet("Sheet4");

    }
}