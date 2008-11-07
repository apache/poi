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

package org.apache.poi.xssf.eventusermodel;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.openxml4j.opc.Package;

/**
 * Tests for XSSFReader
 */
public class TestXSSFReader extends TestCase {
	private String dirName;
	
    public TestXSSFReader(String name) {
		super(name);
		
		dirName = System.getProperty("HSSF.testdata.path");
		assertNotNull(dirName);
		assertTrue( (new File(dirName)).exists() );
		
		// Use system out logger
	    System.setProperty(
	            "org.apache.poi.util.POILogger",
	            "org.apache.poi.util.SystemOutLogger"
	    );
	}
    
    public void testGetBits() throws Exception {
    	File f = new File(dirName, "SampleSS.xlsx");
    	Package pkg = Package.open(f.toString());
    	
    	XSSFReader r = new XSSFReader(pkg);
    	
    	assertNotNull(r.getWorkbookData());
    	assertNotNull(r.getSharedStringsData());
    	assertNotNull(r.getStylesData());
    	
    	assertNotNull(r.getSharedStringsTable());
    	assertNotNull(r.getStylesTable());
    }
    
    public void testStyles() throws Exception {
    	File f = new File(dirName, "SampleSS.xlsx");
    	Package pkg = Package.open(f.toString());
    	
    	XSSFReader r = new XSSFReader(pkg);
    	
    	assertEquals(3, r.getStylesTable().getFonts().size());
    	assertEquals(0, r.getStylesTable()._getNumberFormatSize());
    }
    
    public void testStrings() throws Exception {
    	File f = new File(dirName, "SampleSS.xlsx");
    	Package pkg = Package.open(f.toString());
    	
    	XSSFReader r = new XSSFReader(pkg);
    	
    	assertEquals(11, r.getSharedStringsTable().getItems().size());
    	assertEquals("Test spreadsheet", new XSSFRichTextString(r.getSharedStringsTable().getEntryAt(0)).toString());
    }
    
    public void testSheets() throws Exception {
    	File f = new File(dirName, "SampleSS.xlsx");
    	Package pkg = Package.open(f.toString());
    	
    	XSSFReader r = new XSSFReader(pkg);
    	byte[] data = new byte[4096]; 
    	
    	// By r:id
    	assertNotNull(r.getSheet("rId2"));
    	int read = IOUtils.readFully(r.getSheet("rId2"), data);
    	assertEquals(974, read);
    	
    	// All
    	Iterator<InputStream> it = r.getSheetsData();
    	
    	int count = 0;
    	while(it.hasNext()) {
    		count++;
    		InputStream inp = it.next();
    		assertNotNull(inp);
    		read = IOUtils.readFully(inp, data);
    		inp.close();
    		
    		assertTrue(read > 400);
    		assertTrue(read < 1500);
    	}
    	assertEquals(3, count);
    }

    /**
     * Check that the sheet iterator returns sheets in the logical order
     * (as they are defined in the workbook.xml)
     */
    public void testOrderOfSheets() throws Exception {
        File f = new File(dirName, "reordered_sheets.xlsx");
        Package pkg = Package.open(f.toString());

        XSSFReader r = new XSSFReader(pkg);

        String[] sheetNames = {"Sheet4", "Sheet2", "Sheet3", "Sheet1"};
        XSSFReader.SheetIterator it = (XSSFReader.SheetIterator)r.getSheetsData();

        int count = 0;
        while(it.hasNext()) {
            InputStream inp = it.next();
            assertNotNull(inp);
            inp.close();

            assertEquals(sheetNames[count], it.getSheetName());
            count++;
        }
        assertEquals(4, count);

    }
}
