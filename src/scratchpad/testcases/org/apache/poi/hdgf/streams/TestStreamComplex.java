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

import org.apache.poi.hdgf.chunks.Chunk;
import org.apache.poi.hdgf.chunks.ChunkFactory;
import org.apache.poi.hdgf.pointers.Pointer;
import org.apache.poi.hdgf.pointers.PointerFactory;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;

public final class TestStreamComplex extends StreamTest {
	private byte[] contents;
	private int trailerPointerAt = 0x24;
	private int trailerDataAt = 0x8a94;
	private ChunkFactory chunkFactory;
	private PointerFactory ptrFactory;

	protected void setUp() throws Exception {
		ptrFactory = new PointerFactory(11);
		chunkFactory = new ChunkFactory(11);

        InputStream is = POIDataSamples.getDiagramInstance().openResourceAsStream("Test_Visio-Some_Random_Text.vsd");
		POIFSFileSystem filesystem = new POIFSFileSystem(is);

		DocumentEntry docProps =
			(DocumentEntry)filesystem.getRoot().getEntry("VisioDocument");

		// Grab the document stream
		contents = new byte[docProps.getSize()];
		filesystem.createDocumentInputStream("VisioDocument").read(contents);
	}

	/**
	 * Test creating the trailer, but not looking for children
	 */
	public void testTrailer() {
		// Find the trailer
		Pointer trailerPtr = ptrFactory.createPointer(contents, trailerPointerAt);

		assertEquals(20, trailerPtr.getType());
		assertEquals(trailerDataAt, trailerPtr.getOffset());

		Stream stream = Stream.createStream(trailerPtr, contents, chunkFactory, ptrFactory);
		assertTrue(stream instanceof TrailerStream);
		TrailerStream ts = (TrailerStream)stream;

		assertNotNull(ts.getChildPointers());
		assertNull(ts.getPointedToStreams());

		assertEquals(20, ts.getChildPointers().length);
		assertEquals(0x16, ts.getChildPointers()[0].getType());
		assertEquals(0x17, ts.getChildPointers()[1].getType());
		assertEquals(0x17, ts.getChildPointers()[2].getType());
		assertEquals(0xff, ts.getChildPointers()[3].getType());
	}

	public void testChunks() {
		Pointer trailerPtr = ptrFactory.createPointer(contents, trailerPointerAt);
		TrailerStream ts = (TrailerStream)
			Stream.createStream(trailerPtr, contents, chunkFactory, ptrFactory);

		// Should be 7th one
		Pointer chunkPtr = ts.getChildPointers()[5];
		assertFalse(chunkPtr.destinationHasStrings());
		assertTrue(chunkPtr.destinationHasChunks());
		assertFalse(chunkPtr.destinationHasPointers());

		Stream stream = Stream.createStream(chunkPtr, contents, chunkFactory, ptrFactory);
		assertNotNull(stream);
		assertTrue(stream instanceof ChunkStream);

		// Now find the chunks within it
		ChunkStream cs = (ChunkStream)stream;
		cs.findChunks();
	}

	public void testStrings() {
		Pointer trailerPtr = ptrFactory.createPointer(contents, trailerPointerAt);
		TrailerStream ts = (TrailerStream)
			Stream.createStream(trailerPtr, contents, chunkFactory, ptrFactory);

		// Should be the 1st one
		Pointer stringPtr = ts.getChildPointers()[0];
		assertTrue(stringPtr.destinationHasStrings());
		assertFalse(stringPtr.destinationHasChunks());
		assertFalse(stringPtr.destinationHasPointers());

		Stream stream = Stream.createStream(stringPtr, contents, chunkFactory, ptrFactory);
		assertNotNull(stream);
		assertTrue(stream instanceof StringsStream);
	}

	public void testPointerToStrings() {
		// The stream at 0x347f has strings
		// The stream at 0x4312 has a pointer to 0x347f
		// The stream at 0x44d3 has a pointer to 0x4312
		//  (it's the 2nd one of 3, and the block is compressed)

		TestPointer ptr44d3 = new TestPointer(true, 0x44d3, 0x51, 0x4e, (short)0x56);
		ptr44d3.hasPointers = true;
		PointerContainingStream s44d3 = (PointerContainingStream)
			Stream.createStream(ptr44d3, contents, chunkFactory, ptrFactory);

		// Type: 0d  Addr: 014ff644  Offset: 4312  Len: 48  Format: 54  From: 44d3
		Pointer ptr4312 = s44d3.getChildPointers()[1];
		assertEquals(0x0d, ptr4312.getType());
		assertEquals(0x4312, ptr4312.getOffset());
		assertEquals(0x48, ptr4312.getLength());
		assertEquals(0x54, ptr4312.getFormat());
		assertTrue(ptr4312.destinationHasPointers());
		assertFalse(ptr4312.destinationHasStrings());

		PointerContainingStream s4312 = (PointerContainingStream)
			Stream.createStream(ptr4312, contents, chunkFactory, ptrFactory);

		// Check it has 0x347f
		// Type: 1f  Addr: 01540004  Offset: 347f  Len: 8e8  Format: 46  From: 4312
		assertEquals(2, s4312.getChildPointers().length);
		Pointer ptr347f = s4312.getChildPointers()[0];
		assertEquals(0x1f, ptr347f.getType());
		assertEquals(0x347f, ptr347f.getOffset());
		assertEquals(0x8e8, ptr347f.getLength());
		assertEquals(0x46, ptr347f.getFormat());
		assertFalse(ptr347f.destinationHasPointers());
		assertTrue(ptr347f.destinationHasStrings());

		// Find the children of 0x4312
		assertNull(s4312.getPointedToStreams());
		s4312.findChildren(contents);
		// Should have two, both strings
		assertNotNull(s4312.getPointedToStreams());
		assertEquals(2, s4312.getPointedToStreams().length);
		assertTrue(s4312.getPointedToStreams()[0] instanceof StringsStream);
		assertTrue(s4312.getPointedToStreams()[1] instanceof StringsStream);
	}

