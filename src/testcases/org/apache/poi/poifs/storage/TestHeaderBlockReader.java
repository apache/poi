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

package org.apache.poi.poifs.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Class to test HeaderBlockReader functionality
 *
 * @author Marc Johnson
 */
public final class TestHeaderBlockReader extends TestCase {

	public void testConstructors() throws IOException {
		String[] hexData = {
			"D0 CF 11 E0 A1 B1 1A E1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3B 00 03 00 FE FF 09 00",
			"06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FE FF FF FF 00 00 00 00 00 10 00 00 FE FF FF FF",
			"01 00 00 00 FE FF FF FF 00 00 00 00 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
			"FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
		};
		byte[] content = RawDataUtil.decode(hexData);
		HeaderBlockReader block = new HeaderBlockReader(new ByteArrayInputStream(content));

		assertEquals(-2, block.getPropertyStart());

		// verify we can't read a short block
		byte[] shortblock = new byte[511];

		System.arraycopy(content, 0, shortblock, 0, 511);
		try {
			block = new HeaderBlockReader(new ByteArrayInputStream(shortblock));
			fail("Should have caught IOException reading a short block");
		} catch (IOException ignored) {

			// as expected
		}

		// try various forms of corruption
		for (int index = 0; index < 8; index++) {
			content[index] = (byte) (content[index] - 1);
			try {
				block = new HeaderBlockReader(new ByteArrayInputStream(content));
				fail("Should have caught IOException corrupting byte " + index);
			} catch (IOException ignored) {

				// as expected
			}

			// restore byte value
			content[index] = (byte) (content[index] + 1);
		}
	}
}
