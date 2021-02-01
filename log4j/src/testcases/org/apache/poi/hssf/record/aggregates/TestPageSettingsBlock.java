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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BottomMarginRecord;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FooterRecord;
import org.apache.poi.hssf.record.HCenterRecord;
import org.apache.poi.hssf.record.HeaderFooterRecord;
import org.apache.poi.hssf.record.HeaderRecord;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.UserSViewBegin;
import org.apache.poi.hssf.record.UserSViewEnd;
import org.apache.poi.hssf.record.VCenterRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

/**
 * Tess for {@link PageSettingsBlock}
 */
final class TestPageSettingsBlock {
	@Test
	void testPrintSetup_bug46548() {
		// PageSettingBlock in this file contains PLS (sid=x004D) record
		// followed by ContinueRecord (sid=x003C)
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex46548-23133.xls");
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFPrintSetup ps = sheet.getPrintSetup();

		// bug 46548: PageSettingBlock missing PrintSetupRecord record
		assertEquals(1, ps.getCopies());
	}

	/**
	 * Bug 46840 occurred because POI failed to recognise HEADERFOOTER as part of the
	 * {@link PageSettingsBlock}.
	 */
	@Test
	void testHeaderFooter_bug46840() {

		int rowIx = 5;
		int colIx = 6;
		NumberRecord nr = new NumberRecord();
		nr.setRow(rowIx);
		nr.setColumn((short) colIx);
		nr.setValue(3.0);

		org.apache.poi.hssf.record.Record[] recs = {
				BOFRecord.createSheetBOF(),
				new HeaderRecord("&LSales Figures"),
				new FooterRecord("&LJanuary"),
				new HeaderFooterRecord(HexRead.readFromString("9C 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C4 60 00 00 00 00 00 00 00 00")),
				new DimensionsRecord(),
				new WindowTwoRecord(),
				new UserSViewBegin(HexRead.readFromString("ED 77 3B 86 BC 3F 37 4C A9 58 60 23 43 68 54 4B 01 00 00 00 64 00 00 00 40 00 00 00 02 00 00 00 3D 80 04 00 00 00 00 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F0 3F FF FF 01 00")),
				new HeaderRecord("&LSales Figures"),
				new FooterRecord("&LJanuary"),
				new HeaderFooterRecord(HexRead.readFromString("9C 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C4 60 00 00 00 00 00 00 00 00")),
				new UserSViewEnd(HexRead.readFromString("01, 00")),

				EOFRecord.instance,
		};
		RecordStream rs = new RecordStream(Arrays.asList(recs), 0);
		// bug 46480- two Page Settings Blocks found in the same sheet
		InternalSheet sheet = InternalSheet.createSheet(rs);

		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
		sheet.visitContainedRecords(outRecs::add, rowIx);
		assertEquals(13, outRecs.size());
	}

