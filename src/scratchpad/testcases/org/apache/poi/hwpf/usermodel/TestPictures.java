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

package org.apache.poi.hwpf.usermodel;

import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.POIDataSamples;

/**
 * Test the picture handling
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestPictures extends TestCase {

	/**
	 * two jpegs
	 */
	public void testTwoImages() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("two_images.doc");
		List pics = doc.getPicturesTable().getAllPictures();

		assertNotNull(pics);
		assertEquals(pics.size(), 2);
		for(int i=0; i<pics.size(); i++) {
			Object p = pics.get(i);
			assertTrue(p instanceof Picture);

			Picture pic = (Picture)p;
			assertNotNull(pic.suggestFileExtension());
			assertNotNull(pic.suggestFullFileName());
		}

		Picture picA = (Picture)pics.get(0);
		Picture picB = (Picture)pics.get(1);
		assertEquals("jpg", picA.suggestFileExtension());
		assertEquals("jpg", picA.suggestFileExtension());
	}

	/**
	 * pngs and jpegs
	 */
	public void testDifferentImages() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("testPictures.doc");
		List pics = doc.getPicturesTable().getAllPictures();

		assertNotNull(pics);
		assertEquals(7, pics.size());
		for(int i=0; i<pics.size(); i++) {
			Object p = pics.get(i);
			assertTrue(p instanceof Picture);

			Picture pic = (Picture)p;
			assertNotNull(pic.suggestFileExtension());
			assertNotNull(pic.suggestFullFileName());
		}

		assertEquals("jpg", ((Picture)pics.get(0)).suggestFileExtension());
		assertEquals("jpg", ((Picture)pics.get(1)).suggestFileExtension());
		assertEquals("png", ((Picture)pics.get(3)).suggestFileExtension());
		assertEquals("png", ((Picture)pics.get(4)).suggestFileExtension());
		assertEquals("wmf", ((Picture)pics.get(5)).suggestFileExtension());
		assertEquals("jpg", ((Picture)pics.get(6)).suggestFileExtension());
	}

	/**
	 * emf image, nice and simple
	 */
	public void testEmfImage() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("vector_image.doc");
		List pics = doc.getPicturesTable().getAllPictures();

		assertNotNull(pics);
		assertEquals(1, pics.size());

		Picture pic = (Picture)pics.get(0);
		assertNotNull(pic.suggestFileExtension());
		assertNotNull(pic.suggestFullFileName());
		assertTrue(pic.getSize() > 128);

		// Check right contents
		byte[] emf = POIDataSamples.getDocumentInstance().readFile("vector_image.emf");
		byte[] pemf = pic.getContent();
		assertEquals(emf.length, pemf.length);
		for(int i=0; i<emf.length; i++) {
			assertEquals(emf[i], pemf[i]);
		}
	}

	/**
	 * emf image, with a crazy offset
	 */
	public void disabled_testEmfComplexImage() {

		// Commenting out this test case temporarily. The file emf_2003_image does not contain any
		// pictures. Instead it has an office drawing object. Need to rewrite this test after
		// revisiting the implementation of office drawing objects.

		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("emf_2003_image.doc");
		List pics = doc.getPicturesTable().getAllPictures();

		assertNotNull(pics);
		assertEquals(1, pics.size());

		Picture pic = (Picture)pics.get(0);
		assertNotNull(pic.suggestFileExtension());
		assertNotNull(pic.suggestFullFileName());

		// This one's tricky
		// TODO: Fix once we've sorted bug #41898
		assertNotNull(pic.getContent());
		assertNotNull(pic.getRawContent());

		// These are probably some sort of offset, need to figure them out
		assertEquals(4, pic.getSize());
		assertEquals(0x80000000l, LittleEndian.getUInt(pic.getContent()));
		assertEquals(0x80000000l, LittleEndian.getUInt(pic.getRawContent()));
	}

	public void testPicturesWithTable() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug44603.doc");

		List pics = doc.getPicturesTable().getAllPictures();
		assertEquals(pics.size(), 2);
	}

        public void testPicturesInHeader() {
                HWPFDocument doc = HWPFTestDataSamples.openSampleFile("header_image.doc");

                List pics = doc.getPicturesTable().getAllPictures();
                assertEquals(pics.size(), 2);
        }

}
