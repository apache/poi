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

import java.io.ByteArrayOutputStream;

import org.apache.poi.sl.usermodel.TextShape.TextPlaceholder;
import org.junit.jupiter.api.Test;

/**
 * Tests that TextHeaderAtom works properly
 */
public final class TestTextHeaderAtom {
	// From a real file
	private final byte[] notes_data = { 0, 0, 0x9f-256, 0x0f, 4, 0, 0, 0, 2, 0, 0, 0 };
	private final byte[] title_data = { 0, 0, 0x9f-256, 0x0f, 4, 0, 0, 0, 0, 0, 0, 0 };
	private final byte[] body_data = { 0, 0, 0x9f-256, 0x0f, 4, 0, 0, 0, 1, 0, 0, 0 };

	@Test
	void testRecordType() {
		TextHeaderAtom tha = new TextHeaderAtom(notes_data,0,12);
		assertEquals(3999L, tha.getRecordType());
	}

	@Test
	void testTypes() {
		TextHeaderAtom n_tha = new TextHeaderAtom(notes_data,0,12);
		TextHeaderAtom t_tha = new TextHeaderAtom(title_data,0,12);
		TextHeaderAtom b_tha = new TextHeaderAtom(body_data,0,12);
		assertEquals(TextPlaceholder.NOTES.nativeId, n_tha.getTextType());
		assertEquals(TextPlaceholder.TITLE.nativeId, t_tha.getTextType());
		assertEquals(TextPlaceholder.BODY.nativeId, b_tha.getTextType());
	}

	@Test
	void testWrite() throws Exception {
		TextHeaderAtom tha = new TextHeaderAtom(notes_data,0,12);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tha.writeOut(baos);
		assertArrayEquals(notes_data, baos.toByteArray());
	}
}
