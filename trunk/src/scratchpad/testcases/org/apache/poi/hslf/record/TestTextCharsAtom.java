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
 * Tests that TextCharsAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestTextCharsAtom extends TestCase {
	// From a real file
	private byte[] data = new byte[]  { 0, 0, 0xA0-256, 0x0f, 0x08, 0, 0, 0,
		0x54, 0x00, 0x68, 0x00, 0x69, 0x00, 0x73, 0x00 };
	private String data_text = "This";
	private byte[] alt_data = new byte[] { 0, 0, 0xA0-256, 0x0F, 0x0a, 0, 0, 0,
		0x54, 0x00, 0x68, 0x00, 0x69, 0x00, 0x73, 0x00, 0xa3-256, 0x01 };
	private String alt_text = "This\u01A3";

    public void testRecordType() {
		TextCharsAtom tca = new TextCharsAtom(data,0,data.length);
		assertEquals(4000l, tca.getRecordType());
	}

	public void testTextA() {
		TextCharsAtom tca = new TextCharsAtom(data,0,data.length);
		assertEquals(data_text, tca.getText());
	}
	public void testTextB() {
		TextCharsAtom tca = new TextCharsAtom(alt_data,0,alt_data.length);
		assertEquals(alt_text, tca.getText());
	}

	public void testChangeText() throws Exception {
		TextCharsAtom tca = new TextCharsAtom(data,0,data.length);
		tca.setText(alt_text);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tca.writeOut(baos);
		byte[] b = baos.toByteArray();

		// Compare the header and the text
		assertEquals(alt_data.length, b.length);
		for(int i=0; i<alt_data.length; i++) {
			assertEquals(alt_data[i],b[i]);
		}
	}

	public void testWrite() throws Exception {
		TextCharsAtom tca = new TextCharsAtom(data,0,data.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tca.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data.length, b.length);
		for(int i=0; i<data.length; i++) {
			assertEquals(data[i],b[i]);
		}
	}

	public void testCreateNew() throws Exception {
		TextCharsAtom tca = new TextCharsAtom();
		assertEquals(0, tca.getText().length());

		tca.setText(data_text);
		assertEquals(data_text, tca.getText());

		// Check it's now like data
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tca.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data.length, b.length);
		for(int i=0; i<data.length; i++) {
			assertEquals(data[i],b[i]);
		}
	}

}
