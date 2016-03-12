
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;

/**
 * Class to test that HXF correctly detects OOXML
 *  documents
 */
public class TestDetectAsOOXML extends TestCase
{
	public void testOpensProperly() throws Exception
	{
        OPCPackage.open(HSSFTestDataSamples.openSampleFileStream("sample.xlsx"));
	}
	
	public void testDetectAsPOIFS() throws Exception {
		InputStream in;
		
		// ooxml file is
		in = new PushbackInputStream(
				HSSFTestDataSamples.openSampleFileStream("SampleSS.xlsx"), 10
		);
		assertTrue(DocumentFactoryHelper.hasOOXMLHeader(in));
		in.close();
		
		// xls file isn't
		in = new PushbackInputStream(
				HSSFTestDataSamples.openSampleFileStream("SampleSS.xls"), 10
		);
		assertFalse(DocumentFactoryHelper.hasOOXMLHeader(in));
		in.close();
		
		// text file isn't
		in = new PushbackInputStream(
				HSSFTestDataSamples.openSampleFileStream("SampleSS.txt"), 10
		);
		assertFalse(DocumentFactoryHelper.hasOOXMLHeader(in));
		in.close();
	}
    
    public void testFileCorruption() throws Exception {
	    
	    // create test InputStream
	    byte[] testData = { (byte)1, (byte)2, (byte)3 };
        ByteArrayInputStream testInput = new ByteArrayInputStream(testData);
        
        // detect header
        InputStream in = new PushbackInputStream(testInput, 10);
        assertFalse(DocumentFactoryHelper.hasOOXMLHeader(in));
		//noinspection deprecation
		assertFalse(POIXMLDocument.hasOOXMLHeader(in));
        
        // check if InputStream is still intact
        byte[] test = new byte[3];
        assertEquals(3, in.read(test));
        assertTrue(Arrays.equals(testData, test));
        assertEquals(-1, in.read());
	}
}
