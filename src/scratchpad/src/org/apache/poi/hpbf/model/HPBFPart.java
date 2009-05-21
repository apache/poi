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

package org.apache.poi.hpbf.model;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;

/**
 * Parent class of all HPBF sub-parts, handling
 *  the fiddly reading in / writing out bits
 *  for all of them.
 */
public abstract class HPBFPart {
	protected byte[] data;
	/**
	 * @param path  the path to the part, eg Contents or Quill, QuillSub, CONTENTS
	 */
	public HPBFPart(DirectoryNode baseDir, String[] path) throws IOException {

		DirectoryNode dir = getDir(path, baseDir);
		String name = path[path.length-1];

		DocumentEntry docProps;
		try {
			docProps = (DocumentEntry)dir.getEntry(name);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File invalid - failed to find document entry '"
					+ name + "'");
		}

		// Grab the data from the part stream
		data = new byte[docProps.getSize()];
		dir.createDocumentInputStream(name).read(data);
	}
	private DirectoryNode getDir(String[] path, DirectoryNode baseDir) {
		DirectoryNode dir = baseDir;
		for(int i=0; i<path.length-1; i++) {
			try {
				dir = (DirectoryNode)dir.getEntry(path[i]);
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("File invalid - failed to find directory entry '"
						+ path[i] + "'");
			}
		}
		return dir;
	}

	public void writeOut(DirectoryNode baseDir) throws IOException {
		String[] path = getPath();

		// Ensure that all parent directories exist
		DirectoryNode dir = baseDir;
		for(int i=0; i<path.length-1; i++) {
			try {
				dir = (DirectoryNode)dir.getEntry(path[i]);
			} catch(FileNotFoundException e) {
				dir.createDirectory(path[i]);
			}
		}

		// Update the byte array with the latest data
		generateData();

		// Write out
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		dir.createDocument(path[path.length-1], bais);
	}

	/**
	 * Called just before writing out, to trigger
	 *  the data byte array to be updated with the
	 *  latest contents.
	 */
	protected abstract void generateData();

	/**
	 * Returns the raw data that makes up
	 *  this document part.
	 */
	public byte[] getData() { return data; }

	/**
	 * Returns
	 */
	public final String[] getPath() {return null;}
}
