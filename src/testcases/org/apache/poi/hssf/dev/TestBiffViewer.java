package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class TestBiffViewer extends BaseXLSIteratingTest {
	static {
		// TODO: is it ok to fail these? 
		// Look at the output of the test for the detailed stacktrace of the failures...
		EXCLUDED.add("WORKBOOK_in_capitals.xls"); 	
		EXCLUDED.add("password.xls"); 
		EXCLUDED.add("NoGutsRecords.xls"); 
		EXCLUDED.add("BOOK_in_capitals.xls"); 
		EXCLUDED.add("XRefCalc.xls"); 
		EXCLUDED.add("50833.xls"); 		// probably a problem in BiffViewer
		EXCLUDED.add("43493.xls");
		EXCLUDED.add("51832.xls"); 
		EXCLUDED.add("OddStyleRecord.xls");		

		SILENT_EXCLUDED.add("46904.xls"); 
	};

	@Override
	void runOneFile(String dir, String file, List<String> failed) throws IOException {
		FileInputStream inStream = new FileInputStream(new File(dir, file));
		try {
			POIFSFileSystem fs = new POIFSFileSystem(inStream);
			InputStream is = fs.createDocumentInputStream("Workbook");
			try {
				// use a NullOutputStream to not write the bytes anywhere for best runtime 
				BiffViewer.runBiffViewer(new PrintStream(NULL_OUTPUT_STREAM), is, true, true, true, false);
			} finally {
				is.close();
			}
		} finally {
			inStream.close();
		}
	}
}
