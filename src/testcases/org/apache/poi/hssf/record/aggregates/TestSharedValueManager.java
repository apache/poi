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

package org.apache.poi.hssf.record.aggregates;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.RecordInspector;

/**
 * Tests for {@link SharedValueManager}
 *
 * @author Josh Micich
 */
public final class TestSharedValueManager extends TestCase {

	/**
	 * This Excel workbook contains two sheets that each have a pair of overlapping shared formula
	 * ranges.  The first sheet has one row and one column shared formula ranges which intersect.
	 * The second sheet has two column shared formula ranges - one contained within the other.
	 * These shared formula ranges were created by fill-dragging a single cell formula across the
	 * desired region.  The larger shared formula ranges were placed first.<br/>
	 *
	 * There are probably many ways to produce similar effects, but it should be noted that Excel
	 * is quite temperamental in this regard.  Slight variations in technique can cause the shared
	 * formulas to spill out into plain formula records (which would make these tests pointless).
	 *
	 */
	private static final String SAMPLE_FILE_NAME = "overlapSharedFormula.xls";
	/**
	 * Some of these bugs are intermittent, and the test author couldn't think of a way to write
	 * test code to hit them bug deterministically. The reason for the unpredictability is that
	 * the bugs depended on the {@link SharedFormulaRecord}s being searched in a particular order.
	 * At the time of writing of the test, the order was being determined by the call to {@link
	 * Collection#toArray(Object[])} on {@link HashMap#values()} where the items in the map were
	 * using default {@link Object#hashCode()}<br/>
	 */
	private static final int MAX_ATTEMPTS=5;

	/**
	 * This bug happened when there were two or more shared formula ranges that overlapped.  POI
	 * would sometimes associate formulas in the overlapping region with the wrong shared formula
	 */
	public void testPartiallyOverlappingRanges() {
		Record[] records;

		int attempt=1;
		do {
			HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(SAMPLE_FILE_NAME);

			HSSFSheet sheet = wb.getSheetAt(0);
			RecordInspector.getRecords(sheet, 0);
			assertEquals("1+1", sheet.getRow(2).getCell(0).getCellFormula());
			if ("1+1".equals(sheet.getRow(3).getCell(0).getCellFormula())) {
				throw new AssertionFailedError("Identified bug - wrong shared formula record chosen"
						+ " (attempt " + attempt + ")");
			}
			assertEquals("2+2", sheet.getRow(3).getCell(0).getCellFormula());
			records = RecordInspector.getRecords(sheet, 0);
		} while (attempt++ < MAX_ATTEMPTS);

		int count=0;
		for (int i = 0; i < records.length; i++) {
			if (records[i] instanceof SharedFormulaRecord) {
				count++;
			}
		}
		assertEquals(2, count);
	}

	/**
	 * This bug occurs for similar reasons to the bug in {@link #testPartiallyOverlappingRanges()}
	 * but the symptoms are much uglier - serialization fails with {@link NullPointerException}.<br/>
	 */
	public void testCompletelyOverlappedRanges() {
		Record[] records;

		int attempt=1;
		do {
			HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(SAMPLE_FILE_NAME);

			HSSFSheet sheet = wb.getSheetAt(1);
			try {
				records = RecordInspector.getRecords(sheet, 0);
			} catch (NullPointerException e) {
				throw new AssertionFailedError("Identified bug " +
						"- cannot reserialize completely overlapped shared formula"
						+ " (attempt " + attempt + ")");
			}
		} while (attempt++ < MAX_ATTEMPTS);

		int count=0;
		for (int i = 0; i < records.length; i++) {
			if (records[i] instanceof SharedFormulaRecord) {
				count++;
			}
		}
		assertEquals(2, count);
	}

	/**
	 * Tests fix for a bug in the way shared formula cells are associated with shared formula
	 * records.  Prior to this fix, POI would attempt to use the upper left corner of the
	 * shared formula range as the locator cell.  The correct cell to use is the 'first cell'
	 * in the shared formula group which is not always the top left cell.  This is possible
	 * because shared formula groups may be sparse and may overlap.<br/>
	 *
	 * Two existing sample files (15228.xls and ex45046-21984.xls) had similar issues.
	 * These were not explored fully, but seem to be fixed now.
	 */
	public void testRecalculateFormulas47747() {

		/*
		 * ex47747-sharedFormula.xls is a heavily cut-down version of the spreadsheet from
		 * the attachment (id=24176) in Bugzilla 47747.  This was done to make the sample
		 * file smaller, which hopefully allows the special data encoding condition to be
		 * seen more easily.  Care must be taken when modifying this file since the
		 * special conditions are easily destroyed (which would make this test useless).
		 * It seems that removing the worksheet protection has made this more so - if the
		 * current file is re-saved in Excel(2007) the bug condition disappears.
		 *
		 *
		 * Using BiffViewer, one can see that there are two shared formula groups representing
		 * the essentially same formula over ~20 cells.  The shared group ranges overlap and
		 * are A12:Q20 and A20:Q27.  The locator cell ('first cell') for the second group is
		 * Q20 which is not the top left cell of the enclosing range.  It is this specific
		 * condition which caused the bug to occur
		 */
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex47747-sharedFormula.xls");

		// pick out a cell from within the second shared formula group
		HSSFCell cell = wb.getSheetAt(0).getRow(23).getCell(0);
		String formulaText;
		try {
			formulaText = cell.getCellFormula();
			// succeeds if the formula record has been associated
			// with the second shared formula group
		} catch (RuntimeException e) {
			// bug occurs if the formula record has been associated
			// with the first shared formula group
			if ("Shared Formula Conversion: Coding Error".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 47747");
			}
			throw e;
		}
		assertEquals("$AF24*A$7", formulaText);
	}

	/**
	 * Convenience test method for digging the {@link SharedValueManager} out of a
	 * {@link RowRecordsAggregate}.
	 */
	public static SharedValueManager extractFromRRA(RowRecordsAggregate rra) {
		Field f;
		try {
			f = RowRecordsAggregate.class.getDeclaredField("_sharedValueManager");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

		f.setAccessible(true);
		try {
			return (SharedValueManager) f.get(rra);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
