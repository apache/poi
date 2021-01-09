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
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Tests that TextBytesAtom works properly
 */
public final class TestTextBytesAtom {
	// From a real file
	private final byte[] data = { 0, 0, 0xA8-256, 0x0f, 0x1c, 0, 0, 0,
		0x54, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x74, 0x68,
		0x65, 0x20, 0x74, 0x69, 0x74, 0x6C,	0x65, 0x20, 0x6F, 0x6E,
		0x20, 0x70, 0x61, 0x67, 0x65, 0x20, 0x32 };
	private final byte[] alt_data = { 0, 0, 0xA8-256, 0x0F, 0x14, 0, 0, 0,
		0x54, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20,
		0x74, 0x65, 0x73, 0x74, 0x20, 0x74, 0x69, 0x74, 0x6C, 0x65 };
	private final String alt_text = "This is a test title";

	@Test
    void testRecordType() {
		TextBytesAtom tba = new TextBytesAtom(data,0,data.length);
		assertEquals(4008L, tba.getRecordType());
	}

	@Test
	void testTextA() {
		TextBytesAtom tba = new TextBytesAtom(data,0,data.length);
		String data_text = "This is the title on page 2";
		assertEquals(data_text, tba.getText());
	}

	@Test
	void testTextB() {
		TextBytesAtom tba = new TextBytesAtom(alt_data,0,alt_data.length);
		assertEquals(alt_text, tba.getText());
	}

	@Test
	void testChangeText() throws Exception {
		TextBytesAtom tba = new TextBytesAtom(data,0,data.length);
		tba.setText(alt_text.getBytes(StandardCharsets.ISO_8859_1));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tba.writeOut(baos);
		assertArrayEquals(alt_data, baos.toByteArray());
	}

	@Test
	void testWrite() throws Exception {
		TextBytesAtom tba = new TextBytesAtom(data,0,data.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tba.writeOut(baos);
		assertArrayEquals(data, baos.toByteArray());
	}
}
