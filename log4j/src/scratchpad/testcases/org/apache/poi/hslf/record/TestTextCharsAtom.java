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

import org.junit.jupiter.api.Test;

/**
 * Tests that TextCharsAtom works properly
 */
public final class TestTextCharsAtom {
	// From a real file
	private final byte[] data = new byte[]  { 0, 0, 0xA0-256, 0x0f, 0x08, 0, 0, 0,
		0x54, 0x00, 0x68, 0x00, 0x69, 0x00, 0x73, 0x00 };
	private final String data_text = "This";
	private final byte[] alt_data = new byte[] { 0, 0, 0xA0-256, 0x0F, 0x0a, 0, 0, 0,
		0x54, 0x00, 0x68, 0x00, 0x69, 0x00, 0x73, 0x00, 0xa3-256, 0x01 };
	private final String alt_text = "This\u01A3";

	@Test
    void testRecordType() {
		TextCharsAtom tca = new TextCharsAtom(data,0,data.length);
		assertEquals(4000L, tca.getRecordType());
	}

	@Test
	void testTextA() {
		TextCharsAtom tca = new TextCharsAtom(data,0,data.length);
		assertEquals(data_text, tca.getText());
	}

	@Test
	void testTextB() {
		TextCharsAtom tca = new TextCharsAtom(alt_data,0,alt_data.length);
		assertEquals(alt_text, tca.getText());
	}

	@Test
	void testChangeText() throws Exception {
		TextCharsAtom tca = new TextCharsAtom(data,0,data.length);
		tca.setText(alt_text);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tca.writeOut(baos);
		assertArrayEquals(alt_data, baos.toByteArray());
	}

	@Test
	void testWrite() throws Exception {
		TextCharsAtom tca = new TextCharsAtom(data,0,data.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tca.writeOut(baos);
		assertArrayEquals(data, baos.toByteArray());
	}

	@Test
	void testCreateNew() throws Exception {
		TextCharsAtom tca = new TextCharsAtom();
		assertEquals(0, tca.getText().length());

		tca.setText(data_text);
		assertEquals(data_text, tca.getText());

		// Check it's now like data
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tca.writeOut(baos);
		assertArrayEquals(data, baos.toByteArray());
	}

}
