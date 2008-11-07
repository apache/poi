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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SharedFormulaRecord;
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
	public void testPartiallyOverlappingRanges() throws IOException {
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
	public void testCompletelyOverlappedRanges() throws IOException {
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
}
