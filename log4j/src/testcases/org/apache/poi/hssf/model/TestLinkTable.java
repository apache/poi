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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.ExternalNameRecord;
import org.apache.poi.hssf.record.NameCommentRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.SupBookRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LinkTable}
 */
final class TestLinkTable {

	/**
	 * The example file attached to bugzilla 45046 is a clear example of Name records being present
	 * without an External Book (SupBook) record.  Excel has no trouble reading this file.<br>
	 * TODO get OOO documentation updated to reflect this (that EXTERNALBOOK is optional).
	 *
	 * It's not clear what exact steps need to be taken in Excel to create such a workbook
	 */
	@Test
	void testLinkTableWithoutExternalBookRecord_bug45046() {
		// Bug 45046 b: DEFINEDNAME is part of LinkTable
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex45046-21984.xls");
		// some other sanity checks
		assertEquals(3, wb.getNumberOfSheets());
		String formula = wb.getSheetAt(0).getRow(4).getCell(13).getCellFormula();

		// The reported symptom of this bugzilla is an earlier bug (already fixed)
		// This is observable in version 3.0
		assertNotEquals("ipcSummenproduktIntern($P5,N$6,$A$9,N$5)", formula);

		assertEquals("ipcSummenproduktIntern($C5,N$2,$A$9,N$1)", formula);
	}

	@Test
	void testMultipleExternSheetRecords_bug45698() {
		// Bug: Extern sheet is part of LinkTable
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex45698-22488.xls");
		// some other sanity checks
		assertEquals(7, wb.getNumberOfSheets());
	}

	@Test
	void testExtraSheetRefs_bug45978() {
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
		// Bug: IndexOutOfBoundsException - Index: 2, Size: 2
		String cellFormula = cell.getCellFormula();
		assertEquals("Data!$A2", cellFormula);
	}

	/**
	 * This problem was visible in POI svn r763332
	 * when reading the workbook of attachment 23468 from bugzilla 47001
	 */
	@Test
	void testMissingExternSheetRecord_bug47001b() {

		Record[] recs = {
				SupBookRecord.createAddInFunctions(),
				new SSTRecord(),
		};
		List<org.apache.poi.hssf.record.Record> recList = Arrays.asList(recs);
		WorkbookRecordList wrl = new WorkbookRecordList();

		// Bug 47001b: Expected an EXTERNSHEET record but got (org.apache.poi.hssf.record.SSTRecord)
		LinkTable lt = new LinkTable(recList, 0, wrl, Collections.emptyMap());
		assertNotNull(lt);
	}

	@Test
	void testNameCommentRecordBetweenNameRecords() {

		final Record[] recs = {
        new NameRecord(),
        new NameCommentRecord("name1", "comment1"),
        new NameRecord(),
        new NameCommentRecord("name2", "comment2"),

		};
		final List<org.apache.poi.hssf.record.Record> recList = Arrays.asList(recs);
		final WorkbookRecordList wrl = new WorkbookRecordList();
		final Map<String, NameCommentRecord> commentRecords = new LinkedHashMap<>();

		final LinkTable	lt = new LinkTable(recList, 0, wrl, commentRecords);
		assertNotNull(lt);

		assertEquals(2, commentRecords.size());
        assertSame(recs[1], commentRecords.get("name1")); //== is intentionally not .equals()!
        assertSame(recs[3], commentRecords.get("name2")); //== is intentionally not .equals()!

    	assertEquals(2, lt.getNumNames());
	}

