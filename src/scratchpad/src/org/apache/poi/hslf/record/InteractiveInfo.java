/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;

/**
 * This class represents the metadata of a link in a slide/notes/etc.
 * It normally just holds a InteractiveInfoAtom, with the metadata
 *  in it.
 * @author Nick Burch
 */
public class InteractiveInfo extends RecordContainer {
	private byte[] _header;
	private static long _type = 4082;
	
	// Links to our more interesting children
	private InteractiveInfoAtom infoAtom;
	
	/** 
	 * Returns the InteractiveInfoAtom of this InteractiveInfo
	 */ 
	public InteractiveInfoAtom getInteractiveInfoAtom() { return infoAtom; }
	
	/** 
	 * Set things up, and find our more interesting children
	 */
	protected InteractiveInfo(byte[] source, int start, int len) {
		// Grab the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);
		findInterestingChildren();
	}

	/**
	 * Go through our child records, picking out the ones that are
	 *  interesting, and saving those for use by the easy helper
	 *  methods.
	 */	
	private void findInterestingChildren() {
		// First child should be the InteractiveInfoAtom
		if(_children[0] instanceof InteractiveInfoAtom) {
			infoAtom = (InteractiveInfoAtom)_children[0];
		} else {
			throw new IllegalStateException("First child record wasn't a InteractiveInfoAtom, was of type " + _children[0].getRecordType());
		}
	}
	
	/**
	 * Create a new InteractiveInfo, with blank fields
	 */
	public InteractiveInfo() {
		_header = new byte[8];
		_children = new Record[1];
		
		// Setup our header block
		_header[0] = 0x0f; // We are a container record
		LittleEndian.putShort(_header, 2, (short)_type);
		
		// Setup our child records
		infoAtom = new InteractiveInfoAtom();
		_children[0] = infoAtom;
	}

	/**
	 * We are of type 4802
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],_type,_children,out);
	}
}
