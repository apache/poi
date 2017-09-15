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

package org.apache.poi.poifs.filesystem;

import static org.apache.poi.POITestCase.assertContains;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.poi.hssf.HSSFTestDataSamples;

import junit.framework.TestCase;

/**
 * Class to test that POIFS complains when given an Office 2003 XML
 *  of Office Open XML (OOXML, 2007+) document
 */
public class TestOfficeXMLException extends TestCase {

	private static final InputStream openSampleStream(String sampleFileName) {
		return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
	}
	public void testOOXMLException() throws IOException
	{
		InputStream in = openSampleStream("sample.xlsx");

		try {
			new POIFSFileSystem(in).close();
			fail("expected exception was not thrown");
		} catch(OfficeXmlFileException e) {
			// expected during successful test
			assertContains(e.getMessage(), "The supplied data appears to be in the Office 2007+ XML");
			assertContains(e.getMessage(), "You are calling the part of POI that deals with OLE2 Office Documents");
		}
	}
    public void test2003XMLException() throws IOException
    {
        InputStream in = openSampleStream("SampleSS.xml");

        try {
            new POIFSFileSystem(in).close();
            fail("expected exception was not thrown");
        } catch(NotOLE2FileException e) {
            // expected during successful test
            assertContains(e.getMessage(), "The supplied data appears to be a raw XML file");
            assertContains(e.getMessage(), "Formats such as Office 2003 XML");
        }
    }
	
	public void testDetectAsPOIFS() throws IOException {
		// ooxml file isn't
		confirmIsPOIFS("SampleSS.xlsx", false);
		
        // 2003 xml file isn't
        confirmIsPOIFS("SampleSS.xml", false);
        
		// xls file is
		confirmIsPOIFS("SampleSS.xls", true);
		
		// older biff formats aren't
        confirmIsPOIFS("testEXCEL_3.xls", false);
        confirmIsPOIFS("testEXCEL_4.xls", false);
        
        // newer excel formats are
        confirmIsPOIFS("testEXCEL_5.xls", true);
        confirmIsPOIFS("testEXCEL_95.xls", true);
		
		// text file isn't
		confirmIsPOIFS("SampleSS.txt", false);
	}
	
	private void confirmIsPOIFS(String sampleFileName, boolean expectedResult) throws IOException {
		InputStream in  = FileMagic.prepareToCheckMagic(openSampleStream(sampleFileName));
		try {
    		boolean actualResult;
    		try {
    			actualResult = POIFSFileSystem.hasPOIFSHeader(in);
    		} catch (IOException e) {
    			throw new RuntimeException(e);
    		}
    		assertEquals(expectedResult, actualResult);
		} finally {
		    in.close();
		}
	}
    
    public void testFileCorruption() throws Exception {
        
        // create test InputStream
        byte[] testData = { (byte)1, (byte)2, (byte)3 };
        InputStream testInput = new ByteArrayInputStream(testData);
        
        // detect header
        InputStream in = FileMagic.prepareToCheckMagic(testInput);
        assertFalse(POIFSFileSystem.hasPOIFSHeader(in));
        
        // check if InputStream is still intact
        byte[] test = new byte[3];
        in.read(test);
        assertTrue(Arrays.equals(testData, test));
        assertEquals(-1, in.read());
    }


    public void testFileCorruptionOPOIFS() throws Exception {
        
        // create test InputStream
        byte[] testData = { (byte)1, (byte)2, (byte)3 };
        InputStream testInput = new ByteArrayInputStream(testData);
        
        // detect header
        InputStream in = FileMagic.prepareToCheckMagic(testInput);
        assertFalse(OPOIFSFileSystem.hasPOIFSHeader(in));

        // check if InputStream is still intact
        byte[] test = new byte[3];
        in.read(test);
        assertTrue(Arrays.equals(testData, test));
        assertEquals(-1, in.read());
    }
}
