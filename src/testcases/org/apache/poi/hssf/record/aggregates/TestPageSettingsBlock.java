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

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FooterRecord;
import org.apache.poi.hssf.record.HeaderRecord;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.RecordInspector.RecordCollector;
import org.apache.poi.util.HexRead;

/**
 * Tess for {@link PageSettingsBlock}
 * 
 * @author Dmitriy Kumshayev
 */
public final class TestPageSettingsBlock extends TestCase {
	
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

	/**
	 * Bug 46840 occurred because POI failed to recognise HEADERFOOTER as part of the
	 * {@link PageSettingsBlock}.
	 * 
	 */
	public void testHeaderFooter_bug46840() {

		int rowIx = 5;
		int colIx = 6;
		NumberRecord nr = new NumberRecord();
		nr.setRow(rowIx);
		nr.setColumn((short) colIx);
		nr.setValue(3.0);

		Record[] recs = {
				BOFRecord.createSheetBOF(),
				new HeaderRecord("&LSales Figures"),
				new FooterRecord("&LJanuary"),
				ur(UnknownRecord.HEADER_FOOTER_089C, "9C 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C4 60 00 00 00 00 00 00 00 00"),
				new DimensionsRecord(),
				new WindowTwoRecord(),
				ur(UnknownRecord.USERSVIEWBEGIN_01AA, "ED 77 3B 86 BC 3F 37 4C A9 58 60 23 43 68 54 4B 01 00 00 00 64 00 00 00 40 00 00 00 02 00 00 00 3D 80 04 00 00 00 00 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F0 3F FF FF 01 00"),
				new HeaderRecord("&LSales Figures"),
				new FooterRecord("&LJanuary"),
				ur(UnknownRecord.HEADER_FOOTER_089C, "9C 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C4 60 00 00 00 00 00 00 00 00"),
				ur(UnknownRecord.USERSVIEWEND_01AB, "01, 00"),

				EOFRecord.instance,
		};
		RecordStream rs = new RecordStream(Arrays.asList(recs), 0);
		Sheet sheet;
		try {
			sheet = Sheet.createSheet(rs);
		} catch (RuntimeException e) {
			if (e.getMessage().equals("two Page Settings Blocks found in the same sheet")) {
				throw new AssertionFailedError("Identified bug 46480");
			}
			throw e;
		}

		RecordCollector rv = new RecordCollector();
		sheet.visitContainedRecords(rv, rowIx);
		Record[] outRecs = rv.getRecords();
		assertEquals(13, outRecs.length);
	}

	/**
	 * Bug 46953 occurred because POI didn't handle late PSB records properly.
	 */
	public void testLateHeaderFooter_bug46953() {

		int rowIx = 5;
		int colIx = 6;
		NumberRecord nr = new NumberRecord();
		nr.setRow(rowIx);
		nr.setColumn((short) colIx);
		nr.setValue(3.0);

		Record[] recs = {
				BOFRecord.createSheetBOF(),
				new HeaderRecord("&LSales Figures"),
				new FooterRecord("&LJanuary"),
				new DimensionsRecord(),
				new WindowTwoRecord(),
				ur(UnknownRecord.HEADER_FOOTER_089C, "9C 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C4 60 00 00 00 00 00 00 00 00"),
				EOFRecord.instance,
		};
		RecordStream rs = new RecordStream(Arrays.asList(recs), 0);
		Sheet sheet = Sheet.createSheet(rs);

		RecordCollector rv = new RecordCollector();
		sheet.visitContainedRecords(rv, rowIx);
		Record[] outRecs = rv.getRecords();
		if (outRecs[4] == EOFRecord.instance) {
			throw new AssertionFailedError("Identified bug 46953 - EOF incorrectly appended to PSB");
		}
		assertEquals(recs.length+1, outRecs.length); // +1 for index record

		assertEquals(BOFRecord.class, outRecs[0].getClass());
		assertEquals(IndexRecord.class, outRecs[1].getClass());
		assertEquals(HeaderRecord.class, outRecs[2].getClass());
		assertEquals(FooterRecord.class, outRecs[3].getClass());
		assertEquals(UnknownRecord.HEADER_FOOTER_089C, outRecs[4].getSid());
		assertEquals(DimensionsRecord.class, outRecs[5].getClass());
		assertEquals(WindowTwoRecord.class, outRecs[6].getClass());
		assertEquals(EOFRecord.instance, outRecs[7]);
	}

	private static UnknownRecord ur(int sid, String hexData) {
		return new UnknownRecord(sid, HexRead.readFromString(hexData));
	}
}
