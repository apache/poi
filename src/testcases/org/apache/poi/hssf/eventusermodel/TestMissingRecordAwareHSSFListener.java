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
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
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
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof LastCellOfRowDummyRecord) {
				lrs[lrscount] = (LastCellOfRowDummyRecord)r[i];
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
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof LastCellOfRowDummyRecord) {
				LastCellOfRowDummyRecord eor = (LastCellOfRowDummyRecord) r[i];
				assertEquals(rowCount, eor.getRow());
				rowCount++;
			}
		}
		// Check we got the 33 rows
		assertEquals(33, rowCount);
	}

	private static final class MockHSSFListener implements HSSFListener {
		public MockHSSFListener() {}
		private final List _records = new ArrayList();
		private boolean logToStdOut = false;

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
		for (int i = 0; i < rr.length; i++) {
			Record record = rr[i];
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
}
