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

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.BeforeClass;

public class TestBiffDrawingToXml extends BaseXLSIteratingTest {
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
        EXCLUDED.put("61300.xls", RecordFormatException.class);
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
