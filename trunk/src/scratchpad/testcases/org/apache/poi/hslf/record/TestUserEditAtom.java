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
 * Tests that UserEditAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestUserEditAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] { 0, 0, 0xF5-256, 0x0F, 0x1C, 0, 0, 0,
		00, 01, 00, 00, 0xD9-256, 18, 00, 03,
		00, 00, 00, 00, 00, 0x18, 00, 00, 01, 00, 00, 00,
		05, 00, 00, 00, 01, 00, 0xF6-256, 77 };

	public void testRecordType() {
		UserEditAtom uea = new UserEditAtom(data_a, 0, data_a.length);
		assertEquals(4085l, uea.getRecordType());
	}
	public void testFlags() {
		UserEditAtom uea = new UserEditAtom(data_a, 0, data_a.length);

		assertEquals(256, uea.getLastViewedSlideID() );
		//assertEquals(0x030018D9, uea.getPPTVersion() );
		assertEquals(0, uea.getLastUserEditAtomOffset() );
		assertEquals(0x1800, uea.getPersistPointersOffset() );
		assertEquals(1, uea.getDocPersistRef() );
		assertEquals(5, uea.getMaxPersistWritten() );
		assertEquals((short)1, uea.getLastViewType() );
	}

	public void testWrite() throws Exception {
		UserEditAtom uea = new UserEditAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		uea.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}
}
