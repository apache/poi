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
package org.apache.poi.hpsf.extractor;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

public class TestHPFSPropertiesExtractor extends TestCase {
	private String dir;
	
    protected void setUp() throws Exception {
    	dir = System.getProperty("HPSF.testdata.path");
    	assertNotNull("HPSF.testdata.path not set", dir);
	}
    
	public void testNormalProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(
				new FileInputStream(new File(dir, "TestMickey.doc"))
		);
		HPFSPropertiesExtractor ext = new HPFSPropertiesExtractor(fs);
		ext.getText();
		
		// Check each bit in turn
		String sinfText = ext.getSummaryInformationText();
		String dinfText = ext.getDocumentSummaryInformationText();
		
		assertTrue(sinfText.indexOf("TEMPLATE = Normal") > -1);
		assertTrue(sinfText.indexOf("SUBJECT = sample subject") > -1);
		assertTrue(dinfText.indexOf("MANAGER = sample manager") > -1);
		assertTrue(dinfText.indexOf("COMPANY = sample company") > -1);
		
		// Now overall
		String text = ext.getText();
		assertTrue(text.indexOf("TEMPLATE = Normal") > -1);
		assertTrue(text.indexOf("SUBJECT = sample subject") > -1);
		assertTrue(text.indexOf("MANAGER = sample manager") > -1);
		assertTrue(text.indexOf("COMPANY = sample company") > -1);
	}
	public void testNormalUnicodeProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(
				new FileInputStream(new File(dir, "TestUnicode.xls"))
		);
		HPFSPropertiesExtractor ext = new HPFSPropertiesExtractor(fs);
		ext.getText();
		
		// Check each bit in turn
		String sinfText = ext.getSummaryInformationText();
		String dinfText = ext.getDocumentSummaryInformationText();
		
		assertTrue(sinfText.indexOf("AUTHOR = marshall") > -1);
		assertTrue(sinfText.indexOf("TITLE = Titel: \u00c4h") > -1);
		assertTrue(dinfText.indexOf("COMPANY = Schreiner") > -1);
		assertTrue(dinfText.indexOf("SCALE = false") > -1);
		
		// Now overall
		String text = ext.getText();
		assertTrue(text.indexOf("AUTHOR = marshall") > -1);
		assertTrue(text.indexOf("TITLE = Titel: \u00c4h") > -1);
		assertTrue(text.indexOf("COMPANY = Schreiner") > -1);
		assertTrue(text.indexOf("SCALE = false") > -1);
	}
	public void testCustomProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(
				new FileInputStream(new File(dir, "TestMickey.doc"))
		);
		HPFSPropertiesExtractor ext = new HPFSPropertiesExtractor(fs);

		// Custom properties are part of the document info stream
		String dinfText = ext.getDocumentSummaryInformationText();
		assertTrue(dinfText.indexOf("Client = sample client") > -1);
		assertTrue(dinfText.indexOf("Division = sample division") > -1);
		
		String text = ext.getText();
		assertTrue(text.indexOf("Client = sample client") > -1);
		assertTrue(text.indexOf("Division = sample division") > -1);
	}
    
    public void testConstructors() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(
				new FileInputStream(new File(dir, "TestUnicode.xls"))
		);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		ExcelExtractor excelExt = new ExcelExtractor(wb);
		
		String fsText = (new HPFSPropertiesExtractor(fs)).getText();
		String hwText = (new HPFSPropertiesExtractor(wb)).getText();
		String eeText = (new HPFSPropertiesExtractor(excelExt)).getText();
		
		assertEquals(fsText, hwText);
		assertEquals(fsText, eeText);
		
		assertTrue(fsText.indexOf("AUTHOR = marshall") > -1);
		assertTrue(fsText.indexOf("TITLE = Titel: \u00c4h") > -1);
    }
}
