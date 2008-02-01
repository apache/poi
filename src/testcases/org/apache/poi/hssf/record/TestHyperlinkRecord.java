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
package org.apache.poi.hssf.record;

import java.io.ByteArrayInputStream;
import java.net.URL;

import junit.framework.TestCase;

public class TestHyperlinkRecord extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
	}

	private byte[] data = new byte[] { 
		-72, 1, 110, 0,
		// ??, Row, col, xf
		6, 0, 3, 0,	2, 0, 2, 0, 
		
		// ??
		-48, -55, -22, 121, -7, -70, -50, 17, 
		-116, -126, 0, -86, 0, 75, -87, 11, 
		2, 0, 0, 0, 
		
		// URL length
		23, 0, 0, 0, 
		
		// Label length
		4, 0, 0, 0,
		
		// Label
		76, 0, 44, 0, 65, 0, 0, 0, 
		
		// ??
		-32, -55, -22, 121, -7, -70, -50, 17,
		-116, -126, 0, -86, 0, 75, -87, 11, 
		46, 0, 0, 0,
		
		// URL
		104, 0, 116, 0, 116, 0, 112, 0, 58, 0, 47, 0, 47, 0, 119,
		0, 119, 0, 119, 0, 46, 0, 108, 0, 97, 0, 107, 0, 105,
		0, 110, 0, 103, 0, 115, 0, 46, 0, 99, 0, 111, 0,
		109, 0, 
		0, 0 };
	
	private byte[] data2 = new byte[] {
		-72, 1, -126, 0,
		// ??, Row, col, xf
		2, 0, 2, 0, 4, 0, 4, 0,

		// ??
		-48, -55, -22, 121, -7, -70, -50, 17,
		-116, -126, 0, -86, 0, 75, -87, 11,
		2, 0, 0, 0,
		
		// URL and Label lengths
		23, 0, 0, 0,
		15, 0, 0, 0,

		// Label
		83, 0, 116, 0, 97, 0, 99, 0, 105, 0,
		101, 0, 64, 0, 65, 0, 66, 0, 67, 0,
		46, 0, 99, 0, 111, 0, 109, 0, 0, 0,

		// ??
		-32, -55, -22, 121, -7, -70, -50, 17,
		-116, -126, 0, -86, 0, 75, -87, 11,
		44, 0, 0, 0,

		// URL
		109, 0, 97, 0, 105, 0, 108, 0, 116, 0,
		111, 0, 58, 0, 83, 0, 116, 0, 97, 0,
		99, 0, 105, 0, 101, 0, 64, 0, 65, 0,
		66, 0, 67, 0, 46, 0, 99, 0, 111, 0,
		109, 0, 0, 0 };

	public void testRecordParsing() throws Exception {
        RecordInputStream inp = new RecordInputStream(
                new ByteArrayInputStream(data)
        );
        inp.nextRecord();

        HyperlinkRecord r = new HyperlinkRecord(inp);
        
        assertEquals(3, r.getRow());
        assertEquals(2, r.getColumn());
        assertEquals(2, r.getXFIndex());
        
        assertEquals("L,A", r.getLabel());
        assertEquals("http://www.lakings.com", r.getUrlString());
        assertEquals(new URL("http://www.lakings.com"), r.getUrl());
        
        // Check it serialises as expected
        assertEquals(data.length, r.getRecordSize());
        byte[] d = r.serialize();
        assertEquals(data.length, d.length);
        for(int i=0; i<data.length; i++) {
        	assertEquals(data[i], d[i]);
        }
	}

	public void testSecondRecord() throws Exception {
        RecordInputStream inp = new RecordInputStream(
                new ByteArrayInputStream(data2)
        );
        inp.nextRecord();

        HyperlinkRecord r = new HyperlinkRecord(inp);
        
        assertEquals(2, r.getRow());
        assertEquals(4, r.getColumn());
        assertEquals(4, r.getXFIndex());
        
		assertEquals("Stacie@ABC.com", r.getLabel());
		assertEquals("mailto:Stacie@ABC.com", r.getUrlString());
	}
}
