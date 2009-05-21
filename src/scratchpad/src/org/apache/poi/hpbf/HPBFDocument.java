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

package org.apache.poi.hpbf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.POIDocument;
import org.apache.poi.hpbf.model.EscherDelayStm;
import org.apache.poi.hpbf.model.EscherStm;
import org.apache.poi.hpbf.model.MainContents;
import org.apache.poi.hpbf.model.QuillContents;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * This class provides the basic functionality
 *  for HPBF, our implementation of the publisher
 *  file format.
 */
public final class HPBFDocument extends POIDocument {
	private MainContents mainContents;
	private QuillContents quillContents;
	private EscherStm escherStm;
	private EscherDelayStm escherDelayStm;

	/**
	 * Opens a new publisher document
	 */
	public HPBFDocument(POIFSFileSystem fs) throws IOException {
		this(fs.getRoot(), fs);
	}
	public HPBFDocument(InputStream inp) throws IOException {
		this(new POIFSFileSystem(inp));
	}

	/**
	 * Opens an embeded publisher document,
	 *  at the given directory.
	 */
	public HPBFDocument(DirectoryNode dir, POIFSFileSystem fs) throws IOException {
		super(dir, fs);

		// Go looking for our interesting child
		//  streams
		mainContents = new MainContents(dir);
		quillContents = new QuillContents(dir);

		// Now the Escher bits
		escherStm = new EscherStm(dir);
		escherDelayStm = new EscherDelayStm(dir);
	}

	public MainContents getMainContents() {
		return mainContents;
	}
	public QuillContents getQuillContents() {
		return quillContents;
	}
	public EscherStm getEscherStm() {
		return escherStm;
	}
	public EscherDelayStm getEscherDelayStm() {
		return escherDelayStm;
	}

	public void write(OutputStream out) throws IOException {
		throw new IllegalStateException("Writing is not yet implemented, see http://poi.apache.org/hpbf/");
	}
}
