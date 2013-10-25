package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class TestRecordLister extends BaseXLSIteratingTest {
	static {
		// TODO: is it ok to fail these? 
		// Look at the output of the test for the detailed stacktrace of the failures...
		EXCLUDED.add("WORKBOOK_in_capitals.xls"); 	
		EXCLUDED.add("NoGutsRecords.xls"); 
		EXCLUDED.add("BOOK_in_capitals.xls"); 
		EXCLUDED.add("OddStyleRecord.xls");		

		SILENT_EXCLUDED.add("46904.xls"); 
	};
	
	@Override
	void runOneFile(String dir, String file, List<String> failed) throws IOException {
		PrintStream save = System.out;
		try {
			// redirect standard out during the test to avoid spamming the console with output
			System.setOut(new PrintStream(NULL_OUTPUT_STREAM));

			RecordLister viewer = new RecordLister();
            viewer.setFile(new File(dir, file).getAbsolutePath());
            viewer.run();
		} finally {
			System.setOut(save);
		}
	}
}
