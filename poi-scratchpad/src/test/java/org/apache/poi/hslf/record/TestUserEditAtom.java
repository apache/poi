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
 * Tests that UserEditAtom works properly
 */
public final class TestUserEditAtom {
	// From a real file
	private final byte[] data_a = new byte[] { 0, 0, 0xF5-256, 0x0F, 0x1C, 0, 0, 0,
		0, 1, 0, 0, 0xD9-256, 18, 0, 3,
		0, 0, 0, 0, 0, 0x18, 0, 0, 1, 0, 0, 0,
		5, 0, 0, 0, 1, 0, 0xF6-256, 77 };

	@Test
	void testRecordType() {
		UserEditAtom uea = new UserEditAtom(data_a, 0, data_a.length);
		assertEquals(4085L, uea.getRecordType());
	}

	@Test
	void testFlags() {
		UserEditAtom uea = new UserEditAtom(data_a, 0, data_a.length);

		assertEquals(256, uea.getLastViewedSlideID() );
		//assertEquals(0x030018D9, uea.getPPTVersion() );
		assertEquals(0, uea.getLastUserEditAtomOffset() );
		assertEquals(0x1800, uea.getPersistPointersOffset() );
		assertEquals(1, uea.getDocPersistRef() );
		assertEquals(5, uea.getMaxPersistWritten() );
		assertEquals((short)1, uea.getLastViewType() );
	}

	@Test
	void testWrite() throws Exception {
		UserEditAtom uea = new UserEditAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		uea.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}
}
