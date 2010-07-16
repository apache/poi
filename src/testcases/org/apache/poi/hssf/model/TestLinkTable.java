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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.NameCommentRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.SupBookRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * Tests for {@link LinkTable}
 *
 * @author Josh Micich
 */
public final class TestLinkTable extends TestCase {

	/**
	 * The example file attached to bugzilla 45046 is a clear example of Name records being present
	 * without an External Book (SupBook) record.  Excel has no trouble reading this file.<br/>
	 * TODO get OOO documentation updated to reflect this (that EXTERNALBOOK is optional).
	 *
	 * It's not clear what exact steps need to be taken in Excel to create such a workbook
	 */
	public void testLinkTableWithoutExternalBookRecord_bug45046() {
		HSSFWorkbook wb;

		try {
			wb = HSSFTestDataSamples.openSampleWorkbook("ex45046-21984.xls");
		} catch (RuntimeException e) {
			if ("DEFINEDNAME is part of LinkTable".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 45046 b");
			}
			throw e;
		}
		// some other sanity checks
		assertEquals(3, wb.getNumberOfSheets());
		String formula = wb.getSheetAt(0).getRow(4).getCell(13).getCellFormula();

		if ("ipcSummenproduktIntern($P5,N$6,$A$9,N$5)".equals(formula)) {
			// The reported symptom of this bugzilla is an earlier bug (already fixed)
			throw new AssertionFailedError("Identified bug 41726");
			// This is observable in version 3.0
		}

		assertEquals("ipcSummenproduktIntern($C5,N$2,$A$9,N$1)", formula);
	}

	public void testMultipleExternSheetRecords_bug45698() {
		HSSFWorkbook wb;

		try {
			wb = HSSFTestDataSamples.openSampleWorkbook("ex45698-22488.xls");
		} catch (RuntimeException e) {
			if ("Extern sheet is part of LinkTable".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 45698");
			}
			throw e;
		}
		// some other sanity checks
		assertEquals(7, wb.getNumberOfSheets());
	}

	public void testExtraSheetRefs_bug45978() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex45978-extraLinkTableSheets.xls");
		/*
		ex45978-extraLinkTableSheets.xls is a cut-down version of attachment 22561.
		The original file produces the same error.

		This bug was caused by a combination of invalid sheet indexes in the EXTERNSHEET
		record, and eager initialisation of the extern sheet references. Note - the workbook
		has 2 sheets, but the EXTERNSHEET record refers to sheet indexes 0, 1 and 2.

		Offset 0x3954 (14676)
		recordid = 0x17, size = 32
		[EXTERNSHEET]
		   numOfRefs	 = 5
		refrec		 #0: extBook=0 firstSheet=0 lastSheet=0
		refrec		 #1: extBook=1 firstSheet=2 lastSheet=2
		refrec		 #2: extBook=2 firstSheet=1 lastSheet=1
		refrec		 #3: extBook=0 firstSheet=-1 lastSheet=-1
		refrec		 #4: extBook=0 firstSheet=1 lastSheet=1
		[/EXTERNSHEET]

		As it turns out, the formula in question doesn't even use externSheetIndex #1 - it
		uses #4, which resolves to sheetIndex 1 -> 'Data'.

		It is not clear exactly what externSheetIndex #4 would refer to.  Excel seems to
		display such a formula as "''!$A2", but then complains of broken link errors.
		*/

		HSSFCell cell = wb.getSheetAt(0).getRow(1).getCell(1);
		String cellFormula;
		try {
			cellFormula = cell.getCellFormula();
		} catch (IndexOutOfBoundsException e) {
			if (e.getMessage().equals("Index: 2, Size: 2")) {
				throw new AssertionFailedError("Identified bug 45798");
			}
			throw e;
		}
		assertEquals("Data!$A2", cellFormula);
	}

	/**
	 * This problem was visible in POI svn r763332
	 * when reading the workbook of attachment 23468 from bugzilla 47001
	 */
	public void testMissingExternSheetRecord_bug47001b() {
		
		Record[] recs = {
				SupBookRecord.createAddInFunctions(),
				new SSTRecord(),
		};
		List<Record> recList = Arrays.asList(recs);
		WorkbookRecordList wrl = new WorkbookRecordList();
		
		LinkTable lt;
		try {
			lt = new LinkTable(recList, 0, wrl, Collections.<String, NameCommentRecord>emptyMap());
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Expected an EXTERNSHEET record but got (org.apache.poi.hssf.record.SSTRecord)")) {
				throw new AssertionFailedError("Identified bug 47001b");
			}
		
			throw e;
		}
		assertNotNull(lt);
	}	

	/**
	 *
	 */
	public void testNameCommentRecordBetweenNameRecords() {

		final Record[] recs = {
        new NameRecord(),
        new NameCommentRecord("name1", "comment1"),
        new NameRecord(),
        new NameCommentRecord("name2", "comment2"),

		};
		final List<Record> recList = Arrays.asList(recs);
		final WorkbookRecordList wrl = new WorkbookRecordList();
		final Map<String, NameCommentRecord> commentRecords = new LinkedHashMap<String, NameCommentRecord>();

		final LinkTable	lt = new LinkTable(recList, 0, wrl, commentRecords);
		assertNotNull(lt);

		assertEquals(2, commentRecords.size());
    assertTrue(recs[1] == commentRecords.get("name1")); //== is intentionally not .equals()!
    assertTrue(recs[3] == commentRecords.get("name2")); //== is intentionally not .equals()!

    assertEquals(2, lt.getNumNames());
	}
}
