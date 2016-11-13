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
package org.apache.poi.stress;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

public class HPSFFileHandler extends POIFSFileHandler {
	@Override
    public void handleFile(InputStream stream) throws Exception {
		HPSFPropertiesOnlyDocument hpsf = new HPSFPropertiesOnlyDocument(new POIFSFileSystem(stream));
		assertNotNull(hpsf.getDocumentSummaryInformation());
		assertNotNull(hpsf.getSummaryInformation());
		
		handlePOIDocument(hpsf);
	}
	
	// a test-case to test this locally without executing the full TestAllFiles
	@Override
    @Test
	public void test() throws Exception {
		InputStream stream = new FileInputStream("test-data/hpsf/Test0313rur.adm");
		try {
			handleFile(stream);
		} finally {
			stream.close();
		}
	}

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void testExtractor() throws Exception {
        handleExtracting(new File("test-data/hpsf/TestBug44375.xls"));
    }
}
