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

import org.apache.poi.util.LittleEndian;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * These are tricky beasts. They contain the text of potentially
 *  many (normal) slides. They are made up of several sets of
 *  - SlidePersistAtom
 *  - TextHeaderAtom
 *  - TextBytesAtom / TextCharsAtom
 *  - StyleTextPropAtom (optional)
 *  - TextSpecInfoAtom (optional)
 *  - InteractiveInfo (optional)
 *  - TxInteractiveInfoAtom (optional)
 * and then the next SlidePersistAtom.
 *
 * Eventually, Slides will find the blocks that interest them from all
 *  the SlideListWithText entries, and refere to them
 *
 * For now, we scan through looking for interesting bits, then creating
 *  the helpful Sheet from model for them
 *
 * @author Nick Burch
 */

// For now, pretend to be an atom
public final class SlideListWithText extends RecordContainer {

	/**
	 * Instance filed of the record header indicates that this SlideListWithText stores
	 * references to slides
	 */
	public static final int SLIDES = 0;
	/**
	 * Instance filed of the record header indicates that this SlideListWithText stores
	 * references to master slides
	 */
	public static final int MASTER = 1;
	/**
	 * Instance filed of the record header indicates that this SlideListWithText stores
	 * references to notes
	 */
	public static final int NOTES = 2;

	private byte[] _header;
	private static long _type = 4080;

	private SlideAtomsSet[] slideAtomsSets;

	/**
	 * Create a new holder for slide records
	 */
	protected SlideListWithText(byte[] source, int start, int len) {
		// Grab the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);

		// Group our children together into SlideAtomsSets
		// That way, model layer code can just grab the sets to use,
		//  without having to try to match the children together
		Vector<SlideAtomsSet> sets = new Vector<SlideAtomsSet>();
		for(int i=0; i<_children.length; i++) {
			if(_children[i] instanceof SlidePersistAtom) {
				// Find where the next SlidePersistAtom is
				int endPos = i+1;
				while(endPos < _children.length && !(_children[endPos] instanceof SlidePersistAtom)) {
					endPos += 1;
				}

				int clen = endPos - i - 1;
				boolean emptySet = false;
				if(clen == 0) { emptySet = true; }

				// Create a SlideAtomsSets, not caring if they're empty
				//if(emptySet) { continue; }
				Record[] spaChildren = new Record[clen];
				System.arraycopy(_children,i+1,spaChildren,0,clen);
				SlideAtomsSet set = new SlideAtomsSet((SlidePersistAtom)_children[i],spaChildren);
				sets.add(set);

				// Wind on
				i += clen;
			}
		}

		// Turn the vector into an array
		slideAtomsSets = sets.toArray( new SlideAtomsSet[sets.size()] );
	}

	/**
	 * Create a new, empty, SlideListWithText
	 */
	public SlideListWithText(){
		_header = new byte[8];
		LittleEndian.putUShort(_header, 0, 15);
		LittleEndian.putUShort(_header, 2, (int)_type);
		LittleEndian.putInt(_header, 4, 0);

		// We have no children to start with
		_children = new Record[0];
		slideAtomsSets = new SlideAtomsSet[0];
	}

	/**
	 * Add a new SlidePersistAtom, to the end of the current list,
	 *  and update the internal list of SlidePersistAtoms
	 * @param spa
	 */
	public void addSlidePersistAtom(SlidePersistAtom spa) {
		// Add the new SlidePersistAtom at the end
		appendChildRecord(spa);

		SlideAtomsSet newSAS = new SlideAtomsSet(spa, new Record[0]);

		// Update our SlideAtomsSets with this
		SlideAtomsSet[] sas = new SlideAtomsSet[slideAtomsSets.length+1];
		System.arraycopy(slideAtomsSets, 0, sas, 0, slideAtomsSets.length);
		sas[sas.length-1] = newSAS;
		slideAtomsSets = sas;
	}

	public int getInstance(){
		return LittleEndian.getShort(_header, 0) >> 4;
	}

	public void setInstance(int inst){
		LittleEndian.putShort(_header, (short)((inst << 4) | 0xF));
	}

	/**
	 * Get access to the SlideAtomsSets of the children of this record
	 */
	public SlideAtomsSet[] getSlideAtomsSets() { return slideAtomsSets; }

	/**
	* Get access to the SlideAtomsSets of the children of this record
	*/
	public void setSlideAtomsSets( SlideAtomsSet[] sas ) { slideAtomsSets = sas; }

	/**
	 * Return the value we were given at creation
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],_type,_children,out);
	}

	/**
	 * Inner class to wrap up a matching set of records that hold the
	 *  text for a given sheet. Contains the leading SlidePersistAtom,
	 *  and all of the records until the next SlidePersistAtom. This
	 *  includes sets of TextHeaderAtom and TextBytesAtom/TextCharsAtom,
	 *  along with some others.
	 */
	public class SlideAtomsSet {
		private SlidePersistAtom slidePersistAtom;
		private Record[] slideRecords;

		/** Get the SlidePersistAtom, which gives details on the Slide this text is associated with */
		public SlidePersistAtom getSlidePersistAtom() { return slidePersistAtom; }
		/** Get the Text related records for this slide */
		public Record[] getSlideRecords() { return slideRecords; }

		/** Create one to hold the Records for one Slide's text */
		public SlideAtomsSet(SlidePersistAtom s, Record[] r) {
			slidePersistAtom = s;
			slideRecords = r;
		}
	}
}
