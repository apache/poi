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
 * Tests that TxInteractiveInfoAtom works properly.
 */
public final class TestTxInteractiveInfoAtom {
	// From WithLinks.ppt
	private final byte[] data_a = {
		0, 0, (byte)0xDF, 0x0F, 0x08, 0, 0, 0,
		0x19, 0, 0, 0, 0x38, 0, 0, 0
	};

	private final byte[] data_b = {
		0, 0, (byte)0xDF, 0x0F, 0x08, 0, 0, 0,
		0x39, 0, 0, 0, 0x4E, 0, 0, 0
	};

	@Test
	void testRead() {
		TxInteractiveInfoAtom ia1 = new TxInteractiveInfoAtom(data_a, 0, data_a.length);

		assertEquals(4063, ia1.getRecordType());
		assertEquals(25, ia1.getStartIndex());
		assertEquals(56, ia1.getEndIndex());

		TxInteractiveInfoAtom ia2 = new TxInteractiveInfoAtom(data_b, 0, data_b.length);

		assertEquals(4063, ia2.getRecordType());
		assertEquals(57, ia2.getStartIndex());
		assertEquals(78, ia2.getEndIndex());
	}

	@Test
	void testWrite() throws Exception {
		TxInteractiveInfoAtom atom = new TxInteractiveInfoAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		atom.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

	// Create A from scratch
	@Test
	void testCreate() throws Exception {
		TxInteractiveInfoAtom ia = new TxInteractiveInfoAtom();

		// Set values
		ia.setStartIndex(25);
		ia.setEndIndex(56);

		// Check it's now the same as a
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

	// Try to turn a into b
	@Test
	void testChange() throws Exception {
		TxInteractiveInfoAtom ia = new TxInteractiveInfoAtom(data_a, 0, data_a.length);

		// Change the number
		ia.setStartIndex(57);
		ia.setEndIndex(78);

		// Check bytes are now the same
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		assertArrayEquals(data_b, baos.toByteArray());
	}
}
