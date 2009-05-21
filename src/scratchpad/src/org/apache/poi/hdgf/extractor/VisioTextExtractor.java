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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.hdgf.chunks.Chunk;
import org.apache.poi.hdgf.chunks.Chunk.Command;
import org.apache.poi.hdgf.streams.ChunkStream;
import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.hdgf.streams.Stream;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Class to find all the text in a Visio file, and return it.
 * Can opperate on the command line (outputs to stdout), or
 *  can return the text for you (eg for use with Lucene).
 */
public final class VisioTextExtractor extends POIOLE2TextExtractor {
	private HDGFDiagram hdgf;
	private POIFSFileSystem fs;

	public VisioTextExtractor(HDGFDiagram hdgf) {
		super(hdgf);
		this.hdgf = hdgf;
	}
	public VisioTextExtractor(POIFSFileSystem fs) throws IOException {
		this(fs.getRoot(), fs);
	}
	public VisioTextExtractor(DirectoryNode dir, POIFSFileSystem fs) throws IOException {
		this(new HDGFDiagram(dir, fs));
		this.fs = fs;
	}
	public VisioTextExtractor(InputStream inp) throws IOException {
		this(new POIFSFileSystem(inp));
	}

	/**
	 * Locates all the text entries in the file, and returns their
	 *  contents.
	 */
	public String[] getAllText() {
		ArrayList text = new ArrayList();
		for(int i=0; i<hdgf.getTopLevelStreams().length; i++) {
			findText(hdgf.getTopLevelStreams()[i], text);
		}
		return (String[])text.toArray( new String[text.size()] );
	}
	private void findText(Stream stream, ArrayList text) {
		if(stream instanceof PointerContainingStream) {
			PointerContainingStream ps = (PointerContainingStream)stream;
			for(int i=0; i<ps.getPointedToStreams().length; i++) {
				findText(ps.getPointedToStreams()[i], text);
			}
		}
		if(stream instanceof ChunkStream) {
			ChunkStream cs = (ChunkStream)stream;
			for(int i=0; i<cs.getChunks().length; i++) {
				Chunk chunk = cs.getChunks()[i];
				if(chunk != null &&
						chunk.getName() != null &&
						chunk.getName().equals("Text") &&
						chunk.getCommands().length > 0) {
					// First command
					Command cmd = chunk.getCommands()[0];
					if(cmd != null && cmd.getValue() != null) {
						text.add( cmd.getValue().toString() );
					}
				}
			}
		}
	}

	/**
	 * Returns the textual contents of the file.
	 * Each textual object's text will be separated
	 *  by a newline
	 */
	public String getText() {
		StringBuffer text = new StringBuffer();
		String[] allText = getAllText();
		for(int i=0; i<allText.length; i++) {
			text.append(allText[i]);
			if(!allText[i].endsWith("\r") &&
					!allText[i].endsWith("\n")) {
				text.append("\n");
			}
		}
		return text.toString();
	}

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			System.err.println("Use:");
			System.err.println("   VisioTextExtractor <file.vsd>");
			System.exit(1);
		}

		VisioTextExtractor extractor =
			new VisioTextExtractor(new FileInputStream(args[0]));

		// Print not PrintLn as already has \n added to it
		System.out.print(extractor.getText());
	}
}
