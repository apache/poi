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

package org.apache.poi.hssf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LocaleUtil;
import org.junit.After;
import org.junit.Test;

/**
 *
 */
public final class TestExcelExtractor {
    private static ExcelExtractor createExtractor(String sampleFileName) throws IOException {
		File file = HSSFTestDataSamples.getSampleFile(sampleFileName);
        POIFSFileSystem fs = new POIFSFileSystem(file);
        ExcelExtractor extractor = new ExcelExtractor(fs);
        extractor.setFilesystem(fs);
        return extractor;
	}

	@Test
	public void testSimple() throws IOException {
		try (ExcelExtractor extractor = createExtractor("Simple.xls")) {
			assertEquals("Sheet1\nreplaceMe\nSheet2\nSheet3\n", extractor.getText());

			// Now turn off sheet names
			extractor.setIncludeSheetNames(false);
			assertEquals("replaceMe\n", extractor.getText());
		}
	}

	@Test
	public void testNumericFormula() throws IOException {
		try (ExcelExtractor extractor = createExtractor("sumifformula.xls")) {
			assertEquals(
					"Sheet1\n" +
							"1000\t1\t5\n" +
							"2000\t2\n" +
							"3000\t3\n" +
							"4000\t4\n" +
							"5000\t5\n" +
							"Sheet2\nSheet3\n",
					extractor.getText()
			);

			extractor.setFormulasNotResults(true);

			assertEquals(
					"Sheet1\n" +
							"1000\t1\tSUMIF(A1:A5,\">4000\",B1:B5)\n" +
							"2000\t2\n" +
							"3000\t3\n" +
							"4000\t4\n" +
							"5000\t5\n" +
							"Sheet2\nSheet3\n",
					extractor.getText()
			);
		}
	}

	@Test
	public void testWithContinueRecords() throws IOException {
		try (ExcelExtractor extractor = createExtractor("StringContinueRecords.xls")) {
			// Has masses of text
			// Until we fixed bug #41064, this would've
			//   failed by now
			assertTrue(extractor.getText().length() > 40960);
		}
	}

	@Test
    public void testStringConcat() throws IOException {
		try (ExcelExtractor extractor = createExtractor("SimpleWithFormula.xls")) {
			// Comes out as NaN if treated as a number
			// And as XYZ if treated as a string
			assertEquals("Sheet1\nreplaceme\nreplaceme\nreplacemereplaceme\nSheet2\nSheet3\n", extractor.getText());

			extractor.setFormulasNotResults(true);

			assertEquals("Sheet1\nreplaceme\nreplaceme\nCONCATENATE(A1,A2)\nSheet2\nSheet3\n", extractor.getText());
		}
	}

	@Test
    public void testStringFormula() throws IOException {
		try (ExcelExtractor extractor = createExtractor("StringFormulas.xls")) {
			// Comes out as NaN if treated as a number
			// And as XYZ if treated as a string
			assertEquals("Sheet1\nXYZ\nSheet2\nSheet3\n", extractor.getText());

			extractor.setFormulasNotResults(true);

			assertEquals("Sheet1\nUPPER(\"xyz\")\nSheet2\nSheet3\n", extractor.getText());
		}
	}


	@Test
    public void testEventExtractor() throws Exception {
		// First up, a simple file with string
		//  based formulas in it
		EventBasedExcelExtractor extractor1 = null;
		try {
	        extractor1 = new EventBasedExcelExtractor(
	                new POIFSFileSystem(
	                        HSSFTestDataSamples.openSampleFileStream("SimpleWithFormula.xls")
	                )
	        );
    		extractor1.setIncludeSheetNames(true);
        
    		String text = extractor1.getText();
    		assertEquals("Sheet1\nreplaceme\nreplaceme\nreplacemereplaceme\nSheet2\nSheet3\n", text);
    
    		extractor1.setIncludeSheetNames(false);
    		extractor1.setFormulasNotResults(true);
    
    		text = extractor1.getText();
    		assertEquals("replaceme\nreplaceme\nCONCATENATE(A1,A2)\n", text);
		} finally {
		    if (extractor1 != null) extractor1.close();
		}

		// Now, a slightly longer file with numeric formulas
		EventBasedExcelExtractor extractor2 = null;
		try {
		    extractor2 = new EventBasedExcelExtractor(
	                new POIFSFileSystem(
	                        HSSFTestDataSamples.openSampleFileStream("sumifformula.xls")
	                )
	        );		    
		    
    		extractor2.setIncludeSheetNames(false);
    		extractor2.setFormulasNotResults(true);
    
    		String text = extractor2.getText();
    		assertEquals(
    				"1000\t1\tSUMIF(A1:A5,\">4000\",B1:B5)\n" +
    				"2000\t2\n" +
    				"3000\t3\n" +
    				"4000\t4\n" +
    				"5000\t5\n",
    				text
    		);
		} finally {
		    if (extractor2 != null) extractor2.close();
		}
	}

