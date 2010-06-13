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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 *
 */
public final class TestExcelExtractor extends TestCase {

	private static ExcelExtractor createExtractor(String sampleFileName) {

		InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);

		try {
			return new ExcelExtractor(new POIFSFileSystem(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public void testSimple() {

		ExcelExtractor extractor = createExtractor("Simple.xls");

		assertEquals("Sheet1\nreplaceMe\nSheet2\nSheet3\n", extractor.getText());

		// Now turn off sheet names
		extractor.setIncludeSheetNames(false);
		assertEquals("replaceMe\n", extractor.getText());
	}

	public void testNumericFormula() {

		ExcelExtractor extractor = createExtractor("sumifformula.xls");

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

	public void testwithContinueRecords() {

		ExcelExtractor extractor = createExtractor("StringContinueRecords.xls");

		extractor.getText();

		// Has masses of text
		// Until we fixed bug #41064, this would've
		//   failed by now
		assertTrue(extractor.getText().length() > 40960);
	}

	public void testStringConcat() {

		ExcelExtractor extractor = createExtractor("SimpleWithFormula.xls");

		// Comes out as NaN if treated as a number
		// And as XYZ if treated as a string
		assertEquals("Sheet1\nreplaceme\nreplaceme\nreplacemereplaceme\nSheet2\nSheet3\n", extractor.getText());

		extractor.setFormulasNotResults(true);

		assertEquals("Sheet1\nreplaceme\nreplaceme\nCONCATENATE(A1,A2)\nSheet2\nSheet3\n", extractor.getText());
	}

	public void testStringFormula() {

		ExcelExtractor extractor = createExtractor("StringFormulas.xls");

		// Comes out as NaN if treated as a number
		// And as XYZ if treated as a string
		assertEquals("Sheet1\nXYZ\nSheet2\nSheet3\n", extractor.getText());

		extractor.setFormulasNotResults(true);

		assertEquals("Sheet1\nUPPER(\"xyz\")\nSheet2\nSheet3\n", extractor.getText());
	}


	public void testEventExtractor() throws Exception {
		EventBasedExcelExtractor extractor;

		// First up, a simple file with string
		//  based formulas in it
		extractor = new EventBasedExcelExtractor(
				new POIFSFileSystem(
						HSSFTestDataSamples.openSampleFileStream("SimpleWithFormula.xls")
				)
		);
		extractor.setIncludeSheetNames(true);

		String text = extractor.getText();
		assertEquals("Sheet1\nreplaceme\nreplaceme\nreplacemereplaceme\nSheet2\nSheet3\n", text);

		extractor.setIncludeSheetNames(false);
		extractor.setFormulasNotResults(true);

		text = extractor.getText();
		assertEquals("replaceme\nreplaceme\nCONCATENATE(A1,A2)\n", text);


		// Now, a slightly longer file with numeric formulas
		extractor = new EventBasedExcelExtractor(
				new POIFSFileSystem(
						HSSFTestDataSamples.openSampleFileStream("sumifformula.xls")
				)
		);
		extractor.setIncludeSheetNames(false);
		extractor.setFormulasNotResults(true);

		text = extractor.getText();
		assertEquals(
				"1000\t1\tSUMIF(A1:A5,\">4000\",B1:B5)\n" +
				"2000\t2\n" +
				"3000\t3\n" +
				"4000\t4\n" +
				"5000\t5\n",
				text
		);
	}

	public void testWithComments() {
		ExcelExtractor extractor = createExtractor("SimpleWithComments.xls");
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

	public void testWithBlank() {
		ExcelExtractor extractor = createExtractor("MissingBits.xls");
		String def = extractor.getText();
		extractor.setIncludeBlankCells(true);
		String padded = extractor.getText();

		assertTrue(def.startsWith(
				"Sheet1\n" +
				"&[TAB]\t\n" +
				"Hello\n" +
				"11\t23\n"
		));

		assertTrue(padded.startsWith(
				"Sheet1\n" +
				"&[TAB]\t\n" +
				"Hello\n" +
				"11\t\t\t23\n"
		));
	}

	public void testFormatting() throws Exception {
      ExcelExtractor extractor = createExtractor("Formatting.xls");
      extractor.setIncludeBlankCells(false);
      extractor.setIncludeSheetNames(false);
      String text = extractor.getText();
      
      // Note - not all the formats in the file
      //  actually quite match what they claim to
      //  be, as some are auto-local builtins...
      
      assertTrue(text.startsWith(
            "Dates, all 24th November 2006\n"
      ));
      assertTrue(
            text.indexOf(
               "yyyy/mm/dd\t2006/11/24\n"
            ) > -1
      );
      assertTrue(
            text.indexOf(
               "yyyy-mm-dd\t2006-11-24\n"
            ) > -1
      );
      assertTrue(
            text.indexOf(
               "dd-mm-yy\t24-11-06\n"
            ) > -1
      );
      
      assertTrue(
            text.indexOf(
               "nn.nn\t10.52\n"
            ) > -1
      );
      assertTrue(
            text.indexOf(
               "nn.nnn\t10.520\n"
            ) > -1
      );
      assertTrue(
            text.indexOf(
               "\u00a3nn.nn\t\u00a310.52\n"
            ) > -1
      );
	}

	/**
	 * Embeded in a non-excel file
	 */
	public void testWithEmbeded() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(
			POIDataSamples.getDocumentInstance().openResourceAsStream("word_with_embeded.doc")
		);

		DirectoryNode objPool = (DirectoryNode) fs.getRoot().getEntry("ObjectPool");
		DirectoryNode dirA = (DirectoryNode) objPool.getEntry("_1269427460");
		DirectoryNode dirB = (DirectoryNode) objPool.getEntry("_1269427461");

		HSSFWorkbook wbA = new HSSFWorkbook(dirA, fs, true);
		HSSFWorkbook wbB = new HSSFWorkbook(dirB, fs, true);

		ExcelExtractor exA = new ExcelExtractor(wbA);
		ExcelExtractor exB = new ExcelExtractor(wbB);

		assertEquals("Sheet1\nTest excel file\nThis is the first file\nSheet2\nSheet3\n",
				exA.getText());
		assertEquals("Sample Excel", exA.getSummaryInformation().getTitle());

		assertEquals("Sheet1\nAnother excel file\nThis is the second file\nSheet2\nSheet3\n",
				exB.getText());
		assertEquals("Sample Excel 2", exB.getSummaryInformation().getTitle());
	}

	/**
	 * Excel embeded in excel
	 */
	public void testWithEmbededInOwn() throws Exception {
		POIDataSamples ssSamples = POIDataSamples.getSpreadSheetInstance();
		POIFSFileSystem fs = new POIFSFileSystem(
				ssSamples.openResourceAsStream("excel_with_embeded.xls")
		);

		DirectoryNode dirA = (DirectoryNode) fs.getRoot().getEntry("MBD0000A3B5");
		DirectoryNode dirB = (DirectoryNode) fs.getRoot().getEntry("MBD0000A3B4");

		HSSFWorkbook wbA = new HSSFWorkbook(dirA, fs, true);
		HSSFWorkbook wbB = new HSSFWorkbook(dirB, fs, true);

		ExcelExtractor exA = new ExcelExtractor(wbA);
		ExcelExtractor exB = new ExcelExtractor(wbB);

		assertEquals("Sheet1\nTest excel file\nThis is the first file\nSheet2\nSheet3\n",
				exA.getText());
		assertEquals("Sample Excel", exA.getSummaryInformation().getTitle());

		assertEquals("Sheet1\nAnother excel file\nThis is the second file\nSheet2\nSheet3\n",
				exB.getText());
		assertEquals("Sample Excel 2", exB.getSummaryInformation().getTitle());

		// And the base file too
		ExcelExtractor ex = new ExcelExtractor(fs);
		assertEquals("Sheet1\nI have lots of embeded files in me\nSheet2\nSheet3\n",
				ex.getText());
		assertEquals("Excel With Embeded", ex.getSummaryInformation().getTitle());
	}

	/**
	 * Test that we get text from headers and footers
	 */
	public void test45538() {
		String[] files = {
			"45538_classic_Footer.xls", "45538_form_Footer.xls",
			"45538_classic_Header.xls", "45538_form_Header.xls"
		};
		for(int i=0; i<files.length; i++) {
			ExcelExtractor extractor = createExtractor(files[i]);
			String text = extractor.getText();
			assertTrue("Unable to find expected word in text\n" + text, text.indexOf("testdoc") >=0);
			assertTrue("Unable to find expected word in text\n" + text, text.indexOf("test phrase") >= 0);
		}
	}

	public void testPassword() {
		Biff8EncryptionKey.setCurrentUserPassword("password");
		ExcelExtractor extractor = createExtractor("password.xls");
		String text = extractor.getText();
		Biff8EncryptionKey.setCurrentUserPassword(null);

		assertTrue(text.contains("ZIP"));
	}
}
