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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.util.LocaleUtil;

public class TestBiffViewer extends BaseXLSIteratingTest {
    static {
        // these are likely ok to fail
        EXCLUDED.add("XRefCalc.xls"); 	// "Buffer overrun"
        EXCLUDED.add("50833.xls"); 		// "Name is too long" when setting username
        EXCLUDED.add("OddStyleRecord.xls");		
        EXCLUDED.add("NoGutsRecords.xls"); 
        EXCLUDED.add("51832.xls");	// password 
        EXCLUDED.add("43493.xls");	// HSSFWorkbook cannot open it as well
        EXCLUDED.add("password.xls"); 
        EXCLUDED.add("46904.xls");
        EXCLUDED.add("59074.xls"); // Biff 5 / Excel 95
        EXCLUDED.add("35897-type4.xls"); // unsupported crypto api header 
        EXCLUDED.add("xor-encryption-abc.xls"); // unsupported XOR-encryption
        EXCLUDED.add("testEXCEL_2.xls");  // Biff 2 / Excel 2, pre-OLE2
        EXCLUDED.add("testEXCEL_3.xls");  // Biff 3 / Excel 3, pre-OLE2
        EXCLUDED.add("testEXCEL_4.xls");  // Biff 4 / Excel 4, pre-OLE2
        EXCLUDED.add("testEXCEL_5.xls");  // Biff 5 / Excel 5
        EXCLUDED.add("testEXCEL_95.xls"); // Biff 5 / Excel 95
    }

    @Override
    void runOneFile(File fileIn) throws IOException {
        NPOIFSFileSystem fs  = new NPOIFSFileSystem(fileIn, true);
        try {
            InputStream is = BiffViewer.getPOIFSInputStream(fs);
            try {
                // use a NullOutputStream to not write the bytes anywhere for best runtime
                PrintWriter dummy = new PrintWriter(new OutputStreamWriter(NULL_OUTPUT_STREAM, LocaleUtil.CHARSET_1252));
                BiffViewer.runBiffViewer(dummy, is, true, true, true, false);
            } finally {
                is.close();
            }
        } finally {
            fs.close();
        }
    }
	
//  @Test
//  @Ignore("only used for manual tests")
//  public void testOneFile() throws Exception {
//      POIDataSamples samples = POIDataSamples.getSpreadSheetInstance();
//      runOneFile(samples.getFile("43493.xls"));
//  }
}