	@Test
    void testAddNameX(){
        WorkbookRecordList wrl = new WorkbookRecordList();
        wrl.add(0, new BOFRecord());
        wrl.add(1, new CountryRecord());
        wrl.add(2, EOFRecord.instance);

        int numberOfSheets = 3;
        LinkTable tbl = new LinkTable(numberOfSheets, wrl);
        // creation of a new LinkTable insert two new records: SupBookRecord followed by ExternSheetRecord
        // assure they are in place:
        //    [BOFRecord]
        //    [CountryRecord]
        //    [SUPBOOK Internal References  nSheets= 3]
        //    [EXTERNSHEET]
        //    [EOFRecord]

        assertEquals(5, wrl.getRecords().size());
        assertTrue(wrl.get(2) instanceof SupBookRecord);
        SupBookRecord sup1 = (SupBookRecord)wrl.get(2);
        assertEquals(numberOfSheets, sup1.getNumberOfSheets());
        assertTrue(wrl.get(3) instanceof ExternSheetRecord);
        ExternSheetRecord extSheet = (ExternSheetRecord)wrl.get(3);
        assertEquals(0, extSheet.getNumOfRefs());

        assertNull(tbl.getNameXPtg("ISODD", -1));
        assertEquals(5, wrl.getRecords().size()); //still have five records

        NameXPtg namex1 = tbl.addNameXPtg("ISODD");  // adds two new rercords
        assertEquals(0, namex1.getSheetRefIndex());
        assertEquals(0, namex1.getNameIndex());
		NameXPtg act = tbl.getNameXPtg("ISODD", -1);
		assertNotNull(act);
        assertEquals(namex1.toString(), act.toString());

        // Can only find on the right sheet ref, if restricting
		act = tbl.getNameXPtg("ISODD", 0);
		assertNotNull(act);
        assertEquals(namex1.toString(), act.toString());
        assertNull(tbl.getNameXPtg("ISODD", 1));
        assertNull(tbl.getNameXPtg("ISODD", 2));

        // assure they are in place:
        //    [BOFRecord]
        //    [CountryRecord]
        //    [SUPBOOK Internal References  nSheets= 3]
        //    [SUPBOOK Add-In Functions nSheets= 1]
        //    [EXTERNALNAME .name    = ISODD]
        //    [EXTERNSHEET]
        //    [EOFRecord]

        assertEquals(7, wrl.getRecords().size());
        assertTrue(wrl.get(3) instanceof SupBookRecord);
        SupBookRecord sup2 = (SupBookRecord)wrl.get(3);
        assertTrue(sup2.isAddInFunctions());
        assertTrue(wrl.get(4) instanceof ExternalNameRecord);
        ExternalNameRecord ext1 = (ExternalNameRecord)wrl.get(4);
        assertEquals("ISODD", ext1.getText());
        assertTrue(wrl.get(5) instanceof ExternSheetRecord);
        assertEquals(1, extSheet.getNumOfRefs());

        //check that
        assertEquals(0, tbl.resolveNameXIx(namex1.getSheetRefIndex(), namex1.getNameIndex()));
        assertEquals("ISODD", tbl.resolveNameXText(namex1.getSheetRefIndex(), namex1.getNameIndex(), null));

        assertNull(tbl.getNameXPtg("ISEVEN", -1));
        NameXPtg namex2 = tbl.addNameXPtg("ISEVEN");  // adds two new rercords
        assertEquals(0, namex2.getSheetRefIndex());
        assertEquals(1, namex2.getNameIndex());  // name index increased by one
		act = tbl.getNameXPtg("ISEVEN", -1);
		assertNotNull(act);
        assertEquals(namex2.toString(), act.toString());
        assertEquals(8, wrl.getRecords().size());
        // assure they are in place:
        //    [BOFRecord]
        //    [CountryRecord]
        //    [SUPBOOK Internal References  nSheets= 3]
        //    [SUPBOOK Add-In Functions nSheets= 1]
        //    [EXTERNALNAME .name    = ISODD]
        //    [EXTERNALNAME .name    = ISEVEN]
        //    [EXTERNSHEET]
        //    [EOFRecord]
        assertTrue(wrl.get(3) instanceof SupBookRecord);
        assertTrue(wrl.get(4) instanceof ExternalNameRecord);
        assertTrue(wrl.get(5) instanceof ExternalNameRecord);
        assertEquals("ISODD", ((ExternalNameRecord)wrl.get(4)).getText());
        assertEquals("ISEVEN", ((ExternalNameRecord)wrl.get(5)).getText());
        assertTrue(wrl.get(6) instanceof ExternSheetRecord);
        assertTrue(wrl.get(7) instanceof EOFRecord);

        assertEquals(0, tbl.resolveNameXIx(namex2.getSheetRefIndex(), namex2.getNameIndex()));
        assertEquals("ISEVEN", tbl.resolveNameXText(namex2.getSheetRefIndex(), namex2.getNameIndex(), null));

    }
}
