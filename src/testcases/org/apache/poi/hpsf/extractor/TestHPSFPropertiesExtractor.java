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

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.Thumbnail;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public final class TestHPSFPropertiesExtractor extends TestCase {
	private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();

	public void testNormalProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestMickey.doc"));
		HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs);
		try {
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
		} finally {
		    ext.close();
		}
	}

	public void testNormalUnicodeProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestUnicode.xls"));
		HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs);
		try {
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
		} finally {
		    ext.close();
		}
	}

	public void testCustomProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(
				_samples.openResourceAsStream("TestMickey.doc")
		);
		HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs);
		try {
    		// Custom properties are part of the document info stream
    		String dinfText = ext.getDocumentSummaryInformationText();
    		assertTrue(dinfText.indexOf("Client = sample client") > -1);
    		assertTrue(dinfText.indexOf("Division = sample division") > -1);
    
    		String text = ext.getText();
    		assertTrue(text.indexOf("Client = sample client") > -1);
    		assertTrue(text.indexOf("Division = sample division") > -1);
        } finally {
            ext.close();
        }
	}

	public void testConstructors() throws IOException {
		POIFSFileSystem fs;
		HSSFWorkbook wb;
		try {
			fs = new POIFSFileSystem(_samples.openResourceAsStream("TestUnicode.xls"));
			wb = new HSSFWorkbook(fs);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ExcelExtractor excelExt = new ExcelExtractor(wb);

		final String fsText;
		HPSFPropertiesExtractor fsExt = new HPSFPropertiesExtractor(fs);
		fsExt.setFilesystem(null); // Don't close re-used test resources!
		try {
		    fsText = fsExt.getText();
		} finally {
		    fsExt.close();
		}
        
        final String hwText; 
		HPSFPropertiesExtractor hwExt = new HPSFPropertiesExtractor(wb);
		hwExt.setFilesystem(null); // Don't close re-used test resources!
		try {
		    hwText = hwExt.getText();
		} finally {
		    hwExt.close();
		}
		
        final String eeText;
        HPSFPropertiesExtractor eeExt = new HPSFPropertiesExtractor(excelExt);
        eeExt.setFilesystem(null); // Don't close re-used test resources!
        try {
            eeText = eeExt.getText();
        } finally {
            eeExt.close();
        }

		assertEquals(fsText, hwText);
		assertEquals(fsText, eeText);

		assertTrue(fsText.indexOf("AUTHOR = marshall") > -1);
		assertTrue(fsText.indexOf("TITLE = Titel: \u00c4h") > -1);
		
		// Finally tidy
		wb.close();
	}

	public void test42726() throws IOException {
		HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(HSSFTestDataSamples.openSampleWorkbook("42726.xls"));
		try {
    		String txt = ext.getText();
    		assertTrue(txt.indexOf("PID_AUTHOR") != -1);
    		assertTrue(txt.indexOf("PID_EDITTIME") != -1);
    		assertTrue(txt.indexOf("PID_REVNUMBER") != -1);
    		assertTrue(txt.indexOf("PID_THUMBNAIL") != -1);
        } finally {
            ext.close();
        }
	}
	
    public void testThumbnail() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestThumbnail.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        Thumbnail thumbnail = new Thumbnail(wb.getSummaryInformation().getThumbnail());
        assertEquals(-1, thumbnail.getClipboardFormatTag());
        assertEquals(3, thumbnail.getClipboardFormat());
        assertNotNull(thumbnail.getThumbnailAsWMF());
        wb.close();
    }

    public void test52258() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestVisioWithCodepage.vsd"));
        HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs);
        try {
            assertNotNull(ext.getDocSummaryInformation());
            assertNotNull(ext.getDocumentSummaryInformationText());
            assertNotNull(ext.getSummaryInformation());
            assertNotNull(ext.getSummaryInformationText());
            assertNotNull(ext.getText());
        } finally {
            ext.close();
        }
    }
}
