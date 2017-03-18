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

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.dev.BiffViewer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.RecordFormatException;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;

public class HSSFFileHandler extends SpreadsheetHandler {
	private final POIFSFileHandler delegate = new POIFSFileHandler();
	@Override
    public void handleFile(InputStream stream) throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook(stream);
		handleWorkbook(wb);
		
		// TODO: some documents fail currently...
        // Note - as of Bugzilla 48036 (svn r828244, r828247) POI is capable of evaluating
        // IntersectionPtg.  However it is still not capable of parsing it.
        // So FormulaEvalTestData.xls now contains a few formulas that produce errors here.
        //HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
        //evaluator.evaluateAll();
        
		delegate.handlePOIDocument(wb);

		// also try to see if some of the Records behave incorrectly
		// TODO: still fails on some records... RecordsStresser.handleWorkbook(wb);
	}

	private static final Set<String> EXPECTED_ADDITIONAL_FAILURES = new HashSet<String>();
	static {
		// encrypted
		EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/35897-type4.xls");
		EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/xor-encryption-abc.xls");
		EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/password.xls");
		// broken files
		EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/43493.xls");
		// TODO: ok to ignore?
		EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/50833.xls");
		EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/51832.xls");
		EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/XRefCalc.xls");
	}

	@Override
	public void handleAdditional(File file) throws Exception {
		// redirect stdout as the examples often write lots of text
		PrintStream oldOut = System.out;
		try {
			System.setOut(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			}));

			BiffViewer.main(new String[]{file.getAbsolutePath()});

			assertFalse("Expected Extraction to fail for file " + file + " and handler " + this + ", but did not fail!",
					EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName()));
		} catch (OldExcelFormatException e) {
			// old excel formats are not supported here
		} catch (EncryptedDocumentException e) {
			if(!EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
				throw e;
			}
		} catch (RecordFormatException e) {
			if(!EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
				throw e;
			}
		} catch (RuntimeException e) {
			if(!EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
				throw e;
			}
		} finally {
			System.setOut(oldOut);
		}
	}

	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	public void test() throws Exception {
	    File file = new File("test-data/spreadsheet/49219.xls");
		
		InputStream stream = new FileInputStream(file);
		try {
			handleFile(stream);
		} finally {
			stream.close();
		}
        handleExtracting(file);
	}
}