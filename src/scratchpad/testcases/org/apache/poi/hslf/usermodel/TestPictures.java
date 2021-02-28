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

package org.apache.poi.hslf.usermodel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.blip.DIB;
import org.apache.poi.hslf.blip.EMF;
import org.apache.poi.hslf.blip.JPEG;
import org.apache.poi.hslf.blip.PICT;
import org.apache.poi.hslf.blip.PNG;
import org.apache.poi.hslf.blip.WMF;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.sl.image.ImageHeaderEMF;
import org.apache.poi.sl.image.ImageHeaderPICT;
import org.apache.poi.sl.image.ImageHeaderWMF;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.Units;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test adding/reading pictures
 *
 * @author Yegor Kozlov
 */
public final class TestPictures {
    private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

    /**
     * Test read/write Macintosh PICT
     */
    @Test
    void testPICT() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("cow.pict");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.PICT);
        ImageHeaderPICT nHeader = new ImageHeaderPICT(src_bytes, 512);
        final int expWidth = 197, expHeight = 137;
        Dimension nDim = nHeader.getSize();
        assertEquals(expWidth, nDim.getWidth(), 0);
        assertEquals(expHeight, nDim.getHeight(), 0);

        Dimension dim = data.getImageDimensionInPixels();
        assertEquals(Units.pointsToPixel(expWidth), dim.getWidth(), 0);
        assertEquals(Units.pointsToPixel(expHeight), dim.getHeight(), 0);

        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        List<HSLFPictureData> pictures = ppt.getPictureData();
        assertEquals(1, pictures.size());

        HSLFPictureData pd = pictures.get(0);
        dim = pd.getImageDimension();
        assertEquals(expWidth, dim.width);
        assertEquals(expHeight, dim.height);

        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pd);

        assertEquals(1, pictures.size());
        assertEquals(PictureType.PICT, pd.getType());
        assertTrue(pd instanceof PICT);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pd.getData();
        assertEquals(src_bytes.length, ppt_bytes.length);
        //in PICT the first 512 bytes are MAC specific and may not be preserved, ignore them
        byte[] b1 = Arrays.copyOfRange(src_bytes, 512, src_bytes.length);
        byte[] b2 = Arrays.copyOfRange(ppt_bytes, 512, ppt_bytes.length);
        assertArrayEquals(b1, b2);
    }

    /**
     * Test read/write WMF
     */
    @Test
    void testWMF() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("santa.wmf");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.WMF);
        ImageHeaderWMF nHeader = new ImageHeaderWMF(src_bytes, 0);
        final int expWidth = 136, expHeight = 146;
        Dimension nDim = nHeader.getSize();
        assertEquals(expWidth, nDim.getWidth(), 0);
        assertEquals(expHeight, nDim.getHeight(), 0);

        Dimension dim = data.getImageDimensionInPixels();
        assertEquals(Units.pointsToPixel(expWidth), dim.getWidth(), 0);
        assertEquals(Units.pointsToPixel(expHeight), dim.getHeight(), 0);

        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        List<HSLFPictureData> pictures = ppt.getPictureData();
        assertEquals(1, pictures.size());

        HSLFPictureData pd = pictures.get(0);
        dim = pd.getImageDimension();
        assertEquals(expWidth, dim.width);
        assertEquals(expHeight, dim.height);

        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pd);

        assertEquals(PictureType.WMF, pd.getType());
        assertTrue(pd instanceof WMF);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pd.getData();
        assertEquals(src_bytes.length, ppt_bytes.length);
        //in WMF the first 22 bytes - is a metafile header
        byte[] b1 = Arrays.copyOfRange(src_bytes, 22, src_bytes.length);
        byte[] b2 = Arrays.copyOfRange(ppt_bytes, 22, ppt_bytes.length);
        assertArrayEquals(b1, b2);
    }

    /**
     * Test read/write EMF
     */
    @Test
    void testEMF() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("wrench.emf");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.EMF);
        ImageHeaderEMF nHeader = new ImageHeaderEMF(src_bytes, 0);
        final int expWidth = 190, expHeight = 115;
        Dimension nDim = nHeader.getSize();
        assertEquals(expWidth, nDim.getWidth(), 0);
        assertEquals(expHeight, nDim.getHeight(), 0);

        Dimension dim = data.getImageDimensionInPixels();
        assertEquals(Units.pointsToPixel(expWidth), dim.getWidth(), 0);
        assertEquals(Units.pointsToPixel(expHeight), dim.getHeight(), 0);

        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can get this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        List<HSLFPictureData> pictures = ppt.getPictureData();
        assertEquals(1, pictures.size());

        HSLFPictureData pd = pictures.get(0);
        dim = pd.getImageDimension();
        assertEquals(expWidth, dim.width);
        assertEquals(expHeight, dim.height);

        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pd);

        assertEquals(1, pictures.size());
        assertEquals(PictureType.EMF, pd.getType());
        assertTrue(pd instanceof EMF);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pd.getData();
        assertArrayEquals(src_bytes, ppt_bytes);
    }

    /**
     * Test read/write PNG
     */
    @Test
    void testPNG() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("tomcat.png");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.PNG);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        List<HSLFPictureData> pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures.get(0));

        assertEquals(1, pictures.size());
        assertEquals(PictureType.PNG, pictures.get(0).getType());
        assertTrue(pictures.get(0) instanceof PNG);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures.get(0).getData();
        assertArrayEquals(src_bytes, ppt_bytes);
    }

    /**
     * Test read/write JPEG
     */
    @Test
    void testJPEG() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("clock.jpg");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.JPEG);

        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        List<HSLFPictureData> pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures.get(0));

        assertEquals(1, pictures.size());
        assertEquals(PictureType.JPEG, pictures.get(0).getType());
        assertTrue(pictures.get(0) instanceof JPEG);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures.get(0).getData();
        assertArrayEquals(src_bytes, ppt_bytes);
    }

    /**
     * Test read/write DIB
     */
    @Test
    void testDIB() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("clock.dib");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.DIB);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        List<HSLFPictureData> pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures.get(0));

        assertEquals(1, pictures.size());
        assertEquals(PictureType.DIB, pictures.get(0).getType());
        assertTrue(pictures.get(0) instanceof DIB);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures.get(0).getData();
        assertArrayEquals(src_bytes, ppt_bytes);
    }

    /**
     * Read pictures in different formats from a reference slide show
     */
    @Test
    void testReadPictures() throws IOException {

        byte[] src_bytes, ppt_bytes, b1, b2;
        HSLFPictureShape pict;
        HSLFPictureData pdata;

        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("pictures.ppt");
        List<HSLFSlide> slides = ppt.getSlides();
        List<HSLFPictureData> pictures = ppt.getPictureData();
        assertEquals(5, pictures.size());

        pict = (HSLFPictureShape)slides.get(0).getShapes().get(0); //the first slide contains JPEG
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof JPEG);
        assertEquals(PictureType.JPEG, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("clock.jpg");
        assertArrayEquals(src_bytes, ppt_bytes);

        pict = (HSLFPictureShape)slides.get(1).getShapes().get(0); //the second slide contains PNG
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof PNG);
        assertEquals(PictureType.PNG, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("tomcat.png");
        assertArrayEquals(src_bytes, ppt_bytes);

        pict = (HSLFPictureShape)slides.get(2).getShapes().get(0); //the third slide contains WMF
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("santa.wmf");
        assertEquals(src_bytes.length, ppt_bytes.length);
        //ignore the first 22 bytes - it is a WMF metafile header
        b1 = Arrays.copyOfRange(src_bytes, 22, src_bytes.length);
        b2 = Arrays.copyOfRange(ppt_bytes, 22, ppt_bytes.length);
        assertArrayEquals(b1, b2);

        pict = (HSLFPictureShape)slides.get(3).getShapes().get(0); //the forth slide contains PICT
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof PICT);
        assertEquals(PictureType.PICT, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("cow.pict");
        assertEquals(src_bytes.length, ppt_bytes.length);
        //ignore the first 512 bytes - it is a MAC specific crap
        b1 = Arrays.copyOfRange(src_bytes, 512, src_bytes.length);
        b2 = Arrays.copyOfRange(ppt_bytes, 512, ppt_bytes.length);
        assertArrayEquals(b1, b2);

        pict = (HSLFPictureShape)slides.get(4).getShapes().get(0); //the fifth slide contains EMF
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof EMF);
        assertEquals(PictureType.EMF, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("wrench.emf");
        assertArrayEquals(src_bytes, ppt_bytes);

        ppt.close();
    }

	/**
	 * Test that on a party corrupt powerpoint document, which has
	 *  crazy pictures of type 0, we do our best.
	 */
    @Test
	void testZeroPictureType() throws IOException {
		HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(slTests.openResourceAsStream("PictureTypeZero.ppt"));

		// Should still have 2 real pictures
		assertEquals(2, hslf.getPictureData().size());
		// Both are real pictures, both WMF
		assertEquals(PictureType.WMF, hslf.getPictureData().get(0).getType());
		assertEquals(PictureType.WMF, hslf.getPictureData().get(1).getType());

		// Now test what happens when we use the SlideShow interface
		HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        List<HSLFSlide> slides = ppt.getSlides();
        List<HSLFPictureData> pictures = ppt.getPictureData();
        assertEquals(12, slides.size());
        assertEquals(2, pictures.size());

		HSLFPictureShape pict;
		HSLFPictureData pdata;

        pict = (HSLFPictureShape)slides.get(0).getShapes().get(1); // 2nd object on 1st slide
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());

        pict = (HSLFPictureShape)slides.get(0).getShapes().get(2); // 3rd object on 1st slide
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());

        ppt.close();
	}

    /**
     * YK: The test is disabled because the owner asked to delete the test file from POI svn.
     * See "Please remove my file from your svn" on @poi-dev from Dec 12, 2013
     */
    @Test
    @Disabled("requires an internet connection to a 3rd party site")
    // As of 2017-06-20, the file still exists at the specified URL and the test passes.
	void testZeroPictureLength() throws IOException {
        // take the data from www instead of test directory
        URL url = new URL("http://www.cs.sfu.ca/~anoop/courses/CMPT-882-Fall-2002/chris.ppt");
		HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(url.openStream());
        /* Assume that the file could retrieved...
        InputStream is;
        HSLFSlideShowImpl hslf;
        try {
            is = url.openStream();
            hslf = new HSLFSlideShowImpl(is);
            is.close();
        } catch (final IOException e) {
            Assume.assumeTrue(e.getMessage(), false);
            throw e;
        }
        */

		// Should still have 2 real pictures
		assertEquals(2, hslf.getPictureData().size());
		// Both are real pictures, both WMF
		assertEquals(PictureType.WMF, hslf.getPictureData().get(0).getType());
		assertEquals(PictureType.WMF, hslf.getPictureData().get(1).getType());

		// Now test what happens when we use the SlideShow interface
		HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        List<HSLFSlide> slides = ppt.getSlides();
        List<HSLFPictureData> pictures = ppt.getPictureData();
        assertEquals(27, slides.size());
        assertEquals(2, pictures.size());

		HSLFPictureShape pict;
		HSLFPictureData pdata;

        pict = (HSLFPictureShape)slides.get(6).getShapes().get(13);
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());

        pict = (HSLFPictureShape)slides.get(7).getShapes().get(13);
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());

        //add a new picture, it should be correctly appended to the Pictures stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(HSLFPictureData p : pictures) p.write(out);
        out.close();

        int streamSize = out.size();

        HSLFPictureData data = ppt.addPicture(new byte[100], PictureType.JPEG);
        int offset = data.getOffset();
        assertEquals(streamSize, offset);
        assertEquals(3, ppt.getPictureData().size());

        ppt.close();
    }

    @Test
    void testGetPictureName() throws IOException {
        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("ppt_with_png.ppt");
        HSLFSlide slide = ppt.getSlides().get(0);

        HSLFPictureShape p = (HSLFPictureShape)slide.getShapes().get(0); //the first slide contains JPEG
        assertEquals("test", p.getPictureName());
        ppt.close();
    }

    @Test
    void testSetPictureName() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] img = slTests.readFile("tomcat.png");
        HSLFPictureData data = ppt.addPicture(img, PictureType.PNG);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        pict.setPictureName("tomcat.png");
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));

        HSLFPictureShape p = (HSLFPictureShape)ppt.getSlides().get(0).getShapes().get(0);
        assertEquals("tomcat.png", p.getPictureName());
    }

    @Test
    void testPictureIndexIsOneBased() throws IOException {
        try (HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("ppt_with_png.ppt")) {
            HSLFPictureData picture = ppt.getPictureData().get(0);
            assertEquals(1, picture.getIndex());
        }
    }

    /**
     * Verify that it is possible for a user to change the contents of a {@link HSLFPictureData} using
     * {@link HSLFPictureData#setData(byte[])}, and that the changes are saved to the slideshow.
     */
    @Test
    void testEditPictureData() throws IOException {
        byte[] newImage = slTests.readFile("tomcat.png");
        ByteArrayOutputStream modifiedSlideShow = new ByteArrayOutputStream();

        // Load an existing slideshow and modify the image
        try (HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("ppt_with_png.ppt")) {
            HSLFPictureData picture = ppt.getPictureData().get(0);
            picture.setData(newImage);
            ppt.write(modifiedSlideShow);
        }

        // Load the modified slideshow and verify the image content
        try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(modifiedSlideShow.toByteArray()))) {
            HSLFPictureData picture = ppt.getPictureData().get(0);
            byte[] modifiedImageData = picture.getData();
            assertArrayEquals(newImage, modifiedImageData);
        }
    }

    /**
     * Verify that it is possible for a user to change the contents of an encrypted {@link HSLFPictureData} using
     * {@link HSLFPictureData#setData(byte[])}, and that the changes are saved to the slideshow.
     */
    @Test
    void testEditPictureDataEncrypted() throws IOException {
        byte[] newImage = slTests.readFile("tomcat.png");
        ByteArrayOutputStream modifiedSlideShow = new ByteArrayOutputStream();

        Biff8EncryptionKey.setCurrentUserPassword("password");
        try {
            // Load an existing slideshow and modify the image
            try (HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("ppt_with_png_encrypted.ppt")) {
                HSLFPictureData picture = ppt.getPictureData().get(0);
                picture.setData(newImage);
                ppt.write(modifiedSlideShow);
            }

            // Load the modified slideshow and verify the image content
            try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(modifiedSlideShow.toByteArray()))) {
                HSLFPictureData picture = ppt.getPictureData().get(0);
                byte[] modifiedImageData = picture.getData();
                assertArrayEquals(newImage, modifiedImageData);
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    /**
     * Verify that the {@link EscherBSERecord#getOffset()} values are modified for all images after the image being
     * changed.
     */
    @Test
    void testEditPictureDataRecordOffsetsAreShifted() throws IOException {
        int[] originalOffsets = {0, 12013, 15081, 34162, 59563};
        int[] modifiedOffsets = {0, 35, 3103, 22184, 47585};

        ByteArrayOutputStream inMemory = new ByteArrayOutputStream();
        try (HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("pictures.ppt")) {
            int[] offsets = ppt.getPictureData().stream().mapToInt(HSLFPictureData::getOffset).toArray();
            assertArrayEquals(originalOffsets, offsets);

            HSLFPictureData imageBeingChanged = ppt.getPictureData().get(0);
            // It doesn't matter that this isn't a valid image. We are just testing offsets here.
            imageBeingChanged.setData(new byte[10]);

            // Verify that the in-memory representations have all been updated
            offsets = ppt.getPictureData().stream().mapToInt(HSLFPictureData::getOffset).toArray();
            assertArrayEquals(modifiedOffsets, offsets);

            ppt.write(inMemory);
        }

        try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(inMemory.toByteArray()))) {

            // Verify that the persisted representations have all been updated
            int[] offsets = ppt.getPictureData().stream().mapToInt(HSLFPictureData::getOffset).toArray();
            assertArrayEquals(modifiedOffsets, offsets);
        }
    }

    /**
     * Verify that the {@link EscherBSERecord#getOffset()} values are modified for all images after the image being
     * changed, but assuming that the records are not stored in a sorted-by-offset fashion.
     *
     * We have not encountered a file that has meaningful data that is not sorted. However, we have encountered files
     * that have records with an offset of 0 interspersed between meaningful records. See {@code 53446.ppt} and
     * {@code alterman_security.ppt} for examples.
     */
    @Test
    void testEditPictureDataOutOfOrderRecords() throws IOException {
        int[] modifiedOffsets = {0, 35, 3103, 22184, 47585};

        ByteArrayOutputStream inMemory = new ByteArrayOutputStream();
        try (HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("pictures.ppt")) {

            // For this test we're going to intentionally manipulate the records into a shuffled order.
            EscherContainerRecord container = ppt.getPictureData().get(0).bStore;
            List<EscherRecord> children = container.getChildRecords();
            for (EscherRecord child : children) {
                container.removeChildRecord(child);
            }
            Collections.shuffle(children);
            for (EscherRecord child : children) {
                container.addChildRecord(child);
            }

            HSLFPictureData imageBeingChanged = ppt.getPictureData().get(0);
            // It doesn't matter that this isn't a valid image. We are just testing offsets here.
            imageBeingChanged.setData(new byte[10]);

            // Verify that the in-memory representations have all been updated
            int[] offsets = ppt.getPictureData().stream().mapToInt(HSLFPictureData::getOffset).toArray();
            Arrays.sort(offsets);
            assertArrayEquals(modifiedOffsets, offsets);

            ppt.write(inMemory);
        }

        try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(inMemory.toByteArray()))) {

            // Verify that the persisted representations have all been updated
            int[] offsets = ppt.getPictureData().stream().mapToInt(HSLFPictureData::getOffset).toArray();
            Arrays.sort(offsets);
            assertArrayEquals(modifiedOffsets, offsets);
        }
    }

    /**
     * Verify that a slideshow with records that have offsets not matching those of the pictures in the stream still
     * correctly pairs the records and pictures.
     */
    @Test
    void testSlideshowWithIncorrectOffsets() throws IOException {
        int[] originalOffsets;
        int originalNumberOfRecords;

        // Create a presentation that has records with unmatched offsets, but with matched UIDs.
        ByteArrayOutputStream inMemory = new ByteArrayOutputStream();
        try (HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("pictures.ppt")) {
            originalOffsets = ppt.getPictureData().stream().mapToInt(HSLFPictureData::getOffset).toArray();
            originalNumberOfRecords = ppt.getPictureData().get(0).bStore.getChildRecords().size();

            Random random = new Random();
            for (HSLFPictureData picture : ppt.getPictureData()) {
                // Bound is arbitrary and irrelevant to the test.
                picture.bse.setOffset(random.nextInt(500_000));
            }
            ppt.write(inMemory);
        }

        try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(inMemory.toByteArray()))) {

            // Verify that the offsets all got fixed.
            int[] offsets = ppt.getPictureData().stream().mapToInt(HSLFPictureData::getOffset).toArray();
            assertArrayEquals(originalOffsets, offsets);

            // Verify that there are the same number of records as in the original slideshow.
            int numberOfRecords = ppt.getPictureData().get(0).bStore.getChildRecords().size();
            assertEquals(originalNumberOfRecords, numberOfRecords);
        }
    }
}
