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
 * Tests that TextBytesAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestTextBytesAtom extends TestCase {
	// From a real file
	private byte[] data = new byte[]  { 0, 0, 0xA8-256, 0x0f, 0x1c, 0, 0, 0,
		0x54, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x74, 0x68,
		0x65, 0x20, 0x74, 0x69, 0x74, 0x6C,	0x65, 0x20, 0x6F, 0x6E,
		0x20, 0x70, 0x61, 0x67, 0x65, 0x20, 0x32 };
	private String data_text = "This is the title on page 2";
	private byte[] alt_data = new byte[] { 0, 0, 0xA8-256, 0x0F, 0x14, 0, 0, 0,
		0x54, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20,
		0x74, 0x65, 0x73, 0x74, 0x20, 0x74, 0x69, 0x74, 0x6C, 0x65 };
	private String alt_text = "This is a test title";

    public void testRecordType() {
		TextBytesAtom tba = new TextBytesAtom(data,0,data.length);
		assertEquals(4008l, tba.getRecordType());
	}

	public void testTextA() {
		TextBytesAtom tba = new TextBytesAtom(data,0,data.length);
		assertEquals(data_text, tba.getText());
	}
	public void testTextB() {
		TextBytesAtom tba = new TextBytesAtom(alt_data,0,alt_data.length);
		assertEquals(alt_text, tba.getText());
	}

	public void testChangeText() throws Exception {
		TextBytesAtom tba = new TextBytesAtom(data,0,data.length);
		tba.setText(alt_text.getBytes("ISO-8859-1"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tba.writeOut(baos);
		byte[] b = baos.toByteArray();

		// Compare the header and the text
		assertEquals(alt_data.length, b.length);
		for(int i=0; i<alt_data.length; i++) {
			assertEquals(alt_data[i],b[i]);
		}
	}

	public void testWrite() throws Exception {
		TextBytesAtom tba = new TextBytesAtom(data,0,data.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tba.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data.length, b.length);
		for(int i=0; i<data.length; i++) {
			assertEquals(data[i],b[i]);
		}
	}
}
