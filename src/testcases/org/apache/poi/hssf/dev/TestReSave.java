package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

public class TestReSave extends BaseXLSIteratingTest {
	static {
		// TODO: is it ok to fail these? 
		// Look at the output of the test for the detailed stacktrace of the failures...
		EXCLUDED.add("password.xls"); 
		EXCLUDED.add("43493.xls");
		EXCLUDED.add("51832.xls"); 
		EXCLUDED.add("49219.xls");
		EXCLUDED.add("49931.xls");

		SILENT_EXCLUDED.add("46904.xls"); 
	};
	
	@Override
	void runOneFile(String dir, String file, List<String> failed) throws Exception {
		// avoid running on files leftover from previous failed runs
		if(file.endsWith("-saved.xls")) {
			return;
		}

		PrintStream save = System.out;
		try {
			// redirect standard out during the test to avoid spamming the console with output
			System.setOut(new PrintStream(NULL_OUTPUT_STREAM));

			try {
				ReSave.main(new String[] { new File(dir, file).getAbsolutePath() });
    			try {
        			// had one case where the re-saved could not be re-saved!
        			ReSave.main(new String[] { new File(dir, file.replace(".xls", "-saved.xls")).getAbsolutePath() });
    			} finally {
    				// clean up the re-re-saved file
    				new File(dir, file.replace(".xls", "-saved.xls").replace(".xls", "-saved.xls")).delete();
    			}
			} finally {
				// clean up the re-saved file
				new File(dir, file.replace(".xls", "-saved.xls")).delete();
			}

		} finally {
			System.setOut(save);
		}
	}
}
