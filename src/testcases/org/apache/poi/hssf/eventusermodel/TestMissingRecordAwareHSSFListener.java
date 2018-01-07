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

package org.apache.poi.hssf.eventusermodel;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingRowDummyRecord;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.MulBlankRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
/**
 * Tests for MissingRecordAwareHSSFListener
 */
public final class TestMissingRecordAwareHSSFListener extends TestCase {
	
	private Record[] r;

	private void readRecords(String sampleFileName) {
		HSSFRequest req = new HSSFRequest();
		MockHSSFListener mockListen = new MockHSSFListener();
		MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(mockListen);
		req.addListenerForAllRecords(listener);
		
		HSSFEventFactory factory = new HSSFEventFactory();
		try {
			InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);
			POIFSFileSystem fs = new POIFSFileSystem(is);
			factory.processWorkbookEvents(req, fs);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		r = mockListen.getRecords();
		assertTrue(r.length > 100);
	} 
	public void openNormal() {
		readRecords("MissingBits.xls");
	}
	
	public void testMissingRowRecords() {
		openNormal();
		
		// We have rows 0, 1, 2, 20 and 21
		int row0 = -1;
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof RowRecord) {
				RowRecord rr = (RowRecord)r[i];
				if(rr.getRowNumber() == 0) { row0 = i; }
			}
		}
		assertTrue(row0 > -1);
		
		// Following row 0, we should have 1, 2, then dummy, then 20+21+22
		assertTrue(r[row0] instanceof RowRecord);
		assertTrue(r[row0+1] instanceof RowRecord);
		assertTrue(r[row0+2] instanceof RowRecord);
		assertTrue(r[row0+3] instanceof MissingRowDummyRecord);
		assertTrue(r[row0+4] instanceof MissingRowDummyRecord);
		assertTrue(r[row0+5] instanceof MissingRowDummyRecord);
		assertTrue(r[row0+6] instanceof MissingRowDummyRecord);
		// ...
		assertTrue(r[row0+18] instanceof MissingRowDummyRecord);
		assertTrue(r[row0+19] instanceof MissingRowDummyRecord);
		assertTrue(r[row0+20] instanceof RowRecord);
		assertTrue(r[row0+21] instanceof RowRecord);
		assertTrue(r[row0+22] instanceof RowRecord);
		
		// Check things had the right row numbers
		RowRecord rr;
		rr = (RowRecord)r[row0+2];
		assertEquals(2, rr.getRowNumber());
		rr = (RowRecord)r[row0+20];
		assertEquals(20, rr.getRowNumber());
		rr = (RowRecord)r[row0+21];
		assertEquals(21, rr.getRowNumber());
		
		MissingRowDummyRecord mr;
		mr = (MissingRowDummyRecord)r[row0+3];
		assertEquals(3, mr.getRowNumber());
		mr = (MissingRowDummyRecord)r[row0+4];
		assertEquals(4, mr.getRowNumber());
		mr = (MissingRowDummyRecord)r[row0+5];
		assertEquals(5, mr.getRowNumber());
		mr = (MissingRowDummyRecord)r[row0+18];
		assertEquals(18, mr.getRowNumber());
		mr = (MissingRowDummyRecord)r[row0+19];
		assertEquals(19, mr.getRowNumber());
	}
	
	public void testEndOfRowRecords() {
		openNormal();
		
		// Find the cell at 0,0
		int cell00 = -1;
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof LabelSSTRecord) {
				LabelSSTRecord lr = (LabelSSTRecord)r[i];
				if(lr.getRow() == 0 && lr.getColumn() == 0) { cell00 = i; }
			}
		}
		assertTrue(cell00 > -1);
		
		// We have rows 0, 1, 2, 20 and 21
		// Row 0 has 1 entry
		// Row 1 has 4 entries
		// Row 2 has 6 entries
		// Row 20 has 5 entries
		// Row 21 has 7 entries
		// Row 22 has 12 entries

		// Row 0
		assertFalse(r[cell00+0] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+1] instanceof LastCellOfRowDummyRecord);
		// Row 1
		assertFalse(r[cell00+2] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+3] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+4] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+5] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+6] instanceof LastCellOfRowDummyRecord);
		// Row 2
		assertFalse(r[cell00+7] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+8] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+9] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+10] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+11] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+12] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+13] instanceof LastCellOfRowDummyRecord);
		// Row 3 -> 19
		assertTrue(r[cell00+14] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+15] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+16] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+17] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+18] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+19] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+20] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+21] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+22] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+23] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+24] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+25] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+26] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+27] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+28] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+29] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+30] instanceof LastCellOfRowDummyRecord);
		// Row 20
		assertFalse(r[cell00+31] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+32] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+33] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+34] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+35] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+36] instanceof LastCellOfRowDummyRecord);
		// Row 21
		assertFalse(r[cell00+37] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+38] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+39] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+40] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+41] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+42] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+43] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+44] instanceof LastCellOfRowDummyRecord);
		// Row 22
		assertFalse(r[cell00+45] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+46] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+47] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+48] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+49] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+50] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+51] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+52] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+53] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+54] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+55] instanceof LastCellOfRowDummyRecord);
		assertFalse(r[cell00+56] instanceof LastCellOfRowDummyRecord);
		assertTrue(r[cell00+57] instanceof LastCellOfRowDummyRecord);
		
		// Check the numbers of the last seen columns
		LastCellOfRowDummyRecord[] lrs = new LastCellOfRowDummyRecord[24];
		int lrscount = 0;
		for (final Record rec : r) {
			if(rec instanceof LastCellOfRowDummyRecord) {
				lrs[lrscount] = (LastCellOfRowDummyRecord)rec;
				lrscount++;
			}
		}
		
		assertEquals(0, lrs[0].getLastColumnNumber());
		assertEquals(0, lrs[0].getRow());
		
		assertEquals(3, lrs[1].getLastColumnNumber());
		assertEquals(1, lrs[1].getRow());
		
		assertEquals(5, lrs[2].getLastColumnNumber());
		assertEquals(2, lrs[2].getRow());
		
		for(int i=3; i<=19; i++) {
			assertEquals(-1, lrs[i].getLastColumnNumber());
			assertEquals(i, lrs[i].getRow());
		}
		
		assertEquals(4, lrs[20].getLastColumnNumber());
		assertEquals(20, lrs[20].getRow());
		
		assertEquals(6, lrs[21].getLastColumnNumber());
		assertEquals(21, lrs[21].getRow());
		
		assertEquals(11, lrs[22].getLastColumnNumber());
		assertEquals(22, lrs[22].getRow());
	}
	
	
	public void testMissingCellRecords() {
		openNormal();
		
		// Find the cell at 0,0
		int cell00 = -1;
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof LabelSSTRecord) {
				LabelSSTRecord lr = (LabelSSTRecord)r[i];
				if(lr.getRow() == 0 && lr.getColumn() == 0) { cell00 = i; }
			}
		}
		assertTrue(cell00 > -1);
		
		// We have rows 0, 1, 2, 20 and 21
		// Row 0 has 1 entry, 0
		// Row 1 has 4 entries, 0+3
		// Row 2 has 6 entries, 0+5
		// Row 20 has 5 entries, 0-5
		// Row 21 has 7 entries, 0+1+3+5+6
		// Row 22 has 12 entries, 0+3+4+11
		
		// Row 0
		assertFalse(r[cell00+0] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+1] instanceof MissingCellDummyRecord);
		
		// Row 1
		assertFalse(r[cell00+2] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+3] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+4] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+5] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+6] instanceof MissingCellDummyRecord);
		
		// Row 2
		assertFalse(r[cell00+7] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+8] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+9] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+10] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+11] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+12] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+13] instanceof MissingCellDummyRecord);
		
		// Row 3-19
		assertFalse(r[cell00+14] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+15] instanceof MissingCellDummyRecord);
		
		// Row 20
		assertFalse(r[cell00+31] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+32] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+33] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+34] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+35] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+36] instanceof MissingCellDummyRecord);
		
		// Row 21
		assertFalse(r[cell00+37] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+38] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+39] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+40] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+41] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+42] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+43] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+44] instanceof MissingCellDummyRecord);
		
		// Row 22
		assertFalse(r[cell00+45] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+46] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+47] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+48] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+49] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+50] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+51] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+52] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+53] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+54] instanceof MissingCellDummyRecord);
		assertTrue(r[cell00+55] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+56] instanceof MissingCellDummyRecord);
		assertFalse(r[cell00+57] instanceof MissingCellDummyRecord);
		
		// Check some numbers
		MissingCellDummyRecord mc;
		
		mc = (MissingCellDummyRecord)r[cell00+3];
		assertEquals(1, mc.getRow());
		assertEquals(1, mc.getColumn());
		mc = (MissingCellDummyRecord)r[cell00+4];
		assertEquals(1, mc.getRow());
		assertEquals(2, mc.getColumn());
		
		mc = (MissingCellDummyRecord)r[cell00+8];
		assertEquals(2, mc.getRow());
		assertEquals(1, mc.getColumn());
		mc = (MissingCellDummyRecord)r[cell00+9];
		assertEquals(2, mc.getRow());
		assertEquals(2, mc.getColumn());
		
		mc = (MissingCellDummyRecord)r[cell00+55];
		assertEquals(22, mc.getRow());
		assertEquals(10, mc.getColumn());
	}
	
	// Make sure we don't put in any extra new lines
	//  that aren't already there
	public void testNoExtraNewLines() {
		// Load a different file
		// This file has has something in lines 1-33
		readRecords("MRExtraLines.xls");
		
		int rowCount=0;
		for (Record rec : r) {
			if(rec instanceof LastCellOfRowDummyRecord) {
				LastCellOfRowDummyRecord eor = (LastCellOfRowDummyRecord) rec;
				assertEquals(rowCount, eor.getRow());
				rowCount++;
			}
		}
		// Check we got the 33 rows
		assertEquals(33, rowCount);
	}

	private static final class MockHSSFListener implements HSSFListener {
		public MockHSSFListener() {}
		private final List<Record> _records = new ArrayList<>();
		private final boolean logToStdOut = false;

		@Override
        public void processRecord(Record record) {
			_records.add(record);
			
			if(record instanceof MissingRowDummyRecord) {
				MissingRowDummyRecord mr = (MissingRowDummyRecord)record;
				log("Got dummy row " + mr.getRowNumber());
			}
			if(record instanceof MissingCellDummyRecord) {
				MissingCellDummyRecord mc = (MissingCellDummyRecord)record;
				log("Got dummy cell " + mc.getRow() + " " + mc.getColumn());
			}
			if(record instanceof LastCellOfRowDummyRecord) {
				LastCellOfRowDummyRecord lc = (LastCellOfRowDummyRecord)record;
				log("Got end-of row, row was " + lc.getRow() + ", last column was " + lc.getLastColumnNumber());
			}
			
			if(record instanceof BOFRecord) {
				BOFRecord r = (BOFRecord)record;
				if(r.getType() == BOFRecord.TYPE_WORKSHEET) {
					log("On new sheet");
				}
			}
			if(record instanceof RowRecord) {
				RowRecord rr = (RowRecord)record;
				log("Starting row #" + rr.getRowNumber());
			}
		}
		private void log(String msg) {
			if(logToStdOut) {
				System.out.println(msg);
			}
		}
		public Record[] getRecords() {
			Record[] result = new Record[_records.size()];
			_records.toArray(result);
			return result;
		}
	}
	
	/**
	 * Make sure that the presence of shared formulas does not cause extra 
	 * end-of-row records.
	 */
	public void testEndOfRow_bug45672() {
		readRecords("ex45672.xls");
		Record[] rr = r;
		int eorCount=0;
		int sfrCount=0;
		for (Record record : rr) {
			if (record instanceof SharedFormulaRecord) {
				sfrCount++;
			}
			if (record instanceof LastCellOfRowDummyRecord) {
				eorCount++;
			}
		}
		if (eorCount == 2) {
			throw new AssertionFailedError("Identified bug 45672");
		}
		assertEquals(1, eorCount);
		assertEquals(1, sfrCount);
	}
	
	/**
	 * MulBlank records hold multiple blank cells. Check we
	 *  can handle them correctly.
	 */
	public void testMulBlankHandling() {
		readRecords("45672.xls");
		
		// Check that we don't have any MulBlankRecords, but do
		//  have lots of BlankRecords
		Record[] rr = r;
		int eorCount=0;
		int mbrCount=0;
		int brCount=0;
		for (Record record : rr) {
			if (record instanceof MulBlankRecord) {
				mbrCount++;
			}
			if (record instanceof BlankRecord) {
				brCount++;
			}
			if (record instanceof LastCellOfRowDummyRecord) {
				eorCount++;
			}
		}
		if (mbrCount > 0) {
			throw new AssertionFailedError("Identified bug 45672");
		}
		if (brCount < 20) {
			throw new AssertionFailedError("Identified bug 45672");
		}
		if (eorCount != 2) {
			throw new AssertionFailedError("Identified bug 45672");
		}
		assertEquals(2, eorCount);
	}

    public void testStringRecordHandling(){
        readRecords("53588.xls");
        Record[] rr = r;
        int missingCount=0;
        int lastCount=0;
        for (Record record : rr) {
            if (record instanceof MissingCellDummyRecord) {
                missingCount++;
            }
            if (record instanceof LastCellOfRowDummyRecord) {
                lastCount++;
            }
        }
        assertEquals(1, missingCount);
        assertEquals(1, lastCount);
    }
    
    public void testFormulasWithStringResultsHandling() {
        readRecords("53433.xls");
        
        int pos = 95;
        
        // First three rows are blank
        assertEquals(DimensionsRecord.class, r[pos++].getClass());
        
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(0, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(1, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(2, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        
        // Then rows 4-10 are defined
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(3, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(4, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(5, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(6, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(7, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(8, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(9, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        
        // 5 more blank rows
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(10, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(11, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(12, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(13, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(14, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        
        // 2 defined rows
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(15, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(16, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        
        // one blank row
        assertEquals(MissingRowDummyRecord.class, r[pos].getClass());
        assertEquals(17, ((MissingRowDummyRecord)r[pos]).getRowNumber());
        pos++;
        
        // one last real row
        assertEquals(RowRecord.class, r[pos].getClass());
        assertEquals(18, ((RowRecord)r[pos]).getRowNumber());
        pos++;
        
        
        
        // Now onto the cells
        
        // Because the 3 first rows are missing, should have last-of-row records first
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(0, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(1, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(2, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Onto row 4 (=3)
        
        // Now we have blank cell A4
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(3, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        
        // Now 4 real cells, all strings
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(1, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(2, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        
        // Final dummy cell for the end-of-row
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(3, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(4, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 5 has string, formula of string, number, formula of string
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(4, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(1, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(FormulaRecord.class, r[pos].getClass());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(2, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(StringRecord.class, r[pos].getClass());
        assertEquals("s1", ((StringRecord)r[pos]).getString());
        pos++;
        assertEquals(NumberRecord.class, r[pos].getClass());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(FormulaRecord.class, r[pos].getClass());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(StringRecord.class, r[pos].getClass());
        assertEquals("s3845", ((StringRecord)r[pos]).getString());
        pos++;
        
        // Final dummy cell for the end-of-row
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(4, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(4, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 6 is blank / string formula / number / number / string formula
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(5, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        
        assertEquals(FormulaRecord.class, r[pos].getClass());
        assertEquals(5, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(1, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(StringRecord.class, r[pos].getClass());
        assertEquals("s4", ((StringRecord)r[pos]).getString());
        pos++;
        assertEquals(NumberRecord.class, r[pos].getClass());
        assertEquals(5, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(2, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(NumberRecord.class, r[pos].getClass());
        assertEquals(5, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(FormulaRecord.class, r[pos].getClass());
        assertEquals(5, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(StringRecord.class, r[pos].getClass());
        assertEquals("s3845", ((StringRecord)r[pos]).getString());
        pos++;
        
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(5, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(4, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 7 is blank / blank / number / number / number
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(6, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(6, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(1, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        
        assertEquals(NumberRecord.class, r[pos].getClass());
        assertEquals(6, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(2, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(NumberRecord.class, r[pos].getClass());
        assertEquals(6, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(NumberRecord.class, r[pos].getClass());
        assertEquals(6, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(6, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(4, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 8 is blank / string / number formula / string formula / blank
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(7, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(7, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(1, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(FormulaRecord.class, r[pos].getClass());
        assertEquals(7, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(2, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(FormulaRecord.class, r[pos].getClass());
        assertEquals(7, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(StringRecord.class, r[pos].getClass());
        assertEquals("s4", ((StringRecord)r[pos]).getString());
        pos++;
        assertEquals(BlankRecord.class, r[pos].getClass());
        assertEquals(7, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(7, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(4, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 9 is empty, but with a blank at E9
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(8, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(8, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(1, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(8, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(2, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(8, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(3, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(BlankRecord.class, r[pos].getClass());
        assertEquals(8, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(8, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(4, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 10 has a string in D10
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(9, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(9, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(1, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(9, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(2, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(9, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(9, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(3, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Now 5 blank rows
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(10, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(11, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(12, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(13, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(14, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 16 has a single string in B16
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(15, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(15, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(1, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(15, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 17 has a single string in D17
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(16, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(16, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(1, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(16, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(2, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(16, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(3, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(16, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(3, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 18 is blank
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(17, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(-1, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // Row 19 has a single string in E19
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(18, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(0, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(18, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(1, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(18, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(2, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(MissingCellDummyRecord.class, r[pos].getClass());
        assertEquals(18, ((MissingCellDummyRecord)r[pos]).getRow());
        assertEquals(3, ((MissingCellDummyRecord)r[pos]).getColumn());
        pos++;
        assertEquals(LabelSSTRecord.class, r[pos].getClass());
        assertEquals(18, ((CellValueRecordInterface)r[pos]).getRow());
        assertEquals(4, ((CellValueRecordInterface)r[pos]).getColumn());
        pos++;
        assertEquals(LastCellOfRowDummyRecord.class, r[pos].getClass());
        assertEquals(18, ((LastCellOfRowDummyRecord)r[pos]).getRow());
        assertEquals(4, ((LastCellOfRowDummyRecord)r[pos]).getLastColumnNumber());
        pos++;
        
        
        // And that's it!
        assertEquals(WindowTwoRecord.class, r[pos++].getClass());
    }
}
