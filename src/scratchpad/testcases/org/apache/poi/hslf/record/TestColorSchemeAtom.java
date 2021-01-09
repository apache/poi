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


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests that ColorSchemAtom works properly
 */
public final class TestColorSchemeAtom {
	// From a real file
	private final byte[] data_a = { 60, 0, 0xF0-256, 0x07, 0x20, 0, 0, 0,
		0xFF-256, 0xFF-256, 0xFF-256, 0,   0, 0, 0, 0,
		0x80-256, 0x80-256, 0x80-256, 0,   0, 0, 0, 0,
		0xBB-256, 0xE0-256, 0xE3-256, 0,   0x33, 0x33, 0x99-256, 0,
		0, 0x99-256, 0x99-256, 0,         0x99-256, 0xCC-256, 0, 0
	};

	@Test
	void testRecordType() {
		ColorSchemeAtom csa = new ColorSchemeAtom(data_a,0,data_a.length);
		assertEquals(2032L, csa.getRecordType());
	}

	@Test
	void testToRGB() {
		byte[] rgb = ColorSchemeAtom.splitRGB(3669760);

		assertEquals(3,rgb.length);
		assertEquals(0, rgb[0]);
		assertEquals(255-256, rgb[1]);
		assertEquals(55, rgb[2]);
	}

	@Test
	void testFromRGB() {
		byte[] rgb_a = new byte[] { 0, 255-256, 55 };
		byte[] rgb_b = new byte[] { 255-256, 127, 79 };

		assertEquals( 3669760, ColorSchemeAtom.joinRGB( rgb_a ) );
		assertEquals( 5210111, ColorSchemeAtom.joinRGB( rgb_b ) );

		assertEquals( 3669760, ColorSchemeAtom.joinRGB( rgb_a[0], rgb_a[1], rgb_a[2] ) );
		assertEquals( 5210111, ColorSchemeAtom.joinRGB( rgb_b[0], rgb_b[1], rgb_b[2] ) );
	}

	@Test
	void testRGBs() {
		ColorSchemeAtom csa = new ColorSchemeAtom(data_a,0,data_a.length);

		assertEquals( 16777215 , csa.getBackgroundColourRGB() );
		assertEquals( 0 , csa.getTextAndLinesColourRGB() );
		assertEquals( 8421504 , csa.getShadowsColourRGB() );
		assertEquals( 0 , csa.getTitleTextColourRGB() );
		assertEquals( 14934203 , csa.getFillsColourRGB() );
		assertEquals( 10040115 , csa.getAccentColourRGB() );
		assertEquals( 10066176 , csa.getAccentAndHyperlinkColourRGB() );
		assertEquals( 52377 , csa.getAccentAndFollowingHyperlinkColourRGB() );
	}

	@Test
	void testWrite() throws Exception {
		ColorSchemeAtom csa = new ColorSchemeAtom(data_a,0,data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		csa.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}
}
