
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
        


package org.apache.poi;


import junit.framework.TestCase;
import java.io.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.*;

/**
 * Tests that POIDocument correctly loads and saves the common
 *  (hspf) Document Properties.
 * 
 * This is part 1 of 2 of the tests - it only does the POIDocuments
 *  which are part of the Main (not scratchpad)
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestPOIDocumentMain extends TestCase {
	// The POI Documents to work on
	private POIDocument doc;
	private POIDocument doc2;
	// POIFS primed on the test (two different hssf) data
	private POIFSFileSystem pfs;
	private POIFSFileSystem pfs2;

	/**
	 * Set things up, using a PowerPoint document and 
	 *  a Word Document for our testing
	 */
    public void setUp() throws Exception {
		String dirnameHSSF = System.getProperty("HSSF.testdata.path");
		String filenameHSSF = dirnameHSSF + "/DateFormats.xls";
		String filenameHSSF2 = dirnameHSSF + "/StringFormulas.xls";
		
		FileInputStream fisHSSF = new FileInputStream(filenameHSSF);
		pfs = new POIFSFileSystem(fisHSSF);
		doc = new HSSFWorkbook(pfs);
		
		FileInputStream fisHSSF2 = new FileInputStream(filenameHSSF2);
		pfs2 = new POIFSFileSystem(fisHSSF2);
		doc2 = new HSSFWorkbook(pfs2);
	}
    
    public void testReadProperties() throws Exception {
    	// We should have both sets
    	assertNotNull(doc.getDocumentSummaryInformation());
    	assertNotNull(doc.getSummaryInformation());
    	
    	// Check they are as expected for the test doc
    	assertEquals("Administrator", doc.getSummaryInformation().getAuthor());
    	assertEquals(0, doc.getDocumentSummaryInformation().getByteCount());
    }
    	
    public void testReadProperties2() throws Exception {	
    	// Check again on the word one
    	assertNotNull(doc2.getDocumentSummaryInformation());
    	assertNotNull(doc2.getSummaryInformation());
    	
    	assertEquals("Avik Sengupta", doc2.getSummaryInformation().getAuthor());
    	assertEquals(null, doc2.getSummaryInformation().getKeywords());
    	assertEquals(0, doc2.getDocumentSummaryInformation().getByteCount());
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
