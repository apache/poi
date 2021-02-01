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

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests that NotesAtom works properly
 */
public final class TestNotesAtom {
	// From a real file
	private final byte[] data_a = new byte[] { 1, 0, 0xF1-256, 3, 8, 0, 0, 0,
		0, 0, 0, 0x80-256, 0, 0, 0x0D, 0x30 };

	@Test
	void testRecordType() {
		NotesAtom na = new NotesAtom(data_a, 0, data_a.length);
		assertEquals(1009L, na.getRecordType());
	}

	@Test
	void testFlags() {
		NotesAtom na = new NotesAtom(data_a, 0, data_a.length);
		assertEquals(0x80000000, na.getSlideID());
        assertFalse(na.getFollowMasterObjects());
        assertFalse(na.getFollowMasterScheme());
        assertFalse(na.getFollowMasterBackground());
	}

	@Test
	void testWrite() throws Exception {
		NotesAtom na = new NotesAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		na.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}
}
