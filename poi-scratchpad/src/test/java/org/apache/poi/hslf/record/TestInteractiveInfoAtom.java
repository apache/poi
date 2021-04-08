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
 * Tests that InteractiveInfoAtom works properly.
 */
public class TestInteractiveInfoAtom {
	// From a real file
	private final byte[] data_a = new byte[] {
		0, 0, 0xF3-256, 0x0F, 0x10, 0, 0, 0,
		0, 0, 0, 0, 1, 0, 0, 0,
		4, 0, 0, 0, 8, 0, 0, 0
	};
	private final byte[] data_b = new byte[] {
		0, 0, 0xF3-256, 0x0F, 0x10, 0, 0, 0,
		0, 0, 0, 0, 4, 0, 0, 0,
		4, 0, 0, 0, 8, 0, 0, 0
	};

	@Test
	void testRecordType() {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);
		assertEquals(4083L, ia.getRecordType());
	}

	@Test
	void testGetNumber() {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);
		InteractiveInfoAtom ib = new InteractiveInfoAtom(data_b, 0, data_b.length);

		assertEquals(1, ia.getHyperlinkID());
		assertEquals(4, ib.getHyperlinkID());
	}

	@Test
	void testGetRest() {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);
		InteractiveInfoAtom ib = new InteractiveInfoAtom(data_b, 0, data_b.length);

		assertEquals(0, ia.getSoundRef());
		assertEquals(0, ib.getSoundRef());

		assertEquals(4, ia.getAction());
		assertEquals(4, ib.getAction());

		assertEquals(8, ia.getHyperlinkType());
		assertEquals(8, ib.getHyperlinkType());
	}

	@Test
	void testWrite() throws Exception {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

	// Create A from scratch
	@Test
	void testCreate() throws Exception {
		InteractiveInfoAtom ia = new InteractiveInfoAtom();

		// Set values
		ia.setHyperlinkID(1);
		ia.setSoundRef(0);
		ia.setAction((byte)4);
		ia.setHyperlinkType((byte)8);

		// Check it's now the same as a
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

	// Try to turn a into b
	@Test
	void testChange() throws Exception {
		InteractiveInfoAtom ia = new InteractiveInfoAtom(data_a, 0, data_a.length);

		// Change the number
		ia.setHyperlinkID(4);

		// Check bytes are now the same
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ia.writeOut(baos);
		assertArrayEquals(data_b, baos.toByteArray());
	}
}
