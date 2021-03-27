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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Tests for <tt>LabelRecord</tt>
 */
final class TestLabelRecord  {

	@Test
	void testEmptyString() throws IOException {
		try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("ex42570-20305.xls");
			 HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
			HSSFSheet s1 = wb1.getSheetAt(0);
			HSSFSheet s2 = wb2.getSheetAt(0);
			for (int c=0; c<2; c++) {
				for (int r=0; r<146; r++) {
					assertEquals(s1.getRow(r).getCell(c).getNumericCellValue(), s2.getRow(r).getCell(c).getNumericCellValue(), 0);
				}
			}
		}
	}
}
