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
package org.apache.poi.ooxml;

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleFileStream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Class to test that HXF correctly detects OOXML
 *  documents
 */
@SuppressWarnings("deprecation")
class TestDetectAsOOXML {
    @Test
	void testOpensProperly() throws IOException, InvalidFormatException {
    	try (InputStream is = openSampleFileStream("sample.xlsx");
			 OPCPackage pkg = OPCPackage.open(is)) {
    		assertNotNull(pkg);
		}
	}

    @ParameterizedTest
	@CsvSource({"SampleSS.xlsx, OOXML", "SampleSS.xls, OLE2", "SampleSS.txt, UNKNOWN"})
	void testDetectAsPOIFS(String file, FileMagic fm) throws IOException {
		try (InputStream is = FileMagic.prepareToCheckMagic(openSampleFileStream(file))) {
			FileMagic act = FileMagic.valueOf(is);

			assertEquals(act == FileMagic.OOXML, DocumentFactoryHelper.hasOOXMLHeader(is),
				"OOXML files should be detected, others not");

			assertEquals(fm, act, "file magic failed for " + file);
		}
	}

    @Test
    void testFileCorruption() throws Exception {
	    // create test InputStream
	    byte[] testData = { 1, 2, 3 };
        ByteArrayInputStream testInput = new ByteArrayInputStream(testData);
        InputStream is = FileMagic.prepareToCheckMagic(testInput);

        // detect header
        assertFalse(DocumentFactoryHelper.hasOOXMLHeader(is));

        // check if InputStream is still intact
        byte[] act = IOUtils.toByteArray(is);
        assertArrayEquals(testData, act);
        assertEquals(-1, is.read());
        is.close();
	}
}
