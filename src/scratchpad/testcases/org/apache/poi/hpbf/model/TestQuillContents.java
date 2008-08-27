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
package org.apache.poi.hpbf.model;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.hpbf.model.qcbits.QCTextBit;

import junit.framework.TestCase;

public class TestQuillContents extends TestCase {
	private String dir;

	protected void setUp() throws Exception {
		dir = System.getProperty("HPBF.testdata.path");
	}

	public void testBasics() throws Exception {
		File f = new File(dir, "Sample.pub");
		HPBFDocument doc = new HPBFDocument(
				new FileInputStream(f)
		);
		
		QuillContents qc = doc.getQuillContents();
		assertEquals(20, qc.getBits().length);
		for(int i=0; i<19; i++) {
			assertNotNull(qc.getBits()[i]);
		}
		// Last one is blank
		assertNull(qc.getBits()[19]);
		
		// Should be text, then three STSHs
		assertEquals("TEXT", qc.getBits()[0].getThingType());
		assertEquals("TEXT", qc.getBits()[0].getBitType());
		assertEquals(0, qc.getBits()[0].getOptA());
		
		assertEquals("STSH", qc.getBits()[1].getThingType());
		assertEquals("STSH", qc.getBits()[1].getBitType());
		assertEquals(0, qc.getBits()[1].getOptA());
		
		assertEquals("STSH", qc.getBits()[2].getThingType());
		assertEquals("STSH", qc.getBits()[2].getBitType());
		assertEquals(1, qc.getBits()[2].getOptA());
		
		assertEquals("STSH", qc.getBits()[3].getThingType());
		assertEquals("STSH", qc.getBits()[3].getBitType());
		assertEquals(2, qc.getBits()[3].getOptA());
	}
	
	public void testText() throws Exception {
		File f = new File(dir, "Sample.pub");
		HPBFDocument doc = new HPBFDocument(
				new FileInputStream(f)
		);
		
		QuillContents qc = doc.getQuillContents();
		assertEquals(20, qc.getBits().length);
		
		QCTextBit text = (QCTextBit)qc.getBits()[0];
		String t = text.getText();
		assertTrue(t.startsWith("This is some text on the first page"));
		assertTrue(t.endsWith("Within doc to page 1\r"));
	}
}
