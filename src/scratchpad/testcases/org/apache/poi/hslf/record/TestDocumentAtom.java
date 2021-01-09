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


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests that DocumentAtom works properly
 */
public final class TestDocumentAtom {
	// From a real file
	private final byte[] data_a = { 1, 0, 0xE9-256, 3, 0x28, 0, 0, 0,
		0x80-256, 0x16, 0, 0, 0xE0-256, 0x10, 0, 0,
		0xE0-256, 0x10, 0, 0, 0x80-256, 0x16, 0, 0,
		0x05, 0, 0, 0, 0x0A, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
		1, 0, 0, 0, 0, 0, 0, 1 };

	@Test
    void testRecordType() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(1001l, da.getRecordType());
	}

	@Test
	void testSizeAndZoom() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(5760L, da.getSlideSizeX());
		assertEquals(4320L, da.getSlideSizeY());
		assertEquals(4320L, da.getNotesSizeX());
		assertEquals(5760L, da.getNotesSizeY());

		assertEquals(5L, da.getServerZoomFrom());
		assertEquals(10L, da.getServerZoomTo());
	}

	@Test
	void testMasterPersist() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(2L, da.getNotesMasterPersist());
		assertEquals(0L, da.getHandoutMasterPersist());
	}

	@Test
	void testSlideDetails() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		assertEquals(1, da.getFirstSlideNum());
		assertEquals(0, da.getSlideSizeType());
	}

	@Test
	void testBooleans() {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
        assertFalse(da.getSaveWithFonts());
        assertFalse(da.getOmitTitlePlace());
        assertFalse(da.getRightToLeft());
        assertTrue(da.getShowComments());
	}

	@Test
	void testWrite() throws Exception {
		DocumentAtom da = new DocumentAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		da.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}
}
