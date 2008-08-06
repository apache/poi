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

package org.apache.poi.hssf.model;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.eventmodel.ERFListener;
import org.apache.poi.hssf.eventmodel.EventRecordFactory;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.record.UncalcedRecord;
import org.apache.poi.hssf.record.aggregates.ColumnInfoRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.RowRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.ValueRecordsAggregate;
import org.apache.poi.hssf.util.CellRangeAddress;

/**
 * Unit test for the Sheet class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestSheet extends TestCase {
    public void testCreateSheet() {
        // Check we're adding row and cell aggregates
        List records = new ArrayList();
        records.add( new BOFRecord() );
        records.add( new DimensionsRecord() );
        records.add( new EOFRecord() );
        Sheet sheet = Sheet.createSheet( records, 0, 0 );

        int pos = 0;
        assertTrue( sheet.records.get(pos++) instanceof BOFRecord );
        assertTrue( sheet.records.get(pos++) instanceof ColumnInfoRecordsAggregate );
        assertTrue( sheet.records.get(pos++) instanceof DimensionsRecord );
        assertTrue( sheet.records.get(pos++) instanceof RowRecordsAggregate );
        assertTrue( sheet.records.get(pos++) instanceof ValueRecordsAggregate );
        assertTrue( sheet.records.get(pos++) instanceof EOFRecord );
    }

    private static final class MergedCellListener implements ERFListener {

        private int _count;
        public MergedCellListener() {
            _count = 0;
        }
        public boolean processRecord(Record rec) {
            _count++;
            return true;
        }
        public int getCount() {
            return _count;
        }
    }
    
    public void testAddMergedRegion() {
        Sheet sheet = Sheet.createSheet();
        int regionsToAdd = 4096;

        //simple test that adds a load of regions
        for (int n = 0; n < regionsToAdd; n++)
        {
            int index = sheet.addMergedRegion(0, (short) 0, 1, (short) 1);
            assertTrue("Merged region index expected to be " + n + " got " + index, index == n);
        }

        //test all the regions were indeed added
        assertTrue(sheet.getNumMergedRegions() == regionsToAdd);

        //test that the regions were spread out over the appropriate number of records
        byte[] sheetData = new byte[sheet.getSize()];
        sheet.serialize(0, sheetData);
        MergedCellListener mcListener = new MergedCellListener();
        EventRecordFactory erf = new EventRecordFactory(mcListener, new short[] { MergeCellsRecord.sid, });
//        POIFSFileSystem poifs = new POIFSFileSystem(new ByteArrayInputStream(sheetData));
        erf.processRecords(new ByteArrayInputStream(sheetData));
        int recordsAdded    = mcListener.getCount();
        int recordsExpected = regionsToAdd/1027;
        if ((regionsToAdd % 1027) != 0)
            recordsExpected++;
        assertTrue("The " + regionsToAdd + " merged regions should have been spread out over " 
                + recordsExpected + " records, not " + recordsAdded, recordsAdded == recordsExpected);
        // Check we can't add one with invalid date
        try {
            sheet.addMergedRegion(10, (short)10, 9, (short)12);
            fail("Expected an exception to occur");
        } catch(IllegalArgumentException e) {
            // occurs during successful test
            assertEquals("The 'to' row (9) must not be less than the 'from' row (10)", e.getMessage());
        }
        try {
            sheet.addMergedRegion(10, (short)10, 12, (short)9);
            fail("Expected an exception to occur");
        } catch(IllegalArgumentException e) {
            // occurs during successful test
            assertEquals("The 'to' col (9) must not be less than the 'from' col (10)", e.getMessage());
        }
    }

    public void testRemoveMergedRegion() {
        Sheet sheet = Sheet.createSheet();
        int regionsToAdd = 4096;

        for (int n = 0; n < regionsToAdd; n++) {
            sheet.addMergedRegion(n, 0, n, 1);
        }

        int nSheetRecords = sheet.getRecords().size();

        //remove a third from the beginning
        for (int n = 0; n < regionsToAdd/3; n++)
        {
            sheet.removeMergedRegion(0);
            //assert they have been deleted
            assertEquals("Num of regions", regionsToAdd - n - 1, sheet.getNumMergedRegions());
        }
        
        // merge records are removed from within the MergedCellsTable, 
        // so the sheet record count should not change 
        assertEquals("Sheet Records", nSheetRecords, sheet.getRecords().size());
    }

    /**
     * Bug: 22922 (Reported by Xuemin Guan)
     * <p>
     * Remove mergedregion fails when a sheet loses records after an initial CreateSheet
     * fills up the records.
     *
     */
    public void testMovingMergedRegion() {
        List records = new ArrayList();

        CellRangeAddress[] cras = {
            new CellRangeAddress(0, 1, 0, 2),
        };
        MergeCellsRecord merged = new MergeCellsRecord(cras, 0, cras.length);
        records.add(new DimensionsRecord());
        records.add(new RowRecord(0));
        records.add(new RowRecord(1));
        records.add(new RowRecord(2));
        records.add(merged);

        Sheet sheet = Sheet.createSheet(records, 0);
        sheet.records.remove(0);

        //stub object to throw off list INDEX operations
        sheet.removeMergedRegion(0);
        assertEquals("Should be no more merged regions", 0, sheet.getNumMergedRegions());
    }

    public void testGetMergedRegionAt() {
        //TODO
    }

    public void testGetNumMergedRegions() {
        //TODO
    }

    /**
     * Makes sure all rows registered for this sheet are aggregated, they were being skipped
     *
     */
    public void testRowAggregation() {
        List records = new ArrayList();

        records.add(new DimensionsRecord());
        records.add(new RowRecord(0));
        records.add(new RowRecord(1));
        records.add(new StringRecord());
        records.add(new RowRecord(2));

        Sheet sheet = Sheet.createSheet(records, 0);
        assertNotNull("Row [2] was skipped", sheet.getRow(2));
    }

    /**
     * Make sure page break functionality works (in memory)
     *
     */
    public void testRowPageBreaks() {
        short colFrom = 0;
        short colTo = 255;

        Sheet sheet = Sheet.createSheet();
        sheet.setRowBreak(0, colFrom, colTo);

        assertTrue("no row break at 0", sheet.isRowBroken(0));
        assertEquals("1 row break available", 1, sheet.getNumRowBreaks());

        sheet.setRowBreak(0, colFrom, colTo);
        sheet.setRowBreak(0, colFrom, colTo);

        assertTrue("no row break at 0", sheet.isRowBroken(0));
        assertEquals("1 row break available", 1, sheet.getNumRowBreaks());

        sheet.setRowBreak(10, colFrom, colTo);
        sheet.setRowBreak(11, colFrom, colTo);

        assertTrue("no row break at 10", sheet.isRowBroken(10));
        assertTrue("no row break at 11", sheet.isRowBroken(11));
        assertEquals("3 row break available", 3, sheet.getNumRowBreaks());


        boolean is10 = false;
        boolean is0 = false;
        boolean is11 = false;

        int[] rowBreaks = sheet.getRowBreaks();
        for (int i = 0; i < rowBreaks.length; i++) {
            int main = rowBreaks[i];
            if (main != 0 && main != 10 && main != 11) fail("Invalid page break");
            if (main == 0)     is0 = true;
            if (main == 10) is10= true;
            if (main == 11) is11 = true;
        }

        assertTrue("one of the breaks didnt make it", is0 && is10 && is11);

        sheet.removeRowBreak(11);
        assertFalse("row should be removed", sheet.isRowBroken(11));

        sheet.removeRowBreak(0);
        assertFalse("row should be removed", sheet.isRowBroken(0));

        sheet.removeRowBreak(10);
        assertFalse("row should be removed", sheet.isRowBroken(10));

        assertEquals("no more breaks", 0, sheet.getNumRowBreaks());
    }

    /**
     * Make sure column pag breaks works properly (in-memory)
     *
     */
    public void testColPageBreaks() {
        short rowFrom = 0;
        short rowTo = (short)65535;

        Sheet sheet = Sheet.createSheet();
        sheet.setColumnBreak((short)0, rowFrom, rowTo);

        assertTrue("no col break at 0", sheet.isColumnBroken((short)0));
        assertEquals("1 col break available", 1, sheet.getNumColumnBreaks());

        sheet.setColumnBreak((short)0, rowFrom, rowTo);

        assertTrue("no col break at 0", sheet.isColumnBroken((short)0));
        assertEquals("1 col break available", 1, sheet.getNumColumnBreaks());

        sheet.setColumnBreak((short)1, rowFrom, rowTo);
        sheet.setColumnBreak((short)10, rowFrom, rowTo);
        sheet.setColumnBreak((short)15, rowFrom, rowTo);

        assertTrue("no col break at 1", sheet.isColumnBroken((short)1));
        assertTrue("no col break at 10", sheet.isColumnBroken((short)10));
        assertTrue("no col break at 15", sheet.isColumnBroken((short)15));
        assertEquals("4 col break available", 4, sheet.getNumColumnBreaks());

        boolean is10 = false;
        boolean is0 = false;
        boolean is1 = false;
        boolean is15 = false;

        int[] colBreaks = sheet.getColumnBreaks();
        for (int i = 0; i < colBreaks.length; i++) {
            int main = colBreaks[i];
            if (main != 0 && main != 1 && main != 10 && main != 15) fail("Invalid page break");
            if (main == 0)  is0 = true;
            if (main == 1)  is1 = true;
            if (main == 10) is10= true;
            if (main == 15) is15 = true;
        }

        assertTrue("one of the breaks didnt make it", is0 && is1 && is10 && is15);

        sheet.removeColumnBreak((short)15);
        assertFalse("column break should not be there", sheet.isColumnBroken((short)15));

        sheet.removeColumnBreak((short)0);
        assertFalse("column break should not be there", sheet.isColumnBroken((short)0));

        sheet.removeColumnBreak((short)1);
        assertFalse("column break should not be there", sheet.isColumnBroken((short)1));

        sheet.removeColumnBreak((short)10);
        assertFalse("column break should not be there", sheet.isColumnBroken((short)10));

        assertEquals("no more breaks", 0, sheet.getNumColumnBreaks());
    }

    /**
     * test newly added method Sheet.getXFIndexForColAt(..)
     * works as designed.
     */
    public void testXFIndexForColumn() {
        final short TEST_IDX = 10;
        final short DEFAULT_IDX = 0xF; // 15
        short xfindex = Short.MIN_VALUE;
        Sheet sheet = Sheet.createSheet();

        // without ColumnInfoRecord
        xfindex = sheet.getXFIndexForColAt((short) 0);
        assertEquals(DEFAULT_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 1);
        assertEquals(DEFAULT_IDX, xfindex);

        ColumnInfoRecord nci = ColumnInfoRecordsAggregate.createColInfo();
        sheet._columnInfos.insertColumn(nci);

        // single column ColumnInfoRecord
        nci.setFirstColumn((short) 2);
        nci.setLastColumn((short) 2);
        nci.setXFIndex(TEST_IDX);
        xfindex = sheet.getXFIndexForColAt((short) 0);
        assertEquals(DEFAULT_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 1);
        assertEquals(DEFAULT_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 2);
        assertEquals(TEST_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 3);
        assertEquals(DEFAULT_IDX, xfindex);

        // ten column ColumnInfoRecord
        nci.setFirstColumn((short) 2);
        nci.setLastColumn((short) 11);
        nci.setXFIndex(TEST_IDX);
        xfindex = sheet.getXFIndexForColAt((short) 1);
        assertEquals(DEFAULT_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 2);
        assertEquals(TEST_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 6);
        assertEquals(TEST_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 11);
        assertEquals(TEST_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 12);
        assertEquals(DEFAULT_IDX, xfindex);

        // single column ColumnInfoRecord starting at index 0
        nci.setFirstColumn((short) 0);
        nci.setLastColumn((short) 0);
        nci.setXFIndex(TEST_IDX);
        xfindex = sheet.getXFIndexForColAt((short) 0);
        assertEquals(TEST_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 1);
        assertEquals(DEFAULT_IDX, xfindex);

        // ten column ColumnInfoRecord starting at index 0
        nci.setFirstColumn((short) 0);
        nci.setLastColumn((short) 9);
        nci.setXFIndex(TEST_IDX);
        xfindex = sheet.getXFIndexForColAt((short) 0);
        assertEquals(TEST_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 7);
        assertEquals(TEST_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 9);
        assertEquals(TEST_IDX, xfindex);
        xfindex = sheet.getXFIndexForColAt((short) 10);
        assertEquals(DEFAULT_IDX, xfindex);
    }

    /**
     * Prior to bug 45066, POI would get the estimated sheet size wrong
     * when an <tt>UncalcedRecord</tt> was present.<p/>
     */
    public void testUncalcSize_bug45066() {

        List records = new ArrayList();
        records.add(new BOFRecord());
        records.add(new UncalcedRecord());
        records.add(new DimensionsRecord());
        records.add(new EOFRecord());
        Sheet sheet = Sheet.createSheet(records, 0, 0);

        int estimatedSize = sheet.getSize();
        int serializedSize = sheet.serialize(0, new byte[estimatedSize]);
        if (serializedSize != estimatedSize) {
            throw new AssertionFailedError("Identified bug 45066 b");
        }
        assertEquals(68, serializedSize);
    }

    /**
     * Prior to bug 45145 <tt>RowRecordsAggregate</tt> and <tt>ValueRecordsAggregate</tt> could
     * sometimes occur in reverse order.  This test reproduces one of those situations and makes
     * sure that RRA comes before VRA.<br/>
     *
     * The code here represents a normal POI use case where a spreadsheet is created from scratch.
     */
    public void testRowValueAggregatesOrder_bug45145() {

        Sheet sheet = Sheet.createSheet();

        RowRecord rr = new RowRecord(5);
        sheet.addRow(rr);

        CellValueRecordInterface cvr = new BlankRecord();
        cvr.setColumn((short)0);
        cvr.setRow(5);
        sheet.addValueRecord(5, cvr);


        int dbCellRecordPos = getDbCellRecordPos(sheet);
        if (dbCellRecordPos == 252) {
            // The overt symptom of the bug
            // DBCELL record pos is calculated wrong if VRA comes before RRA
            throw new AssertionFailedError("Identified  bug 45145");
        }

        // make sure that RRA and VRA are in the right place
        int rraIx = sheet.getDimsLoc()+1;
        List recs = sheet.getRecords();
        assertEquals(RowRecordsAggregate.class, recs.get(rraIx).getClass());
        assertEquals(ValueRecordsAggregate.class, recs.get(rraIx+1).getClass());

        assertEquals(242, dbCellRecordPos);
    }

    /**
     * @return the value calculated for the position of the first DBCELL record for this sheet.
     * That value is found on the IndexRecord.
     */
    private static int getDbCellRecordPos(Sheet sheet) {
        int size = sheet.getSize();
        byte[] data = new byte[size];
        sheet.serialize(0, data);

        MyIndexRecordListener myIndexListener = new MyIndexRecordListener();
        EventRecordFactory erf = new EventRecordFactory(myIndexListener, new short[] { IndexRecord.sid, });
        erf.processRecords(new ByteArrayInputStream(data));
        IndexRecord indexRecord = myIndexListener.getIndexRecord();
        int dbCellRecordPos = indexRecord.getDbcellAt(0);
        return dbCellRecordPos;
    }

    private static final class MyIndexRecordListener implements ERFListener {

        private IndexRecord _indexRecord;
        public MyIndexRecordListener() {
            // no-arg constructor
        }
        public boolean processRecord(Record rec) {
            _indexRecord = (IndexRecord)rec;
            return true;
        }
        public IndexRecord getIndexRecord() {
            return _indexRecord;
        }
    }
}

