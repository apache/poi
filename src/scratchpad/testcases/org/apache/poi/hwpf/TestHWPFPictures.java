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

package org.apache.poi.hwpf;

import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.POIDataSamples;

/**
 * Test picture support in HWPF
 * @author nick
 */
public final class TestHWPFPictures extends TestCase {
	private String docAFile;
	private String docBFile;
	private String docCFile;
	private String docDFile;

	private String imgAFile;
	private String imgBFile;
	private String imgCFile;
	private String imgDFile;

	protected void setUp() {

		docAFile = "testPictures.doc";
		docBFile = "two_images.doc";
		docCFile = "vector_image.doc";
		docDFile = "GaiaTest.doc";

		imgAFile = "simple_image.jpg";
		imgBFile = "simple_image.png";
		imgCFile = "vector_image.emf";
		imgDFile = "GaiaTestImg.png";
	}

	/**
	 * Test just opening the files
	 */
	public void testOpen() {
		HWPFDocument docA = HWPFTestDataSamples.openSampleFile(docAFile);
		HWPFDocument docB = HWPFTestDataSamples.openSampleFile(docBFile);
	}

	/**
	 * Test that we have the right numbers of images in each file
	 */
	public void testImageCount() {
		HWPFDocument docA = HWPFTestDataSamples.openSampleFile(docAFile);
		HWPFDocument docB = HWPFTestDataSamples.openSampleFile(docBFile);

		assertNotNull(docA.getPicturesTable());
		assertNotNull(docB.getPicturesTable());

		PicturesTable picA = docA.getPicturesTable();
		PicturesTable picB = docB.getPicturesTable();

		List picturesA = picA.getAllPictures();
		List picturesB = picB.getAllPictures();

		assertEquals(7, picturesA.size());
		assertEquals(2, picturesB.size());
	}

	/**
	 * Test that we have the right images in at least one file
	 */
	public void testImageData() {
		HWPFDocument docB = HWPFTestDataSamples.openSampleFile(docBFile);
		PicturesTable picB = docB.getPicturesTable();
		List picturesB = picB.getAllPictures();

		assertEquals(2, picturesB.size());

		Picture pic1 = (Picture)picturesB.get(0);
		Picture pic2 = (Picture)picturesB.get(1);

		assertNotNull(pic1);
		assertNotNull(pic2);

		// Check the same
		byte[] pic1B = readFile(imgAFile);
		byte[] pic2B = readFile(imgBFile);

		assertEquals(pic1B.length, pic1.getContent().length);
		assertEquals(pic2B.length, pic2.getContent().length);

		assertBytesSame(pic1B, pic1.getContent());
		assertBytesSame(pic2B, pic2.getContent());
	}

	/**
	 * Test that compressed image data is correctly returned.
	 */
	public void testCompressedImageData() {
		HWPFDocument docC = HWPFTestDataSamples.openSampleFile(docCFile);
		PicturesTable picC = docC.getPicturesTable();
		List picturesC = picC.getAllPictures();

		assertEquals(1, picturesC.size());

		Picture pic = (Picture)picturesC.get(0);
		assertNotNull(pic);

		// Check the same
		byte[] picBytes = readFile(imgCFile);

		assertEquals(picBytes.length, pic.getContent().length);
		assertBytesSame(picBytes, pic.getContent());
	}

	/**
	 * Pending the missing files being uploaded to
	 *  bug #44937
	 */
	public void BROKENtestEscherDrawing() {
		HWPFDocument docD = HWPFTestDataSamples.openSampleFile(docDFile);
		List allPictures = docD.getPicturesTable().getAllPictures();

		assertEquals(1, allPictures.size());

		Picture pic = (Picture) allPictures.get(0);
		assertNotNull(pic);
		byte[] picD = readFile(imgDFile);

		assertEquals(picD.length, pic.getContent().length);

		assertBytesSame(picD, pic.getContent());
	}

	private void assertBytesSame(byte[] a, byte[] b) {
		assertEquals(a.length, b.length);
		for(int i=0; i<a.length; i++) {
			assertEquals(a[i],b[i]);
		}
	}

	private static byte[] readFile(String file) {
		return POIDataSamples.getDocumentInstance().readFile(file);
	}
}
