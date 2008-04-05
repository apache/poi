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

package org.apache.poi.xssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import junit.framework.TestCase;

import org.openxml4j.opc.Package;

public class TestXSSFHyperlink extends TestCase {
    public TestXSSFHyperlink(String name) {
		super(name);
		
		// Use system out logger
	    System.setProperty(
	            "org.apache.poi.util.POILogger",
	            "org.apache.poi.util.SystemOutLogger"
	    );
	}
    
    public void testAddNew() throws Exception {
    	
    }

    public void testLoadExisting() throws Exception {
		File xml = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "WithMoreVariousData.xlsx"
		);
		assertTrue(xml.exists());
    	
		XSSFWorkbook workbook = new XSSFWorkbook(xml.toString());
		assertEquals(3, workbook.getNumberOfSheets());
		
		XSSFSheet sheet = (XSSFSheet)workbook.getSheetAt(0);

		// TODO - check hyperlinks
		//assertEquals(4, sheet.getNumHyperlinks());
	}

    public void testLoadSave() throws Exception {
		File xml = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "WithMoreVariousData.xlsx"
		);
		assertTrue(xml.exists());
    	
		XSSFWorkbook workbook = new XSSFWorkbook(xml.toString());
		assertEquals(3, workbook.getNumberOfSheets());

		// TODO - check hyperlinks
		
		// Write out, and check
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		workbook.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		
		// Load up again, check all links still there
		XSSFWorkbook wb2 = new XSSFWorkbook(Package.open(bais));
		assertEquals(3, wb2.getNumberOfSheets());
		assertNotNull(wb2.getSheetAt(0));
		assertNotNull(wb2.getSheetAt(1));
		assertNotNull(wb2.getSheetAt(2));

		// TODO
    }
    
}