	/**
	 * Bug 46953 occurred because POI didn't handle late PSB records properly.
	 */
	@Test
	void testLateHeaderFooter_bug46953() {

		int rowIx = 5;
		int colIx = 6;
		NumberRecord nr = new NumberRecord();
		nr.setRow(rowIx);
		nr.setColumn((short) colIx);
		nr.setValue(3.0);

		org.apache.poi.hssf.record.Record[] recs = {
				BOFRecord.createSheetBOF(),
				new HeaderRecord("&LSales Figures"),
				new FooterRecord("&LJanuary"),
				new DimensionsRecord(),
				new WindowTwoRecord(),
				new HeaderFooterRecord(HexRead.readFromString("9C 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C4 60 00 00 00 00 00 00 00 00")),
				EOFRecord.instance,
		};
		RecordStream rs = new RecordStream(Arrays.asList(recs), 0);
		InternalSheet sheet = InternalSheet.createSheet(rs);

		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
		sheet.visitContainedRecords(outRecs::add, 0);
		// Identified bug 46953 - EOF incorrectly appended to PSB
		assertNotEquals(EOFRecord.instance, outRecs.get(4));
		assertEquals(recs.length+1, outRecs.size()); // +1 for index record

		Class<?>[] act = outRecs.stream().map(Object::getClass).toArray(Class[]::new);
		Class<?>[] exp = { BOFRecord.class, IndexRecord.class, HeaderRecord.class, FooterRecord.class,
				HeaderFooterRecord.class, DimensionsRecord.class, WindowTwoRecord.class, EOFRecord.class };
		assertArrayEquals(exp, act);
	}
	/**
	 * Bug 47199 was due to the margin records being located well after the initial PSB records.
	 * The example file supplied (attachment 23710) had three non-PSB record types
	 * between the PRINTSETUP record and first MARGIN record:
	 * <ul>
	 * <li>PRINTSETUP(0x00A1)</li>
	 * <li>DEFAULTCOLWIDTH(0x0055)</li>
	 * <li>COLINFO(0x007D)</li>
	 * <li>DIMENSIONS(0x0200)</li>
	 * <li>BottomMargin(0x0029)</li>
	 * </ul>
	 */
	@Test
	void testLateMargins_bug47199() {

		BottomMarginRecord bottomMargin = new BottomMarginRecord();
		bottomMargin.setMargin(0.787F);

		org.apache.poi.hssf.record.Record[] recs = {
				BOFRecord.createSheetBOF(),
				new HeaderRecord("&LSales Figures"),
				new FooterRecord("&LJanuary"),
				new DimensionsRecord(),
				bottomMargin,
				new WindowTwoRecord(),
				EOFRecord.instance,
		};
		RecordStream rs = new RecordStream(Arrays.asList(recs), 0);

		// bug 47199a - failed to process late margins records
		InternalSheet sheet = InternalSheet.createSheet(rs);

		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
		sheet.visitContainedRecords(outRecs::add, 0);
		assertEquals(recs.length+1, outRecs.size()); // +1 for index record

		Class<?>[] act = outRecs.stream().map(Object::getClass).toArray(Class[]::new);
		Class<?>[] exp = { BOFRecord.class, IndexRecord.class, HeaderRecord.class, FooterRecord.class,
				BottomMarginRecord.class, DimensionsRecord.class, WindowTwoRecord.class, EOFRecord.class };
		assertArrayEquals(exp, act);
	}

	/**
	 * The PageSettingsBlock should not allow multiple copies of the same record.  This extra assertion
	 * was added while fixing bug 47199.  All existing POI test samples comply with this requirement.
	 */
	@Test
	void testDuplicatePSBRecord_bug47199() {
		// Hypothetical setup of PSB records which should cause POI to crash
		org.apache.poi.hssf.record.Record[] recs = {
			new HeaderRecord("&LSales Figures"),
			new HeaderRecord("&LInventory"),
		};
		RecordStream rs = new RecordStream(Arrays.asList(recs), 0);

		RecordFormatException ex = assertThrows(
			RecordFormatException.class,
			() -> new PageSettingsBlock(rs)
		);
		assertEquals("Duplicate PageSettingsBlock record (sid=0x14)", ex.getMessage());
	}

	/**
	 * Excel tolerates missing header / footer records, but adds them (empty) in when re-saving.
	 * This is not critical functionality but it has been decided to keep POI consistent with
	 * Excel in this regard.
	 */
	@Test
	void testMissingHeaderFooter() {
		// initialise PSB with some records, but not the header / footer
		org.apache.poi.hssf.record.Record[] recs = {
				new HCenterRecord(),
				new VCenterRecord(),
		};
		RecordStream rs = new RecordStream(Arrays.asList(recs), 0);
		PageSettingsBlock psb = new PageSettingsBlock(rs);

		// serialize the PSB to see what records come out
		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
		psb.visitContainedRecords(outRecs::add);

		assertNotEquals(2, outRecs.size(), "PageSettingsBlock didn't add missing header/footer records");

		Class<?>[] act = outRecs.stream().map(Object::getClass).toArray(Class[]::new);
		Class<?>[] exp = { HeaderRecord.class, FooterRecord.class, HCenterRecord.class, VCenterRecord.class};
		assertArrayEquals(exp, act);

		// make sure the added header / footer records are empty
		HeaderRecord hr = (HeaderRecord) outRecs.get(0);
		assertEquals("", hr.getText());
		FooterRecord fr = (FooterRecord) outRecs.get(1);
		assertEquals("", fr.getText());
	}

