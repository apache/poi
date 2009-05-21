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

/**
 * Tests that DocumentEncryptionAtom works properly.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestDocumentEncryptionAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] {
		0x0F, 00, 0x14, 0x2F, 0xBE-256, 00, 00, 00,
		02, 00, 02, 00, 0x0C, 00, 00, 00,
		0x76, 00, 00, 00, 0x0C, 00, 00, 00,
		00, 00, 00, 00, 01, 0x68, 00, 00,
		04, 0x80-256, 00, 00, 0x28, 00, 00, 00,
		01, 00, 00, 00, 0x30, 00, 0x26, 01,
		00, 00, 00, 00,

		0x4D, 00, 0x69, 00,
		0x63, 00, 0x72, 00, 0x6F, 00, 0x73, 00,
		0x6F, 00, 0x66, 00, 0x74, 00, 0x20, 00,
		0x42, 00, 0x61, 00, 0x73, 00, 0x65, 00,
		0x20, 00, 0x43, 00, 0x72, 00, 0x79, 00,
		0x70, 00, 0x74, 00, 0x6F, 00, 0x67, 00,
		0x72, 00, 0x61, 00, 0x70, 00, 0x68, 00,
		0x69, 00, 0x63, 00, 0x20, 00, 0x50, 00,
		0x72, 00, 0x6F, 00, 0x76, 00, 0x69, 00,
		0x64, 00, 0x65, 00, 0x72, 00, 0x20, 00,
		0x76, 00, 0x31, 00, 0x2E, 00, 0x30, 00,
		0x00, 0x00,

		0x10, 00, 0x00, 00,
		0x62, 0xA6-256,
		0xDF-256, 0xEA-256, 0x96-256, 0x84-256,
		0xFB-256, 0x89-256, 0x93-256, 0xCA-256,
		0xBA-256, 0xEE-256, 0x8E-256, 0x43,
		0xC8-256, 0x71, 0xD1-256, 0x89-256,
		0xF6-256, 0x4B, 0x2B, 0xD9-256,
		0x7E, 0x0B, 0x52, 0xFB-256,
		0x68, 0xD7-256, 0x5A, 0x4E, 0x45, 0xDF-256, 0x14, 0x00,
		0x00, 0x00, 0x93-256, 0x15, 0x27, 0xEB-256, 0x21, 0x54,
		0x7F, 0x0B, 0x56, 0x07, 0xEE-256, 0x66, 0xEB-256, 0x6F,
		0xB2-256, 0x8E-256, 0x67, 0x54, 0x07, 0x04, 0x00
	};

	private byte[] data_b = new byte[] {
			15, 0, 20, 47, -66, 0, 0, 0,
			2, 0, 2, 0, 4,
			0, 0, 0, 118, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0,
			0, 1, 104, 0, 0, 4, -128, 0, 0, 56, 0, 0, 0,
			1, 0, 0, 0, 48, 0, 38, 1, 0, 0, 0, 0, 77, 0,
			105, 0, 99, 0, 114, 0, 111, 0, 115, 0, 111,
			0, 102, 0, 116, 0, 32, 0, 66, 0, 97, 0, 115,
			0, 101, 0, 32, 0, 67, 0, 114, 0, 121, 0, 112,
			0, 116, 0, 111, 0, 103, 0, 114, 0, 97, 0,
			112, 0, 104, 0, 105, 0, 99, 0, 32, 0, 80, 0,
			114, 0, 111, 0, 118, 0, 105, 0, 100, 0, 101,
			0, 114, 0, 32, 0, 118, 0, 49, 0, 46, 0, 48,
			0, 0, 0, 16, 0, 0, 0, -80, -66, 112, -40, 57,
			110, 54, 80, 64, 61, -73, -29, 48, -35, -20,
			17, -40, 84, 54, 6, -103, 125, -22, -72, 53,
			103, -114, 13, -48, 111, 29, 78, 20, 0, 0,
			0, -97, -67, 55, -62, -94, 14, 15, -21, 37,
			3, -104, 22, 6, 102, -61, -98, 62, 40, 61, 21
	};

    public void testRecordType() {
		DocumentEncryptionAtom dea1 = new DocumentEncryptionAtom(data_a, 0, data_a.length);
		assertEquals(12052l, dea1.getRecordType());

		DocumentEncryptionAtom dea2 = new DocumentEncryptionAtom(data_b, 0, data_b.length);
		assertEquals(12052l, dea2.getRecordType());

		assertEquals(199, data_a.length);
		assertEquals(198, data_b.length);
	}

    public void testEncryptionTypeName() {
		DocumentEncryptionAtom dea1 = new DocumentEncryptionAtom(data_a, 0, data_a.length);
		assertEquals("Microsoft Base Cryptographic Provider v1.0", dea1.getEncryptionProviderName());

		DocumentEncryptionAtom dea2 = new DocumentEncryptionAtom(data_b, 0, data_b.length);
		assertEquals("Microsoft Base Cryptographic Provider v1.0", dea2.getEncryptionProviderName());
    }
}