	@Test
    public void testWithComments() throws IOException {
		try (ExcelExtractor extractor = createExtractor("SimpleWithComments.xls")) {
			extractor.setIncludeSheetNames(false);

			// Check without comments
			assertEquals(
					"1\tone\n" +
							"2\ttwo\n" +
							"3\tthree\n",
					extractor.getText()
			);

			// Now with
			extractor.setIncludeCellComments(true);
			assertEquals(
					"1\tone Comment by Yegor Kozlov: Yegor Kozlov: first cell\n" +
							"2\ttwo Comment by Yegor Kozlov: Yegor Kozlov: second cell\n" +
							"3\tthree Comment by Yegor Kozlov: Yegor Kozlov: third cell\n",
					extractor.getText()
			);
		}
	}

	@Test
    public void testWithBlank() throws IOException {
		try (ExcelExtractor extractor = createExtractor("MissingBits.xls")) {
			String def = extractor.getText();
			extractor.setIncludeBlankCells(true);
			String padded = extractor.getText();

			assertStartsWith(def,
					"Sheet1\n" +
							"&[TAB]\t\n" +
							"Hello\n" +
							"11\t23\n"
			);

			assertStartsWith(padded,
					"Sheet1\n" +
							"&[TAB]\t\n" +
							"Hello\n" +
							"11\t\t\t23\n"
			);
		}
	}

	@Test
    public void testFormatting() throws Exception {
	    Locale userLocale = LocaleUtil.getUserLocale();
	    LocaleUtil.setUserLocale(Locale.ROOT);
	    try (ExcelExtractor extractor = createExtractor("Formatting.xls")) {
            extractor.setIncludeBlankCells(false);
            extractor.setIncludeSheetNames(false);
            String text = extractor.getText();
            
            // Note - not all the formats in the file
            //  actually quite match what they claim to
            //  be, as some are auto-local builtins...
            
            assertStartsWith(text, "Dates, all 24th November 2006\n");
            assertContains(text, "yyyy/mm/dd\t2006/11/24\n");
            assertContains(text, "yyyy-mm-dd\t2006-11-24\n");
            assertContains(text, "dd-mm-yy\t24-11-06\n");
            
            assertContains(text, "nn.nn\t10.52\n");
            assertContains(text, "nn.nnn\t10.520\n");
            assertContains(text, "\u00a3nn.nn\t\u00a310.52\n");
	    } finally {
	        LocaleUtil.setUserLocale(userLocale);
	    }
	}

	/**
	 * Embeded in a non-excel file
	 */
	@Test
    public void testWithEmbeded() throws Exception {
		POIFSFileSystem fs = null;
		
		HSSFWorkbook wbA = null, wbB = null;
		ExcelExtractor exA = null, exB = null;

		try {
	        fs = new POIFSFileSystem(POIDataSamples.getDocumentInstance().getFile("word_with_embeded.doc"));

	        DirectoryNode objPool = (DirectoryNode) fs.getRoot().getEntry("ObjectPool");
	        DirectoryNode dirA = (DirectoryNode) objPool.getEntry("_1269427460");
	        DirectoryNode dirB = (DirectoryNode) objPool.getEntry("_1269427461");
		    
		    wbA = new HSSFWorkbook(dirA, fs, true);
	        exA = new ExcelExtractor(wbA);
	        wbB = new HSSFWorkbook(dirB, fs, true);
            exB = new ExcelExtractor(wbB);

            assertEquals("Sheet1\nTest excel file\nThis is the first file\nSheet2\nSheet3\n", exA.getText());
    		assertEquals("Sample Excel", exA.getSummaryInformation().getTitle());
    		assertEquals("Sheet1\nAnother excel file\nThis is the second file\nSheet2\nSheet3\n", exB.getText());
    		assertEquals("Sample Excel 2", exB.getSummaryInformation().getTitle());
		} finally {
            if (exB != null) exB.close();
            if (wbB != null) wbB.close();
            if (exA != null) exA.close();
            if (wbA != null) wbA.close();
            if (fs != null) fs.close();
		}
	}

