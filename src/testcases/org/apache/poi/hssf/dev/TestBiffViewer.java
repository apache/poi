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
package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public class TestBiffViewer extends BaseXLSIteratingTest {
	static {
		// Look at the output of the test for the detailed stacktrace of the failures...
		//EXCLUDED.add("");
		
		// these are likely ok to fail
		SILENT_EXCLUDED.add("XRefCalc.xls"); 	// "Buffer overrun"
		SILENT_EXCLUDED.add("50833.xls"); 		// "Name is too long" when setting username
		SILENT_EXCLUDED.add("OddStyleRecord.xls");		
		SILENT_EXCLUDED.add("NoGutsRecords.xls"); 
		SILENT_EXCLUDED.add("51832.xls");	// password 
		SILENT_EXCLUDED.add("43493.xls");	// HSSFWorkbook cannot open it as well
		SILENT_EXCLUDED.add("password.xls"); 
		SILENT_EXCLUDED.add("46904.xls");
		SILENT_EXCLUDED.add("xor-encryption-abc.xls"); // unsupported XOR-encryption
	};

	@Override
	void runOneFile(String dir, String file, List<String> failed) throws IOException {
		InputStream is = BiffViewer.getPOIFSInputStream(new File(dir, file));
		try {
			// use a NullOutputStream to not write the bytes anywhere for best runtime 
			BiffViewer.runBiffViewer(new PrintStream(NULL_OUTPUT_STREAM), is, true, true, true, false);
		} finally {
			is.close();
		}
	}
	
//	@Test
//	public void testOneFile() throws Exception {
//		List<String> failed = new ArrayList<String>();
//		runOneFile("test-data/spreadsheet", "WORKBOOK_in_capitals.xls", failed);
//
//		assertTrue("Expected to have no failed except the ones excluded, but had: " + failed, 
//				failed.isEmpty());
//	}
}
