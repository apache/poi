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

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.hdgf.chunks.ChunkFactory;
import org.apache.poi.hdgf.pointers.Pointer;
import org.apache.poi.hdgf.pointers.PointerFactory;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;

/**
 * Tests for bugs with streams
 */
public final class TestStreamBugs extends StreamTest {
	private byte[] contents;
	private ChunkFactory chunkFactory;
	private PointerFactory ptrFactory;
	private POIFSFileSystem filesystem;

	protected void setUp() throws Exception {
		ptrFactory = new PointerFactory(11);
		chunkFactory = new ChunkFactory(11);

        InputStream is = POIDataSamples.getDiagramInstance().openResourceAsStream("44594.vsd");
        filesystem = new POIFSFileSystem(is);

		DocumentEntry docProps =
			(DocumentEntry)filesystem.getRoot().getEntry("VisioDocument");

		// Grab the document stream
		contents = new byte[docProps.getSize()];
		filesystem.createDocumentInputStream("VisioDocument").read(contents);
	}

	public void testGetTrailer() {
		Pointer trailerPointer = ptrFactory.createPointer(contents, 0x24);
		Stream.createStream(trailerPointer, contents, chunkFactory, ptrFactory);
	}

	public void TOIMPLEMENTtestGetCertainChunks() {
		int offsetA = 3708;
		int offsetB = 3744;
	}

	public void testGetChildren() {
		Pointer trailerPointer = ptrFactory.createPointer(contents, 0x24);
		TrailerStream trailer = (TrailerStream)
			Stream.createStream(trailerPointer, contents, chunkFactory, ptrFactory);

		// Get without recursing
		Pointer[] ptrs = trailer.getChildPointers();
		for(int i=0; i<ptrs.length; i++) {
			Stream.createStream(ptrs[i], contents, chunkFactory, ptrFactory);
		}

		// Get with recursing into chunks
		for(int i=0; i<ptrs.length; i++) {
			Stream stream =
				Stream.createStream(ptrs[i], contents, chunkFactory, ptrFactory);
			if(stream instanceof ChunkStream) {
				ChunkStream cStream = (ChunkStream)stream;
				cStream.findChunks();
			}
		}

		// Get with recursing into chunks and pointers
		for(int i=0; i<ptrs.length; i++) {
			Stream stream =
				Stream.createStream(ptrs[i], contents, chunkFactory, ptrFactory);
			if(stream instanceof PointerContainingStream) {
				PointerContainingStream pStream =
					(PointerContainingStream)stream;
				pStream.findChildren(contents);
			}
		}

		trailer.findChildren(contents);
	}

	public void testOpen() throws Exception {
		HDGFDiagram dg = new HDGFDiagram(filesystem);
	}
}
