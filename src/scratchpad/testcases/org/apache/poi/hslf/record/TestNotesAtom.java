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
 * Tests that NotesAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestNotesAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] { 1, 0, 0xF1-256, 3, 8, 0, 0, 0,
		0, 0, 0, 0x80-256, 0, 0, 0x0D, 0x30 };

	public void testRecordType() {
		NotesAtom na = new NotesAtom(data_a, 0, data_a.length);
		assertEquals(1009l, na.getRecordType());
	}
	public void testFlags() {
		NotesAtom na = new NotesAtom(data_a, 0, data_a.length);
		assertEquals(0x80000000, na.getSlideID());
		assertEquals(false, na.getFollowMasterObjects());
		assertEquals(false, na.getFollowMasterScheme());
		assertEquals(false, na.getFollowMasterBackground());
	}

	public void testWrite() throws Exception {
		NotesAtom na = new NotesAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		na.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}
}
