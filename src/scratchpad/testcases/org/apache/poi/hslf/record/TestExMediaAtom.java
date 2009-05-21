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

package org.apache.poi.hslf.record;


import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Tests that {@link org.apache.poi.hslf.record.HeadersFootersAtom} works properly
 *
 * @author Yegor Kozlov
 */
public final class TestExMediaAtom extends TestCase {
	// From a real file
	private static final byte[] data = {
			0x00, 0x00, (byte)0x04, 0x10, 0x08, 0x00, 0x00, 00,
			0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

	public void testRead() {
		ExMediaAtom record = new ExMediaAtom(data, 0, data.length);
		assertEquals(RecordTypes.ExMediaAtom.typeID, record.getRecordType());

		assertEquals(1, record.getObjectId());
		assertFalse(record.getFlag(ExMediaAtom.fLoop));
		assertFalse(record.getFlag(ExMediaAtom.fNarration));
		assertFalse(record.getFlag(ExMediaAtom.fRewind));
	}

	public void testWrite() throws Exception {
		ExMediaAtom record = new ExMediaAtom(data, 0, data.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertTrue(Arrays.equals(data, b));
	}

	public void testNewRecord() throws Exception {
		ExMediaAtom ref = new ExMediaAtom(data, 0, data.length);
		assertEquals(0, ref.getMask()); //

		ExMediaAtom record = new ExMediaAtom();
		record.setObjectId(1);
		record.setFlag(HeadersFootersAtom.fHasDate, false);
		record.setFlag(HeadersFootersAtom.fHasTodayDate, false);
		record.setFlag(HeadersFootersAtom.fHasFooter, false);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertTrue(Arrays.equals(data, b));
	}

	public void testFlags() {
		ExMediaAtom record = new ExMediaAtom();

		//in a new record all the bits are 0
		for(int i = 0; i < 3; i++) assertFalse(record.getFlag(1 << i));

		record.setFlag(ExMediaAtom.fLoop, true);
		assertTrue(record.getFlag(ExMediaAtom.fLoop));

		record.setFlag(ExMediaAtom.fNarration, true);
		assertTrue(record.getFlag(ExMediaAtom.fNarration));

		record.setFlag(ExMediaAtom.fNarration, false);
		assertFalse(record.getFlag(ExMediaAtom.fNarration));

		record.setFlag(ExMediaAtom.fNarration, false);
		assertFalse(record.getFlag(ExMediaAtom.fNarration));

	}
}
