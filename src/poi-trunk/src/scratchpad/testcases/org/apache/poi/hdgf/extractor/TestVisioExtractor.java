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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

public final class TestVisioExtractor {
    private static POIDataSamples _dgTests = POIDataSamples.getDiagramInstance();

	private final String defFilename = "Test_Visio-Some_Random_Text.vsd";
	private final int defTextChunks = 5;

	/**
	 * Test the 3 different ways of creating one
	 */
	@Test
	public void testCreation() throws IOException {
		VisioTextExtractor extractor1 = openExtractor(defFilename);
		assertNotNull(extractor1);
		assertNotNull(extractor1.getAllText());
		assertEquals(defTextChunks, extractor1.getAllText().length);
		extractor1.close();

		InputStream is2 = _dgTests.openResourceAsStream(defFilename);
		POIFSFileSystem poifs2 = new POIFSFileSystem(is2);
		is2.close();
		VisioTextExtractor extractor2 = new VisioTextExtractor(poifs2);
		assertNotNull(extractor2);
		assertNotNull(extractor2.getAllText());
		assertEquals(defTextChunks, extractor2.getAllText().length);
		extractor2.close();
		poifs2.close();

        InputStream is3 = _dgTests.openResourceAsStream(defFilename);
        POIFSFileSystem poifs3 = new POIFSFileSystem(is3);
        is3.close();
        HDGFDiagram hdgf3 = new HDGFDiagram(poifs3);

		
        VisioTextExtractor extractor3 = new VisioTextExtractor(hdgf3);
		assertNotNull(extractor3);
		assertNotNull(extractor3.getAllText());
		assertEquals(defTextChunks, extractor3.getAllText().length);
		extractor3.close();
		hdgf3.close();
		poifs3.close();
	}

    @Test
	public void testExtraction() throws Exception {
		VisioTextExtractor extractor = openExtractor(defFilename);

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
		extractor.close();
	}

    @Test
	public void testProblemFiles() throws Exception {
		String[] files = {
		      "44594.vsd", "44594-2.vsd", 
		      "ShortChunk1.vsd", "ShortChunk2.vsd", "ShortChunk3.vsd",
		      "NegativeChunkLength.vsd", "NegativeChunkLength2.vsd"
		};
        for(String file : files){
            VisioTextExtractor ex = openExtractor(file);
            ex.getText();
            ex.close();
        }
	}

    @Test
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
        // YK: stdout can contain lots of other stuff if logging is sent to console
        // ( -Dorg.apache.poi.util.POILogger=org.apache.poi.util.SystemOutLogger)
		assertTrue( text.contains(
		      "text\nView\n" +
		      "Test View\nI am a test view\n" +
		      "Some random text, on a page\n"
		      ));
	}
    
    private VisioTextExtractor openExtractor(String fileName) throws IOException {
        InputStream is = _dgTests.openResourceAsStream(fileName);
        try {
            return new VisioTextExtractor(is);
        } finally {
            is.close();
        }
    }
}
