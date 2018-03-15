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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

/**
 * A container record that specifies information about external video data.
 *
 * @author Yegor Kozlov
 */
public final class ExVideoContainer extends RecordContainer {
	private byte[] _header;

	// Links to our more interesting children
	private ExMediaAtom mediaAtom;
    //the UNC or local path to a video file.
    private CString pathAtom;

	/**
	 * Set things up, and find our more interesting children
	 */
	protected ExVideoContainer(byte[] source, int start, int len) {
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

		// First child should be the ExMediaAtom
		if(_children[0] instanceof ExMediaAtom) {
			mediaAtom = (ExMediaAtom)_children[0];
		} else {
			logger.log(POILogger.ERROR, "First child record wasn't a ExMediaAtom, was of type " + _children[0].getRecordType());
		}
        if(_children[1] instanceof CString) {
            pathAtom = (CString)_children[1];
        } else {
            logger.log(POILogger.ERROR, "Second child record wasn't a CString, was of type " + _children[1].getRecordType());
        }
	}

	/**
	 * Create a new ExVideoContainer, with blank fields
	 */
	public ExVideoContainer() {
        // Setup our header block
		_header = new byte[8];
		_header[0] = 0x0f; // We are a container record
		LittleEndian.putShort(_header, 2, (short)getRecordType());

        _children = new Record[2];
		_children[0] = mediaAtom = new ExMediaAtom();
		_children[1] = pathAtom = new CString();
	}

	/**
	 * We are of type 4103
	 */
	public long getRecordType() { return RecordTypes.ExVideoContainer.typeID; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],getRecordType(),_children,out);
	}

    /**
     * Returns the ExMediaAtom of this link
     */
    public ExMediaAtom getExMediaAtom() { return mediaAtom; }

    /**
     * Returns the Path Atom (CString) of this link
     */
    public CString getPathAtom() { return pathAtom; }

}
