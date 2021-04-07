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

import static org.apache.poi.hssf.record.TestcaseRecordInputStream.confirmRecordEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.ArrayPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Tests the NameRecord serializes/deserializes correctly
 */
final class TestNameRecord {

	/**
	 * Makes sure that additional name information is parsed properly such as menu/description
	 */
	@Test
	void testFillExtras() {

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

	@Test
	void testReserialize() {
		byte[] data = HexRead
				.readFromString(""
						+ "20 00 00 01 0B 00 00 00 01 00 00 00 00 00 00 06 3B 00 00 00 00 02 00 00 00 09 00]");
		RecordInputStream in = TestcaseRecordInputStream.create(NameRecord.sid, data);
		NameRecord nr = new NameRecord(in);
		assertEquals(0x0020, nr.getOptionFlag());
		byte[] data2 = nr.serialize();
		confirmRecordEncoding(NameRecord.sid, data, data2);
	}

	@Test
	void testFormulaRelAbs_bug46174() throws IOException {
		// perhaps this testcase belongs on TestHSSFName
		try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFName name = wb.createName();
			wb.createSheet("Sheet1");
			name.setNameName("test");
			name.setRefersToFormula("Sheet1!$B$3");
			assertNotEquals("Sheet1!B3", name.getRefersToFormula());
			assertEquals("Sheet1!$B$3", name.getRefersToFormula());
		}
	}

	@Test
	void testFormulaGeneral() throws IOException {
		// perhaps this testcase belongs on TestHSSFName
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFName name = wb.createName();
		wb.createSheet("Sheet1");
		name.setNameName("test");
		name.setRefersToFormula("Sheet1!A1+Sheet1!A2");
		assertEquals("Sheet1!A1+Sheet1!A2", name.getRefersToFormula());
		name.setRefersToFormula("5*6");
		assertEquals("5*6", name.getRefersToFormula());

		wb.close();
	}

    /**
     *  A NameRecord followed by a ContinueRecord. See Bugzilla 50244
     */
	@Test
    void test50244() throws IOException {
        byte[] data1 = RawDataUtil.decompress(
			"H4sIAAAAAAAAAI3Zu2qWQRAA0I8EQcHGzkrwBcLO7B0sBR9AwdYIKdKFoPhovp7xLyQWyX+m2OWw7TIX5u3x/t1xHG9ePhyXxyl+" +
			"fbz+cf3p/ufd3c3nm/vbm6tvx++L18fXy4fr4tVxxNWXLFelhCENlRCIZuiERAzDJFTEMmxCM/RiCMJApKESJqIZOmEhhmESNmIZ" +
			"tiCKIYohCIlIQyVURDN0QkMMwyR0xDJswjDMYoizGH9Lzib8+6NnEIREpKESKqIZOqEhhmESOmIZNmEUQje0bViEipiGQUhENzRC" +
			"IKohCQURZ3FKT0kYiDAUQjc87iCfwyJUxDQMQiK6oRECUQ1JKIgwFEHZhrUNizAR0zAIA9ENjdAR1ZCEhghDIVRDbsMiBGIaBqEg" +
			"uqEZpmEZNiEMWQxBqIg0VEJDNEMndMQwTMJALMMmTMMqhiBsRBqqIAqiGTohEMMwCYlYhk2ohlYMQeiINFTCQDRDP4vT/FMNzdAJ" +
			"gRiGSUjEMmxCNbRiCEJHpKEahqEbGqEhqiEJFRGGQkhDbMMiFMQ0jLM4padlmIZB6IhuaIT2Pz4c3188+VoNSaiIMBRCGh7PGc9h" +
			"EQpiGoagbEQ3NMJCVEMSJiIMhTAMjxdcz2ERGmIaBqEiuqERElENSQhEGMrT+APX2vm3iyMAAA=="
		);

        RecordInputStream in1 = TestcaseRecordInputStream.create(data1);
        NameRecord nr1 = new NameRecord(in1);
        assert_bug50244(nr1);

        byte[] data2 = nr1.serialize();

        assertEquals(data1.length, data2.length);
        RecordInputStream in2 = TestcaseRecordInputStream.create(data2);
        NameRecord nr2 = new NameRecord(in2);
        assert_bug50244(nr2);
    }

    private void assert_bug50244(NameRecord nr){
        assertEquals("wDataGruppeSerie.", nr.getNameText());
        Ptg[] ptg = nr.getNameDefinition();
        assertEquals(1, ptg.length);
        ArrayPtg arr = (ArrayPtg)ptg[0];
        assertEquals(696, arr.getRowCount());
        assertEquals(1, arr.getColumnCount());
        Object[][] vals = arr.getTokenArrayValues();
        assertEquals("1.T20.001", vals[0][0]);
        assertEquals("1.T20.010", vals[vals.length - 1][0]);
    }

	@Test
    void testBug57923() {
        NameRecord record = new NameRecord();
        assertEquals(0, record.getExternSheetNumber());

        record.setNameDefinition(Ptg.EMPTY_PTG_ARRAY);
        assertEquals(0, record.getExternSheetNumber());

        record.setNameDefinition(new Ptg[] {new NamePtg(1)});
        assertEquals(0, record.getExternSheetNumber());

        record.setNameDefinition(new Ptg[] {new Area3DPtg("area", 1)});
        assertEquals(1, record.getExternSheetNumber());

        record.setNameDefinition(new Ptg[] {new Ref3DPtg("A1", 1)});
        assertEquals(1, record.getExternSheetNumber());
    }

}
