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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFTestHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import junit.framework.TestCase;
/**
 * Tests for <tt>FeatRecord</tt>
 * 
 * @author Josh Micich
 */
public final class TestFeatRecord extends TestCase {
	public void testWithoutFeatRecord() throws Exception {
		HSSFWorkbook hssf = 
			HSSFTestDataSamples.openSampleWorkbook("46136-WithWarnings.xls");
		InternalWorkbook wb = HSSFTestHelper.getWorkbookForTest(hssf);
		
		assertEquals(1, hssf.getNumberOfSheets());
		
		int countFR = 0;
		int countFRH = 0;
		
		// Check on the workbook, but shouldn't be there!
		for(Record r : wb.getRecords()) {
			if(r instanceof FeatRecord) {
				countFR++;
			} else if (r.getSid() == FeatRecord.sid) {
				countFR++;
			}
			if(r instanceof FeatHdrRecord) {
				countFRH++;
			} else if (r.getSid() == FeatHdrRecord.sid) {
				countFRH++;
			}
		}
		
		assertEquals(0, countFR);
		assertEquals(0, countFRH);
		
		// Now check on the sheet
		HSSFSheet s = hssf.getSheetAt(0);
		InternalSheet sheet = HSSFTestHelper.getSheetForTest(s);
		
		for(RecordBase rb : sheet.getRecords()) {
			if(rb instanceof Record) {
				Record r = (Record)rb;
				if(r instanceof FeatRecord) {
					countFR++;
				} else if (r.getSid() == FeatRecord.sid) {
					countFR++;
				}
				if(r instanceof FeatHdrRecord) {
					countFRH++;
				} else if (r.getSid() == FeatHdrRecord.sid) {
					countFRH++;
				}
			}
		}
		
		assertEquals(0, countFR);
		assertEquals(0, countFRH);
	}

	public void testReadFeatRecord() throws Exception {
		HSSFWorkbook hssf = 
			HSSFTestDataSamples.openSampleWorkbook("46136-NoWarnings.xls");
		InternalWorkbook wb = HSSFTestHelper.getWorkbookForTest(hssf);
		
		FeatRecord fr = null;
		
		assertEquals(1, hssf.getNumberOfSheets());
		
		// First check it isn't on the Workbook
		int countFR = 0;
		int countFRH = 0;
		for(Record r : wb.getRecords()) {
			if(r instanceof FeatRecord) {
				fr = (FeatRecord)r;
				countFR++;
			} else if (r.getSid() == FeatRecord.sid) {
				fail("FeatRecord SID found but not created correctly!");
			}
			if(r instanceof FeatHdrRecord) {
				countFRH++;
			} else if (r.getSid() == FeatHdrRecord.sid) {
				fail("FeatHdrRecord SID found but not created correctly!");
			}
		}
		
		assertEquals(0, countFR);
		assertEquals(0, countFRH);
		
		// Now find it on our sheet
		HSSFSheet s = hssf.getSheetAt(0);
		InternalSheet sheet = HSSFTestHelper.getSheetForTest(s);
		
		for(RecordBase rb : sheet.getRecords()) {
			if(rb instanceof Record) {
				Record r = (Record)rb;
				if(r instanceof FeatRecord) {
					fr = (FeatRecord)r;
					countFR++;
				} else if (r.getSid() == FeatRecord.sid) {
					countFR++;
				}
				if(r instanceof FeatHdrRecord) {
					countFRH++;
				} else if (r.getSid() == FeatHdrRecord.sid) {
					countFRH++;
				}
			}
		}
		
		assertEquals(1, countFR);
		assertEquals(1, countFRH);
		assertNotNull(fr);
		
		// Now check the contents are as expected
	}
}