	public void testTrailerContents() {
		Pointer trailerPtr = ptrFactory.createPointer(contents, trailerPointerAt);
		TrailerStream ts = (TrailerStream)
			Stream.createStream(trailerPtr, contents, chunkFactory, ptrFactory);

		assertNotNull(ts.getChildPointers());
		assertNull(ts.getPointedToStreams());
		assertEquals(20, ts.getChildPointers().length);

		ts.findChildren(contents);

		assertNotNull(ts.getChildPointers());
		assertNotNull(ts.getPointedToStreams());
		assertEquals(20, ts.getChildPointers().length);
		assertEquals(20, ts.getPointedToStreams().length);

		// Step down:
		// 8 -> 4 -> 5 -> 1 -> 0 == String
		assertNotNull(ts.getPointedToStreams()[8]);
		assertTrue(ts.getPointedToStreams()[8] instanceof PointerContainingStream);

		PointerContainingStream s8 =
			(PointerContainingStream)ts.getPointedToStreams()[8];
		assertNotNull(s8.getPointedToStreams());

		assertNotNull(s8.getPointedToStreams()[4]);
		assertTrue(s8.getPointedToStreams()[4] instanceof PointerContainingStream);

		PointerContainingStream s84 =
			(PointerContainingStream)s8.getPointedToStreams()[4];
		assertNotNull(s84.getPointedToStreams());

		assertNotNull(s84.getPointedToStreams()[5]);
		assertTrue(s84.getPointedToStreams()[5] instanceof PointerContainingStream);

		PointerContainingStream s845 =
			(PointerContainingStream)s84.getPointedToStreams()[5];
		assertNotNull(s845.getPointedToStreams());

		assertNotNull(s845.getPointedToStreams()[1]);
		assertTrue(s845.getPointedToStreams()[1] instanceof PointerContainingStream);

		PointerContainingStream s8451 =
			(PointerContainingStream)s845.getPointedToStreams()[1];
		assertNotNull(s8451.getPointedToStreams());

		assertNotNull(s8451.getPointedToStreams()[0]);
		assertTrue(s8451.getPointedToStreams()[0] instanceof StringsStream);
		assertTrue(s8451.getPointedToStreams()[1] instanceof StringsStream);
	}

	public void testChunkWithText() {
		// Parent ChunkStream is at 0x7194
		// This is one of the last children of the trailer
		Pointer trailerPtr = ptrFactory.createPointer(contents, trailerPointerAt);
		TrailerStream ts = (TrailerStream)
			Stream.createStream(trailerPtr, contents, chunkFactory, ptrFactory);

		ts.findChildren(contents);

		assertNotNull(ts.getChildPointers());
		assertNotNull(ts.getPointedToStreams());
		assertEquals(20, ts.getChildPointers().length);
		assertEquals(20, ts.getPointedToStreams().length);

		assertEquals(0x7194, ts.getChildPointers()[13].getOffset());
		assertEquals(0x7194, ts.getPointedToStreams()[13].getPointer().getOffset());

		PointerContainingStream ps7194 = (PointerContainingStream)
			ts.getPointedToStreams()[13];

		// First child is at 0x64b3
		assertEquals(0x64b3, ps7194.getChildPointers()[0].getOffset());
		assertEquals(0x64b3, ps7194.getPointedToStreams()[0].getPointer().getOffset());

		ChunkStream cs = (ChunkStream)ps7194.getPointedToStreams()[0];

		// Should be 26bc bytes un-compressed
		assertEquals(0x26bc, cs.getStore().getContents().length);
		// And should have lots of children
		assertEquals(131, cs.getChunks().length);

		// One of which is Text
		boolean hasText = false;
		for(int i=0; i<cs.getChunks().length; i++) {
			if(cs.getChunks()[i].getName().equals("Text")) {
				hasText = true;
			}
		}
		assertTrue(hasText);
		// Which is the 72nd command
		assertEquals("Text", cs.getChunks()[72].getName());

		Chunk text = cs.getChunks()[72];
		assertEquals("Text", text.getName());

		// Which contains our text
		assertEquals(1, text.getCommands().length);
		assertEquals("Test View\n", text.getCommands()[0].getValue());


		// Almost at the end is some more text
		assertEquals("Text", cs.getChunks()[128].getName());
		text = cs.getChunks()[128];
		assertEquals("Text", text.getName());

		assertEquals(1, text.getCommands().length);
		assertEquals("Some random text, on a page\n", text.getCommands()[0].getValue());
	}
}
