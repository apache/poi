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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collection;
import java.util.HashMap;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SharedValueManager}
 */
final class TestSharedValueManager {

	/**
	 * This Excel workbook contains two sheets that each have a pair of overlapping shared formula
	 * ranges.  The first sheet has one row and one column shared formula ranges which intersect.
	 * The second sheet has two column shared formula ranges - one contained within the other.
	 * These shared formula ranges were created by fill-dragging a single cell formula across the
	 * desired region.  The larger shared formula ranges were placed first.<br>
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
	 * using default {@link Object#hashCode()}<br>
	 */
	private static final int MAX_ATTEMPTS=5;

	/**
	 * This bug happened when there were two or more shared formula ranges that overlapped.  POI
	 * would sometimes associate formulas in the overlapping region with the wrong shared formula
	 */
	@Test
	void testPartiallyOverlappingRanges() {


		for (int attempt=1; attempt < MAX_ATTEMPTS; attempt++) {
			HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(SAMPLE_FILE_NAME);

			HSSFSheet sheet = wb.getSheetAt(0);
			assertEquals("1+1", sheet.getRow(2).getCell(0).getCellFormula());
			String act = sheet.getRow(3).getCell(0).getCellFormula();
			assertNotEquals("wrong shared formula record chosen", "1+1", act);
			act = sheet.getRow(3).getCell(0).getCellFormula();
			assertEquals("2+2", act);

			int[] count = { 0 };
			sheet.getSheet().visitContainedRecords(r -> count[0] += r instanceof SharedFormulaRecord ? 1 : 0, 0);
			assertEquals(2, count[0]);
		}
	}

	/**
	 * This bug occurs for similar reasons to the bug in {@link #testPartiallyOverlappingRanges()}
	 * but the symptoms are much uglier - serialization fails with {@link NullPointerException}.<br>
	 */
	@Test
	void testCompletelyOverlappedRanges() {
		for (int attempt=1; attempt < MAX_ATTEMPTS; attempt++) {
			HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(SAMPLE_FILE_NAME);

			HSSFSheet sheet = wb.getSheetAt(1);

			int[] count = { 0 };

			// NullPointerException -> cannot reserialize completely overlapped shared formula
			sheet.getSheet().visitContainedRecords(r -> count[0] += r instanceof SharedFormulaRecord ? 1 : 0, 0);
			assertEquals(2, count[0]);
		}
	}

	/**
	 * Tests fix for a bug in the way shared formula cells are associated with shared formula
	 * records.  Prior to this fix, POI would attempt to use the upper left corner of the
	 * shared formula range as the locator cell.  The correct cell to use is the 'first cell'
	 * in the shared formula group which is not always the top left cell.  This is possible
	 * because shared formula groups may be sparse and may overlap.<br>
	 *
	 * Two existing sample files (15228.xls and ex45046-21984.xls) had similar issues.
	 * These were not explored fully, but seem to be fixed now.
	 */
	@Test
	void testRecalculateFormulas47747() {

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
		// bug occurs if the formula record has been associated
		// with the first (and not the second) shared formula group
		String formulaText = cell.getCellFormula();
		assertEquals("$AF24*A$7", formulaText);
	}

	@Test
    void testBug52527() {
        HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("52527.xls");
        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);

        assertEquals("IF(H3,LINEST(N9:N14,K9:M14,FALSE),LINEST(N8:N14,K8:M14,FALSE))",
                wb1.getSheetAt(0).getRow(4).getCell(11).getCellFormula());
        assertEquals("IF(H3,LINEST(N9:N14,K9:M14,FALSE),LINEST(N8:N14,K8:M14,FALSE))",
                wb2.getSheetAt(0).getRow(4).getCell(11).getCellFormula());

        assertEquals("1/SQRT(J9)",
                wb1.getSheetAt(0).getRow(8).getCell(10).getCellFormula());
        assertEquals("1/SQRT(J9)",
                wb2.getSheetAt(0).getRow(8).getCell(10).getCellFormula());

        assertEquals("1/SQRT(J26)",
                wb1.getSheetAt(0).getRow(25).getCell(10).getCellFormula());
        assertEquals("1/SQRT(J26)",
                wb2.getSheetAt(0).getRow(25).getCell(10).getCellFormula());
    }
}
