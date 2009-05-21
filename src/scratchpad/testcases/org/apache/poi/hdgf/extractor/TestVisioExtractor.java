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
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public final class TestVisioExtractor extends TestCase {
	private String dirname;
	private String defFilename;
	protected void setUp() throws Exception {
		dirname = System.getProperty("HDGF.testdata.path");
		defFilename = dirname + "/Test_Visio-Some_Random_Text.vsd";
	}

	/**
	 * Test the 3 different ways of creating one
	 */
	public void testCreation() throws Exception {
		VisioTextExtractor extractor;

		extractor = new VisioTextExtractor(new FileInputStream(defFilename));
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(3, extractor.getAllText().length);

		extractor = new VisioTextExtractor(
				new POIFSFileSystem(
						new FileInputStream(defFilename)
				)
		);
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(3, extractor.getAllText().length);

		extractor = new VisioTextExtractor(
			new HDGFDiagram(
				new POIFSFileSystem(
						new FileInputStream(defFilename)
				)
			)
		);
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(3, extractor.getAllText().length);
	}

	public void testExtraction() throws Exception {
		VisioTextExtractor extractor =
			new VisioTextExtractor(new FileInputStream(defFilename));

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

	public void testProblemFiles() throws Exception {
		File a = new File(dirname, "44594.vsd");
		VisioTextExtractor.main(new String[] {a.toString()});

		File b = new File(dirname, "44594-2.vsd");
		VisioTextExtractor.main(new String[] {b.toString()});

		File c = new File(dirname, "ShortChunk1.vsd");
		VisioTextExtractor.main(new String[] {c.toString()});

		File d = new File(dirname, "ShortChunk2.vsd");
		VisioTextExtractor.main(new String[] {d.toString()});

		File e = new File(dirname, "ShortChunk3.vsd");
		VisioTextExtractor.main(new String[] {e.toString()});
	}

	public void testMain() throws Exception {
		PrintStream oldOut = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(baos);
		System.setOut(capture);

		VisioTextExtractor.main(new String[] {defFilename});

		// Put things back
		System.setOut(oldOut);

		// Check
		capture.flush();
		String text = baos.toString();
		assertEquals("Test View\nI am a test view\nSome random text, on a page\n", text);
	}
}
