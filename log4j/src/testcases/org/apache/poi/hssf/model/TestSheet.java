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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.aggregates.ConditionalFormattingTable;
import org.apache.poi.hssf.record.aggregates.PageSettingsBlock;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the {@link InternalSheet} class.
 */
final class TestSheet {
	private static InternalSheet createSheet(List<org.apache.poi.hssf.record.Record> inRecs) {
		return InternalSheet.createSheet(new RecordStream(inRecs, 0));
	}

	@Test
	void testCreateSheet() {
		// Check we're adding row and cell aggregates
		List<org.apache.poi.hssf.record.Record> records = new ArrayList<>();
		records.add(BOFRecord.createSheetBOF());
		records.add( new DimensionsRecord() );
		records.add(createWindow2Record());
		records.add(EOFRecord.instance);
		InternalSheet sheet = createSheet(records);

		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
		sheet.visitContainedRecords(outRecs::add, 0);

		Iterator<org.apache.poi.hssf.record.Record> iter = outRecs.iterator();
		assertTrue(iter.next() instanceof BOFRecord );
		assertTrue(iter.next() instanceof IndexRecord);
		assertTrue(iter.next() instanceof DimensionsRecord);
		assertTrue(iter.next() instanceof WindowTwoRecord );
		assertTrue(iter.next() instanceof EOFRecord);
	}

	private static org.apache.poi.hssf.record.Record createWindow2Record() {
		WindowTwoRecord result = new WindowTwoRecord();
		result.setOptions(( short ) 0x6b6);
		result.setTopRow(( short ) 0);
		result.setLeftCol(( short ) 0);
		result.setHeaderColor(0x40);
		result.setPageBreakZoom(( short ) 0);
		result.setNormalZoom(( short ) 0);
		return result;
	}

	private static final class MergedCellListener implements RecordVisitor {

		private int _count;
		public MergedCellListener() {
			_count = 0;
		}
		@Override
        public void visitRecord(org.apache.poi.hssf.record.Record r) {
			if (r instanceof MergeCellsRecord) {
				_count++;
			}
		}
		public int getCount() {
			return _count;
		}
	}

    @Test
	void testAddMergedRegion() {
		InternalSheet sheet = InternalSheet.createSheet();
		final int regionsToAdd = 4096;

		//simple test that adds a load of regions
		for (int n = 0; n < regionsToAdd; n++)
		{
			int index = sheet.addMergedRegion(0, (short) 0, 1, (short) 1);
            assertEquals(index, n, "Merged region index expected to be " + n + " got " + index);
		}

		//test all the regions were indeed added
        assertEquals(sheet.getNumMergedRegions(), regionsToAdd);

		//test that the regions were spread out over the appropriate number of records
		MergedCellListener mcListener = new MergedCellListener();
		sheet.visitContainedRecords(mcListener, 0);
		int recordsAdded	= mcListener.getCount();
		int recordsExpected = regionsToAdd/1027;
		//noinspection ConstantConditions
		if ((regionsToAdd % 1027) != 0) {
			recordsExpected++;
		}
        assertEquals(recordsAdded, recordsExpected,
			"The " + regionsToAdd + " merged regions should have been spread out over "
			+ recordsExpected + " records, not " + recordsAdded);
		// Check we can't add one with invalid date
		IllegalArgumentException e;
		e = assertThrows(IllegalArgumentException.class, () -> sheet.addMergedRegion(10, (short)10, 9, (short)12));
		assertEquals("The 'to' row (9) must not be less than the 'from' row (10)", e.getMessage());

		e = assertThrows(IllegalArgumentException.class, () -> sheet.addMergedRegion(10, (short)10, 12, (short)9));
		assertEquals("The 'to' col (9) must not be less than the 'from' col (10)", e.getMessage());
	}

    @Test
	void testRemoveMergedRegion() {
		InternalSheet sheet = InternalSheet.createSheet();
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
			assertEquals(regionsToAdd - n - 1, sheet.getNumMergedRegions(), "Num of regions");
		}

