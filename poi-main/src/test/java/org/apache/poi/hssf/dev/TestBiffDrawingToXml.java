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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class TestBiffDrawingToXml extends BaseXLSIteratingTest {
	static {
		// TODO: is it ok to fail these? 
		// Look at the output of the test for the detailed stacktrace of the failures...
//		EXCLUDED.add("password.xls"); 
//		EXCLUDED.add("XRefCalc.xls"); 
//		EXCLUDED.add("43493.xls");
//		EXCLUDED.add("51832.xls"); 
	};
	
	@Override
	@Ignore("Not yet done, nearly all files fail with various errors, remove this method when done to use the one from the abstract base class!...")
	@Test
	public void testMain() throws Exception {
	}
	
	@Override
	void runOneFile(String dir, String file, List<String> failed)
			throws Exception {
		PrintStream save = System.out;
		try {
			//System.setOut(new PrintStream(TestBiffViewer.NULL_OUTPUT_STREAM));
			// use a NullOutputStream to not write the bytes anywhere for best runtime 
		    InputStream wb = new FileInputStream(new File(dir, file));
		    try {
		    	BiffDrawingToXml.writeToFile(NULL_OUTPUT_STREAM, wb, false, new String[] {});
		    } finally {
		    	wb.close();
		    }
		} finally {
			System.setOut(save);
		}
	}
}