	/**
	 * Excel embeded in excel
	 */
	@Test
    public void testWithEmbededInOwn() throws Exception {
		POIDataSamples ssSamples = POIDataSamples.getSpreadSheetInstance();
		POIFSFileSystem fs = null;
		HSSFWorkbook wbA = null, wbB = null;
		ExcelExtractor exA = null, exB = null, ex = null;

		try {
	        fs = new POIFSFileSystem(ssSamples.getFile("excel_with_embeded.xls"));

	        DirectoryNode dirA = (DirectoryNode) fs.getRoot().getEntry("MBD0000A3B5");
	        DirectoryNode dirB = (DirectoryNode) fs.getRoot().getEntry("MBD0000A3B4");

	        wbA = new HSSFWorkbook(dirA, fs, true);
	        wbB = new HSSFWorkbook(dirB, fs, true);

	        exA = new ExcelExtractor(wbA);
    		exB = new ExcelExtractor(wbB);
    		assertEquals("Sheet1\nTest excel file\nThis is the first file\nSheet2\nSheet3\n", exA.getText());
    		assertEquals("Sample Excel", exA.getSummaryInformation().getTitle());
    
    		assertEquals("Sheet1\nAnother excel file\nThis is the second file\nSheet2\nSheet3\n", exB.getText());
    		assertEquals("Sample Excel 2", exB.getSummaryInformation().getTitle());
        
    		// And the base file too
    		ex = new ExcelExtractor(fs);
    		assertEquals("Sheet1\nI have lots of embeded files in me\nSheet2\nSheet3\n", ex.getText());
    		assertEquals("Excel With Embeded", ex.getSummaryInformation().getTitle());
		} finally {
		    if (ex != null) ex.close();
            if (exB != null) exB.close();
            if (exA != null) exA.close();
            if (wbB != null) wbB.close();
            if (wbA != null) wbA.close();
            if (fs != null) fs.close();
        }
	}

	/**
	 * Test that we get text from headers and footers
	 */
	@Test
    public void test45538() throws IOException {
		String[] files = {
			"45538_classic_Footer.xls", "45538_form_Footer.xls",
			"45538_classic_Header.xls", "45538_form_Header.xls"
		};
		for (String file : files) {
			try (ExcelExtractor extractor = createExtractor(file)) {
				String text = extractor.getText();
				assertContains(file, text, "testdoc");
				assertContains(file, text, "test phrase");
			}
		}
	}

	@Test
    public void testPassword() throws IOException {
		Biff8EncryptionKey.setCurrentUserPassword("password");
		try (ExcelExtractor extractor = createExtractor("password.xls")) {
			String text = extractor.getText();
			assertContains(text, "ZIP");
		} finally {
			Biff8EncryptionKey.setCurrentUserPassword(null);

		}
	}

	@Test
    public void testNullPointerException() throws IOException {
		try (ExcelExtractor extractor = createExtractor("ar.org.apsme.www_Form%20Inscripcion%20Curso%20NO%20Socios.xls")) {
			assertNotNull(extractor);
			assertNotNull(extractor.getText());
		}
	}

	@Test
	public void test61045() throws IOException {
		//bug 61045. File is govdocs1 626534
		try (ExcelExtractor extractor = createExtractor("61045_govdocs1_626534.xls")) {
			String txt = extractor.getText();
			assertContains(txt, "NONBUSINESS");
		}
	}
}
