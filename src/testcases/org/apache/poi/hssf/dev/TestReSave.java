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

import org.apache.poi.POIDataSamples;
import org.apache.poi.util.LocaleUtil;

public class TestReSave extends BaseXLSIteratingTest {
	static {
		// these are likely ok to fail
		EXCLUDED.add("password.xls"); 
		EXCLUDED.add("43493.xls");	// HSSFWorkbook cannot open it as well
		EXCLUDED.add("46904.xls"); 
		EXCLUDED.add("51832.xls");	// password 
        EXCLUDED.add("44958_1.xls");   // known bad file
	}
	
	@Override
	void runOneFile(File fileIn) throws Exception {
		// avoid running on files leftover from previous failed runs
		if(fileIn.getName().endsWith("-saved.xls")) {
			return;
		}

		PrintStream save = System.out;
		try {
			// redirect standard out during the test to avoid spamming the console with output
			System.setOut(new PrintStream(NULL_OUTPUT_STREAM,true,LocaleUtil.CHARSET_1252.name()));

			File reSavedFile = new File(fileIn.getParentFile(), fileIn.getName().replace(".xls", "-saved.xls"));
			try {
				ReSave.main(new String[] { fileIn.getAbsolutePath() });
				
				// also try BiffViewer on the saved file
                new TestBiffViewer().runOneFile(reSavedFile);

    			try {
        			// had one case where the re-saved could not be re-saved!
        			ReSave.main(new String[] { reSavedFile.getAbsolutePath() });
    			} finally {
    				// clean up the re-re-saved file
    			    new File(fileIn.getParentFile(), reSavedFile.getName().replace(".xls", "-saved.xls")).delete();
    			}
			} finally {
				// clean up the re-saved file
				reSavedFile.delete();
			}

		} finally {
			System.setOut(save);
		}
	}

	//Only used for local testing
	//@Test
	public void testOneFile() throws Exception {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY);
        if(dataDirName == null) {
            dataDirName = "test-data";
        }

		List<String> failed = new ArrayList<String>();
		runOneFile(new File(dataDirName + "/spreadsheet", "49931.xls"));

		assertTrue("Expected to have no failed except the ones excluded, but had: " + failed, 
				failed.isEmpty());
	}
}
