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
 * Tests that ExHyperlinkAtom works properly.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestExHyperlinkAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] {
		00, 00, 0xD3-256, 0x0F, 04, 00, 00, 00,
		01, 00, 00, 00
	};
	private byte[] data_b = new byte[] {
		00, 00, 0xD3-256, 0x0F, 04, 00, 00, 00,
		04, 00, 00, 00
	};

    public void testRecordType() {
    	ExHyperlinkAtom eha = new ExHyperlinkAtom(data_a, 0, data_a.length);
		assertEquals(4051l, eha.getRecordType());
	}

    public void testGetNumber() {
    	ExHyperlinkAtom eha = new ExHyperlinkAtom(data_a, 0, data_a.length);
    	ExHyperlinkAtom ehb = new ExHyperlinkAtom(data_b, 0, data_b.length);

		assertEquals(1, eha.getNumber());
		assertEquals(4, ehb.getNumber());
    }

	public void testWrite() throws Exception {
    	ExHyperlinkAtom eha = new ExHyperlinkAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eha.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	// Create A from scratch
	public void testCreate() throws Exception {
		ExHyperlinkAtom eha = new ExHyperlinkAtom();

		// Set value
		eha.setNumber(1);

		// Check it's now the same as a
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eha.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	// Try to turn a into b
	public void testChange() throws Exception {
		ExHyperlinkAtom eha = new ExHyperlinkAtom(data_a, 0, data_a.length);

		// Change the number
		eha.setNumber(4);

		// Check bytes are now the same
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eha.writeOut(baos);
		byte[] b = baos.toByteArray();

		// Should now be the same
		assertEquals(data_b.length, b.length);
		for(int i=0; i<data_b.length; i++) {
			assertEquals(data_b[i],b[i]);
		}
	}
}
