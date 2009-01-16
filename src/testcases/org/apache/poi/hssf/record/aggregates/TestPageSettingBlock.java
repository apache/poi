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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Tess for {@link PageSettingsBlock}
 * 
 * @author Dmitriy Kumshayev
 */
public final class TestPageSettingBlock extends TestCase {
	
	public void testPrintSetup_bug46548() {
		
		// PageSettingBlock in this file contains PLS (sid=x004D) record 
		// followed by ContinueRecord (sid=x003C)  
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex46548-23133.xls");
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFPrintSetup ps = sheet.getPrintSetup();
		
		try {
			ps.getCopies();
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new AssertionFailedError("Identified bug 46548: PageSettingBlock missing PrintSetupRecord record");
		}
	}
}
