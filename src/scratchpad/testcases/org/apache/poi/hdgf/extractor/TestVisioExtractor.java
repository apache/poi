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
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;

public final class TestVisioExtractor extends TestCase {
    private static POIDataSamples _dgTests = POIDataSamples.getDiagramInstance();

	private String defFilename;
	private int defTextChunks;
	protected void setUp() {
		defFilename = "Test_Visio-Some_Random_Text.vsd";
		defTextChunks = 5;
	}

	/**
	 * Test the 3 different ways of creating one
	 */
	public void testCreation() throws Exception {
		VisioTextExtractor extractor;

		extractor = new VisioTextExtractor(_dgTests.openResourceAsStream(defFilename));
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(defTextChunks, extractor.getAllText().length);

		extractor = new VisioTextExtractor(
				new POIFSFileSystem(
						_dgTests.openResourceAsStream(defFilename)
				)
		);
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(defTextChunks, extractor.getAllText().length);

		extractor = new VisioTextExtractor(
			new HDGFDiagram(
				new POIFSFileSystem(
						_dgTests.openResourceAsStream(defFilename)
				)
			)
		);
		assertNotNull(extractor);
		assertNotNull(extractor.getAllText());
		assertEquals(defTextChunks, extractor.getAllText().length);
	}

	public void testExtraction() throws Exception {
		VisioTextExtractor extractor =
			new VisioTextExtractor(_dgTests.openResourceAsStream(defFilename));

		// Check the array fetch
		String[] text = extractor.getAllText();
		assertNotNull(text);
		assertEquals(defTextChunks, text.length);

      assertEquals("text\n", text[0]);
      assertEquals("View\n", text[1]);
		assertEquals("Test View\n", text[2]);
		assertEquals("I am a test view\n", text[3]);
		assertEquals("Some random text, on a page\n", text[4]);

		// And the all-in fetch
		String textS = extractor.getText();
		assertEquals("text\nView\nTest View\nI am a test view\nSome random text, on a page\n", textS);
	}

	public void testProblemFiles() throws Exception {
		String[] files = {
		      "44594.vsd", "44594-2.vsd", 
		      "ShortChunk1.vsd", "ShortChunk2.vsd", "ShortChunk3.vsd",
		      "NegativeChunkLength.vsd", "NegativeChunkLength2.vsd"
		};
        for(String file : files){
            VisioTextExtractor ex = new VisioTextExtractor(_dgTests.openResourceAsStream(file));
            ex.getText();
        }
	}

	public void testMain() throws Exception {
		PrintStream oldOut = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream capture = new PrintStream(baos);
		System.setOut(capture);

        String path = _dgTests.getFile(defFilename).getPath();
        VisioTextExtractor.main(new String[] {path});

		// Put things back
		System.setOut(oldOut);

		// Check
		capture.flush();
		String text = baos.toString();
		assertEquals(
		      "text\nView\n" +
		      "Test View\nI am a test view\n" +
		      "Some random text, on a page\n", 
		      text);
	}
}
