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
 * Tests that TxInteractiveInfoAtom works properly.
 *
 * @author Yegor Kozlov
 */
public final class TestTxInteractiveInfoAtom extends TestCase {
	// From WithLinks.ppt
	private byte[] data_a = new byte[] {
		00, 00, (byte)0xDF, 0x0F, 0x08, 00, 00, 00,
		0x19, 00, 00, 00, 0x38, 00, 00, 00
	};

	private byte[] data_b = new byte[] {
		00, 00, (byte)0xDF, 0x0F, 0x08, 00, 00, 00,
		0x39, 00, 00, 00, 0x4E, 00, 00, 00
	};

	public void testRead() {
		TxInteractiveInfoAtom ia1 = new TxInteractiveInfoAtom(data_a, 0, data_a.length);

		assertEquals(4063, ia1.getRecordType());
		assertEquals(25, ia1.getStartIndex());
		assertEquals(56, ia1.getEndIndex());

		TxInteractiveInfoAtom ia2 = new TxInteractiveInfoAtom(data_b, 0, data_b.length);

		assertEquals(4063, ia2.getRecordType());
		assertEquals(57, ia2.getStartIndex());
		assertEquals(78, ia2.getEndIndex());
	}

	public void testWrite() throws Exception {
		TxInteractiveInfoAtom atom = new TxInteractiveInfoAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		atom.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	// Create A from scratch
	public void testCreate() throws Exception {
		TxInteractiveInfoAtom ia = new TxInteractiveInfoAtom();

		// Set values
		ia.setStartIndex(25);
		ia.setEndIndex(56);

		// Check it's now the same as a
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	// Try to turn a into b
	public void testChange() throws Exception {
		TxInteractiveInfoAtom ia = new TxInteractiveInfoAtom(data_a, 0, data_a.length);

		// Change the number
		ia.setStartIndex(57);
		ia.setEndIndex(78);

		// Check bytes are now the same
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		byte[] b = baos.toByteArray();

		// Should now be the same
		assertEquals(data_b.length, b.length);
		for(int i=0; i<data_b.length; i++) {
			assertEquals(data_b[i],b[i]);
		}
	}
}
