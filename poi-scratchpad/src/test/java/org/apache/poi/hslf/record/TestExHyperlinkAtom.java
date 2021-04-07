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
 * Tests that ExHyperlinkAtom works properly.
 */
public class TestExHyperlinkAtom {
	// From a real file
	private final byte[] data_a = new byte[] {
		0, 0, 0xD3-256, 0x0F, 4, 0, 0, 0,
		1, 0, 0, 0
	};
	private final byte[] data_b = new byte[] {
		0, 0, 0xD3-256, 0x0F, 4, 0, 0, 0,
		4, 0, 0, 0
	};

	@Test
    void testRecordType() {
    	ExHyperlinkAtom eha = new ExHyperlinkAtom(data_a, 0, data_a.length);
		assertEquals(4051L, eha.getRecordType());
	}

	@Test
    void testGetNumber() {
    	ExHyperlinkAtom eha = new ExHyperlinkAtom(data_a, 0, data_a.length);
    	ExHyperlinkAtom ehb = new ExHyperlinkAtom(data_b, 0, data_b.length);

		assertEquals(1, eha.getNumber());
		assertEquals(4, ehb.getNumber());
    }

	@Test
	void testWrite() throws Exception {
    	ExHyperlinkAtom eha = new ExHyperlinkAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eha.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

	// Create A from scratch
	@Test
	void testCreate() throws Exception {
		ExHyperlinkAtom eha = new ExHyperlinkAtom();

		// Set value
		eha.setNumber(1);

		// Check it's now the same as a
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eha.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

	// Try to turn a into b
	@Test
	void testChange() throws Exception {
		ExHyperlinkAtom eha = new ExHyperlinkAtom(data_a, 0, data_a.length);

		// Change the number
		eha.setNumber(4);

		// Check bytes are now the same
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eha.writeOut(baos);
		assertArrayEquals(data_b, baos.toByteArray());
	}
}
