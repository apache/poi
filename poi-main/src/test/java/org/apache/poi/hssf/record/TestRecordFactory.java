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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexRead;

/**
 * Tests the record factory
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Csaba Nagy (ncsaba at yahoo dot com)
 */
public final class TestRecordFactory extends TestCase {


	/**
	 * TEST NAME:  Test Basic Record Construction <P>
	 * OBJECTIVE:  Test that the RecordFactory given the required parameters for know
	 *             record types can construct the proper record w/values.<P>
	 * SUCCESS:	Record factory creates the records with the expected values.<P>
	 * FAILURE:	The wrong records are creates or contain the wrong values <P>
	 *
	 */
	public void testBasicRecordConstruction() {
		short recType = BOFRecord.sid;
		byte[]   data	= {
			0, 6, 5, 0, -2, 28, -51, 7, -55, 64, 0, 0, 6, 1, 0, 0
		};
		Record[] record  = RecordFactory.createRecord(TestcaseRecordInputStream.create(recType, data));

		assertEquals(BOFRecord.class.getName(),
					 record[ 0 ].getClass().getName());
		BOFRecord bofRecord = ( BOFRecord ) record[ 0 ];

		assertEquals(7422, bofRecord.getBuild());
		assertEquals(1997, bofRecord.getBuildYear());
		assertEquals(16585, bofRecord.getHistoryBitMask());
		assertEquals(20, bofRecord.getRecordSize());
		assertEquals(262, bofRecord.getRequiredVersion());
		assertEquals(2057, bofRecord.getSid());
		assertEquals(5, bofRecord.getType());
		assertEquals(1536, bofRecord.getVersion());
		recType = MMSRecord.sid;
		data = new byte[] {
			0, 0
		};
		record  = RecordFactory.createRecord(TestcaseRecordInputStream.create(recType, data));
		assertEquals(MMSRecord.class.getName(),
					 record[ 0 ].getClass().getName());
		MMSRecord mmsRecord = ( MMSRecord ) record[ 0 ];

		assertEquals(0, mmsRecord.getAddMenuCount());
		assertEquals(0, mmsRecord.getDelMenuCount());
		assertEquals(6, mmsRecord.getRecordSize());
		assertEquals(193, mmsRecord.getSid());
	}

	/**
	 * TEST NAME:  Test Special Record Construction <P>
	 * OBJECTIVE:  Test that the RecordFactory given the required parameters for
	 *			 constructing a RKRecord will return a NumberRecord.<P>
	 * SUCCESS:	Record factory creates the Number record with the expected values.<P>
	 * FAILURE:	The wrong records are created or contain the wrong values <P>
	 *
	 */
	public void testSpecial() {
		short recType = RKRecord.sid;
		byte[] data = {
			0, 0, 0, 0, 21, 0, 0, 0, 0, 0
		};
		Record[] record  = RecordFactory.createRecord(TestcaseRecordInputStream.create(recType, data));

		assertEquals(NumberRecord.class.getName(),
					 record[ 0 ].getClass().getName());
		NumberRecord numberRecord = ( NumberRecord ) record[ 0 ];

		assertEquals(0, numberRecord.getColumn());
		assertEquals(18, numberRecord.getRecordSize());
		assertEquals(0, numberRecord.getRow());
		assertEquals(515, numberRecord.getSid());
		assertEquals(0.0, numberRecord.getValue(), 0.001);
		assertEquals(21, numberRecord.getXFIndex());
	}

	/**
	 * TEST NAME:  Test Creating ContinueRecords After Unknown Records From An InputStream <P>
	 * OBJECTIVE:  Test that the RecordFactory given an InputStream
	 *             constructs the expected array of records.<P>
	 * SUCCESS:	Record factory creates the expected records.<P>
	 * FAILURE:	The wrong records are created or contain the wrong values <P>
	 */
	public void testContinuedUnknownRecord() {
		byte[] data = {
			0, -1, 0, 0, // an unknown record with 0 length
			0x3C , 0, 3, 0, 1, 2, 3, // a continuation record with 3 bytes of data
			0x3C , 0, 1, 0, 4 // one more continuation record with 1 byte of data
		};

		ByteArrayInputStream bois = new ByteArrayInputStream(data);
		Record[] records = RecordFactory.createRecords(bois).toArray(new Record[0]);
		assertEquals("Created record count", 3, records.length);
		assertEquals("1st record's type",
					 UnknownRecord.class.getName(),
					 records[ 0 ].getClass().getName());
		assertEquals("1st record's sid", (short)-256, records[0].getSid());
		assertEquals("2nd record's type",
					 ContinueRecord.class.getName(),
					 records[ 1 ].getClass().getName());
		ContinueRecord record = (ContinueRecord) records[1];
		assertEquals("2nd record's sid", 0x3C, record.getSid());
		assertEquals("1st data byte", 1, record.getData()[ 0 ]);
		assertEquals("2nd data byte", 2, record.getData()[ 1 ]);
		assertEquals("3rd data byte", 3, record.getData()[ 2 ]);
		assertEquals("3rd record's type",
					 ContinueRecord.class.getName(),
					 records[ 2 ].getClass().getName());
		record = (ContinueRecord) records[2];
		assertEquals("3nd record's sid", 0x3C, record.getSid());
		assertEquals("4th data byte", 4, record.getData()[ 0 ]);
	}

