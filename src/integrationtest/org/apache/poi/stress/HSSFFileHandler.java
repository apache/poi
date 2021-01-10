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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.dev.BiffViewer;
import org.apache.poi.hssf.usermodel.HSSFOptimiser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.NullPrintStream;
import org.junit.jupiter.api.Test;

class HSSFFileHandler extends SpreadsheetHandler {
	private final POIFSFileHandler delegate = new POIFSFileHandler();
	@Override
    public void handleFile(InputStream stream, String path) throws Exception {
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

		HSSFOptimiser.optimiseCellStyles(wb);
		for(Sheet sheet : wb) {
			for (Row row : sheet) {
				for (Cell cell : row) {
					assertNotNull(cell.getCellStyle());
				}
			}
		}

		HSSFOptimiser.optimiseFonts(wb);
	}

	private static final Set<String> EXPECTED_ADDITIONAL_FAILURES = new HashSet<>();
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
		EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/61300.xls");
	}

	@Override
	public void handleAdditional(File file) throws Exception {
		// redirect stdout as the examples often write lots of text
		PrintStream oldOut = System.out;
		String fileWithParent = file.getParentFile().getName() + "/" + file.getName();
		try {
			System.setOut(new NullPrintStream());

			BiffViewer.main(new String[]{file.getAbsolutePath()});

			assertFalse( EXPECTED_ADDITIONAL_FAILURES.contains(fileWithParent), "Expected Extraction to fail for file " + file + " and handler " + this + ", but did not fail!" );
		} catch (OldExcelFormatException e) {
			// old excel formats are not supported here
		} catch (RuntimeException e) {
			if(!EXPECTED_ADDITIONAL_FAILURES.contains(fileWithParent)) {
				throw e;
			}
		} finally {
			System.setOut(oldOut);
		}
	}

	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	void test() throws Exception {
        File file = new File("test-data/spreadsheet/49219.xls");

		try (InputStream stream = new FileInputStream(file)) {
			handleFile(stream, file.getPath());
		}

		handleExtracting(file);

		handleAdditional(file);
	}

	// a test-case to test this locally without executing the full TestAllFiles
    @Test
	@SuppressWarnings("java:S2699")
    void testExtractor() throws Exception {
        handleExtracting(new File("test-data/spreadsheet/BOOK_in_capitals.xls"));
    }
}