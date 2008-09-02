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
import org.apache.poi.util.POILogger;

/**
 * This class represents the data of a link in the document. 
 * @author Nick Burch
 */
public class ExHyperlink extends RecordContainer {
	private byte[] _header;
	private static long _type = 4055;
	
	// Links to our more interesting children
	private ExHyperlinkAtom linkAtom;
	private CString linkDetailsA;
	private CString linkDetailsB;
	
	/** 
	 * Returns the ExHyperlinkAtom of this link
	 */ 
	public ExHyperlinkAtom getExHyperlinkAtom() { return linkAtom; }
	
	/**
	 * Returns the URL of the link.
     *
     * @return the URL of the link
	 */
	public String getLinkURL() {
		return linkDetailsB == null ? null : linkDetailsB.getText();
	}

    /**
     * Returns the hyperlink's user-readable name
     *
     * @return the hyperlink's user-readable name
     */
    public String getLinkTitle() {
        return linkDetailsA == null ? null : linkDetailsA.getText();
    }

	/**
	 * Sets the URL of the link
	 * TODO: Figure out if we should always set both
	 */
	public void setLinkURL(String url) {
		if(linkDetailsB != null) {
			linkDetailsB.setText(url);
		}
	}
    public void setLinkTitle(String title) {
        if(linkDetailsA != null) {
            linkDetailsA.setText(title);
        }
    }

	/**
	 * Get the link details (field A)
	 */
	public String _getDetailsA() {
		return linkDetailsA == null ? null : linkDetailsA.getText();
	}
	/**
	 * Get the link details (field B)
	 */
	public String _getDetailsB() {
		return linkDetailsB == null ? null : linkDetailsB.getText();
	}

	/** 
	 * Set things up, and find our more interesting children
	 */
	protected ExHyperlink(byte[] source, int start, int len) {
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

		// First child should be the ExHyperlinkAtom
		if(_children[0] instanceof ExHyperlinkAtom) {
			linkAtom = (ExHyperlinkAtom)_children[0];
		} else {
			logger.log(POILogger.ERROR, "First child record wasn't a ExHyperlinkAtom, was of type " + _children[0].getRecordType());
		}

        for (int i = 1; i < _children.length; i++) {
            if (_children[i] instanceof CString){
                if ( linkDetailsA == null) linkDetailsA = (CString)_children[i];
                else linkDetailsB = (CString)_children[i];
            } else {
                logger.log(POILogger.ERROR, "Record after ExHyperlinkAtom wasn't a CString, was of type " + _children[1].getRecordType());
            }

        }
	}

	/**
	 * Create a new ExHyperlink, with blank fields
	 */
	public ExHyperlink() {
		_header = new byte[8];
		_children = new Record[3];
		
		// Setup our header block
		_header[0] = 0x0f; // We are a container record
		LittleEndian.putShort(_header, 2, (short)_type);
		
		// Setup our child records
		CString csa = new CString();
		CString csb = new CString();
		csa.setOptions(0x00);
		csb.setOptions(0x10);
		_children[0] = new ExHyperlinkAtom();
		_children[1] = csa;
		_children[2] = csb;
		findInterestingChildren();
	}

	/**
	 * We are of type 4055
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
