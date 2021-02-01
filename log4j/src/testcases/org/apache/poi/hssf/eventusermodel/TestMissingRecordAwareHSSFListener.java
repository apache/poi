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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingRowDummyRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.MulBlankRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

/**
 * Tests for MissingRecordAwareHSSFListener
 */
final class TestMissingRecordAwareHSSFListener {

	private final List<org.apache.poi.hssf.record.Record> _records = new ArrayList<>();

	private void readRecords(String sampleFileName) throws IOException {
		_records.clear();
		HSSFRequest req = new HSSFRequest();
		req.addListenerForAllRecords(new MissingRecordAwareHSSFListener(_records::add));

		HSSFEventFactory factory = new HSSFEventFactory();
		try (InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);
			 POIFSFileSystem fs = new POIFSFileSystem(is)) {
			factory.processWorkbookEvents(req, fs);
		}

		assertTrue(_records.size() > 100);
	}

	@Test
	void testMissingRowRecords() throws IOException {
		readRecords("MissingBits.xls");

		// We have rows 0, 1, 2, 20 and 21
		int row0 = lastIndexOf(r -> r instanceof RowRecord && ((RowRecord)r).getRowNumber() == 0);
		assertTrue(row0 > -1);

		// Records: row 0: column 1, 2), then missing rows, rows 20,21,22 each 1 column
		String exp1 =
			"0:rr,1:rr,2:rr,3:mr,4:mr,5:mr,6:mr,7:mr,8:mr,9:mr,10:mr,11:mr,12:mr,13:mr,14:mr," +
			"15:mr,16:mr,17:mr,18:mr,19:mr,20:rr,21:rr,22:rr";
		String act1 = digest(row0, 22);
		assertEquals(exp1, act1);


		// Find the cell at 0,0
		int cell00 = lastIndexOf(r -> r instanceof LabelSSTRecord && ((LabelSSTRecord)r).getRow() == 0 && ((LabelSSTRecord)r).getColumn() == 0);

		String exp2 =
			"0:ls0lc0," +
			"1:nr0/11mc1mc2nr3/23lc3," +
			"2:nr0/45mc1mc2mc3mc4nr5/22lc5," +
			"3:lc,4:lc,5:lc,6:lc,7:lc,8:lc,9:lc,10:lc,11:lc,12:lc,13:lc,14:lc,15:lc,16:lc,17:lc,18:lc,19:lc," +
			"20:nr0/50nr1/51nr2/52nr3/53nr4/54lc4," +
			"21:ls0ls1mc2nr3/12mc4nr5/23nr6/42lc6," +
			"22:ls0mc1mc2ls3ls4mc5mc6mc7mc8mc9mc10ls11lc11";
		String act2 = digest(cell00, 57);
		assertEquals(exp2, act2);
	}

	// Make sure we don't put in any extra new lines that aren't already there
	@Test
	void testNoExtraNewLines() throws IOException {
		// Load a different file
		// This file has has something in lines 1-33
		readRecords("MRExtraLines.xls");

		int rowCount=0;
		for (org.apache.poi.hssf.record.Record rec : _records) {
			if (rec instanceof LastCellOfRowDummyRecord) {
				LastCellOfRowDummyRecord eor = (LastCellOfRowDummyRecord) rec;
				assertEquals(rowCount, eor.getRow());
				rowCount++;
			}
		}
		// Check we got the 33 rows
		assertEquals(33, rowCount);
	}

	/**
	 * Make sure that the presence of shared formulas does not cause extra end-of-row records.
	 */
	@Test
	void testEndOfRow_bug45672() throws IOException {
		readRecords("ex45672.xls");
		assertEquals(1, matches(r -> r instanceof SharedFormulaRecord));
		assertEquals(1, matches(r -> r instanceof LastCellOfRowDummyRecord));
	}

	/**
	 * MulBlank records hold multiple blank cells.
	 * Check that we don't have any MulBlankRecords, but do have lots of BlankRecords
	 */
	@Test
	void testMulBlankHandling() throws IOException {
		readRecords("45672.xls");
		assertEquals(20, matches(r -> r instanceof BlankRecord));
		assertEquals(2, matches(r -> r instanceof LastCellOfRowDummyRecord));
		assertEquals(0, matches(r -> r instanceof MulBlankRecord));
	}

	@Test
	void testStringRecordHandling() throws IOException {
		readRecords("53588.xls");
		assertEquals(1, matches(r -> r instanceof MissingCellDummyRecord));
		assertEquals(1, matches(r -> r instanceof LastCellOfRowDummyRecord));
	}

	@Test
	void testFormulasWithStringResultsHandling() throws IOException {
		readRecords("53433.xls");

		String exp =
			"dr0:mr,1:mr,2:mr,3:rr,4:rr,5:rr,6:rr,7:rr,8:rr,9:rr,10:mr,11:mr,12:mr,13:mr,14:mr,15:rr,16:rr,17:mr,18:rr," +
			"0:lc,1:lc,2:lc,3:mc0ls1ls2ls3ls4lc4,4:mc0ls1cv2urnr3/12cv4urlc4," +
			"5:mc0cv1urnr2/23nr3/23cv4urlc4,6:mc0mc1nr2/25nr3/45nr4/32815lc4," +
			"7:mc0ls1cv2cv3urcv4lc4,8:mc0mc1mc2mc3cv4lc4,9:mc0mc1mc2ls3lc3," +
			"10:lc,11:lc,12:lc,13:lc,14:lc,15:mc0ls1lc1,16:mc0mc1mc2ls3lc3," +
			"17:lc,18:mc0mc1mc2mc3ls4lc4wr";
		String act = digest(95, 89);
		assertEquals(exp, act);
	}

	private int lastIndexOf(Predicate<org.apache.poi.hssf.record.Record> pre) {
		int found = -1;
		int i = 0;
		for (org.apache.poi.hssf.record.Record r : _records) {
			if (pre.test(r)) {
				found = i;
			}
			i++;
		}
		return found;
	}

	private String digest(int start, int len) {
		StringBuilder sb = new StringBuilder(len*10);
		int lastRow = -1;
		for (org.apache.poi.hssf.record.Record r : _records.subList(start, start+len+1)) {
			String dig = null;
			int row = -1;
			if (r instanceof RowRecord) {
				RowRecord rr = (RowRecord)r;
				row = rr.getRowNumber();
				dig = "rr";
			} else if (r instanceof MissingRowDummyRecord) {
				MissingRowDummyRecord mr = (MissingRowDummyRecord)r;
				row = mr.getRowNumber();
				dig = "mr";
			} else if (r instanceof MissingCellDummyRecord) {
				MissingCellDummyRecord mc = (MissingCellDummyRecord)r;
				row = mc.getRow();
				dig = "mc" + mc.getColumn();
			} else if (r instanceof LastCellOfRowDummyRecord) {
				LastCellOfRowDummyRecord lc = (LastCellOfRowDummyRecord)r;
				row = lc.getRow();
				dig = "lc" + (lc.getLastColumnNumber() > -1 ? lc.getLastColumnNumber() : "");
			} else if (r instanceof NumberRecord) {
				NumberRecord nr = (NumberRecord)r;
				row = nr.getRow();
				dig = "nr" + nr.getColumn() + "/" + (int)nr.getValue();
			} else if (r instanceof LabelSSTRecord) {
				LabelSSTRecord ls = (LabelSSTRecord) r;
				row = ls.getRow();
				dig = "ls" + ls.getColumn();
			} else if (r instanceof WindowTwoRecord) {
				dig = "wr";
			} else if (r instanceof DimensionsRecord) {
				dig = "dr";
			} else if (r instanceof CellValueRecordInterface) {
				CellValueRecordInterface cv = (CellValueRecordInterface) r;
				row = cv.getRow();
				dig = "cv" + cv.getColumn();
			} else {
				// unhandled record
				dig = "ur";
			}
			if (lastRow != row && row > -1) {
				sb.append((lastRow > -1 ? "," : "") + row + ":");
				lastRow = row;
			}
			sb.append(dig);
		}
		return sb.toString();
	}

	private long matches(Predicate<org.apache.poi.hssf.record.Record> r) {
		return _records.stream().filter(r).count();
	}
}
