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
package org.apache.poi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

/**
 * Class to test that HXF correctly detects OOXML
 *  documents
 */
@SuppressWarnings("deprecation")
public class TestDetectAsOOXML {
    @Test
	public void testOpensProperly() throws IOException, InvalidFormatException {
        OPCPackage.open(HSSFTestDataSamples.openSampleFileStream("sample.xlsx"));
	}
	
    @Test
	public void testDetectAsPOIFS() throws IOException {
	    Object fileAndMagic[][] = {
            { "SampleSS.xlsx", FileMagic.OOXML },
            { "SampleSS.xls", FileMagic.OLE2 },
            { "SampleSS.txt", FileMagic.UNKNOWN }
	    };

	    for (Object fm[] : fileAndMagic) {
	        InputStream is = HSSFTestDataSamples.openSampleFileStream((String)fm[0]);
	        is = FileMagic.prepareToCheckMagic(is);
	        FileMagic act = FileMagic.valueOf(is);
	        
			assertEquals("OOXML files should be detected, others not",
					act == FileMagic.OOXML, DocumentFactoryHelper.hasOOXMLHeader(is));

	        assertEquals("file magic failed for "+fm[0], fm[1], act);
	        is.close();
	    }
	}
    
    @Test
    public void testFileCorruption() throws Exception {
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
