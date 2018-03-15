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

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.RecordFormatException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestReSave extends BaseXLSIteratingTest {
    @BeforeClass
    public static void setup() {
        EXCLUDED.clear();
        EXCLUDED.put("35897-type4.xls", EncryptedDocumentException.class); // unsupported crypto api header 
        EXCLUDED.put("51832.xls", EncryptedDocumentException.class);
        EXCLUDED.put("xor-encryption-abc.xls", EncryptedDocumentException.class); 
        EXCLUDED.put("password.xls", EncryptedDocumentException.class); 
        EXCLUDED.put("46904.xls", OldExcelFormatException.class);
        EXCLUDED.put("59074.xls", OldExcelFormatException.class);
        EXCLUDED.put("testEXCEL_2.xls", OldExcelFormatException.class);  // Biff 2 / Excel 2, pre-OLE2
        EXCLUDED.put("testEXCEL_3.xls", OldExcelFormatException.class);  // Biff 3 / Excel 3, pre-OLE2
        EXCLUDED.put("testEXCEL_4.xls", OldExcelFormatException.class);  // Biff 4 / Excel 4, pre-OLE2
        EXCLUDED.put("testEXCEL_5.xls", OldExcelFormatException.class);  // Biff 5 / Excel 5
        EXCLUDED.put("60284.xls", OldExcelFormatException.class); // Biff 5 / Excel 5
        EXCLUDED.put("testEXCEL_95.xls", OldExcelFormatException.class); // Biff 5 / Excel 95
        EXCLUDED.put("60284.xls", OldExcelFormatException.class); // Biff 5 / Excel 95
        EXCLUDED.put("43493.xls", RecordInputStream.LeftoverDataException.class);  // HSSFWorkbook cannot open it as well
        EXCLUDED.put("44958_1.xls", RecordInputStream.LeftoverDataException.class);
        EXCLUDED.put("XRefCalc.xls", RuntimeException.class);            // "Buffer overrun"
        EXCLUDED.put("61300.xls", RecordFormatException.class);
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

    			// had one case where the re-saved could not be re-saved!
    			ReSave.main(new String[] { "-bos", reSavedFile.getAbsolutePath() });
			} finally {
				// clean up the re-saved file
				assertTrue(!reSavedFile.exists() || reSavedFile.delete());
			}

		} finally {
			System.setOut(save);
		}
	}

    @Ignore("Only used for local testing")
    @Test
    public void testOneFile() throws Exception {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY);
        if(dataDirName == null) {
            dataDirName = "test-data";
        }

        runOneFile(new File(dataDirName + "/spreadsheet", "49931.xls"));
    }
}
