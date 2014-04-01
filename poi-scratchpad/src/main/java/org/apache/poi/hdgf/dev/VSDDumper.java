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

package org.apache.poi.hdgf.dev;

import java.io.FileInputStream;

import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.hdgf.chunks.Chunk;
import org.apache.poi.hdgf.chunks.Chunk.Command;
import org.apache.poi.hdgf.pointers.Pointer;
import org.apache.poi.hdgf.streams.ChunkStream;
import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.hdgf.streams.Stream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Developer helper class to dump out the pointer+stream structure
 *  of a Visio file
 */
public final class VSDDumper {
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			System.err.println("Use:");
			System.err.println("  VSDDumper <filename>");
			System.exit(1);
		}

		HDGFDiagram hdgf = new HDGFDiagram(
				new POIFSFileSystem(new FileInputStream(args[0]))
		);

		System.out.println("Opened " + args[0]);
		System.out.println("The document claims a size of " +
				hdgf.getDocumentSize() + "   (" +
				Long.toHexString(hdgf.getDocumentSize()) + ")");
		System.out.println();

		dumpStream(hdgf.getTrailerStream(), 0);
	}

	public static void dumpStream(Stream stream, int indent) {
		String ind = "";
		for(int i=0; i<indent; i++) {
			ind += "    ";
		}
		String ind2 = ind  + "    ";
		String ind3 = ind2 + "    ";


		Pointer ptr = stream.getPointer();
		System.out.println(ind + "Stream at\t" + ptr.getOffset() +
				" - " + Integer.toHexString(ptr.getOffset()));
		System.out.println(ind + "  Type is\t" + ptr.getType() +
				" - " + Integer.toHexString(ptr.getType()));
		System.out.println(ind + "  Format is\t" + ptr.getFormat() +
				" - " + Integer.toHexString(ptr.getFormat()));
		System.out.println(ind + "  Length is\t" + ptr.getLength() +
				" - " + Integer.toHexString(ptr.getLength()));
		if(ptr.destinationCompressed()) {
			int decompLen = stream._getContentsLength();
			System.out.println(ind + "  DC.Length is\t" + decompLen +
					" - " + Integer.toHexString(decompLen));
		}
		System.out.println(ind + "  Compressed is\t" + ptr.destinationCompressed());
		System.out.println(ind + "  Stream is\t" + stream.getClass().getName());

		byte[] db = stream._getStore()._getContents();
		String ds = "";
		if(db.length >= 8) {
			for(int i=0; i<8; i++) {
				if(i>0) ds += ", ";
				ds += db[i];
			}
		}
		System.out.println(ind + "  First few bytes are\t" + ds);

		if(stream instanceof PointerContainingStream) {
			PointerContainingStream pcs = (PointerContainingStream)stream;
			System.out.println(ind + "  Has " +
					pcs.getPointedToStreams().length + " children:");

			for(int i=0; i<pcs.getPointedToStreams().length; i++) {
				dumpStream(pcs.getPointedToStreams()[i], (indent+1));
			}
		}
		if(stream instanceof ChunkStream) {
			ChunkStream cs = (ChunkStream)stream;
			System.out.println(ind + "  Has " + cs.getChunks().length +
					" chunks:");

			for(int i=0; i<cs.getChunks().length; i++) {
				Chunk chunk = cs.getChunks()[i];
				System.out.println(ind2 + "" + chunk.getName());
				System.out.println(ind2 + "  Length is " + chunk._getContents().length + " (" + Integer.toHexString(chunk._getContents().length) + ")");
				System.out.println(ind2 + "  OD Size is " + chunk.getOnDiskSize() + " (" + Integer.toHexString(chunk.getOnDiskSize()) + ")");
				System.out.println(ind2 + "  T / S is " + chunk.getTrailer() + " / " + chunk.getSeparator());
				System.out.println(ind2 + "  Holds " + chunk.getCommands().length + " commands");
				for(int j=0; j<chunk.getCommands().length; j++) {
					Command command = chunk.getCommands()[j];
					System.out.println(ind3 + "" +
							command.getDefinition().getName() +
							" " + command.getValue()
					);
				}
			}
		}
	}
}
