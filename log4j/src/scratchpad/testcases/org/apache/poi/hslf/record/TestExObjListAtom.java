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

import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.formula.functions.BaseTestNumeric;
import org.junit.jupiter.api.Test;

/**
 * Tests that ExObjListAtom works properly.
 */
public class TestExObjListAtom {
	// From a real file
	private final byte[] data_a = new byte[] {
		0, 0, 0x0A, 0x04, 4, 0, 0, 0,
		1, 0, 0, 0
	};
	private final byte[] data_b = new byte[] {
		0, 0, 0x0A, 0x04, 4, 0, 0, 0,
		4, 0, 0, 0
	};

	@Test
	void testRecordType() {
		ExObjListAtom eoa = new ExObjListAtom(data_a, 0, data_a.length);
		BaseTestNumeric.assertDouble(1034L, eoa.getRecordType());
	}

	@Test
	void testGetSeed() {
		ExObjListAtom eoa = new ExObjListAtom(data_a, 0, data_a.length);
		ExObjListAtom eob = new ExObjListAtom(data_b, 0, data_b.length);

		BaseTestNumeric.assertDouble(1, eoa.getObjectIDSeed());
		BaseTestNumeric.assertDouble(4, eob.getObjectIDSeed());
	}

	@Test
	void testWrite() throws Exception {
		ExObjListAtom eoa = new ExObjListAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eoa.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

	// Create A from scratch
	@Test
	void testCreate() throws Exception {
		ExObjListAtom eoa = new ExObjListAtom();

		// Set seed
		eoa.setObjectIDSeed(1);

		// Check it's now the same as a
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eoa.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

	// Try to turn a into b
	@Test
	void testChange() throws Exception {
		ExObjListAtom eoa = new ExObjListAtom(data_a, 0, data_a.length);

		// Change the number
		eoa.setObjectIDSeed(4);

		// Check bytes are now the same
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eoa.writeOut(baos);
		assertArrayEquals(data_b, baos.toByteArray());
	}
}
