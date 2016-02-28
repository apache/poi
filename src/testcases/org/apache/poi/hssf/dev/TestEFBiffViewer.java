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
import java.io.PrintStream;

import org.apache.poi.util.LocaleUtil;

public class TestEFBiffViewer extends BaseXLSIteratingTest {
	static {
		// these are likely ok to fail
		EXCLUDED.add("XRefCalc.xls"); 
		EXCLUDED.add("password.xls"); 
		EXCLUDED.add("51832.xls"); 		// password
		EXCLUDED.add("xor-encryption-abc.xls");    // password, ty again later!
		EXCLUDED.add("43493.xls");	// HSSFWorkbook cannot open it as well
		EXCLUDED.add("44958_1.xls");   // known bad file
		EXCLUDED.add("46904.xls");     // Exception, too old
		EXCLUDED.add("47251_1.xls");   // Broken test file
        EXCLUDED.add("testEXCEL_3.xls");  // Biff 3 / Excel 3, pre-OLE2
		EXCLUDED.add("testEXCEL_4.xls");   // old unsupported format
		EXCLUDED.add("testEXCEL_5.xls");   // old unsupported format
		EXCLUDED.add("testEXCEL_95.xls");   // old unsupported format
		EXCLUDED.add("35897-type4.xls");   // unsupported encryption
		EXCLUDED.add("59074.xls");	// Biff 5 / Excel 95
	}
	
	@Override
	void runOneFile(File fileIn) throws IOException {
		PrintStream save = System.out;
		try {
			// redirect standard out during the test to avoid spamming the console with output
			System.setOut(new PrintStream(NULL_OUTPUT_STREAM,true,LocaleUtil.CHARSET_1252.name()));

			EFBiffViewer.main(new String[] { fileIn.getAbsolutePath() });
		} finally {
			System.setOut(save);
		}
	}

	//@Test
	public void testFile() throws IOException {
		EFBiffViewer viewer = new EFBiffViewer();
		viewer.setFile(new File("test-data/spreadsheet/59074.xls").getAbsolutePath());
		viewer.run();
	}
}
