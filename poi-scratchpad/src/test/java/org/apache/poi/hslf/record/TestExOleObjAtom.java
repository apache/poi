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
 * Tests that {@link ExOleObjAtom} works properly
 *
 * @author Yegor Kozlov
 */
public final class TestExOleObjAtom extends TestCase {
	// From a real file (embedded SWF control)
	private byte[] data = {
			0x01, 0x00, (byte)0xC3, 0x0F, 0x18, 0x00, 0x00, 0x00,
			0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, (byte)0x96, 0x13, 0x00  };

	public void testRead() {
		ExOleObjAtom record = new ExOleObjAtom(data, 0, data.length);
		assertEquals(RecordTypes.ExOleObjAtom.typeID, record.getRecordType());

		assertEquals(record.getDrawAspect(), ExOleObjAtom.DRAW_ASPECT_VISIBLE);
		assertEquals(record.getType(), ExOleObjAtom.TYPE_CONTROL);
		assertEquals(record.getObjID(), 1);
		assertEquals(record.getSubType(), ExOleObjAtom.SUBTYPE_DEFAULT);
		assertEquals(record.getObjStgDataRef(), 2);
		assertEquals(record.getOptions(), 1283584); //ther meaning is unknown
	}

	public void testWrite() throws Exception {
		ExOleObjAtom record = new ExOleObjAtom(data, 0, data.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertTrue(Arrays.equals(data, b));
	}

	public void testNewRecord() throws Exception {
		ExOleObjAtom record = new ExOleObjAtom();
		record.setDrawAspect(ExOleObjAtom.DRAW_ASPECT_VISIBLE);
		record.setType(ExOleObjAtom.TYPE_CONTROL);
		record.setObjID(1);
		record.setSubType(ExOleObjAtom.SUBTYPE_DEFAULT);
		record.setObjStgDataRef(2);
		record.setOptions(1283584);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertTrue(Arrays.equals(data, b));
	}
}
