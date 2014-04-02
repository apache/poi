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
import java.util.List;

public class TestRecordLister extends BaseXLSIteratingTest {
	static {
		// TODO: is it ok to fail these? 
		// Look at the output of the test for the detailed stacktrace of the failures...
		//EXCLUDED.add("");

		// these are likely ok to fail
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
