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

package org.apache.poi.hslf.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that if we load something up, get a TextRun, set the text
 *  to be the same as it was before, and write it all back out again,
 *  that we don't break anything in the process.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestTextRunReWrite {
	// HSLFSlideShow primed on the test data
	private HSLFSlideShowImpl hss;
	// HSLFSlideShow primed on the test data
	private HSLFSlideShow ss;
	// POIFS primed on the test data
	private POIFSFileSystem pfs;

	/**
	 * Load up a test PPT file with rich data
	 */
	@Before
    public void setUp() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		String filename = "Single_Coloured_Page_With_Fonts_and_Alignments.ppt";
		pfs = new POIFSFileSystem(slTests.openResourceAsStream(filename));
		hss = new HSLFSlideShowImpl(pfs);
		ss = new HSLFSlideShow(hss);
    }

    @Test
	public void testWritesOutTheSameNonRich() throws Exception {
    	// Grab the first text run on the first sheet
    	HSLFTextParagraph tr1 = ss.getSlides().get(0).getTextParagraphs().get(0);
    	HSLFTextParagraph tr2 = ss.getSlides().get(0).getTextParagraphs().get(1);

    	// Ensure the text lengths are as we'd expect to start with
    	assertEquals(1, ss.getSlides().length);
    	assertEquals(2, ss.getSlides().get(0).getTextParagraphs().length);
    	assertEquals(30, tr1.getRawText().length());
    	assertEquals(179, tr2.getRawText().length());

    	assertEquals(1, tr1.getTextRuns().length);
    	assertEquals(30, tr1.getTextRuns().get(0).getLength());
    	assertEquals(30, tr1.getTextRuns().get(0).getRawText().length());
    	assertEquals(31, tr1.getTextRuns().get(0)._getRawCharacterStyle().getCharactersCovered());
    	assertEquals(31, tr1.getTextRuns().get(0)._getRawParagraphStyle().getCharactersCovered());

    	// Set the text to be as it is now
    	tr1.setText( tr1.getRawText() );

    	// Check the text lengths are still right
    	assertEquals(30, tr1.getRawText().length());
    	assertEquals(179, tr2.getRawText().length());

    	assertEquals(1, tr1.getTextRuns().length);
    	assertEquals(30, tr1.getTextRuns().get(0).getLength());
    	assertEquals(30, tr1.getTextRuns().get(0).getRawText().length());
    	assertEquals(31, tr1.getTextRuns().get(0)._getRawCharacterStyle().getCharactersCovered());
    	assertEquals(31, tr1.getTextRuns().get(0)._getRawParagraphStyle().getCharactersCovered());


		// Write the slideshow out to a byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ss.write(baos);

		// Build an input stream of it
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// Use POIFS to query that lot
		POIFSFileSystem npfs = new POIFSFileSystem(bais);

		// Check that the "PowerPoint Document" sections have the same size
		DocumentEntry oProps = (DocumentEntry)pfs.getRoot().getEntry("PowerPoint Document");
		DocumentEntry nProps = (DocumentEntry)npfs.getRoot().getEntry("PowerPoint Document");
		assertEquals(oProps.getSize(),nProps.getSize());

		// Check that they contain the same data
		byte[] _oData = new byte[oProps.getSize()];
		byte[] _nData = new byte[nProps.getSize()];
		pfs.createDocumentInputStream("PowerPoint Document").read(_oData);
		npfs.createDocumentInputStream("PowerPoint Document").read(_nData);
		for(int i=0; i<_oData.length; i++) {
//			System.out.println(i + "\t" + Integer.toHexString(i));
			assertEquals(_oData[i], _nData[i]);
		}
	}

    @Test
    public void testWritesOutTheSameRich() throws Exception {
    	// Grab the first text run on the first sheet
    	HSLFTextParagraph tr1 = ss.getSlides().get(0).getTextParagraphs().get(0);

    	// Get the first rich text run
    	HSLFTextRun rtr1 = tr1.getTextRuns().get(0);


    	// Check that the text sizes are as expected
    	assertEquals(1, tr1.getTextRuns().length);
    	assertEquals(30, tr1.getRawText().length());
    	assertEquals(30, tr1.getTextRuns().get(0).getRawText().length());
    	assertEquals(30, rtr1.getLength());
    	assertEquals(30, rtr1.getRawText().length());
    	assertEquals(31, rtr1._getRawCharacterStyle().getCharactersCovered());
    	assertEquals(31, rtr1._getRawParagraphStyle().getCharactersCovered());

    	// Set the text to be as it is now
    	rtr1.setText( rtr1.getRawText() );
    	rtr1 = tr1.getTextRuns().get(0);

    	// Check that the text sizes are still as expected
    	assertEquals(1, tr1.getTextRuns().length);
    	assertEquals(30, tr1.getRawText().length());
    	assertEquals(30, tr1.getTextRuns().get(0).getRawText().length());
    	assertEquals(30, rtr1.getLength());
    	assertEquals(30, rtr1.getRawText().length());
    	assertEquals(31, rtr1._getRawCharacterStyle().getCharactersCovered());
    	assertEquals(31, rtr1._getRawParagraphStyle().getCharactersCovered());


		// Write the slideshow out to a byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ss.write(baos);

		// Build an input stream of it
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// Use POIFS to query that lot
		POIFSFileSystem npfs = new POIFSFileSystem(bais);

		// Check that the "PowerPoint Document" sections have the same size
		DocumentEntry oProps = (DocumentEntry)pfs.getRoot().getEntry("PowerPoint Document");
		DocumentEntry nProps = (DocumentEntry)npfs.getRoot().getEntry("PowerPoint Document");
		assertEquals(oProps.getSize(),nProps.getSize());

		// Check that they contain the same data
		byte[] _oData = new byte[oProps.getSize()];
		byte[] _nData = new byte[nProps.getSize()];
		pfs.createDocumentInputStream("PowerPoint Document").read(_oData);
		npfs.createDocumentInputStream("PowerPoint Document").read(_nData);
		for(int i=0; i<_oData.length; i++) {
//			System.out.println(i + "\t" + Integer.toHexString(i) + "\t" + _oData[i]);
			assertEquals(_oData[i], _nData[i]);
		}
	}
}
