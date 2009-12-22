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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.ColumnInfoRecord;

/**
 * @author Tony Poppleton
 */
public final class TestSheetAdditional extends TestCase {

	public void testGetCellWidth() {
		InternalSheet sheet = InternalSheet.createSheet();
		ColumnInfoRecord nci = new ColumnInfoRecord();

		// Prepare test model
		nci.setFirstColumn(5);
		nci.setLastColumn(10);
		nci.setColumnWidth(100);


		sheet._columnInfos.insertColumn(nci);

		assertEquals(100,sheet.getColumnWidth(5));
		assertEquals(100,sheet.getColumnWidth(6));
		assertEquals(100,sheet.getColumnWidth(7));
		assertEquals(100,sheet.getColumnWidth(8));
		assertEquals(100,sheet.getColumnWidth(9));
		assertEquals(100,sheet.getColumnWidth(10));

		sheet.setColumnWidth(6,200);

		assertEquals(100,sheet.getColumnWidth(5));
		assertEquals(200,sheet.getColumnWidth(6));
		assertEquals(100,sheet.getColumnWidth(7));
		assertEquals(100,sheet.getColumnWidth(8));
		assertEquals(100,sheet.getColumnWidth(9));
		assertEquals(100,sheet.getColumnWidth(10));
	}

	public void testMaxColumnWidth() {
		InternalSheet sheet = InternalSheet.createSheet();
		sheet.setColumnWidth(0, 255*256); //the limit
		try {
			sheet.setColumnWidth(0, 256*256); //the limit
			fail("expected exception");
		} catch (IllegalArgumentException e){
			assertEquals(e.getMessage(), "The maximum column width for an individual cell is 255 characters.");
		}
	}
}
