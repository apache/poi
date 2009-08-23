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

package org.apache.poi.ddf;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import junit.framework.TestCase;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.POIDataSamples;

/**
 * Tests for {@link EscherContainerRecord}
 */
public final class TestEscherContainerRecord extends TestCase {
    private static final POIDataSamples _samples = POIDataSamples.getDDFInstance();

	public void testFillFields() {
		EscherRecordFactory f = new DefaultEscherRecordFactory();
		byte[] data = HexRead.readFromString("0F 02 11 F1 00 00 00 00");
		EscherRecord r = f.createRecord(data, 0);
		r.fillFields(data, 0, f);
		assertTrue(r instanceof EscherContainerRecord);
		assertEquals((short) 0x020F, r.getOptions());
		assertEquals((short) 0xF111, r.getRecordId());

		data = HexRead.readFromString("0F 02 11 F1 08 00 00 00" +
				" 02 00 22 F2 00 00 00 00");
		r = f.createRecord(data, 0);
		r.fillFields(data, 0, f);
		EscherRecord c = r.getChild(0);
		assertFalse(c instanceof EscherContainerRecord);
		assertEquals((short) 0x0002, c.getOptions());
		assertEquals((short) 0xF222, c.getRecordId());
	}

	public void testSerialize() {
		UnknownEscherRecord r = new UnknownEscherRecord();
		r.setOptions((short) 0x123F);
		r.setRecordId((short) 0xF112);
		byte[] data = new byte[8];
		r.serialize(0, data, new NullEscherSerializationListener());

		assertEquals("[3F, 12, 12, F1, 00, 00, 00, 00]", HexDump.toHex(data));

		EscherRecord childRecord = new UnknownEscherRecord();
		childRecord.setOptions((short) 0x9999);
		childRecord.setRecordId((short) 0xFF01);
		r.addChildRecord(childRecord);
		data = new byte[16];
		r.serialize(0, data, new NullEscherSerializationListener());

		assertEquals("[3F, 12, 12, F1, 08, 00, 00, 00, 99, 99, 01, FF, 00, 00, 00, 00]", HexDump.toHex(data));

	}

	public void testToString() {
		EscherContainerRecord r = new EscherContainerRecord();
		r.setRecordId(EscherContainerRecord.SP_CONTAINER);
		r.setOptions((short) 0x000F);
		String nl = System.getProperty("line.separator");
		assertEquals("org.apache.poi.ddf.EscherContainerRecord (SpContainer):" + nl +
				"  isContainer: true" + nl +
				"  options: 0x000F" + nl +
				"  recordId: 0xF004" + nl +
				"  numchildren: 0" + nl
				, r.toString());

		EscherOptRecord r2 = new EscherOptRecord();
		r2.setOptions((short) 0x9876);
		r2.setRecordId(EscherOptRecord.RECORD_ID);

		String expected;
		r.addChildRecord(r2);
		expected = "org.apache.poi.ddf.EscherContainerRecord (SpContainer):" + nl +
				   "  isContainer: true" + nl +
				   "  options: 0x000F" + nl +
				   "  recordId: 0xF004" + nl +
				   "  numchildren: 1" + nl +
				   "  children: " + nl +
				   "   Child 0:" + nl +
				   "org.apache.poi.ddf.EscherOptRecord:" + nl +
				   "  isContainer: false" + nl +
				   "  options: 0x0003" + nl +
				   "  recordId: 0xF00B" + nl +
				   "  numchildren: 0" + nl +
				   "  properties:" + nl;
		assertEquals(expected, r.toString());

		r.addChildRecord(r2);
		expected = "org.apache.poi.ddf.EscherContainerRecord (SpContainer):" + nl +
				   "  isContainer: true" + nl +
				   "  options: 0x000F" + nl +
				   "  recordId: 0xF004" + nl +
				   "  numchildren: 2" + nl +
				   "  children: " + nl +
				   "   Child 0:" + nl +
				   "org.apache.poi.ddf.EscherOptRecord:" + nl +
				   "  isContainer: false" + nl +
				   "  options: 0x0003" + nl +
				   "  recordId: 0xF00B" + nl +
				   "  numchildren: 0" + nl +
				   "  properties:" + nl +
				   "   Child 1:" + nl +
				   "org.apache.poi.ddf.EscherOptRecord:" + nl +
				   "  isContainer: false" + nl +
				   "  options: 0x0003" + nl +
				   "  recordId: 0xF00B" + nl +
				   "  numchildren: 0" + nl +
				   "  properties:" + nl;
		assertEquals(expected, r.toString());
	}

	private static final class DummyEscherRecord extends EscherRecord {
		public DummyEscherRecord() { }
		public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) { return 0; }
		public int serialize(int offset, byte[] data, EscherSerializationListener listener) { return 0; }
		public int getRecordSize() { return 10; }
		public String getRecordName() { return ""; }
	}

	public void testGetRecordSize() {
		EscherContainerRecord r = new EscherContainerRecord();
		r.addChildRecord(new DummyEscherRecord());
		assertEquals(18, r.getRecordSize());
	}

	/**
	 * We were having problems with reading too much data on an UnknownEscherRecord,
	 *  but hopefully we now read the correct size.
	 */
	public void testBug44857() throws Exception {
		byte[] data = _samples.readFile("Container.dat");

		// This used to fail with an OutOfMemory
		EscherContainerRecord record = new EscherContainerRecord();
		record.fillFields(data, 0, new DefaultEscherRecordFactory());
	}

	/**
	 * Ensure {@link EscherContainerRecord} doesn't spill its guts everywhere
	 */
	public void testChildren() {
		EscherContainerRecord ecr = new EscherContainerRecord();
		List<EscherRecord> children0 = ecr.getChildRecords();
		assertEquals(0, children0.size());

		EscherRecord chA = new DummyEscherRecord();
		EscherRecord chB = new DummyEscherRecord();
		EscherRecord chC = new DummyEscherRecord();

		ecr.addChildRecord(chA);
		ecr.addChildRecord(chB);
		children0.add(chC);

		List<EscherRecord> children1 = ecr.getChildRecords();
		assertTrue(children0 !=  children1);
		assertEquals(2, children1.size());
		assertEquals(chA, children1.get(0));
		assertEquals(chB, children1.get(1));

		assertEquals(1, children0.size()); // first copy unchanged

		ecr.setChildRecords(children0);
		ecr.addChildRecord(chA);
		List<EscherRecord> children2 = ecr.getChildRecords();
		assertEquals(2, children2.size());
		assertEquals(chC, children2.get(0));
		assertEquals(chA, children2.get(1));
	}
}