	/**
	 * Apparently it's OK to have more than one PLS record.
	 * Attachment 23866 from bug 47415 had a PageSettingsBlock with two PLS records.  This file
	 * seems to open OK in Excel(2007) but both PLS records are removed (perhaps because the
	 * specified printers were not available on the testing machine).  Since the example file does
	 * not upset Excel, POI will preserve multiple PLS records.</p>
	 *
	 * As of June 2009, PLS is still uninterpreted by POI
	 */
	@Test
	void testDuplicatePLS_bug47415() {
		Record plsA = new UnknownRecord(UnknownRecord.PLS_004D, HexRead.readFromString("BA AD F0 0D"));
		Record plsB = new UnknownRecord(UnknownRecord.PLS_004D, HexRead.readFromString("DE AD BE EF"));
		Record contB1 = new ContinueRecord(HexRead.readFromString("FE ED"));
		Record contB2 = new ContinueRecord(HexRead.readFromString("FA CE"));
		org.apache.poi.hssf.record.Record[] recs = {
				new HeaderRecord("&LSales Figures"),
				new FooterRecord("&LInventory"),
				new HCenterRecord(),
				new VCenterRecord(),
				plsA,
				plsB, contB1, contB2, // make sure continuing PLS is still OK
		};
		RecordStream rs = new RecordStream(Arrays.asList(recs), 0);
		// bug 47415 - Duplicate PageSettingsBlock record (sid=0x4d)
		PageSettingsBlock psb = new PageSettingsBlock(rs);

		// serialize the PSB to see what records come out
		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
		psb.visitContainedRecords(outRecs::add);

		// records were assembled in standard order, so this simple check is OK
		assertArrayEquals(recs, outRecs.toArray(new Record[0]));
	}

	@Test
    void testDuplicateHeaderFooter_bug48026() {

        org.apache.poi.hssf.record.Record[] recs = {
                BOFRecord.createSheetBOF(),
                new IndexRecord(),

                //PageSettingsBlock
                new HeaderRecord("&LDecember"),
                new FooterRecord("&LJanuary"),
                new DimensionsRecord(),

                new WindowTwoRecord(),

                //CustomViewSettingsRecordAggregate
                new UserSViewBegin(HexRead.readFromString("53 CE BD CC DE 38 44 45 97 C1 5C 89 F9 37 32 1B 01 00 00 00 64 00 00 00 40 00 00 00 03 00 00 00 7D 00 00 20 00 00 34 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF")),
                new SelectionRecord(0, 0),
                new UserSViewEnd(HexRead.readFromString("01 00")),

                // two HeaderFooterRecord records, the first one has zero GUID (16 bytes at offset 12) and belongs to the PSB,
                // the other is matched with a CustomViewSettingsRecordAggregate having UserSViewBegin with the same GUID
                new HeaderFooterRecord(HexRead.readFromString("9C 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 34 33 00 00 00 00 00 00 00 00")),
                new HeaderFooterRecord(HexRead.readFromString("9C 08 00 00 00 00 00 00 00 00 00 00 53 CE BD CC DE 38 44 45 97 C1 5C 89 F9 37 32 1B 34 33 00 00 00 00 00 00 00 00")),

                EOFRecord.instance,
        };
        RecordStream rs = new RecordStream(Arrays.asList(recs), 0);
        // bug 48026 - Duplicate PageSettingsBlock record (sid=0x89c)
        InternalSheet sheet = InternalSheet.createSheet(rs);

        List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
        sheet.visitContainedRecords(outRecs::add, 0);

        assertEquals(recs.length, outRecs.size());
        //expected order of records:
        Record[] expectedRecs = {
                recs[0],  //BOFRecord
                recs[1],  //IndexRecord

                //PageSettingsBlock
                recs[2],  //HeaderRecord
                recs[3],  //FooterRecord
                recs[9],  //HeaderFooterRecord
                recs[4],  // DimensionsRecord
                recs[5],  // WindowTwoRecord

                //CustomViewSettingsRecordAggregate
                recs[6],  // UserSViewBegin
                recs[7],  // SelectionRecord
                recs[10], // HeaderFooterRecord
                recs[8],  // UserSViewEnd

                recs[11],  //EOFRecord
        };

        assertArrayEquals(
			Stream.of(expectedRecs).map(Object::getClass).toArray(Class[]::new),
			outRecs.stream().map(Object::getClass).toArray(Class[]::new)
		);

        HeaderFooterRecord hd1 = (HeaderFooterRecord)expectedRecs[4];
        //GUID is zero
        assertArrayEquals(new byte[16], hd1.getGuid());
        assertTrue(hd1.isCurrentSheet());

        UserSViewBegin svb = (UserSViewBegin)expectedRecs[7];
        HeaderFooterRecord hd2 = (HeaderFooterRecord)expectedRecs[9];
        assertFalse(hd2.isCurrentSheet());
        //GUIDs of HeaderFooterRecord and UserSViewBegin must be the same
        assertArrayEquals(svb.getGuid(), hd2.getGuid());
    }

