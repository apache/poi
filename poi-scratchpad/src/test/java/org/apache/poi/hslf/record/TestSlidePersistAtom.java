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
 * Tests that SlidePersistAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestSlidePersistAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] { 0, 0, 0xF3-256, 3, 0x14, 0, 0, 0,
		4, 0, 0, 0, 4, 0, 0, 0, 2, 0, 0, 0, 0,
		1, 0, 0, 0, 0, 0, 0 };

	public void testRecordType() {
		SlidePersistAtom spa = new SlidePersistAtom(data_a, 0, data_a.length);
		assertEquals(1011l, spa.getRecordType());
	}
	public void testFlags() {
		SlidePersistAtom spa = new SlidePersistAtom(data_a, 0, data_a.length);
		assertEquals(4, spa.getRefID() );
		assertEquals(true, spa.getHasShapesOtherThanPlaceholders() );
		assertEquals(2, spa.getNumPlaceholderTexts() );
		assertEquals(256, spa.getSlideIdentifier());
	}

	public void testWrite() throws Exception {
		SlidePersistAtom spa = new SlidePersistAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		spa.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}
}
