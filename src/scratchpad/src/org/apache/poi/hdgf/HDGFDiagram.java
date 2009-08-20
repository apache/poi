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

package org.apache.poi.hdgf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.POIDocument;
import org.apache.poi.hdgf.chunks.ChunkFactory;
import org.apache.poi.hdgf.pointers.Pointer;
import org.apache.poi.hdgf.pointers.PointerFactory;
import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.hdgf.streams.Stream;
import org.apache.poi.hdgf.streams.StringsStream;
import org.apache.poi.hdgf.streams.TrailerStream;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;

/**
 * See
 *  http://www.redferni.uklinux.net/visio/
 *  http://www.gnome.ru/projects/docs/vsdocs.html
 *  http://www.gnome.ru/projects/docs/slide1.png
 *  http://www.gnome.ru/projects/docs/slide2.png
 */
public final class HDGFDiagram extends POIDocument {
	private static final String VISIO_HEADER = "Visio (TM) Drawing\r\n";

	private byte[] _docstream;

	private short version;
	private long docSize;

	private Pointer trailerPointer;
	private TrailerStream trailer;

	private ChunkFactory chunkFactory;
	private PointerFactory ptrFactory;

	public HDGFDiagram(POIFSFileSystem fs) throws IOException {
		this(fs.getRoot(), fs);
	}
	public HDGFDiagram(DirectoryNode dir, POIFSFileSystem fs) throws IOException {
		super(dir, fs);

		DocumentEntry docProps =
			(DocumentEntry)dir.getEntry("VisioDocument");

		// Grab the document stream
		_docstream = new byte[docProps.getSize()];
		dir.createDocumentInputStream("VisioDocument").read(_docstream);

		// Check it's really visio
		String typeString = new String(_docstream, 0, 20);
		if(! typeString.equals(VISIO_HEADER)) {
			throw new IllegalArgumentException("Wasn't a valid visio document, started with " + typeString);
		}

		// Grab the version number, 0x1a -> 0x1b
		version = LittleEndian.getShort(_docstream, 0x1a);
		// Grab the document size, 0x1c -> 0x1f
		docSize = LittleEndian.getUInt(_docstream, 0x1c);
		// ??? 0x20 -> 0x23

		// Create the Chunk+Pointer Factories for the document version
		ptrFactory = new PointerFactory(version);
		chunkFactory = new ChunkFactory(version);

		// Grab the pointer to the trailer
		trailerPointer = ptrFactory.createPointer(_docstream, 0x24);

		// Now grab the trailer
		trailer = (TrailerStream)
			Stream.createStream(trailerPointer, _docstream, chunkFactory, ptrFactory);

		// Finally, find all our streams
		trailer.findChildren(_docstream);
	}

	/**
	 * Returns the TrailerStream, which is at the root of the
	 *  tree of Streams.
	 */
	public TrailerStream getTrailerStream() { return trailer; }
	/**
	 * Returns all the top level streams, which are the streams
	 *  pointed to by the TrailerStream.
	 */
	public Stream[] getTopLevelStreams() { return trailer.getPointedToStreams(); }
	public long getDocumentSize() { return docSize; }

	/**
	 * Prints out some simple debug on the base contents of the file.
	 * @see org.apache.poi.hdgf.dev.VSDDumper
	 */
	public void debug() {
		System.err.println("Trailer is at " + trailerPointer.getOffset());
		System.err.println("Trailer has type " + trailerPointer.getType());
		System.err.println("Trailer has length " + trailerPointer.getLength());
		System.err.println("Trailer has format " + trailerPointer.getFormat());

		for(int i=0; i<trailer.getPointedToStreams().length; i++) {
			Stream stream = trailer.getPointedToStreams()[i];
			Pointer ptr = stream.getPointer();

			System.err.println("Looking at pointer " + i);
			System.err.println("\tType is " + ptr.getType() + "\t\t" + Integer.toHexString(ptr.getType()));
			System.err.println("\tOffset is " + ptr.getOffset() + "\t\t" + Long.toHexString(ptr.getOffset()));
			System.err.println("\tAddress is " + ptr.getAddress() + "\t" + Long.toHexString(ptr.getAddress()));
			System.err.println("\tLength is " + ptr.getLength() + "\t\t" + Long.toHexString(ptr.getLength()));
			System.err.println("\tFormat is " + ptr.getFormat() + "\t\t" + Long.toHexString(ptr.getFormat()));
			System.err.println("\tCompressed is " + ptr.destinationCompressed());
			System.err.println("\tStream is " + stream.getClass());

			if(stream instanceof PointerContainingStream) {
				PointerContainingStream pcs = (PointerContainingStream)stream;

				if(pcs.getPointedToStreams() != null && pcs.getPointedToStreams().length > 0) {
					System.err.println("\tContains " + pcs.getPointedToStreams().length + " other pointers/streams");
					for(int j=0; j<pcs.getPointedToStreams().length; j++) {
						Stream ss = pcs.getPointedToStreams()[j];
						Pointer sptr = ss.getPointer();
						System.err.println("\t\t" + j + " - Type is " + sptr.getType() + "\t\t" + Integer.toHexString(sptr.getType()));
						System.err.println("\t\t" + j + " - Length is " + sptr.getLength() + "\t\t" + Long.toHexString(sptr.getLength()));
					}
				}
			}

			if(stream instanceof StringsStream) {
				System.err.println("\t\t**strings**");
				StringsStream ss = (StringsStream)stream;
				System.err.println("\t\t" + ss._getContentsLength());
			}
		}
	}

	public void write(OutputStream out) {
		throw new IllegalStateException("Writing is not yet implemented, see http://poi.apache.org/hdgf/");
	}

	/**
	 * For testing only
	 */
	public static void main(String args[]) throws Exception {
		HDGFDiagram hdgf = new HDGFDiagram(new POIFSFileSystem(new FileInputStream(args[0])));
		hdgf.debug();
	}
}
