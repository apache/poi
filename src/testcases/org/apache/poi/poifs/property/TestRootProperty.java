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

package org.apache.poi.poifs.property;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.storage.RawDataUtil;

/**
 * Class to test RootProperty functionality
 *
 * @author Marc Johnson
 */
public final class TestRootProperty extends TestCase {
	private RootProperty _property;
	private byte[] _testblock;

	public void testConstructor() throws IOException {
		createBasicRootProperty();
		verifyProperty();
	}

	private void createBasicRootProperty() {
		_property = new RootProperty();
		_testblock = new byte[128];
		int index = 0;

		for (; index < 0x40; index++) {
			_testblock[index] = (byte) 0;
		}
		String name = "Root Entry";
		int limit = Math.min(31, name.length());

		_testblock[index++] = (byte) (2 * (limit + 1));
		_testblock[index++] = (byte) 0;
		_testblock[index++] = (byte) 5;
		_testblock[index++] = (byte) 1;
		for (; index < 0x50; index++) {
			_testblock[index] = (byte) 0xff;
		}
		for (; index < 0x74; index++) {
			_testblock[index] = (byte) 0;
		}
		_testblock[index++] = (byte) POIFSConstants.END_OF_CHAIN;
		for (; index < 0x78; index++) {
			_testblock[index] = (byte) 0xff;
		}
		for (; index < 0x80; index++) {
			_testblock[index] = (byte) 0;
		}
		byte[] name_bytes = name.getBytes();

		for (index = 0; index < limit; index++) {
			_testblock[index * 2] = name_bytes[index];
		}
	}

	private void verifyProperty() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream(512);

		_property.writeData(stream);
		byte[] output = stream.toByteArray();

		assertEquals(_testblock.length, output.length);
		for (int j = 0; j < _testblock.length; j++) {
			assertEquals("mismatch at offset " + j, _testblock[j], output[j]);
		}
	}

	public void testSetSize() {
		for (int j = 0; j < 10; j++) {
			createBasicRootProperty();
			_property.setSize(j);
			assertEquals("trying block count of " + j, j * 64, _property.getSize());
		}
	}

	public void testReadingConstructor() {
		String[] input = {
			"52 00 6F 00 6F 00 74 00 20 00 45 00 6E 00 74 00 72 00 79 00 00 00 00 00 00 00 00 00 00 00 00 00",
			"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00",
			"16 00 05 01 FF FF FF FF FF FF FF FF 02 00 00 00 20 08 02 00 00 00 00 00 C0 00 00 00 00 00 00 46",
			"00 00 00 00 00 00 00 00 00 00 00 00 C0 5C E8 23 9E 6B C1 01 FE FF FF FF 00 00 00 00 00 00 00 00",
		};
		verifyReadingProperty(0, RawDataUtil.decode(input), 0, "Root Entry",
				"{00020820-0000-0000-C000-000000000046}");
	}

	private void verifyReadingProperty(int index, byte[] input, int offset, String name,
			String sClsId) {
		RootProperty property = new RootProperty(index, input, offset);
		ByteArrayOutputStream stream = new ByteArrayOutputStream(128);
		byte[] expected = new byte[128];

		System.arraycopy(input, offset, expected, 0, 128);
		try {
			property.writeData(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] output = stream.toByteArray();

		assertEquals(128, output.length);
		for (int j = 0; j < 128; j++) {
			assertEquals("mismatch at offset " + j, expected[j], output[j]);
		}
		assertEquals(index, property.getIndex());
		assertEquals(name, property.getName());
		assertTrue(!property.getChildren().hasNext());
		assertEquals(property.getStorageClsid().toString(), sClsId);
	}
}
