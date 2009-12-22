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

import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.TestHSSFWorkbook;

/**
 * Unit test for the Workbook class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestWorkbook extends TestCase {
	public void testFontStuff() {
		InternalWorkbook wb = TestHSSFWorkbook.getInternalWorkbook(new HSSFWorkbook());

		assertEquals(4, wb.getNumberOfFontRecords());
		assertEquals(68, wb.getRecords().size());

		FontRecord f1 = wb.getFontRecordAt(0);
		FontRecord f4 = wb.getFontRecordAt(3);

		assertEquals(0, wb.getFontIndex(f1));
		assertEquals(3, wb.getFontIndex(f4));

		assertEquals(f1, wb.getFontRecordAt(0));
		assertEquals(f4, wb.getFontRecordAt(3));

		// There is no 4! new ones go in at 5

		FontRecord n = wb.createNewFont();
		assertEquals(69, wb.getRecords().size());
		assertEquals(5, wb.getNumberOfFontRecords());
		assertEquals(5, wb.getFontIndex(n));
		assertEquals(n, wb.getFontRecordAt(5));

		// And another
		FontRecord n6 = wb.createNewFont();
		assertEquals(70, wb.getRecords().size());
		assertEquals(6, wb.getNumberOfFontRecords());
		assertEquals(6, wb.getFontIndex(n6));
		assertEquals(n6, wb.getFontRecordAt(6));


		// Now remove the one formerly at 5
		assertEquals(70, wb.getRecords().size());
		wb.removeFontRecord(n);

		// Check that 6 has gone to 5
		assertEquals(69, wb.getRecords().size());
		assertEquals(5, wb.getNumberOfFontRecords());
		assertEquals(5, wb.getFontIndex(n6));
		assertEquals(n6, wb.getFontRecordAt(5));

		// Check that the earlier ones are unchanged
		assertEquals(0, wb.getFontIndex(f1));
		assertEquals(3, wb.getFontIndex(f4));
		assertEquals(f1, wb.getFontRecordAt(0));
		assertEquals(f4, wb.getFontRecordAt(3));

		// Finally, add another one
		FontRecord n7 = wb.createNewFont();
		assertEquals(70, wb.getRecords().size());
		assertEquals(6, wb.getNumberOfFontRecords());
		assertEquals(6, wb.getFontIndex(n7));
		assertEquals(n7, wb.getFontRecordAt(6));
	}
}
