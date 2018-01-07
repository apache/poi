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

/**
 * Environment, which contains lots of settings for the document.
 *
 * @author Nick Burch
 */

public final class Environment extends PositionDependentRecordContainer
{
	private byte[] _header;
	private static long _type = 1010;

	// Links to our more interesting children
	private FontCollection fontCollection;
    //master style for text with type=TextHeaderAtom.OTHER_TYPE
    private TxMasterStyleAtom txmaster;

	/**
	 * Returns the FontCollection of this Environment
	 */
	public FontCollection getFontCollection() { return fontCollection; }


	/**
	 * Set things up, and find our more interesting children
	 */
	protected Environment(byte[] source, int start, int len) {
		// Grab the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);

		// Find our FontCollection record
		for(int i=0; i<_children.length; i++) {
			if(_children[i] instanceof FontCollection) {
				fontCollection = (FontCollection)_children[i];
			} else if (_children[i] instanceof TxMasterStyleAtom){
                txmaster = (TxMasterStyleAtom)_children[i];
            }
		}

		if(fontCollection == null) {
			throw new IllegalStateException("Environment didn't contain a FontCollection record!");
		}
	}

    public TxMasterStyleAtom getTxMasterStyleAtom(){
        return txmaster;
    }

	/**
	 * We are of type 1010
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
