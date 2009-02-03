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

package org.apache.poi.hssf.record;

import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.HexRead;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests the NameRecord serializes/deserializes correctly
 * 
 * @author Danny Mui (dmui at apache dot org)
 */
public final class TestNameRecord extends TestCase {

	/**
	 * Makes sure that additional name information is parsed properly such as menu/description
	 */
	public void testFillExtras() {

		byte[] examples = HexRead.readFromString(""
				+ "88 03 67 06 07 00 00 00 00 00 00 23 00 00 00 4D "
				+ "61 63 72 6F 31 3A 01 00 00 00 11 00 00 4D 61 63 "
				+ "72 6F 20 72 65 63 6F 72 64 65 64 20 32 37 2D 53 "
				+ "65 70 2D 39 33 20 62 79 20 41 4C 4C 57 4F 52");

		NameRecord name = new NameRecord(TestcaseRecordInputStream.create(NameRecord.sid, examples));
		String description = name.getDescriptionText();
		assertNotNull(description);
		assertTrue(description.endsWith("Macro recorded 27-Sep-93 by ALLWOR"));
	}

	public void testReserialize() {
		byte[] data = HexRead
				.readFromString(""
						+ "20 00 00 01 0B 00 00 00 01 00 00 00 00 00 00 06 3B 00 00 00 00 02 00 00 00 09 00]");
		RecordInputStream in = TestcaseRecordInputStream.create(NameRecord.sid, data);
		NameRecord nr = new NameRecord(in);
		assertEquals(0x0020, nr.getOptionFlag());
		byte[] data2 = nr.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding(NameRecord.sid, data, data2);
	}
	public void testFormulaRelAbs_bug46174() {
		// perhaps this testcase belongs on TestHSSFName
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFName name = wb.createName();
		wb.createSheet("Sheet1");
		name.setNameName("test");
		name.setRefersToFormula("Sheet1!$B$3");
		if (name.getRefersToFormula().equals("Sheet1!B3")) {
			throw new AssertionFailedError("Identified bug 46174");
		}
		assertEquals("Sheet1!$B$3", name.getRefersToFormula());
	}
	public void testFormulaGeneral() {
		// perhaps this testcase belongs on TestHSSFName
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFName name = wb.createName();
		wb.createSheet("Sheet1");
		name.setNameName("test");
		name.setRefersToFormula("Sheet1!A1+Sheet1!A2");
		assertEquals("Sheet1!A1+Sheet1!A2", name.getRefersToFormula());
		name.setRefersToFormula("5*6");
		assertEquals("5*6", name.getRefersToFormula());
	}
}
