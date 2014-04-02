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
import org.apache.poi.hssf.record.common.FeatFormulaErr2;
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
		FeatHdrRecord fhr = null;
		
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
					fhr = (FeatHdrRecord)r;
					countFRH++;
				} else if (r.getSid() == FeatHdrRecord.sid) {
					countFRH++;
				}
			}
		}
		
		assertEquals(1, countFR);
		assertEquals(1, countFRH);
		assertNotNull(fr);
		assertNotNull(fhr);
		
		// Now check the contents are as expected
		assertEquals(
				FeatHdrRecord.SHAREDFEATURES_ISFFEC2,
				fr.getIsf_sharedFeatureType()
		);
		
		// Applies to one cell only
		assertEquals(1, fr.getCellRefs().length);
		assertEquals(0, fr.getCellRefs()[0].getFirstRow());
		assertEquals(0, fr.getCellRefs()[0].getLastRow());
		assertEquals(0, fr.getCellRefs()[0].getFirstColumn());
		assertEquals(0, fr.getCellRefs()[0].getLastColumn());
		
		// More checking of shared features stuff
		assertEquals(4, fr.getCbFeatData());
		assertEquals(4, fr.getSharedFeature().getDataSize());
		assertEquals(FeatFormulaErr2.class, fr.getSharedFeature().getClass());
		
		FeatFormulaErr2 fferr2 = (FeatFormulaErr2)fr.getSharedFeature();
		assertEquals(0x04, fferr2._getRawErrorCheckValue());
		
		assertFalse(fferr2.getCheckCalculationErrors());
		assertFalse(fferr2.getCheckDateTimeFormats());
		assertFalse(fferr2.getCheckEmptyCellRef());
		assertFalse(fferr2.getCheckInconsistentFormulas());
		assertFalse(fferr2.getCheckInconsistentRanges());
		assertTrue(fferr2.getCheckNumbersAsText());
		assertFalse(fferr2.getCheckUnprotectedFormulas());
		assertFalse(fferr2.getPerformDataValidation());
	}

    /**
     *  cloning sheets with feat records 
     */
    public void testCloneSheetWithFeatRecord() throws Exception {
        HSSFWorkbook wb =
            HSSFTestDataSamples.openSampleWorkbook("46136-WithWarnings.xls");
        wb.cloneSheet(0);
    }
}
