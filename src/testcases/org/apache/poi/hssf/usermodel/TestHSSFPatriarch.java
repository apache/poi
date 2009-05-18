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

import org.apache.poi.hssf.HSSFTestDataSamples;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * @author Josh Micich
 */
public final class TestHSSFPatriarch extends TestCase {

	public void testBasic() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();

		HSSFPatriarch patr = sheet.createDrawingPatriarch();
		assertNotNull(patr);

		// assert something more interesting
	}

	// TODO - fix bug 44916 (1-May-2008)
	public void DISABLED_test44916() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();

		// 1. Create drawing patriarch
		HSSFPatriarch patr = sheet.createDrawingPatriarch();

		// 2. Try to re-get the patriarch
		HSSFPatriarch existingPatr;
		try {
			existingPatr = sheet.getDrawingPatriarch();
		} catch (NullPointerException e) {
			throw new AssertionFailedError("Identified bug 44916");
		}

		// 3. Use patriarch
		HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 600, 245, (short) 1, 1, (short) 1, 2);
		anchor.setAnchorType(3);
		byte[] pictureData = HSSFTestDataSamples.getTestDataFileContent("logoKarmokar4.png");
		int idx1 = wb.addPicture(pictureData, HSSFWorkbook.PICTURE_TYPE_PNG);
		patr.createPicture(anchor, idx1);

		// 4. Try to re-use patriarch later
		existingPatr = sheet.getDrawingPatriarch();
		assertNotNull(existingPatr);
	}
}
