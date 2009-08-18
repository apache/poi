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

import org.apache.poi.hslf.record.SlideAtom.*;

import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;

/**
 * Tests that SlideAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestSlideAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] { 1, 0, 0xEF-256, 3, 0x18, 0, 0, 0,
		0, 0, 0, 0, 0x0F, 0x10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x80-256,
		0, 1, 0, 0, 7, 0, 0x0C, 0x30 };

    public void testRecordType() {
		SlideAtom sa = new SlideAtom(data_a, 0, data_a.length);
		assertEquals(1007l, sa.getRecordType());
	}
	public void testFlags() {
		SlideAtom sa = new SlideAtom(data_a, 0, data_a.length);

		// First 12 bytes are a SSlideLayoutAtom, checked elsewhere

		// Check the IDs
		assertEquals(0x80000000, sa.getMasterID());
		assertEquals(256, sa.getNotesID());

		// Check the flags
		assertEquals(true, sa.getFollowMasterObjects());
		assertEquals(true, sa.getFollowMasterScheme());
		assertEquals(true, sa.getFollowMasterBackground());
	}
	public void testSSlideLayoutAtom() {
		SlideAtom sa = new SlideAtom(data_a, 0, data_a.length);
		SSlideLayoutAtom ssla = sa.getSSlideLayoutAtom();

		assertEquals(0, ssla.getGeometryType());

		// Should also check the placehold IDs at some point
	}

	public void testWrite() throws Exception {
		SlideAtom sa = new SlideAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		sa.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}
}