	@Test
    void testDuplicateHeaderFooterInside_bug48026() {

        Record[] recs = {
                BOFRecord.createSheetBOF(),
                new IndexRecord(),

                //PageSettingsBlock
                new HeaderRecord("&LDecember"),
                new FooterRecord("&LJanuary"),
                new DimensionsRecord(),

                new WindowTwoRecord(),

                //CustomViewSettingsRecordAggregate
                new UserSViewBegin(HexRead.readFromString("53 CE BD CC DE 38 44 45 97 C1 5C 89 F9 37 32 1B 01 00 00 00 64 00 00 00 40 00 00 00 03 00 00 00 7D 00 00 20 00 00 34 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF")),
                new SelectionRecord(0, 0),

                // two HeaderFooterRecord records, the first one has zero GUID (16 bytes at offset 12) and belongs to the PSB,
                // the other is matched with a CustomViewSettingsRecordAggregate having UserSViewBegin with the same GUID
                new HeaderFooterRecord(HexRead.readFromString("9C 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 34 33 00 00 00 00 00 00 00 00")),
                new HeaderFooterRecord(HexRead.readFromString("9C 08 00 00 00 00 00 00 00 00 00 00 53 CE BD CC DE 38 44 45 97 C1 5C 89 F9 37 32 1B 34 33 00 00 00 00 00 00 00 00")),

                new UserSViewEnd(HexRead.readFromString("01 00")),

                EOFRecord.instance,
        };
        RecordStream rs = new RecordStream(Arrays.asList(recs), 0);
        // Bug 48026 : Duplicate PageSettingsBlock record (sid=0x89c)
        InternalSheet sheet = InternalSheet.createSheet(rs);

		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
        sheet.visitContainedRecords(outRecs::add, 0);

        assertEquals(recs.length+1, outRecs.size());
        //expected order of records:
        Record[] expectedRecs = {
                recs[0],  //BOFRecord
                recs[1],  //IndexRecord

                //PageSettingsBlock
                recs[2],  //HeaderRecord
                recs[3],  //FooterRecord
                recs[4],  // DimensionsRecord
                recs[5],  // WindowTwoRecord

                //CustomViewSettingsRecordAggregate
                recs[6],  // UserSViewBegin
                recs[7],  // SelectionRecord
                recs[2],  //HeaderRecord
                recs[3],  //FooterRecord
                recs[8], // HeaderFooterRecord
//                recs[9],  //HeaderFooterRecord
                recs[10],  // UserSViewEnd

                recs[11],  //EOFRecord
        };

		assertArrayEquals(
			Stream.of(expectedRecs).map(Object::getClass).toArray(Class[]::new),
			outRecs.stream().map(Object::getClass).toArray(Class[]::new)
		);

        HeaderFooterRecord hd1 = (HeaderFooterRecord)expectedRecs[10];
        //GUID is zero
        assertArrayEquals(new byte[16], hd1.getGuid());
        assertTrue(hd1.isCurrentSheet());

        UserSViewBegin svb = (UserSViewBegin)expectedRecs[6];
        HeaderFooterRecord hd2 = (HeaderFooterRecord)recs[9];
        assertFalse(hd2.isCurrentSheet());
        //GUIDs of HeaderFooterRecord and UserSViewBegin must be the same
        assertArrayEquals(svb.getGuid(), hd2.getGuid());
    }
}
