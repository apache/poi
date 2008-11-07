
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

import junit.framework.TestCase;
import java.io.*;

/**
 * Class to test that HXF correctly detects OOXML
 *  documents
 */
public class TestDetectAsOOXML extends TestCase
{
	public String dirname;

	public void setUp() {
		dirname = System.getProperty("HSSF.testdata.path");
	}

	public void testOpensProperly() throws Exception
	{
		File f = new File(dirname + "/sample.xlsx");

		POIXMLDocument.openPackage(f.toString());
	}
	
	public void testDetectAsPOIFS() throws Exception {
		InputStream in;
		
		// ooxml file is
		in = new PushbackInputStream(
				new FileInputStream(dirname + "/SampleSS.xlsx"), 10
		);
		assertTrue(POIXMLDocument.hasOOXMLHeader(in));
		
		// xls file isn't
		in = new PushbackInputStream(
				new FileInputStream(dirname + "/SampleSS.xls"), 10
		);
		assertFalse(POIXMLDocument.hasOOXMLHeader(in));
		
		// text file isn't
		in = new PushbackInputStream(
				new FileInputStream(dirname + "/SampleSS.txt"), 10
		);
		assertFalse(POIXMLDocument.hasOOXMLHeader(in));
	}
}
