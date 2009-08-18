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

package org.apache.poi.hdgf.pointers;

import junit.framework.TestCase;

/**
 * Tests for the pointer factory, and the pointers themselves
 */
public final class TestPointerFactory extends TestCase {
	// Type: 16   Addr: 0143aff4  Offset: 80   Len: 54   Format: 46   From: 8a94
	private static byte[] vp6_a = new byte[] {
		22, 0, 0, 0, -12, -81, 67, 1, -128, 0, 0, 0, 84, 0, 0, 0, 70, 0
	};
	// Type: 17   Addr: 014fd84c  Offset: d4   Len: 20   Format: 54   From: 8a94
	private static byte[] vp6_b = new byte[] {
		23, 0, 0, 0, 76, -40, 79, 1, -44, 0, 0, 0, 32, 0, 0, 0, 84, 0
	};
	// Type: 17   Addr: 014fd8bc  Offset: f8   Len: 20   Format: 54   From: 8a94
	private static byte[] vp6_c = new byte[] {
		23, 0, 0, 0, -68, -40, 79, 1, -8, 0, 0, 0, 32, 0, 0, 0, 84, 0
	};
	// Type: ff   Addr: 014fffac  Offset: 0    Len:  0   Format: 60   From: 8a94
	private static byte[] vp6_d = new byte[] {
		-1, 0, 0, 0, -84, -1, 79, 1, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0
	};

	public void testCreateV4() {
		PointerFactory pf = new PointerFactory(4);
		try {
			pf.createPointer(new byte[]{}, 0);
			fail();
		} catch(IllegalArgumentException e) {
			// As expected
		}
	}

	public void testCreateV5() {
		PointerFactory pf = new PointerFactory(5);
		try {
			pf.createPointer(new byte[]{}, 0);
			fail();
		} catch(RuntimeException e) {
			// Still to do
			assertEquals("TODO", e.getMessage());
		}
	}

	public void testCreateV6() {
		PointerFactory pf = new PointerFactory(6);

		Pointer a = pf.createPointer(vp6_a, 0);
		assertEquals(0x16, a.getType());
		assertEquals(0x0143aff4, a.getAddress());
		assertEquals(0x80, a.getOffset());
		assertEquals(0x54, a.getLength());
		assertEquals(0x46, a.getFormat());

		assertTrue(a.destinationCompressed());
		assertTrue(a.destinationHasStrings());
		assertFalse(a.destinationHasChunks());
		assertFalse(a.destinationHasPointers());

		assertEquals(18, a.getSizeInBytes());


		Pointer b = pf.createPointer(vp6_b, 0);
		assertEquals(0x17, b.getType());
		assertEquals(0x014fd84c, b.getAddress());
		assertEquals(0xd4, b.getOffset());
		assertEquals(0x20, b.getLength());
		assertEquals(0x54, b.getFormat());

		assertFalse(b.destinationCompressed());
		assertFalse(b.destinationHasStrings());
		assertFalse(b.destinationHasChunks());
		assertTrue(b.destinationHasPointers());

		Pointer c = pf.createPointer(vp6_c, 0);
		assertEquals(0x17, c.getType());
		assertEquals(0x014fd8bc, c.getAddress());
		assertEquals(0xf8, c.getOffset());
		assertEquals(0x20, c.getLength());
		assertEquals(0x54, c.getFormat());

		assertFalse(c.destinationCompressed());
		assertFalse(c.destinationHasStrings());
		assertFalse(c.destinationHasChunks());
		assertTrue(c.destinationHasPointers());

		// Type: ff   Addr: 014fffac  Offset: 0    Len:  0   Format: 60   From: 8a94
		Pointer d = pf.createPointer(vp6_d, 0);
		assertEquals(0xff, d.getType());
		assertEquals(0x014fffac, d.getAddress());
		assertEquals(0x00, d.getOffset());
		assertEquals(0x00, d.getLength());
		assertEquals(0x60, d.getFormat());

		assertFalse(d.destinationCompressed());
		assertFalse(d.destinationHasStrings());
		assertFalse(d.destinationHasChunks());
		assertFalse(d.destinationHasPointers());
	}

	public void testCreateV6FromMid() {
		PointerFactory pf = new PointerFactory(11);

		// Create a from part way down the byte stream
		byte[] bytes = new byte[28];
		System.arraycopy(vp6_b, 0, bytes, 0, 10);
		System.arraycopy(vp6_a, 0, bytes, 10, 18);

		Pointer a = pf.createPointer(bytes, 10);
		assertEquals(0x16, a.getType());
		assertEquals(0x0143aff4, a.getAddress());
		assertEquals(0x80, a.getOffset());
		assertEquals(0x54, a.getLength());
		assertEquals(0x46, a.getFormat());

		assertTrue(a.destinationCompressed());
		assertTrue(a.destinationHasStrings());
		assertFalse(a.destinationHasChunks());
		assertFalse(a.destinationHasPointers());
	}
}
