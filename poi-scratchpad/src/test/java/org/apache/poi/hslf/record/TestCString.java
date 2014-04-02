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
 * Tests that CString works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestCString extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] { 0, 0, 0xBA-256, 0x0f, 0x10, 0, 0, 0,
		0x48, 00, 0x6F, 00, 0x67, 00, 0x77, 00,
		0x61, 00, 0x72, 00, 0x74, 00, 0x73, 00  };
	private byte[] data_b = new byte[] { 0x10, 0, 0xBA-256, 0x0f, 0x10, 0, 0, 0,
		0x43, 00, 0x6F, 00, 0x6D, 00, 0x6D, 00,
		0x65, 00, 0x6E, 00, 0x74, 00, 0x73, 00 };

    public void testRecordType() {
		CString ca = new CString(data_a, 0, data_a.length);
		assertEquals(4026l, ca.getRecordType());
		CString cb = new CString(data_b, 0, data_a.length);
		assertEquals(4026l, cb.getRecordType());
	}
	public void testCount() {
		CString ca = new CString(data_a, 0, data_a.length);
		assertEquals(0, ca.getOptions());
		CString cb = new CString(data_b, 0, data_a.length);
		assertEquals(0x10, cb.getOptions());

		ca.setOptions(28);
		assertEquals(28, ca.getOptions());
	}

	public void testText() {
		CString ca = new CString(data_a, 0, data_a.length);
		assertEquals("Hogwarts", ca.getText());
		CString cb = new CString(data_b, 0, data_a.length);
		assertEquals("Comments", cb.getText());

		ca.setText("FooBar");
		assertEquals("FooBar", ca.getText());
	}

	public void testWrite() throws Exception {
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
	public void testChange() throws Exception {
		CString ca = new CString(data_a, 0, data_a.length);
		ca.setText("Comments");
		ca.setOptions(0x10);

		try {
			for(int i=0; i<data_a.length; i++) {
				assertEquals(data_a[i],data_b[i]);
			}
			fail();
		} catch(Error e) {
			// Good, they're not the same
		}

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
