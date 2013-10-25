package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class TestFormulaViewer extends BaseXLSIteratingTest {
	static {
		// TODO: is it ok to fail these? 
		// Look at the output of the test for the detailed stacktrace of the failures...
//		EXCLUDED.add("WORKBOOK_in_capitals.xls"); 	
//		EXCLUDED.add("NoGutsRecords.xls"); 
//		EXCLUDED.add("BOOK_in_capitals.xls"); 
//		EXCLUDED.add("46904.xls"); 
//		EXCLUDED.add("OddStyleRecord.xls");		
	};
	
	@Override
	@Ignore("Not yet done, nearly all files fail with various errors, remove this method when done to use the one from the abstract base class!...")
	@Test
	public void testMain() throws Exception {
	}

	@Override
	void runOneFile(String dir, String file, List<String> failed) throws Exception {
		PrintStream save = System.out;
		try {
			// redirect standard out during the test to avoid spamming the console with output
			System.setOut(new PrintStream(NULL_OUTPUT_STREAM));

            FormulaViewer viewer = new FormulaViewer();
            viewer.setFile(new File(dir, file).getAbsolutePath());
            viewer.setList(true);
            viewer.run();
		} finally {
			System.setOut(save);
		}
	}
}
