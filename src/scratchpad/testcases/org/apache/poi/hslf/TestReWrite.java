
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        


package org.apache.poi.hslf;


import junit.framework.TestCase;
import java.io.*;

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.poifs.filesystem.*;

/**
 * Tests that HSLFSlideShow writes the powerpoint bit of data back out
 *  correctly. Currently, that means being the same as what it read in
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestReWrite extends TestCase {
	// HSLFSlideShow primed on the test data
	private HSLFSlideShow hss;
	// POIFS primed on the test data
	private POIFSFileSystem pfs;

    public void setUp() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		String filename = dirname + "/basic_test_ppt_file.ppt";
		FileInputStream fis = new FileInputStream(filename);
		pfs = new POIFSFileSystem(fis);
		hss = new HSLFSlideShow(pfs);
    }

    public void testWritesOutTheSame() throws Exception {
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
			System.out.println(i + "\t" + Integer.toHexString(i));
			assertEquals(_oData[i], _nData[i]);
		}
	}

    /**
     * Ensure that simply opening a slideshow (usermodel) view of it
     *  doesn't change things 
     */
    public void testSlideShowWritesOutTheSame() throws Exception {
    	// Create a slideshow covering it
    	SlideShow ss = new SlideShow(hss);
    	
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
			System.out.println(i + "\t" + Integer.toHexString(i));
			assertEquals(_oData[i], _nData[i]);
		}
	}
}
