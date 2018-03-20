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

import org.apache.poi.POIReadOnlyDocument;
import org.apache.poi.hpbf.model.EscherDelayStm;
import org.apache.poi.hpbf.model.EscherStm;
import org.apache.poi.hpbf.model.MainContents;
import org.apache.poi.hpbf.model.QuillContents;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * This class provides the basic functionality
 *  for HPBF, our implementation of the publisher
 *  file format.
 */
public final class HPBFDocument extends POIReadOnlyDocument {
	private MainContents mainContents;
	private QuillContents quillContents;
	private EscherStm escherStm;
	private EscherDelayStm escherDelayStm;

	/**
	 * Opens a new publisher document
	 */
	public HPBFDocument(POIFSFileSystem fs) throws IOException {
	   this(fs.getRoot());
	}
	public HPBFDocument(NPOIFSFileSystem fs) throws IOException {
	   this(fs.getRoot());
	}
	public HPBFDocument(InputStream inp) throws IOException {
	   this(new NPOIFSFileSystem(inp));
	}

	/**
	 * Opens an embedded publisher document,
	 *  at the given directory.
	 */
	public HPBFDocument(DirectoryNode dir) throws IOException {
	   super(dir);

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
}
