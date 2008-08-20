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
	
	public HPBFPart(DirectoryNode baseDir) throws FileNotFoundException, IOException {
		String[] path = getPath(); 
		DirectoryNode dir = getDir(path, baseDir);
		String name = path[path.length-1];
		
		DocumentEntry docProps =
			(DocumentEntry)dir.getEntry(name);

		// Grab the data from the part stream
		data = new byte[docProps.getSize()];
		dir.createDocumentInputStream(name).read(data);
	}
	private DirectoryNode getDir(String[] path, DirectoryNode baseDir) throws FileNotFoundException {
		DirectoryNode dir = baseDir;
		for(int i=0; i<path.length-1; i++) {
			dir = (DirectoryNode)dir.getEntry(path[i]);
		}
		return dir;
	}
	
	public void writeOut(DirectoryNode baseDir) throws IOException {
		
	}
	
	/**
	 * Returns the raw data that makes up
	 *  this document part.
	 */
	public byte[] getData() { return data; }

	/**
	 * Returns the path to the part, eg Contents
	 *  or Quill, QuillSub, CONTENTS
	 */
	public abstract String[] getPath();
}
