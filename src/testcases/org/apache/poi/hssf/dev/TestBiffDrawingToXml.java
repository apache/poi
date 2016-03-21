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

public class TestBiffDrawingToXml extends BaseXLSIteratingTest {
	static {
        EXCLUDED.add("35897-type4.xls"); // unsupported crypto api header 
        EXCLUDED.add("43493.xls");  // HSSFWorkbook cannot open it as well
        EXCLUDED.add("46904.xls");
        EXCLUDED.add("44958_1.xls");
        EXCLUDED.add("51832.xls");
        EXCLUDED.add("59074.xls");
		EXCLUDED.add("password.xls"); 
        EXCLUDED.add("testEXCEL_2.xls");  // Biff 2 / Excel 2, pre-OLE2
        EXCLUDED.add("testEXCEL_3.xls");  // Biff 3 / Excel 3, pre-OLE2
        EXCLUDED.add("testEXCEL_4.xls");  // Biff 4 / Excel 4, pre-OLE2
        EXCLUDED.add("testEXCEL_5.xls");  // Biff 5 / Excel 5
        EXCLUDED.add("testEXCEL_95.xls"); // Biff 5 / Excel 95
		EXCLUDED.add("xor-encryption-abc.xls"); 
	}
	
	@Override
	void runOneFile(File pFile) throws Exception {
		PrintStream save = System.out;
		try {
			//System.setOut(new PrintStream(TestBiffViewer.NULL_OUTPUT_STREAM));
			// use a NullOutputStream to not write the bytes anywhere for best runtime 
		    InputStream wb = new FileInputStream(pFile);
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
