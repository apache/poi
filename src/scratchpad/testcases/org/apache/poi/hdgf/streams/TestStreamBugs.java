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

package org.apache.poi.hdgf.streams;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.hdgf.chunks.ChunkFactory;
import org.apache.poi.hdgf.pointers.Pointer;
import org.apache.poi.hdgf.pointers.PointerFactory;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for bugs with streams
 */
public final class TestStreamBugs extends StreamTest {
	private byte[] contents;
	private ChunkFactory chunkFactory;
	private PointerFactory ptrFactory;
	private POIFSFileSystem filesystem;

	@BeforeEach
    void setUp() throws IOException {
		ptrFactory = new PointerFactory(11);
		chunkFactory = new ChunkFactory(11);

        InputStream is = POIDataSamples.getDiagramInstance().openResourceAsStream("44594.vsd");
        filesystem = new POIFSFileSystem(is);
        is.close();

		// Grab the document stream
		InputStream is2 = filesystem.createDocumentInputStream("VisioDocument");
		contents = IOUtils.toByteArray(is2);
		is2.close();
	}

	@Test
    void testGetTrailer() {
		Pointer trailerPointer = ptrFactory.createPointer(contents, 0x24);
		Stream.createStream(trailerPointer, contents, chunkFactory, ptrFactory);
	}

	@SuppressWarnings("unused")
    void TOIMPLEMENTtestGetCertainChunks() {
		int offsetA = 3708;
		int offsetB = 3744;
	}

	@Test
    void testGetChildren() {
		Pointer trailerPointer = ptrFactory.createPointer(contents, 0x24);
		TrailerStream trailer = (TrailerStream)
			Stream.createStream(trailerPointer, contents, chunkFactory, ptrFactory);

		// Get without recursing
		Pointer[] ptrs = trailer.getChildPointers();
		for (Pointer ptr : ptrs) {
			Stream.createStream(ptr, contents, chunkFactory, ptrFactory);
		}

		// Get with recursing into chunks
		for (Pointer ptr : ptrs) {
			Stream stream =
				Stream.createStream(ptr, contents, chunkFactory, ptrFactory);
			if(stream instanceof ChunkStream) {
				ChunkStream cStream = (ChunkStream)stream;
				cStream.findChunks();
			}
		}

		// Get with recursing into chunks and pointers
		for (Pointer ptr : ptrs) {
			Stream stream =
				Stream.createStream(ptr, contents, chunkFactory, ptrFactory);
			if(stream instanceof PointerContainingStream) {
				PointerContainingStream pStream =
					(PointerContainingStream)stream;
				pStream.findChildren(contents);
			}
		}

		trailer.findChildren(contents);
	}

	@Test
    void testOpen() throws IOException {
		new HDGFDiagram(filesystem).close();
	}
}
