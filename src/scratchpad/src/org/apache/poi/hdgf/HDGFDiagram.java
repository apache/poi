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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

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
public class HDGFDiagram {
	private static final String VISIO_HEADER = "Visio (TM) Drawing\r\n";
	
	private POIFSFileSystem filesystem;
	private byte[] _docstream;
	
	private short version;
	private long docSize;
	
	private VisioPointer trailerPointer;
	private PointerBlock trailer;
	
	public HDGFDiagram(POIFSFileSystem fs) throws IOException {
		filesystem = fs;
		
		DocumentEntry docProps =
			(DocumentEntry)filesystem.getRoot().getEntry("VisioDocument");

		// Grab the document stream
		_docstream = new byte[docProps.getSize()];
		filesystem.createDocumentInputStream("VisioDocument").read(_docstream);
		
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
		
		// Grab the pointer to the trailer
		trailerPointer = VisioPointer.getPointerAt(_docstream, 0x24);
		
		// And now grab the trailer
		trailer = new CompressedPointerBlock(trailerPointer, _docstream);
	}
	
	public void debug() throws IOException {
		System.err.println("Trailer is at " + trailerPointer.offset);
		System.err.println("Trailer has type " + trailerPointer.type);
		System.err.println("Trailer has length " + trailerPointer.length);
		System.err.println("Trailer has format " + trailerPointer.format);
		
		for(int i=0; i<trailer.getPointers().length; i++) {
			VisioPointer ptr = trailer.getPointers()[i];
			
			System.err.println("Looking at pointer " + i);
			System.err.println("\tType is " + ptr.type + "\t\t" + Integer.toHexString(ptr.type));
			System.err.println("\tOffset is " + ptr.offset + "\t\t" + Long.toHexString(ptr.offset));
			System.err.println("\tAddress is " + ptr.address + "\t" + Long.toHexString(ptr.address));
			System.err.println("\tLength is " + ptr.length + "\t\t" + Long.toHexString(ptr.length));
			System.err.println("\tFormat is " + ptr.format + "\t\t" + Long.toHexString(ptr.format));
			System.err.println("\tCompressed is " + ptr.destinationCompressed());
			
			if(ptr.destinationHasPointers()) {
				PointerBlock pb = PointerBlock.createAppropriateBlock(ptr, _docstream);
				
				if(pb.getPointers() != null && pb.getPointers().length > 0) {
					System.err.println("\tContains " + pb.getPointers().length + " other pointers");
					for(int j=0; j<pb.getPointers().length; j++) {
						VisioPointer sptr = pb.getPointers()[j];
						System.err.println("\t\t" + j + " - Type is " + sptr.type + "\t\t" + Integer.toHexString(sptr.type));
						System.err.println("\t\t" + j + " - Length is " + sptr.length + "\t\t" + Long.toHexString(sptr.length));
					}
				}
			}
			
			if(ptr.destinationHasStrings()) {
				System.err.println("**strings**");
				PointerBlock pb = PointerBlock.createAppropriateBlock(ptr, _docstream);
				System.err.println(pb.contents.length);
			}
		}
	}
	/**
	 * Will only work on Test_Visio-Some_Random_Text.vsd !
	 */
	public void debugTestFile() throws Exception {
		System.err.println();
		
		VisioPointer p61ee = trailer.pointers[8];
		System.err.println(p61ee.type + " " + Integer.toHexString(p61ee.type));
		PointerBlock pb61ee = PointerBlock.createAppropriateBlock(p61ee, _docstream);
		
		VisioPointer p4524 = pb61ee.pointers[4];
		System.err.println(p4524.type + " " + Integer.toHexString(p4524.type));
		PointerBlock pb4524 = PointerBlock.createAppropriateBlock(p4524, _docstream);
		
		VisioPointer p44d3 = pb4524.pointers[5];
		System.err.println(p44d3.type + " " + Integer.toHexString(p44d3.type));
		PointerBlock pb44d3 = PointerBlock.createAppropriateBlock(p44d3, _docstream);
		
		VisioPointer p4312 = pb44d3.pointers[1];
		System.err.println(p4312.type + " " + Integer.toHexString(p4312.type));
		PointerBlock pb4312 = PointerBlock.createAppropriateBlock(p4312, _docstream);
		
		VisioPointer p347f = pb4312.pointers[0];
		System.err.println();
		System.err.println(p347f.type + " " + Integer.toHexString(p347f.type));
		System.err.println(p347f.offset + " " + Long.toHexString(p347f.offset));
		System.err.println(p347f.length + " " + Long.toHexString(p347f.length));
		System.err.println("Has Strings - " + p347f.destinationHasStrings());
		System.err.println("Compressed - " + p347f.destinationCompressed());
		
		PointerBlock pb347f = PointerBlock.createAppropriateBlock(p347f, _docstream);
	}
	
