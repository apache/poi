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
package org.apache.poi.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POITextExtractor;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;

/**
 * Test that the extractor factory plays nicely
 */
public class TestExtractorFactory extends TestCase {
	private String excel_dir;
	private String word_dir;
	private String powerpoint_dir;
	private String visio_dir;
	private String poifs_dir;
	
	private File txt;
	
	private File xls;
	private File xlsx;
        private File xltx;

	private File doc;
	private File docx;
        private File dotx;

	private File ppt;
	private File pptx;
	
	private File vsd;

	protected void setUp() throws Exception {
		super.setUp();
		
		excel_dir = System.getProperty("HSSF.testdata.path");
		word_dir = System.getProperty("HWPF.testdata.path");
		powerpoint_dir = System.getProperty("HSLF.testdata.path");
		visio_dir = System.getProperty("HDGF.testdata.path");
		poifs_dir = System.getProperty("POIFS.testdata.path");
		assertNotNull(excel_dir);
		assertNotNull(word_dir);
		assertNotNull(powerpoint_dir);
		assertNotNull(visio_dir);
		assertNotNull(poifs_dir);
		
		txt = new File(powerpoint_dir, "SampleShow.txt");
		
		xls = new File(excel_dir, "SampleSS.xls");
		xlsx = new File(excel_dir, "SampleSS.xlsx");
                xltx = new File(excel_dir, "test.xltx");

		doc = new File(word_dir, "SampleDoc.doc");
		docx = new File(word_dir, "SampleDoc.docx");
        dotx = new File(word_dir, "test.dotx");

		ppt = new File(powerpoint_dir, "SampleShow.ppt");
		pptx = new File(powerpoint_dir, "SampleShow.pptx");
		
		vsd = new File(visio_dir, "Test_Visio-Some_Random_Text.vsd");
	}

	public void testFile() throws Exception {
		// Excel
		assertTrue(
				ExtractorFactory.createExtractor(xls)
				instanceof ExcelExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(xls).getText().length() > 200
		);
		
		assertTrue(
				ExtractorFactory.createExtractor(xlsx)
				instanceof XSSFExcelExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(xlsx).getText().length() > 200
		);

                assertTrue(
                                ExtractorFactory.createExtractor(xltx)
                                instanceof XSSFExcelExtractor
                );
                assertTrue(
                                ExtractorFactory.createExtractor(xltx).getText().contains("test")
                );

		
		// Word
		assertTrue(
				ExtractorFactory.createExtractor(doc)
				instanceof WordExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(doc).getText().length() > 120
		);
		
		assertTrue(
				ExtractorFactory.createExtractor(docx)
				instanceof XWPFWordExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(docx).getText().length() > 120
		);

                assertTrue(
                                ExtractorFactory.createExtractor(dotx)
                                instanceof XWPFWordExtractor
                );
                assertTrue(
                                ExtractorFactory.createExtractor(dotx).getText().contains("Test")
                );

		// PowerPoint
		assertTrue(
				ExtractorFactory.createExtractor(ppt)
				instanceof PowerPointExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(ppt).getText().length() > 120
		);
		
		assertTrue(
				ExtractorFactory.createExtractor(pptx)
				instanceof XSLFPowerPointExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(pptx).getText().length() > 120
		);
		
		// Visio
		assertTrue(
				ExtractorFactory.createExtractor(vsd)
				instanceof VisioTextExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(vsd).getText().length() > 50
		);
		
		// Text
		try {
			ExtractorFactory.createExtractor(txt);
			fail();
		} catch(IllegalArgumentException e) {
			// Good
		}
	}
	
