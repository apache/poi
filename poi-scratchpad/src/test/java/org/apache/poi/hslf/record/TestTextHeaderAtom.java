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
 * Tests that TextHeaderAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestTextHeaderAtom extends TestCase {
	// From a real file
	private byte[] notes_data = new byte[] { 0, 0, 0x9f-256, 0x0f, 4, 0, 0, 0, 2, 0, 0, 0};
	private byte[] title_data = new byte[] { 0, 0, 0x9f-256, 0x0f, 4, 0, 0, 0, 0, 0, 0, 0 };
	private byte[] body_data = new byte[]  { 0, 0, 0x9f-256, 0x0f, 4, 0, 0, 0, 1, 0, 0, 0 };

	public void testRecordType() {
		TextHeaderAtom tha = new TextHeaderAtom(notes_data,0,12);
		assertEquals(3999l, tha.getRecordType());
	}
	public void testTypes() {
		TextHeaderAtom n_tha = new TextHeaderAtom(notes_data,0,12);
		TextHeaderAtom t_tha = new TextHeaderAtom(title_data,0,12);
		TextHeaderAtom b_tha = new TextHeaderAtom(body_data,0,12);
		assertEquals(TextHeaderAtom.NOTES_TYPE, n_tha.getTextType());
		assertEquals(TextHeaderAtom.TITLE_TYPE, t_tha.getTextType());
		assertEquals(TextHeaderAtom.BODY_TYPE, b_tha.getTextType());
	}

	public void testWrite() throws Exception {
		TextHeaderAtom tha = new TextHeaderAtom(notes_data,0,12);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tha.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(notes_data.length, b.length);
		for(int i=0; i<notes_data.length; i++) {
			assertEquals(notes_data[i],b[i]);
		}
	}
}
