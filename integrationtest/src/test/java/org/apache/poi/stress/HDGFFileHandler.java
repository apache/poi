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
package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hdgf.streams.Stream;
import org.apache.poi.hdgf.streams.TrailerStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

class HDGFFileHandler extends POIFSFileHandler {
	@Override
	public void handleFile(InputStream stream, String path) throws IOException {
	    POIFSFileSystem poifs = new POIFSFileSystem(stream);
		HDGFDiagram diagram = new HDGFDiagram(poifs);
		Stream[] topLevelStreams = diagram.getTopLevelStreams();
		assertNotNull(topLevelStreams);
		for(Stream str : topLevelStreams) {
			assertTrue(str.getPointer().getLength() >= 0);
		}

		TrailerStream trailerStream = diagram.getTrailerStream();
		assertNotNull(trailerStream);
		assertTrue(trailerStream.getPointer().getLength() >= 0);
		diagram.close();
		poifs.close();

		// writing is not yet implemented... handlePOIDocument(diagram);
	}

	// a test-case to test this locally without executing the full TestAllFiles
	@Override
    @Test
	void test() throws Exception {
        File file = new File("test-data/diagram/44501.vsd");

        InputStream stream = new FileInputStream(file);
		try {
			handleFile(stream, file.getPath());
		} finally {
			stream.close();
		}

		handleExtracting(file);

		stream = new FileInputStream(file);
		try {
			try (VisioTextExtractor extractor = new VisioTextExtractor(stream)) {
				assertNotNull(extractor.getText());
			}
		} finally {
			stream.close();
		}
	}
}
