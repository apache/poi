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

/**
 * Tests that DocumentAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestDocumentAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] { 1, 0, 0xE9-256, 3, 0x28, 0, 0, 0,
		0x80-256, 0x16, 0, 0, 0xE0-256, 0x10, 0, 0,
		0xE0-256, 0x10, 0, 0, 0x80-256, 0x16, 0, 0,
		0x05, 0, 0, 0, 0x0A, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
		1, 0, 0, 0, 0, 0, 0, 1 };

    public void testRecordType() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(1001l, da.getRecordType());
	}
	public void testSizeAndZoom() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(5760l, da.getSlideSizeX());
		assertEquals(4320l, da.getSlideSizeY());
		assertEquals(4320l, da.getNotesSizeX());
		assertEquals(5760l, da.getNotesSizeY());

		assertEquals(5l, da.getServerZoomFrom());
		assertEquals(10l, da.getServerZoomTo());
	}
	public void testMasterPersist() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(2l, da.getNotesMasterPersist());
		assertEquals(0l, da.getHandoutMasterPersist());
	}
	public void testSlideDetails() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(1, da.getFirstSlideNum());
		assertEquals(0, da.getSlideSizeType());
	}
	public void testBooleans() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(false, da.getSaveWithFonts());
		assertEquals(false, da.getOmitTitlePlace());
		assertEquals(false, da.getRightToLeft());
		assertEquals(true, da.getShowComments());
	}

	public void testWrite() throws Exception {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		da.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}
}
