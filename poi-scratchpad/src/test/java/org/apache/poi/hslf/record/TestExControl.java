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
 * Tests that {@link org.apache.poi.hslf.record.ExControl} works properly
 *
 * @author Yegor Kozlov
 */
public final class TestExControl extends TestCase {

	// From a real file (embedded SWF control)
    /*
     <ExControl info="15" type="4078" size="218" offset="76" header="0F 00 EE 0F DA 00 00 00 ">
       <ExControlAtom info="0" type="4091" size="4" offset="84" header="00 00 FB 0F 04 00 00 00 ">
         00 01 00 00
       </ExControlAtom>
       <ExOleObjAtom info="1" type="4035" size="24" offset="96" header="01 00 C3 0F 18 00 00 00 ">
         01 00 00 00 02 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 00 96 13 00
       </ExOleObjAtom>
       <CString info="16" type="4026" size="44" offset="128" header="10 00 BA 0F 2C 00 00 00 ">
         53 00 68 00 6F 00 63 00 6B 00 77 00 61 00 76 00 65 00 20 00 46 00 6C 00 61
         00 73 00 68 00 20 00 4F 00 62 00 6A 00 65 00 63 00 74 00
       </CString>
       <CString info="32" type="4026" size="62" offset="180" header="20 00 BA 0F 3E 00 00 00 ">
         53 00 68 00 6F 00 63 00 6B 00 77 00 61 00 76 00 65 00 46 00 6C 00 61 00 73
         00 68 00 2E 00 53 00 68 00 6F 00 63 00 6B 00 77 00 61 00 76 00 65 00 46 00
         6C 00 61 00 73 00 68 00 2E 00 39 00
       </CString>
       <CString info="48" type="4026" size="44" offset="250" header="30 00 BA 0F 2C 00 00 00 ">
         53 00 68 00 6F 00 63 00 6B 00 77 00 61 00 76 00 65 00 20 00 46 00 6C 00 61
         00 73 00 68 00 20 00 4F 00 62 00 6A 00 65 00 63 00 74 00
       </CString>
     </ExControl>
     */
    private byte[] data = new byte[] {
            0x0F, 0x00, (byte)0xEE, 0x0F, (byte)0xDA, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xFB, 0x0F, 0x04, 0x00, 0x00, 0x00,
            0x00, 0x01, 0x00, 0x00, 0x01, 0x00, (byte)0xC3, 0x0F, 0x18, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x02, 0x00,
            0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, (byte)0x96, 0x13, 0x00,
            0x10, 0x00, (byte)0xBA, 0x0F, 0x2C, 0x00, 0x00, 0x00, 0x53, 0x00, 0x68, 0x00, 0x6F, 0x00, 0x63, 0x00, 0x6B, 0x00,
            0x77, 0x00, 0x61, 0x00, 0x76, 0x00, 0x65, 0x00, 0x20, 0x00, 0x46, 0x00, 0x6C, 0x00, 0x61, 0x00, 0x73, 0x00, 0x68,
            0x00, 0x20, 0x00, 0x4F, 0x00, 0x62, 0x00, 0x6A, 0x00, 0x65, 0x00, 0x63, 0x00, 0x74, 0x00, 0x20, 0x00, (byte)0xBA,
            0x0F, 0x3E, 0x00, 0x00, 0x00, 0x53, 0x00, 0x68, 0x00, 0x6F, 0x00, 0x63, 0x00, 0x6B, 0x00, 0x77, 0x00, 0x61, 0x00,
            0x76, 0x00, 0x65, 0x00, 0x46, 0x00, 0x6C, 0x00, 0x61, 0x00, 0x73, 0x00, 0x68, 0x00, 0x2E, 0x00, 0x53, 0x00, 0x68,
            0x00, 0x6F, 0x00, 0x63, 0x00, 0x6B, 0x00, 0x77, 0x00, 0x61, 0x00, 0x76, 0x00, 0x65, 0x00, 0x46, 0x00, 0x6C, 0x00,
            0x61, 0x00, 0x73, 0x00, 0x68, 0x00, 0x2E, 0x00, 0x39, 0x00, 0x30, 0x00, (byte)0xBA, 0x0F, 0x2C, 0x00, 0x00, 0x00,
            0x53, 0x00, 0x68, 0x00, 0x6F, 0x00, 0x63, 0x00, 0x6B, 0x00, 0x77, 0x00, 0x61, 0x00, 0x76, 0x00, 0x65, 0x00, 0x20,
            0x00, 0x46, 0x00, 0x6C, 0x00, 0x61, 0x00, 0x73, 0x00, 0x68, 0x00, 0x20, 0x00, 0x4F, 0x00, 0x62, 0x00, 0x6A, 0x00,
            0x65, 0x00, 0x63, 0x00, 0x74, 0x00
    };

	public void testRead() {
		ExControl record = new ExControl(data, 0, data.length);
		assertEquals(RecordTypes.ExControl.typeID, record.getRecordType());

		assertNotNull(record.getExControlAtom());
		assertEquals(256, record.getExControlAtom().getSlideId());

		ExOleObjAtom oleObj = record.getExOleObjAtom();
		assertNotNull(oleObj);
		assertEquals(oleObj.getDrawAspect(), ExOleObjAtom.DRAW_ASPECT_VISIBLE);
		assertEquals(oleObj.getType(), ExOleObjAtom.TYPE_CONTROL);
		assertEquals(oleObj.getSubType(), ExOleObjAtom.SUBTYPE_DEFAULT);

		assertEquals("Shockwave Flash Object", record.getMenuName());
		assertEquals("ShockwaveFlash.ShockwaveFlash.9", record.getProgId());
		assertEquals("Shockwave Flash Object", record.getClipboardName());
	}

	public void testWrite() throws Exception {
		ExControl record = new ExControl(data, 0, data.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertTrue(Arrays.equals(data, b));
	}

	public void testNewRecord() throws Exception {
		ExControl record = new ExControl();
		ExControlAtom ctrl = record.getExControlAtom();
		ctrl.setSlideId(256);

		ExOleObjAtom oleObj = record.getExOleObjAtom();
		oleObj.setDrawAspect(ExOleObjAtom.DRAW_ASPECT_VISIBLE);
		oleObj.setType(ExOleObjAtom.TYPE_CONTROL);
		oleObj.setObjID(1);
		oleObj.setSubType(ExOleObjAtom.SUBTYPE_DEFAULT);
		oleObj.setObjStgDataRef(2);
		oleObj.setOptions(1283584);

		record.setMenuName("Shockwave Flash Object");
		record.setProgId("ShockwaveFlash.ShockwaveFlash.9");
		record.setClipboardName("Shockwave Flash Object");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data.length, b.length);
		assertTrue(Arrays.equals(data, b));
	}
}
