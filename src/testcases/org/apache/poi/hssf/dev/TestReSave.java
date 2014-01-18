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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestReSave extends BaseXLSIteratingTest {
	static {
		// TODO: is it ok to fail these? 
		// Look at the output of the test for the detailed stacktrace of the failures...
		EXCLUDED.add("49931.xls");

		// these are likely ok to fail
		SILENT_EXCLUDED.add("password.xls"); 
		SILENT_EXCLUDED.add("43493.xls");	// HSSFWorkbook cannot open it as well
		SILENT_EXCLUDED.add("46904.xls"); 
		SILENT_EXCLUDED.add("51832.xls");	// password 
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
				
				// also try BiffViewer on the saved file
				new TestBiffViewer().runOneFile(dir, file.replace(".xls", "-saved.xls"), failed);

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

	@Test
	public void testOneFile() throws Exception {
		List<String> failed = new ArrayList<String>();
		runOneFile("test-data/spreadsheet", "49219.xls", failed);

		assertTrue("Expected to have no failed except the ones excluded, but had: " + failed, 
				failed.isEmpty());
	}
}
