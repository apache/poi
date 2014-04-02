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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Tests TextRulerAtom
 *
 * @author Yegor Kozlov
 */
public final class TestTextRulerAtom extends TestCase {

	//from a real file
	private byte[] data_1 = new byte[] {
		0x00, 0x00, (byte)0xA6, 0x0F, 0x18, 0x00, 0x00, 0x00,
		(byte)0xF8, 0x1F, 0x00, 0x00, 0x75, 0x00, (byte)0xE2, 0x00, 0x59,
		0x01, (byte)0xC3, 0x01, 0x1A, 0x03, (byte)0x87, 0x03, (byte)0xF8,
		0x03, 0x69, 0x04, (byte)0xF6, 0x05, (byte)0xF6, 0x05
	};

	private byte[] data_2 = new byte[] {
		0x00, 0x00, (byte)0xA6, 0x0F, 0x0A, 0x00, 0x00, 0x00,
		0x10, 0x03, 0x00, 0x00, (byte)0xF9, 0x00, 0x41, 0x01, 0x41, 0x01
	};

	public void testReadRuler() {
		TextRulerAtom ruler = new TextRulerAtom(data_1, 0, data_1.length);
		assertEquals(ruler.getNumberOfLevels(), 0);
		assertEquals(ruler.getDefaultTabSize(), 0);

		int[] tabStops = ruler.getTabStops();
		assertNull(tabStops);

		int[] textOffsets = ruler.getTextOffsets();
		assertTrue(Arrays.equals(new int[]{226, 451, 903, 1129, 1526}, textOffsets));

		int[] bulletOffsets = ruler.getBulletOffsets();
		assertTrue(Arrays.equals(new int[]{117, 345, 794, 1016, 1526}, bulletOffsets));

	}

	public void testWriteRuler() throws Exception {
		TextRulerAtom ruler = new TextRulerAtom(data_1, 0, data_1.length);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ruler.writeOut(out);

		byte[] result = out.toByteArray();
		assertTrue(Arrays.equals(result, data_1));
	}

	public void testRead2() throws Exception {
		TextRulerAtom ruler = TextRulerAtom.getParagraphInstance();
		ruler.setParagraphIndent((short)249, (short)321);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ruler.writeOut(out);

		byte[] result = out.toByteArray();
		assertTrue(Arrays.equals(result, data_2));
	}
}