	/**
	 * Drawing records have a very strange continue behaviour.
	 * There can actually be OBJ records mixed between the continues.
	 * Record factory must preserve this structure when reading records.
	 */
	public void testMixedContinue() throws Exception {
		/**
		 *  Adapted from a real test sample file 39512.xls (Offset 0x4854).
		 *  See Bug 39512 for details.
		 */
		String dump =
				//OBJ
				"5D 00 48 00 15 00 12 00 0C 00 3C 00 11 00 A0 2E 03 01 CC 42 " +
				"CF 00 00 00 00 00 0A 00 0C 00 00 00 00 00 00 00 00 00 00 00 " +
				"03 00 0B 00 06 00 28 01 03 01 00 00 12 00 08 00 00 00 00 00 " +
				"00 00 03 00 11 00 04 00 3D 00 00 00 00 00 00 00 " +
				 //MSODRAWING
				"EC 00 08 00 00 00 0D F0 00 00 00 00 " +
				//TXO (and 2 trailing CONTINUE records)
				"B6 01 12 00 22 02 00 00 00 00 00 00 00 00 10 00 10 00 00 00 00 00 " +
				"3C 00 11 00 00 4F 70 74 69 6F 6E 20 42 75 74 74 6F 6E 20 33 39 " +
				"3C 00 10 00 00 00 05 00 00 00 00 00 10 00 00 00 00 00 00 00 " +
				// another CONTINUE
				"3C 00 7E 00 0F 00 04 F0 7E 00 00 00 92 0C 0A F0 08 00 00 00 " +
				"3D 04 00 00 00 0A 00 00 A3 00 0B F0 3C 00 00 00 7F 00 00 01 " +
				"00 01 80 00 8C 01 03 01 85 00 01 00 00 00 8B 00 02 00 00 00 " +
				"BF 00 08 00 1A 00 7F 01 29 00 29 00 81 01 41 00 00 08 BF 01 " +
				"00 00 10 00 C0 01 40 00 00 08 FF 01 00 00 08 00 00 00 10 F0 " +
				"12 00 00 00 02 00 02 00 A0 03 18 00 B5 00 04 00 30 02 1A 00 " +
				"00 00 00 00 11 F0 00 00 00 00 " +
				//OBJ
				"5D 00 48 00 15 00 12 00 0C 00 3D 00 11 00 8C 01 03 01 C8 59 CF 00 00 " +
				"00 00 00 0A 00 0C 00 00 00 00 00 00 00 00 00 00 00 03 00 0B 00 06 00 " +
				"7C 16 03 01 00 00 12 00 08 00 00 00 00 00 00 00 03 00 11 00 04 00 01 " +
				"00 00 00 00 00 00 00";
		byte[] data = HexRead.readFromString(dump);

		List records = RecordFactory.createRecords(new ByteArrayInputStream(data));
		assertEquals(5, records.size());
		assertTrue(records.get(0) instanceof ObjRecord);
		assertTrue(records.get(1) instanceof DrawingRecord);
		assertTrue(records.get(2) instanceof TextObjectRecord);
		assertTrue(records.get(3) instanceof ContinueRecord);
		assertTrue(records.get(4) instanceof ObjRecord);

		//serialize and verify that the serialized data is the same as the original
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for(Iterator it = records.iterator(); it.hasNext(); ){
			Record rec = (Record)it.next();
			out.write(rec.serialize());
		}

		byte[] ser = out.toByteArray();
		assertEquals(data.length, ser.length);
		assertTrue(Arrays.equals(data, ser));
	}

	public void testNonZeroPadding_bug46987() {
		Record[] recs = {
			new BOFRecord(),
			new WriteAccessRecord(), // need *something* between BOF and EOF
			EOFRecord.instance,
			BOFRecord.createSheetBOF(),
			EOFRecord.instance,
		};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i = 0; i < recs.length; i++) {
			try {
				baos.write(recs[i].serialize());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		//simulate the bad padding at the end of the workbook stream in attachment 23483 of bug 46987
		baos.write(0x00);
		baos.write(0x11);
		baos.write(0x00);
		baos.write(0x02);
		for (int i = 0; i < 192; i++) {
			baos.write(0x00);
		}


		POIFSFileSystem fs = new POIFSFileSystem();
		InputStream is;
		try {
			fs.createDocument(new ByteArrayInputStream(baos.toByteArray()), "dummy");
			is = fs.getRoot().createDocumentInputStream("dummy");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<Record> outRecs;
		try {
			outRecs = RecordFactory.createRecords(is);
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Buffer underrun - requested 512 bytes but 192 was available")) {
				throw new AssertionFailedError("Identified bug 46987");
			}
			throw e;
		}
		assertEquals(5, outRecs.size());
	}
}
