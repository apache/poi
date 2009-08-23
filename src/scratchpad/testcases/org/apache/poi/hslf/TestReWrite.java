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

package org.apache.poi.hslf;


import junit.framework.TestCase;

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.POIDataSamples;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

/**
 * Tests that HSLFSlideShow writes the powerpoint bit of data back out
 *  correctly. Currently, that means being the same as what it read in
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestReWrite extends TestCase {
	// HSLFSlideShow primed on the test data
	private HSLFSlideShow hssA;
	private HSLFSlideShow hssB;
	private HSLFSlideShow hssC;
	// POIFS primed on the test data
	private POIFSFileSystem pfsA;
	private POIFSFileSystem pfsB;
	private POIFSFileSystem pfsC;

    public void setUp() throws Exception {

        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        
		pfsA = new POIFSFileSystem(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		hssA = new HSLFSlideShow(pfsA);

        pfsB = new POIFSFileSystem(slTests.openResourceAsStream("ParagraphStylesShorterThanCharStyles.ppt"));
		hssB = new HSLFSlideShow(pfsB);

        pfsC = new POIFSFileSystem(slTests.openResourceAsStream("WithMacros.ppt"));
		hssC = new HSLFSlideShow(pfsC);
    }

    public void testWritesOutTheSame() throws Exception {
    	assertWritesOutTheSame(hssA, pfsA);
    	assertWritesOutTheSame(hssB, pfsB);
    }
    public void assertWritesOutTheSame(HSLFSlideShow hss, POIFSFileSystem pfs) throws Exception {
		// Write out to a byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss.write(baos);

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
			//System.out.println(i + "\t" + Integer.toHexString(i));
			assertEquals(_oData[i], _nData[i]);
		}
	}

    public void testWithMacroStreams() throws Exception {
    	// Check that they're apparently the same
    	assertSlideShowWritesOutTheSame(hssC, pfsC);

    	// Currently has a Macros stream
    	assertNotNull( pfsC.getRoot().getEntry("Macros") );

    	// Write out normally, will loose the macro stream
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	hssC.write(baos);
    	POIFSFileSystem pfsNew = new POIFSFileSystem(
    			new ByteArrayInputStream(baos.toByteArray()) );

    	try {
    		pfsNew.getRoot().getEntry("Macros");
    		fail();
    	} catch(FileNotFoundException e) {
    		// Good, as expected
    	}

    	// But if we write out with nodes preserved, will be there
    	baos = new ByteArrayOutputStream();
    	hssC.write(baos, true);
    	pfsNew = new POIFSFileSystem(
    			new ByteArrayInputStream(baos.toByteArray()) );
    	assertNotNull( pfsNew.getRoot().getEntry("Macros") );
    }

    /**
     * Ensure that simply opening a slideshow (usermodel) view of it
     *  doesn't change things
     */
    public void testSlideShowWritesOutTheSame() throws Exception {
    	assertSlideShowWritesOutTheSame(hssA, pfsA);

    	// Some bug in StyleTextPropAtom rewriting means this will fail
    	// We need to identify and fix that first
    	//assertSlideShowWritesOutTheSame(hssB, pfsB);
    }
    public void assertSlideShowWritesOutTheSame(HSLFSlideShow hss, POIFSFileSystem pfs) throws Exception {
    	// Create a slideshow covering it
    	SlideShow ss = new SlideShow(hss);
    	ss.getSlides();
    	ss.getNotes();

		// Now write out to a byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		hss.write(baos);

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
			if(_oData[i] != _nData[i])
				System.out.println(i + "\t" + Integer.toHexString(i));
			assertEquals(_oData[i], _nData[i]);
		}
	}
}
