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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Class to test HeaderBlockWriter functionality
 *
 * @author Marc Johnson
 */
public final class TestHeaderBlockWriter extends TestCase {

	private static void confirmEqual(String[] expectedDataHexDumpLines, byte[] actual) {
		byte[] expected = RawDataUtil.decode(expectedDataHexDumpLines);

		assertEquals(expected.length, actual.length);
		for (int j = 0; j < expected.length; j++) {
			assertEquals("testing byte " + j, expected[j], actual[j]);
		}
	}

	/**
	 * Test creating a HeaderBlockWriter
	 */
	public void testConstructors() throws IOException {
		HeaderBlockWriter block = new HeaderBlockWriter(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
		ByteArrayOutputStream output = new ByteArrayOutputStream(512);

		block.writeBlocks(output);
		byte[] copy = output.toByteArray();
		String[] expected = {
			"D0 CF 11 E0 A1 B1 1A E1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3B 00 03 00 FE FF 09 00",
			"06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FE FF FF FF 00 00 00 00 00 10 00 00 FE FF FF FF",
			"00 00 00 00 FE FF FF FF 00 00 00 00 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
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

		confirmEqual(expected, copy);

		// verify we can read a 'good' HeaderBlockWriter (also test
		// getPropertyStart)
		block.setPropertyStart(0x87654321);
		output = new ByteArrayOutputStream(512);
		block.writeBlocks(output);
		assertEquals(0x87654321, new HeaderBlockReader(new ByteArrayInputStream(output
				.toByteArray())).getPropertyStart());
	}

	/**
	 * Test setting the SBAT start block
	 */
	public void testSetSBATStart() throws IOException {
	   HeaderBlockWriter block = new HeaderBlockWriter(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);

		block.setSBATStart(0x01234567);
		ByteArrayOutputStream output = new ByteArrayOutputStream(512);

		block.writeBlocks(output);
		byte[] copy = output.toByteArray();
		String[] expected = {
			"D0 CF 11 E0 A1 B1 1A E1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3B 00 03 00 FE FF 09 00",
			"06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FE FF FF FF 00 00 00 00 00 10 00 00 67 45 23 01",
			"00 00 00 00 FE FF FF FF 00 00 00 00 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
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
		confirmEqual(expected, copy);
	}

	/**
	 * test setPropertyStart and getPropertyStart
	 */
	public void testSetPropertyStart() throws IOException {
	   HeaderBlockWriter block = new HeaderBlockWriter(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);

		block.setPropertyStart(0x01234567);
		ByteArrayOutputStream output = new ByteArrayOutputStream(512);

		block.writeBlocks(output);
		byte[] copy = output.toByteArray();
		String[] expected = {
			"D0 CF 11 E0 A1 B1 1A E1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3B 00 03 00 FE FF 09 00",
			"06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 67 45 23 01 00 00 00 00 00 10 00 00 FE FF FF FF",
			"00 00 00 00 FE FF FF FF 00 00 00 00 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF",
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
		confirmEqual(expected, copy);
	}

	/**
	 * test setting the BAT blocks; also tests getBATCount, getBATArray,
	 * getXBATCount
	 */
	public void testSetBATBlocks() throws IOException {

		// first, a small set of blocks
	   HeaderBlockWriter block = new HeaderBlockWriter(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
		BATBlock[] xbats = block.setBATBlocks(5, 0x01234567);

		assertEquals(0, xbats.length);
		assertEquals(0, HeaderBlockWriter.calculateXBATStorageRequirements(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS,5));
		ByteArrayOutputStream output = new ByteArrayOutputStream(512);

		block.writeBlocks(output);
		byte[] copy = output.toByteArray();
		String[] expected = {
			"D0 CF 11 E0 A1 B1 1A E1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3B 00 03 00 FE FF 09 00",
			"06 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 FE FF FF FF 00 00 00 00 00 10 00 00 FE FF FF FF",
			"00 00 00 00 FE FF FF FF 00 00 00 00 67 45 23 01 68 45 23 01 69 45 23 01 6A 45 23 01 6B 45 23 01",
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

		confirmEqual(expected, copy);

		// second, a full set of blocks (109 blocks)
		block = new HeaderBlockWriter(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
		xbats = block.setBATBlocks(109, 0x01234567);
		assertEquals(0, xbats.length);
		assertEquals(0, HeaderBlockWriter.calculateXBATStorageRequirements(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS,109));
		output = new ByteArrayOutputStream(512);
		block.writeBlocks(output);
		copy = output.toByteArray();
		String[] expected2 = {
			"D0 CF 11 E0 A1 B1 1A E1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3B 00 03 00 FE FF 09 00",
			"06 00 00 00 00 00 00 00 00 00 00 00 6D 00 00 00 FE FF FF FF 00 00 00 00 00 10 00 00 FE FF FF FF",
			"00 00 00 00 FE FF FF FF 00 00 00 00 67 45 23 01 68 45 23 01 69 45 23 01 6A 45 23 01 6B 45 23 01",
			"6C 45 23 01 6D 45 23 01 6E 45 23 01 6F 45 23 01 70 45 23 01 71 45 23 01 72 45 23 01 73 45 23 01",
			"74 45 23 01 75 45 23 01 76 45 23 01 77 45 23 01 78 45 23 01 79 45 23 01 7A 45 23 01 7B 45 23 01",
			"7C 45 23 01 7D 45 23 01 7E 45 23 01 7F 45 23 01 80 45 23 01 81 45 23 01 82 45 23 01 83 45 23 01",
			"84 45 23 01 85 45 23 01 86 45 23 01 87 45 23 01 88 45 23 01 89 45 23 01 8A 45 23 01 8B 45 23 01",
			"8C 45 23 01 8D 45 23 01 8E 45 23 01 8F 45 23 01 90 45 23 01 91 45 23 01 92 45 23 01 93 45 23 01",
			"94 45 23 01 95 45 23 01 96 45 23 01 97 45 23 01 98 45 23 01 99 45 23 01 9A 45 23 01 9B 45 23 01",
			"9C 45 23 01 9D 45 23 01 9E 45 23 01 9F 45 23 01 A0 45 23 01 A1 45 23 01 A2 45 23 01 A3 45 23 01",
			"A4 45 23 01 A5 45 23 01 A6 45 23 01 A7 45 23 01 A8 45 23 01 A9 45 23 01 AA 45 23 01 AB 45 23 01",
			"AC 45 23 01 AD 45 23 01 AE 45 23 01 AF 45 23 01 B0 45 23 01 B1 45 23 01 B2 45 23 01 B3 45 23 01",
			"B4 45 23 01 B5 45 23 01 B6 45 23 01 B7 45 23 01 B8 45 23 01 B9 45 23 01 BA 45 23 01 BB 45 23 01",
			"BC 45 23 01 BD 45 23 01 BE 45 23 01 BF 45 23 01 C0 45 23 01 C1 45 23 01 C2 45 23 01 C3 45 23 01",
			"C4 45 23 01 C5 45 23 01 C6 45 23 01 C7 45 23 01 C8 45 23 01 C9 45 23 01 CA 45 23 01 CB 45 23 01",
			"CC 45 23 01 CD 45 23 01 CE 45 23 01 CF 45 23 01 D0 45 23 01 D1 45 23 01 D2 45 23 01 D3 45 23 01",
		};
		confirmEqual(expected2, copy);

		// finally, a really large set of blocks (256 blocks)
		block = new HeaderBlockWriter(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
		xbats = block.setBATBlocks(256, 0x01234567);
		assertEquals(2, xbats.length);
		assertEquals(2, HeaderBlockWriter.calculateXBATStorageRequirements(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS,256));
		output = new ByteArrayOutputStream(512);
		block.writeBlocks(output);
		copy = output.toByteArray();
		String[] expected3 = {
			"D0 CF 11 E0 A1 B1 1A E1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3B 00 03 00 FE FF 09 00",
			"06 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 FE FF FF FF 00 00 00 00 00 10 00 00 FE FF FF FF",
			"00 00 00 00 67 46 23 01 02 00 00 00 67 45 23 01 68 45 23 01 69 45 23 01 6A 45 23 01 6B 45 23 01",
			"6C 45 23 01 6D 45 23 01 6E 45 23 01 6F 45 23 01 70 45 23 01 71 45 23 01 72 45 23 01 73 45 23 01",
			"74 45 23 01 75 45 23 01 76 45 23 01 77 45 23 01 78 45 23 01 79 45 23 01 7A 45 23 01 7B 45 23 01",
			"7C 45 23 01 7D 45 23 01 7E 45 23 01 7F 45 23 01 80 45 23 01 81 45 23 01 82 45 23 01 83 45 23 01",
			"84 45 23 01 85 45 23 01 86 45 23 01 87 45 23 01 88 45 23 01 89 45 23 01 8A 45 23 01 8B 45 23 01",
			"8C 45 23 01 8D 45 23 01 8E 45 23 01 8F 45 23 01 90 45 23 01 91 45 23 01 92 45 23 01 93 45 23 01",
			"94 45 23 01 95 45 23 01 96 45 23 01 97 45 23 01 98 45 23 01 99 45 23 01 9A 45 23 01 9B 45 23 01",
			"9C 45 23 01 9D 45 23 01 9E 45 23 01 9F 45 23 01 A0 45 23 01 A1 45 23 01 A2 45 23 01 A3 45 23 01",
			"A4 45 23 01 A5 45 23 01 A6 45 23 01 A7 45 23 01 A8 45 23 01 A9 45 23 01 AA 45 23 01 AB 45 23 01",
			"AC 45 23 01 AD 45 23 01 AE 45 23 01 AF 45 23 01 B0 45 23 01 B1 45 23 01 B2 45 23 01 B3 45 23 01",
			"B4 45 23 01 B5 45 23 01 B6 45 23 01 B7 45 23 01 B8 45 23 01 B9 45 23 01 BA 45 23 01 BB 45 23 01",
			"BC 45 23 01 BD 45 23 01 BE 45 23 01 BF 45 23 01 C0 45 23 01 C1 45 23 01 C2 45 23 01 C3 45 23 01",
			"C4 45 23 01 C5 45 23 01 C6 45 23 01 C7 45 23 01 C8 45 23 01 C9 45 23 01 CA 45 23 01 CB 45 23 01",
			"CC 45 23 01 CD 45 23 01 CE 45 23 01 CF 45 23 01 D0 45 23 01 D1 45 23 01 D2 45 23 01 D3 45 23 01",
		};

		confirmEqual(expected3, copy);

		output = new ByteArrayOutputStream(1028);
		xbats[0].writeBlocks(output);
		xbats[1].writeBlocks(output);
		copy = output.toByteArray();
		int correct = 0x012345D4;
		int offset = 0;
		int k = 0;

		for (; k < 127; k++) {
			assertEquals("XBAT entry " + k, correct, LittleEndian.getInt(copy, offset));
			correct++;
			offset += LittleEndianConsts.INT_SIZE;
		}
		assertEquals("XBAT Chain", 0x01234567 + 257, LittleEndian.getInt(copy, offset));
		offset += LittleEndianConsts.INT_SIZE;
		k++;
		for (; k < 148; k++) {
			assertEquals("XBAT entry " + k, correct, LittleEndian.getInt(copy, offset));
			correct++;
			offset += LittleEndianConsts.INT_SIZE;
		}
		for (; k < 255; k++) {
			assertEquals("XBAT entry " + k, -1, LittleEndian.getInt(copy, offset));
			offset += LittleEndianConsts.INT_SIZE;
		}
		assertEquals("XBAT End of chain", -2, LittleEndian.getInt(copy, offset));
	}
}
