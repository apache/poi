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

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Test the ability to clone a sheet.
 *  If adding new records that belong to a sheet (as opposed to a book)
 *  add that record to the sheet in the testCloneSheetBasic method.
 * @author  avik
 */
public final class TestCloneSheet extends TestCase {

	public void testCloneSheetBasic(){
		HSSFWorkbook b = new HSSFWorkbook();
		HSSFSheet s = b.createSheet("Test");
		s.addMergedRegion(new CellRangeAddress(0, 1, 0, 1));
		HSSFSheet clonedSheet = b.cloneSheet(0);

		assertEquals("One merged area", 1, clonedSheet.getNumMergedRegions());
	}

	/**
	 * Ensures that pagebreak cloning works properly
	 */
	public void testPageBreakClones() {
		HSSFWorkbook b = new HSSFWorkbook();
		HSSFSheet s = b.createSheet("Test");
		s.setRowBreak(3);
		s.setColumnBreak((short) 6);

		HSSFSheet clone = b.cloneSheet(0);
		assertTrue("Row 3 not broken", clone.isRowBroken(3));
		assertTrue("Column 6 not broken", clone.isColumnBroken((short) 6));

		s.removeRowBreak(3);

		assertTrue("Row 3 still should be broken", clone.isRowBroken(3));
	}
}
