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
package org.apache.poi.stress;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;

public class HSSFFileHandler extends SpreadsheetHandler {
	private POIFSFileHandler delegate = new POIFSFileHandler();
	@Override
    public void handleFile(InputStream stream) throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook(stream);
		handleWorkbook(wb, ".xls");
		
		// TODO: some documents fail currently...
        //HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
        //evaluator.evaluateAll();
        
		delegate.handlePOIDocument(wb);

		// also try to see if some of the Records behave incorrectly
		// TODO: still fails on some records... RecordsStresser.handleWorkbook(wb);
	}

	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	public void test() throws Exception {
		InputStream stream = new FileInputStream("test-data/spreadsheet/49219.xls");
		try {
			handleFile(stream);
		} finally {
			stream.close();
		}
	}

	// a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void testExtractor() throws Exception {
        handleExtracting(new File("test-data/spreadsheet/BOOK_in_capitals.xls"));
    }
}