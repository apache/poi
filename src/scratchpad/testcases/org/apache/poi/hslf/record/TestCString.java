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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests that CString works properly
 */
public final class TestCString {
	// From a real file
	private final byte[] data_a = { 0, 0, 0xBA-256, 0x0f, 0x10, 0, 0, 0,
		0x48, 0, 0x6F, 0, 0x67, 0, 0x77, 0,
		0x61, 0, 0x72, 0, 0x74, 0, 0x73, 0  };
	private final byte[] data_b = new byte[] { 0x10, 0, 0xBA-256, 0x0f, 0x10, 0, 0, 0,
		0x43, 0, 0x6F, 0, 0x6D, 0, 0x6D, 0,
		0x65, 0, 0x6E, 0, 0x74, 0, 0x73, 0 };

	@Test
    void testRecordType() {
		CString ca = new CString(data_a, 0, data_a.length);
		assertEquals(4026L, ca.getRecordType());
		CString cb = new CString(data_b, 0, data_a.length);
		assertEquals(4026L, cb.getRecordType());
	}

	@Test
	void testCount() {
		CString ca = new CString(data_a, 0, data_a.length);
		assertEquals(0, ca.getOptions());
		CString cb = new CString(data_b, 0, data_a.length);
		assertEquals(0x10, cb.getOptions());

		ca.setOptions(28);
		assertEquals(28, ca.getOptions());
	}

	@Test
	void testText() {
		CString ca = new CString(data_a, 0, data_a.length);
		assertEquals("Hogwarts", ca.getText());
		CString cb = new CString(data_b, 0, data_a.length);
		assertEquals("Comments", cb.getText());

		ca.setText("FooBar");
		assertEquals("FooBar", ca.getText());
	}

	@Test
	void testWrite() throws Exception {
		CString ca = new CString(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ca.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}

		CString cb = new CString(data_b, 0, data_a.length);
		ByteArrayOutputStream baosB = new ByteArrayOutputStream();
		cb.writeOut(baosB);
		b = baosB.toByteArray();

		assertEquals(data_b.length, b.length);
		for(int i=0; i<data_b.length; i++) {
			assertEquals(data_b[i],b[i]);
		}
	}

	// Turn data_a into data_b
	@Test
	void testChange() throws Exception {
		CString ca = new CString(data_a, 0, data_a.length);
		ca.setText("Comments");
		ca.setOptions(0x10);

		// Check bytes weren't the same
		boolean equals = true;
		for(int i=0; i<data_a.length; i++) {
			if (data_a[i] != data_b[i]) {
				equals = false;
				break;
			}
		}
		assertFalse(equals, "Arrays should not be equals");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ca.writeOut(baos);
		byte[] b = baos.toByteArray();

		// Should now be the same
		assertEquals(data_b.length, b.length);
		for(int i=0; i<data_b.length; i++) {
			assertEquals(data_b[i],b[i]);
		}
	}
}
