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
package org.apache.poi.hdgf.extractor;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.hdgf.chunks.Chunk;
import org.apache.poi.hdgf.chunks.ChunkFactory;
import org.apache.poi.hdgf.pointers.Pointer;
import org.apache.poi.hdgf.pointers.PointerFactory;
import org.apache.poi.hssf.record.formula.eval.StringOperationEval;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class TestVisioExtractor extends TestCase {
	private String filename;
	protected void setUp() throws Exception {
		String dirname = System.getProperty("HDGF.testdata.path");
		filename = dirname + "/Test_Visio-Some_Random_Text.vsd";
	}
	
	/**
	 * Test the 3 different ways of creating one
	 */
	public void testCreation() throws Exception {
		VisioTextExtractor extractor;
		
		extractor = new VisioTextExtractor(new FileInputStream(filename));
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(3, extractor.getAllText().length);
		
		extractor = new VisioTextExtractor(
				new POIFSFileSystem(
						new FileInputStream(filename)
				)
		);
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(3, extractor.getAllText().length);
		
		extractor = new VisioTextExtractor(
			new HDGFDiagram(
				new POIFSFileSystem(
						new FileInputStream(filename)
				)
			)
		);
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(3, extractor.getAllText().length);
	}
	
	public void testExtraction() throws Exception {
		VisioTextExtractor extractor =
			new VisioTextExtractor(new FileInputStream(filename));
		
		// Check the array fetch
		String[] text = extractor.getAllText();
		assertNotNull(text);
		assertEquals(3, text.length);
		
		assertEquals("Test View\n", text[0]);
		assertEquals("I am a test view\n", text[1]);
		assertEquals("Some random text, on a page\n", text[2]);
		
		// And the all-in fetch
		String textS = extractor.getText();
		assertEquals("Test View\nI am a test view\nSome random text, on a page\n", textS);
	}
	
	public void testMain() throws Exception {
		PrintStream oldOut = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(baos);
		System.setOut(capture);
		
		VisioTextExtractor.main(new String[] {filename});
		
		// Put things back
		System.setOut(oldOut);
		
		// Check
		capture.flush();
		String text = baos.toString();
		assertEquals("Test View\nI am a test view\nSome random text, on a page\n", text);
	}
}
