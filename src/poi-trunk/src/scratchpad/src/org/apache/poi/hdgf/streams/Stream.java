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

import org.apache.poi.hdgf.chunks.ChunkFactory;
import org.apache.poi.hdgf.pointers.Pointer;
import org.apache.poi.hdgf.pointers.PointerFactory;
import org.apache.poi.hdgf.exceptions.HDGFException;

/**
 * Base of all Streams within a HDGF document.
 * Streams are what hold the data (the metadata of a stream
 *  is held in the pointer that points to the stream).
 * A stream may be stored compressed or un-compressed on the
 *  disk, but that doesn't appear to change their use.
 */
public abstract class Stream {
	private Pointer pointer;
	private StreamStore store;

	public Pointer getPointer() { return pointer; }
	protected StreamStore getStore() { return store; }
	public StreamStore _getStore() { return store; }
	public int _getContentsLength() { return store.getContents().length; }

	/**
	 * Creates a new Stream, having already used the pointer
	 *  to build a store
	 */
	protected Stream(Pointer pointer, StreamStore store) {
		this.pointer = pointer;
		this.store = store;
	}

	/**
	 * Uses the pointer to locate a Stream within the document
	 *  data, and creates it.
	 * @param pointer The Pointer to create a stream for
	 * @param documentData The raw document data
	 */
	public static Stream createStream(Pointer pointer, byte[] documentData, ChunkFactory chunkFactory, PointerFactory pointerFactory) {
		// Create the store
		StreamStore store;
		if(pointer.destinationCompressed()) {
			try {
				store = new CompressedStreamStore(
					documentData, pointer.getOffset(), pointer.getLength()
				);
			} catch(IOException e) {
				// Should never occur
				throw new HDGFException(e);
			}
		} else {
			store = new StreamStore(
					documentData, pointer.getOffset(), pointer.getLength()
			);
		}

		// Figure out what sort of Stream to create, create and return it
		if(pointer.getType() == 20) {
			return new TrailerStream(pointer, store, chunkFactory, pointerFactory);
		}
		else if(pointer.destinationHasPointers()) {
			return new PointerContainingStream(pointer, store, chunkFactory, pointerFactory);
		}
		else if(pointer.destinationHasChunks()) {
			return new ChunkStream(pointer, store, chunkFactory);
		}
		else if(pointer.destinationHasStrings()) {
			return new StringsStream(pointer, store, chunkFactory);
		}

		// Give up and return a generic one
		return new UnknownStream(pointer, store);
	}
}
