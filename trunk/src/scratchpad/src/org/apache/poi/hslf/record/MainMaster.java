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
import java.util.ArrayList;

/**
 * Master slide
 *
 * @author Yegor Kozlov
 */
public final class MainMaster extends SheetContainer {
	private byte[] _header;
	private static long _type = 1016;

	// Links to our more interesting children
	private SlideAtom slideAtom;
	private PPDrawing ppDrawing;
	private TxMasterStyleAtom[] txmasters;
	private ColorSchemeAtom[] clrscheme;
	private ColorSchemeAtom _colorScheme;

	/**
	 * Returns the SlideAtom of this Slide
	 */
	public SlideAtom getSlideAtom() { return slideAtom; }

	/**
	 * Returns the PPDrawing of this Slide, which has all the
	 *  interesting data in it
	 */
	public PPDrawing getPPDrawing() { return ppDrawing; }

	public TxMasterStyleAtom[] getTxMasterStyleAtoms() { return txmasters; }

	public ColorSchemeAtom[] getColorSchemeAtoms() { return clrscheme; }

	/**
	 * Set things up, and find our more interesting children
	 */
	protected MainMaster(byte[] source, int start, int len) {
		// Grab the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);

		ArrayList<TxMasterStyleAtom> tx = new ArrayList<TxMasterStyleAtom>();
		ArrayList<ColorSchemeAtom> clr = new ArrayList<ColorSchemeAtom>();
		// Find the interesting ones in there
		for(int i=0; i<_children.length; i++) {
			if(_children[i] instanceof SlideAtom) {
				slideAtom = (SlideAtom)_children[i];
			} else if(_children[i] instanceof PPDrawing) {
				ppDrawing = (PPDrawing)_children[i];
			} else if(_children[i] instanceof TxMasterStyleAtom) {
				tx.add( (TxMasterStyleAtom)_children[i] );
			} else if(_children[i] instanceof ColorSchemeAtom) {
				clr.add( (ColorSchemeAtom)_children[i] );
			}

			if(ppDrawing != null && _children[i] instanceof ColorSchemeAtom) {
				_colorScheme = (ColorSchemeAtom)_children[i];
			}

		}
		txmasters = tx.toArray(new TxMasterStyleAtom[tx.size()]);
		clrscheme = clr.toArray(new ColorSchemeAtom[clr.size()]);
	}

	/**
	 * We are of type 1016
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],_type,_children,out);
	}

	public ColorSchemeAtom getColorScheme(){
		return _colorScheme;
	}
}
