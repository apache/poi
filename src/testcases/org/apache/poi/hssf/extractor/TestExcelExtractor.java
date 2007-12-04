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

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

public class TestExcelExtractor extends TestCase {
	public void testSimple() throws Exception {
		String path = System.getProperty("HSSF.testdata.path");
		FileInputStream fin = new FileInputStream(path + File.separator + "Simple.xls");
		
		ExcelExtractor extractor = new ExcelExtractor(new POIFSFileSystem(fin));
		
		assertEquals("Sheet1\nreplaceMe\nSheet2\nSheet3\n", extractor.getText());
		
		// Now turn off sheet names
		extractor.setIncludeSheetNames(false);
		assertEquals("replaceMe\n", extractor.getText());
	}
	
	public void testNumericFormula() throws Exception {
		String path = System.getProperty("HSSF.testdata.path");
		FileInputStream fin = new FileInputStream(path + File.separator + "sumifformula.xls");
		
		ExcelExtractor extractor = new ExcelExtractor(new POIFSFileSystem(fin));
		
		assertEquals(
				"Sheet1\n" +
				"1000.0\t1.0\t5.0\n" +
				"2000.0\t2.0\t\n" +	
				"3000.0\t3.0\t\n" +
				"4000.0\t4.0\t\n" + 
				"5000.0\t5.0\t\n" +
				"Sheet2\nSheet3\n", 
				extractor.getText()
		);
		
		extractor.setFormulasNotResults(true);
		
		assertEquals(
				"Sheet1\n" +
				"1000.0\t1.0\tSUMIF(A1:A5,\">4000\",B1:B5)\n" +
				"2000.0\t2.0\t\n" +	
				"3000.0\t3.0\t\n" +
				"4000.0\t4.0\t\n" + 
				"5000.0\t5.0\t\n" +
				"Sheet2\nSheet3\n", 
				extractor.getText()
		);
	}
	
	public void testwithContinueRecords() throws Exception {
		String path = System.getProperty("HSSF.testdata.path");
		FileInputStream fin = new FileInputStream(path + File.separator + "StringContinueRecords.xls");
		
		ExcelExtractor extractor = new ExcelExtractor(new POIFSFileSystem(fin));
		
		extractor.getText();
		
		// Has masses of text
		// Until we fixed bug #41064, this would've
		//   failed by now
		assertTrue(extractor.getText().length() > 40960);
	}
	
	public void testStringConcat() throws Exception {
		String path = System.getProperty("HSSF.testdata.path");
		FileInputStream fin = new FileInputStream(path + File.separator + "SimpleWithFormula.xls");
		
		ExcelExtractor extractor = new ExcelExtractor(new POIFSFileSystem(fin));
		
		// Comes out as NaN if treated as a number
		// And as XYZ if treated as a string
		assertEquals("Sheet1\nreplaceme\nreplaceme\nreplacemereplaceme\nSheet2\nSheet3\n", extractor.getText());
		
		extractor.setFormulasNotResults(true);
		
		assertEquals("Sheet1\nreplaceme\nreplaceme\nCONCATENATE(A1,A2)\nSheet2\nSheet3\n", extractor.getText());
	}
	
	public void testStringFormula() throws Exception {
		String path = System.getProperty("HSSF.testdata.path");
		FileInputStream fin = new FileInputStream(path + File.separator + "StringFormulas.xls");
		
		ExcelExtractor extractor = new ExcelExtractor(new POIFSFileSystem(fin));
		
		// Comes out as NaN if treated as a number
		// And as XYZ if treated as a string
		assertEquals("Sheet1\nXYZ\nSheet2\nSheet3\n", extractor.getText());
		
		extractor.setFormulasNotResults(true);
		
		assertEquals("Sheet1\nUPPER(\"xyz\")\nSheet2\nSheet3\n", extractor.getText());
	}
}