	/**
	 * For testing only
	 */
	public static void main(String args[]) throws Exception {
		HDGFDiagram hdgf = new HDGFDiagram(new POIFSFileSystem(new FileInputStream(args[0])));
		hdgf.debug();
		hdgf.debugTestFile();
	}

	/**
	 * A block containing lots of pointers to other blocks.
	 */
	public static class PointerBlock {
		protected VisioPointer pointer;
		private byte[] contents;
		
		protected VisioPointer[] pointers;
		
		protected PointerBlock(VisioPointer pointer) {
			this.pointer = pointer;
		}
		protected PointerBlock(VisioPointer pointer, byte[] data) {
			this(pointer);
			
			processData(data, (int)pointer.offset, (int)pointer.length);
		}
		
		protected static PointerBlock createAppropriateBlock(VisioPointer pointer, byte[] data) throws IOException {
			if(pointer.destinationCompressed()) {
				return new CompressedPointerBlock(pointer,data);
			} else {
				return new PointerBlock(pointer,data);
			}
		}
		
		public VisioPointer[] getPointers() { return pointers; }
		
		/**
		 * Splits the data up into header + contents, and processes 
		 */
		protected void processData(byte[] data, int offset, int len) {
			if(len > data.length - offset) {
				len = data.length - offset;
			}
			if(offset < 0) { len = 0; }
			
			contents = new byte[len];
			if(len > 0)
				System.arraycopy(data, offset, contents, 0, contents.length);
			
			// If we're of type 20, we have child pointers
			if(len > 0 && (pointer.type == 20 || pointer.destinationHasPointers())) {
				// Grab the offset to the number of pointers
				int nPointersAt = (int)LittleEndian.getUInt(contents, 0);
				int numPointers = (int)LittleEndian.getUInt(contents, nPointersAt);
				int unknownA = (int)LittleEndian.getUInt(contents, nPointersAt+4);

				pointers = new VisioPointer[numPointers];
				int pos = nPointersAt + 8;
				for(int i=0; i<numPointers; i++) {
					pointers[i] = VisioPointer.getPointerAt(contents, pos);
					pos += 18;
				}
			}
			
			// If we have strings, try to make sense of them
			if(len > 0 && (pointer.destinationHasStrings())) {
				for(int i=0; i<64; i+=1) {
					short s = LittleEndian.getShort(contents, i);
					long l = LittleEndian.getUInt(contents, i);
					System.err.println(i + "\t" + s + "\t" + Integer.toHexString(s));
					System.err.println(i + "\t" + l + "\t" + Long.toHexString(l));
				}
			}
		}
	}
	
	/**
	 * A block containing lots of pointers to other blocks, that
	 *  is itself compressed
	 */
	public static class CompressedPointerBlock extends PointerBlock {
		protected byte[] compressedContents;
		private byte[] blockHeader = new byte[4];
		
		protected CompressedPointerBlock(VisioPointer pointer, byte[] data) throws IOException {
			super(pointer);
			
			compressedContents = new byte[(int)pointer.length];
			System.arraycopy(data, (int)pointer.offset, compressedContents, 0, compressedContents.length);
			
			// Decompress
			ByteArrayInputStream bais = new ByteArrayInputStream(compressedContents);
			
//			TIFFLZWDecoder lzw = new TIFFLZWDecoder();
//			byte[] out = new byte[4096]; 
//			contents = lzw.decode(compressedContents, out);
			
			LZW4HDGF lzw = new LZW4HDGF();
			byte[] decomp = lzw.decode(bais);
			System.arraycopy(decomp, 0, blockHeader, 0, 4);
			processData(decomp, 4, decomp.length-4);
		}
	}
	
	/**
	 * A visio pointer, for visio versions 6+
	 */
	public static class VisioPointer {
		private int type;
		private long address;
		private long offset;
		private long length;
		private short format;
		
		public boolean destinationHasStrings() {
			return (0x40 <= format && format < 0x50);
		}
		public boolean destinationHasPointers() {
			if(format == 0x1d || format == 0x1e) return true;
			return (0x50 <= format && format < 0x60);
		}
		public boolean destinationHasChunks() {
			return (0xd0 <= format && format < 0xd0);
		}
		
		public boolean destinationCompressed() {
			// Apparently, it's the second least significant bit
			return (format & 2) > 0;
		}

		public static VisioPointer getPointerAt(byte[] data, int offset) {
			VisioPointer p = new VisioPointer();
			p.type = LittleEndian.getInt(data, offset+0);
			p.address = LittleEndian.getUInt(data, offset+4);
			p.offset = LittleEndian.getUInt(data, offset+8);
			p.length = LittleEndian.getUInt(data, offset+12);
			p.format = LittleEndian.getShort(data, offset+16);
			
			return p;
		}
	}
}
