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
