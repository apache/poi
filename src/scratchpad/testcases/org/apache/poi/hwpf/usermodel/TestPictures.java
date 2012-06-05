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

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.PicturesTable;

/**
 * Test the picture handling
 *
 * @author Nick Burch
 */
public final class TestPictures extends TestCase {

	/**
	 * two jpegs
	 */
	public void testTwoImages() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("two_images.doc");
		List<Picture> pics = doc.getPicturesTable().getAllPictures();

		assertNotNull(pics);
		assertEquals(pics.size(), 2);
		for(int i=0; i<pics.size(); i++) {
			Picture pic = (Picture)pics.get(i);
			assertNotNull(pic.suggestFileExtension());
			assertNotNull(pic.suggestFullFileName());
		}

		Picture picA = pics.get(0);
		Picture picB = pics.get(1);
		assertEquals("jpg", picA.suggestFileExtension());
		assertEquals("jpg", picA.suggestFileExtension());
	}

	/**
	 * pngs and jpegs
	 */
	public void testDifferentImages() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("testPictures.doc");
		List<Picture> pics = doc.getPicturesTable().getAllPictures();

		assertNotNull(pics);
		assertEquals(7, pics.size());
		for(int i=0; i<pics.size(); i++) {
			Picture pic = (Picture)pics.get(i);
			assertNotNull(pic.suggestFileExtension());
			assertNotNull(pic.suggestFullFileName());
		}

		assertEquals("jpg", pics.get(0).suggestFileExtension());
		assertEquals("image/jpeg", pics.get(0).getMimeType());
		assertEquals("jpg", pics.get(1).suggestFileExtension());
      assertEquals("image/jpeg", pics.get(1).getMimeType());
		assertEquals("png", pics.get(3).suggestFileExtension());
      assertEquals("image/png", pics.get(3).getMimeType());
		assertEquals("png", pics.get(4).suggestFileExtension());
      assertEquals("image/png", pics.get(4).getMimeType());
		assertEquals("wmf", pics.get(5).suggestFileExtension());
      assertEquals("image/x-wmf", pics.get(5).getMimeType());
		assertEquals("jpg", pics.get(6).suggestFileExtension());
      assertEquals("image/jpeg", pics.get(6).getMimeType());
	}

	/**
	 * emf image, nice and simple
	 */
	public void testEmfImage() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("vector_image.doc");
		List<Picture> pics = doc.getPicturesTable().getAllPictures();

		assertNotNull(pics);
		assertEquals(1, pics.size());

		Picture pic = pics.get(0);
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

	public void testPicturesWithTable() {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug44603.doc");

		List<Picture> pics = doc.getPicturesTable().getAllPictures();
		assertEquals(2, pics.size());
	}

	public void testPicturesInHeader() {
	   HWPFDocument doc = HWPFTestDataSamples.openSampleFile("header_image.doc");

	   List<Picture> pics = doc.getPicturesTable().getAllPictures();
	   assertEquals(2, pics.size());
	}

    public void testFastSaved() {
       HWPFDocument doc = HWPFTestDataSamples.openSampleFile("rasp.doc");

       doc.getPicturesTable().getAllPictures(); // just check that we do not throw Exception
    }

    public void testFastSaved2() {
       HWPFDocument doc = HWPFTestDataSamples.openSampleFile("o_kurs.doc");

       doc.getPicturesTable().getAllPictures(); // just check that we do not throw Exception
    }

    public void testFastSaved3() {
       HWPFDocument doc = HWPFTestDataSamples.openSampleFile("ob_is.doc");

       doc.getPicturesTable().getAllPictures(); // just check that we do not throw Exception
    }

    /**
     * When you embed another office document into Word, it stores
     *  a rendered "icon" picture of what that document looks like.
     * This image is re-created when you edit the embeded document,
     *  then used as-is to speed things up.
     * Check that we can properly read one of these
     */
    public void testEmbededDocumentIcon() throws Exception {
       // This file has two embeded excel files, an embeded powerpoint
       //   file and an embeded word file, in that order
       HWPFDocument doc = HWPFTestDataSamples.openSampleFile("word_with_embeded.doc");
       
       // Check we don't break loading the pictures
       doc.getPicturesTable().getAllPictures();
       PicturesTable pictureTable = doc.getPicturesTable();
       
       // Check the text, and its embeded images
       Paragraph p;
       Range r = doc.getRange();
       assertEquals(1, r.numSections());
       assertEquals(5, r.numParagraphs());
       
       p = r.getParagraph(0);
       assertEquals(2, p.numCharacterRuns());
       assertEquals("I have lots of embedded files in me\r", p.text());
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(0)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(1)));
       
       p = r.getParagraph(1);
       assertEquals(5, p.numCharacterRuns());
       assertEquals("\u0013 EMBED Excel.Sheet.8  \u0014\u0001\u0015\r", p.text());
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(0)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(1)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(2)));
       assertEquals(true,  pictureTable.hasPicture(p.getCharacterRun(3)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(4)));
       
       p = r.getParagraph(2);
       assertEquals(6, p.numCharacterRuns());
       assertEquals("\u0013 EMBED Excel.Sheet.8  \u0014\u0001\u0015\r", p.text());
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(0)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(1)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(2)));
       assertEquals(true,  pictureTable.hasPicture(p.getCharacterRun(3)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(4)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(5)));
       
       p = r.getParagraph(3);
       assertEquals(6, p.numCharacterRuns());
       assertEquals("\u0013 EMBED PowerPoint.Show.8  \u0014\u0001\u0015\r", p.text());
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(0)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(1)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(2)));
       assertEquals(true,  pictureTable.hasPicture(p.getCharacterRun(3)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(4)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(5)));
       
       p = r.getParagraph(4);
       assertEquals(6, p.numCharacterRuns());
       assertEquals("\u0013 EMBED Word.Document.8 \\s \u0014\u0001\u0015\r", p.text());
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(0)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(1)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(2)));
       assertEquals(true,  pictureTable.hasPicture(p.getCharacterRun(3)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(4)));
       assertEquals(false, pictureTable.hasPicture(p.getCharacterRun(5)));

       // Look at the pictures table
       List<Picture> pictures = pictureTable.getAllPictures();
       assertEquals(4, pictures.size());

        Picture picture = pictures.get( 0 );
        assertEquals( "emf", picture.suggestFileExtension() );
        assertEquals( "0.emf", picture.suggestFullFileName() );
        assertEquals( "image/x-emf", picture.getMimeType() );

        picture = pictures.get( 1 );
        assertEquals( "emf", picture.suggestFileExtension() );
        assertEquals( "469.emf", picture.suggestFullFileName() );
        assertEquals( "image/x-emf", picture.getMimeType() );

        picture = pictures.get( 2 );
        assertEquals( "emf", picture.suggestFileExtension() );
        assertEquals( "8c7.emf", picture.suggestFullFileName() );
        assertEquals( "image/x-emf", picture.getMimeType() );

        picture = pictures.get( 3 );
        assertEquals( "emf", picture.suggestFileExtension() );
        assertEquals( "10a8.emf", picture.suggestFullFileName() );
        assertEquals( "image/x-emf", picture.getMimeType() );
    }

    public void testEquation()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "equation.doc" );
        PicturesTable pictures = doc.getPicturesTable();

        final List<Picture> allPictures = pictures.getAllPictures();
        assertEquals( 1, allPictures.size() );

        Picture picture = allPictures.get( 0 );
        assertNotNull( picture );
        assertEquals( PictureType.EMF, picture.suggestPictureType() );
        assertEquals( PictureType.EMF.getExtension(),
                picture.suggestFileExtension() );
        assertEquals( PictureType.EMF.getMime(), picture.getMimeType() );
        assertEquals( "0.emf", picture.suggestFullFileName() );
    }

    /**
     * In word you can have floating or fixed pictures.
     * Fixed have a \u0001 in place with an offset to the
     *  picture data.
     * Floating have a \u0008 in place, which references a
     *  \u0001 which has the offset. More than one can
     *  reference the same \u0001
     */
    public void testFloatingPictures() throws Exception {
       HWPFDocument doc = HWPFTestDataSamples.openSampleFile("FloatingPictures.doc");
       PicturesTable pictures = doc.getPicturesTable();
       
       // There are 19 images in the picture, but some are
       //  duplicate floating ones
       assertEquals(17, pictures.getAllPictures().size());
       
       int plain8s = 0;
       int escher8s = 0;
       int image1s = 0;
       
       Range r = doc.getRange();
       for(int np=0; np < r.numParagraphs(); np++) {
          Paragraph p = r.getParagraph(np);
          for(int nc=0; nc < p.numCharacterRuns(); nc++) {
             CharacterRun cr = p.getCharacterRun(nc);
             if(pictures.hasPicture(cr)) {
                image1s++;
             } else if(pictures.hasEscherPicture(cr)) {
                escher8s++;
             } else if(cr.text().startsWith("\u0008")) {
                plain8s++;
             }
          }
       }
       // Total is 20, as the 4 escher 8s all reference
       //  the same regular image
       assertEquals(16, image1s);
       assertEquals(4,  escher8s);
       assertEquals(0, plain8s);
    }

    @SuppressWarnings( "deprecation" )
    public void testCroppedPictures() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("testCroppedPictures.doc");
        List<Picture> pics = doc.getPicturesTable().getAllPictures();

        assertNotNull(pics);
        assertEquals(2, pics.size());

        Picture pic1 = pics.get(0);
        assertEquals(27, pic1.getAspectRatioX());
        assertEquals(270, pic1.getHorizontalScalingFactor());
        assertEquals(27, pic1.getAspectRatioY());
        assertEquals(271, pic1.getVerticalScalingFactor());
        assertEquals(12000, pic1.getDxaGoal());       // 21.17 cm / 2.54 cm/inch * 72dpi * 20 = 12000
        assertEquals(9000, pic1.getDyaGoal());        // 15.88 cm / 2.54 cm/inch * 72dpi * 20 = 9000
        assertEquals(0, pic1.getDxaCropLeft());
        assertEquals(0, pic1.getDxaCropRight());
        assertEquals(0, pic1.getDyaCropTop());
        assertEquals(0, pic1.getDyaCropBottom());

        Picture pic2 = pics.get(1);
        System.out.println(pic2.getWidth());
        assertEquals(76, pic2.getAspectRatioX());
        assertEquals(764, pic2.getHorizontalScalingFactor());
        assertEquals(68, pic2.getAspectRatioY());
        assertEquals(685, pic2.getVerticalScalingFactor());
        assertEquals(12000, pic2.getDxaGoal());       // 21.17 cm / 2.54 cm/inch * 72dpi * 20 = 12000
        assertEquals(9000, pic2.getDyaGoal());        // 15.88 cm / 2.54 cm/inch * 72dpi * 20 = 9000
        assertEquals(0, pic2.getDxaCropLeft());       // TODO YK: The Picture is cropped but HWPF reads the crop parameters all zeros
        assertEquals(0, pic2.getDxaCropRight());
        assertEquals(0, pic2.getDyaCropTop());
        assertEquals(0, pic2.getDyaCropBottom());
    }

    public void testPictureDetectionWithPNG() throws Exception {
        HWPFDocument document = HWPFTestDataSamples.openSampleFile("PngPicture.doc");
        PicturesTable pictureTable = document.getPicturesTable();
        
        assertEquals(1, pictureTable.getAllPictures().size());
        
        Picture p = pictureTable.getAllPictures().get(0);
        assertEquals(PictureType.PNG, p.suggestPictureType());
        assertEquals("png", p.suggestFileExtension());
    }
    
    public void testPictureWithAlternativeText() throws Exception {
        HWPFDocument document = HWPFTestDataSamples.openSampleFile("Picture_Alternative_Text.doc");
        PicturesTable pictureTable = document.getPicturesTable();
        Picture picture = pictureTable.getAllPictures().get(0);

        assertEquals("This is the alternative text for the picture.", picture.getDescription());
    }
}