	public void testInputStream() throws Exception {
		// Excel
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(xls))
				instanceof ExcelExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(xls)).getText().length() > 200
		);
		
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(xlsx))
				instanceof XSSFExcelExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(xlsx)).getText().length() > 200
		);
		
		// Word
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(doc))
				instanceof WordExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(doc)).getText().length() > 120
		);
		
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(docx))
				instanceof XWPFWordExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(docx)).getText().length() > 120
		);
		
		// PowerPoint
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(ppt))
				instanceof PowerPointExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(ppt)).getText().length() > 120
		);
		
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(pptx))
				instanceof XSLFPowerPointExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(pptx)).getText().length() > 120
		);
		
		// Visio
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(vsd))
				instanceof VisioTextExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new FileInputStream(vsd)).getText().length() > 50
		);
		
		// Text
		try {
			ExtractorFactory.createExtractor(new FileInputStream(txt));
			fail();
		} catch(IllegalArgumentException e) {
			// Good
		}
	}
	
	public void testPOIFS() throws Exception {
		// Excel
		assertTrue(
				ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)))
				instanceof ExcelExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls))).getText().length() > 200
		);
		
		// Word
		assertTrue(
				ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc)))
				instanceof WordExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc))).getText().length() > 120
		);
		
		// PowerPoint
		assertTrue(
				ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(ppt)))
				instanceof PowerPointExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(ppt))).getText().length() > 120
		);
		
		// Visio
		assertTrue(
				ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(vsd)))
				instanceof VisioTextExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(vsd))).getText().length() > 50
		);
		
		// Text
		try {
			ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(txt)));
			fail();
		} catch(IOException e) {
			// Good
		}
	}
	
	public void testPackage() throws Exception {
		// Excel
		assertTrue(
				ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString()))
				instanceof XSSFExcelExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString())).getText().length() > 200
		);
		
		// Word
		assertTrue(
				ExtractorFactory.createExtractor(OPCPackage.open(docx.toString()))
				instanceof XWPFWordExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(OPCPackage.open(docx.toString())).getText().length() > 120
		);
		
		// PowerPoint
		assertTrue(
				ExtractorFactory.createExtractor(OPCPackage.open(pptx.toString()))
				instanceof XSLFPowerPointExtractor
		);
		assertTrue(
				ExtractorFactory.createExtractor(OPCPackage.open(pptx.toString())).getText().length() > 120
		);
		
		// Text
		try {
			ExtractorFactory.createExtractor(OPCPackage.open(txt.toString()));
			fail();
		} catch(InvalidOperationException e) {
			// Good
		}
	}

	/**
	 * Test embeded docs text extraction. For now, only
	 *  does poifs embeded, but will do ooxml ones 
	 *  at some point.
	 */
	public void testEmbeded() throws Exception {
		POIOLE2TextExtractor ext;
		POITextExtractor[] embeds;
		File f;
		
		// No embedings
		ext = (POIOLE2TextExtractor)
				ExtractorFactory.createExtractor(xls);
		embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);
		assertEquals(0, embeds.length);
		
		// Excel
		f = new File(poifs_dir, "excel_with_embeded.xls");
		ext = (POIOLE2TextExtractor)
				ExtractorFactory.createExtractor(f);
		embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

		assertEquals(6, embeds.length);
		int numWord = 0, numXls = 0, numPpt = 0;
        for(int i=0; i<embeds.length; i++) {
			assertTrue(embeds[i].getText().length() > 20);

            if(embeds[i] instanceof PowerPointExtractor) numPpt++;
            else if(embeds[i] instanceof ExcelExtractor) numXls++;
            else if(embeds[i] instanceof WordExtractor) numWord++;
        }
		assertEquals(2, numPpt);
        assertEquals(2, numXls);
        assertEquals(2, numWord);

        // Word
		f = new File(poifs_dir, "word_with_embeded.doc");
		ext = (POIOLE2TextExtractor)
				ExtractorFactory.createExtractor(f);
		embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);
		
        numWord = 0; numXls = 0; numPpt = 0;
		assertEquals(4, embeds.length);
		for(int i=0; i<embeds.length; i++) {
			assertTrue(embeds[i].getText().length() > 20);
            if(embeds[i] instanceof PowerPointExtractor) numPpt++;
            else if(embeds[i] instanceof ExcelExtractor) numXls++;
            else if(embeds[i] instanceof WordExtractor) numWord++;
		}
        assertEquals(1, numPpt);
        assertEquals(2, numXls);
        assertEquals(1, numWord);

		// TODO - PowerPoint
		// TODO - Visio
	}
}