		// merge records are removed from within the MergedCellsTable,
		// so the sheet record count should not change
		assertEquals(nSheetRecords, sheet.getRecords().size(), "Sheet Records");
	}

	/**
	 * Bug: 22922 (Reported by Xuemin Guan)
	 * <p>
	 * Remove mergedregion fails when a sheet loses records after an initial CreateSheet
	 * fills up the records.
	 *
	 */
    @Test
	void testMovingMergedRegion() {
		List<org.apache.poi.hssf.record.Record> records = new ArrayList<>();

		CellRangeAddress[] cras = {
			new CellRangeAddress(0, 1, 0, 2),
		};
		MergeCellsRecord merged = new MergeCellsRecord(cras, 0, cras.length);
		records.add(BOFRecord.createSheetBOF());
		records.add(new DimensionsRecord());
		records.add(new RowRecord(0));
		records.add(new RowRecord(1));
		records.add(new RowRecord(2));
		records.add(createWindow2Record());
		records.add(EOFRecord.instance);
		records.add(merged);

		InternalSheet sheet = createSheet(records);
		sheet.getRecords().remove(0); // TODO - what does this line do?

		//stub object to throw off list INDEX operations
		sheet.removeMergedRegion(0);
		assertEquals(0, sheet.getNumMergedRegions(), "Should be no more merged regions");
	}

    // @Test
	// void testGetMergedRegionAt() {
	// TODO
	// }

	// @Test
	// void testGetNumMergedRegions() {
	// TODO
	// }

	/**
	 * Makes sure all rows registered for this sheet are aggregated, they were being skipped
	 *
	 */
    @Test
	void testRowAggregation() {
		List<org.apache.poi.hssf.record.Record> records = new ArrayList<>();

		records.add(InternalSheet.createBOF());
		records.add(new DimensionsRecord());
		records.add(new RowRecord(0));
		records.add(new RowRecord(1));
		FormulaRecord formulaRecord = new FormulaRecord();
		formulaRecord.setCachedResultTypeString();
		records.add(formulaRecord);
		records.add(new StringRecord());
		records.add(new RowRecord(2));
		records.add(createWindow2Record());
		records.add(EOFRecord.instance);

		InternalSheet sheet = createSheet(records);
		assertNotNull(sheet.getRow(2), "Row [2] was skipped");
	}

	/**
	 * Make sure page break functionality works (in memory)
	 *
	 */
    @Test
	void testRowPageBreaks() {
		short colFrom = 0;
		short colTo = 255;

		InternalSheet worksheet = InternalSheet.createSheet();
		PageSettingsBlock sheet = worksheet.getPageSettings();
		sheet.setRowBreak(0, colFrom, colTo);

		assertTrue(sheet.isRowBroken(0), "no row break at 0");
		assertEquals(1, sheet.getNumRowBreaks(), "1 row break available");

		sheet.setRowBreak(0, colFrom, colTo);
		sheet.setRowBreak(0, colFrom, colTo);

		assertTrue(sheet.isRowBroken(0), "no row break at 0");
		assertEquals(1, sheet.getNumRowBreaks(), "1 row break available");

		sheet.setRowBreak(10, colFrom, colTo);
		sheet.setRowBreak(11, colFrom, colTo);

		assertTrue(sheet.isRowBroken(10), "no row break at 10");
		assertTrue(sheet.isRowBroken(11), "no row break at 11");
		assertEquals(3, sheet.getNumRowBreaks(), "3 row break available");


		boolean is10 = false;
		boolean is0 = false;
		boolean is11 = false;

		int[] rowBreaks = sheet.getRowBreaks();
		for (int main : rowBreaks) {
			assertTrue(main == 0 || main == 10 || main == 11, "Invalid page break");
			if (main == 0)	 is0 = true;
			if (main == 10) is10 = true;
			if (main == 11) is11 = true;
		}

		assertTrue(is0 && is10 && is11, "one of the breaks didnt make it");

		sheet.removeRowBreak(11);
		assertFalse(sheet.isRowBroken(11), "row should be removed");

		sheet.removeRowBreak(0);
		assertFalse(sheet.isRowBroken(0), "row should be removed");

		sheet.removeRowBreak(10);
		assertFalse(sheet.isRowBroken(10), "row should be removed");

		assertEquals(0, sheet.getNumRowBreaks(), "no more breaks");
	}

	/**
	 * Make sure column pag breaks works properly (in-memory)
	 *
	 */
    @Test
	void testColPageBreaks() {
		short rowFrom = 0;
		short rowTo = (short)65535;

		InternalSheet worksheet = InternalSheet.createSheet();
		PageSettingsBlock sheet = worksheet.getPageSettings();
		sheet.setColumnBreak((short)0, rowFrom, rowTo);

		assertTrue(sheet.isColumnBroken(0), "no col break at 0");
		assertEquals(1, sheet.getNumColumnBreaks(), "1 col break available");

		sheet.setColumnBreak((short)0, rowFrom, rowTo);

		assertTrue(sheet.isColumnBroken(0), "no col break at 0");
		assertEquals(1, sheet.getNumColumnBreaks(), "1 col break available");

		sheet.setColumnBreak((short)1, rowFrom, rowTo);
		sheet.setColumnBreak((short)10, rowFrom, rowTo);
		sheet.setColumnBreak((short)15, rowFrom, rowTo);

		assertTrue(sheet.isColumnBroken(1), "no col break at 1");
		assertTrue(sheet.isColumnBroken(10), "no col break at 10");
		assertTrue(sheet.isColumnBroken(15), "no col break at 15");
		assertEquals(4, sheet.getNumColumnBreaks(), "4 col break available");

		boolean is10 = false;
		boolean is0 = false;
		boolean is1 = false;
		boolean is15 = false;

		int[] colBreaks = sheet.getColumnBreaks();
		for (int main : colBreaks) {
			assertTrue(main == 0 || main == 1 || main == 10 || main == 15, "Invalid page break");
			if (main == 0)  is0 = true;
			if (main == 1)  is1 = true;
			if (main == 10) is10= true;
			if (main == 15) is15 = true;
		}

		assertTrue(is0 && is1 && is10 && is15, "one of the breaks didnt make it");

		sheet.removeColumnBreak(15);
		assertFalse(sheet.isColumnBroken(15), "column break should not be there");

		sheet.removeColumnBreak(0);
		assertFalse(sheet.isColumnBroken(0), "column break should not be there");

		sheet.removeColumnBreak(1);
		assertFalse(sheet.isColumnBroken(1), "column break should not be there");

		sheet.removeColumnBreak(10);
		assertFalse(sheet.isColumnBroken(10), "column break should not be there");

		assertEquals(0, sheet.getNumColumnBreaks(), "no more breaks");
	}

	/**
	 * test newly added method Sheet.getXFIndexForColAt(..)
	 * works as designed.
	 */
    @Test
	void testXFIndexForColumn() {
		final short TEST_IDX = 10;
		final short DEFAULT_IDX = 0xF; // 15
		InternalSheet sheet = InternalSheet.createSheet();

		// without ColumnInfoRecord
		int xfindex = sheet.getXFIndexForColAt((short) 0);
		assertEquals(DEFAULT_IDX, xfindex);
		xfindex = sheet.getXFIndexForColAt((short) 1);
		assertEquals(DEFAULT_IDX, xfindex);

		ColumnInfoRecord nci = new ColumnInfoRecord();
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
	 * when an <tt>UncalcedRecord</tt> was present.<p>
	 */
    @Test
	void testUncalcSize_bug45066() {

		List<org.apache.poi.hssf.record.Record> records = new ArrayList<>();
		records.add(BOFRecord.createSheetBOF());
		records.add(new UncalcedRecord());
		records.add(new DimensionsRecord());
		records.add(createWindow2Record());
		records.add(EOFRecord.instance);
		InternalSheet sheet = createSheet(records);

		// The original bug was due to different logic for collecting records for sizing and
		// serialization. The code has since been refactored into a single method for visiting
		// all contained records.  Now this test is much less interesting
		int[] totalSize = { 0 };
		byte[] buf = new byte[100];

		sheet.visitContainedRecords(r -> {
			int estimatedSize = r.getRecordSize();
			int serializedSize = r.serialize(0, buf);
			assertEquals(estimatedSize, serializedSize, "serialized size mismatch for record (" + r.getClass().getName() + ")");
			totalSize[0] += estimatedSize;
		}, 0);
		assertEquals(90, totalSize[0]);
	}

	/**
	 * Prior to bug 45145 <tt>RowRecordsAggregate</tt> and <tt>ValueRecordsAggregate</tt> could
	 * sometimes occur in reverse order.  This test reproduces one of those situations and makes
	 * sure that RRA comes before VRA.<br>
	 *
	 * The code here represents a normal POI use case where a spreadsheet is created from scratch.
	 */
    @Test
	void testRowValueAggregatesOrder_bug45145() {

		InternalSheet sheet = InternalSheet.createSheet();

		RowRecord rr = new RowRecord(5);
		sheet.addRow(rr);

		CellValueRecordInterface cvr = new BlankRecord();
		cvr.setColumn((short)0);
		cvr.setRow(5);
		sheet.addValueRecord(5, cvr);


		int dbCellRecordPos = getDbCellRecordPos(sheet);
		// The overt symptom of the bug
		// DBCELL record pos is calculated wrong if VRA comes before RRA
		assertNotEquals (252, dbCellRecordPos);

//		if (false) {
//			// make sure that RRA and VRA are in the right place
//			// (Aug 2008) since the VRA is now part of the RRA, there is much less chance that
//			// they could get out of order. Still, one could write serialize the sheet here,
//			// and read back with EventRecordFactory to make sure...
//		}
		assertEquals(242, dbCellRecordPos);
	}

	/**
	 * @return the value calculated for the position of the first DBCELL record for this sheet.
	 * That value is found on the IndexRecord.
	 */
	private static int getDbCellRecordPos(InternalSheet sheet) {

		MyIndexRecordListener myIndexListener = new MyIndexRecordListener();
		sheet.visitContainedRecords(myIndexListener, 0);
		IndexRecord indexRecord = myIndexListener.getIndexRecord();
        return indexRecord.getDbcellAt(0);
	}

	private static final class MyIndexRecordListener implements RecordVisitor {

		private IndexRecord _indexRecord;
		public MyIndexRecordListener() {
			// no-arg constructor
		}
		public IndexRecord getIndexRecord() {
			return _indexRecord;
		}
		@Override
        public void visitRecord(org.apache.poi.hssf.record.Record r) {
			if (r instanceof IndexRecord) {
				if (_indexRecord != null) {
					throw new RuntimeException("too many index records");
				}
				_indexRecord = (IndexRecord)r;
			}
		}
	}

	/**
	 * Checks for bug introduced around r682282-r683880 that caused a second GUTS records
	 * which in turn got the dimensions record out of alignment
	 */
    @Test
	void testGutsRecord_bug45640() {

		InternalSheet sheet = InternalSheet.createSheet();
		sheet.addRow(new RowRecord(0));
		sheet.addRow(new RowRecord(1));
		sheet.groupRowRange( 0, 1, true );
		assertNotNull(sheet.toString());
		List<RecordBase> recs = sheet.getRecords();
		long count = recs.stream().filter(r -> r instanceof GutsRecord).count();
		assertNotEquals(2, count);
		assertEquals(1, count);
	}

    @Test
	void testMisplacedMergedCellsRecords_bug45699() throws Exception {
		try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex45698-22488.xls")) {
			HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row = sheet.getRow(3);
			HSSFCell cell = row.getCell(4);
			assertNotNull(cell, "Identified bug 45699");
			assertEquals("Informations", cell.getRichStringCellValue().getString());
		}
	}
	/**
	 * In 3.1, setting margins between creating first row and first cell caused an exception.
	 */
    @Test
	void testSetMargins_bug45717() throws Exception {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Vorschauliste");
		HSSFRow row = sheet.createRow(0);

		sheet.setMargin(HSSFSheet.LeftMargin, 0.3);
		try {
			row.createCell(0);
		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Cannot create value records before row records exist")) {
				fail("Identified bug 45717");
			}
			throw e;
		} finally {
		    workbook.close();
		}
	}

	/**
	 * Some apps seem to write files with missing DIMENSION records.
	 * Excel(2007) tolerates this, so POI should too.
	 */
    @Test
	void testMissingDims() {

		int rowIx = 5;
		int colIx = 6;
		NumberRecord nr = new NumberRecord();
		nr.setRow(rowIx);
		nr.setColumn((short) colIx);
		nr.setValue(3.0);

		List<org.apache.poi.hssf.record.Record> inRecs = new ArrayList<>();
		inRecs.add(BOFRecord.createSheetBOF());
		inRecs.add(new RowRecord(rowIx));
		inRecs.add(nr);
		inRecs.add(createWindow2Record());
		inRecs.add(EOFRecord.instance);
		InternalSheet sheet = createSheet(inRecs);

		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
		sheet.visitContainedRecords(outRecs::add, rowIx);
		assertEquals(8, outRecs.size());
		DimensionsRecord dims = (DimensionsRecord) outRecs.get(5);
		assertEquals(rowIx, dims.getFirstRow());
		assertEquals(rowIx, dims.getLastRow());
		assertEquals(colIx, dims.getFirstCol());
		assertEquals(colIx, dims.getLastCol());
	}

	/**
	 * Prior to the fix for bug 46547, shifting formulas would have the side-effect
	 * of creating a {@link ConditionalFormattingTable}.  There was no impairment to
	 * functionality since empty record aggregates are equivalent to missing record
	 * aggregates. However, since this unnecessary creation helped expose bug 46547b,
	 * and since there is a slight performance hit the fix was made to avoid it.
	 */
    @Test
	void testShiftFormulasAddCondFormat_bug46547() {
		// Create a sheet with data validity (similar to bugzilla attachment id=23131).
		InternalSheet sheet = InternalSheet.createSheet();

		List<RecordBase> sheetRecs = sheet.getRecords();
		assertEquals(23, sheetRecs.size());

		FormulaShifter shifter = FormulaShifter.createForRowShift(0, "", 0, 0, 1, SpreadsheetVersion.EXCEL97);
		sheet.updateFormulasAfterCellShift(shifter, 0);
		assertFalse(sheetRecs.size() == 24 && sheetRecs.get(22) instanceof ConditionalFormattingTable);
		assertEquals(23, sheetRecs.size());
	}
	/**
	 * Bug 46547 happened when attempting to add conditional formatting to a sheet
	 * which already had data validity constraints.
	 */
    @Test
	void testAddCondFormatAfterDataValidation_bug46547() {
		// Create a sheet with data validity (similar to bugzilla attachment id=23131).
		InternalSheet sheet = InternalSheet.createSheet();
		sheet.getOrCreateDataValidityTable();

		// attempt to add conditional formatting
		ConditionalFormattingTable cft = sheet.getConditionalFormattingTable();
		assertNotNull(cft);
	}

    @Test
	void testCloneMulBlank_bug46776() {
		org.apache.poi.hssf.record.Record[] recs = {
				InternalSheet.createBOF(),
				new DimensionsRecord(),
				new RowRecord(1),
				new MulBlankRecord(1, 3, new short[] { 0x0F, 0x0F, 0x0F, } ),
				new RowRecord(2),
				createWindow2Record(),
				EOFRecord.instance,
		};

		InternalSheet sheet = createSheet(Arrays.asList(recs));

		InternalSheet sheet2 = sheet.cloneSheet();

		List<org.apache.poi.hssf.record.Record> clonedRecs = new ArrayList<>();
		sheet2.visitContainedRecords(clonedRecs::add, 0);
		// +2 for INDEX and DBCELL
		assertEquals(recs.length+2, clonedRecs.size());
	}

    @Test
    void testCreateAggregate() {
        String msoDrawingRecord1 =
                "0F 00 02 F0 20 01 00 00 10 00 08 F0 08 00 00 00 \n" +
                "03 00 00 00 02 04 00 00 0F 00 03 F0 08 01 00 00 \n" +
                "0F 00 04 F0 28 00 00 00 01 00 09 F0 10 00 00 00 \n" +
                "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 \n" +
                "02 00 0A F0 08 00 00 00 00 04 00 00 05 00 00 00 \n" +
                "0F 00 04 F0 64 00 00 00 42 01 0A F0 08 00 00 00 \n" +
                "01 04 00 00 00 0A 00 00 73 00 0B F0 2A 00 00 00 \n" +
                "BF 00 08 00 08 00 44 01 04 00 00 00 7F 01 00 00 \n" +
                "01 00 BF 01 00 00 11 00 C0 01 40 00 00 08 FF 01 \n" +
                "10 00 10 00 BF 03 00 00 08 00 00 00 10 F0 12 00 \n" +
                "00 00 00 00 01 00 54 00 05 00 45 00 01 00 88 03 \n" +
                "05 00 94 00 00 00 11 F0 00 00 00 00";

        String msoDrawingRecord2 =
                "0F 00 04 F0 64 00 00 00 42 01 0A F0 08 00 00 00 " +
                "02 04 00 00 80 0A 00 00 73 00 0B F0 2A 00 00 00 " +
                "BF 00 08 00 08 00 44 01 04 00 00 00 7F 01 00 00 " +
                "01 00 BF 01 00 00 11 00 C0 01 40 00 00 08 FF 01 " +
                "10 00 10 00 BF 03 00 00 08 00 00 00 10 F0 12 00 " +
                "00 00 00 00 01 00 8D 03 05 00 E4 00 03 00 4D 03 " +
                "0B 00 0C 00 00 00 11 F0 00 00 00 00";

        DrawingRecord d1 = new DrawingRecord();
        d1.setData( HexRead.readFromString( msoDrawingRecord1 ) );

        ObjRecord r1 = new ObjRecord();

        DrawingRecord d2 = new DrawingRecord();
        d2.setData( HexRead.readFromString( msoDrawingRecord2 ) );

        TextObjectRecord r2 = new TextObjectRecord();
        r2.setStr(new HSSFRichTextString("Aggregated"));
        NoteRecord n2 = new NoteRecord();

        List<org.apache.poi.hssf.record.Record> recordStream = new ArrayList<>();
        recordStream.add(InternalSheet.createBOF());
        recordStream.add( d1 );
        recordStream.add( r1 );
        recordStream.add(createWindow2Record());
        recordStream.add(EOFRecord.instance);

        confirmAggregatedRecords(recordStream);


        recordStream = new ArrayList<>();
        recordStream.add(InternalSheet.createBOF());
        recordStream.add( d1 );
        recordStream.add( r1 );
        recordStream.add( d2 );
        recordStream.add( r2 );
        recordStream.add(createWindow2Record());
        recordStream.add(EOFRecord.instance);

        confirmAggregatedRecords(recordStream);

        recordStream = new ArrayList<>();
        recordStream.add(InternalSheet.createBOF());
        recordStream.add( d1 );
        recordStream.add( r1 );
        recordStream.add( d2 );
        recordStream.add( r2 );
        recordStream.add( n2 );
        recordStream.add(createWindow2Record());
        recordStream.add(EOFRecord.instance);

        confirmAggregatedRecords(recordStream);
     }

    private void confirmAggregatedRecords(List<org.apache.poi.hssf.record.Record> recordStream){
        InternalSheet sheet = InternalSheet.createSheet();
        sheet.getRecords().clear();
        sheet.getRecords().addAll(recordStream);

        List<RecordBase> sheetRecords = sheet.getRecords();

        DrawingManager2 drawingManager = new DrawingManager2(new EscherDggRecord() );
        sheet.aggregateDrawingRecords(drawingManager, false);

        assertEquals(4, sheetRecords.size());
        assertEquals(BOFRecord.sid, ((org.apache.poi.hssf.record.Record)sheetRecords.get(0)).getSid());
        assertEquals(EscherAggregate.sid, ((org.apache.poi.hssf.record.Record)sheetRecords.get(1)).getSid());
        assertEquals(WindowTwoRecord.sid, ((org.apache.poi.hssf.record.Record)sheetRecords.get(2)).getSid());
        assertEquals(EOFRecord.sid, ((org.apache.poi.hssf.record.Record)sheetRecords.get(3)).getSid());
    }

    @Test
    void testSheetDimensions() {
        InternalSheet sheet = InternalSheet.createSheet();
        DimensionsRecord dimensions = (DimensionsRecord)sheet.findFirstRecordBySid(DimensionsRecord.sid);
        assertNotNull(dimensions);
        assertEquals(0, dimensions.getFirstCol());
        assertEquals(0, dimensions.getFirstRow());
        assertEquals(1, dimensions.getLastCol());  // plus pne
        assertEquals(1, dimensions.getLastRow());  // plus pne

        RowRecord rr = new RowRecord(0);
        sheet.addRow(rr);

        assertEquals(0, dimensions.getFirstCol());
        assertEquals(0, dimensions.getFirstRow());
        assertEquals(1, dimensions.getLastCol());
        assertEquals(1, dimensions.getLastRow());

        CellValueRecordInterface cvr;

        cvr = new BlankRecord();
        cvr.setColumn((short)0);
        cvr.setRow(0);
        sheet.addValueRecord(0, cvr);

        assertEquals(0, dimensions.getFirstCol());
        assertEquals(0, dimensions.getFirstRow());
        assertEquals(1, dimensions.getLastCol());
        assertEquals(1, dimensions.getLastRow());

        cvr = new BlankRecord();
        cvr.setColumn((short)1);
        cvr.setRow(0);
        sheet.addValueRecord(0, cvr);

        assertEquals(0, dimensions.getFirstCol());
        assertEquals(0, dimensions.getFirstRow());
        assertEquals(2, dimensions.getLastCol());   //YK:  failed until Bugzilla 53414 was fixed
        assertEquals(1, dimensions.getLastRow());
    }
}
