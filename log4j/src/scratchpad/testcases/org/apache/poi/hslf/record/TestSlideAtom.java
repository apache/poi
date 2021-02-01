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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.record.SlideAtomLayout.SlideLayoutType;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.junit.jupiter.api.Test;

/**
 * Tests that SlideAtom works properly
 */
public final class TestSlideAtom {
	// From a real file
	private static final byte[] data_a = new byte[] { 1, 0, 0xEF-256, 3, 0x18, 0, 0, 0,
		0, 0, 0, 0, 0x0F, 0x10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x80-256,
		0, 1, 0, 0, 7, 0, 0x0C, 0x30 };

	@Test
	void testRecordType() {
		SlideAtom sa = new SlideAtom(data_a, 0, data_a.length);
		assertEquals(1007l, sa.getRecordType());
	}

    @Test
	void testFlags() {
		SlideAtom sa = new SlideAtom(data_a, 0, data_a.length);

		// First 12 bytes are a SSlideLayoutAtom, checked elsewhere

		// Check the IDs
		assertEquals(SlideAtom.USES_MASTER_SLIDE_ID, sa.getMasterID());
		assertEquals(256, sa.getNotesID());

		// Check the flags
        assertTrue(sa.getFollowMasterObjects());
        assertTrue(sa.getFollowMasterScheme());
        assertTrue(sa.getFollowMasterBackground());
	}

    @Test
    void testSSlideLayoutAtom() {
		SlideAtom sa = new SlideAtom(data_a, 0, data_a.length);
		SlideAtomLayout ssla = sa.getSSlideLayoutAtom();

		assertEquals(SlideLayoutType.TITLE_SLIDE, ssla.getGeometryType());

		// Should also check the placeholder IDs at some point
	}

    @Test
	void testWrite() throws IOException {
		SlideAtom sa = new SlideAtom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		sa.writeOut(baos);
		assertArrayEquals(data_a, baos.toByteArray());
	}

    @Test
	void testSSSlideInfoAtom() throws IOException {
		HSLFSlideShow ss1 = new HSLFSlideShow();
		HSLFSlide slide1 = ss1.createSlide(), slide2 = ss1.createSlide();
		slide2.setHidden(true);

		HSLFSlideShow ss2 = HSLFTestDataSamples.writeOutAndReadBack(ss1);
		slide1 = ss2.getSlides().get(0);
		slide2 = ss2.getSlides().get(1);
		assertFalse(slide1.isHidden());
		assertTrue(slide2.isHidden());
		ss2.close();
		ss1.close();
	}
}
