
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
        


package org.apache.poi;


import junit.framework.TestCase;
import java.io.*;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.*;

/**
 * Tests that POIDocument correctly loads and saves the common
 *  (hspf) Document Properties
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestPOIDocument extends TestCase {
	// The POI Document to work on
	private POIDocument doc;
	// POIFS primed on the test (powerpoint) data
	private POIFSFileSystem pfs;

	/**
	 * Set things up, using a PowerPoint document for our testing
	 */
    public void setUp() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");
		String filename = dirname + "/basic_test_ppt_file.ppt";
		FileInputStream fis = new FileInputStream(filename);
		pfs = new POIFSFileSystem(fis);
		doc = new HSLFSlideShow(pfs);
	}
    
    public void testReadProperties() throws Exception {
    	// We should have both sets
    	assertNotNull(doc.getDocumentSummaryInformation());
    	assertNotNull(doc.getSummaryInformation());
    	
    	// Check they are as expected for the test doc
    	assertEquals("Hogwarts", doc.getSummaryInformation().getAuthor());
    	assertEquals(10598, doc.getDocumentSummaryInformation().getByteCount());
    }

    public void testWriteProperties() throws Exception {
    	// Just check we can write them back out into a filesystem
    	POIFSFileSystem outFS = new POIFSFileSystem();
    	doc.writeProperties(outFS);
    	
    	// Should now hold them
    	assertNotNull(
    			outFS.createDocumentInputStream("\005SummaryInformation")
    	);
    	assertNotNull(
    			outFS.createDocumentInputStream("\005DocumentSummaryInformation")
    	);
    }

    public void testWriteReadProperties() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
    	// Write them out
    	POIFSFileSystem outFS = new POIFSFileSystem();
    	doc.writeProperties(outFS);
    	outFS.writeFilesystem(baos);
    	
    	// Create a new version
    	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    	POIFSFileSystem inFS = new POIFSFileSystem(bais);
    	
    	// Check they're still there
    	doc.filesystem = inFS;
    	doc.readProperties();
    	
    	// Delegate test
    	testReadProperties();
    }
}
