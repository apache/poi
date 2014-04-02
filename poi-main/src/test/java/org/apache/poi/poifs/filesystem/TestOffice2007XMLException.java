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

import junit.framework.TestCase;
import java.io.*;

import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Class to test that POIFS complains when given an Office 2007 XML document
 *
 * @author Marc Johnson
 */
public class TestOffice2007XMLException extends TestCase {

	private static final InputStream openSampleStream(String sampleFileName) {
		return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
	}
	public void testXMLException() throws IOException
	{
		InputStream in = openSampleStream("sample.xlsx");

		try {
			new POIFSFileSystem(in);
			fail("expected exception was not thrown");
		} catch(OfficeXmlFileException e) {
			// expected during successful test
			assertTrue(e.getMessage().indexOf("The supplied data appears to be in the Office 2007+ XML") > -1);
			assertTrue(e.getMessage().indexOf("You are calling the part of POI that deals with OLE2 Office Documents") > -1);
		}
	}
	
	public void testDetectAsPOIFS() {
		
		// ooxml file isn't
		confirmIsPOIFS("SampleSS.xlsx", false);
		
		// xls file is
		confirmIsPOIFS("SampleSS.xls", true);
		
		// text file isn't
		confirmIsPOIFS("SampleSS.txt", false);
	}
	private void confirmIsPOIFS(String sampleFileName, boolean expectedResult) {
		InputStream in  = new PushbackInputStream(openSampleStream(sampleFileName), 10);
		boolean actualResult;
		try {
			actualResult = POIFSFileSystem.hasPOIFSHeader(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		assertEquals(expectedResult, actualResult);
	}
}
